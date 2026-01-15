package de.rolandsw.schedulemc.economy.blockentity;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.FeeManager;
import de.rolandsw.schedulemc.economy.StateAccount;
import de.rolandsw.schedulemc.economy.TransactionType;
import de.rolandsw.schedulemc.economy.WalletManager;
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

        double atmFee = FeeManager.getATMFee();
        double totalCost = amount + atmFee;
        double balance = EconomyManager.getBalance(player.getUUID());

        if (balance < totalCost) {
            player.displayClientMessage(Component.translatable(
                "block.atm.insufficient_funds",
                String.format("%.2f€", balance),
                String.format("%.2f€", amount),
                String.format("%.2f€", atmFee)
            ), false);
            return false;
        }

        // Prüfe ob Geldbörse in Slot 8 vorhanden ist
        ItemStack wallet = player.getInventory().getItem(8);
        if (!(wallet.getItem() instanceof CashItem)) {
            player.displayClientMessage(Component.translatable(
                "block.atm.no_wallet"
            ), false);
            return false;
        }

        // Transaktion durchführen
        // 1. Ziehe Betrag + Gebühr vom Konto ab (totalCost)
        if (EconomyManager.withdraw(player.getUUID(), totalCost, TransactionType.ATM_WITHDRAW, "ATM-Auszahlung inkl. Gebühr")) {
            // 2. Gib nur den Betrag (ohne Gebühr) ins Wallet (WalletManager!)
            WalletManager.addMoney(player.getUUID(), amount);

            // 3. Überweise Gebühr an Staatskasse
            if (level != null && !level.isClientSide()) {
                StateAccount.getInstance(level.getServer()).deposit(atmFee, "ATM-Gebühr");
            }

            player.displayClientMessage(Component.translatable(
                "block.atm.withdraw_success",
                String.format("%.2f€", amount),
                String.format("%.2f€", atmFee),
                String.format("%.2f€", EconomyManager.getBalance(player.getUUID()))
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

        // Prüfe ob Geldbörse in Slot 8 vorhanden ist
        ItemStack wallet = player.getInventory().getItem(8);
        if (!(wallet.getItem() instanceof CashItem)) {
            player.displayClientMessage(Component.translatable(
                "block.atm.no_wallet"
            ), false);
            return false;
        }

        double atmFee = FeeManager.getATMFee();
        double walletBalance = WalletManager.getBalance(player.getUUID());
        if (walletBalance < amount) {
            player.displayClientMessage(Component.translatable(
                "block.atm.insufficient_cash_in_wallet",
                String.format("%.2f€", walletBalance)
            ), false);
            return false;
        }

        // Transaktion durchführen
        // 1. Entferne Bargeld aus Wallet (WalletManager!)
        if (!WalletManager.removeMoney(player.getUUID(), amount)) {
            return false; // Sollte nicht passieren, aber sicherheitshalber
        }

        // 2. Zahle auf Konto ein (voller Betrag)
        EconomyManager.deposit(player.getUUID(), amount, TransactionType.ATM_DEPOSIT, "ATM-Einzahlung");

        // 3. Ziehe Gebühr vom Konto ab (jetzt ist garantiert genug Geld da)
        if (level != null && !level.isClientSide()) {
            FeeManager.chargeATMFee(player.getUUID(), level.getServer());
        }

        player.displayClientMessage(Component.translatable(
            "block.atm.deposit_success",
            String.format("%.2f€", amount),
            String.format("%.2f€", atmFee),
            String.format("%.2f€", EconomyManager.getBalance(player.getUUID()))
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
        // Bargeld wird im WalletManager gespeichert, nicht im ItemStack
        return WalletManager.getBalance(player.getUUID());
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.atm.name");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ATMMenu(id, playerInventory, this, worldPosition);
    }
}
