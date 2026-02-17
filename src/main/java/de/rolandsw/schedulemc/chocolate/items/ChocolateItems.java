package de.rolandsw.schedulemc.chocolate.items;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Zentrale Registrierung aller Schokoladen-Items
 *
 * Struktur:
 * - Kakao-Rohstoffe (Bohnen, Nibs, Masse)
 * - Verarbeitungsprodukte (Butter, Pulver)
 * - Zusatzstoffe (Milchpulver, Zucker, Aromen)
 * - Zutaten (Nüsse, Früchte)
 * - Werkzeuge und Verpackung
 * - Schokoladentafeln (3 Größen)
 */
public class ChocolateItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        ForgeRegistries.ITEMS, ScheduleMC.MOD_ID
    );

    // ═══════════════════════════════════════════════════════════
    // KAKAO-ROHSTOFFE
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> COCOA_BEANS = ITEMS.register(
        "cocoa_beans_raw",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1f).build()))
    );

    public static final RegistryObject<Item> ROASTED_COCOA_BEANS = ITEMS.register(
        "roasted_cocoa_beans",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2f).build()))
    );

    public static final RegistryObject<Item> COCOA_NIBS = ITEMS.register(
        "cocoa_nibs",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).build()))
    );

    public static final RegistryObject<Item> COCOA_MASS = ITEMS.register(
        "cocoa_mass",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> COCOA_LIQUOR = ITEMS.register(
        "cocoa_liquor",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // VERARBEITUNGSPRODUKTE
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> COCOA_BUTTER = ITEMS.register(
        "cocoa_butter",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> COCOA_POWDER = ITEMS.register(
        "cocoa_powder",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> COCOA_CAKE = ITEMS.register(
        "cocoa_cake",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // ZUSATZSTOFFE
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> MILK_POWDER = ITEMS.register(
        "milk_powder",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> SUGAR = ITEMS.register(
        "sugar_refined",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1f).build()))
    );

    public static final RegistryObject<Item> VANILLA_EXTRACT = ITEMS.register(
        "vanilla_extract",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> LECITHIN = ITEMS.register(
        "lecithin",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> CARAMEL = ITEMS.register(
        "caramel",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.4f).build()))
    );

    public static final RegistryObject<Item> NOUGAT = ITEMS.register(
        "nougat",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.4f).build()))
    );

    // ═══════════════════════════════════════════════════════════
    // ZUTATEN (Nüsse & Früchte)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> HAZELNUTS = ITEMS.register(
        "hazelnuts",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.5f).build()))
    );

    public static final RegistryObject<Item> ALMONDS = ITEMS.register(
        "almonds",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.5f).build()))
    );

    public static final RegistryObject<Item> ROASTED_HAZELNUTS = ITEMS.register(
        "roasted_hazelnuts",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.6f).build()))
    );

    public static final RegistryObject<Item> ROASTED_ALMONDS = ITEMS.register(
        "roasted_almonds",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.6f).build()))
    );

    public static final RegistryObject<Item> DRIED_FRUITS = ITEMS.register(
        "dried_fruits",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.4f).build()))
    );

    public static final RegistryObject<Item> RAISINS = ITEMS.register(
        "raisins",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).build()))
    );

    // ═══════════════════════════════════════════════════════════
    // ZWISCHENPRODUKTE
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> CONCHED_CHOCOLATE = ITEMS.register(
        "conched_chocolate",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> TEMPERED_CHOCOLATE = ITEMS.register(
        "tempered_chocolate",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> CHOCOLATE_MIXTURE = ITEMS.register(
        "chocolate_mixture",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // WERKZEUGE & VERPACKUNG
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> CHOCOLATE_MOLD = ITEMS.register(
        "chocolate_mold",
        () -> new Item(new Item.Properties().stacksTo(16))
    );

    public static final RegistryObject<Item> CHOCOLATE_MOLD_BAR = ITEMS.register(
        "chocolate_mold_bar",
        () -> new Item(new Item.Properties().stacksTo(16))
    );

    public static final RegistryObject<Item> CHOCOLATE_MOLD_PRALINE = ITEMS.register(
        "chocolate_mold_praline",
        () -> new Item(new Item.Properties().stacksTo(16))
    );

    public static final RegistryObject<Item> WRAPPER = ITEMS.register(
        "chocolate_wrapper",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> WRAPPER_GOLD = ITEMS.register(
        "chocolate_wrapper_gold",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> BOX = ITEMS.register(
        "chocolate_box",
        () -> new Item(new Item.Properties().stacksTo(16))
    );

    public static final RegistryObject<Item> BOX_PREMIUM = ITEMS.register(
        "chocolate_box_premium",
        () -> new Item(new Item.Properties().stacksTo(16))
    );

    // ═══════════════════════════════════════════════════════════
    // SCHOKOLADENTAFEL (Fertigprodukt mit NBT-Daten)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> CHOCOLATE_BAR = ITEMS.register(
        "chocolate_bar",
        () -> new ChocolateBarItem(new Item.Properties().stacksTo(16))
    );

    // ═══════════════════════════════════════════════════════════
    // SPEZIELLE SCHOKOLADENPRODUKTE
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> CHOCOLATE_TRUFFLE = ITEMS.register(
        "chocolate_truffle",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6f).build())
            .stacksTo(16))
    );

    public static final RegistryObject<Item> CHOCOLATE_PRALINE = ITEMS.register(
        "chocolate_praline",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(3).saturationMod(0.5f).build())
            .stacksTo(16))
    );

    public static final RegistryObject<Item> HOT_CHOCOLATE_MIX = ITEMS.register(
        "hot_chocolate_mix",
        () -> new Item(new Item.Properties())
    );
}
