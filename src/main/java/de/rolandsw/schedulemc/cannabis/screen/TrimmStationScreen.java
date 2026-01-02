package de.rolandsw.schedulemc.cannabis.screen;

import de.rolandsw.schedulemc.cannabis.menu.TrimmStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für die Trimm-Station mit Minigame
 * Spieler muss im richtigen Moment klicken um Blätter zu entfernen
 */
public class TrimmStationScreen extends AbstractContainerScreen<TrimmStationMenu> {

    private static final int GUI_WIDTH = 260;
    private static final int GUI_HEIGHT = 180;

    // Timing-Bar
    private static final int BAR_X = 30;
    private static final int BAR_Y = 60;
    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 25;

    // Trim Button
    private static final int BUTTON_X = 90;
    private static final int BUTTON_Y = 110;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 30;

    // Perfekter Bereich (Mitte der Bar)
    private static final float PERFECT_CENTER = 0.5f;
    private static final float PERFECT_WIDTH = 0.05f;
    private static final float GOOD_WIDTH = 0.15f;

    private boolean gameStarted = false;
    private int lastTrimResult = -1;
    private int resultShowTicks = 0;

    public TrimmStationScreen(TrimmStationMenu menu, Inventory playerInventory, Component title) {
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

        // Haupthintergrund - Grünes Theme
        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF0A1A0A);
        graphics.fill(x + 3, y + 3, x + GUI_WIDTH - 3, y + GUI_HEIGHT - 3, 0xFF1D2D1D);

        // Header
        graphics.fill(x + 3, y + 3, x + GUI_WIDTH - 3, y + 25, 0xFF1D3D1D);

        // Progress-Info
        renderProgressInfo(graphics, x, y);

        // Timing-Bar
        renderTimingBar(graphics, x + BAR_X, y + BAR_Y);

        // Trim-Button
        renderTrimButton(graphics, x + BUTTON_X, y + BUTTON_Y, mouseX, mouseY);

        // Statistiken
        renderStats(graphics, x, y);

