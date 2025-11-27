package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.util.VersionChecker;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Zeigt Update-Benachrichtigungen in den Hauptmenüs an
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class UpdateNotificationHandler {

    private static boolean hasChecked = false;
    private static int tickCounter = 0;
    private static final int CHECK_DELAY = 100; // 5 Sekunden (20 ticks/sec)

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || hasChecked) {
            return;
        }

        tickCounter++;
        if (tickCounter >= CHECK_DELAY) {
            hasChecked = true;
            VersionChecker.checkForUpdates();
            ScheduleMC.LOGGER.info("Started version check");
        }
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        // Prüfe ob wir auf dem Hauptmenü oder Multiplayer-Menü sind
        if (!(screen instanceof TitleScreen) && !(screen instanceof JoinMultiplayerScreen)) {
            return;
        }

        // Nur anzeigen wenn Update verfügbar ist
        if (!VersionChecker.isUpdateAvailable()) {
            return;
        }

        int width = screen.width;
        int height = screen.height;

        // Position für den Update-Button (oben rechts)
        int buttonWidth = 120;
        int buttonHeight = 20;
        int buttonX = width - buttonWidth - 5;
        int buttonY = 5;

        // Erstelle Update-Button
        Component buttonText = Component.literal("§6§l⚠ Update §r§ev" + VersionChecker.getLatestVersion());

        Button updateButton = Button.builder(buttonText, button -> {
            // Öffne Download-URL im Browser
            try {
                String url = VersionChecker.getDownloadUrl();
                Minecraft.getInstance().keyboardHandler.setClipboard(url);

                // Zeige Nachricht
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(
                        Component.literal("§aDownload-Link in Zwischenablage kopiert!"),
                        false
                    );
                }

                // Versuche Browser zu öffnen
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } catch (Exception e) {
                ScheduleMC.LOGGER.error("Failed to open update URL", e);
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(
                        Component.literal("§cFehler beim Öffnen des Links. URL wurde in Zwischenablage kopiert."),
                        false
                    );
                }
            }
        })
        .bounds(buttonX, buttonY, buttonWidth, buttonHeight)
        .build();

        event.addListener(updateButton);

        // Füge auch einen Info-Text hinzu
        if (screen instanceof TitleScreen) {
            ScheduleMC.LOGGER.info("Added update notification to title screen");
        } else if (screen instanceof JoinMultiplayerScreen) {
            ScheduleMC.LOGGER.info("Added update notification to multiplayer screen");
        }
    }
}
