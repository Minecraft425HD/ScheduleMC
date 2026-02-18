package de.rolandsw.schedulemc.wine.items;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.wine.WineType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Zentrale Registrierung aller Wein-Items
 *
 * Struktur:
 * - 4 Traubensetzlinge (zum Pflanzen)
 * - 4 Weintrauben-Typen (Ernte)
 * - 4 Maische-Typen (zerdrückt)
 * - 4 Traubensaft-Typen (gepresst)
 * - Junger Wein (ungereift)
 * - Weinflasche (0.75L)
 * - Leere Flaschen (zum Abfüllen)
 * - Wein-Konsum (mit Buffs)
 */
public class WineItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        ForgeRegistries.ITEMS, ScheduleMC.MOD_ID
    );

    // ═══════════════════════════════════════════════════════════
    // SETZLINGE (zum Pflanzen)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> RIESLING_GRAPE_SEEDLING = ITEMS.register(
        "riesling_grape_seedling",
        () -> new GrapeSeedlingItem(WineType.RIESLING, new Item.Properties())
    );

    public static final RegistryObject<Item> SPAETBURGUNDER_GRAPE_SEEDLING = ITEMS.register(
        "spaetburgunder_grape_seedling",
        () -> new GrapeSeedlingItem(WineType.SPAETBURGUNDER, new Item.Properties())
    );

    public static final RegistryObject<Item> CHARDONNAY_GRAPE_SEEDLING = ITEMS.register(
        "chardonnay_grape_seedling",
        () -> new GrapeSeedlingItem(WineType.CHARDONNAY, new Item.Properties())
    );

    public static final RegistryObject<Item> MERLOT_GRAPE_SEEDLING = ITEMS.register(
        "merlot_grape_seedling",
        () -> new GrapeSeedlingItem(WineType.MERLOT, new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // WEINTRAUBEN (Ernte von Pflanzen)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> RIESLING_GRAPES = ITEMS.register(
        "riesling_grapes",
        () -> new GrapeItem(WineType.RIESLING, new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).build()))
    );

    public static final RegistryObject<Item> SPAETBURGUNDER_GRAPES = ITEMS.register(
        "spaetburgunder_grapes",
        () -> new GrapeItem(WineType.SPAETBURGUNDER, new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).build()))
    );

    public static final RegistryObject<Item> CHARDONNAY_GRAPES = ITEMS.register(
        "chardonnay_grapes",
        () -> new GrapeItem(WineType.CHARDONNAY, new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).build()))
    );

    public static final RegistryObject<Item> MERLOT_GRAPES = ITEMS.register(
        "merlot_grapes",
        () -> new GrapeItem(WineType.MERLOT, new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).build()))
    );

    // ═══════════════════════════════════════════════════════════
    // MAISCHE (zerdrückte Trauben)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> RIESLING_MASH = ITEMS.register(
        "riesling_mash",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> SPAETBURGUNDER_MASH = ITEMS.register(
        "spaetburgunder_mash",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> CHARDONNAY_MASH = ITEMS.register(
        "chardonnay_mash",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> MERLOT_MASH = ITEMS.register(
        "merlot_mash",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // TRAUBENSAFT (gepresst aus Maische)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> RIESLING_JUICE = ITEMS.register(
        "riesling_juice",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> SPAETBURGUNDER_JUICE = ITEMS.register(
        "spaetburgunder_juice",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> CHARDONNAY_JUICE = ITEMS.register(
        "chardonnay_juice",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> MERLOT_JUICE = ITEMS.register(
        "merlot_juice",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // GÄRENDE MAISCHE & JUNGER WEIN
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> FERMENTING_WINE = ITEMS.register(
        "fermenting_wine",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> YOUNG_WINE = ITEMS.register(
        "young_wine",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // WEINFLASCHE (Fertigprodukt mit NBT-Daten)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> WINE_BOTTLE = ITEMS.register(
        "wine_bottle",
        () -> new WineBottleItem(0.75, new Item.Properties().stacksTo(16))
    );

    // ═══════════════════════════════════════════════════════════
    // LEERE WEINFLASCHE (zum Abfüllen)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> EMPTY_WINE_BOTTLE = ITEMS.register(
        "empty_wine_bottle",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // KONSUMGÜTER (Wein zum Trinken mit Buffs)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> GLASS_OF_WINE = ITEMS.register(
        "glass_of_wine",
        () -> new WineGlassItem(new Item.Properties().stacksTo(1))
    );
}
