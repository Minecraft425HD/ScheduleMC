package de.rolandsw.schedulemc.npc.goals;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Goal: NPC geht zu seiner Arbeitsstätte (einstellbare Zeiten)
 */
public class MoveToWorkGoal extends Goal {

    private final CustomNPCEntity npc;
    private BlockPos workPos;
    private static final double ARRIVAL_THRESHOLD = 2.0D;
    private static final int RECALCULATE_INTERVAL = 100; // Alle 5 Sekunden neu berechnen
    private int tickCounter = 0;

    public MoveToWorkGoal(CustomNPCEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Nur wenn Movement aktiviert ist
        if (!npc.getNpcData().getBehavior().canMove()) {
            return false;
        }

        // Nur wenn eine Work Location gesetzt ist
        BlockPos work = npc.getNpcData().getWorkLocation();
        if (work == null) {
            return false;
        }

        // Prüfe ob Arbeitszeit ist (einstellbare Zeiten)
        Level level = npc.level();
        long dayTime = level.getDayTime() % 24000;
        long workStart = npc.getNpcData().getWorkStartTime();
        long workEnd = npc.getNpcData().getWorkEndTime();

        boolean isWorkTime = isTimeBetween(dayTime, workStart, workEnd);

        if (!isWorkTime) {
            return false;
        }

        // Nur wenn NPC nicht bereits bei der Arbeit ist
        this.workPos = work;
        double distanceToWork = npc.position().distanceTo(
            new Vec3(workPos.getX() + 0.5, workPos.getY(), workPos.getZ() + 0.5)
        );

        return distanceToWork > ARRIVAL_THRESHOLD;
    }

    @Override
    public boolean canContinueToUse() {
        // Weitermachen solange es Arbeitszeit ist und NPC nicht angekommen
        if (workPos == null) {
            return false;
        }

        Level level = npc.level();
        long dayTime = level.getDayTime() % 24000;
        long workStart = npc.getNpcData().getWorkStartTime();
        long workEnd = npc.getNpcData().getWorkEndTime();

        boolean isWorkTime = isTimeBetween(dayTime, workStart, workEnd);

        if (!isWorkTime) {
            return false;
        }

        double distanceToWork = npc.position().distanceTo(
            new Vec3(workPos.getX() + 0.5, workPos.getY(), workPos.getZ() + 0.5)
        );

        return distanceToWork > ARRIVAL_THRESHOLD;
    }

    @Override
    public void tick() {
        if (workPos == null) {
            return;
        }

        tickCounter++;

        // Alle 5 Sekunden Pfad neu berechnen
        if (tickCounter >= RECALCULATE_INTERVAL) {
            tickCounter = 0;
            npc.getNavigation().moveTo(
                workPos.getX() + 0.5,
                workPos.getY(),
                workPos.getZ() + 0.5,
                npc.getNpcData().getBehavior().getMovementSpeed()
            );
        }
    }

    @Override
    public void start() {
        if (workPos != null) {
            npc.getNavigation().moveTo(
                workPos.getX() + 0.5,
                workPos.getY(),
                workPos.getZ() + 0.5,
                npc.getNpcData().getBehavior().getMovementSpeed()
            );
        }
    }

    @Override
    public void stop() {
        npc.getNavigation().stop();
        tickCounter = 0;
    }

    /**
     * Hilfsmethode: Prüft ob eine Zeit zwischen zwei Zeitpunkten liegt
     */
    private boolean isTimeBetween(long time, long start, long end) {
        if (start <= end) {
            return time >= start && time < end;
        } else {
            // Zeit geht über Mitternacht
            return time >= start || time < end;
        }
    }
}
