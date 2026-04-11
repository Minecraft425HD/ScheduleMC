package de.rolandsw.schedulemc.mission.proximity;

import de.rolandsw.schedulemc.managers.NPCNameRegistry;
import de.rolandsw.schedulemc.mission.MissionEventBridge;
import de.rolandsw.schedulemc.mission.MissionStatus;
import de.rolandsw.schedulemc.mission.PlayerMission;
import de.rolandsw.schedulemc.mission.PlayerMissionManager;
import de.rolandsw.schedulemc.mission.MissionDefinition;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.behavior.NPCBehaviorEngine;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Tick-basierter NPC-Naeherungspruef-System (Phase 5a).
 *
 * Laueft alle 10 Server-Ticks und prueft fuer alle Spieler mit aktiver Mission:
 *
 * - GOTO_NPC-Tracking-Key:  Distanz zum Ziel-NPC kleiner Radius → trackProgress("npc_talked", 1)
 * - TAIL_NPC-Tracking-Key:  Distanz im Fenster [2, max_distance] fuer duration Sekunden;
 *                            Unterschreitung → onSuspiciousActivity(); Ueberschreitung → Reset
 *
 * Aufruf aus {@code ScheduleMC.onServerTick()}.
 */
public class NPCProximityChecker {

    private static int globalTickCounter = 0;
    private static final int TICK_INTERVAL = 10;

    /** Wie viele Sekunden der Spieler bereits im TAIL-Fenster ist (pro Mission). */
    private static final Map<String, Integer> TAIL_SECONDS = new ConcurrentHashMap<>();

    /** Ob der GOTO_NPC-Check fuer diese Mission bereits ausgeloest hat (Einmal-Trigger). */
    private static final java.util.Set<String> GOTO_NPC_TRIGGERED =
        java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void tick(MinecraftServer server) {
        globalTickCounter++;
        if (globalTickCounter < TICK_INTERVAL) return;
        globalTickCounter = 0;

        PlayerMissionManager mgr = PlayerMissionManager.getInstance();
        if (mgr == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            List<PlayerMission> missions = mgr.getPlayerMissions(player.getUUID());
            for (PlayerMission mission : missions) {
                if (mission.getStatus() != MissionStatus.ACTIVE) continue;

                MissionDefinition def = mission.getDefinition();
                if (def == null) continue;

                String key = def.getTrackingKey();
                if (key == null) continue;

                if (key.startsWith("goto_npc:")) {
                    checkGotoNpc(server, player, mission, key);
                } else if (key.startsWith("tail_npc:")) {
                    checkTailNpc(server, player, mission, key);
                }
            }
        }
    }

    // ───────────────────────────────────────────────────────────
    // GOTO_NPC: Naeherung → einmaliger Tracking-Event
    // ───────────────────────────────────────────────────────────

    /**
     * Tracking-Key Format: {@code goto_npc:<npcName>:<radiusBlocks>}
     * Beispiel: {@code goto_npc:Kommissar_Vogel:5}
     */
    private static void checkGotoNpc(MinecraftServer server, ServerPlayer player,
                                      PlayerMission mission, String key) {
        String stateKey = player.getUUID() + "_" + mission.getMissionId();
        if (GOTO_NPC_TRIGGERED.contains(stateKey)) return;

        String[] parts = key.split(":", 3);
        if (parts.length < 2) return;
        String npcName = parts[1];
        double radius = parts.length >= 3 ? parseDouble(parts[2], 5.0) : 5.0;

        CustomNPCEntity npc = findNpc(server, npcName);
        if (npc == null) return;

        if (player.distanceTo(npc) <= radius) {
            GOTO_NPC_TRIGGERED.add(stateKey);
            MissionEventBridge.fireTransactionCompleted(player); // generic event um Fortschritt zu tracken
            PlayerMissionManager mgr = PlayerMissionManager.getInstance();
            if (mgr != null) {
                mgr.trackProgress(player, "npc_talked", 1);
                mgr.trackProgress(player, key, 1); // direkter Key-Match
            }
        }
    }

    // ───────────────────────────────────────────────────────────
    // TAIL_NPC: Beschattung im Abstandsfenster
    // ───────────────────────────────────────────────────────────

    /**
     * Tracking-Key Format: {@code tail_npc:<npcName>:<maxDistance>:<durationSeconds>}
     * Beispiel: {@code tail_npc:Dealer_Rico:15:30}
     *
     * Regeln:
     * - Distanz < 2 → zu nah → onSuspiciousActivity(); Reset Timer
     * - 2 ≤ Distanz ≤ maxDistance → im Fenster → Timer hochzaehlen
     * - Distanz > maxDistance → zu weit → Reset Timer
     * - Timer ≥ duration → Erfolg → trackProgress(key, 1)
     */
    private static void checkTailNpc(MinecraftServer server, ServerPlayer player,
                                      PlayerMission mission, String key) {
        String stateKey = player.getUUID() + "_" + mission.getMissionId() + "_tail";

        String[] parts = key.split(":", 4);
        if (parts.length < 2) return;
        String npcName = parts[1];
        double maxDist = parts.length >= 3 ? parseDouble(parts[2], 15.0) : 15.0;
        int durationSec = parts.length >= 4 ? parseInt(parts[3], 30) : 30;

        CustomNPCEntity npc = findNpc(server, npcName);
        if (npc == null) return;

        double dist = player.distanceTo(npc);

        if (dist < 2.0) {
            // Zu nah – Verdacht erregt
            TAIL_SECONDS.put(stateKey, 0);
            NPCBehaviorEngine engine = npc.getBehaviorEngine();
            if (engine != null) {
                try {
                    engine.onSuspiciousActivity(player, "beschattet");
                } catch (Exception ex) {
                    TAIL_SECONDS.put(stateKey, 0);
                }
            }
        } else if (dist <= maxDist) {
            // Im gueltigen Fenster – Timer hochzaehlen (alle 10 Ticks = 0.5s → ×2 fuer Sekunden)
            int current = TAIL_SECONDS.getOrDefault(stateKey, 0) + 1;
            TAIL_SECONDS.put(stateKey, current);

            if (current * (TICK_INTERVAL / 20.0) >= durationSec) {
                // Erfolg
                TAIL_SECONDS.remove(stateKey);
                PlayerMissionManager mgr = PlayerMissionManager.getInstance();
                if (mgr != null) mgr.trackProgress(player, key, 1);
            }
        } else {
            // Zu weit – Reset
            TAIL_SECONDS.put(stateKey, 0);
        }
    }

    // ───────────────────────────────────────────────────────────
    // HELPER
    // ───────────────────────────────────────────────────────────

    private static CustomNPCEntity findNpc(MinecraftServer server, String npcName) {
        Integer entityId = NPCNameRegistry.getEntityId(npcName);
        if (entityId == null) return null;

        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(entityId);
            if (entity instanceof CustomNPCEntity npc) return npc;
        }
        return null;
    }

    private static double parseDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return def; }
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }

    /**
     * Bereinigt State fuer einen Spieler (beim Logout / Mission-Abandon).
     */
    public static void clearPlayerState(java.util.UUID playerUUID) {
        String prefix = playerUUID.toString();
        TAIL_SECONDS.entrySet().removeIf(e -> e.getKey().startsWith(prefix));
        GOTO_NPC_TRIGGERED.removeIf(s -> s.startsWith(prefix));
    }
}
