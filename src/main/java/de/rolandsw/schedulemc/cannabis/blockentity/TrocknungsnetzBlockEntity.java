package de.rolandsw.schedulemc.cannabis.blockentity;
nimport de.rolandsw.schedulemc.util.GameConstants;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.items.FreshBudItem;
import de.rolandsw.schedulemc.cannabis.items.DriedBudItem;
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
 * Trocknungsnetz für Cannabis-Blüten
 * Trocknet frische Blüten über 3-5 Minecraft-Tage
 */
public class TrocknungsnetzBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    public static final int DRYING_TICKS = 72000; // 3 Tage (24000 Ticks pro Tag)
    public static final int MAX_CAPACITY = 4; // 4 Slots

    private ItemStack[] dryingItems = new ItemStack[MAX_CAPACITY];
    private int[] dryingProgress = new int[MAX_CAPACITY];
    private CannabisStrain[] strains = new CannabisStrain[MAX_CAPACITY];
    private CannabisQuality[] qualities = new CannabisQuality[MAX_CAPACITY];
    private int[] weights = new int[MAX_CAPACITY];

    public TrocknungsnetzBlockEntity(BlockPos pos, BlockState state) {
        super(CannabisBlockEntities.TROCKNUNGSNETZ.get(), pos, state);
        for (int i = 0; i < MAX_CAPACITY; i++) {
            dryingItems[i] = ItemStack.EMPTY;
            dryingProgress[i] = 0;
            strains[i] = CannabisStrain.HYBRID;
            qualities[i] = CannabisQuality.MIDS;
            weights[i] = 0;
        }
    }

    public boolean addFreshBud(ItemStack stack, int slot) {
        if (slot < 0 || slot >= MAX_CAPACITY) return false;
        if (!(stack.getItem() instanceof FreshBudItem)) return false;
        if (!dryingItems[slot].isEmpty()) return false;

        strains[slot] = FreshBudItem.getStrain(stack);
        qualities[slot] = FreshBudItem.getQuality(stack);
        weights[slot] = FreshBudItem.getWeight(stack);
        dryingItems[slot] = stack.copy();
        dryingProgress[slot] = 0;

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    public ItemStack extractDriedBud(int slot) {
        if (slot < 0 || slot >= MAX_CAPACITY) return ItemStack.EMPTY;
        if (dryingItems[slot].isEmpty()) return ItemStack.EMPTY;
        if (dryingProgress[slot] < DRYING_TICKS) return ItemStack.EMPTY;

        ItemStack result = DriedBudItem.create(strains[slot], qualities[slot], weights[slot]);

        dryingItems[slot] = ItemStack.EMPTY;
        dryingProgress[slot] = 0;
        weights[slot] = 0;

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;
        for (int i = 0; i < MAX_CAPACITY; i++) {
            if (!dryingItems[i].isEmpty() && dryingProgress[i] < DRYING_TICKS) {
                dryingProgress[i]++;
                changed = true;
            }
        }

        if (changed) {
            setChanged();
        }

        // Utility-Status nur bei Änderung melden
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // IUtilityConsumer Implementation
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public boolean isActivelyConsuming() {
        // Aktiv wenn mindestens ein Item zum Trocknen vorhanden ist
        for (int i = 0; i < MAX_CAPACITY; i++) {
            if (!dryingItems[i].isEmpty() && dryingProgress[i] < DRYING_TICKS) {
                return true;
            }
        }
        return false;
    }

    // Getter
    public boolean hasItem(int slot) {
        return slot >= 0 && slot < MAX_CAPACITY && !dryingItems[slot].isEmpty();
    }

    public boolean isDried(int slot) {
        return slot >= 0 && slot < MAX_CAPACITY && dryingProgress[slot] >= DRYING_TICKS;
    }

    public float getDryingProgress(int slot) {
        if (slot < 0 || slot >= MAX_CAPACITY) return 0;
        return (float) dryingProgress[slot] / DRYING_TICKS;
    }

    public CannabisStrain getStrain(int slot) {
        if (slot < 0 || slot >= MAX_CAPACITY) return CannabisStrain.HYBRID;
        return strains[slot];
    }

    public int getFilledSlots() {
        int count = 0;
        for (ItemStack item : dryingItems) {
            if (!item.isEmpty()) count++;
        }
        return count;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for (int i = 0; i < MAX_CAPACITY; i++) {
            if (!dryingItems[i].isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                dryingItems[i].save(itemTag);
                tag.put("Item" + i, itemTag);
                tag.putInt("Progress" + i, dryingProgress[i]);
                tag.putString("Strain" + i, strains[i].name());
                tag.putString("Quality" + i, qualities[i].name());
                tag.putInt("Weight" + i, weights[i]);
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < MAX_CAPACITY; i++) {
            if (tag.contains("Item" + i)) {
                dryingItems[i] = ItemStack.of(tag.getCompound("Item" + i));
                dryingProgress[i] = tag.getInt("Progress" + i);
                try {
                    strains[i] = CannabisStrain.valueOf(tag.getString("Strain" + i));
                } catch (IllegalArgumentException e) {
                    strains[i] = CannabisStrain.HYBRID;
                }
                try {
                    qualities[i] = CannabisQuality.valueOf(tag.getString("Quality" + i));
                } catch (IllegalArgumentException e) {
                    qualities[i] = CannabisQuality.MIDS;
                }
                weights[i] = tag.getInt("Weight" + i);
            } else {
                dryingItems[i] = ItemStack.EMPTY;
                dryingProgress[i] = 0;
            }
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
