package de.rolandsw.schedulemc.npc.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Custom Door Goal - NPCs können Türen schließen, aber NICHT öffnen
 * NPCs müssen darauf warten, dass Spieler Türen für sie öffnen
 */
public class NPCCloseDoorGoal extends Goal {

    private final Mob mob;
    private BlockPos doorPos = null;
    private int closeDoorDelay = 0;
    private static final int CLOSE_DOOR_DELAY = 20; // 1 Sekunde Verzögerung bevor die Tür geschlossen wird

    public NPCCloseDoorGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Prüfe ob der NPC gerade durch eine Tür gegangen ist
        Path path = this.mob.getNavigation().getPath();
        if (path != null && !path.isDone()) {
            // Prüfe die nächsten Nodes im Pfad
            for (int i = 0; i < Math.min(path.getNextNodeIndex() + 2, path.getNodeCount()); i++) {
                Node node = path.getNode(i);
                BlockPos pos = new BlockPos(node.x, node.y, node.z);

                if (this.isDoorOpen(pos)) {
                    this.doorPos = pos;
                    return true;
                }
            }
        }

        // Prüfe auch die unmittelbare Umgebung auf offene Türen
        BlockPos mobPos = this.mob.blockPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos checkPos = mobPos.offset(dx, 0, dz);
                if (this.isDoorOpen(checkPos)) {
                    // NPC ist gerade an dieser Tür vorbei gegangen
                    double distance = this.mob.position().distanceToSqr(
                        checkPos.getX() + 0.5, checkPos.getY(), checkPos.getZ() + 0.5
                    );
                    // Nur schließen wenn NPC etwas Abstand hat (ist durch die Tür gegangen)
                    if (distance > 1.5 && distance < 4.0) {
                        this.doorPos = checkPos;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.closeDoorDelay > 0 && this.doorPos != null && this.isDoorOpen(this.doorPos);
    }

    @Override
    public void start() {
        this.closeDoorDelay = CLOSE_DOOR_DELAY;
    }

    @Override
    public void stop() {
        this.doorPos = null;
        this.closeDoorDelay = 0;
    }

    @Override
    public void tick() {
        this.closeDoorDelay--;

        if (this.closeDoorDelay == 0 && this.doorPos != null) {
            this.closeDoor(this.doorPos);
        }
    }

    /**
     * Prüft ob an der Position eine offene Tür ist
     */
    private boolean isDoorOpen(BlockPos pos) {
        Level level = this.mob.level();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof DoorBlock) {
            return state.getValue(DoorBlock.OPEN);
        }
        return false;
    }

    /**
     * Schließt die Tür an der gegebenen Position
     */
    private void closeDoor(BlockPos pos) {
        Level level = this.mob.level();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof DoorBlock doorBlock) {
            doorBlock.setOpen(this.mob, level, state, pos, false);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
