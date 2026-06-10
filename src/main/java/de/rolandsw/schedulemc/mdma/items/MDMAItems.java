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

    public static final RegistryObject<Item> SAFROLE =
            ITEMS.register("safrole", SafroleItem::new);

    public static final RegistryObject<Item> BINDING_AGENT =
            ITEMS.register("binding_agent", BindingAgentItem::new);

    public static final RegistryObject<Item> PILL_DYE =
            ITEMS.register("pill_dye", PillDyeItem::new);

    // ═══════════════════════════════════════════════════════════
    // ZWISCHENPRODUKTE
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> MDMA_BASE =
            ITEMS.register("mdma_base", MDMABaseItem::new);

    public static final RegistryObject<Item> MDMA_CRYSTAL =
            ITEMS.register("mdma_crystal", MDMACrystalItem::new);

    // ═══════════════════════════════════════════════════════════
    // ENDPRODUKT
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> ECSTASY_PILL =
            ITEMS.register("ecstasy_pill", EcstasyPillItem::new);
}
