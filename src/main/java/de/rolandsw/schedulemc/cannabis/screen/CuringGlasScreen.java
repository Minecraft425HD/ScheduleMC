package de.rolandsw.schedulemc.cannabis.screen;

import de.rolandsw.schedulemc.cannabis.menu.CuringGlasMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class CuringGlasScreen extends AbstractContainerScreen<CuringGlasMenu> {

    private static final int GUI_WIDTH  = 220;
    private static final int GUI_HEIGHT = 160;

    private static final int BAR_X      = 20;
    private static final int BAR_Y      = 80;
    private static final int BAR_WIDTH  = 180;
    private static final int BAR_HEIGHT = 16;

    private static final int ROW1_Y = 35;
    private static final int ROW2_Y = 50;
    private static final int ROW3_Y = 65;
    private static final int ROW4_Y = 102;

    public CuringGlasScreen(CuringGlasMenu menu, Inventory playerInventory, Component title) {
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

        // Background
        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF0A1A0A);
        graphics.fill(x + 3, y + 3, x + GUI_WIDTH - 3, y + GUI_HEIGHT - 3, 0xFF1D2D1D);
        graphics.fill(x + 3, y + 3, x + GUI_WIDTH - 3, y + 25, 0xFF1D3D1D);

        if (menu.hasContent()) {
            renderInfoRows(graphics, x, y);
        }

        renderProgressBar(graphics, x + BAR_X, y + BAR_Y);
    }

    private void renderInfoRows(GuiGraphics graphics, int x, int y) {
        // Row 1: strain + weight
        String row1 = "§7Sorte: " + menu.getStrain().getColoredName()
                    + " §7| §fGewicht: " + menu.getWeight() + "g";
        graphics.drawString(this.font, row1, x + 10, y + ROW1_Y, 0xFFFFFF, false);

        // Row 2: base quality
        String row2 = "§7Basis-Qualität: " + menu.getBaseQuality().getColoredName();
        graphics.drawString(this.font, row2, x + 10, y + ROW2_Y, 0xFFFFFF, false);

        // Row 3: curing days
        String row3 = "§7Curing-Tage: §f" + menu.getCuringDays() + " §7/ 28";
        graphics.drawString(this.font, row3, x + 10, y + ROW3_Y, 0xFFFFFF, false);
    }

    private void renderProgressBar(GuiGraphics graphics, int x, int y) {
        // Border + background
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF555555);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF222222);

        if (!menu.hasContent()) return;

        int days = menu.getCuringDays();
        int midPoint = BAR_WIDTH / 2; // 14-day mark
        int filledPixels = Math.min(BAR_WIDTH, (int)((days / 28.0f) * BAR_WIDTH));

        if (filledPixels > 0) {
            int greenEnd = Math.min(filledPixels, midPoint);
            graphics.fill(x, y, x + greenEnd, y + BAR_HEIGHT, 0xFF2D6B2D);

            if (filledPixels > midPoint) {
                graphics.fill(x + midPoint, y, x + filledPixels, y + BAR_HEIGHT, 0xFF8B8B22);
            }
        }

        // 14-day divider line (yellow)
        graphics.fill(x + midPoint - 1, y - 2, x + midPoint + 1, y + BAR_HEIGHT + 2, 0xFFFFFF55);

        // Star at 28 days
        if (days >= 28) {
            graphics.drawString(this.font, "§6★", x + BAR_WIDTH - 8, y + BAR_HEIGHT / 2 - 4, 0xFFFFFF, true);
        }

        // Bar labels
        graphics.drawString(this.font, "§70",       x,                              y + BAR_HEIGHT + 3, 0xAAAAAA, false);
        graphics.drawString(this.font, "§714 Tage", x + midPoint - 15,              y + BAR_HEIGHT + 3, 0xFFFF55, false);
        graphics.drawString(this.font, "§628 Tage", x + BAR_WIDTH - 30,             y + BAR_HEIGHT + 3, 0xFFAA00, false);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;

        // Title
        graphics.drawString(this.font,
                Component.translatable("gui.curing_glas.title").getString(),
                x + 10, y + 8, 0xFF88FF88, true);

        if (!menu.hasContent()) {
            String empty = Component.translatable("gui.curing_glas.empty").getString();
            int w = this.font.width(empty);
            graphics.drawString(this.font, empty, x + GUI_WIDTH / 2 - w / 2, y + 70, 0xFF888888, false);
            return;
        }

        // Row 4: expected quality (below bar)
        String expectedText = "§7Erwartete Qualität: " + menu.getExpectedQuality().getColoredName();
        graphics.drawString(this.font, expectedText, x + 10, y + ROW4_Y, 0xFFFFFF, false);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        // Empty
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
