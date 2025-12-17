package de.rolandsw.schedulemc.mdma.items;

import de.rolandsw.schedulemc.mdma.MDMAQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * MDMA-Base - Zwischenprodukt aus Reaktions-Kessel
 * Rohe MDMA-Kristalle zur Weiterverarbeitung
 */
public class MDMABaseItem extends Item {

    public MDMABaseItem() {
        super(new Properties().stacksTo(32));
    }

    public static ItemStack create(MDMAQuality quality, int count) {
        ItemStack stack = new ItemStack(MDMAItems.MDMA_BASE.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Quality", quality.name());
        return stack;
    }

    public static MDMAQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return MDMAQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException e) {
                return MDMAQuality.STANDARD;
            }
        }
        return MDMAQuality.STANDARD;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        MDMAQuality quality = getQuality(stack);
        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§fBraune Kristalle"));
        tooltip.add(Component.literal("§8Nächster Schritt: Trocknungs-Ofen"));
    }

    @Override
    public Component getName(ItemStack stack) {
        MDMAQuality quality = getQuality(stack);
        return Component.literal(quality.getColorCode() + "MDMA-Base");
    }
}
