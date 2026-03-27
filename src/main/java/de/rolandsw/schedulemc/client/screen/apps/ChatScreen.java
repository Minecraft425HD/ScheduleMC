package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.messaging.Conversation;
import de.rolandsw.schedulemc.messaging.HeadRenderer;
import de.rolandsw.schedulemc.messaging.Message;
import de.rolandsw.schedulemc.messaging.network.MessageNetworkHandler;
import de.rolandsw.schedulemc.messaging.network.SendMessagePacket;
import de.rolandsw.schedulemc.messaging.network.DialogueStatePacket;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.SelectDialogueOptionPacket;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WhatsApp-style NPC/Player chat screen.
 *
 * Layout (NPC mode, HEIGHT=300):
 *   Y   0–40   : Green header (← head name online)
 *   Y  45–212  : Scrollable message history (168px)
 *   Y 213–214  : Teal divider line
 *   Y 215–299  : Semi-transparent overlay with 4 dialogue option buttons
 *
 * Layout (Player mode, HEIGHT=300):
 *   Y   0–40   : Header
 *   Y  45–199  : Scrollable message history (155px)
 *   Y 208–227  : EditBox (left 130px) + Send button (right 45px) on same row
 *   Y 234–251  : Back button
 */
@OnlyIn(Dist.CLIENT)
public class ChatScreen extends Screen {

    private final Screen parentScreen;
    private final Conversation conversation;
    /** Entity ID of NPC for dialogue packets (-1 for player chats) */
    private final int npcEntityId;

    private static final int WIDTH         = 200;
    private static final int HEIGHT        = 300;
    private static final int MARGIN_TOP    = 5;
    private static final int MESSAGE_HEIGHT = 30;
    private static final int OVERLAY_START = 215;  // Y where NPC option overlay begins
    private static final int MAX_DIALOGUE_BUTTONS = 4;

    // NPC overlay: 4 buttons × 16px each, 3px gap → spacing 19
    private static final int BTN_HEIGHT    = 16;
    private static final int BTN_SPACING   = 19;

    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;
    private EditBox messageInput;

    /** Dynamic dialogue option buttons (NPC chat only) */
    private final List<Button> dialogueButtons = new ArrayList<>();
    private final String[] dialogueOptionIds   = new String[MAX_DIALOGUE_BUTTONS];
    private final String[] dialogueOptionTexts = new String[MAX_DIALOGUE_BUTTONS];

    // PERFORMANCE: per-frame string cache
    private String cachedOnlineStr;
    private String cachedNoMessagesStr;
    private String cachedNowStr;
    private final Map<String, List<String>> wrappedTextCache = new ConcurrentHashMap<>();
    private long renderFrameTime;

    // ─────────────────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────────────────

    public ChatScreen(Screen parent, Conversation conversation) {
        this(parent, conversation, -1);
    }

    public ChatScreen(Screen parent, Conversation conversation, int npcEntityId) {
        super(Component.translatable("gui.app.chat.title"));
        this.parentScreen = parent;
        this.conversation = conversation;
        this.npcEntityId  = npcEntityId;
    }

    public UUID getParticipantUUID() {
        return conversation.getParticipantUUID();
    }

    // ─────────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos  = MARGIN_TOP;

        // Cache strings once per init
        this.cachedOnlineStr    = Component.translatable("gui.app.chat.online").getString();
        this.cachedNoMessagesStr = Component.translatable("gui.app.chat.no_messages").getString();
        this.cachedNowStr       = Component.translatable("gui.app.chat.now").getString();

