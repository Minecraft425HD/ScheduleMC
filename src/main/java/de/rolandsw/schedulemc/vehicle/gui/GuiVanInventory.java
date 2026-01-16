package de.rolandsw.schedulemc.vehicle.gui;

import de.maxhenkel.corelib.inventory.ScreenBase;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI for Van vehicle inventory - 6 internal slots
 */
public class GuiVanInventory extends ScreenBase<ContainerVanInventory> {

    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.parse("textures/gui/container/shulker_box.png");

    private EntityGenericVehicle vehicle;
    private Inventory playerInventory;

    public GuiVanInventory(ContainerVanInventory container, Inventory playerInventory, Component title) {
        super(GUI_TEXTURE, container, playerInventory, title);
        this.vehicle = container.getVehicle();
        this.playerInventory = playerInventory;
        imageWidth = 176;
        imageHeight = 166; // 1 partial row
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        // Vehicle title
        guiGraphics.drawString(font, vehicle.getDisplayName().getVisualOrderText(), 8, 6, FONT_COLOR, false);

        // Player inventory label
        guiGraphics.drawString(font, playerInventory.getDisplayName().getVisualOrderText(), 8, imageHeight - 96 + 3, FONT_COLOR, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
