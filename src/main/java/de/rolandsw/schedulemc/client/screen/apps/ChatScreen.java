package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.messaging.Conversation;
import de.rolandsw.schedulemc.messaging.HeadRenderer;
import de.rolandsw.schedulemc.messaging.Message;
import de.rolandsw.schedulemc.messaging.NPCMessageTemplates;
import de.rolandsw.schedulemc.messaging.network.MessageNetworkHandler;
import de.rolandsw.schedulemc.messaging.network.SendMessagePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Individual chat screen for a conversation (WhatsApp-style)
 */
@OnlyIn(Dist.CLIENT)
public class ChatScreen extends Screen {

    private final Screen parentScreen;
    private final Conversation conversation;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 240;
    private static final int BORDER_SIZE = 5;
    private static final int MARGIN_TOP = 5;
    private static final int MARGIN_BOTTOM = 60;
    private static final int MESSAGE_HEIGHT = 30;
    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;
    private EditBox messageInput;
    private List<Component> npcMessageOptions;
    private int selectedNPCMessage = -1;

    public ChatScreen(Screen parent, Conversation conversation) {
        super(Component.literal("Chat"));
        this.parentScreen = parent;
        this.conversation = conversation;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;

        // Positioniere oben mit Margin
        this.topPos = MARGIN_TOP;

        // Check if chatting with NPC to show message templates
        if (!conversation.isPlayerParticipant()) {
            // NPC chat - show 3 message options based on reputation with this NPC
            int reputation = conversation.getReputation();
            npcMessageOptions = NPCMessageTemplates.getMessagesForReputation(reputation);

            int buttonY = topPos + HEIGHT - 95;
            for (int i = 0; i < npcMessageOptions.size(); i++) {
                final int index = i;
                Component messageComponent = npcMessageOptions.get(i);
                String messageText = messageComponent.getString();

                addRenderableWidget(Button.builder(Component.literal(truncate(messageText, 25)), button -> {
                    selectedNPCMessage = index;
                    sendMessage(npcMessageOptions.get(index).getString());
                }).bounds(leftPos + 5, buttonY + (i * 22), WIDTH - 10, 20).build());
            }
        } else {
            // Player chat - show text input
            messageInput = new EditBox(this.font, leftPos + 10, topPos + HEIGHT - 55, WIDTH - 20, 20,
                Component.literal("Nachricht..."));
            messageInput.setMaxLength(100);
            addRenderableWidget(messageInput);

            // Send button
            addRenderableWidget(Button.builder(Component.literal("Senden"), button -> {
                if (messageInput != null && !messageInput.getValue().isEmpty()) {
                    sendMessage(messageInput.getValue());
                    messageInput.setValue("");
                }
            }).bounds(leftPos + WIDTH - 70, topPos + HEIGHT - 80, 60, 20).build());
        }

        // Back button
        addRenderableWidget(Button.builder(Component.translatable("gui.achievement_app.back"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());
    }

    private String truncate(String text, int maxLength) {
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }

    private void sendMessage(String content) {
        if (minecraft != null && minecraft.player != null) {
            // Send message via network packet to server
            MessageNetworkHandler.sendToServer(new SendMessagePacket(
                conversation.getParticipantUUID(),
                conversation.getParticipantName(),
                conversation.isPlayerParticipant(),
                content
            ));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Smartphone border (dark frame)
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);

        // WhatsApp chat wallpaper (light beige/cream)
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFFE7DDD3);

        // WhatsApp green header (#075E54)
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 40, 0xFF075E54);

        // Back arrow
        guiGraphics.drawString(this.font, "←", leftPos + 8, topPos + 14, 0xFFFFFFFF, false);

        // Profile picture in header
        int headSize = 28;
        int headX = leftPos + 25;
        int headY = topPos + 6;

        // Render head directly without circle background
        HeadRenderer.renderPlayerHead(guiGraphics, headX, headY, headSize, (ResourceLocation) null);

        // Name (white, bold) - no shadow
        String displayName = conversation.getParticipantName();
        guiGraphics.drawString(this.font, displayName, headX + headSize + 8, topPos + 12, 0xFFFFFFFF, false);

        // Online status (small text below name) - no shadow
        guiGraphics.drawString(this.font, "online", headX + headSize + 8, topPos + 24, 0xFFCCCCCC, false);

        // Messages area
        int messagesY = topPos + 45;
        int messagesHeight = conversation.isPlayerParticipant() ? 120 : 80;

        renderMessages(guiGraphics, messagesY, messagesHeight);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderMessages(GuiGraphics guiGraphics, int startY, int height) {
        List<Message> messages = conversation.getMessages();

        if (messages.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, "§7Keine Nachrichten", leftPos + WIDTH / 2, startY + 20, 0xFFFFFF);
            return;
        }

        int currentY = startY + height - MESSAGE_HEIGHT - scrollOffset;

        // Render from bottom to top (most recent at bottom)
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);

