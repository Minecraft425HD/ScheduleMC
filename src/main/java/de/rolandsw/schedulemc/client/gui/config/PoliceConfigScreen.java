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
 * Police Config Screen - 36 Police System Options (SCROLLABLE!)
 * Organized into logical categories with headers
 */
@OnlyIn(Dist.CLIENT)
public class PoliceConfigScreen extends Screen {
    private final Screen parent;
    private ConfigList configList;

    public PoliceConfigScreen(Screen parent) {
        super(Component.literal("Police Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Create scrollable config list
        this.configList = new ConfigList(this.minecraft, this.width, this.height, 55, this.height - 55, 25);
        this.addWidget(this.configList);

        // === DETECTION & ARREST ===
        configList.addHeader("§c👮 DETECTION & ARREST");
        configList.addRow(
            new IntSlider(0, 0, 180, "Arrest Cooldown: %ds",
                ModConfigHandler.COMMON.POLICE_ARREST_COOLDOWN_SECONDS, 1, 300),
            new IntSlider(0, 0, 180, "Detection Radius: %d blocks",
                ModConfigHandler.COMMON.POLICE_DETECTION_RADIUS, 5, 100)
        );
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Arrest Distance: %.1f blocks",
                ModConfigHandler.COMMON.POLICE_ARREST_DISTANCE, 1, 10),
            new BoolButton(0, 0, 180, "Evidence Multiplier",
                ModConfigHandler.COMMON.POLICE_EVIDENCE_MULTIPLIER_ENABLED)
        );

        // === SEARCH & PURSUIT ===
        configList.addHeader("§e🔍 SEARCH & PURSUIT");
        configList.addRow(
            new IntSlider(0, 0, 180, "Search Duration: %ds",
                ModConfigHandler.COMMON.POLICE_SEARCH_DURATION_SECONDS, 10, 600),
            new IntSlider(0, 0, 180, "Search Radius: %d blocks",
                ModConfigHandler.COMMON.POLICE_SEARCH_RADIUS, 10, 200)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Target Update: %ds",
                ModConfigHandler.COMMON.POLICE_SEARCH_TARGET_UPDATE_SECONDS, 1, 30),
            new IntSlider(0, 0, 180, "Backup Radius: %d blocks",
                ModConfigHandler.COMMON.POLICE_BACKUP_SEARCH_RADIUS, 10, 500)
        );
        configList.addRow(
            new BoolButton(0, 0, 180, "Indoor Hiding",
                ModConfigHandler.COMMON.POLICE_INDOOR_HIDING_ENABLED),
            new BoolButton(0, 0, 180, "Block Doors",
                ModConfigHandler.COMMON.POLICE_BLOCK_DOORS_DURING_PURSUIT)
        );
        configList.addRow(
            new BoolButton(0, 0, 180, "Flanking AI",
                ModConfigHandler.COMMON.POLICE_FLANKING_ENABLED),
            null
        );

