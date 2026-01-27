package de.rolandsw.schedulemc.cheese.items;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Zentrale Registrierung aller Kase-Items
 *
 * Struktur:
 * - Milch-Eimer (Rohstoff)
 * - Lab/Rennet (fur Gerinnung)
 * - Kasebruch/Curd (mit Quality)
 * - Molke/Whey
 * - Kaselaib/Wheel (4 Sorten, gereift)
 * - Kasestucke/Wedges (konsumierbar, verarbeitet)
 * - Verpackungsmaterialien
 */
public class CheeseItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        ForgeRegistries.ITEMS, ScheduleMC.MOD_ID
    );

    // ═══════════════════════════════════════════════════════════
    // ROHSTOFFE
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> MILK_BUCKET = ITEMS.register(
        "milk_bucket",
        () -> new MilkBucketItem(new Item.Properties().stacksTo(16))
    );

    public static final RegistryObject<Item> RENNET = ITEMS.register(
        "rennet",
        () -> new RennetItem(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // ZWISCHENPRODUKTE
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> CHEESE_CURD = ITEMS.register(
        "cheese_curd",
        () -> new CheeseCurdItem(new Item.Properties())
    );

    public static final RegistryObject<Item> WHEY = ITEMS.register(
        "whey",
        () -> new Item(new Item.Properties())
    );

    // ═══════════════════════════════════════════════════════════
    // KASELAIBE (Wheel) - 4 Sorten
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> CHEESE_WHEEL = ITEMS.register(
        "cheese_wheel",
        () -> new CheeseWheelItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<Item> GOUDA_WHEEL = ITEMS.register(
        "gouda_wheel",
        () -> new CheeseWheelItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<Item> EMMENTAL_WHEEL = ITEMS.register(
        "emmental_wheel",
        () -> new CheeseWheelItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<Item> CAMEMBERT_WHEEL = ITEMS.register(
        "camembert_wheel",
        () -> new CheeseWheelItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<Item> PARMESAN_WHEEL = ITEMS.register(
        "parmesan_wheel",
        () -> new CheeseWheelItem(new Item.Properties().stacksTo(1))
    );

    // ═══════════════════════════════════════════════════════════
    // KASESTUCKE (Wedge) - Konsumierbar
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> CHEESE_WEDGE = ITEMS.register(
        "cheese_wedge",
        () -> new CheeseWedgeItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6f).build()))
    );

    public static final RegistryObject<Item> GOUDA_WEDGE = ITEMS.register(
        "gouda_wedge",
        () -> new CheeseWedgeItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6f).build()))
    );

    public static final RegistryObject<Item> EMMENTAL_WEDGE = ITEMS.register(
        "emmental_wedge",
        () -> new CheeseWedgeItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(5).saturationMod(0.7f).build()))
    );

    public static final RegistryObject<Item> CAMEMBERT_WEDGE = ITEMS.register(
        "camembert_wedge",
        () -> new CheeseWedgeItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.8f).build()))
    );

    public static final RegistryObject<Item> PARMESAN_WEDGE = ITEMS.register(
        "parmesan_wedge",
        () -> new CheeseWedgeItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.8f).build()))
    );

    // ═══════════════════════════════════════════════════════════
    // VERARBEITETE KASESORTEN
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> SMOKED_CHEESE = ITEMS.register(
        "smoked_cheese",
        () -> new CheeseWedgeItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(5).saturationMod(0.7f).build()))
    );

    public static final RegistryObject<Item> HERB_CHEESE = ITEMS.register(
        "herb_cheese",
        () -> new CheeseWedgeItem(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(5).saturationMod(0.8f).build()))
    );

    // ═══════════════════════════════════════════════════════════
    // VERPACKUNGSMATERIALIEN
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> CHEESE_CLOTH = ITEMS.register(
        "cheese_cloth",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> WAX_COATING = ITEMS.register(
        "wax_coating",
        () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> CHEESE_PAPER = ITEMS.register(
        "cheese_paper",
        () -> new Item(new Item.Properties())
    );
}
