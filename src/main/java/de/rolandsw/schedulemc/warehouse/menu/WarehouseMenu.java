package de.rolandsw.schedulemc.warehouse.menu;

import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Warehouse Menu Container - Tab-basiertes Management Panel
 */
public class WarehouseMenu extends AbstractContainerMenu {

    private final WarehouseBlockEntity warehouseBE;
    private final Level level;
    private final BlockPos pos;

    // SERVER-SIDE Konstruktor (mit BlockEntity)
    public WarehouseMenu(int id, Inventory playerInventory, WarehouseBlockEntity warehouseBE, BlockPos pos) {
        super(WarehouseMenuTypes.WAREHOUSE_MENU.get(), id);
        this.warehouseBE = warehouseBE;
        this.level = playerInventory.player.level();
        this.pos = pos;
    }

    // CLIENT-SIDE Konstruktor (ohne BlockEntity)
    public WarehouseMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(WarehouseMenuTypes.WAREHOUSE_MENU.get(), id);

        // Lese BlockPos vom Netzwerk
        this.pos = extraData.readBlockPos();
        this.level = playerInventory.player.level();
        BlockEntity be = level.getBlockEntity(pos);
        this.warehouseBE = be instanceof WarehouseBlockEntity ? (WarehouseBlockEntity) be : null;  // NOPMD
    }

    public WarehouseBlockEntity getWarehouse() {
        return warehouseBE;
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Kein Inventar-Transfer nötig
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (warehouseBE == null || warehouseBE.isRemoved()) {
            return false;
        }

        return stillValid(
            ContainerLevelAccess.create(level, pos),
            player,
            WarehouseBlocks.WAREHOUSE.get()
        );
    }
}
