package de.rolandsw.schedulemc.client.gui.config;

import de.rolandsw.schedulemc.weapon.config.WeaponConfig;
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
 * Weapon Config Screen - damage, hit rate (accuracy), fire rate, reload time and
 * range per weapon plus ammo damage multipliers. Scrollable.
 */
@OnlyIn(Dist.CLIENT)
public class WeaponConfigScreen extends Screen {
    private final Screen parent;
    private ConfigList configList;

    public WeaponConfigScreen(Screen parent) {
        super(Component.literal("Weapon Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.configList = new ConfigList(this.minecraft, this.width, this.height, 55, this.height - 55, 25);
        this.addWidget(this.configList);

        addWeapon("§e🔫 PISTOL",
            WeaponConfig.PISTOL_DAMAGE, WeaponConfig.PISTOL_ACCURACY,
            WeaponConfig.PISTOL_COOLDOWN, WeaponConfig.PISTOL_RELOAD_TICKS, WeaponConfig.PISTOL_RANGE, 1000);
        addWeapon("§e🔫 REVOLVER",
            WeaponConfig.REVOLVER_DAMAGE, WeaponConfig.REVOLVER_ACCURACY,
            WeaponConfig.REVOLVER_COOLDOWN, WeaponConfig.REVOLVER_RELOAD_TICKS, WeaponConfig.REVOLVER_RANGE, 1000);
        addWeapon("§6🔫 AK47",
            WeaponConfig.AK47_DAMAGE, WeaponConfig.AK47_ACCURACY,
            WeaponConfig.AK47_COOLDOWN, WeaponConfig.AK47_RELOAD_TICKS, WeaponConfig.AK47_RANGE, 1000);
        addWeapon("§6🔫 MP5",
            WeaponConfig.MP5_DAMAGE, WeaponConfig.MP5_ACCURACY,
            WeaponConfig.MP5_COOLDOWN, WeaponConfig.MP5_RELOAD_TICKS, WeaponConfig.MP5_RANGE, 1000);
        addWeapon("§b🔫 SNIPER",
            WeaponConfig.SNIPER_DAMAGE, WeaponConfig.SNIPER_ACCURACY,
            WeaponConfig.SNIPER_COOLDOWN, WeaponConfig.SNIPER_RELOAD_TICKS, WeaponConfig.SNIPER_RANGE, 2000);
        addWeapon("§c🔫 SHOTGUN",
            WeaponConfig.SHOTGUN_DAMAGE, WeaponConfig.SHOTGUN_ACCURACY,
            WeaponConfig.SHOTGUN_COOLDOWN, WeaponConfig.SHOTGUN_RELOAD_TICKS, WeaponConfig.SHOTGUN_RANGE, 1000);

        // === AMMO ===
        configList.addHeader("§d🎯 AMMO MULTIPLIERS");
        configList.addRow(
            new DoubleSlider(0, 0, 180, "AP Fire: %.2fx",
                WeaponConfig.AMMO_AP_FIRE_MULTIPLIER, 0.0, 10.0),
            new DoubleSlider(0, 0, 180, "AP Hit: %.2fx",
                WeaponConfig.AMMO_AP_HIT_MULTIPLIER, 0.0, 10.0)
        );
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Rubber: %.2fx",
                WeaponConfig.AMMO_RUBBER_DAMAGE_MULTIPLIER, 0.0, 10.0),
            null
        );

        // Back Button
        this.addRenderableWidget(Button.builder(
            Component.literal("« Back"),
            button -> this.minecraft.setScreen(parent)
        ).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private void addWeapon(String header,
                           net.minecraftforge.common.ForgeConfigSpec.IntValue damage,
                           net.minecraftforge.common.ForgeConfigSpec.DoubleValue accuracy,
                           net.minecraftforge.common.ForgeConfigSpec.IntValue cooldown,
                           net.minecraftforge.common.ForgeConfigSpec.IntValue reload,
                           net.minecraftforge.common.ForgeConfigSpec.IntValue range,
                           int rangeMax) {
        configList.addHeader(header);
        configList.addRow(
            new IntSlider(0, 0, 180, "Damage: %d", damage, 1, 100),
            new DoubleSlider(0, 0, 180, "Hit rate: %.2f", accuracy, 0.0, 1.0)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Fire rate: %d t", cooldown, 1, 200),
            new IntSlider(0, 0, 180, "Reload: %d t", reload, 0, 200)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Range: %d", range, 1, rangeMax),
            null
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        this.configList.render(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("§eDamage · Hit rate · Fire rate · Reload · Range"),
            this.width / 2, 22, 0xFFFF55);
        graphics.drawCenteredString(this.font,
            Component.literal("§8All changes are saved immediately"),
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

        public class HeaderEntry extends Entry {
            private final String text;

            public HeaderEntry(String text) {
                this.text = text;
            }

            @Override
            public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                             int mouseX, int mouseY, boolean hovering, float partialTick) {
                graphics.drawCenteredString(
                    WeaponConfigScreen.this.font,
                    Component.literal(text),
                    WeaponConfigScreen.this.width / 2,
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
                int centerX = WeaponConfigScreen.this.width / 2;
                int leftCol = centerX - 190;
                int rightCol = centerX + 10;

                if (leftWidget instanceof AbstractSliderButton slider) {
                    slider.setY(top);
                    slider.setX(leftCol);
                    slider.render(graphics, mouseX, mouseY, partialTick);
                } else if (leftWidget instanceof Button button) {
                    button.setY(top);
                    button.setX(leftCol);
                    button.render(graphics, mouseX, mouseY, partialTick);
                }

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

    // === SLIDER CLASSES (save to WeaponConfig.SPEC) ===

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
            WeaponConfig.SPEC.save();
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
            WeaponConfig.SPEC.save();
        }
    }
}
