package de.rolandsw.schedulemc.npc.entity.component;

import de.rolandsw.schedulemc.mapview.npc.NPCActivityStatus;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

/**
 * Aktivitaets-Tracking-Komponente.
 *
 * Berechnet und verwaltet den aktuellen Aktivitaetsstatus eines NPCs
 * (AT_WORK, AT_HOME, ROAMING, ON_PATROL, AT_STATION).
 *
 * Wird fuer die Karten-Anzeige verwendet, um NPCs nach Status zu filtern.
 * Update-Intervall: alle 100 Ticks (5 Sekunden).
 */
public class ActivityTrackingComponent implements NPCComponent {

    private NPCActivityStatus currentStatus = NPCActivityStatus.ROAMING;
    private static final double LOCATION_RADIUS_SQ = 25.0; // 5 Bloecke Radius (squared)

    @Override
    public String getComponentId() {
        return "activity_tracking";
    }

    @Override
    public int getUpdateInterval() {
        return 100; // Alle 5 Sekunden
    }

    @Override
    public void tick(CustomNPCEntity entity) {
        NPCActivityStatus newStatus = calculateStatus(entity);
        if (newStatus != currentStatus) {
            currentStatus = newStatus;
        }
    }

    /**
     * Berechnet den aktuellen Status basierend auf NPC-Typ, Zeit und Position.
     */
    private NPCActivityStatus calculateStatus(CustomNPCEntity entity) {
        NPCType type = entity.getNpcType();

        // Polizei-NPCs
        if (type == NPCType.POLIZEI) {
            if (entity.getNpcData().getPatrolPoints() != null &&
                !entity.getNpcData().getPatrolPoints().isEmpty()) {
                return NPCActivityStatus.ON_PATROL;
            }
            return NPCActivityStatus.AT_STATION;
        }

        // Zeitplan pruefen
        if (entity.level() instanceof ServerLevel serverLevel) {
            long dayTime = serverLevel.getDayTime() % 24000;
            long workStart = entity.getNpcData().getWorkStartTime();
            long workEnd = entity.getNpcData().getWorkEndTime();
            long homeTime = entity.getNpcData().getHomeTime();

            boolean isWorkTime = isInTimeRange(dayTime, workStart, workEnd);
            boolean isHomeTime = isInTimeRange(dayTime, homeTime, workStart);

            if (isWorkTime && isNearLocation(entity, entity.getNpcData().getWorkLocation())) {
                return NPCActivityStatus.AT_WORK;
            }

            if (isHomeTime && isNearLocation(entity, entity.getNpcData().getHomeLocation())) {
                return NPCActivityStatus.AT_HOME;
            }
        }

        return NPCActivityStatus.ROAMING;
    }

    private boolean isInTimeRange(long current, long start, long end) {
        if (start < end) {
            return current >= start && current < end;
        }
        // Nachtschicht
        return current >= start || current < end;
    }

    private boolean isNearLocation(CustomNPCEntity entity, BlockPos location) {
        if (location == null) return false;
        return entity.blockPosition().distSqr(location) < LOCATION_RADIUS_SQ;
    }

    // ═══════════════════════════════════════════════════════════
    // API
    // ═══════════════════════════════════════════════════════════

    public NPCActivityStatus getCurrentStatus() {
        return currentStatus;
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("status", currentStatus.ordinal());
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("status")) {
            int ordinal = tag.getInt("status");
            NPCActivityStatus[] values = NPCActivityStatus.values();
            if (ordinal >= 0 && ordinal < values.length) {
                currentStatus = values[ordinal];
            }
        }
    }
}
