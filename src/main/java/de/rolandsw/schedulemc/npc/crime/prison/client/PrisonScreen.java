package de.rolandsw.schedulemc.npc.crime.prison.client;

import de.rolandsw.schedulemc.npc.crime.prison.network.PayBailPacket;
import de.rolandsw.schedulemc.npc.crime.prison.network.PrisonNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Gefängnis-GUI - KANN NICHT GESCHLOSSEN WERDEN
 */
@OnlyIn(Dist.CLIENT)
public class PrisonScreen extends Screen {

    private static final int BACKGROUND_WIDTH = 300;
    private static final int BACKGROUND_HEIGHT = 280;

    private static final int COLOR_BACKGROUND = 0xDD1a1a1a;
    private static final int COLOR_HEADER = 0xFF8B0000;
    private static final int COLOR_PROGRESS_BG = 0xFF333333;
    private static final int COLOR_PROGRESS_FILL = 0xFF4CAF50;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_TEXT_GRAY = 0xFFAAAAAA;
    private static final int COLOR_TEXT_RED = 0xFFFF5555;
    private static final int COLOR_TEXT_GREEN = 0xFF55FF55;
    private static final int COLOR_TEXT_GOLD = 0xFFFFAA00;

    private int cellNumber;
    private long totalSentenceTicks;
    private long releaseTime;
    private double bailAmount;
    private double playerBalance;
    private boolean bailAvailable;
    private long bailAvailableAtTick;

    private int leftPos;
    private int topPos;
    private Button bailButton;

    private boolean canClose = false;

    public PrisonScreen(int cellNumber, long totalSentenceTicks, long releaseTime,
                        double bailAmount, double playerBalance, long bailAvailableAtTick) {
        super(Component.translatable("gui.common.prison"));

        this.cellNumber = cellNumber;
        this.totalSentenceTicks = totalSentenceTicks;
        this.releaseTime = releaseTime;
        this.bailAmount = bailAmount;
        this.playerBalance = playerBalance;
        this.bailAvailableAtTick = bailAvailableAtTick;
        this.bailAvailable = false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        if (canClose) {
            super.onClose();
        } else {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(
                    Component.literal("§c⛓ Du kannst nicht entkommen! Warte ab oder zahle Kaution."),
                    true
                );
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(
                    Component.literal("§c⛓ ESC ist gesperrt!"),
                    true
                );
            }
            return true;
        }

        if (keyCode == 69) { // E
            return true;
        }

