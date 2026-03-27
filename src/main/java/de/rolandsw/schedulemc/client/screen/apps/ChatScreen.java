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
 * WhatsApp-style NPC/Player chat screen — Design C (Modernes Overlay).
 *
 * HEIGHT is fixed at 240px (safe for all GUI scales).  topPos is centred
 * vertically so the panel never falls off-screen.
 *
 * NPC layout (Y relative to topPos):
 *   0 – 40  : green header  (← clickable for back)
 *  45 – 154 : scrollable message history (110px)
 * 155 – 156 : teal divider
 * 157 – 238 : dark overlay with ≤3 dialogue-option buttons
 *
 * Player layout:
 *   0 – 40  : green header
 *  45 – 154 : scrollable message history (110px)
 * 162 – 181 : EditBox (130px) + Send button (45px) on the same row
 * 188 – 205 : Back button
 */
@OnlyIn(Dist.CLIENT)
public class ChatScreen extends Screen {

    private final Screen parentScreen;
    private final Conversation conversation;
    /** Entity ID of NPC for dialogue packets, or -1 for player chats. */
    private final int npcEntityId;

    // ── Layout constants ─────────────────────────────────────
    private static final int WIDTH                = 200;
    private static final int HEIGHT               = 240;
    private static final int HEADER_H             = 40;
    private static final int MESSAGES_TOP         = 45;   // = HEADER_H + 5
    private static final int MESSAGES_H           = 110;  // NPC and player share same area
    private static final int OVERLAY_START        = 155;  // = MESSAGES_TOP + MESSAGES_H
    private static final int MAX_DIALOGUE_BUTTONS = 3;
    private static final int BTN_HEIGHT           = 16;
    private static final int BTN_SPACING          = 19;   // BTN_HEIGHT + 3px gap
    /** Minimum gap (px) between consecutive message bubbles. */
    private static final int MSG_GAP              = 3;
    /** Padding inside each message bubble. */
    private static final int MSG_PADDING          = 8;

    // ── Instance state ────────────────────────────────────────
    private int leftPos;
    private int topPos;
    /** Scroll offset in pixels (0 = bottom-most messages visible). */
    private int scrollOffset = 0;
    private EditBox messageInput;

    /** Dialogue option buttons (NPC chat, filled via handleDialogueState). */
    private final List<Button> dialogueButtons = new ArrayList<>();
    private final String[] dialogueOptionIds   = new String[MAX_DIALOGUE_BUTTONS];
    private final String[] dialogueOptionTexts = new String[MAX_DIALOGUE_BUTTONS];
    /**
     * Last known options — persisted so init() (called on resize/fullscreen
     * toggle) can restore button state without a server round-trip.
     */
    private List<DialogueStatePacket.OptionEntry> lastDialogueOptions = new ArrayList<>();

    // ── Per-frame caches ──────────────────────────────────────
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
    // INIT  (called on first open AND on every resize)
    // ─────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;
        // Centre vertically so the panel never falls off-screen
        this.topPos  = Math.max(2, (this.height - HEIGHT) / 2);

        this.cachedOnlineStr    = Component.translatable("gui.app.chat.online").getString();
        this.cachedNoMessagesStr = Component.translatable("gui.app.chat.no_messages").getString();
        this.cachedNowStr       = Component.translatable("gui.app.chat.now").getString();

