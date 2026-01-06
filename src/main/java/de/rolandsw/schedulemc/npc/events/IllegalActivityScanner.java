package de.rolandsw.schedulemc.npc.events;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.items.CashItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Scannt Umgebung nach illegalen Aktivitäten
 *
 * Illegale Items:
 * - Tabakpflanzen (alle Arten)
 * - Bargeld über 10.000€ (auf Boden/Tischen)
 * - Tabak-Verarbeitungsprodukte (feucht, getrocknet, fermentiert, verpackt)
 * - Gold/Diamant-Blöcke
 * - Waffen (vorbereitet für später)
 */
public class IllegalActivityScanner {

    /**
     * Ergebnis eines Scans
     */
    public static class ScanResult {
        public int illegalPlantCount = 0;
        public double totalCashFound = 0.0;
        public int illegalItemCount = 0;
        public int illegalBlockCount = 0;
        public List<String> foundIllegalItems = new ArrayList<>();

        public int calculateSeverity() {
            int severity = 0;

            // Pro Pflanze: +1
            severity += illegalPlantCount;

            // Pro 10.000€: +1
            severity += (int)(totalCashFound / 10000.0);

            // Illegale Items/Blöcke: +1
            severity += Math.min(illegalItemCount + illegalBlockCount, 3);

            // Max 10
            return Math.min(severity, 10);
        }

        public int calculateWantedIncrease() {
            int increase = 0;

            // Pro Pflanze: +1 Stern
            increase += illegalPlantCount;

            // Pro 10.000€: +1 Stern
            increase += (int)(totalCashFound / 10000.0);

            // Max +3 Sterne
            return Math.min(increase, 3);
        }

        public boolean hasIllegalActivity() {
            return illegalPlantCount > 0 || totalCashFound > 10000.0 ||
                   illegalItemCount > 0 || illegalBlockCount > 0;
        }
    }

    /**
     * NEUE METHODE: Intelligenter Raum-basierter Scan
     * Scannt nur Räume, die die Polizei "gesehen" hat
     *
     * @param level Die Welt
     * @param arrestPos Position der Festnahme
     * @param player Der Spieler
     * @return ScanResult mit Fund-Informationen
     */
    public static ScanResult scanRoomBased(Level level, BlockPos arrestPos, ServerPlayer player) {
        ScanResult result = new ScanResult();

        // 1. Scanne den Raum, in dem die Festnahme stattfand
        RoomScanner.RoomScanResult initialRoom = RoomScanner.scanRoom(level, arrestPos);

        com.mojang.logging.LogUtils.getLogger().info(
            "[ROOM-SCAN] Scanne initialen Raum für Spieler {} - {} Blöcke gefunden",
            player.getName().getString(),
            initialRoom.size()
        );

        // Scanne alle Blöcke im initialen Raum
        for (BlockPos pos : initialRoom.roomBlocks) {
            scanBlock(level, pos, result);
        }

        // Scanne Spieler-Inventar
        scanPlayerInventory(player, result);

        // 2. Wenn illegale Aktivitäten gefunden wurden, scanne angrenzende Räume
        if (result.hasIllegalActivity()) {
            // Polizei darf weitere Räume durchsuchen, da Konterband gefunden wurde
            int maxAdditionalRooms = ModConfigHandler.COMMON.POLICE_ROOM_SCAN_MAX_ADDITIONAL_ROOMS.get();
            Set<BlockPos> connectedRooms = RoomScanner.scanConnectedRooms(level, initialRoom, maxAdditionalRooms);

            int additionalBlocks = connectedRooms.size() - initialRoom.size();

            com.mojang.logging.LogUtils.getLogger().info(
                "[ROOM-SCAN] Konterband gefunden! Erweitere Suche - {} zusätzliche Blöcke in angrenzenden Räumen",
                additionalBlocks
            );

            // Scanne alle zusätzlichen Räume
            for (BlockPos pos : connectedRooms) {
                // Überspringe Blöcke, die bereits im ersten Raum gescannt wurden
                if (!initialRoom.roomBlocks.contains(pos)) {
                    scanBlock(level, pos, result);
                }
            }

            // Füge Info hinzu, dass erweiterte Suche durchgeführt wurde
            result.foundIllegalItems.add("§e[Erweiterte Durchsuchung: " + additionalBlocks + " weitere Blöcke]");
        } else {
            com.mojang.logging.LogUtils.getLogger().info(
                "[ROOM-SCAN] Kein Konterband im initialen Raum gefunden - Durchsuchung beendet"
            );
        }

        return result;
    }

