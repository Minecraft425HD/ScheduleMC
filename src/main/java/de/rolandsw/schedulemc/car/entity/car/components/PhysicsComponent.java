package de.rolandsw.schedulemc.car.entity.car.components;

import de.rolandsw.schedulemc.car.DamageSourceCar;
import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import de.rolandsw.schedulemc.car.net.MessageCarHorn;
import de.rolandsw.schedulemc.car.net.MessageCrash;
import de.rolandsw.schedulemc.car.sounds.ModSounds;
import de.rolandsw.schedulemc.car.sounds.SoundLoopHigh;
import de.rolandsw.schedulemc.car.sounds.SoundLoopIdle;
import de.rolandsw.schedulemc.car.sounds.SoundLoopStart;
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
 * Manages physics, movement, controls, and sounds for the car
 */
public class PhysicsComponent extends CarComponent {

    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> STARTED = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FORWARD = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BACKWARD = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LEFT = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RIGHT = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.BOOLEAN);

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

    public PhysicsComponent(EntityGenericCar car) {
        super(car);
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
        defineData(car.getEntityData());
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

        car.move(MoverType.SELF, car.getDeltaMovement());

        if (car.level().isClientSide) {
            updateSounds();
        }

        updateWheelRotation();
    }

    public boolean canCollideWith(Entity entityIn) {
        if (!car.level().isClientSide && Main.SERVER_CONFIG.damageEntities.get() && entityIn instanceof LivingEntity && !car.getPassengers().contains(entityIn)) {
            if (entityIn.getBoundingBox().intersects(car.getBoundingBox())) {
                float speed = getSpeed();
                if (speed > 0.35F) {
                    float damage = speed * 10;
                    tasks.add(() -> {
                        ServerLevel serverLevel = (ServerLevel) car.level();
                        Optional<Holder.Reference<DamageType>> holder = serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(DamageSourceCar.DAMAGE_CAR_TYPE);
                        holder.ifPresent(damageTypeReference -> entityIn.hurt(new DamageSource(damageTypeReference, car), damage));
                    });
                }
            }
        }
        return true;
    }

    public void checkPush() {
        List<Player> list = car.level().getEntitiesOfClass(Player.class, car.getBoundingBox().expandTowards(0.2, 0, 0.2).expandTowards(-0.2, 0, -0.2));

        for (Player player : list) {
            if (!player.hasPassenger(car) && player.isShiftKeyDown()) {
                double motX = car.calculateMotionX(0.05F, player.getYRot());
                double motZ = car.calculateMotionZ(0.05F, player.getYRot());
                car.move(MoverType.PLAYER, new Vec3(motX, 0, motZ));
                return;
            }
        }
    }

    public boolean canEngineStayOn() {
        if (car.isInWater() || car.isInLava()) {
            return false;
        }

        FuelComponent fuel = car.getFuelComponent();
        if (fuel != null && !fuel.hasFuel()) {
            return false;
        }

        DamageComponent damage = car.getDamageComponent();
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

        BatteryComponent battery = car.getBatteryComponent();
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
        if (!car.isVehicle()) {
            setForward(false);
            setBackward(false);
            setLeft(false);
            setRight(false);
        }

        float modifier = getModifier();

        float maxSp = car.getMaxSpeed() * modifier;
        float maxBackSp = car.getMaxReverseSpeed() * modifier;

        float speed = subtractToZero(getSpeed(), car.getRollResistance());

        if (isForward()) {
            if (speed <= maxSp) {
                speed = Math.min(speed + car.getAcceleration(), maxSp);
            }
        }

        if (isBackward()) {
            if (speed >= -maxBackSp) {
                speed = Math.max(speed - car.getAcceleration(), -maxBackSp);
            }
        }

        setSpeed(speed);

        float rotationSpeed = 0;
        if (Math.abs(speed) > 0.02F) {
            rotationSpeed = Mth.abs(car.getRotationModifier() / (float) Math.pow(speed, 2));
            rotationSpeed = Mth.clamp(rotationSpeed, car.getMinRotationSpeed(), car.getMaxRotationSpeed());
        }

        car.setDeltaRotation(0);

        if (speed < 0) {
            rotationSpeed = -rotationSpeed;
        }

        if (isLeft()) {
            car.setDeltaRotation(car.getDeltaRotation() - rotationSpeed);
        }
        if (isRight()) {
            car.setDeltaRotation(car.getDeltaRotation() + rotationSpeed);
        }

        car.setYRot(car.getYRot() + car.getDeltaRotation());
        float delta = Math.abs(car.getYRot() - car.yRotO);
        while (car.getYRot() > 180F) {
            car.setYRot(car.getYRot() - 360F);
            car.yRotO = car.getYRot() - delta;
        }
        while (car.getYRot() <= -180F) {
            car.setYRot(car.getYRot() + 360F);
            car.yRotO = delta + car.getYRot();
        }

        if (car.horizontalCollision) {
            if (car.level().isClientSide && !collidedLastTick) {
                onCollision(speed);
                collidedLastTick = true;
            }
        } else {
            car.setDeltaMovement(car.calculateMotionX(getSpeed(), car.getYRot()), car.getDeltaMovement().y, car.calculateMotionZ(getSpeed(), car.getYRot()));
            if (car.level().isClientSide) {
                collidedLastTick = false;
            }
        }
    }

    private float subtractToZero(float value, float subtract) {
        return Math.max(0, value - subtract);
    }

    public float getModifier() {
        BlockPos pos = new BlockPos((int) car.getX(), (int) (car.getY() - 0.1D), (int) car.getZ());
        BlockState state = car.level().getBlockState(pos);

        if (state.isAir() || Main.SERVER_CONFIG.carDriveBlockList.stream().anyMatch(tag -> tag.contains(state.getBlock()))) {
            return Main.SERVER_CONFIG.carOnroadSpeed.get().floatValue();
        } else {
            return Main.SERVER_CONFIG.carOffroadSpeed.get().floatValue();
        }
    }

    public void onCollision(float speed) {
        if (car.level().isClientSide) {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageCrash(speed, car));
        }

        DamageComponent damage = car.getDamageComponent();
        if (damage != null) {
            damage.onCollision(speed);
        }

        setSpeed(0.01F);
        car.setDeltaMovement(0D, car.getDeltaMovement().y, 0D);
    }

    private void updateGravity() {
        if (car.isNoGravity()) {
            car.setDeltaMovement(car.getDeltaMovement().x, 0D, car.getDeltaMovement().z);
            return;
        }
        car.setDeltaMovement(car.getDeltaMovement().x, car.getDeltaMovement().y - 0.2D, car.getDeltaMovement().z);
    }

    public void updateControls(boolean forward, boolean backward, boolean left, boolean right) {
        boolean needsUpdate = false;

        if (isForward() != forward) {
            setForward(forward);
            needsUpdate = true;
            System.out.println("[PhysicsComponent] FORWARD changed to: " + forward);
        }

        if (isBackward() != backward) {
            setBackward(backward);
            needsUpdate = true;
            System.out.println("[PhysicsComponent] BACKWARD changed to: " + backward);
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
        Player player = car.getDriver();
        if (player != null && canStartCarEngine(player)) {
            setStarted(true);
        }
    }

    public boolean canStartCarEngine(Player player) {
        if (car.isInWater() || car.isInLava()) {
            return false;
        }

        FuelComponent fuel = car.getFuelComponent();
        if (fuel != null && !fuel.hasFuel()) {
            return false;
        }

        DamageComponent damage = car.getDamageComponent();
        if (damage != null && !damage.canStartCarEngine()) {
            return false;
        }

        return true;
    }

    public boolean canPlayerDriveCar(Player player) {
        if (player.equals(car.getDriver()) && isStarted()) {
            FuelComponent fuel = car.getFuelComponent();
            if (fuel != null && !fuel.hasFuel()) {
                return false;
            }
            return true;
        } else if (car.isInWater() || car.isInLava()) {
            return false;
        } else {
            return false;
        }
    }

    public float getKilometerPerHour() {
        return (getSpeed() * 20 * 60 * 60) / 1000;
    }

    public void updateWheelRotation() {
        wheelRotation += car.getWheelRotationAmount();
    }

    public float getWheelRotation(float partialTicks) {
        return wheelRotation + car.getWheelRotationAmount() * partialTicks;
    }

    public boolean isAccelerating() {
        boolean b = (isForward() || isBackward()) && !car.horizontalCollision;
        return b && isStarted();
    }

    public void setSpeed(float speed) {
        car.getEntityData().set(SPEED, speed);
    }

    public float getSpeed() {
        return car.getEntityData().get(SPEED);
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
        car.getEntityData().set(STARTED, started);

        BatteryComponent battery = car.getBatteryComponent();
        if (battery != null) {
            battery.setStarting(false, false);
        }
    }

    public boolean isStarted() {
        return car.getEntityData().get(STARTED);
    }

    public void setForward(boolean forward) {
        car.getEntityData().set(FORWARD, forward);
    }

    public boolean isForward() {
        if (car.getDriver() == null || !canPlayerDriveCar(car.getDriver())) {
            return false;
        }
        return car.getEntityData().get(FORWARD);
    }

    public void setBackward(boolean backward) {
        car.getEntityData().set(BACKWARD, backward);
    }

    public boolean isBackward() {
        if (car.getDriver() == null || !canPlayerDriveCar(car.getDriver())) {
            return false;
        }
        return car.getEntityData().get(BACKWARD);
    }

    public void setLeft(boolean left) {
        car.getEntityData().set(LEFT, left);
    }

    public boolean isLeft() {
        return car.getEntityData().get(LEFT);
    }

    public void setRight(boolean right) {
        car.getEntityData().set(RIGHT, right);
    }

    public boolean isRight() {
        return car.getEntityData().get(RIGHT);
    }

    public void playStopSound() {
        ModSounds.playSound(getStopSound(), car.level(), car.blockPosition(), null, SoundSource.MASTER, 1F);
    }

    public void playFailSound() {
        ModSounds.playSound(getFailSound(), car.level(), car.blockPosition(), null, SoundSource.MASTER, 1F);
    }

    public void playCrashSound() {
        ModSounds.playSound(getCrashSound(), car.level(), car.blockPosition(), null, SoundSource.MASTER, 1F);
    }

    public void playHornSound() {
        ModSounds.playSound(getHornSound(), car.level(), car.blockPosition(), null, SoundSource.MASTER, 1F);
    }

    public SoundEvent getStopSound() {
        return car.getStopSound();
    }

    public SoundEvent getFailSound() {
        return car.getFailSound();
    }

    public SoundEvent getCrashSound() {
        return car.getCrashSound();
    }

    public SoundEvent getStartSound() {
        return car.getStartSound();
    }

    public SoundEvent getStartingSound() {
        return car.getStartingSound();
    }

    public SoundEvent getIdleSound() {
        return car.getIdleSound();
    }

    public SoundEvent getHighSound() {
        return car.getHighSound();
    }

    public SoundEvent getHornSound() {
        return car.getHornSound();
    }

    @OnlyIn(Dist.CLIENT)
    public void checkIdleLoop() {
        if (!isSoundPlaying(idleLoop)) {
            idleLoop = new SoundLoopIdle(car, getIdleSound(), SoundSource.MASTER);
            ModSounds.playSoundLoop(idleLoop, car.level());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void checkHighLoop() {
        if (!isSoundPlaying(highLoop)) {
            highLoop = new SoundLoopHigh(car, getHighSound(), SoundSource.MASTER);
            ModSounds.playSoundLoop(highLoop, car.level());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void checkStartLoop() {
        if (!isSoundPlaying(startLoop)) {
            startLoop = new SoundLoopStart(car, getStartSound(), SoundSource.MASTER);
            ModSounds.playSoundLoop(startLoop, car.level());
        }
    }

    public void onHornPressed(Player player) {
        if (car.level().isClientSide) {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageCarHorn(true, player));
        } else {
            BatteryComponent battery = car.getBatteryComponent();
            if (battery != null) {
                if (battery.getBatteryLevel() < 10) {
                    return;
                }
                if (Main.SERVER_CONFIG.useBattery.get()) {
                    battery.setBatteryLevel(battery.getBatteryLevel() - 10);
                }
            }
            playHornSound();
            if (Main.SERVER_CONFIG.hornFlee.get()) {
                double radius = 15;
                List<Monster> list = car.level().getEntitiesOfClass(Monster.class, new AABB(car.getX() - radius, car.getY() - radius, car.getZ() - radius, car.getX() + radius, car.getY() + radius, car.getZ() + radius));
                for (Monster ent : list) {
                    fleeEntity(ent);
                }
            }
        }
    }

    public void fleeEntity(Monster entity) {
        double fleeDistance = 10;
        Vec3 vecCar = new Vec3(car.getX(), car.getY(), car.getZ());
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
