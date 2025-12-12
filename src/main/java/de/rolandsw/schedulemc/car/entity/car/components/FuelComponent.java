package de.rolandsw.schedulemc.car.entity.car.components;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.config.Fuel;
import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import de.rolandsw.schedulemc.car.fluids.ModFluids;
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
 * Manages fuel storage and consumption for the car
 */
public class FuelComponent extends CarComponent implements IFluidHandler {

    private static final EntityDataAccessor<Integer> FUEL_AMOUNT = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> FUEL_TYPE = SynchedEntityData.defineId(EntityGenericCar.class, EntityDataSerializers.STRING);

    public FuelComponent(EntityGenericCar car) {
        super(car);
    }

    public static void defineData(SynchedEntityData entityData) {
        entityData.define(FUEL_AMOUNT, 0);
        entityData.define(FUEL_TYPE, "");
    }

    @Override
    public void defineSynchedData() {
        defineData(car.getEntityData());
    }

    @Override
    public void tick() {
        if (car.level().isClientSide) {
            return;
        }

        fuelTick();
    }

    protected void fuelTick() {
        int fuel = getFuelAmount();
        int tickFuel = getEfficiency(getFluid());
        if (tickFuel <= 0) {
            System.out.println("[FuelComponent] tickFuel <= 0, skipping fuel consumption. tickFuel=" + tickFuel);
            return;
        }

        PhysicsComponent physics = car.getPhysicsComponent();
        boolean isAccelerating = physics != null && physics.isAccelerating();
        boolean isStarted = physics != null && physics.isStarted();

        // Log every 100 ticks (5 seconds) to avoid spam
        if (car.tickCount % 100 == 0) {
            System.out.println("[FuelComponent] Status check - fuel=" + fuel + ", tickFuel=" + tickFuel + ", isStarted=" + isStarted + ", isAccelerating=" + isAccelerating);
        }

        if (fuel > 0 && physics != null && isAccelerating) {
            if (car.tickCount % tickFuel == 0) {
                System.out.println("[FuelComponent] Accelerating - consuming fuel. Before: " + fuel + ", tickFuel: " + tickFuel);
                acceleratingFuelTick();
                System.out.println("[FuelComponent] After: " + getFuelAmount());
            }
        } else if (fuel > 0 && physics != null && isStarted) {
            if (car.tickCount % (tickFuel * 100) == 0) {
                System.out.println("[FuelComponent] Idling - consuming fuel. Before: " + fuel);
                idleFuelTick();
                System.out.println("[FuelComponent] After: " + getFuelAmount());
            }
        }
    }

    protected void idleFuelTick() {
        removeFuel(1);
    }

    protected void acceleratingFuelTick() {
        removeFuel(1);
    }

    private void removeFuel(int amount) {
        int fuel = getFuelAmount();
        int newFuel = fuel - amount;
        setFuelAmount(Math.max(newFuel, 0));
    }

    public boolean hasFuel() {
        return getFuelAmount() > 0;
    }

    public void setFuelAmount(int fuel) {
        car.getEntityData().set(FUEL_AMOUNT, fuel);
    }

    public void setFuelType(String fluid) {
        if (fluid == null) {
            fluid = "";
        }
        car.getEntityData().set(FUEL_TYPE, fluid);
    }

    public void setFuelType(Fluid fluid) {
        setFuelType(ForgeRegistries.FLUIDS.getKey(fluid).toString());
    }

    public String getFuelType() {
        return car.getEntityData().get(FUEL_TYPE);
    }

    @Nullable
    public Fluid getFluid() {
        String fuelType = getFuelType();
        if (fuelType == null || fuelType.isEmpty()) {
            return null;
        }
        return ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fuelType));
    }

    public int getFuelAmount() {
        return car.getEntityData().get(FUEL_AMOUNT);
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

        float engineEfficiency = car.getCarFuelEfficiency();
        int result = (int) Math.ceil(engineEfficiency * (float) fluidEfficiency);

        // Debug logging
        System.out.println("[FuelComponent.getEfficiency] fluid=" + fluid + ", fluidEff=" + fluidEfficiency + ", engineEff=" + engineEfficiency + ", result=" + result);

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
        return car.getMaxFuel();
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

        int amount = Math.min(resource.getAmount(), car.getMaxFuel() - getFuelAmount());

        if (action.execute()) {
            int i = getFuelAmount() + amount;
            if (i > car.getMaxFuel()) {
                i = car.getMaxFuel();
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
