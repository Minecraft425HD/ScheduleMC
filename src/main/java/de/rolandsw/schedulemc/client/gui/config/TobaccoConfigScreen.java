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
 * Tobacco Config Screen - 31 Tobacco System Options (SCROLLABLE!)
 * Organized into logical categories with headers
 */
@OnlyIn(Dist.CLIENT)
public class TobaccoConfigScreen extends Screen {
    private final Screen parent;
    private ConfigList configList;

    public TobaccoConfigScreen(Screen parent) {
        super(Component.literal("Tobacco Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Create scrollable config list
        this.configList = new ConfigList(this.minecraft, this.width, this.height, 55, this.height - 55, 25);
        this.addWidget(this.configList);

        // === GENERAL SETTINGS ===
        configList.addHeader("§2🌿 GENERAL SETTINGS");
        configList.addRow(
            new BoolButton(0, 0, 180, "Tobacco Enabled",
                ModConfigHandler.TOBACCO.TOBACCO_ENABLED),
            new DoubleSlider(0, 0, 180, "Growth Speed: %.1fx",
                ModConfigHandler.TOBACCO.TOBACCO_GROWTH_SPEED_MULTIPLIER, 0.1, 10.0)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Drying Time: %d ticks",
                ModConfigHandler.TOBACCO.TOBACCO_DRYING_TIME, 100, 72000),
            new IntSlider(0, 0, 180, "Fermenting: %d ticks",
                ModConfigHandler.TOBACCO.TOBACCO_FERMENTING_TIME, 100, 72000)
        );
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Quality Chance: %.0f%%",
                ModConfigHandler.TOBACCO.FERMENTATION_QUALITY_CHANCE, 0, 1.0, 100),
            null
        );

