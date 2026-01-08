package de.rolandsw.schedulemc.mdma.screen;
import de.rolandsw.schedulemc.util.UIColors;

import de.rolandsw.schedulemc.mdma.MDMAQuality;
import de.rolandsw.schedulemc.mdma.menu.PillenPresseMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für die Pillen-Presse
 * Mit Timing-Minigame!
 *
 * Spieler muss im richtigen Moment klicken:
 * - Grüne Zone = Perfekt (0.9 - 1.0)
 * - Gelbe Zone = Gut (0.6 - 0.9)
 * - Rote Zone = Schlecht (0.1 - 0.5)
 */
public class PillenPresseScreen extends AbstractContainerScreen<PillenPresseMenu> {

    // Color Constants
    private static final int COLOR_BACKGROUND_DARK_MAGENTA = 0xFF1A0A1A;
    private static final int COLOR_BACKGROUND_MEDIUM_MAGENTA = 0xFF2D1D2D;
    private static final int COLOR_HEADER_MAGENTA = 0xFF3D1D3D;
    private static final int COLOR_FRAME_GRAY = UIColors.GRAY_DARK;
    private static final int COLOR_BAR_BACKGROUND_DARK = 0xFF222222;
    private static final int COLOR_ZONE_BAD_RED = 0xFFAA3333;
    private static final int COLOR_ZONE_GOOD_YELLOW = 0xFFAAAA33;
    private static final int COLOR_ZONE_PERFECT_GREEN = 0xFF33AA33;
    private static final int COLOR_INDICATOR_WHITE = UIColors.WHITE;
    private static final int COLOR_BUTTON_DISABLED = 0xFF333355;
    private static final int COLOR_BUTTON_ACTIVE_MAGENTA = 0xFFDD55DD;
    private static final int COLOR_BUTTON_HOVERED_MAGENTA = 0xFFAA44AA;
    private static final int COLOR_BUTTON_PROCESSING = 0xFF444455;
    private static final int COLOR_BUTTON_BORDER = 0xFF222244;
    private static final int COLOR_QUALITY_EXCELLENT_ORANGE = UIColors.ACCENT_ORANGE;
    private static final int COLOR_QUALITY_PERFECT_GREEN = UIColors.ACCENT_LIME;
    private static final int COLOR_QUALITY_GOOD_YELLOW = 0xFFFFFF55;
    private static final int COLOR_QUALITY_BAD_RED = 0xFFFF5555;
    private static final int COLOR_TITLE_LIGHT_MAGENTA = 0xFFDD88FF;

    private static final int GUI_WIDTH = 250;
    private static final int GUI_HEIGHT = 180;

    // Timing-Bar
    private static final int BAR_X = 25;
    private static final int BAR_Y = 70;
    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 30;

    // Press Button
    private static final int BUTTON_X = 85;
    private static final int BUTTON_Y = 120;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 30;

    // Timing-Zonen (basierend auf 60 Ticks)
    private static final float PERFECT_START = 25f / 60f;  // ~0.42
    private static final float PERFECT_END = 35f / 60f;    // ~0.58
    private static final float GOOD_START = 20f / 60f;     // ~0.33
    private static final float GOOD_END = 40f / 60f;       // ~0.67

    private boolean hasPressed = false;
    private double lastScore = 0;
    private int resultShowTicks = 0;

    public PillenPresseScreen(PillenPresseMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Haupthintergrund - Dunkles Magenta Theme
        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, COLOR_BACKGROUND_DARK_MAGENTA);
        graphics.fill(x + 3, y + 3, x + GUI_WIDTH - 3, y + GUI_HEIGHT - 3, COLOR_BACKGROUND_MEDIUM_MAGENTA);

        // Header
        graphics.fill(x + 3, y + 3, x + GUI_WIDTH - 3, y + 25, COLOR_HEADER_MAGENTA);

        // Design & Farbe Info
        renderPillInfo(graphics, x, y);

        // Timing-Bar
        renderTimingBar(graphics, x + BAR_X, y + BAR_Y, partialTick);

        // Press-Button
        renderPressButton(graphics, x + BUTTON_X, y + BUTTON_Y, mouseX, mouseY);

