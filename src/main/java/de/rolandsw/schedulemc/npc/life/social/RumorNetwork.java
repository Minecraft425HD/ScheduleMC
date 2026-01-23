package de.rolandsw.schedulemc.npc.life.social;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.*;

/**
 * RumorNetwork - Globales Netzwerk für Gerüchte
 *
 * Verwaltet alle Gerüchte und deren Verbreitung zwischen NPCs.
 * Wird pro ServerLevel gespeichert.
 */
public class RumorNetwork {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON-LIKE PER LEVEL
    // ═══════════════════════════════════════════════════════════

    private static final Map<ServerLevel, RumorNetwork> NETWORKS = new HashMap<>();

    public static RumorNetwork getNetwork(ServerLevel level) {
        return NETWORKS.computeIfAbsent(level, l -> new RumorNetwork());
    }

    public static void removeNetwork(ServerLevel level) {
        NETWORKS.remove(level);
    }

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    /** Alle aktiven Gerüchte, nach Spieler-UUID gruppiert */
    private final Map<UUID, List<Rumor>> rumorsByPlayer = new HashMap<>();

    /** Welche NPCs kennen welche Gerüchte */
    private final Map<UUID, Set<String>> npcKnownRumors = new HashMap<>();

    /** Letzter bekannter Tag für Tageswechsel-Logik */
    private long lastKnownDay = -1;

    /** Maximum an Gerüchten pro Spieler */
    private static final int MAX_RUMORS_PER_PLAYER = 20;

    // ═══════════════════════════════════════════════════════════
    // RUMOR MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt ein neues Gerücht hinzu
     */
    public void addRumor(Rumor rumor) {
        UUID subject = rumor.getSubjectUUID();
        List<Rumor> playerRumors = rumorsByPlayer.computeIfAbsent(subject, k -> new ArrayList<>());

        // Prüfen ob gleiches Gerücht schon existiert
        Optional<Rumor> existing = playerRumors.stream()
            .filter(r -> r.getType() == rumor.getType())
            .findFirst();

        if (existing.isPresent()) {
            // Verstärken statt duplizieren
            existing.get().reinforce(lastKnownDay);
        } else {
            // Limit einhalten
            while (playerRumors.size() >= MAX_RUMORS_PER_PLAYER) {
                // Ältestes entfernen
                playerRumors.stream()
                    .min(Comparator.comparingLong(Rumor::getCreatedDay))
                    .ifPresent(playerRumors::remove);
            }
            playerRumors.add(rumor);
        }
    }

    /**
     * Erstellt ein neues Gerücht von einem NPC
     */
    public void createRumor(UUID subjectPlayer, RumorType type, String details,
                            long currentDay, @Nullable UUID sourceNPC) {
        Rumor rumor = new Rumor(subjectPlayer, type, details, currentDay);
        rumor.setSourceNPC(sourceNPC);
        addRumor(rumor);

        // Quell-NPC kennt das Gerücht
        if (sourceNPC != null) {
            markRumorKnown(sourceNPC, rumor);
        }
    }

    /**
     * Holt alle Gerüchte über einen Spieler
     */
    public List<Rumor> getRumorsAbout(UUID playerUUID) {
        return rumorsByPlayer.getOrDefault(playerUUID, Collections.emptyList());
    }

    /**
     * Holt alle Gerüchte eines bestimmten Typs über einen Spieler
     */
    public List<Rumor> getRumorsOfType(UUID playerUUID, RumorType type) {
        return getRumorsAbout(playerUUID).stream()
            .filter(r -> r.getType() == type)
            .toList();
    }

    /**
     * Berechnet die Gesamt-Reputation eines Spielers basierend auf Gerüchten
     */
    public int calculateReputationFromRumors(UUID playerUUID) {
        return getRumorsAbout(playerUUID).stream()
            .filter(Rumor::isCredible)
            .mapToInt(Rumor::getEffectiveReputationImpact)
            .sum();
    }

    // ═══════════════════════════════════════════════════════════
    // NPC KNOWLEDGE
    // ═══════════════════════════════════════════════════════════

