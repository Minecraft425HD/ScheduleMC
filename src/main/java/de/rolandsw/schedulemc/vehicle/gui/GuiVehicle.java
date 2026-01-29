package de.rolandsw.schedulemc.vehicle.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.math.MathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Card-based vehicle GUI - fully programmatic rendering.
 * Shows vehicle status, special slots, vehicle inventory, and hotbar.
 */
public class GuiVehicle extends ScreenBase<ContainerVehicle> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/gui_vehicle.png");

    // Frame colors (3D beveled Minecraft style)
    private static final int COL_BLACK = 0xFF000000;
    private static final int COL_WHITE = 0xFFFFFFFF;
    private static final int COL_SHADOW = 0xFF555555;
    private static final int COL_BG = 0xFFC6C6C6;

    // Card colors
    private static final int COL_CARD_BG = 0xFFAAAAAA;
    private static final int COL_CARD_HEADER = 0xFF404040;
    private static final int COL_SLOT_BG = 0xFF8B8B8B;

    // Bar colors
    private static final int COL_BAR_BG = 0xFF555555;
    private static final int COL_GREEN = 0xFF00CC00;
    private static final int COL_YELLOW = 0xFFCCCC00;
    private static final int COL_RED = 0xFFCC0000;

    // Text colors
    private static final int TEXT_DARK = 0x404040;
    private static final int TEXT_WHITE = 0xFFFFFF;
    private static final int TEXT_LIGHT = 0xDDDDDD;

    private Inventory playerInv;
    private EntityGenericVehicle vehicle;
    private int internalRows;
    private int externalRows;
    private int internalSlots;
    private int externalSlots;
    private int hotbarY; // Y position for hotbar slots

    public GuiVehicle(ContainerVehicle containerVehicle, Inventory playerInv, Component title) {
        super(TEXTURE, containerVehicle, playerInv, title);
        this.playerInv = playerInv;
        this.vehicle = containerVehicle.getVehicle();

        imageWidth = 176;

        internalSlots = vehicle.getInternalInventory().getContainerSize();
        externalSlots = vehicle.getExternalInventory().getContainerSize();
        internalRows = internalSlots > 0 ? (int) Math.ceil(internalSlots / 9.0) : 0;
        externalRows = externalSlots > 0 ? (int) Math.ceil(externalSlots / 9.0) : 0;

        // Hotbar Y from container (matches slot positions)
        hotbarY = containerVehicle.getHotbarY();

        // imageHeight: hotbar bottom + margin
        imageHeight = hotbarY + 18 + 6;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        // Fully programmatic - do NOT call super
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = leftPos;
        int y = topPos;
        int w = imageWidth;
        int h = imageHeight;

        // === Main GUI frame (3D beveled) ===
        drawFrame(g, x, y, w, h);

        // === STATUS CARD (Y=4 to Y=70, height=66 - extra bottom padding) ===
        drawInsetPanel(g, x + 5, y + 4, w - 10, 66, COL_CARD_BG);
        g.fill(x + 6, y + 5, x + w - 6, y + 17, COL_CARD_HEADER);

        // Status progress bars (12px line spacing, bars at text+2)
        int barX = x + 68;
        int barW = 42;
        drawBar(g, barX, y + 21, barW, 5, getFuelPercent(), false);
        drawBar(g, barX, y + 33, barW, 5, getBatteryPercent(), false);
        drawBar(g, barX, y + 45, barW, 5, getDamagePercent(), true);
        drawBar(g, barX, y + 57, barW, 5, getTemperaturePercent(), true);

        // === WARTUNG SECTION (Y=71 to Y=93, height=22) ===
        drawInsetPanel(g, x + 5, y + 71, w - 10, 22, COL_CARD_BG);
        // Single maintenance slot background (Y=73 fixed by container)
        drawSlotBg(g, x + 150, y + 73);

        // === VEHICLE NAME + INVENTORY CARD (Y=94 to ...) ===
        int invHeight = internalRows * 18;
        if (internalRows > 0 && externalRows > 0) invHeight += 2;
        invHeight += externalRows * 18;

        if (invHeight > 0) {
            drawInsetPanel(g, x + 5, y + 94, w - 10, invHeight + 13, COL_BG);
            g.fill(x + 6, y + 95, x + w - 6, y + 105, COL_CARD_HEADER);

            // Vehicle inventory slot backgrounds (Y=105 fixed by container)
            int slotY = y + 105;
            for (int row = 0; row < internalRows; row++) {
                for (int col = 0; col < 9 && (row * 9 + col) < internalSlots; col++) {
                    drawSlotBg(g, x + 8 + col * 18, slotY + row * 18);
                }
            }
            slotY += internalRows * 18;
            if (internalRows > 0 && externalRows > 0) slotY += 2;
            for (int row = 0; row < externalRows; row++) {
                for (int col = 0; col < 9 && (row * 9 + col) < externalSlots; col++) {
                    drawSlotBg(g, x + 8 + col * 18, slotY + row * 18);
                }
            }
        }

        // === HOTBAR CARD ===
        drawInsetPanel(g, x + 5, y + hotbarY - 14, w - 10, 35, COL_BG);
        g.fill(x + 6, y + hotbarY - 13, x + w - 6, y + hotbarY - 2, COL_CARD_HEADER);

        // Hotbar slot backgrounds (1 row x 9)
        for (int col = 0; col < 9; col++) {
            drawSlotBg(g, x + 8 + col * 18, y + hotbarY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Fully custom labels - do NOT call super

        // === Status card header ===
        g.drawString(font, Component.translatable("gui.vehicle.status.title"), 8, 6, TEXT_WHITE, false);

        // === Status lines (12px spacing) ===
        int ly = 19;
        g.drawString(font, Component.translatable("gui.vehicle.status.fuel"), 9, ly, TEXT_DARK, false);
        drawRightAligned(g, getFuelValueString(), 168, ly, TEXT_DARK);

        ly += 12;
        g.drawString(font, Component.translatable("gui.vehicle.status.battery"), 9, ly, TEXT_DARK, false);
        drawRightAligned(g, getBatteryValueString(), 168, ly, TEXT_DARK);

        ly += 12;
        g.drawString(font, Component.translatable("gui.vehicle.status.damage"), 9, ly, TEXT_DARK, false);
        drawRightAligned(g, getDamageValueString(), 168, ly, TEXT_DARK);

        ly += 12;
        g.drawString(font, Component.translatable("gui.vehicle.status.temperature"), 9, ly, TEXT_DARK, false);
        drawRightAligned(g, getTempValueString(), 168, ly, TEXT_DARK);

        // === Wartung label ===
        g.drawString(font, Component.translatable("gui.vehicle.wartung"), 8, 74, TEXT_DARK, false);

        // === Vehicle name + Odometer ===
        if (internalSlots > 0 || externalSlots > 0) {
            g.drawString(font, vehicle.getDisplayName().getVisualOrderText(), 8, 96, TEXT_WHITE, false);
            drawRightAligned(g, getOdometerValueString(), 170, 96, TEXT_LIGHT);
        } else {
            g.drawString(font, vehicle.getDisplayName().getVisualOrderText(), 8, 96, TEXT_DARK, false);
            drawRightAligned(g, getOdometerValueString(), 170, 96, TEXT_DARK);
        }

        // === Hotbar label (inside dark header) ===
        g.drawString(font, Component.translatable("gui.vehicle.hotbar"), 8, hotbarY - 11, TEXT_WHITE, false);
    }

    // ==================== Drawing Helpers ====================

    private void drawFrame(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + 1, COL_BLACK);
        g.fill(x, y + h - 1, x + w, y + h, COL_BLACK);
        g.fill(x, y, x + 1, y + h, COL_BLACK);
        g.fill(x + w - 1, y, x + w, y + h, COL_BLACK);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, COL_WHITE);
        g.fill(x + 1, y + 1, x + 2, y + h - 1, COL_WHITE);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, COL_SHADOW);
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, COL_SHADOW);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, COL_BG);
    }

    private void drawInsetPanel(GuiGraphics g, int x, int y, int w, int h, int bgColor) {
        g.fill(x, y, x + w, y + 1, COL_SHADOW);
        g.fill(x, y, x + 1, y + h, COL_SHADOW);
        g.fill(x, y + h - 1, x + w, y + h, COL_WHITE);
        g.fill(x + w - 1, y, x + w, y + h, COL_WHITE);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bgColor);
    }

    private void drawSlotBg(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 1, COL_SHADOW);
        g.fill(x, y, x + 1, y + 18, COL_SHADOW);
        g.fill(x + 1, y + 17, x + 18, y + 18, COL_WHITE);
        g.fill(x + 17, y + 1, x + 18, y + 18, COL_WHITE);
        g.fill(x + 1, y + 1, x + 17, y + 17, COL_SLOT_BG);
    }

    private void drawBar(GuiGraphics g, int x, int y, int w, int h, float percent, boolean inverted) {
        g.fill(x, y, x + w, y + h, COL_BAR_BG);
        float clamped = Math.max(0, Math.min(100, percent));
        int fillW = (int) (w * clamped / 100.0f);
        if (fillW > 0) {
            g.fill(x, y, x + fillW, y + h, getBarColor(clamped, inverted));
        }
    }

    private int getBarColor(float percent, boolean inverted) {
        float eff = inverted ? percent : (100 - percent);
        if (eff > 75) return COL_RED;
        if (eff > 50) return COL_YELLOW;
        return COL_GREEN;
    }

    private void drawRightAligned(GuiGraphics g, String text, int rightX, int y, int color) {
        g.drawString(font, text, rightX - font.width(text), y, color, false);
    }

    // ==================== Data Methods ====================

    public float getFuelPercent() {
        float fuelPerc = ((float) vehicle.getFuelComponent().getFuelAmount()) / ((float) vehicle.getMaxFuel()) * 100F;
        return MathUtils.round(fuelPerc, 2);
    }

    public float getFuelLiters() {
        return MathUtils.round(vehicle.getFuelComponent().getFuelAmount() / 1000.0F, 1);
    }

    public float getBatteryPercent() {
        return MathUtils.round(vehicle.getBatteryComponent().getBatteryPercentage() * 100F, 1);
    }

    public float getBatteryVolts() {
        float percentage = vehicle.getBatteryComponent().getBatteryPercentage();
        return MathUtils.round(11.0F + (percentage * 1.5F), 1);
    }

    public float getTemperatureCelsius() {
        return MathUtils.round(vehicle.getDamageComponent().getTemperature(), 1);
    }

    public float getTemperaturePercent() {
        return Math.min(100, getTemperatureCelsius() / 120.0f * 100.0f);
    }

    public float getDamagePercent() {
        float dmg = vehicle.getDamageComponent().getDamage();
        return MathUtils.round(Math.min(dmg, 100), 1);
    }

    public String getFuelValueString() {
        return String.format("%.0f%% %.1fL", getFuelPercent(), getFuelLiters());
    }

    public String getBatteryValueString() {
        return String.format("%.0f%% %.1fV", getBatteryPercent(), getBatteryVolts());
    }

    public String getDamageValueString() {
        return String.format("%.1f%%", getDamagePercent());
    }

    public String getTempValueString() {
        return String.format("%.0f°C", getTemperatureCelsius());
    }

    public String getOdometerValueString() {
        long odo = vehicle.getOdometer();
        return String.format("%,d Bl.", odo).replace(',', '.');
    }
}
