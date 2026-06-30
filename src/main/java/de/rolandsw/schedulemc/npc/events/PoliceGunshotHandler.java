package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.core.EmotionState;
import de.rolandsw.schedulemc.npc.life.core.NPCLifeData;
import de.rolandsw.schedulemc.npc.life.witness.CrimeType;
import de.rolandsw.schedulemc.npc.life.witness.CrimeWitnessUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Schuss-Wahrnehmung der Polizei.
 *
 * <p>Jeder abgefeuerte Schuss meldet sich hier mit einer waffenabhängigen Hörweite.
 * Polizisten in dieser Hörweite reagieren über zwei klare Kanäle:
 * <ul>
 *   <li><b>Sehen</b> (Sichtlinie zum Schützen): Eine Schusswaffe vor den Augen der Polizei
 *       abzufeuern ist Gewalt → sofort Fahndungssterne (über {@link CrimeWitnessUtil#reportPoliceSighted}).</li>
 *   <li><b>Hören</b> (Schuss ohne Sichtlinie): Der Polizist läuft zum Schussort und durchsucht
 *       das Gebiet ({@link PoliceSearchBehavior#startSearchAt}) — ohne Sterne, bis er den
 *       Schützen tatsächlich sieht.</li>
 * </ul>
 */
public final class PoliceGunshotHandler {

    /** Mindestabstand (Ticks) zwischen zwei „Schuss in Sicht"-Fahndungen je Schütze. */
    private static final long SIGHTED_COOLDOWN_TICKS = 60L;

    /** Mindestabstand (Ticks) zwischen zwei NPC-Panik-Wellen je Schütze (Dauerfeuer-Drossel). */
    private static final long SCARE_COOLDOWN_TICKS = 20L;

    /** Schütze-UUID → letzter Tick, an dem ein „Schuss in Sicht" geahndet wurde. */
    private static final Map<UUID, Long> lastSightedTick = new ConcurrentHashMap<>();

    /** Schütze-UUID → letzter Tick, an dem umstehende NPCs aufgeschreckt wurden. */
    private static final Map<UUID, Long> lastScareTick = new ConcurrentHashMap<>();

    private PoliceGunshotHandler() {
    }

    /**
     * Wird pro abgefeuertem Schuss serverseitig aufgerufen.
     *
     * @param shooter       der Schütze
     * @param level         die ServerLevel
     * @param pos           Position des Schusses (Schützen-Position)
     * @param hearingRadius effektive Hörweite in Blöcken (Schalldämpfer verkürzt sie)
     */
    public static void onGunshotFired(ServerPlayer shooter, ServerLevel level, Vec3 pos, double hearingRadius) {
        if (hearingRadius <= 0) {
            return;
        }

        long now = level.getGameTime();
        BlockPos shotPos = BlockPos.containing(pos);

        // Zivilisten in Hörweite geraten in Panik (fliehen) — unabhängig davon, ob Polizei
        // in der Nähe ist. Die Panik lässt die Polizei über PolicePanicHandler den Bereich
        // untersuchen, sobald welche in Reichweite sind.
        scareNearbyNpcs(shooter, level, pos, hearingRadius, now);

        List<CustomNPCEntity> police = new ArrayList<>();
        PoliceAIHandler.getPoliceInRadius(pos, hearingRadius, police);
        if (police.isEmpty()) {
            return;
        }

        boolean seen = false;

        for (CustomNPCEntity npc : police) {
            if (npc.getPersistentData().getBoolean("IsKnockedOut")) {
                continue;
            }
            if (npc.hasLineOfSight(shooter)) {
                seen = true; // Sichtkontakt → Fahndung (unten, einmalig pro Cooldown)
            } else {
                // Schuss gehört, aber Schütze nicht gesehen → Schussfeld absuchen, keine Sterne.
                PoliceSearchBehavior.startSearchAt(npc, shotPos, shooter.getUUID(), now);
            }
        }

        if (seen) {
            Long last = lastSightedTick.get(shooter.getUUID());
            if (last == null || now - last >= SIGHTED_COOLDOWN_TICKS) {
                lastSightedTick.put(shooter.getUUID(), now);
                CrimeWitnessUtil.reportPoliceSighted(shooter, level, shotPos,
                        CrimeType.ILLEGAL_FIREARM, 1, CrimeType.ILLEGAL_FIREARM.getDisplayName());
            }
        }
    }

    /**
     * Lässt Zivilisten-NPCs in Hörweite eines Schusses in Panik geraten (fliehen).
     * Gedrosselt pro Schütze, damit Dauerfeuer keine teuren Entity-Scans pro Tick auslöst.
     */
    private static void scareNearbyNpcs(ServerPlayer shooter, ServerLevel level, Vec3 pos,
                                        double hearingRadius, long now) {
        if (!ModConfigHandler.COMMON.NPC_PANIC_ON_GUNSHOT.get()) {
            return;
        }
        Long last = lastScareTick.get(shooter.getUUID());
        if (last != null && now - last < SCARE_COOLDOWN_TICKS) {
            return;
        }
        lastScareTick.put(shooter.getUUID(), now);

        double radiusSq = hearingRadius * hearingRadius;
        AABB box = new AABB(
                pos.x - hearingRadius, pos.y - hearingRadius, pos.z - hearingRadius,
                pos.x + hearingRadius, pos.y + hearingRadius, pos.z + hearingRadius);
        List<CustomNPCEntity> npcs = level.getEntitiesOfClass(CustomNPCEntity.class, box,
                n -> n.getNpcType() != NPCType.POLICE && n.position().distanceToSqr(pos) <= radiusSq);

        for (CustomNPCEntity npc : npcs) {
            NPCLifeData lifeData = npc.getLifeData();
            if (lifeData == null) {
                continue;
            }
            // Starke Angst → wouldFlee() wird wahr → FleeAction übernimmt im Behavior-Tick.
            lifeData.getEmotions().trigger(EmotionState.FEARFUL, 70.0f);
            lifeData.getNeeds().modifySafety(-40);
        }
    }

    /** Aufräumen, wenn ein Spieler die Welt verlässt. */
    public static void cleanup(UUID shooterUUID) {
        if (shooterUUID != null) {
            lastSightedTick.remove(shooterUUID);
            lastScareTick.remove(shooterUUID);
        }
    }
}
