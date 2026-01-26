package de.rolandsw.schedulemc.coffee.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mittlerer Kaffee-Röster
 * Kapazität: 32 Bohnen
 * Röstzeit: 250 Ticks (12.5 Sekunden) pro Bohne
 */
public class MediumCoffeeRoasterBlockEntity extends AbstractCoffeeRoasterBlockEntity {

    public MediumCoffeeRoasterBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.MEDIUM_COFFEE_ROASTER.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 32;
    }

    @Override
    protected int getRoastingTimePerBean() {
        return 250; // 12.5 Sekunden pro Bohne
    }
}
