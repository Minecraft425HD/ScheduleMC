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
import de.rolandsw.schedulemc.utility.PlotUtilityManager;

public class CrackCookerBlockEntity extends BlockEntity implements IUtilityConsumer {

    private boolean lastActiveState = false;

    public static final int COOK_CYCLE_TICKS = 80;
    public static final int PERFECT_WINDOW_START = 35;
    public static final int PERFECT_WINDOW_END = 45;
    public static final int GOOD_WINDOW_START = 28;
    public static final int GOOD_WINDOW_END = 52;

    private int cocaineGrams = 0;
    private int backpulverCount = 0;
    private CocaType cocaType = CocaType.BOLIVIANISCH;
    private TobaccoQuality inputQuality = TobaccoQuality.GUT;

    private boolean isMinigameActive = false;
    private long startCookTime = -1L;
    private boolean waitingForRemove = false;
    private UUID activePlayer = null;

    private ItemStack outputItem = ItemStack.EMPTY;
    private double lastTimingScore = 0;

    public CrackCookerBlockEntity(BlockPos pos, BlockState state) {
        super(CocaBlockEntities.CRACK_KOCHER.get(), pos, state);
    }

    public boolean addCocaine(ItemStack stack) {
        if (!(stack.getItem() instanceof CocaineItem)) return false;
        if (isMinigameActive || !outputItem.isEmpty()) return false;

        if (cocaineGrams >= 10) return false;

        cocaType = CocaineItem.getType(stack);
        inputQuality = CocaineItem.getQuality(stack);
        cocaineGrams += stack.getCount();

        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
        return true;
    }

    public boolean addBackpulver(ItemStack stack) {
        if (!stack.is(CocaItems.BACKPULVER.get())) return false;
        if (isMinigameActive || !outputItem.isEmpty()) return false;

        backpulverCount += stack.getCount();
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
        return true;
    }

    public boolean canStartCooking() {
        return cocaineGrams >= 1 && backpulverCount >= 1 && !isMinigameActive && outputItem.isEmpty();
    }

    public boolean startCooking(UUID playerUUID) {
        if (!canStartCooking()) return false;
        if (level == null) return false;

        activePlayer = playerUUID;
        isMinigameActive = true;
        waitingForRemove = true;
        startCookTime = level.getDayTime();
        lastTimingScore = 0;

        setChanged();
        return true;
    }

    public double removeCrack() {
        if (!isMinigameActive || !waitingForRemove) return 0;

        int tick = getCookTick();
        double score;
        if (tick >= PERFECT_WINDOW_START && tick <= PERFECT_WINDOW_END) {
            int perfectCenter = (PERFECT_WINDOW_START + PERFECT_WINDOW_END) / 2;
            double distanceFromPerfect = Math.abs(tick - perfectCenter);
            score = 1.0 - (distanceFromPerfect / 10.0) * 0.05;
        } else if (tick >= GOOD_WINDOW_START && tick <= GOOD_WINDOW_END) {
            score = 0.6 + (0.3 * (1.0 - Math.abs(tick - 40) / 20.0));
        } else if (tick < GOOD_WINDOW_START) {
            score = 0.2 + (tick / (double) GOOD_WINDOW_START) * 0.3;
        } else {
            score = Math.max(0.1, 0.5 - ((tick - GOOD_WINDOW_END) / 30.0) * 0.4);
        }

        lastTimingScore = score;
        waitingForRemove = false;

        createCrack(score);

        return score;
    }

    private void createCrack(double timingScore) {
        CrackQuality quality = CrackQuality.fromTimingScore(timingScore);

        if (inputQuality == TobaccoQuality.LEGENDAER && quality.getLevel() < CrackQuality.LEGENDAER.getLevel()) {
            quality = CrackQuality.fromLevel(quality.getLevel() + 1);
        }

        int crackWeight = (int) (cocaineGrams * 0.8);
        crackWeight = Math.max(1, crackWeight);

        outputItem = CrackRockItem.create(cocaType, quality, crackWeight);

        cocaineGrams = 0;
        backpulverCount = Math.max(0, backpulverCount - 1);

        isMinigameActive = false;
        startCookTime = -1L;
        activePlayer = null;

        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    public ItemStack extractCrack() {
        if (outputItem.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = outputItem.copy();
        outputItem = ItemStack.EMPTY;

        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
        return result;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;
        if (!PlotUtilityManager.areUtilitiesEnabled(getBlockPos())) return;

        if (isMinigameActive && waitingForRemove) {
            int elapsed = getCookTick();

            if (elapsed >= COOK_CYCLE_TICKS) {
                removeCrack();
            }

            if (level.getGameTime() % 2 == 0) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

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

    public void cancelCooking() {
        isMinigameActive = false;
        waitingForRemove = false;
        startCookTime = -1L;
        activePlayer = null;
        setChanged();
    }

    // Getters
    public int getCocaineGrams() { return cocaineGrams; }
    public int getBackpulverCount() { return backpulverCount; }
    public boolean isMinigameActive() { return isMinigameActive; }
    public boolean isWaitingForRemove() { return waitingForRemove; }

    public int getCookTick() {
        if (!isMinigameActive || startCookTime < 0 || level == null) return 0;
        return (int) Math.min(level.getDayTime() - startCookTime, COOK_CYCLE_TICKS);
    }

    public float getCookProgress() { return (float) getCookTick() / COOK_CYCLE_TICKS; }
    public boolean hasOutput() { return !outputItem.isEmpty(); }
    public CocaType getCocaType() { return cocaType; }
    public double getLastTimingScore() { return lastTimingScore; }
    public UUID getActivePlayer() { return activePlayer; }

    public int getCurrentZone() {
        int tick = getCookTick();
        if (tick >= PERFECT_WINDOW_START && tick <= PERFECT_WINDOW_END) return 2;
        if (tick >= GOOD_WINDOW_START && tick <= GOOD_WINDOW_END) return 1;
        if (tick < GOOD_WINDOW_START) return 0;
        return 3;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("CocaineGrams", cocaineGrams);
        tag.putInt("Backpulver", backpulverCount);
        tag.putString("CocaType", cocaType.name());
        tag.putString("InputQuality", inputQuality.name());
        tag.putBoolean("MinigameActive", isMinigameActive);
        tag.putLong("StartCookTime", startCookTime);
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
        startCookTime = tag.contains("StartCookTime") ? tag.getLong("StartCookTime") : -1L;
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
