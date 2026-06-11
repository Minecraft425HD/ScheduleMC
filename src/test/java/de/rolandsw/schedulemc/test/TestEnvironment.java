package de.rolandsw.schedulemc.test;

import com.electronwill.nightconfig.core.CommentedConfig;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

/**
 * Zentrale Testumgebung: bootstrappt Vanilla-Registries (für Item/Player-
 * Mocks bzw. echte ItemStacks) und lädt die Forge-Config-Specs mit ihren
 * Default-Werten, damit Config-Zugriffe in Unit-Tests nicht fehlschlagen.
 */
public final class TestEnvironment {

    private static boolean initialized = false;

    private TestEnvironment() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        try {
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        } catch (Throwable t) {
            // Forge-spezifischer Tail (NetworkHooks.init) schlägt ohne FML-Umgebung
            // fehl — die Vanilla-Registries sind zu diesem Zeitpunkt bereits
            // initialisiert, was für Unit-Tests ausreicht.
        }
        loadConfigDefaults();
        initialized = true;
    }

    private static void loadConfigDefaults() {
        acceptDefaults(ModConfigHandler.SPEC);
        acceptDefaults(ModConfigHandler.CLIENT_SPEC);
    }

    private static void acceptDefaults(net.minecraftforge.common.ForgeConfigSpec spec) {
        CommentedConfig config = CommentedConfig.inMemory();
        spec.correct(config);
        spec.acceptConfig(config);
    }
}
