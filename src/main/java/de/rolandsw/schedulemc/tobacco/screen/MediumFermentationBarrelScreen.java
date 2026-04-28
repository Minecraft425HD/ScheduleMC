package de.rolandsw.schedulemc.tobacco.screen;

import de.rolandsw.schedulemc.tobacco.menu.MediumFermentationBarrelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für das Mittlere Fermentierungsfass.
 * Identisches Layout wie Small, jedoch mit Orange-Palette.
 */
public class MediumFermentationBarrelScreen extends AbstractContainerScreen<MediumFermentationBarrelMenu> {

    // Orange-Palette für Medium
    private static final int COLOR_PROGRESS  = 0xFFFF9800;
    private static final int COLOR_HIGHLIGHT = 0xFFFFCC02;
    private static final int COLOR_CAPACITY  = 0xFFE65100;
    private static final int COLOR_TEXT      = 0xFFFF9800;

    public MediumFermentationBarrelScreen(MediumFermentationBarrelMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return keyCode == 69 || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        g.fill(x,     y,     x + imageWidth,     y + imageHeight,     0xFF2B2B2B);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF4C4C4C);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + 18,              0xFF1E1E1E);

        drawSlot(g, x + 22,  y + 30);
        drawSlot(g, x + 134, y + 30);

        // Fortschrittsbalken
        g.fill(x + 54, y + 26, x + 122, y + 52, 0xFF1A1A1A);
        int progress  = menu.getProgressPercent();
        int progressW = (int)(68 * (progress / 100f));
        if (progressW > 0) {
            g.fill(x + 54, y + 26, x + 54 + progressW, y + 52, COLOR_PROGRESS);
            g.fill(x + 54, y + 26, x + 54 + progressW, y + 29, COLOR_HIGHLIGHT);
        }

        g.fill(x + 8, y + 62, x + 168, y + 63, 0xFF5A5A5A);

        // Kapazitätsbalken
        g.fill(x + 8, y + 77, x + 168, y + 85, 0xFF1A1A1A);
        int cap   = menu.getCapacity();
        int count = menu.getInputCount();
        if (cap > 0 && count > 0) {
            int capW = (int)(160 * (count / (float) cap));
            g.fill(x + 8, y + 77, x + 8 + capW, y + 85, COLOR_CAPACITY);
        }

        g.fill(x + 8, y + 112, x + 168, y + 113, 0xFF5A5A5A);

        for (int i = 0; i < 9; i++) {
            drawSlot(g, x + 8 + i * 18, y + 127);
        }
    }

    private void drawSlot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
        g.fill(x,     y,     x + 16, y + 16,  0xFF373737);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;

        g.drawString(font,
            Component.translatable("block.schedulemc.medium_fermentation_barrel").getString(),
            x + 8, y + 6, 0xFFFFFF, false);

        g.drawString(font,
            Component.translatable("gui.fermentation.input_label").getString(),
            x + 16, y + 21, 0xAAAAAA, false);
        g.drawString(font,
            Component.translatable("gui.fermentation.output_label").getString(),
            x + 128, y + 21, 0xAAAAAA, false);

        int progress = menu.getProgressPercent();
        int textColor = progress > 0 ? COLOR_TEXT : 0x666666;
        g.drawCenteredString(font, progress + "%", x + 88, y + 35, textColor);

        g.drawString(font,
            Component.translatable("block.fermentation.capacity",
                menu.getInputCount(), menu.getCapacity()).getString(),
            x + 8, y + 67, 0xCCCCCC, false);

        String ready = Component.translatable("gui.fermentation.output_label").getString()
            + ": " + menu.getOutputCount();
        g.drawString(font, ready, x + 8, y + 90, 0xCCCCCC, false);

        g.drawString(font,
            Component.translatable("gui.progress_percent", progress).getString(),
            x + 90, y + 90, COLOR_TEXT, false);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
        // Alle Labels werden in render() gezeichnet
    }
}
