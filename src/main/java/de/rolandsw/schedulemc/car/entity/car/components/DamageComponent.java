package de.rolandsw.schedulemc.car.entity.car.components;

import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import de.rolandsw.schedulemc.car.items.ItemRepairKit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Manages damage and temperature system for the car
 */
public class DamageComponent extends CarComponent {

    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TEMPERATURE = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.FLOAT);

    private long lastDamage;

    public DamageComponent(EntityGenericCar car) {
        super(car);
    }

    @Override
    public void defineSynchedData() {
        car.getEntityData().define(DAMAGE, 0F);
        car.getEntityData().define(TEMPERATURE, 0F);
    }

    @Override
    public void tick() {
        if (car.isInLava()) {
            addDamage(1);
        }

        PhysicsComponent physics = car.getPhysicsComponent();
        if ((physics != null && physics.isStarted()) || getDamage() > 99F) {
            spawnDamageParticles();
        }

        // Temperature logic
        if (!car.level().isClientSide && car.tickCount % 20 == 0) {
            updateTemperature();
        }
    }

    private void updateTemperature() {
        PhysicsComponent physics = car.getPhysicsComponent();
        if (physics == null) {
            return;
        }

        float speedPerc = physics.getSpeed() / car.getMaxSpeed();
        int tempRate = (int) (speedPerc * 10F) + 1;

        if (tempRate > 5) {
            tempRate = 5;
        }

        float rate = tempRate * 0.2F + (car.getRandom().nextFloat() - 0.5F) * 0.1F;
        float temp = getTemperature();
        float tempToReach = getTemperatureToReach();

        if (isInBounds(temp, tempToReach, rate)) {
            setTemperature(tempToReach);
        } else {
            if (tempToReach < temp) {
                rate = -rate;
            }
            setTemperature(temp + rate);
        }
    }

    private boolean isInBounds(float value, float target, float tolerance) {
        return Math.abs(value - target) <= tolerance;
    }

    public float getTemperatureToReach() {
        PhysicsComponent physics = car.getPhysicsComponent();
        float biomeTemp = getBiomeTemperatureCelsius();

        if (physics == null || !physics.isStarted()) {
            return biomeTemp;
        }

        float optimalTemp = car.getOptimalTemperature();

        if (biomeTemp > 45F) {
            optimalTemp = 100F;
        } else if (biomeTemp <= 0F) {
            optimalTemp = 80F;
        }
        return Math.max(biomeTemp, optimalTemp);
    }

    public float getBiomeTemperatureCelsius() {
        return (car.level().getBiome(car.blockPosition()).value().getBaseTemperature() - 0.3F) * 30F;
    }

    public void spawnDamageParticles() {
        if (!car.level().isClientSide) {
            return;
        }

        if (getDamage() < 50) {
            return;
        }

        int amount;
        int damage = (int) getDamage();

        if (damage < 70) {
            if (car.getRandom().nextInt(10) != 0) {
                return;
            }
            amount = 1;
        } else if (damage < 80) {
            if (car.getRandom().nextInt(5) != 0) {
                return;
            }
            amount = 1;
        } else if (damage < 90) {
            amount = 2;
        } else {
            amount = 3;
        }

        for (int i = 0; i < amount; i++) {
            car.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    car.getX() + (car.getRandom().nextDouble() - 0.5D) * car.getCarWidth(),
                    car.getY() + car.getRandom().nextDouble() * car.getCarHeight(),
                    car.getZ() + (car.getRandom().nextDouble() - 0.5D) * car.getCarWidth(),
                    0.0D, 0.0D, 0.0D);
        }
    }

    public void onCollision(float speed) {
        PhysicsComponent physics = car.getPhysicsComponent();
        if (physics == null) {
            return;
        }

        float percSpeed = speed / car.getMaxSpeed();

        if (percSpeed > 0.8F) {
            addDamage(percSpeed * 5);
            physics.playCrashSound();

            if (percSpeed > 0.9F) {
                physics.setStarted(false);
                physics.playStopSound();
            }
        }
    }

    @Override
    public boolean onInteract(Player player, InteractionHand hand) {
        if (car.level().isClientSide || !car.isAlive()) {
            return false;
        }

        if (!(player instanceof Player)) {
            return false;
        }

        if (player.equals(car.getDriver())) {
            return false;
        }

        ItemStack stack = player.getMainHandItem();

        if (stack.getItem() instanceof ItemRepairKit) {
            long time = player.level().getGameTime();
            if (time - lastDamage < 10L) {
                car.destroyCar(player, true);
                stack.hurtAndBreak(50, player, playerEntity -> playerEntity.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            } else {
                lastDamage = time;
            }
            return true;
        }

        return false;
    }

    public void addDamage(float val) {
        setDamage(getDamage() + val);
    }

    public boolean canStartCarEngine() {
        return getDamage() < 100F;
    }

    public boolean canEngineStayOn() {
        if (car.isInWater()) {
            addDamage(25);
            return false;
        }
        if (car.isInLava() || getDamage() >= 100) {
            return false;
        }
        return true;
    }

    public int getTimeToStart() {
        int value = 0;

        if (getDamage() >= 95) {
            value += car.getRandom().nextInt(25) + 50;
        } else if (getDamage() >= 90) {
            value += car.getRandom().nextInt(15) + 30;
        } else if (getDamage() >= 80) {
            value += car.getRandom().nextInt(15) + 10;
        } else if (getDamage() >= 50) {
            value += car.getRandom().nextInt(10) + 5;
        }

        return value;
    }

    public void setDamage(float damage) {
        if (damage > 100F) {
            damage = 100F;
        } else if (damage < 0) {
            damage = 0;
        }
        car.getEntityData().set(DAMAGE, damage);
    }

    public float getDamage() {
        return car.getEntityData().get(DAMAGE);
    }

    public float getTemperature() {
        return car.getEntityData().get(TEMPERATURE);
    }

    public void setTemperature(float temperature) {
        car.getEntityData().set(TEMPERATURE, temperature);
    }

    public void initTemperature() {
        setTemperature(getBiomeTemperatureCelsius());
    }

    @Override
    public void saveAdditionalData(CompoundTag compound) {
        compound.putFloat("damage", getDamage());
        compound.putFloat("temperature", getTemperature());
    }

    @Override
    public void readAdditionalData(CompoundTag compound) {
        setDamage(compound.getFloat("damage"));
        setTemperature(compound.getFloat("temperature"));
    }
}
