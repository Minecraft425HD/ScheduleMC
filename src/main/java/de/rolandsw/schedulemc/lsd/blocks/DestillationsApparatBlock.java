package de.rolandsw.schedulemc.lsd.blocks;

import de.rolandsw.schedulemc.lsd.blockentity.DestillationsApparatBlockEntity;
import de.rolandsw.schedulemc.lsd.items.ErgotKulturItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DestillationsApparatBlock extends Block implements EntityBlock {

    public DestillationsApparatBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DestillationsApparatBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof DestillationsApparatBlockEntity apparat) {
                apparat.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof DestillationsApparatBlockEntity apparat)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Ergot-Kultur hinzufügen
        if (heldItem.getItem() instanceof ErgotKulturItem) {
            if (apparat.addErgotKultur(heldItem)) {
                if (!player.isCreative()) heldItem.shrink(1);
                player.displayClientMessage(Component.literal(
                        "§a✓ Ergot-Kultur hinzugefügt! (" + apparat.getErgotCount() + "/4)"
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.BREWING_STAND_BREW, 0.5f, 0.8f);
                return InteractionResult.SUCCESS;
            }
        }

        // Leere Hand
        if (heldItem.isEmpty()) {
            if (apparat.hasOutput()) {
                ItemStack output = apparat.extractOutput();
                if (!player.getInventory().add(output)) {
                    player.drop(output, false);
                }
                player.displayClientMessage(Component.literal(
                        "§a✓ " + output.getCount() + "x Lysergsäure entnommen!"
                ), true);
                return InteractionResult.SUCCESS;
            }

            // Status
            StringBuilder status = new StringBuilder();
            status.append("§d⚗ Destillations-Apparat\n");
            status.append("§7Ergot-Kultur: §f").append(apparat.getErgotCount()).append("/4\n");
            if (apparat.isActive()) {
                status.append("§7Fortschritt: §e").append((int)(apparat.getProgress() * 100)).append("%");
            } else if (apparat.hasOutput()) {
                status.append("§a").append(apparat.getOutputCount()).append("x Lysergsäure fertig!");
            }
            player.displayClientMessage(Component.literal(status.toString()), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
