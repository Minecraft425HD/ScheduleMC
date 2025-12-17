package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.tobacco.blocks.GrowLightSlabBlock;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity für Grow Light Slabs - Stromverbrauch-Tracking für Utility-System
 *
 * Grow Lights sind immer aktiv wenn platziert (emittieren konstant Licht)
 */
public class GrowLightSlabBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;
    private GrowLightSlabBlock.GrowLightTier tier;

    public GrowLightSlabBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.GROW_LIGHT_SLAB.get(), pos, state);
        // Tier aus Block-State ermitteln
        if (state.getBlock() instanceof GrowLightSlabBlock growLight) {
            this.tier = growLight.getTier();
        } else {
            this.tier = GrowLightSlabBlock.GrowLightTier.BASIC;
        }
    }

    public GrowLightSlabBlock.GrowLightTier getTier() {
        return tier;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        // Utility-Status nur bei Änderung melden
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        // Grow Lights sind immer aktiv wenn platziert
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Tier", tier.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        try {
            tier = GrowLightSlabBlock.GrowLightTier.valueOf(tag.getString("Tier"));
        } catch (IllegalArgumentException e) {
            tier = GrowLightSlabBlock.GrowLightTier.BASIC;
        }
    }
}
