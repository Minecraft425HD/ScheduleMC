package de.rolandsw.schedulemc.tobacco.screen;

import de.rolandsw.schedulemc.tobacco.menu.SmallFermentationBarrelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für das Kleine Fermentierungsfass.
 *
 * Layout (176 × 166):
 *  ┌─ Header (Titel) ───────────────────────────────┐
 *  │ [Einlegen]  [═══════ Fortschrittsbalken ══════] [Fertig] │
 *  ├────────────────────────────────────────────────┤
 *  │ Kapazitätsanzeige + Balken                     │
 *  │ Fertige Blätter-Anzahl                         │
 *  ├────────────────────────────────────────────────┤
 *  │ [Hotbar]                                       │
 *  └────────────────────────────────────────────────┘
 */
public class SmallFermentationBarrelScreen extends AbstractContainerScreen<SmallFermentationBarrelMenu> {

    // Grün-Palette für Small
    private static final int COLOR_PROGRESS   = 0xFF4CAF50;
    private static final int COLOR_HIGHLIGHT  = 0xFF81C784;
    private static final int COLOR_CAPACITY   = 0xFF2E7D32;
    private static final int COLOR_TEXT       = 0xFF4CAF50;

    public SmallFermentationBarrelScreen(SmallFermentationBarrelMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return keyCode == 69 || super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ─────────────────────────────────────────────────────────────
    // Hintergrund + Slots + Balken
    // ─────────────────────────────────────────────────────────────

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // Äußerer Rahmen / inneres Panel / dunkler Header
        g.fill(x,     y,     x + imageWidth,     y + imageHeight,     0xFF2B2B2B);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF4C4C4C);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + 18,              0xFF1E1E1E);

        // Maschinenslots
        drawSlot(g, x + 22,  y + 30);  // Slot 0 – Input
        drawSlot(g, x + 134, y + 30);  // Slot 1 – Output

        // Fortschrittsbalken (68 × 26 px)
        g.fill(x + 54, y + 26, x + 122, y + 52, 0xFF1A1A1A);
        int progress  = menu.getProgressPercent();
        int progressW = (int)(68 * (progress / 100f));
        if (progressW > 0) {
            g.fill(x + 54, y + 26, x + 54 + progressW, y + 52, COLOR_PROGRESS);
            // Glanzlinie oben
            g.fill(x + 54, y + 26, x + 54 + progressW, y + 29, COLOR_HIGHLIGHT);
        }

        // Trennlinie nach Maschinenbereich
        g.fill(x + 8, y + 62, x + 168, y + 63, 0xFF5A5A5A);

        // Kapazitätsbalken (160 × 8 px)
        g.fill(x + 8, y + 77, x + 168, y + 85, 0xFF1A1A1A);
        int cap   = menu.getCapacity();
        int count = menu.getInputCount();
        if (cap > 0 && count > 0) {
            int capW = (int)(160 * (count / (float) cap));
            g.fill(x + 8, y + 77, x + 8 + capW, y + 85, COLOR_CAPACITY);
        }

        // Trennlinie vor Hotbar
        g.fill(x + 8, y + 112, x + 168, y + 113, 0xFF5A5A5A);

        // Hotbar-Slots
        for (int i = 0; i < 9; i++) {
            drawSlot(g, x + 8 + i * 18, y + 127);
        }
    }

    private void drawSlot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
        g.fill(x,     y,     x + 16, y + 16,  0xFF373737);
    }

    // ─────────────────────────────────────────────────────────────
    // Texte
    // ─────────────────────────────────────────────────────────────

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;

        // Titel
        g.drawString(font,
            Component.translatable("block.schedulemc.small_fermentation_barrel").getString(),
            x + 8, y + 6, 0xFFFFFF, false);

        // Labels über den Slots
        g.drawString(font,
            Component.translatable("gui.fermentation.input_label").getString(),
            x + 16, y + 21, 0xAAAAAA, false);
        g.drawString(font,
            Component.translatable("gui.fermentation.output_label").getString(),
            x + 128, y + 21, 0xAAAAAA, false);

        // Fortschritts-Prozent zentriert im Balken
        int progress = menu.getProgressPercent();
        String pct = progress + "%";
        int textColor = progress > 0 ? COLOR_TEXT : 0x666666;
        g.drawCenteredString(font, pct, x + 88, y + 35, textColor);

        // Kapazitätstext ("Eingefüllt: X / Y")
        g.drawString(font,
            Component.translatable("block.fermentation.capacity",
                menu.getInputCount(), menu.getCapacity()).getString(),
            x + 8, y + 67, 0xCCCCCC, false);

        // Fertige Blätter
        String ready = Component.translatable("gui.fermentation.output_label").getString()
            + ": " + menu.getOutputCount();
        g.drawString(font, ready, x + 8, y + 90, 0xCCCCCC, false);

        // Fortschritts-Text (rechte Hälfte der Info-Zeile)
        g.drawString(font,
            Component.translatable("gui.progress_percent", progress).getString(),
            x + 90, y + 90, COLOR_TEXT, false);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
        // Alle Labels werden in render() gezeichnet
    }
}
