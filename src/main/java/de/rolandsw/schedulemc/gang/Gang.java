package de.rolandsw.schedulemc.gang;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repraesentiert eine Gang mit Mitgliedern, Level, Perks und Territorien.
 *
 * Thread-Safety: Alle Collections sind thread-safe.
 */
public class Gang {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final UUID gangId;
    private volatile String name;
    private volatile String tag; // 3-4 Buchstaben
    private volatile int gangLevel;
    private volatile int gangXP;
    private volatile int gangBalance;
    private volatile ChatFormatting color;
    private volatile long foundedTimestamp;

    private final ConcurrentHashMap<UUID, GangMemberData> members = new ConcurrentHashMap<>();
    private final Set<String> unlockedPerks = ConcurrentHashMap.newKeySet();
    private final Set<Long> territories = ConcurrentHashMap.newKeySet();

    // Pending invites (playerUUID -> expiry timestamp)
    private final ConcurrentHashMap<UUID, Long> pendingInvites = new ConcurrentHashMap<>();

    /**
     * Erstellt eine neue Gang.
     */
    public Gang(UUID gangId, String name, String tag, UUID founderUUID, ChatFormatting color) {
        this.gangId = gangId;
        this.name = name;
        this.tag = tag.toUpperCase();
        this.gangLevel = 1;
        this.gangXP = 0;
        this.gangBalance = 0;
        this.color = color;
        this.foundedTimestamp = System.currentTimeMillis();

        // Gruender ist Boss
        members.put(founderUUID, new GangMemberData(founderUUID, GangRank.BOSS));
    }

    /**
     * Deserialisierung-Konstruktor.
     */
    public Gang(UUID gangId, String name, String tag, int gangLevel, int gangXP,
                int gangBalance, ChatFormatting color, long foundedTimestamp) {
        this.gangId = gangId;
        this.name = name;
        this.tag = tag;
        this.gangLevel = gangLevel;
        this.gangXP = gangXP;
        this.gangBalance = gangBalance;
        this.color = color;
        this.foundedTimestamp = foundedTimestamp;
    }

    // ═══════════════════════════════════════════════════════════
    // XP & LEVEL
    // ═══════════════════════════════════════════════════════════

