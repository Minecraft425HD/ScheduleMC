package de.rolandsw.schedulemc.lock.items;

import de.rolandsw.schedulemc.lock.LockData;
import de.rolandsw.schedulemc.lock.LockManager;
import de.rolandsw.schedulemc.lock.LockType;
import de.rolandsw.schedulemc.lock.network.HackingResultPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.List;

/**
 * Hacking-Tool: Knackt Zahlenschloesser und Dual-Locks ueber ein Minigame.
 *
 * Haltbarkeit: 5 Versuche.
 * Rechtsklick auf gesperrte Tuer mit Code-Schloss -> oeffnet Hacking-Screen.
 * Funktioniert NUR bei COMBINATION und DUAL (Schloesser mit Code).
 */
public class HackingToolItem extends Item {

    private static final int MAX_DURABILITY = 5;

    public HackingToolItem() {
        super(new Properties().stacksTo(1).durability(MAX_DURABILITY));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        if (level.isClientSide()) return InteractionResult.PASS;
        if (!(level.getBlockState(pos).getBlock() instanceof DoorBlock)) return InteractionResult.PASS;
        if (!(ctx.getPlayer() instanceof ServerPlayer player)) return InteractionResult.PASS;

        // Immer lower half
        if (level.getBlockState(pos).getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
        }

        LockManager mgr = LockManager.getInstance();
        if (mgr == null) return InteractionResult.FAIL;

        String dim = level.dimension().location().toString();
        String posKey = LockManager.posKey(dim, pos.getX(), pos.getY(), pos.getZ());
        LockData lockData = mgr.getLock(posKey);

        if (lockData == null) {
            player.sendSystemMessage(Component.literal("\u00A77Diese Tuer ist nicht gesperrt."));
            return InteractionResult.PASS;
        }

        LockType type = lockData.getType();

        // Nur Zahlenschloesser und Dual-Locks
        if (!type.hasCode()) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cDieses Schloss hat keinen Code! Verwende einen Dietrich."));
            return InteractionResult.FAIL;
        }

        // Haltbarkeit reduzieren
        ItemStack stack = ctx.getItemInHand();
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));

        // Oeffne Hacking-Screen auf Client
        // Sende Lock-Daten per Packet an Client
        de.rolandsw.schedulemc.lock.network.LockNetworkHandler.sendToPlayer(
                new de.rolandsw.schedulemc.lock.network.OpenHackingScreenPacket(
                        lockData.getLockId(),
                        posKey,
                        type.name(),
                        lockData.getCode().length()
                ),
                player
        );

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tips, TooltipFlag flag) {
        tips.add(Component.literal("\u00A7dHacking-Tool").withStyle(ChatFormatting.LIGHT_PURPLE));
        tips.add(Component.literal("\u00A77Rechtsklick auf Zahlenschloss"));
        tips.add(Component.literal(""));
        tips.add(Component.literal("\u00A78Knackt Code-basierte Schloesser:"));
        tips.add(Component.literal("\u00A76  Zahlenschloss: \u00A7aMittel"));
        tips.add(Component.literal("\u00A75  Dual-Lock: \u00A7cSchwer + Alarm"));
        tips.add(Component.literal(""));
        tips.add(Component.literal("\u00A78Dietrich-Schloesser: \u00A7cNicht moeglich"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
