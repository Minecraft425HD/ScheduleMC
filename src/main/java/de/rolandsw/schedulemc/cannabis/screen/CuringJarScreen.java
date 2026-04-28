package de.rolandsw.schedulemc.cannabis.screen;

import de.rolandsw.schedulemc.cannabis.menu.CuringJarMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CuringJarScreen extends AbstractContainerScreen<CuringJarMenu> {

    private static final int COLOR_PROGRESS  = 0xFF2D6B2D;
    private static final int COLOR_PROGRESS2 = 0xFF8B8B22;
    private static final int COLOR_PROGRESS3 = 0xFFAA7700;
    private static final int COLOR_HIGHLIGHT = 0xFF55AA55;
    private static final int COLOR_SEP       = 0xFF3A5A3A;

    public CuringJarScreen(CuringJarMenu menu, Inventory inv, Component title) {
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

        // Panel
        g.fill(x,     y,     x + 176, y + 166, 0xFF1A2B1A);
        g.fill(x + 2, y + 2, x + 174, y + 164, 0xFF2B3B2B);
        g.fill(x + 2, y + 2, x + 174, y + 18,  0xFF0E1A0E);

        // Machine slots
        drawSlot(g, x + 22,  y + 30);  // IN
        drawSlot(g, x + 134, y + 30);  // OUT

        // Arrow / curing progress area
        g.fill(x + 54, y + 26, x + 122, y + 52, 0xFF111A11);
        float progress = menu.getCuringProgress();
        if (menu.hasContent() && progress > 0) {
            int filled = (int)(progress * 68);
            int zone1 = 68 / 3, zone2 = 68 * 2 / 3;
            g.fill(x + 54, y + 26, x + 54 + Math.min(filled, zone1), y + 52, COLOR_PROGRESS);
            if (filled > zone1)
                g.fill(x + 54 + zone1, y + 26, x + 54 + Math.min(filled, zone2), y + 52, COLOR_PROGRESS2);
            if (filled > zone2)
                g.fill(x + 54 + zone2, y + 26, x + 54 + filled, y + 52, COLOR_PROGRESS3);
            g.fill(x + 54, y + 26, x + 54 + Math.min(filled, 68), y + 29, COLOR_HIGHLIGHT);
        } else if (menu.hasOutput()) {
            g.fill(x + 54, y + 26, x + 122, y + 52, COLOR_PROGRESS3);
            g.fill(x + 54, y + 26, x + 122, y + 29, COLOR_HIGHLIGHT);
        }

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
        g.drawString(font, Component.translatable("gui.curing_glas.title").getString(),
                x + 8, y + 6, 0xFF88FF88, true);

        // Slot labels
        g.drawString(font, Component.translatable("gui.fermentation.input_label").getString(),
                x + 16, y + 19, 0xAAAAAA, false);
        g.drawString(font, Component.translatable("gui.fermentation.output_label").getString(),
                x + 128, y + 19, 0xAAAAAA, false);

        if (!menu.hasContent() && !menu.hasOutput()) {
            String empty = Component.translatable("gui.curing_glas.empty").getString();
            g.drawCenteredString(font, empty, x + 88, y + 35, 0xFF888888);
            return;
        }

        // Progress label / fertig-Indikator in der Pfeilfläche
        if (menu.hasContent()) {
            int pct = (int)(menu.getCuringProgress() * 100);
            g.drawCenteredString(font, pct + "%", x + 88, y + 36, 0xFFFFFF);
        } else if (menu.hasOutput()) {
            g.drawCenteredString(font, "§6★ 100%", x + 88, y + 36, 0xFFFFFF);
        }

        // Info rows
        g.drawString(font,
                Component.translatable("gui.curing_glas.info_strain").getString()
                + menu.getStrain().getColoredName() + " §7| " + menu.getWeight() + "g",
                x + 8, y + 66, 0xFFFFFF, false);
        g.drawString(font,
                Component.translatable("gui.curing_glas.info_base_quality").getString()
                + menu.getBaseQuality().getColoredName(),
                x + 8, y + 76, 0xFFFFFF, false);
        g.drawString(font,
                Component.translatable("gui.curing_glas.info_expected_quality").getString()
                + menu.getExpectedQuality().getColoredName()
                + " §7(+1)",
                x + 8, y + 86, 0xFFFFFF, false);
        g.drawString(font,
                "§7+20% " + Component.translatable("gui.curing_glas.price_bonus").getString(),
                x + 8, y + 96, 0xFFFFFF, false);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) { }

    @Override
    public boolean isPauseScreen() { return false; }
}