    /**
     * Fuegt Gang-XP hinzu und prueft auf Level-Up.
     * @return true wenn Level-Up stattfand
     */
    public boolean addXP(int xp, UUID contributorUUID) {
        if (xp <= 0) return false;
        if (gangLevel >= GangLevelRequirements.MAX_LEVEL) return false;

        gangXP += xp;

        // Contributor-XP tracken
        GangMemberData member = members.get(contributorUUID);
        if (member != null) {
            member.addContributedXP(xp);
        }

        int newLevel = GangLevelRequirements.getLevelForXP(gangXP);
        if (newLevel > gangLevel) {
            int oldLevel = gangLevel;
            gangLevel = newLevel;
            LOGGER.info("Gang '{}' leveled up: {} -> {}", name, oldLevel, newLevel);
            return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════
    // MITGLIEDER
    // ═══════════════════════════════════════════════════════════

    public boolean addMember(UUID playerUUID, GangRank rank) {
        if (members.size() >= getMaxMembers()) return false;
        if (members.containsKey(playerUUID)) return false;
        members.put(playerUUID, new GangMemberData(playerUUID, rank));
        return true;
    }

    public boolean removeMember(UUID playerUUID) {
        return members.remove(playerUUID) != null;
    }

    public boolean isMember(UUID playerUUID) {
        return members.containsKey(playerUUID);
    }

    public GangMemberData getMember(UUID playerUUID) {
        return members.get(playerUUID);
    }

    public GangRank getRank(UUID playerUUID) {
        GangMemberData member = members.get(playerUUID);
        return member != null ? member.getRank() : null;
    }

    public boolean setRank(UUID playerUUID, GangRank rank) {
        GangMemberData member = members.get(playerUUID);
        if (member == null) return false;
        member.setRank(rank);
        return true;
    }

    public UUID getBoss() {
        for (Map.Entry<UUID, GangMemberData> entry : members.entrySet()) {
            if (entry.getValue().getRank() == GangRank.BOSS) {
                return entry.getKey();
            }
        }
        return null;
    }

    public int getMemberCount() {
        return members.size();
    }

    public int getMaxMembers() {
        return GangLevelRequirements.getMaxMembers(gangLevel);
    }

    public Map<UUID, GangMemberData> getMembers() {
        return Collections.unmodifiableMap(members);
    }

    // ═══════════════════════════════════════════════════════════
    // INVITES
    // ═══════════════════════════════════════════════════════════

    private static final long INVITE_EXPIRY_MS = 5 * 60 * 1000; // 5 Minuten

    public boolean invite(UUID playerUUID) {
        if (isMember(playerUUID)) return false;
        if (members.size() >= getMaxMembers()) return false;
        pendingInvites.put(playerUUID, System.currentTimeMillis() + INVITE_EXPIRY_MS);
        return true;
    }

    public boolean hasValidInvite(UUID playerUUID) {
        Long expiry = pendingInvites.get(playerUUID);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            pendingInvites.remove(playerUUID);
            return false;
        }
        return true;
    }

    public void removeInvite(UUID playerUUID) {
        pendingInvites.remove(playerUUID);
    }

    public void cleanExpiredInvites() {
        long now = System.currentTimeMillis();
        pendingInvites.entrySet().removeIf(e -> now > e.getValue());
    }

    // ═══════════════════════════════════════════════════════════
    // PERKS
    // ═══════════════════════════════════════════════════════════

    public boolean unlockPerk(GangPerk perk) {
        if (!perk.canUnlock(gangLevel)) return false;
        if (unlockedPerks.contains(perk.name())) return false;

        int usedPoints = unlockedPerks.size();
        int availablePoints = GangLevelRequirements.getAvailablePerkPoints(gangLevel);
        if (usedPoints >= availablePoints) return false;

        unlockedPerks.add(perk.name());
        return true;
    }

    public boolean hasPerk(GangPerk perk) {
        return unlockedPerks.contains(perk.name());
    }

    public Set<String> getUnlockedPerks() {
        return Collections.unmodifiableSet(unlockedPerks);
    }

    public int getUsedPerkPoints() {
        return unlockedPerks.size();
    }

    public int getAvailablePerkPoints() {
        return GangLevelRequirements.getAvailablePerkPoints(gangLevel) - unlockedPerks.size();
    }

    // ═══════════════════════════════════════════════════════════
    // TERRITORY
    // ═══════════════════════════════════════════════════════════

    public boolean addTerritory(long chunkKey) {
        if (territories.size() >= getMaxTerritory()) return false;
        return territories.add(chunkKey);
    }

    public boolean removeTerritory(long chunkKey) {
        return territories.remove(chunkKey);
    }

    public boolean ownsTerritory(long chunkKey) {
        return territories.contains(chunkKey);
    }

    public int getTerritoryCount() {
        return territories.size();
    }

    public int getMaxTerritory() {
        int base = GangLevelRequirements.getBaseMaxTerritory(gangLevel);
        if (hasPerk(GangPerk.TERRITORY_EMPIRE)) return Integer.MAX_VALUE;
        if (hasPerk(GangPerk.TERRITORY_STRONGHOLD)) return Math.max(base, 25);
        if (hasPerk(GangPerk.TERRITORY_DOMINANCE)) return Math.max(base, 16);
        if (hasPerk(GangPerk.TERRITORY_EXPAND)) return Math.max(base, 9);
        return base;
    }

    public Set<Long> getTerritories() {
        return Collections.unmodifiableSet(territories);
    }

    // ═══════════════════════════════════════════════════════════
    // FINANZEN
    // ═══════════════════════════════════════════════════════════

    public int getGangBalance() { return gangBalance; }

    public boolean deposit(int amount) {
        if (amount <= 0) return false;
        gangBalance += amount;
        return true;
    }

    public boolean withdraw(int amount) {
        if (amount <= 0 || amount > gangBalance) return false;
        gangBalance -= amount;
        return true;
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public UUID getGangId() { return gangId; }
    public String getName() { return name; }
    public String getTag() { return tag; }
    public int getGangLevel() { return gangLevel; }
    public int getGangXP() { return gangXP; }
    public ChatFormatting getColor() { return color; }
    public long getFoundedTimestamp() { return foundedTimestamp; }

    public void setName(String name) { this.name = name; }
    public void setTag(String tag) { this.tag = tag.toUpperCase(); }
    public void setColor(ChatFormatting color) { this.color = color; }

    public GangReputation getReputation() {
        return GangReputation.getForLevel(gangLevel);
    }

    public String getStars() {
        return GangReputation.getLevelStars(gangLevel);
    }

    /**
     * Formatierter Gang-Tag fuer Anzeige.
     * z.B. "§c[MAFIA §4★★★§c]"
     */
    public String getFormattedTag() {
        String stars = getStars();
        String colorStr = "\u00A7" + color.getChar();
        if (stars.isEmpty()) {
            return colorStr + "[" + tag + "]";
        }
        return colorStr + "[" + tag + " " + stars + colorStr + "]";
    }

    /**
     * Gang-Fortschritt zum naechsten Level.
     */
    public double getProgress() {
        return GangLevelRequirements.getProgress(gangLevel, gangXP);
    }

    public int getXPToNextLevel() {
        return GangLevelRequirements.getXPToNextLevel(gangLevel, gangXP);
    }

    /**
     * Interne Serialisierungsdaten fuer Mitglieder.
     */
    public void addMemberDirect(UUID uuid, GangMemberData data) {
        members.put(uuid, data);
    }

    public void addUnlockedPerkDirect(String perkName) {
        unlockedPerks.add(perkName);
    }

    public void addTerritoryDirect(long chunkKey) {
        territories.add(chunkKey);
    }
}
