package de.rolandsw.schedulemc.tobacco.items;

import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Fermentierte Tabakblätter
 * Endprodukt - kann verkauft werden
 */
public class FermentedTobaccoLeafItem extends Item {
    
    public FermentedTobaccoLeafItem() {
        super(new Properties()
                .stacksTo(20)); // 1 Blatt = 1g, max 20 Blätter pro Stack
    }
    
    /**
     * Erstellt ItemStack mit Typ und Qualität
     */
    public static ItemStack create(TobaccoType type, TobaccoQuality quality, int count) {
        // Wähle das richtige Item basierend auf dem TobaccoType
        Item item = switch (type) {
            case VIRGINIA -> TobaccoItems.FERMENTED_VIRGINIA_LEAF.get();
            case BURLEY -> TobaccoItems.FERMENTED_BURLEY_LEAF.get();
            case ORIENTAL -> TobaccoItems.FERMENTED_ORIENTAL_LEAF.get();
            case HAVANA -> TobaccoItems.FERMENTED_HAVANA_LEAF.get();
        };

        ItemStack stack = new ItemStack(item, count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("TobaccoType", type.name());
        tag.putString("Quality", quality.name());
        return stack;
    }
    
    /**
     * Liest Tabaksorte aus ItemStack
     */
    public static TobaccoType getType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("TobaccoType")) {
            try {
                return TobaccoType.valueOf(tag.getString("TobaccoType"));
            } catch (IllegalArgumentException ignored) {}
        }
        return TobaccoType.VIRGINIA;
    }
    
    /**
     * Liest Qualität aus ItemStack
     */
    public static TobaccoQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return TobaccoQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException ignored) {}
        }
        return TobaccoQuality.GUT;
    }
    
    /**
     * Berechnet Verkaufspreis
     */
    public static double getPrice(ItemStack stack) {
        TobaccoType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);
        try {
            return type.calculateDynamicPrice(quality, stack.getCount(), null);
        } catch (Exception e) {
            return type.calculatePrice(quality, stack.getCount());
        }
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        TobaccoType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);
        
        tooltip.add(Component.translatable("tooltip.tobacco.type_label").append(type.getColoredName()));
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.literal(""));
        
        double pricePerItem;
        try {
            pricePerItem = type.calculateDynamicPrice(quality, 1, null);
        } catch (Exception e) {
            pricePerItem = type.calculatePrice(quality, 1);
        }
        tooltip.add(Component.translatable("tooltip.fermented_tobacco.sale_price_per", String.format("%.2f", pricePerItem)));

        if (stack.getCount() > 1) {
            double totalPrice;
            try {
                totalPrice = type.calculateDynamicPrice(quality, stack.getCount(), null);
            } catch (Exception e) {
                totalPrice = type.calculatePrice(quality, stack.getCount());
            }
            tooltip.add(Component.translatable("tooltip.fermented_tobacco.total", String.format("%.2f", totalPrice)));
        }
    }
    
    @Override
    public Component getName(ItemStack stack) {
        TobaccoType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);
        return Component.empty()
            .append(quality.getColoredName())
            .append(Component.translatable("item.fermented_tobacco_leaf.name", type.getDisplayName()));
    }
}
