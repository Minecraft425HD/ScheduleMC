package de.rolandsw.schedulemc.cannabis.blocks;

import de.rolandsw.schedulemc.cannabis.blockentity.TrimmStationBlockEntity;
import de.rolandsw.schedulemc.cannabis.blockentity.CannabisBlockEntities;
import de.rolandsw.schedulemc.cannabis.items.DriedBudItem;
import de.rolandsw.schedulemc.cannabis.menu.TrimmStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Trimm-Station mit Minigame
 */
public class TrimmStationBlock extends BaseEntityBlock {

    public TrimmStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrimmStationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, CannabisBlockEntities.TRIMM_STATION.get(),
                (lvl, pos, st, be) -> be.tick());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TrimmStationBlockEntity station)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Output entnehmen wenn vorhanden
        if (station.hasOutput()) {
            ItemStack bud = station.extractTrimmedBud();
            ItemStack trim = station.extractTrim();
            if (!bud.isEmpty()) player.addItem(bud);
            if (!trim.isEmpty()) player.addItem(trim);
            player.displayClientMessage(Component.translatable("block.trimming_station.output_taken"), true);
            return InteractionResult.CONSUME;
        }

        // Getrocknete Buds hinzufügen
        if (heldItem.getItem() instanceof DriedBudItem && !station.hasInput()) {
            if (station.addDriedBud(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }
                player.displayClientMessage(Component.translatable("block.trimming_station.buds_added"), true);
                return InteractionResult.CONSUME;
            }
        }

        // GUI öffnen wenn Input vorhanden
        if (station.hasInput() && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new TrimmStationMenu.Provider(station), pos);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TrimmStationBlockEntity station) {
                if (station.hasOutput()) {
                    ItemStack bud = station.extractTrimmedBud();
                    ItemStack trim = station.extractTrim();
                    if (!bud.isEmpty()) Block.popResource(level, pos, bud);
                    if (!trim.isEmpty()) Block.popResource(level, pos, trim);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
