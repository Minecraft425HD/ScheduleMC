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
        tooltip.add(Component.translatable("tooltip.tobacco_bottle.effect", effectDescription));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.tobacco_bottle.right_click"));
    }
}

/**
 * Dünger - Erhöht Ertrag, verringert Qualität
 */
class FertilizerBottleItem extends TobaccoBottleItem {
    public FertilizerBottleItem() {
        super("Mehr Ertrag (max 10g), -1 Qualität", 50.0);
    }
    
    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.fertilizer_bottle.name");
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
        return Component.translatable("item.growth_booster_bottle.name");
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
        return Component.translatable("item.quality_booster_bottle.name");
    }
}
