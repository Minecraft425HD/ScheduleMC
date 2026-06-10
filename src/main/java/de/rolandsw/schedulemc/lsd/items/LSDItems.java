package de.rolandsw.schedulemc.lsd.items;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller LSD-Items
 */
public class LSDItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // GRUNDZUTATEN
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> ERGOT =
            ITEMS.register("ergot", ErgotItem::new);

    public static final RegistryObject<Item> BLOTTER_PAPER =
            ITEMS.register("blotter_paper", BlotterPaperItem::new);

    // ═══════════════════════════════════════════════════════════
    // ZWISCHENPRODUKTE
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> ERGOT_CULTURE =
            ITEMS.register("ergot_culture", ErgotCultureItem::new);

    public static final RegistryObject<Item> LYSERGIC_ACID =
            ITEMS.register("lysergic_acid", LysergicAcidItem::new);

    public static final RegistryObject<Item> LSD_SOLUTION =
            ITEMS.register("lsd_solution", LSDSolutionItem::new);

    // ═══════════════════════════════════════════════════════════
    // ENDPRODUKT
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> BLOTTER =
            ITEMS.register("lsd_blotter", BlotterItem::new);
}
