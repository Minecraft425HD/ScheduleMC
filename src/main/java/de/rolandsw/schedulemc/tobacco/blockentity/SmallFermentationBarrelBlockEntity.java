package de.rolandsw.schedulemc.tobacco.blockentity;

import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.items.DriedTobaccoLeafItem;
import de.rolandsw.schedulemc.tobacco.items.FermentedTobaccoLeafItem;
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
 * Kleines Fermentierungsfass BlockEntity
 * Kapazität: 6 Tabakblätter
 */
public class SmallFermentationBarrelBlockEntity extends BlockEntity {

    private static final int CAPACITY = 6;
    private static final int FERMENTATION_TIME = 12000; // 10 Minuten

    private final ItemStack[] inputs = new ItemStack[CAPACITY];
    private final ItemStack[] outputs = new ItemStack[CAPACITY];
    private final int[] fermentationProgress = new int[CAPACITY];
    private final TobaccoType[] tobaccoTypes = new TobaccoType[CAPACITY];
    private final TobaccoQuality[] qualities = new TobaccoQuality[CAPACITY];

    public SmallFermentationBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(TobaccoBlockEntities.SMALL_FERMENTATION_BARREL.get(), pos, state);
        for (int i = 0; i < CAPACITY; i++) {
            inputs[i] = ItemStack.EMPTY;
            outputs[i] = ItemStack.EMPTY;
            fermentationProgress[i] = 0;
        }
    }

    public boolean addDriedLeaves(ItemStack stack) {
        if (!(stack.getItem() instanceof DriedTobaccoLeafItem)) {
            return false;
        }

        // Finde leeren Slot
        for (int i = 0; i < CAPACITY; i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                inputs[i] = stack.copy();
                inputs[i].setCount(1);
                tobaccoTypes[i] = DriedTobaccoLeafItem.getType(stack);
                qualities[i] = DriedTobaccoLeafItem.getQuality(stack);
                fermentationProgress[i] = 0;
                setChanged();
                return true;
            }
        }
        return false;
    }

    public ItemStack extractAllFermentedLeaves() {
        int totalCount = 0;
        TobaccoType type = null;
        TobaccoQuality quality = null;

        // Sammle alle fertigen Blätter
        for (int i = 0; i < CAPACITY; i++) {
            if (!outputs[i].isEmpty()) {
                if (type == null) {
                    type = FermentedTobaccoLeafItem.getType(outputs[i]);
                    quality = FermentedTobaccoLeafItem.getQuality(outputs[i]);
                }
                totalCount += outputs[i].getCount();
                outputs[i] = ItemStack.EMPTY;
                inputs[i] = ItemStack.EMPTY;
                fermentationProgress[i] = 0;
                tobaccoTypes[i] = null;
                qualities[i] = null;
            }
        }

        setChanged();
        return totalCount > 0 ? FermentedTobaccoLeafItem.create(type, quality, totalCount) : ItemStack.EMPTY;
    }

    public boolean isFull() {
        for (int i = 0; i < CAPACITY; i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasInput() {
        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOutput() {
        for (int i = 0; i < CAPACITY; i++) {
            if (!outputs[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public int getInputCount() {
        int count = 0;
        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty()) count++;
        }
        return count;
    }

    public int getOutputCount() {
        int count = 0;
        for (int i = 0; i < CAPACITY; i++) {
            if (!outputs[i].isEmpty()) count++;
        }
        return count;
    }

    public float getAverageFermentationPercentage() {
        int activeSlots = 0;
        float totalProgress = 0;

        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty()) {
                activeSlots++;
                totalProgress += (float) fermentationProgress[i] / FERMENTATION_TIME;
            }
        }

        return activeSlots > 0 ? totalProgress / activeSlots : 0;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;

        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                fermentationProgress[i]++;

                if (fermentationProgress[i] >= FERMENTATION_TIME) {
                    // Fermentierung abgeschlossen - mit 30% Chance auf Qualitätsverbesserung
                    TobaccoQuality finalQuality = calculateFinalQuality(qualities[i]);
                    outputs[i] = FermentedTobaccoLeafItem.create(tobaccoTypes[i], finalQuality, 1);
                    changed = true;
                }

                if (fermentationProgress[i] % 20 == 0) {
                    changed = true;
                }
            }
        }

        if (changed) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private TobaccoQuality calculateFinalQuality(TobaccoQuality quality) {
        if (quality == TobaccoQuality.LEGENDAER) {
            return quality;
        }

        // 30% Chance auf Upgrade
        if (level != null && level.random.nextFloat() < 0.3f) {
            return quality.upgrade();
        }

        return quality;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty()) {
                CompoundTag inputTag = new CompoundTag();
                inputs[i].save(inputTag);
                tag.put("Input" + i, inputTag);
            }

            if (!outputs[i].isEmpty()) {
                CompoundTag outputTag = new CompoundTag();
                outputs[i].save(outputTag);
                tag.put("Output" + i, outputTag);
            }

            tag.putInt("Progress" + i, fermentationProgress[i]);

            if (tobaccoTypes[i] != null) {
                tag.putString("Type" + i, tobaccoTypes[i].name());
            }
            if (qualities[i] != null) {
                tag.putString("Quality" + i, qualities[i].name());
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        for (int i = 0; i < CAPACITY; i++) {
            inputs[i] = tag.contains("Input" + i) ? ItemStack.of(tag.getCompound("Input" + i)) : ItemStack.EMPTY;
            outputs[i] = tag.contains("Output" + i) ? ItemStack.of(tag.getCompound("Output" + i)) : ItemStack.EMPTY;
            fermentationProgress[i] = tag.getInt("Progress" + i);

            if (tag.contains("Type" + i)) {
                tobaccoTypes[i] = TobaccoType.valueOf(tag.getString("Type" + i));
            }
            if (tag.contains("Quality" + i)) {
                qualities[i] = TobaccoQuality.valueOf(tag.getString("Quality" + i));
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
