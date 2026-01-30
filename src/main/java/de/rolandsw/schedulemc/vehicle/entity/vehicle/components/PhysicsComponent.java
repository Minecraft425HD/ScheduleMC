package de.rolandsw.schedulemc.vehicle.entity.vehicle.components;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.DamageSourceVehicle;
import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.VehicleConstants;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.net.MessageVehicleHorn;
import de.rolandsw.schedulemc.vehicle.net.MessageCrash;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import de.rolandsw.schedulemc.vehicle.sounds.SoundLoopHigh;
import de.rolandsw.schedulemc.vehicle.sounds.SoundLoopIdle;
import de.rolandsw.schedulemc.vehicle.sounds.SoundLoopStart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

/**
 * Manages physics, movement, controls, and sounds for the vehicle
 */
public class PhysicsComponent extends VehicleComponent {

    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> STARTED = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FORWARD = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BACKWARD = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LEFT = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RIGHT = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ODOMETER_SYNCED = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.INT);

    private float wheelRotation;

    // Odometer: Gesamte gefahrene Distanz in Blöcken (1 Block ≈ 1 Meter)
    private long odometer;
    private double lastPosX;
    private double lastPosZ;
    private double odometerAccumulator;
    private boolean odometerInitialized;

    @OnlyIn(Dist.CLIENT)
    private boolean collidedLastTick;

    @OnlyIn(Dist.CLIENT)
    private SoundLoopStart startLoop;
    @OnlyIn(Dist.CLIENT)
    private SoundLoopIdle idleLoop;
    @OnlyIn(Dist.CLIENT)
    private SoundLoopHigh highLoop;

    @OnlyIn(Dist.CLIENT)
    private boolean startedLast;

    // Optimierung: ArrayDeque statt LinkedBlockingQueue (single-threaded, kein Sync-Overhead)
    private final Deque<Runnable> tasks = new ArrayDeque<>(4);

    public PhysicsComponent(EntityGenericVehicle vehicle) {
        super(vehicle);
    }

    public static void defineData(SynchedEntityData entityData) {
        entityData.define(STARTED, false);
        entityData.define(SPEED, 0F);
        entityData.define(FORWARD, false);
        entityData.define(BACKWARD, false);
        entityData.define(LEFT, false);
        entityData.define(RIGHT, false);
        entityData.define(ODOMETER_SYNCED, 0);
    }

    @Override
    public void defineSynchedData() {
        defineData(vehicle.getEntityData());
    }

    @Override
    public void tick() {
        Runnable task;
        while ((task = tasks.poll()) != null) {
            task.run();
        }

        if (isStarted() && !canEngineStayOn()) {
            setStarted(false);
        }

        // Don't allow movement if locked in werkstatt
        if (vehicle.isLockedInWerkstatt()) {
            vehicle.setDeltaMovement(Vec3.ZERO);
            setSpeed(0F);
        } else {
            updateGravity();
            controlVehicle();
            checkPush();
        }

        vehicle.move(MoverType.SELF, vehicle.getDeltaMovement());

        // Odometer: Distanz messen (nur serverseitig)
        if (!vehicle.level().isClientSide) {
            updateOdometer();
        }

        if (vehicle.level().isClientSide) {
            updateSounds();
        }

        updateWheelRotation();
    }

    public boolean canCollideWith(Entity entityIn) {
        if (!vehicle.level().isClientSide && ModConfigHandler.VEHICLE_SERVER.damageEntities.get() && entityIn instanceof LivingEntity && !vehicle.getPassengers().contains(entityIn)) {
            if (entityIn.getBoundingBox().intersects(vehicle.getBoundingBox())) {
                float speed = getSpeed();
                if (speed > VehicleConstants.MIN_DAMAGE_SPEED) {
                    float damage = speed * VehicleConstants.DAMAGE_MULTIPLIER;
                    tasks.add(() -> {
                        ServerLevel serverLevel = (ServerLevel) vehicle.level();
                        Optional<Holder.Reference<DamageType>> holder = serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(DamageSourceVehicle.DAMAGE_VEHICLE_TYPE);
                        holder.ifPresent(damageTypeReference -> entityIn.hurt(new DamageSource(damageTypeReference, vehicle), damage));
                    });
                }
            }
        }
        return true;
    }

    public void checkPush() {
        List<Player> list = vehicle.level().getEntitiesOfClass(Player.class, vehicle.getBoundingBox().expandTowards(0.2, 0, 0.2).expandTowards(-0.2, 0, -0.2));

        for (Player player : list) {
            if (!player.hasPassenger(vehicle) && player.isShiftKeyDown()) {
                double motX = vehicle.calculateMotionX(0.05F, player.getYRot());
                double motZ = vehicle.calculateMotionZ(0.05F, player.getYRot());
                vehicle.move(MoverType.PLAYER, new Vec3(motX, 0, motZ));
                return;
            }
        }
    }

    public boolean canEngineStayOn() {
        // Always allow engine to stay on when parked at towing yard
        if (vehicle.isOnTowingYard()) {
            return true;
        }

        if (vehicle.isInWater() || vehicle.isInLava()) {
            return false;
        }

        // Optimierung: Cache Component-Getter (nur 1 Aufruf statt 2)
        FuelComponent fuel = vehicle.getFuelComponent();
        if (fuel != null && !fuel.hasFuel()) {
            return false;
        }

        // Optimierung: Cache Component-Getter (nur 1 Aufruf statt 2)
        DamageComponent damage = vehicle.getDamageComponent();
        if (damage != null && !damage.canEngineStayOn()) {
            return false;
        }

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void updateSounds() {
        // Optimierung: Cache Zustandsprüfungen
        float currentSpeed = getSpeed();
        boolean started = isStarted();

        if (currentSpeed == 0 && started) {
            if (!startedLast) {
                checkStartLoop();
            } else if (!isSoundPlaying(startLoop)) {
                if (startLoop != null) {
                    startLoop.setDonePlaying();
                    startLoop = null;
                }
                checkIdleLoop();
            }
        }
        if (currentSpeed != 0 && started) {
            checkHighLoop();
        }

        // Optimierung: Cache Component-Getter
        BatteryComponent battery = vehicle.getBatteryComponent();
        if (battery != null && !started && battery.isStarting()) {
            battery.checkStartingLoop();
        }

        startedLast = started;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isSoundPlaying(SoundInstance sound) {
        if (sound == null) {
            return false;
        }
        return Minecraft.getInstance().getSoundManager().isActive(sound);
    }

    private void controlVehicle() {
        if (!vehicle.isVehicle()) {
            setForward(false);
            setBackward(false);
            setLeft(false);
            setRight(false);
        }

        float modifier = getModifier();

        float maxSp = vehicle.getMaxSpeed() * modifier;
        float maxBackSp = vehicle.getMaxReverseSpeed() * modifier;

        float speed = subtractToZero(getSpeed(), vehicle.getRollResistance());

        if (isForward()) {
            if (speed <= maxSp) {
                speed = Math.min(speed + vehicle.getAcceleration(), maxSp);
            }
        }

        if (isBackward()) {
            if (speed >= -maxBackSp) {
                speed = Math.max(speed - vehicle.getAcceleration(), -maxBackSp);
            }
        }

        setSpeed(speed);

        float rotationSpeed = 0;
        if (Math.abs(speed) > VehicleConstants.MIN_ROTATION_THRESHOLD) {
            rotationSpeed = Mth.abs(vehicle.getRotationModifier() / (float) Math.pow(speed, 2));
            rotationSpeed = Mth.clamp(rotationSpeed, vehicle.getMinRotationSpeed(), vehicle.getMaxRotationSpeed());
        }

        vehicle.setDeltaRotation(0);

        if (speed < 0) {
            rotationSpeed = -rotationSpeed;
        }

        if (isLeft()) {
            vehicle.setDeltaRotation(vehicle.getDeltaRotation() - rotationSpeed);
        }
        if (isRight()) {
            vehicle.setDeltaRotation(vehicle.getDeltaRotation() + rotationSpeed);
        }

        vehicle.setYRot(vehicle.getYRot() + vehicle.getDeltaRotation());
        float delta = Math.abs(vehicle.getYRot() - vehicle.yRotO);
        while (vehicle.getYRot() > 180F) {
            vehicle.setYRot(vehicle.getYRot() - 360F);
            vehicle.yRotO = vehicle.getYRot() - delta;
        }
        while (vehicle.getYRot() <= -180F) {
            vehicle.setYRot(vehicle.getYRot() + 360F);
            vehicle.yRotO = delta + vehicle.getYRot();
        }

        if (vehicle.horizontalCollision) {
            if (vehicle.level().isClientSide && !collidedLastTick) {
                onCollision(speed);
                collidedLastTick = true;
            }
        } else {
            vehicle.setDeltaMovement(vehicle.calculateMotionX(getSpeed(), vehicle.getYRot()), vehicle.getDeltaMovement().y, vehicle.calculateMotionZ(getSpeed(), vehicle.getYRot()));
            if (vehicle.level().isClientSide) {
                collidedLastTick = false;
            }
        }
    }

    private float subtractToZero(float value, float subtract) {
        if (value > 0) {
            return Math.max(0, value - subtract);
        } else if (value < 0) {
            return Math.min(0, value + subtract);
        }
        return 0;
    }

    public float getModifier() {
        BlockPos pos = new BlockPos((int) vehicle.getX(), (int) (vehicle.getY() - 0.1D), (int) vehicle.getZ());
        BlockState state = vehicle.level().getBlockState(pos);

        float baseModifier;
        if (state.isAir() || ModConfigHandler.VEHICLE_SERVER.vehicleDriveBlockList.stream().anyMatch(tag -> tag.contains(state.getBlock()))) {
            baseModifier = ModConfigHandler.VEHICLE_SERVER.vehicleOnroadSpeed.get().floatValue();
        } else {
            baseModifier = ModConfigHandler.VEHICLE_SERVER.vehicleOffroadSpeed.get().floatValue();
        }

        // Tire season modifier (Serene Seasons Integration)
        float seasonModifier = getTireSeasonModifier();

        return baseModifier * seasonModifier;
    }

    private float getTireSeasonModifier() {
        de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartTireBase tire = vehicle.getPartByClass(
            de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartTireBase.class);
        if (tire == null) {
            return 1.0F;
        }
        return de.rolandsw.schedulemc.vehicle.util.SereneSeasonsCompat.getTireSeasonModifier(
            tire.getSeasonType(), vehicle.level());
    }

    public void onCollision(float speed) {
        if (vehicle.level().isClientSide) {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageCrash(speed, vehicle));
        }

        // Optimierung: Cache Component-Getter
        DamageComponent damage = vehicle.getDamageComponent();
        if (damage != null) {
            damage.onCollision(speed);
        }

        setSpeed(VehicleConstants.POST_COLLISION_SPEED);
        vehicle.setDeltaMovement(0D, vehicle.getDeltaMovement().y, 0D);
    }

    private void updateGravity() {
        if (vehicle.isNoGravity()) {
            vehicle.setDeltaMovement(vehicle.getDeltaMovement().x, 0D, vehicle.getDeltaMovement().z);
            return;
        }
        vehicle.setDeltaMovement(vehicle.getDeltaMovement().x, vehicle.getDeltaMovement().y - 0.2D, vehicle.getDeltaMovement().z);
    }

    public void updateControls(boolean forward, boolean backward, boolean left, boolean right) {
        boolean needsUpdate = false;

        if (isForward() != forward) {
            setForward(forward);
            needsUpdate = true;
        }

        if (isBackward() != backward) {
            setBackward(backward);
            needsUpdate = true;
        }

        if (isLeft() != left) {
            setLeft(left);
            needsUpdate = true;
        }

        if (isRight() != right) {
            setRight(right);
            needsUpdate = true;
        }
    }

    public void startVehicleEngine() {
        Player player = vehicle.getDriver();
        if (player != null && canStartVehicleEngine(player)) {
            setStarted(true);

            // Remove towing yard flag when engine starts (player is taking vehicle from yard)
            if (vehicle.isOnTowingYard()) {
                vehicle.setIsOnTowingYard(false);
            }

            // Consume fuel when starting the engine
            // Optimierung: Cache Component-Getter
            FuelComponent fuel = vehicle.getFuelComponent();
            if (fuel != null) {
                int startFuelCost = ModConfigHandler.VEHICLE_SERVER.engineStartFuelConsumption.get();
                fuel.removeFuel(startFuelCost);
            }
        }
    }

    public boolean canStartVehicleEngine(Player player) {
        // Always allow engine start when parked at towing yard (no fuel consumption there)
        if (vehicle.isOnTowingYard()) {
            return true;
        }

        if (vehicle.isInWater() || vehicle.isInLava()) {
            return false;
        }

        // Optimierung: Cache Component-Getter (nur 1 Aufruf statt 2)
        FuelComponent fuel = vehicle.getFuelComponent();
        if (fuel != null && !fuel.hasFuel()) {
            return false;
        }

        // Optimierung: Cache Component-Getter (nur 1 Aufruf statt 2)
        DamageComponent damage = vehicle.getDamageComponent();
        if (damage != null && !damage.canStartVehicleEngine()) {
            return false;
        }

        return true;
    }

    public boolean canPlayerDriveVehicle(Player player) {
        if (player.equals(vehicle.getDriver()) && isStarted()) {
            FuelComponent fuel = vehicle.getFuelComponent();
            if (fuel != null && !fuel.hasFuel()) {
                return false;
            }
            return true;
        } else if (vehicle.isInWater() || vehicle.isInLava()) {
            return false;
        } else {
            return false;
        }
    }

    public float getKilometerPerHour() {
        return (getSpeed() * 20 * 60 * 60) / 1000;
    }

    public void updateWheelRotation() {
        wheelRotation += vehicle.getWheelRotationAmount();
    }

    public float getWheelRotation(float partialTicks) {
        return wheelRotation + vehicle.getWheelRotationAmount() * partialTicks;
    }

    public boolean isAccelerating() {
        boolean b = (isForward() || isBackward()) && !vehicle.horizontalCollision;
        return b && isStarted();
    }

    public void setSpeed(float speed) {
        vehicle.getEntityData().set(SPEED, speed);
    }

    public float getSpeed() {
        return vehicle.getEntityData().get(SPEED);
    }

    public void setStarted(boolean started) {
        setStarted(started, true, false);
    }

    public void setStarted(boolean started, boolean playStopSound, boolean playFailSound) {
        if (!started && playStopSound) {
            playStopSound();
        } else if (!started && playFailSound) {
            playFailSound();
        }
        if (started) {
            setForward(false);
            setBackward(false);
            setLeft(false);
            setRight(false);
        }
        vehicle.getEntityData().set(STARTED, started);

        // Optimierung: Cache Component-Getter
        BatteryComponent battery = vehicle.getBatteryComponent();
        if (battery != null) {
            battery.setStarting(false, false);
        }
    }

    public boolean isStarted() {
        return vehicle.getEntityData().get(STARTED);
    }

    public void setForward(boolean forward) {
        vehicle.getEntityData().set(FORWARD, forward);
    }

    public boolean isForward() {
        if (vehicle.getDriver() == null || !canPlayerDriveVehicle(vehicle.getDriver())) {
            return false;
        }
        return vehicle.getEntityData().get(FORWARD);
    }

    public void setBackward(boolean backward) {
        vehicle.getEntityData().set(BACKWARD, backward);
    }

    public boolean isBackward() {
        if (vehicle.getDriver() == null || !canPlayerDriveVehicle(vehicle.getDriver())) {
            return false;
        }
        return vehicle.getEntityData().get(BACKWARD);
    }

    public void setLeft(boolean left) {
        vehicle.getEntityData().set(LEFT, left);
    }

    public boolean isLeft() {
        if (vehicle.getDriver() == null || !canPlayerDriveVehicle(vehicle.getDriver())) {
            return false;
        }
        return vehicle.getEntityData().get(LEFT);
    }

    public void setRight(boolean right) {
        vehicle.getEntityData().set(RIGHT, right);
    }

    public boolean isRight() {
        if (vehicle.getDriver() == null || !canPlayerDriveVehicle(vehicle.getDriver())) {
            return false;
        }
        return vehicle.getEntityData().get(RIGHT);
    }

    public void playStopSound() {
        ModSounds.playSound(getStopSound(), vehicle.level(), vehicle.blockPosition(), null, SoundSource.MASTER, 1F);
    }

    public void playFailSound() {
        ModSounds.playSound(getFailSound(), vehicle.level(), vehicle.blockPosition(), null, SoundSource.MASTER, 1F);
    }

    public void playCrashSound() {
        ModSounds.playSound(getCrashSound(), vehicle.level(), vehicle.blockPosition(), null, SoundSource.MASTER, 1F);
    }

    public void playHornSound() {
        ModSounds.playSound(getHornSound(), vehicle.level(), vehicle.blockPosition(), null, SoundSource.MASTER, 1F);
    }

    public SoundEvent getStopSound() {
        return vehicle.getStopSound();
    }

    public SoundEvent getFailSound() {
        return vehicle.getFailSound();
    }

    public SoundEvent getCrashSound() {
        return vehicle.getCrashSound();
    }

    public SoundEvent getStartSound() {
        return vehicle.getStartSound();
    }

    public SoundEvent getStartingSound() {
        return vehicle.getStartingSound();
    }

    public SoundEvent getIdleSound() {
        return vehicle.getIdleSound();
    }

    public SoundEvent getHighSound() {
        return vehicle.getHighSound();
    }

    public SoundEvent getHornSound() {
        return vehicle.getHornSound();
    }

    @OnlyIn(Dist.CLIENT)
    public void checkIdleLoop() {
        if (!isSoundPlaying(idleLoop)) {
            idleLoop = new SoundLoopIdle(vehicle, getIdleSound(), SoundSource.MASTER);
            ModSounds.playSoundLoop(idleLoop, vehicle.level());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void checkHighLoop() {
        if (!isSoundPlaying(highLoop)) {
            highLoop = new SoundLoopHigh(vehicle, getHighSound(), SoundSource.MASTER);
            ModSounds.playSoundLoop(highLoop, vehicle.level());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void checkStartLoop() {
        if (!isSoundPlaying(startLoop)) {
            startLoop = new SoundLoopStart(vehicle, getStartSound(), SoundSource.MASTER);
            ModSounds.playSoundLoop(startLoop, vehicle.level());
        }
    }

    public void onHornPressed(Player player) {
        if (vehicle.level().isClientSide) {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageVehicleHorn(true, player));
        } else {
            // Optimierung: Cache Component-Getter
            BatteryComponent battery = vehicle.getBatteryComponent();
            if (battery != null) {
                if (battery.getBatteryLevel() < VehicleConstants.MIN_BATTERY_FOR_HORN) {
                    return;
                }
                if (ModConfigHandler.VEHICLE_SERVER.useBattery.get()) {
                    battery.setBatteryLevel(battery.getBatteryLevel() - VehicleConstants.HORN_BATTERY_COST);
                }
            }
            playHornSound();
            if (ModConfigHandler.VEHICLE_SERVER.hornFlee.get()) {
                double radius = VehicleConstants.HORN_FLEE_RADIUS;
                // Optimierung: Stream-API statt for-Schleife
                vehicle.level().getEntitiesOfClass(Monster.class,
                    new AABB(vehicle.getX() - radius, vehicle.getY() - radius, vehicle.getZ() - radius,
                             vehicle.getX() + radius, vehicle.getY() + radius, vehicle.getZ() + radius))
                    .forEach(this::fleeEntity);
            }
        }
    }

    public void fleeEntity(Monster entity) {
        Vec3 vecVehicle = new Vec3(vehicle.getX(), vehicle.getY(), vehicle.getZ());
        Vec3 vecEntity = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        Vec3 fleeDir = vecEntity.subtract(vecVehicle);
        fleeDir = fleeDir.normalize();
        entity.getNavigation().moveTo(
            vecEntity.x + fleeDir.x * VehicleConstants.FLEE_DISTANCE,
            vecEntity.y + fleeDir.y * VehicleConstants.FLEE_DISTANCE,
            vecEntity.z + fleeDir.z * VehicleConstants.FLEE_DISTANCE,
            VehicleConstants.FLEE_SPEED
        );
    }

    // ═══════════════════════════════════════════════════════════
    // ODOMETER
    // ═══════════════════════════════════════════════════════════

    private void updateOdometer() {
        double currentX = vehicle.getX();
        double currentZ = vehicle.getZ();

        if (!odometerInitialized) {
            lastPosX = currentX;
            lastPosZ = currentZ;
            odometerInitialized = true;
            return;
        }

        double dx = currentX - lastPosX;
        double dz = currentZ - lastPosZ;
        double distSq = dx * dx + dz * dz;

        // Nur zählen wenn sich das Fahrzeug tatsächlich bewegt hat (> 0.01 Blöcke)
        if (distSq > 0.0001) {
            odometerAccumulator += Math.sqrt(distSq);
            lastPosX = currentX;
            lastPosZ = currentZ;

            if (odometerAccumulator >= 1.0) {
                long blocks = (long) odometerAccumulator;
                odometer += blocks;
                odometerAccumulator -= blocks;
                vehicle.getEntityData().set(ODOMETER_SYNCED, (int) odometer);
            }
        }
    }

    public long getOdometer() {
        if (vehicle.level().isClientSide) {
            return vehicle.getEntityData().get(ODOMETER_SYNCED);
        }
        return odometer;
    }

    public void setOdometer(long odometer) {
        this.odometer = odometer;
        vehicle.getEntityData().set(ODOMETER_SYNCED, (int) odometer);
    }

    /**
     * Gibt die maximale Gesundheit (0.0 - 1.0) basierend auf dem Kilometerstand zurück.
     * Wird von DamageComponent verwendet um den Schaden zu begrenzen.
     */
    public float getMaxHealthPercent() {
        if (!ModConfigHandler.VEHICLE_SERVER.vehicleAgingEnabled.get()) {
            return 1.0F;
        }

        long tier1 = ModConfigHandler.VEHICLE_SERVER.odometerTier1.get();
        long tier2 = ModConfigHandler.VEHICLE_SERVER.odometerTier2.get();
        long tier3 = ModConfigHandler.VEHICLE_SERVER.odometerTier3.get();

        if (odometer < tier1) {
            return ModConfigHandler.VEHICLE_SERVER.agingMaxHealthTier0.get().floatValue();
        } else if (odometer < tier2) {
            return ModConfigHandler.VEHICLE_SERVER.agingMaxHealthTier1.get().floatValue();
        } else if (odometer < tier3) {
            return ModConfigHandler.VEHICLE_SERVER.agingMaxHealthTier2.get().floatValue();
        } else {
            return ModConfigHandler.VEHICLE_SERVER.agingMaxHealthTier3.get().floatValue();
        }
    }

    @Override
    public void saveAdditionalData(CompoundTag compound) {
        compound.putBoolean("started", isStarted());
        compound.putLong("odometer", odometer);
    }

    @Override
    public void readAdditionalData(CompoundTag compound) {
        setStarted(compound.getBoolean("started"), false, false);
        odometer = compound.getLong("odometer");
        vehicle.getEntityData().set(ODOMETER_SYNCED, (int) odometer);
    }
}
