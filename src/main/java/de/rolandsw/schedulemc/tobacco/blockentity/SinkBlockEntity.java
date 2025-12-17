package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity für Waschbecken - Wasserverbrauch-Tracking für Utility-System
 *
 * Waschbecken verbraucht Wasser wenn Gießkannen gefüllt werden
 */
public class SinkBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;
    private int waterUsageCooldown = 0; // Ticks seit letzter Wassernutzung

    public SinkBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.SINK.get(), pos, state);
    }

    /**
     * Wird aufgerufen wenn Wasser genutzt wird (Gießkanne gefüllt)
     */
    public void onWaterUsed() {
        waterUsageCooldown = 100; // 5 Sekunden "aktiv" nach Wassernutzung
        setChanged();
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        // Cooldown runterzählen
        if (waterUsageCooldown > 0) {
            waterUsageCooldown--;
        }

        // Utility-Status nur bei Änderung melden
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        // Aktiv wenn kürzlich Wasser verwendet wurde
        return waterUsageCooldown > 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("WaterCooldown", waterUsageCooldown);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        waterUsageCooldown = tag.getInt("WaterCooldown");
    }
}
