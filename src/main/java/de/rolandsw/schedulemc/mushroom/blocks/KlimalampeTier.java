package de.rolandsw.schedulemc.mushroom.blocks;

import net.minecraft.network.chat.Component;

/**
 * Klimalampe-Stufen mit verschiedenen Eigenschaften
 */
public enum KlimalampeTier {
    SMALL("§7", false, 0.0, 0.0),
    MEDIUM("§e", true, 0.10, 0.0),
    LARGE("§6", true, 0.25, 0.10);

    private final String colorCode;
    private final boolean automatic;
    private final double growthBonus;
    private final double qualityBonus;

    KlimalampeTier(String colorCode, boolean automatic, double growthBonus, double qualityBonus) {
        this.colorCode = colorCode;
        this.automatic = automatic;
        this.growthBonus = growthBonus;
        this.qualityBonus = qualityBonus;
    }

    public Component getDisplayName() {
        return Component.translatable("enum.klimalampe_tier." + this.name().toLowerCase());
    }

    public String getColorCode() {
        return colorCode;
    }

    public Component getColoredName() {
        return Component.literal(colorCode).append(getDisplayName());
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public double getGrowthBonus() {
        return growthBonus;
    }

    public double getQualityBonus() {
        return qualityBonus;
    }

    public String getRegistryName() {
        return name().toLowerCase();
    }
}
