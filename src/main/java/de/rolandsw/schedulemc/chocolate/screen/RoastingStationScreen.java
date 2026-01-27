package de.rolandsw.schedulemc.chocolate.screen;

import de.rolandsw.schedulemc.chocolate.menu.RoastingStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class RoastingStationScreen extends AbstractContainerScreen<RoastingStationMenu> {

    public RoastingStationScreen(RoastingStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 130;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 69) return true; // Block E key
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Dark background
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF2B2B2B);
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, 0xFF4C4C4C);
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + 18, 0xFF1E1E1E);

        // Slots
        drawSlot(graphics, x + 56, y + 35);  // Input slot
        drawSlot(graphics, x + 116, y + 35); // Output slot

        // Progress bar background
        graphics.fill(x + 76, y + 35, x + 100, y + 51, 0xFF373737);

        // Progress bar fill
        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();
        if (maxProgress > 0) {
            float percentage = (float) progress / maxProgress;
            int progressWidth = (int) (24 * percentage);
            if (progressWidth > 0) {
                graphics.fill(x + 76, y + 35, x + 76 + progressWidth, y + 51, 0xFF8B4513); // Chocolate brown color
            }
        }

        // Player Hotbar
        for (int i = 0; i < 9; i++) {
            drawSlot(graphics, x + 8 + i * 18, y + 106);
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
        graphics.drawString(this.font, "Röststation", x + 8, y + 6, 0xFFFFFF, false);

        // Progress percentage
        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();
        if (progress > 0 && maxProgress > 0) {
            int percent = (int) (100.0f * progress / maxProgress);
            graphics.drawString(this.font, percent + "%", x + 80, y + 56, 0x8B4513, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Empty - render in render() method
    }
}
