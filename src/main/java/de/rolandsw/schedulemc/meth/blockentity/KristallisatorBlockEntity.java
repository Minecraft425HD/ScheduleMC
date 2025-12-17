package de.rolandsw.schedulemc.meth.blockentity;

import de.rolandsw.schedulemc.meth.MethQuality;
import de.rolandsw.schedulemc.meth.items.KristallMethItem;
import de.rolandsw.schedulemc.meth.items.MethItems;
import de.rolandsw.schedulemc.meth.items.RohMethItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;

/**
 * Kristallisator - Dritter Schritt der Meth-Herstellung
 * Wandelt Roh-Meth in Kristall-Meth (feucht) um
 * Passiver Prozess - keine aktive Interaktion nötig
 */
public class KristallisatorBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;
    private static final int CRYSTALLIZATION_TIME = 800; // 40 Sekunden
    private static final int CAPACITY = 4; // Kann 4 Batches gleichzeitig verarbeiten

    private ItemStack[] inputs = new ItemStack[CAPACITY];
    private ItemStack[] outputs = new ItemStack[CAPACITY];
    private int[] progress = new int[CAPACITY];
    private MethQuality[] qualities = new MethQuality[CAPACITY];

    private boolean isActive = false;

    public KristallisatorBlockEntity(BlockPos pos, BlockState state) {
        super(MethBlockEntities.KRISTALLISATOR.get(), pos, state);
        initArrays();
    }

    private void initArrays() {
        for (int i = 0; i < CAPACITY; i++) {
            inputs[i] = ItemStack.EMPTY;
            outputs[i] = ItemStack.EMPTY;
            progress[i] = 0;
            qualities[i] = MethQuality.STANDARD;
        }
    }

    /**
     * Fügt Roh-Meth hinzu
     */
    public boolean addRohMeth(ItemStack stack) {
        if (!(stack.getItem() instanceof RohMethItem)) {
            return false;
        }

        for (int i = 0; i < CAPACITY; i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                inputs[i] = stack.copy();
                inputs[i].setCount(1);
                qualities[i] = RohMethItem.getQuality(stack);
                progress[i] = 0;
                setChanged();
                return true;
            }
        }
        return false;
    }

    /**
     * Extrahiert alle fertigen Kristalle
     */
    public ItemStack extractAllOutput() {
        int totalCount = 0;
        MethQuality bestQuality = MethQuality.STANDARD;

        for (int i = 0; i < CAPACITY; i++) {
            if (!outputs[i].isEmpty()) {
                MethQuality quality = KristallMethItem.getQuality(outputs[i]);
                if (quality.getLevel() > bestQuality.getLevel()) {
                    bestQuality = quality;
                }
                totalCount += outputs[i].getCount();

                // Slots leeren
                outputs[i] = ItemStack.EMPTY;
                inputs[i] = ItemStack.EMPTY;
                progress[i] = 0;
                qualities[i] = MethQuality.STANDARD;
            }
        }

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        return totalCount > 0 ? KristallMethItem.create(bestQuality, totalCount) : ItemStack.EMPTY;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;
        boolean anyActive = false;

        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                anyActive = true;
                progress[i]++;

                if (progress[i] >= CRYSTALLIZATION_TIME) {
                    // Kristallisation abgeschlossen
                    // Kleine Chance auf Qualitätsverbesserung
                    MethQuality finalQuality = qualities[i];
                    if (level.random.nextFloat() < 0.15) { // 15% Chance
                        finalQuality = finalQuality.upgrade();
                    }

                    outputs[i] = KristallMethItem.create(finalQuality, 1);
                    changed = true;
                }

                if (progress[i] % 40 == 0) {
                    changed = true;
                }
            }
        }

        isActive = anyActive;

        if (changed) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        // Utility-Status nur bei Änderung melden
        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GETTER
    // ═══════════════════════════════════════════════════════════

    public boolean isActive() {
        return isActive;
    }

    public boolean hasOutput() {
        for (int i = 0; i < CAPACITY; i++) {
            if (!outputs[i].isEmpty()) return true;
        }
        return false;
    }

    public boolean hasInput() {
        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty()) return true;
        }
        return false;
    }

    public int getActiveSlots() {
        int count = 0;
        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) count++;
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

    public float getAverageProgress() {
        int activeSlots = 0;
        float totalProgress = 0;

        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                activeSlots++;
                totalProgress += (float) progress[i] / CRYSTALLIZATION_TIME;
            }
        }

        return activeSlots > 0 ? totalProgress / activeSlots : 0;
    }

    @Override
    public boolean isActivelyConsuming() {
        return isActive;
    }

    // ═══════════════════════════════════════════════════════════
    // NBT
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                inputs[i].save(slotTag);
                tag.put("Input" + i, slotTag);
            }
            if (!outputs[i].isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                outputs[i].save(slotTag);
                tag.put("Output" + i, slotTag);
            }
            tag.putInt("Progress" + i, progress[i]);
            tag.putString("Quality" + i, qualities[i].name());
        }
        tag.putBoolean("Active", isActive);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (inputs == null) {
            initArrays();
        }

        for (int i = 0; i < CAPACITY; i++) {
            inputs[i] = tag.contains("Input" + i) ? ItemStack.of(tag.getCompound("Input" + i)) : ItemStack.EMPTY;
            outputs[i] = tag.contains("Output" + i) ? ItemStack.of(tag.getCompound("Output" + i)) : ItemStack.EMPTY;
            progress[i] = tag.getInt("Progress" + i);
            if (tag.contains("Quality" + i)) {
                try {
                    qualities[i] = MethQuality.valueOf(tag.getString("Quality" + i));
                } catch (IllegalArgumentException e) {
                    qualities[i] = MethQuality.STANDARD;
                }
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
