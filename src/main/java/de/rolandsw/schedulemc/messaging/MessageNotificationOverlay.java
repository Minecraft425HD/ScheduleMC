package de.rolandsw.schedulemc.messaging;
import de.rolandsw.schedulemc.util.EventHelper;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Displays message notifications at the top center of the screen
 * SICHERHEIT: Thread-safe Queue und volatile Felder für concurrent access
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "schedulemc")
public class MessageNotificationOverlay {

    // SICHERHEIT: ConcurrentLinkedQueue für Thread-safe access von showNotification() und onRenderOverlay()
    private static final Queue<Notification> notifications = new ConcurrentLinkedQueue<>();
    // SICHERHEIT: volatile für Memory Visibility zwischen Threads
    private static volatile Notification currentNotification = null;
    private static volatile long notificationStartTime = 0;
    private static final long NOTIFICATION_DURATION = 3000; // 3 seconds

    public static void showNotification(String senderName, String message) {
        notifications.add(new Notification(senderName, message));
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        EventHelper.handleEvent(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

        long currentTime = System.currentTimeMillis();

        // Check if we need to show a new notification
        if (currentNotification == null && !notifications.isEmpty()) {
            Notification next = notifications.poll();
            if (next != null) {
                next.computeWidths(mc.font);
                notificationStartTime = currentTime;
                currentNotification = next;
            }
        }

        // Render current notification
        if (currentNotification != null) {
            long elapsed = currentTime - notificationStartTime;

            if (elapsed >= NOTIFICATION_DURATION) {
                currentNotification = null;
                return;
            }

            // Calculate fade-in and fade-out
            float alpha = 1.0f;
            if (elapsed < 300) {
                alpha = elapsed / 300f;
            } else if (elapsed > NOTIFICATION_DURATION - 300) {
                alpha = (NOTIFICATION_DURATION - elapsed) / 300f;
            }

            renderNotification(event.getGuiGraphics(), mc, currentNotification, alpha);
        }
        }, "onRenderOverlay");
    }

    private static void renderNotification(GuiGraphics guiGraphics, Minecraft mc,
                                          Notification notification, float alpha) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int boxWidth = notification.boxWidth;
        int boxHeight = 40;
        int x = (screenWidth - boxWidth) / 2;
        int y = 10;

        int alphaValue = (int)(alpha * 255);
        int bgColor = (alphaValue << 24) | 0x1C1C1C;
        int borderColor = (alphaValue << 24) | 0x6B4423;

        // Draw background
        guiGraphics.fill(x, y, x + boxWidth, y + boxHeight, bgColor);

        // Draw border
        guiGraphics.fill(x, y, x + boxWidth, y + 1, borderColor);
        guiGraphics.fill(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, borderColor);
        guiGraphics.fill(x, y, x + 1, y + boxHeight, borderColor);
        guiGraphics.fill(x + boxWidth - 1, y, x + boxWidth, y + boxHeight, borderColor);

        // Draw text (uses pre-computed formatted strings from Notification)
        RenderSystem.enableBlend();
        guiGraphics.drawString(mc.font, notification.formattedTitle, x + 10, y + 8, 0xFFFFFF | (alphaValue << 24));
        guiGraphics.drawString(mc.font, notification.formattedMessage, x + 10, y + 22, 0xFFFFFF | (alphaValue << 24));
        RenderSystem.disableBlend();
    }

    private static class Notification {
        final String senderName;
        final String message;
        // Pre-computed formatted strings (avoids per-frame string concatenation)
        final String formattedTitle;
        final String formattedMessage;
        // Cached font width calculations (computed once when notification becomes active)
        int titleWidth;
        int messageWidth;
        int boxWidth;

        Notification(String senderName, String message) {
            this.senderName = senderName;
            this.message = message.length() > 40 ? message.substring(0, 37) + "..." : message;
            this.formattedTitle = "\u00A76\u00A7l" + senderName;
            this.formattedMessage = "\u00A77" + this.message;
        }

        void computeWidths(net.minecraft.client.gui.Font font) {
            this.titleWidth = font.width(formattedTitle);
            this.messageWidth = font.width(formattedMessage);
            this.boxWidth = Math.max(titleWidth, messageWidth) + 20;
        }
    }
}
