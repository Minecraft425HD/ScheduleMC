package de.rolandsw.schedulemc.poppy.screen;

import de.rolandsw.schedulemc.poppy.menu.OpiumPressMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OpiumPressScreen extends AbstractContainerScreen<OpiumPressMenu> {

    private static final int COLOR_BG         = 0xFF1A2B1A;
    private static final int COLOR_PANEL      = 0xFF2B3B2B;
    private static final int COLOR_HEADER     = 0xFF0E1A0E;
    private static final int COLOR_SEP        = 0xFF3A5A3A;
    private static final int COLOR_PROGRESS   = 0xFF44AA44;
    private static final int COLOR_HIGHLIGHT  = 0xFF66CC66;
    private static final int COLOR_DIESEL_BG  = 0xFF111A11;
    private static final int COLOR_DIESEL_FILL = 0xFF3388CC;
    private static final int COLOR_DIESEL_TOP  = 0xFF55AAFF;

    public OpiumPressScreen(OpiumPressMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return keyCode == 69 || super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawSlot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
        g.fill(x,     y,     x + 16, y + 16, 0xFF373737);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        g.fill(x,     y,     x + 176, y + 166, COLOR_BG);
        g.fill(x + 2, y + 2, x + 174, y + 164, COLOR_PANEL);
        g.fill(x + 2, y + 2, x + 174, y + 18,  COLOR_HEADER);

        int barX = x + 8;
        int barY = y + 30;
        int barW = 10;
        int barH = 28;
        g.fill(barX, barY, barX + barW, barY + barH, COLOR_DIESEL_BG);

        int dieselLevel = menu.getDieselLevel();
        int dieselMax   = menu.getDieselMax();
        if (dieselMax > 0 && dieselLevel > 0) {
            int filled = (int)((float)dieselLevel / dieselMax * barH);
            g.fill(barX, barY + barH - filled, barX + barW, barY + barH, COLOR_DIESEL_FILL);
            g.fill(barX, barY + barH - filled, barX + barW, barY + barH - filled + 1, COLOR_DIESEL_TOP);
        }

        drawSlot(g, x + 22, y + 30);
        drawSlot(g, x + 74, y + 30);

        g.fill(x + 94, y + 26, x + 114, y + 52, COLOR_DIESEL_BG);
        float progress = menu.getProgressScaled();
        if (progress > 0 && menu.getInputCount() > 0) {
            int filled = (int)(progress * 20);
            g.fill(x + 94, y + 26, x + 94 + filled, y + 52, COLOR_PROGRESS);
            g.fill(x + 94, y + 26, x + 94 + filled, y + 29, COLOR_HIGHLIGHT);
        }

        drawSlot(g, x + 124, y + 30);

        g.fill(x + 8, y + 62, x + 168, y + 63, COLOR_SEP);
        g.fill(x + 8, y + 134, x + 168, y + 135, COLOR_SEP);

        for (int i = 0; i < 9; i++) {
            drawSlot(g, x + 8 + i * 18, y + 142);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;

        g.drawString(font, Component.translatable("gui.opium_press.title").getString(),
                x + 8, y + 6, 0xFF88FF88, true);

        g.drawString(font, Component.translatable("gui.opium_press.diesel_label").getString(), x + 22, y + 19, 0xFF55AAFF, false);
        g.drawString(font, Component.translatable("gui.opium_press.input_label").getString(),
                x + 74, y + 19, 0xAAAAAA, false);
        g.drawString(font, Component.translatable("gui.opium_press.output_label").getString(),
                x + 124, y + 19, 0xAAAAAA, false);

        if (menu.getInputCount() > 0) {
            String pct = (int)(menu.getProgressScaled() * 100) + "%";
            g.drawCenteredString(font, pct, x + 104, y + 36, 0xFFFFFF);
        }

        g.drawString(font,
                Component.translatable("gui.opium_press.diesel_label").getString()
                + ": " + menu.getDieselLevel() + "/" + menu.getDieselMax() + " mB",
                x + 8, y + 65, 0xFF55AAFF, false);
        g.drawString(font,
                Component.translatable("gui.opium_press.info_slots").getString()
                + menu.getInputCount() + "/" + menu.getCapacity()
                + " | "
                + Component.translatable("gui.opium_press.info_output").getString()
                + menu.getOutputCount(),
                x + 8, y + 75, 0xFFFFFF, false);

        float prog = menu.getProgressScaled() * 100;
        g.drawString(font,
                Component.translatable("gui.opium_press.info_progress").getString()
                + String.format("%.1f", prog) + "%",
                x + 8, y + 85, 0xFFFFFF, false);

        g.drawString(font, Component.translatable("gui.opium_press.inventory_label").getString(),
                x + 8, y + 130, 0xAAAAAA, false);

        if (mouseX >= x + 8 && mouseX < x + 44 && mouseY >= y + 30 && mouseY < y + 58) {
            List<FormattedCharSequence> tip = List.of(
                Component.literal("§eFuel: Diesel Canister").getVisualOrderText(),
                Component.literal("§7Creative tab: §bVehicles").getVisualOrderText()
            );
            g.renderTooltip(font, tip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) { }

    @Override
    public boolean isPauseScreen() { return false; }
}
