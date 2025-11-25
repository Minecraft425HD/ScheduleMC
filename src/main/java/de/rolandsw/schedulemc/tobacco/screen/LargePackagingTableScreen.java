package de.rolandsw.schedulemc.tobacco.screen;

import de.rolandsw.schedulemc.tobacco.menu.LargePackagingTableMenu;
import de.rolandsw.schedulemc.tobacco.network.ModNetworking;
import de.rolandsw.schedulemc.tobacco.network.LargePackageRequestPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für Large Packaging Table (20g)
 */
public class LargePackagingTableScreen extends AbstractContainerScreen<LargePackagingTableMenu> {

    public LargePackagingTableScreen(LargePackagingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 110;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 50;
        int buttonHeight = 20;
        int startX = this.leftPos + 8;
        int startY = this.topPos + 60;

        // 20g Button
        this.addRenderableWidget(
            Button.builder(
                Component.literal("20g"),
                btn -> onPackageButton(20)
            )
            .bounds(startX, startY, buttonWidth, buttonHeight)
            .build()
        );

        // Unpack Button
        this.addRenderableWidget(
            Button.builder(
                Component.literal("Unpack"),
                btn -> onPackageButton(-1)
            )
            .bounds(startX + 60, startY, buttonWidth, buttonHeight)
            .build()
        );
    }

    private void onPackageButton(int weight) {
        ModNetworking.sendToServer(new LargePackageRequestPacket(menu.blockEntity.getBlockPos(), weight));
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF2B2B2B);
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, 0xFF4C4C4C);
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + 18, 0xFF1E1E1E);

        renderSlotBorders(graphics, x, y);
    }

    private void renderSlotBorders(GuiGraphics graphics, int x, int y) {
        // Input Slot
        drawSlot(graphics, x + 56, y + 35);

        // Output Slots (3x3 Grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                drawSlot(graphics, x + 116 + col * 18, y + 17 + row * 18);
            }
        }

        // Player Hotbar
        for (int i = 0; i < 9; i++) {
            drawSlot(graphics, x + 8 + i * 18, y + 86);
        }
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
        graphics.fill(x, y, x + 16, y + 16, 0xFF373737);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;
        graphics.drawString(this.font, "Großer Packtisch", x + 8, y + 6, 0xFFFFFF, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
