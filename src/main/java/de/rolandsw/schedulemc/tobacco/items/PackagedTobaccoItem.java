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
 * Abgepackter Tabak für den Verkauf an NPCs
 *
 * NBT-Struktur:
 * - TobaccoType: VIRGINIA, BURLEY, ORIENTAL, HAVANA
 * - Quality: BASIS, GUT, SEHR_GUT, EXZELLENT, LEGENDAER
 * - Weight: Gramm (50, 100, 250, 500)
 * - PackagedDate: Minecraft-Tag (für Alterung)
 */
public class PackagedTobaccoItem extends Item {

    public PackagedTobaccoItem() {
        super(new Properties()
                .stacksTo(1)); // NICHT STACKBAR - jedes Paket hat eigene Daten!
    }

    /**
     * Erstellt abgepackten Tabak
     */
    public static ItemStack create(TobaccoType type, TobaccoQuality quality, int weight, long currentDay) {
        ItemStack stack = new ItemStack(TobaccoItems.PACKAGED_TOBACCO.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("TobaccoType", type.name());
        tag.putString("Quality", quality.name());
        tag.putInt("Weight", weight);
        tag.putLong("PackagedDate", currentDay);
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
        return TobaccoType.VIRGINIA;
    }

    /**
     * Liest Qualität aus ItemStack
     */
    public static TobaccoQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            return TobaccoQuality.valueOf(tag.getString("Quality"));
        }
        return TobaccoQuality.GUT;
    }

    /**
     * Liest Gewicht aus ItemStack
     */
    public static int getWeight(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Weight")) {
            return tag.getInt("Weight");
        }
        return 100; // Default
    }

    /**
     * Liest Verpackungsdatum aus ItemStack
     */
    public static long getPackagedDate(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("PackagedDate")) {
            return tag.getLong("PackagedDate");
        }
        return 0;
    }

    /**
     * Berechnet Basis-Verkaufspreis (ohne NPC-Modifikatoren)
     */
    public static double getBasePrice(ItemStack stack) {
        TobaccoType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);
        int weight = getWeight(stack);

        // Basispreis pro Gramm
        double pricePerGram = type.getBasePrice(); // z.B. Virginia = 0.50€/g

        // Qualitäts-Multiplikator
        double qualityMultiplier = quality.getPriceMultiplier();

        // Gesamt-Preis
        return pricePerGram * qualityMultiplier * weight;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        TobaccoType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);
        int weight = getWeight(stack);
        long packagedDate = getPackagedDate(stack);

        // Gewicht
        tooltip.add(Component.literal("§7Gewicht: §e" + weight + "g"));

        // Qualität
        tooltip.add(Component.literal("§7Qualität: " + quality.getColoredName()));

        // Verpackt am
        tooltip.add(Component.literal("§7Verpackt am: §aTTag " + packagedDate));

        tooltip.add(Component.literal(""));

        // Geschätzter Wert
        double basePrice = getBasePrice(stack);
        double pricePerGram = basePrice / weight;
        tooltip.add(Component.literal("§7Geschätzter Wert:"));
        tooltip.add(Component.literal("§e" + String.format("%.2f€", basePrice) +
                                    " §7(~" + String.format("%.2f€/g", pricePerGram) + ")"));
    }

    @Override
    public Component getName(ItemStack stack) {
        TobaccoType type = getType(stack);
        TobaccoQuality quality = getQuality(stack);
        return Component.literal(quality.getColoredName() + " §r" + type.getDisplayName() + "-Tabak (Abgepackt)");
    }
}
