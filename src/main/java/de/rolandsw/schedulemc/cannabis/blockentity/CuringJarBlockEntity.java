package de.rolandsw.schedulemc.cannabis.blockentity;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.items.TrimmedBudItem;
import de.rolandsw.schedulemc.cannabis.items.CuredBudItem;
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
import de.rolandsw.schedulemc.utility.PlotUtilityManager;

/**
 * Curing-Glas für Cannabis
 * Je länger das Curing, desto besser die Qualität
 * Minimum: 14 Tage, Optimal: 28+ Tage
 */
public class CuringJarBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    public static final int TICKS_PER_DAY       = 24000;
    public static final int CURING_TICKS_NEEDED = TICKS_PER_DAY; // 1 MC day → +1 quality
    public static final int MAX_WEIGHT          = 10;

    private ItemStack storedItem = ItemStack.EMPTY;
    private ItemStack outputItem = ItemStack.EMPTY;
    private CannabisStrain strain = CannabisStrain.HYBRID;
    private CannabisQuality baseQuality = CannabisQuality.GUT;
    private int weight = 0;
    private long startDayTime = -1L; // absolute world-DayTime bei Einlegen

    public CuringJarBlockEntity(BlockPos pos, BlockState state) {
        super(CannabisBlockEntities.CURING_JAR.get(), pos, state);
    }

    public boolean addTrimmedBud(ItemStack stack) {
        if (!(stack.getItem() instanceof TrimmedBudItem)) return false;
        if (!outputItem.isEmpty()) return false; // warte bis Output entnommen

        CannabisStrain  addStrain  = TrimmedBudItem.getStrain(stack);
        CannabisQuality addQuality = TrimmedBudItem.getQuality(stack);

        if (storedItem.isEmpty()) {
            strain        = addStrain;
            baseQuality   = addQuality;
            storedItem    = stack.copy();
            storedItem.setCount(1);
            weight        = 1;
            startDayTime  = (level != null) ? level.getDayTime() : -1L;
        } else {
            if (strain != addStrain || baseQuality != addQuality) return false;
            if (storedItem.getCount() >= MAX_WEIGHT) return false;
            storedItem.grow(1);
            weight = storedItem.getCount();
        }

        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    public ItemStack extractCuredBud() {
        if (outputItem.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = outputItem.copy();
        outputItem  = ItemStack.EMPTY;
        weight      = 0;
        strain      = CannabisStrain.HYBRID;
        baseQuality = CannabisQuality.GUT;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return result;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;
        if (!PlotUtilityManager.areUtilitiesEnabled(getBlockPos())) return;

        if (!storedItem.isEmpty()) {
            // startDayTime bei erstem Tick initialisieren (Rückwärts-Kompatibilität)
            if (startDayTime < 0) {
                startDayTime = level.getDayTime();
                setChanged();
            }

            long elapsed = level.getDayTime() - startDayTime;

            if (elapsed >= CURING_TICKS_NEEDED) {
                // Curing abgeschlossen: +1 Qualitätsstufe, +20% Preis
                CannabisQuality finalQuality = baseQuality.upgrade();
                outputItem = CuredBudItem.create(strain, finalQuality, weight, 1);
                outputItem.getOrCreateTag().putFloat("PriceBonus", 0.20f);
                storedItem   = ItemStack.EMPTY;
                startDayTime = -1L;
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            } else if (elapsed % (TICKS_PER_DAY / 20) == 0) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        return !storedItem.isEmpty();
    }

    // Getter
    public boolean hasContent()      { return !storedItem.isEmpty(); }
    public boolean hasOutput()       { return !outputItem.isEmpty(); }
    public ItemStack getStoredItem() { return storedItem; }
    public ItemStack getOutputItem() { return outputItem; }
    public void clearStoredItem() {
        storedItem   = ItemStack.EMPTY;
        startDayTime = -1L;
        weight       = 0;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    public CannabisStrain  getStrain() { return strain; }
    public CannabisQuality getBaseQuality() { return baseQuality; }
    public int getWeight() { return weight; }

    /** Erwartete Qualität nach dem Curing (immer +1 Stufe) */
    public CannabisQuality getExpectedQuality() { return baseQuality.upgrade(); }

    /** Fortschritt 0–1000 für ContainerData-Sync (funktioniert mit /time add) */
    public int getCuringProgressScaled() {
        if (!hasContent() || level == null || startDayTime < 0) return 0;
        long elapsed = level.getDayTime() - startDayTime;
        return (int) Math.min(1000L, elapsed * 1000L / CURING_TICKS_NEEDED);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!storedItem.isEmpty()) tag.put("StoredItem", storedItem.save(new CompoundTag()));
        if (!outputItem.isEmpty()) tag.put("OutputItem", outputItem.save(new CompoundTag()));
        tag.putString("Strain", strain.name());
        tag.putString("Quality", baseQuality.name());
        tag.putInt("Weight", weight);
        tag.putLong("StartDayTime", startDayTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storedItem  = tag.contains("StoredItem")  ? ItemStack.of(tag.getCompound("StoredItem"))  : ItemStack.EMPTY;
        outputItem  = tag.contains("OutputItem")  ? ItemStack.of(tag.getCompound("OutputItem"))  : ItemStack.EMPTY;
        try { strain = CannabisStrain.valueOf(tag.getString("Strain")); }
        catch (IllegalArgumentException e) { strain = CannabisStrain.HYBRID; }
        try { baseQuality = CannabisQuality.valueOf(tag.getString("Quality")); }
        catch (IllegalArgumentException e) { baseQuality = CannabisQuality.GUT; }
        weight       = tag.getInt("Weight");
        startDayTime = tag.contains("StartDayTime") ? tag.getLong("StartDayTime") : -1L;
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
