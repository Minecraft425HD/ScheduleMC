package de.rolandsw.schedulemc.cannabis.blockentity;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.items.TrimItem;
import de.rolandsw.schedulemc.cannabis.items.HashItem;
import de.rolandsw.schedulemc.cannabis.items.CannabisItems;
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
 * Hash-Presse - presst Trim zu Haschisch
 * Benötigt mindestens 20g Trim für 5g Hash
 */
public class HashPresseBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    public static final int PRESS_TICKS = 6000;  // 5 Minuten
    public static final int MIN_TRIM_WEIGHT = 20; // Minimum 20g Trim
    public static final int MAX_TRIM_WEIGHT = 20; // Maximum 20g Trim
    public static final float CONVERSION_RATE = 0.25f; // 20g Trim -> 5g Hash

    private int trimWeight = 0;
    private CannabisQuality trimQuality = CannabisQuality.GUT;
    private CannabisStrain strain = CannabisStrain.HYBRID;
    private int pressProgress = 0;
    private boolean isPressing = false;  // NOPMD
    private ItemStack outputItem = ItemStack.EMPTY;

    public HashPresseBlockEntity(BlockPos pos, BlockState state) {
        super(CannabisBlockEntities.HASH_PRESSE.get(), pos, state);
    }

    public boolean addTrim(ItemStack stack) {
        if (!(stack.getItem() instanceof TrimItem)) return false;
        if (isPressing || !outputItem.isEmpty()) return false;
        if (trimWeight >= MAX_TRIM_WEIGHT) return false;

        CannabisStrain  trimStrain  = TrimItem.getStrain(stack);
        CannabisQuality addQuality  = TrimItem.getQuality(stack);
        int addWeight = TrimItem.getWeight(stack);

        // Nur gleiche Sorte erlauben
        if (trimWeight > 0 && trimStrain != strain) {
            return false;
        }

        // Überlauf verhindern
        if (trimWeight + addWeight > MAX_TRIM_WEIGHT) return false;

        strain = trimStrain;
        if (trimWeight == 0) { trimQuality = addQuality; }
        trimWeight += addWeight;

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    public boolean startPressing() {
        if (trimWeight < MIN_TRIM_WEIGHT || isPressing || !outputItem.isEmpty()) {
            return false;
        }

        isPressing = true;
        pressProgress = 0;

        setChanged();
        return true;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (isPressing) {
            pressProgress = Math.min(pressProgress + 1, PRESS_TICKS);

            if (pressProgress >= PRESS_TICKS) {
                finishPressing();
            }

            setChanged();
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
        return isPressing;
    }

    private void finishPressing() {
        int hashWeight = Math.max(1, (int) (trimWeight * CONVERSION_RATE));

        // Jedes Gramm = 1 Item (Weight=1 in NBT), count = hashWeight
        outputItem = HashItem.create(strain, trimQuality, 1);
        outputItem.setCount(hashWeight);

        trimWeight = 0;
        isPressing = false;
        pressProgress = 0;

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public ItemStack extractHash() {
        if (outputItem.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = outputItem.copy();
        outputItem = ItemStack.EMPTY;

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    // Getter
    public int getTrimWeight() { return trimWeight; }
    public boolean canStart() { return trimWeight >= MIN_TRIM_WEIGHT && !isPressing && outputItem.isEmpty(); }
    public boolean isPressing() { return isPressing; }
    public boolean hasOutput() { return !outputItem.isEmpty(); }
    public float getPressProgress() { return (float) pressProgress / PRESS_TICKS; }
    public int getExpectedHashWeight() { return Math.max(1, (int) (trimWeight * CONVERSION_RATE)); }
    public CannabisStrain getStrain() { return strain; }
    public CannabisQuality getTrimQuality() { return trimQuality; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("TrimWeight", trimWeight);
        tag.putString("Strain", strain.name());
        tag.putString("TrimQuality", trimQuality.name());
        tag.putInt("PressProgress", pressProgress);
        tag.putBoolean("IsPressing", isPressing);
        if (!outputItem.isEmpty()) {
            CompoundTag outputTag = new CompoundTag();
            outputItem.save(outputTag);
            tag.put("Output", outputTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        trimWeight = tag.getInt("TrimWeight");
        try { strain = CannabisStrain.valueOf(tag.getString("Strain")); }
        catch (IllegalArgumentException e) { strain = CannabisStrain.HYBRID; }
        try { trimQuality = CannabisQuality.valueOf(tag.getString("TrimQuality")); }
        catch (IllegalArgumentException e) { trimQuality = CannabisQuality.GUT; }
        pressProgress = tag.getInt("PressProgress");
        isPressing = tag.getBoolean("IsPressing");
        outputItem = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
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
