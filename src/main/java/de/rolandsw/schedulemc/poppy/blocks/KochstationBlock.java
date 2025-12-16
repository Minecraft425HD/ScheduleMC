package de.rolandsw.schedulemc.poppy.blocks;

import de.rolandsw.schedulemc.poppy.blockentity.KochstationBlockEntity;
import de.rolandsw.schedulemc.poppy.items.RawOpiumItem;
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
 * Kochstation - kocht Rohopium zu Morphin
 * Kann auch für andere Prozesse verwendet werden
 */
public class KochstationBlock extends Block implements EntityBlock {

    public KochstationBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KochstationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof KochstationBlockEntity kochBE) {
                kochBE.tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof KochstationBlockEntity kochBE)) return InteractionResult.PASS;

        ItemStack handStack = player.getItemInHand(hand);

        // 1. Wasser hinzufügen (mit Wassereimer)
        if (handStack.is(Items.WATER_BUCKET)) {
            if (kochBE.getWaterLevel() >= kochBE.getMaxWater()) {
                player.displayClientMessage(Component.literal("§c✗ Wasser-Tank ist voll!"), true);
                return InteractionResult.FAIL;
            }

            kochBE.addWater(250); // 250 mB pro Eimer
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }
            player.displayClientMessage(Component.literal(
                    "§b✓ Wasser hinzugefügt!\n" +
                            "§7Tank: §e" + kochBE.getWaterLevel() + "/" + kochBE.getMaxWater() + " mB"
            ), true);
            player.playSound(net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // 2. Brennstoff hinzufügen
        if (isFuel(handStack)) {
            int fuelValue = getFuelValue(handStack);
            if (kochBE.getFuelLevel() >= kochBE.getMaxFuel()) {
                player.displayClientMessage(Component.literal("§c✗ Brennstoff-Tank ist voll!"), true);
                return InteractionResult.FAIL;
            }

            kochBE.addFuel(fuelValue);
            handStack.shrink(1);
            player.displayClientMessage(Component.literal(
                    "§c✓ Brennstoff hinzugefügt!\n" +
                            "§7Tank: §e" + kochBE.getFuelLevel() + "/" + kochBE.getMaxFuel()
            ), true);
            player.playSound(net.minecraft.sounds.SoundEvents.FIRE_AMBIENT, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }

        // 3. Rohopium hinzufügen
        if (handStack.getItem() instanceof RawOpiumItem) {
            if (kochBE.isFull()) {
                player.displayClientMessage(Component.literal("§c✗ Kochstation ist voll!"), true);
                return InteractionResult.FAIL;
            }

            if (kochBE.addOpium(handStack)) {
                handStack.shrink(1);
                player.displayClientMessage(Component.literal(
                        "§6✓ Rohopium hinzugefügt!\n" +
                                "§7Inhalt: §e" + kochBE.getInputCount() + "/" + kochBE.getCapacity()
                ), true);
                player.playSound(net.minecraft.sounds.SoundEvents.SLIME_BLOCK_PLACE, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }

        // 4. Morphin entnehmen
        if (handStack.isEmpty() && player.isShiftKeyDown()) {
            if (kochBE.hasOutput()) {
                ItemStack morphine = kochBE.extractAllMorphine();
                if (!morphine.isEmpty()) {
                    player.getInventory().add(morphine);
                    player.displayClientMessage(Component.literal(
                            "§d✓ Morphin entnommen!\n" +
                                    "§7Menge: §e" + morphine.getCount()
                    ), true);
                    player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        // 5. Status anzeigen
        if (handStack.isEmpty() && !player.isShiftKeyDown()) {
            float progress = kochBE.getAverageProgress() * 100;
            player.displayClientMessage(Component.literal(
                    "§d═══ Kochstation ═══\n" +
                            "§7Wasser: §b" + kochBE.getWaterLevel() + "/" + kochBE.getMaxWater() + " mB\n" +
                            "§7Brennstoff: §e" + kochBE.getFuelLevel() + "/" + kochBE.getMaxFuel() + "\n" +
                            "§7Rohopium: §e" + kochBE.getInputCount() + "/" + kochBE.getCapacity() + "\n" +
                            "§7Morphin fertig: §e" + kochBE.getOutputCount() + "\n" +
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
        if (stack.is(Items.COAL_BLOCK)) return 400;
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) return 50;
        if (stack.getItem().toString().contains("log")) return 25;
        if (stack.getItem().toString().contains("planks")) return 12;
        return 12;
    }
}
