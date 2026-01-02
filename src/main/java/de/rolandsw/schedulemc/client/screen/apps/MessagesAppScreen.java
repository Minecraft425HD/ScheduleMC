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

        // Smartphone border (dark frame)
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);

        // WhatsApp background (light gray/white)
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFFECE5DD);

        // WhatsApp green header (#075E54)
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 35, 0xFF075E54);

        // Header title in white - no shadow
        guiGraphics.drawString(this.font, "WhatsApp", leftPos + 10, topPos + 13, 0xFFFFFFFF, false);

        // Content area
        int contentY = topPos + 40;
        int contentHeight = HEIGHT - 80;

        if (conversations == null || conversations.isEmpty()) {
            // Empty state
            guiGraphics.fill(leftPos, contentY, leftPos + WIDTH, contentY + 60, 0xFFFFFFFF);
            guiGraphics.drawCenteredString(this.font, "§8Keine Chats", leftPos + WIDTH / 2, contentY + 20, 0xFF666666);
            guiGraphics.drawCenteredString(this.font, "§7Tippe einen Spieler an,", leftPos + WIDTH / 2, contentY + 32, 0xFF999999);
            guiGraphics.drawCenteredString(this.font, "§7um zu chatten", leftPos + WIDTH / 2, contentY + 42, 0xFF999999);
        } else {
            // Render conversation list
            for (int i = 0; i < conversations.size(); i++) {
                int itemY = contentY + (i * CHAT_ITEM_HEIGHT) - scrollOffset;

                // Skip if not visible
                if (itemY + CHAT_ITEM_HEIGHT < contentY || itemY > contentY + contentHeight) {
                    continue;
                }

                renderConversationItem(guiGraphics, conversations.get(i), leftPos, itemY, mouseX, mouseY);
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderConversationItem(GuiGraphics guiGraphics, Conversation conversation, int x, int y, int mouseX, int mouseY) {
        int itemWidth = WIDTH;

        // Check if mouse is hovering
        boolean isHovering = mouseX >= x && mouseX <= x + itemWidth && mouseY >= y && mouseY <= y + CHAT_ITEM_HEIGHT;

        // WhatsApp-style background (white with hover)
        int bgColor = isHovering ? 0xFFF5F5F5 : 0xFFFFFFFF;
        guiGraphics.fill(x, y, x + itemWidth, y + CHAT_ITEM_HEIGHT, bgColor);

        // Bottom border line (light gray)
        guiGraphics.fill(x + 70, y + CHAT_ITEM_HEIGHT - 1, x + itemWidth, y + CHAT_ITEM_HEIGHT, 0xFFE0E0E0);

        // Profile picture
        int headSize = 32;
        int headX = x + 12;
        int headY = y + (CHAT_ITEM_HEIGHT - headSize) / 2;

        // Render head directly without circle background
        HeadRenderer.renderPlayerHead(guiGraphics, headX, headY, headSize, null);

        // Text area
        int textX = headX + headSize + 12;
        int nameY = y + 10;
        int previewY = y + 26;
        int timeY = y + 10;

        // Name (black, bold) - no shadow
        String displayName = conversation.getParticipantName();
        if (displayName.length() > 15) {
            displayName = displayName.substring(0, 12) + "...";
        }
        guiGraphics.drawString(this.font, displayName, textX, nameY, 0xFF000000, false);

        // Message preview (gray) - no shadow
        String preview = conversation.getPreviewText();
        if (preview.length() > 25) {
            preview = preview.substring(0, 22) + "...";
        }
        guiGraphics.drawString(this.font, preview, textX, previewY, 0xFF667781, false);

        // Timestamp (top right, small gray text) - no shadow
        String timeStr = getTimeString(conversation.getLastMessageTime());
        int timeWidth = this.font.width(timeStr);
        guiGraphics.drawString(this.font, timeStr, x + itemWidth - timeWidth - 10, timeY, 0xFF667781, false);
    }

    private String getTimeString(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d";
        } else if (hours > 0) {
            return hours + "h";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return "now";
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && conversations != null) { // Left click
            int contentY = topPos + 40;

            for (int i = 0; i < conversations.size(); i++) {
                int itemY = contentY + (i * CHAT_ITEM_HEIGHT) - scrollOffset;
                int itemX = leftPos;
                int itemWidth = WIDTH;

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
