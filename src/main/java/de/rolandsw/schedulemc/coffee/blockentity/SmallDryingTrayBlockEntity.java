package de.rolandsw.schedulemc.coffee.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kleines Trocknungstablett
 * Kapazität: 10 Kirschen
 * Trocknungszeit: 600 Ticks (30 Sekunden) pro Kirsche
 */
public class SmallDryingTrayBlockEntity extends AbstractCoffeeDryingTrayBlockEntity {

    public SmallDryingTrayBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.SMALL_DRYING_TRAY.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 10;
    }

    @Override
    protected int getDryingTimePerCherry() {
        return 600; // 30 Sekunden pro Kirsche
    }
}
