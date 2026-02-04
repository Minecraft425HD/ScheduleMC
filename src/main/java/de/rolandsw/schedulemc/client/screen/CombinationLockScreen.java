package de.rolandsw.schedulemc.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Zahlenschloss-GUI: 4-stellige Code-Eingabe.
 *
 * Layout:
 * ┌──────────────────────┐
 * │   ZAHLENSCHLOSS      │
 * │   Lock-ID: abc123    │
 * │                      │
 * │   [ _ _ _ _ ]        │
 * │                      │
 * │   [1][2][3]          │
 * │   [4][5][6]          │
 * │   [7][8][9]          │
 * │   [C][0][OK]         │
 * └──────────────────────┘
 */
public class CombinationLockScreen extends Screen {

    private static final int PAD_W = 140;
    private static final int PAD_H = 180;
    private static final int BTN_SIZE = 28;
    private static final int BTN_GAP = 4;

    private final String lockId;
    private final boolean isSetMode;  // true = Code setzen, false = Code eingeben
    private final java.util.function.Consumer<String> onSubmit;

    private final StringBuilder code = new StringBuilder();

    public CombinationLockScreen(String lockId, boolean isSetMode,
                                  java.util.function.Consumer<String> onSubmit) {
        super(Component.literal("Zahlenschloss"));
        this.lockId = lockId;
        this.isSetMode = isSetMode;
        this.onSubmit = onSubmit;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);

        int cx = this.width / 2, cy = this.height / 2;
        int px = cx - PAD_W / 2, py = cy - PAD_H / 2;

        // Hintergrund
        g.fill(px - 2, py - 2, px + PAD_W + 2, py + PAD_H + 2, 0xFF000000);
        g.fill(px, py, px + PAD_W, py + PAD_H, 0xFF1A1A2E);
        g.fill(px, py, px + PAD_W, py + 2, 0xFFF39C12);

        // Titel
        String title = isSetMode ? "CODE FESTLEGEN" : "CODE EINGEBEN";
        g.drawCenteredString(this.font, "\u00A7l" + title, cx, py + 8, 0xFFF39C12);
        g.drawCenteredString(this.font, "\u00A78Lock: " + lockId, cx, py + 20, 0x888888);

        // Code-Anzeige
        int codeY = py + 36;
        g.fill(cx - 44, codeY - 2, cx + 44, codeY + 18, 0xFF0A0A1A);
        g.fill(cx - 44, codeY - 2, cx + 44, codeY - 1, 0xFF3498DB);
        for (int i = 0; i < 4; i++) {
            int dx = cx - 32 + i * 18;
            String digit = i < code.length() ? String.valueOf(code.charAt(i)) : "_";
            g.drawCenteredString(this.font, "\u00A7l" + digit, dx + 5, codeY + 3, 0xFFFFFF);
        }

        // Nummernpad
        int padX = cx - (3 * BTN_SIZE + 2 * BTN_GAP) / 2;
        int padY = codeY + 26;

        // Reihen 1-9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int num = row * 3 + col + 1;
                int bx = padX + col * (BTN_SIZE + BTN_GAP);
                int by = padY + row * (BTN_SIZE + BTN_GAP);
                boolean hover = mx >= bx && mx < bx + BTN_SIZE && my >= by && my < by + BTN_SIZE;
                g.fill(bx, by, bx + BTN_SIZE, by + BTN_SIZE, hover ? 0xFF2980B9 : 0xFF16213E);
                g.fill(bx, by, bx + BTN_SIZE, by + 1, 0xFF3498DB);
                g.drawCenteredString(this.font, String.valueOf(num), bx + BTN_SIZE / 2, by + 10, 0xFFFFFF);
            }
        }

        // Letzte Reihe: [C] [0] [OK]
        int lastY = padY + 3 * (BTN_SIZE + BTN_GAP);
        String[] labels = {"C", "0", "OK"};
        int[] colors = {0xFFE74C3C, 0xFF16213E, 0xFF2ECC71};
        int[] hColors = {0xFFC0392B, 0xFF2980B9, 0xFF27AE60};
        for (int col = 0; col < 3; col++) {
            int bx = padX + col * (BTN_SIZE + BTN_GAP);
            boolean hover = mx >= bx && mx < bx + BTN_SIZE && my >= lastY && my < lastY + BTN_SIZE;
            g.fill(bx, lastY, bx + BTN_SIZE, lastY + BTN_SIZE, hover ? hColors[col] : colors[col]);
            g.fill(bx, lastY, bx + BTN_SIZE, lastY + 1, hover ? 0xFFFFFFFF : 0xFF3498DB);
            g.drawCenteredString(this.font, labels[col], bx + BTN_SIZE / 2, lastY + 10, 0xFFFFFF);
        }

        // ESC-Hinweis
        g.drawCenteredString(this.font, "\u00A78[ESC] Abbrechen", cx, py + PAD_H - 12, 0x555555);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX, my = (int) mouseY;
        int cx = this.width / 2, cy = this.height / 2;
        int padX = cx - (3 * BTN_SIZE + 2 * BTN_GAP) / 2;
        int padY = cy - PAD_H / 2 + 36 + 26;

        // Nummern 1-9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int bx = padX + col * (BTN_SIZE + BTN_GAP);
                int by = padY + row * (BTN_SIZE + BTN_GAP);
                if (mx >= bx && mx < bx + BTN_SIZE && my >= by && my < by + BTN_SIZE) {
                    appendDigit((char) ('1' + row * 3 + col));
                    return true;
                }
            }
        }

        // Letzte Reihe
        int lastY = padY + 3 * (BTN_SIZE + BTN_GAP);
        for (int col = 0; col < 3; col++) {
            int bx = padX + col * (BTN_SIZE + BTN_GAP);
            if (mx >= bx && mx < bx + BTN_SIZE && my >= lastY && my < lastY + BTN_SIZE) {
                if (col == 0) clearCode();
                else if (col == 1) appendDigit('0');
                else submitCode();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mod) {
        // Zahlen-Tasten (Haupttastatur und Numpad)
        if (key >= 48 && key <= 57) { appendDigit((char) key); return true; }
        if (key >= 320 && key <= 329) { appendDigit((char) ('0' + (key - 320))); return true; }
        if (key == 259) { // Backspace
            if (code.length() > 0) code.deleteCharAt(code.length() - 1);
            return true;
        }
        if (key == 257 || key == 335) { submitCode(); return true; } // Enter / Numpad Enter
        return super.keyPressed(key, scan, mod);
    }

    private void appendDigit(char digit) {
        if (code.length() < 4) code.append(digit);
    }

    private void clearCode() {
        code.setLength(0);
    }

    private void submitCode() {
        if (code.length() != 4) return;
        onSubmit.accept(code.toString());
        onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
