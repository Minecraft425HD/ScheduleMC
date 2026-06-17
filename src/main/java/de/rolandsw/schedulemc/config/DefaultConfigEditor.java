package de.rolandsw.schedulemc.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.weapon.config.WeaponConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Erlaubt das Bearbeiten der Pro-Welt-Config-VORLAGE für NEUE Welten direkt im
 * Welt-erstellen-Screen — also bevor eine Welt (und damit die SERVER-Config) geladen ist.
 *
 * Dazu werden die {@code defaultconfigs/}-Dateien (Forges Vorlage für neue Welten) an die
 * SERVER-Specs gebunden, sodass die bestehenden Config-Screens sie ganz normal editieren.
 * Forge initialisiert jede neu erstellte Welt aus diesen Vorlagen.
 */
public final class DefaultConfigEditor {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile boolean active = false;
    private static final List<CommentedFileConfig> openConfigs = new ArrayList<>();

    private DefaultConfigEditor() {
    }

    /** True, solange die Vorlagen gebunden sind (Config-Screens dürfen dann editieren). */
    public static boolean isActive() {
        return active;
    }

    /** Bindet die defaultconfigs/-Vorlagen an die SERVER-Specs (idempotent). */
    public static synchronized void begin() {
        if (active) {
            return;
        }
        try {
            Path dir = FMLPaths.GAMEDIR.get().resolve("defaultconfigs");
            Files.createDirectories(dir);
            bind(ModConfigHandler.SPEC, dir.resolve("schedulemc-server.toml"));
            bind(WeaponConfig.SPEC, dir.resolve("schedulemc-weapons.toml"));
            active = true;
        } catch (Throwable t) {
            LOGGER.error("Could not open new-world default config editor", t);
            cleanup();
            active = false;
        }
    }

    private static void bind(ForgeConfigSpec spec, Path file) {
        CommentedFileConfig cfg = CommentedFileConfig.builder(file)
            .sync()
            .writingMode(WritingMode.REPLACE)
            .build();
        cfg.load();
        // setConfig korrigiert die Config intern (füllt fehlende Schlüssel mit Defaults);
        // ab jetzt liefern/speichern .get()/.set() gegen diese Vorlage.
        spec.setConfig(cfg);
        cfg.save(); // vollständige Vorlage (inkl. Defaults) auf Platte schreiben
        openConfigs.add(cfg);
    }

    /** Speichert die Vorlagen und löst die Bindung wieder (zurück in den "nicht geladen"-Zustand). */
    public static synchronized void end() {
        if (!active) {
            return;
        }
        for (CommentedFileConfig cfg : openConfigs) {
            try {
                cfg.save();
            } catch (Exception ignored) {
                // best effort
            }
        }
        // Bindung lösen, damit ein echter Weltladevorgang sauber neu bindet.
        unbind(ModConfigHandler.SPEC);
        unbind(WeaponConfig.SPEC);
        cleanup();
        active = false;
    }

    private static void unbind(ForgeConfigSpec spec) {
        try {
            spec.setConfig(null);
        } catch (Exception ignored) {
            // Falls Forge null nicht akzeptiert: Bindung bleibt bis zum Weltladen bestehen (unkritisch).
        }
    }

    private static void cleanup() {
        for (CommentedFileConfig cfg : openConfigs) {
            try {
                cfg.close();
            } catch (Exception ignored) {
                // best effort
            }
        }
        openConfigs.clear();
    }
}
