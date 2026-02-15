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
 * Economy Config Screen
 */
@OnlyIn(Dist.CLIENT)
public class EconomyConfigScreen extends Screen {

    private final Screen parent;

    public EconomyConfigScreen(Screen parent) {
        super(Component.literal("Economy Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int leftCol = this.width / 2 - 155;
        int rightCol = this.width / 2 + 5;
        int w = 150;
        int y = 50;
        int s = 25;

        // Start Balance Slider
        this.addRenderableWidget(new DoubleSlider(leftCol, y, w,
            "Start Balance: §e%.0f€",
            ModConfigHandler.COMMON.START_BALANCE, 0, 10000));

        // Save Interval Slider
        this.addRenderableWidget(new IntSlider(rightCol, y, w,
            "Save Interval: §e%dmin",
            ModConfigHandler.COMMON.SAVE_INTERVAL_MINUTES, 1, 60));

        // Daily Reward Slider
        this.addRenderableWidget(new DoubleSlider(leftCol, y + s, w,
            "Daily Reward: §e%.0f€",
            ModConfigHandler.COMMON.DAILY_REWARD, 1, 1000));

        // Streak Bonus Slider
        this.addRenderableWidget(new DoubleSlider(rightCol, y + s, w,
            "Streak Bonus: §e%.0f€",
            ModConfigHandler.COMMON.DAILY_REWARD_STREAK_BONUS, 0, 500));

        // Max Streak Days
        this.addRenderableWidget(new IntSlider(leftCol, y + s * 2, w,
            "Max Streak: §e%d days",
            ModConfigHandler.COMMON.MAX_STREAK_DAYS, 1, 365));

        // Savings Interest Rate
        this.addRenderableWidget(new DoubleSlider(rightCol, y + s * 2, w,
            "Savings Rate: §e%.1f%%",
            ModConfigHandler.COMMON.SAVINGS_INTEREST_RATE, 0, 0.5, 100));

        // Overdraft Interest Rate
        this.addRenderableWidget(new DoubleSlider(leftCol, y + s * 3, w,
            "Overdraft Rate: §e%.1f%%",
            ModConfigHandler.COMMON.OVERDRAFT_INTEREST_RATE, 0, 1.0, 100));

        // Tax Property per Chunk
        this.addRenderableWidget(new DoubleSlider(rightCol, y + s * 3, w,
            "Property Tax: §e%.0f€",
            ModConfigHandler.COMMON.TAX_PROPERTY_PER_CHUNK, 0, 1000));

        // Sales Tax Rate
        this.addRenderableWidget(new DoubleSlider(leftCol, y + s * 4, w,
            "Sales Tax: §e%.0f%%",
            ModConfigHandler.COMMON.TAX_SALES_RATE, 0, 1.0, 100));

        // Buy Multiplier
        this.addRenderableWidget(new DoubleSlider(rightCol, y + s * 4, w,
            "Buy Mult: §e%.1fx",
            ModConfigHandler.COMMON.BUY_MULTIPLIER, 0.1, 10.0));

        // Sell Multiplier
        this.addRenderableWidget(new DoubleSlider(leftCol, y + s * 5, w,
            "Sell Mult: §e%.1fx",
            ModConfigHandler.COMMON.SELL_MULTIPLIER, 0.1, 10.0));

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
            Component.literal("§7Economy & Money System"),
            this.width / 2, 35, 0x808080);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    // Generic Double Slider
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
            super(x, y, w, 20, Component.empty(),
                (config.get() - min) / (max - min));
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

    // Generic Int Slider
    private static class IntSlider extends AbstractSliderButton {
        private final net.minecraftforge.common.ForgeConfigSpec.IntValue config;
        private final int min, max;
        private final String format;

        public IntSlider(int x, int y, int w, String format,
                        net.minecraftforge.common.ForgeConfigSpec.IntValue config,
                        int min, int max) {
            super(x, y, w, 20, Component.empty(),
                (double)(config.get() - min) / (max - min));
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
