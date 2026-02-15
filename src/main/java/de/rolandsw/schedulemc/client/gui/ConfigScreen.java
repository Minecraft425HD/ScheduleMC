package de.rolandsw.schedulemc.client.gui;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Simple Config Screen for ScheduleMC
 * Shows the debug logging toggle and config file location
 */
@OnlyIn(Dist.CLIENT)
public class ConfigScreen extends Screen {

    private final Screen parent;
    private Button debugLoggingButton;

    public ConfigScreen(Screen parent) {
        super(Component.literal("ScheduleMC Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Get current debug logging state
        boolean debugEnabled = ModConfigHandler.VEHICLE_CLIENT.debugLogging.get();

        // Debug Logging Toggle Button (centered)
        int buttonWidth = 200;
        int buttonX = this.width / 2 - buttonWidth / 2;
        int buttonY = this.height / 2 - 30;

        debugLoggingButton = Button.builder(
            Component.literal("Debug Logging: " + (debugEnabled ? "§aON" : "§cOFF")),
            button -> {
                // Toggle debug logging
                boolean current = ModConfigHandler.VEHICLE_CLIENT.debugLogging.get();
                ModConfigHandler.VEHICLE_CLIENT.debugLogging.set(!current);
                // Save config immediately
                ModConfigHandler.CLIENT_SPEC.save();
                // Update button text
                button.setMessage(Component.literal("Debug Logging: " + (!current ? "§aON" : "§cOFF")));
            }
        )
        .bounds(buttonX, buttonY, buttonWidth, 20)
        .build();

        this.addRenderableWidget(debugLoggingButton);

        // Done Button
        this.addRenderableWidget(Button.builder(
            Component.literal("Done"),
            button -> this.minecraft.setScreen(parent)
        )
        .bounds(this.width / 2 - 100, this.height - 28, 200, 20)
        .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Title
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // Info text
        graphics.drawCenteredString(this.font,
            Component.literal("§7Client Configuration"),
            this.width / 2, 50, 0xFFFFFF);

        // Config file location
        String configPath = "config/schedulemc-client.toml";
        graphics.drawCenteredString(this.font,
            Component.literal("§8Config file: " + configPath),
            this.width / 2, this.height / 2 + 20, 0xFFFFFF);

        // Additional info
        graphics.drawCenteredString(this.font,
            Component.literal("§8For advanced settings, edit the config file"),
            this.width / 2, this.height / 2 + 35, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
