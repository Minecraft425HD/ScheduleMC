package de.rolandsw.schedulemc.gang.client;

import de.rolandsw.schedulemc.gang.network.PlayerGangInfo;
import de.rolandsw.schedulemc.gang.network.SyncGangDataPacket;
import de.rolandsw.schedulemc.gang.network.SyncGangListPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Client-seitiger Cache fuer Gang-Daten.
 *
 * Zwei Ebenen:
 * 1. playerInfos: Leichtgewichtige Gang+Level Info aller Spieler (fuer Nametag/TAB)
 * 2. myGangData: Vollstaendige Gang-Daten des eigenen Spielers (fuer Gang-App)
 *
 * Thread-Safety: ConcurrentHashMap und volatile fuer concurrent access.
 */
@OnlyIn(Dist.CLIENT)
public class ClientGangCache {

    // Gang+Level Info aller Online-Spieler (fuer Nametag + TAB)
    private static final Map<UUID, PlayerGangInfo> playerInfos = new ConcurrentHashMap<>();

    // Vollstaendige Gang-Daten (fuer Gang-App)
    private static volatile SyncGangDataPacket myGangData = null;

    // Liste aller Gangs auf dem Server (fuer "Andere Gangs" Sektion)
    private static volatile List<SyncGangListPacket.GangListEntry> gangList = new CopyOnWriteArrayList<>();

    private static volatile Runnable updateListener = null;

    // ═══════════════════════════════════════════════════════════
    // PLAYER INFO (Nametag + TAB)
    // ═══════════════════════════════════════════════════════════

    /**
     * Aktualisiert die Info aller Spieler (kompletter Replace).
     */
    public static void updateAllPlayerInfos(List<PlayerGangInfo> infos) {
        playerInfos.clear();
        for (PlayerGangInfo info : infos) {
            playerInfos.put(info.getPlayerUUID(), info);
        }
    }

    /**
     * Gibt die Gang+Level Info eines Spielers zurueck.
     */
    public static PlayerGangInfo getPlayerInfo(UUID playerUUID) {
        return playerInfos.get(playerUUID);
    }

    /**
     * Alle Player-Infos (fuer TAB-Liste Sortierung).
     */
    public static Map<UUID, PlayerGangInfo> getAllPlayerInfos() {
        return playerInfos;
    }

    // ═══════════════════════════════════════════════════════════
    // EIGENE GANG-DATEN (Gang-App)
    // ═══════════════════════════════════════════════════════════

    public static void updateGangData(SyncGangDataPacket data) {
        myGangData = data;
        if (updateListener != null) {
            updateListener.run();
        }
    }

    public static SyncGangDataPacket getMyGangData() {
        return myGangData;
    }

    public static boolean hasGang() {
        return myGangData != null && myGangData.hasGang();
    }

    // ═══════════════════════════════════════════════════════════
    // GANG-LISTE (Andere Gangs)
    // ═══════════════════════════════════════════════════════════

    public static void updateGangList(List<SyncGangListPacket.GangListEntry> list) {
        gangList = new CopyOnWriteArrayList<>(list);
        if (updateListener != null) {
            updateListener.run();
        }
    }

    public static List<SyncGangListPacket.GangListEntry> getGangList() {
        return gangList;
    }

    // ═══════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════

    public static void clear() {
        playerInfos.clear();
        myGangData = null;
        gangList = new CopyOnWriteArrayList<>();
        updateListener = null;
    }

    public static void setUpdateListener(Runnable listener) {
        updateListener = listener;
    }

    public static void removeUpdateListener() {
        updateListener = null;
    }
}
