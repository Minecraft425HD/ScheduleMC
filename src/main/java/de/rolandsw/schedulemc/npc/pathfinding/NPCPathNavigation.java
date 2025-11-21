package de.rolandsw.schedulemc.npc.pathfinding;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom PathNavigation für NPCs
 * Erlaubt nur Bewegung auf konfigurierten Blocktypen
 * NPCs können Türen öffnen und schließen
 */
public class NPCPathNavigation extends GroundPathNavigation {

    private static Set<String> allowedBlocks = new HashSet<>();

    public NPCPathNavigation(Mob mob, Level level) {
        super(mob, level);
        loadAllowedBlocks();
        // Erlaube NPCs das Öffnen von Türen
        this.setCanOpenDoors(true);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new NPCNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true); // NPCs können durch Türen gehen
        this.nodeEvaluator.setCanOpenDoors(true); // NPCs können Türen öffnen
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
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
     */
    @Override
    protected boolean canUpdatePath() {
        // Nur aktualisieren wenn der NPC auf einem erlaubten Block steht
        BlockPos below = this.mob.blockPosition().below();
        BlockState state = this.level.getBlockState(below);
        return isBlockWalkable(state) && super.canUpdatePath();
    }

    /**
     * Reload Config (kann von außen aufgerufen werden)
     */
    public static void reloadConfig() {
        loadAllowedBlocks();
    }
}
