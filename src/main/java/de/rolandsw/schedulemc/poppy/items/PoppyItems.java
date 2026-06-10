package de.rolandsw.schedulemc.poppy.items;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.poppy.PoppyType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry für alle Mohn-bezogenen Items
 */
public class PoppyItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // Samen
    public static final RegistryObject<Item> AFGHAN_SEEDS = ITEMS.register("afghan_poppy_seeds",
            () -> new PoppySeedItem(PoppyType.AFGHAN));
    public static final RegistryObject<Item> TURKISH_SEEDS = ITEMS.register("turkish_poppy_seeds",
            () -> new PoppySeedItem(PoppyType.TURKISH));
    public static final RegistryObject<Item> INDIAN_SEEDS = ITEMS.register("indian_poppy_seeds",
            () -> new PoppySeedItem(PoppyType.INDIAN));

    // Mohnkapseln (je eine Variante pro Sorte)
    public static final RegistryObject<Item> AFGHAN_POPPY_POD = ITEMS.register("afghan_poppy_pod",
            () -> new PoppyPodItem(PoppyType.AFGHAN));
    public static final RegistryObject<Item> TURKISH_POPPY_POD = ITEMS.register("turkish_poppy_pod",
            () -> new PoppyPodItem(PoppyType.TURKISH));
    public static final RegistryObject<Item> INDIAN_POPPY_POD = ITEMS.register("indian_poppy_pod",
            () -> new PoppyPodItem(PoppyType.INDIAN));

    // Rohopium (braun)
    public static final RegistryObject<Item> RAW_OPIUM = ITEMS.register("raw_opium",
            RawOpiumItem::new);

    // Morphin-Base
    public static final RegistryObject<Item> MORPHINE = ITEMS.register("morphine",
            MorphineItem::new);

    // Heroin (weiß)
    public static final RegistryObject<Item> HEROIN = ITEMS.register("heroin",
            HeroinItem::new);

    // Ritzmesser (Tool)
    public static final RegistryObject<Item> SCORING_KNIFE = ITEMS.register("scoring_knife",
            ScoringKnifeItem::new);
}
