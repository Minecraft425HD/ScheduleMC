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
     * Scannt Umgebung nach illegalen Items
     */
    public static ScanResult scanArea(Level level, BlockPos center, ServerPlayer player) {
        ScanResult result = new ScanResult();

        int radius = ModConfigHandler.COMMON.POLICE_RAID_SCAN_RADIUS.get();

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

        // TODO: Bargeld-Blöcke scannen (wenn implementiert)
        // TODO: Verarbeitungsmaschinen scannen
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

        // TODO: Waffen scannen (später wenn Mod hinzugefügt)
        // List<String> illegalWeapons = ModConfigHandler.COMMON.POLICE_ILLEGAL_WEAPONS.get();
        // if (illegalWeapons.contains(itemName)) { ... }
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
