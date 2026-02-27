package de.rolandsw.schedulemc.cannabis.screen;

import de.rolandsw.schedulemc.cannabis.menu.HashPresseMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class HashPresseScreen extends AbstractContainerScreen<HashPresseMenu> {

    private static final int GUI_WIDTH  = 220;
    private static final int GUI_HEIGHT = 170;

    private static final int BAR_X      = 20;
    private static final int BAR_Y      = 70;
    private static final int BAR_WIDTH  = 180;
    private static final int BAR_HEIGHT = 14;

    private static final int BUTTON_X      = 70;
    private static final int BUTTON_Y      = 100;
    private static final int BUTTON_WIDTH  = 80;
    private static final int BUTTON_HEIGHT = 20;

    private static final int ROW1_Y   = 35;
    private static final int ROW2_Y   = 50;
    private static final int STATUS_Y = 128;

    public HashPresseScreen(HashPresseMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth  = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width  - GUI_WIDTH)  / 2;
        this.topPos  = (this.height - GUI_HEIGHT) / 2;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF0A1A0A);
        graphics.fill(x + 3, y + 3, x + GUI_WIDTH - 3, y + GUI_HEIGHT - 3, 0xFF1D2D1D);
        graphics.fill(x + 3, y + 3, x + GUI_WIDTH - 3, y + 25, 0xFF1D3D1D);

        renderInfoRows(graphics, x, y);

        if (menu.isPressing()) {
            renderProgressBar(graphics, x + BAR_X, y + BAR_Y);
        }

        renderActionButton(graphics, x + BUTTON_X, y + BUTTON_Y, mouseX, mouseY);
    }

    private void renderInfoRows(GuiGraphics graphics, int x, int y) {
        String row1 = "§7Trim: §f" + menu.getTrimWeight() + "g §7| Sorte: " + menu.getStrain().getColoredName();
        graphics.drawString(this.font, row1, x + 10, y + ROW1_Y, 0xFFFFFF, false);

        String row2 = "§7Hash: §f" + menu.getExpectedHashWeight() + "g §7| Qualität: " + menu.getBaseQuality().getColoredName();
        graphics.drawString(this.font, row2, x + 10, y + ROW2_Y, 0xFFFFFF, false);
    }

    private void renderProgressBar(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF555555);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF222222);

        int filled = (int)(menu.getPressProgressF() * BAR_WIDTH);
        if (filled > 0) {
            graphics.fill(x, y, x + filled, y + BAR_HEIGHT, 0xFF33AA33);
        }

        String pct = (int)(menu.getPressProgressF() * 100) + "%";
        int tw = this.font.width(pct);
        graphics.drawString(this.font, pct, x + BAR_WIDTH / 2 - tw / 2, y + 2, 0xFFFFFF, true);
    }

    private void renderActionButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + BUTTON_WIDTH &&
                          mouseY >= y && mouseY < y + BUTTON_HEIGHT;

        String text;
        int color;

        if (menu.hasOutput()) {
            text  = Component.translatable("gui.hash_presse.hash_ready").getString();
            color = 0xFF335533;
        } else if (menu.isPressing()) {
            text  = Component.translatable("gui.hash_presse.pressing").getString();
            color = 0xFF333355;
        } else if (menu.canStart()) {
            color = hovered ? 0xFF55DD55 : 0xFF44AA44;
            text  = Component.translatable("gui.hash_presse.button_start").getString();
        } else {
            text  = Component.translatable("gui.hash_presse.button_start").getString();
            color = 0xFF2A3A2A;
        }

        // Shadow
        graphics.fill(x + 2, y + 2, x + BUTTON_WIDTH + 2, y + BUTTON_HEIGHT + 2, 0x66000000);
        // Border + fill
        graphics.fill(x - 1, y - 1, x + BUTTON_WIDTH + 1, y + BUTTON_HEIGHT + 1, 0xFF224422);
        graphics.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, color);
        // Text
        int tw = this.font.width(text);
        graphics.drawString(this.font, text, x + BUTTON_WIDTH / 2 - tw / 2, y + 6, 0xFFFFFF, true);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;

        graphics.drawString(this.font,
                Component.translatable("gui.hash_presse.title").getString(),
                x + 10, y + 8, 0xFF88FF88, true);

        if (menu.hasOutput()) {
            String ready = Component.translatable("gui.hash_presse.hash_ready").getString();
            int tw = this.font.width(ready);
            graphics.drawString(this.font, ready, x + GUI_WIDTH / 2 - tw / 2, y + STATUS_Y, 0xFF55FF55, true);
            this.onClose();
            return;
        }

        if (!menu.isPressing() && !menu.hasOutput() && menu.getTrimWeight() < de.rolandsw.schedulemc.cannabis.blockentity.HashPresseBlockEntity.MIN_TRIM_WEIGHT) {
            String warn = "§7(Mindestens " + de.rolandsw.schedulemc.cannabis.blockentity.HashPresseBlockEntity.MIN_TRIM_WEIGHT + "g Trim benötigt)";
            graphics.drawString(this.font, warn, x + 10, y + STATUS_Y, 0xAAAAAA, false);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        // Empty
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int bx = this.leftPos + BUTTON_X;
            int by = this.topPos  + BUTTON_Y;
            if (mouseX >= bx && mouseX < bx + BUTTON_WIDTH &&
                mouseY >= by && mouseY < by + BUTTON_HEIGHT) {
                if (menu.canStart()) {
                    java.util.Objects.requireNonNull(this.minecraft).gameMode
                            .handleInventoryButtonClick(this.menu.containerId, HashPresseMenu.BUTTON_START);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
