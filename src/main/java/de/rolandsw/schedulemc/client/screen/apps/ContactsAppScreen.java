package de.rolandsw.schedulemc.client.screen.apps;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Kontakte App - Kontaktliste anzeigen
 */
@OnlyIn(Dist.CLIENT)
public class ContactsAppScreen extends Screen {

    private final Screen parentScreen;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 288; // Reduziert von 320 (10% kleiner)
    private static final int BORDER_SIZE = 5; // Rahmen um das Smartphone
    private static final int MARGIN = 10; // Mindestabstand vom Bildschirmrand
    private int leftPos;
    private int topPos;

    public ContactsAppScreen(Screen parent) {
        super(Component.literal("Kontakte"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;

        // Zentriere vertikal mit Margin-Check
        int centeredTop = (this.height - HEIGHT) / 2;
        int minTop = MARGIN + BORDER_SIZE;
        int maxTop = this.height - HEIGHT - BORDER_SIZE - MARGIN;
        this.topPos = Math.max(minTop, Math.min(centeredTop, maxTop));

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.literal("← Zurück"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Smartphone-Hintergrund
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF2A2A2A);

        // Header
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 30, 0xFF1A1A1A);
        guiGraphics.drawCenteredString(this.font, "§6§lKontakte", leftPos + WIDTH / 2, topPos + 12, 0xFFFFFF);

        // Content-Bereich
        int contentY = topPos + 40;
        guiGraphics.drawCenteredString(this.font, "§7Meine Kontakte", leftPos + WIDTH / 2, contentY, 0xFFFFFF);
        contentY += 20;

        // Platzhalter für Kontakte
        guiGraphics.drawString(this.font, "§8Gespeicherte Kontakte:", leftPos + 20, contentY, 0xAAAAAA);
        contentY += 15;
        guiGraphics.drawString(this.font, "§7• Kontakt 1", leftPos + 25, contentY, 0xFFFFFF);
        contentY += 12;
        guiGraphics.drawString(this.font, "§7• Kontakt 2", leftPos + 25, contentY, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
