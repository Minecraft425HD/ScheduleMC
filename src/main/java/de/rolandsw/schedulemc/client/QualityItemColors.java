package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.beer.items.BeerItems;
import de.rolandsw.schedulemc.cannabis.items.CannabisItems;
import de.rolandsw.schedulemc.chocolate.items.ChocolateItems;
import de.rolandsw.schedulemc.coca.items.CocaItems;
import de.rolandsw.schedulemc.coffee.items.CoffeeItems;
import de.rolandsw.schedulemc.cheese.items.CheeseItems;
import de.rolandsw.schedulemc.honey.items.HoneyItems;
import de.rolandsw.schedulemc.items.ModItems;
import de.rolandsw.schedulemc.lsd.items.LSDItems;
import de.rolandsw.schedulemc.mdma.items.MDMAItems;
import de.rolandsw.schedulemc.meth.items.MethItems;
import de.rolandsw.schedulemc.mushroom.items.MushroomItems;
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
 * Einheitliches 4-Stufen-System:
 * - Level 0: SCHLECHT (Grau)
 * - Level 1: GUT (Gelb)
 * - Level 2: SEHR_GUT (Grün)
 * - Level 3: LEGENDAER (Gold)
 *
 * Layer 0: Item-Textur (keine Färbung)
 * Layer 1: Qualitäts-Rahmen (wird basierend auf Qualität eingefärbt)
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class QualityItemColors {

    // Qualitäts-Farben (ARGB Format) - Einheitliches 4-Stufen-System
    // Farben entsprechen exakt den Minecraft Chat-Codes!
    private static final int COLOR_QUALITY_0 = 0xFFFF5555; // Rot - SCHLECHT (§c)
    private static final int COLOR_QUALITY_1 = 0xFFFFFF55; // Gelb - GUT (§e)
    private static final int COLOR_QUALITY_2 = 0xFF55FF55; // Grün - SEHR_GUT (§a)
    private static final int COLOR_QUALITY_3 = 0xFFFFAA00; // Gold - LEGENDAER (§6)
    private static final int COLOR_DEFAULT = 0xFFFFFFFF;   // Weiß - Keine Qualität/Fallback

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // ═══════════════════════════════════════════════════════════
        // ILLEGALE PRODUKTE
        // ═══════════════════════════════════════════════════════════

        // Tobacco Items (8)
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

        // Cannabis Items (6)
        event.register(QualityItemColors::getQualityColor,
                CannabisItems.FRESH_BUD.get(),
                CannabisItems.DRIED_BUD.get(),
                CannabisItems.TRIMMED_BUD.get(),
                CannabisItems.CURED_BUD.get(),
                CannabisItems.HASH.get(),
                CannabisItems.CANNABIS_OIL.get()
        );

        // Coca Items (2)
        event.register(QualityItemColors::getQualityColor,
                CocaItems.COCAINE.get(),
                CocaItems.CRACK_ROCK.get()
        );

        // Poppy Items (4)
        event.register(QualityItemColors::getQualityColor,
                PoppyItems.POPPY_POD.get(),
                PoppyItems.RAW_OPIUM.get(),
                PoppyItems.MORPHINE.get(),
                PoppyItems.HEROIN.get()
        );

        // Meth Items (4)
        event.register(QualityItemColors::getQualityColor,
                MethItems.ROH_METH.get(),
                MethItems.METH_PASTE.get(),
                MethItems.KRISTALL_METH.get(),
                MethItems.METH.get()
        );

        // MDMA Items (2)
        event.register(QualityItemColors::getQualityColor,
                MDMAItems.MDMA_BASE.get(),
                MDMAItems.MDMA_KRISTALL.get()
        );

        // LSD Items (1)
        event.register(QualityItemColors::getQualityColor,
                LSDItems.BLOTTER.get()
        );

        // Mushroom Items (6)
        event.register(QualityItemColors::getQualityColor,
                MushroomItems.FRESH_CUBENSIS.get(),
                MushroomItems.FRESH_AZURESCENS.get(),
                MushroomItems.FRESH_MEXICANA.get(),
                MushroomItems.DRIED_CUBENSIS.get(),
                MushroomItems.DRIED_AZURESCENS.get(),
                MushroomItems.DRIED_MEXICANA.get()
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
     * Alle Qualitäts-Enums verwenden jetzt das einheitliche 4-Stufen-System:
     * SCHLECHT (0), GUT (1), SEHR_GUT (2), LEGENDAER (3)
     */
    private static int extractQualityLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 1; // Default: GUT
        }

        // Standard "Quality" Tag (verwendet von den meisten Items)
        if (tag.contains("Quality")) {
            String qualityStr = tag.getString("Quality");
            int level = parseUnifiedQualityLevel(qualityStr);

            // DEBUG: Log wenn "poor" im String vorkommt aber nicht Level 0 ist
            if (qualityStr.toLowerCase().contains("poor") && level != 0) {
                System.out.println("[QualityItemColors] WARNING: Quality string '" + qualityStr +
                    "' contains 'poor' but was parsed as level " + level + " instead of 0!");
                System.out.println("[QualityItemColors] Item: " + stack.getItem().toString());
                System.out.println("[QualityItemColors] Full NBT: " + tag.toString());
            }

            return level;
        }

        // CannabisQuality Tag
        if (tag.contains("CannabisQuality")) {
            String qualityStr = tag.getString("CannabisQuality");
            return parseUnifiedQualityLevel(qualityStr);
        }

        // MethQuality Tag
        if (tag.contains("MethQuality")) {
            String qualityStr = tag.getString("MethQuality");
            return parseUnifiedQualityLevel(qualityStr);
        }

        // LSD Dosage Tag
        if (tag.contains("LSDDosage")) {
            String qualityStr = tag.getString("LSDDosage");
            return parseUnifiedQualityLevel(qualityStr);
        }

        // PackagedDrugItem Format "ClassName.VALUE"
        if (tag.contains("DrugType")) {
            String qualityStr = tag.getString("Quality");
            if (qualityStr.contains(".")) {
                String[] parts = qualityStr.split("\\.");
                if (parts.length >= 2) {
                    return parseUnifiedQualityLevel(parts[1]);
                }
            }
            return parseUnifiedQualityLevel(qualityStr);
        }

        return 1; // Default: GUT
    }

    /**
     * Parst einheitliche Qualitätsnamen und gibt das Level zurück.
     *
     * Einheitliches 4-Stufen-System:
     * - 0: SCHLECHT
     * - 1: GUT
     * - 2: SEHR_GUT
     * - 3: LEGENDAER
     */
    private static int parseUnifiedQualityLevel(String qualityName) {
        if (qualityName == null || qualityName.isEmpty()) return 1;

        // Normalisiere Input: trim, uppercase, Umlaute konvertieren
        String normalized = qualityName.trim().toUpperCase()
            .replace("Ä", "AE")
            .replace("Ö", "OE")
            .replace("Ü", "UE")
            .replace("ß", "SS");

        return switch (normalized) {
            // === LEVEL 0: SCHLECHT / POOR ===
            // Deutsche Namen
            case "SCHLECHT" -> 0;
            // Englische Namen
            case "POOR", "BAD", "INFERIOR", "LOW" -> 0;
            // Übersetzungs-Varianten
            case "§CPOOR", "§C§LPOOR" -> 0;

            // === LEVEL 1: GUT / GOOD ===
            // Deutsche Namen
            case "GUT" -> 1;
            // Englische Namen
            case "GOOD", "AVERAGE", "STANDARD", "NORMAL", "DECENT" -> 1;
            // Übersetzungs-Varianten
            case "§EGOOD", "§E§LGOOD" -> 1;

            // === LEVEL 2: SEHR_GUT / EXCELLENT ===
            // Deutsche Namen
            case "SEHR_GUT", "SEHR GUT", "SEHRGUT" -> 2;
            // Englische Namen
            case "VERY_GOOD", "VERY GOOD", "EXCELLENT", "SUPERIOR", "HIGH" -> 2;
            // Übersetzungs-Varianten
            case "§AEXCELLENT", "§A§LEXCELLENT" -> 2;

            // === LEVEL 3: LEGENDAER / LEGENDARY ===
            // Deutsche Namen (LEGENDÄR wird zu LEGENDAER normalisiert)
            case "LEGENDAER" -> 3;
            // Englische Namen
            case "LEGENDARY", "PREMIUM", "EXCEPTIONAL", "EPIC" -> 3;
            // Übersetzungs-Varianten
            case "§6LEGENDARY", "§6§LLEGENDARY" -> 3;

            default -> {
                // Versuche Format "ClassName.QUALITY" zu parsen
                if (normalized.contains(".")) {
                    String[] parts = normalized.split("\\.");
                    if (parts.length >= 2) {
                        // Rekursiver Aufruf mit dem Teil nach dem Punkt
                        int result = parseUnifiedQualityLevel(parts[parts.length - 1]);
                        if (result != 1 || parts[parts.length - 1].equals("GUT") || parts[parts.length - 1].equals("GOOD")) {
                            yield result;
                        }
                    }
                }
                yield 1; // Default: GUT
            }
        };
    }

    /**
     * Gibt die Farbe für ein Qualitäts-Level zurück.
     */
    private static int getColorForLevel(int level) {
        return switch (level) {
            case 0 -> COLOR_QUALITY_0; // Rot (§c) - SCHLECHT
            case 1 -> COLOR_QUALITY_1; // Gelb (§e) - GUT
            case 2 -> COLOR_QUALITY_2; // Grün (§a) - SEHR_GUT
            case 3 -> COLOR_QUALITY_3; // Gold (§6) - LEGENDAER
            default -> COLOR_DEFAULT;
        };
    }
}
