package de.rolandsw.schedulemc.production.blocks;

import de.rolandsw.schedulemc.production.core.ProductionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
 * Abstrakte Basis-Klasse für alle Pflanzen-Blöcke
 * Gemeinsame Logik für Tobacco, Cannabis, Coca, Mushroom, Poppy, etc.
 *
 * Features:
 * - 8 Wachstumsstufen (AGE 0-7)
 * - Zwei-Block-Höhe ab Stufe 4
 * - Typ-spezifisches Wachstum
 * - Automatisches Wachstum via randomTick
 *
 * @param <T> Der spezifische ProductionType (TobaccoType, CannabisStrain, etc.)
 */
public abstract class AbstractPlantBlock<T extends ProductionType> extends Block {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_7; // 0-7
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    // VoxelShapes für verschiedene Wachstumsstufen
    protected static final VoxelShape SHAPE_SMALL = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);
    protected static final VoxelShape SHAPE_MEDIUM = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    protected static final VoxelShape SHAPE_TALL = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    protected final T productionType;

    public AbstractPlantBlock(T type, BlockBehaviour.Properties properties) {
        super(properties);
        this.productionType = type;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public AbstractPlantBlock(T type) {
        this(type, BlockBehaviour.Properties.copy(Blocks.WHEAT)
                .noCollission()
                .strength(0.0F)
                .randomTicks() // Wichtig für Wachstum!
                .sound(net.minecraft.world.level.block.SoundType.CROP));
    }

    /**
     * @return Der Produktionstyp dieser Pflanze
     */
    public T getProductionType() {
        return productionType;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        DoubleBlockHalf half = state.getValue(HALF);

        if (age < getDoubleBlockAge()) {
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

        // Überprüfe vertikale Verbindungen für Doppel-Block
        if (direction.getAxis() == Direction.Axis.Y) {
            if (half == DoubleBlockHalf.LOWER && direction == Direction.UP) {
                if (state.getValue(AGE) >= getDoubleBlockAge() && neighborState.getBlock() != this) {
                    return Blocks.AIR.defaultBlockState();
                }
            } else if (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN) {
                if (neighborState.getBlock() != this || neighborState.getValue(HALF) != DoubleBlockHalf.LOWER) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }

        // Überprüfe ob Pflanze überleben kann
        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN) {
            return !canSurvive(state, level, currentPos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Kann auf dem Block darunter überleben?
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        // Upper-Half kann nur auf Lower-Half überleben
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return belowState.getBlock() == this && belowState.getValue(HALF) == DoubleBlockHalf.LOWER;
        }

        // Lower-Half braucht gültigen Boden
        return canPlaceOn(belowState, level, belowPos);
    }

    /**
     * Überprüft ob die Pflanze auf diesem Block platziert werden kann
     * Override für spezifische Anforderungen (z.B. nur auf Töpfen, nur auf Erde, etc.)
     */
    protected abstract boolean canPlaceOn(BlockState state, LevelReader level, BlockPos pos);

    /**
     * Random Tick für Wachstum
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Nur Lower-Half wächst
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return;
        }

        int age = state.getValue(AGE);
        if (age < getMaxAge()) {
            // Prüfe Wachstumsbedingungen
            if (canGrow(state, level, pos, random)) {
                // Wachse um eine Stufe
                growPlant(level, pos, state, age + 1);
            }
        }
    }

    /**
     * Lässt die Pflanze um eine Stufe wachsen
     */
    protected void growPlant(ServerLevel level, BlockPos pos, BlockState state, int newAge) {
        int currentAge = state.getValue(AGE);

        // Wenn wir zu Doppel-Block werden
        if (currentAge < getDoubleBlockAge() && newAge >= getDoubleBlockAge()) {
            BlockPos abovePos = pos.above();
            if (level.getBlockState(abovePos).isAir()) {
                // Setze Lower-Half
                level.setBlock(pos, state.setValue(AGE, newAge).setValue(HALF, DoubleBlockHalf.LOWER), 3);
                // Setze Upper-Half
                level.setBlock(abovePos, state.setValue(AGE, newAge).setValue(HALF, DoubleBlockHalf.UPPER), 3);
            }
        } else if (newAge >= getDoubleBlockAge()) {
            // Beide Hälften aktualisieren
            level.setBlock(pos, state.setValue(AGE, newAge).setValue(HALF, DoubleBlockHalf.LOWER), 3);
            BlockPos abovePos = pos.above();
            if (level.getBlockState(abovePos).getBlock() == this) {
                level.setBlock(abovePos, state.setValue(AGE, newAge).setValue(HALF, DoubleBlockHalf.UPPER), 3);
            }
        } else {
            // Einfach Age erhöhen
            level.setBlock(pos, state.setValue(AGE, newAge), 3);
        }
    }

    /**
     * Überprüft ob die Pflanze wachsen kann
     * Override für spezifische Bedingungen (Wasser, Licht, Temperatur, etc.)
     */
    protected abstract boolean canGrow(BlockState state, ServerLevel level, BlockPos pos, RandomSource random);

    /**
     * @return Ab welchem Alter die Pflanze zwei Blöcke hoch wird (Standard: 4)
     */
    protected int getDoubleBlockAge() {
        return 4;
    }

    /**
     * @return Maximales Alter der Pflanze (Standard: 7)
     */
    protected int getMaxAge() {
        return 7;
    }

    /**
     * @return Ob die Pflanze vollständig gewachsen ist
     */
    public boolean isMaxAge(BlockState state) {
        return state.getValue(AGE) >= getMaxAge();
    }

    /**
     * @return Aktuelles Alter der Pflanze
     */
    public int getAge(BlockState state) {
        return state.getValue(AGE);
    }
}
