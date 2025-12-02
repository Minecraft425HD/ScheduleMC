package de.rolandsw.schedulemc.warehouse;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private static boolean dirty = false;
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 20; // Prüfe jede Sekunde (20 ticks) für schnelle Reaktion

    /**
     * Registriert ein Warehouse
     */
    public static void registerWarehouse(ServerLevel level, BlockPos pos) {
        String levelKey = getLevelKey(level);
        warehouses.computeIfAbsent(levelKey, k -> ConcurrentHashMap.newKeySet()).add(pos);
        dirty = true;
        LOGGER.info("Warehouse registriert: {} in Level {}", pos.toShortString(), levelKey);
    }

    /**
     * Deregistriert ein Warehouse
     */
    public static void unregisterWarehouse(ServerLevel level, BlockPos pos) {
        String levelKey = getLevelKey(level);
        Set<BlockPos> levelWarehouses = warehouses.get(levelKey);
        if (levelWarehouses != null) {
            levelWarehouses.remove(pos);
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
     * WICHTIG: Verwendet die gleiche Logik wie NPCDailySalaryHandler!
     * Prüft einfach für jedes Warehouse ob genug Zeit vergangen ist.
     * Funktioniert mit /time add, /time set, Schlafen, etc.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = event.getServer();
        if (server == null) return;

        // Überspringe wenn keine Spieler online sind
        if (server.getPlayerCount() == 0) return;

        // Prüfe nur jede Sekunde (20 ticks) statt jeden Tick
        tickCounter++;

        // DEBUG: Log every second to verify this is running
        if (tickCounter == 1) {
            LOGGER.info("★★★ [WarehouseManager] TICK! Warehouses registered: {}, players online: {}",
                warehouses.size(), server.getPlayerCount());
            for (Map.Entry<String, Set<BlockPos>> entry : warehouses.entrySet()) {
                LOGGER.info("  → Level {}: {} warehouses", entry.getKey(), entry.getValue().size());
            }
        }

        if (tickCounter < CHECK_INTERVAL) return;
        tickCounter = 0;

        LOGGER.info("★★★ [WarehouseManager] Checking {} warehouse groups for delivery", warehouses.size());

        // Prüfe alle registrierten Warehouses
        for (Map.Entry<String, Set<BlockPos>> entry : warehouses.entrySet()) {
            String levelKey = entry.getKey();
            ServerLevel level = getLevelByKey(server, levelKey);

            if (level == null) {
                LOGGER.warn("[WarehouseManager] Level {} not found!", levelKey);
                continue;
            }

            long currentTime = level.getGameTime();
            LOGGER.info("[WarehouseManager] Checking level {} at time {}, {} warehouses",
                levelKey, currentTime, entry.getValue().size());

            for (BlockPos pos : new ArrayList<>(entry.getValue())) {
                checkWarehouseDelivery(level, pos, currentTime);
            }
        }
    }

    /**
     * Prüft ein einzelnes Warehouse auf notwendige Delivery
     */
    private static void checkWarehouseDelivery(ServerLevel level, BlockPos pos, long currentTime) {
        // Lade Chunk falls nötig (force load für diesen Tick)
        ChunkPos chunkPos = new ChunkPos(pos);
        boolean wasLoaded = level.isLoaded(pos);

        try {
            // Force-load chunk temporär
            if (!wasLoaded) {
                level.getChunk(chunkPos.x, chunkPos.z);
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof WarehouseBlockEntity warehouse)) {
                // Warehouse existiert nicht mehr - deregistrieren
                unregisterWarehouse(level, pos);
                return;
            }

            // Prüfe ob Delivery fällig ist
            long intervalTicks = WarehouseConfig.DELIVERY_INTERVAL_DAYS.get() * 24000L;
            long timeSinceLastDelivery = currentTime - warehouse.getLastDeliveryTime();

            if (timeSinceLastDelivery >= intervalTicks) {
                LOGGER.info("WarehouseManager: Triggering delivery for warehouse @ {} (timeSince={}, interval={})",
                    pos.toShortString(), timeSinceLastDelivery, intervalTicks);

                warehouse.performDelivery(level);
                warehouse.setLastDeliveryTime(currentTime);
                warehouse.setChanged();
                warehouse.syncToClient();
            }

        } catch (Exception e) {
            LOGGER.error("Fehler beim Prüfen von Warehouse @ " + pos.toShortString(), e);
        }
    }

    /**
     * Block Place Event - Auto-registriere neue Warehouses
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockEntity be = level.getBlockEntity(event.getPos());
        if (be instanceof WarehouseBlockEntity) {
            registerWarehouse(level, event.getPos());
        }
    }

    /**
     * Block Break Event - Auto-deregistriere entfernte Warehouses
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockEntity be = level.getBlockEntity(event.getPos());
        if (be instanceof WarehouseBlockEntity) {
            unregisterWarehouse(level, event.getPos());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════

    /**
     * Lädt Warehouse-Positionen beim Server-Start
     */
    public static void load(MinecraftServer server) {
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
     * Speichert Warehouse-Positionen beim Server-Stop
     */
    public static void save(MinecraftServer server) {
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

    private static String getLevelKey(ServerLevel level) {
        return level.dimension().location().toString();
    }

    private static ServerLevel getLevelByKey(MinecraftServer server, String key) {
        for (ServerLevel level : server.getAllLevels()) {
            if (getLevelKey(level).equals(key)) {
                return level;
            }
        }
        return null;
    }

    private static File getDataFile(MinecraftServer server) {
        return new File(server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile(),
            "data/schedulemc_warehouses.dat");
    }

    /**
     * Gibt alle registrierten Warehouse-Positionen zurück (für Debugging)
     */
    public static Map<String, Set<BlockPos>> getAllWarehouses() {
        return new HashMap<>(warehouses);
    }
}
