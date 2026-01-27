package de.rolandsw.schedulemc.beer.items;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Zentrale Registrierung aller Bier-Items
 *
 * Struktur:
 * - Hauptprodukt (Bierflasche mit NBT)
 * - Getreide (Gerste, Weizen, Roggen)
 * - Gemälztes Getreide (für Brauvorgang)
 * - Hopfen (verschiedene Formen)
 * - Hefe (verschiedene Typen)
 * - Zwischen- und Endprodukte
 * - Container und Verpackung
 */
public class BeerItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        ForgeRegistries.ITEMS, ScheduleMC.MOD_ID
    );

    // ═══════════════════════════════════════════════════════════
    // HAUPTPRODUKT (Bierflasche mit NBT-Daten)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> BEER_BOTTLE = ITEMS.register(
        "beer_bottle",
        () -> new BeerBottleItem(0.5, new Item.Properties().stacksTo(16))
    );

    // ═══════════════════════════════════════════════════════════
    // GETREIDE (Rohstoffe für Malz)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> BARLEY = ITEMS.register(
        "barley",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.2f).build()))
    );

    public static final RegistryObject<Item> WHEAT_GRAIN = ITEMS.register(
        "wheat_grain",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.2f).build()))
    );

    public static final RegistryObject<Item> RYE = ITEMS.register(
        "rye",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.2f).build()))
    );

    // ═══════════════════════════════════════════════════════════
    // GEMÄLZTES GETREIDE (nach Mälzen)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> MALTED_BARLEY = ITEMS.register(
        "malted_barley",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> MALTED_WHEAT = ITEMS.register(
        "malted_wheat",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> MALTED_RYE = ITEMS.register(
        "malted_rye",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // HOPFEN (verschiedene Verarbeitungsstufen)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> HOPS_CONE = ITEMS.register(
        "hops_cone",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DRIED_HOPS = ITEMS.register(
        "dried_hops",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> HOP_EXTRACT = ITEMS.register(
        "hop_extract",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> HOP_PELLETS = ITEMS.register(
        "hop_pellets",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // HEFE (verschiedene Typen für unterschiedliche Biere)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> YEAST = ITEMS.register(
        "yeast",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> BREWING_YEAST = ITEMS.register(
        "brewing_yeast",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> LAGER_YEAST = ITEMS.register(
        "lager_yeast",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> ALE_YEAST = ITEMS.register(
        "ale_yeast",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // ZWISCHENPRODUKTE (Brau-Prozess)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> MALT_EXTRACT = ITEMS.register(
        "malt_extract",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> WORT_BUCKET = ITEMS.register(
        "wort_bucket",
        () -> new Item(new Item.Properties().stacksTo(16))
    );

    public static final RegistryObject<Item> FERMENTING_BEER = ITEMS.register(
        "fermenting_beer",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> GREEN_BEER = ITEMS.register(
        "green_beer",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> CONDITIONED_BEER = ITEMS.register(
        "conditioned_beer",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // CONTAINER & VERPACKUNG
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> BEER_KEG = ITEMS.register(
        "beer_keg",
        () -> new Item(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<Item> BEER_BOTTLE_EMPTY = ITEMS.register(
        "beer_bottle_empty",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> BEER_CAN_EMPTY = ITEMS.register(
        "beer_can_empty",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> BOTTLE_CAP = ITEMS.register(
        "bottle_cap",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> CROWN_CAP = ITEMS.register(
        "crown_cap",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // ZUSATZSTOFFE & ZUTATEN
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> BREWING_SUGAR = ITEMS.register(
        "brewing_sugar",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> IRISH_MOSS = ITEMS.register(
        "irish_moss",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> BREWING_SALT = ITEMS.register(
        "brewing_salt",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // SPEZIALZUTATEN (für verschiedene Bierstile)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> ROASTED_BARLEY = ITEMS.register(
        "roasted_barley",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> CHOCOLATE_MALT = ITEMS.register(
        "chocolate_malt",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> CARAMEL_MALT = ITEMS.register(
        "caramel_malt",
        () -> new Item(new Item.Properties())
    );
}