            if (currentY + MESSAGE_HEIGHT >= startY && currentY <= startY + height) {
                renderMessage(guiGraphics, msg, currentY);
            }

            currentY -= MESSAGE_HEIGHT;
        }
    }

    private void renderMessage(GuiGraphics guiGraphics, Message message, int y) {
        boolean isSentByMe = minecraft != null && minecraft.player != null &&
            message.getSenderUUID().equals(minecraft.player.getUUID());

        String content = message.getContent();
        int maxWidth = WIDTH - 70;

        // Word wrap
        List<String> lines = wrapText(content, maxWidth);

        // Calculate bubble size
        int maxLineWidth = 0;
        for (String line : lines) {
            maxLineWidth = Math.max(maxLineWidth, this.font.width(line));
        }

        int padding = 8;
        int msgWidth = Math.min(maxWidth, maxLineWidth + padding * 2 + 20); // Extra space for timestamp
        int msgHeight = lines.size() * 10 + padding * 2 + 6; // Extra space for timestamp

        // Get timestamp
        String timeStr = getMessageTime(message.getTimestamp());
        int timeWidth = this.font.width(timeStr);

        if (isSentByMe) {
            // Right-aligned (sent messages) - WhatsApp green (#DCF8C6)
            int msgX = leftPos + WIDTH - msgWidth - 8;

            // Message bubble with rounded corner effect
            drawRoundedBubble(guiGraphics, msgX, y, msgWidth, msgHeight, 0xFFDCF8C6);

            // Message text (black) - no shadow
            for (int i = 0; i < lines.size(); i++) {
                guiGraphics.drawString(this.font, lines.get(i), msgX + padding, y + padding + (i * 10), 0xFF000000, false);
            }

            // Timestamp (bottom right, small, gray) - no shadow
            guiGraphics.drawString(this.font, timeStr,
                msgX + msgWidth - timeWidth - padding,
                y + msgHeight - 10, 0xFF888888, false);

        } else {
            // Left-aligned (received messages) - White
            int msgX = leftPos + 8;

            // Message bubble with rounded corner effect
            drawRoundedBubble(guiGraphics, msgX, y, msgWidth, msgHeight, 0xFFFFFFFF);

            // Message text (black) - no shadow
            for (int i = 0; i < lines.size(); i++) {
                guiGraphics.drawString(this.font, lines.get(i), msgX + padding, y + padding + (i * 10), 0xFF000000, false);
            }

            // Timestamp (bottom right, small, gray) - no shadow
            guiGraphics.drawString(this.font, timeStr,
                msgX + msgWidth - timeWidth - padding,
                y + msgHeight - 10, 0xFF888888, false);
        }
    }

    private void drawRoundedBubble(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        // Main rectangle
        guiGraphics.fill(x + 2, y, x + width - 2, y + height, color);
        guiGraphics.fill(x, y + 2, x + width, y + height - 2, color);

        // Corners (simple approximation)
        guiGraphics.fill(x + 1, y + 1, x + 2, y + 2, color);
        guiGraphics.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
        guiGraphics.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
        guiGraphics.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
    }

    private String getMessageTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%02d:%02d", hours % 24, minutes % 60);
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return "now";
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;

            if (this.font.width(testLine) <= maxWidth) {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines.isEmpty() ? List.of(text) : lines;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int totalHeight = conversation.getMessages().size() * MESSAGE_HEIGHT;
        int visibleHeight = conversation.isPlayerParticipant() ? 120 : 80;

        if (totalHeight > visibleHeight) {
            scrollOffset -= (int)(delta * 10);
            scrollOffset = Math.max(0, Math.min(scrollOffset, totalHeight - visibleHeight));
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
