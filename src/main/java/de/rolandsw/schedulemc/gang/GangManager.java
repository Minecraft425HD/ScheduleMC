package de.rolandsw.schedulemc.gang;

import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.level.ProducerLevel;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zentraler Manager fuer das Gang-System.
 *
 * Verwaltet:
 * - Alle Gangs und ihre Mitglieder
 * - Player-zu-Gang Mapping
 * - Gang-XP und Level-Ups
 * - Persistenz via AbstractPersistenceManager
 *
 * Thread-Safety: Alle Methoden sind thread-safe.
 */
public class GangManager extends AbstractPersistenceManager<Map<String, GangManager.SavedGangData>> {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Singleton
    private static volatile GangManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    // Daten
    private final ConcurrentHashMap<UUID, Gang> gangs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, UUID> playerToGang = new ConcurrentHashMap<>(); // playerUUID -> gangId

    @Nullable
    private MinecraftServer server;

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private GangManager(MinecraftServer server) {
        super(
            new File(server.getServerDirectory(), "config/schedulemc_gangs.json"),
            GsonHelper.get()
        );
        this.server = server;
        load();
    }

    @Nullable
    public static GangManager getInstance() {
        return instance;
    }

    public static GangManager getInstance(MinecraftServer server) {
        GangManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new GangManager(server);
                }
            }
        }
        return result;
    }

    public static void resetInstance() {
        synchronized (INSTANCE_LOCK) {
            if (instance != null) {
                instance.save();
            }
            instance = null;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GANG CRUD
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt eine neue Gang.
     * @return Die neue Gang, oder null wenn Name/Tag schon vergeben oder Spieler schon in einer Gang
     */
    @Nullable
    public Gang createGang(String name, String tag, UUID founderUUID, ChatFormatting color) {
        // Pruefe ob Spieler schon in einer Gang ist
        if (playerToGang.containsKey(founderUUID)) return null;

        // Pruefe ob Name oder Tag schon vergeben
        String lowerName = name.toLowerCase();
        String upperTag = tag.toUpperCase();
        for (Gang g : gangs.values()) {
            if (g.getName().toLowerCase().equals(lowerName)) return null;
            if (g.getTag().equals(upperTag)) return null;
        }

        // Validierung
        if (name.length() < 3 || name.length() > 20) return null;
        if (tag.length() < 2 || tag.length() > 5) return null;

        UUID gangId = UUID.randomUUID();
        Gang gang = new Gang(gangId, name, tag, founderUUID, color);
        gangs.put(gangId, gang);
        playerToGang.put(founderUUID, gangId);
        markDirty();

        LOGGER.info("Gang created: '{}' [{}] by {}", name, tag, founderUUID);
        return gang;
    }

    /**
     * Loest eine Gang auf.
     */
    public boolean disbandGang(UUID gangId, UUID requesterUUID) {
        Gang gang = gangs.get(gangId);
        if (gang == null) return false;

        GangRank rank = gang.getRank(requesterUUID);
        if (rank == null || !rank.canDisband()) return false;

        // Alle Mitglieder entfernen
        for (UUID memberUUID : gang.getMembers().keySet()) {
            playerToGang.remove(memberUUID);
        }

        gangs.remove(gangId);
        markDirty();

        LOGGER.info("Gang disbanded: '{}' [{}] by {}", gang.getName(), gang.getTag(), requesterUUID);
        return true;
    }

    // ═══════════════════════════════════════════════════════════
    // MITGLIEDER
    // ═══════════════════════════════════════════════════════════

    /**
     * Fuegt einen Spieler zu einer Gang hinzu.
     */
    public boolean joinGang(UUID playerUUID, UUID gangId) {
        if (playerToGang.containsKey(playerUUID)) return false;

        Gang gang = gangs.get(gangId);
        if (gang == null) return false;
        if (!gang.hasValidInvite(playerUUID)) return false;

        boolean added = gang.addMember(playerUUID, GangRank.RECRUIT);
        if (added) {
            playerToGang.put(playerUUID, gangId);
            gang.removeInvite(playerUUID);
            markDirty();
            LOGGER.info("Player {} joined gang '{}'", playerUUID, gang.getName());

            // Mission-Tracking: Mitglied rekrutiert
            de.rolandsw.schedulemc.gang.mission.GangMissionManager mm = de.rolandsw.schedulemc.gang.mission.GangMissionManager.getInstance();
            if (mm != null) mm.onMemberRecruited(gangId);
        }
        return added;
    }

    /**
     * Entfernt einen Spieler aus seiner Gang.
     */
    public boolean leaveGang(UUID playerUUID) {
        UUID gangId = playerToGang.get(playerUUID);
        if (gangId == null) return false;

        Gang gang = gangs.get(gangId);
        if (gang == null) {
            playerToGang.remove(playerUUID);
            return false;
        }

        // Boss kann nicht einfach gehen (muss Gang aufloesen oder Boss uebertragen)
        GangRank rank = gang.getRank(playerUUID);
        if (rank == GangRank.BOSS && gang.getMemberCount() > 1) return false;

        gang.removeMember(playerUUID);
        playerToGang.remove(playerUUID);

        // Wenn die Gang jetzt leer ist, loeschen
        if (gang.getMemberCount() == 0) {
            gangs.remove(gangId);
            LOGGER.info("Gang '{}' auto-disbanded (no members left)", gang.getName());
        }

        markDirty();
        return true;
    }

    /**
     * Kickt einen Spieler aus einer Gang.
     */
    public boolean kickMember(UUID kickerUUID, UUID targetUUID) {
        UUID gangId = playerToGang.get(kickerUUID);
        if (gangId == null) return false;

        Gang gang = gangs.get(gangId);
        if (gang == null) return false;

        GangRank kickerRank = gang.getRank(kickerUUID);
        GangRank targetRank = gang.getRank(targetUUID);
        if (kickerRank == null || targetRank == null) return false;
        if (!kickerRank.canKickRank(targetRank)) return false;

        gang.removeMember(targetUUID);
        playerToGang.remove(targetUUID);
        markDirty();

        LOGGER.info("Player {} kicked {} from gang '{}'", kickerUUID, targetUUID, gang.getName());
        return true;
    }

    /**
     * Befoerdert ein Mitglied.
     */
    public boolean promoteMember(UUID promoterUUID, UUID targetUUID, GangRank newRank) {
        UUID gangId = playerToGang.get(promoterUUID);
        if (gangId == null) return false;

        Gang gang = gangs.get(gangId);
        if (gang == null) return false;

        GangRank promoterRank = gang.getRank(promoterUUID);
        if (promoterRank == null || !promoterRank.canPromoteTo(newRank)) return false;

        // BOSS-Transfer
        if (newRank == GangRank.BOSS) {
            if (promoterRank != GangRank.BOSS) return false;
            gang.setRank(promoterUUID, GangRank.UNDERBOSS);
        }

        boolean result = gang.setRank(targetUUID, newRank);
        if (result) {
            markDirty();

            // Mission-Tracking: Mitglied befoerdert
            UUID gid = playerToGang.get(promoterUUID);
            if (gid != null) {
                de.rolandsw.schedulemc.gang.mission.GangMissionManager mm = de.rolandsw.schedulemc.gang.mission.GangMissionManager.getInstance();
                if (mm != null) mm.onMemberPromoted(gid);
            }
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════
    // GANG XP
    // ═══════════════════════════════════════════════════════════

    /**
     * Fuegt Gang-XP hinzu. Gibt true zurueck bei Level-Up.
     */
    public boolean awardGangXP(UUID playerUUID, GangXPSource source, int amount) {
        UUID gangId = playerToGang.get(playerUUID);
        if (gangId == null) return false;

        Gang gang = gangs.get(gangId);
        if (gang == null) return false;

        int xp = source.calculateXP(amount);
        boolean leveledUp = gang.addXP(xp, playerUUID);
        markDirty();

        // Mission-Tracking: Gang-XP vergeben
        de.rolandsw.schedulemc.gang.mission.GangMissionManager mm = de.rolandsw.schedulemc.gang.mission.GangMissionManager.getInstance();
        if (mm != null) mm.onGangXPAwarded(gangId, xp);

        if (leveledUp) {
            onGangLevelUp(gang);
        }
        return leveledUp;
    }

    private void onGangLevelUp(Gang gang) {
        LOGGER.info("Gang '{}' leveled up to {}", gang.getName(), gang.getGangLevel());

        // Benachrichtige alle Online-Mitglieder
        if (server != null) {
            for (UUID memberUUID : gang.getMembers().keySet()) {
                ServerPlayer player = server.getPlayerList().getPlayer(memberUUID);
                if (player != null) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "\u00A76\u00A7l\u2605 GANG LEVEL UP! \u2605 \u00A7f" + gang.getName() +
                        " \u00A77ist jetzt \u00A76Level " + gang.getGangLevel() + "\u00A77!"
                    ));
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // WOCHENBEITRAG
    // ═══════════════════════════════════════════════════════════

    /**
     * Sammelt faellige Wochenbeitraege von allen Online-Mitgliedern aller Gangs.
     * Wird periodisch aufgerufen (z.B. alle 60 Sekunden).
     *
     * Vorschlag 2 Staffelung:
     * RECRUIT: 100%, MEMBER: 50%, UNDERBOSS: 10%, BOSS: 0%
     *
     * Bei 3 verpassten Zahlungen: Auto-Kick.
     */
    public void collectWeeklyFees(MinecraftServer srv) {
        if (srv == null) return;

        for (Gang gang : gangs.values()) {
            int baseFee = gang.getWeeklyFee();
            if (baseFee <= 0) continue;

            List<UUID> toKick = new ArrayList<>();

            for (Map.Entry<UUID, GangMemberData> entry : gang.getMembers().entrySet()) {
                UUID memberUUID = entry.getKey();
                GangMemberData memberData = entry.getValue();

                if (memberData.getRank() == GangRank.BOSS) continue;
                if (!memberData.isFeeDue()) continue;

                int fee = memberData.calculateFee(baseFee);
                if (fee <= 0) continue;

                ServerPlayer player = srv.getPlayerList().getPlayer(memberUUID);
                if (player == null) continue;

                double balance = EconomyManager.getBalance(memberUUID);
                if (balance >= fee) {
                    EconomyManager.withdraw(memberUUID, fee);
                    gang.deposit(fee);
                    memberData.resetFeePaid();
                    markDirty();

                    // Mission-Tracking: Beitraege kassiert
                    de.rolandsw.schedulemc.gang.mission.GangMissionManager mmFee = de.rolandsw.schedulemc.gang.mission.GangMissionManager.getInstance();
                    if (mmFee != null) mmFee.onFeesCollected(gang.getGangId(), fee);

                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "\u00A76[Gang] \u00A77Wochenbeitrag von \u00A7c" + fee +
                            "\u20AC \u00A77fuer \u00A7f" + gang.getName() + " \u00A77eingezogen."));
                } else {
                    memberData.incrementMissedFeePayments();
                    markDirty();

                    if (memberData.getMissedFeePayments() >= 3) {
                        toKick.add(memberUUID);
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "\u00A7c[Gang] \u00A77Du wurdest aus \u00A7f" + gang.getName() +
                                " \u00A77entfernt (3x Beitrag nicht gezahlt)."));
                    } else {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "\u00A7c[Gang] \u00A77Nicht genug Geld fuer Wochenbeitrag (" +
                                fee + "\u20AC). Verpasst: " + memberData.getMissedFeePayments() + "/3"));
                    }
                }
            }

            for (UUID kickUUID : toKick) {
                gang.removeMember(kickUUID);
                playerToGang.remove(kickUUID);
                LOGGER.info("Auto-kicked {} from gang '{}' (missed 3 fee payments)", kickUUID, gang.getName());
            }
            if (!toKick.isEmpty()) {
                markDirty();
                if (gang.getMemberCount() == 0) {
                    gangs.remove(gang.getGangId());
                    LOGGER.info("Gang '{}' auto-disbanded (no members after fee kicks)", gang.getName());
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    @Nullable
    public Gang getGang(UUID gangId) {
        return gangs.get(gangId);
    }

    @Nullable
    public Gang getPlayerGang(UUID playerUUID) {
        UUID gangId = playerToGang.get(playerUUID);
        return gangId != null ? gangs.get(gangId) : null;
    }

    @Nullable
    public UUID getPlayerGangId(UUID playerUUID) {
        return playerToGang.get(playerUUID);
    }

    public boolean isInGang(UUID playerUUID) {
        return playerToGang.containsKey(playerUUID);
    }

    public Collection<Gang> getAllGangs() {
        return Collections.unmodifiableCollection(gangs.values());
    }

    public int getGangCount() {
        return gangs.size();
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<String, SavedGangData>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<String, SavedGangData> data) {
        gangs.clear();
        playerToGang.clear();

        for (Map.Entry<String, SavedGangData> entry : data.entrySet()) {
            try {
                UUID gangId = UUID.fromString(entry.getKey());
                SavedGangData saved = entry.getValue();

                ChatFormatting cf = ChatFormatting.getByName(saved.color);
                if (cf == null) cf = ChatFormatting.WHITE;

                Gang gang = new Gang(gangId, saved.name, saved.tag, saved.gangLevel,
                        saved.gangXP, saved.gangBalance, cf, saved.foundedTimestamp, saved.weeklyFee);

                // Mitglieder laden
                if (saved.members != null) {
                    for (Map.Entry<String, SavedMemberData> memberEntry : saved.members.entrySet()) {
                        try {
                            UUID memberUUID = UUID.fromString(memberEntry.getKey());
                            SavedMemberData sm = memberEntry.getValue();
                            GangRank rank;
                            try {
                                rank = GangRank.valueOf(sm.rank);
                            } catch (IllegalArgumentException e) {
                                rank = GangRank.RECRUIT;
                            }
                            gang.addMemberDirect(memberUUID,
                                    new GangMemberData(memberUUID, rank, sm.contributedXP,
                                            sm.joinTimestamp, sm.lastFeePaid, sm.missedFeePayments));
                            playerToGang.put(memberUUID, gangId);
                        } catch (IllegalArgumentException e) {
                            LOGGER.error("Invalid member UUID: {}", memberEntry.getKey());
                        }
                    }
                }

                // Perks laden
                if (saved.unlockedPerks != null) {
                    for (String perkName : saved.unlockedPerks) {
                        gang.addUnlockedPerkDirect(perkName);
                    }
                }

                // Territories laden
                if (saved.territories != null) {
                    for (Long chunkKey : saved.territories) {
                        gang.addTerritoryDirect(chunkKey);
                    }
                }

                gangs.put(gangId, gang);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Invalid gang UUID: {}", entry.getKey());
            }
        }

        LOGGER.info("GangManager loaded: {} gangs, {} players in gangs", gangs.size(), playerToGang.size());
    }

    @Override
    protected Map<String, SavedGangData> getCurrentData() {
        Map<String, SavedGangData> data = new HashMap<>();

        for (Map.Entry<UUID, Gang> entry : gangs.entrySet()) {
            Gang gang = entry.getValue();
            SavedGangData saved = new SavedGangData();
            saved.name = gang.getName();
            saved.tag = gang.getTag();
            saved.gangLevel = gang.getGangLevel();
            saved.gangXP = gang.getGangXP();
            saved.gangBalance = gang.getGangBalance();
            saved.color = gang.getColor().getName();
            saved.foundedTimestamp = gang.getFoundedTimestamp();
            saved.weeklyFee = gang.getWeeklyFee();

            // Mitglieder
            saved.members = new HashMap<>();
            for (Map.Entry<UUID, GangMemberData> memberEntry : gang.getMembers().entrySet()) {
                SavedMemberData sm = new SavedMemberData();
                GangMemberData md = memberEntry.getValue();
                sm.rank = md.getRank().name();
                sm.contributedXP = md.getContributedXP();
                sm.joinTimestamp = md.getJoinTimestamp();
                sm.lastFeePaid = md.getLastFeePaid();
                sm.missedFeePayments = md.getMissedFeePayments();
                saved.members.put(memberEntry.getKey().toString(), sm);
            }

            // Perks
            saved.unlockedPerks = new ArrayList<>(gang.getUnlockedPerks());

            // Territories
            saved.territories = new ArrayList<>(gang.getTerritories());

            data.put(entry.getKey().toString(), saved);
        }

        return data;
    }

    @Override
    protected String getComponentName() {
        return "GangManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("%d Gangs, %d Spieler", gangs.size(), playerToGang.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        gangs.clear();
        playerToGang.clear();
    }

    @Override
    public int getPriority() {
        return 3; // Hoehere Prioritaet als Default
    }

    // ═══════════════════════════════════════════════════════════
    // SERIALISIERBARE DATENSTRUKTUREN
    // ═══════════════════════════════════════════════════════════

    static class SavedGangData {
        String name;
        String tag;
        int gangLevel;
        int gangXP;
        int gangBalance;
        String color;
        long foundedTimestamp;
        int weeklyFee;
        Map<String, SavedMemberData> members;
        List<String> unlockedPerks;
        List<Long> territories;
    }

    static class SavedMemberData {
        String rank;
        int contributedXP;
        long joinTimestamp;
        long lastFeePaid;
        int missedFeePayments;
    }
}