        // Ergebnis-Feedback
        if (resultShowTicks > 0) {
            renderResultFeedback(graphics, x, y);
        }
    }

    private void renderProgressInfo(GuiGraphics graphics, int x, int y) {
        int infoY = y + 35;
        graphics.fill(x + 20, infoY, x + GUI_WIDTH - 20, infoY + 18, 0xFF0A1A0A);

        String strainName = menu.getStrain().getColoredName();
        int removed = menu.getLeavesRemoved();
        int total = menu.getTotalLeaves();

        String progressText = "§7Blätter: §f" + removed + "/" + total + " §7| Sorte: " + strainName;
        graphics.drawString(this.font, progressText, x + 30, infoY + 5, 0xFFFFFF, false);
    }

    private void renderTimingBar(GuiGraphics graphics, int x, int y) {
        // Rahmen
        graphics.fill(x - 2, y - 2, x + BAR_WIDTH + 2, y + BAR_HEIGHT + 2, 0xFF555555);

        // Hintergrund
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF222222);

        // Zonen zeichnen
        int perfectCenterX = x + (int)(PERFECT_CENTER * BAR_WIDTH);
        int perfectHalfWidth = (int)(PERFECT_WIDTH * BAR_WIDTH);
        int goodHalfWidth = (int)(GOOD_WIDTH * BAR_WIDTH);

        // Rote Zonen (Ränder)
        graphics.fill(x, y + 2, perfectCenterX - goodHalfWidth, y + BAR_HEIGHT - 2, 0xFFAA3333);
        graphics.fill(perfectCenterX + goodHalfWidth, y + 2, x + BAR_WIDTH, y + BAR_HEIGHT - 2, 0xFFAA3333);

        // Gelbe Zonen
        graphics.fill(perfectCenterX - goodHalfWidth, y + 2, perfectCenterX - perfectHalfWidth, y + BAR_HEIGHT - 2, 0xFFAAAA33);
        graphics.fill(perfectCenterX + perfectHalfWidth, y + 2, perfectCenterX + goodHalfWidth, y + BAR_HEIGHT - 2, 0xFFAAAA33);

        // Grüne Zone (Perfekt)
        graphics.fill(perfectCenterX - perfectHalfWidth, y + 2, perfectCenterX + perfectHalfWidth, y + BAR_HEIGHT - 2, 0xFF33AA33);

        // Bewegender Indikator
        if (menu.isMinigameActive()) {
            float progress = menu.getCycleProgress();
            int indicatorX = x + (int)(progress * BAR_WIDTH);

            // Indikator-Linie
            int indicatorWidth = 4;
            graphics.fill(indicatorX - indicatorWidth/2 - 1, y - 4, indicatorX + indicatorWidth/2 + 1, y + BAR_HEIGHT + 4, 0xFFFFFFFF);
            graphics.fill(indicatorX - indicatorWidth/2, y - 3, indicatorX + indicatorWidth/2, y + BAR_HEIGHT + 3, 0xFFEEEEEE);
        }

        // Labels
        graphics.drawString(this.font, "§cSchlecht", x + 5, y + BAR_HEIGHT + 5, 0xAAAAAA, false);
        graphics.drawString(this.font, "§aPERFEKT", x + BAR_WIDTH/2 - 20, y + BAR_HEIGHT + 5, 0xAAAAAA, false);
        graphics.drawString(this.font, "§cSchlecht", x + BAR_WIDTH - 50, y + BAR_HEIGHT + 5, 0xAAAAAA, false);
    }

    private void renderTrimButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + BUTTON_WIDTH &&
                          mouseY >= y && mouseY < y + BUTTON_HEIGHT;
        boolean canTrim = menu.isMinigameActive();

        int buttonColor;
        String buttonText;

        if (!gameStarted && !menu.isMinigameActive()) {
            buttonColor = hovered ? 0xFF55AA55 : 0xFF44AA44;
            buttonText = "§f▶ STARTEN";
        } else if (canTrim && hovered) {
            buttonColor = 0xFF55DD55;
            buttonText = "§f✂ SCHNEIDEN!";
        } else if (canTrim) {
            buttonColor = 0xFF44AA44;
            buttonText = "§f✂ SCHNEIDEN!";
        } else {
            buttonColor = 0xFF333355;
            buttonText = "§7Fertig!";
        }

        // Animation
        int yOffset = 0;
        if (canTrim) {
            yOffset = (int)(Math.sin(System.currentTimeMillis() / 150.0) * 2);
        }

        // Schatten
        graphics.fill(x + 2, y + 2 + yOffset, x + BUTTON_WIDTH + 2, y + BUTTON_HEIGHT + 2 + yOffset, 0x66000000);

        // Button
        graphics.fill(x - 1, y - 1 + yOffset, x + BUTTON_WIDTH + 1, y + BUTTON_HEIGHT + 1 + yOffset, 0xFF224422);
        graphics.fill(x, y + yOffset, x + BUTTON_WIDTH, y + BUTTON_HEIGHT + yOffset, buttonColor);

        // Text
        int textWidth = this.font.width(buttonText);
        graphics.drawString(this.font, buttonText, x + BUTTON_WIDTH/2 - textWidth/2, y + 10 + yOffset, 0xFFFFFF, true);
    }

    private void renderStats(GuiGraphics graphics, int x, int y) {
        int statsY = y + 148;

        // Stats-Box
        graphics.fill(x + 20, statsY, x + GUI_WIDTH - 20, statsY + 25, 0xFF0A1A0A);

        String stats = String.format("§aPerfekt: %d §7| §eGut: %d §7| §cSchlecht: %d",
                menu.getPerfectTrims(), menu.getGoodTrims(), menu.getBadTrims());

        int statsWidth = this.font.width(stats);
        graphics.drawString(this.font, stats, x + GUI_WIDTH/2 - statsWidth/2, statsY + 8, 0xFFFFFF, false);
    }

    private void renderResultFeedback(GuiGraphics graphics, int x, int y) {
        int feedbackY = y + 95;

        String feedbackText;
        int feedbackColor;

        switch (lastTrimResult) {
            case 2 -> {
                feedbackText = "§a★ PERFEKT! ★";
                feedbackColor = 0x4455FF55;
            }
            case 1 -> {
                feedbackText = "§eGut!";
                feedbackColor = 0x44FFFF55;
            }
            default -> {
                feedbackText = "§cDaneben...";
                feedbackColor = 0x44FF5555;
            }
        }

        int textWidth = this.font.width(feedbackText);
        graphics.fill(x + GUI_WIDTH/2 - textWidth/2 - 5, feedbackY - 2,
                      x + GUI_WIDTH/2 + textWidth/2 + 5, feedbackY + 12, feedbackColor);
        graphics.drawString(this.font, feedbackText, x + GUI_WIDTH/2 - textWidth/2, feedbackY, 0xFFFFFF, true);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;

        // Titel
        graphics.drawString(this.font, "§a✂ §lTRIMM-STATION §a✂", x + 10, y + 8, 0xFF88FF88, true);

        // Anleitung
        if (menu.isMinigameActive()) {
            String hint = "§e▶ Klicke im §a§lGRÜNEN§e Bereich! ◀";
            int hintWidth = this.font.width(hint);
            graphics.drawString(this.font, hint, x + GUI_WIDTH/2 - hintWidth/2, y + 143, 0xFFFFFF, true);
        }

        // Result-Timer
        if (resultShowTicks > 0) {
            resultShowTicks--;
        }

        // Prüfe ob Minigame beendet
        if (gameStarted && !menu.isMinigameActive() && menu.getLeavesRemoved() >= menu.getTotalLeaves()) {
            // Fertig - schließe GUI nach kurzer Verzögerung
            if (resultShowTicks <= 0) {
                this.onClose();
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Leer
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = this.leftPos;
            int y = this.topPos;

            int buttonX = x + BUTTON_X;
            int buttonY = y + BUTTON_Y;

            if (mouseX >= buttonX && mouseX < buttonX + BUTTON_WIDTH &&
                mouseY >= buttonY && mouseY < buttonY + BUTTON_HEIGHT) {

                if (!gameStarted && !menu.isMinigameActive()) {
                    // Start
                    if (menu.startMinigame(minecraft.player)) {
                        gameStarted = true;
                    }
                    return true;
                } else if (menu.isMinigameActive()) {
                    // Trim
                    lastTrimResult = menu.trimClick();
                    resultShowTicks = 20;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Leertaste zum Trimmen
        if (keyCode == 32 && menu.isMinigameActive()) {
            lastTrimResult = menu.trimClick();
            resultShowTicks = 20;
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
