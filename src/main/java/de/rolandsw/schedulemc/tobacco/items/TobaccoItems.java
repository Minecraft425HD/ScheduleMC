package de.rolandsw.schedulemc.tobacco.items;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Tabak-Items (FIXED - ohne Type in Konstruktor)
 */
public class TobaccoItems {
    
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);
    
    // ═══════════════════════════════════════════════════════════
    // SAMEN
    // ═══════════════════════════════════════════════════════════
    
    public static final RegistryObject<Item> VIRGINIA_SEEDS = 
        ITEMS.register("virginia_seeds", () -> new TobaccoSeedItem(TobaccoType.VIRGINIA));
    
    public static final RegistryObject<Item> BURLEY_SEEDS = 
        ITEMS.register("burley_seeds", () -> new TobaccoSeedItem(TobaccoType.BURLEY));
    
    public static final RegistryObject<Item> ORIENTAL_SEEDS = 
        ITEMS.register("oriental_seeds", () -> new TobaccoSeedItem(TobaccoType.ORIENTAL));
    
    public static final RegistryObject<Item> HAVANA_SEEDS = 
        ITEMS.register("havana_seeds", () -> new TobaccoSeedItem(TobaccoType.HAVANA));
    
    // ═══════════════════════════════════════════════════════════
    // FRISCHE BLÄTTER (KEIN TYPE IM KONSTRUKTOR!)
    // ═══════════════════════════════════════════════════════════
    
    public static final RegistryObject<Item> FRESH_VIRGINIA_LEAF = 
        ITEMS.register("fresh_virginia_leaf", FreshTobaccoLeafItem::new);
    
    public static final RegistryObject<Item> FRESH_BURLEY_LEAF = 
        ITEMS.register("fresh_burley_leaf", FreshTobaccoLeafItem::new);
    
    public static final RegistryObject<Item> FRESH_ORIENTAL_LEAF = 
        ITEMS.register("fresh_oriental_leaf", FreshTobaccoLeafItem::new);
    
    public static final RegistryObject<Item> FRESH_HAVANA_LEAF = 
        ITEMS.register("fresh_havana_leaf", FreshTobaccoLeafItem::new);
    
    // ═══════════════════════════════════════════════════════════
    // GETROCKNETE BLÄTTER (KEIN TYPE IM KONSTRUKTOR!)
    // ═══════════════════════════════════════════════════════════
    
    public static final RegistryObject<Item> DRIED_VIRGINIA_LEAF = 
        ITEMS.register("dried_virginia_leaf", DriedTobaccoLeafItem::new);
    
    public static final RegistryObject<Item> DRIED_BURLEY_LEAF = 
        ITEMS.register("dried_burley_leaf", DriedTobaccoLeafItem::new);
    
    public static final RegistryObject<Item> DRIED_ORIENTAL_LEAF = 
        ITEMS.register("dried_oriental_leaf", DriedTobaccoLeafItem::new);
    
    public static final RegistryObject<Item> DRIED_HAVANA_LEAF = 
        ITEMS.register("dried_havana_leaf", DriedTobaccoLeafItem::new);
    
    // ═══════════════════════════════════════════════════════════
    // FERMENTIERTE BLÄTTER (KEIN TYPE IM KONSTRUKTOR!)
    // ═══════════════════════════════════════════════════════════
    
    public static final RegistryObject<Item> FERMENTED_VIRGINIA_LEAF = 
        ITEMS.register("fermented_virginia_leaf", FermentedTobaccoLeafItem::new);
    
    public static final RegistryObject<Item> FERMENTED_BURLEY_LEAF = 
        ITEMS.register("fermented_burley_leaf", FermentedTobaccoLeafItem::new);
    
    public static final RegistryObject<Item> FERMENTED_ORIENTAL_LEAF = 
        ITEMS.register("fermented_oriental_leaf", FermentedTobaccoLeafItem::new);
    
    public static final RegistryObject<Item> FERMENTED_HAVANA_LEAF = 
        ITEMS.register("fermented_havana_leaf", FermentedTobaccoLeafItem::new);
    
    // ═══════════════════════════════════════════════════════════
    // FLASCHEN
    // ═══════════════════════════════════════════════════════════
    
    public static final RegistryObject<Item> FERTILIZER_BOTTLE = 
        ITEMS.register("fertilizer_bottle", FertilizerBottleItem::new);
    
    public static final RegistryObject<Item> GROWTH_BOOSTER_BOTTLE = 
        ITEMS.register("growth_booster_bottle", GrowthBoosterBottleItem::new);
    
    public static final RegistryObject<Item> QUALITY_BOOSTER_BOTTLE = 
        ITEMS.register("quality_booster_bottle", QualityBoosterBottleItem::new);
    
    // ═══════════════════════════════════════════════════════════
    // WERKZEUGE
    // ═══════════════════════════════════════════════════════════
    
    public static final RegistryObject<Item> WATERING_CAN = 
        ITEMS.register("watering_can", WateringCanItem::new);
    
    // ═══════════════════════════════════════════════════════════
    // NEUE FEATURES: ERDSÄCKE
    // ═══════════════════════════════════════════════════════════
    
    public static final RegistryObject<Item> SOIL_BAG_SMALL = 
        ITEMS.register("soil_bag_small", () -> new SoilBagItem(SoilBagType.SMALL));
    
    public static final RegistryObject<Item> SOIL_BAG_MEDIUM = 
        ITEMS.register("soil_bag_medium", () -> new SoilBagItem(SoilBagType.MEDIUM));
    
    public static final RegistryObject<Item> SOIL_BAG_LARGE = 
        ITEMS.register("soil_bag_large", () -> new SoilBagItem(SoilBagType.LARGE));
}