        if (keyCode == 84) { // T für Chat
            
            // Block E key (inventory key - 69) from closing the screen
            if (keyCode == 69) { // GLFW_KEY_E
                return true; // Consume event, prevent closing
            }
            
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= leftPos && mouseX <= leftPos + BACKGROUND_WIDTH &&
            mouseY >= topPos && mouseY <= topPos + BACKGROUND_HEIGHT) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - BACKGROUND_WIDTH) / 2;
        this.topPos = (this.height - BACKGROUND_HEIGHT) / 2;

        this.bailButton = addRenderableWidget(Button.builder(
            Component.literal("KAUTION ZAHLEN"),
            this::onBailButtonClick
        ).bounds(
            leftPos + 50,
            topPos + BACKGROUND_HEIGHT - 80,
            BACKGROUND_WIDTH - 100,
            30
        ).build());

        updateBailButton();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        renderWindow(graphics);
        renderHeader(graphics);
        renderCellInfo(graphics);
        renderBailSection(graphics);
        renderHintText(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderWindow(GuiGraphics graphics) {
        graphics.fill(leftPos, topPos, leftPos + BACKGROUND_WIDTH, topPos + BACKGROUND_HEIGHT, COLOR_BACKGROUND);
        graphics.fill(leftPos, topPos, leftPos + BACKGROUND_WIDTH, topPos + 2, COLOR_HEADER);
        graphics.fill(leftPos, topPos + BACKGROUND_HEIGHT - 2, leftPos + BACKGROUND_WIDTH, topPos + BACKGROUND_HEIGHT, COLOR_HEADER);
        graphics.fill(leftPos, topPos, leftPos + 2, topPos + BACKGROUND_HEIGHT, COLOR_HEADER);
        graphics.fill(leftPos + BACKGROUND_WIDTH - 2, topPos, leftPos + BACKGROUND_WIDTH, topPos + BACKGROUND_HEIGHT, COLOR_HEADER);
    }

    private void renderHeader(GuiGraphics graphics) {
        graphics.fill(leftPos + 2, topPos + 2, leftPos + BACKGROUND_WIDTH - 2, topPos + 35, COLOR_HEADER);
        String title = "⛓ GEFÄNGNIS ⛓";
        int titleWidth = font.width(title);
        graphics.drawString(font, title, leftPos + (BACKGROUND_WIDTH - titleWidth) / 2, topPos + 12, COLOR_TEXT, true);
    }

    private void renderCellInfo(GuiGraphics graphics) {
        int y = topPos + 50;

        graphics.fill(leftPos + 20, y, leftPos + BACKGROUND_WIDTH - 20, y + 70, 0xFF2a2a2a);

        String cellText = "ZELLE " + cellNumber;
        int cellWidth = font.width(cellText);
        graphics.drawString(font, cellText, leftPos + (BACKGROUND_WIDTH - cellWidth) / 2, y + 8, COLOR_TEXT_GOLD, true);

        int barX = leftPos + 40;
        int barY = y + 28;
        int barWidth = BACKGROUND_WIDTH - 80;
        int barHeight = 12;

        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, COLOR_PROGRESS_BG);

        long currentTick = getCurrentGameTime();
        long remainingTicks = Math.max(0, releaseTime - currentTick);
        float progress = 1.0f - ((float) remainingTicks / (float) totalSentenceTicks);
        progress = Math.max(0, Math.min(1, progress));

        int progressWidth = (int) (barWidth * progress);
        graphics.fill(barX, barY, barX + progressWidth, barY + barHeight, COLOR_PROGRESS_FILL);

        int remainingSeconds = (int) (remainingTicks / 20);
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String timeText = String.format("%d:%02d VERBLEIBEND", minutes, seconds);
        int timeWidth = font.width(timeText);
        graphics.drawString(font, timeText, leftPos + (BACKGROUND_WIDTH - timeWidth) / 2, y + 48, COLOR_TEXT, true);
    }

    private void renderBailSection(GuiGraphics graphics) {
        int y = topPos + 130;

        graphics.fill(leftPos + 20, y, leftPos + BACKGROUND_WIDTH - 20, y + 1, 0xFF555555);

        y += 10;
        graphics.drawString(font, "KAUTION", leftPos + 25, y, COLOR_TEXT_GOLD, true);

        y += 18;
        graphics.drawString(font, "Betrag:", leftPos + 25, y, COLOR_TEXT_GRAY, false);
        graphics.drawString(font, String.format("%.0f€", bailAmount), leftPos + 120, y, COLOR_TEXT, false);

        y += 14;
        graphics.drawString(font, "Dein Konto:", leftPos + 25, y, COLOR_TEXT_GRAY, false);
        int balanceColor = playerBalance >= bailAmount ? COLOR_TEXT_GREEN : COLOR_TEXT_RED;
        graphics.drawString(font, String.format("%.0f€", playerBalance), leftPos + 120, y, balanceColor, false);

        y += 14;
        long currentTick = getCurrentGameTime();

        if (bailAvailable || currentTick >= bailAvailableAtTick) {
            bailAvailable = true;
            graphics.drawString(font, "Status:", leftPos + 25, y, COLOR_TEXT_GRAY, false);
            graphics.drawString(font, "✓ Verfügbar!", leftPos + 120, y, COLOR_TEXT_GREEN, false);
        } else {
            long waitTicks = bailAvailableAtTick - currentTick;
            int waitSeconds = (int) (waitTicks / 20);
            int waitMin = waitSeconds / 60;
            int waitSec = waitSeconds % 60;

            graphics.drawString(font, "Status:", leftPos + 25, y, COLOR_TEXT_GRAY, false);
            graphics.drawString(font, String.format("⏳ Noch %d:%02d warten...", waitMin, waitSec),
                leftPos + 120, y, COLOR_TEXT_GRAY, false);
        }
    }

    private void renderHintText(GuiGraphics graphics) {
        int y = topPos + BACKGROUND_HEIGHT - 35;

        String hint1 = "Du kannst dieses Fenster nicht schließen.";
        String hint2 = "Warte deine Strafe ab oder zahle Kaution.";

        int hint1Width = font.width(hint1);
        int hint2Width = font.width(hint2);

        graphics.drawString(font, hint1, leftPos + (BACKGROUND_WIDTH - hint1Width) / 2, y, COLOR_TEXT_GRAY, false);
        graphics.drawString(font, hint2, leftPos + (BACKGROUND_WIDTH - hint2Width) / 2, y + 12, COLOR_TEXT_GRAY, false);
    }

    @Override
    public void tick() {
        super.tick();

        long currentTick = getCurrentGameTime();
        if (currentTick >= releaseTime) {
            allowClose();
        }

        if (!bailAvailable && currentTick >= bailAvailableAtTick) {
            bailAvailable = true;
        }
        updateBailButton();
    }

    private void updateBailButton() {
        if (bailButton != null) {
            boolean canAfford = playerBalance >= bailAmount;
            bailButton.active = bailAvailable && canAfford;

            if (!bailAvailable) {
                bailButton.setMessage(Component.translatable("gui.common.not_yet_available"));
            } else if (!canAfford) {
                bailButton.setMessage(Component.literal("§c✗ Nicht genug Geld"));
            } else {
                bailButton.setMessage(Component.literal("§a✓ KAUTION ZAHLEN"));
            }
        }
    }

    private void onBailButtonClick(Button button) {
        if (!bailAvailable || playerBalance < bailAmount) {
            return;
        }

        PrisonNetworkHandler.sendToServer(new PayBailPacket());

        button.active = false;
        button.setMessage(Component.literal("§7Verarbeite..."));
    }

    public void allowClose() {
        this.canClose = true;
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    public void updateBalance(double newBalance) {
        this.playerBalance = newBalance;
        updateBailButton();
    }

    private long getCurrentGameTime() {
        if (minecraft != null && minecraft.level != null) {
            return minecraft.level.getGameTime();
        }
        return 0;
    }

    public static void open(int cellNumber, long totalSentenceTicks, long releaseTime,
                           double bailAmount, double playerBalance, long bailAvailableAtTick) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new PrisonScreen(
            cellNumber, totalSentenceTicks, releaseTime,
            bailAmount, playerBalance, bailAvailableAtTick
        ));
    }
}
