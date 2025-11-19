package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.items.DriedTobaccoLeafItem;
import de.rolandsw.schedulemc.tobacco.items.FreshTobaccoLeafItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * TileEntity für Trocknungsgestell
 * Trocknet frische Tabakblätter über Zeit
 */
public class DryingRackBlockEntity extends BlockEntity {
    
    private static final int DRYING_TIME = 6000; // 5 Minuten (6000 ticks)
    
    private ItemStack input = ItemStack.EMPTY; // Frische Blätter
    private ItemStack output = ItemStack.EMPTY; // Getrocknete Blätter
    private int dryingProgress = 0;
    private TobaccoType tobaccoType = null;
    private TobaccoQuality quality = null;
    
    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.DRYING_RACK.get(), pos, state);
    }
    
    /**
     * Fügt frische Blätter hinzu
     */
    public boolean addFreshLeaves(ItemStack stack) {
        if (!input.isEmpty() || !(stack.getItem() instanceof FreshTobaccoLeafItem)) {
            return false;
        }
        
        this.input = stack.copy();
        this.tobaccoType = FreshTobaccoLeafItem.getType(stack);
        this.quality = FreshTobaccoLeafItem.getQuality(stack);
        this.dryingProgress = 0;
        
        setChanged();
        return true;
    }
    
    /**
     * Gibt getrocknete Blätter zurück
     */
    public ItemStack extractDriedLeaves() {
        if (output.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        ItemStack result = output.copy();
        output = ItemStack.EMPTY;
        input = ItemStack.EMPTY;
        dryingProgress = 0;
        tobaccoType = null;
        quality = null;
        
        setChanged();
        return result;
    }
    
    public boolean hasInput() {
        return !input.isEmpty();
    }
    
    public boolean hasOutput() {
        return !output.isEmpty();
    }
    
    public int getDryingProgress() {
        return dryingProgress;
    }
    
    public float getDryingPercentage() {
        return (float) dryingProgress / DRYING_TIME;
    }
    
    /**
     * Tick-Update für Trocknungsprozess
     */
    public void tick() {
        if (level == null || level.isClientSide) return;
        
        if (!input.isEmpty() && output.isEmpty()) {
            dryingProgress++;
            
            if (dryingProgress >= DRYING_TIME) {
                // Trocknung abgeschlossen
                output = DriedTobaccoLeafItem.create(tobaccoType, quality, input.getCount());
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            
            if (dryingProgress % 20 == 0) { // Alle Sekunde update
                setChanged();
            }
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        
        if (!input.isEmpty()) {
            CompoundTag inputTag = new CompoundTag();
            input.save(inputTag);
            tag.put("Input", inputTag);
        }
        
        if (!output.isEmpty()) {
            CompoundTag outputTag = new CompoundTag();
            output.save(outputTag);
            tag.put("Output", outputTag);
        }
        
        tag.putInt("DryingProgress", dryingProgress);
        
        if (tobaccoType != null) {
            tag.putString("TobaccoType", tobaccoType.name());
        }
        if (quality != null) {
            tag.putString("Quality", quality.name());
        }
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        
        if (tag.contains("Input")) {
            input = ItemStack.of(tag.getCompound("Input"));
        }
        if (tag.contains("Output")) {
            output = ItemStack.of(tag.getCompound("Output"));
        }
        
        dryingProgress = tag.getInt("DryingProgress");
        
        if (tag.contains("TobaccoType")) {
            tobaccoType = TobaccoType.valueOf(tag.getString("TobaccoType"));
        }
        if (tag.contains("Quality")) {
            quality = TobaccoQuality.valueOf(tag.getString("Quality"));
        }
    }
    
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }
}
