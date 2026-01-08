package de.rolandsw.schedulemc.coca.blocks;

import de.rolandsw.schedulemc.coca.blockentity.CrackKocherBlockEntity;
import de.rolandsw.schedulemc.coca.blockentity.CocaBlockEntities;
import de.rolandsw.schedulemc.coca.items.CocaineItem;
import de.rolandsw.schedulemc.coca.items.CocaItems;
import de.rolandsw.schedulemc.coca.CrackQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import org.jetbrains.annotations.Nullable;

/**
 * Crack-Kocher - Kocht Kokain zu Crack
 * Mit Timing-Minigame!
 */
public class CrackKocherBlock extends BaseEntityBlock {

    public CrackKocherBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrackKocherBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, CocaBlockEntities.CRACK_KOCHER.get(),
                (lvl, pos, st, be) -> be.tick());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CrackKocherBlockEntity kocher)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        // Fertiges Crack entnehmen
        if (kocher.hasOutput()) {
            ItemStack crack = kocher.extractCrack();
            if (!crack.isEmpty()) {
                player.addItem(crack);
                player.displayClientMessage(Component.translatable("block.crack_kocher.crack_removed"), true);
                return InteractionResult.CONSUME;
            }
        }

        // Während des Kochens - Crack herausnehmen (Timing!)
        if (kocher.isWaitingForRemove()) {
            double score = kocher.removeCrack();
            CrackQuality quality = CrackQuality.fromTimingScore(score);

            Component message = switch (quality) {
                case FISHSCALE -> Component.translatable("block.crack_kocher.fishscale");
                case GUT -> Component.translatable("block.crack_kocher.good");
                case STANDARD -> Component.translatable("block.crack_kocher.standard");
                case SCHLECHT -> kocher.getCookTick() < 28 ? Component.translatable("block.crack_kocher.bad_early") : Component.translatable("block.crack_kocher.bad_burnt");
            };
            player.displayClientMessage(message, true);
            return InteractionResult.CONSUME;
        }

        // Kokain hinzufügen
        if (heldItem.getItem() instanceof CocaineItem && !kocher.isMinigameActive()) {
            if (kocher.addCocaine(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(heldItem.getCount());
                }
                player.displayClientMessage(Component.translatable("block.crack_kocher.cocaine_added")
                        .append(Component.literal(kocher.getCocaineGrams() + ""))
                        .append(Component.translatable("block.crack_kocher.cocaine_grams")), true);
                return InteractionResult.CONSUME;
            }
        }

        // Backpulver hinzufügen
        if (heldItem.is(CocaItems.BACKPULVER.get()) && !kocher.isMinigameActive()) {
            if (kocher.addBackpulver(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(heldItem.getCount());
                }
                player.displayClientMessage(Component.translatable("block.crack_kocher.baking_soda_added")
                        .append(Component.literal(kocher.getBackpulverCount() + ""))
                        .append(Component.translatable("block.crack_kocher.baking_soda_count")), true);
                return InteractionResult.CONSUME;
            }
        }

        // Kochen starten (leere Hand + Shift)
        if (heldItem.isEmpty() && player.isShiftKeyDown() && kocher.canStartCooking()) {
            if (kocher.startCooking(player.getUUID())) {
                player.displayClientMessage(Component.translatable("block.crack_kocher.cooking_started"), true);
                return InteractionResult.CONSUME;
            }
        }

        // Status anzeigen
        if (kocher.isMinigameActive()) {
            int progress = (int) (kocher.getCookProgress() * 100);
            Component zone = switch (kocher.getCurrentZone()) {
                case 0 -> Component.translatable("block.crack_kocher.zone_too_early");
                case 1 -> Component.translatable("block.crack_kocher.zone_good");
                case 2 -> Component.translatable("block.crack_kocher.zone_perfect");
                default -> Component.translatable("block.crack_kocher.zone_too_late");
            };
            player.displayClientMessage(Component.translatable("block.crack_kocher.cooking_progress")
                    .append(Component.literal(progress + ""))
                    .append(Component.translatable("block.crack_kocher.cooking_percent"))
                    .append(zone), true);
        } else if (kocher.getCocaineGrams() > 0) {
            player.displayClientMessage(Component.translatable("block.crack_kocher.status_cocaine")
                    .append(Component.literal(kocher.getCocaineGrams() + ""))
                    .append(Component.translatable("block.crack_kocher.status_grams"))
                    .append(Component.literal(kocher.getBackpulverCount() + "")), true);
            if (kocher.canStartCooking()) {
                player.displayClientMessage(Component.translatable("block.crack_kocher.shift_to_start"), false);
            } else if (kocher.getBackpulverCount() < 1) {
                player.displayClientMessage(Component.translatable("block.crack_kocher.need_baking_soda"), false);
            }
        } else {
            player.displayClientMessage(Component.translatable("block.crack_kocher.empty"), true);
            player.displayClientMessage(Component.translatable("block.crack_kocher.add_materials"), false);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CrackKocherBlockEntity kocher && kocher.hasOutput()) {
                ItemStack crack = kocher.extractCrack();
                if (!crack.isEmpty()) {
                    Block.popResource(level, pos, crack);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
