package de.rolandsw.schedulemc.npc.life.social;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RumorNetwork - Globales Netzwerk für Gerüchte
 *
 * Verwaltet alle Gerüchte und deren Verbreitung zwischen NPCs.
 * Wird pro ServerLevel gespeichert.
 *
 * Thread-Safe: Nutzt ConcurrentHashMap für Multi-Level Support
 */
public class RumorNetwork {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON-LIKE PER LEVEL
    // ═══════════════════════════════════════════════════════════

    private static final Map<ServerLevel, RumorNetwork> NETWORKS = new ConcurrentHashMap<>();

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
    private final Map<UUID, List<Rumor>> rumorsByPlayer = new ConcurrentHashMap<>();

    /** Welche NPCs kennen welche Gerüchte */
    private final Map<UUID, Set<String>> npcKnownRumors = new ConcurrentHashMap<>();

    /** Letzter bekannter Tag für Tageswechsel-Logik */
    private long lastKnownDay = -1;

    /** Maximum an Gerüchten pro Spieler */
    private static final int MAX_RUMORS_PER_PLAYER = 20;

    // PERFORMANCE: Globale Größenlimits verhindern unbegrenztes Wachstum bei langer Laufzeit
    private static final int MAX_TRACKED_PLAYERS = 5000;
    private static final int MAX_TRACKED_NPCS = 10000;

    // ═══════════════════════════════════════════════════════════
    // RUMOR MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Fügt ein neues Gerücht hinzu
     */
    public void addRumor(Rumor rumor) {
        UUID subject = rumor.getSubjectUUID();

        // PERFORMANCE: Globales Limit prüfen - verhindert unbegrenztes Wachstum
        if (rumorsByPlayer.size() >= MAX_TRACKED_PLAYERS && !rumorsByPlayer.containsKey(subject)) {
            return; // Neue Spieler ablehnen wenn Limit erreicht
        }

        List<Rumor> playerRumors = rumorsByPlayer.computeIfAbsent(subject, k -> new ArrayList<>());

        // Prüfen ob gleiches Gerücht schon existiert
        Rumor existing = null;
        for (Rumor r : playerRumors) {
            if (r.getType() == rumor.getType()) {
                existing = r;
                break;
            }
        }

        if (existing != null) {
            // Verstärken statt duplizieren
            existing.reinforce(lastKnownDay);
        } else {
            // PERFORMANCE: Limit einhalten - sortiere einmal statt Stream pro Iteration
            // Vorher: Stream-Erstellung in While-Schleife (O(n) pro Iteration)
            // Nachher: Einmaliges Sortieren + removeRange-äquivalent (O(n log n) einmalig)
            if (playerRumors.size() >= MAX_RUMORS_PER_PLAYER) {
                playerRumors.sort(Comparator.comparingLong(Rumor::getCreatedDay));
                // Entferne die ältesten, bis Platz für das neue Gerücht ist
                while (playerRumors.size() >= MAX_RUMORS_PER_PLAYER) {
                    playerRumors.remove(0); // Ältestes Element (nach Sortierung vorne)
                }
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
        List<Rumor> all = getRumorsAbout(playerUUID);
        List<Rumor> result = new ArrayList<>();
        for (Rumor r : all) {
            if (r.getType() == type) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Berechnet die Gesamt-Reputation eines Spielers basierend auf Gerüchten
     */
    public int calculateReputationFromRumors(UUID playerUUID) {
        int sum = 0;
        for (Rumor r : getRumorsAbout(playerUUID)) {
            if (r.isCredible()) {
                sum += r.getEffectiveReputationImpact();
            }
        }
        return sum;
    }

    // ═══════════════════════════════════════════════════════════
    // NPC KNOWLEDGE
    // ═══════════════════════════════════════════════════════════

    /**
     * Markiert ein Gerücht als bekannt für einen NPC
     */
    public void markRumorKnown(UUID npcUUID, Rumor rumor) {
        // PERFORMANCE: Globales NPC-Limit prüfen
        if (npcKnownRumors.size() >= MAX_TRACKED_NPCS && !npcKnownRumors.containsKey(npcUUID)) {
            return;
        }
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
        List<Rumor> all = getRumorsAbout(playerUUID);
        List<Rumor> result = new ArrayList<>();
        for (Rumor r : all) {
            if (knownKeys.contains(createRumorKey(r))) {
                result.add(r);
            }
        }
        return result;
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
        for (List<Rumor> rumors : rumorsByPlayer.values()) {
            rumors.removeIf(r -> r.isExpired(currentDay) || !r.isCredible());
        }

        // Leere Listen entfernen
        rumorsByPlayer.entrySet().removeIf(e -> e.getValue().isEmpty());

        // NPC-Known-Rumors aufräumen: Entferne NPCs die keine aktiven Gerüchte mehr referenzieren
        if (npcKnownRumors.size() > MAX_TRACKED_NPCS) {
            // Eviction: Entferne NPCs mit den wenigsten bekannten Gerüchten
            npcKnownRumors.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().size()))
                .limit(npcKnownRumors.size() - MAX_TRACKED_NPCS)
                .map(Map.Entry::getKey)
                .toList()
                .forEach(npcKnownRumors::remove);
        }

        // Eviction fuer rumorsByPlayer wenn Limit ueberschritten
        if (rumorsByPlayer.size() > MAX_TRACKED_PLAYERS) {
            rumorsByPlayer.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().size()))
                .limit(rumorsByPlayer.size() - MAX_TRACKED_PLAYERS)
                .map(Map.Entry::getKey)
                .toList()
                .forEach(rumorsByPlayer::remove);
        }
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
        Set<UUID> allPlayers = new HashSet<>(rumorsByPlayer.keySet());

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
        for (Rumor r : getRumorsAbout(playerUUID)) {
            if (r.getType().isNegative() && r.isCredible()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prüft ob es kriminelle Gerüchte über einen Spieler gibt
     */
    public boolean hasCrimeRumors(UUID playerUUID) {
        for (Rumor r : getRumorsAbout(playerUUID)) {
            if (r.getType().isCrimeRelated() && r.isCredible()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt das schwerwiegendste Gerücht über einen Spieler zurück
     */
    @Nullable
    public Rumor getMostSevereRumor(UUID playerUUID) {
        Rumor best = null;
        int bestImpact = Integer.MAX_VALUE;
        for (Rumor r : getRumorsAbout(playerUUID)) {
            if (r.isCredible()) {
                int impact = r.getType().getReputationImpact();
                if (impact < bestImpact) {
                    bestImpact = impact;
                    best = r;
                }
            }
        }
        return best;
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
        int totalRumors = 0;
        for (List<Rumor> rumors : rumorsByPlayer.values()) {
            totalRumors += rumors.size();
        }
        return String.format("RumorNetwork{totalRumors=%d, playersAffected=%d}",
            totalRumors, rumorsByPlayer.size());
    }
}
