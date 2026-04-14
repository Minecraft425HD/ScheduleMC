package de.rolandsw.schedulemc.tobacco.screen;

import de.rolandsw.schedulemc.tobacco.menu.BigFermentationBarrelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class BigFermentationBarrelScreen extends AbstractContainerScreen<BigFermentationBarrelMenu> {
    public BigFermentationBarrelScreen(BigFermentationBarrelMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 110;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF2B2B2B);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF4C4C4C);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + 18, 0xFF1E1E1E);
        graphics.fill(x + 18, y + 44, x + 158, y + 56, 0xFF373737);

        int progress = menu.getProgressPercent();
        int progressWidth = (int) (1.4f * progress);
        if (progressWidth > 0) {
            graphics.fill(x + 18, y + 44, x + 18 + progressWidth, y + 56, 0xFF8B5A2B);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;
        graphics.drawString(font, Component.translatable("block.big_fermentation_barrel.header"), x + 8, y + 6, 0xFFFFFF, false);
        graphics.drawString(font, "Input: " + menu.getInputCount() + "/" + menu.getCapacity(), x + 8, y + 24, 0xE0E0E0, false);
        graphics.drawString(font, "Output: " + menu.getOutputCount(), x + 8, y + 34, 0xE0E0E0, false);
        graphics.drawString(font, "Progress: " + menu.getProgressPercent() + "%", x + 8, y + 60, 0xD7A46A, false);
        graphics.drawString(font, "RMB: Add leaf | Shift+RMB: Extract", x + 8, y + 78, 0xCFCFCF, false);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        // Drawn in render()
    }
}
