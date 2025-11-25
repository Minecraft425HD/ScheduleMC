package de.rolandsw.schedulemc.economy.blockentity;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.items.CashItem;
import de.rolandsw.schedulemc.economy.menu.ATMMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Geldautomat BlockEntity (KORREKT - deposit/withdraw)
 */
public class ATMBlockEntity extends BlockEntity implements MenuProvider {
    
    public ATMBlockEntity(BlockPos pos, BlockState state) {
        super(de.rolandsw.schedulemc.economy.blocks.EconomyBlocks.ATM_BLOCK_ENTITY.get(), pos, state);
    }
    
    /**
     * Zahlt Geld vom Konto in die Geldbörse aus
     */
    public boolean withdraw(Player player, double amount) {
        if (amount <= 0) return false;
        
        double balance = EconomyManager.getBalance(player.getUUID());
        if (balance < amount) {
            player.displayClientMessage(Component.literal(
                "§c✗ Nicht genug Geld auf dem Konto!\n" +
                "§7Kontostand: §e" + String.format("%.2f€", balance)
            ), false);
            return false;
        }
        
        // Finde Geldbörse in Slot 8
        ItemStack wallet = player.getInventory().getItem(8);
        if (!(wallet.getItem() instanceof CashItem)) {
            player.displayClientMessage(Component.literal(
                "§c✗ Keine Geldbörse in Slot 9 gefunden!"
            ), false);
            return false;
        }
        
        // Transaktion durchführen - withdraw gibt boolean zurück!
        if (EconomyManager.withdraw(player.getUUID(), amount)) {
            CashItem.addValue(wallet, amount);
            
            player.displayClientMessage(Component.literal(
                "§a✓ Auszahlung erfolgreich!\n" +
                "§7Ausgezahlt: §e" + String.format("%.2f€", amount) + "\n" +
                "§7Neuer Kontostand: §e" + String.format("%.2f€", EconomyManager.getBalance(player.getUUID()))
            ), false);
            
            player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            return true;
        }
        
        return false;
    }
    
    /**
     * Zahlt Geld aus der Geldbörse auf das Konto ein
     */
    public boolean deposit(Player player, double amount) {
        if (amount <= 0) return false;
        
        // Finde Geldbörse in Slot 8
        ItemStack wallet = player.getInventory().getItem(8);
        if (!(wallet.getItem() instanceof CashItem)) {
            player.displayClientMessage(Component.literal(
                "§c✗ Keine Geldbörse in Slot 9 gefunden!"
            ), false);
            return false;
        }
        
        double walletBalance = CashItem.getValue(wallet);
        if (walletBalance < amount) {
            player.displayClientMessage(Component.literal(
                "§c✗ Nicht genug Bargeld in der Geldbörse!\n" +
                "§7Bargeld: §e" + String.format("%.2f€", walletBalance)
            ), false);
            return false;
        }
        
        // Transaktion durchführen - deposit gibt void zurück
        CashItem.removeValue(wallet, amount);
        EconomyManager.deposit(player.getUUID(), amount);
        
        player.displayClientMessage(Component.literal(
            "§a✓ Einzahlung erfolgreich!\n" +
            "§7Eingezahlt: §e" + String.format("%.2f€", amount) + "\n" +
            "§7Neuer Kontostand: §e" + String.format("%.2f€", EconomyManager.getBalance(player.getUUID()))
        ), false);
        
        player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        return true;
    }
    
    /**
     * Gibt Kontostand zurück
     */
    public double getBalance(Player player) {
        return EconomyManager.getBalance(player.getUUID());
    }
    
    /**
     * Gibt Bargeld in Geldbörse zurück
     */
    public double getWalletBalance(Player player) {
        ItemStack wallet = player.getInventory().getItem(8);
        if (wallet.getItem() instanceof CashItem) {
            return CashItem.getValue(wallet);
        }
        return 0.0;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.literal("§6§lGELDAUTOMAT");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ATMMenu(id, playerInventory, this, worldPosition);
    }
}
