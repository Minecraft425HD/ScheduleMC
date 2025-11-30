package de.rolandsw.schedulemc.messaging;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Displays message notifications at the top center of the screen
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "schedulemc")
public class MessageNotificationOverlay {

    private static final Queue<Notification> notifications = new LinkedList<>();
    private static Notification currentNotification = null;
    private static long notificationStartTime = 0;
    private static final long NOTIFICATION_DURATION = 3000; // 3 seconds

    public static void showNotification(String senderName, String message) {
        notifications.add(new Notification(senderName, message));
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        long currentTime = System.currentTimeMillis();

        // Check if we need to show a new notification
        if (currentNotification == null && !notifications.isEmpty()) {
            currentNotification = notifications.poll();
            notificationStartTime = currentTime;
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
    }

    private static void renderNotification(GuiGraphics guiGraphics, Minecraft mc,
                                          Notification notification, float alpha) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        String title = "§6§l" + notification.senderName;
        String message = "§7" + notification.message;

        int titleWidth = mc.font.width(title);
        int messageWidth = mc.font.width(message);
        int maxWidth = Math.max(titleWidth, messageWidth);

        int boxWidth = maxWidth + 20;
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

        // Draw text
        RenderSystem.enableBlend();
        guiGraphics.drawString(mc.font, title, x + 10, y + 8, 0xFFFFFF | (alphaValue << 24));
        guiGraphics.drawString(mc.font, message, x + 10, y + 22, 0xFFFFFF | (alphaValue << 24));
        RenderSystem.disableBlend();
    }

    private static class Notification {
        final String senderName;
        final String message;

        Notification(String senderName, String message) {
            this.senderName = senderName;
            this.message = message.length() > 40 ? message.substring(0, 37) + "..." : message;
        }
    }
}