    /**
     * Markiert ein Gerücht als bekannt für einen NPC
     */
    public void markRumorKnown(UUID npcUUID, Rumor rumor) {
        String rumorKey = createRumorKey(rumor);
        npcKnownRumors.computeIfAbsent(npcUUID, k -> new HashSet<>()).add(rumorKey);
    }

    /**
     * Prüft ob ein NPC ein Gerücht kennt
     */
    public boolean doesNPCKnowRumor(UUID npcUUID, Rumor rumor) {
        String rumorKey = createRumorKey(rumor);
        Set<String> known = npcKnownRumors.get(npcUUID);
        return known != null && known.contains(rumorKey);
    }

    /**
     * Versucht ein Gerücht von einem NPC zu einem anderen zu übertragen
     */
    public boolean trySpreadRumor(Rumor rumor, UUID fromNPC, UUID toNPC) {
        // Prüfen ob Empfänger das Gerücht schon kennt
        if (doesNPCKnowRumor(toNPC, rumor)) {
            return false;
        }

        // Versuche zu verbreiten
        if (rumor.trySpread()) {
            markRumorKnown(toNPC, rumor);
            return true;
        }
        return false;
    }

    /**
     * Holt alle Gerüchte die ein NPC über einen Spieler kennt
     */
    public List<Rumor> getRumorsKnownByNPC(UUID npcUUID, UUID playerUUID) {
        Set<String> knownKeys = npcKnownRumors.getOrDefault(npcUUID, Collections.emptySet());
        return getRumorsAbout(playerUUID).stream()
            .filter(r -> knownKeys.contains(createRumorKey(r)))
            .toList();
    }

    private String createRumorKey(Rumor rumor) {
        return rumor.getSubjectUUID().toString() + "_" + rumor.getType().name() + "_" + rumor.getCreatedDay();
    }

    // ═══════════════════════════════════════════════════════════
    // TICK / UPDATE
    // ═══════════════════════════════════════════════════════════

