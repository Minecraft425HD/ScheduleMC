package de.rolandsw.schedulemc.mushroom.blocks;

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
 * Registriert alle Pilz-Blöcke
 */
public class MushroomBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // KLIMALAMPEN (3 Stufen)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> KLIMALAMPE_SMALL = BLOCKS.register("klimalampe_small",
            () -> new KlimalampeBlock(KlimalampeTier.SMALL,
                    BlockBehaviour.Properties.of()
                            .strength(2.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final RegistryObject<Block> KLIMALAMPE_MEDIUM = BLOCKS.register("klimalampe_medium",
            () -> new KlimalampeBlock(KlimalampeTier.MEDIUM,
                    BlockBehaviour.Properties.of()
                            .strength(2.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final RegistryObject<Block> KLIMALAMPE_LARGE = BLOCKS.register("klimalampe_large",
            () -> new KlimalampeBlock(KlimalampeTier.LARGE,
                    BlockBehaviour.Properties.of()
                            .strength(3.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    // ═══════════════════════════════════════════════════════════
    // WASSERTANK
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> WASSERTANK = BLOCKS.register("wassertank",
            () -> new WassertankBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    // ═══════════════════════════════════════════════════════════
    // BLOCK ITEMS
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> KLIMALAMPE_SMALL_ITEM = ITEMS.register("klimalampe_small",
            () -> new BlockItem(KLIMALAMPE_SMALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> KLIMALAMPE_MEDIUM_ITEM = ITEMS.register("klimalampe_medium",
            () -> new BlockItem(KLIMALAMPE_MEDIUM.get(), new Item.Properties()));

    public static final RegistryObject<Item> KLIMALAMPE_LARGE_ITEM = ITEMS.register("klimalampe_large",
            () -> new BlockItem(KLIMALAMPE_LARGE.get(), new Item.Properties()));

    public static final RegistryObject<Item> WASSERTANK_ITEM = ITEMS.register("wassertank",
            () -> new BlockItem(WASSERTANK.get(), new Item.Properties()));
}
