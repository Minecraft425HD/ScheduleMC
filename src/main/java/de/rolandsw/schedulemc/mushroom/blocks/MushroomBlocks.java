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
    // CLIMATE_LAMPN (3 Stufen)
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> CLIMATE_LAMP_SMALL = BLOCKS.register("klimalampe_small",
            () -> new ClimateLampBlock(ClimateLampTier.SMALL,
                    BlockBehaviour.Properties.of()
                            .strength(2.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final RegistryObject<Block> CLIMATE_LAMP_MEDIUM = BLOCKS.register("klimalampe_medium",
            () -> new ClimateLampBlock(ClimateLampTier.MEDIUM,
                    BlockBehaviour.Properties.of()
                            .strength(2.5f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final RegistryObject<Block> CLIMATE_LAMP_LARGE = BLOCKS.register("klimalampe_large",
            () -> new ClimateLampBlock(ClimateLampTier.LARGE,
                    BlockBehaviour.Properties.of()
                            .strength(3.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    // ═══════════════════════════════════════════════════════════
    // WATER_TANK
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Block> WATER_TANK = BLOCKS.register("wassertank",
            () -> new WaterTankBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    // ═══════════════════════════════════════════════════════════
    // BLOCK ITEMS
    // ═══════════════════════════════════════════════════════════
    public static final RegistryObject<Item> CLIMATE_LAMP_SMALL_ITEM = ITEMS.register("klimalampe_small",
            () -> new BlockItem(CLIMATE_LAMP_SMALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> CLIMATE_LAMP_MEDIUM_ITEM = ITEMS.register("klimalampe_medium",
            () -> new BlockItem(CLIMATE_LAMP_MEDIUM.get(), new Item.Properties()));

    public static final RegistryObject<Item> CLIMATE_LAMP_LARGE_ITEM = ITEMS.register("klimalampe_large",
            () -> new BlockItem(CLIMATE_LAMP_LARGE.get(), new Item.Properties()));

    public static final RegistryObject<Item> WATER_TANK_ITEM = ITEMS.register("wassertank",
            () -> new BlockItem(WATER_TANK.get(), new Item.Properties()));
}
