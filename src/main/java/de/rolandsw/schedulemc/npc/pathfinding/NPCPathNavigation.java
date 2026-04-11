package de.rolandsw.schedulemc.npc.pathfinding;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom PathNavigation für NPCs
 * Erlaubt nur Bewegung auf konfigurierten Blocktypen
 */
public class NPCPathNavigation extends GroundPathNavigation {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static Set<String> allowedBlocks = ConcurrentHashMap.newKeySet();
    /** Cache: Block → Walkability-Ergebnis (vermeidet wiederholte ForgeRegistries-Lookups) */
    private static final Map<Block, Boolean> walkabilityCache = new ConcurrentHashMap<>();
    private static volatile boolean configDeferredLogged = false;

    public NPCPathNavigation(Mob mob, Level level) {
        super(mob, level);
        loadAllowedBlocks();
        // Türen öffnen und durchgehen erlauben
        this.setCanOpenDoors(true);
        this.setCanPassDoors(true);
        // Erlaube Float (Schwimmen/Fliegen falls nötig)
        this.setCanFloat(true);
        // Treppen und Stufen erlauben - wichtig für Navigation
        this.setCanWalkOverFences(false);
    }

    /**
     * Lädt die erlaubten Blöcke aus der Config
     */
    private static void loadAllowedBlocks() {
        allowedBlocks.clear();
        if (ModConfigHandler.COMMON != null && ModConfigHandler.COMMON.NPC_WALKABLE_BLOCKS != null) {
            try {
                allowedBlocks.addAll(ModConfigHandler.COMMON.NPC_WALKABLE_BLOCKS.get());
                configDeferredLogged = false;
            } catch (IllegalStateException e) {
                if (!configDeferredLogged) {
                    configDeferredLogged = true;
                    LOGGER.debug("NPCPathNavigation: config not ready yet, deferring allowedBlocks initialization", e);
                }
            }
        }
    }

    /**
     * Prüft, ob ein Block zum Laufen erlaubt ist.
     * Optimiert: Nutzt Block→Boolean Cache statt ForgeRegistries-Lookup pro Aufruf.
     */
    public static boolean isBlockWalkable(BlockState state) {
        if (allowedBlocks.isEmpty()) {
            loadAllowedBlocks();
        }

        return walkabilityCache.computeIfAbsent(state.getBlock(), block -> {
            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
            return blockId != null && allowedBlocks.contains(blockId.toString());
        });
    }

    /**
     * Prüft, ob eine Position erreichbar ist
     * Entfernt die restriktive Prüfung, um Treppen-Navigation zu ermöglichen
     */
    @Override
    protected boolean canUpdatePath() {
        return super.canUpdatePath();
    }

    /**
     * Reload Config (kann von außen aufgerufen werden)
     */
    public static void reloadConfig() {
        loadAllowedBlocks();
        walkabilityCache.clear();
    }
}
