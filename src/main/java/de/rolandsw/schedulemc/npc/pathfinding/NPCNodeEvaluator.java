package de.rolandsw.schedulemc.npc.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom NodeEvaluator für NPCs
 * Erlaubt nur Bewegung auf konfigurierten Blocktypen
 * NPCs können Türen öffnen und schließen
 */
public class NPCNodeEvaluator extends WalkNodeEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NPCNodeEvaluator.class);

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState currentState = level.getBlockState(pos);

        // Türen erlauben (NPCs können sie öffnen)
        if (currentState.getBlock() instanceof DoorBlock) {
            return currentState.getValue(DoorBlock.OPEN) ? BlockPathTypes.DOOR_OPEN : BlockPathTypes.DOOR_WOOD_CLOSED;
        }

        // Prüfe Block unter den Füßen (y-1) - muss solid und walkable sein
        BlockPos below = new BlockPos(x, y - 1, z);
        BlockState stateBelow = level.getBlockState(below);

        // Prüfe ob der Block unter den Füßen zum Laufen erlaubt ist
        if (!NPCPathNavigation.isBlockWalkable(stateBelow)) {
            return BlockPathTypes.BLOCKED;
        }

        // Prüfe ob der aktuelle Block passierbar ist (Luft, Gras, etc.)
        // Der aktuelle Block darf nicht solid sein, sonst kann der NPC nicht durchgehen
        if (currentState.blocksMotion() && !(currentState.getBlock() instanceof DoorBlock)) {
            return BlockPathTypes.BLOCKED;
        }

        // Prüfe Block darüber (y+1) - muss auch passierbar sein (Höhe 2 Blöcke)
        BlockPos above = new BlockPos(x, y + 1, z);
        BlockState stateAbove = level.getBlockState(above);
        if (stateAbove.blocksMotion() && !(stateAbove.getBlock() instanceof DoorBlock)) {
            return BlockPathTypes.BLOCKED;
        }

        // Verwende Standard-Evaluation für erlaubte Blöcke
        return super.getBlockPathType(level, x, y, z);
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z, Mob mob) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState currentState = level.getBlockState(pos);

        // Türen erlauben (NPCs können sie öffnen)
        if (currentState.getBlock() instanceof DoorBlock) {
            return currentState.getValue(DoorBlock.OPEN) ? BlockPathTypes.DOOR_OPEN : BlockPathTypes.DOOR_WOOD_CLOSED;
        }

        // Prüfe Block unter den Füßen - muss solid und walkable sein
        BlockPos below = new BlockPos(x, y - 1, z);
        BlockState stateBelow = level.getBlockState(below);

        // Prüfe ob der Block zum Laufen erlaubt ist
        if (!NPCPathNavigation.isBlockWalkable(stateBelow)) {
            return BlockPathTypes.BLOCKED;
        }

        // Prüfe ob der aktuelle Block passierbar ist
        if (currentState.blocksMotion() && !(currentState.getBlock() instanceof DoorBlock)) {
            return BlockPathTypes.BLOCKED;
        }

        // Prüfe Block darüber - muss auch passierbar sein (Höhe 2 Blöcke)
        BlockPos above = new BlockPos(x, y + 1, z);
        BlockState stateAbove = level.getBlockState(above);
        if (stateAbove.blocksMotion() && !(stateAbove.getBlock() instanceof DoorBlock)) {
            return BlockPathTypes.BLOCKED;
        }

        // Verwende Standard-Evaluation für erlaubte Blöcke
        return super.getBlockPathType(level, x, y, z, mob);
    }
}
