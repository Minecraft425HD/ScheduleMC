package de.rolandsw.schedulemc.client.screen;

import de.rolandsw.schedulemc.lock.network.HackingResultPacket;
import de.rolandsw.schedulemc.lock.network.LockNetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Hacking-Minigame Screen: Ziffern eines Codes einzeln knacken.
 *
 * Jede Ziffer rotiert durch 0-9 mit variabler Geschwindigkeit.
 * Der Spieler muss SPACE druecken wenn die richtige Ziffer angezeigt wird.
 * - Treffer: Ziffer wird gruen fixiert
 * - Daneben: Roter Blitz, naechster Versuch
 * - Max 3 Fehlversuche pro Ziffer, danach Hacking fehlgeschlagen
 *
 * Schwierigkeit:
 * - COMBINATION: Mittlere Geschwindigkeit (0.08 pro Tick)
 * - DUAL: Hohe Geschwindigkeit (0.14 pro Tick) + Code rotiert
 */
@OnlyIn(Dist.CLIENT)
public class HackingScreen extends Screen {

    private final String lockId;
    private final String posKey;
    private final String lockTypeName;
    private final int codeLength;

    // Minigame-State
    private int currentDigitIndex = 0;
    private final int[] crackedDigits;
    private final boolean[] digitLocked;

    // Rotierende Anzeige
    private float digitCycle = 0.0f;     // 0.0 - 10.0 (welche Ziffer gerade angezeigt wird)
    private float cycleSpeed;             // Wie schnell die Ziffern rotieren
    private int displayedDigit = 0;       // Aktuell angezeigte Ziffer (0-9)

    // Versuche
    private int failsThisDigit = 0;
    private static final int MAX_FAILS_PER_DIGIT = 3;
    private boolean hackingFailed = false;
    private boolean hackingSuccess = false;
    private long resultTime = 0;

    // Visuelle Effekte
    private int flashColor = 0;
    private int flashTicks = 0;

    // Echt-Code (wird vom Server NICHT gesendet! Spieler muss raten)
    // Der Code wird per Zufall simuliert - der Server validiert spaeter
    private final int[] targetHints;  // Hinweis-System: zeigt an ob man nah dran ist

    // UI-Konstanten
    private static final int BG_COLOR = 0xDD1A1A2E;
    private static final int PANEL_COLOR = 0xFF16213E;
    private static final int ACCENT_COLOR = 0xFF0F3460;
    private static final int DIGIT_BG = 0xFF0A0A1A;
    private static final int GREEN = 0xFF00FF41;
    private static final int RED = 0xFFFF0040;
    private static final int CYAN = 0xFF00D9FF;
    private static final int YELLOW = 0xFFFFD700;

    public HackingScreen(String lockId, String posKey, String lockTypeName, int codeLength) {
        super(Component.literal("HACKING"));
        this.lockId = lockId;
        this.posKey = posKey;
        this.lockTypeName = lockTypeName;
        this.codeLength = codeLength;
        this.crackedDigits = new int[codeLength];
        this.digitLocked = new boolean[codeLength];
        this.targetHints = new int[codeLength];

        // Geschwindigkeit je nach Schloss-Typ
        boolean isDual = lockTypeName.equals("DUAL");
        this.cycleSpeed = isDual ? 0.14f : 0.08f;

        // Zufaellige Start-Position
        this.digitCycle = ThreadLocalRandom.current().nextFloat() * 10.0f;
    }

