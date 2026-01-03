package de.rolandsw.schedulemc.npc.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.Set;

/**
 * Client-seitiger Cache für registrierte NPC-Namen
 * Wird vom Server über SyncNPCNamesPacket (Full-Sync) oder
 * DeltaSyncNPCNamesPacket (Delta-Sync) synchronisiert
 */
@OnlyIn(Dist.CLIENT)
public class ClientNPCNameCache {

    private static Set<String> npcNames = new HashSet<>();

    /**
     * Setzt die NPC-Namen (Full-Sync vom Server)
     */
    public static void setNPCNames(Set<String> names) {
        npcNames = new HashSet<>(names);
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
