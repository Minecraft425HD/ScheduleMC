package de.rolandsw.schedulemc.npc.life.companion;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.social.Faction;
import de.rolandsw.schedulemc.npc.life.social.FactionManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.*;

/**
 * CompanionManager - Verwaltet alle Begleiter im System
 *
 * Verantwortlich für:
 * - Begleiter-Rekrutierung
 * - Spieler-Begleiter-Zuordnung
 * - Begleiter-Persistenz
 * - Beschwören und Entlassen
 */
public class CompanionManager {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON-LIKE PER LEVEL
    // ═══════════════════════════════════════════════════════════

    private static final Map<ServerLevel, CompanionManager> MANAGERS = new HashMap<>();

    public static CompanionManager getManager(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level, l -> new CompanionManager(l));
    }

    public static void removeManager(ServerLevel level) {
        MANAGERS.remove(level);
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

    private final ServerLevel level;

    /** Alle registrierten Begleiter: Companion UUID -> Data */
    private final Map<UUID, CompanionData> allCompanions = new HashMap<>();

    /** Spieler-Begleiter-Zuordnung: Player UUID -> List of Companion UUIDs */
    private final Map<UUID, List<UUID>> playerCompanions = new HashMap<>();

    /** Aktive Begleiter-Entities: Companion UUID -> Entity */
    private final Map<UUID, CustomNPCEntity> activeCompanionEntities = new HashMap<>();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private CompanionManager(ServerLevel level) {
        this.level = level;
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
        FactionManager factionManager = FactionManager.getManager(level);
        Faction npcFaction = Faction.forNPCType(npc.getNpcType());
        int playerRep = factionManager.getReputation(playerUUID, npcFaction);

        if (playerRep < type.getRequiredReputation()) {
            return RecruitmentResult.INSUFFICIENT_REPUTATION;
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

        return data.giveCommand(command);
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
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // Alle Begleiter
        ListTag companionsTag = new ListTag();
        for (CompanionData data : allCompanions.values()) {
            companionsTag.add(data.save());
        }
        tag.put("companions", companionsTag);

        // Spieler-Zuordnungen
        CompoundTag playerTag = new CompoundTag();
        for (Map.Entry<UUID, List<UUID>> entry : playerCompanions.entrySet()) {
            ListTag uuidList = new ListTag();
            for (UUID uuid : entry.getValue()) {
                CompoundTag uuidTag = new CompoundTag();
                uuidTag.putUUID("uuid", uuid);
                uuidList.add(uuidTag);
            }
            playerTag.put(entry.getKey().toString(), uuidList);
        }
        tag.put("playerCompanions", playerTag);

        return tag;
    }

    public void load(CompoundTag tag) {
        allCompanions.clear();
        playerCompanions.clear();

        // Begleiter laden
        ListTag companionsTag = tag.getList("companions", Tag.TAG_COMPOUND);
        for (int i = 0; i < companionsTag.size(); i++) {
            CompanionData data = CompanionData.load(companionsTag.getCompound(i));
            allCompanions.put(data.getCompanionUUID(), data);
        }

        // Spieler-Zuordnungen laden
        CompoundTag playerTag = tag.getCompound("playerCompanions");
        for (String key : playerTag.getAllKeys()) {
            UUID playerUUID = UUID.fromString(key);
            ListTag uuidList = playerTag.getList(key, Tag.TAG_COMPOUND);
            List<UUID> companions = new ArrayList<>();
            for (int i = 0; i < uuidList.size(); i++) {
                companions.add(uuidList.getCompound(i).getUUID("uuid"));
            }
            playerCompanions.put(playerUUID, companions);
        }
    }

    @Override
    public String toString() {
        return String.format("CompanionManager{total=%d, active=%d}",
            allCompanions.size(), activeCompanionEntities.size());
    }
}