        // Ergebnis anzeigen
        if (resultShowTicks > 0) {
            renderResult(graphics, x, y);
        }
    }

    private void renderPillInfo(GuiGraphics graphics, int x, int y) {
        // Info-Box
        int infoY = y + 35;
        graphics.fill(x + 20, infoY, x + GUI_WIDTH - 20, infoY + 28, COLOR_BACKGROUND_DARK_MAGENTA);

        // Design und Farbe
        String designText = "§7Design: " + menu.getSelectedDesign().getColoredName() +
                           " " + menu.getSelectedDesign().getSymbol();
        String colorText = "§7Farbe: " + menu.getSelectedColor().getColoredName();

        graphics.drawString(this.font, designText, x + 30, infoY + 5, 0xFFFFFF, false);
        graphics.drawString(this.font, colorText, x + 30, infoY + 16, 0xFFFFFF, false);

        // Material-Anzeige rechts
        String kristallText = "§f" + menu.getKristallCount() + "x §bKristalle";
        String bindeText = "§f" + menu.getBindemittelCount() + "x §aBinde";

        graphics.drawString(this.font, kristallText, x + GUI_WIDTH - 90, infoY + 5, 0xFFFFFF, false);
        graphics.drawString(this.font, bindeText, x + GUI_WIDTH - 90, infoY + 16, 0xFFFFFF, false);
    }

    private void renderTimingBar(GuiGraphics graphics, int x, int y, float partialTick) {
        // Rahmen
        graphics.fill(x - 2, y - 2, x + BAR_WIDTH + 2, y + BAR_HEIGHT + 2, COLOR_FRAME_GRAY);

        // Hintergrund
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_BAR_BACKGROUND_DARK);

        // Zonen zeichnen (von links nach rechts)
        int goodStartX = x + (int)(GOOD_START * BAR_WIDTH);
        int goodEndX = x + (int)(GOOD_END * BAR_WIDTH);
        int perfectStartX = x + (int)(PERFECT_START * BAR_WIDTH);
        int perfectEndX = x + (int)(PERFECT_END * BAR_WIDTH);

        // Rote Zonen (Ränder)
        graphics.fill(x, y + 2, goodStartX, y + BAR_HEIGHT - 2, COLOR_ZONE_BAD_RED);
        graphics.fill(goodEndX, y + 2, x + BAR_WIDTH, y + BAR_HEIGHT - 2, COLOR_ZONE_BAD_RED);

        // Gelbe Zonen
        graphics.fill(goodStartX, y + 2, perfectStartX, y + BAR_HEIGHT - 2, COLOR_ZONE_GOOD_YELLOW);
        graphics.fill(perfectEndX, y + 2, goodEndX, y + BAR_HEIGHT - 2, COLOR_ZONE_GOOD_YELLOW);

        // Grüne Zone (Perfekt)
        graphics.fill(perfectStartX, y + 2, perfectEndX, y + BAR_HEIGHT - 2, COLOR_ZONE_PERFECT_GREEN);

        // Pulsierender Indikator wenn aktiv
        if (menu.isWaitingForPress() && !hasPressed) {
            float progress = menu.getProgress();
            int indicatorX = x + (int)(progress * BAR_WIDTH);

            // Indikator-Linie (weißer Balken)
            int indicatorWidth = 6;
            int pulseOffset = (int)(Math.sin(System.currentTimeMillis() / 100.0) * 2);

            graphics.fill(indicatorX - indicatorWidth/2 - 1, y - 3 + pulseOffset,
                         indicatorX + indicatorWidth/2 + 1, y + BAR_HEIGHT + 3 + pulseOffset, COLOR_INDICATOR_WHITE);
            graphics.fill(indicatorX - indicatorWidth/2, y - 2 + pulseOffset,
                         indicatorX + indicatorWidth/2, y + BAR_HEIGHT + 2 + pulseOffset, 0xFFDDDDDD);

            // Pfeil oben
            graphics.fill(indicatorX - 4, y - 8, indicatorX + 4, y - 3, COLOR_INDICATOR_WHITE);
        }

        // Labels
        graphics.drawString(this.font, "§cZu früh", x + 5, y + BAR_HEIGHT + 5, 0xAAAAAA, false);
        graphics.drawString(this.font, "§aPERFEKT", x + BAR_WIDTH/2 - 20, y + BAR_HEIGHT + 5, 0xAAAAAA, false);
        graphics.drawString(this.font, "§cZu spät", x + BAR_WIDTH - 45, y + BAR_HEIGHT + 5, 0xAAAAAA, false);
    }

    private void renderPressButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + BUTTON_WIDTH &&
                          mouseY >= y && mouseY < y + BUTTON_HEIGHT;
        boolean canPress = menu.isWaitingForPress() && !hasPressed;

        int buttonColor;
        String buttonText;

        if (hasPressed) {
            buttonColor = COLOR_BUTTON_DISABLED;
            buttonText = "§7GEPRESST!";
        } else if (canPress && hovered) {
            buttonColor = COLOR_BUTTON_ACTIVE_MAGENTA;
            buttonText = "§f⚡ PRESS! ⚡";
        } else if (canPress) {
            buttonColor = COLOR_BUTTON_HOVERED_MAGENTA;
            buttonText = "§f⚡ PRESS! ⚡";
        } else {
            buttonColor = COLOR_BUTTON_PROCESSING;
            buttonText = "§8Warte...";
        }

        // Button mit Animation wenn aktiv
        int yOffset = 0;
        if (canPress && !hasPressed) {
            yOffset = (int)(Math.sin(System.currentTimeMillis() / 150.0) * 2);
        }

        // Schatten
        graphics.fill(x + 2, y + 2 + yOffset, x + BUTTON_WIDTH + 2, y + BUTTON_HEIGHT + 2 + yOffset, 0x66000000);

        // Button
        graphics.fill(x - 1, y - 1 + yOffset, x + BUTTON_WIDTH + 1, y + BUTTON_HEIGHT + 1 + yOffset, COLOR_BUTTON_BORDER);
        graphics.fill(x, y + yOffset, x + BUTTON_WIDTH, y + BUTTON_HEIGHT + yOffset, buttonColor);

        // Text
        int textWidth = this.font.width(buttonText);
        graphics.drawString(this.font, buttonText, x + BUTTON_WIDTH / 2 - textWidth / 2, y + 10 + yOffset, 0xFFFFFF, true);
    }

    private void renderResult(GuiGraphics graphics, int x, int y) {
        // Ergebnis-Overlay
        int overlayY = y + 155;

        MDMAQuality quality = MDMAQuality.fromTimingScore(lastScore);
        String qualityText;
        int qualityColor;

        switch (quality) {
            case PREMIUM -> {
                qualityText = "§6★ PERFEKT! ★ §fPremium-Qualität!";
                qualityColor = COLOR_QUALITY_EXCELLENT_ORANGE;
            }
            case GUT -> {
                qualityText = "§aGUT! §fGute Qualität";
                qualityColor = COLOR_QUALITY_PERFECT_GREEN;
            }
            case STANDARD -> {
                qualityText = "§eOKAY §7Standard-Qualität";
                qualityColor = COLOR_QUALITY_GOOD_YELLOW;
            }
            default -> {
                qualityText = "§cSCHLECHT §7Minderwertig...";
                qualityColor = COLOR_QUALITY_BAD_RED;
            }
        }

        // Hintergrund für Ergebnis
        graphics.fill(x + 20, overlayY - 2, x + GUI_WIDTH - 20, overlayY + 12, qualityColor & 0x33FFFFFF);

        int textWidth = this.font.width(qualityText);
        graphics.drawString(this.font, qualityText, x + GUI_WIDTH / 2 - textWidth / 2, overlayY, 0xFFFFFF, true);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;

        // Titel
        graphics.drawString(this.font, "§d💊 §lPILLEN-PRESSE §d💊", x + 10, y + 8, COLOR_TITLE_LIGHT_MAGENTA, true);

        // Anleitung
        if (menu.isWaitingForPress() && !hasPressed) {
            String hint = "§e▶ Drücke im §a§lGRÜNEN§e Bereich! ◀";
            int hintWidth = this.font.width(hint);
            graphics.drawString(this.font, hint, x + GUI_WIDTH / 2 - hintWidth / 2, y + 108, 0xFFFFFF, true);
        }

        // Ergebnis-Timer
        if (resultShowTicks > 0) {
            resultShowTicks--;
            if (resultShowTicks <= 0 && !menu.isWaitingForPress()) {
                // Schließe GUI nach Ergebnis-Anzeige
                this.onClose();
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Leer - alles in render()
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = this.leftPos;
            int y = this.topPos;

            // Button Click
            int buttonX = x + BUTTON_X;
            int buttonY = y + BUTTON_Y;
            if (mouseX >= buttonX && mouseX < buttonX + BUTTON_WIDTH &&
                mouseY >= buttonY && mouseY < buttonY + BUTTON_HEIGHT) {
                if (menu.isWaitingForPress() && !hasPressed) {
                    lastScore = menu.pressButton();
                    hasPressed = true;
                    resultShowTicks = 60; // 3 Sekunden Ergebnis anzeigen
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Leertaste auch als Press
        if (keyCode == 32 && menu.isWaitingForPress() && !hasPressed) {
            lastScore = menu.pressButton();
            hasPressed = true;
            resultShowTicks = 60;
            return true;
        }
        
        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true; // Consume event, prevent closing
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
