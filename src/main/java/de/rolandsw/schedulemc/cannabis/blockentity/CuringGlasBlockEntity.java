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
    public static final int MIN_CURING_DAYS = 14;
    public static final int OPTIMAL_CURING_DAYS = 28;

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
        if (!storedItem.isEmpty()) return false;

        strain = TrimmedBudItem.getStrain(stack);
        baseQuality = TrimmedBudItem.getQuality(stack);
        weight = TrimmedBudItem.getWeight(stack);
        storedItem = stack.copy();
        curingTicks = 0;

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
    public int getCuringDays() { return curingTicks / TICKS_PER_DAY; }
    public int getCuringTicks() { return curingTicks; }
    public boolean isReadyForExtraction() { return getCuringDays() >= MIN_CURING_DAYS; }
    public boolean isOptimallyCured() { return getCuringDays() >= OPTIMAL_CURING_DAYS; }
    public CannabisStrain getStrain() { return strain; }
    public CannabisQuality getBaseQuality() { return baseQuality; }
    public int getWeight() { return weight; }

    public CannabisQuality getExpectedQuality() {
        return CannabisQuality.fromCuringTime(getCuringDays(), baseQuality);
    }

    public float getCuringProgress() {
        return Math.min(1.0f, (float) getCuringDays() / OPTIMAL_CURING_DAYS);
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
