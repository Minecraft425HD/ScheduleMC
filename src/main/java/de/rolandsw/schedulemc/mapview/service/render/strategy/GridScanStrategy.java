package de.rolandsw.schedulemc.mapview.service.render.strategy;

import java.util.function.BiConsumer;

/**
 * Grid-based scanning strategy that processes chunks in row-by-row order.
 * Scans from bottom to top, left to right.
 *
 * Part of Phase 2B refactoring - extracted from hardcoded implementation
 * to enable strategy-based scanning with different algorithms.
 */
public class GridScanStrategy implements ChunkScanStrategy {

    @Override
    public void scan(int left, int top, int right, int bottom, BiConsumer<Integer, Integer> scanner) {
        // Original grid scan: bottom to top, left to right
        for (int y = bottom; y >= top; --y) {
            for (int x = left; x <= right; ++x) {
                scanner.accept(x, y);
            }
        }
    }

    @Override
    public String getName() {
        return "Grid Scan (Bottom-Up)";
    }
}
