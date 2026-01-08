package de.rolandsw.schedulemc.mapview.service.render.strategy;

import java.util.function.BiConsumer;

/**
 * Strategy interface for chunk scanning algorithms.
 * Allows different scanning patterns to be used interchangeably.
 *
 * Part of Phase 2B refactoring to reduce structural similarity
 * by introducing Strategy Pattern instead of hardcoded algorithms.
 */
@FunctionalInterface
public interface ChunkScanStrategy {

    /**
     * Scans a rectangular region using a specific scanning pattern.
     *
     * @param left   Left boundary of the scan region
     * @param top    Top boundary of the scan region
     * @param right  Right boundary of the scan region
     * @param bottom Bottom boundary of the scan region
     * @param scanner Callback function to process each (x, y) coordinate
     */
    void scan(int left, int top, int right, int bottom, BiConsumer<Integer, Integer> scanner);

    /**
     * Gets a human-readable name for this scanning strategy.
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
