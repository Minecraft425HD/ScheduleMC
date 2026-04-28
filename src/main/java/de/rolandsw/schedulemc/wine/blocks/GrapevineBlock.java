package de.rolandsw.schedulemc.wine.blocks;

import de.rolandsw.schedulemc.production.blockentity.PlantPotBlockEntity;
import de.rolandsw.schedulemc.wine.WineQuality;
import de.rolandsw.schedulemc.wine.WineType;
import de.rolandsw.schedulemc.wine.items.GrapeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class GrapevineBlock extends Block {
    public static final int MAX_AGE = 7;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
        Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    };

    public GrapevineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                        @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE_BY_AGE[state.getValue(AGE)];
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level,
                          @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (level.getRawBrightness(pos, 0) >= 9) {
            int age = state.getValue(AGE);
            if (age < MAX_AGE && random.nextInt(5) == 0) {
                level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public void playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos,
                                  @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide) {
            BlockPos potPos = pos.below();
            var be = level.getBlockEntity(potPos);
            if (be instanceof PlantPotBlockEntity potBE) {
                var potData = potBE.getPotData();
                if (potData.hasGrapePlant()) {
                    var grape = potData.getGrapePlant();
                    if (grape.isFullyGrown()) {
                        WineQuality quality = WineQuality.determineQuality(
                            grape.getType().getQualityFactor(), new java.util.Random(level.random.nextLong()));
                        int yield = grape.getType().getYieldPerPlant();
                        ItemStack grapes = GrapeItem.create(grape.getType(), quality, yield);
                        player.getInventory().add(grapes);
                        if (!grapes.isEmpty()) {
                            Block.popResource(level, pos, grapes);
                        }
                        double newSoil = Math.max(0, potData.getSoilLevelExact() - 33);
                        potData.setSoilLevel(newSoil);
                        potData.setSoilLevelAtPlanting(newSoil);
                        player.displayClientMessage(Component.translatable(
                            "block.plant_pot.grape_harvested",
                            yield, quality.getColoredName()), true);
                    }
                    potData.clearPlant();
                    potData.setWaterLevel(0);
                    potBE.setChanged();
                    level.sendBlockUpdated(potPos, level.getBlockState(potPos), level.getBlockState(potPos), 3);
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    public static void growToStage(Level level, BlockPos pos, int newAge, WineType type) {
        BlockPos plantPos = pos.above();
        BlockState current = level.getBlockState(plantPos);
        if (current.getBlock() instanceof GrapevineBlock || newAge == 0) {
            level.setBlock(plantPos,
                WineBlocks.GRAPEVINE.get().defaultBlockState().setValue(AGE, Math.min(newAge, MAX_AGE)), 3);
        }
    }
}
