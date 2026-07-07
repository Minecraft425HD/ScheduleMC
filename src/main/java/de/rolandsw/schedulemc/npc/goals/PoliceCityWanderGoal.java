package de.rolandsw.schedulemc.npc.goals;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.mapview.navigation.graph.RoadBlockDetector;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.pathfinding.NPCPathNavigation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Goal: Polizist OHNE gesetzte Patrouillenpunkte streift straßengebunden durch die Stadt,
 * statt nur an der Station zu stehen.
 *
 * - Anker = Polizeistation (falls gesetzt), sonst eine einmalig gemerkte Startposition.
 * - Streifziele werden zufällig im Radius um den Anker gewählt und müssen auf einem
 *   Straßenblock (RoadBlockDetector) liegen — oder begehbar (NPCPathNavigation) und
 *   höchstens POLICE_MAX_ROAD_DISTANCE von einer Straße entfernt.
 * - "Leine": Entfernt sich der Polizist weiter als der Max-Radius von jeder Straße,
 *   wird das nächste Ziel der nächstgelegene Straßenblock (zurück zur Straße).
 */
public class PoliceCityWanderGoal extends Goal {

    private final CustomNPCEntity npc;

    private static final int MAX_TARGET_ATTEMPTS = 16;
    /** Chance pro Tick, einen neuen Streifgang zu starten (~alle 2,5 s). */
    private static final double START_CHANCE = 0.02D;
    private static final double MIN_DIST = 6.0D;

    private static final String ANCHOR_X = "WanderAnchorX";
    private static final String ANCHOR_Y = "WanderAnchorY";
    private static final String ANCHOR_Z = "WanderAnchorZ";
    private static final String ANCHOR_SET = "WanderAnchorSet";

    private double targetX;
    private double targetY;
    private double targetZ;
    private boolean hasTarget = false;
    private int waitCounter = 0;
    /** PERFORMANCE: frühester Tick für den nächsten Straßen-Scan (kappt Scan-Bursts). */
    private long nextScanTick = 0;

