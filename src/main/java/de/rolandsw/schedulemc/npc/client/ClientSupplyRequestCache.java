package de.rolandsw.schedulemc.npc.client;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-Cache: NPCs (Data-UUIDs), die eine offene Warenanfrage an den
 * lokalen Spieler haben — Renderer zeigt dann ein "!" vor dem Namen.
 */
public final class ClientSupplyRequestCache {

    private static final Set<UUID> ACTIVE = ConcurrentHashMap.newKeySet();

    private ClientSupplyRequestCache() {
    }

    public static boolean has(UUID npcDataId) {
        return npcDataId != null && ACTIVE.contains(npcDataId);
    }

    public static void add(UUID npcDataId) {
        ACTIVE.add(npcDataId);
    }

    public static void remove(UUID npcDataId) {
        ACTIVE.remove(npcDataId);
    }

    public static void clear() {
        ACTIVE.clear();
    }
}
