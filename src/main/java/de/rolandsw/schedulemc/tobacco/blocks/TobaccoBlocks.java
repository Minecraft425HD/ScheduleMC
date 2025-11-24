package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.tobacco.PotType;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Registrierung aller Tabak-Blöcke (ERWEITERT mit Arbeiter-Schrank)
 */
public class TobaccoBlocks {
    
    public static final DeferredRegister<Block> BLOCKS = 
        DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);
    
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);
    
    // ═══════════════════════════════════════════════════════════
    // TÖPFE (4 Varianten)
    // ═══════════════════════════════════════════════════════════
    
    public static final RegistryObject<Block> TERRACOTTA_POT = registerBlockWithItem(
        "terracotta_pot",
        () -> new TobaccoPotBlock(
            PotType.TERRACOTTA,
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.STONE)
                .noOcclusion()
        )
    );
    
    public static final RegistryObject<Block> CERAMIC_POT = registerBlockWithItem(
        "ceramic_pot",
        () -> new TobaccoPotBlock(
            PotType.CERAMIC,
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.STONE)
                .noOcclusion()
        )
    );
    
    public static final RegistryObject<Block> IRON_POT = registerBlockWithItem(
        "iron_pot",
        () -> new TobaccoPotBlock(
            PotType.IRON,
            BlockBehaviour.Properties.of()
                .strength(3.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
        )
    );
    
    public static final RegistryObject<Block> GOLDEN_POT = registerBlockWithItem(
        "golden_pot",
        () -> new TobaccoPotBlock(
            PotType.GOLDEN,
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
        )
    );
    
    // ═══════════════════════════════════════════════════════════
    // VERARBEITUNGS-BLÖCKE
    // ═══════════════════════════════════════════════════════════
    
    public static final RegistryObject<Block> DRYING_RACK = registerBlockWithItem(
        "drying_rack",
        () -> new DryingRackBlock(
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.WOOD)
                .noOcclusion()
        )
    );
    
    public static final RegistryObject<Block> FERMENTATION_BARREL = registerBlockWithItem(
        "fermentation_barrel",
        () -> new FermentationBarrelBlock(
            BlockBehaviour.Properties.of()
                .strength(2.5f)
                .sound(SoundType.WOOD)
        )
    );
    
    public static final RegistryObject<Block> SINK = registerBlockWithItem(
        "sink",
        () -> new SinkBlock(
            BlockBehaviour.Properties.of()
                .strength(3.0f)
                .sound(SoundType.STONE)
                .noOcclusion()
        )
    );

    public static final RegistryObject<Block> PACKAGING_TABLE = registerBlockWithItem(
        "packaging_table",
        () -> new PackagingTableBlock(
            BlockBehaviour.Properties.of()
                .strength(2.5f)
                .sound(SoundType.WOOD)
        )
    );

    // ═══════════════════════════════════════════════════════════
    // PFLANZEN-BLÖCKE (4 Tabak-Typen, KEIN BlockItem!)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> VIRGINIA_PLANT = BLOCKS.register(
        "virginia_plant",
        () -> new TobaccoPlantBlock(TobaccoType.VIRGINIA)
    );

    public static final RegistryObject<Block> BURLEY_PLANT = BLOCKS.register(
        "burley_plant",
        () -> new TobaccoPlantBlock(TobaccoType.BURLEY)
    );

    public static final RegistryObject<Block> ORIENTAL_PLANT = BLOCKS.register(
        "oriental_plant",
        () -> new TobaccoPlantBlock(TobaccoType.ORIENTAL)
    );

    public static final RegistryObject<Block> HAVANA_PLANT = BLOCKS.register(
        "havana_plant",
        () -> new TobaccoPlantBlock(TobaccoType.HAVANA)
    );

    // ═══════════════════════════════════════════════════════════
    // GROW LIGHTS (3 Tier-Stufen)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> BASIC_GROW_LIGHT_SLAB = registerBlockWithItem(
        "basic_grow_light_slab",
        () -> new GrowLightSlabBlock(
            BlockBehaviour.Properties.of()
                .strength(1.5f)
                .sound(SoundType.GLASS)
                .noOcclusion(),
            GrowLightSlabBlock.GrowLightTier.BASIC
        )
    );

    public static final RegistryObject<Block> ADVANCED_GROW_LIGHT_SLAB = registerBlockWithItem(
        "advanced_grow_light_slab",
        () -> new GrowLightSlabBlock(
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.GLASS)
                .noOcclusion(),
            GrowLightSlabBlock.GrowLightTier.ADVANCED
        )
    );

    public static final RegistryObject<Block> PREMIUM_GROW_LIGHT_SLAB = registerBlockWithItem(
        "premium_grow_light_slab",
        () -> new GrowLightSlabBlock(
            BlockBehaviour.Properties.of()
                .strength(2.5f)
                .sound(SoundType.GLASS)
                .noOcclusion(),
            GrowLightSlabBlock.GrowLightTier.PREMIUM
        )
    );

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Registriert Block + BlockItem
     */
    private static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, Supplier<T> block) {
        RegistryObject<T> registeredBlock = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new BlockItem(registeredBlock.get(), new Item.Properties()));
        return registeredBlock;
    }
}
