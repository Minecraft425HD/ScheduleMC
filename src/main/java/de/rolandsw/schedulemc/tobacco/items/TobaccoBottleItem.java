package de.rolandsw.schedulemc.tobacco.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Basis-Klasse für Tabak-Flaschen
 */
public abstract class TobaccoBottleItem extends Item {
    
    private final String effectDescription;
    private final double price;
    
    public TobaccoBottleItem(String effectDescription, double price) {
        super(new Properties()
                .stacksTo(16));
        this.effectDescription = effectDescription;
        this.price = price;
    }
    
    public double getPrice() {
        return price;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Effekt: §e" + effectDescription));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§8Rechtsklick auf Tabakpflanze"));
    }
}

/**
 * Dünger - Erhöht Ertrag, verringert Qualität
 */
class FertilizerBottleItem extends TobaccoBottleItem {
    public FertilizerBottleItem() {
        super("+50% Ertrag, -1 Qualität", 50.0);
    }
    
    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("§a§lDünger");
    }
}

/**
 * Wachstumsbeschleuniger - Doppelte Geschwindigkeit, verringert Qualität
 */
class GrowthBoosterBottleItem extends TobaccoBottleItem {
    public GrowthBoosterBottleItem() {
        super("2x Wachstum, -1 Qualität", 75.0);
    }
    
    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("§b§lWachstumsbeschleuniger");
    }
}

/**
 * Qualitätsverbesserer - Erhöht Qualität
 */
class QualityBoosterBottleItem extends TobaccoBottleItem {
    public QualityBoosterBottleItem() {
        super("+1 Qualität", 100.0);
    }
    
    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("§6§lQualitätsverbesserer");
    }
}
