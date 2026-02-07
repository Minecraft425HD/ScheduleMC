package de.rolandsw.schedulemc.npc.driving;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * Repraesentiert eine einzelne NPC-Fahrt entlang eines RoadGraph-Pfads.
 * Wird vom NPCDrivingScheduler im Round-Robin-Verfahren geupdated.
 */
public class NPCDrivingTask {

    private final CustomNPCEntity npc;
    private final List<BlockPos> path;
    private final BlockPos destination;
    private int currentIndex = 0;
    private int lastUpdateTick = 0;
    private boolean finished = false;

    private static final float BLOCKS_PER_TICK = 0.6f;

    public NPCDrivingTask(CustomNPCEntity npc, List<BlockPos> path, BlockPos destination) {
        this.npc = npc;
        this.path = path;
        this.destination = destination;
    }

    /**
     * Bewegt den NPC entlang des Pfads.
     * Kompensiert uebersprungene Ticks durch den Round-Robin-Scheduler.
     *
     * @param currentTick Der aktuelle Server-Tick
     */
    public void advance(int currentTick) {
        if (finished || path.isEmpty()) {
            finished = true;
            return;
        }

        // Berechne wieviele Ticks seit dem letzten Update vergangen sind
        int ticksElapsed = lastUpdateTick == 0 ? 1 : (currentTick - lastUpdateTick);
        lastUpdateTick = currentTick;

        // Kompensiere: Bei laengerem Intervall mehr Bloecke pro Schritt
        int stepsToAdvance = Math.max(1, (int) (BLOCKS_PER_TICK * ticksElapsed));
        currentIndex = Math.min(currentIndex + stepsToAdvance, path.size() - 1);

        // Setze NPC-Position
        BlockPos targetBlock = path.get(currentIndex);
        npc.setPos(
                targetBlock.getX() + 0.5,
                targetBlock.getY(),
                targetBlock.getZ() + 0.5
        );

        // Berechne Yaw-Richtung zum naechsten Punkt
        if (currentIndex < path.size() - 1) {
            BlockPos nextBlock = path.get(Math.min(currentIndex + 1, path.size() - 1));
            float yaw = calculateYaw(targetBlock, nextBlock);
            npc.setYRot(yaw);
            npc.setYHeadRot(yaw);
            npc.setVehicleYaw(yaw);
        }

        // Navigation stoppen damit Minecraft-Pathfinding nicht interferiert
        npc.getNavigation().stop();

        // Pruefe ob Ziel erreicht
        if (currentIndex >= path.size() - 1) {
            finished = true;
        }
    }

    private float calculateYaw(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
    }

    public boolean isFinished() {
        return finished;
    }

    public CustomNPCEntity getNpc() {
        return npc;
    }

    public BlockPos getDestination() {
        return destination;
    }

    public int getPathLength() {
        return path.size();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }
}