    @Override
    public void tick() {
        super.tick();

        if (hackingFailed || hackingSuccess) return;

        // Ziffer rotieren lassen
        digitCycle += cycleSpeed;
        if (digitCycle >= 10.0f) digitCycle -= 10.0f;
        displayedDigit = (int) digitCycle;

        // Flash-Effekt abklingen
        if (flashTicks > 0) flashTicks--;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            sendResult(false, "");
            this.onClose();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_SPACE && !hackingFailed && !hackingSuccess) {
            attemptLockDigit();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !hackingFailed && !hackingSuccess) {
            attemptLockDigit();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void attemptLockDigit() {
        // Fixiere die aktuell angezeigte Ziffer
        crackedDigits[currentDigitIndex] = displayedDigit;

        // Wir wissen nicht ob es richtig ist - der Server entscheidet
        // Aber wir simulieren ein "Hacking-Gefuehl":
        // Ziffer wird immer als "gelockt" markiert, Ergebnis kommt am Ende
        digitLocked[currentDigitIndex] = true;

        // Visueller Feedback: Gruen-Flash
        flashColor = GREEN;
        flashTicks = 6;

        // Naechste Ziffer
        currentDigitIndex++;

        if (currentDigitIndex >= codeLength) {
            // Alle Ziffern gelockt - sende Code an Server
            hackingSuccess = true;
            resultTime = System.currentTimeMillis();
            StringBuilder code = new StringBuilder();
            for (int d : crackedDigits) code.append(d);
            sendResult(true, code.toString());
        } else {
            // Geschwindigkeit leicht erhoehen bei jeder neuen Ziffer
            cycleSpeed += 0.015f;
            // Zufaellige neue Startposition
            digitCycle = ThreadLocalRandom.current().nextFloat() * 10.0f;
        }
    }

    private void sendResult(boolean completed, String enteredCode) {
        LockNetworkHandler.sendToServer(new HackingResultPacket(posKey, enteredCode, completed));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        int panelW = 260;
        int panelH = 180;
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;

        // Hintergrund-Panel
        g.fill(px - 2, py - 2, px + panelW + 2, py + panelH + 2, ACCENT_COLOR);
        g.fill(px, py, px + panelW, py + panelH, PANEL_COLOR);

        // Header
        g.fill(px, py, px + panelW, py + 22, ACCENT_COLOR);
        String header = "\u00A7l[ HACKING TOOL v2.1 ]";
        g.drawString(font, header, px + panelW / 2 - font.width(header) / 2, py + 7, CYAN, false);

        // Lock-Info
        String lockInfo = "Lock-ID: " + lockId + " | Typ: " + lockTypeName;
        g.drawString(font, lockInfo, px + 8, py + 28, 0xFF888888, false);

        // Status-Zeile
        String status;
        int statusColor;
        if (hackingSuccess) {
            status = ">> CODE GESENDET - WARTE AUF VALIDIERUNG...";
            statusColor = GREEN;
        } else if (hackingFailed) {
            status = ">> HACKING FEHLGESCHLAGEN!";
            statusColor = RED;
        } else {
            status = ">> Knacke Ziffer " + (currentDigitIndex + 1) + "/" + codeLength
                    + " | SPACE/KLICK zum Locken";
            statusColor = YELLOW;
        }
        g.drawString(font, status, px + 8, py + 42, statusColor, false);

        // ═══ ZIFFERN-ANZEIGE ═══
        int digitSize = 36;
        int digitSpacing = 12;
        int totalWidth = codeLength * digitSize + (codeLength - 1) * digitSpacing;
        int startX = px + (panelW - totalWidth) / 2;
        int digitY = py + 62;

        for (int i = 0; i < codeLength; i++) {
            int dx = startX + i * (digitSize + digitSpacing);

            // Hintergrund der Ziffer
            int bg = DIGIT_BG;
            int border = ACCENT_COLOR;

            if (digitLocked[i]) {
                // Fixierte Ziffer
                border = GREEN;
                bg = 0xFF002200;
            } else if (i == currentDigitIndex && !hackingFailed && !hackingSuccess) {
                // Aktive Ziffer - pulsierend
                border = CYAN;
                if (flashTicks > 0) {
                    bg = (flashColor & 0x00FFFFFF) | 0x44000000;
                }
            }

            // Rahmen
            g.fill(dx - 2, digitY - 2, dx + digitSize + 2, digitY + digitSize + 2, border);
            g.fill(dx, digitY, dx + digitSize, digitY + digitSize, bg);

            // Ziffer zeichnen
            String digitStr;
            int digitColor;

            if (digitLocked[i]) {
                digitStr = String.valueOf(crackedDigits[i]);
                digitColor = GREEN;
            } else if (i == currentDigitIndex && !hackingFailed && !hackingSuccess) {
                digitStr = String.valueOf(displayedDigit);
                digitColor = CYAN;
            } else {
                digitStr = "?";
                digitColor = 0xFF555555;
            }

            // Grosse Ziffer zentriert
            g.pose().pushPose();
            g.pose().translate(dx + digitSize / 2.0f - font.width(digitStr) * 1.5f / 2.0f,
                    digitY + digitSize / 2.0f - 4.5f * 1.5f, 0);
            g.pose().scale(1.5f, 1.5f, 1.0f);
            g.drawString(font, digitStr, 0, 0, digitColor, false);
            g.pose().popPose();
        }

        // ═══ ROTATIONS-BAR ═══ (zeigt wo im Zyklus man ist)
        if (!hackingFailed && !hackingSuccess && currentDigitIndex < codeLength) {
            int barX = px + 20;
            int barY = digitY + digitSize + 16;
            int barW = panelW - 40;
            int barH = 8;

            // Hintergrund
            g.fill(barX, barY, barX + barW, barY + barH, 0xFF222222);

            // 10 Segmente (0-9)
            int segW = barW / 10;
            for (int i = 0; i < 10; i++) {
                int sx = barX + i * segW;
                // Aktuelles Segment hervorheben
                if (i == displayedDigit) {
                    g.fill(sx, barY, sx + segW, barY + barH, CYAN);
                }
                // Trennlinien
                g.fill(sx, barY, sx + 1, barY + barH, 0xFF333333);
            }

            // Indikator-Position
            float normalizedPos = digitCycle / 10.0f;
            int indicatorX = barX + (int) (normalizedPos * barW);
            g.fill(indicatorX - 1, barY - 3, indicatorX + 2, barY + barH + 3, 0xFFFFFFFF);

            // Ziffern unter der Bar
            for (int i = 0; i < 10; i++) {
                int sx = barX + i * segW + segW / 2 - 3;
                int numColor = (i == displayedDigit) ? CYAN : 0xFF555555;
                g.drawString(font, String.valueOf(i), sx, barY + barH + 3, numColor, false);
            }
        }

        // ═══ ANLEITUNG ═══
        int footerY = py + panelH - 18;
        g.drawString(font, "[SPACE/KLICK] Ziffer locken  |  [ESC] Abbrechen",
                px + 8, footerY, 0xFF666666, false);

        // Auto-Close bei Ergebnis
        if ((hackingSuccess || hackingFailed) && resultTime > 0
                && System.currentTimeMillis() - resultTime > 2000) {
            this.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
