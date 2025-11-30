package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.messaging.Conversation;
import de.rolandsw.schedulemc.messaging.HeadRenderer;
import de.rolandsw.schedulemc.messaging.MessageManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Nachrichten App - WhatsApp-style chat list
 */
@OnlyIn(Dist.CLIENT)
public class MessagesAppScreen extends Screen {

    private final Screen parentScreen;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 240;
    private static final int BORDER_SIZE = 5;
    private static final int MARGIN_TOP = 15;
    private static final int MARGIN_BOTTOM = 60;
    private static final int CHAT_ITEM_HEIGHT = 50;
    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;
    private List<Conversation> conversations;

    public MessagesAppScreen(Screen parent) {
        super(Component.literal("Nachrichten"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;

        // Zentriere vertikal mit Margin-Check
        int centeredTop = (this.height - HEIGHT) / 2;
        int minTop = MARGIN_TOP + BORDER_SIZE;
        int maxTop = this.height - HEIGHT - BORDER_SIZE - MARGIN_BOTTOM;
        this.topPos = Math.max(minTop, Math.min(centeredTop, maxTop));

        // Load conversations
        if (minecraft != null && minecraft.player != null) {
            conversations = MessageManager.getConversations(minecraft.player.getUUID());
        }

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
        guiGraphics.drawCenteredString(this.font, "§6§lNachrichten", leftPos + WIDTH / 2, topPos + 12, 0xFFFFFF);

        // Content area
        int contentY = topPos + 40;
        int contentHeight = HEIGHT - 80; // Space for header and back button

        if (conversations == null || conversations.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, "§7Keine Nachrichten", leftPos + WIDTH / 2, contentY + 40, 0xFFFFFF);
        } else {
            // Render conversation list (WhatsApp style)
            for (int i = 0; i < conversations.size(); i++) {
                int itemY = contentY + (i * CHAT_ITEM_HEIGHT) - scrollOffset;

                // Skip if not visible
                if (itemY + CHAT_ITEM_HEIGHT < contentY || itemY > contentY + contentHeight) {
                    continue;
                }

                renderConversationItem(guiGraphics, conversations.get(i), leftPos + 5, itemY, mouseX, mouseY);
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderConversationItem(GuiGraphics guiGraphics, Conversation conversation, int x, int y, int mouseX, int mouseY) {
        int itemWidth = WIDTH - 10;

        // Check if mouse is hovering
        boolean isHovering = mouseX >= x && mouseX <= x + itemWidth && mouseY >= y && mouseY <= y + CHAT_ITEM_HEIGHT;

        // Background
        int bgColor = isHovering ? 0xFF3A3A3A : 0xFF2A2A2A;
        guiGraphics.fill(x, y, x + itemWidth, y + CHAT_ITEM_HEIGHT, bgColor);

        // Separator line
        guiGraphics.fill(x, y + CHAT_ITEM_HEIGHT - 1, x + itemWidth, y + CHAT_ITEM_HEIGHT, 0xFF1A1A1A);

        // Profile picture (head)
        int headSize = 32;
        int headX = x + 8;
        int headY = y + (CHAT_ITEM_HEIGHT - headSize) / 2;

        if (conversation.isPlayerParticipant()) {
            // Render player head
            HeadRenderer.renderPlayerHead(guiGraphics, headX, headY, headSize, null);
        } else {
            // Render NPC head (we'll need to get skin filename from NPC data)
            HeadRenderer.renderPlayerHead(guiGraphics, headX, headY, headSize, null);
        }

        // Name and message preview
        int textX = headX + headSize + 8;
        int nameY = y + 8;
        int previewY = y + 24;

        // Name
        String displayName = conversation.getParticipantName();
        guiGraphics.drawString(this.font, "§f§l" + displayName, textX, nameY, 0xFFFFFF);

        // Message preview
        String preview = conversation.getPreviewText();
        guiGraphics.drawString(this.font, "§7" + preview, textX, previewY, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && conversations != null) { // Left click
            int contentY = topPos + 40;
            int contentHeight = HEIGHT - 80;

            for (int i = 0; i < conversations.size(); i++) {
                int itemY = contentY + (i * CHAT_ITEM_HEIGHT) - scrollOffset;
                int itemX = leftPos + 5;
                int itemWidth = WIDTH - 10;

                if (mouseX >= itemX && mouseX <= itemX + itemWidth &&
                    mouseY >= itemY && mouseY <= itemY + CHAT_ITEM_HEIGHT) {

                    // Open chat screen for this conversation
                    if (minecraft != null) {
                        minecraft.setScreen(new ChatScreen(this, conversations.get(i)));
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (conversations != null && conversations.size() * CHAT_ITEM_HEIGHT > HEIGHT - 80) {
            scrollOffset -= (int)(delta * 10);
            scrollOffset = Math.max(0, Math.min(scrollOffset, conversations.size() * CHAT_ITEM_HEIGHT - (HEIGHT - 80)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
