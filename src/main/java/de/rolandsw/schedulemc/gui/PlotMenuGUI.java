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
        // PERFORMANCE: Berechne alle Stats einmal statt 5+ separate stream() Aufrufe
        PlotStats stats = new PlotStats(player);

        SimpleContainer container = new SimpleContainer(27); // 3 Reihen
        
        // ═══════════════════════════════════════════════════════════
        // REIHE 1: Hauptoptionen
        // ═══════════════════════════════════════════════════════════
        
        // Slot 1: Eigene Plots
        ItemStack ownPlots = new ItemStack(Items.OAK_SIGN);
        ownPlots.setHoverName(Component.translatable("gui.plotmenu.own_plots"));
        addLore(ownPlots,
            Component.translatable("gui.plotmenu.own_plots.desc").getString(),
            Component.translatable("gui.plotmenu.own_plots.count", stats.ownedCount).getString(),
            "",
            Component.translatable("gui.plotmenu.click_to_open").getString()
        );
        container.setItem(10, ownPlots);
        
        // Slot 2: Plots kaufen
        ItemStack buyPlots = new ItemStack(Items.EMERALD);
        buyPlots.setHoverName(Component.translatable("gui.plotmenu.buy_plots"));
        addLore(buyPlots,
            Component.translatable("gui.plotmenu.buy_plots.desc").getString(),
            Component.translatable("gui.plotmenu.buy_plots.available", stats.availableCount).getString(),
            "",
            Component.translatable("gui.plotmenu.click_to_open").getString()
        );
        container.setItem(12, buyPlots);
        
        // Slot 3: Plots mieten
        ItemStack rentPlots = new ItemStack(Items.GOLD_INGOT);
        rentPlots.setHoverName(Component.translatable("gui.plotmenu.rent_plots"));
        addLore(rentPlots,
            Component.translatable("gui.plotmenu.rent_plots.desc").getString(),
            Component.translatable("gui.plotmenu.rent_plots.available", stats.rentableCount).getString(),
            "",
            Component.translatable("gui.plotmenu.click_to_open").getString()
        );
        container.setItem(14, rentPlots);
        
        // Slot 4: Top Plots
        ItemStack topPlots = new ItemStack(Items.DIAMOND);
        topPlots.setHoverName(Component.translatable("gui.plotmenu.top_plots"));
        addLore(topPlots,
            Component.translatable("gui.plotmenu.top_plots.desc").getString(),
            Component.translatable("gui.plotmenu.top_plots.top10").getString(),
            "",
            Component.translatable("gui.plotmenu.click_to_open").getString()
        );
        container.setItem(16, topPlots);
        
        // ═══════════════════════════════════════════════════════════
        // REIHE 2: Zusatzoptionen
        // ═══════════════════════════════════════════════════════════
        
        // Shop (falls aktiviert)
        ItemStack shop = new ItemStack(Items.CHEST);
        shop.setHoverName(Component.translatable("gui.plotmenu.shop"));
        addLore(shop,
            Component.translatable("gui.plotmenu.shop.desc").getString(),
            "",
            Component.translatable("gui.plotmenu.click_to_open").getString()
        );
        container.setItem(19, shop);
        
        // Daily Reward Info
        ItemStack daily = new ItemStack(Items.GOLD_BLOCK);
        daily.setHoverName(Component.translatable("gui.daily_reward"));
        addLore(daily,
            Component.translatable("gui.plotmenu.daily.desc").getString(),
            Component.translatable("gui.plotmenu.daily.command").getString(),
            "",
            Component.translatable("gui.plotmenu.daily.streak").getString()
        );
        container.setItem(21, daily);
        
        // Statistiken
        ItemStack statsItem = new ItemStack(Items.BOOK);
        statsItem.setHoverName(Component.translatable("gui.plotmenu.stats"));
        addLore(statsItem,
            Component.translatable("gui.plotmenu.stats.desc").getString(),
            Component.translatable("gui.plotmenu.stats.owned", stats.ownedCount).getString(),
            Component.translatable("gui.plotmenu.stats.rented", stats.rentedCount).getString(),
            Component.translatable("gui.plotmenu.stats.trusted", stats.trustedCount).getString()
        );
        container.setItem(23, statsItem);
        
        // ═══════════════════════════════════════════════════════════
        // REIHE 3: Schließen-Button
        // ═══════════════════════════════════════════════════════════
        
        ItemStack close = new ItemStack(Items.BARRIER);
        close.setHoverName(Component.translatable("gui.common.close_red"));
        container.setItem(26, close);
        
        // Container öffnen
        MenuProvider provider = new SimpleMenuProvider(
            (id, inv, p) -> new PlotMenuContainer(id, inv, container),
            Component.translatable("gui.plot.menu")
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
    
    // ═══════════════════════════════════════════════════════════
    // PERFORMANCE: Plot Statistics Cache
    // ═══════════════════════════════════════════════════════════

    /**
     * Cached plot statistics to avoid multiple expensive stream operations.
     * Instead of 5+ separate PlotManager.getPlots().stream() calls,
     * we iterate once and calculate all stats in a single pass.
     */
    private static class PlotStats {
        final int ownedCount;
        final int availableCount;
        final int rentableCount;
        final int rentedCount;
        final int trustedCount;

        PlotStats(ServerPlayer player) {
            var plots = PlotManager.getPlots();
            String playerUUIDStr = player.getStringUUID();
            java.util.UUID playerUUID = player.getUUID();

            // Single-pass calculation of all stats
            int owned = 0, available = 0, rentable = 0, rented = 0, trusted = 0;

            for (PlotRegion plot : plots) {
                if (plot.isOwnedBy(playerUUID)) owned++;
                if (!plot.hasOwner()) available++;
                if (plot.isForRent() && !plot.isRented()) rentable++;
                if (plot.isRented() && plot.getRenterUUID().equals(playerUUIDStr)) rented++;
                if (plot.isTrusted(playerUUID)) trusted++;
            }

            this.ownedCount = owned;
            this.availableCount = available;
            this.rentableCount = rentable;
            this.rentedCount = rented;
            this.trustedCount = trusted;
        }
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
