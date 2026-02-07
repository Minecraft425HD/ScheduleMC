package de.rolandsw.schedulemc.npc.driving;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.managers.NPCEntityRegistry;
import de.rolandsw.schedulemc.mapview.navigation.graph.RoadGraph;
import de.rolandsw.schedulemc.mapview.navigation.graph.RoadNavigationService;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.behavior.BehaviorState;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Zentraler Budget-Scheduler fuer NPC-Autofahrten.
 *
 * Performance-Garantie: Maximal BUDGET_PER_TICK NPC-Updates pro Server-Tick,
 * unabhaengig davon wie viele NPCs gleichzeitig fahren.
 *
 * Nutzt Round-Robin: Jeder fahrende NPC wird fair abwechselnd geupdated.
 * Pfade werden ueber den bestehenden RoadGraph (Spieler-Navigation) berechnet und gecacht.
 */
@Mod.EventBusSubscriber
public class NPCDrivingScheduler {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Max Anzahl NPC-Bewegungs-Updates pro Server-Tick */
    private static final int BUDGET_PER_TICK = 3;

    /** Minimale Entfernung zum Ziel damit ein NPC faehrt statt laeuft */
    private static final double MIN_DRIVING_DISTANCE = 80.0;

    /** Intervall fuer Vehicle-Assignment-Pruefung (alle 5 Minuten) */
    private static final int ASSIGNMENT_CHECK_INTERVAL = 6000;

    private static final Deque<NPCDrivingTask> activeTasks = new ConcurrentLinkedDeque<>();
    private static final Map<UUID, NPCDrivingTask> tasksByNpc = new ConcurrentHashMap<>();
    private static int tickCounter = 0;
    private static int assignmentCheckCounter = 0;

    /**
     * Startet eine NPC-Fahrt entlang des RoadGraph-Pfads.
     *
     * @param npc Der fahrende NPC
     * @param destination Zielposition
     * @return true wenn die Fahrt gestartet wurde
     */
    public static boolean startDriving(CustomNPCEntity npc, BlockPos destination) {
        // Bereits am Fahren?
        if (tasksByNpc.containsKey(npc.getUUID())) {
            return false;
        }

        // RoadGraph verfuegbar?
        RoadNavigationService navService = RoadNavigationService.getInstance();
        if (navService == null) {
            return false;
        }

        RoadGraph graph = navService.getGraph();
        if (graph == null || graph.isEmpty()) {
            return false;
        }

        // Pfad berechnen
        List<BlockPos> path = graph.findPath(npc.blockPosition(), destination);
        if (path.isEmpty() || path.size() < 3) {
            return false; // Zu kurz fuer Autofahrt
        }

        // Task erstellen und registrieren
        NPCDrivingTask task = new NPCDrivingTask(npc, path, destination);
        activeTasks.offerLast(task);
        tasksByNpc.put(npc.getUUID(), task);

        // NPC in Fahrmodus setzen
        npc.setDriving(true);
        npc.getNavigation().stop();

        // Fahrzeugfarbe setzen
        if (npc.level() instanceof ServerLevel serverLevel) {
            NPCVehicleAssignment assignment = NPCVehicleAssignment.get(serverLevel);
            npc.setVehicleColor(assignment.getVehicleColor(npc.getNpcData().getNpcUUID()));
        }

        LOGGER.debug("[NPCDrivingScheduler] Started driving: {} (path: {} blocks)",
                npc.getNpcName(), path.size());
        return true;
    }

    /**
     * Stoppt die Fahrt eines NPCs
     */
    public static void stopDriving(CustomNPCEntity npc) {
        NPCDrivingTask task = tasksByNpc.remove(npc.getUUID());
        if (task != null) {
            activeTasks.remove(task);
        }
        npc.setDriving(false);
    }

    /**
     * Prueft ob ein NPC gerade faehrt
     */
    public static boolean isDriving(UUID npcId) {
        return tasksByNpc.containsKey(npcId);
    }

    /**
     * Prueft ob ein NPC fuer eine Fahrt in Frage kommt
     */
    public static boolean canDrive(CustomNPCEntity npc, BlockPos destination) {
        if (npc.level().isClientSide) return false;
        if (tasksByNpc.containsKey(npc.getUUID())) return false;

        // Distanz pruefen
        double distance = npc.blockPosition().distSqr(destination);
        if (distance < MIN_DRIVING_DISTANCE * MIN_DRIVING_DISTANCE) {
            return false;
        }

        // Hat dieser NPC ein Fahrzeug?
        if (!(npc.level() instanceof ServerLevel serverLevel)) return false;
        NPCVehicleAssignment assignment = NPCVehicleAssignment.get(serverLevel);
        return assignment.hasVehicle(npc.getNpcData().getNpcUUID());
    }

    // ═══════════════════════════════════════════════════════════
    // SERVER TICK
    // ═══════════════════════════════════════════════════════════

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        EventHelper.handleServerTickEnd(event, NPCDrivingScheduler::tick);
    }

    private static void tick(MinecraftServer server) {
        tickCounter++;

        // Vehicle-Assignments periodisch aktualisieren
        assignmentCheckCounter++;
        if (assignmentCheckCounter >= ASSIGNMENT_CHECK_INTERVAL) {
            assignmentCheckCounter = 0;
            updateVehicleAssignments(server);
        }

        // Budget-basiertes Round-Robin Update
        int processed = 0;
        int tasksToProcess = Math.min(BUDGET_PER_TICK, activeTasks.size());

        for (int i = 0; i < tasksToProcess; i++) {
            NPCDrivingTask task = activeTasks.pollFirst();
            if (task == null) break;

            // Pruefe ob NPC noch existiert/geladen ist
            if (task.getNpc().isRemoved() || !task.getNpc().isAlive()) {
                tasksByNpc.remove(task.getNpc().getUUID());
                task.getNpc().setDriving(false);
                continue;
            }

            // NPC bewegen
            task.advance(tickCounter);
            processed++;

            if (task.isFinished()) {
                // Fahrt beendet
                tasksByNpc.remove(task.getNpc().getUUID());
                task.getNpc().setDriving(false);
                LOGGER.debug("[NPCDrivingScheduler] Finished driving: {}", task.getNpc().getNpcName());
            } else {
                // Zurueck ans Ende der Queue
                activeTasks.offerLast(task);
            }
        }
    }

    /**
     * Aktualisiert die Fahrzeug-Zuweisungen fuer alle geladenen NPCs
     */
    private static void updateVehicleAssignments(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            Collection<CustomNPCEntity> npcs = NPCEntityRegistry.getAllNPCs(level);
            if (npcs == null || npcs.isEmpty()) continue;

            List<UUID> npcIds = new ArrayList<>();
            for (CustomNPCEntity npc : npcs) {
                npcIds.add(npc.getNpcData().getNpcUUID());
            }

            NPCVehicleAssignment assignment = NPCVehicleAssignment.get(level);
            assignment.ensureAssignments(npcIds);
        }
    }

    /**
     * Gibt die Anzahl der aktuell fahrenden NPCs zurueck
     */
    public static int getActiveDrivingCount() {
        return activeTasks.size();
    }

    /**
     * Bereinigt alle Tasks (z.B. bei Server-Stop)
     */
    public static void clearAll() {
        for (NPCDrivingTask task : activeTasks) {
            task.getNpc().setDriving(false);
        }
        activeTasks.clear();
        tasksByNpc.clear();
        tickCounter = 0;
        assignmentCheckCounter = 0;
    }
}
