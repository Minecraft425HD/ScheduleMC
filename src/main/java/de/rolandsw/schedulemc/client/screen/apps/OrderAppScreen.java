package de.rolandsw.schedulemc.client.screen.apps;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Bestellung App - Bestellungen verwalten
 */
@OnlyIn(Dist.CLIENT)
public class OrderAppScreen extends Screen {

    private final Screen parentScreen;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 240; // Reduziert von 320 (10% kleiner)
    private static final int BORDER_SIZE = 5; // Rahmen um das Smartphone
    private static final int MARGIN_TOP = 5;
    private static final int MARGIN_BOTTOM = 60; // Mindestabstand vom Bildschirmrand
    private int leftPos;
    private int topPos;

    // PERFORMANCE: Cache static translatable strings
    private String cachedTitle;
    private String cachedMyOrders;
    private String cachedActiveOrders;
    private String cachedNoOrders;

    public OrderAppScreen(Screen parent) {
        super(Component.translatable("gui.app.order.title"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;

        // Positioniere oben mit Margin
        this.topPos = MARGIN_TOP;

        // Cache translatable strings
        this.cachedTitle = Component.translatable("gui.app.order.title").getString();
        this.cachedMyOrders = Component.translatable("gui.app.order.my_orders").getString();
        this.cachedActiveOrders = Component.translatable("gui.app.order.active_orders").getString();
        this.cachedNoOrders = Component.translatable("gui.app.order.no_orders").getString();

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.translatable("gui.app.order.back"), button -> {
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
        guiGraphics.drawCenteredString(this.font, "\u00a76\u00a7l" + cachedTitle, leftPos + WIDTH / 2, topPos + 12, 0xFFFFFF);

        // Content-Bereich
        int contentY = topPos + 40;
        guiGraphics.drawCenteredString(this.font, cachedMyOrders, leftPos + WIDTH / 2, contentY, 0xFFFFFF);
        contentY += 20;

        // Platzhalter für Bestellungen
        guiGraphics.drawString(this.font, cachedActiveOrders, leftPos + 20, contentY, 0xAAAAAA);
        contentY += 15;
        guiGraphics.drawString(this.font, cachedNoOrders, leftPos + 25, contentY, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
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
