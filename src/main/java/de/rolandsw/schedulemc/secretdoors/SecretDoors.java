package de.rolandsw.schedulemc.secretdoors;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.secretdoors.blockentity.HiddenSwitchBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blockentity.SecretDoorBlockEntity;
import de.rolandsw.schedulemc.secretdoors.blocks.*;
import de.rolandsw.schedulemc.secretdoors.items.RemoteControlItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SecretDoors {

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);

    // ─────────────────────────────────────────────────────────────────
    // Block Properties
    // ─────────────────────────────────────────────────────────────────
    private static BlockBehaviour.Properties oakProps() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .sound(SoundType.WOOD)
            .strength(2.0f, 3.0f)
            .noOcclusion();
    }

    private static BlockBehaviour.Properties stoneProps() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .sound(SoundType.STONE)
            .strength(3.5f, 6.0f)
            .noOcclusion();
    }

    private static BlockBehaviour.Properties ironProps() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .sound(SoundType.METAL)
            .strength(5.0f, 10.0f)
            .noOcclusion();
    }

    // ─────────────────────────────────────────────────────────────────
    // Geheime Schwenktüren (Secret Pivot Doors)
    // ─────────────────────────────────────────────────────────────────
    public static final RegistryObject<Block> SECRET_DOOR_OAK = BLOCKS.register("secret_door_oak",
        () -> new SecretDoorBlock(oakProps(), AbstractSecretDoorBlock.DoorMaterial.OAK));
    public static final RegistryObject<Block> SECRET_DOOR_STONE = BLOCKS.register("secret_door_stone",
        () -> new SecretDoorBlock(stoneProps(), AbstractSecretDoorBlock.DoorMaterial.STONE));
    public static final RegistryObject<Block> SECRET_DOOR_IRON = BLOCKS.register("secret_door_iron",
        () -> new SecretDoorBlock(ironProps(), AbstractSecretDoorBlock.DoorMaterial.IRON));

    // ─────────────────────────────────────────────────────────────────
    // Schiebetüren (Sliding Doors)
    // ─────────────────────────────────────────────────────────────────
    public static final RegistryObject<Block> SLIDING_DOOR_OAK = BLOCKS.register("sliding_door_oak",
        () -> new SlidingDoorBlock(oakProps(), AbstractSecretDoorBlock.DoorMaterial.OAK));
    public static final RegistryObject<Block> SLIDING_DOOR_STONE = BLOCKS.register("sliding_door_stone",
        () -> new SlidingDoorBlock(stoneProps(), AbstractSecretDoorBlock.DoorMaterial.STONE));
    public static final RegistryObject<Block> SLIDING_DOOR_IRON = BLOCKS.register("sliding_door_iron",
        () -> new SlidingDoorBlock(ironProps(), AbstractSecretDoorBlock.DoorMaterial.IRON));

    // ─────────────────────────────────────────────────────────────────
    // Fallgitter / Tore (Portcullis)
    // ─────────────────────────────────────────────────────────────────
    public static final RegistryObject<Block> PORTCULLIS_OAK = BLOCKS.register("portcullis_oak",
        () -> new PortcullisBlock(oakProps(), AbstractSecretDoorBlock.DoorMaterial.OAK));
    public static final RegistryObject<Block> PORTCULLIS_STONE = BLOCKS.register("portcullis_stone",
        () -> new PortcullisBlock(stoneProps(), AbstractSecretDoorBlock.DoorMaterial.STONE));
    public static final RegistryObject<Block> PORTCULLIS_IRON = BLOCKS.register("portcullis_iron",
        () -> new PortcullisBlock(ironProps(), AbstractSecretDoorBlock.DoorMaterial.IRON));

    // ─────────────────────────────────────────────────────────────────
    // Bodenluken / Falltüren (Hatches)
    // ─────────────────────────────────────────────────────────────────
    public static final RegistryObject<Block> HATCH_OAK = BLOCKS.register("hatch_oak",
        () -> new HatchBlock(oakProps(), AbstractSecretDoorBlock.DoorMaterial.OAK));
    public static final RegistryObject<Block> HATCH_STONE = BLOCKS.register("hatch_stone",
        () -> new HatchBlock(stoneProps(), AbstractSecretDoorBlock.DoorMaterial.STONE));
    public static final RegistryObject<Block> HATCH_IRON = BLOCKS.register("hatch_iron",
        () -> new HatchBlock(ironProps(), AbstractSecretDoorBlock.DoorMaterial.IRON));

    // ─────────────────────────────────────────────────────────────────
    // Versteckte Schalter (Hidden Switches)
    // ─────────────────────────────────────────────────────────────────
    public static final RegistryObject<Block> HIDDEN_SWITCH_STONE = BLOCKS.register("hidden_switch_stone",
        () -> new HiddenSwitchBlock(stoneProps().strength(3.5f, 6.0f)));
    public static final RegistryObject<Block> HIDDEN_SWITCH_OAK = BLOCKS.register("hidden_switch_oak",
        () -> new HiddenSwitchBlock(oakProps().strength(2.0f, 3.0f)));

    // ─────────────────────────────────────────────────────────────────
    // Interner Füller-Block (kein Item!)
    // ─────────────────────────────────────────────────────────────────
    public static final RegistryObject<Block> DOOR_FILLER = BLOCKS.register("door_filler",
        () -> new DoorFillerBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.NONE)
            .strength(-1.0f, 3600000.0f)
            .noOcclusion()
            .noLootTable()));

    // ─────────────────────────────────────────────────────────────────
    // Block Items (alle außer door_filler)
    // ─────────────────────────────────────────────────────────────────
    public static final RegistryObject<Item> SECRET_DOOR_OAK_ITEM = ITEMS.register("secret_door_oak",
        () -> new BlockItem(SECRET_DOOR_OAK.get(), new Item.Properties()));
    public static final RegistryObject<Item> SECRET_DOOR_STONE_ITEM = ITEMS.register("secret_door_stone",
        () -> new BlockItem(SECRET_DOOR_STONE.get(), new Item.Properties()));
    public static final RegistryObject<Item> SECRET_DOOR_IRON_ITEM = ITEMS.register("secret_door_iron",
        () -> new BlockItem(SECRET_DOOR_IRON.get(), new Item.Properties()));

    public static final RegistryObject<Item> SLIDING_DOOR_OAK_ITEM = ITEMS.register("sliding_door_oak",
        () -> new BlockItem(SLIDING_DOOR_OAK.get(), new Item.Properties()));
    public static final RegistryObject<Item> SLIDING_DOOR_STONE_ITEM = ITEMS.register("sliding_door_stone",
        () -> new BlockItem(SLIDING_DOOR_STONE.get(), new Item.Properties()));
    public static final RegistryObject<Item> SLIDING_DOOR_IRON_ITEM = ITEMS.register("sliding_door_iron",
        () -> new BlockItem(SLIDING_DOOR_IRON.get(), new Item.Properties()));

    public static final RegistryObject<Item> PORTCULLIS_OAK_ITEM = ITEMS.register("portcullis_oak",
        () -> new BlockItem(PORTCULLIS_OAK.get(), new Item.Properties()));
    public static final RegistryObject<Item> PORTCULLIS_STONE_ITEM = ITEMS.register("portcullis_stone",
        () -> new BlockItem(PORTCULLIS_STONE.get(), new Item.Properties()));
    public static final RegistryObject<Item> PORTCULLIS_IRON_ITEM = ITEMS.register("portcullis_iron",
        () -> new BlockItem(PORTCULLIS_IRON.get(), new Item.Properties()));

    public static final RegistryObject<Item> HATCH_OAK_ITEM = ITEMS.register("hatch_oak",
        () -> new BlockItem(HATCH_OAK.get(), new Item.Properties()));
    public static final RegistryObject<Item> HATCH_STONE_ITEM = ITEMS.register("hatch_stone",
        () -> new BlockItem(HATCH_STONE.get(), new Item.Properties()));
    public static final RegistryObject<Item> HATCH_IRON_ITEM = ITEMS.register("hatch_iron",
        () -> new BlockItem(HATCH_IRON.get(), new Item.Properties()));

    public static final RegistryObject<Item> HIDDEN_SWITCH_STONE_ITEM = ITEMS.register("hidden_switch_stone",
        () -> new BlockItem(HIDDEN_SWITCH_STONE.get(), new Item.Properties()));
    public static final RegistryObject<Item> HIDDEN_SWITCH_OAK_ITEM = ITEMS.register("hidden_switch_oak",
        () -> new BlockItem(HIDDEN_SWITCH_OAK.get(), new Item.Properties()));

    // ─────────────────────────────────────────────────────────────────
    // Fernbedienung (Remote Control Item)
    // ─────────────────────────────────────────────────────────────────
    public static final RegistryObject<Item> REMOTE_CONTROL = ITEMS.register("remote_control",
        () -> new RemoteControlItem(new Item.Properties().stacksTo(1)));

    // ─────────────────────────────────────────────────────────────────
    // Block Entities
    // ─────────────────────────────────────────────────────────────────
    public static final RegistryObject<BlockEntityType<SecretDoorBlockEntity>> SECRET_DOOR_BE =
        BLOCK_ENTITIES.register("secret_door_be", () -> BlockEntityType.Builder.of(
            SecretDoorBlockEntity::new,
            SECRET_DOOR_OAK.get(), SECRET_DOOR_STONE.get(), SECRET_DOOR_IRON.get(),
            SLIDING_DOOR_OAK.get(), SLIDING_DOOR_STONE.get(), SLIDING_DOOR_IRON.get(),
            PORTCULLIS_OAK.get(), PORTCULLIS_STONE.get(), PORTCULLIS_IRON.get(),
            HATCH_OAK.get(), HATCH_STONE.get(), HATCH_IRON.get()
        ).build(null));

    public static final RegistryObject<BlockEntityType<HiddenSwitchBlockEntity>> HIDDEN_SWITCH_BE =
        BLOCK_ENTITIES.register("hidden_switch_be", () -> BlockEntityType.Builder.of(
            HiddenSwitchBlockEntity::new,
            HIDDEN_SWITCH_STONE.get(), HIDDEN_SWITCH_OAK.get()
        ).build(null));

    public static final RegistryObject<BlockEntityType<de.rolandsw.schedulemc.secretdoors.blockentity.DoorFillerBlockEntity>> DOOR_FILLER_BE =
        BLOCK_ENTITIES.register("door_filler_be", () -> BlockEntityType.Builder.of(
            de.rolandsw.schedulemc.secretdoors.blockentity.DoorFillerBlockEntity::new,
            DOOR_FILLER.get()
        ).build(null));
}
