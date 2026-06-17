package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.client.gui.config.ConfigCategoryScreen;
import de.rolandsw.schedulemc.config.DefaultConfigEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Geteilter Zustand für den "ScheduleMC Config"-Button im Welt-erstellen-Screen.
 *
 * Der Mixin ({@code CreateWorldMoreTabMixin}) setzt {@link #moreTabButtonAdded} auf true,
 * sobald er den Button erfolgreich in den "More"-Reiter eingefügt hat. Der ScreenEvent-
 * Fallback ({@code WorldCreateConfigScreenHandler}) fügt seinen Button nur ein, wenn der
 * Mixin NICHT gegriffen hat — so gibt es nie doppelte und nie gar keinen Button.
 */
public final class WorldCreateConfigState {

    public static volatile boolean moreTabButtonAdded = false;

    private WorldCreateConfigState() {
    }

    /** Öffnet die Config-Vorlage für neue Welten (Defaults-Modus). */
    public static void openConfig(Screen parent) {
        DefaultConfigEditor.begin();
        Minecraft.getInstance().setScreen(new ConfigCategoryScreen(parent, true));
    }
}
