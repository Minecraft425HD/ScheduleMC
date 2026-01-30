package de.rolandsw.schedulemc.economy;

import net.minecraft.network.chat.Component;

/**
 * Typen von Transaktionen im Economy-System
 */
public enum TransactionType {
    TRANSFER,
    ATM_DEPOSIT,
    ATM_WITHDRAW,
    SHOP_PAYOUT,
    DEATH_FEE,
    ADMIN_SET,
    ADMIN_GIVE,
    ADMIN_TAKE,
    NPC_PURCHASE,
    VEHICLE_PURCHASE,
    WERKSTATT_FEE,
    SHOP_INVESTMENT,
    SHOP_DIVESTMENT,
    ATM_FEE,
    TRANSFER_FEE,
    TAX_INCOME,
    TAX_SALES,
    TAX_PROPERTY,
    INTEREST,
    INTEREST_SAVINGS,
    LOAN_DISBURSEMENT,
    LOAN_REPAYMENT,
    LOAN_INTEREST,
    OVERDRAFT_FEE,
    BOND_PURCHASE,
    BOND_MATURITY,
    INSURANCE_PAYMENT,
    INSURANCE_PAYOUT,
    STATE_SUBSIDY,
    STATE_SPENDING,
    SAVINGS_DEPOSIT,
    SAVINGS_WITHDRAW,
    DAILY_REWARD,
    PLOT_PURCHASE,
    PLOT_SALE,
    PLOT_RENT,
    OVERDRAFT_REPAY_WALLET,
    OVERDRAFT_REPAY_SAVINGS,
    PRISON_DEBT_CLEARED,
    OTHER;

    public String getDisplayName() {
        return Component.translatable("enum.transaction_type." + this.name().toLowerCase()).getString();
    }
}
