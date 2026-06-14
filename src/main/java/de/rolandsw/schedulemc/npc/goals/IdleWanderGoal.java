package de.rolandsw.schedulemc.npc.goals;

import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Goal: Bürger/Händler schlendern leicht um ihren aktuellen Standort,
 * statt regungslos zu stehen, wenn gerade kein Schedule-Goal aktiv ist.
 *
 * Der Anker wird beim Start auf die aktuelle Position gesetzt (der "Posten");
 * der Leash-Radius (2.5) liegt UNTER der Ankunftsschwelle der Schedule-Goals
 * (3.0), damit MoveToWork/Home/Leisure nicht reaktivieren und der NPC seinen
 * Bereich nie verlässt (kein Hin-und-Her-Laufen).
 */
public class IdleWanderGoal extends Goal {

    private final CustomNPCEntity npc;
    private static final double LEASH_RADIUS = 2.5D;
    private static final double WANDER_SPEED = 0.4D;
    /** Chance pro Tick, ein Schlendern zu starten (~alle 5 s). */
    private static final double START_CHANCE = 0.01D;

    private BlockPos anchor;
    private double targetX;
    private double targetY;
    private double targetZ;

    public IdleWanderGoal(CustomNPCEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        NPCType type = npc.getNpcType();
        if (type != NPCType.CITIZEN && type != NPCType.MERCHANT) return false;
        if (npc.isDriving() || npc.getTarget() != null) return false;
        if (npc.getPersistentData().getBoolean("IsKnockedOut")) return false;
        if (!npc.getNavigation().isDone()) return false;
        if (npc.getRandom().nextDouble() >= START_CHANCE) return false;

        this.anchor = npc.blockPosition();
        // Zufallspunkt innerhalb des Leash-Radius um den Posten
        double angle = npc.getRandom().nextDouble() * Math.PI * 2.0;
        double dist = npc.getRandom().nextDouble() * LEASH_RADIUS;
        this.targetX = anchor.getX() + 0.5 + Math.cos(angle) * dist;
        this.targetZ = anchor.getZ() + 0.5 + Math.sin(angle) * dist;
        this.targetY = anchor.getY();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !npc.getNavigation().isDone()
            && npc.getTarget() == null
            && !npc.getPersistentData().getBoolean("IsKnockedOut");
    }

    @Override
    public void start() {
        npc.getNavigation().moveTo(targetX, targetY, targetZ, WANDER_SPEED);
    }

    @Override
    public void stop() {
        npc.getNavigation().stop();
    }

    /** Schätzdistanz zum Anker (für Tests/Debug). */
    public double distanceFromAnchor() {
        if (anchor == null) return 0.0;
        return Math.sqrt(npc.distanceToSqr(Vec3.atCenterOf(anchor)));
    }
}
