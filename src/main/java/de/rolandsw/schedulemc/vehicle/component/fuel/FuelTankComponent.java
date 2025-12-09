package de.rolandsw.schedulemc.vehicle.component.fuel;

import de.rolandsw.schedulemc.vehicle.core.component.BaseComponent;
import de.rolandsw.schedulemc.vehicle.core.component.ComponentType;
import de.rolandsw.schedulemc.vehicle.core.component.IVehicleComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Component representing a vehicle's fuel tank.
 * Stores fuel type, capacity, and current amount.
 */
public class FuelTankComponent extends BaseComponent {

    private FuelTankSpecification specification;
    private Fluid currentFluid = Fluids.EMPTY;
    private float currentAmount = 0.0f; // in millibuckets

    public FuelTankComponent(FuelTankSpecification specification) {
        super(ComponentType.FUEL_TANK);
        this.specification = specification;
    }

    public FuelTankComponent() {
        this(FuelTankSpecification.MEDIUM);
    }

    // Getters and Setters
    public FuelTankSpecification getSpecification() {
        return specification;
    }

    public void setSpecification(FuelTankSpecification specification) {
        this.specification = specification;
    }

    public Fluid getCurrentFluid() {
        return currentFluid;
    }

    public float getCurrentAmount() {
        return currentAmount;
    }

    /**
     * Fills the tank with the specified fluid.
     * @return Amount actually filled
     */
    public float fill(Fluid fluid, float amount) {
        if (fluid == Fluids.EMPTY || amount <= 0) {
            return 0;
        }

        // If tank is empty or contains same fluid
        if (currentFluid == Fluids.EMPTY || currentFluid == fluid) {
            float spaceAvailable = specification.getCapacity() - currentAmount;
            float amountToFill = Math.min(amount, spaceAvailable);

            if (amountToFill > 0) {
                currentFluid = fluid;
                currentAmount += amountToFill;
            }

            return amountToFill;
        }

        return 0; // Different fluid type
    }

    /**
     * Drains fuel from the tank.
     * @return Amount actually drained
     */
    public float drain(float amount) {
        if (amount <= 0 || currentAmount <= 0) {
            return 0;
        }

        float amountToDrain = Math.min(amount, currentAmount);
        currentAmount -= amountToDrain;

        if (currentAmount <= 0.001f) {
            currentAmount = 0;
            currentFluid = Fluids.EMPTY;
        }

        return amountToDrain;
    }

    /**
     * Gets the fuel level as a percentage.
     */
    public float getFuelPercentage() {
        return specification.getCapacity() > 0 ? currentAmount / specification.getCapacity() : 0;
    }

    /**
     * Checks if tank has enough fuel.
     */
    public boolean hasEnoughFuel(float required) {
        return currentAmount >= required && currentFluid != Fluids.EMPTY;
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        writeTypeToNbt(tag);
        tag.putString("TankType", specification.getIdentifier());

        if (currentFluid != Fluids.EMPTY && currentFluid != null) {
            ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(currentFluid);
            if (fluidId != null) {
                tag.putString("Fluid", fluidId.toString());
            }
        }

        tag.putFloat("Amount", currentAmount);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        String tankType = tag.getString("TankType");
        this.specification = FuelTankSpecification.getByIdentifier(tankType);

        if (tag.contains("Fluid")) {
            ResourceLocation fluidId = new ResourceLocation(tag.getString("Fluid"));
            this.currentFluid = ForgeRegistries.FLUIDS.getValue(fluidId);
            if (this.currentFluid == null) {
                this.currentFluid = Fluids.EMPTY;
            }
        } else {
            this.currentFluid = Fluids.EMPTY;
        }

        this.currentAmount = tag.getFloat("Amount");
    }

    @Override
    public IVehicleComponent duplicate() {
        FuelTankComponent copy = new FuelTankComponent(specification);
        copy.currentFluid = this.currentFluid;
        copy.currentAmount = this.currentAmount;
        return copy;
    }

    @Override
    public boolean isValid() {
        return specification != null && currentAmount >= 0 && currentAmount <= specification.getCapacity();
    }
}
