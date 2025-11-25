package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.items.DriedTobaccoLeafItem;
import de.rolandsw.schedulemc.tobacco.items.FermentedTobaccoLeafItem;
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
 * TileEntity für Fermentierungsfass
 * Fermentiert getrocknete Tabakblätter und verbessert Qualität
 */
public class FermentationBarrelBlockEntity extends BlockEntity {
    
    private static final int FERMENTATION_TIME = 12000; // 10 Minuten (12000 ticks)
    
    private ItemStack input = ItemStack.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private int fermentationProgress = 0;
    private TobaccoType tobaccoType = null;
    private TobaccoQuality quality = null;
    
    public FermentationBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.FERMENTATION_BARREL.get(), pos, state);
    }
    
    /**
     * Fügt getrocknete Blätter hinzu
     */
    public boolean addDriedLeaves(ItemStack stack) {
        if (!input.isEmpty() || !(stack.getItem() instanceof DriedTobaccoLeafItem)) {
            return false;
        }
        
        this.input = stack.copy();
        this.tobaccoType = DriedTobaccoLeafItem.getType(stack);
        this.quality = DriedTobaccoLeafItem.getQuality(stack);
        this.fermentationProgress = 0;
        
        setChanged();
        return true;
    }
    
    /**
     * Gibt fermentierte Blätter zurück
     */
    public ItemStack extractFermentedLeaves() {
        if (output.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        ItemStack result = output.copy();
        output = ItemStack.EMPTY;
        input = ItemStack.EMPTY;
        fermentationProgress = 0;
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
    
    public int getFermentationProgress() {
        return fermentationProgress;
    }
    
    public float getFermentationPercentage() {
        return (float) fermentationProgress / FERMENTATION_TIME;
    }
    
    /**
     * Tick-Update für Fermentierungsprozess
     */
    public void tick() {
        if (level == null || level.isClientSide) return;
        
        if (!input.isEmpty() && output.isEmpty()) {
            fermentationProgress++;
            
            if (fermentationProgress >= FERMENTATION_TIME) {
                // Fermentierung abgeschlossen - Qualität kann sich verbessern
                TobaccoQuality finalQuality = calculateFinalQuality();
                output = FermentedTobaccoLeafItem.create(tobaccoType, finalQuality, input.getCount());
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            
            if (fermentationProgress % 20 == 0) {
                setChanged();
            }
        }
    }
    
    /**
     * Berechnet finale Qualität nach Fermentierung
     * 30% Chance auf Qualitätsverbesserung
     */
    private TobaccoQuality calculateFinalQuality() {
        if (quality == TobaccoQuality.LEGENDAER) {
            return quality; // Kann nicht besser werden
        }
        
        // 30% Chance auf Upgrade
        if (level != null && level.random.nextFloat() < 0.3f) {
            return quality.upgrade();
        }
        
        return quality;
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
        
        tag.putInt("FermentationProgress", fermentationProgress);
        
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
        
        fermentationProgress = tag.getInt("FermentationProgress");
        
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
