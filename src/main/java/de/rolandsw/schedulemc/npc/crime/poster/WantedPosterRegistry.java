package de.rolandsw.schedulemc.npc.crime.poster;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung von {@link WantedPosterBlock}, {@link WantedPosterItem} und der
 * zugehörigen {@link WantedPosterBlockEntity}.
 */
public class WantedPosterRegistry {

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<Block> WANTED_POSTER_BLOCK = BLOCKS.register(
        "wanted_poster",
        () -> new WantedPosterBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(0.1f)
                .sound(SoundType.WOOL)
                .noOcclusion()
                .noCollission()
        )
    );

    public static final RegistryObject<Item> WANTED_POSTER_ITEM = ITEMS.register(
        "wanted_poster",
        () -> new WantedPosterItem(WANTED_POSTER_BLOCK.get(), new Item.Properties().stacksTo(16))
    );

    public static final RegistryObject<BlockEntityType<WantedPosterBlockEntity>> WANTED_POSTER_BLOCK_ENTITY =
        BLOCK_ENTITIES.register("wanted_poster",
            () -> BlockEntityType.Builder.of(
                WantedPosterBlockEntity::new,
                WANTED_POSTER_BLOCK.get()
            ).build(null));
}
