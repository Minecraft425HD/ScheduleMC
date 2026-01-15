package de.rolandsw.schedulemc.poppy.blocks;

import de.rolandsw.schedulemc.poppy.PoppyType;
import de.rolandsw.schedulemc.poppy.items.PoppyPodItem;
import de.rolandsw.schedulemc.production.blockentity.PlantPotBlockEntity;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.production.blocks.PlantPotBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Mohn-Pflanze Block - wächst in 8 Stufen, wird ab Stufe 4 zwei Blöcke hoch
 * Droppt Mohnkapseln beim Abbauen
 */
public class PoppyPlantBlock extends Block {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_7; // 0-7
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape SHAPE_SMALL = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);
    private static final VoxelShape SHAPE_MEDIUM = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE_TALL = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    private final PoppyType poppyType;

    public PoppyPlantBlock(PoppyType type) {
        super(BlockBehaviour.Properties.copy(Blocks.WHEAT)
                .noCollission()
                .strength(0.0F)
                .sound(net.minecraft.world.level.block.SoundType.CROP));

        this.poppyType = type;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public PoppyType getPoppyType() {
        return poppyType;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        DoubleBlockHalf half = state.getValue(HALF);

        if (age < 4) {
            return SHAPE_SMALL;
        } else if (half == DoubleBlockHalf.UPPER) {
            return SHAPE_TALL;
        } else {
            return SHAPE_MEDIUM;
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (direction.getAxis() == Direction.Axis.Y) {
            if (half == DoubleBlockHalf.LOWER && direction == Direction.UP) {
                if (state.getValue(AGE) >= 4 && neighborState.getBlock() != this) {
                    return Blocks.AIR.defaultBlockState();
                }
            } else if (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN) {
                if (neighborState.getBlock() != this || neighborState.getValue(HALF) != DoubleBlockHalf.LOWER) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }

        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN) {
            return !canSurvive(state, level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }

        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.getBlock() instanceof PlantPotBlock;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();

        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            int age = state.getValue(AGE);

            if (age >= 7) {
                int yield = poppyType.getBaseYield();
                drops.add(PoppyPodItem.create(poppyType, TobaccoQuality.GUT, yield));
            } else if (age >= 4) {
                int yield = (int) (poppyType.getBaseYield() * 0.5);
                drops.add(PoppyPodItem.create(poppyType, TobaccoQuality.SCHLECHT, yield));
            }
        }

        return drops;
    }

    /**
     * Wächst zur nächsten Stufe (wird vom PlantPotBlockEntity aufgerufen)
     */
    public static void growToStage(Level level, BlockPos potPos, int newAge, PoppyType type) {
        BlockPos plantPos = potPos.above();

        Block plantBlock = getPlantBlockForType(type);

        if (newAge <= 7) {
            BlockState lowerState = plantBlock.defaultBlockState()
                    .setValue(AGE, newAge)
                    .setValue(HALF, DoubleBlockHalf.LOWER);
            level.setBlock(plantPos, lowerState, 3);

            if (newAge >= 4) {
                BlockState upperState = plantBlock.defaultBlockState()
                        .setValue(AGE, newAge)
                        .setValue(HALF, DoubleBlockHalf.UPPER);
                level.setBlock(plantPos.above(), upperState, 3);
            } else {
                BlockState above = level.getBlockState(plantPos.above());
                if (above.getBlock() instanceof PoppyPlantBlock) {
                    level.setBlock(plantPos.above(), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    /**
     * Entfernt die Pflanze (beim Ernten)
     */
    public static void removePlant(Level level, BlockPos potPos) {
        BlockPos plantPos = potPos.above();
        BlockState state = level.getBlockState(plantPos);

        if (state.getBlock() instanceof PoppyPlantBlock) {
            level.setBlock(plantPos, Blocks.AIR.defaultBlockState(), 3);

            BlockState above = level.getBlockState(plantPos.above());
            if (above.getBlock() instanceof PoppyPlantBlock) {
                level.setBlock(plantPos.above(), Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    /**
     * Linksklick-Ernte - wird aufgerufen wenn die Pflanze abgebaut wird
     * Nur möglich wenn die Pflanze erntebereit ist!
     */
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos potPos;

            // Finde Topf-Position (unter der unteren Pflanzenhälfte)
            if (half == DoubleBlockHalf.LOWER) {
                potPos = pos.below();
            } else {
                // Obere Hälfte wurde abgebaut - Topf ist 2 Blöcke darunter
                potPos = pos.below(2);
                // Bei oberer Hälfte: normale Destroy-Logik (entfernt Pflanze aus Topf)
                super.playerWillDestroy(level, pos, state, player);
                return;
            }

            // Nur untere Hälfte kann geerntet werden
            var be = level.getBlockEntity(potPos);
            if (be instanceof PlantPotBlockEntity potBE) {
                var potData = potBE.getPotData();
                if (potData.hasPoppyPlant()) {
                    var plant = potData.getPoppyPlant();

                    // Prüfe ob Pflanze erntebereit ist
                    if (plant.isFullyGrown()) {
                        // Verifikation und Korrektur der Ressourcen
                        verifyAndCorrectResources(potData, 100, 33);

                        // Ernte Poppy
                        var harvested = potData.harvestPoppy();
                        if (harvested != null) {
                            // Golden Pot Qualitäts-Boost
                            var quality = harvested.getQuality();
                            var potType = ((PlantPotBlock) level.getBlockState(potPos).getBlock()).getPotType();
                            if (potType.hasQualityBoost()) {
                                quality = quality.upgrade();
                            }

                            ItemStack items = PoppyPodItem.create(
                                harvested.getType(),
                                quality,
                                harvested.getHarvestYield()
                            );

                            // Droppe Item an Spieler-Position
                            player.getInventory().add(items);

                            // Falls Inventar voll, droppe auf Boden
                            if (!items.isEmpty()) {
                                Block.popResource(level, pos, items);
                            }

                            potBE.setChanged();
                            level.sendBlockUpdated(potPos, level.getBlockState(potPos), level.getBlockState(potPos), 3);

                            String qualityBoostMsg = potType.hasQualityBoost() ? " §d(+1 Qualität!)" : "";
                            player.displayClientMessage(Component.translatable(
                                "block.plant_pot.poppy_harvested",
                                harvested.getHarvestYield(),
                                quality.getColoredName()
                            ).append(qualityBoostMsg), true);

                            player.playSound(net.minecraft.sounds.SoundEvents.CROP_BREAK, 1.0f, 1.0f);
                        }
                    } else {
                        // Nicht erntebereit - zeige Warnung aber erlaube Abbauen
                        player.displayClientMessage(Component.translatable(
                            "block.plant_pot.poppy_not_fully_grown",
                            (plant.getGrowthStage() * 100 / 7)
                        ), true);
                    }

                    // Entferne Pflanze aus Topf
                    potData.clearPlant();
                    potBE.setChanged();

                    // Client-Update senden
                    BlockState potState = level.getBlockState(potPos);
                    level.sendBlockUpdated(potPos, potState, potState, 3);
                }
            }

            // Wenn obere Hälfte abgebaut wird, entferne auch untere Hälfte
            if (half == DoubleBlockHalf.UPPER) {
                BlockPos lowerPos = pos.below();
                BlockState lowerState = level.getBlockState(lowerPos);
                if (lowerState.getBlock() instanceof PoppyPlantBlock) {
                    level.destroyBlock(lowerPos, false); // false = keine Drops
                }
            }
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Verifiziert und korrigiert die Ressourcen nach der Ernte
     * GARANTIERT dass EXAKT 100 Wasser und 33 Erde verbraucht wurden
     * Korrigiert in beide Richtungen (zu viel UND zu wenig verbraucht)
     */
    private void verifyAndCorrectResources(de.rolandsw.schedulemc.production.data.PlantPotData potData,
                                           int targetWater, int targetSoil) {
        int maxWater = potData.getMaxWater();
        int maxSoil = potData.getMaxSoil();

        double currentWater = potData.getWaterLevelExact();
        double currentSoil = potData.getSoilLevelExact();

        // Berechne wie viel TATSÄCHLICH verbraucht wurde
        double actualWaterConsumed = maxWater - currentWater;
        double actualSoilConsumed = maxSoil - currentSoil;

        // ═══════════════════════════════════════════════════════
        // WASSER: Garantiere EXAKT 100 verbraucht
        // ═══════════════════════════════════════════════════════
        double waterDifference = targetWater - actualWaterConsumed;
        if (Math.abs(waterDifference) > 0.001) {  // Nur wenn Abweichung vorhanden
            // Zu wenig verbraucht → noch mehr abziehen
            // Zu viel verbraucht → etwas zurückgeben
            double correctedWater = currentWater - waterDifference;
            potData.setWaterLevel(Math.max(0, correctedWater));
        }

        // ═══════════════════════════════════════════════════════
        // ERDE: Garantiere EXAKT 33 verbraucht
        // ═══════════════════════════════════════════════════════
        double soilDifference = targetSoil - actualSoilConsumed;
        if (Math.abs(soilDifference) > 0.001) {  // Nur wenn Abweichung vorhanden
            // Zu wenig verbraucht → noch mehr abziehen
            // Zu viel verbraucht → etwas zurückgeben
            double correctedSoil = currentSoil - soilDifference;
            potData.setSoilLevel(Math.max(0, correctedSoil));
        }
    }

    private static Block getPlantBlockForType(PoppyType type) {
        return switch (type) {
            case AFGHANISCH -> PoppyBlocks.AFGHANISCH_PLANT.get();
            case TUERKISCH -> PoppyBlocks.TUERKISCH_PLANT.get();
            case INDISCH -> PoppyBlocks.INDISCH_PLANT.get();
        };
    }
}
