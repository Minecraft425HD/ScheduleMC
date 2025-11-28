package de.rolandsw.schedulemc.npc.goals;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

/**
 * Goal: Polizist patrouilliert zwischen gesetzten Patrouillenpunkten
 * - Geht zum aktuellen Patrouillenpunkt
 * - Wartet dort für konfigurierte Zeit (Standard 3 Minuten)
 * - Bewegt sich asynchron im konfigurierbaren Radius (Standard 3 Blöcke)
 * - Nach Wartezeit: Nächster Punkt (Loop zurück zu Punkt 1 = Polizeistation)
 */
public class PolicePatrolGoal extends Goal {

    private final CustomNPCEntity npc;
    private BlockPos currentTarget;
    private BlockPos wanderTarget;
    private static final double ARRIVAL_THRESHOLD = 2.0D;
    private static final int RECALCULATE_INTERVAL = 100; // Alle 5 Sekunden neu berechnen
    private int tickCounter = 0;
    private int wanderTickCounter = 0;
    private static final int WANDER_INTERVAL = 200; // Alle 10 Sekunden neues Wander-Ziel
    private final Random random = new Random();
    private boolean hasArrived = false;

    public PolicePatrolGoal(CustomNPCEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // NUR für Polizei-NPCs aktiv
        if (npc.getNpcData().getNpcType() != de.rolandsw.schedulemc.npc.data.NPCType.POLIZEI) {
            return false;
        }

        // Nur wenn Movement aktiviert ist
        if (!npc.getNpcData().getBehavior().canMove()) {
            return false;
        }

        // Nur wenn Patrouillenpunkte gesetzt sind
        List<BlockPos> patrolPoints = npc.getNpcData().getPatrolPoints();
        if (patrolPoints.isEmpty()) {
            return false;
        }

        // Hole aktuellen Patrouillenpunkt
        int currentIndex = npc.getNpcData().getCurrentPatrolIndex();
        if (currentIndex >= patrolPoints.size()) {
            // Index zurücksetzen wenn außerhalb
            npc.getNpcData().setCurrentPatrolIndex(0);
            currentIndex = 0;
        }

        this.currentTarget = patrolPoints.get(currentIndex);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (currentTarget == null) {
            return false;
        }

        List<BlockPos> patrolPoints = npc.getNpcData().getPatrolPoints();
        if (patrolPoints.isEmpty()) {
            return false;
        }

        // Prüfe ob genug Zeit am Punkt gewartet wurde
        if (hasArrived) {
            long currentTime = npc.level().getGameTime();
            long arrivalTime = npc.getNpcData().getPatrolArrivalTime();
            long waitTimeTicks = ModConfigHandler.COMMON.POLICE_PATROL_WAIT_MINUTES.get() * 60 * 20; // Minuten → Ticks

            if (currentTime - arrivalTime >= waitTimeTicks) {
                // Wartezeit vorbei - gehe zum nächsten Punkt (OHNE zu stoppen!)
                npc.getNpcData().incrementPatrolIndex();

                // Hole nächsten Punkt
                int nextIndex = npc.getNpcData().getCurrentPatrolIndex();
                if (nextIndex >= patrolPoints.size()) {
                    npc.getNpcData().setCurrentPatrolIndex(0);
                    nextIndex = 0;
                }

                // Setze neues Ziel
                currentTarget = patrolPoints.get(nextIndex);
                hasArrived = false;
                tickCounter = 0;
                wanderTickCounter = 0;

                // Starte Bewegung zum nächsten Punkt
                npc.getNavigation().moveTo(
                    currentTarget.getX() + 0.5,
                    currentTarget.getY(),
                    currentTarget.getZ() + 0.5,
                    npc.getNpcData().getBehavior().getMovementSpeed()
                );
            }
        }

        return true;
    }

    @Override
    public void start() {
        if (currentTarget != null) {
            hasArrived = false;
            tickCounter = 0;
            wanderTickCounter = 0;
            wanderTarget = null;

            // Gehe zum Patrouillenpunkt
            npc.getNavigation().moveTo(
                currentTarget.getX() + 0.5,
                currentTarget.getY(),
                currentTarget.getZ() + 0.5,
                npc.getNpcData().getBehavior().getMovementSpeed()
            );
        }
    }

    @Override
    public void tick() {
        if (currentTarget == null) {
            return;
        }

        tickCounter++;
        wanderTickCounter++;

        // Prüfe ob am Ziel angekommen
        double distanceToTarget = npc.position().distanceTo(
            new Vec3(currentTarget.getX() + 0.5, currentTarget.getY(), currentTarget.getZ() + 0.5)
        );

        if (!hasArrived && distanceToTarget <= ARRIVAL_THRESHOLD) {
            // Gerade angekommen - speichere Ankunftszeit
            hasArrived = true;
            npc.getNpcData().setPatrolArrivalTime(npc.level().getGameTime());
            npc.getNavigation().stop();
        }

        if (hasArrived) {
            // Am Punkt angekommen - wandere im Radius herum
            if (wanderTickCounter >= WANDER_INTERVAL) {
                wanderTickCounter = 0;
                setNewWanderTarget();
            }
        } else {
            // Noch unterwegs zum Punkt - Pfad regelmäßig neu berechnen
            if (tickCounter >= RECALCULATE_INTERVAL) {
                tickCounter = 0;
                npc.getNavigation().moveTo(
                    currentTarget.getX() + 0.5,
                    currentTarget.getY(),
                    currentTarget.getZ() + 0.5,
                    npc.getNpcData().getBehavior().getMovementSpeed()
                );
            }
        }
    }

    @Override
    public void stop() {
        npc.getNavigation().stop();

        // Reset state (Index wird jetzt in canContinueToUse() inkrementiert)
        tickCounter = 0;
        wanderTickCounter = 0;
        currentTarget = null;
        wanderTarget = null;
        hasArrived = false;
    }

    /**
     * Setzt ein neues Wander-Ziel im konfigurierten Radius um den Patrouillenpunkt
     */
    private void setNewWanderTarget() {
        if (currentTarget == null) {
            return;
        }

        int radius = ModConfigHandler.COMMON.POLICE_PATROL_RADIUS.get();

        // Zufällige Position im Radius
        int offsetX = random.nextInt(radius * 2 + 1) - radius;
        int offsetZ = random.nextInt(radius * 2 + 1) - radius;

        wanderTarget = currentTarget.offset(offsetX, 0, offsetZ);

        // Gehe zum Wander-Ziel
        npc.getNavigation().moveTo(
            wanderTarget.getX() + 0.5,
            wanderTarget.getY(),
            wanderTarget.getZ() + 0.5,
            npc.getNpcData().getBehavior().getMovementSpeed() * 0.8 // Etwas langsamer beim Wandern
        );
    }
}
