package de.rolandsw.schedulemc.npc.goals;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

/**
 * Goal: NPC geht in seiner Freizeit zu einem von 3 festgelegten Orten
 * und bleibt dort in einem Umkreis von 15 Blöcken
 */
public class MoveToLeisureGoal extends Goal {

    private final CustomNPCEntity npc;
    private final Random random;
    private BlockPos targetLeisurePos;
    private BlockPos currentWanderTarget;

    private static final double ARRIVAL_THRESHOLD = 2.0D;
    private static final int LEISURE_RADIUS = 15; // Umkreis von 15 Blöcken
    private static final int RECALCULATE_INTERVAL = 100; // Alle 5 Sekunden neu berechnen
    private static final int WANDER_INTERVAL = 200; // Alle 10 Sekunden neuen Wander-Punkt wählen

    private int tickCounter = 0;
    private int wanderCounter = 0;

    public MoveToLeisureGoal(CustomNPCEntity npc) {
        this.npc = npc;
        this.random = new Random();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Nur wenn Movement aktiviert ist
        if (!npc.getNpcData().getBehavior().canMove()) {
            return false;
        }

        // Nur wenn mindestens ein Freizeitort gesetzt ist
        List<BlockPos> leisureLocations = npc.getNpcData().getLeisureLocations();
        if (leisureLocations.isEmpty()) {
            return false;
        }

        // Prüfe ob Freizeit ist (nicht Arbeitszeit, nicht Heimzeit)
        if (!isLeisureTime()) {
            return false;
        }

        // Wähle zufälligen Freizeitort aus, wenn noch keiner gewählt wurde
        if (targetLeisurePos == null) {
            targetLeisurePos = leisureLocations.get(random.nextInt(leisureLocations.size()));
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        // Weitermachen solange es Freizeit ist
        if (!isLeisureTime()) {
            return false;
        }

        // Weitermachen solange mindestens ein Freizeitort definiert ist
        return !npc.getNpcData().getLeisureLocations().isEmpty();
    }

    @Override
    public void tick() {
        if (targetLeisurePos == null) {
            return;
        }

        tickCounter++;
        wanderCounter++;

        // Prüfe ob NPC im Umkreis des Freizeitortes ist
        double distanceToLeisure = npc.position().distanceTo(
            new Vec3(targetLeisurePos.getX() + 0.5, targetLeisurePos.getY(), targetLeisurePos.getZ() + 0.5)
        );

        // Wenn NPC noch nicht am Freizeitort ist, gehe dorthin
        if (distanceToLeisure > LEISURE_RADIUS) {
            if (tickCounter >= RECALCULATE_INTERVAL) {
                tickCounter = 0;
                npc.getNavigation().moveTo(
                    targetLeisurePos.getX() + 0.5,
                    targetLeisurePos.getY(),
                    targetLeisurePos.getZ() + 0.5,
                    npc.getNpcData().getBehavior().getMovementSpeed()
                );
            }
        } else {
            // NPC ist am Freizeitort - wandere im Umkreis
            if (wanderCounter >= WANDER_INTERVAL) {
                wanderCounter = 0;
                selectNewWanderTarget();
            }

            // Bewege zum aktuellen Wander-Ziel
            if (currentWanderTarget != null) {
                double distanceToWander = npc.position().distanceTo(
                    new Vec3(currentWanderTarget.getX() + 0.5, currentWanderTarget.getY(), currentWanderTarget.getZ() + 0.5)
                );

                if (distanceToWander > ARRIVAL_THRESHOLD) {
                    if (tickCounter >= RECALCULATE_INTERVAL) {
                        tickCounter = 0;
                        npc.getNavigation().moveTo(
                            currentWanderTarget.getX() + 0.5,
                            currentWanderTarget.getY(),
                            currentWanderTarget.getZ() + 0.5,
                            npc.getNpcData().getBehavior().getMovementSpeed()
                        );
                    }
                }
            }
        }
    }

    @Override
    public void start() {
        if (targetLeisurePos != null) {
            npc.getNavigation().moveTo(
                targetLeisurePos.getX() + 0.5,
                targetLeisurePos.getY(),
                targetLeisurePos.getZ() + 0.5,
                npc.getNpcData().getBehavior().getMovementSpeed()
            );
        }
    }

    @Override
    public void stop() {
        npc.getNavigation().stop();
        tickCounter = 0;
        wanderCounter = 0;
        targetLeisurePos = null;
        currentWanderTarget = null;
    }

    /**
     * Wählt einen neuen zufälligen Punkt im Umkreis des Freizeitortes
     */
    private void selectNewWanderTarget() {
        if (targetLeisurePos == null) {
            return;
        }

        // Wähle zufälligen Punkt im Umkreis von LEISURE_RADIUS
        int offsetX = random.nextInt(LEISURE_RADIUS * 2 + 1) - LEISURE_RADIUS;
        int offsetZ = random.nextInt(LEISURE_RADIUS * 2 + 1) - LEISURE_RADIUS;

        currentWanderTarget = targetLeisurePos.offset(offsetX, 0, offsetZ);
    }

    /**
     * Prüft ob gerade Freizeit ist
     */
    private boolean isLeisureTime() {
        Level level = npc.level();
        long dayTime = level.getDayTime() % 24000;

        long workStart = npc.getNpcData().getWorkStartTime();
        long workEnd = npc.getNpcData().getWorkEndTime();
        long homeTime = npc.getNpcData().getHomeTime();

        // Freizeit = NICHT Arbeitszeit UND NICHT Heimzeit
        boolean isWorkTime = isTimeBetween(dayTime, workStart, workEnd);
        boolean isHomeTime;

        // Heimzeit kann über Mitternacht gehen (z.B. 23000 bis workStart)
        if (homeTime > workStart) {
            // Heimzeit geht über Mitternacht (z.B. 23000 bis 0)
            isHomeTime = dayTime >= homeTime || dayTime < workStart;
        } else {
            // Heimzeit geht nicht über Mitternacht
            isHomeTime = isTimeBetween(dayTime, homeTime, workStart);
        }

        return !isWorkTime && !isHomeTime;
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
