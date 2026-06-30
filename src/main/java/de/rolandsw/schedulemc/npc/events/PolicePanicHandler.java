package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Panik-Wahrnehmung der Polizei.
 *
 * <p>Wenn ein NPC anfängt, panisch zu fliehen, ist das für umstehende Polizisten ein
 * sichtbares Alarmsignal: Sie laufen zum Ort der Panik und durchsuchen das Gebiet
 * (dasselbe Such-System wie bei gehörten Schüssen). Es werden keine Fahndungssterne
 * vergeben — die Polizei untersucht lediglich, was die Leute aufgescheucht hat.
 */
public final class PolicePanicHandler {

    /** Mindestabstand (Ticks) zwischen zwei Panik-Alarmen desselben NPCs. */
    private static final long PANIC_COOLDOWN_TICKS = 100L;

    /** NPC-UUID → letzter Tick, an dem dessen Panik die Polizei alarmiert hat. */
    private static final Map<UUID, Long> lastPanicTick = new ConcurrentHashMap<>();

    private PolicePanicHandler() {
    }

    /**
     * Meldet, dass ein NPC in Panik geraten ist. Alarmiert umstehende Polizisten,
     * den Bereich zu untersuchen.
     *
     * @param npc   der panische NPC
     * @param level die ServerLevel
     */
    public static void onNpcPanic(CustomNPCEntity npc, ServerLevel level) {
        if (!ModConfigHandler.COMMON.POLICE_INVESTIGATE_PANIC.get()) {
            return;
        }
        // Ein panischer Polizist alarmiert nicht sich selbst/Kollegen.
        if (npc.getNpcType() == NPCType.POLICE) {
            return;
        }

        long now = level.getGameTime();
        UUID npcId = npc.getUUID();
        Long last = lastPanicTick.get(npcId);
        if (last != null && now - last < PANIC_COOLDOWN_TICKS) {
            return;
        }

        double radius = ModConfigHandler.COMMON.POLICE_PANIC_RADIUS.get();
        List<CustomNPCEntity> police = new ArrayList<>();
        PoliceAIHandler.getPoliceInRadius(npc.position(), radius, police);
        if (police.isEmpty()) {
            return;
        }

        lastPanicTick.put(npcId, now);
        BlockPos pos = npc.blockPosition();
        for (CustomNPCEntity officer : police) {
            if (officer.getPersistentData().getBoolean("IsKnockedOut")) {
                continue;
            }
            // Polizisten mit aktiver Verfolgung nicht von ihrem Ziel abziehen.
            if (PoliceBackupSystem.getAssignedTarget(officer.getUUID()) != null) {
                continue;
            }
            PoliceSearchBehavior.startSearchAt(officer, pos, npcId, now);
        }
    }

    /** Aufräumen, wenn ein NPC entfernt wird. */
    public static void cleanup(UUID npcUUID) {
        if (npcUUID != null) {
            lastPanicTick.remove(npcUUID);
        }
    }
}
