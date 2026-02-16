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
 * Economy Config Screen - ALL Economy Settings (Scrollable!)
 * Now includes: Savings, Overdraft, Recurring, Tax, Daily Rewards, Shop Multipliers
 */
@OnlyIn(Dist.CLIENT)
public class EconomyConfigScreen extends Screen {
    private final Screen parent;
    private ConfigList configList;

    public EconomyConfigScreen(Screen parent) {
        super(Component.literal("Economy Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Create scrollable config list
        this.configList = new ConfigList(this.minecraft, this.width, this.height, 55, this.height - 55, 25);
        this.addWidget(this.configList);

        // === BASIC ECONOMY ===
        configList.addHeader("§6💰 BASIC ECONOMY");
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Start Balance: %.0f€",
                ModConfigHandler.COMMON.START_BALANCE, 0, 10000),
            new IntSlider(0, 0, 180, "Save Interval: %d min",
                ModConfigHandler.COMMON.SAVE_INTERVAL_MINUTES, 1, 60)
        );

        // === DAILY REWARDS ===
        configList.addHeader("§e⭐ DAILY REWARDS");
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Daily Reward: %.0f€",
                ModConfigHandler.COMMON.DAILY_REWARD, 1, 1000),
            new DoubleSlider(0, 0, 180, "Streak Bonus: %.0f€",
                ModConfigHandler.COMMON.DAILY_REWARD_STREAK_BONUS, 0, 500)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Max Streak: %d days",
                ModConfigHandler.COMMON.MAX_STREAK_DAYS, 1, 365),
            null
        );

        // === SAVINGS ACCOUNTS ===
        configList.addHeader("§2💳 SAVINGS ACCOUNTS");
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Max Per Player: %.0f€",
                ModConfigHandler.COMMON.SAVINGS_MAX_PER_PLAYER, 1000, 10000000),
            new DoubleSlider(0, 0, 180, "Min Deposit: %.0f€",
                ModConfigHandler.COMMON.SAVINGS_MIN_DEPOSIT, 100, 100000)
        );
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Interest Rate: %.1f%%",
                ModConfigHandler.COMMON.SAVINGS_INTEREST_RATE, 0, 0.5, 100),
            new IntSlider(0, 0, 180, "Lock Period: %d weeks",
                ModConfigHandler.COMMON.SAVINGS_LOCK_PERIOD_WEEKS, 1, 52)
        );
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Early Penalty: %.0f%%",
                ModConfigHandler.COMMON.SAVINGS_EARLY_WITHDRAWAL_PENALTY, 0, 0.5, 100),
            null
        );

        // === OVERDRAFT (DISPO) ===
        configList.addHeader("§c⚠ OVERDRAFT (DISPO)");
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Dispo Rate: %.0f%%",
                ModConfigHandler.COMMON.OVERDRAFT_INTEREST_RATE, 0, 1.0, 100),
            null
        );

        // === RECURRING PAYMENTS ===
        configList.addHeader("§b⟳ RECURRING PAYMENTS");
        configList.addRow(
            new IntSlider(0, 0, 180, "Max Per Player: %d",
                ModConfigHandler.COMMON.RECURRING_MAX_PER_PLAYER, 1, 100),
            null
        );

        // === TAX SYSTEM ===
        configList.addHeader("§d📊 TAX SYSTEM");
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Property Tax: %.0f€/chunk",
                ModConfigHandler.COMMON.TAX_PROPERTY_PER_CHUNK, 0, 1000),
            new DoubleSlider(0, 0, 180, "Sales Tax: %.0f%%",
                ModConfigHandler.COMMON.TAX_SALES_RATE, 0, 1.0, 100)
        );

        // === SHOP MULTIPLIERS ===
        configList.addHeader("§a🛒 SHOP MULTIPLIERS");
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Buy Multiplier: %.1fx",
                ModConfigHandler.COMMON.BUY_MULTIPLIER, 0.1, 10.0),
            new DoubleSlider(0, 0, 180, "Sell Multiplier: %.1fx",
                ModConfigHandler.COMMON.SELL_MULTIPLIER, 0.1, 10.0)
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
            Component.literal("§7Complete Economy & Money System - 16 Options"),
            this.width / 2, 22, 0xFFFF55);

        // Info
        graphics.drawCenteredString(this.font,
            Component.literal("§8All changes saved immediately"),
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
            return 400;
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
                    EconomyConfigScreen.this.font,
                    Component.literal(text),
                    EconomyConfigScreen.this.width / 2,
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
                int centerX = EconomyConfigScreen.this.width / 2;
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

        public abstract class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        }
    }

    // === SLIDER CLASSES ===

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
