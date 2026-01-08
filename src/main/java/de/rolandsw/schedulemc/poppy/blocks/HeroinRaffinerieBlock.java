package de.rolandsw.schedulemc.poppy.blocks;

import de.rolandsw.schedulemc.poppy.blockentity.HeroinRaffinerieBlockEntity;
import de.rolandsw.schedulemc.poppy.items.MorphineItem;
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
 * Heroin-Raffinerie - raffiniert Morphin zu Heroin
 */
public class HeroinRaffinerieBlock extends Block implements EntityBlock {

    public HeroinRaffinerieBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeroinRaffinerieBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof HeroinRaffinerieBlockEntity raffBE) {
                raffBE.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof HeroinRaffinerieBlockEntity raffBE)) return InteractionResult.PASS;

        ItemStack handStack = player.getItemInHand(hand);

        // 1. Brennstoff hinzufügen
        if (isFuel(handStack)) {
            int fuelValue = getFuelValue(handStack);
            if (raffBE.getFuelLevel() >= raffBE.getMaxFuel()) {
                player.displayClientMessage(Component.translatable("message.heroin_raffinerie.fuel_tank_full"), true);
                return InteractionResult.FAIL;
            }

            raffBE.addFuel(fuelValue);
            handStack.shrink(1);
            player.displayClientMessage(Component.translatable("message.heroin_raffinerie.fuel_added", raffBE.getFuelLevel(), raffBE.getMaxFuel()), true);
            player.playSound(net.minecraft.sounds.SoundEvents.FIRE_AMBIENT, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // 2. Morphin hinzufügen
        if (handStack.getItem() instanceof MorphineItem) {
            if (raffBE.isFull()) {
                player.displayClientMessage(Component.translatable("message.heroin_raffinerie.refinery_full"), true);
                return InteractionResult.FAIL;
            }

            if (raffBE.addMorphine(handStack)) {
                handStack.shrink(1);
                player.displayClientMessage(Component.translatable("message.heroin_raffinerie.morphine_added", raffBE.getInputCount(), raffBE.getCapacity()), true);
                player.playSound(net.minecraft.sounds.SoundEvents.BREWING_STAND_BREW, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // 3. Heroin entnehmen
        if (handStack.isEmpty() && player.isShiftKeyDown()) {
            if (raffBE.hasOutput()) {
                ItemStack heroin = raffBE.extractAllHeroin();
                if (!heroin.isEmpty()) {
                    player.getInventory().add(heroin);
                    player.displayClientMessage(Component.translatable("message.heroin_raffinerie.heroin_extracted", heroin.getCount()), true);
                    player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        // 4. Status anzeigen
        if (handStack.isEmpty() && !player.isShiftKeyDown()) {
            float progress = raffBE.getAverageProgress() * 100;
            player.displayClientMessage(Component.translatable("message.heroin_raffinerie.status",
                    raffBE.getFuelLevel(), raffBE.getMaxFuel(),
                    raffBE.getInputCount(), raffBE.getCapacity(),
                    raffBE.getOutputCount(),
                    String.format("%.1f", progress)), false);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private boolean isFuel(ItemStack stack) {
        return stack.is(Items.COAL) || stack.is(Items.CHARCOAL) ||
                stack.is(Items.COAL_BLOCK) || stack.is(Items.OAK_LOG) ||
                stack.is(Items.BIRCH_LOG) || stack.is(Items.SPRUCE_LOG) ||
                stack.is(Items.JUNGLE_LOG) || stack.is(Items.ACACIA_LOG) ||
                stack.is(Items.DARK_OAK_LOG) || stack.is(Items.MANGROVE_LOG) ||
                stack.is(Items.CHERRY_LOG) || stack.is(Items.OAK_PLANKS) ||
                stack.is(Items.BIRCH_PLANKS) || stack.is(Items.SPRUCE_PLANKS);
    }

    private int getFuelValue(ItemStack stack) {
        if (stack.is(Items.COAL_BLOCK)) return 800;
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) return 100;
        if (stack.getItem().toString().contains("log")) return 50;
        if (stack.getItem().toString().contains("planks")) return 25;
        return 25;
    }
}
