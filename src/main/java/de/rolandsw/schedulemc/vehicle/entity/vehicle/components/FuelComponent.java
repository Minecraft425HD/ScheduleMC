package de.rolandsw.schedulemc.vehicle.entity.vehicle.components;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.config.Fuel;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.fluids.ModFluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages fuel storage and consumption for the vehicle
 */
public class FuelComponent extends VehicleComponent implements IFluidHandler {

    private static final EntityDataAccessor<Integer> FUEL_AMOUNT = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> FUEL_TYPE = SynchedEntityData.defineId(EntityGenericVehicle.class, EntityDataSerializers.STRING);

    public FuelComponent(EntityGenericVehicle vehicle) {
        super(vehicle);
    }

    public static void defineData(SynchedEntityData entityData) {
        entityData.define(FUEL_AMOUNT, 0);
        entityData.define(FUEL_TYPE, "");
    }

    @Override
    public void defineSynchedData() {
        defineData(vehicle.getEntityData());
    }

    @Override
    public void tick() {
        if (vehicle.level().isClientSide) {
            return;
        }

        fuelTick();
    }

    protected void fuelTick() {
        // No fuel consumption when vehicle is on towing yard
        if (vehicle.isOnTowingYard()) {
            return;
        }

        int fuel = getFuelAmount();
        int tickFuel = getEfficiency(getFluid());
        if (tickFuel <= 0) {
            return;
        }

        PhysicsComponent physics = vehicle.getPhysicsComponent();
        if (fuel > 0 && physics != null && physics.isAccelerating()) {
            // Fuel consumption while accelerating (based on efficiency)
            if (vehicle.tickCount % tickFuel == 0) {
                acceleratingFuelTick();
            }
        } else if (fuel > 0 && physics != null && physics.isStarted()) {
            // Fuel consumption while idling (configurable interval)
            int idleInterval = ModConfigHandler.VEHICLE_SERVER.idleFuelConsumptionInterval.get();
            if (vehicle.tickCount % idleInterval == 0) {
                idleFuelTick();
            }
        }
    }

    protected void idleFuelTick() {
        removeFuel(1);
    }

    protected void acceleratingFuelTick() {
        removeFuel(1);
    }

    public void removeFuel(int amount) {
        int fuel = getFuelAmount();
        int newFuel = fuel - amount;
        setFuelAmount(Math.max(newFuel, 0));
    }

    public boolean hasFuel() {
        return getFuelAmount() > 0;
    }

    public void setFuelAmount(int fuel) {
        vehicle.getEntityData().set(FUEL_AMOUNT, fuel);
    }

    public void setFuelType(String fluid) {
        if (fluid == null) {
            fluid = "";
        }
        vehicle.getEntityData().set(FUEL_TYPE, fluid);
    }

    public void setFuelType(Fluid fluid) {
        setFuelType(ForgeRegistries.FLUIDS.getKey(fluid).toString());
    }

    public String getFuelType() {
        return vehicle.getEntityData().get(FUEL_TYPE);
    }

    @Nullable
    public Fluid getFluid() {
        String fuelType = getFuelType();
        if (fuelType == null || fuelType.isEmpty()) {
            return null;
        }
        return ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(fuelType));
    }

    public int getFuelAmount() {
        return vehicle.getEntityData().get(FUEL_AMOUNT);
    }

    public boolean isValidFuel(Fluid fluid) {
        if (fluid == null) {
            return false;
        }
        return getEfficiency(fluid) > 0;
    }

    public int getEfficiency(@Nullable Fluid fluid) {
        int fluidEfficiency = 0;

        if (fluid == null) {
            fluidEfficiency = 100;
        } else {
            Fuel fuel = Main.FUEL_CONFIG.getFuels().getOrDefault(fluid, null);
            if (fuel != null) {
                fluidEfficiency = fuel.getEfficiency();
            }
        }

        float engineEfficiency = vehicle.getVehicleFuelEfficiency();
        int result = (int) Math.ceil(engineEfficiency * (float) fluidEfficiency);

        return result;
    }

    @Override
    public void saveAdditionalData(CompoundTag compound) {
        compound.putInt("fuel", getFuelAmount());
        compound.putString("fuel_type", getFuelType());
    }

    @Override
    public void readAdditionalData(CompoundTag compound) {
        setFuelAmount(compound.getInt("fuel"));
        if (compound.contains("fuel_type")) {
            setFuelType(compound.getString("fuel_type"));
        }
    }

    // IFluidHandler implementation
    @Override
    public int getTanks() {
        return 1;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        Fluid f = getFluid();
        if (f == null) {
            return new FluidStack(ModFluids.BIO_DIESEL.get(), getFuelAmount());
        } else {
            return new FluidStack(f, getFuelAmount());
        }
    }

    @Override
    public int getTankCapacity(int tank) {
        return vehicle.getMaxFuel();
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return isValidFuel(stack.getFluid());
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource == null || !isValidFuel(resource.getFluid())) {
            return 0;
        }

        if (getFluid() != null && getFuelAmount() > 0 && !resource.getFluid().equals(getFluid())) {
            return 0;
        }

        int amount = Math.min(resource.getAmount(), vehicle.getMaxFuel() - getFuelAmount());

        if (action.execute()) {
            int i = getFuelAmount() + amount;
            if (i > vehicle.getMaxFuel()) {
                i = vehicle.getMaxFuel();
            }
            setFuelAmount(i);
            setFuelType(resource.getFluid());
        }

        return amount;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource == null) {
            return FluidStack.EMPTY;
        }

        if (resource.getFluid() == null || !resource.getFluid().equals(getFluid())) {
            return FluidStack.EMPTY;
        }

        return drain(resource.getAmount(), action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        Fluid fluid = getFluid();
        int totalAmount = getFuelAmount();

        if (fluid == null) {
            return FluidStack.EMPTY;
        }

        int amount = Math.min(maxDrain, totalAmount);

        if (action.execute()) {
            int newAmount = totalAmount - amount;

            if (newAmount <= 0) {
                setFuelType((String) null);
                setFuelAmount(0);
            } else {
                setFuelAmount(newAmount);
            }
        }

        return new FluidStack(fluid, amount);
    }
}
