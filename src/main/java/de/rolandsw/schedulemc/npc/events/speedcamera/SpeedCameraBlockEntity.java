package de.rolandsw.schedulemc.npc.events.speedcamera;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import de.rolandsw.schedulemc.npc.life.witness.CrimeType;
import de.rolandsw.schedulemc.npc.life.witness.WitnessManager;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Erfasst vorbeifahrende Fahrzeuge, wenn dieser Blitzer gerade {@link #active} ist
 * (siehe {@link SpeedCameraManager} für die Aktiv/Inaktiv-Rotation). Die Erfassung ist
 * bewusst radiusbasiert und richtungsunabhängig - keine Fahrspur-/Blickrichtungs-Logik,
 * um kein ungetestetes Winkel-/Rotations-Risiko einzugehen (siehe Lehren aus dem
 * Roadblock-Feature).
 */
public class SpeedCameraBlockEntity extends BlockEntity {

    /** Radius in Blöcken, in dem Fahrzeuge erfasst werden */
    private static final double DETECTION_RADIUS = 6.0;

    private boolean active = true;

    public SpeedCameraBlockEntity(BlockPos pos, BlockState state) {
        super(SpeedCameraRegistry.SPEED_CAMERA_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Wird ausschließlich von {@link SpeedCameraManager} beim periodischen Rotieren
     * der aktiven Blitzer-Teilmenge aufgerufen.
     */
    void setActive(boolean active) {
        if (this.active == active) return;
        this.active = active;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Prüft 1x/Sekunde auf zu schnell fahrende Fahrzeuge in Reichweite, sofern aktiv.
     */
    public void tick(ServerLevel level) {
        if (!active) return;
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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Active", active);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        active = tag.getBoolean("Active");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
