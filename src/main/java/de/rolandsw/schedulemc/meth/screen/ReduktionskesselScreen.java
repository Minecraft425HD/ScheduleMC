package de.rolandsw.schedulemc.meth.screen;

import de.rolandsw.schedulemc.meth.MethQuality;
import de.rolandsw.schedulemc.meth.blockentity.ReduktionskesselBlockEntity;
import de.rolandsw.schedulemc.meth.menu.ReduktionskesselMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Arcade-Style GUI für den Reduktionskessel
 * Der Spieler muss den Button gedrückt halten um die Temperatur zu erhöhen
 * Ziel: Temperatur im optimalen Bereich (80-120°C) halten
 */
public class ReduktionskesselScreen extends AbstractContainerScreen<ReduktionskesselMenu> {

    // Color Constants
    private static final int COLOR_BACKGROUND_VERY_DARK = 0xFF1A1A1A;
    private static final int COLOR_BACKGROUND_DARK = 0xFF2D2D2D;
    private static final int COLOR_HEADER_BLACK = 0xFF0D0D0D;
    private static final int COLOR_THERMOMETER_FRAME = 0xFF555555;
    private static final int COLOR_THERMOMETER_BACKGROUND = 0xFF1E1E1E;
    private static final int COLOR_TEMP_COLD_BLUE = 0xFF2255AA;
    private static final int COLOR_TEMP_OPTIMAL_GREEN = 0xFF22AA55;
    private static final int COLOR_TEMP_DANGER_ORANGE = 0xFFDD8800;
    private static final int COLOR_TEMP_HOT_RED = 0xFFCC2222;
    private static final int COLOR_TEMP_MARKER_WHITE = 0xFFFFFFFF;
    private static final int COLOR_BUTTON_HEAT_RED = 0xFFCC3333;
    private static final int COLOR_BUTTON_DISABLED = 0xFF3D3D3D;
    private static final int COLOR_BUTTON_SHADOW = 0xFF111111;

    // GUI Dimensionen
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 180;

    // Thermometer Position
    private static final int THERMO_X = 30;
    private static final int THERMO_Y = 30;
    private static final int THERMO_WIDTH = 40;
    private static final int THERMO_HEIGHT = 120;

    // Button Position
    private static final int BUTTON_X = 100;
    private static final int BUTTON_Y = 100;
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 50;

    // Temperatur-Zonen (relativ zum Thermometer)
    private static final float ZONE_COLD_END = 0.43f;     // 80°C / 140°C range
    private static final float ZONE_OPTIMAL_END = 0.71f;  // 120°C
    private static final float ZONE_DANGER_END = 0.93f;   // 150°C

    private boolean isButtonPressed = false;

    public ReduktionskesselScreen(ReduktionskesselMenu menu, Inventory playerInventory, Component title) {
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

        // Haupthintergrund (dunkel)
        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, COLOR_BACKGROUND_VERY_DARK);

        // Innerer Rahmen
        graphics.fill(x + 4, y + 4, x + GUI_WIDTH - 4, y + GUI_HEIGHT - 4, COLOR_BACKGROUND_DARK);

        // Header
        graphics.fill(x + 4, y + 4, x + GUI_WIDTH - 4, y + 24, COLOR_HEADER_BLACK);

        // Thermometer rendern
        renderThermometer(graphics, x + THERMO_X, y + THERMO_Y);

        // Heat-Button rendern
        renderHeatButton(graphics, x + BUTTON_X, y + BUTTON_Y, mouseX, mouseY);

        // Progress-Bar rendern
        renderProgressBar(graphics, x + 100, y + 50);

