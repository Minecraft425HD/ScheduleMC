package de.rolandsw.schedulemc.client.gui.config;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Economy Config Screen - 34 Advanced Economy Options (SCROLLABLE!)
 * Includes: Rent, Shop, Ratings, Bank, Stock Market, Economy Cycle,
 *          Level System, Risk Premium, Anti-Exploit
 */
@OnlyIn(Dist.CLIENT)
public class AdvancedEconomyConfigScreen extends Screen {
    private final Screen parent;
    private ConfigList configList;

    public AdvancedEconomyConfigScreen(Screen parent) {
        super(Component.literal("Advanced Economy"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Create scrollable config list
        this.configList = new ConfigList(this.minecraft, this.width, this.height, 55, this.height - 55, 25);
        this.addWidget(this.configList);

        // === RENT SYSTEM ===
        configList.addHeader("§6⌂ RENT SYSTEM");
        configList.addRow(
            new BoolButton(0, 0, 180, "Rent Enabled",
                ModConfigHandler.COMMON.RENT_ENABLED),
            new BoolButton(0, 0, 180, "Auto Evict",
                ModConfigHandler.COMMON.AUTO_EVICT_EXPIRED)
        );
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Min Price: %.0f€",
                ModConfigHandler.COMMON.MIN_RENT_PRICE, 1, 10000),
            new IntSlider(0, 0, 180, "Min Days: %d",
                ModConfigHandler.COMMON.MIN_RENT_DAYS, 1, 365)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Max Days: %d",
                ModConfigHandler.COMMON.MAX_RENT_DAYS, 1, 365),
            null
        );

        // === SHOP SYSTEM ===
        configList.addHeader("§b⌘ SHOP SYSTEM");
        configList.addRow(
            new BoolButton(0, 0, 180, "Shop Enabled",
                ModConfigHandler.COMMON.SHOP_ENABLED),
            null
        );

