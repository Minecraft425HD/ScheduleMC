package de.rolandsw.schedulemc.mapview.navigation.graph;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.mapview.service.data.WorldMapData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RoadBlockDetector - Erkennt Straßenblöcke für die Navigation
 *
 * Lädt konfigurierbare Blocktypen aus der Config und prüft,
 * ob ein Block als "Straße" für die Navigation gilt.
 */
public class RoadBlockDetector {

    // Gecachte Straßenblöcke für schnellen Lookup
    private static Set<Block> roadBlocks = new HashSet<>();
    private static boolean initialized = false;

    /**
     * Initialisiert den Detector mit Blöcken aus der Config
     */
    public static void initialize() {
        loadFromConfig();
        initialized = true;
    }

    /**
     * Lädt Straßenblöcke aus der Mod-Config
     */
    public static void loadFromConfig() {
        roadBlocks.clear();

        // Versuche zuerst NAVIGATION_ROAD_BLOCKS zu laden
        if (ModConfigHandler.COMMON != null && ModConfigHandler.COMMON.NAVIGATION_ROAD_BLOCKS != null) {
            List<? extends String> blockIds = ModConfigHandler.COMMON.NAVIGATION_ROAD_BLOCKS.get();
            for (String blockId : blockIds) {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                if (block != null) {
                    roadBlocks.add(block);
                }
            }
        }

        // Fallback auf NPC_WALKABLE_BLOCKS wenn Navigation-Config leer ist
        if (roadBlocks.isEmpty() && ModConfigHandler.COMMON != null && ModConfigHandler.COMMON.NPC_WALKABLE_BLOCKS != null) {
            List<? extends String> blockIds = ModConfigHandler.COMMON.NPC_WALKABLE_BLOCKS.get();
            for (String blockId : blockIds) {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                if (block != null) {
                    roadBlocks.add(block);
                }
            }
        }

        // Fallback wenn Config leer ist
        if (roadBlocks.isEmpty()) {
            loadDefaults();
        }
    }

    /**
     * Lädt Standard-Straßenblöcke als Fallback
     */
    private static void loadDefaults() {
        String[] defaults = {
            "minecraft:cobblestone",
            "minecraft:stone_bricks",
            "minecraft:gravel",
            "minecraft:dirt_path",
            "minecraft:smooth_stone",
            "minecraft:polished_andesite",
            "minecraft:polished_diorite",
            "minecraft:polished_granite",
            "minecraft:bricks",
            "minecraft:stone"
        };

        for (String blockId : defaults) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
            if (block != null) {
                roadBlocks.add(block);
            }
        }
    }

    /**
     * Prüft ob ein BlockState ein Straßenblock ist
     *
     * @param state Der zu prüfende BlockState
     * @return true wenn es ein Straßenblock ist
     */
    public static boolean isRoadBlock(BlockState state) {
        if (!initialized) {
            initialize();
        }

        if (state == null) {
            return false;
        }

        return roadBlocks.contains(state.getBlock());
    }

    /**
     * Prüft ob ein Block ein Straßenblock ist
     *
     * @param block Der zu prüfende Block
     * @return true wenn es ein Straßenblock ist
     */
    public static boolean isRoadBlock(Block block) {
        if (!initialized) {
            initialize();
        }

        return block != null && roadBlocks.contains(block);
    }

    /**
     * Prüft ob eine Position auf der Karte ein Straßenblock ist
     * Nutzt gecachte WorldMapData für Performance
     *
     * @param mapData Die WorldMapData-Instanz
     * @param x X-Koordinate
     * @param z Z-Koordinate
     * @return true wenn an dieser Position ein Straßenblock ist
     */
    public static boolean isRoadAt(WorldMapData mapData, int x, int z) {
        if (mapData == null) {
            return false;
        }

        BlockState state = mapData.getBlockStateAt(x, z);
        return isRoadBlock(state);
    }

    /**
     * Prüft ob eine BlockPos ein Straßenblock ist
     *
     * @param mapData Die WorldMapData-Instanz
     * @param pos Die zu prüfende Position
     * @return true wenn an dieser Position ein Straßenblock ist
     */
    public static boolean isRoadAt(WorldMapData mapData, BlockPos pos) {
        return isRoadAt(mapData, pos.getX(), pos.getZ());
    }

    /**
     * Gibt die Anzahl der konfigurierten Straßenblöcke zurück
     *
     * @return Anzahl der Straßenblöcke
     */
    public static int getRoadBlockCount() {
        if (!initialized) {
            initialize();
        }
        return roadBlocks.size();
    }

    /**
     * Fügt einen Straßenblock manuell hinzu (für Tests/Runtime)
     *
     * @param block Der hinzuzufügende Block
     */
    public static void addRoadBlock(Block block) {
        if (block != null) {
            roadBlocks.add(block);
        }
    }

    /**
     * Entfernt einen Straßenblock (für Tests/Runtime)
     *
     * @param block Der zu entfernende Block
     */
    public static void removeRoadBlock(Block block) {
        roadBlocks.remove(block);
    }

    /**
     * Gibt alle konfigurierten Straßenblöcke zurück
     *
     * @return Set der Straßenblöcke (unmodifiable)
     */
    public static Set<Block> getRoadBlocks() {
        if (!initialized) {
            initialize();
        }
        return Set.copyOf(roadBlocks);
    }

    /**
     * Setzt den Detector zurück (für Reload)
     */
    public static void reset() {
        roadBlocks.clear();
        initialized = false;
    }
}
