package de.rolandsw.schedulemc.economy.blocks;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.economy.blockentity.ATMBlockEntity;
import de.rolandsw.schedulemc.economy.blockentity.CashBlockEntity;
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
 * Registrierung aller Economy-Blöcke (MIT ATM!)
 */
public class EconomyBlocks {
    
    public static final DeferredRegister<Block> BLOCKS = 
        DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);
    
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);
    
    // ═══════════════════════════════════════════════════════════
    // BARGELD-BLOCK
    // ═══════════════════════════════════════════════════════════
    
    public static final RegistryObject<Block> CASH_BLOCK = BLOCKS.register("cash_block",
        () -> new CashBlock());
    
    public static final RegistryObject<Item> CASH_BLOCK_ITEM = ITEMS.register("cash_block",
        () -> new BlockItem(CASH_BLOCK.get(), new Item.Properties()));
    
    public static final RegistryObject<BlockEntityType<CashBlockEntity>> CASH_BLOCK_ENTITY = 
        BLOCK_ENTITIES.register("cash_block_entity", () -> 
            BlockEntityType.Builder.of(CashBlockEntity::new, CASH_BLOCK.get()).build(null));
    
    // ═══════════════════════════════════════════════════════════
    // GELDAUTOMAT (ATM) - NEU!
    // ═══════════════════════════════════════════════════════════
    
    public static final RegistryObject<Block> ATM_BLOCK = BLOCKS.register("atm",
        () -> new ATMBlock(BlockBehaviour.Properties.of()
            .strength(5.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()));
    
    public static final RegistryObject<Item> ATM_BLOCK_ITEM = ITEMS.register("atm",
        () -> new BlockItem(ATM_BLOCK.get(), new Item.Properties()));
    
    public static final RegistryObject<BlockEntityType<ATMBlockEntity>> ATM_BLOCK_ENTITY = 
        BLOCK_ENTITIES.register("atm_block_entity", () -> 
            BlockEntityType.Builder.of(ATMBlockEntity::new, ATM_BLOCK.get()).build(null));
}
