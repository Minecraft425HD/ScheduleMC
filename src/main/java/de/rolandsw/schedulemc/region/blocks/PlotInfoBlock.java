package de.rolandsw.schedulemc.region.blocks;

import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Plot-Info-Block
 *
 * Zeigt Plot-Informationen:
 * - HUD-Overlay beim Anschauen (PlotInfoHudOverlay)
 * - GUI-Screen beim Rechtsklick (PlotInfoScreen)
 *
 * Features:
 * - Plot-Name, Besitzer, Größe
 * - Verkaufs-/Mietstatus mit Preisen
 * - Apartment-Liste mit Verfügbarkeit
 * - Interaktive Buttons (Kaufen, Mieten)
 */
public class PlotInfoBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public PlotInfoBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
            .setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                   Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            // Server-Seite: Nichts tun (GUI wird clientseitig geöffnet)
            return InteractionResult.SUCCESS;
        }

        // Client-Seite: Öffne GUI
        PlotRegion plot = PlotManager.getPlotAt(pos);

        if (plot == null) {
            player.sendSystemMessage(Component.literal(
                "§c✗ Kein Plot gefunden!\n" +
                "§7Dieser Info-Block ist nicht in einem gültigen Plot platziert."
            ));
            return InteractionResult.SUCCESS;
        }

        // Öffne GUI-Screen
        openPlotInfoScreen(plot);

        return InteractionResult.SUCCESS;
    }

    /**
     * Öffnet das Plot-Info-GUI (nur Client-Seite)
     */
    private void openPlotInfoScreen(PlotRegion plot) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        mc.setScreen(new de.rolandsw.schedulemc.client.PlotInfoScreen(plot));
    }
}
