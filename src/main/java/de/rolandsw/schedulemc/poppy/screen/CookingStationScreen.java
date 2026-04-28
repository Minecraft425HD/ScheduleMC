package de.rolandsw.schedulemc.poppy.screen;

import de.rolandsw.schedulemc.poppy.menu.CookingStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CookingStationScreen extends AbstractContainerScreen<CookingStationMenu> {

    private static final int COLOR_BG         = 0xFF1A2B1A;
    private static final int COLOR_PANEL      = 0xFF2B3B2B;
    private static final int COLOR_HEADER     = 0xFF0E1A0E;
    private static final int COLOR_SEP        = 0xFF3A5A3A;
    private static final int COLOR_PROGRESS   = 0xFF44AA44;
    private static final int COLOR_HIGHLIGHT  = 0xFF66CC66;
    private static final int COLOR_RES_BG     = 0xFF111A11;
    private static final int COLOR_WATER_FILL = 0xFF2266CC;
    private static final int COLOR_WATER_TOP  = 0xFF44AAFF;
    private static final int COLOR_FUEL_FILL  = 0xFFCC6600;
    private static final int COLOR_FUEL_TOP   = 0xFFFF8800;

    public CookingStationScreen(CookingStationMenu menu, Inventory inv, Component title) {
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

        int barH = 28;
        int barY = y + 30;

        // Water bar (left)
        int wBarX = x + 5;
        int wBarW = 8;
        g.fill(wBarX, barY, wBarX + wBarW, barY + barH, COLOR_RES_BG);
        int waterLevel = menu.getWaterLevel();
        int waterMax   = menu.getWaterMax();
        if (waterMax > 0 && waterLevel > 0) {
            int filled = (int)((float)waterLevel / waterMax * barH);
            g.fill(wBarX, barY + barH - filled, wBarX + wBarW, barY + barH, COLOR_WATER_FILL);
            g.fill(wBarX, barY + barH - filled, wBarX + wBarW, barY + barH - filled + 1, COLOR_WATER_TOP);
        }

        // Fuel bar (right of water bar)
        int fBarX = x + 36;
        int fBarW = 8;
        g.fill(fBarX, barY, fBarX + fBarW, barY + barH, COLOR_RES_BG);
        int fuelLevel = menu.getFuelLevel();
        int fuelMax   = menu.getFuelMax();
        if (fuelMax > 0 && fuelLevel > 0) {
            int filled = (int)((float)fuelLevel / fuelMax * barH);
            g.fill(fBarX, barY + barH - filled, fBarX + fBarW, barY + barH, COLOR_FUEL_FILL);
            g.fill(fBarX, barY + barH - filled, fBarX + fBarW, barY + barH - filled + 1, COLOR_FUEL_TOP);
        }

        // Water action slot
        drawSlot(g, x + 16, y + 30);
        // Fuel action slot
        drawSlot(g, x + 47, y + 30);

        // Input slot (raw opium)
        drawSlot(g, x + 78, y + 30);

        // Progress arrow area
        g.fill(x + 97, y + 26, x + 117, y + 52, COLOR_RES_BG);
        float progress = menu.getProgressScaled();
        if (progress > 0 && menu.getInputCount() > 0) {
            int filled = (int)(progress * 20);
            g.fill(x + 97, y + 26, x + 97 + filled, y + 52, COLOR_PROGRESS);
            g.fill(x + 97, y + 26, x + 97 + filled, y + 29, COLOR_HIGHLIGHT);
        }

        // Output slot (morphine)
        drawSlot(g, x + 128, y + 30);

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

        g.drawString(font, Component.translatable("gui.cooking_station.title").getString(),
                x + 8, y + 6, 0xFF88FF88, true);

        g.drawString(font, Component.translatable("gui.cooking_station.water_label").getString(), x + 16, y + 19, 0xFF44AAFF, false);
        g.drawString(font, Component.translatable("gui.cooking_station.fuel_label").getString(), x + 47, y + 19, 0xFFFF8800, false);
        g.drawString(font, Component.translatable("gui.cooking_station.input_label").getString(),
                x + 78, y + 19, 0xAAAAAA, false);
        g.drawString(font, Component.translatable("gui.cooking_station.output_label").getString(),
                x + 128, y + 19, 0xAAAAAA, false);

        if (menu.getInputCount() > 0) {
            String pct = (int)(menu.getProgressScaled() * 100) + "%";
            g.drawCenteredString(font, pct, x + 107, y + 36, 0xFFFFFF);
        }

        g.drawString(font,
                Component.translatable("gui.cooking_station.water_label").getString()
                + ": " + menu.getWaterLevel() + "/" + menu.getWaterMax() + " mB",
                x + 8, y + 65, 0xFF44AAFF, false);
        g.drawString(font,
                Component.translatable("gui.cooking_station.fuel_label").getString()
                + ": " + menu.getFuelLevel() + "/" + menu.getFuelMax(),
                x + 8, y + 75, 0xFFFF8800, false);
        g.drawString(font,
                Component.translatable("gui.cooking_station.info_slots").getString()
                + menu.getInputCount() + "/" + menu.getCapacity()
                + " | "
                + Component.translatable("gui.cooking_station.info_output").getString()
                + menu.getOutputCount(),
                x + 8, y + 85, 0xFFFFFF, false);

        float prog = menu.getProgressScaled() * 100;
        g.drawString(font,
                Component.translatable("gui.cooking_station.info_progress").getString()
                + String.format("%.1f", prog) + "%",
                x + 8, y + 95, 0xFFFFFF, false);

        g.drawString(font, Component.translatable("gui.cooking_station.inventory_label").getString(),
                x + 8, y + 130, 0xAAAAAA, false);

        // Tooltip over water area
        if (mouseX >= x + 5 && mouseX < x + 36 && mouseY >= y + 30 && mouseY < y + 58) {
            List<FormattedCharSequence> tip = List.of(
                Component.literal("§eAdd water:").getVisualOrderText(),
                Component.literal("§7Water bucket (1000 mB each)").getVisualOrderText()
            );
            g.renderTooltip(font, tip, mouseX, mouseY);
        }

        // Tooltip over fuel area
        if (mouseX >= x + 36 && mouseX < x + 67 && mouseY >= y + 30 && mouseY < y + 58) {
            List<FormattedCharSequence> tip = List.of(
                Component.literal("§eAccepted fuels:").getVisualOrderText(),
                Component.literal("§7Coal, Charcoal").getVisualOrderText(),
                Component.literal("§7Coal Block (×400)").getVisualOrderText(),
                Component.literal("§7Logs, Planks").getVisualOrderText()
            );
            g.renderTooltip(font, tip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) { }

    @Override
    public boolean isPauseScreen() { return false; }
}
