package de.rolandsw.schedulemc.cannabis.blockentity;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.items.DriedBudItem;
import de.rolandsw.schedulemc.cannabis.items.TrimmedBudItem;
import de.rolandsw.schedulemc.cannabis.items.TrimItem;
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

import java.util.UUID;

/**
 * Trimm-Station mit Minigame
 * Spieler muss Blätter entfernen - je besser, desto höhere Qualität
 */
public class TrimmStationBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    // Minigame Konstanten
    public static final int TRIM_CYCLE_TICKS = 100; // 5 Sekunden pro Zyklus
    public static final int LEAVES_TO_TRIM = 10;    // Anzahl der zu entfernenden Blätter
    public static final int PERFECT_WINDOW = 5;     // Ticks für perfektes Timing

    private ItemStack inputItem = ItemStack.EMPTY;
    private CannabisStrain strain = CannabisStrain.HYBRID;
    private CannabisQuality baseQuality = CannabisQuality.MIDS;
    private int weight = 0;

    private boolean isMinigameActive = false;
    private int minigameTick = 0;
    private int leavesRemoved = 0;
    private int perfectTrims = 0;
    private int goodTrims = 0;
    private int badTrims = 0;

    private UUID activePlayer = null;
    private ItemStack outputBud = ItemStack.EMPTY;
    private ItemStack outputTrim = ItemStack.EMPTY;

    public TrimmStationBlockEntity(BlockPos pos, BlockState state) {
        super(CannabisBlockEntities.TRIMM_STATION.get(), pos, state);
    }

    public boolean addDriedBud(ItemStack stack) {
        if (!(stack.getItem() instanceof DriedBudItem)) return false;
        if (!inputItem.isEmpty() || isMinigameActive) return false;

        strain = DriedBudItem.getStrain(stack);
        baseQuality = DriedBudItem.getQuality(stack);
        weight = DriedBudItem.getWeight(stack);
        inputItem = stack.copy();

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    public boolean startMinigame(UUID playerUUID) {
        if (inputItem.isEmpty() || isMinigameActive || !outputBud.isEmpty()) {
            return false;
        }

        activePlayer = playerUUID;
        isMinigameActive = true;
        minigameTick = 0;
        leavesRemoved = 0;
        perfectTrims = 0;
        goodTrims = 0;
        badTrims = 0;

        setChanged();
        return true;
    }

    /**
     * Spieler klickt zum Trimmen
     * Gibt Timing-Qualität zurück: 0 = schlecht, 1 = gut, 2 = perfekt
     */
    public int trimClick() {
        if (!isMinigameActive) return -1;

        int cyclePosition = minigameTick % TRIM_CYCLE_TICKS;
        int perfectCenter = TRIM_CYCLE_TICKS / 2;

        int result;
        if (Math.abs(cyclePosition - perfectCenter) <= PERFECT_WINDOW) {
            // Perfekt!
            perfectTrims++;
            result = 2;
        } else if (Math.abs(cyclePosition - perfectCenter) <= PERFECT_WINDOW * 3) {
            // Gut
            goodTrims++;
            result = 1;
        } else {
            // Schlecht
            badTrims++;
            result = 0;
        }

        leavesRemoved++;

        // Prüfe ob fertig
        if (leavesRemoved >= LEAVES_TO_TRIM) {
            finishTrimming();
        }

        setChanged();
        return result;
    }

    private void finishTrimming() {
        // Berechne Gesamtqualität basierend auf Trim-Performance
        double score = (perfectTrims * 1.0 + goodTrims * 0.6 + badTrims * 0.2) / LEAVES_TO_TRIM;
        CannabisQuality finalQuality = CannabisQuality.fromTrimScore(score);

        // Wenn Base-Qualität besser, behalte sie
        if (baseQuality.getLevel() > finalQuality.getLevel()) {
            finalQuality = baseQuality;
        }

        // Erstelle Output
        int trimmedWeight = (int) (weight * 0.8); // 20% Verlust durch Trimmen
        int trimWeight = weight - trimmedWeight;

        outputBud = TrimmedBudItem.create(strain, finalQuality, trimmedWeight);
        outputTrim = TrimItem.create(strain, trimWeight);

        inputItem = ItemStack.EMPTY;
        isMinigameActive = false;
        activePlayer = null;

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public ItemStack extractTrimmedBud() {
        if (outputBud.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = outputBud.copy();
        outputBud = ItemStack.EMPTY;
        setChanged();
        return result;
    }

    public ItemStack extractTrim() {
        if (outputTrim.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = outputTrim.copy();
        outputTrim = ItemStack.EMPTY;
        setChanged();
        return result;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (isMinigameActive) {
            minigameTick++;

            // Update für GUI
            if (minigameTick % 2 == 0) {
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
        return isMinigameActive;
    }

    public void cancelMinigame() {
        isMinigameActive = false;
        activePlayer = null;
        minigameTick = 0;
        leavesRemoved = 0;
        setChanged();
    }

    // Getter
    public boolean hasInput() { return !inputItem.isEmpty(); }
    public boolean isMinigameActive() { return isMinigameActive; }
    public boolean hasOutput() { return !outputBud.isEmpty(); }
    public int getMinigameTick() { return minigameTick; }
    public int getCycleTicks() { return TRIM_CYCLE_TICKS; }
    public float getCycleProgress() { return (float) (minigameTick % TRIM_CYCLE_TICKS) / TRIM_CYCLE_TICKS; }
    public int getLeavesRemoved() { return leavesRemoved; }
    public int getTotalLeaves() { return LEAVES_TO_TRIM; }
    public int getPerfectTrims() { return perfectTrims; }
    public int getGoodTrims() { return goodTrims; }
    public int getBadTrims() { return badTrims; }
    public CannabisStrain getStrain() { return strain; }
    public UUID getActivePlayer() { return activePlayer; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!inputItem.isEmpty()) {
            CompoundTag inputTag = new CompoundTag();
            inputItem.save(inputTag);
            tag.put("Input", inputTag);
        }
        tag.putString("Strain", strain.name());
        tag.putString("Quality", baseQuality.name());
        tag.putInt("Weight", weight);
        tag.putBoolean("MinigameActive", isMinigameActive);
        tag.putInt("MinigameTick", minigameTick);
        tag.putInt("LeavesRemoved", leavesRemoved);
        tag.putInt("PerfectTrims", perfectTrims);
        tag.putInt("GoodTrims", goodTrims);
        tag.putInt("BadTrims", badTrims);
        if (!outputBud.isEmpty()) {
            CompoundTag outputTag = new CompoundTag();
            outputBud.save(outputTag);
            tag.put("OutputBud", outputTag);
        }
        if (!outputTrim.isEmpty()) {
            CompoundTag trimTag = new CompoundTag();
            outputTrim.save(trimTag);
            tag.put("OutputTrim", trimTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inputItem = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        try { strain = CannabisStrain.valueOf(tag.getString("Strain")); }
        catch (IllegalArgumentException e) { strain = CannabisStrain.HYBRID; }
        try { baseQuality = CannabisQuality.valueOf(tag.getString("Quality")); }
        catch (IllegalArgumentException e) { baseQuality = CannabisQuality.MIDS; }
        weight = tag.getInt("Weight");
        isMinigameActive = tag.getBoolean("MinigameActive");
        minigameTick = tag.getInt("MinigameTick");
        leavesRemoved = tag.getInt("LeavesRemoved");
        perfectTrims = tag.getInt("PerfectTrims");
        goodTrims = tag.getInt("GoodTrims");
        badTrims = tag.getInt("BadTrims");
        outputBud = tag.contains("OutputBud") ? ItemStack.of(tag.getCompound("OutputBud")) : ItemStack.EMPTY;
        outputTrim = tag.contains("OutputTrim") ? ItemStack.of(tag.getCompound("OutputTrim")) : ItemStack.EMPTY;
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
