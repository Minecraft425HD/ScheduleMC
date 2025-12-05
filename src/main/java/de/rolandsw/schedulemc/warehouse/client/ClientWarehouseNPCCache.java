package de.rolandsw.schedulemc.warehouse.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-seitiger Cache für NPC-Namen im Warehouse-System
 * Speichert UUID -> Name Mapping damit NPCs auch angezeigt werden können
 * wenn sie nicht im Rendering-Bereich sind
 */
@OnlyIn(Dist.CLIENT)
public class ClientWarehouseNPCCache {

    private static final Map<UUID, String> npcNames = new ConcurrentHashMap<>();

    /**
     * Fügt einen NPC zum Cache hinzu
     */
    public static void putNPC(UUID uuid, String name) {
        if (uuid != null && name != null) {
            npcNames.put(uuid, name);
        }
    }

    /**
     * Holt den Namen eines NPCs aus dem Cache
     */
    public static String getNPCName(UUID uuid) {
        return npcNames.get(uuid);
    }

    /**
     * Prüft ob ein NPC im Cache ist
     */
    public static boolean hasNPC(UUID uuid) {
        return npcNames.containsKey(uuid);
    }

    /**
     * Entfernt einen NPC aus dem Cache
     */
    public static void removeNPC(UUID uuid) {
        npcNames.remove(uuid);
    }

    /**
     * Löscht den gesamten Cache (z.B. beim Disconnect)
     */
    public static void clear() {
        npcNames.clear();
    }

    /**
     * Gibt die Anzahl gecachter NPCs zurück
     */
    public static int size() {
        return npcNames.size();
    }
}
