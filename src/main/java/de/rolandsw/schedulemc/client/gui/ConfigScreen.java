package de.rolandsw.schedulemc.client.gui;

import de.rolandsw.schedulemc.client.gui.config.ConfigCategoryScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Config Screen Entry Point - Redirects to Category Selection
 * This class maintains compatibility with existing mod menu registration
 */
@OnlyIn(Dist.CLIENT)
public class ConfigScreen extends ConfigCategoryScreen {

    public ConfigScreen(Screen parent) {
        super(parent);
    }
}
