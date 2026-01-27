package de.rolandsw.schedulemc.coffee.items;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.coffee.CoffeeType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Kaffee-Items
 */
public class CoffeeItems {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // SETZLINGE (für Töpfe)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> ARABICA_SEEDLING =
        ITEMS.register("arabica_seedling", () -> new CoffeeSeedlingItem(CoffeeType.ARABICA));

    public static final RegistryObject<Item> ROBUSTA_SEEDLING =
        ITEMS.register("robusta_seedling", () -> new CoffeeSeedlingItem(CoffeeType.ROBUSTA));

    public static final RegistryObject<Item> LIBERICA_SEEDLING =
        ITEMS.register("liberica_seedling", () -> new CoffeeSeedlingItem(CoffeeType.LIBERICA));

    public static final RegistryObject<Item> EXCELSA_SEEDLING =
        ITEMS.register("excelsa_seedling", () -> new CoffeeSeedlingItem(CoffeeType.EXCELSA));

    // ═══════════════════════════════════════════════════════════
    // KAFFEEKIRSCHEN (frisch geerntet)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> ARABICA_CHERRY =
        ITEMS.register("arabica_cherry", CoffeeCherryItem::new);

    public static final RegistryObject<Item> ROBUSTA_CHERRY =
        ITEMS.register("robusta_cherry", CoffeeCherryItem::new);

    public static final RegistryObject<Item> LIBERICA_CHERRY =
        ITEMS.register("liberica_cherry", CoffeeCherryItem::new);

    public static final RegistryObject<Item> EXCELSA_CHERRY =
        ITEMS.register("excelsa_cherry", CoffeeCherryItem::new);

    // ═══════════════════════════════════════════════════════════
    // GRÜNE BOHNEN (nach Processing)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> GREEN_ARABICA_BEANS =
        ITEMS.register("green_arabica_beans", GreenCoffeeBeanItem::new);

    public static final RegistryObject<Item> GREEN_ROBUSTA_BEANS =
        ITEMS.register("green_robusta_beans", GreenCoffeeBeanItem::new);

    public static final RegistryObject<Item> GREEN_LIBERICA_BEANS =
        ITEMS.register("green_liberica_beans", GreenCoffeeBeanItem::new);

    public static final RegistryObject<Item> GREEN_EXCELSA_BEANS =
        ITEMS.register("green_excelsa_beans", GreenCoffeeBeanItem::new);

    // ═══════════════════════════════════════════════════════════
    // GERÖSTETE BOHNEN (mit NBT-Daten)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> ROASTED_COFFEE_BEANS =
        ITEMS.register("roasted_coffee_beans", RoastedCoffeeBeanItem::new);

    // ═══════════════════════════════════════════════════════════
    // GEMAHLENER KAFFEE (Endprodukt)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> GROUND_COFFEE =
        ITEMS.register("ground_coffee", GroundCoffeeItem::new);

    // ═══════════════════════════════════════════════════════════
    // VERPACKTE PRODUKTE (verschiedene Größen)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> COFFEE_PACKAGE_250G =
        ITEMS.register("coffee_package_250g", PackagedCoffeeItem::new);

    public static final RegistryObject<Item> COFFEE_PACKAGE_500G =
        ITEMS.register("coffee_package_500g", PackagedCoffeeItem::new);

    public static final RegistryObject<Item> COFFEE_PACKAGE_1KG =
        ITEMS.register("coffee_package_1kg", PackagedCoffeeItem::new);

    // ═══════════════════════════════════════════════════════════
    // WERKZEUGE & HILFSMITTEL
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> WATERING_CAN =
        ITEMS.register("coffee_watering_can", () -> new Item(new Item.Properties().stacksTo(1).durability(100)));

    public static final RegistryObject<Item> PULPING_TOOL =
        ITEMS.register("pulping_tool", () -> new Item(new Item.Properties().stacksTo(1).durability(200)));

    public static final RegistryObject<Item> ROASTING_TRAY =
        ITEMS.register("roasting_tray", () -> new Item(new Item.Properties().stacksTo(16)));

    // ═══════════════════════════════════════════════════════════
    // ZUSÄTZE & BOOSTER
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> COFFEE_FERTILIZER =
        ITEMS.register("coffee_fertilizer", () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> GROWTH_ACCELERATOR =
        ITEMS.register("growth_accelerator", () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> QUALITY_ENHANCER =
        ITEMS.register("quality_enhancer", () -> new Item(new Item.Properties().stacksTo(64)));

    // ═══════════════════════════════════════════════════════════
    // VERPACKUNGSMATERIALIEN
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> COFFEE_BAG_SMALL =
        ITEMS.register("coffee_bag_small", () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> COFFEE_BAG_MEDIUM =
        ITEMS.register("coffee_bag_medium", () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> COFFEE_BAG_LARGE =
        ITEMS.register("coffee_bag_large", () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> VACUUM_SEAL =
        ITEMS.register("vacuum_seal", () -> new Item(new Item.Properties().stacksTo(64)));

    // ═══════════════════════════════════════════════════════════
    // CONSUMABLES (trinkbarer Kaffee)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> BREWED_COFFEE =
        ITEMS.register("brewed_coffee", BrewedCoffeeItem::new);

    public static final RegistryObject<Item> ESPRESSO =
        ITEMS.register("espresso", EspressoItem::new);
}
