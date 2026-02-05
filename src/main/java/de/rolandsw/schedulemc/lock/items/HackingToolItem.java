package de.rolandsw.schedulemc.lock.items;

import de.rolandsw.schedulemc.lock.LockData;
import de.rolandsw.schedulemc.lock.LockManager;
import de.rolandsw.schedulemc.lock.LockType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Abstrakte Basisklasse fuer Hacking-Tools.
 *
 * 3 Varianten:
 * - Code-Cracker:  Nur COMBINATION, 10 Versuche
 * - Bypass-Modul:  Nur DUAL, 5 Versuche
 * - Omni-Hack:     Beide Typen, 3 Versuche
 *
 * Mechanik: 50% Erfolgschance, sofort auf Server entschieden.
 * Kein Minigame, kein Netzwerk-Packet noetig.
 */
public abstract class HackingToolItem extends Item {

    private static final float SUCCESS_CHANCE = 0.5f;

    protected HackingToolItem(int maxDurability, Rarity rarity) {
        super(new Properties().stacksTo(1).durability(maxDurability).rarity(rarity));
    }

    /** Welche Lock-Typen kann dieses Tool hacken? */
    protected abstract boolean canHackLockType(LockType type);

    /** Name des Tools fuer Tooltip. */
    protected abstract String getToolName();

    /** Farbcode des Tool-Namens (z.B. "\u00A7a"). */
    protected abstract String getToolColor();

    /** Tool-spezifische Tooltip-Zeilen. */
    protected abstract void appendSpecificTooltip(List<Component> tips);

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

        // Nur Code-basierte Schloesser
        if (!type.hasCode()) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cDieses Schloss hat keinen Code! Verwende einen Dietrich."));
            return InteractionResult.FAIL;
        }

        // Pruefen ob dieses Tool den Lock-Typ hacken kann
        if (!canHackLockType(type)) {
            String msg = switch (type) {
                case COMBINATION -> "\u00A7cDieses Tool ist fuer Dual-Locks konzipiert! Verwende einen Code-Cracker oder Omni-Hack.";
                case DUAL -> "\u00A7cDieses Tool ist zu schwach fuer ein Dual-Lock! Verwende ein Bypass-Modul oder Omni-Hack.";
                default -> "\u00A7cDieses Tool funktioniert nicht an diesem Schloss!";
            };
            player.sendSystemMessage(Component.literal(msg));
            return InteractionResult.FAIL;
        }

        // Haltbarkeit reduzieren
        ItemStack stack = ctx.getItemInHand();
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));

        // 50% Erfolgschance - Server entscheidet sofort
        boolean success = ThreadLocalRandom.current().nextFloat() < SUCCESS_CHANCE;

        if (success) {
            // Tuer oeffnen
            player.sendSystemMessage(Component.literal("\u00A7a\u2714 Code geknackt! Tuer entriegelt."));
            var state = level.getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock door) {
                door.setOpen(null, level, state, pos, !state.getValue(DoorBlock.OPEN));
            }
        } else {
            // Fehlschlag
            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            player.sendSystemMessage(Component.literal(
                    "\u00A7c\u2716 Hacking fehlgeschlagen!" +
                    (remaining > 0 ? " \u00A77(noch " + remaining + " Versuche uebrig)" : "")));

            // Alarm bei Dual-Lock
            if (type.triggersAlarm()) {
                player.sendSystemMessage(Component.literal(
                        "\u00A74\u26A0 ALARM AUSGELOEST! Fahndungslevel erhoecht!"));
                triggerAlarm(player);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private void triggerAlarm(ServerPlayer player) {
        try {
            long currentDay = player.level().getDayTime() / 24000;
            de.rolandsw.schedulemc.npc.crime.CrimeManager.addWantedLevel(player.getUUID(), 2, currentDay);

            if (player.level() instanceof ServerLevel serverLevel) {
                var witnessManager = de.rolandsw.schedulemc.npc.life.witness.WitnessManager.getManager(serverLevel);
                witnessManager.registerCrime(
                        player,
                        de.rolandsw.schedulemc.npc.life.witness.CrimeType.PETTY_THEFT,
                        player.blockPosition(),
                        serverLevel,
                        null
                );
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tips, TooltipFlag flag) {
        tips.add(Component.literal(getToolColor() + getToolName()));
        tips.add(Component.literal("\u00A77Rechtsklick auf Code-Schloss"));
        tips.add(Component.literal("\u00A77Erfolgschance: \u00A7a50%"));
        tips.add(Component.literal(""));
        appendSpecificTooltip(tips);
    }
}
