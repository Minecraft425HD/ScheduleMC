package de.rolandsw.schedulemc.npc.pathfinding;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom PathNavigation für NPCs
 * Erlaubt nur Bewegung auf konfigurierten Blocktypen
 */
public class NPCPathNavigation extends GroundPathNavigation {

    private static Set<String> allowedBlocks = new HashSet<>();

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
            allowedBlocks.addAll(ModConfigHandler.COMMON.NPC_WALKABLE_BLOCKS.get());
        }
    }

    /**
     * Prüft, ob ein Block zum Laufen erlaubt ist
     */
    public static boolean isBlockWalkable(BlockState state) {
        if (allowedBlocks.isEmpty()) {
            loadAllowedBlocks();
        }

        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId == null) {
            return false;
        }

        return allowedBlocks.contains(blockId.toString());
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
    }
}
