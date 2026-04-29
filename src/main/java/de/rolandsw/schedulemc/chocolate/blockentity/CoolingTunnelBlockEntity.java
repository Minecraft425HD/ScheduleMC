package de.rolandsw.schedulemc.chocolate.blockentity;

import de.rolandsw.schedulemc.production.blockentity.AbstractItemHandlerBlockEntity;
import de.rolandsw.schedulemc.utility.IUtilityConsumer;
import de.rolandsw.schedulemc.utility.UtilityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import de.rolandsw.schedulemc.utility.PlotUtilityManager;

/**
 * Cooling Tunnel - Kühlt Schokolade schnell ab
 *
 * Input: Beliebige Schokoladen-Items
 * Output: Gekühlte Schokoladen-Items
 * Processing Time: 200 Ticks (10 Sekunden)
 *
 * Funktion:
 * - Beschleunigt den Abkühlprozess
 * - Setzt "Cooled" NBT-Tag
 * - Wichtig für Qualitätserhalt bei temperierten Produkten
 * - Verhindert "Bloom" (weiße Verfärbung)
 */
public class CoolingTunnelBlockEntity extends AbstractItemHandlerBlockEntity implements IUtilityConsumer, MenuProvider {
    private boolean lastActiveState = false;

    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;
    private int coolingProgress = 0;
    private long lastGameTime = -1L;

    private static final int PROCESSING_TIME = 200; // 10 seconds

    public CoolingTunnelBlockEntity(BlockPos pos, BlockState state) {
        super(ChocolateBlockEntities.COOLING_TUNNEL.get(), pos, state);
        createItemHandler();
    }

    private void createItemHandler() {
        itemHandler = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == 0) syncInputFromHandler();
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == 0) {
                    // Accept any chocolate-related item that has NBT data
                    return stack.hasTag();
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot == 1) return super.extractItem(slot, amount, simulate);
                if (slot == 0 && coolingProgress == 0) return super.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY;
            }
        };
    }

    private void syncInputFromHandler() {
        ItemStack handlerInput = itemHandler.getStackInSlot(0);
        if (!handlerInput.isEmpty() && inputStack.isEmpty()) {
            inputStack = handlerInput.copy();
            coolingProgress = 0;
        } else if (handlerInput.isEmpty()) {
            inputStack = ItemStack.EMPTY;
            coolingProgress = 0;
        } else {
            inputStack = handlerInput.copy();
        }
    }

    private void syncToHandler() {
        itemHandler.setStackInSlot(0, inputStack.copy());
        itemHandler.setStackInSlot(1, outputStack.copy());
    }

    public int getCoolingProgressValue() {
        return coolingProgress;
    }

    public int getTotalCoolingTime() {
        return PROCESSING_TIME;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;
        if (!PlotUtilityManager.areUtilitiesEnabled(getBlockPos())) return;

        boolean changed = false;

        long now = level.getDayTime();
        long ticksPassed = (lastGameTime < 0) ? 1L : Math.max(0L, now - lastGameTime);
        lastGameTime = now;
        if (ticksPassed == 0) return;

        if (!inputStack.isEmpty() && outputStack.isEmpty()) {
            int prevProgress = coolingProgress;
            coolingProgress = (int) Math.min((long) coolingProgress + ticksPassed, PROCESSING_TIME);

            if (coolingProgress >= PROCESSING_TIME) {
                // Cooling complete: Add "Cooled" tag
                ItemStack cooledItem = inputStack.copy();

                CompoundTag tag = cooledItem.getOrCreateTag();
                tag.putBoolean("Cooled", true);
                if (level != null) {
                    tag.putLong("CoolingTime", level.getGameTime());
                }

                outputStack = cooledItem;
                inputStack = ItemStack.EMPTY;
                coolingProgress = 0;
                changed = true;
            } else if (coolingProgress / 20 > prevProgress / 20) {
                changed = true;
            }
        }

        if (changed) {
            syncToHandler();
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        boolean currentActive = isActivelyConsuming();
        if (currentActive != lastActiveState) {
            lastActiveState = currentActive;
            UtilityEventHandler.reportBlockEntityActivity(this, currentActive);
        }
    }

    @Override
    public boolean isActivelyConsuming() {
        return !inputStack.isEmpty() && outputStack.isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!inputStack.isEmpty()) tag.put("Input", inputStack.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
        tag.putInt("Progress", coolingProgress);
        tag.putLong("LastGameTime", lastGameTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (itemHandler == null) createItemHandler();
        inputStack = tag.contains("Input") ? ItemStack.of(tag.getCompound("Input")) : ItemStack.EMPTY;
        outputStack = tag.contains("Output") ? ItemStack.of(tag.getCompound("Output")) : ItemStack.EMPTY;
        coolingProgress = tag.getInt("Progress");
        lastGameTime = tag.contains("LastGameTime") ? tag.getLong("LastGameTime") : -1L;
        syncToHandler();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.schedulemc.cooling_tunnel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
        return new de.rolandsw.schedulemc.chocolate.menu.CoolingTunnelMenu(id, inv, this);
    }
}
