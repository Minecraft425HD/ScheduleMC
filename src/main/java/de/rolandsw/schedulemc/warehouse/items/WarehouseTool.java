package de.rolandsw.schedulemc.warehouse.items;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.warehouse.WarehouseBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Warehouse-Tool zum Verknüpfen von NPCs mit Warehouses
 *
 * Workflow:
 * 1. Rechtsklick auf Warehouse-Block → Warehouse auswählen
 * 2. Linksklick auf NPC → NPC mit Warehouse verknüpfen
 * 3. Shift+Rechtsklick auf Warehouse → Warehouse-Info anzeigen
 * 4. Shift+Linksklick auf NPC → NPC-Warehouse-Info anzeigen
 */
public class WarehouseTool extends Item {

    // Speichere ausgewähltes Warehouse pro Spieler (UUID -> BlockPos)
    private static final Map<UUID, BlockPos> selectedWarehouses = new HashMap<>();

    public WarehouseTool() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(clickedPos);

            if (!(blockEntity instanceof WarehouseBlockEntity warehouse)) {
                player.sendSystemMessage(
                    Component.literal("§cDas ist kein Warehouse-Block!")
                );
                return InteractionResult.FAIL;
            }

            // Shift+Rechtsklick = Info anzeigen
            if (player.isCrouching()) {
                showWarehouseInfo(player, warehouse, clickedPos);
                return InteractionResult.SUCCESS;
            }

            // Warehouse auswählen
            selectedWarehouses.put(player.getUUID(), clickedPos);

            player.sendSystemMessage(
                Component.literal("§a✓ Warehouse ausgewählt!")
                    .append(Component.literal("\n§7Position: §f" + clickedPos.toShortString()))
                    .append(Component.literal("\n§7Shop-ID: §e" +
                        (warehouse.getShopId() != null ? warehouse.getShopId() : "§cNicht gesetzt")))
            );

            player.sendSystemMessage(
                Component.literal("§7→ §eLinksklick auf NPC §7um ihn mit diesem Warehouse zu verknüpfen")
            );

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Wird vom Event-Handler aufgerufen bei Linksklick auf NPC
     */
    public static InteractionResult onLeftClickNPC(Player player, CustomNPCEntity npc, ItemStack stack) {
        if (player.level().isClientSide) {
            return InteractionResult.PASS;
        }

        // Shift+Linksklick = Info anzeigen
        if (player.isCrouching()) {
            showNPCWarehouseInfo(player, npc);
            return InteractionResult.SUCCESS;
        }

        // Prüfe ob Warehouse ausgewählt wurde
        BlockPos warehousePos = selectedWarehouses.get(player.getUUID());
        if (warehousePos == null) {
            player.sendSystemMessage(
                Component.literal("§cKein Warehouse ausgewählt!")
                    .append(Component.literal("\n§7→ Rechtsklick auf Warehouse-Block um es auszuwählen"))
            );
            return InteractionResult.FAIL;
        }

        // Prüfe ob Warehouse noch existiert
        BlockEntity be = player.level().getBlockEntity(warehousePos);
        if (!(be instanceof WarehouseBlockEntity warehouse)) {
            player.sendSystemMessage(
                Component.literal("§cDas ausgewählte Warehouse existiert nicht mehr!")
            );
            selectedWarehouses.remove(player.getUUID());
            return InteractionResult.FAIL;
        }

        // Verknüpfe NPC mit Warehouse (bidirektional)
        npc.getNpcData().setAssignedWarehouse(warehousePos);
        warehouse.addSeller(npc.getNpcData().getNpcUUID());

        player.sendSystemMessage(
            Component.literal("§a✓ NPC mit Warehouse verknüpft!")
                .append(Component.literal("\n§7NPC: §e" + npc.getNpcName()))
                .append(Component.literal("\n§7Warehouse: §f" + warehousePos.toShortString()))
                .append(Component.literal("\n§7Shop-ID: §e" +
                    (warehouse.getShopId() != null ? warehouse.getShopId() : "§cNicht gesetzt")))
                .append(Component.literal("\n§7Verknüpfte Verkäufer: §e" + warehouse.getLinkedSellers().size()))
        );

        // Warehouse-Auswahl beibehalten für weitere NPCs
        player.sendSystemMessage(
            Component.literal("§7→ Linksklick auf weitere NPCs um sie ebenfalls zu verknüpfen")
        );

        return InteractionResult.SUCCESS;
    }

