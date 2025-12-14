package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiVehicleInventory extends ScreenBase<ContainerVehicleInventory> {

    private static final ResourceLocation GUI_TEXTURE_3 = new ResourceLocation("textures/gui/container/shulker_box.png");
    private static final ResourceLocation GUI_TEXTURE_6 = new ResourceLocation("textures/gui/container/generic_54.png");

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
        guiGraphics.drawString(font, vehicle.getDisplayName().getVisualOrderText(), 8, 6, FONT_COLOR, false);
        guiGraphics.drawString(font, playerInventory.getDisplayName().getVisualOrderText(), 8, imageHeight - 96 + 3, FONT_COLOR, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
