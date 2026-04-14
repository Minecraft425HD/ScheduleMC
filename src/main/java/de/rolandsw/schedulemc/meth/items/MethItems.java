package de.rolandsw.schedulemc.meth.items;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Meth-Items
 */
public class MethItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // GRUNDZUTATEN
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> EPHEDRINE =
            ITEMS.register("ephedrin", EphedrineItem::new);

    public static final RegistryObject<Item> PSEUDOEPHEDRINE =
            ITEMS.register("pseudoephedrin", PseudoephedrineItem::new);

    public static final RegistryObject<Item> RED_PHOSPHORUS =
            ITEMS.register("roter_phosphor", RedPhosphorusItem::new);

    public static final RegistryObject<Item> IODINE =
            ITEMS.register("jod", IodineItem::new);

    // ═══════════════════════════════════════════════════════════
    // ZWISCHENPRODUKTE
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> METH_PASTE =
            ITEMS.register("meth_paste", MethPasteItem::new);

    public static final RegistryObject<Item> RAW_METH =
            ITEMS.register("roh_meth", RawMethItem::new);

    public static final RegistryObject<Item> CRYSTAL_METH =
            ITEMS.register("kristall_meth", CrystalMethItem::new);

    // ═══════════════════════════════════════════════════════════
    // ENDPRODUKT
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> METH =
            ITEMS.register("meth", MethItem::new);
}
