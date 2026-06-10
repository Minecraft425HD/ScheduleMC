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
            ITEMS.register("bolivian_coca_seeds", () -> new CocaSeedItem(CocaType.BOLIVIANISCH));

    public static final RegistryObject<Item> KOLUMBIANISCH_SEEDS =
            ITEMS.register("colombian_coca_seeds", () -> new CocaSeedItem(CocaType.KOLUMBIANISCH));

    public static final RegistryObject<Item> PERUANISCH_SEEDS =
            ITEMS.register("peruvian_coca_seeds", () -> new CocaSeedItem(CocaType.PERUANISCH));

    // ═══════════════════════════════════════════════════════════
    // FRISCHE BLÄTTER
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> FRESH_BOLIVIANISCH_LEAF =
            ITEMS.register("fresh_bolivian_coca_leaf", () -> new FreshCocaLeafItem(CocaType.BOLIVIANISCH));

    public static final RegistryObject<Item> FRESH_KOLUMBIANISCH_LEAF =
            ITEMS.register("fresh_colombian_coca_leaf", () -> new FreshCocaLeafItem(CocaType.KOLUMBIANISCH));

    public static final RegistryObject<Item> FRESH_PERUANISCH_LEAF =
            ITEMS.register("fresh_peruvian_coca_leaf", () -> new FreshCocaLeafItem(CocaType.PERUANISCH));

    // ═══════════════════════════════════════════════════════════
    // KOKA-PASTE (braun - Zwischenprodukt)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> COCA_PASTE_BOLIVIANISCH =
            ITEMS.register("coca_paste_bolivian", () -> new CocaPasteItem(CocaType.BOLIVIANISCH));

    public static final RegistryObject<Item> COCA_PASTE_KOLUMBIANISCH =
            ITEMS.register("coca_paste_colombian", () -> new CocaPasteItem(CocaType.KOLUMBIANISCH));

    public static final RegistryObject<Item> COCA_PASTE_PERUANISCH =
            ITEMS.register("coca_paste_peruvian", () -> new CocaPasteItem(CocaType.PERUANISCH));

    // ═══════════════════════════════════════════════════════════
    // KOKAIN (weiß - Endprodukt)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> COCAINE =
            ITEMS.register("cocaine", CocaineItem::new);

    // ═══════════════════════════════════════════════════════════
    // DIESEL-KANISTER: Nutzt jetzt Vehicle-System ItemDieselCanister
    // ═══════════════════════════════════════════════════════════

    // ═══════════════════════════════════════════════════════════
    // CRACK (Gekochtes Kokain)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> CRACK_ROCK =
            ITEMS.register("crack_rock", CrackRockItem::new);

    public static final RegistryObject<Item> BACKPULVER =
            ITEMS.register("baking_powder", BackpulverItem::new);
}
