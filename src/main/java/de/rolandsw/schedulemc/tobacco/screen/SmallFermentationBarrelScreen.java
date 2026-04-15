package de.rolandsw.schedulemc.tobacco.screen;

import de.rolandsw.schedulemc.tobacco.menu.SmallFermentationBarrelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class SmallFermentationBarrelScreen extends AbstractContainerScreen<SmallFermentationBarrelMenu> {
    public SmallFermentationBarrelScreen(SmallFermentationBarrelMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 108;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return keyCode == 69 || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF2B2B2B);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF4C4C4C);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + 18, 0xFF1E1E1E);
        drawSlot(graphics, x + 56, y + 35);
        drawSlot(graphics, x + 116, y + 35);
        graphics.fill(x + 76, y + 35, x + 100, y + 51, 0xFF373737);

        int progress = menu.getProgressPercent();
        int progressWidth = (int) (24 * (progress / 100f));
        if (progressWidth > 0) {
            graphics.fill(x + 76, y + 35, x + 76 + progressWidth, y + 51, 0xFF4CAF50);
        }

        for (int i = 0; i < 9; i++) {
            drawSlot(graphics, x + 8 + i * 18, y + 84);
        }
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
        graphics.fill(x, y, x + 16, y + 16, 0xFF373737);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;
        graphics.drawString(font, Component.translatable("block.schedulemc.small_fermentation_barrel").getString(), x + 8, y + 6, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("block.fermentation.capacity", menu.getInputCount(), menu.getCapacity()).getString(), x + 8, y + 22, 0xCCCCCC, false);
        graphics.drawString(font, Component.translatable("gui.fermentation.input_label").getString(), x + 48, y + 56, 0xCCCCCC, false);
        graphics.drawString(font, Component.translatable("gui.fermentation.output_label", menu.getOutputCount()).getString(), x + 104, y + 56, 0xCCCCCC, false);
        graphics.drawString(font, Component.translatable("gui.progress_percent", menu.getProgressPercent()).getString(), x + 80, y + 70, 0x4CAF50, false);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        // Drawn in render()
    }
}
