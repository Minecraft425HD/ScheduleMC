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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Dietrich-Set: Versucht gesperrte Tueren zu knacken.
 *
 * Haltbarkeit: 15 Versuche.
 * Erfolgsrate abhaengig von Schloss-Typ.
 * Bei Fehlschlag an Hochsicherheits-Schloessern: Alarm + Fahndung.
 */
public class LockPickItem extends Item {

    private static final int MAX_DURABILITY = 15;

    public LockPickItem() {
        super(new Properties().stacksTo(1).durability(MAX_DURABILITY));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        if (level.isClientSide()) return InteractionResult.PASS;
        if (!(level.getBlockState(pos).getBlock() instanceof DoorBlock)) return InteractionResult.PASS;
        if (!(ctx.getPlayer() instanceof ServerPlayer player)) return InteractionResult.PASS;

        if (level.getBlockState(pos).getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
        }

        LockManager mgr = LockManager.getInstance();
        if (mgr == null) return InteractionResult.FAIL;

        String dim = level.dimension().location().toString();
        String posKey = LockManager.posKey(dim, pos.getX(), pos.getY(), pos.getZ());
        LockData lockData = mgr.getLock(posKey);

        if (lockData == null) {
            player.sendSystemMessage(Component.translatable("lock.pick.not_locked"));
            return InteractionResult.PASS;
        }

        LockType type = lockData.getType();

        // Zahlenschloss: Dietrich funktioniert nicht
        if (type == LockType.COMBINATION) {
            player.sendSystemMessage(Component.translatable("lock.pick.combination_useless"));
            return InteractionResult.FAIL;
        }

        // Haltbarkeit reduzieren
        ItemStack stack = ctx.getItemInHand();
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));

        // Erfolg pruefen
        float chance = type.getPickChance();
        boolean success = ThreadLocalRandom.current().nextFloat() < chance;

        if (success) {
            // Tuer oeffnen
            DoorBlock door = (DoorBlock) level.getBlockState(pos).getBlock();
            door.setOpen(null, level, level.getBlockState(pos), pos, !level.getBlockState(pos).getValue(DoorBlock.OPEN));

            player.sendSystemMessage(Component.translatable("lock.pick.success", (int)(chance * 100)));
        } else {
            player.sendSystemMessage(Component.translatable("lock.pick.failed", (int)(chance * 100)));

            // Alarm bei Hochsicherheit
            if (type.triggersAlarm()) {
                player.sendSystemMessage(Component.translatable("lock.pick.alarm"));
                triggerAlarm(player);
            } else if (type == LockType.SECURITY) {
                player.sendSystemMessage(Component.translatable("lock.pick.warning"));
            }
        }

        return InteractionResult.SUCCESS;
    }

    private void triggerAlarm(ServerPlayer player) {
        // CrimeManager-Integration: Fahndungslevel erhoehen
        try {
            long currentDay = player.level().getDayTime() / 24000;
            de.rolandsw.schedulemc.npc.crime.CrimeManager.addWantedLevel(player.getUUID(), 1, currentDay);
        } catch (Exception ignored) {
            // CrimeManager nicht verfuegbar — kein Problem
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tips, TooltipFlag flag) {
        tips.add(Component.translatable("lock.pick.tooltip.title").withStyle(ChatFormatting.GOLD));
        tips.add(Component.translatable("lock.pick.tooltip.rightclick"));
        tips.add(Component.literal(""));
        tips.add(Component.translatable("lock.pick.tooltip.chances"));
        tips.add(Component.translatable("lock.pick.tooltip.chance.simple"));
        tips.add(Component.translatable("lock.pick.tooltip.chance.security"));
        tips.add(Component.translatable("lock.pick.tooltip.chance.high_security"));
        tips.add(Component.translatable("lock.pick.tooltip.chance.dual"));
        tips.add(Component.translatable("lock.pick.tooltip.chance.combination"));
    }
}
