package de.rolandsw.schedulemc.car.entity.car.components;

import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;

/**
 * Base class for all car components.
 * Components encapsulate specific functionality (fuel, damage, etc.)
 * and can be added/removed from cars modularly.
 */
public abstract class CarComponent {

    protected final EntityGenericCar car;

    public CarComponent(EntityGenericCar car) {
        this.car = car;
    }

    /**
     * Called every tick. Override to add tick logic.
     */
    public void tick() {
    }

    /**
     * Called on client side every tick. Override for client-only logic.
     */
    public void clientTick() {
    }

    /**
     * Called on server side every tick. Override for server-only logic.
     */
    public void serverTick() {
    }

    /**
     * Define synched entity data. Called during entity initialization.
     */
    public void defineSynchedData() {
    }

    /**
     * Save component data to NBT
     */
    public void saveAdditionalData(CompoundTag compound) {
    }

    /**
     * Load component data from NBT
     */
    public void readAdditionalData(CompoundTag compound) {
    }

    /**
     * Called when entity is removed from world
     */
    public void onRemove() {
    }

    /**
     * Called when player interacts with car.
     * Return true if interaction was handled by this component.
     */
    public boolean onInteract(Player player, net.minecraft.world.InteractionHand hand) {
        return false;
    }

}
