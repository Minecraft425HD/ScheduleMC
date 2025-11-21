package de.rolandsw.schedulemc.npc.goals;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Goal: NPC geht nachts (13000-23000 Ticks) zu seinem Wohnort
 */
public class MoveToHomeGoal extends Goal {

    private final CustomNPCEntity npc;
    private BlockPos homePos;
    private static final double ARRIVAL_THRESHOLD = 2.0D;
    private static final int RECALCULATE_INTERVAL = 100; // Alle 5 Sekunden neu berechnen
    private int tickCounter = 0;

    public MoveToHomeGoal(CustomNPCEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Nur wenn Movement aktiviert ist
        if (!npc.getNpcData().getBehavior().canMove()) {
            return false;
        }

        // Nur wenn eine Home Location gesetzt ist
        BlockPos home = npc.getNpcData().getHomeLocation();
        if (home == null) {
            return false;
        }

        // Nur nachts (13000 - 23000 Ticks, ca. 19:00 - 05:00 Uhr)
        Level level = npc.level();
        long dayTime = level.getDayTime() % 24000;
        boolean isNight = dayTime >= 13000 && dayTime <= 23000;

        if (!isNight) {
            return false;
        }

        // Nur wenn NPC nicht bereits zu Hause ist
        this.homePos = home;
        double distanceToHome = npc.position().distanceTo(
            new Vec3(homePos.getX() + 0.5, homePos.getY(), homePos.getZ() + 0.5)
        );

        return distanceToHome > ARRIVAL_THRESHOLD;
    }

    @Override
    public boolean canContinueToUse() {
        // Weitermachen solange es Nacht ist und NPC nicht angekommen
        if (homePos == null) {
            return false;
        }

        Level level = npc.level();
        long dayTime = level.getDayTime() % 24000;
        boolean isNight = dayTime >= 13000 && dayTime <= 23000;

        if (!isNight) {
            return false;
        }

        double distanceToHome = npc.position().distanceTo(
            new Vec3(homePos.getX() + 0.5, homePos.getY(), homePos.getZ() + 0.5)
        );

        return distanceToHome > ARRIVAL_THRESHOLD;
    }

    @Override
    public void tick() {
        if (homePos == null) {
            return;
        }

        tickCounter++;

        // Alle 5 Sekunden Pfad neu berechnen
        if (tickCounter >= RECALCULATE_INTERVAL) {
            tickCounter = 0;
            npc.getNavigation().moveTo(
                homePos.getX() + 0.5,
                homePos.getY(),
                homePos.getZ() + 0.5,
                npc.getNpcData().getBehavior().getMovementSpeed()
            );
        }
    }

    @Override
    public void start() {
        if (homePos != null) {
            npc.getNavigation().moveTo(
                homePos.getX() + 0.5,
                homePos.getY(),
                homePos.getZ() + 0.5,
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