        if (!conversation.isPlayerParticipant()) {
            // ── NPC chat: up to 3 dialogue option buttons in overlay ─────
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
            // Restore state lost during resize / fullscreen toggle
            if (!lastDialogueOptions.isEmpty()) {
                applyDialogueOptions(lastDialogueOptions);
            }
            // Back via ← in header (see mouseClicked) — no extra button needed

        } else {
            // ── Player chat: input row + back button ─────────────────────
            messageInput = new EditBox(this.font,
                leftPos + 10, topPos + 162, 130, 20,
                Component.translatable("gui.app.chat.message_hint"));
            messageInput.setMaxLength(100);
            addRenderableWidget(messageInput);

            addRenderableWidget(Button.builder(
                Component.translatable("gui.app.chat.send"), b -> {
                    if (messageInput != null && !messageInput.getValue().isEmpty()) {
                        sendMessage(messageInput.getValue());
                        messageInput.setValue("");
                    }
                }).bounds(leftPos + 145, topPos + 162, 45, 20).build());

            addRenderableWidget(Button.builder(
                Component.translatable("gui.achievement_app.back"), b -> {
                    if (minecraft != null) minecraft.setScreen(parentScreen);
                }).bounds(leftPos + 10, topPos + 188, 80, 18).build());
        }
    }

    // ─────────────────────────────────────────────────────────
    // DIALOGUE STATE  (called by DialogueStatePacket handler)
    // ─────────────────────────────────────────────────────────

    public void handleDialogueState(List<DialogueStatePacket.OptionEntry> options) {
        lastDialogueOptions = new ArrayList<>(options); // persist for resize
        applyDialogueOptions(options);
    }

    private void applyDialogueOptions(List<DialogueStatePacket.OptionEntry> options) {
        for (int i = 0; i < dialogueButtons.size(); i++) {
            dialogueButtons.get(i).visible = false;
            dialogueOptionIds[i]   = null;
            dialogueOptionTexts[i] = null;
        }
        int count = Math.min(options.size(), MAX_DIALOGUE_BUTTONS);
        for (int i = 0; i < count; i++) {
            DialogueStatePacket.OptionEntry e = options.get(i);
            dialogueOptionIds[i]   = e.id();
            dialogueOptionTexts[i] = e.text();
            Button btn = dialogueButtons.get(i);
            btn.setMessage(Component.literal(truncate(e.text(), 26)));
            btn.visible = true;
        }
    }

    // ─────────────────────────────────────────────────────────
    // RENDER
    // ─────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);

        // Smartphone border
        g.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);
        // Chat wallpaper (cream)
        g.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFFE7DDD3);
        // WhatsApp green header
        g.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEADER_H, 0xFF075E54);

        // ← back arrow  (interactive — see mouseClicked)
        g.drawString(this.font, "←", leftPos + 8, topPos + 14, 0xFFFFFFFF, false);

        // Profile picture
        int headX = leftPos + 25;
        int headY = topPos + 6;
        HeadRenderer.renderPlayerHead(g, headX, headY, 28, (ResourceLocation) null);

        // Name + online status
        g.drawString(this.font, conversation.getParticipantName(),
            headX + 36, topPos + 12, 0xFFFFFFFF, false);
        g.drawString(this.font, cachedOnlineStr,
            headX + 36, topPos + 24, 0xFFCCCCCC, false);

        this.renderFrameTime = System.currentTimeMillis();

        // Messages
        renderMessages(g, topPos + MESSAGES_TOP, MESSAGES_H);

        // NPC overlay
        if (!conversation.isPlayerParticipant()) {
            // Teal divider
            g.fill(leftPos, topPos + OVERLAY_START,
                   leftPos + WIDTH, topPos + OVERLAY_START + 2, 0xFF128C7E);
            // Dark semi-transparent panel
            g.fill(leftPos, topPos + OVERLAY_START + 2,
                   leftPos + WIDTH, topPos + HEIGHT, 0xCC1B4332);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    // ─────────────────────────────────────────────────────────
    // MESSAGE RENDERING  (variable-height bubbles, no overlap)
    // ─────────────────────────────────────────────────────────

    private void renderMessages(GuiGraphics g, int startY, int areaHeight) {
        List<Message> messages = conversation.getMessages();
        if (messages.isEmpty()) {
            g.drawCenteredString(this.font, cachedNoMessagesStr,
                leftPos + WIDTH / 2, startY + 20, 0xFFFFFF);
            return;
        }

        // Scissor-clip to the message area so bubbles never bleed into the
        // header above or the NPC overlay panel below.
        g.enableScissor(leftPos, startY, leftPos + WIDTH, startY + areaHeight);

        // Render newest-first from the bottom of the area upwards.
        // Each message's bubble height is calculated on-the-fly so there
        // is never any overlap regardless of text length.
        int y = startY + areaHeight - scrollOffset;
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            int bubbleH = bubbleHeight(msg.getContent());
            y -= bubbleH + MSG_GAP;
            if (y + bubbleH >= startY && y <= startY + areaHeight) {
                renderMessage(g, msg, y, startY, startY + areaHeight);
            }
            if (y < startY - bubbleH) break; // no more visible messages above
        }

        g.disableScissor();
    }

    /** Height in pixels of the bubble for the given message text. */
    private int bubbleHeight(String content) {
        List<String> lines = wrappedTextCache.computeIfAbsent(
            content, k -> wrapText(k, WIDTH - 70));
        return lines.size() * 10 + MSG_PADDING * 2 + 6;
    }

    private void renderMessage(GuiGraphics g, Message message,
                               int y, int clipTop, int clipBottom) {
        // Clip bubbles that are partially scrolled out of the area
        if (y + bubbleHeight(message.getContent()) <= clipTop || y >= clipBottom) return;

        boolean mine = minecraft != null && minecraft.player != null
            && message.getSenderUUID().equals(minecraft.player.getUUID());

        String content = message.getContent();
        List<String> lines = wrappedTextCache.computeIfAbsent(
            content, k -> wrapText(k, WIDTH - 70));

        int maxLineW = 0;
        for (String l : lines) maxLineW = Math.max(maxLineW, this.font.width(l));

        int padding   = MSG_PADDING;
        int msgWidth  = Math.min(WIDTH - 70, maxLineW + padding * 2 + 20);
        int msgHeight = lines.size() * 10 + padding * 2 + 6;

        String timeStr = getMessageTime(message.getTimestamp());
        int timeW      = this.font.width(timeStr);

        if (mine) {
            int msgX = leftPos + WIDTH - msgWidth - 8;
            drawRoundedBubble(g, msgX, y, msgWidth, msgHeight, 0xFFDCF8C6);
            for (int i = 0; i < lines.size(); i++)
                g.drawString(this.font, lines.get(i),
                    msgX + padding, y + padding + i * 10, 0xFF000000, false);
            g.drawString(this.font, timeStr,
                msgX + msgWidth - timeW - padding, y + msgHeight - 10, 0xFF888888, false);
        } else {
            int msgX = leftPos + 8;
            drawRoundedBubble(g, msgX, y, msgWidth, msgHeight, 0xFFFFFFFF);
            for (int i = 0; i < lines.size(); i++)
                g.drawString(this.font, lines.get(i),
                    msgX + padding, y + padding + i * 10, 0xFF000000, false);
            g.drawString(this.font, timeStr,
                msgX + msgWidth - timeW - padding, y + msgHeight - 10, 0xFF888888, false);
        }
    }

    private void drawRoundedBubble(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x + 2, y,     x + w - 2, y + h,     color);
        g.fill(x,     y + 2, x + w,     y + h - 2, color);
        g.fill(x + 1, y + 1, x + 2,     y + 2,     color);
        g.fill(x + w - 2, y + 1, x + w - 1, y + 2, color);
        g.fill(x + 1, y + h - 2, x + 2,     y + h - 1, color);
        g.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, color);
    }

    // ─────────────────────────────────────────────────────────
    // INPUT HANDLING
    // ─────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Header ← zone: navigate back
        if (mx >= leftPos + 4 && mx <= leftPos + 24
            && my >= topPos + 8 && my <= topPos + 28) {
            if (minecraft != null) minecraft.setScreen(parentScreen);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Compute total pixel height of all messages
        int totalH = 0;
        for (Message m : conversation.getMessages()) {
            totalH += bubbleHeight(m.getContent()) + MSG_GAP;
        }
        if (totalH > MESSAGES_H) {
            scrollOffset -= (int) (delta * 12);
            scrollOffset = Math.max(0, Math.min(scrollOffset, totalH - MESSAGES_H));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
        StringBuilder cur = new StringBuilder();
        for (String word : words) {
            String test = cur.length() == 0 ? word : cur + " " + word;
            if (this.font.width(test) <= maxWidth) {
                if (cur.length() > 0) cur.append(' ');
                cur.append(word);
            } else {
                if (cur.length() > 0) { lines.add(cur.toString()); cur = new StringBuilder(word); }
                else                  { lines.add(word); }
            }
        }
        if (cur.length() > 0) lines.add(cur.toString());
        return lines.isEmpty() ? List.of(text) : lines;
    }
}
