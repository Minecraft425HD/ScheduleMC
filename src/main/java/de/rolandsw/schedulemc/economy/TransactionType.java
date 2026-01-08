package de.rolandsw.schedulemc.economy;

import net.minecraft.network.chat.Component;

/**
 * Typen von Transaktionen im Economy-System
 */
public enum TransactionType {
    TRANSFER(Component.translatable("enum.transaction_type.transfer").getString()),
    ATM_DEPOSIT(Component.translatable("enum.transaction_type.atm_deposit").getString()),
    ATM_WITHDRAW(Component.translatable("enum.transaction_type.atm_withdraw").getString()),
    SHOP_PAYOUT(Component.translatable("enum.transaction_type.shop_payout").getString()),
    DEATH_FEE(Component.translatable("enum.transaction_type.death_fee").getString()),
    ADMIN_SET(Component.translatable("enum.transaction_type.admin_set").getString()),
    ADMIN_GIVE(Component.translatable("enum.transaction_type.admin_give").getString()),
    ADMIN_TAKE(Component.translatable("enum.transaction_type.admin_take").getString()),
    NPC_PURCHASE(Component.translatable("enum.transaction_type.npc_purchase").getString()),
    VEHICLE_PURCHASE(Component.translatable("enum.transaction_type.vehicle_purchase").getString()),
    GARAGE_FEE(Component.translatable("enum.transaction_type.garage_fee").getString()),
    SHOP_INVESTMENT(Component.translatable("enum.transaction_type.shop_investment").getString()),
    SHOP_DIVESTMENT(Component.translatable("enum.transaction_type.shop_divestment").getString()),
    ATM_FEE(Component.translatable("enum.transaction_type.atm_fee").getString()),
    TRANSFER_FEE(Component.translatable("enum.transaction_type.transfer_fee").getString()),
    TAX_INCOME(Component.translatable("enum.transaction_type.tax_income").getString()),
    TAX_SALES(Component.translatable("enum.transaction_type.tax_sales").getString()),
    TAX_PROPERTY(Component.translatable("enum.transaction_type.tax_property").getString()),
    INTEREST(Component.translatable("enum.transaction_type.interest").getString()),
    INTEREST_SAVINGS(Component.translatable("enum.transaction_type.interest_savings").getString()),
    LOAN_DISBURSEMENT(Component.translatable("enum.transaction_type.loan_disbursement").getString()),
    LOAN_REPAYMENT(Component.translatable("enum.transaction_type.loan_repayment").getString()),
    LOAN_INTEREST(Component.translatable("enum.transaction_type.loan_interest").getString()),
    OVERDRAFT_FEE(Component.translatable("enum.transaction_type.overdraft_fee").getString()),
    BOND_PURCHASE(Component.translatable("enum.transaction_type.bond_purchase").getString()),
    BOND_MATURITY(Component.translatable("enum.transaction_type.bond_maturity").getString()),
    INSURANCE_PAYMENT(Component.translatable("enum.transaction_type.insurance_payment").getString()),
    INSURANCE_PAYOUT(Component.translatable("enum.transaction_type.insurance_payout").getString()),
    STATE_SUBSIDY(Component.translatable("enum.transaction_type.state_subsidy").getString()),
    STATE_SPENDING(Component.translatable("enum.transaction_type.state_spending").getString()),
    SAVINGS_DEPOSIT(Component.translatable("enum.transaction_type.savings_deposit").getString()),
    SAVINGS_WITHDRAW(Component.translatable("enum.transaction_type.savings_withdraw").getString()),
    DAILY_REWARD(Component.translatable("enum.transaction_type.daily_reward").getString()),
    PLOT_PURCHASE(Component.translatable("enum.transaction_type.plot_purchase").getString()),
    PLOT_SALE(Component.translatable("enum.transaction_type.plot_sale").getString()),
    PLOT_RENT(Component.translatable("enum.transaction_type.plot_rent").getString()),
    OTHER(Component.translatable("enum.transaction_type.other").getString());

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
