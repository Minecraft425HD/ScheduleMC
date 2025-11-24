package de.rolandsw.schedulemc.region.blocks;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Plot-Blöcke
 */
public class PlotBlocks {

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // PLOT-INFO-BLOCK
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> PLOT_INFO_BLOCK = BLOCKS.register("plot_info_block",
        () -> new PlotInfoBlock(BlockBehaviour.Properties.of()
            .strength(2.0f)
            .sound(SoundType.WOOD)
            .noOcclusion()));

    public static final RegistryObject<Item> PLOT_INFO_BLOCK_ITEM = ITEMS.register("plot_info_block",
        () -> new BlockItem(PLOT_INFO_BLOCK.get(), new Item.Properties()));
}
