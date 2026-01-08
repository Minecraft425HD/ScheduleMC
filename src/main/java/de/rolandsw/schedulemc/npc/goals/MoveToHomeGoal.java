package de.rolandsw.schedulemc.npc.goals;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Goal: NPC geht zu seinem Wohnort (einstellbare Zeiten)
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
        // NICHT für Polizei-NPCs (die haben eigene Goals)
        if (npc.getNpcData().getNpcType() == de.rolandsw.schedulemc.npc.data.NPCType.POLIZEI) {
            return false;
        }

        // Nur wenn Movement aktiviert ist
        if (!npc.getNpcData().getBehavior().canMove()) {
            return false;
        }

        // Nur wenn eine Home Location gesetzt ist
        BlockPos home = npc.getNpcData().getHomeLocation();
        if (home == null) {
            return false;
        }

        // Prüfe ob Heimzeit ist (einstellbare Zeiten)
        Level level = npc.level();
        long dayTime = level.getDayTime() % 24000;
        long homeTime = npc.getNpcData().getHomeTime();
        long workStart = npc.getNpcData().getWorkStartTime();

        // Heimzeit = ab homeTime bis workStart (kann über Mitternacht gehen)
        boolean isHomeTime = isTimeBetween(dayTime, homeTime, workStart);

        if (!isHomeTime) {
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
        // Weitermachen solange es Heimzeit ist und NPC nicht angekommen
        if (homePos == null) {
            return false;
        }

        Level level = npc.level();
        long dayTime = level.getDayTime() % 24000;
        long homeTime = npc.getNpcData().getHomeTime();
        long workStart = npc.getNpcData().getWorkStartTime();

        boolean isHomeTime = isTimeBetween(dayTime, homeTime, workStart);

        if (!isHomeTime) {
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
