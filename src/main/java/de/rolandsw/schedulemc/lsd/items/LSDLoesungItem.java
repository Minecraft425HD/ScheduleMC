package de.rolandsw.schedulemc.lsd.items;

import de.rolandsw.schedulemc.lsd.LSDDosage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * LSD-Lösung - Dosierte Flüssigkeit
 * Wird auf Blotter-Papier getropft
 */
public class LSDLoesungItem extends Item {

    public LSDLoesungItem() {
        super(new Properties().stacksTo(1));
    }

    /**
     * Erstellt ItemStack mit Dosierung
     */
    public static ItemStack create(LSDDosage dosage, int micrograms, int charges) {
        ItemStack stack = new ItemStack(LSDItems.LSD_LOESUNG.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Dosage", dosage.name());
        tag.putInt("Micrograms", micrograms);
        tag.putInt("Charges", charges); // Wie viele Blotter können noch gemacht werden
        return stack;
    }

    public static LSDDosage getDosage(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Dosage")) {
            try {
                return LSDDosage.valueOf(tag.getString("Dosage"));
            } catch (IllegalArgumentException e) {
                return LSDDosage.STANDARD;
            }
        }
        return LSDDosage.STANDARD;
    }

    public static int getMicrograms(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Micrograms")) {
            return tag.getInt("Micrograms");
        }
        return 100;
    }

    public static int getCharges(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Charges")) {
            return tag.getInt("Charges");
        }
        return 10;
    }

    public static void setCharges(ItemStack stack, int charges) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("Charges", charges);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        LSDDosage dosage = getDosage(stack);
        int micrograms = getMicrograms(stack);
        int charges = getCharges(stack);

        tooltip.add(Component.translatable("tooltip.lsd.dosage_label").append(dosage.getColoredName()));
        tooltip.add(Component.translatable("tooltip.lsd_loesung.concentration").append(Component.literal("§f" + micrograms + "μg/Tropfen")));
        tooltip.add(Component.translatable("tooltip.lsd_loesung.charges").append(Component.literal("§e" + charges + " Blotter")));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.lsd_loesung.clear_liquid"));
        tooltip.add(Component.translatable("tooltip.lsd_loesung.next_press"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.lsd_loesung.light_sensitive"));
    }

    @Override
    public Component getName(ItemStack stack) {
        LSDDosage dosage = getDosage(stack);
        return Component.literal(dosage.getColorCode() + "LSD-Lösung (" + dosage.getDosageString() + ")");
    }
}
