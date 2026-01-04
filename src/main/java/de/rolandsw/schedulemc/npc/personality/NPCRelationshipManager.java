package de.rolandsw.schedulemc.npc.personality;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.util.GsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NPC Relationship Manager - Verwaltet alle NPC-Spieler Beziehungen
 *
 * Features:
 * - Beziehungstracking zwischen NPCs und Spielern
 * - Persistence (JSON)
 * - Lookup-Performance-Optimierung
 */
public class NPCRelationshipManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile NPCRelationshipManager instance;

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

    private static final File RELATIONSHIPS_FILE = new File("config/plotmod_npc_relationships.json");
    private static final Gson GSON = GsonHelper.get();
    private boolean dirty = false;

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    private NPCRelationshipManager() {
        LOGGER.info("NPCRelationshipManager initialized");
    }

    public static NPCRelationshipManager getInstance() {
        if (instance == null) {
            synchronized (NPCRelationshipManager.class) {
                if (instance == null) {
                    instance = new NPCRelationshipManager();
                }
            }
        }
        return instance;
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

            dirty = true;
            LOGGER.debug("Created new relationship: NPC {} <-> Player {}", npcId, playerId);
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

            dirty = true;
            LOGGER.debug("Removed relationship: NPC {} <-> Player {}", npcId, playerId);
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
            dirty = true;
            LOGGER.info("Removed all relationships for player {}", playerId);
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
            dirty = true;
            LOGGER.info("Removed all relationships for NPC {}", npcId);
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
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════

    /**
     * Lädt alle Relationships aus Datei
     */
    public void load() {
        if (!RELATIONSHIPS_FILE.exists()) {
            LOGGER.info("No relationships file found, starting fresh");
            return;
        }

        try (FileReader reader = new FileReader(RELATIONSHIPS_FILE)) {
            List<SerializedRelationship> loaded = GSON.fromJson(
                reader,
                new TypeToken<List<SerializedRelationship>>(){}.getType()
            );

            if (loaded == null) {
                LOGGER.warn("Loaded relationships is null");
                return;
            }

            relationships.clear();
            playerToNPCs.clear();
            npcToPlayers.clear();

            for (SerializedRelationship sr : loaded) {
                try {
                    UUID npcId = UUID.fromString(sr.npcId);
                    UUID playerId = UUID.fromString(sr.playerId);

                    NPCRelationship rel = new NPCRelationship(npcId, playerId);
                    rel.setRelationshipLevel(sr.relationshipLevel);

                    String key = makeKey(npcId, playerId);
                    relationships.put(key, rel);

                    // Update Indices
                    playerToNPCs.computeIfAbsent(playerId, p -> ConcurrentHashMap.newKeySet()).add(npcId);
                    npcToPlayers.computeIfAbsent(npcId, n -> ConcurrentHashMap.newKeySet()).add(playerId);

                } catch (Exception e) {
                    LOGGER.error("Error loading relationship: {}", sr, e);
                }
            }

            dirty = false;
            LOGGER.info("Loaded {} relationships", relationships.size());

        } catch (IOException e) {
            LOGGER.error("Error loading relationships", e);
        }
    }

    /**
     * Speichert alle Relationships in Datei
     */
    public void save() {
        if (!dirty) {
            return;  // Keine Änderungen
        }

        try {
            RELATIONSHIPS_FILE.getParentFile().mkdirs();

            // Serialize
            List<SerializedRelationship> toSave = new ArrayList<>();
            for (NPCRelationship rel : relationships.values()) {
                toSave.add(new SerializedRelationship(
                    rel.getNpcId().toString(),
                    rel.getPlayerId().toString(),
                    rel.getRelationshipLevel()
                ));
            }

            // Write
            try (FileWriter writer = new FileWriter(RELATIONSHIPS_FILE)) {
                GSON.toJson(toSave, writer);
                writer.flush();
            }

            dirty = false;
            LOGGER.info("Saved {} relationships", relationships.size());

        } catch (IOException e) {
            LOGGER.error("Error saving relationships", e);
        }
    }

    public void saveIfNeeded() {
        if (dirty) {
            save();
        }
    }

    /**
     * Serialization helper class
     */
    private static class SerializedRelationship {
        String npcId;
        String playerId;
        int relationshipLevel;

        SerializedRelationship(String npcId, String playerId, int relationshipLevel) {
            this.npcId = npcId;
            this.playerId = playerId;
            this.relationshipLevel = relationshipLevel;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    public void markDirty() {
        dirty = true;
    }

    public void clearAll() {
        relationships.clear();
        playerToNPCs.clear();
        npcToPlayers.clear();
        dirty = true;
        LOGGER.warn("All relationships cleared!");
    }

    public void printStatus() {
        LOGGER.info("═══ NPCRelationshipManager Status ═══");
        LOGGER.info("Total Relationships: {}", relationships.size());
        LOGGER.info("Players with Relationships: {}", playerToNPCs.size());
        LOGGER.info("NPCs with Relationships: {}", npcToPlayers.size());
        LOGGER.info("Dirty: {}", dirty);
        LOGGER.info("═══════════════════════════════════════");
    }
}
