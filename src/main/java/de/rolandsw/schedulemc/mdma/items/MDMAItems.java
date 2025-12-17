package de.rolandsw.schedulemc.mdma.items;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller MDMA-Items
 */
public class MDMAItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // GRUNDZUTATEN
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> SAFROL =
            ITEMS.register("safrol", SafrolItem::new);

    public static final RegistryObject<Item> BINDEMITTEL =
            ITEMS.register("bindemittel", BindemittelItem::new);

    public static final RegistryObject<Item> FARBSTOFF =
            ITEMS.register("pillen_farbstoff", FarbstoffItem::new);

    // ═══════════════════════════════════════════════════════════
    // ZWISCHENPRODUKTE
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> MDMA_BASE =
            ITEMS.register("mdma_base", MDMABaseItem::new);

    public static final RegistryObject<Item> MDMA_KRISTALL =
            ITEMS.register("mdma_kristall", MDMAKristallItem::new);

    // ═══════════════════════════════════════════════════════════
    // ENDPRODUKT
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> ECSTASY_PILL =
            ITEMS.register("ecstasy_pill", EcstasyPillItem::new);
}
