package de.rolandsw.schedulemc.warehouse;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registrierung aller Warehouse-Blöcke
 */
public class WarehouseBlocks {

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);

    // ═══════════════════════════════════════════════════════════
    // WAREHOUSE BLOCK
    // ═══════════════════════════════════════════════════════════

    public static final RegistryObject<Block> WAREHOUSE = BLOCKS.register("warehouse",
        () -> new WarehouseBlock(BlockBehaviour.Properties.of()
            .strength(5.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> WAREHOUSE_ITEM = ITEMS.register("warehouse",
        () -> new BlockItem(WAREHOUSE.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<WarehouseBlockEntity>> WAREHOUSE_BLOCK_ENTITY =
        BLOCK_ENTITIES.register("warehouse_block_entity", () ->
            BlockEntityType.Builder.of(WarehouseBlockEntity::new, WAREHOUSE.get()).build(null));
}
