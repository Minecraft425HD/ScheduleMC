package de.rolandsw.schedulemc.vehicle.blocks;

import de.rolandsw.schedulemc.vehicle.blocks.tileentity.TileEntityWerkstatt;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.gui.TileEntityContainerProvider;
import de.maxhenkel.corelib.blockentity.SimpleBlockEntityTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class BlockWerkstatt extends BlockOrientableHorizontal {

    public BlockWerkstatt() {
        super(MapColor.METAL, SoundType.METAL, 4.5F, 60F);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityWerkstatt werkstatt) {
                // Try to find a vehicle near the werkstatt
                EntityGenericVehicle vehicle = werkstatt.getTrackedVehicle();

                if (vehicle != null && !vehicle.isRemoved()) {
                    // Open werkstatt GUI with the tracked vehicle
                    if (player instanceof ServerPlayer serverPlayer) {
                        werkstatt.openWerkstattGUI(serverPlayer, vehicle);
                    }
                    return InteractionResult.SUCCESS;
                } else {
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.schedulemc.werkstatt.no_vehicle"),
                        true
                    );
                    return InteractionResult.FAIL;
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityWerkstatt(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return new SimpleBlockEntityTicker<>();
    }
}
