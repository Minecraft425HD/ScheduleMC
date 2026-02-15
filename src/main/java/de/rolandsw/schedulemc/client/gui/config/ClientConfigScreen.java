package de.rolandsw.schedulemc.client.gui.config;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client Config Screen - Vehicle & Debug Settings
 */
@OnlyIn(Dist.CLIENT)
public class ClientConfigScreen extends Screen {

    private final Screen parent;
    private Button thirdPersonButton;
    private Button tempUnitButton;
    private Button debugLoggingButton;
    private VolumeSlider volumeSlider;
    private ZoomSlider zoomSlider;

    public ClientConfigScreen(Screen parent) {
        super(Component.literal("Client Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int leftColumn = this.width / 2 - 155;
        int rightColumn = this.width / 2 + 5;
        int buttonWidth = 150;
        int startY = 60;
        int spacing = 25;

        // === VEHICLE SETTINGS ===

        boolean thirdPerson = ModConfigHandler.VEHICLE_CLIENT.thirdPersonEnter.get();
        thirdPersonButton = Button.builder(
            Component.literal("Third Person: " + (thirdPerson ? "§aON" : "§cOFF")),
            button -> {
                boolean current = ModConfigHandler.VEHICLE_CLIENT.thirdPersonEnter.get();
                ModConfigHandler.VEHICLE_CLIENT.thirdPersonEnter.set(!current);
                ModConfigHandler.CLIENT_SPEC.save();
                button.setMessage(Component.literal("Third Person: " + (!current ? "§aON" : "§cOFF")));
            }
        )
        .bounds(leftColumn, startY, buttonWidth, 20)
        .build();
        this.addRenderableWidget(thirdPersonButton);

        boolean fahrenheit = ModConfigHandler.VEHICLE_CLIENT.tempInFarenheit.get();
        tempUnitButton = Button.builder(
            Component.literal("Temp: " + (fahrenheit ? "§e°F" : "§b°C")),
            button -> {
                boolean current = ModConfigHandler.VEHICLE_CLIENT.tempInFarenheit.get();
                ModConfigHandler.VEHICLE_CLIENT.tempInFarenheit.set(!current);
                ModConfigHandler.CLIENT_SPEC.save();
                button.setMessage(Component.literal("Temp: " + (!current ? "§e°F" : "§b°C")));
            }
        )
        .bounds(rightColumn, startY, buttonWidth, 20)
        .build();
        this.addRenderableWidget(tempUnitButton);

        volumeSlider = new VolumeSlider(leftColumn, startY + spacing, buttonWidth);
        this.addRenderableWidget(volumeSlider);

        zoomSlider = new ZoomSlider(rightColumn, startY + spacing, buttonWidth);
        this.addRenderableWidget(zoomSlider);

        // === DEBUG SETTINGS ===

        boolean debugEnabled = ModConfigHandler.VEHICLE_CLIENT.debugLogging.get();
        debugLoggingButton = Button.builder(
            Component.literal("Debug Log: " + (debugEnabled ? "§aON" : "§cOFF")),
            button -> {
                boolean current = ModConfigHandler.VEHICLE_CLIENT.debugLogging.get();
                ModConfigHandler.VEHICLE_CLIENT.debugLogging.set(!current);
                ModConfigHandler.CLIENT_SPEC.save();
                button.setMessage(Component.literal("Debug Log: " + (!current ? "§aON" : "§cOFF")));
            }
        )
        .bounds(this.width / 2 - 75, startY + spacing * 2, 150, 20)
        .build();
        this.addRenderableWidget(debugLoggingButton);

        // Back Button
        this.addRenderableWidget(Button.builder(
            Component.literal("« Back"),
            button -> this.minecraft.setScreen(parent)
        )
        .bounds(this.width / 2 - 100, this.height - 28, 200, 20)
        .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("§6§lVehicle Settings"),
            this.width / 2, 45, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("§c§lDebug Settings"),
            this.width / 2, 45 + 50, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private static class VolumeSlider extends AbstractSliderButton {
        public VolumeSlider(int x, int y, int width) {
            super(x, y, width, 20, Component.empty(),
                ModConfigHandler.VEHICLE_CLIENT.vehicleVolume.get());
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int percent = (int)(this.value * 100);
            this.setMessage(Component.literal("Volume: §e" + percent + "%"));
        }

        @Override
        protected void applyValue() {
            ModConfigHandler.VEHICLE_CLIENT.vehicleVolume.set(this.value);
            ModConfigHandler.CLIENT_SPEC.save();
        }
    }

    private static class ZoomSlider extends AbstractSliderButton {
        public ZoomSlider(int x, int y, int width) {
            super(x, y, width, 20, Component.empty(),
                (ModConfigHandler.VEHICLE_CLIENT.vehicleZoom.get() - 1.0) / 19.0);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double zoom = 1.0 + (this.value * 19.0);
            this.setMessage(Component.literal("Zoom: §e" + String.format("%.1f", zoom)));
        }

        @Override
        protected void applyValue() {
            double zoom = 1.0 + (this.value * 19.0);
            ModConfigHandler.VEHICLE_CLIENT.vehicleZoom.set(zoom);
            ModConfigHandler.CLIENT_SPEC.save();
        }
    }
}
