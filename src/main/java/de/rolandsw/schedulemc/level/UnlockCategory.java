package de.rolandsw.schedulemc.level;

import net.minecraft.network.chat.Component;

/**
 * Kategorien von freischaltbaren Inhalten im Level-System.
 */
public enum UnlockCategory {

    PRODUCTION_CHAIN("gui.app.level.category.production_chain.name", "§a", "gui.app.level.category.production_chain.desc"),
    MACHINE("gui.app.level.category.machine.name", "§b", "gui.app.level.category.machine.desc"),
    POT("gui.app.level.category.pot.name", "§6", "gui.app.level.category.pot.desc"),
    QUALITY("gui.app.level.category.quality.name", "§d", "gui.app.level.category.quality.desc"),
    STRAIN("gui.app.level.category.strain.name", "§e", "gui.app.level.category.strain.desc"),
    PROCESSING("gui.app.level.category.processing.name", "§5", "gui.app.level.category.processing.desc"),
    VEHICLE("gui.app.level.category.vehicle.name", "§7", "gui.app.level.category.vehicle.desc"),
    SHOP_FEATURE("gui.app.level.category.shop_feature.name", "§f", "gui.app.level.category.shop_feature.desc"),
    ECONOMY_FEATURE("gui.app.level.category.economy_feature.name", "§6", "gui.app.level.category.economy_feature.desc"),
    GANG("gui.app.level.category.gang.name", "§c", "gui.app.level.category.gang.desc");

    private final String displayNameKey;
    private final String colorCode;
    private final String descriptionKey;

    UnlockCategory(String displayNameKey, String colorCode, String descriptionKey) {
        this.displayNameKey = displayNameKey;
        this.colorCode = colorCode;
        this.descriptionKey = descriptionKey;
    }

    public String getDisplayName() {
        return Component.translatable(displayNameKey).getString();
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getDescription() {
        return Component.translatable(descriptionKey).getString();
    }

    public String getFormattedName() {
        return colorCode + getDisplayName() + "§r";
    }
}
