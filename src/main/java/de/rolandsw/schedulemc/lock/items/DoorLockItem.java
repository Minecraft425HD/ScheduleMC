package de.rolandsw.schedulemc.lock.items;

import de.rolandsw.schedulemc.lock.LockData;
import de.rolandsw.schedulemc.lock.LockManager;
import de.rolandsw.schedulemc.lock.LockType;
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
 * Schloss-Item: Rechtsklick auf Tuer = Tuer wird gesperrt.
 * Verbraucht sich bei Nutzung. 1 Schloss = 1 Tuer.
 */
public class DoorLockItem extends Item {

    private final LockType lockType;

    public DoorLockItem(LockType lockType) {
        super(new Properties().stacksTo(16));
        this.lockType = lockType;
    }

    public LockType getLockType() { return lockType; }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        if (level.isClientSide()) return InteractionResult.PASS;
        if (!(level.getBlockState(pos).getBlock() instanceof DoorBlock)) return InteractionResult.PASS;
        if (!(ctx.getPlayer() instanceof ServerPlayer player)) return InteractionResult.PASS;

        // Immer untere Haelfte verwenden
        if (level.getBlockState(pos).getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
        }

        LockManager mgr = LockManager.getInstance();
        if (mgr == null) return InteractionResult.FAIL;

        String dim = level.dimension().location().toString();
        String posKey = LockManager.posKey(dim, pos.getX(), pos.getY(), pos.getZ());

        // Bereits gesperrt?
        if (mgr.isLocked(posKey)) {
            player.sendSystemMessage(Component.literal("\u00A7cDiese Tuer ist bereits gesperrt!"));
            return InteractionResult.FAIL;
        }

        // Schloss platzieren
        LockData data = mgr.placeLock(lockType, player.getUUID(), player.getGameProfile().getName(),
                pos.getX(), pos.getY(), pos.getZ(), dim);

        // Item verbrauchen
        ctx.getItemInHand().shrink(1);

        // Nachricht
        if (lockType.hasCode()) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7a\u2714 " + lockType.getDisplayName() + " angebracht! Code: \u00A7e\u00A7l" + data.getCode()));
            if (lockType.supportsKeys()) {
                player.sendSystemMessage(Component.literal(
                        "\u00A77Dieser Lock braucht Schluessel UND Code."));
            }
        } else {
            player.sendSystemMessage(Component.literal(
                    "\u00A7a\u2714 " + lockType.getDisplayName() + " angebracht!"));
            player.sendSystemMessage(Component.literal(
                    "\u00A77Benutze einen Schluessel-Rohling auf diese Tuer."));
        }
        player.sendSystemMessage(Component.literal(
                "\u00A78Lock-ID: " + data.getLockId()));

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tips, TooltipFlag flag) {
        tips.add(Component.literal(lockType.getDisplayName()).withStyle(ChatFormatting.GOLD));
        tips.add(Component.literal("\u00A77Rechtsklick auf Tuer zum Sperren"));

        if (lockType.supportsKeys()) {
            long dur = lockType.getKeyDurationMs();
            String durStr = dur >= 86400000 ? (dur / 86400000) + " Tage" : (dur / 3600000) + " Stunden";
            tips.add(Component.literal("\u00A78Schluessel-Dauer: " + durStr));
            tips.add(Component.literal("\u00A78Nutzungen: " + lockType.getKeyMaxUses() + "x"));
        }
        if (lockType.hasCode()) {
            tips.add(Component.literal("\u00A7e\u2699 Zahlenschloss (4-stellig)"));
        }
        int pct = (int)(lockType.getPickChance() * 100);
        tips.add(Component.literal("\u00A78Dietrich: " + pct + "% Chance"));
        if (lockType.triggersAlarm()) {
            tips.add(Component.literal("\u00A7c\u26A0 Alarm bei Einbruchversuch"));
        }
    }
}
