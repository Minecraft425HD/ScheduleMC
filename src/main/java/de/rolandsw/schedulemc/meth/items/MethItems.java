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

    public static final RegistryObject<Item> EPHEDRIN =
            ITEMS.register("ephedrin", EphedrinItem::new);

    public static final RegistryObject<Item> PSEUDOEPHEDRIN =
            ITEMS.register("pseudoephedrin", PseudoephedrinItem::new);

    public static final RegistryObject<Item> ROTER_PHOSPHOR =
            ITEMS.register("roter_phosphor", RoterPhosphorItem::new);

    public static final RegistryObject<Item> JOD =
            ITEMS.register("jod", JodItem::new);

    // ═══════════════════════════════════════════════════════════
    // ZWISCHENPRODUKTE
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> METH_PASTE =
            ITEMS.register("meth_paste", MethPasteItem::new);

    public static final RegistryObject<Item> ROH_METH =
            ITEMS.register("roh_meth", RohMethItem::new);

    public static final RegistryObject<Item> KRISTALL_METH =
            ITEMS.register("kristall_meth", KristallMethItem::new);

    // ═══════════════════════════════════════════════════════════
    // ENDPRODUKT
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> METH =
            ITEMS.register("meth", MethItem::new);

    // ═══════════════════════════════════════════════════════════
    // VERPACKTES METH (für Verkauf)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> PACKAGED_METH_1G =
            ITEMS.register("packaged_meth_1g", () -> new PackagedMethItem(1));

    public static final RegistryObject<Item> PACKAGED_METH_3_5G =
            ITEMS.register("packaged_meth_3_5g", () -> new PackagedMethItem(3)); // 3.5g abgerundet

    public static final RegistryObject<Item> PACKAGED_METH_7G =
            ITEMS.register("packaged_meth_7g", () -> new PackagedMethItem(7));

    public static final RegistryObject<Item> PACKAGED_METH_14G =
            ITEMS.register("packaged_meth_14g", () -> new PackagedMethItem(14));

    public static final RegistryObject<Item> PACKAGED_METH_28G =
            ITEMS.register("packaged_meth_28g", () -> new PackagedMethItem(28)); // 1 Unze
}
