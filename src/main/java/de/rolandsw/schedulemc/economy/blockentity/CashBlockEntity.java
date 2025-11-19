package de.rolandsw.schedulemc.economy.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TileEntity für Bargeld-Block
 */
public class CashBlockEntity extends BlockEntity {
    
    private double value = 0.0;
    private static final double MAX_VALUE = 1000.0;
    
    public CashBlockEntity(BlockPos pos, BlockState state) {
        super(de.rolandsw.schedulemc.economy.blocks.EconomyBlocks.CASH_BLOCK_ENTITY.get(), pos, state);
    }
    
    public double getValue() {
        return value;
    }
    
    public void setValue(double value) {
        this.value = Math.max(0, Math.min(MAX_VALUE, value));
        setChanged();
    }
    
    public void addValue(double amount) {
        setValue(value + amount);
    }
    
    public boolean canAddValue(double amount) {
        return value + amount <= MAX_VALUE;
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("CashValue", value);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("CashValue")) {
            value = tag.getDouble("CashValue");
        }
    }
}
