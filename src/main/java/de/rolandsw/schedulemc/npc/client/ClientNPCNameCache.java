package de.rolandsw.schedulemc.npc.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-seitiger Cache für registrierte NPC-Namen
 * Wird vom Server über SyncNPCNamesPacket (Full-Sync) oder
 * DeltaSyncNPCNamesPacket (Delta-Sync) synchronisiert
 * SICHERHEIT: Thread-safe Set für concurrent access vom Network-Thread und Client-Thread
 */
@OnlyIn(Dist.CLIENT)
public class ClientNPCNameCache {

    // SICHERHEIT: ConcurrentHashMap.newKeySet() für Thread-safe Set-Operationen
    private static final Set<String> npcNames = ConcurrentHashMap.newKeySet();

    /**
     * Setzt die NPC-Namen (Full-Sync vom Server)
     * SICHERHEIT: Thread-safe clear + addAll Operation
     */
    public static void setNPCNames(Set<String> names) {
        npcNames.clear();
        npcNames.addAll(names);
    }

    /**
     * Fügt Namen hinzu (Delta-Sync)
     */
    public static void addNames(Set<String> names) {
        npcNames.addAll(names);
    }

    /**
     * Entfernt Namen (Delta-Sync)
     */
    public static void removeNames(Set<String> names) {
        npcNames.removeAll(names);
    }

    /**
     * Prüft ob ein Name bereits vergeben ist
     */
    public static boolean isNameTaken(String name) {
        if (name == null) return false;
        return npcNames.contains(name.trim());
    }

    /**
     * Löscht den Cache (z.B. beim Disconnect)
     */
    public static void clear() {
        npcNames.clear();
    }

    /**
     * Gibt alle registrierten Namen zurück
     */
    public static Set<String> getAllNames() {
        return new HashSet<>(npcNames);
    }
}
