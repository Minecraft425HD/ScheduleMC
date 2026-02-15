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
 * Police Config Screen - 44 Police System Options (Compact Layout)
 */
@OnlyIn(Dist.CLIENT)
public class PoliceConfigScreen extends Screen {
    private final Screen parent;

    public PoliceConfigScreen(Screen parent) {
        super(Component.literal("Police Settings"));
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
        int y = 50;
        int s = 22;

        // Row 1: Detection & Arrest
        addWidget(new IntSlider(col1, y, w, "ArrestCD:%ds",
            ModConfigHandler.COMMON.POLICE_ARREST_COOLDOWN_SECONDS, 1, 300));
        addWidget(new IntSlider(col2, y, w, "Detect:%dm",
            ModConfigHandler.COMMON.POLICE_DETECTION_RADIUS, 5, 100));
        addWidget(new DoubleSlider(col3, y, w, "Dist:%.1fm",
            ModConfigHandler.COMMON.POLICE_ARREST_DISTANCE, 1, 10));
        addWidget(new BoolButton(col4, y, w, "Evidence",
            ModConfigHandler.COMMON.POLICE_EVIDENCE_MULTIPLIER_ENABLED));
        y += s;

        // Row 2: Search
        addWidget(new IntSlider(col1, y, w, "Search:%ds",
            ModConfigHandler.COMMON.POLICE_SEARCH_DURATION_SECONDS, 10, 600));
        addWidget(new IntSlider(col2, y, w, "SearchR:%dm",
            ModConfigHandler.COMMON.POLICE_SEARCH_RADIUS, 10, 200));
        addWidget(new IntSlider(col3, y, w, "Update:%ds",
            ModConfigHandler.COMMON.POLICE_SEARCH_TARGET_UPDATE_SECONDS, 1, 30));
        addWidget(new IntSlider(col4, y, w, "Backup:%dm",
            ModConfigHandler.COMMON.POLICE_BACKUP_SEARCH_RADIUS, 10, 500));
        y += s;

        // Row 3: Search Features
        addWidget(new BoolButton(col1, y, w, "IndoorHide",
            ModConfigHandler.COMMON.POLICE_INDOOR_HIDING_ENABLED));
        addWidget(new BoolButton(col2, y, w, "BlockDoors",
            ModConfigHandler.COMMON.POLICE_BLOCK_DOORS_DURING_PURSUIT));
        addWidget(new BoolButton(col3, y, w, "Flanking",
            ModConfigHandler.COMMON.POLICE_FLANKING_ENABLED));
        y += s;

        // Row 4: Raids
        addWidget(new IntSlider(col1, y, w, "RaidR:%dm",
            ModConfigHandler.COMMON.POLICE_RAID_SCAN_RADIUS, 5, 50));
        addWidget(new DoubleSlider(col2, y, w, "Cash:%.0f€",
            ModConfigHandler.COMMON.POLICE_ILLEGAL_CASH_THRESHOLD, 1000, 1000000));
        addWidget(new DoubleSlider(col3, y, w, "Raid%%:%.0f",
            ModConfigHandler.COMMON.POLICE_RAID_ACCOUNT_PERCENTAGE, 0, 1.0, 100));
        addWidget(new DoubleSlider(col4, y, w, "MinF:%.0f€",
            ModConfigHandler.COMMON.POLICE_RAID_MIN_FINE, 100, 100000));
        y += s;

        // Row 5: Room Scan
        addWidget(new BoolButton(col1, y, w, "RoomScan",
            ModConfigHandler.COMMON.POLICE_ROOM_SCAN_ENABLED));
        addWidget(new IntSlider(col2, y, w, "RoomSz:%d",
            ModConfigHandler.COMMON.POLICE_ROOM_SCAN_MAX_SIZE, 10, 1000));
        addWidget(new IntSlider(col3, y, w, "Depth:%d",
            ModConfigHandler.COMMON.POLICE_ROOM_SCAN_MAX_DEPTH, 1, 20));
        addWidget(new IntSlider(col4, y, w, "Extra:%d",
            ModConfigHandler.COMMON.POLICE_ROOM_SCAN_MAX_ADDITIONAL_ROOMS, 0, 10));
        y += s;

        // Row 6: Container & Station
        addWidget(new IntSlider(col1, y, w, "ContScan:%d",
            ModConfigHandler.COMMON.POLICE_CONTAINER_SCAN_DEPTH, 1, 10));
        addWidget(new IntSlider(col2, y, w, "StaWait:%dm",
            ModConfigHandler.COMMON.POLICE_STATION_WAIT_MINUTES, 1, 60));
        addWidget(new IntSlider(col3, y, w, "StaR:%dm",
            ModConfigHandler.COMMON.POLICE_STATION_RADIUS, 10, 200));
        y += s;

        // Row 7: Patrol
        addWidget(new IntSlider(col1, y, w, "PatWait:%dm",
            ModConfigHandler.COMMON.POLICE_PATROL_WAIT_MINUTES, 1, 60));
        addWidget(new IntSlider(col2, y, w, "PatR:%dm",
            ModConfigHandler.COMMON.POLICE_PATROL_RADIUS, 10, 200));
        y += s;

        // Row 8: Vehicles
        addWidget(new BoolButton(col1, y, w, "VehChase",
            ModConfigHandler.COMMON.POLICE_VEHICLE_PURSUIT_ENABLED));
        addWidget(new DoubleSlider(col2, y, w, "Speed:%.1fx",
            ModConfigHandler.COMMON.POLICE_VEHICLE_SPEED_MULTIPLIER, 0.5, 3.0));
        addWidget(new BoolButton(col3, y, w, "Siren",
            ModConfigHandler.COMMON.POLICE_SIREN_ENABLED));
        addWidget(new IntSlider(col4, y, w, "SirenR:%dm",
            ModConfigHandler.COMMON.POLICE_SIREN_SOUND_RADIUS, 10, 200));
        y += s;

        // Row 9: Roadblocks
        addWidget(new BoolButton(col1, y, w, "Roadblock",
            ModConfigHandler.COMMON.POLICE_ROADBLOCK_ENABLED));
        addWidget(new IntSlider(col2, y, w, "MaxBlock:%d",
            ModConfigHandler.COMMON.POLICE_MAX_ROADBLOCKS, 1, 20));
        addWidget(new IntSlider(col3, y, w, "BlockT:%ds",
            ModConfigHandler.COMMON.POLICE_ROADBLOCK_DURATION_SECONDS, 30, 600));
        y += s;

        // Row 10: Traffic
        addWidget(new BoolButton(col1, y, w, "Traffic",
            ModConfigHandler.COMMON.POLICE_TRAFFIC_VIOLATIONS_ENABLED));
        addWidget(new DoubleSlider(col2, y, w, "Limit:%.0f",
            ModConfigHandler.COMMON.POLICE_SPEED_LIMIT_DEFAULT, 20, 200));
        y += s;

        // Row 11: Warnings & Wanted
        addWidget(new BoolButton(col1, y, w, "Warnings",
            ModConfigHandler.COMMON.POLICE_WARNING_ENABLED));
        addWidget(new IntSlider(col2, y, w, "WarnT:%ds",
            ModConfigHandler.COMMON.POLICE_WARNING_TIMEOUT_SECONDS, 5, 60));
        addWidget(new IntSlider(col3, y, w, "PosterLv:%d",
            ModConfigHandler.COMMON.POLICE_WANTED_POSTERS_MIN_LEVEL, 1, 5));

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
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("§c44 Police Options - All configurable!"),
            this.width / 2, 25, 0xFF5555);
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
