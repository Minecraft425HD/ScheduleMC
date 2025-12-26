package de.rolandsw.schedulemc.mapview.service.render.strategy;

import java.util.function.BiConsumer;

/**
 * Spiral-based scanning strategy that processes chunks from center outward.
 * Scans in a clockwise spiral pattern starting from the center of the region.
 *
 * Part of Phase 2B refactoring - alternative algorithm to reduce structural
 * similarity with VoxelMap's grid-based approach.
 *
 * Benefits:
 * - Processes center (player vicinity) first
 * - More cache-friendly for centered rendering
 * - Completely different pattern than grid scan
 */
public class SpiralScanStrategy implements ChunkScanStrategy {

    @Override
    public void scan(int left, int top, int right, int bottom, BiConsumer<Integer, Integer> scanner) {
        int width = right - left + 1;
        int height = bottom - top + 1;

        // Start from center
        int centerX = left + width / 2;
        int centerY = top + height / 2;

        // Spiral outward
        int x = centerX;
        int y = centerY;
        int dx = 0;
        int dy = -1;

        int maxSteps = Math.max(width, height) * Math.max(width, height);

        for (int i = 0; i < maxSteps; i++) {
            // Process current coordinate if within bounds
            if (x >= left && x <= right && y >= top && y <= bottom) {
                scanner.accept(x, y);
            }

            // Spiral pattern logic
            if (x == y || (x < 0 && x == -y) || (x > 0 && x == 1 - y)) {
                // Change direction
                int temp = dx;
                dx = -dy;
                dy = temp;
            }

            x += dx;
            y += dy;
        }
    }

    @Override
    public String getName() {
        return "Spiral Scan (Center-Out)";
    }
}
