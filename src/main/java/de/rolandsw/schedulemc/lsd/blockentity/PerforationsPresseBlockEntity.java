package de.rolandsw.schedulemc.lsd.blockentity;

import de.rolandsw.schedulemc.lsd.BlotterDesign;
import de.rolandsw.schedulemc.lsd.LSDDosage;
import de.rolandsw.schedulemc.lsd.items.BlotterItem;
import de.rolandsw.schedulemc.lsd.items.BlotterPapierItem;
import de.rolandsw.schedulemc.lsd.items.LSDItems;
import de.rolandsw.schedulemc.lsd.items.LSDLoesungItem;
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
 * Perforations-Presse - Vierter und letzter Schritt der LSD-Herstellung
 * Träufelt LSD-Lösung auf Blotter-Papier und stanzt in Tabs
 */
public class PerforationsPresseBlockEntity extends BlockEntity {

    private static final int PRESS_TIME = 100; // 5 Sekunden pro Batch
    private static final int TABS_PER_PAPER = 9; // 9 Tabs pro Blotter-Papier (3x3)

    private ItemStack lsdLoesung = ItemStack.EMPTY;
    private int blotterPapierCount = 0;
    private BlotterDesign selectedDesign = BlotterDesign.TOTENKOPF;
    private int pressProgress = 0;
    private ItemStack outputItem = ItemStack.EMPTY;
    private boolean isPressing = false;

    public PerforationsPresseBlockEntity(BlockPos pos, BlockState state) {
        super(LSDBlockEntities.PERFORATIONS_PRESSE.get(), pos, state);
    }

    /**
     * Fügt LSD-Lösung hinzu
     */
    public boolean addLSDLoesung(ItemStack stack) {
        if (!(stack.getItem() instanceof LSDLoesungItem)) return false;
        if (!lsdLoesung.isEmpty()) return false;

        lsdLoesung = stack.copy();
        lsdLoesung.setCount(1);
        setChanged();
        return true;
    }

    /**
     * Fügt Blotter-Papier hinzu
     */
    public boolean addBlotterPapier(ItemStack stack) {
        if (!(stack.getItem() instanceof BlotterPapierItem)) return false;
        if (blotterPapierCount >= 16) return false;

        blotterPapierCount++;
        setChanged();
        return true;
    }

    /**
     * Setzt das Blotter-Design
     */
    public void setDesign(BlotterDesign design) {
        this.selectedDesign = design;
        setChanged();
    }

    /**
     * Wechselt zum nächsten Design
     */
    public void cycleDesign() {
        int next = (selectedDesign.ordinal() + 1) % BlotterDesign.values().length;
        selectedDesign = BlotterDesign.values()[next];
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Startet Pressvorgang
     */
    public boolean startPressing() {
        if (lsdLoesung.isEmpty() || blotterPapierCount <= 0 || isPressing || !outputItem.isEmpty()) {
            return false;
        }

        int charges = LSDLoesungItem.getCharges(lsdLoesung);
        if (charges <= 0) return false;

        isPressing = true;
        pressProgress = 0;
        setChanged();
        return true;
    }

    /**
     * Extrahiert fertige Blotter
     */
    public ItemStack extractOutput() {
        if (outputItem.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = outputItem.copy();
        outputItem = ItemStack.EMPTY;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return result;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (isPressing) {
            pressProgress++;

            if (pressProgress >= PRESS_TIME) {
                // Pressen abgeschlossen
                LSDDosage dosage = LSDLoesungItem.getDosage(lsdLoesung);
                int micrograms = LSDLoesungItem.getMicrograms(lsdLoesung);
                int charges = LSDLoesungItem.getCharges(lsdLoesung);

                // Berechne wie viele Blotter wir machen können
                int possibleFromPaper = blotterPapierCount * TABS_PER_PAPER;
                int possibleFromCharges = charges;
                int actualTabs = Math.min(possibleFromPaper, possibleFromCharges);

                // Verbrauche Ressourcen
                int paperUsed = (int) Math.ceil((double) actualTabs / TABS_PER_PAPER);
                blotterPapierCount -= paperUsed;

                int chargesUsed = actualTabs;
                int remainingCharges = charges - chargesUsed;

                if (remainingCharges > 0) {
                    LSDLoesungItem.setCharges(lsdLoesung, remainingCharges);
                } else {
                    lsdLoesung = ItemStack.EMPTY;
                }

                // Erstelle Output
                outputItem = BlotterItem.create(dosage, micrograms, selectedDesign, actualTabs);

                isPressing = false;
                pressProgress = 0;

                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            } else if (pressProgress % 20 == 0) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // Getter
    public boolean isPressing() { return isPressing; }
    public boolean hasOutput() { return !outputItem.isEmpty(); }
    public boolean hasLoesung() { return !lsdLoesung.isEmpty(); }
    public boolean hasPapier() { return blotterPapierCount > 0; }
    public int getBlotterPapierCount() { return blotterPapierCount; }
    public BlotterDesign getSelectedDesign() { return selectedDesign; }
    public ItemStack getLsdLoesung() { return lsdLoesung; }
    public float getProgress() { return (float) pressProgress / PRESS_TIME; }

    public int getExpectedTabs() {
        if (lsdLoesung.isEmpty() || blotterPapierCount <= 0) return 0;
        int fromPaper = blotterPapierCount * TABS_PER_PAPER;
        int fromCharges = LSDLoesungItem.getCharges(lsdLoesung);
        return Math.min(fromPaper, fromCharges);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!lsdLoesung.isEmpty()) {
            CompoundTag loesungTag = new CompoundTag();
            lsdLoesung.save(loesungTag);
            tag.put("LSDLoesung", loesungTag);
        }
        tag.putInt("BlotterPapier", blotterPapierCount);
        tag.putString("Design", selectedDesign.name());
        tag.putInt("Progress", pressProgress);
        tag.putBoolean("Pressing", isPressing);
        if (!outputItem.isEmpty()) {
            CompoundTag outputTag = new CompoundTag();
            outputItem.save(outputTag);
            tag.put("Output", outputTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lsdLoesung = tag.contains("LSDLoesung") ? ItemStack.of(tag.getCompound("LSDLoesung")) : ItemStack.EMPTY;
        blotterPapierCount = tag.getInt("BlotterPapier");
        if (tag.contains("Design")) {
            try {
                selectedDesign = BlotterDesign.valueOf(tag.getString("Design"));
            } catch (IllegalArgumentException e) {
                selectedDesign = BlotterDesign.TOTENKOPF;
            }
        }
        pressProgress = tag.getInt("Progress");
        isPressing = tag.getBoolean("Pressing");
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
