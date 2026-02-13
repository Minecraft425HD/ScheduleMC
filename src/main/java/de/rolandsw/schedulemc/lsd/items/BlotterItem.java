package de.rolandsw.schedulemc.lsd.items;

import de.rolandsw.schedulemc.lsd.BlotterDesign;
import de.rolandsw.schedulemc.lsd.LSDDosage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * LSD-Blotter - Fertiges Endprodukt
 * Löschpapier mit LSD getränkt, in Tabs perforiert
 */
public class BlotterItem extends Item {

    public BlotterItem() {
        super(new Properties().stacksTo(64));
    }

    /**
     * Erstellt ItemStack mit Dosierung und Design
     */
    public static ItemStack create(LSDDosage dosage, int micrograms, BlotterDesign design, int count) {
        ItemStack stack = new ItemStack(LSDItems.BLOTTER.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Dosage", dosage.name());
        tag.putInt("Micrograms", micrograms);
        tag.putString("Design", design.name());
        return stack;
    }

    public static LSDDosage getDosage(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Dosage")) {
            try {
                return LSDDosage.valueOf(tag.getString("Dosage"));
            } catch (IllegalArgumentException e) {
                return LSDDosage.GUT;
            }
        }
        return LSDDosage.GUT;
    }

    public static int getMicrograms(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Micrograms")) {
            return tag.getInt("Micrograms");
        }
        return 100;
    }

    public static BlotterDesign getDesign(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Design")) {
            try {
                return BlotterDesign.valueOf(tag.getString("Design"));
            } catch (IllegalArgumentException e) {
                return BlotterDesign.TOTENKOPF;
            }
        }
        return BlotterDesign.TOTENKOPF;
    }

    /**
     * Berechnet Basispreis für NPC-Verkauf
     */
    public double getBasePrice(LSDDosage dosage) {
        return 25.0 * dosage.getPriceMultiplier(); // Basispreis pro Tab
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        LSDDosage dosage = getDosage(stack);
        int micrograms = getMicrograms(stack);
        BlotterDesign design = getDesign(stack);

        tooltip.add(Component.translatable("tooltip.lsd.dosage_label").append(dosage.getColoredName()));
        tooltip.add(Component.translatable("tooltip.lsd.strength_label").append(Component.literal("§f" + micrograms + "μg")));
        tooltip.add(Component.translatable("tooltip.lsd.design_label").append(Component.literal(design.getColoredName() + " " + design.getSymbol())));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.lsd.dosage." + dosage.name().toLowerCase() + ".description"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.blotter.paper_with_design"));
        tooltip.add(Component.translatable("tooltip.blotter.can_consume_sell"));

        if (dosage == LSDDosage.LEGENDAER) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("tooltip.blotter.bicycle_day"));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        LSDDosage dosage = getDosage(stack);
        BlotterDesign design = getDesign(stack);
        return Component.literal(dosage.getColorCode() + design.getSymbol() + " ")
            .append(Component.translatable("item.blotter.name", getMicrograms(stack)));
    }
}
