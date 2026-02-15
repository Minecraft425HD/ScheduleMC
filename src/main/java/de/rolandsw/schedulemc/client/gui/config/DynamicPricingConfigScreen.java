package de.rolandsw.schedulemc.client.gui.config;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DynamicPricingConfigScreen extends Screen {
    private final Screen parent;

    public DynamicPricingConfigScreen(Screen parent) {
        super(Component.literal("Dynamic Pricing (UDPS)"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int leftCol = this.width / 2 - 155;
        int rightCol = this.width / 2 + 5;
        int w = 150;
        int y = 60;
        int s = 25;

        boolean enabled = ModConfigHandler.COMMON.DYNAMIC_PRICING_ENABLED.get();
        this.addRenderableWidget(Button.builder(
            Component.literal("UDPS: " + (enabled ? "§aENABLED" : "§cDISABLED")),
            button -> {
                boolean c = ModConfigHandler.COMMON.DYNAMIC_PRICING_ENABLED.get();
                ModConfigHandler.COMMON.DYNAMIC_PRICING_ENABLED.set(!c);
                ModConfigHandler.SPEC.save();
                button.setMessage(Component.literal("UDPS: " + (!c ? "§aENABLED" : "§cDISABLED")));
            }
        ).bounds(this.width / 2 - 75, y, 150, 20).build());

        this.addRenderableWidget(new DoubleSlider(leftCol, y + s, w,
            "SD Factor: §e%.1f",
            ModConfigHandler.COMMON.DYNAMIC_PRICING_SD_FACTOR, 0, 1.0));

        this.addRenderableWidget(new DoubleSlider(rightCol, y + s, w,
            "Min Mult: §e%.1fx",
            ModConfigHandler.COMMON.DYNAMIC_PRICING_MIN_MULTIPLIER, 0.1, 1.0));

        this.addRenderableWidget(new DoubleSlider(leftCol, y + s * 2, w,
            "Max Mult: §e%.1fx",
            ModConfigHandler.COMMON.DYNAMIC_PRICING_MAX_MULTIPLIER, 1.0, 20.0));

        this.addRenderableWidget(new IntSlider(rightCol, y + s * 2, w,
            "Update: §e%dmin",
            ModConfigHandler.COMMON.DYNAMIC_PRICING_UPDATE_INTERVAL_MINUTES, 1, 30));

        this.addRenderableWidget(Button.builder(Component.literal("« Back"),
            button -> this.minecraft.setScreen(parent))
            .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("§7Unified Dynamic Pricing System"),
            this.width / 2, 40, 0x808080);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

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
