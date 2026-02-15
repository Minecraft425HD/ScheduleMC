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
 * Getrocknete Tabakblätter
 * Können fermentiert werden für bessere Qualität
 */
public class DriedTobaccoLeafItem extends Item {
    
    public DriedTobaccoLeafItem() {
        super(new Properties()
                .stacksTo(20)); // 1 Blatt = 1g, max 20 Blätter pro Stack
    }
    
    /**
     * Erstellt ItemStack mit Typ und Qualität
     */
    public static ItemStack create(TobaccoType type, TobaccoQuality quality, int count) {
        // Wähle das richtige Item basierend auf dem TobaccoType
        Item item = switch (type) {
            case VIRGINIA -> TobaccoItems.DRIED_VIRGINIA_LEAF.get();
            case BURLEY -> TobaccoItems.DRIED_BURLEY_LEAF.get();
            case ORIENTAL -> TobaccoItems.DRIED_ORIENTAL_LEAF.get();
            case HAVANA -> TobaccoItems.DRIED_HAVANA_LEAF.get();
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
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        TobaccoType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);
        
        tooltip.add(Component.translatable("tooltip.tobacco.type_label").append(type.getColoredName()));
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.dried_tobacco.can_ferment"));
    }
    
    @Override
    public Component getName(ItemStack stack) {
        TobaccoType type = getType(stack);
        return Component.translatable("item.dried_tobacco_leaf.name", type.getDisplayName());
    }
}
