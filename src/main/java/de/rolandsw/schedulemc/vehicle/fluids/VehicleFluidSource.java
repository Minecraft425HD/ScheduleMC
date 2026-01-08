package de.rolandsw.schedulemc.vehicle.fluids;

import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class VehicleFluidSource extends ForgeFlowingFluid.Source implements IEffectApplyable {

    public VehicleFluidSource(Properties properties) {
        super(properties);
    }
}

