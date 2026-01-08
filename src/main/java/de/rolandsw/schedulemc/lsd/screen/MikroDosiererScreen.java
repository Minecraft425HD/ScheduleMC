package de.rolandsw.schedulemc.lsd.screen;

import de.rolandsw.schedulemc.lsd.LSDDosage;
import de.rolandsw.schedulemc.lsd.menu.MikroDosiererMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für den Mikro-Dosierer
 * Mit Dosierungs-Slider (50-300 μg)
 */
public class MikroDosiererScreen extends AbstractContainerScreen<MikroDosiererMenu> {

    private static final int GUI_WIDTH = 220;
    private static final int GUI_HEIGHT = 160;

    // Slider
    private static final int SLIDER_X = 30;
    private static final int SLIDER_Y = 50;
    private static final int SLIDER_WIDTH = 160;
    private static final int SLIDER_HEIGHT = 20;

    // Start Button
    private static final int BUTTON_X = 70;
    private static final int BUTTON_Y = 110;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 25;

    private boolean isDraggingSlider = false;

    public MikroDosiererScreen(MikroDosiererMenu menu, Inventory playerInventory, Component title) {
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

        // Haupthintergrund
        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF1A1A2E);
        graphics.fill(x + 3, y + 3, x + GUI_WIDTH - 3, y + GUI_HEIGHT - 3, 0xFF2D2D44);

        // Header
        graphics.fill(x + 3, y + 3, x + GUI_WIDTH - 3, y + 22, 0xFF16213E);

        // Slider rendern
        renderSlider(graphics, x + SLIDER_X, y + SLIDER_Y, mouseX, mouseY);

        // Dosierungs-Info rendern
        renderDosageInfo(graphics, x, y);

