package de.rolandsw.schedulemc.test;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to bootstrap Minecraft for unit tests.
 *
 * This is necessary because Minecraft classes require the Bootstrap to be called
 * before they can be instantiated or mocked. Without this, you'll get errors like:
 * "Not bootstrapped (called from registry ResourceKey[minecraft:root / minecraft:root])"
 */
public class MinecraftTestBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftTestBootstrap.class);
    private static boolean bootstrapped = false;

    /**
     * Initialize Minecraft bootstrap for testing.
     * This method is idempotent - it can be called multiple times safely.
     */
    public static synchronized void init() {
        if (!bootstrapped) {
            try {
                SharedConstants.tryDetectVersion();
                Bootstrap.bootStrap();
                bootstrapped = true;
                LOGGER.info("Minecraft Bootstrap initialized for testing");
            } catch (Exception e) {
                LOGGER.error("Failed to bootstrap Minecraft for tests", e);
                throw new RuntimeException("Failed to bootstrap Minecraft", e);
            }
        }
    }

    /**
     * Check if Minecraft has been bootstrapped.
     * @return true if bootstrapped, false otherwise
     */
    public static boolean isBootstrapped() {
        return bootstrapped;
    }
}
