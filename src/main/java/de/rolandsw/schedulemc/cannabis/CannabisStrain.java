package de.rolandsw.schedulemc.cannabis;

import de.rolandsw.schedulemc.production.core.ProductionType;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import net.minecraft.network.chat.Component;

/**
 * Cannabis-Sorten mit unterschiedlichen Eigenschaften
 */
public enum CannabisStrain implements ProductionType {
    // Farbe, Preis, Wachstum, THC%, CBD%, Ertrag, BlüteZeit
    INDICA("§5", 25.0, 120, 22.0, 1.0, 6, 56),           // Entspannend, hoher Ertrag
    SATIVA("§a", 30.0, 160, 18.0, 0.5, 6, 70),           // Energetisch, länger
    HYBRID("§e", 35.0, 140, 20.0, 2.0, 6, 63),           // Ausgewogen
    AUTOFLOWER("§b", 20.0, 70, 15.0, 3.0, 6, 42);        // Schnell, weniger Ertrag

    private final String colorCode;
    private final double seedPrice;
    private final int growthTicks;      // Wachstums-Ticks (vegetative Phase)
    private final double thcContent;    // THC-Gehalt in %
    private final double cbdContent;    // CBD-Gehalt in %
    private final int baseYield;        // Basis-Ertrag in Gramm
    private final int floweringDays;    // Blütezeit in Minecraft-Tagen

    CannabisStrain(String colorCode, double seedPrice,
                   int growthTicks, double thcContent, double cbdContent,
                   int baseYield, int floweringDays) {
        this.colorCode = colorCode;
        this.seedPrice = seedPrice;
        this.growthTicks = growthTicks;
        this.thcContent = thcContent;
        this.cbdContent = cbdContent;
        this.baseYield = baseYield;
        this.floweringDays = floweringDays;
    }

    public String getDisplayName() {
        return Component.translatable("enum.cannabis_strain." + this.name().toLowerCase()).getString();
    }
    public String getColorCode() { return colorCode; }
    public String getColoredName() { return colorCode + getDisplayName(); }
    public double getSeedPrice() { return seedPrice; }
    public int getGrowthTicks() { return growthTicks; }
    public double getThcContent() { return thcContent; }
    public double getCbdContent() { return cbdContent; }
    public int getBaseYield() { return baseYield; }
    public int getFloweringDays() { return floweringDays; }
    public String getRegistryName() { return name().toLowerCase(); }

    @Override
    public double getBasePrice() {
        return seedPrice / 10.0; // Base price per gram
    }

    /**
     * Berechnet den Preis basierend auf THC-Gehalt und Qualität
     */
    public double calculatePrice(CannabisQuality quality) {
        double basePrice = seedPrice * 2; // Verkaufspreis höher als Einkauf
        double thcBonus = thcContent / 10.0; // THC beeinflusst Preis
        return basePrice * thcBonus * quality.getPriceMultiplier();
    }

    @Override
    public double calculatePrice(ProductionQuality quality, int amount) {
        double basePrice = seedPrice * 2;
        double thcBonus = thcContent / 10.0;
        return basePrice * thcBonus * quality.getPriceMultiplier() * amount;
    }

    /**
     * Gibt die Effekt-Beschreibung der Sorte zurück
     */
    public String getEffectDescription() {
        return switch (this) {
            case INDICA -> "§7" + Component.translatable("enum.cannabis_strain.desc.indica").getString();
            case SATIVA -> "§7" + Component.translatable("enum.cannabis_strain.desc.sativa").getString();
            case HYBRID -> "§7" + Component.translatable("enum.cannabis_strain.desc.hybrid").getString();
            case AUTOFLOWER -> "§7" + Component.translatable("enum.cannabis_strain.desc.autoflower").getString();
        };
    }
}
