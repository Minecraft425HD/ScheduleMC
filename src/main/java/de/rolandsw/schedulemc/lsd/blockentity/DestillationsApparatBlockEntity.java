package de.rolandsw.schedulemc.lsd.blockentity;

import de.rolandsw.schedulemc.lsd.items.ErgotKulturItem;
import de.rolandsw.schedulemc.lsd.items.LSDItems;
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
 * Destillations-Apparat - Zweiter Schritt der LSD-Herstellung
 * Destilliert Ergot-Kultur zu reiner Lysergsäure
 */
public class DestillationsApparatBlockEntity extends BlockEntity implements IUtilityConsumer {

    private static final int DISTILLATION_TIME = 800; // 40 Sekunden
    private static final int CAPACITY = 4;

    private boolean lastActiveState = false;
    private int ergotCount = 0;
    private int distillationProgress = 0;
    private int outputCount = 0;
    private boolean isActive = false;

    public DestillationsApparatBlockEntity(BlockPos pos, BlockState state) {
        super(LSDBlockEntities.DESTILLATIONS_APPARAT.get(), pos, state);
    }

    /**
     * Fügt Ergot-Kultur hinzu
     */
    public boolean addErgotKultur(ItemStack stack) {
        if (!(stack.getItem() instanceof ErgotKulturItem)) return false;
        if (ergotCount >= CAPACITY) return false;
        if (outputCount > 0) return false;

        ergotCount++;
        if (distillationProgress == 0) {
            isActive = true;
        }
        setChanged();
        return true;
    }

    /**
     * Extrahiert Lysergsäure
     */
    public ItemStack extractOutput() {
        if (outputCount <= 0) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(LSDItems.LYSERGSAEURE.get(), outputCount);
        outputCount = 0;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (ergotCount > 0 && outputCount == 0) {
            isActive = true;
            distillationProgress++;

            if (distillationProgress >= DISTILLATION_TIME) {
                // Destillation abgeschlossen - 2 Lysergsäure pro 1 Ergot-Kultur
                outputCount = ergotCount * 2;
                ergotCount = 0;
                distillationProgress = 0;
                isActive = false;

                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            } else if (distillationProgress % 40 == 0) {
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
    public int getErgotCount() { return ergotCount; }
    public int getOutputCount() { return outputCount; }
    public float getProgress() { return (float) distillationProgress / DISTILLATION_TIME; }

    @Override
    public boolean isActivelyConsuming() {
        return isActive;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Ergot", ergotCount);
        tag.putInt("Progress", distillationProgress);
        tag.putInt("Output", outputCount);
        tag.putBoolean("Active", isActive);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ergotCount = tag.getInt("Ergot");
        distillationProgress = tag.getInt("Progress");
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
