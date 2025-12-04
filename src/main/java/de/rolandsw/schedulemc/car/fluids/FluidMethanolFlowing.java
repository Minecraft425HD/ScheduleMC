package de.rolandsw.schedulemc.car.fluids;

import de.rolandsw.schedulemc.car.blocks.ModBlocks;
import de.rolandsw.schedulemc.car.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FluidMethanolFlowing extends CarFluidFlowing {

    protected FluidMethanolFlowing() {
        super(new Properties(
                () -> ModFluids.METHANOL_TYPE.get(),
                () -> ModFluids.METHANOL.get(),
                () -> ModFluids.METHANOL_FLOWING.get())
                .block(() -> ModBlocks.METHANOL.get())
                .bucket(() -> ModItems.METHANOL_BUCKET.get())
        );
    }

    @Override
    public void applyEffects(Entity entity, BlockState state, Level worldIn, BlockPos pos) {
        ModFluids.METHANOL.get().applyEffects(entity, state, worldIn, pos);
    }
}
