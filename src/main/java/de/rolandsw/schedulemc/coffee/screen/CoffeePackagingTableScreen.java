package de.rolandsw.schedulemc.coffee.screen;

import de.rolandsw.schedulemc.coffee.menu.CoffeePackagingTableMenu;
import de.rolandsw.schedulemc.coffee.network.CoffeeNetworking;
import de.rolandsw.schedulemc.coffee.network.CoffeePackageRequestPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für Coffee Packaging Table (Verpackungsstation)
 */
public class CoffeePackagingTableScreen extends AbstractContainerScreen<CoffeePackagingTableMenu> {

    public CoffeePackagingTableScreen(CoffeePackagingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 146;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 50;
        int buttonHeight = 18;
        int centerX = this.leftPos + this.imageWidth / 2;
        int buttonY = this.topPos + 58;

        // 3 Buttons: 250g, 500g, 1kg
        // 250g Button
        this.addRenderableWidget(
            Button.builder(
                Component.translatable("gui.coffee.button_250g"),
                btn -> onPackageButton(250)
            )
            .bounds(centerX - 80, buttonY, buttonWidth, buttonHeight)
            .build()
        );

        // 500g Button
        this.addRenderableWidget(
            Button.builder(
                Component.translatable("gui.coffee.button_500g"),
                btn -> onPackageButton(500)
            )
            .bounds(centerX - 25, buttonY, buttonWidth, buttonHeight)
            .build()
        );

        // 1kg Button
        this.addRenderableWidget(
            Button.builder(
                Component.translatable("gui.coffee.button_1kg"),
                btn -> onPackageButton(1000)
            )
            .bounds(centerX + 30, buttonY, buttonWidth, buttonHeight)
            .build()
        );
    }

    private void onPackageButton(int weight) {
        if (menu.blockEntity != null) {
            CoffeeNetworking.sendToServer(new CoffeePackageRequestPacket(menu.blockEntity.getBlockPos(), weight));
        }
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
        drawSlot(graphics, x + 56, y + 35);  // Coffee Input
        drawSlot(graphics, x + 86, y + 35);  // Bag Input
        drawSlot(graphics, x + 126, y + 35); // Package Output

        // Progress Arrow Background (zwischen Bag und Output)
        graphics.fill(x + 104, y + 37, x + 122, y + 49, 0xFF373737);

        // Progress Arrow Fill
        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();
        if (progress > 0 && maxProgress > 0) {
            int arrowWidth = 18;
            int filledWidth = (int) ((float) progress / maxProgress * arrowWidth);

            // Grüner Fortschrittsbalken
            graphics.fill(x + 104, y + 37, x + 104 + filledWidth, y + 49, 0xFF00AA00);
        }

        // Player Hotbar
        for (int i = 0; i < 9; i++) {
            drawSlot(graphics, x + 8 + i * 18, y + 122);
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
        graphics.drawString(this.font, Component.translatable("gui.coffee.packaging").getString(), x + 8, y + 6, 0xFFFFFF, false);

        // Instructions
        graphics.drawString(this.font, Component.translatable("gui.coffee.select_size").getString(), x + 8, y + 22, 0xCCCCCC, false);

        // Slot Labels
        graphics.drawString(this.font, Component.translatable("gui.coffee.label_coffee").getString(), x + 48, y + 56, 0xCCCCCC, false);
        graphics.drawString(this.font, Component.translatable("gui.coffee.label_bag").getString(), x + 82, y + 56, 0xCCCCCC, false);
        graphics.drawString(this.font, Component.translatable("gui.coffee.label_package").getString(), x + 116, y + 56, 0xCCCCCC, false);

        // Progress Percentage
        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();
        if (progress > 0 && maxProgress > 0) {
            int percentage = (int) ((float) progress / maxProgress * 100);
            String progressText = Component.translatable("gui.progress_percent", percentage).getString();
            int textWidth = this.font.width(progressText);
            graphics.drawString(this.font, progressText, x + 113 - textWidth / 2, y + 82, 0x00FF00, false);
        }

        // Selected Package Size Indicator
        int selectedSize = menu.getSelectedPackageSize(); // 0=SMALL, 1=MEDIUM, 2=LARGE
        String sizeText = switch (selectedSize) {
            case 0 -> Component.translatable("gui.coffee.selected_250g").getString();
            case 1 -> Component.translatable("gui.coffee.selected_500g").getString();
            case 2 -> Component.translatable("gui.coffee.selected_1kg").getString();
            default -> Component.translatable("gui.coffee.selected_500g").getString();
        };
        graphics.drawString(this.font, sizeText, x + 8, y + 82, 0xFFFF00, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Empty - we render in render()
    }
}