        // === RAIDS & SCANNING ===
        configList.addHeader("§4🏠 RAIDS & SCANNING");
        configList.addRow(
            new IntSlider(0, 0, 180, "Raid Radius: %d blocks",
                ModConfigHandler.COMMON.POLICE_RAID_SCAN_RADIUS, 5, 50),
            new DoubleSlider(0, 0, 180, "Illegal Cash: %.0f€",
                ModConfigHandler.COMMON.POLICE_ILLEGAL_CASH_THRESHOLD, 1000, 1000000)
        );
        configList.addRow(
            new DoubleSlider(0, 0, 180, "Account Fine: %.0f%%",
                ModConfigHandler.COMMON.POLICE_RAID_ACCOUNT_PERCENTAGE, 0, 1.0, 100),
            new DoubleSlider(0, 0, 180, "Min Fine: %.0f€",
                ModConfigHandler.COMMON.POLICE_RAID_MIN_FINE, 100, 100000)
        );
        configList.addRow(
            new BoolButton(0, 0, 180, "Room Scan",
                ModConfigHandler.COMMON.POLICE_ROOM_SCAN_ENABLED),
            new IntSlider(0, 0, 180, "Max Room Size: %d",
                ModConfigHandler.COMMON.POLICE_ROOM_SCAN_MAX_SIZE, 10, 1000)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Scan Depth: %d",
                ModConfigHandler.COMMON.POLICE_ROOM_SCAN_MAX_DEPTH, 1, 20),
            new IntSlider(0, 0, 180, "Extra Rooms: %d",
                ModConfigHandler.COMMON.POLICE_ROOM_SCAN_MAX_ADDITIONAL_ROOMS, 0, 10)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Container Depth: %d",
                ModConfigHandler.COMMON.POLICE_CONTAINER_SCAN_DEPTH, 1, 10),
            null
        );

        // === STATION & PATROL ===
        configList.addHeader("§9🏢 STATION & PATROL");
        configList.addRow(
            new IntSlider(0, 0, 180, "Station Wait: %d min",
                ModConfigHandler.COMMON.POLICE_STATION_WAIT_MINUTES, 1, 60),
            new IntSlider(0, 0, 180, "Station Radius: %d blocks",
                ModConfigHandler.COMMON.POLICE_STATION_RADIUS, 10, 200)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Patrol Wait: %d min",
                ModConfigHandler.COMMON.POLICE_PATROL_WAIT_MINUTES, 1, 60),
            new IntSlider(0, 0, 180, "Patrol Radius: %d blocks",
                ModConfigHandler.COMMON.POLICE_PATROL_RADIUS, 10, 200)
        );

        // === VEHICLES & PURSUIT ===
        configList.addHeader("§b🚓 VEHICLES & PURSUIT");
        configList.addRow(
            new BoolButton(0, 0, 180, "Vehicle Chase",
                ModConfigHandler.COMMON.POLICE_VEHICLE_PURSUIT_ENABLED),
            new DoubleSlider(0, 0, 180, "Speed: %.1fx",
                ModConfigHandler.COMMON.POLICE_VEHICLE_SPEED_MULTIPLIER, 0.5, 3.0)
        );
        configList.addRow(
            new BoolButton(0, 0, 180, "Siren Enabled",
                ModConfigHandler.COMMON.POLICE_SIREN_ENABLED),
            new IntSlider(0, 0, 180, "Siren Radius: %d blocks",
                ModConfigHandler.COMMON.POLICE_SIREN_SOUND_RADIUS, 10, 200)
        );

        // === ROADBLOCKS ===
        configList.addHeader("§6🚧 ROADBLOCKS");
        configList.addRow(
            new BoolButton(0, 0, 180, "Roadblocks",
                ModConfigHandler.COMMON.POLICE_ROADBLOCK_ENABLED),
            new IntSlider(0, 0, 180, "Max Roadblocks: %d",
                ModConfigHandler.COMMON.POLICE_MAX_ROADBLOCKS, 1, 20)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Duration: %ds",
                ModConfigHandler.COMMON.POLICE_ROADBLOCK_DURATION_SECONDS, 30, 600),
            null
        );

        // === TRAFFIC & VIOLATIONS ===
        configList.addHeader("§a🚦 TRAFFIC & VIOLATIONS");
        configList.addRow(
            new BoolButton(0, 0, 180, "Traffic Violations",
                ModConfigHandler.COMMON.POLICE_TRAFFIC_VIOLATIONS_ENABLED),
            new DoubleSlider(0, 0, 180, "Speed Limit: %.0f",
                ModConfigHandler.COMMON.POLICE_SPEED_LIMIT_DEFAULT, 20, 200)
        );

        // === WARNINGS & WANTED ===
        configList.addHeader("§d⚠ WARNINGS & WANTED");
        configList.addRow(
            new BoolButton(0, 0, 180, "Warnings",
                ModConfigHandler.COMMON.POLICE_WARNING_ENABLED),
            new IntSlider(0, 0, 180, "Warning Timeout: %ds",
                ModConfigHandler.COMMON.POLICE_WARNING_TIMEOUT_SECONDS, 5, 60)
        );
        configList.addRow(
            new IntSlider(0, 0, 180, "Poster Min Level: %d",
                ModConfigHandler.COMMON.POLICE_WANTED_POSTERS_MIN_LEVEL, 1, 5),
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
            Component.literal("§c36 Police Options - Now Scrollable & Organized!"),
            this.width / 2, 22, 0xFFFF55);

        // Info
        graphics.drawCenteredString(this.font,
            Component.literal("§8Scroll to see all police features"),
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
                    PoliceConfigScreen.this.font,
                    Component.literal(text),
                    PoliceConfigScreen.this.width / 2,
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
                int centerX = PoliceConfigScreen.this.width / 2;
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
