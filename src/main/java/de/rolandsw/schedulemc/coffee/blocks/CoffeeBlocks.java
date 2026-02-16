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
    // TÖPFE - Verwende TobaccoBlocks.TERRACOTTA_POT, CERAMIC_POT, IRON_POT, GOLDEN_POT
    // Coffee-spezifische Pots wurden entfernt
    // ═══════════════════════════════════════════════════════════

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

    // Dry Processing Trays - ENTFERNT
    // Verwende TobaccoBlocks.SMALL_DRYING_RACK, MEDIUM_DRYING_RACK, LARGE_DRYING_RACK

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