        // Qualitätsanzeige rendern
        renderQualityIndicator(graphics, x + 100, y + 75);
    }

    private void renderThermometer(GuiGraphics graphics, int x, int y) {
        // Thermometer Rahmen
        graphics.fill(x - 2, y - 2, x + THERMO_WIDTH + 2, y + THERMO_HEIGHT + 2, COLOR_THERMOMETER_FRAME);
        graphics.fill(x, y, x + THERMO_WIDTH, y + THERMO_HEIGHT, COLOR_THERMOMETER_BACKGROUND);

        // Temperatur-Zonen (von unten nach oben)
        int zoneHeight = THERMO_HEIGHT;

        // Kalt-Zone (blau) - unten
        int coldHeight = (int) (zoneHeight * ZONE_COLD_END);
        graphics.fill(x + 2, y + THERMO_HEIGHT - coldHeight, x + THERMO_WIDTH - 2, y + THERMO_HEIGHT - 2, COLOR_TEMP_COLD_BLUE);

        // Optimal-Zone (grün)
        int optimalStart = THERMO_HEIGHT - (int) (zoneHeight * ZONE_OPTIMAL_END);
        int optimalEnd = THERMO_HEIGHT - coldHeight;
        graphics.fill(x + 2, y + optimalStart, x + THERMO_WIDTH - 2, y + optimalEnd, COLOR_TEMP_OPTIMAL_GREEN);

        // Gefahr-Zone (orange)
        int dangerStart = THERMO_HEIGHT - (int) (zoneHeight * ZONE_DANGER_END);
        int dangerEnd = optimalStart;
        graphics.fill(x + 2, y + dangerStart, x + THERMO_WIDTH - 2, y + dangerEnd, COLOR_TEMP_DANGER_ORANGE);

        // Kritisch-Zone (rot) - oben
        graphics.fill(x + 2, y + 2, x + THERMO_WIDTH - 2, y + dangerStart, COLOR_TEMP_HOT_RED);

        // Temperatur-Markierungen
        graphics.fill(x + THERMO_WIDTH - 8, y + THERMO_HEIGHT - coldHeight, x + THERMO_WIDTH - 2, y + THERMO_HEIGHT - coldHeight + 2, COLOR_TEMP_MARKER_WHITE);
        graphics.fill(x + THERMO_WIDTH - 8, y + optimalStart, x + THERMO_WIDTH - 2, y + optimalStart + 2, COLOR_TEMP_MARKER_WHITE);
        graphics.fill(x + THERMO_WIDTH - 8, y + dangerStart, x + THERMO_WIDTH - 2, y + dangerStart + 2, COLOR_TEMP_MARKER_WHITE);

        // Aktuelle Temperatur-Anzeige (horizontaler Balken)
        float thermoPos = menu.getThermometerPosition();
        int indicatorY = y + THERMO_HEIGHT - (int) (thermoPos * (THERMO_HEIGHT - 4)) - 4;

        // Temperatur-Indikator (weißer Balken mit Farbe)
        int tempColor = menu.getTemperatureColor();
        graphics.fill(x - 4, indicatorY - 2, x + THERMO_WIDTH + 4, indicatorY + 4, COLOR_TEMP_MARKER_WHITE);
        graphics.fill(x, indicatorY, x + THERMO_WIDTH, indicatorY + 2, tempColor | 0xFF000000);

        // Temperatur-Labels
        graphics.drawString(this.font, "150°C", x + THERMO_WIDTH + 5, y + 2, 0xFF5555, false);
        graphics.drawString(this.font, "120°C", x + THERMO_WIDTH + 5, y + optimalStart - 4, 0xFFAA00, false);
        graphics.drawString(this.font, "80°C", x + THERMO_WIDTH + 5, y + optimalEnd - 4, 0x55FF55, false);
        graphics.drawString(this.font, "20°C", x + THERMO_WIDTH + 5, y + THERMO_HEIGHT - 10, 0x5555FF, false);
    }

    private void renderHeatButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + BUTTON_WIDTH &&
                          mouseY >= y && mouseY < y + BUTTON_HEIGHT;

        // Button-Farbe basierend auf Status
        int buttonColor;
        int textColor = 0xFFFFFF;

        if (isButtonPressed) {
            // Gedrückt - rot glühend
            buttonColor = COLOR_BUTTON_HEAT_RED;
            textColor = 0xFFFF55;
        } else if (hovered) {
            // Hover - heller
            buttonColor = COLOR_THERMOMETER_FRAME;
        } else {
            // Normal
            buttonColor = COLOR_BUTTON_DISABLED;
        }

        // Button Schatten
        graphics.fill(x + 3, y + 3, x + BUTTON_WIDTH + 3, y + BUTTON_HEIGHT + 3, COLOR_BUTTON_SHADOW);

        // Button Rahmen
        graphics.fill(x - 2, y - 2, x + BUTTON_WIDTH + 2, y + BUTTON_HEIGHT + 2, 0xFF666666);

        // Button Fläche
        graphics.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, buttonColor);

        // Button Text
        String buttonText = isButtonPressed ? "§e§l[HEIZEN]" : "§f[HALTEN ZUM HEIZEN]";
        int textWidth = this.font.width(buttonText);
        graphics.drawString(this.font, buttonText, x + (BUTTON_WIDTH - textWidth) / 2, y + 12, textColor, true);

        // Temperatur-Anzeige auf Button
        int temp = menu.getTemperature();
        String tempStr = temp + "°C";
        String zoneStr = menu.getTemperatureZone();
        graphics.drawString(this.font, "§f" + tempStr + " " + zoneStr,
                x + (BUTTON_WIDTH - this.font.width(tempStr + " " + zoneStr)) / 2 + 10,
                y + 30, 0xFFFFFF, false);

        // Warnhinweis
        if (temp > ReduktionskesselBlockEntity.TEMP_OPTIMAL_MAX) {
            graphics.drawString(this.font, "§c⚠ VORSICHT!", x + (BUTTON_WIDTH - 70) / 2, y + BUTTON_HEIGHT + 5, 0xFF5555, false);
        }
    }

    private void renderProgressBar(GuiGraphics graphics, int x, int y) {
        int barWidth = 130;
        int barHeight = 16;

        // Hintergrund
        graphics.fill(x, y, x + barWidth, y + barHeight, COLOR_THERMOMETER_BACKGROUND);

        // Rahmen
        graphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, COLOR_THERMOMETER_FRAME);
        graphics.fill(x, y, x + barWidth, y + barHeight, COLOR_BACKGROUND_DARK);

        // Progress füllen
        float progress = menu.getProgressPercent();
        int progressWidth = (int) (barWidth * progress);
        if (progressWidth > 0) {
            // Farbverlauf basierend auf Progress
            int progressColor = menu.isProcessing() ? 0xFF55FF55 : 0xFF888888;
            graphics.fill(x + 1, y + 1, x + progressWidth - 1, y + barHeight - 1, progressColor);
        }

        // Progress Text
        int percent = (int) (progress * 100);
        String progressText = menu.isProcessing() ? percent + "%" : "Warten...";
        graphics.drawString(this.font, progressText, x + (barWidth - this.font.width(progressText)) / 2, y + 4, 0xFFFFFF, false);

        // Label
        graphics.drawString(this.font, "§7Fortschritt:", x, y - 12, 0xAAAAAA, false);
    }

    private void renderQualityIndicator(GuiGraphics graphics, int x, int y) {
        MethQuality expected = menu.getExpectedQuality();

        // Hintergrund
        graphics.fill(x - 1, y - 1, x + 131, y + 17, COLOR_THERMOMETER_FRAME);
        graphics.fill(x, y, x + 130, y + 16, COLOR_BACKGROUND_DARK);

        // Qualitäts-Farbe
        int qualityColor = switch (expected) {
            case STANDARD -> 0xFFAAAAAA; // Grau-weiß
            case GUT -> 0xFFFFFF55;      // Gelb
            case BLUE_SKY -> 0xFF55FFFF; // Cyan/Blau
        };

        graphics.fill(x + 2, y + 2, x + 128, y + 14, qualityColor);

        // Qualitäts-Text
        String qualityText = "Erwartete Qualität: " + expected.getDisplayName();
        graphics.drawString(this.font, qualityText, x + 5, y + 4, 0x111111, false);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;

        // Titel
        graphics.drawString(this.font, "§6⚗ §lREDUKTIONSKESSEL", x + 10, y + 8, 0xFFAA00, true);

        // Anleitung
        graphics.drawString(this.font, "§7Halte Temperatur zwischen §a80-120°C", x + 100, y + 160, 0xAAAAAA, false);

        // Explosionswarnung bei kritischer Temperatur
        if (menu.getTemperature() > ReduktionskesselBlockEntity.TEMP_DANGER_MAX) {
            // Blinkender Text
            if (System.currentTimeMillis() % 500 < 250) {
                graphics.drawString(this.font, "§4§l☠ EXPLOSION DROHEND! ☠", x + 80, y + 30, 0xFF0000, true);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Leer - wir rendern alles in render()
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Linksklick
            int x = this.leftPos + BUTTON_X;
            int y = this.topPos + BUTTON_Y;

            if (mouseX >= x && mouseX < x + BUTTON_WIDTH &&
                mouseY >= y && mouseY < y + BUTTON_HEIGHT) {
                isButtonPressed = true;
                menu.setHeating(true);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isButtonPressed) {
            isButtonPressed = false;
            menu.setHeating(false);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        // Sicherstellen dass Heizung aus ist
        if (isButtonPressed) {
            isButtonPressed = false;
            menu.setHeating(false);
        }
        super.onClose();
    }    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
