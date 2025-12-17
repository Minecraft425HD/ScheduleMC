package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.utility.PlotUtilityData;
import de.rolandsw.schedulemc.utility.PlotUtilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Settings App - Einstellungen auf dem Smartphone
 *
 * Features:
 * - 3 Tabs mit Scroll-Support
 * - Tab 1: Plot-Einstellungen (Verkauf/Miete, Trusted Players, Umbenennen)
 * - Tab 2: Benachrichtigungen (Utility-Warnungen, Schwellenwerte)
 * - Tab 3: Konto (Kontostand)
 */
@OnlyIn(Dist.CLIENT)
public class SettingsAppScreen extends Screen {

    private final Screen parentScreen;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 240;
    private static final int BORDER_SIZE = 5;
    private static final int MARGIN_TOP = 15;
    private static final int MARGIN_BOTTOM = 60;

    // Tab-System
    private int currentTab = 0;
    private static final String[] TAB_NAMES = {"Plot", "Meldung", "Konto"};
    private static final int TAB_HEIGHT = 22;
    private static final int TAB_WIDTH = 58;

    // Scrolling
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int SCROLL_SPEED = 15;
    private static final int CONTENT_HEIGHT = 160;

    private int leftPos;
    private int topPos;

    // Cached Data
    private PlotRegion currentPlot;
    private List<PlotRegion> myPlots;

    // Settings State (simuliert - würde normalerweise persistent gespeichert)
    private boolean utilityWarningsEnabled = true;
    private double electricityWarningThreshold = 100.0; // kWh
    private double waterWarningThreshold = 500.0; // L

    // Simulated account balance
    private double accountBalance = 15000.0; // €

    public SettingsAppScreen(Screen parent) {
        super(Component.literal("Settings"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;

        int centeredTop = (this.height - HEIGHT) / 2;
        int minTop = MARGIN_TOP + BORDER_SIZE;
        int maxTop = this.height - HEIGHT - BORDER_SIZE - MARGIN_BOTTOM;
        this.topPos = Math.max(minTop, Math.min(centeredTop, maxTop));

        // Cache data
        refreshData();

        // Tab-Buttons
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int tabIndex = i;
            addRenderableWidget(Button.builder(
                Component.literal(TAB_NAMES[i]),
                button -> {
                    currentTab = tabIndex;
                    scrollOffset = 0;
                    refreshData();
                }
            ).bounds(leftPos + 10 + (i * TAB_WIDTH), topPos + 30, TAB_WIDTH - 2, TAB_HEIGHT).build());
        }

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.literal("<< Zurück"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());
    }

    /**
     * Aktualisiert gecachte Daten
     */
    private void refreshData() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        String playerUUID = mc.player.getUUID().toString();

        // Aktueller Plot
        currentPlot = PlotManager.getPlotAt(playerPos);

        // Meine Plots
        myPlots = new ArrayList<>();
        for (PlotRegion plot : PlotManager.getAllPlots()) {
            if (playerUUID.equals(plot.getOwnerUUID())) {
                myPlots.add(plot);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Smartphone-Hintergrund
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF2A2A2A);

        // Header
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 28, 0xFF1A1A1A);
        guiGraphics.drawCenteredString(this.font, "§f§lEinstellungen", leftPos + WIDTH / 2, topPos + 10, 0xFFFFFF);

        // Tab-Hintergrund (aktiver Tab hervorheben)
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tabX = leftPos + 10 + (i * TAB_WIDTH);
            int tabY = topPos + 30;
            if (i == currentTab) {
                guiGraphics.fill(tabX - 1, tabY - 1, tabX + TAB_WIDTH - 1, tabY + TAB_HEIGHT + 1, 0xFF4A90E2);
            }
        }

        // Content-Bereich
        int contentY = topPos + 55;
        int contentEndY = topPos + HEIGHT - 40;

        // Render Tab-Content
        switch (currentTab) {
            case 0 -> renderPlotSettingsTab(guiGraphics, contentY, contentEndY);
            case 1 -> renderNotificationsTab(guiGraphics, contentY, contentEndY);
            case 2 -> renderAccountTab(guiGraphics, contentY, contentEndY);
        }

