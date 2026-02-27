package de.rolandsw.schedulemc.cannabis.menu;

import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
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
 * Menu für die Trimm-Station.
 * Kein Minigame – Spieler klickt 5× auf "Trimmen".
 */
public class TrimmStationMenu extends AbstractContainerMenu {

    public final TrimmStationBlockEntity blockEntity;
    private final ContainerData data;

    private static final int DATA_CLICK_COUNT = 0;
    private static final int DATA_STRAIN      = 1;
    private static final int DATA_QUALITY     = 2;
    private static final int DATA_SIZE        = 3;

    public static final int BUTTON_TRIM = 0;

    // Server-side constructor
    public TrimmStationMenu(int containerId, Inventory playerInventory, TrimmStationBlockEntity blockEntity) {
        super(CannabisMenuTypes.TRIMM_STATION_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_CLICK_COUNT -> blockEntity.getClickCount();
                    case DATA_STRAIN      -> blockEntity.getLastStrain().ordinal();
                    case DATA_QUALITY     -> blockEntity.getLastQuality().ordinal();
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) { }
            @Override public int getCount() { return DATA_SIZE; }
        };

        addDataSlots(this.data);
    }

    // Client-side constructor
    public TrimmStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(CannabisMenuTypes.TRIMM_STATION_MENU.get(), containerId);

        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        this.blockEntity = be instanceof TrimmStationBlockEntity station ? station : null;

        this.data = new SimpleContainerData(DATA_SIZE);
        addDataSlots(this.data);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_TRIM && blockEntity != null && !blockEntity.isRemoved()) {
            return blockEntity.doTrimClick(player);
        }
        return false;
    }

    // Getter
    public int getClickCount() { return this.data.get(DATA_CLICK_COUNT); }

    public CannabisStrain getLastStrain() {
        int ordinal = this.data.get(DATA_STRAIN);
        CannabisStrain[] values = CannabisStrain.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : CannabisStrain.HYBRID;
    }

    public CannabisQuality getLastQuality() {
        int ordinal = this.data.get(DATA_QUALITY);
        CannabisQuality[] values = CannabisQuality.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : CannabisQuality.GUT;
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

    public static class Provider implements MenuProvider {
        private final TrimmStationBlockEntity blockEntity;

        public Provider(TrimmStationBlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable("gui.trimm_station.menu_title");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
            return new TrimmStationMenu(containerId, playerInventory, blockEntity);
        }
    }
}
