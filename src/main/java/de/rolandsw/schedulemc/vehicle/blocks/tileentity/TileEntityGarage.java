package de.rolandsw.schedulemc.vehicle.blocks.tileentity;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.blocks.ModBlocks;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.gui.ContainerGarage;
import de.rolandsw.schedulemc.vehicle.gui.TileEntityContainerProvider;
import de.maxhenkel.corelib.blockentity.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class TileEntityGarage extends TileEntityBase implements ITickableBlockEntity {

    private static final double DETECTION_RADIUS = 1.5D;
    private static final double PRE_SCAN_RADIUS = 3.0D;
    private static final int SCAN_INTERVAL = 5; // Scan every 5 ticks

    @Nullable
    private UUID trackedVehicleUUID;
    private boolean isActive;
    private int tickCounter;
    private boolean wasVehicleNearby;

    private final ContainerData fields = new ContainerData() {
        @Override
        public int get(int index) {
            return isActive ? 1 : 0;
        }

        @Override
        public void set(int index, int value) {
            isActive = value == 1;
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public TileEntityGarage(BlockPos pos, BlockState state) {
        super(ModBlocks.GARAGE_TILE_ENTITY_TYPE.get(), pos, state);
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        tickCounter++;

        // Scan for vehicles every SCAN_INTERVAL ticks
        if (tickCounter % SCAN_INTERVAL == 0) {
            scanForVehicles();
        }

        // Synchronize to client every 20 ticks (1 second)
        synchronize(20);
    }

    private void scanForVehicles() {
        // Get the position in front of the garage (based on facing direction)
        BlockPos scanPos = getBlockPos();
        Vec3 scanCenter = new Vec3(scanPos.getX() + 0.5, scanPos.getY() + 0.5, scanPos.getZ() + 0.5);

        // Create AABB for vehicle detection
        AABB detectionBox = new AABB(scanCenter.subtract(DETECTION_RADIUS, DETECTION_RADIUS, DETECTION_RADIUS),
                                     scanCenter.add(DETECTION_RADIUS, DETECTION_RADIUS, DETECTION_RADIUS));

        // Find vehicles in detection range
        List<EntityGenericVehicle> vehicles = level.getEntitiesOfClass(
            EntityGenericVehicle.class,
            detectionBox,
            vehicle -> !vehicle.isRemoved()
        );

        boolean vehicleNearby = !vehicles.isEmpty();

        if (vehicleNearby) {
            // Get the nearest vehicle
            EntityGenericVehicle nearestVehicle = vehicles.stream()
                .min((v1, v2) -> Double.compare(
                    v1.distanceToSqr(scanCenter),
                    v2.distanceToSqr(scanCenter)
                ))
                .orElse(null);

            if (nearestVehicle != null) {
                // Lock the vehicle if it has a driver and isn't already locked
                if (nearestVehicle.getControllingPassenger() instanceof Player driver) {
                    if (!nearestVehicle.isLockedInGarage()) {
                        lockVehicle(nearestVehicle, driver);
                    }
                }

                // Track this vehicle
                trackedVehicleUUID = nearestVehicle.getUUID();
                isActive = true;

                // Visual feedback - green particles when vehicle detected
                if (!wasVehicleNearby && level instanceof ServerLevel serverLevel) {
                    spawnDetectionParticles(serverLevel, nearestVehicle.position(), true);
                    level.playSound(null, getBlockPos(), SoundEvents.NOTE_BLOCK_PLING.value(),
                                  SoundSource.BLOCKS, 0.5F, 1.2F);
                }
            }
        } else {
            // No vehicle nearby
            if (wasVehicleNearby) {
                // Vehicle left the area
                unlockTrackedVehicle();
                isActive = false;
            }
            trackedVehicleUUID = null;
        }

        // Show pre-scan particles (yellow) when no vehicle is locked but in range
        if (level instanceof ServerLevel serverLevel && tickCounter % 20 == 0) {
            showPreScanParticles(serverLevel);
        }

        wasVehicleNearby = vehicleNearby;
    }

    private void lockVehicle(EntityGenericVehicle vehicle, Player driver) {
        vehicle.lockInGarage(getBlockPos());

        // Play lock sound
        level.playSound(null, getBlockPos(), SoundEvents.IRON_DOOR_CLOSE,
                       SoundSource.BLOCKS, 0.8F, 1.0F);

        // Notify player
        driver.displayClientMessage(
            Component.translatable("message.schedulemc.garage.vehicle_locked"),
            true
        );

        // Open GUI automatically
        if (driver instanceof ServerPlayer serverPlayer) {
            openGarageGUI(serverPlayer, vehicle);
        }
    }

    private void unlockTrackedVehicle() {
        EntityGenericVehicle vehicle = getTrackedVehicle();
        if (vehicle != null) {
            vehicle.unlockFromGarage();

            // Play unlock sound
            level.playSound(null, getBlockPos(), SoundEvents.IRON_DOOR_OPEN,
                           SoundSource.BLOCKS, 0.8F, 1.0F);
        }
    }

    private void showPreScanParticles(ServerLevel level) {
        // Show yellow particles in a circle around optimal parking position
        Vec3 center = Vec3.atCenterOf(getBlockPos());

        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2;
            double x = center.x + Math.cos(angle) * PRE_SCAN_RADIUS;
            double z = center.z + Math.sin(angle) * PRE_SCAN_RADIUS;
            double y = center.y;

            level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0.1, 0, 0.02);
        }
    }

    private void spawnDetectionParticles(ServerLevel level, Vec3 pos, boolean success) {
        // Spawn particles around vehicle when detected
        for (int i = 0; i < 10; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2;
            double offsetY = level.random.nextDouble() * 2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2;

            level.sendParticles(
                success ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.SMOKE,
                pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                1, 0, 0, 0, 0
            );
        }
    }

    @Nullable
    public EntityGenericVehicle getTrackedVehicle() {
        if (level == null || trackedVehicleUUID == null) {
            return null;
        }

        // Find vehicle by UUID in a larger radius (vehicle might have moved slightly)
        Vec3 scanCenter = Vec3.atCenterOf(getBlockPos());
        AABB searchBox = new AABB(scanCenter.subtract(10, 10, 10), scanCenter.add(10, 10, 10));

        return level.getEntitiesOfClass(EntityGenericVehicle.class, searchBox)
            .stream()
            .filter(v -> v.getUUID().equals(trackedVehicleUUID))
            .findFirst()
            .orElse(null);
    }

    public void openGarageGUI(ServerPlayer player, EntityGenericVehicle vehicle) {
        TileEntityContainerProvider.openGui(player, this, packetBuffer -> {
            // Send vehicle UUID to client
            packetBuffer.writeUUID(vehicle.getUUID());
        }, (i, playerInventory, playerEntity) ->
            new ContainerGarage(i, vehicle, this, playerInventory)
        );
    }

    @Override
    public Component getTranslatedName() {
        return Component.translatable("container.schedulemc.garage");
    }

    @Override
    public ContainerData getFields() {
        return fields;
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        if (trackedVehicleUUID != null) {
            compound.putUUID("TrackedVehicle", trackedVehicleUUID);
        }
        compound.putBoolean("Active", isActive);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        if (compound.hasUUID("TrackedVehicle")) {
            trackedVehicleUUID = compound.getUUID("TrackedVehicle");
        }
        isActive = compound.getBoolean("Active");
    }
}
