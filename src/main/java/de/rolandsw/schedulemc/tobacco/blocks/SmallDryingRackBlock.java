package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.tobacco.blockentity.SmallDryingRackBlockEntity;
import de.rolandsw.schedulemc.tobacco.menu.SmallDryingRackMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Kleines Trocknungsgestell (3 Blöcke breit: links, mitte, rechts)
 * Kapazität: 6 Tabakblätter
 */
public class SmallDryingRackBlock extends AbstractDryingRackBlock {

    public SmallDryingRackBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Nur der CENTER-Block hat ein BlockEntity
        if (isMasterBlock(state)) {
            return new SmallDryingRackBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || !isMasterBlock(state)) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof SmallDryingRackBlockEntity rackBE) {
                rackBE.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        // Finde das Master-BlockEntity (CENTER)
        RackPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        BlockPos masterPos = getMasterPos(pos, part, facing);

        BlockEntity be = level.getBlockEntity(masterPos);
        if (!(be instanceof SmallDryingRackBlockEntity rackBE)) {
            return InteractionResult.PASS;
        }

        // GUI öffnen
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return Component.literal("Kleines Trocknungsgestell");
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                    return new SmallDryingRackMenu(containerId, playerInventory, rackBE);
                }
            }, masterPos);
        }

        return InteractionResult.SUCCESS;
    }
}
