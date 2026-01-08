package de.rolandsw.schedulemc.mushroom.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.mushroom.blocks.MushroomBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registriert alle Pilz-BlockEntities
 */
public class MushroomBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID);

    // Climate Lamp (all 3 tiers)
    public static final RegistryObject<BlockEntityType<ClimateLampBlockEntity>> CLIMATE_LAMP =
            BLOCK_ENTITIES.register("klimalampe", () ->
                    BlockEntityType.Builder.of(ClimateLampBlockEntity::new,
                            MushroomBlocks.CLIMATE_LAMP_SMALL.get(),
                            MushroomBlocks.CLIMATE_LAMP_MEDIUM.get(),
                            MushroomBlocks.CLIMATE_LAMP_LARGE.get()
                    ).build(null));

    /** @deprecated Use {@link #CLIMATE_LAMP} */
    @Deprecated
    public static final RegistryObject<BlockEntityType<ClimateLampBlockEntity>> KLIMALAMPE = CLIMATE_LAMP;

    // Wassertank
    public static final RegistryObject<BlockEntityType<WassertankBlockEntity>> WASSERTANK =
            BLOCK_ENTITIES.register("wassertank", () ->
                    BlockEntityType.Builder.of(WassertankBlockEntity::new,
                            MushroomBlocks.WASSERTANK.get()
                    ).build(null));
}
