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
 * Stealing/Crime Config Screen - 4 Stealing Mechanics Options
 */
@OnlyIn(Dist.CLIENT)
public class StealingConfigScreen extends Screen {
    private final Screen parent;

    public StealingConfigScreen(Screen parent) {
        super(Component.literal("Stealing/Crime Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int leftCol = this.width / 2 - 155;
        int rightCol = this.width / 2 + 5;
        int w = 150;
        int y = 80;
        int s = 25;

        this.addRenderableWidget(new DoubleSlider(leftCol, y, w,
            "Indicator Speed: §e%.2f",
            ModConfigHandler.COMMON.STEALING_INDICATOR_SPEED, 0.01, 1.0));

        this.addRenderableWidget(new IntSlider(rightCol, y, w,
            "Max Attempts: §e%d",
            ModConfigHandler.COMMON.STEALING_MAX_ATTEMPTS, 1, 20));

        this.addRenderableWidget(new DoubleSlider(leftCol, y + s, w,
            "Min Zone: §e%.1f",
            ModConfigHandler.COMMON.STEALING_MIN_ZONE_SIZE, 0.1, 1.0));

        this.addRenderableWidget(new DoubleSlider(rightCol, y + s, w,
            "Max Zone: §e%.1f",
            ModConfigHandler.COMMON.STEALING_MAX_ZONE_SIZE, 0.1, 1.0));

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
            Component.literal("§7Vehicle Stealing Mechanics"),
            this.width / 2, 50, 0x808080);
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
