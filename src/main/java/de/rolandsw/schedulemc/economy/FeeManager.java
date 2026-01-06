package de.rolandsw.schedulemc.economy;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Verwaltet Transaktionsgebühren und leitet sie an die Staatskasse weiter
 */
public class FeeManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    // Konfigurierbare Gebühren
    private static final double ATM_FEE = 5.0; // 5€ pro ATM-Transaktion
    private static final double TRANSFER_FEE_PERCENTAGE = 0.01; // 1% des Betrags
    private static final double MIN_TRANSFER_FEE = 10.0; // Mindestens 10€

    /**
     * Berechnet ATM-Gebühr
     */
    public static double getATMFee() {
        return ATM_FEE;
    }

    /**
     * Berechnet Transfer-Gebühr basierend auf Betrag
     */
    public static double getTransferFee(double amount) {
        double fee = amount * TRANSFER_FEE_PERCENTAGE;
        return Math.max(fee, MIN_TRANSFER_FEE);
    }

    /**
     * Zieht ATM-Gebühr ab und transferiert an Staatskasse
     * @return true wenn erfolgreich, false wenn nicht genug Guthaben
     */
    public static boolean chargeATMFee(@Nonnull UUID playerUUID, @Nonnull MinecraftServer server) {
        if (EconomyManager.withdraw(playerUUID, ATM_FEE, TransactionType.ATM_FEE, "ATM-Gebühr")) {
            StateAccount.getInstance(server).deposit(ATM_FEE, "ATM-Gebühr");
            LOGGER.debug("ATM-Gebühr {} € von {} abgezogen", ATM_FEE, playerUUID);
            return true;
        }
        return false;
    }

    /**
     * Zieht Transfer-Gebühr ab und transferiert an Staatskasse
     * @return true wenn erfolgreich, false wenn nicht genug Guthaben
     */
    public static boolean chargeTransferFee(@Nonnull UUID playerUUID, double transferAmount, @Nonnull MinecraftServer server) {
        double fee = getTransferFee(transferAmount);
        if (EconomyManager.withdraw(playerUUID, fee, TransactionType.TRANSFER_FEE,
                String.format("Transfer-Gebühr (%.2f%%)", TRANSFER_FEE_PERCENTAGE * 100))) {
            StateAccount.getInstance(server).deposit(fee, "Transfer-Gebühr");
            LOGGER.debug("Transfer-Gebühr {} € von {} abgezogen", fee, playerUUID);
            return true;
        }
        return false;
    }

    /**
     * Prüft ob Spieler sich ATM-Gebühr leisten kann
     */
    public static boolean canAffordATMFee(@Nonnull UUID playerUUID) {
        return EconomyManager.getBalance(playerUUID) >= ATM_FEE;
    }

    /**
     * Prüft ob Spieler sich Transfer + Gebühr leisten kann
     */
    public static boolean canAffordTransfer(@Nonnull UUID playerUUID, double transferAmount) {
        double fee = getTransferFee(transferAmount);
        double totalCost = transferAmount + fee;
        return EconomyManager.getBalance(playerUUID) >= totalCost;
    }

    /**
     * Gibt Gebühren-Info als formatierten Text zurück
     */
    public static String getFeeInfo() {
        return String.format(
            "§e§lGEBÜHREN-ÜBERSICHT\n" +
            "§7ATM-Gebühr: §c%.2f€\n" +
            "§7Transfer-Gebühr: §c%.1f%% §7(min. %.2f€)",
            ATM_FEE,
            TRANSFER_FEE_PERCENTAGE * 100,
            MIN_TRANSFER_FEE
        );
    }
}
