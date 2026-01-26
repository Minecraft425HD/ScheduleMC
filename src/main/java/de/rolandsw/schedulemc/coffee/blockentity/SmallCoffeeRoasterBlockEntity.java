package de.rolandsw.schedulemc.coffee.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kleiner Kaffee-Röster
 * Kapazität: 16 Bohnen
 * Röstzeit: 300 Ticks (15 Sekunden) pro Bohne
 */
public class SmallCoffeeRoasterBlockEntity extends AbstractCoffeeRoasterBlockEntity {

    public SmallCoffeeRoasterBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.SMALL_COFFEE_ROASTER.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 16;
    }

    @Override
    protected int getRoastingTimePerBean() {
        return 300; // 15 Sekunden pro Bohne
    }
}
