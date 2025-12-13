package de.rolandsw.schedulemc.vehicle.entity.vehicle.components;

import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;

/**
 * Base class for all vehicle components.
 * Components encapsulate specific functionality (fuel, damage, etc.)
 * and can be added/removed from vehicles modularly.
 */
public abstract class VehicleComponent {

    protected final EntityGenericVehicle vehicle;

    public VehicleComponent(EntityGenericVehicle vehicle) {
        this.vehicle = vehicle;
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
     * Called when player interacts with vehicle.
     * Return true if interaction was handled by this component.
     */
    public boolean onInteract(Player player, net.minecraft.world.InteractionHand hand) {
        return false;
    }

}
