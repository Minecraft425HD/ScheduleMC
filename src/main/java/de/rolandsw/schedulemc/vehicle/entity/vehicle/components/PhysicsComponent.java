package de.rolandsw.schedulemc.vehicle.entity.vehicle.components;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.DamageSourceVehicle;
import de.rolandsw.schedulemc.vehicle.Main;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    private float wheelRotation;

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

    private final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

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

        updateGravity();
        controlCar();
        checkPush();

        vehicle.move(MoverType.SELF, vehicle.getDeltaMovement());

        if (vehicle.level().isClientSide) {
            updateSounds();
        }

        updateWheelRotation();
    }

    public boolean canCollideWith(Entity entityIn) {
        if (!vehicle.level().isClientSide && ModConfigHandler.VEHICLE_SERVER.damageEntities.get() && entityIn instanceof LivingEntity && !vehicle.getPassengers().contains(entityIn)) {
            if (entityIn.getBoundingBox().intersects(vehicle.getBoundingBox())) {
                float speed = getSpeed();
                if (speed > 0.35F) {
                    float damage = speed * 10;
                    tasks.add(() -> {
                        ServerLevel serverLevel = (ServerLevel) vehicle.level();
                        Optional<Holder.Reference<DamageType>> holder = serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(DamageSourceVehicle.DAMAGE_CAR_TYPE);
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
        if (vehicle.isInWater() || vehicle.isInLava()) {
            return false;
        }

        FuelComponent fuel = vehicle.getFuelComponent();
        if (fuel != null && !fuel.hasFuel()) {
            return false;
        }

        DamageComponent damage = vehicle.getDamageComponent();
        if (damage != null && !damage.canEngineStayOn()) {
            return false;
        }

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void updateSounds() {
        if (getSpeed() == 0 && isStarted()) {
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
        if (getSpeed() != 0 && isStarted()) {
            checkHighLoop();
        }

        BatteryComponent battery = vehicle.getBatteryComponent();
        if (battery != null && !isStarted() && battery.isStarting()) {
            battery.checkStartingLoop();
        }

        startedLast = isStarted();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isSoundPlaying(SoundInstance sound) {
        if (sound == null) {
            return false;
        }
        return Minecraft.getInstance().getSoundManager().isActive(sound);
    }

    private void controlCar() {
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
        if (Math.abs(speed) > 0.02F) {
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
        return Math.max(0, value - subtract);
    }

    public float getModifier() {
        BlockPos pos = new BlockPos((int) vehicle.getX(), (int) (vehicle.getY() - 0.1D), (int) vehicle.getZ());
        BlockState state = vehicle.level().getBlockState(pos);

        if (state.isAir() || ModConfigHandler.VEHICLE_SERVER.vehicleDriveBlockList.stream().anyMatch(tag -> tag.contains(state.getBlock()))) {
            return ModConfigHandler.VEHICLE_SERVER.vehicleOnroadSpeed.get().floatValue();
        } else {
            return ModConfigHandler.VEHICLE_SERVER.vehicleOffroadSpeed.get().floatValue();
        }
    }

    public void onCollision(float speed) {
        if (vehicle.level().isClientSide) {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageCrash(speed, vehicle));
        }

        DamageComponent damage = vehicle.getDamageComponent();
        if (damage != null) {
            damage.onCollision(speed);
        }

        setSpeed(0.01F);
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

    public void startCarEngine() {
        Player player = vehicle.getDriver();
        if (player != null && canStartCarEngine(player)) {
            setStarted(true);

            // Consume fuel when starting the engine
            FuelComponent fuel = vehicle.getFuelComponent();
            if (fuel != null) {
                int startFuelCost = ModConfigHandler.VEHICLE_SERVER.engineStartFuelConsumption.get();
                fuel.removeFuel(startFuelCost);
            }
        }
    }

    public boolean canStartCarEngine(Player player) {
        if (vehicle.isInWater() || vehicle.isInLava()) {
            return false;
        }

        FuelComponent fuel = vehicle.getFuelComponent();
        if (fuel != null && !fuel.hasFuel()) {
            return false;
        }

        DamageComponent damage = vehicle.getDamageComponent();
        if (damage != null && !damage.canStartCarEngine()) {
            return false;
        }

        return true;
    }

    public boolean canPlayerDriveCar(Player player) {
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
        if (vehicle.getDriver() == null || !canPlayerDriveCar(vehicle.getDriver())) {
            return false;
        }
        return vehicle.getEntityData().get(FORWARD);
    }

    public void setBackward(boolean backward) {
        vehicle.getEntityData().set(BACKWARD, backward);
    }

    public boolean isBackward() {
        if (vehicle.getDriver() == null || !canPlayerDriveCar(vehicle.getDriver())) {
            return false;
        }
        return vehicle.getEntityData().get(BACKWARD);
    }

    public void setLeft(boolean left) {
        vehicle.getEntityData().set(LEFT, left);
    }

    public boolean isLeft() {
        return vehicle.getEntityData().get(LEFT);
    }

    public void setRight(boolean right) {
        vehicle.getEntityData().set(RIGHT, right);
    }

    public boolean isRight() {
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
            BatteryComponent battery = vehicle.getBatteryComponent();
            if (battery != null) {
                if (battery.getBatteryLevel() < 10) {
                    return;
                }
                if (ModConfigHandler.VEHICLE_SERVER.useBattery.get()) {
                    battery.setBatteryLevel(battery.getBatteryLevel() - 10);
                }
            }
            playHornSound();
            if (ModConfigHandler.VEHICLE_SERVER.hornFlee.get()) {
                double radius = 15;
                List<Monster> list = vehicle.level().getEntitiesOfClass(Monster.class, new AABB(vehicle.getX() - radius, vehicle.getY() - radius, vehicle.getZ() - radius, vehicle.getX() + radius, vehicle.getY() + radius, vehicle.getZ() + radius));
                for (Monster ent : list) {
                    fleeEntity(ent);
                }
            }
        }
    }

    public void fleeEntity(Monster entity) {
        double fleeDistance = 10;
        Vec3 vecCar = new Vec3(vehicle.getX(), vehicle.getY(), vehicle.getZ());
        Vec3 vecEntity = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        Vec3 fleeDir = vecEntity.subtract(vecCar);
        fleeDir = fleeDir.normalize();
        entity.getNavigation().moveTo(vecEntity.x + fleeDir.x * fleeDistance, vecEntity.y + fleeDir.y * fleeDistance, vecEntity.z + fleeDir.z * fleeDistance, 2.5);
    }

    @Override
    public void saveAdditionalData(CompoundTag compound) {
        compound.putBoolean("started", isStarted());
    }

    @Override
    public void readAdditionalData(CompoundTag compound) {
        setStarted(compound.getBoolean("started"), false, false);
    }
}
