package de.rolandsw.schedulemc.npc.personality;

import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NPC Relationship Manager - Verwaltet alle NPC-Spieler Beziehungen mit JSON-Persistenz
 *
 * Features:
 * - Beziehungstracking zwischen NPCs und Spielern
 * - Persistence (JSON)
 * - Lookup-Performance-Optimierung
 */
public class NPCRelationshipManager extends AbstractPersistenceManager<NPCRelationshipManager.NPCRelationshipManagerData> {

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private static volatile NPCRelationshipManager instance;
    private static final Object INSTANCE_LOCK = new Object();

    @Nullable
    public static NPCRelationshipManager getInstance() {
        return instance;
    }

    public static NPCRelationshipManager getInstance(MinecraftServer server) {
        NPCRelationshipManager result = instance;
        if (result == null) {
            synchronized (INSTANCE_LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new NPCRelationshipManager(server);
                }
            }
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    /**
     * Relationships: "npcId:playerId" → NPCRelationship
     */
    private final Map<String, NPCRelationship> relationships = new ConcurrentHashMap<>();

    /**
     * Index: PlayerId → Set<NPCId> (für schnellen Lookup)
     */
    private final Map<UUID, Set<UUID>> playerToNPCs = new ConcurrentHashMap<>();

    /**
     * Index: NPCId → Set<PlayerId> (für schnellen Lookup)
     */
    private final Map<UUID, Set<UUID>> npcToPlayers = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private NPCRelationshipManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("npc_life_relationships.json").toFile(),
            GsonHelper.get()
        );
        load();
    }

    // ═══════════════════════════════════════════════════════════
    // RELATIONSHIP MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Holt oder erstellt Relationship
     */
    public NPCRelationship getOrCreateRelationship(UUID npcId, UUID playerId) {
        String key = makeKey(npcId, playerId);

        return relationships.computeIfAbsent(key, k -> {
            NPCRelationship rel = new NPCRelationship(npcId, playerId);

            // Update Indices
            playerToNPCs.computeIfAbsent(playerId, p -> ConcurrentHashMap.newKeySet()).add(npcId);
            npcToPlayers.computeIfAbsent(npcId, n -> ConcurrentHashMap.newKeySet()).add(playerId);

            markDirty();
            return rel;
        });
    }

    /**
     * Holt bestehende Relationship (oder null)
     */
    @Nullable
    public NPCRelationship getRelationship(UUID npcId, UUID playerId) {
        return relationships.get(makeKey(npcId, playerId));
    }

    /**
     * Holt alle Relationships eines Spielers
     */
    public List<NPCRelationship> getPlayerRelationships(UUID playerId) {
        Set<UUID> npcIds = playerToNPCs.get(playerId);
        if (npcIds == null) {
            return Collections.emptyList();
        }

        List<NPCRelationship> result = new ArrayList<>();
        for (UUID npcId : npcIds) {
            NPCRelationship rel = getRelationship(npcId, playerId);
            if (rel != null) {
                result.add(rel);
            }
        }
        return result;
    }

    /**
     * Holt alle Relationships eines NPCs
     */
    public List<NPCRelationship> getNPCRelationships(UUID npcId) {
        Set<UUID> playerIds = npcToPlayers.get(npcId);
        if (playerIds == null) {
            return Collections.emptyList();
        }

        List<NPCRelationship> result = new ArrayList<>();
        for (UUID playerId : playerIds) {
            NPCRelationship rel = getRelationship(npcId, playerId);
            if (rel != null) {
                result.add(rel);
            }
        }
        return result;
    }

    /**
     * Löscht Relationship
     */
    public void removeRelationship(UUID npcId, UUID playerId) {
        String key = makeKey(npcId, playerId);
        if (relationships.remove(key) != null) {
            // Update Indices
            Set<UUID> npcs = playerToNPCs.get(playerId);
            if (npcs != null) npcs.remove(npcId);

            Set<UUID> players = npcToPlayers.get(npcId);
            if (players != null) players.remove(playerId);

            markDirty();
        }
    }

    /**
     * Löscht alle Relationships eines Spielers
     */
    public void removePlayerRelationships(UUID playerId) {
        Set<UUID> npcIds = playerToNPCs.remove(playerId);
        if (npcIds != null) {
            for (UUID npcId : npcIds) {
                relationships.remove(makeKey(npcId, playerId));
                Set<UUID> players = npcToPlayers.get(npcId);
                if (players != null) players.remove(playerId);
            }
            markDirty();
        }
    }

    /**
     * Löscht alle Relationships eines NPCs
     */
    public void removeNPCRelationships(UUID npcId) {
        Set<UUID> playerIds = npcToPlayers.remove(npcId);
        if (playerIds != null) {
            for (UUID playerId : playerIds) {
                relationships.remove(makeKey(npcId, playerId));
                Set<UUID> npcs = playerToNPCs.get(playerId);
                if (npcs != null) npcs.remove(npcId);
            }
            markDirty();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    private String makeKey(UUID npcId, UUID playerId) {
        return npcId.toString() + ":" + playerId.toString();
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTICS
    // ═══════════════════════════════════════════════════════════

    public int getTotalRelationships() {
        return relationships.size();
    }

    public int getPlayerCount() {
        return playerToNPCs.size();
    }

    public int getNPCCount() {
        return npcToPlayers.size();
    }

    /**
     * Gibt Top N freundlichste Relationships für Spieler zurück
     */
    public List<NPCRelationship> getTopFriendlyRelationships(UUID playerId, int limit) {
        List<NPCRelationship> all = getPlayerRelationships(playerId);
        all.sort((a, b) -> Integer.compare(b.getRelationshipLevel(), a.getRelationshipLevel()));
        return all.subList(0, Math.min(limit, all.size()));
    }

    /**
     * Gibt Top N feindlichste Relationships für Spieler zurück
     */
    public List<NPCRelationship> getTopHostileRelationships(UUID playerId, int limit) {
        List<NPCRelationship> all = getPlayerRelationships(playerId);
        all.sort((a, b) -> Integer.compare(a.getRelationshipLevel(), b.getRelationshipLevel()));
        return all.subList(0, Math.min(limit, all.size()));
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<NPCRelationshipManagerData>(){}.getType();
    }

    @Override
    protected void onDataLoaded(NPCRelationshipManagerData data) {
        relationships.clear();
        playerToNPCs.clear();
        npcToPlayers.clear();

        int invalidCount = 0;
        int correctedCount = 0;

        if (data.relationships != null) {
            // Check collection size
            if (data.relationships.size() > 100000) {
                LOGGER.warn("Relationships list size ({}) exceeds limit, potential corruption",
                    data.relationships.size());
                correctedCount++;
            }

            for (SerializedRelationship sr : data.relationships) {
                try {
                    // NULL CHECK
                    if (sr == null) {
                        LOGGER.warn("Null serialized relationship, skipping");
                        invalidCount++;
                        continue;
                    }

                    // VALIDATE UUIDs
                    if (sr.npcId == null || sr.npcId.isEmpty()) {
                        LOGGER.warn("Null/empty NPC ID in relationship, skipping");
                        invalidCount++;
                        continue;
                    }
                    if (sr.playerId == null || sr.playerId.isEmpty()) {
                        LOGGER.warn("Null/empty player ID in relationship, skipping");
                        invalidCount++;
                        continue;
                    }

                    UUID npcId;
                    UUID playerId;
                    try {
                        npcId = UUID.fromString(sr.npcId);
                        playerId = UUID.fromString(sr.playerId);
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn("Invalid UUID in relationship (npc: {}, player: {}), skipping",
                            sr.npcId, sr.playerId);
                        invalidCount++;
                        continue;
                    }

                    NPCRelationship rel = new NPCRelationship(npcId, playerId);

                    // VALIDATE RELATIONSHIP LEVEL
                    if (sr.relationshipLevel < -100 || sr.relationshipLevel > 100) {
                        LOGGER.warn("Relationship level {} out of range for NPC {} and player {}, clamping to -100/100",
                            sr.relationshipLevel, npcId, playerId);
                        sr.relationshipLevel = Math.max(-100, Math.min(100, sr.relationshipLevel));
                        correctedCount++;
                    }

                    rel.setRelationshipLevel(sr.relationshipLevel);

                    String key = makeKey(npcId, playerId);
                    relationships.put(key, rel);

                    // Update Indices
                    playerToNPCs.computeIfAbsent(playerId, p -> ConcurrentHashMap.newKeySet()).add(npcId);
                    npcToPlayers.computeIfAbsent(npcId, n -> ConcurrentHashMap.newKeySet()).add(playerId);

                } catch (Exception e) {
                    LOGGER.error("Error loading relationship: {}", sr, e);
                    invalidCount++;
                }
            }
        }

        // SUMMARY
        if (invalidCount > 0 || correctedCount > 0) {
            LOGGER.warn("Data validation: {} invalid entries, {} corrected entries",
                invalidCount, correctedCount);
            if (correctedCount > 0) {
                markDirty(); // Re-save corrected data
            }
        }
    }

    @Override
    protected NPCRelationshipManagerData getCurrentData() {
        NPCRelationshipManagerData data = new NPCRelationshipManagerData();
        data.relationships = new ArrayList<>();

        for (NPCRelationship rel : relationships.values()) {
            data.relationships.add(new SerializedRelationship(
                rel.getNpcId().toString(),
                rel.getPlayerId().toString(),
                rel.getRelationshipLevel()
            ));
        }

        return data;
    }

    @Override
    protected String getComponentName() {
        return "NPCRelationshipManager";
    }

    @Override
    protected String getHealthDetails() {
        return String.format("%d relationships, %d players, %d NPCs",
            relationships.size(), playerToNPCs.size(), npcToPlayers.size());
    }

    @Override
    protected void onCriticalLoadFailure() {
        relationships.clear();
        playerToNPCs.clear();
        npcToPlayers.clear();
    }

    // ═══════════════════════════════════════════════════════════
    // DATA CLASSES FOR JSON SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public static class NPCRelationshipManagerData {
        public List<SerializedRelationship> relationships;
    }

    public static class SerializedRelationship {
        public String npcId;
        public String playerId;
        public int relationshipLevel;

        public SerializedRelationship(String npcId, String playerId, int relationshipLevel) {
            this.npcId = npcId;
            this.playerId = playerId;
            this.relationshipLevel = relationshipLevel;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    public void clearAll() {
        relationships.clear();
        playerToNPCs.clear();
        npcToPlayers.clear();
        markDirty();
        LOGGER.warn("All relationships cleared!");
    }

    public void printStatus() {
        LOGGER.info("═══ NPCRelationshipManager Status ═══");
        LOGGER.info("Total Relationships: {}", relationships.size());
        LOGGER.info("Players with Relationships: {}", playerToNPCs.size());
        LOGGER.info("NPCs with Relationships: {}", npcToPlayers.size());
        LOGGER.info("═══════════════════════════════════════");
    }
}
