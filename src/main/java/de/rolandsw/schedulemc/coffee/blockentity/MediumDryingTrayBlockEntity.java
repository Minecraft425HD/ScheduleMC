package de.rolandsw.schedulemc.coffee.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mittleres Trocknungstablett
 * Kapazität: 20 Kirschen
 * Trocknungszeit: 500 Ticks (25 Sekunden) pro Kirsche
 */
public class MediumDryingTrayBlockEntity extends AbstractCoffeeDryingTrayBlockEntity {

    public MediumDryingTrayBlockEntity(BlockPos pos, BlockState state) {
        super(CoffeeBlockEntities.MEDIUM_DRYING_TRAY.get(), pos, state);
    }

    @Override
    protected int getCapacity() {
        return 20;
    }

    @Override
    protected int getDryingTimePerCherry() {
        return 500; // 25 Sekunden pro Kirsche
    }
}
