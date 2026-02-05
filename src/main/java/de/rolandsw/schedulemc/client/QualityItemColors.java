package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.beer.items.BeerItems;
import de.rolandsw.schedulemc.cannabis.CannabisQuality;
import de.rolandsw.schedulemc.cannabis.items.CannabisItems;
import de.rolandsw.schedulemc.chocolate.items.ChocolateItems;
import de.rolandsw.schedulemc.coca.items.CocaItems;
import de.rolandsw.schedulemc.coffee.items.CoffeeItems;
import de.rolandsw.schedulemc.cheese.items.CheeseItems;
import de.rolandsw.schedulemc.honey.items.HoneyItems;
import de.rolandsw.schedulemc.items.ModItems;
import de.rolandsw.schedulemc.mdma.items.MDMAItems;
import de.rolandsw.schedulemc.meth.MethQuality;
import de.rolandsw.schedulemc.meth.items.MethItems;
import de.rolandsw.schedulemc.poppy.items.PoppyItems;
import de.rolandsw.schedulemc.tobacco.items.TobaccoItems;
import de.rolandsw.schedulemc.wine.items.WineItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registriert Item Colors für das Qualitäts-Rahmen-System.
 *
 * Layer 0: Item-Textur (keine Färbung)
 * Layer 1: Qualitäts-Rahmen (wird basierend auf Qualität eingefärbt)
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class QualityItemColors {

    // Qualitäts-Farben (ARGB Format)
    private static final int COLOR_QUALITY_0 = 0xFF888888; // Grau - Schlecht/Schwag
    private static final int COLOR_QUALITY_1 = 0xFF55FF55; // Grün - Gut/Mids/Standard
    private static final int COLOR_QUALITY_2 = 0xFF5555FF; // Blau - Sehr Gut/Dank
    private static final int COLOR_QUALITY_3 = 0xFFFFD700; // Gold - Legendär/Top Shelf/Premium
    private static final int COLOR_QUALITY_4 = 0xFFFF55FF; // Pink/Lila - Exotic (Cannabis)
    private static final int COLOR_DEFAULT = 0xFFFFFFFF;   // Weiß - Keine Qualität/Fallback

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Tobacco Items
        event.register(QualityItemColors::getQualityColor,
                TobaccoItems.FERMENTED_VIRGINIA_LEAF.get(),
                TobaccoItems.FERMENTED_BURLEY_LEAF.get(),
                TobaccoItems.FERMENTED_ORIENTAL_LEAF.get(),
                TobaccoItems.FERMENTED_HAVANA_LEAF.get(),
                TobaccoItems.DRIED_VIRGINIA_LEAF.get(),
                TobaccoItems.DRIED_BURLEY_LEAF.get(),
                TobaccoItems.DRIED_ORIENTAL_LEAF.get(),
                TobaccoItems.DRIED_HAVANA_LEAF.get()
        );

        // Cannabis Items
        event.register(QualityItemColors::getQualityColor,
                CannabisItems.FRESH_BUD.get(),
                CannabisItems.DRIED_BUD.get(),
                CannabisItems.TRIMMED_BUD.get(),
                CannabisItems.CURED_BUD.get(),
                CannabisItems.HASH.get(),
                CannabisItems.CANNABIS_OIL.get()
        );

        // Coca Items (Cocaine, Crack)
        event.register(QualityItemColors::getQualityColor,
                CocaItems.COCAINE.get(),
                CocaItems.CRACK_ROCK.get()
        );

        // Poppy Items (Opium, Morphine, Heroin)
        event.register(QualityItemColors::getQualityColor,
                PoppyItems.RAW_OPIUM.get(),
                PoppyItems.MORPHINE.get(),
                PoppyItems.HEROIN.get()
        );

        // Meth Items
        event.register(QualityItemColors::getQualityColor,
                MethItems.ROH_METH.get(),
                MethItems.METH_PASTE.get(),
                MethItems.KRISTALL_METH.get(),
                MethItems.METH.get()
        );

        // MDMA Items
        event.register(QualityItemColors::getQualityColor,
                MDMAItems.MDMA_BASE.get(),
                MDMAItems.MDMA_KRISTALL.get()
        );

        // Packaged Drug (universal)
        event.register(QualityItemColors::getQualityColor,
                ModItems.PACKAGED_DRUG.get()
        );

        // ═══════════════════════════════════════════════════════════
        // LEGALE PRODUKTE
        // ═══════════════════════════════════════════════════════════

        // Coffee Items (7)
        event.register(QualityItemColors::getQualityColor,
                CoffeeItems.ROASTED_COFFEE_BEANS.get(),
                CoffeeItems.GROUND_COFFEE.get(),
                CoffeeItems.COFFEE_PACKAGE_250G.get(),
                CoffeeItems.COFFEE_PACKAGE_500G.get(),
                CoffeeItems.COFFEE_PACKAGE_1KG.get(),
                CoffeeItems.BREWED_COFFEE.get(),
                CoffeeItems.ESPRESSO.get()
        );

        // Wine Items (8)
        event.register(QualityItemColors::getQualityColor,
                WineItems.WINE_BOTTLE_375ML.get(),
                WineItems.WINE_BOTTLE_750ML.get(),
                WineItems.WINE_BOTTLE_1500ML.get(),
                WineItems.GLASS_OF_WINE.get(),
                WineItems.RIESLING_GRAPES.get(),
                WineItems.SPAETBURGUNDER_GRAPES.get(),
                WineItems.CHARDONNAY_GRAPES.get(),
                WineItems.MERLOT_GRAPES.get()
        );

        // Cheese Items (13)
        event.register(QualityItemColors::getQualityColor,
                CheeseItems.CHEESE_CURD.get(),
                CheeseItems.CHEESE_WHEEL.get(),
                CheeseItems.GOUDA_WHEEL.get(),
                CheeseItems.EMMENTAL_WHEEL.get(),
                CheeseItems.CAMEMBERT_WHEEL.get(),
                CheeseItems.PARMESAN_WHEEL.get(),
                CheeseItems.CHEESE_WEDGE.get(),
                CheeseItems.GOUDA_WEDGE.get(),
                CheeseItems.EMMENTAL_WEDGE.get(),
                CheeseItems.CAMEMBERT_WEDGE.get(),
                CheeseItems.PARMESAN_WEDGE.get(),
                CheeseItems.SMOKED_CHEESE.get(),
                CheeseItems.HERB_CHEESE.get()
        );

        // Honey Items (3)
        event.register(QualityItemColors::getQualityColor,
                HoneyItems.HONEY_JAR_250G.get(),
                HoneyItems.HONEY_JAR_500G.get(),
                HoneyItems.HONEY_JAR_1KG.get()
        );

        // Chocolate Items (3)
        event.register(QualityItemColors::getQualityColor,
                ChocolateItems.CHOCOLATE_BAR_100G.get(),
                ChocolateItems.CHOCOLATE_BAR_200G.get(),
                ChocolateItems.CHOCOLATE_BAR_500G.get()
        );

        // Beer Items (1)
        event.register(QualityItemColors::getQualityColor,
                BeerItems.BEER_BOTTLE.get()
        );
    }

    /**
     * Gibt die Farbe für einen bestimmten Tint-Index zurück.
     *
     * @param stack Der ItemStack
     * @param tintIndex 0 = Basis-Textur, 1 = Qualitäts-Rahmen
     * @return ARGB Farbwert
     */
    private static int getQualityColor(ItemStack stack, int tintIndex) {
        // Layer 0 (Basis-Textur) - keine Färbung
        if (tintIndex == 0) {
            return COLOR_DEFAULT;
        }

        // Layer 1 (Qualitäts-Rahmen) - Qualitätsfarbe
        if (tintIndex == 1) {
            int qualityLevel = extractQualityLevel(stack);
            return getColorForLevel(qualityLevel);
        }

        return COLOR_DEFAULT;
    }

    /**
     * Extrahiert das Qualitäts-Level aus einem ItemStack.
     * Unterstützt verschiedene NBT-Formate.
     */
    private static int extractQualityLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 1; // Default: Gut/Standard
        }

        // Versuche "Quality" Tag (TobaccoQuality, etc.)
        if (tag.contains("Quality")) {
            String qualityStr = tag.getString("Quality");
            return parseQualityLevel(qualityStr);
        }

        // Versuche "CannabisQuality" Tag
        if (tag.contains("CannabisQuality")) {
            String qualityStr = tag.getString("CannabisQuality");
            return parseCannabisQualityLevel(qualityStr);
        }

        // Versuche "MethQuality" Tag
        if (tag.contains("MethQuality")) {
            String qualityStr = tag.getString("MethQuality");
            return parseMethQualityLevel(qualityStr);
        }

        // Versuche PackagedDrugItem Format "ClassName.VALUE"
        if (tag.contains("DrugType")) {
            // Lese Qualität aus PackagedDrugItem
            String qualityStr = tag.getString("Quality");
            if (qualityStr.contains(".")) {
                String[] parts = qualityStr.split("\\.");
                if (parts.length >= 2) {
                    return parseQualityLevel(parts[1]);
                }
            }
            return parseQualityLevel(qualityStr);
        }

        return 1; // Default
    }

    /**
     * Parst Standard-Qualitätsnamen und gibt das normalisierte Qualitäts-Level zurück.
     * Die Farben sind:
     * - 0: Grau (Schlecht/Schwag/Standard)
     * - 1: Grün (Gut/Mids)
     * - 2: Blau (Sehr Gut/Dank)
     * - 3: Gold (Legendär/Top Shelf/Premium/Fishscale/Blue Sky)
     * - 4: Pink (Exotic)
     */
    private static int parseQualityLevel(String qualityName) {
        if (qualityName == null) return 1;

        return switch (qualityName.toUpperCase()) {
            case "SCHLECHT", "SCHWAG" -> 0;
            case "STANDARD" -> 0; // Meth Standard = niedrigste
            case "GUT", "MIDS" -> 1;
            case "SEHR_GUT", "DANK" -> 2;
            case "LEGENDAER", "LEGENDÄR", "TOP_SHELF", "PREMIUM", "FISHSCALE", "BLUE_SKY" -> 3;
            case "EXOTIC" -> 4;
            default -> 1;
        };
    }

    /**
     * Parst Cannabis-Qualitätsnamen
     */
    private static int parseCannabisQualityLevel(String qualityName) {
        try {
            CannabisQuality quality = CannabisQuality.valueOf(qualityName);
            return quality.getLevel();
        } catch (Exception e) {
            return parseQualityLevel(qualityName);
        }
    }

    /**
     * Parst Meth-Qualitätsnamen.
     * Meth hat nur 3 Stufen (0=Standard, 1=Gut, 2=Blue Sky),
     * wir mappen sie auf unsere Farbstufen.
     */
    private static int parseMethQualityLevel(String qualityName) {
        try {
            MethQuality quality = MethQuality.valueOf(qualityName);
            // Meth Level 0 -> Farbe 0 (Grau)
            // Meth Level 1 -> Farbe 1 (Grün)
            // Meth Level 2 (Blue Sky) -> Farbe 3 (Gold) - höchste Qualität
            return switch (quality) {
                case STANDARD -> 0;
                case GUT -> 1;
                case BLUE_SKY -> 3;
            };
        } catch (Exception e) {
            return parseQualityLevel(qualityName);
        }
    }

    /**
     * Gibt die Farbe für ein Qualitäts-Level zurück.
     */
    private static int getColorForLevel(int level) {
        return switch (level) {
            case 0 -> COLOR_QUALITY_0; // Grau
            case 1 -> COLOR_QUALITY_1; // Grün
            case 2 -> COLOR_QUALITY_2; // Blau
            case 3 -> COLOR_QUALITY_3; // Gold
            case 4 -> COLOR_QUALITY_4; // Pink
            default -> COLOR_DEFAULT;
        };
    }
}
