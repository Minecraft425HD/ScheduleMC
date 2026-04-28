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
public class HashPressBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    public static final int PRESS_TICKS = 6000;  // 5 Minuten
    public static final int MIN_TRIM_WEIGHT = 20; // Minimum 20g Trim
    public static final int MAX_TRIM_WEIGHT = 20; // Maximum 20g Trim
    public static final float CONVERSION_RATE = 0.25f; // 20g Trim -> 5g Hash

    private int trimWeight = 0;
    private CannabisQuality trimQuality = CannabisQuality.GUT;
    private CannabisStrain strain = CannabisStrain.HYBRID;
    private long startDayTime = -1L;
    private boolean isPressing = false;
    private ItemStack outputItem = ItemStack.EMPTY;

    public HashPressBlockEntity(BlockPos pos, BlockState state) {
        super(CannabisBlockEntities.HASH_PRESS.get(), pos, state);
    }

    /**
     * Fügt Trim hinzu. Gibt die Anzahl verbrauchter Items zurück (0 = nichts hinzugefügt).
     * Berücksichtigt Stack-Count: 20 Items à 1g = 20g.
     */
    public int addTrim(ItemStack stack) {
        if (!(stack.getItem() instanceof TrimItem)) return 0;
        if (isPressing || !outputItem.isEmpty()) return 0;
        if (trimWeight >= MAX_TRIM_WEIGHT) return 0;

        CannabisStrain  trimStrain = TrimItem.getStrain(stack);
        CannabisQuality addQuality = TrimItem.getQuality(stack);

        if (trimWeight > 0 && trimStrain != strain) return 0;

        int space    = MAX_TRIM_WEIGHT - trimWeight;
        int canAdd   = Math.min(stack.getCount(), space); // 1 Item = 1g
        if (canAdd <= 0) return 0;

        strain = trimStrain;
        if (trimWeight == 0) trimQuality = addQuality;
        trimWeight += canAdd;

        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return canAdd;
    }

    public boolean startPressing() {
        if (trimWeight < MIN_TRIM_WEIGHT || isPressing || !outputItem.isEmpty()) {
            return false;
        }

        isPressing = true;
        startDayTime = (level != null) ? level.getDayTime() : 0L;

        setChanged();
        return true;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (isPressing) {
            if (startDayTime < 0) {
                startDayTime = level.getDayTime();
                setChanged();
            }
            long elapsed = level.getDayTime() - startDayTime;
            if (elapsed >= PRESS_TICKS) {
                finishPressing();
            } else {
                setChanged();
            }
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
        startDayTime = -1L;

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
    public float getPressProgress() {
        if (!isPressing || level == null || startDayTime < 0) return 0.0f;
        return (float) Math.min(1.0, (double)(level.getDayTime() - startDayTime) / PRESS_TICKS);
    }
    public int getExpectedHashWeight() { return Math.max(1, (int) (trimWeight * CONVERSION_RATE)); }
    public CannabisStrain getStrain() { return strain; }
    public CannabisQuality getTrimQuality() { return trimQuality; }
    public ItemStack getOutputItem() { return outputItem; }
    public void clearTrim() {
        trimWeight = 0;
        trimQuality = CannabisQuality.GUT;
        strain = CannabisStrain.HYBRID;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public ItemStack getInputDisplayItem() {
        if (trimWeight == 0) return ItemStack.EMPTY;
        ItemStack item = TrimItem.create(strain, trimQuality, trimWeight);
        return item;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("TrimWeight", trimWeight);
        tag.putString("Strain", strain.name());
        tag.putString("TrimQuality", trimQuality.name());
        tag.putLong("StartDayTime", startDayTime);
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
        startDayTime = tag.contains("StartDayTime") ? tag.getLong("StartDayTime") : -1L;
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