    /**
     * Wird täglich aufgerufen - entfernt abgelaufene Gerüchte
     */
    public void onDayChange(long currentDay) {
        lastKnownDay = currentDay;

        // Abgelaufene Gerüchte entfernen
        rumorsByPlayer.values().forEach(rumors ->
            rumors.removeIf(r -> r.isExpired(currentDay) || !r.isCredible())
        );

        // Leere Listen entfernen
        rumorsByPlayer.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    /**
     * Periodisches Update
     */
    public void tick() {
        // Täglich Gerüchte aufräumen
        // (wird durch onDayChange behandelt)
    }

    /**
     * Verbreitet ein Gerücht von einer Position aus zu nahegelegenen NPCs
     */
    public void spreadRumor(Rumor rumor, BlockPos sourcePos) {
        // Korrigiere expirationDay wenn nötig (für Factory-erstellte Gerüchte)
        if (rumor.needsExpirationCorrection()) {
            rumor.correctExpiration(Math.max(0, lastKnownDay));
        }
        addRumor(rumor);
        // Gerücht wird durch normale NPC-Interaktionen weiterverbreitet
    }

    /**
     * Verbreitet ein Gerücht an alle NPCs (server-weit)
     */
    public void broadcastRumor(Rumor rumor) {
        // Korrigiere expirationDay wenn nötig (für Factory-erstellte Gerüchte)
        if (rumor.needsExpirationCorrection()) {
            rumor.correctExpiration(Math.max(0, lastKnownDay));
        }
        addRumor(rumor);
        // Wichtige Gerüchte (z.B. Welt-Events) werden sofort allen bekannt
        rumor.setCredibility(100.0f);
    }

    /**
     * Verbreitet Gerüchte zwischen nahegelegenen NPCs
     */
    public void spreadRumorsBetweenNPCs(CustomNPCEntity npc1, CustomNPCEntity npc2, long currentDay) {
        UUID npc1UUID = npc1.getNpcData().getNpcUUID();
        UUID npc2UUID = npc2.getNpcData().getNpcUUID();

        // Alle Spieler-UUIDs die beide NPCs kennen
        Set<UUID> allPlayers = new HashSet<>();
        rumorsByPlayer.keySet().forEach(allPlayers::add);

        for (UUID playerUUID : allPlayers) {
            List<Rumor> rumors = getRumorsAbout(playerUUID);
            for (Rumor rumor : rumors) {
                // NPC1 -> NPC2
                if (doesNPCKnowRumor(npc1UUID, rumor) && !doesNPCKnowRumor(npc2UUID, rumor)) {
                    trySpreadRumor(rumor, npc1UUID, npc2UUID);
                }
                // NPC2 -> NPC1
                if (doesNPCKnowRumor(npc2UUID, rumor) && !doesNPCKnowRumor(npc1UUID, rumor)) {
                    trySpreadRumor(rumor, npc2UUID, npc1UUID);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob es negative Gerüchte über einen Spieler gibt
     */
    public boolean hasNegativeRumors(UUID playerUUID) {
        return getRumorsAbout(playerUUID).stream()
            .anyMatch(r -> r.getType().isNegative() && r.isCredible());
    }

    /**
     * Prüft ob es kriminelle Gerüchte über einen Spieler gibt
     */
    public boolean hasCrimeRumors(UUID playerUUID) {
        return getRumorsAbout(playerUUID).stream()
            .anyMatch(r -> r.getType().isCrimeRelated() && r.isCredible());
    }

    /**
     * Gibt das schwerwiegendste Gerücht über einen Spieler zurück
     */
    @Nullable
    public Rumor getMostSevereRumor(UUID playerUUID) {
        return getRumorsAbout(playerUUID).stream()
            .filter(Rumor::isCredible)
            .min(Comparator.comparingInt(r -> r.getType().getReputationImpact()))
            .orElse(null);
    }

    // ═══════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("LastKnownDay", lastKnownDay);

        // Gerüchte speichern
        ListTag rumorList = new ListTag();
        for (List<Rumor> rumors : rumorsByPlayer.values()) {
            for (Rumor rumor : rumors) {
                rumorList.add(rumor.save());
            }
        }
        tag.put("Rumors", rumorList);

        // NPC-Wissen speichern
        CompoundTag knowledgeTag = new CompoundTag();
        for (Map.Entry<UUID, Set<String>> entry : npcKnownRumors.entrySet()) {
            ListTag keyList = new ListTag();
            for (String key : entry.getValue()) {
                CompoundTag keyTag = new CompoundTag();
                keyTag.putString("Key", key);
                keyList.add(keyTag);
            }
            knowledgeTag.put(entry.getKey().toString(), keyList);
        }
        tag.put("NPCKnowledge", knowledgeTag);

        return tag;
    }

    public void load(CompoundTag tag) {
        lastKnownDay = tag.getLong("LastKnownDay");

        // Gerüchte laden
        rumorsByPlayer.clear();
        ListTag rumorList = tag.getList("Rumors", Tag.TAG_COMPOUND);
        for (int i = 0; i < rumorList.size(); i++) {
            Rumor rumor = Rumor.load(rumorList.getCompound(i));
            rumorsByPlayer.computeIfAbsent(rumor.getSubjectUUID(), k -> new ArrayList<>()).add(rumor);
        }

        // NPC-Wissen laden
        npcKnownRumors.clear();
        CompoundTag knowledgeTag = tag.getCompound("NPCKnowledge");
        for (String uuidStr : knowledgeTag.getAllKeys()) {
            UUID npcUUID = UUID.fromString(uuidStr);
            Set<String> keys = new HashSet<>();
            ListTag keyList = knowledgeTag.getList(uuidStr, Tag.TAG_COMPOUND);
            for (int i = 0; i < keyList.size(); i++) {
                keys.add(keyList.getCompound(i).getString("Key"));
            }
            npcKnownRumors.put(npcUUID, keys);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DEBUG
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        int totalRumors = rumorsByPlayer.values().stream().mapToInt(List::size).sum();
        return String.format("RumorNetwork{totalRumors=%d, playersAffected=%d}",
            totalRumors, rumorsByPlayer.size());
    }
}
