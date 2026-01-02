package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.Transaction;
import de.rolandsw.schedulemc.economy.TransactionHistory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

/**
 * Bank-App - Kontoverwaltung auf dem Smartphone
 *
 * Features:
 * - Kontostand anzeigen
 * - Überweisungen tätigen
 * - Transaktionshistorie anzeigen
 * - Finanzstatistiken
 */
@OnlyIn(Dist.CLIENT)
public class BankAppScreen extends Screen {

    private final Screen parentScreen;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 240;
    private static final int BORDER_SIZE = 5;
    private static final int MARGIN_TOP = 15;
    private static final int MARGIN_BOTTOM = 60;

    // Tab-System
    private int currentTab = 0;
    private static final String[] TAB_NAMES = {"Konto", "Historie", "Überweisung"};
    private static final int TAB_HEIGHT = 22;
    private static final int TAB_WIDTH = 62;

    // Scrolling
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int SCROLL_SPEED = 15;
    private static final int CONTENT_HEIGHT = 140;

    private int leftPos;
    private int topPos;

    // Transfer Form
    private EditBox transferRecipientBox;
    private EditBox transferAmountBox;
    private String transferMessage = "";
    private int transferMessageColor = 0xFFFFFF;

    // Cached Data
    private double balance = 0.0;
    private List<Transaction> recentTransactions = List.of();
    private double totalIncome = 0.0;
    private double totalExpenses = 0.0;

