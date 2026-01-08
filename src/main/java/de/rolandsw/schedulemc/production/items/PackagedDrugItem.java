package de.rolandsw.schedulemc.production.items;

import de.rolandsw.schedulemc.cannabis.items.CuredBudItem;
import de.rolandsw.schedulemc.cannabis.items.TrimmedBudItem;
import de.rolandsw.schedulemc.coca.items.CocaineItem;
import de.rolandsw.schedulemc.coca.items.CrackRockItem;
import de.rolandsw.schedulemc.meth.items.MethItem;
import de.rolandsw.schedulemc.mushroom.items.DriedMushroomItem;
import de.rolandsw.schedulemc.poppy.items.HeroinItem;
import de.rolandsw.schedulemc.tobacco.items.FermentedTobaccoLeafItem;
import de.rolandsw.schedulemc.production.core.DrugType;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.production.core.ProductionType;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.coca.CocaType;
import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.meth.MethQuality;
import de.rolandsw.schedulemc.mushroom.MushroomType;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.CannabisStrain;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Universelles verpacktes Drogen-Item
 * Speichert alle Informationen in NBT:
 * - DrugType (TOBACCO, COCAINE, HEROIN, METH, MUSHROOM, CANNABIS)
 * - ItemType (COCAINE, CRACK, TRIMMED_CANNABIS, CURED_CANNABIS, etc.)
 * - Weight (1g, 5g, 10g, 20g)
 * - Quality (als String, da verschiedene Enum-Typen)
 * - Variant (als String, z.B. Virginia, Bolivianisch, etc.)
 * - PackageDate (Minecraft-Tag)
 */
public class PackagedDrugItem extends Item {

    public PackagedDrugItem() {
        super(new Properties()
                .stacksTo(1)); // Verpackte Ware nicht stapelbar
    }

    /**
     * Prüft ob ein Item verpackbar ist (verarbeitetes Drug-Item)
     */
    public static boolean isPackageableItem(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof FermentedTobaccoLeafItem ||
               item instanceof CocaineItem ||
               item instanceof CrackRockItem ||
               item instanceof HeroinItem ||
               item instanceof MethItem ||
               item instanceof DriedMushroomItem ||
               item instanceof TrimmedBudItem ||
               item instanceof CuredBudItem;
    }

    // ═══════════════════════════════════════════════════════════
    // CREATE METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Erstellt verpackte Droge mit allen Eigenschaften (mit ItemType)
     */
    public static ItemStack create(DrugType drugType, int weight, String quality, @Nullable String variant, long packageDate, @Nullable String itemType) {
        ItemStack stack = new ItemStack(de.rolandsw.schedulemc.items.ModItems.PACKAGED_DRUG.get(), 1);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("DrugType", drugType.name());
        tag.putInt("Weight", weight);
        tag.putString("Quality", quality);
        if (variant != null) {
            tag.putString("Variant", variant);
        }
        tag.putLong("PackageDate", packageDate);
        if (itemType != null) {
            tag.putString("ItemType", itemType);
        }
        return stack;
    }

    /**
     * Erstellt verpackte Droge mit allen Eigenschaften (ohne ItemType - für Backwards Compatibility)
     */
    public static ItemStack create(DrugType drugType, int weight, String quality, @Nullable String variant, long packageDate) {
        return create(drugType, weight, quality, variant, packageDate, null);
    }

    /**
     * Vereinfachte create-Methode mit ProductionType und ProductionQuality (mit ItemType)
     */
    public static ItemStack create(DrugType drugType, int weight, ProductionQuality quality, @Nullable ProductionType type, long packageDate, @Nullable String itemType) {
        String qualityStr = quality.getClass().getSimpleName() + "." + ((Enum<?>) quality).name();
        String variantStr = type != null ? (type.getClass().getSimpleName() + "." + ((Enum<?>) type).name()) : null;
        return create(drugType, weight, qualityStr, variantStr, packageDate, itemType);
    }

    /**
     * Vereinfachte create-Methode mit ProductionType und ProductionQuality (ohne ItemType - für Backwards Compatibility)
     */
    public static ItemStack create(DrugType drugType, int weight, ProductionQuality quality, @Nullable ProductionType type, long packageDate) {
        return create(drugType, weight, quality, type, packageDate, null);
    }

    // ═══════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════

