package de.rolandsw.schedulemc.mission.scenario;

import de.rolandsw.schedulemc.gang.scenario.MissionScenario;
import de.rolandsw.schedulemc.gang.scenario.ObjectiveType;
import de.rolandsw.schedulemc.gang.scenario.ScenarioManager;
import de.rolandsw.schedulemc.gang.scenario.ScenarioObjective;
import de.rolandsw.schedulemc.managers.NPCNameRegistry;
import de.rolandsw.schedulemc.mission.MissionStatus;
import de.rolandsw.schedulemc.mission.PlayerMission;
import de.rolandsw.schedulemc.mission.PlayerMissionManager;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tick-basierter Executor fuer STORY_* Spieler-Missionen.
 *
 * Laeuft alle 20 Server-Ticks (= 1 Sekunde) und prueft fuer jeden Spieler
 * mit aktiver STORY-Mission, ob der aktuell ausstehende Szenario-Block
 * abgeschlossen werden kann. Wird aus {@code ScheduleMC.onServerTick()} aufgerufen.
 *
 * Block-Typen mit passiver Erkennung (kein Event erforderlich):
 * - GOTO_LOCATION: Spieler-Position in Radius
 * - GOTO_NPC / TALK_TO_NPC: Naeherung an NPC (delegiert an NPCProximityChecker)
 * - SURVIVE_TIME: Countdown (Spieler muss am Leben bleiben)
 * - PROTECT_NPC: Ueberwacht NPC-HP
 * - GOTO_PLOT: Spieler in Plot-Bereich
 * - COMMENT / PARALLEL_HINT: sofort ueberspringen
 * - START: sofort ueberspringen
 * - REWARD: Mission automatisch abschliessen
 */
public class PlayerMissionScenarioExecutor {

    /** Tickzaehler pro aktiver Player-Mission (fuer zeitbasierte Objectives). */
    private static final Map<String, Integer> TICK_COUNTERS = new ConcurrentHashMap<>();

    /** Aktuell aktiver Objective-Block pro spieler-missionsId. */
    private static final Map<String, String> CURRENT_OBJECTIVE = new ConcurrentHashMap<>();

    // Aufruf-Intervall: alle 20 Ticks (1 Sekunde)
    private static int globalTickCounter = 0;
    private static final int TICK_INTERVAL = 20;

    /**
     * Wird von ScheduleMC.onServerTick() aufgerufen.
     */
    public static void tick(MinecraftServer server) {
        globalTickCounter++;
        if (globalTickCounter < TICK_INTERVAL) return;
        globalTickCounter = 0;

        PlayerMissionManager mgr = PlayerMissionManager.getInstance();
        if (mgr == null) return;

        ScenarioManager scenarioManager = ScenarioManager.getInstance();
        if (scenarioManager == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            List<PlayerMission> missions = mgr.getPlayerMissions(player.getUUID());
            for (PlayerMission mission : missions) {
                if (mission.getStatus() != MissionStatus.ACTIVE) continue;

                // Nur STORY_*-Missionen haben Szenarien
                MissionScenario scenario = scenarioManager.getScenario(mission.getDefinitionId());
                if (scenario == null) continue;
                if (scenario.getMissionType() == null || !scenario.getMissionType().startsWith("STORY_")) continue;

                tickMission(server, player, mission, scenario, mgr);
            }
        }
    }

    // ───────────────────────────────────────────────────────────
    // MISSION TICK
    // ───────────────────────────────────────────────────────────

    private static void tickMission(MinecraftServer server, ServerPlayer player,
                                    PlayerMission mission, MissionScenario scenario,
                                    PlayerMissionManager mgr) {
        String stateKey = player.getUUID() + "_" + mission.getMissionId();

        // Aktuellen Objective-Block bestimmen
        String currentObjId = CURRENT_OBJECTIVE.computeIfAbsent(stateKey,
            k -> findStartObjectiveId(scenario));

        if (currentObjId == null) return;

        ScenarioObjective obj = findObjectiveById(scenario, currentObjId);
        if (obj == null) {
            CURRENT_OBJECTIVE.remove(stateKey);
            return;
        }

        boolean advance = evaluateObjective(server, player, mission, obj, stateKey);
        if (advance) {
            advanceToNext(player, mission, scenario, obj, stateKey, mgr);
        }
    }

    // ───────────────────────────────────────────────────────────
    // OBJECTIVE EVALUATION
    // ───────────────────────────────────────────────────────────

