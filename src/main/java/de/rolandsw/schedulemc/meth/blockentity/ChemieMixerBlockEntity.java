package de.rolandsw.schedulemc.meth.blockentity;

import de.rolandsw.schedulemc.meth.MethQuality;
import de.rolandsw.schedulemc.meth.items.EphedrinItem;
import de.rolandsw.schedulemc.meth.items.JodItem;
import de.rolandsw.schedulemc.meth.items.MethItems;
import de.rolandsw.schedulemc.meth.items.MethPasteItem;
import de.rolandsw.schedulemc.meth.items.PseudoephedrinItem;
import de.rolandsw.schedulemc.meth.items.RoterPhosphorItem;
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
 * Chemie-Mixer - Erster Schritt der Meth-Herstellung
 * Kombiniert: Ephedrin/Pseudoephedrin + Roter Phosphor + Jod → Meth-Paste
 */
public class ChemieMixerBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;
    private static final int MIXING_TIME = 600; // 30 Sekunden (600 Ticks)
    private static final int CAPACITY = 4; // Kann 4 Batches gleichzeitig verarbeiten

    private ItemStack[] ephedrinSlots = new ItemStack[CAPACITY];
    private ItemStack[] phosphorSlots = new ItemStack[CAPACITY];
    private ItemStack[] jodSlots = new ItemStack[CAPACITY];
    private ItemStack[] outputSlots = new ItemStack[CAPACITY];

    private int[] mixingProgress = new int[CAPACITY];
    private boolean[] usedPseudoephedrin = new boolean[CAPACITY]; // Für Qualitätsbonus
    private boolean isActive = false;

    public ChemieMixerBlockEntity(BlockPos pos, BlockState state) {
        super(MethBlockEntities.CHEMIE_MIXER.get(), pos, state);
        initArrays();
    }

    private void initArrays() {
        for (int i = 0; i < CAPACITY; i++) {
            ephedrinSlots[i] = ItemStack.EMPTY;
            phosphorSlots[i] = ItemStack.EMPTY;
            jodSlots[i] = ItemStack.EMPTY;
            outputSlots[i] = ItemStack.EMPTY;
            mixingProgress[i] = 0;
            usedPseudoephedrin[i] = false;
        }
    }

    /**
     * Fügt Ephedrin oder Pseudoephedrin hinzu
     */
    public boolean addEphedrin(ItemStack stack) {
        if (!(stack.getItem() instanceof EphedrinItem) &&
            !(stack.getItem() instanceof PseudoephedrinItem)) {
            return false;
        }

        for (int i = 0; i < CAPACITY; i++) {
            if (ephedrinSlots[i].isEmpty() && outputSlots[i].isEmpty()) {
                ephedrinSlots[i] = stack.copy();
                ephedrinSlots[i].setCount(1);
                usedPseudoephedrin[i] = stack.getItem() instanceof PseudoephedrinItem;
                setChanged();
                return true;
            }
        }
        return false;
    }

    /**
     * Fügt Roten Phosphor hinzu
     */
    public boolean addPhosphor(ItemStack stack) {
        if (!(stack.getItem() instanceof RoterPhosphorItem)) {
            return false;
        }

        for (int i = 0; i < CAPACITY; i++) {
            if (phosphorSlots[i].isEmpty() && !ephedrinSlots[i].isEmpty() && outputSlots[i].isEmpty()) {
                phosphorSlots[i] = stack.copy();
                phosphorSlots[i].setCount(1);
                setChanged();
                return true;
            }
        }
        return false;
    }

    /**
     * Fügt Jod hinzu
     */
    public boolean addJod(ItemStack stack) {
        if (!(stack.getItem() instanceof JodItem)) {
            return false;
        }

        for (int i = 0; i < CAPACITY; i++) {
            if (jodSlots[i].isEmpty() && !phosphorSlots[i].isEmpty() && outputSlots[i].isEmpty()) {
                jodSlots[i] = stack.copy();
                jodSlots[i].setCount(1);
                setChanged();
                return true;
            }
        }
        return false;
    }

    /**
     * Extrahiert alle fertigen Meth-Pasten
     */
    public ItemStack extractAllOutput() {
        int totalCount = 0;
        MethQuality bestQuality = MethQuality.SCHLECHT;

        for (int i = 0; i < CAPACITY; i++) {
            if (!outputSlots[i].isEmpty()) {
                MethQuality quality = MethPasteItem.getQuality(outputSlots[i]);
                if (quality.getLevel() > bestQuality.getLevel()) {
                    bestQuality = quality;
                }
                totalCount += outputSlots[i].getCount();

                // Leere alle Slots
                outputSlots[i] = ItemStack.EMPTY;
                ephedrinSlots[i] = ItemStack.EMPTY;
                phosphorSlots[i] = ItemStack.EMPTY;
                jodSlots[i] = ItemStack.EMPTY;
                mixingProgress[i] = 0;
                usedPseudoephedrin[i] = false;
            }
        }

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        return totalCount > 0 ? MethPasteItem.create(bestQuality, totalCount) : ItemStack.EMPTY;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean changed = false;
        boolean anyActive = false;

        for (int i = 0; i < CAPACITY; i++) {
            // Prüfe ob alle Zutaten vorhanden sind
            if (!ephedrinSlots[i].isEmpty() && !phosphorSlots[i].isEmpty() &&
                !jodSlots[i].isEmpty() && outputSlots[i].isEmpty()) {

                anyActive = true;
                mixingProgress[i]++;

                if (mixingProgress[i] >= MIXING_TIME) {
                    // Mischen abgeschlossen
                    MethQuality quality = calculateQuality(i);
                    outputSlots[i] = MethPasteItem.create(quality, 1);

                    // Verbrauche Zutaten (bleiben als "used" markiert im Slot bis Entnahme)
                    ephedrinSlots[i] = ItemStack.EMPTY;
                    phosphorSlots[i] = ItemStack.EMPTY;
                    jodSlots[i] = ItemStack.EMPTY;

                    changed = true;
                }

                if (mixingProgress[i] % 20 == 0) {
                    changed = true; // Update alle Sekunde
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

    private MethQuality calculateQuality(int slot) {
        // Pseudoephedrin gibt 10% Bonus auf Qualitätschance
        double qualityChance = usedPseudoephedrin[slot] ? 0.35 : 0.25;

        if (level != null && level.random.nextFloat() < qualityChance) {
            return MethQuality.GUT; // Chance auf bessere Qualität
        }
        return MethQuality.SCHLECHT;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean hasOutput() {
        for (int i = 0; i < CAPACITY; i++) {
            if (!outputSlots[i].isEmpty()) return true;
        }
        return false;
    }

    public boolean hasIngredients() {
        for (int i = 0; i < CAPACITY; i++) {
            if (!ephedrinSlots[i].isEmpty() || !phosphorSlots[i].isEmpty() || !jodSlots[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public int getActiveSlots() {
        int count = 0;
        for (int i = 0; i < CAPACITY; i++) {
            if (!ephedrinSlots[i].isEmpty() && !phosphorSlots[i].isEmpty() && !jodSlots[i].isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public int getOutputCount() {
        int count = 0;
        for (int i = 0; i < CAPACITY; i++) {
            if (!outputSlots[i].isEmpty()) count++;
        }
        return count;
    }

    public float getAverageProgress() {
        int activeSlots = 0;
        float totalProgress = 0;

        for (int i = 0; i < CAPACITY; i++) {
            if (!ephedrinSlots[i].isEmpty() && !phosphorSlots[i].isEmpty() && !jodSlots[i].isEmpty() && outputSlots[i].isEmpty()) {
                activeSlots++;
                totalProgress += (float) mixingProgress[i] / MIXING_TIME;
            }
        }

        return activeSlots > 0 ? totalProgress / activeSlots : 0;
    }

    public String getIngredientStatus() {
        int ephedrin = 0, phosphor = 0, jod = 0;
        for (int i = 0; i < CAPACITY; i++) {
            if (!ephedrinSlots[i].isEmpty()) ephedrin++;
            if (!phosphorSlots[i].isEmpty()) phosphor++;
            if (!jodSlots[i].isEmpty()) jod++;
        }
        return "E:" + ephedrin + " P:" + phosphor + " J:" + jod;
    }

    @Override
    public boolean isActivelyConsuming() {
        return isActive;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        for (int i = 0; i < CAPACITY; i++) {
            if (!ephedrinSlots[i].isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                ephedrinSlots[i].save(slotTag);
                tag.put("Ephedrin" + i, slotTag);
            }
            if (!phosphorSlots[i].isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                phosphorSlots[i].save(slotTag);
                tag.put("Phosphor" + i, slotTag);
            }
            if (!jodSlots[i].isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                jodSlots[i].save(slotTag);
                tag.put("Jod" + i, slotTag);
            }
            if (!outputSlots[i].isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                outputSlots[i].save(slotTag);
                tag.put("Output" + i, slotTag);
            }
            tag.putInt("Progress" + i, mixingProgress[i]);
            tag.putBoolean("Pseudo" + i, usedPseudoephedrin[i]);
        }
        tag.putBoolean("Active", isActive);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (ephedrinSlots == null) {
            initArrays();
        }

        for (int i = 0; i < CAPACITY; i++) {
            ephedrinSlots[i] = tag.contains("Ephedrin" + i) ?
                ItemStack.of(tag.getCompound("Ephedrin" + i)) : ItemStack.EMPTY;
            phosphorSlots[i] = tag.contains("Phosphor" + i) ?
                ItemStack.of(tag.getCompound("Phosphor" + i)) : ItemStack.EMPTY;
            jodSlots[i] = tag.contains("Jod" + i) ?
                ItemStack.of(tag.getCompound("Jod" + i)) : ItemStack.EMPTY;
            outputSlots[i] = tag.contains("Output" + i) ?
                ItemStack.of(tag.getCompound("Output" + i)) : ItemStack.EMPTY;
            mixingProgress[i] = tag.getInt("Progress" + i);
            usedPseudoephedrin[i] = tag.getBoolean("Pseudo" + i);
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
