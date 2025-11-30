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
    private static final int MARGIN_TOP = 15;
    private static final int MARGIN_BOTTOM = 60;
    private static final int MESSAGE_HEIGHT = 30;
    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;
    private EditBox messageInput;
    private List<String> npcMessageOptions;
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

        int centeredTop = (this.height - HEIGHT) / 2;
        int minTop = MARGIN_TOP + BORDER_SIZE;
        int maxTop = this.height - HEIGHT - BORDER_SIZE - MARGIN_BOTTOM;
        this.topPos = Math.max(minTop, Math.min(centeredTop, maxTop));

        // Check if chatting with NPC to show message templates
        if (!conversation.isPlayerParticipant()) {
            // NPC chat - show 3 message options
            // TODO: Get actual reputation from NPCBusinessMetrics
            int reputation = 50; // Default neutral reputation
            npcMessageOptions = NPCMessageTemplates.getMessagesForReputation(reputation);

            int buttonY = topPos + HEIGHT - 95;
            for (int i = 0; i < npcMessageOptions.size(); i++) {
                final int index = i;
                String message = npcMessageOptions.get(i);

                addRenderableWidget(Button.builder(Component.literal(truncate(message, 25)), button -> {
                    selectedNPCMessage = index;
                    sendMessage(npcMessageOptions.get(index));
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
        addRenderableWidget(Button.builder(Component.literal("← Zurück"), button -> {
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

        // Smartphone background
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF2A2A2A);

        // Header with participant name and head
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 40, 0xFF1A1A1A);

        // Profile picture in header
        int headSize = 24;
        int headX = leftPos + 8;
        int headY = topPos + 8;
        HeadRenderer.renderPlayerHead(guiGraphics, headX, headY, headSize, null);

        // Name
        String displayName = conversation.getParticipantName();
        guiGraphics.drawString(this.font, "§6§l" + displayName, headX + headSize + 8, topPos + 14, 0xFFFFFF);

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
        int maxWidth = WIDTH - 60;

        // Word wrap
        List<String> lines = wrapText(content, maxWidth);
        int msgHeight = lines.size() * 10 + 10;

        if (isSentByMe) {
            // Right-aligned (sent messages) - green
            int msgWidth = Math.min(maxWidth, this.font.width(content) + 10);
            int msgX = leftPos + WIDTH - msgWidth - 10;

            guiGraphics.fill(msgX, y, msgX + msgWidth, y + msgHeight, 0xFF2D5016);

            for (int i = 0; i < lines.size(); i++) {
                guiGraphics.drawString(this.font, "§f" + lines.get(i), msgX + 5, y + 5 + (i * 10), 0xFFFFFF);
            }
        } else {
            // Left-aligned (received messages) - dark gray
            int msgWidth = Math.min(maxWidth, this.font.width(content) + 10);
            int msgX = leftPos + 10;

            guiGraphics.fill(msgX, y, msgX + msgWidth, y + msgHeight, 0xFF3A3A3A);

            for (int i = 0; i < lines.size(); i++) {
                guiGraphics.drawString(this.font, "§f" + lines.get(i), msgX + 5, y + 5 + (i * 10), 0xFFFFFF);
            }
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
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
