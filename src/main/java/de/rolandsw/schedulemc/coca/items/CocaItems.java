package de.rolandsw.schedulemc.coca.items;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.coca.CocaType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Koka-Items
 */
public class CocaItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // SAMEN (für Töpfe)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> BOLIVIANISCH_SEEDS =
            ITEMS.register("bolivianisch_coca_seeds", () -> new CocaSeedItem(CocaType.BOLIVIANISCH));

    public static final RegistryObject<Item> KOLUMBIANISCH_SEEDS =
            ITEMS.register("kolumbianisch_coca_seeds", () -> new CocaSeedItem(CocaType.KOLUMBIANISCH));

    // ═══════════════════════════════════════════════════════════
    // FRISCHE BLÄTTER
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> FRESH_BOLIVIANISCH_LEAF =
            ITEMS.register("fresh_bolivianisch_coca_leaf", FreshCocaLeafItem::new);

    public static final RegistryObject<Item> FRESH_KOLUMBIANISCH_LEAF =
            ITEMS.register("fresh_kolumbianisch_coca_leaf", FreshCocaLeafItem::new);

    // ═══════════════════════════════════════════════════════════
    // KOKA-PASTE (braun - Zwischenprodukt)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> COCA_PASTE =
            ITEMS.register("coca_paste", CocaPasteItem::new);

    // ═══════════════════════════════════════════════════════════
    // KOKAIN (weiß - Endprodukt)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> COCAINE =
            ITEMS.register("cocaine", CocaineItem::new);

    // ═══════════════════════════════════════════════════════════
    // VERPACKTES KOKAIN (für Verkauf an NPCs)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> PACKAGED_COCAINE =
            ITEMS.register("packaged_cocaine", PackagedCocaineItem::new);

    // ═══════════════════════════════════════════════════════════
    // DIESEL-KANISTER (für Extraktionswanne)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> DIESEL_CANISTER =
            ITEMS.register("diesel_canister", DieselCanisterItem::new);
}
