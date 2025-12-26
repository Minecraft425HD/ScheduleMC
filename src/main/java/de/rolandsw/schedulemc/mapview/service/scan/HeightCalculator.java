package de.rolandsw.schedulemc.mapview.service.scan;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Utility class for height-related calculations.
 */
public final class HeightCalculator {
    private HeightCalculator() {}

    /**
     * Finds a safe height at the given coordinates by searching downward from startY.
     * Returns the Y position of the first non-air block found.
     *
     * @param blockX The X coordinate
     * @param startY The Y coordinate to start searching from
     * @param blockZ The Z coordinate
     * @param level The world/level
     * @return The safe height, or startY if no solid block found
     */
    public static int getSafeHeight(int blockX, int startY, int blockZ, Level level) {
        if (level == null) {
            return startY;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(blockX, startY, blockZ);
        int minY = level.getMinBuildHeight();

        for (int y = startY; y >= minY; y--) {
            pos.setY(y);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.getBlock() != Blocks.VOID_AIR) {
                return y;
            }
        }

        return startY;
    }
}
