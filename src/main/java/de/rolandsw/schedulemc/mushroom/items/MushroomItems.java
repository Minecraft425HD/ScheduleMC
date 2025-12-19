package de.rolandsw.schedulemc.mushroom.items;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.mushroom.MushroomType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registriert alle Pilz-Items
 */
public class MushroomItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // MIST-SÄCKE (3 Stufen)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> MIST_BAG_SMALL = ITEMS.register("mist_bag_small",
            () -> new MistBagItem(MistBagType.SMALL));
    public static final RegistryObject<Item> MIST_BAG_MEDIUM = ITEMS.register("mist_bag_medium",
            () -> new MistBagItem(MistBagType.MEDIUM));
    public static final RegistryObject<Item> MIST_BAG_LARGE = ITEMS.register("mist_bag_large",
            () -> new MistBagItem(MistBagType.LARGE));

    // ═══════════════════════════════════════════════════════════
    // SPOREN-SPRITZEN (pro Sorte)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> SPORE_SYRINGE_CUBENSIS = ITEMS.register("spore_syringe_cubensis",
            () -> new SporeSyringeItem(MushroomType.CUBENSIS));
    public static final RegistryObject<Item> SPORE_SYRINGE_AZURESCENS = ITEMS.register("spore_syringe_azurescens",
            () -> new SporeSyringeItem(MushroomType.AZURESCENS));
    public static final RegistryObject<Item> SPORE_SYRINGE_MEXICANA = ITEMS.register("spore_syringe_mexicana",
            () -> new SporeSyringeItem(MushroomType.MEXICANA));

    // ═══════════════════════════════════════════════════════════
    // FRISCHE PILZE (pro Sorte)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> FRESH_CUBENSIS = ITEMS.register("fresh_cubensis",
            () -> new FreshMushroomItem(MushroomType.CUBENSIS));
    public static final RegistryObject<Item> FRESH_AZURESCENS = ITEMS.register("fresh_azurescens",
            () -> new FreshMushroomItem(MushroomType.AZURESCENS));
    public static final RegistryObject<Item> FRESH_MEXICANA = ITEMS.register("fresh_mexicana",
            () -> new FreshMushroomItem(MushroomType.MEXICANA));

    // ═══════════════════════════════════════════════════════════
    // GETROCKNETE PILZE (pro Sorte)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> DRIED_CUBENSIS = ITEMS.register("dried_cubensis",
            () -> new DriedMushroomItem(MushroomType.CUBENSIS));
    public static final RegistryObject<Item> DRIED_AZURESCENS = ITEMS.register("dried_azurescens",
            () -> new DriedMushroomItem(MushroomType.AZURESCENS));
    public static final RegistryObject<Item> DRIED_MEXICANA = ITEMS.register("dried_mexicana",
            () -> new DriedMushroomItem(MushroomType.MEXICANA));
}
