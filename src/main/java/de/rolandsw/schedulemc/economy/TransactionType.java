package de.rolandsw.schedulemc.economy;

/**
 * Typen von Transaktionen im Economy-System
 */
public enum TransactionType {
    TRANSFER("Transfer"),
    ATM_DEPOSIT("ATM Einzahlung"),
    ATM_WITHDRAW("ATM Abhebung"),
    SHOP_PAYOUT("Shop Auszahlung"),
    DEATH_FEE("Krankenhaus-Gebühr"),
    ADMIN_SET("Admin: Setzung"),
    ADMIN_GIVE("Admin: Hinzufügen"),
    ADMIN_TAKE("Admin: Entfernen"),
    NPC_PURCHASE("NPC Kauf"),
    VEHICLE_PURCHASE("Fahrzeugkauf"),
    GARAGE_FEE("Garagen-Gebühr"),
    SHOP_INVESTMENT("Shop-Investment"),
    SHOP_DIVESTMENT("Shop-Verkauf"),
    ATM_FEE("ATM-Gebühr"),
    TRANSFER_FEE("Transfer-Gebühr"),
    TAX_INCOME("Einkommenssteuer"),
    TAX_SALES("Umsatzsteuer"),
    TAX_PROPERTY("Grundsteuer"),
    INTEREST("Kontozinsen"),
    INTEREST_SAVINGS("Sparkonto-Zinsen"),
    LOAN_DISBURSEMENT("Kredit-Auszahlung"),
    LOAN_REPAYMENT("Kredit-Rückzahlung"),
    LOAN_INTEREST("Kredit-Zinsen"),
    OVERDRAFT_FEE("Überziehungszinsen"),
    BOND_PURCHASE("Anleihen-Kauf"),
    BOND_MATURITY("Anleihen-Fälligkeit"),
    INSURANCE_PAYMENT("Versicherungs-Beitrag"),
    INSURANCE_PAYOUT("Versicherungs-Auszahlung"),
    STATE_SUBSIDY("Staatliche Subvention"),
    STATE_SPENDING("Staatliche Ausgabe"),
    SAVINGS_DEPOSIT("Sparkonto Einzahlung"),
    SAVINGS_WITHDRAW("Sparkonto Abhebung"),
    DAILY_REWARD("Tägliche Belohnung"),
    PLOT_PURCHASE("Grundstücks-Kauf"),
    PLOT_SALE("Grundstücks-Verkauf"),
    PLOT_RENT("Grundstücks-Miete"),
    OTHER("Sonstiges");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
