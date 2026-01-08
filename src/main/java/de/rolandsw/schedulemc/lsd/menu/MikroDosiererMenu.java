package de.rolandsw.schedulemc.lsd.menu;

import de.rolandsw.schedulemc.lsd.LSDDosage;
import de.rolandsw.schedulemc.lsd.blockentity.MikroDosiererBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Container-Menu für den Mikro-Dosierer
 * Mit Dosierungs-Slider (50-300 μg)
 */
public class MikroDosiererMenu extends AbstractContainerMenu {

    public final MikroDosiererBlockEntity blockEntity;
    private final ContainerData data;

    private static final int DATA_SLIDER = 0;
    private static final int DATA_MICROGRAMS = 1;
    private static final int DATA_LYSERGSAEURE = 2;
    private static final int DATA_PROGRESS = 3;
    private static final int DATA_IS_PROCESSING = 4;
    private static final int DATA_DOSAGE_LEVEL = 5;
    private static final int DATA_SIZE = 6;

    // Server-side constructor
    public MikroDosiererMenu(int containerId, Inventory playerInventory, MikroDosiererBlockEntity blockEntity) {
        super(LSDMenuTypes.MIKRO_DOSIERER_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_SLIDER -> blockEntity.getDosageSlider();
                    case DATA_MICROGRAMS -> blockEntity.getCurrentMicrograms();
                    case DATA_LYSERGSAEURE -> blockEntity.getLysergsaeureCount();
                    case DATA_PROGRESS -> (int) (blockEntity.getProgress() * 100);
                    case DATA_IS_PROCESSING -> blockEntity.isProcessing() ? 1 : 0;
                    case DATA_DOSAGE_LEVEL -> blockEntity.getCurrentDosage().getLevel();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == DATA_SLIDER) {
                    blockEntity.setDosageSlider(value);
                }
            }

            @Override
            public int getCount() {
                return DATA_SIZE;
            }
        };

        addDataSlots(this.data);
    }

    // Client-side constructor
    public MikroDosiererMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(LSDMenuTypes.MIKRO_DOSIERER_MENU.get(), containerId);

        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof MikroDosiererBlockEntity dosierer) {
            this.blockEntity = dosierer;
        } else {
            this.blockEntity = null;
        }

        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
    }

    /**
     * Setzt den Slider-Wert (0-100)
     */
    public void setSliderValue(int value) {
        this.data.set(DATA_SLIDER, value);
    }

    /**
     * Startet den Prozess
     */
    public boolean startProcess() {
        if (blockEntity != null) {
            return blockEntity.startProcess();
        }
        return false;
    }

    // Getter
    public int getSliderValue() {
        return this.data.get(DATA_SLIDER);
    }

    public int getMicrograms() {
        return this.data.get(DATA_MICROGRAMS);
    }

    public int getLysergsaeureCount() {
        return this.data.get(DATA_LYSERGSAEURE);
    }

    public int getProgressPercent() {
        return this.data.get(DATA_PROGRESS);
    }

    public boolean isProcessing() {
        return this.data.get(DATA_IS_PROCESSING) == 1;
    }

    public LSDDosage getCurrentDosage() {
        return LSDDosage.fromLevel(this.data.get(DATA_DOSAGE_LEVEL));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null && !blockEntity.isRemoved() &&
                player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                        blockEntity.getBlockPos().getY() + 0.5,
                        blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }
}
