package de.rolandsw.schedulemc.coffee.blocks;

import de.rolandsw.schedulemc.coffee.CoffeeType;
import de.rolandsw.schedulemc.production.blockentity.PlantPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Kaffee-Pflanze Block - wächst in 10 Stufen (0-9), wird ab Stufe 5 zwei Blöcke hoch
 * Droppt Kaffeekirschen beim Ernten
 */
public class CoffeePlantBlock extends Block {

    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 9);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    // Collision-Boxes
    private static final VoxelShape SHAPE_SMALL = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 8.0D, 13.0D);
    private static final VoxelShape SHAPE_MEDIUM = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE_TALL = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    private final CoffeeType coffeeType;

    public CoffeePlantBlock(CoffeeType type) {
        super(BlockBehaviour.Properties.copy(Blocks.WHEAT)
                .noCollission()
                .strength(0.0F)
                .sound(net.minecraft.world.level.block.SoundType.CROP));

        this.coffeeType = type;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public CoffeeType getCoffeeType() {
        return coffeeType;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        DoubleBlockHalf half = state.getValue(HALF);

        if (age < 5) {
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
                if (state.getValue(AGE) >= 5 && neighborState.getBlock() != this) {
                    return Blocks.AIR.defaultBlockState();
                }
            } else if (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN) {
                if (neighborState.getBlock() != this || neighborState.getValue(HALF) != DoubleBlockHalf.LOWER) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }

        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN) {
            return !canSurvive(state, level, currentPos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }

        // Unterer Block: muss auf PlantPotBlock stehen
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.getBlock() instanceof de.rolandsw.schedulemc.production.blocks.PlantPotBlock;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos potPos;

            if (half == DoubleBlockHalf.LOWER) {
                potPos = pos.below();
            } else {
                potPos = pos.below(2);
                super.playerWillDestroy(level, pos, state, player);
                return;
            }

            var be = level.getBlockEntity(potPos);
            if (be instanceof PlantPotBlockEntity potBE) {
                // Coffee plants grow as separate blocks on top of pots
                // They don't use the PlantPotData system
                int age = state.getValue(AGE);
                if (age >= 9) {
                    // TODO: Ernte Kaffeekirschen implementieren
                    player.displayClientMessage(Component.literal("Coffee cherries harvested!"), true);
                } else {
                    player.displayClientMessage(Component.translatable(
                        "block.plant_pot.coffee_not_fully_grown",
                        (age * 100 / 9)
                    ), true);
                }
                potBE.setChanged();
            }

            if (half == DoubleBlockHalf.UPPER) {
                BlockPos lowerPos = pos.below();
                BlockState lowerState = level.getBlockState(lowerPos);
                if (lowerState.getBlock() instanceof CoffeePlantBlock) {
                    level.destroyBlock(lowerPos, false);
                }
            }
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Wächst zur nächsten Stufe
     */
    public static void growToStage(Level level, BlockPos potPos, int newAge, CoffeeType type) {
        BlockPos plantPos = potPos.above();
        Block plantBlock = getPlantBlockForType(type);

        if (newAge <= 9) {
            BlockState lowerState = plantBlock.defaultBlockState()
                    .setValue(AGE, newAge)
                    .setValue(HALF, DoubleBlockHalf.LOWER);
            level.setBlock(plantPos, lowerState, 3);

            if (newAge >= 5) {
                BlockState upperState = plantBlock.defaultBlockState()
                        .setValue(AGE, newAge)
                        .setValue(HALF, DoubleBlockHalf.UPPER);
                level.setBlock(plantPos.above(), upperState, 3);
            } else {
                BlockState above = level.getBlockState(plantPos.above());
                if (above.getBlock() instanceof CoffeePlantBlock) {
                    level.setBlock(plantPos.above(), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    /**
     * Entfernt die Pflanze
     */
    public static void removePlant(Level level, BlockPos potPos) {
        BlockPos plantPos = potPos.above();
        BlockState state = level.getBlockState(plantPos);

        if (state.getBlock() instanceof CoffeePlantBlock) {
            level.setBlock(plantPos, Blocks.AIR.defaultBlockState(), 3);

            BlockState above = level.getBlockState(plantPos.above());
            if (above.getBlock() instanceof CoffeePlantBlock) {
                level.setBlock(plantPos.above(), Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    /**
     * Gibt den Pflanzen-Block für einen Kaffee-Typ zurück
     */
    private static Block getPlantBlockForType(CoffeeType type) {
        return switch (type) {
            case ARABICA -> CoffeeBlocks.ARABICA_PLANT.get();
            case ROBUSTA -> CoffeeBlocks.ROBUSTA_PLANT.get();
            case LIBERICA -> CoffeeBlocks.LIBERICA_PLANT.get();
            case EXCELSA -> CoffeeBlocks.EXCELSA_PLANT.get();
        };
    }
}