    /**
     * Zeigt Warehouse-Informationen an
     */
    private void showWarehouseInfo(Player player, WarehouseBlockEntity warehouse, BlockPos pos) {
        player.sendSystemMessage(
            Component.literal("§6§l═══ Warehouse Info ═══")
        );

        player.sendSystemMessage(
            Component.literal("§bPosition: §f" + pos.toShortString())
        );

        player.sendSystemMessage(
            Component.literal("§bShop-ID: §e" +
                (warehouse.getShopId() != null ? warehouse.getShopId() : "§cNicht gesetzt"))
        );

        player.sendSystemMessage(
            Component.literal("§bSlots: §e" + warehouse.getUsedSlots() + " §7/ §e" + warehouse.getSlots().length)
        );

        player.sendSystemMessage(
            Component.literal("§bTotal Items: §e" + warehouse.getTotalItems())
        );

        player.sendSystemMessage(
            Component.literal("§bVerknüpfte Verkäufer: §e" + warehouse.getLinkedSellers().size())
        );

        long daysSinceDelivery = (player.level().getDayTime() - warehouse.getLastDeliveryTime()) / 24000;
        player.sendSystemMessage(
            Component.literal("§bLetzter Delivery: §e" + daysSinceDelivery + " Tage her")
        );
    }

    /**
     * Zeigt NPC-Warehouse-Informationen an
     */
    private static void showNPCWarehouseInfo(Player player, CustomNPCEntity npc) {
        player.sendSystemMessage(
            Component.literal("§6§l═══ NPC Warehouse Info ═══")
        );

        player.sendSystemMessage(
            Component.literal("§bNPC: §e" + npc.getNpcName())
        );

        BlockPos warehousePos = npc.getNpcData().getAssignedWarehouse();
        if (warehousePos == null) {
            player.sendSystemMessage(
                Component.literal("§bWarehouse: §cNicht zugewiesen")
            );
            player.sendSystemMessage(
                Component.literal("§7→ Rechtsklick auf Warehouse, dann Linksklick auf NPC")
            );
        } else {
            player.sendSystemMessage(
                Component.literal("§bWarehouse: §f" + warehousePos.toShortString())
            );

            // Prüfe ob Warehouse noch existiert
            BlockEntity be = player.level().getBlockEntity(warehousePos);
            if (be instanceof WarehouseBlockEntity warehouse) {
                player.sendSystemMessage(
                    Component.literal("§bShop-ID: §e" +
                        (warehouse.getShopId() != null ? warehouse.getShopId() : "§cNicht gesetzt"))
                );
                player.sendSystemMessage(
                    Component.literal("§bLagerbestand: §e" + warehouse.getTotalItems() + " Items")
                );
            } else {
                player.sendSystemMessage(
                    Component.literal("§c⚠ Warehouse existiert nicht mehr!")
                );
            }
        }
    }

    /**
     * Gibt die ausgewählte Warehouse-Position für einen Spieler zurück
     */
    public static BlockPos getSelectedWarehouse(UUID playerUUID) {
        return selectedWarehouses.get(playerUUID);
    }

    /**
     * Setzt das ausgewählte Warehouse für einen Spieler
     */
    public static void setSelectedWarehouse(UUID playerUUID, BlockPos pos) {
        selectedWarehouses.put(playerUUID, pos);
    }

    /**
     * Entfernt die Warehouse-Auswahl für einen Spieler
     */
    public static void clearSelectedWarehouse(UUID playerUUID) {
        selectedWarehouses.remove(playerUUID);
    }
}
