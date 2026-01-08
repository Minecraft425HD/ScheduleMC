package de.rolandsw.schedulemc.mdma.blocks;

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
 * Registrierung aller MDMA-Blöcke
 */
public class MDMABlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // REACTION CAULDRON (Step 1)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> REACTION_CAULDRON = registerBlockWithItem(
            "reaktions_kessel", // Keep registry name for backwards compatibility
            () -> new ReactionCauldronBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .lightLevel(state -> 4)
            )
    );

    /**
     * @deprecated Use {@link #REACTION_CAULDRON} instead
     */
    @Deprecated
    public static final RegistryObject<Block> REAKTIONS_KESSEL = REACTION_CAULDRON;

    // ═══════════════════════════════════════════════════════════
    // DRYING OVEN (Step 2)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> DRYING_OVEN = registerBlockWithItem(
            "trocknungs_ofen", // Keep registry name for backwards compatibility
            () -> new DryingOvenBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .lightLevel(state -> 8)
            )
    );

    /**
     * @deprecated Use {@link #DRYING_OVEN} instead
     */
    @Deprecated
    public static final RegistryObject<Block> TROCKNUNGS_OFEN = DRYING_OVEN;

    // ═══════════════════════════════════════════════════════════
    // PILLEN-PRESSE (Schritt 3) - Mit Timing-Minigame!
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> PILLEN_PRESSE = registerBlockWithItem(
            "pillen_presse",
            () -> new PillenPresseBlock(
                    BlockBehaviour.Properties.of()
                            .strength(4.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
            )
    );

    private static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, Supplier<T> block) {
        RegistryObject<T> registeredBlock = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new BlockItem(registeredBlock.get(), new Item.Properties()));
        return registeredBlock;
    }
}
