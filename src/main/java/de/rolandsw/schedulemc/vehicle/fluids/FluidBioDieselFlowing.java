package de.rolandsw.schedulemc.vehicle.fluids;

import de.rolandsw.schedulemc.vehicle.blocks.ModBlocks;
import de.rolandsw.schedulemc.vehicle.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FluidBioDieselFlowing extends VehicleFluidFlowing {

    protected FluidBioDieselFlowing() {
        super(new Properties(
                () -> ModFluids.BIO_DIESEL_TYPE.get(),
                () -> ModFluids.BIO_DIESEL.get(),
                () -> ModFluids.BIO_DIESEL_FLOWING.get())
                .block(() -> ModBlocks.BIO_DIESEL.get())
        );
    }

    @Override
    public void applyEffects(Entity entity, BlockState state, Level worldIn, BlockPos pos) {
        ModFluids.BIO_DIESEL.get().applyEffects(entity, state, worldIn, pos);
    }
}
