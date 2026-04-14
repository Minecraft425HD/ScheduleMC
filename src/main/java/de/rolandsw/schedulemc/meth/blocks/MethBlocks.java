package de.rolandsw.schedulemc.meth.blocks;

import de.rolandsw.schedulemc.ScheduleMC;
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
 * Registrierung aller Meth-Blöcke
 */
public class MethBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // CHEMIE-MIXER (Schritt 1)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> CHEMICAL_MIXER = registerBlockWithItem(
            "chemie_mixer",
            () -> new ChemicalMixerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
            )
    );

    // ═══════════════════════════════════════════════════════════
    // REDUCTION_KETTLE (Schritt 2) - Mit Explosionsgefahr!
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> REDUCTION_KETTLE = registerBlockWithItem(
            "reduktionskessel",
            () -> new ReductionKettleBlock(
                    BlockBehaviour.Properties.of()
                            .strength(4.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .lightLevel(state -> 4) // Leichtes Glühen
            )
    );

    // ═══════════════════════════════════════════════════════════
    // CRYSTALLIZER (Schritt 3)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> CRYSTALLIZER = registerBlockWithItem(
            "kristallisator",
            () -> new CrystallizerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
            )
    );

    // ═══════════════════════════════════════════════════════════
    // VAKUUM-TROCKNER (Schritt 4 - Final)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> VACUUM_DRYER = registerBlockWithItem(
            "vakuum_trockner",
            () -> new VacuumDryerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
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
