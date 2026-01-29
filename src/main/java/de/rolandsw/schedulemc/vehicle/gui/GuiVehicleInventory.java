package de.rolandsw.schedulemc.vehicle.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Card-based vehicle inventory GUI - fully programmatic rendering.
 */
public class GuiVehicleInventory extends ScreenBase<ContainerVehicleInventory> {

    // Keep texture reference for ScreenBase constructor (not actually rendered)
    private static final ResourceLocation GUI_TEXTURE_3 = ResourceLocation.parse("textures/gui/container/shulker_box.png");

    // Frame colors
    private static final int COL_BLACK = 0xFF000000;
    private static final int COL_WHITE = 0xFFFFFFFF;
    private static final int COL_SHADOW = 0xFF555555;
    private static final int COL_BG = 0xFFC6C6C6;

    // Card colors
    private static final int COL_CARD_BG = 0xFFAAAAAA;
    private static final int COL_CARD_HEADER = 0xFF404040;
    private static final int COL_SLOT_BG = 0xFF8B8B8B;

    // Text colors
    private static final int TEXT_DARK = 0x404040;
    private static final int TEXT_WHITE = 0xFFFFFF;
    private static final int TEXT_LIGHT = 0xDDDDDD;

    private EntityGenericVehicle vehicle;
    private Inventory playerInventory;
    private int internalSlots;
    private int externalSlots;
    private int internalRows;
    private int externalRows;
    private int invOffset;

    public GuiVehicleInventory(ContainerVehicleInventory vehicleInventory, Inventory playerInventory, Component title) {
        super(GUI_TEXTURE_3, vehicleInventory, playerInventory, title);
        this.vehicle = vehicleInventory.getVehicle();
        this.playerInventory = playerInventory;
        this.internalSlots = vehicleInventory.getInternalSlots();
        this.externalSlots = vehicleInventory.getExternalSlots();
        this.internalRows = internalSlots > 0 ? (int) Math.ceil(internalSlots / 9.0) : 0;
        this.externalRows = externalSlots > 0 ? (int) Math.ceil(externalSlots / 9.0) : 0;
        this.invOffset = vehicleInventory.getInvOffset();

        imageWidth = 176;
        imageHeight = vehicleInventory.getRows() == 3 ? 166 : 222;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        // Fully programmatic - do NOT call super
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = leftPos;
        int y = topPos;
        int w = imageWidth;
        int h = imageHeight;

        // === Main GUI frame ===
        drawFrame(g, x, y, w, h);

        // === VEHICLE HEADER CARD (Y=3 to Y=14) ===
        drawInsetPanel(g, x + 5, y + 3, w - 10, 12, COL_CARD_HEADER);

        // === VEHICLE INVENTORY CARD ===
        int vehicleInvHeight = 0;
        if (internalRows > 0) vehicleInvHeight += internalRows * 18;
        if (externalRows > 0) {
            if (internalRows > 0) vehicleInvHeight += 4;
            vehicleInvHeight += externalRows * 18;
        }
        if (vehicleInvHeight > 0) {
            drawInsetPanel(g, x + 5, y + 16, w - 10, vehicleInvHeight + 4, COL_CARD_BG);

            // Vehicle inventory slot backgrounds
            int slotY = y + 18;
            for (int row = 0; row < internalRows; row++) {
                for (int col = 0; col < 9 && (row * 9 + col) < internalSlots; col++) {
                    drawSlotBg(g, x + 8 + col * 18, slotY + row * 18);
                }
            }
            slotY += internalRows * 18;
            if (internalRows > 0 && externalRows > 0) slotY += 4;
            for (int row = 0; row < externalRows; row++) {
                for (int col = 0; col < 9 && (row * 9 + col) < externalSlots; col++) {
                    drawSlotBg(g, x + 8 + col * 18, slotY + row * 18);
                }
            }
        }

        // === PLAYER INVENTORY CARD (with dark header for label) ===
        int mainInvY = y + 84 + invOffset;
        int hotbarY = y + 142 + invOffset;
        drawInsetPanel(g, x + 5, mainInvY - 14, w - 10, 93, COL_BG);
        g.fill(x + 6, mainInvY - 13, x + w - 6, mainInvY - 2, COL_CARD_HEADER);

        // Main inventory slots (3 rows × 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBg(g, x + 8 + col * 18, mainInvY + row * 18);
            }
        }
        // Hotbar (1 row × 9)
        for (int col = 0; col < 9; col++) {
            drawSlotBg(g, x + 8 + col * 18, hotbarY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Do NOT call super - fully custom

        // === Vehicle name + Odometer in header ===
        g.drawString(font, vehicle.getDisplayName().getVisualOrderText(), 8, 5, TEXT_WHITE, false);
        long odo = vehicle.getOdometer();
        String odometerText = String.format("%,d Bl.", odo).replace(',', '.');
        int odoWidth = font.width(odometerText);
        g.drawString(font, odometerText, imageWidth - 8 - odoWidth, 5, TEXT_LIGHT, false);

        // === Internal inventory label ===
        if (internalSlots > 0) {
            g.drawString(font, Component.translatable("gui.vehicle.internal_inventory").getVisualOrderText(), 8, 17, TEXT_DARK, false);
        }

        // === External inventory label ===
        if (externalSlots > 0) {
            int extLabelY = 18 + (internalRows * 18);
            if (internalRows > 0) extLabelY += 4;
            extLabelY -= 1; // just above external slots
            g.drawString(font, Component.translatable("gui.vehicle.external_inventory").getVisualOrderText(), 8, extLabelY, TEXT_DARK, false);
        }

        // === Player inventory label (inside dark header) ===
        g.drawString(font, playerInventory.getDisplayName().getVisualOrderText(), 8, 84 + invOffset - 11, TEXT_WHITE, false);
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
