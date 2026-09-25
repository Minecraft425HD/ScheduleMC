package de.rolandsw.schedulemc.meth;

import de.rolandsw.schedulemc.economy.ItemCategory;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.production.core.ProductionType;

/**
 * Meth-Reinheitsstufen als eigenständige UDPS-Produkte.
 *
 * Anders als Cannabis/Tabak/Koka/Mohn hat Meth keine anbaubare Sorte (keine "Variant"-NBT auf
 * verpackten Meth-Items) - stattdessen bestimmt die {@link MethQuality} (Verarbeitungsergebnis
 * im ReductionKettle) direkt, welches der 3 UDPS-Produkte gilt. Jedes hat einen eigenen
 * Basispreis und damit einen eigenen Supply&Demand-Zustand in DynamicPriceManager.
 */
public enum MethVariant implements ProductionType {
    STANDARD("Standard", "§7", 30.0),
    GOOD("Good", "§e", 50.0),
    BLUE_SKY("Blue Sky", "§b", 80.0);

    private final String displayName;
    private final String colorCode;
    private final double basePrice;

    MethVariant(String displayName, String colorCode, double basePrice) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.basePrice = basePrice;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getColorCode() {
        return colorCode;
    }

    @Override
    public double getBasePrice() {
        return basePrice;
    }

    @Override
    public int getGrowthTicks() {
        return 0; // Meth wird synthetisiert, nicht angebaut
    }

    @Override
    public int getBaseYield() {
        return 1;
    }

    @Override
    public String getProductId() {
        return "METH_" + name();
    }

    @Override
    public ItemCategory getItemCategory() {
        return ItemCategory.METH;
    }

    @Override
    public double calculatePrice(ProductionQuality quality, int amount) {
        return basePrice * quality.getPriceMultiplier() * amount;
    }

    /**
     * Ordnet eine {@link MethQuality} dem passenden UDPS-Produkt zu.
     * POOR fällt auf STANDARD zurück (kein eigenes, noch günstigeres Produkt registriert).
     */
    public static MethVariant fromQuality(MethQuality quality) {
        return switch (quality) {
            case POOR, GOOD -> STANDARD;
            case VERY_GOOD -> GOOD;
            case LEGENDARY -> BLUE_SKY;
        };
    }
}
