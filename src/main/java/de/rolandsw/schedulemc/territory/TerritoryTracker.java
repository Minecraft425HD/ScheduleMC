package de.rolandsw.schedulemc.territory;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Trackt Territory-Wechsel und zeigt Hologramm beim Betreten/Verlassen
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public class TerritoryTracker {

    // Speichert letztes Territory pro Spieler
    private static final Map<UUID, Territory> lastTerritory = new HashMap<>();

    // Cooldown um Spam zu vermeiden
    private static final Map<UUID, Long> lastNotificationTime = new HashMap<>();
    private static final long NOTIFICATION_COOLDOWN = 2000; // 2 Sekunden

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.player.level().isClientSide) return;

        // Nur jede 10 Ticks prüfen (= 0.5 Sekunden)
        if (player.tickCount % 10 != 0) return;

        TerritoryManager manager = TerritoryManager.getInstance();
        if (manager == null) return;

        // Aktuelles Territory ermitteln
        int chunkX = player.blockPosition().getX() >> 4;
        int chunkZ = player.blockPosition().getZ() >> 4;
        Territory currentTerritory = manager.getTerritory(chunkX, chunkZ);

        // Letztes Territory des Spielers
        Territory lastPlayerTerritory = lastTerritory.get(player.getUUID());

        // Hat sich Territory geändert?
        if (hasChanged(currentTerritory, lastPlayerTerritory)) {
            // Cooldown prüfen
            long now = System.currentTimeMillis();
            Long lastNotification = lastNotificationTime.get(player.getUUID());

            if (lastNotification == null || (now - lastNotification) > NOTIFICATION_COOLDOWN) {
                // Benachrichtigung anzeigen
                showTerritoryChange(player, lastPlayerTerritory, currentTerritory);

                // Update
                lastNotificationTime.put(player.getUUID(), now);
            }

            // Territory aktualisieren
            if (currentTerritory != null) {
                lastTerritory.put(player.getUUID(), currentTerritory);
            } else {
                lastTerritory.remove(player.getUUID());
            }
        }
    }

    /**
     * Prüft ob sich Territory geändert hat
     */
    private static boolean hasChanged(@Nullable Territory current, @Nullable Territory last) {
        if (current == null && last == null) {
            return false; // Beide null - keine Änderung
        }

        if (current == null || last == null) {
            return true; // Einer null, anderer nicht - Änderung
        }

        // Vergleiche Chunk-Position
        return current.getChunkX() != last.getChunkX() ||
               current.getChunkZ() != last.getChunkZ();
    }

    /**
     * Zeigt Hologramm mit Territory-Name beim Betreten/Verlassen
     */
    private static void showTerritoryChange(ServerPlayer player, @Nullable Territory from, @Nullable Territory to) {
        String title;
        String subtitle;

        if (to != null) {
            // Territory betreten
            title = to.getType().getEmoji() + " " + to.getType().getDisplayName();

            if (to.getName() != null && !to.getName().isEmpty()) {
                subtitle = "§7" + to.getName();
            } else {
                subtitle = "§7Unbenanntes Gebiet";
            }

        } else {
            // Territory verlassen (jetzt in freiem Gebiet)
            title = "§7Freies Gebiet";
            subtitle = "";
        }

        // Sende Title-Pakete
        sendTitle(player, title, subtitle, 5, 40, 10); // fadeIn: 5, stay: 40, fadeOut: 10
    }

    /**
     * Sendet Title + Subtitle an Spieler
     */
    private static void sendTitle(ServerPlayer player, String title, String subtitle,
                                  int fadeInTicks, int stayTicks, int fadeOutTicks) {
        // Animation-Packet (Timings)
        player.connection.send(new ClientboundSetTitlesAnimationPacket(
            fadeInTicks, stayTicks, fadeOutTicks
        ));

        // Title-Packet
        player.connection.send(new ClientboundSetTitleTextPacket(
            Component.literal(title)
        ));

        // Subtitle-Packet
        if (!subtitle.isEmpty()) {
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                Component.literal(subtitle)
            ));
        }
    }

    /**
     * Cleanup wenn Spieler disconnected
     */
    public static void cleanupPlayer(UUID playerUUID) {
        lastTerritory.remove(playerUUID);
        lastNotificationTime.remove(playerUUID);
    }

    /**
     * Cleanup aller Spieler (Server Stop)
     */
    public static void cleanupAll() {
        lastTerritory.clear();
        lastNotificationTime.clear();
    }
}
