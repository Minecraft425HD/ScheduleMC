package de.rolandsw.schedulemc.coca.blocks;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.coca.CocaType;
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
 * Registrierung aller Koka-Blöcke
 */
public class CocaBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // PFLANZEN-BLÖCKE (2 Koka-Typen, KEIN BlockItem!)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> BOLIVIANISCH_PLANT = BLOCKS.register(
            "bolivianisch_coca_plant",
            () -> new CocaPlantBlock(CocaType.BOLIVIANISCH)
    );

    public static final RegistryObject<Block> KOLUMBIANISCH_PLANT = BLOCKS.register(
            "kolumbianisch_coca_plant",
            () -> new CocaPlantBlock(CocaType.KOLUMBIANISCH)
    );

    // ═══════════════════════════════════════════════════════════
    // EXTRAKTIONSWANNEN (3 Größen)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> SMALL_EXTRACTION_VAT = registerBlockWithItem(
            "small_extraction_vat",
            () -> new SmallExtractionVatBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
            )
    );

    public static final RegistryObject<Block> MEDIUM_EXTRACTION_VAT = registerBlockWithItem(
            "medium_extraction_vat",
            () -> new MediumExtractionVatBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
            )
    );

    public static final RegistryObject<Block> BIG_EXTRACTION_VAT = registerBlockWithItem(
            "big_extraction_vat",
            () -> new BigExtractionVatBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
            )
    );

    // ═══════════════════════════════════════════════════════════
    // RAFFINERIEN (3 Größen)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> SMALL_REFINERY = registerBlockWithItem(
            "small_refinery",
            () -> new SmallRefineryBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .lightLevel(state -> 8) // Glüht leicht
            )
    );

    public static final RegistryObject<Block> MEDIUM_REFINERY = registerBlockWithItem(
            "medium_refinery",
            () -> new MediumRefineryBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .lightLevel(state -> 10)
            )
    );

    public static final RegistryObject<Block> BIG_REFINERY = registerBlockWithItem(
            "big_refinery",
            () -> new BigRefineryBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .lightLevel(state -> 12)
            )
    );

    // ═══════════════════════════════════════════════════════════
    // CRACK-KOCHER
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> CRACK_KOCHER = registerBlockWithItem(
            "crack_kocher",
            () -> new CrackKocherBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .lightLevel(state -> 6)
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
