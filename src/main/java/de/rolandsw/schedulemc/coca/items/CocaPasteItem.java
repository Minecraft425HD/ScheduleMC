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
 * Koka-Paste (braun) - Zwischenprodukt aus Extraktion
 * Muss im Raffinerie-Ofen verarbeitet werden
 */
public class CocaPasteItem extends Item {

    public CocaPasteItem() {
        super(new Properties()
                .stacksTo(20));
    }

    /**
     * Erstellt ItemStack mit Typ und Qualität
     */
    public static ItemStack create(CocaType type, TobaccoQuality quality, int count) {
        ItemStack stack = new ItemStack(CocaItems.COCA_PASTE.get(), count);
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
            return CocaType.valueOf(tag.getString("CocaType"));
        }
        return CocaType.BOLIVIANISCH; // Default
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
        CocaType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);

        tooltip.add(Component.literal("§7Sorte: " + type.getColoredName()));
        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§6Braune Paste"));
        tooltip.add(Component.literal("§8Muss raffiniert werden (Raffinerie-Ofen)"));
    }

    @Override
    public Component getName(ItemStack stack) {
        CocaType type = getType(stack);
        return Component.literal(type.getDisplayName() + " Koka-Paste");
    }
}
