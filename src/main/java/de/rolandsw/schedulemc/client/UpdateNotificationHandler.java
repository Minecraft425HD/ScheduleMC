package de.rolandsw.schedulemc.client;
import de.rolandsw.schedulemc.util.EventHelper;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.util.VersionChecker;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zeigt Update-Benachrichtigungen in den Hauptmenüs und beim Login an
 * SICHERHEIT: Thread-safe Set für concurrent player notification tracking
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class UpdateNotificationHandler {
    // Time Constants
    private static final int CHECK_DELAY_TICKS = 100; // 5 seconds delay (100 ticks at 20 ticks/sec)

    // UI Dimension Constants
    private static final int BUTTON_WIDTH = 120;      // Update button width
    private static final int BUTTON_HEIGHT = 20;      // Update button height

    private static volatile boolean hasChecked = false;
    private static volatile int tickCounter = 0;
    // SICHERHEIT: ConcurrentHashMap.newKeySet() für Thread-safe Set
    private static final Set<UUID> notifiedPlayers = ConcurrentHashMap.newKeySet();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        EventHelper.handleEvent(() -> {
            if (event.phase != TickEvent.Phase.END || hasChecked) {
                return;
            }

            tickCounter++;
            if (tickCounter >= CHECK_DELAY_TICKS) {
                hasChecked = true;
                VersionChecker.checkForUpdates();
                ScheduleMC.LOGGER.info("Started version check");
            }
        }, "onClientTick");
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        EventHelper.handleEvent(() -> {
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
            int buttonWidth = BUTTON_WIDTH;
            int buttonHeight = BUTTON_HEIGHT;
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
                } catch (java.net.URISyntaxException e) {
                    ScheduleMC.LOGGER.error("Invalid URL format: {}", e.getMessage());
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(
                            Component.literal("§cUngültige URL. Link wurde in Zwischenablage kopiert."),
                            false
                        );
                    }
                } catch (java.io.IOException e) {
                    ScheduleMC.LOGGER.error("Failed to open browser: {}", e.getMessage());
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(
                            Component.literal("§cFehler beim Öffnen des Browsers. URL wurde in Zwischenablage kopiert."),
                            false
                        );
                    }
                } catch (UnsupportedOperationException e) {
                    ScheduleMC.LOGGER.error("Desktop browse operation not supported: {}", e.getMessage());
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(
                            Component.literal("§cBrowser-Öffnen nicht unterstützt. URL wurde in Zwischenablage kopiert."),
                            false
                        );
                    }
                } catch (Exception e) {
                    // Fallback for unexpected errors
                    ScheduleMC.LOGGER.error("Unexpected error opening update URL", e);
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
        }, "onScreenInit");
    }

    /**
     * Benachrichtigt OPs beim Login über verfügbare Updates
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EventHelper.handleEvent(() -> {
            Player player = event.getEntity();

            // Prüfe ob Spieler schon benachrichtigt wurde
            if (notifiedPlayers.contains(player.getUUID())) {
                return;
            }

            // Prüfe ob Spieler OP ist oder kreativ Mode hat (für Singleplayer)
            if (!player.hasPermissions(2) && !player.isCreative()) {
                return;
            }

            // Prüfe ob Update verfügbar ist
            if (!VersionChecker.isUpdateAvailable()) {
                return;
            }

            // Markiere als benachrichtigt
            notifiedPlayers.add(player.getUUID());

            // Sende Update-Benachrichtigung
            Component message = Component.literal("\n§6§l[ScheduleMC]§r §eUpdate verfügbar!§r\n")
                .append(Component.literal("§7Aktuelle Version: §f" + VersionChecker.getCurrentVersion() + "\n"))
                .append(Component.literal("§7Neue Version: §a" + VersionChecker.getLatestVersion() + "\n"))
                .append(Component.literal("§b[Hier klicken zum Download]§r\n")
                    .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, VersionChecker.getDownloadUrl()))
                        .withUnderlined(true)
                    )
                );

            player.sendSystemMessage(message);
            ScheduleMC.LOGGER.info("Sent update notification to player: " + player.getName().getString());
        }, "onPlayerLogin");
    }
}