        // Scroll-Indikator
        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(20, CONTENT_HEIGHT * CONTENT_HEIGHT / (CONTENT_HEIGHT + maxScroll));
            int scrollBarY = contentY + (scrollOffset * (CONTENT_HEIGHT - scrollBarHeight) / maxScroll);
            guiGraphics.fill(leftPos + WIDTH - 8, contentY, leftPos + WIDTH - 5, contentEndY, 0x44FFFFFF);
            guiGraphics.fill(leftPos + WIDTH - 8, scrollBarY, leftPos + WIDTH - 5, scrollBarY + scrollBarHeight, 0xAAFFFFFF);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 1: PLOT-EINSTELLUNGEN
    // ═══════════════════════════════════════════════════════════════════════════

    private void renderPlotSettingsTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String playerUUID = mc.player.getUUID().toString();

        // Aktueller Plot Check
        if (currentPlot == null) {
            guiGraphics.drawCenteredString(this.font, "§7Kein Plot gefunden", leftPos + WIDTH / 2, y + 20, 0xAAAAAA);
            guiGraphics.drawCenteredString(this.font, "§8Stehe auf einem Plot", leftPos + WIDTH / 2, y + 35, 0x666666);
            maxScroll = 0;
            return;
        }

        boolean isOwner = playerUUID.equals(currentPlot.getOwnerUUID());

