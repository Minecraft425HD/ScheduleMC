package de.rolandsw.schedulemc.vehicle.blocks.entity;

import de.rolandsw.schedulemc.vehicle.VehicleMod;
import de.rolandsw.schedulemc.vehicle.blocks.VehicleBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for all vehicle-related block entities.
 */
public class VehicleBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, VehicleMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<GasStationBlockEntity>> GAS_STATION =
            BLOCK_ENTITIES.register("gas_station",
                    () -> BlockEntityType.Builder.of(
                            GasStationBlockEntity::new,
                            VehicleBlocks.GAS_STATION.get()
                    ).build(null));
}