        // Start-Button rendern
        renderStartButton(graphics, x + BUTTON_X, y + BUTTON_Y, mouseX, mouseY);
    }

    private void renderSlider(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        // Slider Hintergrund mit Zonen
        graphics.fill(x - 1, y - 1, x + SLIDER_WIDTH + 1, y + SLIDER_HEIGHT + 1, 0xFF555577);
        graphics.fill(x, y, x + SLIDER_WIDTH, y + SLIDER_HEIGHT, 0xFF3D3D5C);

        // Zonen-Markierungen (Schwach, Standard, Stark, Bicycle Day)
        int zoneWidth = SLIDER_WIDTH / 4;

        // Schwach (Grau)
        graphics.fill(x, y + 2, x + zoneWidth, y + SLIDER_HEIGHT - 2, 0xFF777777);
        // Standard (Grün)
        graphics.fill(x + zoneWidth, y + 2, x + zoneWidth * 2, y + SLIDER_HEIGHT - 2, 0xFF55AA55);
        // Stark (Gelb)
        graphics.fill(x + zoneWidth * 2, y + 2, x + zoneWidth * 3, y + SLIDER_HEIGHT - 2, 0xFFAAAA55);
        // Bicycle Day (Magenta)
        graphics.fill(x + zoneWidth * 3, y + 2, x + SLIDER_WIDTH, y + SLIDER_HEIGHT - 2, 0xFFAA55AA);

        // Slider-Handle
        int sliderValue = menu.getSliderValue();
        int handleX = x + (int) ((sliderValue / 100.0) * (SLIDER_WIDTH - 10));

        graphics.fill(handleX - 1, y - 3, handleX + 11, y + SLIDER_HEIGHT + 3, 0xFFFFFFFF);
        graphics.fill(handleX, y - 2, handleX + 10, y + SLIDER_HEIGHT + 2, 0xFFDDDDDD);

        // Labels unter dem Slider
        graphics.drawString(this.font, "50μg", x, y + SLIDER_HEIGHT + 5, 0x888888, false);
        graphics.drawString(this.font, "175μg", x + SLIDER_WIDTH / 2 - 15, y + SLIDER_HEIGHT + 5, 0x888888, false);
        graphics.drawString(this.font, "300μg", x + SLIDER_WIDTH - 25, y + SLIDER_HEIGHT + 5, 0x888888, false);
    }

    private void renderDosageInfo(GuiGraphics graphics, int x, int y) {
        int micrograms = menu.getMicrograms();
        LSDDosage dosage = menu.getCurrentDosage();

        // Info-Box
        int infoY = y + 85;
        graphics.fill(x + 30, infoY, x + GUI_WIDTH - 30, infoY + 20, 0xFF16213E);

        // Dosierung anzeigen
        String dosageText = micrograms + "μg - " + dosage.getDisplayName();
        int textColor = switch (dosage) {
            case SCHWACH -> 0xAAAAAA;
            case STANDARD -> 0x55FF55;
            case STARK -> 0xFFFF55;
            case BICYCLE_DAY -> 0xFF55FF;
        };

        int textWidth = this.font.width(dosageText);
        graphics.drawString(this.font, dosageText, x + GUI_WIDTH / 2 - textWidth / 2, infoY + 6, textColor, true);

        // Lysergsäure-Anzahl
        graphics.drawString(this.font, Component.translatable("gui.mikro_dosierer.lysergic_acid", menu.getLysergsaeureCount()).getString(),
                x + 30, y + 140, 0xFFFFFF, false);
    }

    private void renderStartButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + BUTTON_WIDTH &&
                          mouseY >= y && mouseY < y + BUTTON_HEIGHT;
        boolean canStart = menu.getLysergsaeureCount() > 0 && !menu.isProcessing();

        int buttonColor;
        if (menu.isProcessing()) {
            buttonColor = 0xFF444466;
        } else if (canStart && hovered) {
            buttonColor = 0xFF5577AA;
        } else if (canStart) {
            buttonColor = 0xFF446699;
        } else {
            buttonColor = 0xFF333355;
        }

        // Button
        graphics.fill(x - 1, y - 1, x + BUTTON_WIDTH + 1, y + BUTTON_HEIGHT + 1, 0xFF222244);
        graphics.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, buttonColor);

        // Button Text
        String buttonText;
        if (menu.isProcessing()) {
            buttonText = Component.translatable("gui.mikro_dosierer.dosing", menu.getProgressPercent()).getString();
        } else if (canStart) {
            buttonText = Component.translatable("gui.mikro_dosierer.start").getString();
        } else {
            buttonText = Component.translatable("gui.mikro_dosierer.no_lysergic_acid").getString();
        }

        int textWidth = this.font.width(buttonText);
        graphics.drawString(this.font, buttonText, x + BUTTON_WIDTH / 2 - textWidth / 2, y + 8, 0xFFFFFF, false);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;

        // Titel
        graphics.drawString(this.font, Component.translatable("gui.mikro_dosierer.title").getString(), x + 10, y + 8, 0xFFDD88FF, true);

        // Beschreibung
        graphics.drawString(this.font, Component.translatable("gui.mikro_dosierer.select_dosage").getString(), x + 30, y + 35, 0xAAAAAA, false);
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

            // Slider Click
            int sliderX = x + SLIDER_X;
            int sliderY = y + SLIDER_Y;
            if (mouseX >= sliderX && mouseX < sliderX + SLIDER_WIDTH &&
                mouseY >= sliderY && mouseY < sliderY + SLIDER_HEIGHT) {
                isDraggingSlider = true;
                updateSliderFromMouse(mouseX, sliderX);
                return true;
            }

            // Button Click
            int buttonX = x + BUTTON_X;
            int buttonY = y + BUTTON_Y;
            if (mouseX >= buttonX && mouseX < buttonX + BUTTON_WIDTH &&
                mouseY >= buttonY && mouseY < buttonY + BUTTON_HEIGHT) {
                if (menu.getLysergsaeureCount() > 0 && !menu.isProcessing()) {
                    menu.startProcess();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingSlider && button == 0) {
            int sliderX = this.leftPos + SLIDER_X;
            updateSliderFromMouse(mouseX, sliderX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDraggingSlider = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateSliderFromMouse(double mouseX, int sliderX) {
        double relative = (mouseX - sliderX) / SLIDER_WIDTH;
        int value = (int) (Math.max(0, Math.min(1, relative)) * 100);
        menu.setSliderValue(value);
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