        // Plot-Header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§6§l" + currentPlot.getPlotName(), leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        if (!isOwner) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 25, 0x44AA0000);
                guiGraphics.drawString(this.font, "§c✗ Nicht dein Grundstück", leftPos + 15, y + 8, 0xFF5555);
            }
            y += 30;
            contentHeight += 30;
            maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
            return;
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // VERKAUF / MIETE
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§e§l🏷 VERKAUF / MIETE", leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        // Aktueller Status
        String saleStatus;
        if (currentPlot.isForSale()) {
            saleStatus = "§a● Zum Verkauf (" + String.format("%.0f€", currentPlot.getSalePrice()) + ")";
        } else if (currentPlot.isForRent()) {
            if (currentPlot.isRented()) {
                saleStatus = "§e● Vermietet";
            } else {
                saleStatus = "§d● Zu vermieten (" + String.format("%.0f€/Tag", currentPlot.getRentPricePerDay()) + ")";
            }
        } else {
            saleStatus = "§7● Privat (nicht angeboten)";
        }

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§7Status: " + saleStatus, leftPos + 15, y, 0xAAAAAA);
        }
        y += 12;
        contentHeight += 12;

        // Optionen (Info-Text)
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 35, 0x33333333);
            guiGraphics.drawString(this.font, "§8Verwende /plot sell <Preis>", leftPos + 15, y + 4, 0x666666);
            guiGraphics.drawString(this.font, "§8oder /plot rent <Preis>", leftPos + 15, y + 14, 0x666666);
            guiGraphics.drawString(this.font, "§8um anzubieten.", leftPos + 15, y + 24, 0x666666);
        }
        y += 40;
        contentHeight += 40;

        // ═══════════════════════════════════════════════════════════════════════════
        // TRUSTED PLAYERS
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§b§l👥 TRUSTED PLAYERS", leftPos + 15, y, 0x55FFFF);
        }
        y += 15;
        contentHeight += 15;

        // Zeige Trusted Players
        List<String> trustedPlayers = currentPlot.getTrustedPlayers();
        if (trustedPlayers.isEmpty()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§8Keine vertrauenswürdigen Spieler", leftPos + 15, y, 0x666666);
            }
            y += 12;
            contentHeight += 12;
        } else {
            for (String trusted : trustedPlayers) {
                if (y >= startY - 10 && y < endY) {
                    guiGraphics.drawString(this.font, "§a● §f" + trusted, leftPos + 20, y, 0xFFFFFF);
                }
                y += 11;
                contentHeight += 11;
            }
        }

        // Hinweis
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y + 5, leftPos + WIDTH - 10, y + 30, 0x33333333);
            guiGraphics.drawString(this.font, "§8/plot trust <Spieler>", leftPos + 15, y + 9, 0x666666);
            guiGraphics.drawString(this.font, "§8/plot untrust <Spieler>", leftPos + 15, y + 19, 0x666666);
        }
        y += 35;
        contentHeight += 35;

        // ═══════════════════════════════════════════════════════════════════════════
        // PLOT UMBENENNEN
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§d§l✏ PLOT-NAME", leftPos + 15, y, 0xFF55FF);
        }
        y += 15;
        contentHeight += 15;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§7Aktuell: §f" + currentPlot.getPlotName(), leftPos + 15, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y + 3, leftPos + WIDTH - 10, y + 18, 0x33333333);
            guiGraphics.drawString(this.font, "§8/plot rename <Neuer Name>", leftPos + 15, y + 6, 0x666666);
        }
        y += 25;
        contentHeight += 25;

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 2: BENACHRICHTIGUNGEN
    // ═══════════════════════════════════════════════════════════════════════════

    private void renderNotificationsTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        // ═══════════════════════════════════════════════════════════════════════════
        // UTILITY-WARNUNGEN
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§e§l⚠ UTILITY-WARNUNGEN", leftPos + 15, y, 0xFFAA00);
        }
        y += 18;
        contentHeight += 18;

        // An/Aus Status
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 25, 0x44333333);
            String status = utilityWarningsEnabled ? "§a● Aktiviert" : "§c● Deaktiviert";
            guiGraphics.drawString(this.font, "§7Warnungen: " + status, leftPos + 15, y + 8, 0xFFFFFF);
        }
        y += 30;
        contentHeight += 30;

        // Info über Warnungen
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§8Du erhältst Warnungen bei", leftPos + 15, y, 0x666666);
        }
        y += 11;
        contentHeight += 11;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§8hohem Strom-/Wasserverbrauch.", leftPos + 15, y, 0x666666);
        }
        y += 18;
        contentHeight += 18;

        // ═══════════════════════════════════════════════════════════════════════════
        // SCHWELLENWERTE
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 10;
        contentHeight += 10;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§b§l📊 SCHWELLENWERTE", leftPos + 15, y, 0x55FFFF);
        }
        y += 18;
        contentHeight += 18;

        // Strom-Schwellenwert
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 30, 0x33333333);
            guiGraphics.drawString(this.font, "§e⚡ Strom-Warnung ab:", leftPos + 15, y + 4, 0xFFAA00);
            guiGraphics.drawString(this.font, "§f" + String.format("%.0f kWh", electricityWarningThreshold), leftPos + 130, y + 4, 0xFFFFFF);

            // Mini-Balken
            int barWidth = WIDTH - 40;
            int filledWidth = (int) ((electricityWarningThreshold / 500.0) * barWidth);
            guiGraphics.fill(leftPos + 15, y + 18, leftPos + 15 + barWidth, y + 24, 0x44666666);
            guiGraphics.fill(leftPos + 15, y + 18, leftPos + 15 + filledWidth, y + 24, 0xAAFFAA00);
        }
        y += 35;
        contentHeight += 35;

        // Wasser-Schwellenwert
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 30, 0x33333333);
            guiGraphics.drawString(this.font, "§b💧 Wasser-Warnung ab:", leftPos + 15, y + 4, 0x55AAFF);
            guiGraphics.drawString(this.font, "§f" + String.format("%.0f L", waterWarningThreshold), leftPos + 135, y + 4, 0xFFFFFF);

            // Mini-Balken
            int barWidth = WIDTH - 40;
            int filledWidth = (int) ((waterWarningThreshold / 2000.0) * barWidth);
            guiGraphics.fill(leftPos + 15, y + 18, leftPos + 15 + barWidth, y + 24, 0x44666666);
            guiGraphics.fill(leftPos + 15, y + 18, leftPos + 15 + filledWidth, y + 24, 0xAA55AAFF);
        }
        y += 38;
        contentHeight += 38;

        // Info-Text
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§8Bei Überschreitung siehst du", leftPos + 15, y, 0x666666);
        }
        y += 11;
        contentHeight += 11;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§8eine Warnung in der Finanz-App.", leftPos + 15, y, 0x666666);
        }
        y += 20;
        contentHeight += 20;

        // ═══════════════════════════════════════════════════════════════════════════
        // VERDÄCHTIGKEITS-HINWEIS
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 10;
        contentHeight += 10;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§c§l🚨 POLIZEI-HEAT", leftPos + 15, y, 0xFF5555);
        }
        y += 15;
        contentHeight += 15;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 45, 0x44330000);
            guiGraphics.drawString(this.font, "§8Hoher Verbrauch kann", leftPos + 15, y + 5, 0x666666);
            guiGraphics.drawString(this.font, "§8Aufmerksamkeit erregen!", leftPos + 15, y + 15, 0x666666);
            guiGraphics.drawString(this.font, "§c>200 kWh §8oder §c>1000 L", leftPos + 15, y + 30, 0xAA5555);
        }
        y += 50;
        contentHeight += 50;

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 3: KONTO
    // ═══════════════════════════════════════════════════════════════════════════

    private void renderAccountTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        // ═══════════════════════════════════════════════════════════════════════════
        // KONTOSTAND
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§6§l💰 KONTOSTAND", leftPos + 15, y, 0xFFAA00);
        }
        y += 18;
        contentHeight += 18;

        // Großer Kontostand-Display
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 50, 0x44228B22);
            guiGraphics.drawCenteredString(this.font, "§fVerfügbar:", leftPos + WIDTH / 2, y + 8, 0xFFFFFF);

            // Großer Betrag
            String balanceStr = String.format("§a§l%.2f €", accountBalance);
            guiGraphics.drawCenteredString(this.font, balanceStr, leftPos + WIDTH / 2, y + 25, 0x55FF55);

            guiGraphics.drawCenteredString(this.font, "§8Bank of Schedule", leftPos + WIDTH / 2, y + 40, 0x666666);
        }
        y += 58;
        contentHeight += 58;

        // ═══════════════════════════════════════════════════════════════════════════
        // MONATLICHE KOSTEN ÜBERSICHT
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 10;
        contentHeight += 10;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§e§l📊 LAUFENDE KOSTEN", leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        // Berechne geschätzte monatliche Kosten
        double totalDailyElec = 0;
        double totalDailyWater = 0;

        for (PlotRegion plot : myPlots) {
            Optional<PlotUtilityData> dataOpt = PlotUtilityManager.getPlotData(plot.getId());
            if (dataOpt.isPresent()) {
                PlotUtilityData data = dataOpt.get();
                totalDailyElec += data.get7DayAverageElectricity();
                totalDailyWater += data.get7DayAverageWater();
            }
        }

        double dailyCost = (totalDailyElec * 0.35) + (totalDailyWater * 0.005);
        double weeklyCost = dailyCost * 7;
        double monthlyCost = dailyCost * 30;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 55, 0x33333333);

            guiGraphics.drawString(this.font, "§7Täglich:", leftPos + 15, y + 5, 0xAAAAAA);
            guiGraphics.drawString(this.font, String.format("§e%.2f €", dailyCost), leftPos + 100, y + 5, 0xFFAA00);

            guiGraphics.drawString(this.font, "§7Wöchentlich:", leftPos + 15, y + 18, 0xAAAAAA);
            guiGraphics.drawString(this.font, String.format("§e%.2f €", weeklyCost), leftPos + 100, y + 18, 0xFFAA00);

            guiGraphics.drawString(this.font, "§7Monatlich (30d):", leftPos + 15, y + 31, 0xAAAAAA);
            guiGraphics.drawString(this.font, String.format("§e%.2f €", monthlyCost), leftPos + 100, y + 31, 0xFFAA00);

            // Reichweite
            int daysUntilEmpty = dailyCost > 0 ? (int) (accountBalance / dailyCost) : 999;
            String reichweiteColor = daysUntilEmpty < 7 ? "§c" : (daysUntilEmpty < 30 ? "§e" : "§a");
            guiGraphics.drawString(this.font, "§8Reichweite: " + reichweiteColor + daysUntilEmpty + " Tage", leftPos + 15, y + 44, 0x888888);
        }
        y += 60;
        contentHeight += 60;

        // ═══════════════════════════════════════════════════════════════════════════
        // GRUNDSTÜCKE
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 10;
        contentHeight += 10;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§b§l🏠 EIGENTUM", leftPos + 15, y, 0x55FFFF);
        }
        y += 15;
        contentHeight += 15;

        if (myPlots.isEmpty()) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§8Keine Grundstücke", leftPos + 15, y, 0x666666);
            }
            y += 12;
            contentHeight += 12;
        } else {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§a" + myPlots.size() + " Grundstück(e)", leftPos + 15, y, 0x55FF55);
            }
            y += 12;
            contentHeight += 12;

            // Liste der Grundstücke
            for (PlotRegion plot : myPlots) {
                if (y >= startY - 10 && y < endY) {
                    guiGraphics.drawString(this.font, "§7● " + plot.getPlotName(), leftPos + 20, y, 0xAAAAAA);
                }
                y += 11;
                contentHeight += 11;
            }
        }
        y += 10;
        contentHeight += 10;

        // ═══════════════════════════════════════════════════════════════════════════
        // HINWEIS
        // ═══════════════════════════════════════════════════════════════════════════
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 30, 0x33222222);
            guiGraphics.drawCenteredString(this.font, "§8Geld verdienen:", leftPos + WIDTH / 2, y + 5, 0x666666);
            guiGraphics.drawCenteredString(this.font, "§7Produkte verkaufen!", leftPos + WIDTH / 2, y + 17, 0xAAAAAA);
        }
        y += 35;
        contentHeight += 35;

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SCROLL HANDLING
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll > 0) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - delta * SCROLL_SPEED));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
