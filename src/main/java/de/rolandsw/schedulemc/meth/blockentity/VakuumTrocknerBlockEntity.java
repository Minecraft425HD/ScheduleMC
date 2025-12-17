package de.rolandsw.schedulemc.meth.blockentity;

import de.rolandsw.schedulemc.meth.MethQuality;
import de.rolandsw.schedulemc.meth.items.KristallMethItem;
import de.rolandsw.schedulemc.meth.items.MethItem;
import de.rolandsw.schedulemc.meth.items.MethItems;
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
 * Vakuum-Trockner - Vierter und letzter Schritt der Meth-Herstellung
 * Trocknet Kristall-Meth (feucht) zu fertigem Meth
 * Passiver Prozess - keine aktive Interaktion nötig
 */
public class VakuumTrocknerBlockEntity extends BlockEntity {

    private static final int DRYING_TIME = 600; // 30 Sekunden
    private static final int CAPACITY = 6; // Kann 6 Batches gleichzeitig verarbeiten

    private ItemStack[] inputs = new ItemStack[CAPACITY];
    private ItemStack[] outputs = new ItemStack[CAPACITY];
    private int[] progress = new int[CAPACITY];
    private MethQuality[] qualities = new MethQuality[CAPACITY];

    private boolean isActive = false;

    public VakuumTrocknerBlockEntity(BlockPos pos, BlockState state) {
        super(MethBlockEntities.VAKUUM_TROCKNER.get(), pos, state);
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
     * Fügt Kristall-Meth hinzu
     */
    public boolean addKristallMeth(ItemStack stack) {
        if (!(stack.getItem() instanceof KristallMethItem)) {
            return false;
        }

        for (int i = 0; i < CAPACITY; i++) {
            if (inputs[i].isEmpty() && outputs[i].isEmpty()) {
                inputs[i] = stack.copy();
                inputs[i].setCount(1);
                qualities[i] = KristallMethItem.getQuality(stack);
                progress[i] = 0;
                setChanged();
                return true;
            }
        }
        return false;
    }

    /**
     * Extrahiert alles fertige Meth
     */
    public ItemStack extractAllOutput() {
        int totalCount = 0;
        MethQuality bestQuality = MethQuality.STANDARD;

        for (int i = 0; i < CAPACITY; i++) {
            if (!outputs[i].isEmpty()) {
                MethQuality quality = MethItem.getQuality(outputs[i]);
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

        return totalCount > 0 ? MethItem.create(bestQuality, totalCount) : ItemStack.EMPTY;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;
        boolean anyActive = false;

        for (int i = 0; i < CAPACITY; i++) {
            if (!inputs[i].isEmpty() && outputs[i].isEmpty()) {
                anyActive = true;
                progress[i]++;

                if (progress[i] >= DRYING_TIME) {
                    // Trocknung abgeschlossen - Qualität bleibt gleich
                    outputs[i] = MethItem.create(qualities[i], 1);
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
                totalProgress += (float) progress[i] / DRYING_TIME;
            }
        }

        return activeSlots > 0 ? totalProgress / activeSlots : 0;
    }

    /**
     * Gibt die beste Qualität im Trockner zurück
     */
    public MethQuality getBestQuality() {
        MethQuality best = MethQuality.STANDARD;
        for (int i = 0; i < CAPACITY; i++) {
            if ((!inputs[i].isEmpty() || !outputs[i].isEmpty()) && qualities[i].getLevel() > best.getLevel()) {
                best = qualities[i];
            }
        }
        return best;
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
