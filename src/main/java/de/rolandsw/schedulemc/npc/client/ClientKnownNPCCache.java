package de.rolandsw.schedulemc.npc.client;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-seitiger Cache der NPCs, die der lokale Spieler kennt
 * (NPC-Data-UUIDs). Gespiegelt vom Server via SyncKnownNPCsPacket /
 * KnownNPCAddedPacket.
 */
public final class ClientKnownNPCCache {

    private static final Set<UUID> KNOWN = ConcurrentHashMap.newKeySet();

    private ClientKnownNPCCache() {
    }

    public static boolean isKnown(UUID npcDataId) {
        return npcDataId != null && KNOWN.contains(npcDataId);
    }

    public static void setAll(Set<UUID> ids) {
        KNOWN.clear();
        KNOWN.addAll(ids);
    }

    public static void addKnown(UUID npcDataId) {
        KNOWN.add(npcDataId);
    }

    public static void clear() {
        KNOWN.clear();
    }
}
