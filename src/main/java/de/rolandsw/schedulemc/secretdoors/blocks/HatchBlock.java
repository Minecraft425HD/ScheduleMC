package de.rolandsw.schedulemc.secretdoors.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Bodenluke / Falltür (Hatch).
 * Öffnet sich als Bodenplatte nach unten/zur Seite.
 * Unterstützt dynamische Größen von 1×1 bis 10×10.
 * Breitet sich horizontal (X-Z-Ebene) aus statt vertikal.
 */
public class HatchBlock extends AbstractSecretDoorBlock {

    // Luken haben keine horizontale Ausrichtung - verwende FACING für die Seite zur Wand
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public HatchBlock(BlockBehaviour.Properties props, DoorMaterial material) {
        super(props, material, DoorType.HATCH);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN, FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Luke wird an der Bodenebene in Blickrichtung platziert
        return defaultBlockState()
            .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
            .setValue(OPEN, false)
            .setValue(POWERED, false);
    }
}
