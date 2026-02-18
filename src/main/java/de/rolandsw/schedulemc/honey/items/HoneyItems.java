package de.rolandsw.schedulemc.honey.items;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Zentrale Registrierung aller Honig-Items
 *
 * Struktur:
 * - Honigglas (1kg, mit NBT-Daten)
 * - Rohmaterial (Waben, Pollen, etc.)
 * - Zwischenprodukte (gefiltert, verarbeitet)
 * - Nebenprodukte (Bienenwachs, Propolis)
 * - Verpackungsmaterial (Gläser, Deckel)
 */
public class HoneyItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        ForgeRegistries.ITEMS, ScheduleMC.MOD_ID
    );

    // ═══════════════════════════════════════════════════════════
    // HONIGGLAS (Fertigprodukt mit NBT-Daten)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> HONEY_JAR = ITEMS.register(
        "honey_jar",
        () -> new HoneyJarItem(1.0, new Item.Properties().stacksTo(16))
    );

    // ═══════════════════════════════════════════════════════════
    // ROHMATERIAL - HONIGWABEN
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> RAW_HONEYCOMB = ITEMS.register(
        "raw_honeycomb",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.2f).build()))
    );

    public static final RegistryObject<Item> FILTERED_HONEYCOMB = ITEMS.register(
        "filtered_honeycomb",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> HONEYCOMB_CHUNK = ITEMS.register(
        "honeycomb_chunk",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).build()))
    );

    // ═══════════════════════════════════════════════════════════
    // ROHMATERIAL - HONIG (Flüssig)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> RAW_HONEY_BUCKET = ITEMS.register(
        "raw_honey_bucket",
        () -> new Item(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<Item> FILTERED_HONEY_BUCKET = ITEMS.register(
        "filtered_honey_bucket",
        () -> new Item(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<Item> LIQUID_HONEY_BOTTLE = ITEMS.register(
        "liquid_honey_bottle",
        () -> new Item(new Item.Properties().stacksTo(16))
    );

    // ═══════════════════════════════════════════════════════════
    // NEBENPRODUKTE
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> BEESWAX = ITEMS.register(
        "beeswax",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> BEESWAX_BLOCK = ITEMS.register(
        "beeswax_block",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> PROPOLIS = ITEMS.register(
        "propolis",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> POLLEN = ITEMS.register(
        "pollen",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> ROYAL_JELLY = ITEMS.register(
        "royal_jelly",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.8f).build()))
    );

    // ═══════════════════════════════════════════════════════════
    // VERPACKUNGSMATERIAL
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> GLASS_JAR = ITEMS.register(
        "glass_jar",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> JAR_LID = ITEMS.register(
        "jar_lid",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> JAR_LID_GOLD = ITEMS.register(
        "jar_lid_gold",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // ZWISCHENPRODUKTE - VERARBEITETER HONIG
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> CREAMED_HONEY = ITEMS.register(
        "creamed_honey",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> CRYSTALLIZED_HONEY = ITEMS.register(
        "crystallized_honey",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.5f).build()))
    );

    public static final RegistryObject<Item> HONEY_CRYSTALS = ITEMS.register(
        "honey_crystals",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.4f).build()))
    );

    // ═══════════════════════════════════════════════════════════
    // BIENENZUCHT - AUSRÜSTUNG & WERKZEUGE
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> BEEKEEPER_SUIT = ITEMS.register(
        "beekeeper_suit",
        () -> new Item(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<Item> SMOKER = ITEMS.register(
        "smoker",
        () -> new Item(new Item.Properties().stacksTo(1).durability(256))
    );

    public static final RegistryObject<Item> HIVE_TOOL = ITEMS.register(
        "hive_tool",
        () -> new Item(new Item.Properties().stacksTo(1).durability(512))
    );

    // ═══════════════════════════════════════════════════════════
    // KONSUMGÜTER - HONIG ZUM ESSEN
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> HONEY_CANDY = ITEMS.register(
        "honey_candy",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.5f).fast().build()))
    );

    public static final RegistryObject<Item> HONEYCOMB_TREAT = ITEMS.register(
        "honeycomb_treat",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6f).build()))
    );
}
