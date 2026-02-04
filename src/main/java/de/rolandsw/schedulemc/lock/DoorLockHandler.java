package de.rolandsw.schedulemc.lock;

import de.rolandsw.schedulemc.lock.items.DoorLockItem;
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

        LockManager mgr = LockManager.getInstance();
        if (mgr == null) return;

        String dim = level.dimension().location().toString();
        String posKey = LockManager.posKey(dim, pos.getX(), pos.getY(), pos.getZ());
        LockData lockData = mgr.getLock(posKey);

        if (lockData == null) return; // Nicht gesperrt → normal weiter

        // Spezial-Items ueberspringen (haben bereits in useOn() reagiert)
        ItemStack heldItem = event.getEntity().getMainHandItem();
        if (heldItem.getItem() instanceof DoorLockItem) return;
        if (heldItem.getItem() instanceof KeyItem) return;
        if (heldItem.getItem() instanceof KeyRingItem) return;
        if (heldItem.getItem() instanceof LockPickItem) return;

        // Besitzer darf immer oeffnen
        if (event.getEntity() instanceof ServerPlayer player) {
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
        }

        // Event abbrechen → Tuer bleibt zu
        event.setCanceled(true);
    }

    /**
     * Verhindert auch das Oeffnen per Redstone-Signal bei gesperrten Tueren?
     * Optional: Hier koennte man BlockEvent abfangen.
     * Fuer jetzt reicht es, Spieler-Interaktion zu blockieren.
     */
}
