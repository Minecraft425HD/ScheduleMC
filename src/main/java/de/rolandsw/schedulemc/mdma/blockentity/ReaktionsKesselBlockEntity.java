package de.rolandsw.schedulemc.mdma.blockentity;

import de.rolandsw.schedulemc.mdma.MDMAQuality;
import de.rolandsw.schedulemc.mdma.items.MDMABaseItem;
import de.rolandsw.schedulemc.mdma.items.MDMAItems;
import de.rolandsw.schedulemc.mdma.items.SafrolItem;
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
 * Reaktions-Kessel - Erster Schritt der MDMA-Herstellung
 * Synthetisiert Safrol zu MDMA-Base
 * Passiver Prozess - keine aktive Interaktion nötig
 */
public class ReaktionsKesselBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    private static final int SYNTHESIS_TIME = 1000; // 50 Sekunden
    private static final int CAPACITY = 8;

    private int safrolCount = 0;
    private int synthesisProgress = 0;
    private int outputCount = 0;
    private MDMAQuality outputQuality = MDMAQuality.STANDARD;
    private boolean isActive = false;

    public ReaktionsKesselBlockEntity(BlockPos pos, BlockState state) {
        super(MDMABlockEntities.REAKTIONS_KESSEL.get(), pos, state);
    }

    public boolean addSafrol(ItemStack stack) {
        if (!(stack.getItem() instanceof SafrolItem)) return false;
        if (safrolCount >= CAPACITY || outputCount > 0) return false;

        safrolCount++;
        if (synthesisProgress == 0) isActive = true;
        setChanged();
        return true;
    }

    public ItemStack extractOutput() {
        if (outputCount <= 0) return ItemStack.EMPTY;

        ItemStack result = MDMABaseItem.create(outputQuality, outputCount);
        outputCount = 0;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (safrolCount > 0 && outputCount == 0) {
            isActive = true;
            synthesisProgress++;

            if (synthesisProgress >= SYNTHESIS_TIME) {
                // Synthese abgeschlossen - Qualität basiert auf Menge
                double qualityChance = safrolCount >= 6 ? 0.4 : (safrolCount >= 4 ? 0.25 : 0.1);
                if (level.random.nextFloat() < qualityChance) {
                    outputQuality = MDMAQuality.GUT;
                } else {
                    outputQuality = MDMAQuality.STANDARD;
                }

                outputCount = safrolCount;
                safrolCount = 0;
                synthesisProgress = 0;
                isActive = false;

                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            } else if (synthesisProgress % 40 == 0) {
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
    public int getSafrolCount() { return safrolCount; }
    public int getOutputCount() { return outputCount; }
    public float getProgress() { return (float) synthesisProgress / SYNTHESIS_TIME; }

    @Override
    public boolean isActivelyConsuming() {
        return isActive;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Safrol", safrolCount);
        tag.putInt("Progress", synthesisProgress);
        tag.putInt("Output", outputCount);
        tag.putString("Quality", outputQuality.name());
        tag.putBoolean("Active", isActive);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        safrolCount = tag.getInt("Safrol");
        synthesisProgress = tag.getInt("Progress");
        outputCount = tag.getInt("Output");
        if (tag.contains("Quality")) {
            try {
                outputQuality = MDMAQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException e) {
                outputQuality = MDMAQuality.STANDARD;
            }
        }
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
