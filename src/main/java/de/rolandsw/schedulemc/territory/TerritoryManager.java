package de.rolandsw.schedulemc.territory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rolandsw.schedulemc.util.AbstractPersistenceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Chunk-basierte Territorien
 * Extends AbstractPersistenceManager
 */
public class TerritoryManager extends AbstractPersistenceManager<Map<Long, Territory>> {
    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile TerritoryManager instance;

    // ChunkKey -> Territory
    private final Map<Long, Territory> territories = new ConcurrentHashMap<>();

    private MinecraftServer server;

    private TerritoryManager(MinecraftServer server) {
        super(
            server.getServerDirectory().toPath().resolve("config").resolve("plotmod_territories.json").toFile(),
            new GsonBuilder().setPrettyPrinting().create()
        );
        this.server = server;
        load();
    }

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static TerritoryManager getInstance(MinecraftServer server) {
        TerritoryManager localRef = instance;
        if (localRef == null) {
            synchronized (TerritoryManager.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new TerritoryManager(server);
                }
            }
        }
        localRef.server = server;
        return localRef;
    }

    @Nullable
    public static TerritoryManager getInstance() {
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // TERRITORY MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Setzt Territory für Chunk
     * Performance-Optimierung: Sendet Delta-Update statt Full Sync
     */
    public void setTerritory(int chunkX, int chunkZ, TerritoryType type, String name, @Nullable UUID ownerUUID) {
        long key = Territory.getChunkKey(chunkX, chunkZ);
        Territory territory = new Territory(chunkX, chunkZ, type, name, ownerUUID);
        territories.put(key, territory);
        save();

        // Performance-Optimierung: Delta-Update an alle Clients senden
        if (server != null) {
            de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.broadcastDeltaUpdate(
                de.rolandsw.schedulemc.territory.network.SyncTerritoryDeltaPacket.update(territory)
            );
        }
    }

    /**
     * Entfernt Territory von Chunk
     * Performance-Optimierung: Sendet Delta-Update statt Full Sync
     */
    public boolean removeTerritory(int chunkX, int chunkZ) {
        long key = Territory.getChunkKey(chunkX, chunkZ);
        boolean removed = territories.remove(key) != null;
        if (removed) {
            save();

            // Performance-Optimierung: Delta-Update an alle Clients senden
            if (server != null) {
                de.rolandsw.schedulemc.territory.network.TerritoryNetworkHandler.broadcastDeltaUpdate(
                    de.rolandsw.schedulemc.territory.network.SyncTerritoryDeltaPacket.remove(chunkX, chunkZ)
                );
            }
        }
        return removed;
    }

    /**
     * Gibt Territory für Chunk zurück
     */
    @Nullable
    public Territory getTerritory(int chunkX, int chunkZ) {
        long key = Territory.getChunkKey(chunkX, chunkZ);
        return territories.get(key);
    }

    /**
     * Gibt Territory für BlockPos zurück
     */
    @Nullable
    public Territory getTerritoryAt(BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        return getTerritory(chunkX, chunkZ);
    }

    /**
     * Prüft ob Chunk ein Territory hat
     */
    public boolean hasTerritory(int chunkX, int chunkZ) {
        long key = Territory.getChunkKey(chunkX, chunkZ);
        return territories.containsKey(key);
    }

    /**
     * Gibt alle Territorien zurück (read-only View ohne Kopie)
     * OPTIMIERUNG: Collections.unmodifiableCollection statt ArrayList-Kopie
     */
    public Collection<Territory> getAllTerritories() {
        return Collections.unmodifiableCollection(territories.values());
    }

    /**
     * Gibt Territorien eines Typs zurück
     */
    public List<Territory> getTerritoriesByType(TerritoryType type) {
        return territories.values().stream()
            .filter(t -> t.getType() == type)
            .toList();
    }

    /**
     * Gibt Territorien eines Owners zurück
     */
    public List<Territory> getTerritoriesByOwner(UUID ownerUUID) {
        return territories.values().stream()
            .filter(t -> ownerUUID.equals(t.getOwnerUUID()))
            .toList();
    }

    /**
     * Gibt Anzahl Territorien zurück
     */
    public int getTerritoryCount() {
        return territories.size();
    }

    /**
     * Löscht alle Territorien eines Typs
     */
    public int clearTerritoriesByType(TerritoryType type) {
        int removed = 0;
        Iterator<Map.Entry<Long, Territory>> it = territories.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().getType() == type) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            save();
        }
        return removed;
    }

    /**
     * Gibt Statistiken zurück
     */
    public String getStatistics() {
        Map<TerritoryType, Integer> counts = new HashMap<>();
        for (Territory territory : territories.values()) {
            counts.merge(territory.getType(), 1, Integer::sum);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Territories: ").append(territories.size()).append("\n");
        for (Map.Entry<TerritoryType, Integer> entry : counts.entrySet()) {
            sb.append("  ").append(entry.getKey().getDisplayName())
              .append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════
    // CLIENT-SIDE ACCESS (für Map Editor)
    // ═══════════════════════════════════════════════════════════

    /**
     * Public getter für Map Editor (read-only View ohne Kopie)
     * OPTIMIERUNG: Collections.unmodifiableMap statt HashMap-Kopie
     */
    public Map<Long, Territory> getTerritoriesMap() {
        return Collections.unmodifiableMap(territories);
    }

    // ═══════════════════════════════════════════════════════════
    // ABSTRACT PERSISTENCE MANAGER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected Type getDataType() {
        return new TypeToken<Map<Long, Territory>>(){}.getType();
    }

    @Override
    protected void onDataLoaded(Map<Long, Territory> data) {
        territories.clear();
        territories.putAll(data);
    }

    @Override
    protected Map<Long, Territory> getCurrentData() {
        return new HashMap<>(territories);
    }

    @Override
    protected String getComponentName() {
        return "TerritoryManager";
    }

    @Override
    protected String getHealthDetails() {
        return territories.size() + " Territorien";
    }

    @Override
    protected void onCriticalLoadFailure() {
        territories.clear();
    }
}
