package de.rolandsw.schedulemc.lsd.blockentity;

import de.rolandsw.schedulemc.lsd.items.ErgotKulturItem;
import de.rolandsw.schedulemc.lsd.items.LSDItems;
import de.rolandsw.schedulemc.lsd.items.MutterkornItem;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
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
 * Fermentations-Tank - Erster Schritt der LSD-Herstellung
 * Fermentiert Mutterkorn zu Ergot-Kultur
 */
public class FermentationsTankBlockEntity extends BlockEntity implements IUtilityConsumer {

    private static final int FERMENTATION_TIME = 1200; // 60 Sekunden
    private static final int CAPACITY = 8;

    private boolean lastActiveState = false;
    private int mutterkornCount = 0;
    private int fermentationProgress = 0;
    private int outputCount = 0;
    private boolean isActive = false;

    public FermentationsTankBlockEntity(BlockPos pos, BlockState state) {
        super(LSDBlockEntities.FERMENTATIONS_TANK.get(), pos, state);
    }

    /**
     * Fügt Mutterkorn hinzu
     */
    public boolean addMutterkorn(ItemStack stack) {
        if (!(stack.getItem() instanceof MutterkornItem)) return false;
        if (mutterkornCount >= CAPACITY) return false;
        if (outputCount > 0) return false; // Erst Output entnehmen

        mutterkornCount++;
        if (fermentationProgress == 0) {
            isActive = true;
        }
        setChanged();
        return true;
    }

    /**
     * Extrahiert alle Ergot-Kulturen
     */
    public ItemStack extractOutput() {
        if (outputCount <= 0) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(LSDItems.ERGOT_KULTUR.get(), outputCount);
        outputCount = 0;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (mutterkornCount > 0 && outputCount == 0) {
            isActive = true;
            fermentationProgress++;

            if (fermentationProgress >= FERMENTATION_TIME) {
                // Fermentation abgeschlossen
                outputCount = mutterkornCount;
                mutterkornCount = 0;
                fermentationProgress = 0;
                isActive = false;

                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            } else if (fermentationProgress % 40 == 0) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        } else {
            isActive = false;
        }

        // Utility-Status nur bei Änderung melden
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    // Getter
    public boolean isActive() { return isActive; }
    public boolean hasOutput() { return outputCount > 0; }
    public int getMutterkornCount() { return mutterkornCount; }
    public int getOutputCount() { return outputCount; }
    public float getProgress() { return (float) fermentationProgress / FERMENTATION_TIME; }

    @Override
    public boolean isActivelyConsuming() {
        return isActive;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Mutterkorn", mutterkornCount);
        tag.putInt("Progress", fermentationProgress);
        tag.putInt("Output", outputCount);
        tag.putBoolean("Active", isActive);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mutterkornCount = tag.getInt("Mutterkorn");
        fermentationProgress = tag.getInt("Progress");
        outputCount = tag.getInt("Output");
        isActive = tag.getBoolean("Active");
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
