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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trackt Territory-Wechsel und zeigt Hologramm beim Betreten/Verlassen
 * SICHERHEIT: Verwendet ConcurrentHashMap für Thread-Sicherheit bei Event-Handlern
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID)
public class TerritoryTracker {

    // SICHERHEIT: ConcurrentHashMap statt HashMap für Thread-Sicherheit
    // Speichert letztes Territory pro Spieler
    private static final Map<UUID, Territory> lastTerritory = new ConcurrentHashMap<>();

    // Cooldown um Spam zu vermeiden
    private static final Map<UUID, Long> lastNotificationTime = new ConcurrentHashMap<>();
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
     * WICHTIG: Vergleicht NAMEN statt Position, sodass zusammenhängende Chunks
     * mit gleichem Namen als EIN Territory gelten
     */
    private static boolean hasChanged(@Nullable Territory current, @Nullable Territory last) {
        if (current == null && last == null) {
            return false; // Beide null - keine Änderung
        }

        if (current == null || last == null) {
            return true; // Einer null, anderer nicht - Änderung
        }

        // Vergleiche Namen (nicht Position!)
        // Chunks mit gleichem Namen = GLEICHES Territory
        String currentName = current.getName();
        String lastName = last.getName();

        // Normalisiere null/empty
        if (currentName == null || currentName.isEmpty()) currentName = null;
        if (lastName == null || lastName.isEmpty()) lastName = null;

        // Wenn Namen unterschiedlich -> Territory hat sich geändert
        if (!java.util.Objects.equals(currentName, lastName)) {
            return true;
        }

        // Namen gleich -> prüfe ob Farbe unterschiedlich
        return current.getType() != last.getType();
    }

    /**
     * Zeigt Hologramm mit Territory-Name beim Betreten/Verlassen
     * KEINE Farbanzeige mehr - nur der Name!
     */
    private static void showTerritoryChange(ServerPlayer player, @Nullable Territory from, @Nullable Territory to) {
        String title;
        String subtitle = ""; // Kein Subtitle mehr

        if (to != null) {
            // Territory betreten
            if (to.getName() != null && !to.getName().isEmpty()) {
                // Custom Name vorhanden -> zeige nur Namen
                title = to.getName();
            } else {
                // Kein Name -> zeige "Unbenanntes Gebiet"
                title = "Unbenanntes Gebiet";
            }

        } else {
            // Territory verlassen (jetzt in freiem Gebiet)
            title = "§7Freies Gebiet";
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
