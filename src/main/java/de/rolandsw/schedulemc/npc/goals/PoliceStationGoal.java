package de.rolandsw.schedulemc.npc.goals;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.util.SecureRandomUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Goal: Polizist bleibt an der Polizeistation (wenn KEINE Patrouillenpunkte gesetzt sind)
 * - Geht zur Polizeistation
 * - Bleibt dort für konfigurierte Zeit (Standard 5 Minuten)
 * - Bewegt sich asynchron im konfigurierbaren Radius (Standard 10 Blöcke)
 * - Nach Wartezeit: Erneuert die Wartezeit und bleibt weiter an der Station
 */
public class PoliceStationGoal extends Goal {

    private final CustomNPCEntity npc;
    private BlockPos stationPos;
    private BlockPos wanderTarget;
    private static final double ARRIVAL_THRESHOLD = 2.0D;
    private static final int RECALCULATE_INTERVAL = 100; // Alle 5 Sekunden neu berechnen
    private int tickCounter = 0;
    private int wanderTickCounter = 0;
    private static final int WANDER_INTERVAL = 200; // Alle 10 Sekunden neues Wander-Ziel
    private boolean hasArrived = false;

    public PoliceStationGoal(CustomNPCEntity npc) {
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

        // Nur wenn eine Polizeistation gesetzt ist
        BlockPos station = npc.getNpcData().getPoliceStation();
        if (station == null) {
            return false;
        }

        // NUR aktiv wenn KEINE Patrouillenpunkte gesetzt sind
        // (Wenn Patrouillenpunkte existieren, übernimmt PolicePatrolGoal)
        if (!npc.getNpcData().getPatrolPoints().isEmpty()) {
            return false;
        }

        this.stationPos = station;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (stationPos == null) {
            return false;
        }

        // Prüfe ob Patrouillenpunkte hinzugefügt wurden (dann stoppen)
        if (!npc.getNpcData().getPatrolPoints().isEmpty()) {
            return false;
        }

        // Wenn angekommen, prüfe ob Wartezeit abgelaufen
        if (hasArrived) {
            long currentTime = npc.level().getGameTime();
            long arrivalTime = npc.getNpcData().getStationArrivalTime();
            long waitTimeTicks = ModConfigHandler.COMMON.POLICE_STATION_WAIT_MINUTES.get() * 60 * 20; // Minuten → Ticks

            if (currentTime - arrivalTime >= waitTimeTicks) {
                // Wartezeit vorbei - erneuere Wartezeit und bleibe an Station
                npc.getNpcData().setStationArrivalTime(currentTime);
            }
        }

        return true;
    }

    @Override
    public void start() {
        if (stationPos != null) {
            hasArrived = false;
            tickCounter = 0;
            wanderTickCounter = 0;
            wanderTarget = null;

            // Gehe zur Polizeistation
            npc.getNavigation().moveTo(
                stationPos.getX() + 0.5,
                stationPos.getY(),
                stationPos.getZ() + 0.5,
                npc.getNpcData().getBehavior().getMovementSpeed()
            );
        }
    }

    @Override
    public void tick() {
        if (stationPos == null) {
            return;
        }

        tickCounter++;
        wanderTickCounter++;

        // Prüfe ob am Ziel angekommen
        double distanceToStation = npc.position().distanceTo(
            new Vec3(stationPos.getX() + 0.5, stationPos.getY(), stationPos.getZ() + 0.5)
        );

        if (!hasArrived && distanceToStation <= ARRIVAL_THRESHOLD) {
            // Gerade angekommen - speichere Ankunftszeit
            hasArrived = true;
            npc.getNpcData().setStationArrivalTime(npc.level().getGameTime());
            npc.getNavigation().stop();
        }

        if (hasArrived) {
            // An der Station - wandere im Radius herum
            if (wanderTickCounter >= WANDER_INTERVAL) {
                wanderTickCounter = 0;
                setNewWanderTarget();
            }
        } else {
            // Noch unterwegs zur Station - Pfad regelmäßig neu berechnen
            if (tickCounter >= RECALCULATE_INTERVAL) {
                tickCounter = 0;
                npc.getNavigation().moveTo(
                    stationPos.getX() + 0.5,
                    stationPos.getY(),
                    stationPos.getZ() + 0.5,
                    npc.getNpcData().getBehavior().getMovementSpeed()
                );
            }
        }
    }

    @Override
    public void stop() {
        npc.getNavigation().stop();
        tickCounter = 0;
        wanderTickCounter = 0;
        stationPos = null;
        wanderTarget = null;
        hasArrived = false;
    }

    /**
     * Setzt ein neues Wander-Ziel im konfigurierten Radius um die Polizeistation
     */
    private void setNewWanderTarget() {
        if (stationPos == null) {
            return;
        }

        int radius = ModConfigHandler.COMMON.POLICE_STATION_RADIUS.get();

        // Zufällige Position im Radius (SICHERHEIT: SecureRandom)
        int offsetX = SecureRandomUtil.nextInt(radius * 2 + 1) - radius;
        int offsetZ = SecureRandomUtil.nextInt(radius * 2 + 1) - radius;

        wanderTarget = stationPos.offset(offsetX, 0, offsetZ);

        // Gehe zum Wander-Ziel
        npc.getNavigation().moveTo(
            wanderTarget.getX() + 0.5,
            wanderTarget.getY(),
            wanderTarget.getZ() + 0.5,
            npc.getNpcData().getBehavior().getMovementSpeed() * 0.8 // Etwas langsamer beim Wandern
        );
    }
}
