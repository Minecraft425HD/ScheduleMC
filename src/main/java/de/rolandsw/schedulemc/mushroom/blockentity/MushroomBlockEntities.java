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

    // Klimalampe (alle 3 Stufen)
    public static final RegistryObject<BlockEntityType<KlimalampeBlockEntity>> KLIMALAMPE =
            BLOCK_ENTITIES.register("klimalampe", () ->
                    BlockEntityType.Builder.of(KlimalampeBlockEntity::new,
                            MushroomBlocks.KLIMALAMPE_SMALL.get(),
                            MushroomBlocks.KLIMALAMPE_MEDIUM.get(),
                            MushroomBlocks.KLIMALAMPE_LARGE.get()
                    ).build(null));

    // Wassertank
    public static final RegistryObject<BlockEntityType<WassertankBlockEntity>> WASSERTANK =
            BLOCK_ENTITIES.register("wassertank", () ->
                    BlockEntityType.Builder.of(WassertankBlockEntity::new,
                            MushroomBlocks.WASSERTANK.get()
                    ).build(null));
}
