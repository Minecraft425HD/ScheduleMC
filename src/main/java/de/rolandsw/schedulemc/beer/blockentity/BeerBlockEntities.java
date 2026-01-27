package de.rolandsw.schedulemc.beer.blockentity;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.beer.blocks.BeerBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Zentrale Registrierung aller Beer BlockEntities
 */
public class BeerBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        ForgeRegistries.BLOCK_ENTITY_TYPES, ScheduleMC.MOD_ID
    );

    // Malting Station
    public static final RegistryObject<BlockEntityType<MaltingStationBlockEntity>> MALTING_STATION =
        BLOCK_ENTITIES.register("malting_station", () ->
            BlockEntityType.Builder.of(MaltingStationBlockEntity::new,
                BeerBlocks.MALTING_STATION.get()
            ).build(null));

    // Mash Tun
    public static final RegistryObject<BlockEntityType<MashTunBlockEntity>> MASH_TUN =
        BLOCK_ENTITIES.register("mash_tun", () ->
            BlockEntityType.Builder.of(MashTunBlockEntity::new,
                BeerBlocks.MASH_TUN.get()
            ).build(null));

    // Brew Kettles
    public static final RegistryObject<BlockEntityType<SmallBrewKettleBlockEntity>> SMALL_BREW_KETTLE =
        BLOCK_ENTITIES.register("small_brew_kettle", () ->
            BlockEntityType.Builder.of(SmallBrewKettleBlockEntity::new,
                BeerBlocks.SMALL_BREW_KETTLE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumBrewKettleBlockEntity>> MEDIUM_BREW_KETTLE =
        BLOCK_ENTITIES.register("medium_brew_kettle", () ->
            BlockEntityType.Builder.of(MediumBrewKettleBlockEntity::new,
                BeerBlocks.MEDIUM_BREW_KETTLE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargeBrewKettleBlockEntity>> LARGE_BREW_KETTLE =
        BLOCK_ENTITIES.register("large_brew_kettle", () ->
            BlockEntityType.Builder.of(LargeBrewKettleBlockEntity::new,
                BeerBlocks.LARGE_BREW_KETTLE.get()
            ).build(null));

    // Fermentation Tanks
    public static final RegistryObject<BlockEntityType<SmallBeerFermentationTankBlockEntity>> SMALL_BEER_FERMENTATION_TANK =
        BLOCK_ENTITIES.register("small_beer_fermentation_tank", () ->
            BlockEntityType.Builder.of(SmallBeerFermentationTankBlockEntity::new,
                BeerBlocks.SMALL_FERMENTATION_TANK.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumBeerFermentationTankBlockEntity>> MEDIUM_BEER_FERMENTATION_TANK =
        BLOCK_ENTITIES.register("medium_beer_fermentation_tank", () ->
            BlockEntityType.Builder.of(MediumBeerFermentationTankBlockEntity::new,
                BeerBlocks.MEDIUM_FERMENTATION_TANK.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargeBeerFermentationTankBlockEntity>> LARGE_BEER_FERMENTATION_TANK =
        BLOCK_ENTITIES.register("large_beer_fermentation_tank", () ->
            BlockEntityType.Builder.of(LargeBeerFermentationTankBlockEntity::new,
                BeerBlocks.LARGE_FERMENTATION_TANK.get()
            ).build(null));

    // Conditioning Tanks
    public static final RegistryObject<BlockEntityType<SmallConditioningTankBlockEntity>> SMALL_CONDITIONING_TANK =
        BLOCK_ENTITIES.register("small_conditioning_tank", () ->
            BlockEntityType.Builder.of(SmallConditioningTankBlockEntity::new,
                BeerBlocks.SMALL_CONDITIONING_TANK.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MediumConditioningTankBlockEntity>> MEDIUM_CONDITIONING_TANK =
        BLOCK_ENTITIES.register("medium_conditioning_tank", () ->
            BlockEntityType.Builder.of(MediumConditioningTankBlockEntity::new,
                BeerBlocks.MEDIUM_CONDITIONING_TANK.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LargeConditioningTankBlockEntity>> LARGE_CONDITIONING_TANK =
        BLOCK_ENTITIES.register("large_conditioning_tank", () ->
            BlockEntityType.Builder.of(LargeConditioningTankBlockEntity::new,
                BeerBlocks.LARGE_CONDITIONING_TANK.get()
            ).build(null));

    // Bottling Station
    public static final RegistryObject<BlockEntityType<BottlingStationBlockEntity>> BEER_BOTTLING_STATION =
        BLOCK_ENTITIES.register("beer_bottling_station", () ->
            BlockEntityType.Builder.of(BottlingStationBlockEntity::new,
                BeerBlocks.BOTTLING_STATION.get()
            ).build(null));
}
