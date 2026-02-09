package de.rolandsw.schedulemc.coca.items;

import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Frisch geerntete Koka-Blätter
 * Müssen in der Extraktionswanne verarbeitet werden
 */
public class FreshCocaLeafItem extends Item {

    public FreshCocaLeafItem() {
        super(new Properties()
                .stacksTo(20)); // 1 Blatt = 1g, max 20 Blätter pro Stack
    }

    /**
     * Erstellt ItemStack mit Typ und Qualität
     */
    public static ItemStack create(CocaType type, TobaccoQuality quality, int count) {
        ItemStack stack = new ItemStack(CocaItems.FRESH_BOLIVIANISCH_LEAF.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("CocaType", type.name());
        tag.putString("Quality", quality.name());
        return stack;
    }

    /**
     * Liest Koka-Sorte aus ItemStack
     */
    public static CocaType getType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("CocaType")) {
            try {
                return CocaType.valueOf(tag.getString("CocaType"));
            } catch (IllegalArgumentException ignored) {}
        }
        return CocaType.BOLIVIANISCH; // Default
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
        return TobaccoQuality.GUT; // Default
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CocaType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);

        tooltip.add(Component.translatable("tooltip.coca.type_label").append(type.getColoredName()));
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.coca_leaf.must_extract"));
    }

    @Override
    public Component getName(ItemStack stack) {
        CocaType type = getType(stack);
        return Component.translatable("item.fresh_coca_leaf.name", type.getDisplayName());
    }
}
