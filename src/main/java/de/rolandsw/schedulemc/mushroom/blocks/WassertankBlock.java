package de.rolandsw.schedulemc.mushroom.blocks;

import de.rolandsw.schedulemc.mushroom.blockentity.MushroomBlockEntities;
import de.rolandsw.schedulemc.mushroom.blockentity.WassertankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Wassertank-Block - bewässert automatisch benachbarte Töpfe
 */
public class WassertankBlock extends Block implements EntityBlock {

    public WassertankBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WassertankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof WassertankBlockEntity tank) {
                tank.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof WassertankBlockEntity tank)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Mit Wassereimer befüllen
        if (heldItem.getItem() == Items.WATER_BUCKET) {
            int added = tank.addWater(1000);
            if (added > 0) {
                // Eimer leeren
                if (!player.isCreative()) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                player.displayClientMessage(Component.translatable(
                        "block.wassertank.water_filled", tank.getWaterLevel(), tank.getMaxWater()
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable(
                        "block.wassertank.tank_full"
                ), true);
                return InteractionResult.FAIL;
            }
        }

        // Mit leerem Eimer: Wasser entnehmen
        if (heldItem.getItem() == Items.BUCKET) {
            if (tank.getWaterLevel() >= 1000) {
                tank.removeWater(1000);
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                    player.getInventory().add(new ItemStack(Items.WATER_BUCKET));
                }
                player.displayClientMessage(Component.translatable(
                        "block.wassertank.water_removed", tank.getWaterLevel(), tank.getMaxWater()
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.BUCKET_FILL, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable(
                        "block.wassertank.not_enough"
                ), true);
                return InteractionResult.FAIL;
            }
        }

        // Info anzeigen
        player.displayClientMessage(Component.translatable(
                "block.wassertank.info", tank.getWaterLevel(), tank.getMaxWater()
        ), true);

        return InteractionResult.SUCCESS;
    }
}
