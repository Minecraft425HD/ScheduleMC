package de.rolandsw.schedulemc.vehicle.entity.vehicle.components;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.vehicle.VehicleConstants;
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

        // Optimierung: Cache Component-Getter
        PhysicsComponent physics = vehicle.getPhysicsComponent();
        if ((physics != null && physics.isStarted()) || getDamage() > (VehicleConstants.MAX_DAMAGE - 1F)) {
            spawnDamageParticles();
        }

        // Temperature logic (konfigurierbar via Config)
        int tempUpdateInterval = ModConfigHandler.VEHICLE_SERVER.temperatureUpdateInterval.get();
        if (!vehicle.level().isClientSide && vehicle.tickCount % tempUpdateInterval == 0) {
            updateTemperature();
        }
    }

    private void updateTemperature() {
        PhysicsComponent physics = vehicle.getPhysicsComponent();
        if (physics == null) {
            return;
        }

        float maxSpeed = vehicle.getMaxSpeed();
        if (maxSpeed <= 0F) return; // Kein Motor/Body → kein Speed-basierter Temperaturanstieg
        float speedPerc = physics.getSpeed() / maxSpeed;
        int tempRate = (int) (speedPerc * 10F) + 1;

        if (tempRate > VehicleConstants.TEMP_RATE_MAX) {
            tempRate = VehicleConstants.TEMP_RATE_MAX;
        }

        float rate = tempRate * VehicleConstants.TEMP_RATE_BASE + (vehicle.getRandom().nextFloat() - 0.5F) * VehicleConstants.TEMP_RATE_RANDOMNESS;
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

        if (biomeTemp > VehicleConstants.BIOME_TEMP_HOT_THRESHOLD) {
            optimalTemp = VehicleConstants.TEMP_HOT_ENGINE_TARGET;
        } else if (biomeTemp <= VehicleConstants.BIOME_TEMP_COLD_THRESHOLD) {
            optimalTemp = VehicleConstants.TEMP_COLD_ENGINE_TARGET;
        }
        return Math.max(biomeTemp, optimalTemp);
    }

    public float getBiomeTemperatureCelsius() {
        return (vehicle.level().getBiome(vehicle.blockPosition()).value().getBaseTemperature() - VehicleConstants.BIOME_TEMP_OFFSET) * VehicleConstants.BIOME_TEMP_MULTIPLIER;
    }

    public void spawnDamageParticles() {
        if (!vehicle.level().isClientSide) {
            return;
        }

        if (getDamage() < VehicleConstants.DAMAGE_THRESHOLD_LOW) {
            return;
        }

        int amount;
        int damage = (int) getDamage();

        if (damage < VehicleConstants.DAMAGE_THRESHOLD_MEDIUM) {
            if (vehicle.getRandom().nextInt(VehicleConstants.PARTICLE_CHANCE_LOW) != 0) {
                return;
            }
            amount = VehicleConstants.PARTICLES_LOW;
        } else if (damage < VehicleConstants.DAMAGE_THRESHOLD_HIGH) {
            if (vehicle.getRandom().nextInt(VehicleConstants.PARTICLE_CHANCE_MEDIUM) != 0) {
                return;
            }
            amount = VehicleConstants.PARTICLES_LOW;
        } else if (damage < VehicleConstants.DAMAGE_THRESHOLD_CRITICAL) {
            amount = VehicleConstants.PARTICLES_MEDIUM;
        } else {
            amount = VehicleConstants.PARTICLES_HIGH;
        }

        for (int i = 0; i < amount; i++) {
            vehicle.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    vehicle.getX() + (vehicle.getRandom().nextDouble() - 0.5D) * vehicle.getVehicleWidth(),
                    vehicle.getY() + vehicle.getRandom().nextDouble() * vehicle.getVehicleHeight(),
                    vehicle.getZ() + (vehicle.getRandom().nextDouble() - 0.5D) * vehicle.getVehicleWidth(),
                    0.0D, 0.0D, 0.0D);
        }
    }

    public void onCollision(float speed) {
        // Optimierung: Cache Component-Getter
        PhysicsComponent physics = vehicle.getPhysicsComponent();
        if (physics == null) {
            return;
        }

        float maxSpeed = vehicle.getMaxSpeed();
        if (maxSpeed <= 0F) return; // Kein Motor → kein Kollisions-Schwellwert prüfbar
        float percSpeed = speed / maxSpeed;

        if (percSpeed > VehicleConstants.COLLISION_DAMAGE_THRESHOLD) {
            addDamage(percSpeed * VehicleConstants.COLLISION_DAMAGE_MULTIPLIER);
            physics.playCrashSound();

            if (percSpeed > VehicleConstants.COLLISION_ENGINE_STOP_THRESHOLD) {
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

        if (player.equals(vehicle.getDriver())) {
            return false;
        }

        ItemStack stack = player.getMainHandItem();

        if (stack.getItem() instanceof ItemRepairKit) {
            long time = player.level().getGameTime();
            if (time - lastDamage < VehicleConstants.REPAIR_DOUBLE_CLICK_WINDOW) {
                vehicle.destroyVehicle(player, true);
                stack.hurtAndBreak(VehicleConstants.REPAIR_KIT_DESTROY_COST, player, playerEntity -> playerEntity.broadcastBreakEvent(InteractionHand.MAIN_HAND));
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

    public boolean canStartVehicleEngine() {
        return getDamage() < VehicleConstants.MAX_DAMAGE;
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
        if (damage > VehicleConstants.MAX_DAMAGE) {
            damage = VehicleConstants.MAX_DAMAGE;
        } else if (damage < 0) {
            damage = 0;
        }
        // Vehicle Aging: Schaden kann nicht unter den Aging-Mindestwert repariert werden
        float minDamage = getAgingMinDamage();
        if (damage < minDamage) {
            damage = minDamage;
        }
        vehicle.getEntityData().set(DAMAGE, damage);
    }

    /**
     * Berechnet den minimalen Schaden basierend auf Kilometerstand (Aging).
     * Bei 100% max health = 0 min damage
     * Bei 75% max health = 25 min damage
     * Bei 50% max health = 50 min damage
     * Bei 25% max health = 75 min damage
     */
    public float getAgingMinDamage() {
        PhysicsComponent physics = vehicle.getPhysicsComponent();
        if (physics == null) {
            return 0F;
        }
        float maxHealthPercent = physics.getMaxHealthPercent();
        return VehicleConstants.MAX_DAMAGE * (1.0F - maxHealthPercent);
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
