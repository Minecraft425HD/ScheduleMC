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
import java.util.Random;

/**
 * Dietrich-Set: Versucht gesperrte Tueren zu knacken.
 *
 * Haltbarkeit: 15 Versuche.
 * Erfolgsrate abhaengig von Schloss-Typ.
 * Bei Fehlschlag an Hochsicherheits-Schloessern: Alarm + Fahndung.
 */
public class LockPickItem extends Item {

    private static final int MAX_DURABILITY = 15;
    private static final Random RANDOM = new Random();

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
            player.sendSystemMessage(Component.literal("\u00A77Diese Tuer ist nicht gesperrt."));
            return InteractionResult.PASS;
        }

        LockType type = lockData.getType();

        // Zahlenschloss: Dietrich funktioniert nicht
        if (type == LockType.COMBINATION) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cZahlenschloss! Dietrich nutzlos. Versuche den Code."));
            return InteractionResult.FAIL;
        }

        // Haltbarkeit reduzieren
        ItemStack stack = ctx.getItemInHand();
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));

        // Erfolg pruefen
        float chance = type.getPickChance();
        boolean success = RANDOM.nextFloat() < chance;

        if (success) {
            // Tuer oeffnen
            DoorBlock door = (DoorBlock) level.getBlockState(pos).getBlock();
            door.setOpen(null, level, level.getBlockState(pos), pos, !level.getBlockState(pos).getValue(DoorBlock.OPEN));

            player.sendSystemMessage(Component.literal(
                    "\u00A7a\u2714 Schloss geknackt! (" + (int)(chance * 100) + "% Chance)"));
        } else {
            player.sendSystemMessage(Component.literal(
                    "\u00A7c\u2716 Fehlgeschlagen! (" + (int)(chance * 100) + "% Chance)"));

            // Alarm bei Hochsicherheit
            if (type.triggersAlarm()) {
                player.sendSystemMessage(Component.literal(
                        "\u00A74\u26A0 ALARM AUSGELOEST! Fahndungslevel erhoecht!"));
                triggerAlarm(player);
            } else if (type == LockType.SECURITY) {
                player.sendSystemMessage(Component.literal(
                        "\u00A7e\u26A0 Warnung: Verdaechtige Aktivitaet registriert."));
            }
        }

        return InteractionResult.SUCCESS;
    }

    private void triggerAlarm(ServerPlayer player) {
        // CrimeManager-Integration: Fahndungslevel erhoehen
        try {
            Class<?> crimeClass = Class.forName("de.rolandsw.schedulemc.npc.crime.CrimeManager");
            var method = crimeClass.getMethod("increaseWantedLevel", java.util.UUID.class, int.class);
            method.invoke(null, player.getUUID(), 1);
        } catch (Exception ignored) {
            // CrimeManager nicht verfuegbar — kein Problem
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tips, TooltipFlag flag) {
        tips.add(Component.literal("\u00A76Dietrich-Set").withStyle(ChatFormatting.GOLD));
        tips.add(Component.literal("\u00A77Rechtsklick auf gesperrte Tuer"));
        tips.add(Component.literal(""));
        tips.add(Component.literal("\u00A78Erfolgschancen:"));
        tips.add(Component.literal("\u00A7a  Einfach: 80%"));
        tips.add(Component.literal("\u00A7e  Sicher: 40% + Warnung"));
        tips.add(Component.literal("\u00A7c  Hochsicher: 10% + Alarm"));
        tips.add(Component.literal("\u00A75  Dual-Lock: 5% + Alarm"));
        tips.add(Component.literal("\u00A78  Zahlenschloss: Nicht moeglich"));
    }
}
