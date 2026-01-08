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
    // REAKTIONS-KESSEL (Schritt 1)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> REAKTIONS_KESSEL = registerBlockWithItem(
            "reaktions_kessel",
            () -> new ReaktionsKesselBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .lightLevel(state -> 4)
            )
    );

    // ═══════════════════════════════════════════════════════════
    // TROCKNUNGS-OFEN (Schritt 2)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> TROCKNUNGS_OFEN = registerBlockWithItem(
            "trocknungs_ofen",
            () -> new TrocknungsOfenBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .lightLevel(state -> 8)
            )
    );

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
