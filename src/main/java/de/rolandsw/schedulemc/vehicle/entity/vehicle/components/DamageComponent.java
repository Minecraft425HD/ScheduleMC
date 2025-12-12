package de.rolandsw.schedulemc.vehicle.entity.vehicle.components;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.items.ItemRepairKit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Manages damage and temperature system for the vehicle
 */
public class DamageComponent extends VehicleComponent {

    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TEMPERATURE = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.FLOAT);

    private long lastDamage;

    public DamageComponent(EntityGenericVehicle vehicle) {
        super(vehicle);
    }

    public static void defineData(SynchedEntityData entityData) {
        entityData.define(DAMAGE, 0F);
        entityData.define(TEMPERATURE, 0F);
    }

    @Override
    public void defineSynchedData() {
        defineData(vehicle.getEntityData());
    }

    @Override
    public void tick() {
        if (vehicle.isInLava()) {
            addDamage(1);
        }

        PhysicsComponent physics = vehicle.getPhysicsComponent();
        if ((physics != null && physics.isStarted()) || getDamage() > 99F) {
            spawnDamageParticles();
        }

        // Temperature logic
        if (!vehicle.level().isClientSide && vehicle.tickCount % 20 == 0) {
            updateTemperature();
        }
    }

    private void updateTemperature() {
        PhysicsComponent physics = vehicle.getPhysicsComponent();
        if (physics == null) {
            return;
        }

        float speedPerc = physics.getSpeed() / vehicle.getMaxSpeed();
        int tempRate = (int) (speedPerc * 10F) + 1;

        if (tempRate > 5) {
            tempRate = 5;
        }

        float rate = tempRate * 0.2F + (vehicle.getRandom().nextFloat() - 0.5F) * 0.1F;
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
        PhysicsComponent physics = vehicle.getPhysicsComponent();
        float biomeTemp = getBiomeTemperatureCelsius();

        if (physics == null || !physics.isStarted()) {
            return biomeTemp;
        }

        float optimalTemp = vehicle.getOptimalTemperature();

        if (biomeTemp > 45F) {
            optimalTemp = 100F;
        } else if (biomeTemp <= 0F) {
            optimalTemp = 80F;
        }
        return Math.max(biomeTemp, optimalTemp);
    }

    public float getBiomeTemperatureCelsius() {
        return (vehicle.level().getBiome(vehicle.blockPosition()).value().getBaseTemperature() - 0.3F) * 30F;
    }

    public void spawnDamageParticles() {
        if (!vehicle.level().isClientSide) {
            return;
        }

        if (getDamage() < 50) {
            return;
        }

        int amount;
        int damage = (int) getDamage();

        if (damage < 70) {
            if (vehicle.getRandom().nextInt(10) != 0) {
                return;
            }
            amount = 1;
        } else if (damage < 80) {
            if (vehicle.getRandom().nextInt(5) != 0) {
                return;
            }
            amount = 1;
        } else if (damage < 90) {
            amount = 2;
        } else {
            amount = 3;
        }

        for (int i = 0; i < amount; i++) {
            vehicle.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    vehicle.getX() + (vehicle.getRandom().nextDouble() - 0.5D) * vehicle.getCarWidth(),
                    vehicle.getY() + vehicle.getRandom().nextDouble() * vehicle.getCarHeight(),
                    vehicle.getZ() + (vehicle.getRandom().nextDouble() - 0.5D) * vehicle.getCarWidth(),
                    0.0D, 0.0D, 0.0D);
        }
    }

    public void onCollision(float speed) {
        PhysicsComponent physics = vehicle.getPhysicsComponent();
        if (physics == null) {
            return;
        }

        float percSpeed = speed / vehicle.getMaxSpeed();

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
        if (vehicle.level().isClientSide || !vehicle.isAlive()) {
            return false;
        }

        if (!(player instanceof Player)) {
            return false;
        }

        if (player.equals(vehicle.getDriver())) {
            return false;
        }

        ItemStack stack = player.getMainHandItem();

        if (stack.getItem() instanceof ItemRepairKit) {
            long time = player.level().getGameTime();
            if (time - lastDamage < 10L) {
                vehicle.destroyCar(player, true);
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
        if (vehicle.isInWater()) {
            addDamage(25);
            return false;
        }
        if (vehicle.isInLava() || getDamage() >= 100) {
            return false;
        }
        return true;
    }

    public int getTimeToStart() {
        int value = 0;

        if (getDamage() >= 95) {
            value += vehicle.getRandom().nextInt(25) + 50;
        } else if (getDamage() >= 90) {
            value += vehicle.getRandom().nextInt(15) + 30;
        } else if (getDamage() >= 80) {
            value += vehicle.getRandom().nextInt(15) + 10;
        } else if (getDamage() >= 50) {
            value += vehicle.getRandom().nextInt(10) + 5;
        }

        return value;
    }

    public void setDamage(float damage) {
        if (damage > 100F) {
            damage = 100F;
        } else if (damage < 0) {
            damage = 0;
        }
        vehicle.getEntityData().set(DAMAGE, damage);
    }

    public float getDamage() {
        return vehicle.getEntityData().get(DAMAGE);
    }

    public float getTemperature() {
        return vehicle.getEntityData().get(TEMPERATURE);
    }

    public void setTemperature(float temperature) {
        vehicle.getEntityData().set(TEMPERATURE, temperature);
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
