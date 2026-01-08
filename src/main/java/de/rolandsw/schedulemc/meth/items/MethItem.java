package de.rolandsw.schedulemc.meth.items;

import de.rolandsw.schedulemc.meth.MethQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Fertiges Meth - Endprodukt aus Vakuum-Trockner
 * Kann verpackt und verkauft werden
 */
public class MethItem extends Item {

    public MethItem() {
        super(new Properties()
                .stacksTo(20));
    }

    /**
     * Erstellt ItemStack mit Qualität
     */
    public static ItemStack create(MethQuality quality, int count) {
        ItemStack stack = new ItemStack(MethItems.METH.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Quality", quality.name());
        return stack;
    }

    /**
     * Liest Qualität aus ItemStack
     */
    public static MethQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            try {
                return MethQuality.valueOf(tag.getString("Quality"));
            } catch (IllegalArgumentException e) {
                return MethQuality.STANDARD;
            }
        }
        return MethQuality.STANDARD;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        MethQuality quality = getQuality(stack);

        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));
        tooltip.add(Component.literal("§7Reinheit: §f" + getPurityPercent(quality) + "%"));
        tooltip.add(Component.literal("§7Gewicht: §f" + stack.getCount() + "g §8(" + stack.getCount() + "x 1g)"));
        tooltip.add(Component.literal(""));

        String colorDesc = switch (quality) {
            case STANDARD -> "§fWeiße Kristalle";
            case GUT -> "§eGelbliche Kristalle";
            case BLUE_SKY -> "§b§lBlaue Kristalle";
        };
        tooltip.add(Component.literal(colorDesc));
        tooltip.add(Component.literal("§8Kann verpackt und verkauft werden"));

        if (quality == MethQuality.BLUE_SKY) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("§b\"Say my name.\""));
        }
    }

    private int getPurityPercent(MethQuality quality) {
        return switch (quality) {
            case STANDARD -> 70 + (int)(Math.random() * 10); // 70-79%
            case GUT -> 80 + (int)(Math.random() * 10);      // 80-89%
            case BLUE_SKY -> 96 + (int)(Math.random() * 4);  // 96-99%
        };
    }

    @Override
    public Component getName(ItemStack stack) {
        MethQuality quality = getQuality(stack);
        String name = switch (quality) {
            case STANDARD -> "Crystal Meth";
            case GUT -> "Premium Crystal";
            case BLUE_SKY -> "Blue Sky";
        };
        return Component.literal(quality.getColorCode() + name);
    }
}
