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

    public static final RegistryObject<Item> MUTTERKORN =
            ITEMS.register("mutterkorn", MutterkornItem::new);

    public static final RegistryObject<Item> BLOTTER_PAPIER =
            ITEMS.register("blotter_papier", BlotterPapierItem::new);

    // ═══════════════════════════════════════════════════════════
    // ZWISCHENPRODUKTE
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> ERGOT_KULTUR =
            ITEMS.register("ergot_kultur", ErgotKulturItem::new);

    public static final RegistryObject<Item> LYSERGSAEURE =
            ITEMS.register("lysergsaeure", LysergsaeureItem::new);

    public static final RegistryObject<Item> LSD_LOESUNG =
            ITEMS.register("lsd_loesung", LSDLoesungItem::new);

    // ═══════════════════════════════════════════════════════════
    // ENDPRODUKT
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> BLOTTER =
            ITEMS.register("lsd_blotter", BlotterItem::new);
}
