package de.rolandsw.schedulemc.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Bestätigungs-Dialog für kritische Aktionen
 */
@OnlyIn(Dist.CLIENT)
public class ConfirmDialogScreen extends Screen {

    private final Screen parent;
    private final String title;
    private final String message;
    private final String warningText;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public ConfirmDialogScreen(Screen parent, String title, String message, String warningText,
                              Runnable onConfirm, Runnable onCancel) {
        super(Component.literal(title));
        this.parent = parent;
        this.title = title;
        this.message = message;
        this.warningText = warningText;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    public ConfirmDialogScreen(Screen parent, String title, String message,
                              Runnable onConfirm) {
        this(parent, title, message, null, onConfirm, null);
    }

    @Override
    protected void init() {
        super.init();

        int dialogWidth = 260;
        int dialogHeight = warningText != null ? 140 : 110;
        int leftPos = (this.width - dialogWidth) / 2;
        int topPos = (this.height - dialogHeight) / 2;

        int buttonY = topPos + dialogHeight - 30;

        // Ja-Button (Rot für kritische Aktionen)
        addRenderableWidget(Button.builder(Component.literal("§c✓ Ja, fortfahren"), button -> {
            onConfirm.run();
            if (minecraft != null) {
                minecraft.setScreen(parent);
            }
        }).bounds(leftPos + 20, buttonY, 100, 20).build());

        // Nein-Button
        addRenderableWidget(Button.builder(Component.literal("✗ Abbrechen"), button -> {
            if (onCancel != null) {
                onCancel.run();
            }
            if (minecraft != null) {
                minecraft.setScreen(parent);
            }
        }).bounds(leftPos + 140, buttonY, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        int dialogWidth = 260;
        int dialogHeight = warningText != null ? 140 : 110;
        int leftPos = (this.width - dialogWidth) / 2;
        int topPos = (this.height - dialogHeight) / 2;

        // Dialog-Box
        guiGraphics.fill(leftPos - 2, topPos - 2, leftPos + dialogWidth + 2, topPos + dialogHeight + 2, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + dialogWidth, topPos + dialogHeight, 0xFF2A2A2A);

        // Titel (Rot für Warnung)
        guiGraphics.fill(leftPos, topPos, leftPos + dialogWidth, topPos + 25, 0xFF3A0000);
        guiGraphics.drawCenteredString(this.font, "§c§l" + title, leftPos + dialogWidth / 2, topPos + 8, 0xFFFFFF);

        // Nachricht
        int y = topPos + 35;
        for (String line : message.split("\n")) {
            guiGraphics.drawCenteredString(this.font, "§f" + line, leftPos + dialogWidth / 2, y, 0xFFFFFF);
            y += 12;
        }

        // Warnung (falls vorhanden)
        if (warningText != null) {
            y += 5;
            guiGraphics.fill(leftPos + 10, y, leftPos + dialogWidth - 10, y + 25, 0x44AA0000);
            guiGraphics.drawCenteredString(this.font, "§c⚠ " + warningText, leftPos + dialogWidth / 2, y + 8, 0xFF5555);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