    public BankAppScreen(Screen parent) {
        super(Component.literal("Bank"));
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
                    transferMessage = "";
                    refreshData();
                    clearWidgets();
                    init();
                }
            ).bounds(leftPos + 5 + (i * TAB_WIDTH), topPos + 30, TAB_WIDTH - 2, TAB_HEIGHT).build());
        }

        // Transfer Form (nur in Tab 2)
        if (currentTab == 2) {
            int formY = topPos + 78;

            // Empfänger
            transferRecipientBox = new EditBox(this.font, leftPos + 15, formY, WIDTH - 30, 18, Component.literal("Empfänger"));
            transferRecipientBox.setMaxLength(100);
            transferRecipientBox.setHint(Component.literal("Spielername"));
            addRenderableWidget(transferRecipientBox);

            // Betrag
            transferAmountBox = new EditBox(this.font, leftPos + 15, formY + 38, WIDTH - 30, 18, Component.literal("Betrag"));
            transferAmountBox.setMaxLength(10);
            transferAmountBox.setHint(Component.literal("Betrag in €"));
            addRenderableWidget(transferAmountBox);

            // Senden-Button
            addRenderableWidget(Button.builder(Component.literal("Überweisen"), button -> {
                performTransfer();
            }).bounds(leftPos + 15, formY + 67, WIDTH - 30, 20).build());
        }

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.literal("← Zurück"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());

        // Refresh-Button
        addRenderableWidget(Button.builder(Component.literal("↻"), button -> {
            refreshData();
            transferMessage = "§aDaten aktualisiert!";
            transferMessageColor = 0x55FF55;
        }).bounds(leftPos + WIDTH - 30, topPos + HEIGHT - 30, 20, 20).build());
    }

    /**
     * Aktualisiert gecachte Daten
     */
    private void refreshData() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        UUID playerUUID = mc.player.getUUID();

        // Balance
        balance = EconomyManager.getBalance(playerUUID);

        // Transaction History
        TransactionHistory history = TransactionHistory.getInstance();
        if (history != null) {
            recentTransactions = history.getRecentTransactions(playerUUID, 20);
            totalIncome = history.getTotalIncome(playerUUID);
            totalExpenses = history.getTotalExpenses(playerUUID);
        }
    }

    /**
     * Führt Überweisung aus
     */
    private void performTransfer() {
        if (transferRecipientBox == null || transferAmountBox == null) {
            return;
        }

        String recipient = transferRecipientBox.getValue().trim();
        String amountStr = transferAmountBox.getValue().trim();

        // Validierung
        if (recipient.isEmpty()) {
            transferMessage = "§cBitte Empfänger eingeben!";
            transferMessageColor = 0xFF5555;
            return;
        }

        if (amountStr.isEmpty()) {
            transferMessage = "§cBitte Betrag eingeben!";
            transferMessageColor = 0xFF5555;
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            transferMessage = "§cUngültiger Betrag!";
            transferMessageColor = 0xFF5555;
            return;
        }

        if (amount <= 0) {
            transferMessage = "§cBetrag muss positiv sein!";
            transferMessageColor = 0xFF5555;
            return;
        }

        if (amount > balance) {
            transferMessage = "§cNicht genug Geld!";
            transferMessageColor = 0xFF5555;
            return;
        }

        // Hier würde normalerweise ein Packet an den Server gesendet werden
        // Für diese Demo zeigen wir nur eine Erfolgsmeldung
        transferMessage = String.format("§aÜberweisung an %s (%.2f€) gesendet!", recipient, amount);
        transferMessageColor = 0x55FF55;

        // Felder leeren
        transferRecipientBox.setValue("");
        transferAmountBox.setValue("");

        // TODO: Sende Packet an Server für tatsächliche Überweisung
        // TransferPacket.send(recipient, amount);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Smartphone-Hintergrund
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF2A2A2A);

        // Header
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 28, 0xFF1A1A1A);
        guiGraphics.drawCenteredString(this.font, "§6§lBank", leftPos + WIDTH / 2, topPos + 10, 0xFFFFFF);

        // Tab-Hintergrund (aktiver Tab hervorheben)
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tabX = leftPos + 5 + (i * TAB_WIDTH);
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
            case 0 -> renderAccountTab(guiGraphics, contentY, contentEndY);
            case 1 -> renderHistoryTab(guiGraphics, contentY, contentEndY);
            case 2 -> renderTransferTab(guiGraphics, contentY, contentEndY);
        }

        // Scroll-Indikator
        if (maxScroll > 0 && currentTab != 2) {
            int scrollBarHeight = Math.max(20, CONTENT_HEIGHT * CONTENT_HEIGHT / (CONTENT_HEIGHT + maxScroll));
            int scrollBarY = contentY + (scrollOffset * (CONTENT_HEIGHT - scrollBarHeight) / maxScroll);
            guiGraphics.fill(leftPos + WIDTH - 8, contentY, leftPos + WIDTH - 5, contentEndY, 0x44FFFFFF);
            guiGraphics.fill(leftPos + WIDTH - 8, scrollBarY, leftPos + WIDTH - 5, scrollBarY + scrollBarHeight, 0xAAFFFFFF);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 1: KONTOSTAND
    // ═══════════════════════════════════════════════════════════

    private void renderAccountTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        // Balance-Box
        if (y >= startY - 60 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 55, 0x44228B22);

            guiGraphics.drawCenteredString(this.font, "§f§lKontostand", leftPos + WIDTH / 2, y + 5, 0xFFFFFF);

            String balanceStr = String.format("§6§l%.2f€", balance);
            guiGraphics.drawCenteredString(this.font, balanceStr, leftPos + WIDTH / 2, y + 22, 0xFFAA00);

            String balanceColor = balance >= 0 ? "§a" : "§c";
            String balanceStatus = balance >= 0 ? "Positiv" : "Dispo";
            guiGraphics.drawCenteredString(this.font, balanceColor + balanceStatus, leftPos + WIDTH / 2, y + 40, 0xFFFFFF);
        }
        y += 60;
        contentHeight += 60;

        // Statistiken
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§6§lStatistiken", leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        // Einnahmen
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§aEinnahmen:", leftPos + 15, y, 0x55FF55);
            guiGraphics.drawString(this.font, String.format("§f+%.2f€", totalIncome), leftPos + 110, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        // Ausgaben
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§cAusgaben:", leftPos + 15, y, 0xFF5555);
            guiGraphics.drawString(this.font, String.format("§f-%.2f€", totalExpenses), leftPos + 110, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        // Bilanz
        double balance = totalIncome - totalExpenses;
        String balanceColor = balance >= 0 ? "§a" : "§c";
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§fBilanz:", leftPos + 15, y, 0xFFFFFF);
            guiGraphics.drawString(this.font, balanceColor + String.format("%.2f€", balance), leftPos + 110, y, 0xFFFFFF);
        }
        y += 15;
        contentHeight += 15;

        // Info
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawCenteredString(this.font, "§7Transaktionen: " + recentTransactions.size(), leftPos + WIDTH / 2, y, 0xAAAAAA);
        }
        y += 12;
        contentHeight += 12;

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 2: TRANSAKTIONSHISTORIE
    // ═══════════════════════════════════════════════════════════

    private void renderHistoryTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        if (recentTransactions.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, "§7Keine Transaktionen", leftPos + WIDTH / 2, y + 20, 0xAAAAAA);
            maxScroll = 0;
            return;
        }

        // Header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§e" + recentTransactions.size() + " Transaktionen", leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        for (Transaction tx : recentTransactions) {
            if (y >= startY - 40 && y < endY) {
                // Transaction-Box
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 35, 0x44333333);

                // Typ & Betrag
                String amountColor = tx.getAmount() > 0 ? "§a" : "§c";
                String amountStr = String.format("%s%.2f€", amountColor, Math.abs(tx.getAmount()));

                guiGraphics.drawString(this.font, "§f" + tx.getType().getDisplayName(), leftPos + 15, y + 3, 0xFFFFFF);
                guiGraphics.drawString(this.font, amountStr, leftPos + 130, y + 3, 0xFFFFFF);

                // Beschreibung
                String desc = tx.getDescription();
                if (desc != null && !desc.isEmpty() && desc.length() > 20) {
                    desc = desc.substring(0, 17) + "...";
                }
                guiGraphics.drawString(this.font, "§8" + (desc != null ? desc : ""), leftPos + 15, y + 14, 0x666666);

                // Datum
                guiGraphics.drawString(this.font, "§7" + tx.getFormattedDate(), leftPos + 15, y + 25, 0x888888);
            }
            y += 40;
            contentHeight += 40;
        }

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 3: ÜBERWEISUNG
    // ═══════════════════════════════════════════════════════════

    private void renderTransferTab(GuiGraphics guiGraphics, int startY, int endY) {
        // Aktueller Kontostand
        guiGraphics.drawString(this.font, "§7Verfügbar:", leftPos + 15, startY, 0xAAAAAA);
        guiGraphics.drawString(this.font, String.format("§6%.2f€", balance), leftPos + 110, startY, 0xFFAA00);

        // Labels für Form-Felder (über den EditBoxen mit ausreichend Abstand)
        guiGraphics.drawString(this.font, "§fEmpfängername:", leftPos + 15, startY + 13, 0xFFFFFF);
        guiGraphics.drawString(this.font, "§fBetrag in €:", leftPos + 15, startY + 51, 0xFFFFFF);

        // Transfer-Message
        if (!transferMessage.isEmpty()) {
            int messageY = startY + 100;
            // Mehrzeiliger Text wenn zu lang
            String[] lines = transferMessage.split("\n");
            for (String line : lines) {
                guiGraphics.drawCenteredString(this.font, line, leftPos + WIDTH / 2, messageY, transferMessageColor);
                messageY += 12;
            }
        }

        // Info
        guiGraphics.drawCenteredString(this.font, "§8Überweisung an andere Spieler", leftPos + WIDTH / 2, startY + 130, 0x666666);
    }

    // ═══════════════════════════════════════════════════════════
    // SCROLL HANDLING
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (currentTab != 2 && maxScroll > 0) {
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
