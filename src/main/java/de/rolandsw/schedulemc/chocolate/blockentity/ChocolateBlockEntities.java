package de.rolandsw.schedulemc.chocolate.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.chocolate.blocks.ChocolateBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Zentrale Registrierung aller Chocolate BlockEntities
 */
public class ChocolateBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID
    );

    // Roasting Station
    public static final RegistryObject<BlockEntityType<RoastingStationBlockEntity>> ROASTING_STATION =
        BLOCK_ENTITIES.register("roasting_station", () ->
            BlockEntityType.Builder.of(RoastingStationBlockEntity::new,
                ChocolateBlocks.ROASTING_STATION.get()
            ).build(null));

    // Winnowing Machine
    public static final RegistryObject<BlockEntityType<WinnowingMachineBlockEntity>> WINNOWING_MACHINE =
        BLOCK_ENTITIES.register("winnowing_machine", () ->
            BlockEntityType.Builder.of(WinnowingMachineBlockEntity::new,
                ChocolateBlocks.WINNOWING_MACHINE.get()
            ).build(null));

    // Grinding Mill
    public static final RegistryObject<BlockEntityType<GrindingMillBlockEntity>> GRINDING_MILL =
        BLOCK_ENTITIES.register("grinding_mill", () ->
            BlockEntityType.Builder.of(GrindingMillBlockEntity::new,
                ChocolateBlocks.GRINDING_MILL.get()
            ).build(null));

    // Pressing Station
    public static final RegistryObject<BlockEntityType<PressingStationBlockEntity>> PRESSING_STATION =
        BLOCK_ENTITIES.register("pressing_station", () ->
            BlockEntityType.Builder.of(PressingStationBlockEntity::new,
                ChocolateBlocks.PRESSING_STATION.get()
            ).build(null));

    // Conching Machines
    public static final RegistryObject<BlockEntityType<SmallConchingMachineBlockEntity>> SMALL_CONCHING_MACHINE =
        BLOCK_ENTITIES.register("small_conching_machine", () ->
            BlockEntityType.Builder.of(SmallConchingMachineBlockEntity::new,
                ChocolateBlocks.SMALL_CONCHING_MACHINE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumConchingMachineBlockEntity>> MEDIUM_CONCHING_MACHINE =
        BLOCK_ENTITIES.register("medium_conching_machine", () ->
            BlockEntityType.Builder.of(MediumConchingMachineBlockEntity::new,
                ChocolateBlocks.MEDIUM_CONCHING_MACHINE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargeConchingMachineBlockEntity>> LARGE_CONCHING_MACHINE =
        BLOCK_ENTITIES.register("large_conching_machine", () ->
            BlockEntityType.Builder.of(LargeConchingMachineBlockEntity::new,
                ChocolateBlocks.LARGE_CONCHING_MACHINE.get()
            ).build(null));

    // Tempering Station
    public static final RegistryObject<BlockEntityType<TemperingStationBlockEntity>> TEMPERING_STATION =
        BLOCK_ENTITIES.register("tempering_station", () ->
            BlockEntityType.Builder.of(TemperingStationBlockEntity::new,
                ChocolateBlocks.TEMPERING_STATION.get()
            ).build(null));

    // Molding Stations
    public static final RegistryObject<BlockEntityType<SmallMoldingStationBlockEntity>> SMALL_MOLDING_STATION =
        BLOCK_ENTITIES.register("small_molding_station", () ->
            BlockEntityType.Builder.of(SmallMoldingStationBlockEntity::new,
                ChocolateBlocks.SMALL_MOLDING_STATION.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumMoldingStationBlockEntity>> MEDIUM_MOLDING_STATION =
        BLOCK_ENTITIES.register("medium_molding_station", () ->
            BlockEntityType.Builder.of(MediumMoldingStationBlockEntity::new,
                ChocolateBlocks.MEDIUM_MOLDING_STATION.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargeMoldingStationBlockEntity>> LARGE_MOLDING_STATION =
        BLOCK_ENTITIES.register("large_molding_station", () ->
            BlockEntityType.Builder.of(LargeMoldingStationBlockEntity::new,
                ChocolateBlocks.LARGE_MOLDING_STATION.get()
            ).build(null));

    // Enrobing Machine
    public static final RegistryObject<BlockEntityType<EnrobingMachineBlockEntity>> ENROBING_MACHINE =
        BLOCK_ENTITIES.register("enrobing_machine", () ->
            BlockEntityType.Builder.of(EnrobingMachineBlockEntity::new,
                ChocolateBlocks.ENROBING_MACHINE.get()
            ).build(null));

    // Cooling Tunnel
    public static final RegistryObject<BlockEntityType<CoolingTunnelBlockEntity>> COOLING_TUNNEL =
        BLOCK_ENTITIES.register("cooling_tunnel", () ->
            BlockEntityType.Builder.of(CoolingTunnelBlockEntity::new,
                ChocolateBlocks.COOLING_TUNNEL.get()
            ).build(null));

    // Wrapping Station
    public static final RegistryObject<BlockEntityType<WrappingStationBlockEntity>> WRAPPING_STATION =
        BLOCK_ENTITIES.register("wrapping_station", () ->
            BlockEntityType.Builder.of(WrappingStationBlockEntity::new,
                ChocolateBlocks.WRAPPING_STATION.get()
            ).build(null));
}
