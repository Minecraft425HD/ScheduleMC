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
 * Frisch geerntete Tabakblätter
 * Müssen getrocknet werden
 */
public class FreshTobaccoLeafItem extends Item {
    
    public FreshTobaccoLeafItem() {
        super(new Properties()
                .stacksTo(20)); // 1 Blatt = 1g, max 20 Blätter pro Stack
    }
    
    /**
     * Erstellt ItemStack mit Typ und Qualität
     */
    public static ItemStack create(TobaccoType type, TobaccoQuality quality, int count) {
        ItemStack stack = new ItemStack(TobaccoItems.FRESH_VIRGINIA_LEAF.get(), count);
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
            return TobaccoType.valueOf(tag.getString("TobaccoType"));
        }
        return TobaccoType.VIRGINIA; // Default
    }
    
    /**
     * Liest Qualität aus ItemStack
     */
    public static TobaccoQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            return TobaccoQuality.valueOf(tag.getString("Quality"));
        }
        return TobaccoQuality.GUT; // Default
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        TobaccoType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);
        
        tooltip.add(Component.literal("§7Sorte: " + type.getColoredName()));
        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§8Muss getrocknet werden"));
    }
    
    @Override
    public Component getName(ItemStack stack) {
        TobaccoType type = getType(stack);
        return Component.literal("Frische " + type.getDisplayName() + "-Blätter");
    }
}
