package de.rolandsw.schedulemc.lsd.blockentity;

import de.rolandsw.schedulemc.lsd.BlotterDesign;
import de.rolandsw.schedulemc.lsd.LSDDosage;
import de.rolandsw.schedulemc.lsd.items.BlotterItem;
import de.rolandsw.schedulemc.lsd.items.BlotterPaperItem;
import de.rolandsw.schedulemc.lsd.items.LSDItems;
import de.rolandsw.schedulemc.lsd.items.LSDSolutionItem;
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
 * Perforations-Presse - Vierter und letzter Schritt der LSD-Herstellung
 * Träufelt LSD-Lösung auf Blotter-Papier und stanzt in Tabs
 */
public class PerforationPressBlockEntity extends BlockEntity implements IUtilityConsumer {

    private static final int PRESS_TIME = 100; // 5 Sekunden pro Batch
    private static final int TABS_PER_PAPER = 9; // 9 Tabs pro Blotter-Papier (3x3)

    private boolean lastActiveState = false;
    private ItemStack lsdSolution = ItemStack.EMPTY;
    private int blotterPaperCount = 0;
    private BlotterDesign selectedDesign = BlotterDesign.SKULL;
    private int pressProgress = 0;
    private ItemStack outputItem = ItemStack.EMPTY;
    private boolean isPressing = false;
    private long lastGameTime = -1L;

    public PerforationPressBlockEntity(BlockPos pos, BlockState state) {
        super(LSDBlockEntities.PERFORATION_PRESS.get(), pos, state);
    }

    /**
     * Fügt LSD-Lösung hinzu
     */
    public boolean addLSDSolution(ItemStack stack) {
        if (!(stack.getItem() instanceof LSDSolutionItem)) return false;
        if (!lsdSolution.isEmpty()) return false;

        lsdSolution = stack.copy();
        lsdSolution.setCount(1);
        setChanged();
        return true;
    }

    /**
     * Fügt Blotter-Papier hinzu
     */
    public boolean addBlotterPaper(ItemStack stack) {
        if (!(stack.getItem() instanceof BlotterPaperItem)) return false;
        if (blotterPaperCount >= 16) return false;

        blotterPaperCount = Math.min(blotterPaperCount + 1, 16);
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
        if (lsdSolution.isEmpty() || blotterPaperCount <= 0 || isPressing || !outputItem.isEmpty()) {
            return false;
        }

        int charges = LSDSolutionItem.getCharges(lsdSolution);
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
        if (!PlotUtilityManager.areUtilitiesEnabled(getBlockPos())) return;

        long now = level.getDayTime();
        long ticksPassed = (lastGameTime < 0) ? 1L : Math.max(0L, now - lastGameTime);
        lastGameTime = now;

        if (isPressing) {
            int prevProgress = pressProgress;
            pressProgress = (int) Math.min((long) pressProgress + ticksPassed, PRESS_TIME);

            if (pressProgress >= PRESS_TIME) {
                // Pressen abgeschlossen
                LSDDosage dosage = LSDSolutionItem.getDosage(lsdSolution);
                int micrograms = LSDSolutionItem.getMicrograms(lsdSolution);
                int charges = LSDSolutionItem.getCharges(lsdSolution);

                // Berechne wie viele Blotter wir machen können
                int possibleFromPaper = blotterPaperCount * TABS_PER_PAPER;
                int possibleFromCharges = charges;
                int actualTabs = Math.min(possibleFromPaper, possibleFromCharges);

                // Verbrauche Ressourcen
                int paperUsed = (int) Math.ceil((double) actualTabs / TABS_PER_PAPER);
                blotterPaperCount -= paperUsed;

                int chargesUsed = actualTabs;
                int remainingCharges = charges - chargesUsed;

                if (remainingCharges > 0) {
                    LSDSolutionItem.setCharges(lsdSolution, remainingCharges);
                } else {
                    lsdSolution = ItemStack.EMPTY;
                }

                // Erstelle Output
                outputItem = BlotterItem.create(dosage, micrograms, selectedDesign, actualTabs);

                isPressing = false;
                pressProgress = 0;

                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            } else if (pressProgress / 20 > prevProgress / 20) {
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

    // Getter
    public boolean isPressing() { return isPressing; }
    public boolean hasOutput() { return !outputItem.isEmpty(); }
    public boolean hasSolution() { return !lsdSolution.isEmpty(); }
    public boolean hasPaper() { return blotterPaperCount > 0; }
    public int getBlotterPaperCount() { return blotterPaperCount; }
    public BlotterDesign getSelectedDesign() { return selectedDesign; }
    public ItemStack getLsdSolution() { return lsdSolution; }
    public float getProgress() { return (float) pressProgress / PRESS_TIME; }

    public int getExpectedTabs() {
        if (lsdSolution.isEmpty() || blotterPaperCount <= 0) return 0;
        int fromPaper = blotterPaperCount * TABS_PER_PAPER;
        int fromCharges = LSDSolutionItem.getCharges(lsdSolution);
        return Math.min(fromPaper, fromCharges);
    }

    @Override
    public boolean isActivelyConsuming() {
        return isPressing;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!lsdSolution.isEmpty()) {
            CompoundTag solutionTag = new CompoundTag();
            lsdSolution.save(solutionTag);
            tag.put("LSDLoesung", solutionTag);
        }
        tag.putInt("BlotterPaper", blotterPaperCount);
        tag.putString("Design", selectedDesign.name());
        tag.putInt("Progress", pressProgress);
        tag.putBoolean("Pressing", isPressing);
        tag.putLong("LastGameTime", lastGameTime);
        if (!outputItem.isEmpty()) {
            CompoundTag outputTag = new CompoundTag();
            outputItem.save(outputTag);
            tag.put("Output", outputTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lsdSolution = tag.contains("LSDLoesung") ? ItemStack.of(tag.getCompound("LSDLoesung")) : ItemStack.EMPTY;
        blotterPaperCount = tag.getInt("BlotterPaper");
        if (tag.contains("Design")) {
            try {
                selectedDesign = BlotterDesign.valueOf(tag.getString("Design"));
            } catch (IllegalArgumentException e) {
                selectedDesign = BlotterDesign.SKULL;
            }
        }
        pressProgress = tag.getInt("Progress");
        isPressing = tag.getBoolean("Pressing");
        lastGameTime = tag.contains("LastGameTime") ? tag.getLong("LastGameTime") : -1L;
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
