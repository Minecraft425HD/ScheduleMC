package de.rolandsw.schedulemc.coffee.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Großes Trocknungstablett
 * Kapazität: 30 Kirschen
 * Trocknungszeit: 400 Ticks (20 Sekunden) pro Kirsche
 */
public class LargeDryingTrayBlockEntity extends AbstractCoffeeDryingTrayBlockEntity {

    public LargeDryingTrayBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.LARGE_DRYING_TRAY.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 30;
    }

    @Override
    protected int getDryingTimePerCherry() {
        return 400; // 20 Sekunden pro Kirsche
    }
}
