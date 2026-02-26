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

/**
 * Curing-Glas für Cannabis
 * Je länger das Curing, desto besser die Qualität
 * Minimum: 14 Tage, Optimal: 28+ Tage
 */
public class CuringGlasBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    public static final int TICKS_PER_DAY = 24000;
    public static final int MAX_WEIGHT    = 10;  // Glas fasst bis zu 10g

    private ItemStack storedItem = ItemStack.EMPTY;
    private CannabisStrain strain = CannabisStrain.HYBRID;
    private CannabisQuality baseQuality = CannabisQuality.GUT;
    private int weight = 0;
    private int curingTicks = 0;

    public CuringGlasBlockEntity(BlockPos pos, BlockState state) {
        super(CannabisBlockEntities.CURING_GLAS.get(), pos, state);
    }

    public boolean addTrimmedBud(ItemStack stack) {
        if (!(stack.getItem() instanceof TrimmedBudItem)) return false;

        CannabisStrain  addStrain  = TrimmedBudItem.getStrain(stack);
        CannabisQuality addQuality = TrimmedBudItem.getQuality(stack);

        if (storedItem.isEmpty()) {
            strain      = addStrain;
            baseQuality = addQuality;
            storedItem  = stack.copy();
            storedItem.setCount(1);
            weight      = 1;
            curingTicks = 0;
        } else {
            // Nur gleiche Sorte & Qualität, und noch Platz
            if (strain != addStrain || baseQuality != addQuality) return false;
            if (storedItem.getCount() >= MAX_WEIGHT) return false;
            storedItem.grow(1);
            weight = storedItem.getCount();
        }

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    public ItemStack extractCuredBud() {
        if (storedItem.isEmpty()) return ItemStack.EMPTY;

        int curingDays = curingTicks / TICKS_PER_DAY;

        // Berechne finale Qualität basierend auf Curing-Zeit
        CannabisQuality finalQuality = CannabisQuality.fromCuringTime(curingDays, baseQuality);

        ItemStack result = CuredBudItem.create(strain, finalQuality, weight, curingDays);

        storedItem = ItemStack.EMPTY;
        curingTicks = 0;

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (!storedItem.isEmpty()) {
            curingTicks++;

            // Update jeden Minecraft-Tag
            if (curingTicks % TICKS_PER_DAY == 0) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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
        return !storedItem.isEmpty();
    }

    // Getter
    public boolean hasContent() { return !storedItem.isEmpty(); }
    public int getCuringDays()  { return curingTicks / TICKS_PER_DAY; }
    public int getCuringTicks() { return curingTicks; }
    public CannabisStrain  getStrain()      { return strain; }
    public CannabisQuality getBaseQuality() { return baseQuality; }
    public int getWeight() { return storedItem.isEmpty() ? 0 : storedItem.getCount(); }

    /** Qualität hat sich mindestens 1 Stufe verbessert */
    public boolean isReadyForExtraction() { return getCuringDays() >= 1; }

    /** Maximale Qualität (LEGENDAER) erreicht */
    public boolean isOptimallyCured() {
        int daysNeeded = CannabisQuality.LEGENDAER.getLevel() - baseQuality.getLevel();
        return getCuringDays() >= daysNeeded;
    }

    public CannabisQuality getExpectedQuality() {
        return CannabisQuality.fromCuringTime(getCuringDays(), baseQuality);
    }

    /** Fortschritt 0.0–1.0 bis zur maximalen Qualitätsstufe */
    public float getCuringProgress() {
        int daysNeeded = CannabisQuality.LEGENDAER.getLevel() - baseQuality.getLevel();
        if (daysNeeded <= 0) return 1.0f;
        return Math.min(1.0f, (float) getCuringDays() / daysNeeded);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!storedItem.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            storedItem.save(itemTag);
            tag.put("StoredItem", itemTag);
        }
        tag.putString("Strain", strain.name());
        tag.putString("Quality", baseQuality.name());
        tag.putInt("Weight", weight);
        tag.putInt("CuringTicks", curingTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storedItem = tag.contains("StoredItem") ? ItemStack.of(tag.getCompound("StoredItem")) : ItemStack.EMPTY;
        try { strain = CannabisStrain.valueOf(tag.getString("Strain")); }
        catch (IllegalArgumentException e) { strain = CannabisStrain.HYBRID; }
        try { baseQuality = CannabisQuality.valueOf(tag.getString("Quality")); }
        catch (IllegalArgumentException e) { baseQuality = CannabisQuality.GUT; }
        weight = tag.getInt("Weight");
        curingTicks = tag.getInt("CuringTicks");
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
