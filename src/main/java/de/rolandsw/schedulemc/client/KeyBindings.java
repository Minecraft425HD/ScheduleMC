package de.rolandsw.schedulemc.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Registrierung aller Custom Keybindings für ScheduleMC
 */
public class KeyBindings {

    public static final String CATEGORY = "key.categories.schedulemc";

    // Keybinding für das Smartphone
    public static final KeyMapping OPEN_SMARTPHONE = new KeyMapping(
        "key.schedulemc.open_smartphone",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_P, // Standard: P-Taste
        CATEGORY
    );

    public static void register() {
        // Keybindings werden automatisch durch die Annotation registriert
        // Diese Methode dient als Referenzpunkt für spätere Erweiterungen
    }
}
