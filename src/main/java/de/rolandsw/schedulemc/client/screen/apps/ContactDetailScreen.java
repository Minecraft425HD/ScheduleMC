package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.messaging.Conversation;
import de.rolandsw.schedulemc.messaging.HeadRenderer;
import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.player.PlayerTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Contact Detail Screen - Shows options for a specific contact
 */
@OnlyIn(Dist.CLIENT)
public class ContactDetailScreen extends Screen {

    private final Screen parentScreen;
    private final PlayerTracker.PlayerContact contact;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 240;
    private static final int MARGIN_TOP = 5;
    private int leftPos;
    private int topPos;

    public ContactDetailScreen(Screen parent, PlayerTracker.PlayerContact contact) {
        super(Component.literal("Kontakt"));
        this.parentScreen = parent;
        this.contact = contact;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = MARGIN_TOP;

        int buttonY = topPos + 140;
        int buttonWidth = WIDTH - 40;

        // Option 1: Nachricht senden (Opens chat)
        addRenderableWidget(Button.builder(Component.translatable("gui.app.contact.message"), button -> {
            if (minecraft != null && minecraft.player != null) {
                // Get or create conversation
                Conversation conversation = MessageManager.getOrCreateConversation(
                    minecraft.player.getUUID(),
                    contact.getUuid(),
                    contact.getName(),
                    true // isPlayerParticipant
                );

                // Open chat screen
                minecraft.setScreen(new ChatScreen(this, conversation));
            }
        }).bounds(leftPos + 20, buttonY, buttonWidth, 30).build());

        // Option 2: Spieler Info (Placeholder)
        addRenderableWidget(Button.builder(Component.translatable("gui.app.contact.info"), button -> {
            // Placeholder for future feature
        }).bounds(leftPos + 20, buttonY + 40, buttonWidth, 30).build());

        // Option 3: Zu Favoriten hinzufügen (Placeholder)
        addRenderableWidget(Button.builder(Component.translatable("gui.app.contact.favorite"), button -> {
            // Placeholder for future feature
        }).bounds(leftPos + 20, buttonY + 80, buttonWidth, 30).build());

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.translatable("gui.app.back"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Smartphone border (dark frame)
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);

        // White background
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFFFFFFFF);

        // Header (iOS-style)
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 35, 0xFFF8F8F8);
        guiGraphics.drawString(this.font, "§0§lKontakt", leftPos + 10, topPos + 13, 0x000000, false);

        // Profile section
        int profileY = topPos + 50;

        // Large profile picture
        int headSize = 64;
        int headX = leftPos + (WIDTH - headSize) / 2;
        HeadRenderer.renderPlayerHead(guiGraphics, headX, profileY, headSize, contact.getUuid());

        // Name
        int nameY = profileY + headSize + 10;
        guiGraphics.drawCenteredString(this.font, "§0§l" + contact.getName(),
            leftPos + WIDTH / 2, nameY, 0x000000);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
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
