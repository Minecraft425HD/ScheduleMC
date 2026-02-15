package de.rolandsw.schedulemc.lock;

import de.rolandsw.schedulemc.lock.items.DoorLockItem;
import de.rolandsw.schedulemc.lock.items.HackingToolItem;
import de.rolandsw.schedulemc.lock.items.KeyItem;
import de.rolandsw.schedulemc.lock.items.KeyRingItem;
import de.rolandsw.schedulemc.lock.items.LockPickItem;
import de.rolandsw.schedulemc.lock.network.LockNetworkHandler;
import de.rolandsw.schedulemc.lock.network.OpenCodeEntryPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Verhindert das Oeffnen gesperrter Tueren ohne passenden Schluessel.
 *
 * Wird NACH den Item-useOn Handlern ausgefuehrt (Priority LOW),
 * sodass Schloss/Schluessel/Dietrich Items zuerst verarbeitet werden.
 *
 * Wenn ein Spieler eine gesperrte Tuer OHNE spezielles Item anklickt,
 * wird das Event gecancelt und die Tuer bleibt zu.
 *
 * SHIFT+Rechtsklick zeigt Lock-Info (Lock-ID, Typ, Position).
 */
public class DoorLockHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onDoorInteract(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();
        if (!(level.getBlockState(pos).getBlock() instanceof DoorBlock)) return;

        // Untere Haelfte normalisieren
        if (level.getBlockState(pos).getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        LockManager mgr = LockManager.getInstance();
        if (mgr == null) return;

        String dim = level.dimension().location().toString();
        String posKey = LockManager.posKey(dim, pos.getX(), pos.getY(), pos.getZ());
        LockData lockData = mgr.getLock(posKey);

        // Gehaltenes Item pruefen
        ItemStack heldItem = player.getMainHandItem();

        // ═══════════════════════════════════════════════════════════
        // DoorLockItem auf ungesperrte Tuer: Schloss platzieren lassen
        // ═══════════════════════════════════════════════════════════
        if (heldItem.getItem() instanceof DoorLockItem && lockData == null) {
            // Event durchlassen → DoorLockItem.useOn() platziert das Schloss
            return;
        }

        // ═══════════════════════════════════════════════════════════
        // SHIFT+Rechtsklick: Lock-Info anzeigen
        // ═══════════════════════════════════════════════════════════
        if (player.isShiftKeyDown()) {
            showLockInfo(player, lockData, pos);
            event.setCanceled(true);
            return;
        }

        if (lockData == null) return; // Nicht gesperrt → normal weiter

        // Spezial-Items ueberspringen (haben bereits in useOn() reagiert)
        if (heldItem.getItem() instanceof DoorLockItem) return;
        if (heldItem.getItem() instanceof KeyItem) return;
        if (heldItem.getItem() instanceof KeyRingItem) return;
        if (heldItem.getItem() instanceof LockPickItem) return;
        if (heldItem.getItem() instanceof HackingToolItem) return;

        // Besitzer darf immer oeffnen
        if (lockData.isAuthorized(player.getUUID())) return;

        // Zahlenschloss: Code-Eingabe GUI oeffnen
        if (lockData.getType() == LockType.COMBINATION) {
            // Reines Zahlenschloss: Nur Code noetig
            LockNetworkHandler.sendToPlayer(
                    new OpenCodeEntryPacket(lockData.getLockId(), pos, dim), player);
        } else if (lockData.getType() == LockType.DUAL) {
            // Dual-Lock: Schluessel + Code noetig (Schluessel oeffnet die GUI via KeyItem)
            player.sendSystemMessage(Component.translatable("lock.dual.needs_key_and_code"));
            player.sendSystemMessage(Component.translatable("lock.dual.use_key"));
        } else {
            player.sendSystemMessage(Component.translatable("lock.locked_with",
                    lockData.getType().getDisplayName()));
        }

        // Event abbrechen → Tuer bleibt zu
        event.setCanceled(true);
    }

    /**
     * Zeigt Lock-Info bei Shift+Rechtsklick.
     * Hilfreich fuer Missions-Setup und allgemeine Information.
     */
    private void showLockInfo(ServerPlayer player, LockData lockData, BlockPos pos) {
        player.sendSystemMessage(Component.translatable("lock.info.header"));
        player.sendSystemMessage(Component.translatable("lock.info.position",
                pos.getX(), pos.getY(), pos.getZ()));

        if (lockData == null) {
            player.sendSystemMessage(Component.translatable("lock.info.not_locked"));
            player.sendSystemMessage(Component.translatable("lock.info.footer"));
            return;
        }

        // Lock-ID prominent anzeigen
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.translatable("lock.info.lock_id", lockData.getLockId()));
        player.sendSystemMessage(Component.translatable("lock.info.lock_id_hint"));
        player.sendSystemMessage(Component.literal(""));

        player.sendSystemMessage(Component.translatable("lock.info.lock_type",
                getLockTypeColor(lockData.getType()), lockData.getType().getDisplayName()));
        if (lockData.hasNoOwner()) {
            player.sendSystemMessage(Component.translatable("lock.info.no_owner"));
        } else {
            player.sendSystemMessage(Component.translatable("lock.info.owner", lockData.getOwnerName()));
        }

        // Zeige benoetigte Schluessel-Stufe
        if (lockData.getType().getRequiredBlankTier() >= 0) {
            int tier = lockData.getType().getRequiredBlankTier();
            String keyTierKey = "lock.info.key_tier." + tier;
            player.sendSystemMessage(Component.translatable("lock.info.key_tier_label",
                    Component.translatable(keyTierKey).getString()));
        }

        // Zeige Code-Info (Besitzer/OPs sehen den Code, andere nur den Hinweis)
        if (lockData.getType().hasCode()) {
            player.sendSystemMessage(Component.literal(""));
            boolean canSeeCode = lockData.isAuthorized(player.getUUID()) || player.hasPermissions(2);
            if (canSeeCode) {
                // Besitzer/Autorisierte/OPs sehen den Code
                player.sendSystemMessage(Component.translatable("lock.info.code_current", lockData.getCode()));
            } else {
                // Andere sehen nur den Hinweis
                player.sendSystemMessage(Component.translatable("lock.info.code_required"));
                player.sendSystemMessage(Component.translatable("lock.info.code_command", lockData.getLockId()));
            }
        }

        // Zeige Dietrich-Chance
        float pickChance = lockData.getType().getPickChance();
        if (pickChance > 0) {
            int pct = (int)(pickChance * 100);
            player.sendSystemMessage(Component.translatable("lock.info.lockpick_chance", pct));
        } else if (!lockData.getType().hasCode()) {
            player.sendSystemMessage(Component.translatable("lock.info.lockpick_impossible"));
        }

        // Zeige Alarm-Status
        if (lockData.getType().triggersAlarm()) {
            player.sendSystemMessage(Component.translatable("lock.info.alarm"));
        }

        player.sendSystemMessage(Component.translatable("lock.info.footer"));
    }

    private String getLockTypeColor(LockType type) {
        return switch (type) {
            case SIMPLE -> "\u00A7a";
            case SECURITY -> "\u00A79";
            case HIGH_SECURITY -> "\u00A7c";
            case COMBINATION -> "\u00A76";
            case DUAL -> "\u00A7d";
        };
    }
}
