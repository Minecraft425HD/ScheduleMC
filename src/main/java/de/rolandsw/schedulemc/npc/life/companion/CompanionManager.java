package de.rolandsw.schedulemc.npc.life.companion;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CompanionManager - Verwaltet alle Begleiter im System mit JSON-Persistenz
 *
 * Verantwortlich für:
 * - Begleiter-Rekrutierung
 * - Spieler-Begleiter-Zuordnung
 * - Begleiter-Persistenz
 * - Beschwören und Entlassen
 */
public class CompanionManager extends AbstractPersistenceManager<CompanionManager.CompanionManagerData> {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private static volatile CompanionManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    @Nullable
    public static CompanionManager getInstance() {
        return instance;
    }

    public static CompanionManager getInstance(MinecraftServer server) {
        CompanionManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new CompanionManager(server);
                }
            }
        }
        return result;
    }

    /**
     * Helper method for level-based access.
     * Note: Manager is server-wide, not per-level.
     */
    public static CompanionManager getManager(ServerLevel level) {
        return getInstance(level.getServer());
    }

    // ═══════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════

    /** Maximale Anzahl aktiver Begleiter pro Spieler */
    public static final int MAX_ACTIVE_COMPANIONS = 2;

    /** Maximale Anzahl rekrutierter Begleiter pro Spieler (inkl. inaktive) */
    public static final int MAX_TOTAL_COMPANIONS = 5;

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    private MinecraftServer server;

    /** Alle registrierten Begleiter: Companion UUID -> Data */
    private final Map<UUID, CompanionData> allCompanions = new ConcurrentHashMap<>();

    /** Spieler-Begleiter-Zuordnung: Player UUID -> List of Companion UUIDs */
    private final Map<UUID, List<UUID>> playerCompanions = new ConcurrentHashMap<>();

    /** Aktive Begleiter-Entities: Companion UUID -> Entity (TRANSIENT - nicht persistiert) */
    private final Map<UUID, CustomNPCEntity> activeCompanionEntities = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private CompanionManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("npc_life_companions.json").toFile(),
            GsonHelper.get()
        );
        this.server = server;
        load();
    }

    // ═══════════════════════════════════════════════════════════
    // RECRUITMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob ein Spieler einen Begleiter rekrutieren kann
     */
    public RecruitmentResult canRecruit(ServerPlayer player, CompanionType type, CustomNPCEntity npc) {
        UUID playerUUID = player.getUUID();

        // Prüfe maximale Anzahl
        List<UUID> companions = playerCompanions.getOrDefault(playerUUID, Collections.emptyList());
        if (companions.size() >= MAX_TOTAL_COMPANIONS) {
            return RecruitmentResult.TOO_MANY_COMPANIONS;
        }

        // Prüfe Reputation
        ServerLevel level = (ServerLevel) player.level();
        FactionManager factionManager = FactionManager.getInstance();
        if (factionManager != null) {
            Faction npcFaction = Faction.forNPCType(npc.getNpcType());
            int playerRep = factionManager.getReputation(playerUUID, npcFaction);

            if (playerRep < type.getRequiredReputation()) {
                return RecruitmentResult.INSUFFICIENT_REPUTATION;
            }
        }

        // Prüfe Geld
        long playerMoney = getPlayerMoney(player);
        if (playerMoney < type.getRecruitmentCost()) {
            return RecruitmentResult.INSUFFICIENT_FUNDS;
        }

        return RecruitmentResult.SUCCESS;
    }

    /**
     * Rekrutiert einen NPC als Begleiter
     */
    @Nullable
    public CompanionData recruit(ServerPlayer player, CompanionType type, CustomNPCEntity npc, String name) {
        RecruitmentResult result = canRecruit(player, type, npc);
        if (result != RecruitmentResult.SUCCESS) {
            return null;
        }

        UUID playerUUID = player.getUUID();

        // Kosten abziehen
        deductPlayerMoney(player, type.getRecruitmentCost());

        // Begleiter erstellen
        UUID companionUUID = UUID.randomUUID();
        CompanionData data = new CompanionData(companionUUID, type, name);
        data.setOwnerUUID(playerUUID);

        // Registrieren
        allCompanions.put(companionUUID, data);
        playerCompanions.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(companionUUID);

        markDirty();
        return data;
    }

    /**
     * Ergebnis einer Rekrutierungsanfrage
     */
    public enum RecruitmentResult {
        SUCCESS,
        TOO_MANY_COMPANIONS,
        INSUFFICIENT_REPUTATION,
        INSUFFICIENT_FUNDS,
        NPC_NOT_RECRUITABLE
    }

    // ═══════════════════════════════════════════════════════════
    // SUMMONING
    // ═══════════════════════════════════════════════════════════

    /**
     * Beschwört einen Begleiter
     */
    @Nullable
    public CustomNPCEntity summon(ServerPlayer player, UUID companionUUID) {
        CompanionData data = allCompanions.get(companionUUID);
        if (data == null) return null;

        // Prüfe Besitz
        if (!player.getUUID().equals(data.getOwnerUUID())) {
            return null;
        }

        // Prüfe ob beschwörbar
        if (!data.isSummonable()) {
            return null;
        }

        // Prüfe aktive Begleiter-Limit
        int activeCount = countActiveCompanions(player.getUUID());
        if (activeCount >= MAX_ACTIVE_COMPANIONS) {
            return null;
        }

        // Prüfe ob bereits beschworen
        if (activeCompanionEntities.containsKey(companionUUID)) {
            return activeCompanionEntities.get(companionUUID);
        }

        // Entity erstellen (dies würde normalerweise eine spezielle Companion-Entity sein)
        // Für jetzt verwenden wir CustomNPCEntity mit Companion-Daten
        // In der Praxis würde man hier die Entity spawnen

        // Placeholder - Entity-Erstellung müsste hier implementiert werden
        // CustomNPCEntity entity = createCompanionEntity(data, player);
        // activeCompanionEntities.put(companionUUID, entity);
        // return entity;

        return null;
    }

    /**
     * Entlässt (despawnt) einen aktiven Begleiter
     */
    public void dismiss(UUID companionUUID) {
        CustomNPCEntity entity = activeCompanionEntities.remove(companionUUID);
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }

    /**
     * Entlässt alle aktiven Begleiter eines Spielers
     */
    public void dismissAll(UUID playerUUID) {
        List<UUID> companions = playerCompanions.getOrDefault(playerUUID, Collections.emptyList());
        for (UUID companionUUID : companions) {
            dismiss(companionUUID);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // COMPANION MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Gibt einen Befehl an einen Begleiter
     */
    public boolean giveCommand(UUID companionUUID, CompanionData.CompanionCommand command) {
        CompanionData data = allCompanions.get(companionUUID);
        if (data == null) return false;

        boolean result = data.giveCommand(command);
        if (result) {
            markDirty();
        }
        return result;
    }

    /**
     * Entlässt einen Begleiter permanent (nicht nur despawn)
     */
    public void releaseCompanion(UUID playerUUID, UUID companionUUID) {
        // Erst despawnen
        dismiss(companionUUID);

        // Aus Listen entfernen
        allCompanions.remove(companionUUID);
        List<UUID> companions = playerCompanions.get(playerUUID);
        if (companions != null) {
            companions.remove(companionUUID);
        }

        markDirty();
    }

    /**
     * Überträgt einen Begleiter an einen anderen Spieler
     */
    public boolean transferCompanion(UUID fromPlayer, UUID toPlayer, UUID companionUUID) {
        CompanionData data = allCompanions.get(companionUUID);
        if (data == null) return false;

        // Prüfe Besitz
        if (!fromPlayer.equals(data.getOwnerUUID())) {
            return false;
        }

        // Prüfe ob Ziel-Spieler Platz hat
        List<UUID> toCompanions = playerCompanions.getOrDefault(toPlayer, Collections.emptyList());
        if (toCompanions.size() >= MAX_TOTAL_COMPANIONS) {
            return false;
        }

        // Erst despawnen
        dismiss(companionUUID);

        // Transfer
        data.setOwnerUUID(toPlayer);

        List<UUID> fromCompanions = playerCompanions.get(fromPlayer);
        if (fromCompanions != null) {
            fromCompanions.remove(companionUUID);
        }
        playerCompanions.computeIfAbsent(toPlayer, k -> new ArrayList<>()).add(companionUUID);

        // Loyalität sinkt bei Transfer
        data.addLoyalty(-20);

        markDirty();
        return true;
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Holt alle Begleiter eines Spielers
     */
    public List<CompanionData> getCompanions(UUID playerUUID) {
        List<UUID> companionUUIDs = playerCompanions.getOrDefault(playerUUID, Collections.emptyList());
        List<CompanionData> result = new ArrayList<>();
        for (UUID uuid : companionUUIDs) {
            CompanionData data = allCompanions.get(uuid);
            if (data != null) {
                result.add(data);
            }
        }
        return result;
    }

    /**
     * Holt einen spezifischen Begleiter
     */
    @Nullable
    public CompanionData getCompanion(UUID companionUUID) {
        return allCompanions.get(companionUUID);
    }

    /**
     * Holt die Entity eines aktiven Begleiters
     */
    @Nullable
    public CustomNPCEntity getActiveEntity(UUID companionUUID) {
        return activeCompanionEntities.get(companionUUID);
    }

    /**
     * Zählt aktive Begleiter eines Spielers
     */
    public int countActiveCompanions(UUID playerUUID) {
        int count = 0;
        List<UUID> companions = playerCompanions.getOrDefault(playerUUID, Collections.emptyList());
        for (UUID uuid : companions) {
            if (activeCompanionEntities.containsKey(uuid)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Prüft ob ein Begleiter aktiv ist
     */
    public boolean isActive(UUID companionUUID) {
        return activeCompanionEntities.containsKey(companionUUID);
    }

    // ═══════════════════════════════════════════════════════════
    // TICK
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird regelmäßig aufgerufen
     */
    public void tick() {
        // Alle Begleiter-Daten ticken
        for (CompanionData data : allCompanions.values()) {
            data.tick();
        }

        // Tote Entity-Referenzen entfernen
        activeCompanionEntities.entrySet().removeIf(entry -> {
            CustomNPCEntity entity = entry.getValue();
            if (entity == null || entity.isRemoved()) {
                CompanionData data = allCompanions.get(entry.getKey());
                if (data != null && data.isDead()) {
                    data.onIncapacitated();
                }
                return true;
            }
            return false;
        });
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    /**
     * Holt das Geld eines Spielers
     */
    private long getPlayerMoney(ServerPlayer player) {
        CompoundTag data = player.getPersistentData().getCompound("ScheduleMC");
        return data.getLong("wallet");
    }

    /**
     * Zieht Geld vom Spieler ab
     */
    private void deductPlayerMoney(ServerPlayer player, int amount) {
        CompoundTag data = player.getPersistentData().getCompound("ScheduleMC");
        long current = data.getLong("wallet");
        data.putLong("wallet", Math.max(0, current - amount));
        player.getPersistentData().put("ScheduleMC", data);
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<CompanionManagerData>(){}.getType();
    }

    @Override
    protected void onDataLoaded(CompanionManagerData data) {
        allCompanions.clear();
        playerCompanions.clear();

        if (data.allCompanions != null) {
            allCompanions.putAll(data.allCompanions);
        }
        if (data.playerCompanions != null) {
            playerCompanions.putAll(data.playerCompanions);
        }
    }

    @Override
    protected CompanionManagerData getCurrentData() {
        CompanionManagerData data = new CompanionManagerData();
        data.allCompanions = new HashMap<>(allCompanions);
        data.playerCompanions = new HashMap<>(playerCompanions);
        return data;
    }

    @Override
    protected String getComponentName() {
        return "CompanionManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("%d companions, %d active",
            allCompanions.size(), activeCompanionEntities.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        allCompanions.clear();
        playerCompanions.clear();
    }

    // ═══════════════════════════════════════════════════════════
    // DATA CLASS FOR JSON SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public static class CompanionManagerData {
        public Map<UUID, CompanionData> allCompanions;
        public Map<UUID, List<UUID>> playerCompanions;
    }

    @Override
    public String toString() {
        return String.format("CompanionManager{total=%d, active=%d}",
            allCompanions.size(), activeCompanionEntities.size());
    }
}
