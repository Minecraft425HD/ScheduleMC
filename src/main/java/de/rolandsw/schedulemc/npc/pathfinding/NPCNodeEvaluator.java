package de.rolandsw.schedulemc.npc.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Custom NodeEvaluator für NPCs
 * Erlaubt nur Bewegung auf konfigurierten Blocktypen
 */
public class NPCNodeEvaluator extends WalkNodeEvaluator {

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);

        // Prüfe ob der Block zum Laufen erlaubt ist
        if (!NPCPathNavigation.isBlockWalkable(state)) {
            return BlockPathTypes.BLOCKED;
        }

        // Verwende Standard-Evaluation für erlaubte Blöcke
        return super.getBlockPathType(level, x, y, z);
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z, Mob mob) {
        BlockPos pos = new BlockPos(x, y - 1, z); // Block unter den Füßen
        BlockState state = level.getBlockState(pos);

        // Prüfe ob der Block zum Laufen erlaubt ist
        if (!NPCPathNavigation.isBlockWalkable(state)) {
            return BlockPathTypes.BLOCKED;
        }

        // Verwende Standard-Evaluation für erlaubte Blöcke
        return super.getBlockPathType(level, x, y, z, mob);
    }
}
