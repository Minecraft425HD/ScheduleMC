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
        ownPlots.setHoverName(Component.translatable("gui.plotmenu.own_plots"));
        addLore(ownPlots,
            Component.translatable("gui.plotmenu.own_plots.desc").getString(),
            Component.translatable("gui.plotmenu.own_plots.count", getOwnedPlotsCount(player)).getString(),
            "",
            Component.translatable("gui.plotmenu.click_to_open").getString()
        );
        container.setItem(10, ownPlots);
        
        // Slot 2: Plots kaufen
        ItemStack buyPlots = new ItemStack(Items.EMERALD);
        buyPlots.setHoverName(Component.translatable("gui.plotmenu.buy_plots"));
        addLore(buyPlots,
            Component.translatable("gui.plotmenu.buy_plots.desc").getString(),
            Component.translatable("gui.plotmenu.buy_plots.available", getAvailablePlotsCount()).getString(),
            "",
            Component.translatable("gui.plotmenu.click_to_open").getString()
        );
        container.setItem(12, buyPlots);
        
        // Slot 3: Plots mieten
        ItemStack rentPlots = new ItemStack(Items.GOLD_INGOT);
        rentPlots.setHoverName(Component.translatable("gui.plotmenu.rent_plots"));
        addLore(rentPlots,
            Component.translatable("gui.plotmenu.rent_plots.desc").getString(),
            Component.translatable("gui.plotmenu.rent_plots.available", getRentablePlotsCount()).getString(),
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
        ItemStack stats = new ItemStack(Items.BOOK);
        stats.setHoverName(Component.translatable("gui.plotmenu.stats"));
        addLore(stats,
            Component.translatable("gui.plotmenu.stats.desc").getString(),
            Component.translatable("gui.plotmenu.stats.owned", getOwnedPlotsCount(player)).getString(),
            Component.translatable("gui.plotmenu.stats.rented", getRentedPlotsCount(player)).getString(),
            Component.translatable("gui.plotmenu.stats.trusted", getTrustedInCount(player)).getString()
        );
        container.setItem(23, stats);
        
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
