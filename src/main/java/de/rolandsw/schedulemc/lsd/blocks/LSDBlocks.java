package de.rolandsw.schedulemc.lsd.blocks;

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
 * Registrierung aller LSD-Blöcke
 */
public class LSDBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // FERMENTATIONS-TANK (Schritt 1)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> FERMENTATIONS_TANK = registerBlockWithItem(
            "fermentations_tank",
            () -> new FermentationsTankBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0f)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
            )
    );

    // ═══════════════════════════════════════════════════════════
    // DESTILLATIONS-APPARAT (Schritt 2)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> DESTILLATIONS_APPARAT = registerBlockWithItem(
            "destillations_apparat",
            () -> new DestillationsApparatBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0f)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .lightLevel(state -> 6)
            )
    );

    // ═══════════════════════════════════════════════════════════
    // MIKRO-DOSIERER (Schritt 3) - Mit GUI
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> MIKRO_DOSIERER = registerBlockWithItem(
            "mikro_dosierer",
            () -> new MikroDosiererBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()
            )
    );

    // ═══════════════════════════════════════════════════════════
    // PERFORATIONS-PRESSE (Schritt 4 - Final)
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> PERFORATIONS_PRESSE = registerBlockWithItem(
            "perforations_presse",
            () -> new PerforationsPresseBlock(
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
