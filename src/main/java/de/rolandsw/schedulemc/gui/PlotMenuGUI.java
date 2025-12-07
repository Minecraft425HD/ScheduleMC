package de.rolandsw.schedulemc.gui;

import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.SimpleContainer;

/**
 * Haupt-GUI für Plot-Verwaltung
 * 
 * Layout:
 * [Eigene Plots] [Plots kaufen] [Plots mieten] [Top Plots] [Statistiken]
 */
public class PlotMenuGUI {

    /**
     * Öffnet das Haupt-Menü
     */
    public static void open(ServerPlayer player) {
        SimpleContainer container = new SimpleContainer(27); // 3 Reihen
        
        // ═══════════════════════════════════════════════════════════
        // REIHE 1: Hauptoptionen
        // ═══════════════════════════════════════════════════════════
        
        // Slot 1: Eigene Plots
        ItemStack ownPlots = new ItemStack(Items.OAK_SIGN);
        ownPlots.setHoverName(Component.literal("§a§lEigene Plots"));
        addLore(ownPlots, 
            "§7Verwalte deine Plots",
            "§7Plots: §e" + getOwnedPlotsCount(player),
            "",
            "§eKlick zum Öffnen"
        );
        container.setItem(10, ownPlots);
        
        // Slot 2: Plots kaufen
        ItemStack buyPlots = new ItemStack(Items.EMERALD);
        buyPlots.setHoverName(Component.literal("§a§lPlots kaufen"));
        addLore(buyPlots,
            "§7Kaufe verfügbare Plots",
            "§7Verfügbar: §e" + getAvailablePlotsCount(),
            "",
            "§eKlick zum Öffnen"
        );
        container.setItem(12, buyPlots);
        
        // Slot 3: Plots mieten
        ItemStack rentPlots = new ItemStack(Items.GOLD_INGOT);
        rentPlots.setHoverName(Component.literal("§d§lPlots mieten"));
        addLore(rentPlots,
            "§7Miete Plots temporär",
            "§7Zur Miete: §e" + getRentablePlotsCount(),
            "",
            "§eKlick zum Öffnen"
        );
        container.setItem(14, rentPlots);
        
        // Slot 4: Top Plots
        ItemStack topPlots = new ItemStack(Items.DIAMOND);
        topPlots.setHoverName(Component.literal("§6§lTop Plots"));
        addLore(topPlots,
            "§7Bestbewertete Plots",
            "§7Top 10 nach Rating",
            "",
            "§eKlick zum Öffnen"
        );
        container.setItem(16, topPlots);
        
        // ═══════════════════════════════════════════════════════════
        // REIHE 2: Zusatzoptionen
        // ═══════════════════════════════════════════════════════════
        
        // Shop (falls aktiviert)
        ItemStack shop = new ItemStack(Items.CHEST);
        shop.setHoverName(Component.literal("§e§lShop"));
        addLore(shop,
            "§7Items kaufen & verkaufen",
            "",
            "§eKlick zum Öffnen"
        );
        container.setItem(19, shop);
        
        // Daily Reward Info
        ItemStack daily = new ItemStack(Items.GOLD_BLOCK);
        daily.setHoverName(Component.literal("§6§lTägliche Belohnung"));
        addLore(daily,
            "§7Hole deine tägliche Belohnung ab!",
            "§7Befehl: §e/daily",
            "",
            "§7Baue einen Streak auf für Boni!"
        );
        container.setItem(21, daily);
        
        // Statistiken
        ItemStack stats = new ItemStack(Items.BOOK);
        stats.setHoverName(Component.literal("§b§lStatistiken"));
        addLore(stats,
            "§7Deine Plot-Statistiken",
            "§7Besessen: §e" + getOwnedPlotsCount(player),
            "§7Gemietet: §e" + getRentedPlotsCount(player),
            "§7Vertraut in: §e" + getTrustedInCount(player)
        );
        container.setItem(23, stats);
        
        // ═══════════════════════════════════════════════════════════
        // REIHE 3: Schließen-Button
        // ═══════════════════════════════════════════════════════════
        
        ItemStack close = new ItemStack(Items.BARRIER);
        close.setHoverName(Component.literal("§c§lSchließen"));
        container.setItem(26, close);
        
        // Container öffnen
        MenuProvider provider = new SimpleMenuProvider(
            (id, inv, p) -> new PlotMenuContainer(id, inv, container),
            Component.literal("§8Plot-Menü")
        );
        
        player.openMenu(provider);
    }
    
    // ═══════════════════════════════════════════════════════════
    // HELPER METHODEN
    // ═══════════════════════════════════════════════════════════
    
    private static void addLore(ItemStack stack, String... lines) {
        // TODO: Implement lore addition
        // net.minecraft.world.item.component.ItemLore
    }
    
    private static int getOwnedPlotsCount(ServerPlayer player) {
        return (int) PlotManager.getPlots().stream()
            .filter(p -> p.isOwnedBy(player.getUUID()))
            .count();
    }
    
    private static int getAvailablePlotsCount() {
        return (int) PlotManager.getPlots().stream()
            .filter(p -> !p.hasOwner())
            .count();
    }
    
    private static int getRentablePlotsCount() {
        return (int) PlotManager.getPlots().stream()
            .filter(p -> p.isForRent() && !p.isRented())
            .count();
    }
    
    private static int getRentedPlotsCount(ServerPlayer player) {
        return (int) PlotManager.getPlots().stream()
            .filter(p -> p.isRented() && p.getRenterUUID().equals(player.getStringUUID()))
            .count();
    }
    
    private static int getTrustedInCount(ServerPlayer player) {
        return (int) PlotManager.getPlots().stream()
            .filter(p -> p.isTrusted(player.getUUID()))
            .count();
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONTAINER KLASSE
    // ═══════════════════════════════════════════════════════════
    
    private static class PlotMenuContainer extends ChestMenu {
        private final SimpleContainer container;
        
        public PlotMenuContainer(int id, Inventory playerInv, SimpleContainer container) {
            super(MenuType.GENERIC_9x3, id, playerInv, container, 3);
            this.container = container;
        }
        
        @Override
        public boolean stillValid(Player player) {
            return true;
        }
        
        @Override
        public void removed(Player player) {
            super.removed(player);
            // Cleanup wenn nötig
        }
    }
}
