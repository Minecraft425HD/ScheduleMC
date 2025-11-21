package de.rolandsw.schedulemc.npc.goals;

import de.rolandsw.schedulemc.npc.data.ScheduleEntry;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Schedule-basiertes Movement Goal
 * NPCs folgen ihrem Zeitplan und bewegen sich zu den entsprechenden Locations
 */
public class ScheduleBasedMovementGoal extends Goal {

    private final CustomNPCEntity npc;
    private BlockPos targetPos;
    private ScheduleEntry currentEntry;
    private static final double ARRIVAL_THRESHOLD = 2.0D;
    private static final int RECALCULATE_INTERVAL = 100; // Alle 5 Sekunden neu berechnen
    private int tickCounter = 0;
    private int lastCheckTime = -1000; // Letzter Zeitpunkt der Schedule-Prüfung

    public ScheduleBasedMovementGoal(CustomNPCEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Nur wenn Movement aktiviert ist
        if (!npc.getNpcData().getBehavior().canMove()) {
            return false;
        }

        // Aktuellen Schedule-Eintrag abrufen
        Level level = npc.level();
        long dayTime = level.getDayTime() % 24000;
        ScheduleEntry entry = npc.getNpcData().getCurrentScheduleEntry(dayTime);

        if (entry == null) {
            return false;
        }

        // Ziel-Location für diesen Eintrag ermitteln
        BlockPos target = npc.getNpcData().getTargetLocationForEntry(entry);
        if (target == null) {
            return false;
        }

        // Nur wenn NPC nicht bereits am Ziel ist
        this.targetPos = target;
        this.currentEntry = entry;

        double distanceToTarget = npc.position().distanceTo(
            new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5)
        );

        return distanceToTarget > ARRIVAL_THRESHOLD;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPos == null) {
            return false;
        }

        // Prüfe ob sich der Schedule geändert hat (alle paar Ticks)
        Level level = npc.level();
        int currentTime = (int) (level.getDayTime() % 24000);

        if (Math.abs(currentTime - lastCheckTime) > 200) { // Alle 10 Sekunden prüfen
            lastCheckTime = currentTime;
            ScheduleEntry newEntry = npc.getNpcData().getCurrentScheduleEntry(level.getDayTime());

            // Wenn sich der Schedule-Eintrag geändert hat, Goal beenden
            if (newEntry != currentEntry) {
                return false;
            }
        }

        // Weitermachen solange NPC nicht angekommen ist
        double distanceToTarget = npc.position().distanceTo(
            new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5)
        );

        return distanceToTarget > ARRIVAL_THRESHOLD;
    }

    @Override
    public void tick() {
        if (targetPos == null) {
            return;
        }

        tickCounter++;

        // Alle 5 Sekunden Pfad neu berechnen
        if (tickCounter >= RECALCULATE_INTERVAL) {
            tickCounter = 0;
            navigateToTarget();
        }
    }

    @Override
    public void start() {
        if (targetPos != null) {
            lastCheckTime = (int) (npc.level().getDayTime() % 24000);
            navigateToTarget();
        }
    }

    @Override
    public void stop() {
        npc.getNavigation().stop();
        tickCounter = 0;
        targetPos = null;
        currentEntry = null;
    }

    private void navigateToTarget() {
        if (targetPos != null) {
            npc.getNavigation().moveTo(
                targetPos.getX() + 0.5,
                targetPos.getY(),
                targetPos.getZ() + 0.5,
                npc.getNpcData().getBehavior().getMovementSpeed()
            );
        }
    }
}
