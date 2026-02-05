package de.rolandsw.schedulemc.lock;

import de.rolandsw.schedulemc.lock.items.DoorLockItem;
import de.rolandsw.schedulemc.lock.items.HackingToolItem;
import de.rolandsw.schedulemc.lock.items.KeyItem;
import de.rolandsw.schedulemc.lock.items.KeyRingItem;
import de.rolandsw.schedulemc.lock.items.LockPickItem;
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

        // Reines Zahlenschloss: Code-Eingabe noetig
        if (lockData.getType() == LockType.COMBINATION || lockData.getType() == LockType.DUAL) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7e\u2699 Diese Tuer hat ein Zahlenschloss. " +
                            "Benutze /lock code " + lockData.getLockId() + " <code>"));
        } else {
            player.sendSystemMessage(Component.literal(
                    "\u00A7c\u2716 Diese Tuer ist mit einem " + lockData.getType().getDisplayName() +
                            " gesperrt!"));
        }

        // Event abbrechen → Tuer bleibt zu
        event.setCanceled(true);
    }

    /**
     * Zeigt Lock-Info bei Shift+Rechtsklick.
     * Hilfreich fuer Missions-Setup und allgemeine Information.
     */
    private void showLockInfo(ServerPlayer player, LockData lockData, BlockPos pos) {
        player.sendSystemMessage(Component.literal("\u00A78\u2550\u2550\u2550\u2550\u2550 \u00A7e\uD83D\uDD12 Tuer-Info \u00A78\u2550\u2550\u2550\u2550\u2550"));
        player.sendSystemMessage(Component.literal("\u00A77Position: \u00A7f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));

        if (lockData == null) {
            player.sendSystemMessage(Component.literal("\u00A77Status: \u00A7aKein Schloss vorhanden"));
            player.sendSystemMessage(Component.literal("\u00A78\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"));
            return;
        }

        // Lock-ID prominent anzeigen
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("\u00A7e\u00A7l\u2192 Schloss-ID: \u00A7b\u00A7l" + lockData.getLockId()));
        player.sendSystemMessage(Component.literal("\u00A78  (Fuer Task-Bloecke & Schluessel)"));
        player.sendSystemMessage(Component.literal(""));

        player.sendSystemMessage(Component.literal("\u00A77Schloss-Typ: " + getLockTypeColor(lockData.getType()) + lockData.getType().getDisplayName()));
        player.sendSystemMessage(Component.literal("\u00A77Besitzer: \u00A7f" + lockData.getOwnerName()));

        // Zeige benoetigte Schluessel-Stufe
        if (lockData.getType().getRequiredBlankTier() >= 0) {
            String keyTierName = switch (lockData.getType().getRequiredBlankTier()) {
                case 0 -> "\u00A76Kupfer-Schluessel";
                case 1 -> "\u00A77Eisen-Schluessel";
                case 2 -> "\u00A75Netherite-Schluessel";
                default -> "\u00A7fUnbekannt";
            };
            player.sendSystemMessage(Component.literal("\u00A77Schluessel-Typ: " + keyTierName));
        }

        // Zeige ob Code benoetigt wird
        if (lockData.getType().hasCode()) {
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("\u00A7e\uD83D\uDD22 Code erforderlich:"));
            player.sendSystemMessage(Component.literal("\u00A77  /lock code \u00A7b" + lockData.getLockId() + " \u00A7e<4-stellig>"));
        }

        // Zeige Dietrich-Chance
        float pickChance = lockData.getType().getPickChance();
        if (pickChance > 0) {
            int pct = (int)(pickChance * 100);
            player.sendSystemMessage(Component.literal("\u00A77Dietrich-Chance: \u00A7a" + pct + "%"));
        } else if (!lockData.getType().hasCode()) {
            player.sendSystemMessage(Component.literal("\u00A77Dietrich-Chance: \u00A7c0% (unmoeglich)"));
        }

        // Zeige Alarm-Status
        if (lockData.getType().triggersAlarm()) {
            player.sendSystemMessage(Component.literal("\u00A7c\u26A0 Loest Alarm bei Einbruch aus!"));
        }

        player.sendSystemMessage(Component.literal("\u00A78\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"));
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