    /**
     * Prueft ob ein Objective abgeschlossen ist.
     * @return true wenn zum naechsten Block weitergeschaltet werden soll
     */
    private static boolean evaluateObjective(MinecraftServer server, ServerPlayer player,
                                              PlayerMission mission, ScenarioObjective obj,
                                              String stateKey) {
        ObjectiveType type = obj.getType();
        switch (type) {
            // ── Sofort-Bloecke ─────────────────────────────────
            case START, COMMENT, PARALLEL_HINT, SECRET_BLOCK_LIST -> { return true; }

            // ── Position / Naeherung ───────────────────────────
            case GOTO_LOCATION, GOTO_PLOT -> {
                return checkPlayerNearLocation(player, obj);
            }
            case GOTO_NPC, TALK_TO_NPC -> {
                return checkPlayerNearNpc(server, player, obj);
            }

            // ── Zeit-Bloecke ───────────────────────────────────
            case SURVIVE_TIME -> {
                return tickDown(stateKey, parseDuration(obj.getParam("duration_seconds"), 30));
            }
            case MISSION_TIMER -> {
                // Timer-Ablauf = Mission-Fail (negative Bedingung – hier vereinfacht: ablaufen = fail)
                boolean expired = tickDown(stateKey, parseDuration(obj.getParam("duration_seconds"), 60));
                if (expired) {
                    failMission(player, mission, "Zeit abgelaufen!");
                }
                return false; // Timer-Block blockiert nie weiter (bleibt aktiv bis Ablauf)
            }

            // ── NPC-Schutz ────────────────────────────────────
            case PROTECT_NPC -> {
                return checkProtectNpc(server, player, obj, stateKey);
            }

            // ── Belohnung / Abschluss ─────────────────────────
            case REWARD -> {
                // Automatischer Missions-Abschluss wenn REWARD-Block erreicht
                autoComplete(player, mission);
                return false;
            }

            // ── Logik-Bloecke ─────────────────────────────────
            case CONDITION_BRANCH -> {
                // Vereinfacht: immer if_true-Pfad nehmen (vollstaendige Implementierung via Event-Flags)
                return true;
            }
            case LOOP_REPEAT -> {
                int remaining = TICK_COUNTERS.getOrDefault(stateKey + "_loop", 0);
                if (remaining <= 0) {
                    String countStr = obj.getParam("count");
                    remaining = parseDuration(countStr, 1);
                }
                remaining--;
                TICK_COUNTERS.put(stateKey + "_loop", remaining);
                return remaining <= 0;
            }

            // ── Standard: nicht automatisch auswertbar ─────────
            default -> { return false; }
        }
    }

    // ───────────────────────────────────────────────────────────
    // HELPER: Positionspruefung
    // ───────────────────────────────────────────────────────────

