package de.rolandsw.schedulemc.production.core;

import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Generic Quality System - Vereinheitlicht alle Quality-Enums
 *
 * Ersetzt:
 * - TobaccoQuality (4 Tiers)
 * - CannabisQuality (5 Tiers)
 * - MDMAQuality (4 Tiers)
 *
 * Unterstützt variable Tier-Counts und konfigurierbare Multiplier
 */
public class GenericQuality implements ProductionQuality {

    // ═══════════════════════════════════════════════════════════
    // QUALITY DATA
    // ═══════════════════════════════════════════════════════════

    private final String displayName;
    private final String colorCode;
    private final int level;
    private final double priceMultiplier;
    private final String description;
    private final GenericQuality[] allTiers;  // Reference to all tiers
    private final int tierIndex;              // Index in allTiers array

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    private GenericQuality(String displayName, String colorCode, int level,
                          double priceMultiplier, String description,
                          GenericQuality[] allTiers, int tierIndex) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
        this.description = description;
        this.allTiers = allTiers;
        this.tierIndex = tierIndex;
    }

    // ═══════════════════════════════════════════════════════════
    // FACTORY METHODS - PREDEFINED SYSTEMS
    // ═══════════════════════════════════════════════════════════

    /**
     * Standard 4-Tier System (Tobacco, Coca, Poppy, MDMA)
     */
    public static GenericQuality[] createStandard4TierSystem() {
        GenericQuality[] tiers = new GenericQuality[4];

        tiers[0] = new GenericQuality(
            Component.translatable("enum.generic_quality.schlecht").getString(), "§c", 0, 0.7,
            Component.translatable("enum.generic_quality.desc.niedrige_qualitaet_mit_verunreinigungen").getString(),
            tiers, 0
        );
        tiers[1] = new GenericQuality(
            Component.translatable("enum.generic_quality.gut").getString(), "§e", 1, 1.0,
            Component.translatable("enum.generic_quality.desc.solide_qualitaet_fuer_den_standardmarkt").getString(),
            tiers, 1
        );
        tiers[2] = new GenericQuality(
            Component.translatable("enum.generic_quality.sehr_gut").getString(), "§a", 2, 1.5,
            Component.translatable("enum.generic_quality.desc.hohe_qualitaet_mit_konsistenten_eigenschaften").getString(),
            tiers, 2
        );
        tiers[3] = new GenericQuality(
            Component.translatable("enum.generic_quality.legendaer").getString(), "§6", 3, 2.5,
            Component.translatable("enum.generic_quality.desc.aussergewoehnliche_qualitaet_premium_produkt").getString(),
            tiers, 3
        );

        return tiers;
    }

    /**
     * Cannabis 5-Tier System
     */
    public static GenericQuality[] createCannabis5TierSystem() {
        GenericQuality[] tiers = new GenericQuality[5];

        tiers[0] = new GenericQuality(
            Component.translatable("enum.generic_quality.schwag").getString(), "§8", 0, 0.5,
            Component.translatable("enum.generic_quality.desc.minderwertiges_cannabis_mit_staengeln_und_samen").getString(),
            tiers, 0
        );
        tiers[1] = new GenericQuality(
            Component.translatable("enum.generic_quality.mids").getString(), "§7", 1, 1.0,
            Component.translatable("enum.generic_quality.desc.durchschnittliche_qualitaet").getString(),
            tiers, 1
        );
        tiers[2] = new GenericQuality(
            Component.translatable("enum.generic_quality.dank").getString(), "§e", 2, 1.8,
            Component.translatable("enum.generic_quality.desc.gute_qualitaet_mit_starker_potenz").getString(),
            tiers, 2
        );
        tiers[3] = new GenericQuality(
            Component.translatable("enum.generic_quality.top_shelf").getString(), "§a", 3, 3.0,
            Component.translatable("enum.generic_quality.desc.premium_qualitaet").getString(),
            tiers, 3
        );
        tiers[4] = new GenericQuality(
            Component.translatable("enum.generic_quality.exotic").getString(), "§d", 4, 5.0,
            Component.translatable("enum.generic_quality.desc.aussergewoehnliche_qualitaet_connoisseur_grade").getString(),
            tiers, 4
        );

        return tiers;
    }

    /**
     * Custom Quality System mit konfigurierbaren Tiers
     *
     * @param tierCount Anzahl der Tiers (2-10)
     * @param baseMultiplier Start-Multiplier (z.B. 0.5)
     * @param maxMultiplier Max-Multiplier (z.B. 5.0)
     * @return Array mit GenericQuality-Instanzen
     */
    public static GenericQuality[] createCustomTierSystem(int tierCount, double baseMultiplier, double maxMultiplier) {
        if (tierCount < 2 || tierCount > 10) {
            throw new IllegalArgumentException("Tier count must be between 2 and 10");
        }

        GenericQuality[] tiers = new GenericQuality[tierCount];

        // Standard Color Progression
        String[] colors = {"§c", "§e", "§a", "§2", "§b", "§d", "§5", "§6", "§l§6", "§k§l§6"};

        // Standard Name Progression
        String[] names = {
            Component.translatable("enum.generic_quality.schlecht").getString(),
            Component.translatable("enum.generic_quality.befriedigend").getString(),
            Component.translatable("enum.generic_quality.gut").getString(),
            Component.translatable("enum.generic_quality.sehr_gut").getString(),
            Component.translatable("enum.generic_quality.ausgezeichnet").getString(),
            Component.translatable("enum.generic_quality.premium").getString(),
            Component.translatable("enum.generic_quality.elite").getString(),
            Component.translatable("enum.generic_quality.legendaer").getString(),
            Component.translatable("enum.generic_quality.goettlich").getString(),
            Component.translatable("enum.generic_quality.mythisch").getString()
        };

        for (int i = 0; i < tierCount; i++) {
            String name = (i < names.length) ? names[i] : "Tier " + i;
            String color = (i < colors.length) ? colors[i] : "§f";

            // Linear interpolation der Multiplier
            double multiplier = baseMultiplier + (maxMultiplier - baseMultiplier) * ((double) i / (tierCount - 1));

            tiers[i] = new GenericQuality(
                name,
                color,
                i,
                multiplier,
                "Quality Level " + i,
                tiers,
                i
            );
        }

        return tiers;
    }

    // ═══════════════════════════════════════════════════════════
    // PRODUCTIONQUALITY INTERFACE IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getColorCode() {
        return colorCode;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ProductionQuality upgrade() {
        if (canUpgrade()) {
            return allTiers[tierIndex + 1];
        }
        return this;  // Already max quality
    }

    @Override
    public ProductionQuality downgrade() {
        if (canDowngrade()) {
            return allTiers[tierIndex - 1];
        }
        return this;  // Already min quality
    }

    // ═══════════════════════════════════════════════════════════
    // ADDITIONAL METHODS
    // ═══════════════════════════════════════════════════════════

    public boolean canUpgrade() {
        return tierIndex < allTiers.length - 1;
    }

    public boolean canDowngrade() {
        return tierIndex > 0;
    }

    public boolean isMinQuality() {
        return tierIndex == 0;
    }

    public boolean isMaxQuality() {
        return tierIndex == allTiers.length - 1;
    }

    public int getTierIndex() {
        return tierIndex;
    }

    public int getTotalTiers() {
        return allTiers.length;
    }

    public List<GenericQuality> getAllTiers() {
        return Arrays.asList(allTiers);
    }

    // ═══════════════════════════════════════════════════════════
    // STATIC UTILITY METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Findet Quality nach Level
     */
    public static GenericQuality getByLevel(GenericQuality[] tiers, int level) {
        for (GenericQuality tier : tiers) {
            if (tier.getLevel() == level) {
                return tier;
            }
        }
        return tiers[0];  // Fallback to worst quality
    }

    /**
     * Findet Quality nach Display Name
     */
    public static GenericQuality getByName(GenericQuality[] tiers, String name) {
        for (GenericQuality tier : tiers) {
            if (tier.getDisplayName().equalsIgnoreCase(name)) {
                return tier;
            }
        }
        return tiers[0];  // Fallback to worst quality
    }

    // ═══════════════════════════════════════════════════════════
    // OBJECT METHODS
    // ═══════════════════════════════════════════════════════════

    @Override
    public String toString() {
        return String.format("%s%s§r (Level %d, x%.1f)",
            colorCode, displayName, level, priceMultiplier);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GenericQuality other)) return false;
        return level == other.level && displayName.equals(other.displayName);
    }

    @Override
    public int hashCode() {
        return 31 * level + displayName.hashCode();
    }

    // ═══════════════════════════════════════════════════════════
    // BUILDER PATTERN (Alternative Construction)
    // ═══════════════════════════════════════════════════════════

    /**
     * Builder für Custom Quality Systems
     */
    public static class Builder {
        private final int tierCount;
        private String[] names;
        private String[] colorCodes;
        private double[] priceMultipliers;
        private String[] descriptions;

        public Builder(int tierCount) {
            if (tierCount < 2 || tierCount > 10) {
                throw new IllegalArgumentException("Tier count must be between 2 and 10");
            }
            this.tierCount = tierCount;
            this.names = new String[tierCount];
            this.colorCodes = new String[tierCount];
            this.priceMultipliers = new double[tierCount];
            this.descriptions = new String[tierCount];

            // Defaults
            for (int i = 0; i < tierCount; i++) {
                names[i] = "Tier " + i;
                colorCodes[i] = "§f";
                priceMultipliers[i] = 1.0 + i * 0.5;
                descriptions[i] = "Quality level " + i;
            }
        }

        public Builder names(String... names) {
            if (names.length != tierCount) {
                throw new IllegalArgumentException("Must provide exactly " + tierCount + " names");
            }
            this.names = names;
            return this;
        }

        public Builder colorCodes(String... colorCodes) {
            if (colorCodes.length != tierCount) {
                throw new IllegalArgumentException("Must provide exactly " + tierCount + " color codes");
            }
            this.colorCodes = colorCodes;
            return this;
        }

        public Builder priceMultipliers(double... multipliers) {
            if (multipliers.length != tierCount) {
                throw new IllegalArgumentException("Must provide exactly " + tierCount + " price multipliers");
            }
            this.priceMultipliers = multipliers;
            return this;
        }

        public Builder descriptions(String... descriptions) {
            if (descriptions.length != tierCount) {
                throw new IllegalArgumentException("Must provide exactly " + tierCount + " descriptions");
            }
            this.descriptions = descriptions;
            return this;
        }

        public GenericQuality[] build() {
            GenericQuality[] tiers = new GenericQuality[tierCount];

            for (int i = 0; i < tierCount; i++) {
                tiers[i] = new GenericQuality(
                    names[i],
                    colorCodes[i],
                    i,
                    priceMultipliers[i],
                    descriptions[i],
                    tiers,
                    i
                );
            }

            return tiers;
        }
    }
}
