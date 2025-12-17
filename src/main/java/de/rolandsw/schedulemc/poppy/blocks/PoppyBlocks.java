package de.rolandsw.schedulemc.poppy.blocks;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.poppy.PoppyType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Registry für alle Mohn-bezogenen Blöcke
 */
public class PoppyBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // Pflanzen-Blöcke (keine Items, nur intern verwendet)
    public static final RegistryObject<Block> AFGHANISCH_PLANT = BLOCKS.register("afghanisch_poppy_plant",
            () -> new PoppyPlantBlock(PoppyType.AFGHANISCH));
    public static final RegistryObject<Block> TUERKISCH_PLANT = BLOCKS.register("tuerkisch_poppy_plant",
            () -> new PoppyPlantBlock(PoppyType.TUERKISCH));
    public static final RegistryObject<Block> INDISCH_PLANT = BLOCKS.register("indisch_poppy_plant",
            () -> new PoppyPlantBlock(PoppyType.INDISCH));

    // Verarbeitungs-Blöcke
    public static final RegistryObject<Block> RITZMASCHINE = registerBlockWithItem("ritzmaschine",
            () -> new RitzmaschineBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.5F, 6.0F)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> OPIUM_PRESSE = registerBlockWithItem("opium_presse",
            () -> new OpiumPresseBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(4.0F, 6.0F)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> KOCHSTATION = registerBlockWithItem("kochstation",
            () -> new KochstationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.0F, 6.0F)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> HEROIN_RAFFINERIE = registerBlockWithItem("heroin_raffinerie",
            () -> new HeroinRaffinerieBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(4.0F, 6.0F)
                    .requiresCorrectToolForDrops()));

    private static RegistryObject<Block> registerBlockWithItem(String name, Supplier<Block> blockSupplier) {
        RegistryObject<Block> block = BLOCKS.register(name, blockSupplier);
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
}
