package de.rolandsw.schedulemc.npc.events.speedcamera;

import de.rolandsw.schedulemc.ScheduleMC;
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

/**
 * Registrierung von {@link SpeedCameraBlock} + zugehöriger {@link SpeedCameraBlockEntity}
 * sowie dem admin-Markierungswerkzeug {@link SpeedCameraMarkerItem}.
 */
public class SpeedCameraRegistry {

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, ScheduleMC.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);

    public static final RegistryObject<Block> SPEED_CAMERA_BLOCK = BLOCKS.register(
        "speed_camera",
        () -> new SpeedCameraBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(3.5f)
                .sound(SoundType.METAL)
                .noOcclusion()
        )
    );

    public static final RegistryObject<Item> SPEED_CAMERA_ITEM = ITEMS.register(
        "speed_camera",
        () -> new BlockItem(SPEED_CAMERA_BLOCK.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> SPEED_CAMERA_MARKER_ITEM = ITEMS.register(
        "speed_camera_marker",
        () -> new SpeedCameraMarkerItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<BlockEntityType<SpeedCameraBlockEntity>> SPEED_CAMERA_BLOCK_ENTITY =
        BLOCK_ENTITIES.register("speed_camera",
            () -> BlockEntityType.Builder.of(
                SpeedCameraBlockEntity::new,
                SPEED_CAMERA_BLOCK.get()
            ).build(null));
}
