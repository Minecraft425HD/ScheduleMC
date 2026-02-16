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
 * Werkstatt/Workshop Config Screen - 12 Vehicle Workshop Options
 */
@OnlyIn(Dist.CLIENT)
public class WerkstattConfigScreen extends Screen {
    private final Screen parent;

    public WerkstattConfigScreen(Screen parent) {
        super(Component.literal("Werkstatt/Workshop Settings"));
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

        // === BASIC SERVICES ===

        this.addRenderableWidget(new DoubleSlider(leftCol, y, w,
            "Inspection: §e%.0f€",
            ModConfigHandler.COMMON.WERKSTATT_BASE_INSPECTION_FEE, 1, 1000));

        this.addRenderableWidget(new DoubleSlider(rightCol, y, w,
            "Repair/%%: §e%.0f€",
            ModConfigHandler.COMMON.WERKSTATT_REPAIR_COST_PER_PERCENT, 1, 500));

        this.addRenderableWidget(new DoubleSlider(leftCol, y + s, w,
            "Battery/%%: §e%.0f€",
            ModConfigHandler.COMMON.WERKSTATT_BATTERY_COST_PER_PERCENT, 1, 500));

        this.addRenderableWidget(new DoubleSlider(rightCol, y + s, w,
            "Oil Change: §e%.0f€",
            ModConfigHandler.COMMON.WERKSTATT_OIL_CHANGE_COST, 1, 1000));

        // === MOTOR UPGRADES ===

        this.addRenderableWidget(new DoubleSlider(leftCol, y + s * 2, w,
            "Motor Lvl2: §e%.0f€",
            ModConfigHandler.COMMON.WERKSTATT_MOTOR_UPGRADE_COST_LVL2, 100, 100000));

        this.addRenderableWidget(new DoubleSlider(rightCol, y + s * 2, w,
            "Motor Lvl3: §e%.0f€",
            ModConfigHandler.COMMON.WERKSTATT_MOTOR_UPGRADE_COST_LVL3, 100, 100000));

        // === TANK UPGRADES ===

        this.addRenderableWidget(new DoubleSlider(leftCol, y + s * 3, w,
            "Tank Lvl2: §e%.0f€",
            ModConfigHandler.COMMON.WERKSTATT_TANK_UPGRADE_COST_LVL2, 100, 100000));

        this.addRenderableWidget(new DoubleSlider(rightCol, y + s * 3, w,
            "Tank Lvl3: §e%.0f€",
            ModConfigHandler.COMMON.WERKSTATT_TANK_UPGRADE_COST_LVL3, 100, 100000));

        // === OTHER UPGRADES ===

        this.addRenderableWidget(new DoubleSlider(leftCol, y + s * 4, w,
            "Tires: §e%.0f€",
            ModConfigHandler.COMMON.WERKSTATT_TIRE_UPGRADE_COST, 100, 50000));

        this.addRenderableWidget(new DoubleSlider(rightCol, y + s * 4, w,
            "Paint: §e%.0f€",
            ModConfigHandler.COMMON.WERKSTATT_PAINT_CHANGE_COST, 100, 50000));

        // === FENDER UPGRADES ===

        this.addRenderableWidget(new DoubleSlider(leftCol, y + s * 5, w,
            "Fender Lvl2: §e%.0f€",
            ModConfigHandler.COMMON.WERKSTATT_FENDER_UPGRADE_COST_LVL2, 100, 100000));

        this.addRenderableWidget(new DoubleSlider(rightCol, y + s * 5, w,
            "Fender Lvl3: §e%.0f€",
            ModConfigHandler.COMMON.WERKSTATT_FENDER_UPGRADE_COST_LVL3, 100, 100000));

        // Back Button
        this.addRenderableWidget(Button.builder(
            Component.literal("« Back"),
            button -> this.minecraft.setScreen(parent)
        ).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("§7Vehicle Repair & Upgrade Prices"),
            this.width / 2, 40, 0x808080);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    // === SLIDER CLASS ===

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
}
