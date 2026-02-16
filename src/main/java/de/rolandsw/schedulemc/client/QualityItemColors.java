package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.beer.items.BeerBottleItem;
import de.rolandsw.schedulemc.beer.items.BeerItems;
import de.rolandsw.schedulemc.cannabis.items.*;
import de.rolandsw.schedulemc.cheese.items.CheeseCurdItem;
import de.rolandsw.schedulemc.cheese.items.CheeseItems;
import de.rolandsw.schedulemc.cheese.items.CheeseWedgeItem;
import de.rolandsw.schedulemc.cheese.items.CheeseWheelItem;
import de.rolandsw.schedulemc.chocolate.items.ChocolateBarItem;
import de.rolandsw.schedulemc.chocolate.items.ChocolateItems;
import de.rolandsw.schedulemc.coca.items.*;
import de.rolandsw.schedulemc.coffee.items.CoffeeItems;
import de.rolandsw.schedulemc.coffee.items.GroundCoffeeItem;
import de.rolandsw.schedulemc.coffee.items.RoastedCoffeeBeanItem;
import de.rolandsw.schedulemc.honey.items.HoneyItems;
import de.rolandsw.schedulemc.honey.items.HoneyJarItem;
import de.rolandsw.schedulemc.items.ModItems;
import de.rolandsw.schedulemc.lsd.items.BlotterItem;
import de.rolandsw.schedulemc.lsd.items.LSDItems;
import de.rolandsw.schedulemc.lsd.items.LSDLoesungItem;
import de.rolandsw.schedulemc.mdma.items.*;
import de.rolandsw.schedulemc.meth.items.*;
import de.rolandsw.schedulemc.mushroom.items.DriedMushroomItem;
import de.rolandsw.schedulemc.mushroom.items.FreshMushroomItem;
import de.rolandsw.schedulemc.mushroom.items.MushroomItems;
import de.rolandsw.schedulemc.poppy.items.*;
import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import de.rolandsw.schedulemc.tobacco.items.*;
import de.rolandsw.schedulemc.wine.items.GrapeItem;
import de.rolandsw.schedulemc.wine.items.WineBottleItem;
import de.rolandsw.schedulemc.wine.items.WineItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
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

        // Tobacco Items (12 - Fresh, Dried, Fermented)
        event.register(QualityItemColors::getQualityColor,
                TobaccoItems.FRESH_VIRGINIA_LEAF.get(),
                TobaccoItems.FRESH_BURLEY_LEAF.get(),
                TobaccoItems.FRESH_ORIENTAL_LEAF.get(),
                TobaccoItems.FRESH_HAVANA_LEAF.get(),
                TobaccoItems.DRIED_VIRGINIA_LEAF.get(),
                TobaccoItems.DRIED_BURLEY_LEAF.get(),
                TobaccoItems.DRIED_ORIENTAL_LEAF.get(),
                TobaccoItems.DRIED_HAVANA_LEAF.get(),
                TobaccoItems.FERMENTED_VIRGINIA_LEAF.get(),
                TobaccoItems.FERMENTED_BURLEY_LEAF.get(),
                TobaccoItems.FERMENTED_ORIENTAL_LEAF.get(),
                TobaccoItems.FERMENTED_HAVANA_LEAF.get()
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

        // Coca Items (8 - Fresh Leaves, Paste (3 types), Cocaine, Crack)
        event.register(QualityItemColors::getQualityColor,
                CocaItems.FRESH_BOLIVIANISCH_LEAF.get(),
                CocaItems.FRESH_KOLUMBIANISCH_LEAF.get(),
                CocaItems.FRESH_PERUANISCH_LEAF.get(),
                CocaItems.COCA_PASTE_BOLIVIANISCH.get(),
                CocaItems.COCA_PASTE_KOLUMBIANISCH.get(),
                CocaItems.COCA_PASTE_PERUANISCH.get(),
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

        // MDMA Items (3 - Base, Kristall, Ecstasy Pills)
        event.register(QualityItemColors::getQualityColor,
                MDMAItems.MDMA_BASE.get(),
                MDMAItems.MDMA_KRISTALL.get(),
                MDMAItems.ECSTASY_PILL.get()
        );

        // LSD Items (2 - Blotter, Lösung)
        event.register(QualityItemColors::getQualityColor,
                LSDItems.BLOTTER.get(),
                LSDItems.LSD_LOESUNG.get()
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

        // Coffee Items (2 - nur Beans und Ground haben Quality)
        event.register(QualityItemColors::getQualityColor,
                CoffeeItems.ROASTED_COFFEE_BEANS.get(),
                CoffeeItems.GROUND_COFFEE.get()
        );

        // Wine Items (7 - Bottles und Grapes haben Quality, nicht Glass)
        event.register(QualityItemColors::getQualityColor,
                WineItems.WINE_BOTTLE_375ML.get(),
                WineItems.WINE_BOTTLE_750ML.get(),
                WineItems.WINE_BOTTLE_1500ML.get(),
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
     *
     * LÖSUNG 2: Ruft die Item-spezifische getQuality() Methode auf!
     * Dies garantiert 100% Konsistenz zwischen Tooltip und Rahmenfarbe,
     * da beide die GLEICHE Methode verwenden!
     *
     * Alle Qualitäts-Enums verwenden das einheitliche 4-Stufen-System:
     * SCHLECHT (0), GUT (1), SEHR_GUT (2), LEGENDAER (3)
     */
    private static int extractQualityLevel(ItemStack stack) {
        Item item = stack.getItem();

        // === METH ITEMS ===
        if (item instanceof MethItem) {
            return MethItem.getQuality(stack).ordinal();
        }
        if (item instanceof KristallMethItem) {
            return KristallMethItem.getQuality(stack).ordinal();
        }
        if (item instanceof RohMethItem) {
            return RohMethItem.getQuality(stack).ordinal();
        }
        if (item instanceof MethPasteItem) {
            return MethPasteItem.getQuality(stack).ordinal();
        }

        // === MDMA ITEMS ===
        if (item instanceof MDMAKristallItem) {
            return MDMAKristallItem.getQuality(stack).ordinal();
        }
        if (item instanceof MDMABaseItem) {
            return MDMABaseItem.getQuality(stack).ordinal();
        }
        if (item instanceof EcstasyPillItem) {
            return EcstasyPillItem.getQuality(stack).ordinal();
        }

        // === CANNABIS ITEMS ===
        if (item instanceof CuredBudItem) {
            return CuredBudItem.getQuality(stack).ordinal();
        }
        if (item instanceof DriedBudItem) {
            return DriedBudItem.getQuality(stack).ordinal();
        }
        if (item instanceof FreshBudItem) {
            return FreshBudItem.getQuality(stack).ordinal();
        }
        if (item instanceof TrimmedBudItem) {
            return TrimmedBudItem.getQuality(stack).ordinal();
        }
        if (item instanceof HashItem) {
            return HashItem.getQuality(stack).ordinal();
        }
        if (item instanceof CannabisOilItem) {
            return CannabisOilItem.getQuality(stack).ordinal();
        }

        // === TOBACCO ITEMS ===
        if (item instanceof FreshTobaccoLeafItem) {
            return FreshTobaccoLeafItem.getQuality(stack).ordinal();
        }
        if (item instanceof DriedTobaccoLeafItem) {
            return DriedTobaccoLeafItem.getQuality(stack).ordinal();
        }
        if (item instanceof FermentedTobaccoLeafItem) {
            return FermentedTobaccoLeafItem.getQuality(stack).ordinal();
        }

        // === COCA ITEMS ===
        if (item instanceof CocaineItem) {
            return CocaineItem.getQuality(stack).ordinal();
        }
        if (item instanceof CocaPasteItem) {
            return CocaPasteItem.getQuality(stack).ordinal();
        }
        if (item instanceof FreshCocaLeafItem) {
            return FreshCocaLeafItem.getQuality(stack).ordinal();
        }
        if (item instanceof CrackRockItem) {
            return CrackRockItem.getQuality(stack).ordinal();
        }

        // === POPPY/OPIATE ITEMS ===
        if (item instanceof HeroinItem) {
            return HeroinItem.getQuality(stack).ordinal();
        }
        if (item instanceof MorphineItem) {
            return MorphineItem.getQuality(stack).ordinal();
        }
        if (item instanceof RawOpiumItem) {
            return RawOpiumItem.getQuality(stack).ordinal();
        }
        if (item instanceof PoppyPodItem) {
            return PoppyPodItem.getQuality(stack).ordinal();
        }

        // === WINE ITEMS ===
        if (item instanceof WineBottleItem) {
            return WineBottleItem.getQuality(stack).ordinal();
        }
        if (item instanceof GrapeItem) {
            return GrapeItem.getQuality(stack).ordinal();
        }

        // === BEER ITEMS ===
        if (item instanceof BeerBottleItem) {
            return BeerBottleItem.getQuality(stack).ordinal();
        }

        // === CHEESE ITEMS ===
        if (item instanceof CheeseCurdItem) {
            return CheeseCurdItem.getQuality(stack).ordinal();
        }
        if (item instanceof CheeseWedgeItem) {
            return CheeseWedgeItem.getQuality(stack).ordinal();
        }
        if (item instanceof CheeseWheelItem) {
            return CheeseWheelItem.getQuality(stack).ordinal();
        }

        // === COFFEE ITEMS ===
        if (item instanceof RoastedCoffeeBeanItem) {
            return RoastedCoffeeBeanItem.getQuality(stack).ordinal();
        }
        if (item instanceof GroundCoffeeItem) {
            return GroundCoffeeItem.getQuality(stack).ordinal();
        }

        // === MUSHROOM ITEMS ===
        if (item instanceof FreshMushroomItem) {
            return FreshMushroomItem.getQuality(stack).ordinal();
        }
        if (item instanceof DriedMushroomItem) {
            return DriedMushroomItem.getQuality(stack).ordinal();
        }

        // === OTHER ITEMS ===
        if (item instanceof HoneyJarItem) {
            return HoneyJarItem.getQuality(stack).ordinal();
        }
        if (item instanceof ChocolateBarItem) {
            return ChocolateBarItem.getQuality(stack).ordinal();
        }
        if (item instanceof PackagedDrugItem) {
            // PackagedDrugItem speichert Quality als String und parst es dann
            ProductionQuality quality = PackagedDrugItem.parseQuality(PackagedDrugItem.getQuality(stack));
            return quality.getLevel(); // Interface hat getLevel() statt ordinal()
        }

        // === LSD ITEMS (special handling for dosage) ===
        if (item instanceof BlotterItem) {
            return BlotterItem.getDosage(stack).ordinal();
        }
        if (item instanceof LSDLoesungItem) {
            return LSDLoesungItem.getDosage(stack).ordinal();
        }

        // Fallback: Item hat keine Quality
        return 0; // Default: SCHLECHT
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
