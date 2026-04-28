package de.rolandsw.schedulemc.coca.screen;

import de.rolandsw.schedulemc.coca.blockentity.CrackCookerBlockEntity;
import de.rolandsw.schedulemc.coca.menu.CrackCookerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CrackCookerScreen extends AbstractContainerScreen<CrackCookerMenu> {

    private static final int COLOR_BG        = 0xFF1A2B1A;
    private static final int COLOR_PANEL     = 0xFF2B3B2B;
    private static final int COLOR_HEADER    = 0xFF0E1A0E;
    private static final int COLOR_SEP       = 0xFF3A5A3A;

    // Minigame bar dimensions
    private static final int BAR_X_OFFSET = 44;
    private static final int BAR_Y_OFFSET = 62;
    private static final int BAR_W        = 88; // pixels for 80 ticks
    private static final int BAR_H        = 16;

    // Zone colors
    private static final int COLOR_EARLY   = 0xFFAA2222;
    private static final int COLOR_LATE    = 0xFF882222;
    private static final int COLOR_GOOD    = 0xFFCCAA00;
    private static final int COLOR_PERFECT = 0xFF33AA33;
    private static final int COLOR_BAR_BG  = 0xFF1A1A1A;
    private static final int COLOR_MARKER  = 0xFFFFFFFF;

    // Button layout
    private static final int BTN_W = 80;
    private static final int BTN_H = 16;
    private static final int BTN_START_X  = 48;
    private static final int BTN_START_Y  = 82;
    private static final int BTN_REMOVE_X = 44;
    private static final int BTN_REMOVE_Y = 90;

    public CrackCookerScreen(CrackCookerMenu menu, Inventory inv, Component title) {
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

        // Input slots
        drawSlot(g, x + 22, y + 30); // cocaine
        drawSlot(g, x + 44, y + 30); // backpulver

        // Output slot
        drawSlot(g, x + 134, y + 30);

        // Separator after machine area
        g.fill(x + 8, y + 55, x + 168, y + 56, COLOR_SEP);

        // Minigame bar (only when active)
        if (menu.isMinigameActive()) {
            renderMinigameBar(g, x, y, partialTick);
        }

        // Button
        boolean active = menu.isMinigameActive();
        if (active) {
            renderButton(g, x + BTN_REMOVE_X, y + BTN_REMOVE_Y, mouseX, mouseY, true);
        } else if (menu.canStart()) {
            renderButton(g, x + BTN_START_X, y + BTN_START_Y, mouseX, mouseY, false);
        }

        // Separator before hotbar
        g.fill(x + 8, y + 134, x + 168, y + 135, COLOR_SEP);

        // Hotbar slots
        for (int i = 0; i < 9; i++) {
            drawSlot(g, x + 8 + i * 18, y + 142);
        }
    }

    private void renderMinigameBar(GuiGraphics g, int x, int y, float partialTick) {
        int bx = x + BAR_X_OFFSET;
        int by = y + BAR_Y_OFFSET;

        // Background
        g.fill(bx, by, bx + BAR_W, by + BAR_H, COLOR_BAR_BG);

        // Scale: 88px for 80 ticks → 1.1px/tick
        float scale = (float) BAR_W / CrackCookerBlockEntity.COOK_CYCLE_TICKS;

        // Early zone (0-27)
        int earlyEnd = (int)(CrackCookerBlockEntity.GOOD_WINDOW_START * scale);
        g.fill(bx, by, bx + earlyEnd, by + BAR_H, COLOR_EARLY);

        // Good zone left (28-34)
        int goodLeftStart = earlyEnd;
        int goodLeftEnd   = (int)(CrackCookerBlockEntity.PERFECT_WINDOW_START * scale);
        g.fill(bx + goodLeftStart, by, bx + goodLeftEnd, by + BAR_H, COLOR_GOOD);

        // Perfect zone (35-45)
        int perfectStart = goodLeftEnd;
        int perfectEnd   = (int)((CrackCookerBlockEntity.PERFECT_WINDOW_END + 1) * scale);
        g.fill(bx + perfectStart, by, bx + perfectEnd, by + BAR_H, COLOR_PERFECT);

        // Good zone right (46-52)
        int goodRightStart = perfectEnd;
        int goodRightEnd   = (int)((CrackCookerBlockEntity.GOOD_WINDOW_END + 1) * scale);
        g.fill(bx + goodRightStart, by, bx + goodRightEnd, by + BAR_H, COLOR_GOOD);

        // Late zone (53-80)
        g.fill(bx + goodRightEnd, by, bx + BAR_W, by + BAR_H, COLOR_LATE);

        // Zone borders
        g.fill(bx + earlyEnd, by, bx + earlyEnd + 1, by + BAR_H, 0xFFFFFFAA);
        g.fill(bx + goodLeftEnd, by, bx + goodLeftEnd + 1, by + BAR_H, 0xFFFFFFAA);
        g.fill(bx + perfectEnd, by, bx + perfectEnd + 1, by + BAR_H, 0xFFFFFFAA);
        g.fill(bx + goodRightEnd, by, bx + goodRightEnd + 1, by + BAR_H, 0xFFFFFFAA);

        // Bar border
        g.fill(bx - 1, by - 1, bx + BAR_W + 1, by, 0xFF666666);
        g.fill(bx - 1, by + BAR_H, bx + BAR_W + 1, by + BAR_H + 1, 0xFF666666);
        g.fill(bx - 1, by, bx, by + BAR_H, 0xFF666666);
        g.fill(bx + BAR_W, by, bx + BAR_W + 1, by + BAR_H, 0xFF666666);

        // Current position marker (white vertical line)
        if (menu.isMinigameActive()) {
            int tick = menu.getCookTick();
            int markerX = (int)(tick * scale);
            markerX = Math.min(markerX, BAR_W - 1);
            g.fill(bx + markerX, by - 2, bx + markerX + 2, by + BAR_H + 2, COLOR_MARKER);
        }
    }

    private void renderButton(GuiGraphics g, int x, int y, int mouseX, int mouseY, boolean isRemove) {
        boolean hovered = mouseX >= x && mouseX < x + BTN_W && mouseY >= y && mouseY < y + BTN_H;

        String text;
        int color;
        if (isRemove) {
            text  = Component.translatable("gui.crack_cooker.button_remove").getString();
            color = hovered ? 0xFFFF4444 : 0xFFDD2222;
        } else {
            text  = Component.translatable("gui.crack_cooker.button_start").getString();
            color = hovered ? 0xFF55DD55 : 0xFF44AA44;
        }

        g.fill(x + 2, y + 2, x + BTN_W + 2, y + BTN_H + 2, 0x66000000);
        g.fill(x - 1, y - 1, x + BTN_W + 1, y + BTN_H + 1, isRemove ? 0xFF442222 : 0xFF224422);
        g.fill(x, y, x + BTN_W, y + BTN_H, color);
        int tw = font.width(text);
        g.drawString(font, text, x + BTN_W / 2 - tw / 2, y + 4, 0xFFFFFF, true);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;

        // Title
        g.drawString(font, Component.translatable("gui.crack_cooker.title").getString(),
                x + 8, y + 6, 0xFF88FF88, true);

        // Slot labels — cocaine slot at x+22, backpulver at x+44, output at x+134
        g.drawString(font, Component.translatable("gui.crack_cooker.cocaine_label").getString(),
                x + 18, y + 19, 0xAAAAAA, false);
        g.drawString(font, Component.translatable("gui.crack_cooker.backpulver_label").getString(),
                x + 44, y + 19, 0xAAAAAA, false);
        g.drawString(font, Component.translatable("gui.crack_cooker.output_label").getString(),
                x + 130, y + 19, 0xAAAAAA, false);

        // Info / status
        if (menu.isMinigameActive()) {
            // Zone label below the bar
            String zoneLabel;
            int zoneColor;
            switch (menu.getCookZone()) {
                case 0 -> { zoneLabel = Component.translatable("gui.crack_cooker.zone_too_early").getString(); zoneColor = 0xFFCC4444; }
                case 1 -> { zoneLabel = Component.translatable("gui.crack_cooker.zone_good").getString();      zoneColor = 0xFFCCAA00; }
                case 2 -> { zoneLabel = Component.translatable("gui.crack_cooker.zone_perfect").getString();   zoneColor = 0xFF44FF44; }
                default -> { zoneLabel = Component.translatable("gui.crack_cooker.zone_too_late").getString(); zoneColor = 0xFFAA3333; }
            }
            g.drawCenteredString(font, zoneLabel, x + 88, y + 80, zoneColor);
        } else if (menu.hasOutput()) {
            g.drawCenteredString(font,
                    Component.translatable("gui.crack_cooker.crack_ready").getString(),
                    x + 88, y + 66, 0xFF44FF44);
        } else {
            // Show input status
            String info = menu.getCocaineGrams() + "g " + Component.translatable("gui.crack_cooker.cocaine_unit").getString()
                    + " | " + menu.getBackpulverCount() + "x " + Component.translatable("gui.crack_cooker.backpulver_unit").getString();
            g.drawCenteredString(font, info, x + 88, y + 66, 0xFFFFFF);

            if (!menu.canStart() && menu.getCocaineGrams() == 0) {
                g.drawCenteredString(font,
                        Component.translatable("gui.crack_cooker.hint_add_cocaine").getString(),
                        x + 88, y + 76, 0xAAAAAA);
            } else if (menu.getCocaineGrams() > 0 && menu.getBackpulverCount() == 0) {
                g.drawCenteredString(font,
                        Component.translatable("gui.crack_cooker.hint_add_backpulver").getString(),
                        x + 88, y + 76, 0xAAAAAA);
            }
        }

        // Inventar label
        g.drawString(font, Component.translatable("gui.crack_cooker.inventory_label").getString(),
                x + 8, y + 130, 0xAAAAAA, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = leftPos;
            int y = topPos;

            if (menu.isMinigameActive()) {
                int bx = x + BTN_REMOVE_X;
                int by = y + BTN_REMOVE_Y;
                if (mouseX >= bx && mouseX < bx + BTN_W && mouseY >= by && mouseY < by + BTN_H) {
                    Objects.requireNonNull(this.minecraft).gameMode
                            .handleInventoryButtonClick(this.menu.containerId, CrackCookerMenu.BUTTON_REMOVE);
                    return true;
                }
            } else if (menu.canStart()) {
                int bx = x + BTN_START_X;
                int by = y + BTN_START_Y;
                if (mouseX >= bx && mouseX < bx + BTN_W && mouseY >= by && mouseY < by + BTN_H) {
                    Objects.requireNonNull(this.minecraft).gameMode
                            .handleInventoryButtonClick(this.menu.containerId, CrackCookerMenu.BUTTON_START);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) { }

    @Override
    public boolean isPauseScreen() { return false; }
}