    public PoliceCityWanderGoal(CustomNPCEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (npc.getNpcType() != NPCType.POLICE) return false;
        // Während einer laufenden Festnahme nicht wegstreifen.
        if (npc.getPersistentData().getLong("ArrestHoldUntil") >= npc.level().getGameTime()) return false;
        if (!ModConfigHandler.COMMON.POLICE_CITY_WANDER_ENABLED.get()) return false;
        if (!npc.getNpcData().getBehavior().canMove()) return false;
        if (npc.isDriving() || npc.getTarget() != null) return false;
        if (npc.getPersistentData().getBoolean("IsKnockedOut")) return false;
        // Nur ohne Patrouillenpunkte (sonst übernimmt PolicePatrolGoal)
        if (!npc.getNpcData().getPoliceData().getPatrolPoints().isEmpty()) return false;
        if (!npc.getNavigation().isDone()) return false;
        if (!(npc.level() instanceof ServerLevel level)) return false;
        if (npc.getRandom().nextDouble() >= START_CHANCE) return false;

        // PERFORMANCE: Der Straßen-Scan (Spiral-Block-Suche) ist teuer — max. 1 Versuch
        // alle 100 Ticks pro Polizist, damit die START_CHANCE nicht in Folge-Ticks erneut
        // den Scan auslöst (Streif-Frequenz bleibt praktisch unverändert).
        long now = level.getGameTime();
        if (now < nextScanTick) return false;
        nextScanTick = now + 100;

        BlockPos anchor = resolveAnchor();
        BlockPos target = chooseTarget(level, anchor);
        if (target == null) return false;

        this.targetX = target.getX() + 0.5;
        this.targetY = target.getY();
        this.targetZ = target.getZ() + 0.5;
        this.hasTarget = true;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!hasTarget) return false;
        if (npc.getTarget() != null) return false;
        if (npc.getPersistentData().getBoolean("IsKnockedOut")) return false;
        if (!npc.getNpcData().getPoliceData().getPatrolPoints().isEmpty()) return false;
        int waitTicks = ModConfigHandler.COMMON.POLICE_CITY_WANDER_WAIT_SECONDS.get() * 20;
        return !npc.getNavigation().isDone() || waitCounter < waitTicks;
    }

    @Override
    public void start() {
        waitCounter = 0;
        npc.getNavigation().moveTo(targetX, targetY, targetZ,
            npc.getNpcData().getBehavior().getMovementSpeed());
    }

    @Override
    public void tick() {
        if (npc.getNavigation().isDone()) {
            waitCounter++;
        }
    }

    @Override
    public void stop() {
        npc.getNavigation().stop();
        hasTarget = false;
        waitCounter = 0;
    }

    /** Anker = Polizeistation, sonst einmalig gemerkte Startposition (reload-fest in PersistentData). */
    private BlockPos resolveAnchor() {
        BlockPos station = npc.getNpcData().getPoliceData().getPoliceStation();
        if (station != null) return station;

        CompoundTag pd = npc.getPersistentData();
        if (!pd.getBoolean(ANCHOR_SET)) {
            BlockPos p = npc.blockPosition();
            pd.putInt(ANCHOR_X, p.getX());
            pd.putInt(ANCHOR_Y, p.getY());
            pd.putInt(ANCHOR_Z, p.getZ());
            pd.putBoolean(ANCHOR_SET, true);
        }
        return new BlockPos(pd.getInt(ANCHOR_X), pd.getInt(ANCHOR_Y), pd.getInt(ANCHOR_Z));
    }

    /** Wählt ein straßengebundenes Streifziel; gibt {@code null} zurück, wenn keine Straße erreichbar ist. */
    @Nullable
    private BlockPos chooseTarget(ServerLevel level, BlockPos anchor) {
        int maxRoad = ModConfigHandler.COMMON.POLICE_MAX_ROAD_DISTANCE.get();

        // Leine: zu weit von jeder Straße? -> zurück zur nächsten Straße
        BlockPos nearestToNpc = findNearestRoadWithin(level, npc.blockPosition(), maxRoad);
        if (nearestToNpc == null) {
            return findNearestRoadWithin(level, anchor, maxRoad);
        }

        int radius = ModConfigHandler.COMMON.POLICE_CITY_WANDER_RADIUS.get();
        for (int i = 0; i < MAX_TARGET_ATTEMPTS; i++) {
            double angle = npc.getRandom().nextDouble() * Math.PI * 2.0;
            double dist = MIN_DIST + npc.getRandom().nextDouble() * (radius - MIN_DIST);
            int x = anchor.getX() + (int) Math.round(Math.cos(angle) * dist);
            int z = anchor.getZ() + (int) Math.round(Math.sin(angle) * dist);
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos top = new BlockPos(x, y - 1, z);
            BlockState st = level.getBlockState(top);
            if (RoadBlockDetector.isRoadBlock(st)) {
                return top;
            }
            if (NPCPathNavigation.isBlockWalkable(st)
                    && findNearestRoadWithin(level, top, maxRoad) != null) {
                return top;
            }
        }
        // Fallback: nächste Straße ansteuern statt stehen zu bleiben
        return nearestToNpc;
    }

    /**
     * Sucht den nächstgelegenen Straßenblock in einer XZ-Spirale (nur Ringrand) um origin,
     * bis maxRadius. Y kommt aus der Heightmap; Frühabbruch beim ersten Treffer.
     *
     * @return Straßenblock-Position (mit Heightmap-Y) oder {@code null}, wenn keine in Reichweite
     */
    @Nullable
    private static BlockPos findNearestRoadWithin(ServerLevel level, BlockPos origin, int maxRadius) {
        for (int r = 0; r <= maxRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue; // nur Ringrand
                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                    BlockPos pos = new BlockPos(x, y - 1, z);
                    if (RoadBlockDetector.isRoadBlock(level.getBlockState(pos))) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }
}
