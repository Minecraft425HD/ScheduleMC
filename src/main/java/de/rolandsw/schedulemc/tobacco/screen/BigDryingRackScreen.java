package de.rolandsw.schedulemc.tobacco.screen;

import de.rolandsw.schedulemc.tobacco.menu.BigDryingRackMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für Big Drying Rack (10 Blätter Kapazität)
 */
public class BigDryingRackScreen extends AbstractContainerScreen<BigDryingRackMenu> {

    public BigDryingRackScreen(BigDryingRackMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 108;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
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

        // Dunkler Hintergrund
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF2B2B2B);

        // Hellerer innerer Bereich
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, 0xFF4C4C4C);

        // Header-Bereich
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + 18, 0xFF1E1E1E);

        // Slot-Umrandungen
        drawSlot(graphics, x + 56, y + 35);  // Input
        drawSlot(graphics, x + 116, y + 35); // Output

        // Progress-Pfeil Hintergrund
        graphics.fill(x + 76, y + 35, x + 100, y + 51, 0xFF373737);

        // Progress-Pfeil füllen
        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();
        if (maxProgress > 0 && progress > 0) {
            int progressWidth = (int) (24 * ((float) progress / maxProgress));
            graphics.fill(x + 76, y + 35, x + 76 + progressWidth, y + 51, 0xFFF44336);
        }

        // Player Hotbar
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
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;

        // Title
        graphics.drawString(this.font, Component.translatable("gui.big_drying_rack.title").getString(), x + 8, y + 6, 0xFFFFFF, false);

        // Kapazität
        graphics.drawString(this.font, Component.translatable("gui.big_drying_rack.capacity").getString(), x + 8, y + 22, 0xCCCCCC, false);

        // Progress Prozent
        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();
        if (maxProgress > 0 && progress > 0) {
            int percent = (int) (100 * ((float) progress / maxProgress));
            graphics.drawString(this.font, Component.translatable("gui.progress_percent", percent).getString(), x + 80, y + 56, 0xF44336, false);
        }

        // Labels
        graphics.drawString(this.font, Component.translatable("gui.big_drying_rack.fresh").getString(), x + 48, y + 56, 0xCCCCCC, false);
        graphics.drawString(this.font, Component.translatable("gui.big_drying_rack.dry").getString(), x + 104, y + 56, 0xCCCCCC, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Leer lassen - wir rendern in render()
    }
}