        // === RATINGS SYSTEM ===
        configList.addHeader("§e★ RATINGS SYSTEM");
        configList.addRow(
            new BoolButton(0, 0, 180, "Ratings Enabled",
                ModConfigHandler.COMMON.RATINGS_ENABLED),
            new BoolButton(0, 0, 180, "Multiple Ratings",
                ModConfigHandler.COMMON.ALLOW_MULTIPLE_RATINGS)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Min Rating: %d",
                ModConfigHandler.COMMON.MIN_RATING, 1, 5),
            new IntSlider(0, 0, 180, "Max Rating: %d",
                ModConfigHandler.COMMON.MAX_RATING, 1, 10)
        );

        // === BANK SYSTEM ===
        configList.addHeader("§2$ BANK SYSTEM");
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Deposit Limit: %.0f€",
                ModConfigHandler.COMMON.BANK_DEPOSIT_LIMIT, 1000, 10000000),
            new DoubleSlider(0, 0, 180, "Transfer Limit: %.0f€",
                ModConfigHandler.COMMON.BANK_TRANSFER_DAILY_LIMIT, 1000, 10000000)
        );

        // === STOCK MARKET ===
        configList.addHeader("§6📈 STOCK MARKET");
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Gold Base: %.0f€",
                ModConfigHandler.COMMON.STOCK_GOLD_BASE_PRICE, 1, 10000),
            new DoubleSlider(0, 0, 180, "Diamond Base: %.0f€",
                ModConfigHandler.COMMON.STOCK_DIAMOND_BASE_PRICE, 1, 10000)
        );
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Emerald Base: %.0f€",
                ModConfigHandler.COMMON.STOCK_EMERALD_BASE_PRICE, 1, 10000),
            new DoubleSlider(0, 0, 180, "Max Change: %.0f%%",
                ModConfigHandler.COMMON.STOCK_MAX_PRICE_CHANGE_PERCENT, 0, 1.0, 100)
        );

        // === ECONOMY CYCLE ===
        configList.addHeader("§d⟳ ECONOMY CYCLE");
        configList.addRow(
            new BoolButton(0, 0, 180, "Cycle Enabled",
                ModConfigHandler.COMMON.ECONOMY_CYCLE_ENABLED),
            new IntSlider(0, 0, 180, "Min Duration: %d days",
                ModConfigHandler.COMMON.ECONOMY_CYCLE_MIN_DURATION_DAYS, 1, 365)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Max Duration: %d days",
                ModConfigHandler.COMMON.ECONOMY_CYCLE_MAX_DURATION_DAYS, 1, 365),
            new DoubleSlider(0, 0, 180, "Event Chance: %.0f%%",
                ModConfigHandler.COMMON.ECONOMY_CYCLE_EVENT_BASE_CHANCE, 0, 1.0, 100)
        );

        // === LEVEL SYSTEM ===
        configList.addHeader("§a⚡ LEVEL SYSTEM");
        configList.addRow(
            new BoolButton(0, 0, 180, "Levels Enabled",
                ModConfigHandler.COMMON.LEVEL_SYSTEM_ENABLED),
            new IntSlider(0, 0, 180, "Max Level: %d",
                ModConfigHandler.COMMON.LEVEL_MAX, 1, 100)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Base XP: %d",
                ModConfigHandler.COMMON.LEVEL_BASE_XP, 100, 100000),
            new DoubleSlider(0, 0, 180, "XP Exponent: %.2f",
                ModConfigHandler.COMMON.LEVEL_XP_EXPONENT, 1.0, 3.0)
        );
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Illegal XP: %.1fx",
                ModConfigHandler.COMMON.LEVEL_ILLEGAL_XP_MULTIPLIER, 0.1, 10.0),
            new DoubleSlider(0, 0, 180, "Legal XP: %.1fx",
                ModConfigHandler.COMMON.LEVEL_LEGAL_XP_MULTIPLIER, 0.1, 10.0)
        );

        // === RISK PREMIUM ===
        configList.addHeader("§c⚠ RISK PREMIUM");
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Cannabis: %.2f",
                ModConfigHandler.COMMON.RISK_BASE_CANNABIS, 0, 10.0),
            new DoubleSlider(0, 0, 180, "Cocaine: %.2f",
                ModConfigHandler.COMMON.RISK_BASE_COCAINE, 0, 10.0)
        );
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Heroin: %.2f",
                ModConfigHandler.COMMON.RISK_BASE_HEROIN, 0, 10.0),
            new DoubleSlider(0, 0, 180, "Meth: %.2f",
                ModConfigHandler.COMMON.RISK_BASE_METH, 0, 10.0)
        );
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Confiscation: %.2fx",
                ModConfigHandler.COMMON.RISK_CONFISCATION_MULTIPLIER, 0, 10.0),
            null
        );

        // === ANTI-EXPLOIT ===
        configList.addHeader("§4⚔ ANTI-EXPLOIT");
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Daily Limit: %.0f€",
                ModConfigHandler.COMMON.ANTI_EXPLOIT_DAILY_SELL_LIMIT, 1000, 10000000),
            new IntSlider(0, 0, 180, "Sell Cooldown: %ds",
                ModConfigHandler.COMMON.ANTI_EXPLOIT_MASS_SELL_COOLDOWN_SECONDS, 1, 600)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Sell Threshold: %d",
                ModConfigHandler.COMMON.ANTI_EXPLOIT_MASS_SELL_THRESHOLD, 1, 1000),
            new DoubleSlider(0, 0, 180, "Sell Penalty: %.0f%%",
                ModConfigHandler.COMMON.ANTI_EXPLOIT_MASS_SELL_PENALTY, 0, 1.0, 100)
        );

        // Back Button
        this.addRenderableWidget(Button.builder(
            Component.literal("« Back"),
            button -> this.minecraft.setScreen(parent)
        ).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        // Render scrollable list
        this.configList.render(graphics, mouseX, mouseY, partialTick);

        super.render(graphics, mouseX, mouseY, partialTick);

        // Title
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);

        // Subtitle
        graphics.drawCenteredString(this.font,
            Component.literal("§e34 Advanced Economy Options - Now Scrollable!"),
            this.width / 2, 22, 0xFFFF55);

        // Info
        graphics.drawCenteredString(this.font,
            Component.literal("§8Scroll to see all options"),
            this.width / 2, this.height - 45, 0x808080);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    // === SCROLLABLE CONFIG LIST ===

    private class ConfigList extends ContainerObjectSelectionList<ConfigList.Entry> {

        public ConfigList(net.minecraft.client.Minecraft mc, int width, int height, int top, int bottom, int itemHeight) {
            super(mc, width, height, top, bottom, itemHeight);
        }

        @Override
        public int getRowWidth() {
            return 400; // Width for two columns
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 210;
        }

        public void addHeader(String text) {
            this.addEntry(new HeaderEntry(text));
        }

        public void addRow(@Nullable GuiEventListener left, @Nullable GuiEventListener right) {
            this.addEntry(new WidgetRowEntry(left, right));
        }

        // === HEADER ENTRY ===

        public class HeaderEntry extends Entry {
            private final String text;

            public HeaderEntry(String text) {
                this.text = text;
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                             int mouseX, int mouseY, boolean hovering, float partialTick) {
                graphics.drawCenteredString(
                    AdvancedEconomyConfigScreen.this.font,
                    Component.literal(text),
                    AdvancedEconomyConfigScreen.this.width / 2,
                    top + 5,
                    0xFFFFAA
                );
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of();
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of();
            }
        }

        // === WIDGET ROW ENTRY ===

        public class WidgetRowEntry extends Entry {
            private final List<GuiEventListener> children = new ArrayList<>();
            @Nullable private final GuiEventListener leftWidget;
            @Nullable private final GuiEventListener rightWidget;

            public WidgetRowEntry(@Nullable GuiEventListener left, @Nullable GuiEventListener right) {
                this.leftWidget = left;
                this.rightWidget = right;
                if (left != null) children.add(left);
                if (right != null) children.add(right);
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                             int mouseX, int mouseY, boolean hovering, float partialTick) {
                int centerX = AdvancedEconomyConfigScreen.this.width / 2;
                int leftCol = centerX - 190;
                int rightCol = centerX + 10;

                // Render left widget
                if (leftWidget instanceof AbstractSliderButton slider) {
                    slider.setY(top);
                    slider.setX(leftCol);
                    slider.render(graphics, mouseX, mouseY, partialTick);
                } else if (leftWidget instanceof Button button) {
                    button.setY(top);
                    button.setX(leftCol);
                    button.render(graphics, mouseX, mouseY, partialTick);
                }

                // Render right widget
                if (rightWidget instanceof AbstractSliderButton slider) {
                    slider.setY(top);
                    slider.setX(rightCol);
                    slider.render(graphics, mouseX, mouseY, partialTick);
                } else if (rightWidget instanceof Button button) {
                    button.setY(top);
                    button.setX(rightCol);
                    button.render(graphics, mouseX, mouseY, partialTick);
                }
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                List<NarratableEntry> list = new ArrayList<>();
                if (leftWidget instanceof NarratableEntry ne) list.add(ne);
                if (rightWidget instanceof NarratableEntry ne) list.add(ne);
                return list;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                for (GuiEventListener child : children) {
                    if (child.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                }
                return false;
            }
        }

        // Base Entry class
        public abstract class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        }
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
            this.setMessage(Component.literal(label + ": " + (val ? "§aON" : "§cOFF")));
        }

        @Override
        public void onPress() {
            config.set(!config.get());
            ModConfigHandler.SPEC.save();
            updateMessage();
        }
    }
}
