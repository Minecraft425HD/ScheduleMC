package de.rolandsw.schedulemc.mdma.blockentity;

import de.rolandsw.schedulemc.mdma.MDMAQuality;
import de.rolandsw.schedulemc.mdma.items.MDMABaseItem;
import de.rolandsw.schedulemc.mdma.items.MDMAItems;
import de.rolandsw.schedulemc.mdma.items.MDMAKristallItem;
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
 * Drying Oven - Second step of MDMA production
 * Dries MDMA base into pure crystals
 * Passive process
 */
public class DryingOvenBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    private static final int DRYING_TIME = 600; // 30 seconds
    private static final int CAPACITY = 8;

    private int inputCount = 0;
    private MDMAQuality inputQuality = MDMAQuality.STANDARD;
    private int dryingProgress = 0;
    private int outputCount = 0;
    private boolean isActive = false;

    public DryingOvenBlockEntity(BlockPos pos, BlockState state) {
        super(MDMABlockEntities.DRYING_OVEN.get(), pos, state);
    }

    public boolean addMDMABase(ItemStack stack) {
        if (!(stack.getItem() instanceof MDMABaseItem)) return false;
        if (inputCount >= CAPACITY || outputCount > 0) return false;

        inputQuality = MDMABaseItem.getQuality(stack);
        inputCount++;
        if (dryingProgress == 0) isActive = true;
        setChanged();
        return true;
    }

    public ItemStack extractOutput() {
        if (outputCount <= 0) return ItemStack.EMPTY;

        ItemStack result = MDMAKristallItem.create(inputQuality, outputCount);
        outputCount = 0;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (inputCount > 0 && outputCount == 0) {
            isActive = true;
            dryingProgress++;

            if (dryingProgress >= DRYING_TIME) {
                outputCount = inputCount;
                inputCount = 0;
                dryingProgress = 0;
                isActive = false;

                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            } else if (dryingProgress % 40 == 0) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        } else {
            isActive = false;
        }

        // Report utility status only on changes
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    // Getters
    public boolean isActive() { return isActive; }
    public boolean hasOutput() { return outputCount > 0; }
    public int getInputCount() { return inputCount; }
    public int getOutputCount() { return outputCount; }
    public float getProgress() { return (float) dryingProgress / DRYING_TIME; }

    @Override
    public boolean isActivelyConsuming() {
        return isActive;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Input", inputCount);
        tag.putString("Quality", inputQuality.name());
        tag.putInt("Progress", dryingProgress);
        tag.putInt("Output", outputCount);
        tag.putBoolean("Active", isActive);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inputCount = tag.getInt("Input");
        if (tag.contains("Quality")) {
            try {
                inputQuality = MDMAQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException e) {
                inputQuality = MDMAQuality.STANDARD;
            }
        }
        dryingProgress = tag.getInt("Progress");
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