        if (!conversation.isPlayerParticipant()) {
            // ── NPC chat: 4 invisible dialogue-option buttons in overlay ──
            dialogueButtons.clear();
            int buttonBaseY = topPos + OVERLAY_START + 2;
            for (int i = 0; i < MAX_DIALOGUE_BUTTONS; i++) {
                final int idx = i;
                Button btn = Button.builder(Component.literal(""), b -> {
                    if (dialogueOptionIds[idx] != null && npcEntityId != -1) {
                        NPCNetworkHandler.sendToServer(new SelectDialogueOptionPacket(
                            npcEntityId, dialogueOptionIds[idx], dialogueOptionTexts[idx]));
                    }
                }).bounds(leftPos + 5, buttonBaseY + idx * BTN_SPACING, WIDTH - 10, BTN_HEIGHT).build();
                btn.visible = false;
                dialogueButtons.add(btn);
                addRenderableWidget(btn);
            }
            // No explicit back button — ← in header is clickable (see mouseClicked)

        } else {
            // ── Player chat: EditBox + Send on same row, Back below ──
            messageInput = new EditBox(this.font,
                leftPos + 10, topPos + 208, 130, 20,
                Component.translatable("gui.app.chat.message_hint"));
            messageInput.setMaxLength(100);
            addRenderableWidget(messageInput);

            addRenderableWidget(Button.builder(
                Component.translatable("gui.app.chat.send"), b -> {
                    if (messageInput != null && !messageInput.getValue().isEmpty()) {
                        sendMessage(messageInput.getValue());
                        messageInput.setValue("");
                    }
                }).bounds(leftPos + 145, topPos + 208, 45, 20).build());

            addRenderableWidget(Button.builder(
                Component.translatable("gui.achievement_app.back"), b -> {
                    if (minecraft != null) minecraft.setScreen(parentScreen);
                }).bounds(leftPos + 10, topPos + 234, 80, 18).build());
        }
    }

    // ─────────────────────────────────────────────────────────
    // DIALOGUE STATE (called by DialogueStatePacket handler)
    // ─────────────────────────────────────────────────────────

    public void handleDialogueState(List<DialogueStatePacket.OptionEntry> options) {
        for (int i = 0; i < dialogueButtons.size(); i++) {
            dialogueButtons.get(i).visible = false;
            dialogueOptionIds[i]   = null;
            dialogueOptionTexts[i] = null;
        }
        int count = Math.min(options.size(), MAX_DIALOGUE_BUTTONS);
        for (int i = 0; i < count; i++) {
            DialogueStatePacket.OptionEntry entry = options.get(i);
            dialogueOptionIds[i]   = entry.id();
            dialogueOptionTexts[i] = entry.text();
            Button btn = dialogueButtons.get(i);
            btn.setMessage(Component.literal(truncate(entry.text(), 26)));
            btn.visible = true;
        }
    }

    // ─────────────────────────────────────────────────────────
    // RENDER
    // ─────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);

        // Smartphone outer border
        g.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);

        // Chat wallpaper (cream)
        g.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFFE7DDD3);

        // WhatsApp green header
        g.fill(leftPos, topPos, leftPos + WIDTH, topPos + 40, 0xFF075E54);

        // ← back arrow (interactive area, see mouseClicked)
        g.drawString(this.font, "←", leftPos + 8, topPos + 14, 0xFFFFFFFF, false);

        // Profile picture
        int headSize = 28;
        int headX    = leftPos + 25;
        int headY    = topPos + 6;
        HeadRenderer.renderPlayerHead(g, headX, headY, headSize, (ResourceLocation) null);

        // Name + online status
        String displayName = conversation.getParticipantName();
        g.drawString(this.font, displayName,      headX + headSize + 8, topPos + 12, 0xFFFFFFFF, false);
        g.drawString(this.font, cachedOnlineStr,  headX + headSize + 8, topPos + 24, 0xFFCCCCCC, false);

        this.renderFrameTime = System.currentTimeMillis();

        // ── Message area ──
        int messagesY      = topPos + 45;
        int messagesHeight = conversation.isPlayerParticipant() ? 155 : 168;
        renderMessages(g, messagesY, messagesHeight);

        // ── NPC overlay ──
        if (!conversation.isPlayerParticipant()) {
            // Teal divider line
            g.fill(leftPos, topPos + 213, leftPos + WIDTH, topPos + 215, 0xFF128C7E);
            // Dark semi-transparent panel (≈80 % opaque dark green)
            g.fill(leftPos, topPos + 215, leftPos + WIDTH, topPos + HEIGHT, 0xCC1B4332);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    // ─────────────────────────────────────────────────────────
    // MESSAGES
    // ─────────────────────────────────────────────────────────

    private void renderMessages(GuiGraphics g, int startY, int height) {
        List<Message> messages = conversation.getMessages();
        if (messages.isEmpty()) {
            g.drawCenteredString(this.font, cachedNoMessagesStr,
                leftPos + WIDTH / 2, startY + 20, 0xFFFFFF);
            return;
        }

        int currentY = startY + height - MESSAGE_HEIGHT - scrollOffset;
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            if (currentY + MESSAGE_HEIGHT >= startY && currentY <= startY + height) {
                renderMessage(g, msg, currentY);
            }
            currentY -= MESSAGE_HEIGHT;
        }
    }

    private void renderMessage(GuiGraphics g, Message message, int y) {
        boolean isSentByMe = minecraft != null && minecraft.player != null
            && message.getSenderUUID().equals(minecraft.player.getUUID());

        String content  = message.getContent();
        int maxWidth    = WIDTH - 70;
        List<String> lines = wrappedTextCache.computeIfAbsent(content, k -> wrapText(k, maxWidth));

        int maxLineWidth = 0;
        for (String line : lines) maxLineWidth = Math.max(maxLineWidth, this.font.width(line));

        int padding   = 8;
        int msgWidth  = Math.min(maxWidth, maxLineWidth + padding * 2 + 20);
        int msgHeight = lines.size() * 10 + padding * 2 + 6;

        String timeStr  = getMessageTime(message.getTimestamp());
        int timeWidth   = this.font.width(timeStr);

        if (isSentByMe) {
            int msgX = leftPos + WIDTH - msgWidth - 8;
            drawRoundedBubble(g, msgX, y, msgWidth, msgHeight, 0xFFDCF8C6);
            for (int i = 0; i < lines.size(); i++)
                g.drawString(this.font, lines.get(i), msgX + padding, y + padding + i * 10, 0xFF000000, false);
            g.drawString(this.font, timeStr, msgX + msgWidth - timeWidth - padding, y + msgHeight - 10, 0xFF888888, false);
        } else {
            int msgX = leftPos + 8;
            drawRoundedBubble(g, msgX, y, msgWidth, msgHeight, 0xFFFFFFFF);
            for (int i = 0; i < lines.size(); i++)
                g.drawString(this.font, lines.get(i), msgX + padding, y + padding + i * 10, 0xFF000000, false);
            g.drawString(this.font, timeStr, msgX + msgWidth - timeWidth - padding, y + msgHeight - 10, 0xFF888888, false);
        }
    }

    private void drawRoundedBubble(GuiGraphics g, int x, int y, int width, int height, int color) {
        g.fill(x + 2, y, x + width - 2, y + height, color);
        g.fill(x, y + 2, x + width, y + height - 2, color);
        g.fill(x + 1, y + 1, x + 2, y + 2, color);
        g.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
        g.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
        g.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
    }

    // ─────────────────────────────────────────────────────────
    // INPUT HANDLING
    // ─────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // ← header area (leftPos+4..22, topPos+8..28) navigates back
        if (mouseX >= leftPos + 4 && mouseX <= leftPos + 22
            && mouseY >= topPos + 8 && mouseY <= topPos + 28) {
            if (minecraft != null) minecraft.setScreen(parentScreen);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int visibleHeight = conversation.isPlayerParticipant() ? 155 : 168;
        int totalHeight   = conversation.getMessages().size() * MESSAGE_HEIGHT;
        if (totalHeight > visibleHeight) {
            scrollOffset -= (int) (delta * 10);
            scrollOffset = Math.max(0, Math.min(scrollOffset, totalHeight - visibleHeight));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E (inventory key = 69) from closing the screen
        return keyCode == 69 || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    private void sendMessage(String content) {
        if (minecraft != null && minecraft.player != null) {
            MessageNetworkHandler.sendToServer(new SendMessagePacket(
                conversation.getParticipantUUID(),
                conversation.getParticipantName(),
                conversation.isPlayerParticipant(),
                content));
        }
    }

    private String truncate(String text, int maxLength) {
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }

    private String getMessageTime(long timestamp) {
        long diff    = renderFrameTime - timestamp;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours   = minutes / 60;
        if (hours > 0)   return String.format("%02d:%02d", hours % 24, minutes % 60);
        if (minutes > 0) return minutes + "m";
        return cachedNowStr;
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String test = current.length() == 0 ? word : current + " " + word;
            if (this.font.width(test) <= maxWidth) {
                if (current.length() > 0) current.append(' ');
                current.append(word);
            } else {
                if (current.length() > 0) { lines.add(current.toString()); current = new StringBuilder(word); }
                else                      { lines.add(word); }
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines.isEmpty() ? List.of(text) : lines;
    }
}
