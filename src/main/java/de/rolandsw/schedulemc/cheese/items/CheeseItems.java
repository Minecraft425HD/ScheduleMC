package de.rolandsw.schedulemc.cheese.items;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Zentrale Registrierung aller Kase-Items
 *
 * Struktur:
 * - Lab/Rennet (fur Gerinnung)
 * - Kasebruch/Curd (mit Quality)
 * - Molke/Whey
 * - Kaselaib/Wheel (universal, Sorte per NBT)
 * - Verpackungsmaterialien
 */
public class CheeseItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        ForgeRegistries.ITEMS, ScheduleMC.MOD_ID
    );

    // ═══════════════════════════════════════════════════════════
    // ROHSTOFFE
    // ═══════════════════════════════════════════════════════════
    // MILK_BUCKET wurde entfernt - verwende net.minecraft.world.item.Items.MILK_BUCKET

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
    // KASELAIB (Wheel) - Universal, Sorte per NBT
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> CHEESE_WHEEL = ITEMS.register(
        "cheese_wheel",
        () -> new CheeseWheelItem(new Item.Properties().stacksTo(1))
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
