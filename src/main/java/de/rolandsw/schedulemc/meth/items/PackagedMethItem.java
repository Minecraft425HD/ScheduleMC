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
 * Verpacktes Meth - Für den Verkauf an NPCs
 * Verschiedene Packungsgrößen verfügbar
 */
public class PackagedMethItem extends Item {

    private final int gramAmount;

    public PackagedMethItem(int gramAmount) {
        super(new Properties()
                .stacksTo(gramAmount == 1 ? 64 : (gramAmount <= 7 ? 32 : 16)));
        this.gramAmount = gramAmount;
    }

    /**
     * Erstellt ItemStack mit Qualität
     */
    public static ItemStack create(Item item, MethQuality quality, int count) {
        ItemStack stack = new ItemStack(item, count);
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

    public int getGramAmount() {
        return gramAmount;
    }

    /**
     * Berechnet Basispreis für NPC-Verkauf
     */
    public double getBasePrice(MethQuality quality) {
        double pricePerGram = 150.0; // Grundpreis pro Gramm
        return gramAmount * pricePerGram * quality.getPriceMultiplier();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        MethQuality quality = getQuality(stack);

        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));
        tooltip.add(Component.literal("§7Menge: §f" + gramAmount + "g"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§7Basispreis: §6$" + String.format("%.0f", getBasePrice(quality))));
        tooltip.add(Component.literal(""));

        String desc = switch (quality) {
            case STANDARD -> "§fVersiegelte Tüte mit weißen Kristallen";
            case GUT -> "§eVersiegelte Tüte mit gelblichen Kristallen";
            case BLUE_SKY -> "§bVersiegelte Tüte mit blauen Kristallen";
        };
        tooltip.add(Component.literal(desc));
        tooltip.add(Component.literal("§8Für den Straßenverkauf"));
    }

    @Override
    public Component getName(ItemStack stack) {
        MethQuality quality = getQuality(stack);
        String qualityName = quality == MethQuality.BLUE_SKY ? "Blue Sky" : "Crystal";
        return Component.literal(quality.getColorCode() + qualityName + " §7(" + gramAmount + "g)");
    }
}
