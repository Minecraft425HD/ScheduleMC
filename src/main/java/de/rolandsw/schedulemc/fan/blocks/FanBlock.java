package de.rolandsw.schedulemc.fan.blocks;

import de.rolandsw.schedulemc.fan.FanTier;
import de.rolandsw.schedulemc.multiblock.IMultiblockBooster;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Ventilator-Block in drei Leistungsstufen.
 *
 * Wenn der Ventilator mit seiner FACING-Seite in Richtung eines Trocknungsgestells
 * (oder eines anderen {@code MultiblockHelper}-kompatiblen Blocks) zeigt, beschleunigt
 * er dessen Verarbeitungszeit proportional zum Tier-Multiplikator.
 *
 * Platzierung: Der Ventilator zeigt automatisch zum Spieler hin (Vorderseite = FACING).
 * Tipp: Stelle den Ventilator so auf, dass die Lüftungsöffnung auf das Trocknungsgestell zeigt.
 *
 * Multiblock-Regeln:
 *  - Ventilator muss innerhalb von 4 Blöcken des Gestell-Masterblocks sein
 *  - FACING des Ventilators muss auf den Masterblock zeigen
 *  - Zwischen Ventilator und Gestell darf nur Luft sein (keine Wände)
 *  - Mehrere Ventilatoren stapeln sich (additive Multiplikatoren)
 */
public class FanBlock extends HorizontalDirectionalBlock implements IMultiblockBooster {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // Kompakter Körper: 2 Blöcke Tiefe (Voxel 1-16, Y 0-16, Z 1-8 = Ventilatorgehäuse)
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 4, 16, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 0, 16, 16, 12);
    private static final VoxelShape SHAPE_WEST  = Block.box(4, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_EAST  = Block.box(0, 0, 0, 12, 16, 16);

    private final FanTier tier;

    public FanBlock(FanTier tier, BlockBehaviour.Properties props) {
        super(props);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * Beim Platzieren zeigt die Vorderseite des Ventilators (FACING) zum Spieler hin,
     * d.h. der Luftstrom geht weg vom Spieler in die entgegengesetzte Richtung.
     * Stell den Ventilator hinter das Trocknungsgestell, damit er hineinbläst.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // FACING = Richtung zur Vorderseite (Luftausstritt)
        // Spieler schaut in eine Richtung → Ventilator zeigt in dieselbe Richtung
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            case EAST  -> SHAPE_EAST;
            default    -> SHAPE_NORTH;  // NORTH
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return getShape(state, level, pos, ctx);
    }

    // ─────────────────────────────────────────────────────────────────
    // IMultiblockBooster
    // ─────────────────────────────────────────────────────────────────

    @Override
    public float getBoostMultiplier() {
        return tier.getMultiplier();
    }

    // ─────────────────────────────────────────────────────────────────
    // Tooltip
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.schedulemc.fan.tooltip.tier",   tier.getLevel()));
        tooltip.add(Component.translatable("block.schedulemc.fan.tooltip.boost",  (int)((tier.getMultiplier() - 1.0f) * 100)));
        tooltip.add(Component.translatable("block.schedulemc.fan.tooltip.range"));
        tooltip.add(Component.translatable("block.schedulemc.fan.tooltip.hint"));
    }

    // ─────────────────────────────────────────────────────────────────
    // Getter
    // ─────────────────────────────────────────────────────────────────

    public FanTier getTier() {
        return tier;
    }
}
