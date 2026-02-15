package de.rolandsw.schedulemc.client.gui.config;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * NPC & Navigation Config Screen
 */
@OnlyIn(Dist.CLIENT)
public class NPCConfigScreen extends Screen {
    private final Screen parent;

    public NPCConfigScreen(Screen parent) {
        super(Component.literal("NPC & Navigation Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int y = 70;
        int s = 25;
        int w = 200;

        // NAVIGATION SETTINGS
        this.addRenderableWidget(new IntSlider(centerX - w/2, y, w,
            "Scan Radius: §e%d blocks",
            ModConfigHandler.COMMON.NAVIGATION_SCAN_RADIUS, 5, 100));

        this.addRenderableWidget(new IntSlider(centerX - w/2, y + s, w,
            "Path Update: §e%d ticks",
            ModConfigHandler.COMMON.NAVIGATION_PATH_UPDATE_INTERVAL, 1, 100));

        this.addRenderableWidget(new DoubleSlider(centerX - w/2, y + s * 2, w,
            "Arrival Distance: §e%.1f blocks",
            ModConfigHandler.COMMON.NAVIGATION_ARRIVAL_DISTANCE, 0.5, 5.0));

        // Back Button
        this.addRenderableWidget(Button.builder(
            Component.literal("« Back"),
            button -> this.minecraft.setScreen(parent)
        ).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("§7NPC Pathfinding & Navigation"),
            this.width / 2, 40, 0x808080);

        // Info about block lists
        graphics.drawCenteredString(this.font,
            Component.literal("§8NPC walkable blocks & road blocks:"),
            this.width / 2, this.height - 70, 0x808080);
        graphics.drawCenteredString(this.font,
            Component.literal("§7Edit in config/schedulemc-common.toml"),
            this.width / 2, this.height - 55, 0x808080);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    // === SLIDER CLASSES ===

    private static class DoubleSlider extends AbstractSliderButton {
        private final net.minecraftforge.common.ForgeConfigSpec.DoubleValue config;
        private final double min, max;
        private final String format;

        public DoubleSlider(int x, int y, int w, String format,
                           net.minecraftforge.common.ForgeConfigSpec.DoubleValue config,
                           double min, double max) {
            super(x, y, w, 20, Component.empty(), (config.get() - min) / (max - min));
            this.config = config;
            this.min = min;
            this.max = max;
            this.format = format;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double val = min + (value * (max - min));
            this.setMessage(Component.literal(String.format(format, val)));
        }

        @Override
        protected void applyValue() {
            double val = min + (value * (max - min));
            config.set(val);
            ModConfigHandler.SPEC.save();
        }
    }

    private static class IntSlider extends AbstractSliderButton {
        private final net.minecraftforge.common.ForgeConfigSpec.IntValue config;
        private final int min, max;
        private final String format;

        public IntSlider(int x, int y, int w, String format,
                        net.minecraftforge.common.ForgeConfigSpec.IntValue config,
                        int min, int max) {
            super(x, y, w, 20, Component.empty(), (double)(config.get() - min) / (max - min));
            this.config = config;
            this.min = min;
            this.max = max;
            this.format = format;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int val = min + (int)(value * (max - min));
            this.setMessage(Component.literal(String.format(format, val)));
        }

        @Override
        protected void applyValue() {
            int val = min + (int)(value * (max - min));
            config.set(val);
            ModConfigHandler.SPEC.save();
        }
    }
}
