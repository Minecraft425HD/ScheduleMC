package de.rolandsw.schedulemc.vehicle.fluids;

import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class VehicleFluidFlowing extends ForgeFlowingFluid.Flowing implements IEffectApplyable {

    public VehicleFluidFlowing(Properties properties) {
        super(properties);
    }
}

