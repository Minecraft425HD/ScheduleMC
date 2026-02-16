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
 * Advanced Economy Config Screen - 34 Advanced Economy Options
 * Includes: Rent, Shop, Ratings, Bank, Stock Market, Economy Cycle,
 *          Level System, Risk Premium, Anti-Exploit
 */
@OnlyIn(Dist.CLIENT)
public class AdvancedEconomyConfigScreen extends Screen {
    private final Screen parent;

    public AdvancedEconomyConfigScreen(Screen parent) {
        super(Component.literal("Advanced Economy"));
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

        // === RENT SYSTEM ===
        addWidget(new BoolButton(col1, y, w, "Rent",
            ModConfigHandler.COMMON.RENT_ENABLED));
        addWidget(new DoubleSlider(col2, y, w, "MinP:%.0f€",
            ModConfigHandler.COMMON.MIN_RENT_PRICE, 1, 10000));
        addWidget(new IntSlider(col3, y, w, "MinD:%d",
            ModConfigHandler.COMMON.MIN_RENT_DAYS, 1, 365));
        addWidget(new IntSlider(col4, y, w, "MaxD:%d",
            ModConfigHandler.COMMON.MAX_RENT_DAYS, 1, 365));
        y += s;

        // === SHOP SYSTEM ===
        addWidget(new BoolButton(col1, y, w, "Shop",
            ModConfigHandler.COMMON.SHOP_ENABLED));
        y += s;

        // === RATINGS SYSTEM ===
        addWidget(new BoolButton(col1, y, w, "Ratings",
            ModConfigHandler.COMMON.RATINGS_ENABLED));
        addWidget(new BoolButton(col2, y, w, "Multi",
            ModConfigHandler.COMMON.ALLOW_MULTIPLE_RATINGS));
        addWidget(new IntSlider(col3, y, w, "Min:%d",
            ModConfigHandler.COMMON.MIN_RATING, 1, 5));
        addWidget(new IntSlider(col4, y, w, "Max:%d",
            ModConfigHandler.COMMON.MAX_RATING, 1, 10));
        y += s;

        // === BANK SYSTEM ===
        addWidget(new DoubleSlider(col1, y, w, "BankDep:%.0f€",
            ModConfigHandler.COMMON.BANK_DEPOSIT_LIMIT, 1000, 10000000));
        addWidget(new DoubleSlider(col2, y, w, "Transfer:%.0f€",
            ModConfigHandler.COMMON.BANK_TRANSFER_DAILY_LIMIT, 1000, 10000000));
        y += s;

        // === STOCK MARKET ===
        addWidget(new DoubleSlider(col1, y, w, "Gold:%.0f€",
            ModConfigHandler.COMMON.STOCK_GOLD_BASE_PRICE, 1, 10000));
        addWidget(new DoubleSlider(col2, y, w, "Dia:%.0f€",
            ModConfigHandler.COMMON.STOCK_DIAMOND_BASE_PRICE, 1, 10000));
        addWidget(new DoubleSlider(col3, y, w, "Emer:%.0f€",
            ModConfigHandler.COMMON.STOCK_EMERALD_BASE_PRICE, 1, 10000));
        addWidget(new DoubleSlider(col4, y, w, "MaxΔ:%.0f%%",
            ModConfigHandler.COMMON.STOCK_MAX_PRICE_CHANGE_PERCENT, 0, 1.0, 100));
        y += s;

        // === ECONOMY CYCLE ===
        addWidget(new BoolButton(col1, y, w, "EcoCycle",
            ModConfigHandler.COMMON.ECONOMY_CYCLE_ENABLED));
        addWidget(new IntSlider(col2, y, w, "MinD:%d",
            ModConfigHandler.COMMON.ECONOMY_CYCLE_MIN_DURATION_DAYS, 1, 365));
        addWidget(new IntSlider(col3, y, w, "MaxD:%d",
            ModConfigHandler.COMMON.ECONOMY_CYCLE_MAX_DURATION_DAYS, 1, 365));
        addWidget(new DoubleSlider(col4, y, w, "Event:%.0f%%",
            ModConfigHandler.COMMON.ECONOMY_CYCLE_EVENT_BASE_CHANCE, 0, 1.0, 100));
        y += s;

        // === LEVEL SYSTEM ===
        addWidget(new BoolButton(col1, y, w, "Levels",
            ModConfigHandler.COMMON.LEVEL_SYSTEM_ENABLED));
        addWidget(new IntSlider(col2, y, w, "MaxLv:%d",
            ModConfigHandler.COMMON.LEVEL_MAX, 1, 100));
        addWidget(new IntSlider(col3, y, w, "BaseXP:%d",
            ModConfigHandler.COMMON.LEVEL_BASE_XP, 100, 100000));
        addWidget(new DoubleSlider(col4, y, w, "Exp:%.2f",
            ModConfigHandler.COMMON.LEVEL_XP_EXPONENT, 1.0, 3.0));
        y += s;

        addWidget(new DoubleSlider(col1, y, w, "IllgXP:%.1fx",
            ModConfigHandler.COMMON.LEVEL_ILLEGAL_XP_MULTIPLIER, 0.1, 10.0));
        addWidget(new DoubleSlider(col2, y, w, "LegXP:%.1fx",
            ModConfigHandler.COMMON.LEVEL_LEGAL_XP_MULTIPLIER, 0.1, 10.0));
        y += s;

        // === RISK PREMIUM ===
        addWidget(new DoubleSlider(col1, y, w, "Cannabis:%.2f",
            ModConfigHandler.COMMON.RISK_BASE_CANNABIS, 0, 10.0));
        addWidget(new DoubleSlider(col2, y, w, "Cocaine:%.2f",
            ModConfigHandler.COMMON.RISK_BASE_COCAINE, 0, 10.0));
        addWidget(new DoubleSlider(col3, y, w, "Heroin:%.2f",
            ModConfigHandler.COMMON.RISK_BASE_HEROIN, 0, 10.0));
        addWidget(new DoubleSlider(col4, y, w, "Meth:%.2f",
            ModConfigHandler.COMMON.RISK_BASE_METH, 0, 10.0));
        y += s;

        addWidget(new DoubleSlider(col1, y, w, "Confis:%.2fx",
            ModConfigHandler.COMMON.RISK_CONFISCATION_MULTIPLIER, 0, 10.0));
        y += s;

        // === ANTI-EXPLOIT ===
        addWidget(new DoubleSlider(col1, y, w, "DayLim:%.0f€",
            ModConfigHandler.COMMON.ANTI_EXPLOIT_DAILY_SELL_LIMIT, 1000, 10000000));
        addWidget(new IntSlider(col2, y, w, "SellCD:%ds",
            ModConfigHandler.COMMON.ANTI_EXPLOIT_MASS_SELL_COOLDOWN_SECONDS, 1, 600));
        addWidget(new IntSlider(col3, y, w, "Thresh:%d",
            ModConfigHandler.COMMON.ANTI_EXPLOIT_MASS_SELL_THRESHOLD, 1, 1000));
        addWidget(new DoubleSlider(col4, y, w, "Pen:%.0f%%",
            ModConfigHandler.COMMON.ANTI_EXPLOIT_MASS_SELL_PENALTY, 0, 1.0, 100));

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
            Component.literal("§e34 Advanced Economy Options!"),
            this.width / 2, 22, 0xFFFF55);
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
