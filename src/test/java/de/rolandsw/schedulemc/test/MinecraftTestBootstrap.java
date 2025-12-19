package de.rolandsw.schedulemc.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to configure Mockito for testing Minecraft classes.
 *
 * This class ensures that Mockito is properly configured to mock Minecraft classes
 * without requiring full Minecraft Bootstrap initialization.
 *
 * Note: With Mockito's inline mock maker (configured in mockito-extensions),
 * we can mock final classes and methods without bootstrapping Minecraft.
 */
public class MinecraftTestBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftTestBootstrap.class);
    private static boolean initialized = false;

    /**
     * Initialize test environment for Minecraft classes.
     * This method is idempotent - it can be called multiple times safely.
     *
     * Currently this is a no-op, as Mockito's inline mock maker handles
     * mocking Minecraft classes automatically. This method exists to maintain
     * API compatibility and for potential future initialization needs.
     */
    public static synchronized void init() {
        if (!initialized) {
            try {
                // Mockito inline mock maker handles everything automatically
                // No need to bootstrap Minecraft for unit tests
                initialized = true;
                LOGGER.debug("Test environment initialized for Minecraft classes");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize test environment", e);
                throw new RuntimeException("Failed to initialize test environment", e);
            }
        }
    }

    /**
     * Check if test environment has been initialized.
     * @return true if initialized, false otherwise
     */
    public static boolean isBootstrapped() {
        return initialized;
    }
}
