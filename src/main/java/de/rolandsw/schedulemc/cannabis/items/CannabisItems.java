package de.rolandsw.schedulemc.cannabis.items;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registriert alle Cannabis-Items
 */
public class CannabisItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // Samen
    public static final RegistryObject<Item> CANNABIS_SEED = ITEMS.register("cannabis_seed",
            () -> new CannabisSeedItem(new Item.Properties().stacksTo(64)));

    // Verarbeitungsstufen
    public static final RegistryObject<Item> FRESH_BUD = ITEMS.register("fresh_cannabis_bud",
            () -> new FreshBudItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> DRIED_BUD = ITEMS.register("dried_cannabis_bud",
            () -> new DriedBudItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> TRIMMED_BUD = ITEMS.register("trimmed_cannabis_bud",
            () -> new TrimmedBudItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> CURED_BUD = ITEMS.register("cured_cannabis_bud",
            () -> new CuredBudItem(new Item.Properties().stacksTo(16)));

    // Nebenprodukte
    public static final RegistryObject<Item> TRIM = ITEMS.register("cannabis_trim",
            () -> new TrimItem(new Item.Properties().stacksTo(64)));

    // Konzentrate
    public static final RegistryObject<Item> HASH = ITEMS.register("cannabis_hash",
            () -> new HashItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> CANNABIS_OIL = ITEMS.register("cannabis_oil",
            () -> new CannabisOilItem(new Item.Properties().stacksTo(16)));

    // Zusätzliche Items für Verarbeitung
    public static final RegistryObject<Item> POLLEN_PRESS_MOLD = ITEMS.register("pollen_press_mold",
            () -> new Item(new Item.Properties().stacksTo(1).durability(100)));

    public static final RegistryObject<Item> EXTRACTION_SOLVENT = ITEMS.register("extraction_solvent",
            () -> new Item(new Item.Properties().stacksTo(16)));
}
