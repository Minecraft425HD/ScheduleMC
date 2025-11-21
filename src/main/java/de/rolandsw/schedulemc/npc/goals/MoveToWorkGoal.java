package de.rolandsw.schedulemc.npc.goals;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Goal: NPC geht tagsüber (0-13000 Ticks) zu seiner Arbeitsstätte
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

        // Nur tagsüber (0 - 13000 Ticks, ca. 06:00 - 19:00 Uhr)
        Level level = npc.level();
        long dayTime = level.getDayTime() % 24000;
        boolean isDay = dayTime >= 0 && dayTime < 13000;

        if (!isDay) {
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
        // Weitermachen solange es Tag ist und NPC nicht angekommen
        if (workPos == null) {
            return false;
        }

        Level level = npc.level();
        long dayTime = level.getDayTime() % 24000;
        boolean isDay = dayTime >= 0 && dayTime < 13000;

        if (!isDay) {
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
}
