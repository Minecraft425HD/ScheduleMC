package de.rolandsw.schedulemc.mdma.items;

import de.rolandsw.schedulemc.mdma.MDMAQuality;
import de.rolandsw.schedulemc.mdma.PillColor;
import de.rolandsw.schedulemc.mdma.PillDesign;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Ecstasy-Pille - Fertiges Endprodukt
 * Gepresste Pille mit Design und Farbe
 */
public class EcstasyPillItem extends Item {

    public EcstasyPillItem() {
        super(new Properties().stacksTo(64));
    }

    public static ItemStack create(MDMAQuality quality, PillDesign design, PillColor color, int count) {
        ItemStack stack = new ItemStack(MDMAItems.ECSTASY_PILL.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Quality", quality.name());
        tag.putString("Design", design.name());
        tag.putString("Color", color.name());
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

    public static PillDesign getDesign(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Design")) {
            try {
                return PillDesign.valueOf(tag.getString("Design"));
            } catch (IllegalArgumentException e) {
                return PillDesign.TESLA;
            }
        }
        return PillDesign.TESLA;
    }

    public static PillColor getColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Color")) {
            try {
                return PillColor.valueOf(tag.getString("Color"));
            } catch (IllegalArgumentException e) {
                return PillColor.PINK;
            }
        }
        return PillColor.PINK;
    }

    public double getBasePrice(MDMAQuality quality) {
        return 30.0 * quality.getPriceMultiplier();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        MDMAQuality quality = getQuality(stack);
        PillDesign design = getDesign(stack);
        PillColor color = getColor(stack);

        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));
        tooltip.add(Component.literal("§7Design: " + design.getColoredName() + " " + design.getSymbol()));
        tooltip.add(Component.literal("§7Farbe: " + color.getColoredName()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal(color.getColorCode() + design.getSymbol() + " Gepresste Pille"));
        tooltip.add(Component.literal("§8" + quality.getDescription()));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§7Preis: §6$" + String.format("%.0f", getBasePrice(quality))));
    }

    @Override
    public Component getName(ItemStack stack) {
        PillDesign design = getDesign(stack);
        PillColor color = getColor(stack);
        return Component.literal(color.getColorCode() + design.getSymbol() + " " + design.getDisplayName() + " Ecstasy");
    }
}
