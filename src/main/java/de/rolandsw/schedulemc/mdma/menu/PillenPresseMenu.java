package de.rolandsw.schedulemc.mdma.menu;

import de.rolandsw.schedulemc.mdma.MDMAQuality;
import de.rolandsw.schedulemc.mdma.PillColor;
import de.rolandsw.schedulemc.mdma.PillDesign;
import de.rolandsw.schedulemc.mdma.blockentity.PillenPresseBlockEntity;
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
 * Container-Menu für die Pillen-Presse
 * Mit Timing-Minigame Daten
 */
public class PillenPresseMenu extends AbstractContainerMenu {

    public final PillenPresseBlockEntity blockEntity;
    private final ContainerData data;

    private static final int DATA_MINIGAME_TICK = 0;
    private static final int DATA_CYCLE_TICKS = 1;
    private static final int DATA_IS_ACTIVE = 2;
    private static final int DATA_WAITING_PRESS = 3;
    private static final int DATA_KRISTALL_COUNT = 4;
    private static final int DATA_BINDEMITTEL_COUNT = 5;
    private static final int DATA_DESIGN = 6;
    private static final int DATA_COLOR = 7;
    private static final int DATA_LAST_SCORE = 8;
    private static final int DATA_ZONE = 9;
    private static final int DATA_SIZE = 10;

    // Server-side constructor
    public PillenPresseMenu(int containerId, Inventory playerInventory, PillenPresseBlockEntity blockEntity) {
        super(MDMAMenuTypes.PILLEN_PRESSE_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_MINIGAME_TICK -> blockEntity.getMinigameTick();
                    case DATA_CYCLE_TICKS -> blockEntity.getPressCycleTicks();
                    case DATA_IS_ACTIVE -> blockEntity.isMinigameActive() ? 1 : 0;
                    case DATA_WAITING_PRESS -> blockEntity.isWaitingForPress() ? 1 : 0;
                    case DATA_KRISTALL_COUNT -> blockEntity.getKristallCount();
                    case DATA_BINDEMITTEL_COUNT -> blockEntity.getBindemittelCount();
                    case DATA_DESIGN -> blockEntity.getSelectedDesign().ordinal();
                    case DATA_COLOR -> blockEntity.getSelectedColor().ordinal();
                    case DATA_LAST_SCORE -> (int) (blockEntity.getLastTimingScore() * 100);
                    case DATA_ZONE -> blockEntity.getCurrentZone();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Read-only für Client
            }

            @Override
            public int getCount() {
                return DATA_SIZE;
            }
        };

        addDataSlots(this.data);
    }

    // Client-side constructor
    public PillenPresseMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(MDMAMenuTypes.PILLEN_PRESSE_MENU.get(), containerId);

        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof PillenPresseBlockEntity presse) {
            this.blockEntity = presse;
        } else {
            this.blockEntity = null;
        }

        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
    }

    /**
     * Spieler drückt den Press-Button
     */
    public double pressButton() {
        if (blockEntity != null) {
            return blockEntity.pressButton();
        }
        return 0;
    }

    // Getter
    public int getMinigameTick() {
        return this.data.get(DATA_MINIGAME_TICK);
    }

    public int getCycleTicks() {
        return this.data.get(DATA_CYCLE_TICKS);
    }

    public float getProgress() {
        int cycle = getCycleTicks();
        if (cycle == 0) return 0;
        return (float) getMinigameTick() / cycle;
    }

    public boolean isMinigameActive() {
        return this.data.get(DATA_IS_ACTIVE) == 1;
    }

    public boolean isWaitingForPress() {
        return this.data.get(DATA_WAITING_PRESS) == 1;
    }

    public int getKristallCount() {
        return this.data.get(DATA_KRISTALL_COUNT);
    }

    public int getBindemittelCount() {
        return this.data.get(DATA_BINDEMITTEL_COUNT);
    }

    public PillDesign getSelectedDesign() {
        int ordinal = this.data.get(DATA_DESIGN);
        PillDesign[] values = PillDesign.values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return PillDesign.TESLA;
    }

    public PillColor getSelectedColor() {
        int ordinal = this.data.get(DATA_COLOR);
        PillColor[] values = PillColor.values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return PillColor.PINK;
    }

    public double getLastTimingScore() {
        return this.data.get(DATA_LAST_SCORE) / 100.0;
    }

    public int getCurrentZone() {
        return this.data.get(DATA_ZONE);
    }

    public MDMAQuality getExpectedQuality() {
        return MDMAQuality.fromTimingScore(getProgress());
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
