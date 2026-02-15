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
 * Tobacco Config Screen - 31 Tobacco System Options (Compact Layout)
 */
@OnlyIn(Dist.CLIENT)
public class TobaccoConfigScreen extends Screen {
    private final Screen parent;

    public TobaccoConfigScreen(Screen parent) {
        super(Component.literal("Tobacco Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int col1 = 10;
        int col2 = 110;
        int col3 = 210;
        int col4 = 310;
        int w = 95;
        int y = 45;
        int s = 22;

        // Row 1: General
        addWidget(new BoolButton(col1, y, w, "Enabled",
            ModConfigHandler.TOBACCO.TOBACCO_ENABLED));
        addWidget(new DoubleSlider(col2, y, w, "Grow:%.1fx",
            ModConfigHandler.TOBACCO.TOBACCO_GROWTH_SPEED_MULTIPLIER, 0.1, 10.0));
        addWidget(new IntSlider(col3, y, w, "Dry:%dt",
            ModConfigHandler.TOBACCO.TOBACCO_DRYING_TIME, 100, 72000));
        addWidget(new IntSlider(col4, y, w, "Ferm:%dt",
            ModConfigHandler.TOBACCO.TOBACCO_FERMENTING_TIME, 100, 72000));
        y += s;

        // Row 2: Quality
        addWidget(new DoubleSlider(col1, y, w, "FermQ:%.0f%%",
            ModConfigHandler.TOBACCO.FERMENTATION_QUALITY_CHANCE, 0, 1.0, 100));
        y += s;

        // Row 3-4: Drying Racks
        addWidget(new IntSlider(col1, y, w, "RackS:%d",
            ModConfigHandler.TOBACCO.SMALL_DRYING_RACK_CAPACITY, 1, 64));
        addWidget(new IntSlider(col2, y, w, "RackM:%d",
            ModConfigHandler.TOBACCO.MEDIUM_DRYING_RACK_CAPACITY, 1, 64));
        addWidget(new IntSlider(col3, y, w, "RackL:%d",
            ModConfigHandler.TOBACCO.BIG_DRYING_RACK_CAPACITY, 1, 64));
        y += s;

        // Row 5-6: Fermentation Barrels
        addWidget(new IntSlider(col1, y, w, "BarrelS:%d",
            ModConfigHandler.TOBACCO.SMALL_FERMENTATION_BARREL_CAPACITY, 1, 64));
        addWidget(new IntSlider(col2, y, w, "BarrelM:%d",
            ModConfigHandler.TOBACCO.MEDIUM_FERMENTATION_BARREL_CAPACITY, 1, 64));
        addWidget(new IntSlider(col3, y, w, "BarrelL:%d",
            ModConfigHandler.TOBACCO.BIG_FERMENTATION_BARREL_CAPACITY, 1, 64));
        y += s;

        // Row 7-8: Pot Water Capacities
        addWidget(new IntSlider(col1, y, w, "TerraW:%d",
            ModConfigHandler.TOBACCO.TERRACOTTA_WATER_CAPACITY, 10, 10000));
        addWidget(new IntSlider(col2, y, w, "CeramW:%d",
            ModConfigHandler.TOBACCO.CERAMIC_WATER_CAPACITY, 10, 10000));
        addWidget(new IntSlider(col3, y, w, "IronW:%d",
            ModConfigHandler.TOBACCO.IRON_WATER_CAPACITY, 10, 10000));
        addWidget(new IntSlider(col4, y, w, "GoldW:%d",
            ModConfigHandler.TOBACCO.GOLDEN_WATER_CAPACITY, 10, 10000));
        y += s;

        // Row 9-10: Pot Soil Capacities
        addWidget(new IntSlider(col1, y, w, "TerraS:%d",
            ModConfigHandler.TOBACCO.TERRACOTTA_SOIL_CAPACITY, 10, 10000));
        addWidget(new IntSlider(col2, y, w, "CeramS:%d",
            ModConfigHandler.TOBACCO.CERAMIC_SOIL_CAPACITY, 10, 10000));
        addWidget(new IntSlider(col3, y, w, "IronS:%d",
            ModConfigHandler.TOBACCO.IRON_SOIL_CAPACITY, 10, 10000));
        addWidget(new IntSlider(col4, y, w, "GoldS:%d",
            ModConfigHandler.TOBACCO.GOLDEN_SOIL_CAPACITY, 10, 10000));
        y += s;

        // Row 11: Bottle Effects
        addWidget(new DoubleSlider(col1, y, w, "Fert:%.1fx",
            ModConfigHandler.TOBACCO.FERTILIZER_YIELD_BONUS, 0, 5.0));
        addWidget(new DoubleSlider(col2, y, w, "Boost:%.1fx",
            ModConfigHandler.TOBACCO.GROWTH_BOOSTER_SPEED_MULTIPLIER, 1.0, 10.0));
        y += s;

        // Row 12: Light Requirements
        addWidget(new BoolButton(col1, y, w, "NeedLight",
            ModConfigHandler.TOBACCO.REQUIRE_LIGHT_FOR_GROWTH));
        addWidget(new IntSlider(col2, y, w, "MinLvl:%d",
            ModConfigHandler.TOBACCO.MIN_LIGHT_LEVEL, 0, 15));
        y += s;

        // Row 13-14: Basic Grow Light
        addWidget(new IntSlider(col1, y, w, "BasicL:%d",
            ModConfigHandler.TOBACCO.BASIC_GROW_LIGHT_LEVEL, 0, 15));
        addWidget(new DoubleSlider(col2, y, w, "BasicS:%.1fx",
            ModConfigHandler.TOBACCO.BASIC_GROW_LIGHT_SPEED, 0.1, 10.0));
        y += s;

        // Row 15-16: Advanced Grow Light
        addWidget(new IntSlider(col1, y, w, "AdvL:%d",
            ModConfigHandler.TOBACCO.ADVANCED_GROW_LIGHT_LEVEL, 0, 15));
        addWidget(new DoubleSlider(col2, y, w, "AdvS:%.2fx",
            ModConfigHandler.TOBACCO.ADVANCED_GROW_LIGHT_SPEED, 0.1, 10.0));
        y += s;

        // Row 17-19: Premium Grow Light
        addWidget(new IntSlider(col1, y, w, "PremL:%d",
            ModConfigHandler.TOBACCO.PREMIUM_GROW_LIGHT_LEVEL, 0, 15));
        addWidget(new DoubleSlider(col2, y, w, "PremS:%.1fx",
            ModConfigHandler.TOBACCO.PREMIUM_GROW_LIGHT_SPEED, 0.1, 10.0));
        addWidget(new DoubleSlider(col3, y, w, "PremQ:%.0f%%",
            ModConfigHandler.TOBACCO.PREMIUM_GROW_LIGHT_QUALITY_BONUS, 0, 1.0, 100));

        // Back Button
        this.addRenderableWidget(Button.builder(
            Component.literal("« Back"),
            button -> this.minecraft.setScreen(parent)
        ).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private void addWidget(AbstractSliderButton widget) {
        this.addRenderableWidget(widget);
    }

    private void addWidget(Button widget) {
        this.addRenderableWidget(widget);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("§231 Tobacco Options - Complete Control!"),
            this.width / 2, 22, 0x55FF55);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    // === SLIDER & BUTTON CLASSES ===

    private static class DoubleSlider extends AbstractSliderButton {
        private final net.minecraftforge.common.ForgeConfigSpec.DoubleValue config;
        private final double min, max, multiplier;
        private final String format;

        public DoubleSlider(int x, int y, int w, String format,
                           net.minecraftforge.common.ForgeConfigSpec.DoubleValue config,
                           double min, double max) {
            this(x, y, w, format, config, min, max, 1.0);
        }

        public DoubleSlider(int x, int y, int w, String format,
                           net.minecraftforge.common.ForgeConfigSpec.DoubleValue config,
                           double min, double max, double multiplier) {
            super(x, y, w, 20, Component.empty(), (config.get() - min) / (max - min));
            this.config = config;
            this.min = min;
            this.max = max;
            this.multiplier = multiplier;
            this.format = format;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double val = min + (value * (max - min));
            this.setMessage(Component.literal(String.format(format, val * multiplier)));
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

    private static class BoolButton extends Button {
        private final net.minecraftforge.common.ForgeConfigSpec.BooleanValue config;
        private final String label;

        public BoolButton(int x, int y, int w, String label,
                         net.minecraftforge.common.ForgeConfigSpec.BooleanValue config) {
            super(x, y, w, 20, Component.empty(), btn -> {}, DEFAULT_NARRATION);
            this.config = config;
            this.label = label;
            updateMessage();
        }

        private void updateMessage() {
            boolean val = config.get();
            this.setMessage(Component.literal(label + ":" + (val ? "§aON" : "§cOFF")));
        }

        @Override
        public void onPress() {
            config.set(!config.get());
            ModConfigHandler.SPEC.save();
            updateMessage();
        }
    }
}
