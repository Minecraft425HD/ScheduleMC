package de.rolandsw.schedulemc.vehicle.blocks.fluid;

import de.rolandsw.schedulemc.vehicle.fluids.IEffectApplyable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

public class VehicleFluidBlock extends LiquidBlock {

    public VehicleFluidBlock(Supplier<? extends FlowingFluid> fluidSupplier) {
        super(fluidSupplier, Block.Properties.of().noCollission().strength(100F));
    }

    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
        FlowingFluid fluid = getFluid();
        if (fluid instanceof IEffectApplyable) {
            ((IEffectApplyable) fluid).applyEffects(entityIn, state, worldIn, pos);
        }
        super.entityInside(state, worldIn, pos, entityIn);
    }
}
