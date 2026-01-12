package de.rolandsw.schedulemc.towing;

import de.rolandsw.schedulemc.config.ModConfigHandler;

/**
 * Membership tiers for the towing service
 * Determines coverage percentage and monthly fees
 */
public enum MembershipTier {
    NONE(0, 0, 0.0, "none"),
    BRONZE(1, ModConfigHandler.VEHICLE_SERVER.membershipBronzeCoveragePercent::get, ModConfigHandler.VEHICLE_SERVER.membershipBronzeFee::get, "bronze"),
    SILVER(2, ModConfigHandler.VEHICLE_SERVER.membershipSilverCoveragePercent::get, ModConfigHandler.VEHICLE_SERVER.membershipSilverFee::get, "silver"),
    GOLD(3, ModConfigHandler.VEHICLE_SERVER.membershipGoldCoveragePercent::get, ModConfigHandler.VEHICLE_SERVER.membershipGoldFee::get, "gold");

    private final int level;
    private final java.util.function.Supplier<Integer> coveragePercentSupplier;
    private final java.util.function.Supplier<Double> monthlyFeeSupplier;
    private final String translationKey;

    MembershipTier(int level, int coveragePercent, double monthlyFee, String translationKey) {
        this.level = level;
        this.coveragePercentSupplier = () -> coveragePercent;
        this.monthlyFeeSupplier = () -> monthlyFee;
        this.translationKey = translationKey;
    }

    MembershipTier(int level, java.util.function.Supplier<Integer> coveragePercentSupplier,
                   java.util.function.Supplier<Double> monthlyFeeSupplier, String translationKey) {
        this.level = level;
        this.coveragePercentSupplier = coveragePercentSupplier;
        this.monthlyFeeSupplier = monthlyFeeSupplier;
        this.translationKey = translationKey;
    }

    /**
     * Gets the percentage of towing costs covered by this tier
     */
    public int getCoveragePercent() {
        return coveragePercentSupplier.get();
    }

    /**
     * Gets the monthly fee for this tier
     */
    public double getMonthlyFee() {
        return monthlyFeeSupplier.get();
    }

    /**
     * Gets the level (higher = better)
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets the translation key for this tier
     */
    public String getTranslationKey() {
        return "towing.membership.tier." + translationKey;
    }

    /**
     * Calculates the player cost after coverage
     */
    public double calculatePlayerCost(double totalCost) {
        if (this == NONE) {
            return totalCost;
        }
        double coveragePercent = getCoveragePercent() / 100.0;
        return totalCost * (1.0 - coveragePercent);
    }

    /**
     * Gets tier by ordinal (safe)
     */
    public static MembershipTier fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            return NONE;
        }
        return values()[ordinal];
    }
}
