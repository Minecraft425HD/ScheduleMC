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

    public static final RegistryObject<Item> BOLIVIAN_SEEDS =
            ITEMS.register("bolivian_coca_seeds", () -> new CocaSeedItem(CocaType.BOLIVIAN));

    public static final RegistryObject<Item> COLOMBIAN_SEEDS =
            ITEMS.register("colombian_coca_seeds", () -> new CocaSeedItem(CocaType.COLOMBIAN));

    public static final RegistryObject<Item> PERUVIAN_SEEDS =
            ITEMS.register("peruvian_coca_seeds", () -> new CocaSeedItem(CocaType.PERUVIAN));

    // ═══════════════════════════════════════════════════════════
    // FRISCHE BLÄTTER
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> FRESH_BOLIVIAN_LEAF =
            ITEMS.register("fresh_bolivian_coca_leaf", () -> new FreshCocaLeafItem(CocaType.BOLIVIAN));

    public static final RegistryObject<Item> FRESH_COLOMBIAN_LEAF =
            ITEMS.register("fresh_colombian_coca_leaf", () -> new FreshCocaLeafItem(CocaType.COLOMBIAN));

    public static final RegistryObject<Item> FRESH_PERUVIAN_LEAF =
            ITEMS.register("fresh_peruvian_coca_leaf", () -> new FreshCocaLeafItem(CocaType.PERUVIAN));

    // ═══════════════════════════════════════════════════════════
    // KOKA-PASTE (braun - Zwischenprodukt)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Item> COCA_PASTE_BOLIVIAN =
            ITEMS.register("coca_paste_bolivian", () -> new CocaPasteItem(CocaType.BOLIVIAN));

    public static final RegistryObject<Item> COCA_PASTE_COLOMBIAN =
            ITEMS.register("coca_paste_colombian", () -> new CocaPasteItem(CocaType.COLOMBIAN));

    public static final RegistryObject<Item> COCA_PASTE_PERUVIAN =
            ITEMS.register("coca_paste_peruvian", () -> new CocaPasteItem(CocaType.PERUVIAN));

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

    public static final RegistryObject<Item> BAKING_POWDER =
            ITEMS.register("baking_powder", BakingPowderItem::new);
}
