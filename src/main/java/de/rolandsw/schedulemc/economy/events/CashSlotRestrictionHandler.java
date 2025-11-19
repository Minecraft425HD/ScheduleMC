package de.rolandsw.schedulemc.economy.events;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.economy.items.CashItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Geldbörsen-System:
 * - NUR in Slot 8 (Slot 9 im UI)
 * - Automatisch beim Beitritt erstellen
 * - NICHT entfernbar
 * - NICHT droppbar (auch bei Tod!) - ANTI-CHEAT
 * - Unlimited Speicher
 */
public class CashSlotRestrictionHandler {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CASH_SLOT = 8; // Slot 8 = Hotbar Slot 9 (ganz rechts)
    
    /**
     * Erstellt Geldbörse beim Beitritt wenn nicht vorhanden
     */
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        
        // Prüfe ob Geldbörse in Slot 8 existiert
        ItemStack slot8 = player.getInventory().getItem(CASH_SLOT);
        
        if (!(slot8.getItem() instanceof CashItem)) {
            // Erstelle neue Geldbörse mit 0€
            ItemStack wallet = CashItem.create(0.0);
            player.getInventory().setItem(CASH_SLOT, wallet);
            
            player.displayClientMessage(Component.literal(
                "§a✓ Geldbörse erhalten!\n" +
                "§7Die Geldbörse ist in Slot 9 gesperrt.\n" +
                "§7Sie kann nicht entfernt werden."
            ), false);
        }
    }
    
    /**
     * Verhindert Verwendung von Bargeld außerhalb von Slot 8
     */
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();
        
        // Prüfe ob Bargeld gehalten wird
        if (heldItem.getItem() instanceof CashItem) {
            // Prüfe ob es in Slot 8 ist
            int slot = player.getInventory().selected; // Aktuell ausgewählter Hotbar-Slot
            
            if (slot != CASH_SLOT) {
                event.setCanceled(true);
                player.displayClientMessage(Component.literal(
                    "§c✗ Geldbörse kann nur in Slot 9 verwendet werden!"
                ), true);
            }
        }
    }
    
    /**
     * Verhindert dass Geldbörse aus Slot 8 entfernt wird
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        
        Player player = event.player;
        ItemStack slot8 = player.getInventory().getItem(CASH_SLOT);
        
        // Prüfe ob Slot 8 leer ist oder kein Cash-Item
        if (slot8.isEmpty() || !(slot8.getItem() instanceof CashItem)) {
            // Suche Geldbörse im Inventar
            ItemStack foundWallet = null;
            int foundSlot = -1;
            
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (i == CASH_SLOT) continue; // Skip Slot 8
                
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() instanceof CashItem) {
                    foundWallet = stack;
                    foundSlot = i;
                    break;
                }
            }
            
            if (foundWallet != null) {
                // Verschiebe Geldbörse zurück zu Slot 8
                player.getInventory().setItem(CASH_SLOT, foundWallet.copy());
                player.getInventory().setItem(foundSlot, ItemStack.EMPTY);
                
                player.displayClientMessage(Component.literal(
                    "§e⚠ Geldbörse wurde zurück zu Slot 9 verschoben!"
                ), true);
            } else {
                // Erstelle neue Geldbörse wenn komplett verschwunden
                ItemStack newWallet = CashItem.create(0.0);
                player.getInventory().setItem(CASH_SLOT, newWallet);
                
                player.displayClientMessage(Component.literal(
                    "§c⚠ Geldbörse wurde neu erstellt!\n" +
                    "§7Guthaben auf 0€ zurückgesetzt."
                ), true);
            }
        }
    }
    
    /**
     * Verhindert dass Geldbörse aufgesammelt wird (außer in Slot 8)
     */
    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        ItemStack stack = event.getItem().getItem();
        
        if (stack.getItem() instanceof CashItem) {
            Player player = event.getEntity();
            ItemStack slot8 = player.getInventory().getItem(CASH_SLOT);
            
            if (slot8.getItem() instanceof CashItem) {
                // Merge mit existierender Geldbörse
                double value = CashItem.getValue(stack);
                CashItem.addValue(slot8, value);
                
                player.displayClientMessage(Component.literal(
                    "§a+ " + String.format("%.2f€", value) + " §7zur Geldbörse hinzugefügt"
                ), true);
                
                event.setCanceled(true);
                event.getItem().discard();
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ANTI-CHEAT: VERHINDERT DROP BEI TOD!
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * WICHTIG: Verhindert dass Geldbörse bei Tod gedroppt wird
     * ANTI-CHEAT gegen Geld-Duplizierung!
     */
    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        // Entferne ALLE Geldbörsen aus Drops
        List<ItemEntity> toRemove = new ArrayList<>();
        
        for (ItemEntity itemEntity : event.getDrops()) {
            ItemStack stack = itemEntity.getItem();
            if (stack.getItem() instanceof CashItem) {
                toRemove.add(itemEntity);
            }
        }
        
        // Entferne alle gefundenen Geldbörsen
        event.getDrops().removeAll(toRemove);
        
        // Log für Anti-Cheat Monitoring
        if (!toRemove.isEmpty()) {
            LOGGER.info("[ANTI-CHEAT] Prevented {} wallet drop(s) on death for player: {}", 
                toRemove.size(),
                event.getEntity().getName().getString());
        }
    }
}
