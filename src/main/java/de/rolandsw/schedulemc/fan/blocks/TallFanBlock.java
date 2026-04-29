package de.rolandsw.schedulemc.fan.blocks;

import de.rolandsw.schedulemc.fan.FanTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import javax.annotation.Nullable;

/**
 * 2 Blöcke hoher Ventilator (Tier 2 und 3).
 *
 * Funktioniert wie eine Tür: untere und obere Hälfte werden gleichzeitig
 * platziert und gemeinsam abgebaut. Der {@code IMultiblockBooster} ist auf
 * beiden Hälften aktiv, da {@code MultiblockHelper} nur horizontal scannt
 * und pro Richtung immer nur eine Hälfte trifft.
 *
 * Platzierung: Spieler schaut in Richtung Ziel → FACING zeigt in diese Richtung.
 * Voraussetzung: über der Zielposition muss ein ersetzbarer Block sein.
 */
public class TallFanBlock extends FanBlock {

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public TallFanBlock(FanTier tier, BlockBehaviour.Properties props) {
        super(tier, props);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    // ─────────────────────────────────────────────────────────────────
    // Platzierung: beide Hälften gleichzeitig setzen
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        Level level = ctx.getLevel();
        // Obere Hälfte muss ersetzbar sein und innerhalb der Weltgrenze liegen
        if (pos.getY() < level.getMaxBuildHeight() - 1
                && level.getBlockState(pos.above()).canBeReplaced(ctx)) {
            return defaultBlockState()
                    .setValue(FACING, ctx.getHorizontalDirection())
                    .setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null; // Kein Platz für obere Hälfte → Platzierung abbrechen
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                             @Nullable LivingEntity placer, ItemStack stack) {
        // Obere Hälfte automatisch platzieren
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    // ─────────────────────────────────────────────────────────────────
    // Halbstruktur: wenn eine Hälfte zerstört wird, fällt die andere
    // ─────────────────────────────────────────────────────────────────

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                   LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        boolean partnerDirection =
                (half == DoubleBlockHalf.LOWER && direction == Direction.UP) ||
                (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN);

        if (partnerDirection) {
            // Die andere Hälfte muss derselbe Block mit entgegengesetztem HALF sein
            if (!neighborState.is(this) || neighborState.getValue(HALF) == half) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    /**
     * Im Creative-Modus: beide Hälften auf einmal abbauen (kein doppelter Item-Drop).
     */
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherPos = (half == DoubleBlockHalf.LOWER) ? pos.above() : pos.below();
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(this) && otherState.getValue(HALF) != half) {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, 2001, otherPos, Block.getId(otherState));
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}
