package de.rolandsw.schedulemc.mapview.service.render.strategy;

/**
 * Factory for creating chunk scanning strategy instances.
 * Provides centralized access to different scanning algorithms.
 *
 * Part of Phase 2B refactoring - enables flexible algorithm selection
 * without hardcoded implementation dependencies.
 */
public class ChunkScanStrategyFactory {

    /**
     * Available scanning strategies.
     */
    public enum StrategyType {
        GRID,    // Traditional grid scan (bottom-up, left-right)
        SPIRAL   // Spiral scan from center outward
    }

    // Default strategy
    private static ChunkScanStrategy defaultStrategy = new GridScanStrategy();

    /**
     * Gets the current default scanning strategy.
     * Can be configured by mods or configuration system.
     */
    public static ChunkScanStrategy getDefault() {
        return defaultStrategy;
    }

    /**
     * Sets the default scanning strategy.
     * Allows runtime strategy switching.
     */
    public static void setDefault(ChunkScanStrategy strategy) {
        if (strategy != null) {
            defaultStrategy = strategy;
        }
    }

    /**
     * Creates a strategy instance by type.
     */
    public static ChunkScanStrategy create(StrategyType type) {
        return switch (type) {
            case GRID -> new GridScanStrategy();
            case SPIRAL -> new SpiralScanStrategy();
        };
    }
}
