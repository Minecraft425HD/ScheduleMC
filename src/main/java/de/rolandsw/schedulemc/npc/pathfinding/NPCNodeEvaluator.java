package de.rolandsw.schedulemc.npc.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Custom NodeEvaluator für NPCs
 * Erlaubt nur Bewegung auf konfigurierten Blocktypen
 * Unterstützt Treppen und Türen
 */
public class NPCNodeEvaluator extends WalkNodeEvaluator {

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);

        // Erlaube Treppen immer (alle Typen)
        if (state.getBlock() instanceof StairBlock) {
            return BlockPathTypes.WALKABLE;
        }

        // Erlaube alle Türen (offen und geschlossen)
        if (state.getBlock() instanceof DoorBlock) {
            return BlockPathTypes.DOOR_OPEN;
        }

        // Erlaube Falltüren
        if (state.getBlock() instanceof TrapDoorBlock) {
            return BlockPathTypes.TRAPDOOR;
        }

        // Erlaube Zäune/Tore (NPCs können darüber springen wenn nötig)
        if (state.getBlock() instanceof FenceBlock || state.getBlock() instanceof FenceGateBlock) {
            return BlockPathTypes.FENCE;
        }

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

        // Aktuelle Position (für Türprüfung)
        BlockPos currentPos = new BlockPos(x, y, z);
        BlockState currentState = level.getBlockState(currentPos);

        // Erlaube Treppen immer
        if (state.getBlock() instanceof StairBlock) {
            return BlockPathTypes.WALKABLE;
        }

        // Erlaube alle Türen
        if (currentState.getBlock() instanceof DoorBlock) {
            return BlockPathTypes.DOOR_OPEN;
        }

        // Erlaube Falltüren
        if (currentState.getBlock() instanceof TrapDoorBlock) {
            return BlockPathTypes.TRAPDOOR;
        }

        // Prüfe ob der Block zum Laufen erlaubt ist
        if (!NPCPathNavigation.isBlockWalkable(state)) {
            return BlockPathTypes.BLOCKED;
        }

        // Verwende Standard-Evaluation für erlaubte Blöcke
        return super.getBlockPathType(level, x, y, z, mob);
    }
}
