package de.rolandsw.schedulemc.coffee.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Großer Kaffee-Röster
 * Kapazität: 64 Bohnen
 * Röstzeit: 200 Ticks (10 Sekunden) pro Bohne
 */
public class LargeCoffeeRoasterBlockEntity extends AbstractCoffeeRoasterBlockEntity {

    public LargeCoffeeRoasterBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.LARGE_COFFEE_ROASTER.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 64;
    }

    @Override
    protected int getRoastingTimePerBean() {
        return 200; // 10 Sekunden pro Bohne
    }
}
