package de.rolandsw.schedulemc.coffee.screen;

import de.rolandsw.schedulemc.coffee.CoffeeRoastLevel;
import de.rolandsw.schedulemc.coffee.menu.MediumCoffeeRoasterMenu;
import de.rolandsw.schedulemc.coffee.network.CoffeeNetworking;
import de.rolandsw.schedulemc.coffee.network.RoasterLevelPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für Medium Coffee Roaster (32 Bohnen Kapazität)
 */
public class MediumCoffeeRoasterScreen extends AbstractContainerScreen<MediumCoffeeRoasterMenu> {

    public MediumCoffeeRoasterScreen(MediumCoffeeRoasterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 140;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true; // Consume event, prevent closing
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && menu.blockEntity != null) { // Left click
            int x = this.leftPos;
            int y = this.topPos;
            int buttonY = y + 65;
            int buttonWidth = 35;
            int buttonHeight = 16;
            int spacing = 37;

            if (isMouseOver(mouseX, mouseY, x + 8, buttonY, buttonWidth, buttonHeight)) {
                CoffeeNetworking.sendToServer(new RoasterLevelPacket(menu.blockEntity.getBlockPos(), CoffeeRoastLevel.LIGHT));
                return true;
            }
            if (isMouseOver(mouseX, mouseY, x + 8 + spacing, buttonY, buttonWidth, buttonHeight)) {
                CoffeeNetworking.sendToServer(new RoasterLevelPacket(menu.blockEntity.getBlockPos(), CoffeeRoastLevel.MEDIUM));
                return true;
            }
            if (isMouseOver(mouseX, mouseY, x + 8 + spacing * 2, buttonY, buttonWidth, buttonHeight)) {
                CoffeeNetworking.sendToServer(new RoasterLevelPacket(menu.blockEntity.getBlockPos(), CoffeeRoastLevel.DARK));
                return true;
            }
            if (isMouseOver(mouseX, mouseY, x + 8 + spacing * 3, buttonY, buttonWidth, buttonHeight)) {
                CoffeeNetworking.sendToServer(new RoasterLevelPacket(menu.blockEntity.getBlockPos(), CoffeeRoastLevel.ESPRESSO));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
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
            graphics.fill(x + 76, y + 35, x + 76 + progressWidth, y + 51, 0xFFFF6F00);
        }

        // Roast Level Auswahlbuttons
        drawRoastLevelButtons(graphics, x, y);

        // Player Hotbar
        for (int i = 0; i < 9; i++) {
            drawSlot(graphics, x + 8 + i * 18, y + 116);
        }
    }

    private void drawRoastLevelButtons(GuiGraphics graphics, int x, int y) {
        CoffeeRoastLevel selected = menu.blockEntity != null ? menu.blockEntity.getSelectedRoastLevel() : CoffeeRoastLevel.MEDIUM;

        int buttonY = y + 65;
        int buttonWidth = 35;
        int buttonHeight = 16;
        int spacing = 37;

        // Light
        drawButton(graphics, x + 8, buttonY, buttonWidth, buttonHeight, selected == CoffeeRoastLevel.LIGHT);
        // Medium
        drawButton(graphics, x + 8 + spacing, buttonY, buttonWidth, buttonHeight, selected == CoffeeRoastLevel.MEDIUM);
        // Dark
        drawButton(graphics, x + 8 + spacing * 2, buttonY, buttonWidth, buttonHeight, selected == CoffeeRoastLevel.DARK);
        // Espresso
        drawButton(graphics, x + 8 + spacing * 3, buttonY, buttonWidth, buttonHeight, selected == CoffeeRoastLevel.ESPRESSO);
    }

    private void drawButton(GuiGraphics graphics, int x, int y, int width, int height, boolean selected) {
        int color = selected ? 0xFFFF6F00 : 0xFF555555;
        graphics.fill(x, y, x + width, y + height, color);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF2B2B2B);
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
        graphics.drawString(this.font, Component.translatable("gui.coffee.medium_roaster").getString(), x + 8, y + 6, 0xFFFFFF, false);

        // Kapazität
        graphics.drawString(this.font, Component.translatable("gui.coffee.capacity_beans", 32).getString(), x + 8, y + 22, 0xCCCCCC, false);

        // Progress Prozent
        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();
        if (maxProgress > 0 && progress > 0) {
            int percent = (int) (100 * ((float) progress / maxProgress));
            graphics.drawString(this.font, Component.translatable("gui.progress_percent", percent).getString(), x + 80, y + 56, 0xFF6F00, false);
        }

        // Labels
        graphics.drawString(this.font, Component.translatable("gui.coffee.label_green").getString(), x + 48, y + 56, 0xCCCCCC, false);
        graphics.drawString(this.font, Component.translatable("gui.coffee.label_roasted").getString(), x + 100, y + 56, 0xCCCCCC, false);

        // Roast Level Button Labels
        int buttonY = y + 67;
        graphics.drawString(this.font, Component.translatable("gui.coffee.roast_light").getString(), x + 13, buttonY + 4, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("gui.coffee.roast_medium").getString(), x + 47, buttonY + 4, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("gui.coffee.roast_dark").getString(), x + 82, buttonY + 4, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("gui.coffee.roast_espresso").getString(), x + 124, buttonY + 4, 0xFFFFFF, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Leer lassen - wir rendern in render()
    }
}
