package de.rolandsw.schedulemc.tobacco.items;

import de.rolandsw.schedulemc.production.core.PotType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Erdsack-Item - Alle Säcke geben 33 Erde (genug für 1 Pflanze)
 *
 * Unterschied liegt in der Stack-Größe:
 * - SMALL: Stack 1 (billigste Option, einzeln)
 * - MEDIUM: Stack 2 (mittlere Option)
 * - LARGE: Stack 3 (beste Option, mehr auf einmal tragen)
 */
public class SoilBagItem extends Item {

    private final SoilBagType type;

    public SoilBagItem(SoilBagType type) {
        super(new Properties()
                .stacksTo(type.getMaxStackSize()));
        this.type = type;
    }

    public SoilBagType getType() {
        return type;
    }

    /**
     * Gibt die Menge an Erde zurück die dieser Sack enthält (immer 33)
     */
    public int getSoilAmount() {
        return PotType.SOIL_PER_PLANT; // 33 Erde pro Sack
    }

    /**
     * Erstellt neuen vollen Erdsack
     */
    public static ItemStack createFull(SoilBagType type) {
        return new ItemStack(getItemForType(type));
    }

    /**
     * Gibt Item für Typ zurück
     */
    private static Item getItemForType(SoilBagType type) {
        return switch (type) {
            case SMALL -> TobaccoItems.SOIL_BAG_SMALL.get();
            case MEDIUM -> TobaccoItems.SOIL_BAG_MEDIUM.get();
            case LARGE -> TobaccoItems.SOIL_BAG_LARGE.get();
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int soilAmount = getSoilAmount();

        tooltip.add(Component.translatable("tooltip.soil_bag.contains", soilAmount));
        tooltip.add(Component.translatable("tooltip.soil_bag.enough_for_one_plant"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.soil_bag.stack_size", type.getMaxStackSize()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.soil_bag.right_click_pot"));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(type.getColor())
            .append(Component.translatable("item.soil_bag." + type.name().toLowerCase()));
    }
}

/**
 * Verschiedene Erdsack-Typen
 * Alle geben 33 Erde, unterscheiden sich nur in Stack-Größe und Preis
 */
enum SoilBagType {
    SMALL("§7", 1, 10.0),   // Stack 1, billig
    MEDIUM("§e", 2, 25.0),  // Stack 2, mittel
    LARGE("§6", 3, 50.0);   // Stack 3, teuer aber mehr tragbar

    private final String color;
    private final int maxStackSize;
    private final double basePrice;

    SoilBagType(String color, int maxStackSize, double basePrice) {
        this.color = color;
        this.maxStackSize = maxStackSize;
        this.basePrice = basePrice;
    }

    public Component getDisplayName() {
        return Component.translatable("enum.soil_bag_type." + this.name().toLowerCase());
    }

    public String getColor() {
        return color;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public double getBasePrice() {
        return basePrice;
    }
}
