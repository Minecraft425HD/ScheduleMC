package de.rolandsw.schedulemc.vehicle.entity.vehicle.components;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.sounds.ModSounds;
import de.rolandsw.schedulemc.vehicle.sounds.SoundLoopStarting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Manages battery, starting system, and exhaust particles for the vehicle
 */
public class BatteryComponent extends VehicleComponent {

    private static final EntityDataAccessor<Integer> BATTERY_LEVEL = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STARTING_TIME = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> STARTING = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.BOOLEAN);

    @OnlyIn(Dist.CLIENT)
    private SoundLoopStarting startingLoop;

    // Server side
    private boolean carStopped;
    private boolean carStarted;

    // Client side
    private int timeSinceStarted;
    private int timeToStart;

    public BatteryComponent(EntityGenericVehicle vehicle) {
        super(vehicle);
    }

    public static void defineData(SynchedEntityData entityData) {
        entityData.define(BATTERY_LEVEL, 1000); // getMaxBatteryLevel() returns 1000
        entityData.define(STARTING_TIME, 0);
        entityData.define(STARTING, Boolean.FALSE);
    }

    @Override
    public void defineSynchedData() {
        defineData(vehicle.getEntityData());
    }

    @Override
    public void clientTick() {
        PhysicsComponent physics = vehicle.getPhysicsComponent();
        if (physics != null && physics.isStarted()) {
            timeSinceStarted++;
            if (vehicle.tickCount % 2 == 0) {
                spawnParticles(physics.getSpeed() > 0.1F);
            }
        } else {
            timeSinceStarted = 0;
        }
    }

    @Override
    public void serverTick() {
        PhysicsComponent physics = vehicle.getPhysicsComponent();
        if (physics == null) {
            return;
        }

        if (isStarting()) {
            if (vehicle.tickCount % 2 == 0) {
                setBatteryLevel(getBatteryLevel() - getBatteryUsage());
            }

            setStartingTime(getStartingTime() + 1);
            if (getBatteryLevel() <= 0) {
                setStarting(false, true);
            }
        } else {
            setStartingTime(0);
        }

        int time = getStartingTime();

        if (time > 0) {
            if (timeToStart <= 0) {
                timeToStart = getTimeToStart();
            }

            if (time > getTimeToStart()) {
                physics.startCarEngine();
                timeToStart = 0;
            }
        }

        if (physics.isStarted()) {
            setStartingTime(0);
            carStarted = true;

            // Realistic battery charging simulation
            rechargeBattery(physics);
        }
    }

    /**
     * Recharges battery based on engine state and speed
     * Simulates alternator charging - faster when driving, slower when idle
     */
    private void rechargeBattery(PhysicsComponent physics) {
        int baseRechargeRate = ModConfigHandler.CAR_SERVER.idleBatteryRechargeRate.get();

        if (baseRechargeRate <= 0) {
            return; // Charging disabled
        }

        int chargeAmount;
        float speed = physics.getSpeed();

        if (physics.isAccelerating() && speed > 0.01F) {
            // Driving: alternator generates more power at higher RPM
            double multiplier = ModConfigHandler.CAR_SERVER.drivingBatteryRechargeMultiplier.get();
            chargeAmount = (int) Math.ceil(baseRechargeRate * speed * multiplier);
        } else {
            // Idling: alternator generates minimal power
            chargeAmount = baseRechargeRate;
        }

        // Recharge battery
        setBatteryLevel(getBatteryLevel() + chargeAmount);
    }

    public void spawnParticles(boolean driving) {
        if (!vehicle.level().isClientSide) {
            return;
        }
        Vec3 lookVec = vehicle.getLookAngle().normalize();
        double offX = lookVec.x * -1D;
        double offY = lookVec.y;
        double offZ = lookVec.z * -1D;

        DamageComponent damage = vehicle.getDamageComponent();
        PhysicsComponent physics = vehicle.getPhysicsComponent();

        // Engine started smoke
        if (timeSinceStarted > 0 && timeSinceStarted < 20 && damage != null && damage.getTemperature() < 50F) {
            double speedX = lookVec.x * -0.1D;
            double speedZ = lookVec.z * -0.1D;

            if (damage != null) {
                float damageValue = damage.getDamage();
                int count = 1;
                double r = 0.1;

                if (damageValue > 0.9F) {
                    count = 6;
                    r = 0.7;
                } else if (damageValue > 0.75F) {
                    count = 3;
                    r = 0.7;
                } else if (damageValue > 0.5F) {
                    count = 2;
                    r = 0.3;
                }
                for (int i = 0; i <= count; i++) {
                    spawnParticle(ParticleTypes.LARGE_SMOKE, offX, offY, offZ, speedX, speedZ, r);
                }
            } else {
                spawnParticle(ParticleTypes.LARGE_SMOKE, offX, offY, offZ, speedX, speedZ);
            }
        } else if (driving) {
            double speedX = lookVec.x * -0.2D;
            double speedZ = lookVec.z * -0.2D;
            spawnParticle(ParticleTypes.SMOKE, offX, offY, offZ, speedX, speedZ);
        } else if (physics != null && physics.isStarted()) {
            double speedX = lookVec.x * -0.05D;
            double speedZ = lookVec.z * -0.05D;
            spawnParticle(ParticleTypes.SMOKE, offX, offY, offZ, speedX, speedZ);
        }
    }

    private void spawnParticle(ParticleOptions particleTypes, double offX, double offY, double offZ, double speedX, double speedZ, double r) {
        vehicle.level().addParticle(particleTypes,
                vehicle.getX() + offX + (vehicle.getRandom().nextDouble() * r - r / 2D),
                vehicle.getY() + offY + (vehicle.getRandom().nextDouble() * r - r / 2D) + vehicle.getCarHeight() / 8F,
                vehicle.getZ() + offZ + (vehicle.getRandom().nextDouble() * r - r / 2D),
                speedX, 0.0D, speedZ);
    }

    private void spawnParticle(ParticleOptions particleTypes, double offX, double offY, double offZ, double speedX, double speedZ) {
        spawnParticle(particleTypes, offX, offY, offZ, speedX, speedZ, 0.1D);
    }

    public int getTimeToStart() {
        DamageComponent damage = vehicle.getDamageComponent();
        int time = vehicle.getRandom().nextInt(10) + 5;

        if (damage != null) {
            float temp = damage.getTemperature();
            if (temp < 0F) {
                time += 40;
            } else if (temp < 10F) {
                time += 35;
            } else if (temp < 30F) {
                time += 10;
            } else if (temp < 60F) {
                time += 5;
            }
        }

        float batteryPerc = getBatteryPercentage();

        if (batteryPerc < 0.5F) {
            time += 20 + vehicle.getRandom().nextInt(10);
        } else if (batteryPerc < 0.75F) {
            time += 10 + vehicle.getRandom().nextInt(10);
        }

        return time;
    }

    public int getBatteryUsage() {
        if (!ModConfigHandler.CAR_SERVER.useBattery.get()) {
            return 0;
        }

        DamageComponent damage = vehicle.getDamageComponent();
        float temp = damage != null ? damage.getBiomeTemperatureCelsius() : 20F;
        int baseUsage = 2;
        if (temp < 0F) {
            baseUsage += 2;
        } else if (temp < 15F) {
            baseUsage += 1;
        }
        return baseUsage;
    }

    public void setStarting(boolean starting, boolean playFailSound) {
        PhysicsComponent physics = vehicle.getPhysicsComponent();
        if (physics == null) {
            return;
        }

        if (starting) {
            if (getBatteryLevel() <= 0) {
                return;
            }
            if (physics.isStarted()) {
                physics.setStarted(false, true, false);
                carStopped = true;
                return;
            }
        } else {
            if (carStarted || carStopped) {
                carStopped = false;
                carStarted = false;
                return;
            }
            if (playFailSound) {
                if (getBatteryLevel() > 0) {
                    playFailSound();
                }
            }
        }
        vehicle.getEntityData().set(STARTING, starting);
    }

    public float getBatterySoundPitchLevel() {
        int batteryLevel = getBatteryLevel();
        int startLevel = getMaxBatteryLevel() / 3;

        float basePitch = 1F - 0.002F * ((float) getStartingTime());

        if (batteryLevel > startLevel) {
            return basePitch;
        }

        int levelUnder = startLevel - batteryLevel;
        float perc = (float) levelUnder / (float) startLevel;

        return basePitch - (perc / 2.3F);
    }

    public float getBatteryPercentage() {
        return ((float) getBatteryLevel()) / ((float) getMaxBatteryLevel());
    }

    public void setBatteryLevel(int level) {
        if (level < 0) {
            level = 0;
        } else if (level > getMaxBatteryLevel()) {
            level = getMaxBatteryLevel();
        }
        vehicle.getEntityData().set(BATTERY_LEVEL, level);
    }

    public int getBatteryLevel() {
        return vehicle.getEntityData().get(BATTERY_LEVEL);
    }

    public int getMaxBatteryLevel() {
        return 1000;
    }

    public int getStartingTime() {
        return vehicle.getEntityData().get(STARTING_TIME);
    }

    public void setStartingTime(int time) {
        vehicle.getEntityData().set(STARTING_TIME, time);
    }

    public boolean isStarting() {
        return vehicle.getEntityData().get(STARTING);
    }

    @OnlyIn(Dist.CLIENT)
    public void checkStartingLoop() {
        PhysicsComponent physics = vehicle.getPhysicsComponent();
        if (physics == null) {
            return;
        }

        if (!isSoundPlaying(startingLoop)) {
            startingLoop = new SoundLoopStarting(vehicle, physics.getStartingSound(), SoundSource.MASTER);
            ModSounds.playSoundLoop(startingLoop, vehicle.level());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isSoundPlaying(net.minecraft.client.resources.sounds.SoundInstance sound) {
        if (sound == null) {
            return false;
        }
        return Minecraft.getInstance().getSoundManager().isActive(sound);
    }

    public void playFailSound() {
        PhysicsComponent physics = vehicle.getPhysicsComponent();
        if (physics != null) {
            ModSounds.playSound(physics.getFailSound(), vehicle.level(), vehicle.blockPosition(), null, SoundSource.MASTER, 1F, getBatterySoundPitchLevel());
        }
    }

    @Override
    public void saveAdditionalData(CompoundTag compound) {
        compound.putInt("battery", getBatteryLevel());
    }

    @Override
    public void readAdditionalData(CompoundTag compound) {
        setBatteryLevel(compound.getInt("battery"));
    }
}
