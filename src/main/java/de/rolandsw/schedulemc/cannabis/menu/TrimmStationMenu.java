package de.rolandsw.schedulemc.cannabis.menu;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.blockentity.TrimmStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Menu für die Trimm-Station mit Minigame
 */
public class TrimmStationMenu extends AbstractContainerMenu {

    public final TrimmStationBlockEntity blockEntity;
    private final ContainerData data;

    private static final int DATA_CYCLE_TICK = 0;
    private static final int DATA_CYCLE_TOTAL = 1;
    private static final int DATA_LEAVES_REMOVED = 2;
    private static final int DATA_TOTAL_LEAVES = 3;
    private static final int DATA_PERFECT_TRIMS = 4;
    private static final int DATA_GOOD_TRIMS = 5;
    private static final int DATA_BAD_TRIMS = 6;
    private static final int DATA_IS_ACTIVE = 7;
    private static final int DATA_STRAIN = 8;
    private static final int DATA_SIZE = 9;

    // Server-side constructor
    public TrimmStationMenu(int containerId, Inventory playerInventory, TrimmStationBlockEntity blockEntity) {
        super(CannabisMenuTypes.TRIMM_STATION_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_CYCLE_TICK -> blockEntity.getMinigameTick() % blockEntity.getCycleTicks();
                    case DATA_CYCLE_TOTAL -> blockEntity.getCycleTicks();
                    case DATA_LEAVES_REMOVED -> blockEntity.getLeavesRemoved();
                    case DATA_TOTAL_LEAVES -> blockEntity.getTotalLeaves();
                    case DATA_PERFECT_TRIMS -> blockEntity.getPerfectTrims();
                    case DATA_GOOD_TRIMS -> blockEntity.getGoodTrims();
                    case DATA_BAD_TRIMS -> blockEntity.getBadTrims();
                    case DATA_IS_ACTIVE -> blockEntity.isMinigameActive() ? 1 : 0;
                    case DATA_STRAIN -> blockEntity.getStrain().ordinal();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Read-only
            }

            @Override
            public int getCount() {
                return DATA_SIZE;
            }
        };

        addDataSlots(this.data);
    }

    // Client-side constructor
    public TrimmStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(CannabisMenuTypes.TRIMM_STATION_MENU.get(), containerId);

        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof TrimmStationBlockEntity station) {
            this.blockEntity = station;
        } else {
            this.blockEntity = null;
        }

        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
    }

    /**
     * Startet das Minigame
     */
    public boolean startMinigame(Player player) {
        if (blockEntity != null) {
            return blockEntity.startMinigame(player.getUUID());
        }
        return false;
    }

    /**
     * Spieler klickt zum Trimmen
     */
    public int trimClick() {
        if (blockEntity != null) {
            return blockEntity.trimClick();
        }
        return -1;
    }

    // Getter
    public int getCycleTick() { return this.data.get(DATA_CYCLE_TICK); }
    public int getCycleTotal() { return this.data.get(DATA_CYCLE_TOTAL); }
    public float getCycleProgress() {
        int total = getCycleTotal();
        if (total == 0) return 0;
        return (float) getCycleTick() / total;
    }
    public int getLeavesRemoved() { return this.data.get(DATA_LEAVES_REMOVED); }
    public int getTotalLeaves() { return this.data.get(DATA_TOTAL_LEAVES); }
    public int getPerfectTrims() { return this.data.get(DATA_PERFECT_TRIMS); }
    public int getGoodTrims() { return this.data.get(DATA_GOOD_TRIMS); }
    public int getBadTrims() { return this.data.get(DATA_BAD_TRIMS); }
    public boolean isMinigameActive() { return this.data.get(DATA_IS_ACTIVE) == 1; }
    public CannabisStrain getStrain() {
        int ordinal = this.data.get(DATA_STRAIN);
        CannabisStrain[] values = CannabisStrain.values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return CannabisStrain.HYBRID;
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

    /**
     * MenuProvider für NetworkHooks.openScreen
     */
    public static class Provider implements MenuProvider {
        private final TrimmStationBlockEntity blockEntity;

        public Provider(TrimmStationBlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.literal("Trimm-Station");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
            return new TrimmStationMenu(containerId, playerInventory, blockEntity);
        }
    }
}
