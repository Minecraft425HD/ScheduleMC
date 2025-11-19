package de.rolandsw.schedulemc.economy.menu;

import de.rolandsw.schedulemc.economy.blockentity.ATMBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * ATM Menu Container - FINAL FIXED VERSION
 */
public class ATMMenu extends AbstractContainerMenu {
    
    private final ATMBlockEntity atmBE;
    private final Level level;
    private final BlockPos pos;
    
    // SERVER-SIDE Konstruktor (mit BlockEntity)
    public ATMMenu(int id, Inventory playerInventory, ATMBlockEntity atmBE, BlockPos pos) {
        super(EconomyMenuTypes.ATM_MENU.get(), id);
        this.atmBE = atmBE;
        this.level = playerInventory.player.level();
        this.pos = pos;
    }
    
    // CLIENT-SIDE Konstruktor (ohne BlockEntity) - FIXED!
    public ATMMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(EconomyMenuTypes.ATM_MENU.get(), id);
        
        // Lese BlockPos NUR EINMAL!
        this.pos = extraData.readBlockPos();
        this.level = playerInventory.player.level();
        this.atmBE = (ATMBlockEntity) level.getBlockEntity(pos);
    }
    
    public ATMBlockEntity getATM() {
        return atmBE;
    }
    
    public BlockPos getBlockPos() {
        return pos;
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        if (atmBE == null || atmBE.isRemoved()) {
            return false;
        }
        
        return stillValid(
            net.minecraft.world.inventory.ContainerLevelAccess.create(level, pos),
            player,
            de.rolandsw.schedulemc.economy.blocks.EconomyBlocks.ATM_BLOCK.get()
        );
    }
}
