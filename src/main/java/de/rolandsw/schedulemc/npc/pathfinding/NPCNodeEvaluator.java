package de.rolandsw.schedulemc.npc.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Custom NodeEvaluator für NPCs
 * Erlaubt nur Bewegung auf konfigurierten Blocktypen
 * NPCs können Türen öffnen und schließen
 */
public class NPCNodeEvaluator extends WalkNodeEvaluator {

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);

        // Türen erlauben (NPCs können sie öffnen)
        if (state.getBlock() instanceof DoorBlock) {
            return state.getValue(DoorBlock.OPEN) ? BlockPathTypes.DOOR_OPEN : BlockPathTypes.DOOR_WOOD_CLOSED;
        }

        // Prüfe Block unter den Füßen (y-1)
        BlockPos below = new BlockPos(x, y - 1, z);
        BlockState stateBelow = level.getBlockState(below);

        // Prüfe ob der Block unter den Füßen zum Laufen erlaubt ist
        if (!NPCPathNavigation.isBlockWalkable(stateBelow)) {
            return BlockPathTypes.BLOCKED;
        }

        // Verwende Standard-Evaluation für erlaubte Blöcke
        return super.getBlockPathType(level, x, y, z);
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z, Mob mob) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);

        // Türen erlauben (NPCs können sie öffnen)
        if (state.getBlock() instanceof DoorBlock) {
            return state.getValue(DoorBlock.OPEN) ? BlockPathTypes.DOOR_OPEN : BlockPathTypes.DOOR_WOOD_CLOSED;
        }

        // Prüfe Block unter den Füßen
        BlockPos below = new BlockPos(x, y - 1, z);
        BlockState stateBelow = level.getBlockState(below);

        // Prüfe ob der Block zum Laufen erlaubt ist
        if (!NPCPathNavigation.isBlockWalkable(stateBelow)) {
            return BlockPathTypes.BLOCKED;
        }

        // Verwende Standard-Evaluation für erlaubte Blöcke
        return super.getBlockPathType(level, x, y, z, mob);
    }
}
