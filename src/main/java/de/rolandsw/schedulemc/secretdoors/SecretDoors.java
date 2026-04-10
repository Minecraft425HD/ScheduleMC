package de.rolandsw.schedulemc.secretdoors;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.secretdoors.blockentity.ElevatorBlockEntity;
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
    private static BlockBehaviour.Properties doorProps() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .sound(SoundType.STONE)
            .strength(3.5f, 6.0f)
            .noOcclusion();
    }

    // ─────────────────────────────────────────────────────────────────
    // Geheimtür (horizontal – Wand)
    // ─────────────────────────────────────────────────────────────────
    public static final RegistryObject<Block> SECRET_DOOR = BLOCKS.register("secret_door",
        () -> new SecretDoorBlock(doorProps()));

    // ─────────────────────────────────────────────────────────────────
    // Bodenluke (vertikal – Boden/Decke)
    // ─────────────────────────────────────────────────────────────────
    public static final RegistryObject<Block> HATCH = BLOCKS.register("hatch",
        () -> new HatchBlock(doorProps()));

    // ─────────────────────────────────────────────────────────────────
    // Versteckte Schalter (Hidden Switches)
    // ─────────────────────────────────────────────────────────────────
    public static final RegistryObject<Block> HIDDEN_SWITCH_STONE = BLOCKS.register("hidden_switch_stone",
        () -> new HiddenSwitchBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .sound(SoundType.STONE)
            .strength(3.5f, 6.0f)
            .noOcclusion()));

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
    public static final RegistryObject<Item> SECRET_DOOR_ITEM = ITEMS.register("secret_door",
        () -> new BlockItem(SECRET_DOOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> HATCH_ITEM = ITEMS.register("hatch",
        () -> new BlockItem(HATCH.get(), new Item.Properties()));

    public static final RegistryObject<Item> HIDDEN_SWITCH_STONE_ITEM = ITEMS.register("hidden_switch_stone",
        () -> new BlockItem(HIDDEN_SWITCH_STONE.get(), new Item.Properties()));

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
            SECRET_DOOR.get(), HATCH.get()
        ).build(null));

    public static final RegistryObject<BlockEntityType<HiddenSwitchBlockEntity>> HIDDEN_SWITCH_BE =
        BLOCK_ENTITIES.register("hidden_switch_be", () -> BlockEntityType.Builder.of(
            HiddenSwitchBlockEntity::new,
            HIDDEN_SWITCH_STONE.get()
        ).build(null));

    public static final RegistryObject<BlockEntityType<de.rolandsw.schedulemc.secretdoors.blockentity.DoorFillerBlockEntity>> DOOR_FILLER_BE =
        BLOCK_ENTITIES.register("door_filler_be", () -> BlockEntityType.Builder.of(
            de.rolandsw.schedulemc.secretdoors.blockentity.DoorFillerBlockEntity::new,
            DOOR_FILLER.get()
        ).build(null));

    // ─────────────────────────────────────────────────────────────────
    // Aufzug (Elevator)
    // ─────────────────────────────────────────────────────────────────
    public static final RegistryObject<Block> ELEVATOR = BLOCKS.register("elevator",
        () -> new ElevatorBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .sound(SoundType.STONE)
            .strength(3.5f, 6.0f)
            .noOcclusion()));

    public static final RegistryObject<Item> ELEVATOR_ITEM = ITEMS.register("elevator",
        () -> new BlockItem(ELEVATOR.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<ElevatorBlockEntity>> ELEVATOR_BE =
        BLOCK_ENTITIES.register("elevator_be", () -> BlockEntityType.Builder.of(
            ElevatorBlockEntity::new,
            ELEVATOR.get()
        ).build(null));
}
