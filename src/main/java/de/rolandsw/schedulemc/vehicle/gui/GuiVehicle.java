package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.math.MathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiVehicle extends ScreenBase<ContainerVehicle> {

    private static final ResourceLocation VEHICLE_GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/gui_vehicle.png");

    private static final int fontColor = 4210752;

    private Inventory playerInv;
    private EntityGenericVehicle vehicle;

    public GuiVehicle(ContainerVehicle containerVehicle, Inventory playerInv, Component title) {
        super(VEHICLE_GUI_TEXTURE, containerVehicle, playerInv, title);
        this.playerInv = playerInv;
        this.vehicle = containerVehicle.getVehicle();

        imageWidth = 176;

        // Calculate total rows needed for internal + external inventory
        int internalSlots = vehicle.getInternalInventory().getContainerSize();
        int externalSlots = vehicle.getExternalInventory().getContainerSize();
        int internalRows = internalSlots > 0 ? (int) Math.ceil(internalSlots / 9.0) : 0;
        int externalRows = externalSlots > 0 ? (int) Math.ceil(externalSlots / 9.0) : 0;
        int totalRows = internalRows + externalRows;

        // Status area (90px) + inventory rows + spacing + bottom margin
        imageHeight = 90 + totalRows * 18 + (totalRows > 0 ? 2 : 0) + 18;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        // Status Header centered (mit Übersetzung)
        Component header = Component.translatable("gui.vehicle.status.title");
        int headerX = (imageWidth - font.width(header)) / 2;
        guiGraphics.drawString(font, header, headerX, 6, fontColor, false);

        // Status values in two-column format
        int startY = 18;
        int lineHeight = 12;
        int labelX = 8;
        int valueX = 72; // Values start here (after longest label)

        // Draw labels and values (mit Übersetzungen)
        guiGraphics.drawString(font, Component.translatable("gui.vehicle.status.fuel"), labelX, startY, fontColor, false);
        guiGraphics.drawString(font, getFuelValueString(), valueX, startY, fontColor, false);

        guiGraphics.drawString(font, Component.translatable("gui.vehicle.status.battery"), labelX, startY + lineHeight, fontColor, false);
        guiGraphics.drawString(font, getBatteryValueString(), valueX, startY + lineHeight, fontColor, false);

        guiGraphics.drawString(font, Component.translatable("gui.vehicle.status.damage"), labelX, startY + lineHeight * 2, fontColor, false);
        guiGraphics.drawString(font, getDamageValueString(), valueX, startY + lineHeight * 2, fontColor, false);

        guiGraphics.drawString(font, Component.translatable("gui.vehicle.status.temperature"), labelX, startY + lineHeight * 3, fontColor, false);
        guiGraphics.drawString(font, getTempValueString(), valueX, startY + lineHeight * 3, fontColor, false);

        // Vehicle name at bottom of status area
        guiGraphics.drawString(font, vehicle.getDisplayName().getVisualOrderText(), 8, 87, fontColor, false);
    }

    private void drawRightAlignedString(GuiGraphics guiGraphics, String text, int rightX, int y) {
        int width = font.width(text);
        guiGraphics.drawString(font, text, rightX - width, y, fontColor, false);
    }

    // ===== Calculation Methods =====

    public float getFuelPercent() {
        float fuelPerc = ((float) vehicle.getFuelComponent().getFuelAmount()) / ((float) vehicle.getMaxFuel()) * 100F;
        return MathUtils.round(fuelPerc, 2);
    }

    public float getFuelLiters() {
        float liters = vehicle.getFuelComponent().getFuelAmount() / 1000.0F;
        return MathUtils.round(liters, 1);
    }

    public float getBatteryPercent() {
        return MathUtils.round(vehicle.getBatteryComponent().getBatteryPercentage() * 100F, 1);
    }

    public float getBatteryVolts() {
        // Battery voltage range: 11.0V (0%) to 12.5V (100%)
        float percentage = vehicle.getBatteryComponent().getBatteryPercentage();
        float volts = 11.0F + (percentage * 1.5F);
        return MathUtils.round(volts, 1);
    }

    public float getTemperatureCelsius() {
        return MathUtils.round(vehicle.getDamageComponent().getTemperature(), 1);
    }

    public float getDamagePercent() {
        float dmg = vehicle.getDamageComponent().getDamage();
        dmg = Math.min(dmg, 100);
        return MathUtils.round(dmg, 1);
    }

    // ===== Display String Methods =====

    public String getFuelValueString() {
        return String.format("%.1f%% | %.1f Liter", getFuelPercent(), getFuelLiters());
    }

    public String getBatteryValueString() {
        return String.format("%.1f%% | %.1f Volt", getBatteryPercent(), getBatteryVolts());
    }

    public String getDamageValueString() {
        return String.format("%.1f%%", getDamagePercent());
    }

    public String getTempValueString() {
        return String.format("%.1f°C", getTemperatureCelsius());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);

        // Cover old bar graphics from the background texture with a solid rectangle
        // This rectangle covers the status area where old bars were displayed
        // Stops before the 3 special slots (fuel, battery, repair) at Y=66
        int bgColor = 0xFFC6C6C6; // Light gray matching GUI background
        guiGraphics.fill(leftPos + 7, topPos + 5, leftPos + 169, topPos + 62, bgColor);

        // Cover unused slot graphics with background color
        int internalSlots = vehicle.getInternalInventory().getContainerSize();
        int externalSlots = vehicle.getExternalInventory().getContainerSize();

        int slotY = 98; // Start Y for internal slots

        // Cover unused internal inventory slots
        if (internalSlots > 0) {
            int internalRows = (int) Math.ceil(internalSlots / 9.0);
            int usedSlotsInLastRow = internalSlots % 9;
            if (usedSlotsInLastRow == 0) usedSlotsInLastRow = 9;

            // Cover unused slots in the last row
            int lastRowY = slotY + (internalRows - 1) * 18;
            int startX = 8 + usedSlotsInLastRow * 18;
            guiGraphics.fill(leftPos + startX, topPos + lastRowY, leftPos + 8 + 9 * 18, topPos + lastRowY + 16, bgColor);

            slotY += internalRows * 18 + 2;
        }

        // Cover unused external inventory slots
        if (externalSlots > 0) {
            int externalRows = (int) Math.ceil(externalSlots / 9.0);
            int usedSlotsInLastRow = externalSlots % 9;
            if (usedSlotsInLastRow == 0) usedSlotsInLastRow = 9;

            // Cover unused slots in the last row
            int lastRowY = slotY + (externalRows - 1) * 18;
            int startX = 8 + usedSlotsInLastRow * 18;
            guiGraphics.fill(leftPos + startX, topPos + lastRowY, leftPos + 8 + 9 * 18, topPos + lastRowY + 16, bgColor);
        }
    }

}
