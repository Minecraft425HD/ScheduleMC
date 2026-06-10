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
            ITEMS.register("safrole", SafrolItem::new);

    public static final RegistryObject<Item> BINDEMITTEL =
            ITEMS.register("binding_agent", BindemittelItem::new);

    public static final RegistryObject<Item> FARBSTOFF =
            ITEMS.register("pill_dye", FarbstoffItem::new);

    // ═══════════════════════════════════════════════════════════
    // ZWISCHENPRODUKTE
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> MDMA_BASE =
            ITEMS.register("mdma_base", MDMABaseItem::new);

    public static final RegistryObject<Item> MDMA_KRISTALL =
            ITEMS.register("mdma_crystal", MDMACrystalItem::new);

    // ═══════════════════════════════════════════════════════════
    // ENDPRODUKT
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> ECSTASY_PILL =
            ITEMS.register("ecstasy_pill", EcstasyPillItem::new);
}
