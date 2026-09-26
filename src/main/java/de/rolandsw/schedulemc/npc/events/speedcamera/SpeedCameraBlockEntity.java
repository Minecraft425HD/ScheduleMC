package de.rolandsw.schedulemc.npc.events.speedcamera;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.life.witness.CrimeType;
import de.rolandsw.schedulemc.npc.life.witness.WitnessManager;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Erfasst vorbeifahrende Fahrzeuge. Existiert nur, solange dieser Blitzer laut
 * {@link SpeedCameraManager}s 7-Tage-Rotation gerade der aktive Standort ist - der
 * Manager platziert/entfernt den Block selbst, es gibt also kein separates
 * Aktiv/Inaktiv-Flag mehr: die bloße Existenz des Blocks bedeutet aktiv. Die Erfassung
 * ist bewusst radiusbasiert und richtungsunabhängig - keine Fahrspur-/Blickrichtungs-
 * Logik, um kein ungetestetes Winkel-/Rotations-Risiko einzugehen (siehe Lehren aus dem
 * Roadblock-Feature).
 */
public class SpeedCameraBlockEntity extends BlockEntity {

    /** Radius in Blöcken, in dem Fahrzeuge erfasst werden */
    private static final double DETECTION_RADIUS = 6.0;

    public SpeedCameraBlockEntity(BlockPos pos, BlockState state) {
        super(SpeedCameraRegistry.SPEED_CAMERA_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * Prüft 1x/Sekunde auf zu schnell fahrende Fahrzeuge in Reichweite.
     */
    public void tick(ServerLevel level) {
        if (level.getGameTime() % 20 != 0) return;
        if (!ModConfigHandler.COMMON.POLICE_TRAFFIC_VIOLATIONS_ENABLED.get()) return;

        float speedLimit = ModConfigHandler.COMMON.POLICE_SPEED_LIMIT_DEFAULT.get().floatValue();
        AABB scanArea = new AABB(worldPosition).inflate(DETECTION_RADIUS);
        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, scanArea);

        for (ServerPlayer driver : nearbyPlayers) {
            if (!(driver.getVehicle() instanceof EntityGenericVehicle vehicle)) continue;
            if (Math.abs(vehicle.getSpeed()) <= speedLimit) continue;

            if (!SpeedCameraManager.getInstance().tryRecordViolation(driver.getUUID())) {
                continue; // Cooldown aktiv - dieser Spieler wurde schon kuerzlich geblitzt
            }

            long currentDay = level.getDayTime() / 24000;
            CrimeManager.addWantedLevel(driver.getUUID(), CrimeType.TRAFFIC_VIOLATION.getWantedStars(), currentDay,
                CrimeType.TRAFFIC_VIOLATION, worldPosition);

            driver.sendSystemMessage(Component.translatable("event.traffic.speed_camera"));

            WitnessManager witnessManager = WitnessManager.getManager(level);
            witnessManager.registerCrime(driver, CrimeType.TRAFFIC_VIOLATION, worldPosition, level, null);
        }
    }
}
