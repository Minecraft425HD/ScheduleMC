package de.rolandsw.schedulemc.wine.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Zentrale Registrierung aller Wine BlockEntities
 *
 * Wird in Part 2 implementiert
 */
public class WineBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID
    );

    // BlockEntities werden in Part 2 registriert
}
