package de.rolandsw.schedulemc.coca.blockentity;

import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.coca.CrackQuality;
import de.rolandsw.schedulemc.coca.items.CocaineItem;
import de.rolandsw.schedulemc.coca.items.CrackRockItem;
import de.rolandsw.schedulemc.coca.items.CocaItems;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
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
 * Crack-Kocher - Kocht Kokain zu Crack
 *
 * Prozess: Kokain + Backpulver + Wasser → erhitzen → Crack
 *
 * Timing-Minigame:
 * - Zu früh = schlecht (unterkokt, feucht)
 * - Perfekt = Fishscale (glänzt, beste Qualität)
 * - Zu spät = schlecht (überkokt, verbrannt)
 */
public class CrackKocherBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    // Timing window constants (for minigame)
    public static final int COOK_CYCLE_TICKS = 80;  // 4 Sekunden (4 seconds total cook time)
    public static final int PERFECT_WINDOW_START = 35;
    public static final int PERFECT_WINDOW_END = 45;
    public static final int GOOD_WINDOW_START = 28;
    public static final int GOOD_WINDOW_END = 52;

    // Score calculation constants
    private static final double PERFECT_BASE_SCORE = 1.0;
    private static final double PERFECT_DISTANCE_PENALTY = 0.05;
    private static final double PERFECT_PENALTY_DIVISOR = 10.0;
    private static final double GOOD_BASE_SCORE = 0.6;
    private static final double GOOD_BONUS_MULTIPLIER = 0.3;
    private static final double GOOD_DISTANCE_DIVISOR = 20.0;
    private static final int GOOD_CENTER_TICK = 40;
    private static final double EARLY_BASE_SCORE = 0.2;
    private static final double EARLY_PROGRESS_BONUS = 0.3;
    private static final double LATE_BASE_SCORE = 0.5;
    private static final double LATE_MIN_SCORE = 0.1;
    private static final double LATE_PENALTY_MULTIPLIER = 0.4;
    private static final double LATE_PENALTY_DIVISOR = 30.0;

    // Zutaten
    private int cocaineGrams = 0;
    private int backpulverCount = 0;
    private CocaType cocaType = CocaType.BOLIVIANISCH;
    private TobaccoQuality inputQuality = TobaccoQuality.GUT;

    // Minigame
    private boolean isMinigameActive = false;
    private int cookTick = 0;
    private boolean waitingForRemove = false;
    private UUID activePlayer = null;

    // Output
    private ItemStack outputItem = ItemStack.EMPTY;
    private double lastTimingScore = 0;

    public CrackKocherBlockEntity(BlockPos pos, BlockState state) {
        super(CocaBlockEntities.CRACK_KOCHER.get(), pos, state);
    }

    public boolean addCocaine(ItemStack stack) {
        if (!(stack.getItem() instanceof CocaineItem)) return false;
        if (isMinigameActive || !outputItem.isEmpty()) return false;

        // Max 10g pro Cook
        if (cocaineGrams >= 10) return false;

        cocaType = CocaineItem.getType(stack);
        inputQuality = CocaineItem.getQuality(stack);
        cocaineGrams += stack.getCount();

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    public boolean addBackpulver(ItemStack stack) {
        if (!stack.is(CocaItems.BACKPULVER.get())) return false;
        if (isMinigameActive || !outputItem.isEmpty()) return false;

        backpulverCount += stack.getCount();
        setChanged();
        return true;
    }

    public boolean canStartCooking() {
        return cocaineGrams >= 1 && backpulverCount >= 1 && !isMinigameActive && outputItem.isEmpty();
    }

    public boolean startCooking(UUID playerUUID) {
        if (!canStartCooking()) return false;

        activePlayer = playerUUID;
        isMinigameActive = true;
        waitingForRemove = true;
        cookTick = 0;
        lastTimingScore = 0;

        setChanged();
        return true;
    }

    /**
     * Spieler nimmt Crack aus dem Kocher
     * Timing bestimmt Qualität
     */
    public double removeCrack() {
        if (!isMinigameActive || !waitingForRemove) return 0;

        // Berechne Timing-Score
        double score;
        if (cookTick >= PERFECT_WINDOW_START && cookTick <= PERFECT_WINDOW_END) {
            // Perfekt - Fishscale!
            int perfectCenter = (PERFECT_WINDOW_START + PERFECT_WINDOW_END) / 2;
            double distanceFromPerfect = Math.abs(cookTick - perfectCenter);
            score = PERFECT_BASE_SCORE - (distanceFromPerfect / PERFECT_PENALTY_DIVISOR) * PERFECT_DISTANCE_PENALTY;
        } else if (cookTick >= GOOD_WINDOW_START && cookTick <= GOOD_WINDOW_END) {
            // Gut
            score = GOOD_BASE_SCORE + (GOOD_BONUS_MULTIPLIER * (PERFECT_BASE_SCORE - Math.abs(cookTick - GOOD_CENTER_TICK) / GOOD_DISTANCE_DIVISOR));
        } else if (cookTick < GOOD_WINDOW_START) {
            // Zu früh - unterkokt
            score = EARLY_BASE_SCORE + (cookTick / (double) GOOD_WINDOW_START) * EARLY_PROGRESS_BONUS;
        } else {
            // Zu spät - überkokt/verbrannt
            score = Math.max(LATE_MIN_SCORE, LATE_BASE_SCORE - ((cookTick - GOOD_WINDOW_END) / LATE_PENALTY_DIVISOR) * LATE_PENALTY_MULTIPLIER);
        }

        lastTimingScore = score;
        waitingForRemove = false;

        createCrack(score);

        return score;
    }

    private void createCrack(double timingScore) {
        CrackQuality quality = CrackQuality.fromTimingScore(timingScore);

        // Input-Qualität kann Ergebnis verbessern
        if (inputQuality == TobaccoQuality.LEGENDAER && quality.getLevel() < CrackQuality.FISHSCALE.getLevel()) {
            quality = CrackQuality.fromLevel(quality.getLevel() + 1);
        }

        // Crack-Gewicht: ~80% des Kokain-Gewichts
        int crackWeight = (int) (cocaineGrams * 0.8);
        crackWeight = Math.max(1, crackWeight);

        outputItem = CrackRockItem.create(cocaType, quality, crackWeight);

        // Verbrauche Zutaten
        cocaineGrams = 0;
        backpulverCount = Math.max(0, backpulverCount - 1);

        isMinigameActive = false;
        activePlayer = null;

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public ItemStack extractCrack() {
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

        if (isMinigameActive && waitingForRemove) {
            cookTick++;

            // Timeout - automatisch verbrannt
            if (cookTick >= COOK_CYCLE_TICKS) {
                removeCrack();
            }

            // Update für GUI
            if (cookTick % 2 == 0) {
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
        // Aktiv wenn das Minigame läuft
        return isMinigameActive;
    }

    public void cancelCooking() {
        isMinigameActive = false;
        waitingForRemove = false;
        cookTick = 0;
        activePlayer = null;
        setChanged();
    }

    // Getter
    public int getCocaineGrams() { return cocaineGrams; }
    public int getBackpulverCount() { return backpulverCount; }
    public boolean isMinigameActive() { return isMinigameActive; }
    public boolean isWaitingForRemove() { return waitingForRemove; }
    public int getCookTick() { return cookTick; }
    public float getCookProgress() { return (float) cookTick / COOK_CYCLE_TICKS; }
    public boolean hasOutput() { return !outputItem.isEmpty(); }
    public CocaType getCocaType() { return cocaType; }
    public double getLastTimingScore() { return lastTimingScore; }
    public UUID getActivePlayer() { return activePlayer; }

    public int getCurrentZone() {
        if (cookTick >= PERFECT_WINDOW_START && cookTick <= PERFECT_WINDOW_END) return 2; // Perfekt
        if (cookTick >= GOOD_WINDOW_START && cookTick <= GOOD_WINDOW_END) return 1;       // Gut
        if (cookTick < GOOD_WINDOW_START) return 0;                                        // Zu früh
        return 3;                                                                           // Zu spät
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("CocaineGrams", cocaineGrams);
        tag.putInt("Backpulver", backpulverCount);
        tag.putString("CocaType", cocaType.name());
        tag.putString("InputQuality", inputQuality.name());
        tag.putBoolean("MinigameActive", isMinigameActive);
        tag.putInt("CookTick", cookTick);
        tag.putBoolean("WaitingForRemove", waitingForRemove);
        tag.putDouble("LastScore", lastTimingScore);
        if (!outputItem.isEmpty()) {
            CompoundTag outputTag = new CompoundTag();
            outputItem.save(outputTag);
            tag.put("Output", outputTag);
        }
        if (activePlayer != null) {
            tag.putUUID("ActivePlayer", activePlayer);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        cocaineGrams = tag.getInt("CocaineGrams");
        backpulverCount = tag.getInt("Backpulver");
        try { cocaType = CocaType.valueOf(tag.getString("CocaType")); }
        catch (IllegalArgumentException e) { cocaType = CocaType.BOLIVIANISCH; }
        try { inputQuality = TobaccoQuality.valueOf(tag.getString("InputQuality")); }
        catch (IllegalArgumentException e) { inputQuality = TobaccoQuality.GUT; }
        isMinigameActive = tag.getBoolean("MinigameActive");
        cookTick = tag.getInt("CookTick");
        waitingForRemove = tag.getBoolean("WaitingForRemove");
        lastTimingScore = tag.getDouble("LastScore");
        outputItem = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        if (tag.hasUUID("ActivePlayer")) {
            activePlayer = tag.getUUID("ActivePlayer");
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
