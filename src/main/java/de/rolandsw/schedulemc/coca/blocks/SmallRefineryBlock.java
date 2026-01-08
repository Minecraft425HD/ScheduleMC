package de.rolandsw.schedulemc.coca.blocks;

import de.rolandsw.schedulemc.coca.blockentity.SmallRefineryBlockEntity;
import de.rolandsw.schedulemc.coca.items.CocaPasteItem;
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
 * Kleine Raffinerie - verarbeitet Koka-Paste zu Kokain (weiß)
 */
public class SmallRefineryBlock extends Block implements EntityBlock {

    public SmallRefineryBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmallRefineryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof SmallRefineryBlockEntity refineryBE) {
                refineryBE.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SmallRefineryBlockEntity refineryBE)) return InteractionResult.PASS;

        ItemStack handStack = player.getItemInHand(hand);

        // 1. Brennstoff hinzufügen (Kohle, Holz, etc.)
        if (isFuel(handStack)) {
            int fuelValue = getFuelValue(handStack);
            if (refineryBE.getFuelLevel() >= refineryBE.getMaxFuel()) {
                player.displayClientMessage(Component.literal("§c✗ Brennstoff-Tank ist voll!"), true);
                return InteractionResult.FAIL;
            }

            refineryBE.addFuel(fuelValue);
            handStack.shrink(1);
            player.displayClientMessage(Component.literal(
                    "§c✓ Brennstoff hinzugefügt!\n" +
                            "§7Tank: §e" + refineryBE.getFuelLevel() + "/" + refineryBE.getMaxFuel()
            ), true);
            player.playSound(net.minecraft.sounds.SoundEvents.FIRE_AMBIENT, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // 2. Koka-Paste hinzufügen
        if (handStack.getItem() instanceof CocaPasteItem) {
            if (refineryBE.isFull()) {
                player.displayClientMessage(Component.literal("§c✗ Raffinerie ist voll!"), true);
                return InteractionResult.FAIL;
            }

            if (refineryBE.addPaste(handStack)) {
                handStack.shrink(1);
                player.displayClientMessage(Component.literal(
                        "§6✓ Koka-Paste hinzugefügt!\n" +
                                "§7Inhalt: §e" + refineryBE.getInputCount() + "/" + refineryBE.getCapacity()
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.SLIME_BLOCK_PLACE, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // 3. Kokain entnehmen (Shift + Rechtsklick mit leerer Hand)
        if (handStack.isEmpty() && player.isShiftKeyDown()) {
            if (refineryBE.hasOutput()) {
                ItemStack cocaine = refineryBE.extractAllCocaine();
                if (!cocaine.isEmpty()) {
                    player.getInventory().add(cocaine);
                    player.displayClientMessage(Component.literal(
                            "§f✓ Kokain entnommen!\n" +
                                    "§7Menge: §e" + cocaine.getCount() + "g"
                    ), true);
                    player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        // 4. Status anzeigen (Rechtsklick mit leerer Hand)
        if (handStack.isEmpty() && !player.isShiftKeyDown()) {
            float progress = refineryBE.getAverageRefineryPercentage() * 100;
            player.displayClientMessage(Component.literal(
                    "§c═══ Kleine Raffinerie ═══\n" +
                            "§7Brennstoff: §e" + refineryBE.getFuelLevel() + "/" + refineryBE.getMaxFuel() + "\n" +
                            "§7Paste: §e" + refineryBE.getInputCount() + "/" + refineryBE.getCapacity() + "\n" +
                            "§7Kokain fertig: §e" + refineryBE.getOutputCount() + "\n" +
                            "§7Fortschritt: §e" + String.format("%.1f", progress) + "%"
            ), false);
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
        return 25; // Default
    }
}