    public static DrugType getDrugType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("DrugType")) {
            return DrugType.fromString(tag.getString("DrugType"));
        }
        return DrugType.TOBACCO;
    }

    public static int getWeight(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Weight")) {
            return tag.getInt("Weight");
        }
        return 1;
    }

    public static String getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Quality")) {
            return tag.getString("Quality");
        }
        return "TobaccoQuality.GUT";
    }

    @Nullable
    public static String getVariant(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Variant")) {
            return tag.getString("Variant");
        }
        return null;
    }

    @Nullable
    public static String getItemType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ItemType")) {
            return tag.getString("ItemType");
        }
        return null;
    }

    public static long getPackageDate(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("PackageDate")) {
            return tag.getLong("PackageDate");
        }
        return 0L;
    }

    // ═══════════════════════════════════════════════════════════
    // QUALITY PARSING
    // ═══════════════════════════════════════════════════════════

    /**
     * Parse Quality Enum aus String (Format: "ClassName.ENUM_VALUE")
     */
    public static ProductionQuality parseQuality(String qualityStr) {
        if (qualityStr == null || !qualityStr.contains(".")) {
            return TobaccoQuality.GUT;
        }

        String[] parts = qualityStr.split("\\.");
        String className = parts[0];
        String enumValue = parts[1];

        try {
            return switch (className) {
                case "TobaccoQuality" -> TobaccoQuality.valueOf(enumValue);
                case "MethQuality" -> MethQuality.valueOf(enumValue);
                case "CannabisQuality" -> CannabisQuality.valueOf(enumValue);
                default -> TobaccoQuality.GUT;
            };
        } catch (IllegalArgumentException e) {
            return TobaccoQuality.GUT;
        }
    }

    /**
     * Parse Variant Enum aus String (Format: "ClassName.ENUM_VALUE")
     */
    @Nullable
    public static ProductionType parseVariant(String variantStr) {
        if (variantStr == null || !variantStr.contains(".")) {
            return null;
        }

        String[] parts = variantStr.split("\\.");
        String className = parts[0];
        String enumValue = parts[1];

        try {
            return switch (className) {
                case "TobaccoType" -> TobaccoType.valueOf(enumValue);
                case "CocaType" -> CocaType.valueOf(enumValue);
                case "PoppyType" -> PoppyType.valueOf(enumValue);
                case "MushroomType" -> MushroomType.valueOf(enumValue);
                case "CannabisStrain" -> CannabisStrain.valueOf(enumValue);
                default -> null;
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PRICE CALCULATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Berechnet Verkaufspreis basierend auf Typ, Qualität, Variante und Gewicht
     */
    public static double calculatePrice(ItemStack stack) {
        DrugType drugType = getDrugType(stack);
        int weight = getWeight(stack);
        ProductionQuality quality = parseQuality(getQuality(stack));
        ProductionType variant = parseVariant(getVariant(stack));

        // Basis-Preis pro Gramm (vom Variant, falls vorhanden)
        double basePricePerGram;
        if (variant != null) {
            basePricePerGram = variant.getBasePrice();
        } else {
            // Fallback wenn kein Variant vorhanden
            basePricePerGram = switch (drugType) {
                case TOBACCO -> 0.50;
                case COCAINE -> 80.0;
                case HEROIN -> 100.0;
                case METH -> 120.0;
                case MUSHROOM -> 15.0;
                case CANNABIS -> 10.0;
            };
        }

        // Qualitäts-Multiplikator
        double qualityMultiplier = quality.getPriceMultiplier();

        return basePricePerGram * weight * qualityMultiplier;
    }

    // ═══════════════════════════════════════════════════════════
    // DISPLAY
    // ═══════════════════════════════════════════════════════════

    @Override
    public Component getName(ItemStack stack) {
        DrugType drugType = getDrugType(stack);
        int weight = getWeight(stack);
        ProductionType variant = parseVariant(getVariant(stack));
        String itemType = getItemType(stack);

        Component name;

        // Spezielle Anzeige basierend auf ItemType
        if (itemType != null) {
            switch (itemType) {
                case "CRACK":
                    if (variant != null) {
                        name = Component.literal(variant.getColoredName()).append(Component.translatable("item.packaged.crack"));
                    } else {
                        name = Component.translatable("item.packaged.crack");
                    }
                    break;
                case "COCAINE":
                    if (variant != null) {
                        name = Component.literal(variant.getColoredName()).append(Component.translatable("item.packaged.cocaine"));
                    } else {
                        name = Component.translatable("item.packaged.cocaine");
                    }
                    break;
                case "CURED_CANNABIS":
                    if (variant != null) {
                        name = Component.literal(variant.getColoredName()).append(Component.translatable("item.packaged.cannabis_cured"));
                    } else {
                        name = Component.translatable("item.packaged.cannabis_cured");
                    }
                    break;
                case "TRIMMED_CANNABIS":
                    if (variant != null) {
                        name = Component.literal(variant.getColoredName()).append(Component.translatable("item.packaged.cannabis"));
                    } else {
                        name = Component.translatable("item.packaged.cannabis");
                    }
                    break;
                default:
                    // Fallback auf Standard-Logik
                    if (variant != null) {
                        name = Component.literal(variant.getColoredName()).append(Component.literal(" " + drugType.getDisplayName()));
                    } else {
                        name = Component.literal(drugType.getColoredName());
                    }
            }
        } else {
            // Alte Logik für Items ohne ItemType (Backwards Compatibility)
            if (variant != null) {
                name = Component.literal(variant.getColoredName()).append(Component.literal(" " + drugType.getDisplayName()));
            } else {
                name = Component.literal(drugType.getColoredName());
            }
        }

        return name.copy().append(Component.translatable("item.packaged.weight_suffix", weight));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        DrugType drugType = getDrugType(stack);
        int weight = getWeight(stack);
        ProductionQuality quality = parseQuality(getQuality(stack));
        ProductionType variant = parseVariant(getVariant(stack));
        long packageDate = getPackageDate(stack);
        double price = calculatePrice(stack);

        // Variante (falls vorhanden)
        if (variant != null) {
            tooltip.add(Component.translatable("tooltip.packaged.type_label").append(variant.getColoredName()));
        }

        // Qualität
        tooltip.add(Component.translatable("tooltip.quality.label").append(quality.getColoredName()));

        // Gewicht
        tooltip.add(Component.translatable("tooltip.packaged.weight", weight));

        // Verpackungsdatum
        long currentDay = level != null ? level.getDayTime() / 24000L : 0;
        long daysOld = currentDay - packageDate;
        if (daysOld > 0) {
            tooltip.add(Component.translatable("tooltip.packaged.packaged_days_ago", daysOld));
        } else {
            tooltip.add(Component.translatable("tooltip.packaged.packaged_today"));
        }

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.packaged.sale_price", String.format("%.2f", price)));
    }
}
