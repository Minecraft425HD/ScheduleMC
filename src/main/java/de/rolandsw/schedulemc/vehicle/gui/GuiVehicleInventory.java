package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiVehicleInventory extends ScreenBase<ContainerVehicleInventory> {

    private static final ResourceLocation GUI_TEXTURE_3 = ResourceLocation.parse("textures/gui/container/shulker_box.png");
    private static final ResourceLocation GUI_TEXTURE_6 = ResourceLocation.parse("textures/gui/container/generic_54.png");

    private EntityGenericVehicle vehicle;
    private Inventory playerInventory;

    public GuiVehicleInventory(ContainerVehicleInventory vehicleInventory, Inventory playerInventory, Component title) {
        super(vehicleInventory.getRows() == 3 ? GUI_TEXTURE_3 : GUI_TEXTURE_6, vehicleInventory, playerInventory, title);
        this.vehicle = vehicleInventory.getVehicle();
        this.playerInventory = playerInventory;
        imageWidth = 176;
        imageHeight = vehicleInventory.getRows() == 3 ? 166 : 222;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        int yOffset = 6;

        // Vehicle title
        guiGraphics.drawString(font, vehicle.getDisplayName().getVisualOrderText(), 8, yOffset, FONT_COLOR, false);

        // Odometer
        long odo = vehicle.getOdometer();
        String odometerText = String.format("%,d Bl.", odo).replace(',', '.');
        int odometerWidth = font.width(odometerText);
        guiGraphics.drawString(font, odometerText, imageWidth - 8 - odometerWidth, yOffset, FONT_COLOR, false);

        // Internal inventory label (if it exists)
        int internalSlots = menu.getInternalSlots();
        if (internalSlots > 0) {
            yOffset += 12; // Move below title
            guiGraphics.drawString(font, Component.translatable("gui.vehicle.internal_inventory").getVisualOrderText(), 8, yOffset, FONT_COLOR, false);
        }

        // External inventory label (if it exists)
        int externalSlots = menu.getExternalSlots();
        if (externalSlots > 0) {
            int internalRows = internalSlots > 0 ? (int) Math.ceil(internalSlots / 9.0) : 0;
            yOffset = 6 + 12 + (internalRows * 18) + 4; // After internal inventory + spacing
            guiGraphics.drawString(font, Component.translatable("gui.vehicle.external_inventory").getVisualOrderText(), 8, yOffset, FONT_COLOR, false);
        }

        // Player inventory label
        guiGraphics.drawString(font, playerInventory.getDisplayName().getVisualOrderText(), 8, imageHeight - 96 + 3, FONT_COLOR, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
