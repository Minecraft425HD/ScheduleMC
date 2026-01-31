package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.messaging.HeadRenderer;
import de.rolandsw.schedulemc.player.PlayerTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Kontakte App - Shows all players who have ever joined the server
 */
@OnlyIn(Dist.CLIENT)
public class ContactsAppScreen extends Screen {

    private final Screen parentScreen;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 240;
    private static final int BORDER_SIZE = 5;
    private static final int MARGIN_TOP = 5;
    private static final int MARGIN_BOTTOM = 60;
    private static final int CONTACT_ITEM_HEIGHT = 45;
    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;
    private List<PlayerTracker.PlayerContact> contacts;

    public ContactsAppScreen(Screen parent) {
        super(Component.translatable("gui.app.contacts.title"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = MARGIN_TOP;

        // Load all player contacts, excluding the current player
        contacts = PlayerTracker.getAllContacts();
        if (minecraft != null && minecraft.player != null) {
            contacts.removeIf(contact -> contact.getUuid().equals(minecraft.player.getUUID()));
        }

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.translatable("gui.app.contacts.back"), button -> {
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
        guiGraphics.drawString(this.font, "\u00a70\u00a7l" + Component.translatable("gui.app.contacts.title").getString(), leftPos + 10, topPos + 13, 0x000000, false);

        // Content area
        int contentY = topPos + 40;
        int contentHeight = HEIGHT - 80;

        if (contacts == null || contacts.isEmpty()) {
            // Empty state
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.contacts.no_contacts").getString(),
                leftPos + WIDTH / 2, contentY + 30, 0xFF999999);
        } else {
            // Render contact list
            for (int i = 0; i < contacts.size(); i++) {
                int itemY = contentY + (i * CONTACT_ITEM_HEIGHT) - scrollOffset;

                // Skip if not visible
                if (itemY + CONTACT_ITEM_HEIGHT < contentY || itemY > contentY + contentHeight) {
                    continue;
                }

                renderContactItem(guiGraphics, contacts.get(i), leftPos, itemY, mouseX, mouseY);
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderContactItem(GuiGraphics guiGraphics, PlayerTracker.PlayerContact contact, int x, int y, int mouseX, int mouseY) {
        int itemWidth = WIDTH;

        // Check if mouse is hovering
        boolean isHovering = mouseX >= x && mouseX <= x + itemWidth && mouseY >= y && mouseY <= y + CONTACT_ITEM_HEIGHT;

        // iOS-style background (white with hover)
        int bgColor = isHovering ? 0xFFF0F0F0 : 0xFFFFFFFF;
        guiGraphics.fill(x, y, x + itemWidth, y + CONTACT_ITEM_HEIGHT, bgColor);

        // Bottom border line (light gray)
        guiGraphics.fill(x + 60, y + CONTACT_ITEM_HEIGHT - 1, x + itemWidth, y + CONTACT_ITEM_HEIGHT, 0xFFE0E0E0);

        // Profile picture
        int headSize = 28;
        int headX = x + 12;
        int headY = y + (CONTACT_ITEM_HEIGHT - headSize) / 2;

        // Render player head
        HeadRenderer.renderPlayerHead(guiGraphics, headX, headY, headSize, contact.getUuid());

        // Name
        int textX = headX + headSize + 12;
        int nameY = y + (CONTACT_ITEM_HEIGHT - this.font.lineHeight) / 2;

        String displayName = contact.getName();
        if (displayName.length() > 18) {
            displayName = displayName.substring(0, 15) + "...";
        }
        guiGraphics.drawString(this.font, displayName, textX, nameY, 0xFF000000, false);

        // Arrow indicator (right side)
        guiGraphics.drawString(this.font, "›", x + itemWidth - 15, nameY, 0xFFC7C7CC, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && contacts != null) { // Left click
            int contentY = topPos + 40;

            for (int i = 0; i < contacts.size(); i++) {
                int itemY = contentY + (i * CONTACT_ITEM_HEIGHT) - scrollOffset;
                int itemX = leftPos;
                int itemWidth = WIDTH;

                if (mouseX >= itemX && mouseX <= itemX + itemWidth &&
                    mouseY >= itemY && mouseY <= itemY + CONTACT_ITEM_HEIGHT) {

                    // Open contact detail screen
                    if (minecraft != null) {
                        minecraft.setScreen(new ContactDetailScreen(this, contacts.get(i)));
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (contacts != null && contacts.size() * CONTACT_ITEM_HEIGHT > HEIGHT - 80) {
            scrollOffset -= (int)(delta * 10);
            scrollOffset = Math.max(0, Math.min(scrollOffset, contacts.size() * CONTACT_ITEM_HEIGHT - (HEIGHT - 80)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
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
