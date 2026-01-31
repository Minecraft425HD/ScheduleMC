package de.rolandsw.schedulemc.npc.goals;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Goal: NPC geht in seiner Freizeit zu einem von bis zu 10 festgelegten Orten
 * und bleibt dort in einem Umkreis von 15 Blöcken.
 * Alle 5 Minuten wechselt der NPC zufällig zu einem anderen Freizeitort.
 * BEWOHNER arbeiten nicht und haben nur Freizeit + Heimzeit.
 */
public class MoveToLeisureGoal extends Goal {

    private final CustomNPCEntity npc;
    private BlockPos targetLeisurePos;
    private BlockPos currentWanderTarget;

    private static final double ARRIVAL_THRESHOLD = 2.0D;
    private static final int LEISURE_RADIUS = 15; // Umkreis von 15 Blöcken
    private static final int RECALCULATE_INTERVAL = 100; // Alle 5 Sekunden neu berechnen
    private static final int WANDER_INTERVAL = 200; // Alle 10 Sekunden neuen Wander-Punkt wählen
    private static final int LOCATION_CHANGE_INTERVAL = 6000; // Alle 5 Minuten (6000 Ticks) Freizeitort wechseln

    private int tickCounter = 0;
    private int wanderCounter = 0;
    private int locationChangeCounter = 0;

    public MoveToLeisureGoal(CustomNPCEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // NICHT für Polizei-NPCs (die haben eigene Goals)
        if (npc.getNpcData().getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.POLIZEI) {
            return false;
        }

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
            targetLeisurePos = leisureLocations.get(ThreadLocalRandom.current().nextInt(leisureLocations.size()));
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
        locationChangeCounter++;

        // Alle 5 Minuten: Wechsle zu einem anderen Freizeitort
        List<BlockPos> leisureLocations = npc.getNpcData().getLeisureLocations();
        if (locationChangeCounter >= LOCATION_CHANGE_INTERVAL && leisureLocations.size() > 1) {
            locationChangeCounter = 0;
            // Wähle einen anderen Freizeitort (nicht den aktuellen)
            BlockPos newTarget;
            do {
                newTarget = leisureLocations.get(ThreadLocalRandom.current().nextInt(leisureLocations.size()));
            } while (newTarget.equals(targetLeisurePos) && leisureLocations.size() > 1);

            targetLeisurePos = newTarget;
            currentWanderTarget = null; // Reset Wander-Ziel
        }

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
        locationChangeCounter = 0;
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
        int offsetX = ThreadLocalRandom.current().nextInt(LEISURE_RADIUS * 2 + 1) - LEISURE_RADIUS;
        int offsetZ = ThreadLocalRandom.current().nextInt(LEISURE_RADIUS * 2 + 1) - LEISURE_RADIUS;

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

        // Für BEWOHNER: Freizeit = NICHT Heimzeit (sie arbeiten nicht)
        // Für VERKAEUFER: Freizeit = NICHT Arbeitszeit UND NICHT Heimzeit
        boolean isWorkTime = false;
        if (npc.getNpcData().getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.VERKAEUFER
            && npc.getNpcData().getWorkLocation() != null) {
            isWorkTime = isTimeBetween(dayTime, workStart, workEnd);
        }

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
