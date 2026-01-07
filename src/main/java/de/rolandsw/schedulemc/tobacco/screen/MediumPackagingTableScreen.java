package de.rolandsw.schedulemc.tobacco.screen;
import de.rolandsw.schedulemc.util.UIColors;

import de.rolandsw.schedulemc.tobacco.menu.MediumPackagingTableMenu;
import de.rolandsw.schedulemc.tobacco.network.ModNetworking;
import de.rolandsw.schedulemc.tobacco.network.MediumPackageRequestPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für Medium Packaging Table (10g)
 */
public class MediumPackagingTableScreen extends AbstractContainerScreen<MediumPackagingTableMenu> {

    public MediumPackagingTableScreen(MediumPackagingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 190;  // Erhöht für mehr Platz
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 50;
        int buttonHeight = 18;
        int centerX = this.leftPos + this.imageWidth / 2;
        int buttonY = this.topPos + 145;  // Unter den Slots

        // 10g Button
        this.addRenderableWidget(
            Button.builder(
                Component.literal("10g"),
                btn -> onPackageButton(10)
            )
            .bounds(centerX - 55, buttonY, buttonWidth, buttonHeight)
            .build()
        );

        // Unpack Button
        this.addRenderableWidget(
            Button.builder(
                Component.literal("Unpack"),
                btn -> onPackageButton(-1)
            )
            .bounds(centerX + 5, buttonY, buttonWidth, buttonHeight)
            .build()
        );
    }

    private void onPackageButton(int weight) {
        ModNetworking.sendToServer(new MediumPackageRequestPacket(menu.blockEntity.getBlockPos(), weight));
    }    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true; // Consume event, prevent closing
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }



    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, UIColors.BACKGROUND_MEDIUM);
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, UIColors.BACKGROUND_PANEL);
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + 18, UIColors.BACKGROUND_DARK);

        renderSlotBorders(graphics, x, y);
    }

    private void renderSlotBorders(GuiGraphics graphics, int x, int y) {
        // Input Slot
        drawSlot(graphics, x + 80, y + 20);

        // Schachteln-Slots
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++) {
                drawSlot(graphics, x + 71 + col * 18, y + 50 + row * 18);
            }
        }

        // Player Hotbar (nur Schnellzugriffsleiste)
        for (int i = 0; i < 9; i++) {
            drawSlot(graphics, x + 8 + i * 18, y + 168);  // Weiter nach unten
        }
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, UIColors.GRAY_LIGHT);
        graphics.fill(x, y, x + 16, y + 16, UIColors.BACKGROUND_LIGHTER);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;
        graphics.drawString(this.font, "Mittlerer Packtisch", x + 8, y + 6, 0xFFFFFF, false);
        graphics.drawString(this.font, "Schachteln", x + 71, y + 40, 0xCCCCCC, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
