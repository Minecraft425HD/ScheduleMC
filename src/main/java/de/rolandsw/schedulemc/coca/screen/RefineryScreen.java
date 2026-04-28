package de.rolandsw.schedulemc.coca.screen;

import de.rolandsw.schedulemc.coca.menu.RefineryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class RefineryScreen extends AbstractContainerScreen<RefineryMenu> {

    private static final int COLOR_BG        = 0xFF1A2B1A;
    private static final int COLOR_PANEL     = 0xFF2B3B2B;
    private static final int COLOR_HEADER    = 0xFF0E1A0E;
    private static final int COLOR_SEP       = 0xFF3A5A3A;
    private static final int COLOR_PROGRESS  = 0xFF44AA44;
    private static final int COLOR_HIGHLIGHT = 0xFF66CC66;
    private static final int COLOR_FUEL_BG   = 0xFF111A11;
    private static final int COLOR_FUEL_FILL = 0xFFCC6600;
    private static final int COLOR_FUEL_TOP  = 0xFFFF8800;

    public RefineryScreen(RefineryMenu menu, Inventory inv, Component title) {
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
        g.fill(x,     y,     x + 16, y + 16,  0xFF373737);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // Panel background
        g.fill(x,     y,     x + 176, y + 166, COLOR_BG);
        g.fill(x + 2, y + 2, x + 174, y + 164, COLOR_PANEL);
        g.fill(x + 2, y + 2, x + 174, y + 18,  COLOR_HEADER);

        // Fuel bar background (x=8, y=22, 10px wide, 28px tall)
        int barX = x + 8;
        int barY = y + 22;
        int barW = 10;
        int barH = 28;
        g.fill(barX, barY, barX + barW, barY + barH, COLOR_FUEL_BG);

        // Fuel fill from bottom
        int fuelLevel = menu.getFuelLevel();
        int fuelMax   = menu.getFuelMax();
        if (fuelMax > 0 && fuelLevel > 0) {
            int filled = (int)((float)fuelLevel / fuelMax * barH);
            g.fill(barX, barY + barH - filled, barX + barW, barY + barH, COLOR_FUEL_FILL);
            g.fill(barX, barY + barH - filled, barX + barW, barY + barH - filled + 1, COLOR_FUEL_TOP);
        }

        // Fuel slot visual
        drawSlot(g, x + 22, y + 30);

        // Input slot (paste) — center
        drawSlot(g, x + 74, y + 30);

        // Progress arrow area — between input and output
        g.fill(x + 94, y + 26, x + 114, y + 52, COLOR_FUEL_BG);
        float progress = menu.getProgressScaled();
        if (progress > 0 && menu.getInputCount() > 0) {
            int filled = (int)(progress * 20);
            g.fill(x + 94, y + 26, x + 94 + filled, y + 52, COLOR_PROGRESS);
            g.fill(x + 94, y + 26, x + 94 + filled, y + 29, COLOR_HIGHLIGHT);
        }

        // Output slot (cocaine) — right
        drawSlot(g, x + 124, y + 30);

        // Separator after machine area
        g.fill(x + 8, y + 62, x + 168, y + 63, COLOR_SEP);

        // Separator before hotbar
        g.fill(x + 8, y + 134, x + 168, y + 135, COLOR_SEP);

        // Hotbar slots
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

        // Title
        g.drawString(font, Component.translatable("gui.refinery.title").getString(),
                x + 8, y + 6, 0xFF88FF88, true);

        // Slot area labels — left-aligned with each slot
        g.drawString(font, Component.translatable("gui.refinery.fuel_label").getString(),
                x + 22, y + 19, 0xFFAA44, false);
        g.drawString(font, Component.translatable("gui.refinery.input_label").getString(),
                x + 74, y + 19, 0xAAAAAA, false);
        g.drawString(font, Component.translatable("gui.refinery.output_label").getString(),
                x + 124, y + 19, 0xAAAAAA, false);

        // Progress percentage inside arrow area
        if (menu.getInputCount() > 0) {
            String pct = (int)(menu.getProgressScaled() * 100) + "%";
            g.drawCenteredString(font, pct, x + 104, y + 36, 0xFFFFFF);
        }

        // Info section (below separator at y+62)
        g.drawString(font,
                Component.translatable("gui.refinery.fuel_label").getString()
                + ": " + menu.getFuelLevel() + "/" + menu.getFuelMax(),
                x + 8, y + 65, 0xFFAA44, false);
        g.drawString(font,
                Component.translatable("gui.refinery.info_slots").getString()
                + menu.getInputCount() + "/" + menu.getCapacity()
                + " | "
                + Component.translatable("gui.refinery.info_output").getString()
                + menu.getOutputCount(),
                x + 8, y + 75, 0xFFFFFF, false);

        float prog = menu.getProgressScaled() * 100;
        g.drawString(font,
                Component.translatable("gui.refinery.info_progress").getString()
                + String.format("%.1f", prog) + "%",
                x + 8, y + 85, 0xFFFFFF, false);

        // Inventar label
        g.drawString(font, Component.translatable("gui.refinery.inventory_label").getString(),
                x + 8, y + 130, 0xAAAAAA, false);

        // Tooltip: accepted fuels when hovering fuel bar/slot area
        if (mouseX >= x + 8 && mouseX < x + 44 && mouseY >= y + 18 && mouseY < y + 55) {
            List<FormattedCharSequence> tip = List.of(
                Component.literal("§eAkzeptierte Brennstoffe:").getVisualOrderText(),
                Component.literal("§7Kohle, Holzkohle").getVisualOrderText(),
                Component.literal("§7Kohleblock (×8)").getVisualOrderText(),
                Component.literal("§7Holzstämme, Holzbretter").getVisualOrderText()
            );
            g.renderTooltip(font, tip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) { }

    @Override
    public boolean isPauseScreen() { return false; }
}