        // === DRYING RACKS ===
        configList.addHeader("§6📦 DRYING RACKS");
        configList.addRow(
            new IntSlider(0, 0, 180, "Small Rack: %d",
                ModConfigHandler.TOBACCO.SMALL_DRYING_RACK_CAPACITY, 1, 64),
            new IntSlider(0, 0, 180, "Medium Rack: %d",
                ModConfigHandler.TOBACCO.MEDIUM_DRYING_RACK_CAPACITY, 1, 64)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Large Rack: %d",
                ModConfigHandler.TOBACCO.BIG_DRYING_RACK_CAPACITY, 1, 64),
            null
        );

        // === FERMENTATION BARRELS ===
        configList.addHeader("§c🛢 FERMENTATION BARRELS");
        configList.addRow(
            new IntSlider(0, 0, 180, "Small Barrel: %d",
                ModConfigHandler.TOBACCO.SMALL_FERMENTATION_BARREL_CAPACITY, 1, 64),
            new IntSlider(0, 0, 180, "Medium Barrel: %d",
                ModConfigHandler.TOBACCO.MEDIUM_FERMENTATION_BARREL_CAPACITY, 1, 64)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Large Barrel: %d",
                ModConfigHandler.TOBACCO.BIG_FERMENTATION_BARREL_CAPACITY, 1, 64),
            null
        );

        // === POT WATER CAPACITY ===
        configList.addHeader("§9💧 POT WATER CAPACITY");
        configList.addRow(
            new IntSlider(0, 0, 180, "Terracotta: %d",
                ModConfigHandler.TOBACCO.TERRACOTTA_WATER_CAPACITY, 10, 10000),
            new IntSlider(0, 0, 180, "Ceramic: %d",
                ModConfigHandler.TOBACCO.CERAMIC_WATER_CAPACITY, 10, 10000)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Iron: %d",
                ModConfigHandler.TOBACCO.IRON_WATER_CAPACITY, 10, 10000),
            new IntSlider(0, 0, 180, "Golden: %d",
                ModConfigHandler.TOBACCO.GOLDEN_WATER_CAPACITY, 10, 10000)
        );

        // === POT SOIL CAPACITY ===
        configList.addHeader("§e🏺 POT SOIL CAPACITY");
        configList.addRow(
            new IntSlider(0, 0, 180, "Terracotta: %d",
                ModConfigHandler.TOBACCO.TERRACOTTA_SOIL_CAPACITY, 10, 10000),
            new IntSlider(0, 0, 180, "Ceramic: %d",
                ModConfigHandler.TOBACCO.CERAMIC_SOIL_CAPACITY, 10, 10000)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Iron: %d",
                ModConfigHandler.TOBACCO.IRON_SOIL_CAPACITY, 10, 10000),
            new IntSlider(0, 0, 180, "Golden: %d",
                ModConfigHandler.TOBACCO.GOLDEN_SOIL_CAPACITY, 10, 10000)
        );

        // === BOTTLE EFFECTS ===
        configList.addHeader("§d🧪 BOTTLE EFFECTS");
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Fertilizer: %.1fx",
                ModConfigHandler.TOBACCO.FERTILIZER_YIELD_BONUS, 0, 5.0),
            new DoubleSlider(0, 0, 180, "Growth Boost: %.1fx",
                ModConfigHandler.TOBACCO.GROWTH_BOOSTER_SPEED_MULTIPLIER, 1.0, 10.0)
        );

        // === LIGHT REQUIREMENTS ===
        configList.addHeader("§6💡 LIGHT REQUIREMENTS");
        configList.addRow(
            new BoolButton(0, 0, 180, "Require Light",
                ModConfigHandler.TOBACCO.REQUIRE_LIGHT_FOR_GROWTH),
            new IntSlider(0, 0, 180, "Min Light Level: %d",
                ModConfigHandler.TOBACCO.MIN_LIGHT_LEVEL, 0, 15)
        );

        // === BASIC GROW LIGHT ===
        configList.addHeader("§a🔦 BASIC GROW LIGHT");
        configList.addRow(
            new IntSlider(0, 0, 180, "Light Level: %d",
                ModConfigHandler.TOBACCO.BASIC_GROW_LIGHT_LEVEL, 0, 15),
            new DoubleSlider(0, 0, 180, "Speed: %.1fx",
                ModConfigHandler.TOBACCO.BASIC_GROW_LIGHT_SPEED, 0.1, 10.0)
        );

        // === ADVANCED GROW LIGHT ===
        configList.addHeader("§b✨ ADVANCED GROW LIGHT");
        configList.addRow(
            new IntSlider(0, 0, 180, "Light Level: %d",
                ModConfigHandler.TOBACCO.ADVANCED_GROW_LIGHT_LEVEL, 0, 15),
            new DoubleSlider(0, 0, 180, "Speed: %.2fx",
                ModConfigHandler.TOBACCO.ADVANCED_GROW_LIGHT_SPEED, 0.1, 10.0)
        );

        // === PREMIUM GROW LIGHT ===
        configList.addHeader("§6⭐ PREMIUM GROW LIGHT");
        configList.addRow(
            new IntSlider(0, 0, 180, "Light Level: %d",
                ModConfigHandler.TOBACCO.PREMIUM_GROW_LIGHT_LEVEL, 0, 15),
            new DoubleSlider(0, 0, 180, "Speed: %.1fx",
                ModConfigHandler.TOBACCO.PREMIUM_GROW_LIGHT_SPEED, 0.1, 10.0)
        );
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Quality Bonus: %.0f%%",
                ModConfigHandler.TOBACCO.PREMIUM_GROW_LIGHT_QUALITY_BONUS, 0, 1.0, 100),
            null
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
            Component.literal("§231 Tobacco Options - Now Scrollable & Organized!"),
            this.width / 2, 22, 0xFFFF55);

        // Info
        graphics.drawCenteredString(this.font,
            Component.literal("§8Scroll to see all tobacco features"),
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
                    TobaccoConfigScreen.this.font,
                    Component.literal(text),
                    TobaccoConfigScreen.this.width / 2,
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
                int centerX = TobaccoConfigScreen.this.width / 2;
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
