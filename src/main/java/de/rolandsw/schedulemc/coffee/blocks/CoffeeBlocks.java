package de.rolandsw.schedulemc.coffee.blocks;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.coffee.CoffeeType;
import de.rolandsw.schedulemc.production.blocks.PlantPotBlock;
import de.rolandsw.schedulemc.production.core.PotType;
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
 * Registrierung aller Kaffee-Blöcke
 */
public class CoffeeBlocks {

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // TÖPFE (nutzen dieselben wie Tabak aus production.blocks)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> COFFEE_TERRACOTTA_POT = registerBlockWithItem(
        "coffee_terracotta_pot",
        () -> new PlantPotBlock(
            PotType.TERRACOTTA,
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.STONE)
                .noOcclusion()
        )
    );

    public static final RegistryObject<Block> COFFEE_CERAMIC_POT = registerBlockWithItem(
        "coffee_ceramic_pot",
        () -> new PlantPotBlock(
            PotType.CERAMIC,
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.STONE)
                .noOcclusion()
        )
    );

    public static final RegistryObject<Block> COFFEE_IRON_POT = registerBlockWithItem(
        "coffee_iron_pot",
        () -> new PlantPotBlock(
            PotType.IRON,
            BlockBehaviour.Properties.of()
                .strength(3.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
        )
    );

    public static final RegistryObject<Block> COFFEE_GOLDEN_POT = registerBlockWithItem(
        "coffee_golden_pot",
        () -> new PlantPotBlock(
            PotType.GOLDEN,
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
        )
    );

    // ═══════════════════════════════════════════════════════════
    // PROCESSING-BLÖCKE
    // ═══════════════════════════════════════════════════════════

    // Wet Processing Station
    public static final RegistryObject<Block> WET_PROCESSING_STATION = registerBlockWithItem(
        "wet_processing_station",
        () -> new WetProcessingStationBlock(
            BlockBehaviour.Properties.of()
                .strength(3.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
        )
    );

    // Dry Processing Trays (3 Größen)
    public static final RegistryObject<Block> SMALL_DRYING_TRAY = registerBlockWithItem(
        "small_coffee_drying_tray",
        () -> new SmallDryingTrayBlock(
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.WOOD)
                .noOcclusion()
        )
    );

    public static final RegistryObject<Block> MEDIUM_DRYING_TRAY = registerBlockWithItem(
        "medium_coffee_drying_tray",
        () -> new MediumDryingTrayBlock(
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.WOOD)
                .noOcclusion()
        )
    );

    public static final RegistryObject<Block> LARGE_DRYING_TRAY = registerBlockWithItem(
        "large_coffee_drying_tray",
        () -> new LargeDryingTrayBlock(
            BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.WOOD)
                .noOcclusion()
        )
    );

    // Coffee Roaster (3 Größen)
    public static final RegistryObject<Block> SMALL_COFFEE_ROASTER = registerBlockWithItem(
        "small_coffee_roaster",
        () -> new SmallCoffeeRoasterBlock(
            BlockBehaviour.Properties.of()
                .strength(3.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
        )
    );

    public static final RegistryObject<Block> MEDIUM_COFFEE_ROASTER = registerBlockWithItem(
        "medium_coffee_roaster",
        () -> new MediumCoffeeRoasterBlock(
            BlockBehaviour.Properties.of()
                .strength(3.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
        )
    );

    public static final RegistryObject<Block> LARGE_COFFEE_ROASTER = registerBlockWithItem(
        "large_coffee_roaster",
        () -> new LargeCoffeeRoasterBlock(
            BlockBehaviour.Properties.of()
                .strength(3.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
        )
    );

    // Coffee Grinder
    public static final RegistryObject<Block> COFFEE_GRINDER = registerBlockWithItem(
        "coffee_grinder",
        () -> new CoffeeGrinderBlock(
            BlockBehaviour.Properties.of()
                .strength(3.0f)
                .sound(SoundType.STONE)
                .noOcclusion()
        )
    );

    // Packaging Table
    public static final RegistryObject<Block> COFFEE_PACKAGING_TABLE = registerBlockWithItem(
        "coffee_packaging_table",
        () -> new CoffeePackagingTableBlock(
            BlockBehaviour.Properties.of()
                .strength(2.5f)
                .sound(SoundType.WOOD)
                .noOcclusion()
        )
    );

    // ═══════════════════════════════════════════════════════════
    // PFLANZEN-BLÖCKE (4 Kaffee-Typen, KEIN BlockItem!)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> ARABICA_PLANT = BLOCKS.register(
        "arabica_plant",
        () -> new CoffeePlantBlock(CoffeeType.ARABICA)
    );

    public static final RegistryObject<Block> ROBUSTA_PLANT = BLOCKS.register(
        "robusta_plant",
        () -> new CoffeePlantBlock(CoffeeType.ROBUSTA)
    );

    public static final RegistryObject<Block> LIBERICA_PLANT = BLOCKS.register(
        "liberica_plant",
        () -> new CoffeePlantBlock(CoffeeType.LIBERICA)
    );

    public static final RegistryObject<Block> EXCELSA_PLANT = BLOCKS.register(
        "excelsa_plant",
        () -> new CoffeePlantBlock(CoffeeType.EXCELSA)
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
