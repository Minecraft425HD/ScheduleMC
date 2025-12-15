package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.tobacco.blockentity.SmallDryingRackBlockEntity;
import de.rolandsw.schedulemc.tobacco.items.FreshTobaccoLeafItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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

        ItemStack heldItem = player.getItemInHand(hand);

        // ═══════════════════════════════════════════════════════════
        // 1. FRISCHE BLÄTTER HINZUFÜGEN
        // ═══════════════════════════════════════════════════════════
        if (heldItem.getItem() instanceof FreshTobaccoLeafItem) {
            if (rackBE.isFull()) {
                player.displayClientMessage(Component.literal(
                    "§c✗ Trocknungsgestell ist voll! (6/6)"
                ), true);
                return InteractionResult.FAIL;
            }

            if (rackBE.addFreshLeaves(heldItem)) {
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }

                int count = rackBE.getInputCount();
                player.displayClientMessage(Component.literal(
                    "§a✓ Blatt hinzugefügt! (" + count + "/6)\n" +
                    "§7Dauer: §e~5 Minuten"
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        // ═══════════════════════════════════════════════════════════
        // 2. GETROCKNETE BLÄTTER ENTNEHMEN
        // ═══════════════════════════════════════════════════════════
        if (player.isShiftKeyDown() && rackBE.hasOutput()) {
            ItemStack dried = rackBE.extractAllDriedLeaves();
            if (!dried.isEmpty()) {
                player.getInventory().add(dried);

                player.displayClientMessage(Component.literal(
                    "§a✓ Trocknung abgeschlossen!\n" +
                    "§7Erhalten: §e" + dried.getCount() + "x §7getrocknete Blätter"
                ), true);
                return InteractionResult.SUCCESS;
            }
        }

        // ═══════════════════════════════════════════════════════════
        // 3. FORTSCHRITT ANZEIGEN
        // ═══════════════════════════════════════════════════════════
        if (rackBE.hasInput()) {
            float progress = rackBE.getAverageDryingPercentage() * 100;
            String bar = createProgressBar(rackBE.getAverageDryingPercentage());
            int inputCount = rackBE.getInputCount();
            int outputCount = rackBE.getOutputCount();

            player.displayClientMessage(Component.literal(
                "§6═══ Kleines Trocknungsgestell ═══\n" +
                "§7Kapazität: §e" + inputCount + "/6\n" +
                "§7Fortschritt: " + bar + " §e" + String.format("%.1f", progress) + "%\n" +
                "§7Fertig: §e" + outputCount + "x\n" +
                (rackBE.hasOutput() ? "§a✓ Shift+Rechtsklick zum Entnehmen" : "§7Trocknung läuft...")
            ), false);
        } else {
            player.displayClientMessage(Component.literal(
                "§6═══ Kleines Trocknungsgestell ═══\n" +
                "§7Kapazität: §e6 Blätter\n" +
                "§7Leer - Lege frische Tabakblätter hinein"
            ), false);
        }

        return InteractionResult.SUCCESS;
    }

    private String createProgressBar(float progress) {
        int filled = (int) (progress * 10);
        int empty = 10 - filled;
        return "§a" + "▰".repeat(Math.max(0, filled)) + "§7" + "▱".repeat(Math.max(0, empty));
    }
}
