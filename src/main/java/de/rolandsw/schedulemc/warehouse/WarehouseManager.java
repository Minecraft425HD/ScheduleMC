package de.rolandsw.schedulemc.warehouse;
import de.rolandsw.schedulemc.util.EventHelper;
import de.rolandsw.schedulemc.util.ConfigCache;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.HashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.HashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Collections;
import java.util.HashMap;

/**
 * WarehouseManager - Globaler Manager für alle Warehouses
 *
 * Funktionen:
 * - Trackt alle Warehouse-Positionen weltweit
 * - Führt Auto-Delivery unabhängig von Chunk-Loading durch
 * - Persistiert Warehouse-Positionen beim Server-Stop
 */
@Mod.EventBusSubscriber(modid = "schedulemc")
public class WarehouseManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Set<BlockPos>> warehouses = new ConcurrentHashMap<>();
    // SICHERHEIT: volatile für Memory Visibility zwischen Threads
    private static volatile boolean dirty = false;
    private static volatile int tickCounter = 0;
    private static final int CHECK_INTERVAL = 20; // Prüfe jede Sekunde (20 ticks) für schnelle Reaktion

    // OPTIMIERUNG: Cache für letzte Delivery-Tage (vermeidet Block-Entity-Lookups jeden Tick)
    private static final Map<BlockPos, Long> lastDeliveryDayCache = new ConcurrentHashMap<>();

    /**
     * Registers a warehouse in the global warehouse tracking system.
     * <p>
     * Adds the warehouse to the internal registry and marks the manager as dirty,
     * ensuring the warehouse will be included in the next save operation and
     * participate in automated delivery checks.
     * </p>
     * <p>
     * <strong>Thread Safety:</strong> This method is thread-safe. Uses ConcurrentHashMap
     * for safe concurrent access.
     * </p>
     * <p>
     * <strong>Persistence:</strong> Triggers dirty flag; changes will be persisted
     * on next server save or shutdown.
     * </p>
     *
     * @param level the server level containing the warehouse
     * @param pos the block position of the warehouse block entity
     * @see #unregisterWarehouse(ServerLevel, BlockPos)
     * @see #save(MinecraftServer)
     */
    public static void registerWarehouse(@Nonnull ServerLevel level, @Nonnull BlockPos pos) {
        String levelKey = getLevelKey(level);
        warehouses.computeIfAbsent(levelKey, k -> ConcurrentHashMap.newKeySet()).add(pos);
        dirty = true;
        LOGGER.info("Warehouse registriert: {} in Level {}", pos.toShortString(), levelKey);
    }

    /**
     * Unregisters a warehouse from the global warehouse tracking system.
     * <p>
     * Removes the warehouse from the internal registry and clears any cached
     * delivery data associated with it. If this was the last warehouse in the
     * level, removes the level entry entirely.
     * </p>
     * <p>
     * <strong>Thread Safety:</strong> This method is thread-safe. Uses ConcurrentHashMap
     * for safe concurrent access.
     * </p>
     * <p>
     * <strong>Persistence:</strong> Triggers dirty flag; changes will be persisted
     * on next server save or shutdown.
     * </p>
     * <p>
     * <strong>Cache Management:</strong> Automatically clears the lastDeliveryDay cache
     * entry for this position to prevent memory leaks.
     * </p>
     *
     * @param level the server level containing the warehouse
     * @param pos the block position of the warehouse block entity
     * @see #registerWarehouse(ServerLevel, BlockPos)
     * @see #save(MinecraftServer)
     */
    public static void unregisterWarehouse(@Nonnull ServerLevel level, @Nonnull BlockPos pos) {
        String levelKey = getLevelKey(level);
        Set<BlockPos> levelWarehouses = warehouses.get(levelKey);
        if (levelWarehouses != null) {
            levelWarehouses.remove(pos);
            lastDeliveryDayCache.remove(pos); // Cache bereinigen
            if (levelWarehouses.isEmpty()) {
                warehouses.remove(levelKey);
            }
            dirty = true;
            LOGGER.info("Warehouse deregistriert: {} in Level {}", pos.toShortString(), levelKey);
        }
    }

    /**
     * Server Tick Event - Prüft alle Warehouses auf Auto-Delivery
     *
     * Verwendet Tag-basierte Logik wie NPCDailySalaryHandler.
     * Funktioniert mit /time add, /time set, Schlafen, etc.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        EventHelper.handleServerTickEnd(event, server -> {
            // Überspringe wenn keine Spieler online sind
            if (server.getPlayerCount() == 0) return;

            // Prüfe nur jede Sekunde (20 ticks) statt jeden Tick
            tickCounter++;
            if (tickCounter < CHECK_INTERVAL) return;
            tickCounter = 0;

            // Prüfe alle registrierten Warehouses
            for (Map.Entry<String, Set<BlockPos>> entry : warehouses.entrySet()) {
                String levelKey = entry.getKey();
                ServerLevel level = getLevelByKey(server, levelKey);

                if (level == null) {
                    LOGGER.warn("[WarehouseManager] Level {} not found!", levelKey);
                    continue;
                }

                long currentDay = level.getDayTime() / 24000L;

                // OPTIMIERUNG: Direkte Iteration ohne Kopie, ConcurrentHashMap.newKeySet() ist iterationssicher
                for (BlockPos pos : entry.getValue()) {
                    checkWarehouseDelivery(level, pos, currentDay);
                }
            }
        });
    }

    /**
     * Prüft ein einzelnes Warehouse auf notwendige Delivery
     *
     * OPTIMIERT: Verwendet Cache um Block-Entity-Lookups zu minimieren.
     * Block-Entity wird nur abgefragt wenn tatsächlich eine Delivery nötig ist.
     */
    private static void checkWarehouseDelivery(@Nonnull ServerLevel level, @Nonnull BlockPos pos, long currentDay) {
        long intervalDays = ConfigCache.getWarehouseDeliveryIntervalDays();

        // OPTIMIERUNG: Prüfe Cache zuerst - vermeide Block-Entity-Lookup wenn keine Delivery nötig
        Long cachedLastDeliveryDay = lastDeliveryDayCache.get(pos);
        if (cachedLastDeliveryDay != null && currentDay < cachedLastDeliveryDay + intervalDays) {
            // Keine Delivery nötig laut Cache - überspringe Block-Entity-Lookup
            return;
        }

        // Delivery möglicherweise nötig - jetzt Block-Entity laden
        ChunkPos chunkPos = new ChunkPos(pos);
        boolean wasLoaded = level.isLoaded(pos);

        try {
            // Force-load chunk temporär
            if (!wasLoaded) {
                level.getChunk(chunkPos.x, chunkPos.z);
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) {
                // Warehouse existiert nicht mehr - deregistrieren und aus Cache entfernen
                lastDeliveryDayCache.remove(pos);
                unregisterWarehouse(level, pos);
                return;
            }

            long lastDeliveryDay = warehouse.getLastDeliveryDay();

            // Cache aktualisieren
            lastDeliveryDayCache.put(pos, lastDeliveryDay);

            // Prüfe ob genug Tage vergangen sind
            if (currentDay >= lastDeliveryDay + intervalDays) {
                warehouse.performDelivery(level);
                warehouse.setLastDeliveryDay(currentDay);
                warehouse.setChanged();
                warehouse.syncToClient();

                // Cache mit neuem Wert aktualisieren
                lastDeliveryDayCache.put(pos, currentDay);
            }

        } catch (IllegalStateException e) {
            // Chunk loading or world state issues
            LOGGER.error("Invalid world state while checking warehouse @ {}: {}", pos.toShortString(), e.getMessage());
        } catch (Exception e) {
            // Intentionally catching all other exceptions - tick handler must not crash
            LOGGER.error("Unexpected error while checking warehouse @ {}", pos.toShortString(), e);
        }
    }

    /**
     * Block Place Event - Auto-registriere neue Warehouses
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        EventHelper.handleBlockPlace(event, player -> {
            if (!(event.getLevel() instanceof ServerLevel level)) return;

            BlockEntity be = level.getBlockEntity(event.getPos());
            if (be instanceof WarehouseBlockEntity) {
                registerWarehouse(level, event.getPos());
            }
        });
    }

    /**
     * Block Break Event - Auto-deregistriere entfernte Warehouses
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        EventHelper.handleBlockBreak(event, player -> {
            if (!(event.getLevel() instanceof ServerLevel level)) return;

            BlockEntity be = level.getBlockEntity(event.getPos());
            if (be instanceof WarehouseBlockEntity) {
                unregisterWarehouse(level, event.getPos());
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════

    /**
     * Loads warehouse positions from persistent storage on server startup.
     * <p>
     * Reads the warehouse registry from compressed NBT format stored in the world's
     * data directory. If the data file doesn't exist (e.g., first run), the warehouse
     * registry remains empty without error.
     * </p>
     * <p>
     * <strong>File Location:</strong> {@code <world>/data/schedulemc_warehouses.dat}
     * </p>
     * <p>
     * <strong>Data Format:</strong> Compressed NBT containing level keys and block positions
     * </p>
     * <p>
     * <strong>Error Handling:</strong> Logs errors but does not throw exceptions. On failure,
     * the warehouse registry remains in its current state (typically empty on startup).
     * </p>
     * <p>
     * <strong>Thread Safety:</strong> Should only be called during server initialization
     * before concurrent access begins.
     * </p>
     *
     * @param server the Minecraft server instance, used to locate the world data directory
     * @see #save(MinecraftServer)
     */
    public static void load(@Nonnull MinecraftServer server) {
        LOGGER.info("★★★ [WarehouseManager] load() aufgerufen! ★★★");
        File dataFile = getDataFile(server);
        LOGGER.info("[WarehouseManager] Data file: {}, exists: {}", dataFile.getAbsolutePath(), dataFile.exists());

        if (!dataFile.exists()) {
            LOGGER.info("★★★ [WarehouseManager] Keine Warehouse-Daten gefunden - warehouses map ist leer! ★★★");
            return;
        }

        try (FileInputStream fis = new FileInputStream(dataFile)) {
            CompoundTag tag = net.minecraft.nbt.NbtIo.readCompressed(fis);

            ListTag levelsList = tag.getList("Levels", Tag.TAG_COMPOUND);
            for (int i = 0; i < levelsList.size(); i++) {
                CompoundTag levelTag = levelsList.getCompound(i);
                String levelKey = levelTag.getString("Key");

                ListTag posList = levelTag.getList("Positions", Tag.TAG_COMPOUND);
                Set<BlockPos> positions = ConcurrentHashMap.newKeySet();

                for (int j = 0; j < posList.size(); j++) {
                    CompoundTag posTag = posList.getCompound(j);
                    BlockPos pos = NbtUtils.readBlockPos(posTag);
                    positions.add(pos);
                }

                warehouses.put(levelKey, positions);
            }

            LOGGER.info("Warehouse-Daten geladen: {} Levels, {} total Warehouses",
                warehouses.size(), warehouses.values().stream().mapToInt(Set::size).sum());

        } catch (IOException e) {
            LOGGER.error("Fehler beim Laden der Warehouse-Daten", e);
        }
    }

    /**
     * Saves warehouse positions to persistent storage.
     * <p>
     * Writes the warehouse registry to compressed NBT format in the world's data
     * directory. Only performs the write operation if the dirty flag is set,
     * avoiding unnecessary disk I/O when no changes have been made.
     * </p>
     * <p>
     * <strong>File Location:</strong> {@code <world>/data/schedulemc_warehouses.dat}
     * </p>
     * <p>
     * <strong>Data Format:</strong> Compressed NBT containing level keys and block positions
     * </p>
     * <p>
     * <strong>Optimization:</strong> Skips save operation if no changes have been made
     * (dirty flag is false).
     * </p>
     * <p>
     * <strong>Error Handling:</strong> Logs errors but does not throw exceptions. The dirty
     * flag is only cleared on successful save.
     * </p>
     * <p>
     * <strong>Thread Safety:</strong> Should typically be called during server shutdown
     * when concurrent modifications have ceased.
     * </p>
     *
     * @param server the Minecraft server instance, used to locate the world data directory
     * @see #load(MinecraftServer)
     */
    public static void save(@Nonnull MinecraftServer server) {
        if (!dirty) return;

        File dataFile = getDataFile(server);
        dataFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(dataFile)) {
            CompoundTag tag = new CompoundTag();

            ListTag levelsList = new ListTag();
            for (Map.Entry<String, Set<BlockPos>> entry : warehouses.entrySet()) {
                CompoundTag levelTag = new CompoundTag();
                levelTag.putString("Key", entry.getKey());

                ListTag posList = new ListTag();
                for (BlockPos pos : entry.getValue()) {
                    CompoundTag posTag = NbtUtils.writeBlockPos(pos);
                    posList.add(posTag);
                }

                levelTag.put("Positions", posList);
                levelsList.add(levelTag);
            }

            tag.put("Levels", levelsList);
            net.minecraft.nbt.NbtIo.writeCompressed(tag, fos);

            int totalWarehouses = warehouses.values().stream().mapToInt(Set::size).sum();
            LOGGER.info("Warehouse-Daten gespeichert: {} Levels, {} Warehouses", warehouses.size(), totalWarehouses);
            dirty = false;

        } catch (IOException e) {
            LOGGER.error("Fehler beim Speichern der Warehouse-Daten", e);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════

    private static String getLevelKey(@Nonnull ServerLevel level) {
        return level.dimension().location().toString();
    }

    /**
     * Retrieves a ServerLevel by its dimension key string.
     * <p>
     * Searches through all loaded server levels to find one matching the specified
     * dimension key (e.g., "minecraft:overworld", "minecraft:the_nether").
     * </p>
     *
     * @param server the Minecraft server instance
     * @param key the dimension location key as a string
     * @return the matching ServerLevel, or null if not found
     */
    @Nullable
    private static ServerLevel getLevelByKey(@Nonnull MinecraftServer server, @Nonnull String key) {
        for (ServerLevel level : server.getAllLevels()) {
            if (getLevelKey(level).equals(key)) {
                return level;
            }
        }
        return null;
    }

    private static File getDataFile(@Nonnull MinecraftServer server) {
        return new File(server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile(),
            "data/schedulemc_warehouses.dat");
    }

    /**
     * Returns all registered warehouse positions across all levels.
     * <p>
     * Primarily used for debugging, monitoring, and administrative commands.
     * Returns an unmodifiable deep copy of the warehouse registry to prevent
     * external modification of the internal state.
     * </p>
     * <p>
     * <strong>Thread Safety:</strong> Returns a snapshot copy that is safe to iterate
     * without synchronization. The returned map and its value sets are independent
     * copies and will not reflect subsequent changes to the registry.
     * </p>
     * <p>
     * <strong>Performance:</strong> Creates deep copies of all data structures.
     * Avoid calling frequently in performance-critical code.
     * </p>
     *
     * @return an unmodifiable map of level keys to sets of warehouse positions,
     *         where the map and sets are defensive copies
     */
    public static Map<String, Set<BlockPos>> getAllWarehouses() {
        Map<String, Set<BlockPos>> copy = new HashMap<>();
        for (Map.Entry<String, Set<BlockPos>> entry : warehouses.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }
}