    /**
     * ALTE METHODE: Radius-basierter Scan (deprecated, aber für Rückwärtskompatibilität beibehalten)
     *
     * @deprecated Verwende stattdessen {@link #scanRoomBased(Level, BlockPos, ServerPlayer)}
     */
    @Deprecated
    public static ScanResult scanArea(Level level, BlockPos center, ServerPlayer player) {
        ScanResult result = new ScanResult();

        int configRadius = ModConfigHandler.COMMON.POLICE_RAID_SCAN_RADIUS.get();
        // Hard-Limit bei 15 Blöcken für Performance (15³ = 3375 Blöcke statt 20³ = 8000)
        int radius = Math.min(configRadius, 15);

        // Scanne alle Blöcke im Radius
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    scanBlock(level, pos, result);
                }
            }
        }

        // Scanne Spieler-Inventar
        scanPlayerInventory(player, result);

        return result;
    }

    /**
     * Scannt einen einzelnen Block
     */
    private static void scanBlock(Level level, BlockPos pos, ScanResult result) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        String blockName = BuiltInRegistries.BLOCK.getKey(block).toString();

        // Prüfe auf Tabakpflanzen
        if (blockName.contains("schedulemc:") && blockName.contains("_plant")) {
            result.illegalPlantCount++;
            result.foundIllegalItems.add("Tabakpflanze bei " + pos.toShortString());
        }

        // Prüfe auf illegale Blöcke (Gold, Diamant)
        if (blockName.equals("minecraft:gold_block")) {
            result.illegalBlockCount++;
            result.foundIllegalItems.add("Goldblock bei " + pos.toShortString());
        } else if (blockName.equals("minecraft:diamond_block")) {
            result.illegalBlockCount++;
            result.foundIllegalItems.add("Diamantblock bei " + pos.toShortString());
        }

        // FEATURE REQUEST: Scan for additional illegal blocks
        // 1. Cash storage blocks (when cash block system is implemented)
        // 2. Processing machines (drug production equipment)
        // These require corresponding block types to be implemented first
    }

    /**
     * Scannt Spieler-Inventar
     */
    private static void scanPlayerInventory(ServerPlayer player, ScanResult result) {
        // Scanne Inventar
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                scanItemStack(stack, result);
            }
        }

        // Scanne Bargeld in Slot 9 (Wallet)
        ItemStack wallet = player.getInventory().getItem(8); // Slot 9 = Index 8
        if (wallet.getItem() instanceof CashItem) {
            double cashAmount = CashItem.getValue(wallet);
            if (cashAmount > ModConfigHandler.COMMON.POLICE_ILLEGAL_CASH_THRESHOLD.get()) {
                result.totalCashFound += cashAmount;
                result.foundIllegalItems.add("Bargeld: " + cashAmount + "€");
            }
        }
    }

    /**
     * Scannt einen ItemStack
     */
    private static void scanItemStack(ItemStack stack, ScanResult result) {
        Item item = stack.getItem();
        String itemName = BuiltInRegistries.ITEM.getKey(item).toString();

        // Prüfe auf illegale Tabak-Produkte
        if (itemName.contains("schedulemc:")) {
            if (itemName.contains("moist_tobacco") ||
                itemName.contains("dried_tobacco") ||
                itemName.contains("fermented_tobacco") ||
                itemName.contains("packaged_tobacco")) {

                result.illegalItemCount += stack.getCount();
                result.foundIllegalItems.add(stack.getCount() + "x " + itemName);
            }
        }

        // NOTE: Weapon scanning not implemented - requires weapons mod integration
        // Future enhancement: ModConfigHandler.COMMON.POLICE_ILLEGAL_WEAPONS support
    }

    /**
     * Gibt detaillierten Bericht über gefundene illegale Items
     */
    public static String generateReport(ScanResult result) {
        StringBuilder report = new StringBuilder();
        report.append("§c§l⚠ POLIZEI RAID ⚠\n");
        report.append("§7Illegale Aktivitäten festgestellt:\n\n");

        if (result.illegalPlantCount > 0) {
            report.append("§c• ").append(result.illegalPlantCount).append(" Tabakpflanzen\n");
        }

        if (result.totalCashFound > 10000.0) {
            report.append("§c• ").append(String.format("%.0f", result.totalCashFound)).append("€ illegales Bargeld\n");
        }

        if (result.illegalItemCount > 0) {
            report.append("§c• ").append(result.illegalItemCount).append(" illegale Tabak-Produkte\n");
        }

        if (result.illegalBlockCount > 0) {
            report.append("§c• ").append(result.illegalBlockCount).append(" illegale Blöcke\n");
        }

        report.append("\n§eSchweregrad: ").append(result.calculateSeverity()).append("/10");

        return report.toString();
    }
}
