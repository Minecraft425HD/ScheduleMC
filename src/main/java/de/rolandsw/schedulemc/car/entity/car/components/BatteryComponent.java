package de.rolandsw.schedulemc.car.entity.car.components;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import de.rolandsw.schedulemc.car.sounds.ModSounds;
import de.rolandsw.schedulemc.car.sounds.SoundLoopStarting;
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
 * Manages battery, starting system, and exhaust particles for the car
 */
public class BatteryComponent extends CarComponent {

    private static final EntityDataAccessor<Integer> BATTERY_LEVEL = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STARTING_TIME = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> STARTING = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.BOOLEAN);

    @OnlyIn(Dist.CLIENT)
    private SoundLoopStarting startingLoop;

    // Server side
    private boolean carStopped;
    private boolean carStarted;

    // Client side
    private int timeSinceStarted;
    private int timeToStart;

    public BatteryComponent(EntityGenericCar car) {
        super(car);
    }

    @Override
    public void defineSynchedData() {
        car.getEntityData().define(BATTERY_LEVEL, getMaxBatteryLevel());
        car.getEntityData().define(STARTING_TIME, 0);
        car.getEntityData().define(STARTING, Boolean.FALSE);
    }

    @Override
    public void clientTick() {
        PhysicsComponent physics = car.getPhysicsComponent();
        if (physics != null && physics.isStarted()) {
            timeSinceStarted++;
            if (car.tickCount % 2 == 0) {
                spawnParticles(physics.getSpeed() > 0.1F);
            }
        } else {
            timeSinceStarted = 0;
        }
    }

    @Override
    public void serverTick() {
        PhysicsComponent physics = car.getPhysicsComponent();
        if (physics == null) {
            return;
        }

        if (isStarting()) {
            if (car.tickCount % 2 == 0) {
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
            float speedPerc = physics.getSpeed() / car.getMaxSpeed();

            int chargingRate = (int) (speedPerc * 7F);
            if (chargingRate < 5) {
                chargingRate = 1;
            }

            if (car.tickCount % 20 == 0) {
                setBatteryLevel(getBatteryLevel() + chargingRate);
            }
        }
    }

    public void spawnParticles(boolean driving) {
        if (!car.level().isClientSide) {
            return;
        }
        Vec3 lookVec = car.getLookAngle().normalize();
        double offX = lookVec.x * -1D;
        double offY = lookVec.y;
        double offZ = lookVec.z * -1D;

        DamageComponent damage = car.getDamageComponent();
        PhysicsComponent physics = car.getPhysicsComponent();

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
        car.level().addParticle(particleTypes,
                car.getX() + offX + (car.getRandom().nextDouble() * r - r / 2D),
                car.getY() + offY + (car.getRandom().nextDouble() * r - r / 2D) + car.getCarHeight() / 8F,
                car.getZ() + offZ + (car.getRandom().nextDouble() * r - r / 2D),
                speedX, 0.0D, speedZ);
    }

    private void spawnParticle(ParticleOptions particleTypes, double offX, double offY, double offZ, double speedX, double speedZ) {
        spawnParticle(particleTypes, offX, offY, offZ, speedX, speedZ, 0.1D);
    }

    public int getTimeToStart() {
        DamageComponent damage = car.getDamageComponent();
        int time = car.getRandom().nextInt(10) + 5;

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
            time += 20 + car.getRandom().nextInt(10);
        } else if (batteryPerc < 0.75F) {
            time += 10 + car.getRandom().nextInt(10);
        }

        return time;
    }

    public int getBatteryUsage() {
        if (!Main.SERVER_CONFIG.useBattery.get()) {
            return 0;
        }

        DamageComponent damage = car.getDamageComponent();
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
        PhysicsComponent physics = car.getPhysicsComponent();
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
        car.getEntityData().set(STARTING, starting);
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
        car.getEntityData().set(BATTERY_LEVEL, level);
    }

    public int getBatteryLevel() {
        return car.getEntityData().get(BATTERY_LEVEL);
    }

    public int getMaxBatteryLevel() {
        return 1000;
    }

    public int getStartingTime() {
        return car.getEntityData().get(STARTING_TIME);
    }

    public void setStartingTime(int time) {
        car.getEntityData().set(STARTING_TIME, time);
    }

    public boolean isStarting() {
        return car.getEntityData().get(STARTING);
    }

    @OnlyIn(Dist.CLIENT)
    public void checkStartingLoop() {
        PhysicsComponent physics = car.getPhysicsComponent();
        if (physics == null) {
            return;
        }

        if (!isSoundPlaying(startingLoop)) {
            startingLoop = new SoundLoopStarting(car, physics.getStartingSound(), SoundSource.MASTER);
            ModSounds.playSoundLoop(startingLoop, car.level());
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
        PhysicsComponent physics = car.getPhysicsComponent();
        if (physics != null) {
            ModSounds.playSound(physics.getFailSound(), car.level(), car.blockPosition(), null, SoundSource.MASTER, 1F, getBatterySoundPitchLevel());
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