    private static boolean checkPlayerNearLocation(ServerPlayer player, ScenarioObjective obj) {
        try {
            double x = Double.parseDouble(obj.getParam("x"));
            double y = Double.parseDouble(obj.getParam("y"));
            double z = Double.parseDouble(obj.getParam("z"));
            double radius = parseDuration(obj.getParam("radius"), 10);

            double dx = player.getX() - x;
            double dy = player.getY() - y;
            double dz = player.getZ() - z;
            return (dx * dx + dy * dy + dz * dz) <= (radius * radius);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean checkPlayerNearNpc(MinecraftServer server, ServerPlayer player,
                                               ScenarioObjective obj) {
        String npcName = obj.getParam("npc_name");
        if (npcName == null || npcName.isEmpty()) return false;

        Integer entityId = NPCNameRegistry.getEntityId(npcName);
        if (entityId == null) return false;

        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(entityId);
            if (entity instanceof CustomNPCEntity npc) {
                double radius = parseDuration(obj.getParam("radius"), 5);
                double dist = player.distanceTo(npc);
                return dist <= radius;
            }
        }
        return false;
    }

    private static boolean checkProtectNpc(MinecraftServer server, ServerPlayer player,
                                            ScenarioObjective obj, String stateKey) {
        String npcName = obj.getParam("npc_name");
        int duration = parseDuration(obj.getParam("duration_seconds"), 60);

        if (npcName != null && !npcName.isEmpty()) {
            Integer entityId = NPCNameRegistry.getEntityId(npcName);
            if (entityId != null) {
                for (ServerLevel level : server.getAllLevels()) {
                    Entity entity = level.getEntity(entityId);
                    if (entity == null || entity.isRemoved()) {
                        failMission(player, null, "Zu schuetzender NPC wurde eliminiert!");
                        return false;
                    }
                }
            }
        }
        return tickDown(stateKey, duration);
    }

    // ───────────────────────────────────────────────────────────
    // HELPER: Tick-Counter
    // ───────────────────────────────────────────────────────────

    /** Zaehlt einen Sekunden-Counter herunter; gibt true zurueck wenn 0 erreicht. */
    private static boolean tickDown(String stateKey, int totalSeconds) {
        int remaining = TICK_COUNTERS.getOrDefault(stateKey, totalSeconds);
        remaining--;
        if (remaining <= 0) {
            TICK_COUNTERS.remove(stateKey);
            return true;
        }
        TICK_COUNTERS.put(stateKey, remaining);
        return false;
    }

    // ───────────────────────────────────────────────────────────
    // HELPER: Navigation
    // ───────────────────────────────────────────────────────────

    private static void advanceToNext(ServerPlayer player, PlayerMission mission,
                                       MissionScenario scenario, ScenarioObjective current,
                                       String stateKey, PlayerMissionManager _mgr) {
        String nextId = current.getNextObjectiveId();
        TICK_COUNTERS.remove(stateKey);

        if (nextId == null || nextId.isEmpty()) {
            // Kein Nachfolger → Mission-Ende
            autoComplete(player, mission);
            CURRENT_OBJECTIVE.remove(stateKey);
            return;
        }

        ScenarioObjective next = findObjectiveById(scenario, nextId);
        if (next == null) {
            CURRENT_OBJECTIVE.remove(stateKey);
            return;
        }

        CURRENT_OBJECTIVE.put(stateKey, nextId);

        // Spieler-Nachricht mit Ziel-Information
        String label = next.getType().getDisplayName();
        String summary = next.getParamSummary();
        player.sendSystemMessage(Component.literal(
            "§e[Mission] §fNaechstes Ziel: §b" + label + (summary.isEmpty() ? "" : " §7(" + summary + ")")));
    }

    private static void autoComplete(ServerPlayer player, PlayerMission mission) {
        PlayerMissionManager mgr = PlayerMissionManager.getInstance();
        if (mgr == null) return;

        boolean claimed = mgr.claimMission(player, mission.getMissionId());
        if (claimed) {
            player.sendSystemMessage(Component.literal(
                "§a§l[Mission abgeschlossen] §r§a" + mission.getDefinition().getTitle()
                + " §f– Belohnung erhalten!"));
        }
        TICK_COUNTERS.entrySet().removeIf(e -> e.getKey().startsWith(player.getUUID() + "_" + mission.getMissionId()));
        CURRENT_OBJECTIVE.remove(player.getUUID() + "_" + mission.getMissionId());
    }

    private static void failMission(ServerPlayer player, PlayerMission mission, String reason) {
        player.sendSystemMessage(Component.literal(
            "§c[Mission gescheitert] §f" + reason));
        if (mission != null) {
            TICK_COUNTERS.entrySet().removeIf(e -> e.getKey().startsWith(player.getUUID() + "_" + mission.getMissionId()));
            CURRENT_OBJECTIVE.remove(player.getUUID() + "_" + mission.getMissionId());
        }
    }

    // ───────────────────────────────────────────────────────────
    // HELPER: Scenario-Traversal
    // ───────────────────────────────────────────────────────────

    private static String findStartObjectiveId(MissionScenario scenario) {
        for (ScenarioObjective obj : scenario.getObjectives()) {
            if (obj.getType() == ObjectiveType.START) return obj.getId();
        }
        // Fallback: erstes Objective
        if (!scenario.getObjectives().isEmpty()) return scenario.getObjectives().get(0).getId();
        return null;
    }

    private static ScenarioObjective findObjectiveById(MissionScenario scenario, String id) {
        for (ScenarioObjective obj : scenario.getObjectives()) {
            if (obj.getId().equals(id)) return obj;
        }
        return null;
    }

    private static int parseDuration(String value, int defaultVal) {
        if (value == null || value.isEmpty()) return defaultVal;
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return defaultVal; }
    }

    /**
     * Bereinigt State fuer einen Spieler (beim Logout / Mission-Abandon).
     */
    public static void clearPlayerState(UUID playerUUID) {
        String prefix = playerUUID.toString();
        TICK_COUNTERS.entrySet().removeIf(e -> e.getKey().startsWith(prefix));
        CURRENT_OBJECTIVE.entrySet().removeIf(e -> e.getKey().startsWith(prefix));
    }

    /**
     * Liefert alle aktuell aktiven Objective-Blöcke eines Spielers.
     * Wird u.a. für temporäre missionsbasierte Zugriffsregeln verwendet.
     */
    public static List<ScenarioObjective> getActiveObjectivesForPlayer(UUID playerUUID) {
        if (playerUUID == null) {
            return Collections.emptyList();
        }

        PlayerMissionManager missionManager = PlayerMissionManager.getInstance();
        ScenarioManager scenarioManager = ScenarioManager.getInstance();
        if (missionManager == null || scenarioManager == null) {
            return Collections.emptyList();
        }

        String prefix = playerUUID + "_";
        List<ScenarioObjective> objectives = new ArrayList<>();

        for (Map.Entry<String, String> entry : CURRENT_OBJECTIVE.entrySet()) {
            String stateKey = entry.getKey();
            if (!stateKey.startsWith(prefix)) continue;

            String missionId = stateKey.substring(prefix.length());
            PlayerMission mission = missionManager.getPlayerMissions(playerUUID).stream()
                .filter(m -> m.getMissionId().equals(missionId) && m.getStatus() == MissionStatus.ACTIVE)
                .findFirst()
                .orElse(null);
            if (mission == null) continue;

            MissionScenario scenario = scenarioManager.getScenario(mission.getDefinitionId());
            if (scenario == null) continue;

            ScenarioObjective objective = findObjectiveById(scenario, entry.getValue());
            if (objective != null) {
                objectives.add(objective);
            }
        }

        return objectives;
    }
}
