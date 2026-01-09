package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.CreditLoan;
import de.rolandsw.schedulemc.economy.CreditLoanManager;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.RecurringPayment;
import de.rolandsw.schedulemc.economy.RecurringPaymentInterval;
import de.rolandsw.schedulemc.economy.RecurringPaymentManager;
import de.rolandsw.schedulemc.economy.Transaction;
import de.rolandsw.schedulemc.economy.TransactionHistory;
import de.rolandsw.schedulemc.npc.network.BankTransferPacket;
import de.rolandsw.schedulemc.npc.network.CreateRecurringPaymentPacket;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
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
    private static final int WIDTH = 240;
    private static final int HEIGHT = 240;
    private static final int BORDER_SIZE = 5;
    private static final int MARGIN_TOP = 15;
    private static final int MARGIN_BOTTOM = 60;

    // Tab-System
    private int currentTab = 0;
    private static final int TAB_HEIGHT = 22;
    // Tab widths: Account (45), History (45), Transfer (56), Recurring (76)
    private static final int[] TAB_WIDTHS = {45, 45, 56, 76};

    /**
     * Get localized tab names
     */
    private String[] getTabNames() {
        return new String[]{
            Component.translatable("gui.app.bank.tab.account").getString(),
            Component.translatable("gui.app.bank.tab.history").getString(),
            Component.translatable("gui.app.bank.tab.transfer").getString(),
            Component.translatable("gui.app.bank.tab.recurring").getString()
        };
    }

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

    // Recurring Payment Form
    private EditBox recurringRecipientBox;
    private EditBox recurringAmountBox;
    private Button recurringIntervalButton;
    private RecurringPaymentInterval selectedInterval = RecurringPaymentInterval.MONTHLY;

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

        // Tab-Buttons mit individuellen Breiten
        int currentX = leftPos + 5;
        for (int i = 0; i < getTabNames().length; i++) {
            final int tabIndex = i;
            addRenderableWidget(Button.builder(
                Component.literal(getTabNames()[i]),
                button -> {
                    currentTab = tabIndex;
                    scrollOffset = 0;
                    transferMessage = "";
                    refreshData();
                    clearWidgets();
                    init();
                }
            ).bounds(currentX, topPos + 30, TAB_WIDTHS[i] - 2, TAB_HEIGHT).build());
            currentX += TAB_WIDTHS[i];
        }

        // Transfer Form (nur in Tab 2)
        if (currentTab == 2) {
            int formY = topPos + 78;

            // Empfänger
            transferRecipientBox = new EditBox(this.font, leftPos + 15, formY, WIDTH - 30, 18, Component.translatable("message.bank.recipient"));
            transferRecipientBox.setMaxLength(100);
            transferRecipientBox.setHint(Component.translatable("gui.bank.player_name_hint"));
            addRenderableWidget(transferRecipientBox);

            // Betrag
            transferAmountBox = new EditBox(this.font, leftPos + 15, formY + 38, WIDTH - 30, 18, Component.translatable("gui.common.amount"));
            transferAmountBox.setMaxLength(10);
            transferAmountBox.setHint(Component.translatable("gui.common.amount_euro"));
            addRenderableWidget(transferAmountBox);

            // Senden-Button
            addRenderableWidget(Button.builder(Component.translatable("gui.bank.transfer_button"), button -> {
                performTransfer();
            }).bounds(leftPos + 15, formY + 67, WIDTH - 30, 20).build());
        }

        // Recurring Payment Form (nur in Tab 3)
        if (currentTab == 3) {
            int contentY = topPos + 55;  // Gleich wie render()

            // Empfänger (Label bei contentY + 25, Box bei contentY + 37)
            recurringRecipientBox = new EditBox(this.font, leftPos + 15, contentY + 37, WIDTH - 30, 18, Component.translatable("message.bank.recipient"));
            recurringRecipientBox.setMaxLength(100);
            recurringRecipientBox.setHint(Component.translatable("gui.bank.player_name_hint"));
            addRenderableWidget(recurringRecipientBox);

            // Betrag (Label bei contentY + 59, Box bei contentY + 71)
            recurringAmountBox = new EditBox(this.font, leftPos + 15, contentY + 71, WIDTH - 30, 18, Component.translatable("gui.common.amount"));
            recurringAmountBox.setMaxLength(10);
            recurringAmountBox.setHint(Component.translatable("gui.common.amount_euro"));
            addRenderableWidget(recurringAmountBox);

            // Intervall-Button (Label bei contentY + 93, Button bei contentY + 105)
            recurringIntervalButton = addRenderableWidget(Button.builder(
                Component.literal(selectedInterval.getDisplayName()),
                button -> {
                    selectedInterval = selectedInterval.next();
                    recurringIntervalButton.setMessage(Component.literal(selectedInterval.getDisplayName()));
                }
            ).bounds(leftPos + 15, contentY + 105, WIDTH - 30, 18).build());

            // Erstellen-Button (bei contentY + 127)
            addRenderableWidget(Button.builder(Component.translatable("gui.bank.create_standing_order"), button -> {
                performCreateRecurringPayment();
            }).bounds(leftPos + 15, contentY + 127, WIDTH - 30, 20).build());
        }

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.translatable("gui.achievement_app.back"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());

        // Refresh-Button
        addRenderableWidget(Button.builder(Component.literal("↻"), button -> {
            refreshData();
            transferMessage = Component.translatable("message.bank.data_refreshed").getString();
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
            transferMessage = Component.translatable("message.bank.enter_recipient").getString();
            transferMessageColor = 0xFF5555;
            return;
        }

        if (amountStr.isEmpty()) {
            transferMessage = Component.translatable("message.bank.enter_amount").getString();
            transferMessageColor = 0xFF5555;
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            transferMessage = Component.translatable("message.bank.invalid_amount").getString();
            transferMessageColor = 0xFF5555;
            return;
        }

        if (amount <= 0) {
            transferMessage = Component.translatable("message.bank.amount_positive").getString();
            transferMessageColor = 0xFF5555;
            return;
        }

        if (amount > balance) {
            transferMessage = Component.translatable("message.bank.insufficient_funds").getString();
            transferMessageColor = 0xFF5555;
            return;
        }

        // Sende Überweisung an Server
        NPCNetworkHandler.sendToServer(new BankTransferPacket(recipient, amount));

        // Lokale Bestätigung (Server sendet detaillierte Nachricht)
        transferMessage = Component.translatable("message.bank.transfer_processing", recipient, amount).getString();
        transferMessageColor = 0x55FF55;

        // Felder leeren
        transferRecipientBox.setValue("");
        transferAmountBox.setValue("");

        // Daten aktualisieren nach kurzer Verzögerung
        refreshData();
    }

    /**
     * Erstellt Dauerauftrag
     */
    private void performCreateRecurringPayment() {
        if (recurringRecipientBox == null || recurringAmountBox == null) {
            return;
        }

        String recipient = recurringRecipientBox.getValue().trim();
        String amountStr = recurringAmountBox.getValue().trim();

        // Validierung: Empfänger
        if (recipient.isEmpty()) {
            transferMessage = Component.translatable("message.bank.enter_recipient").getString();
            transferMessageColor = 0xFF5555;
            return;
        }

        // Validierung: Betrag
        if (amountStr.isEmpty()) {
            transferMessage = Component.translatable("message.bank.enter_amount").getString();
            transferMessageColor = 0xFF5555;
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            transferMessage = Component.translatable("message.bank.invalid_amount").getString();
            transferMessageColor = 0xFF5555;
            return;
        }

        if (amount <= 0) {
            transferMessage = Component.translatable("message.bank.amount_positive").getString();
            transferMessageColor = 0xFF5555;
            return;
        }

        // Sende Packet an Server
        NPCNetworkHandler.sendToServer(new CreateRecurringPaymentPacket(recipient, amount, selectedInterval));

        // Erfolgs-Nachricht
        transferMessage = Component.translatable("message.bank.recurring_created",
            recipient, amount, selectedInterval.getDisplayName().toLowerCase()).getString();
        transferMessageColor = 0x55FF55;

        // Felder leeren
        recurringRecipientBox.setValue("");
        recurringAmountBox.setValue("");
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
        int currentX = leftPos + 5;
        for (int i = 0; i < getTabNames().length; i++) {
            int tabY = topPos + 30;
            if (i == currentTab) {
                guiGraphics.fill(currentX - 1, tabY - 1, currentX + TAB_WIDTHS[i] - 3, tabY + TAB_HEIGHT + 1, 0xFF4A90E2);
            }
            currentX += TAB_WIDTHS[i];
        }

        // Content-Bereich
        int contentY = topPos + 55;
        int contentEndY = topPos + HEIGHT - 40;

        // Render Tab-Content
        switch (currentTab) {
            case 0 -> renderAccountTab(guiGraphics, contentY, contentEndY);
            case 1 -> renderHistoryTab(guiGraphics, contentY, contentEndY);
            case 2 -> renderTransferTab(guiGraphics, contentY, contentEndY);
            case 3 -> renderDauerauftraegeTab(guiGraphics, contentY, contentEndY);
        }

        // Scroll-Indikator (nicht in Tab 2)
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

            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.bank.balance_title").getString(), leftPos + WIDTH / 2, y + 5, 0xFFFFFF);

            String balanceStr = String.format("§6§l%.2f€", balance);
            guiGraphics.drawCenteredString(this.font, balanceStr, leftPos + WIDTH / 2, y + 22, 0xFFAA00);

            String balanceColor = balance >= 0 ? "§a" : "§c";
            String balanceStatus = balance >= 0 ?
                Component.translatable("gui.app.bank.balance_positive").getString() :
                Component.translatable("gui.app.bank.balance_overdraft").getString();
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
            guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.statistics").getString(), leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        // Einnahmen
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.income").getString(), leftPos + 15, y, 0x55FF55);
            guiGraphics.drawString(this.font, String.format("§f+%.2f€", totalIncome), leftPos + 110, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        // Ausgaben
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.expenses").getString(), leftPos + 15, y, 0xFF5555);
            guiGraphics.drawString(this.font, String.format("§f-%.2f€", totalExpenses), leftPos + 110, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        // Bilanz
        double balance = totalIncome - totalExpenses;
        String balanceColor = balance >= 0 ? "§a" : "§c";
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.balance_calc").getString(), leftPos + 15, y, 0xFFFFFF);
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
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.bank.transactions_count").getString() + recentTransactions.size(), leftPos + WIDTH / 2, y, 0xAAAAAA);
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
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.bank.no_transactions").getString(), leftPos + WIDTH / 2, y + 20, 0xAAAAAA);
            maxScroll = 0;
            return;
        }

        // Header
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.transactions_header", recentTransactions.size()).getString(), leftPos + 15, y, 0xFFAA00);
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
        guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.available").getString(), leftPos + 15, startY, 0xAAAAAA);
        guiGraphics.drawString(this.font, String.format("§6%.2f€", balance), leftPos + 110, startY, 0xFFAA00);

        // Labels für Form-Felder (über den EditBoxen mit ausreichend Abstand)
        guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.recipient_name").getString(), leftPos + 15, startY + 13, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.amount_label").getString(), leftPos + 15, startY + 51, 0xFFFFFF);

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
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.bank.transfer_info").getString(), leftPos + WIDTH / 2, startY + 130, 0x666666);
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 4: DAUERAUFTRÄGE
    // ═══════════════════════════════════════════════════════════

    private void renderDauerauftraegeTab(GuiGraphics guiGraphics, int startY, int endY) {
        // ═══════════════════════════════════════════════════════════
        // FIXED SECTION: Form (kein Scrolling, bleibt mit EditBoxes aligned)
        // ═══════════════════════════════════════════════════════════

        // Überschrift
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.bank.recurring_title").getString(), leftPos + WIDTH / 2, startY, 0xFFAA00);

        // Limit-Anzeige (10er-Limit prüfen)
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null && mc.level.getServer() != null) {
            RecurringPaymentManager manager = RecurringPaymentManager.getInstance(mc.level.getServer());
            java.util.List<RecurringPayment> payments = manager.getPayments(mc.player.getUUID());
            int maxPerPlayer = ModConfigHandler.COMMON.RECURRING_MAX_PER_PLAYER.get();

            // Kredit zählt auch als Dauerauftrag
            CreditLoanManager loanManager = CreditLoanManager.getInstance(mc.level.getServer());
            int creditCount = loanManager.hasActiveLoan(mc.player.getUUID()) ? 1 : 0;
            int totalCount = payments.size() + creditCount;

            String limitStr = totalCount + "/" + maxPerPlayer;
            int limitColor = totalCount >= maxPerPlayer ? 0xFF5555 : 0x00AA00;
            guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.limit").getString() + limitStr, leftPos + WIDTH - 70, startY, limitColor, false);

            if (totalCount >= maxPerPlayer) {
                guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.max").getString(), leftPos + WIDTH - 70, startY + 10, 0xFF5555, false);
            }
        }

        // Neuer Dauerauftrag Überschrift
        guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.new_recurring").getString(), leftPos + 15, startY + 10, 0xFFFFFF);

        // Form Labels (direkt über den Input-Feldern - FIXED positions wie EditBoxes)
        guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.recipient_label").getString(), leftPos + 15, startY + 25, 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.amount_recurring_label").getString(), leftPos + 15, startY + 59, 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.interval_label").getString(), leftPos + 15, startY + 93, 0xAAAAAA);

        // Erfolgsmeldung (unter dem Erstellen-Button)
        if (!transferMessage.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, transferMessage, leftPos + WIDTH / 2, startY + 152, transferMessageColor);
        }

        // ═══════════════════════════════════════════════════════════
        // SCROLLABLE SECTION: Liste (scrollt, wenn zu viele Einträge)
        // ═══════════════════════════════════════════════════════════

        int listStartY = startY + 165;  // Start der scrollbaren Liste
        int y = listStartY - scrollOffset;
        int contentHeight = 0;

        // Trennlinie
        if (y >= listStartY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        // Aktive Daueraufträge Header
        if (y >= listStartY - 15 && y < endY) {
            guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.active_recurring").getString(), leftPos + 15, y, 0xFFFFFF);
        }
        y += 15;
        contentHeight += 15;

        // Liste anzeigen (wenn Server verfügbar)
        Minecraft mcList = Minecraft.getInstance();
        if (mcList.player != null && mcList.level != null && mcList.level.getServer() != null) {
            RecurringPaymentManager manager = RecurringPaymentManager.getInstance(mcList.level.getServer());
            java.util.List<RecurringPayment> payments = manager.getPayments(mcList.player.getUUID());

            // Kredit-Dauerauftrag anzeigen (wenn vorhanden)
            CreditLoanManager loanManager = CreditLoanManager.getInstance(mcList.level.getServer());
            CreditLoan activeLoan = loanManager.getLoan(mcList.player.getUUID());

            boolean hasAnyPayments = !payments.isEmpty() || activeLoan != null;

            if (!hasAnyPayments) {
                if (y >= listStartY - 15 && y < endY) {
                    guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.bank.no_recurring").getString(),
                        leftPos + WIDTH / 2, y, 0xAAAAAA);
                }
                y += 15;
                contentHeight += 15;
            } else {
                // Kredit-Dauerauftrag ZUERST anzeigen (als spezielle Zahlung)
                if (activeLoan != null) {
                    if (y >= listStartY - 50 && y < endY) {
                        // Kredit-Box (besondere Hervorhebung)
                        guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 45, 0x44AA6600);

                        // Kredit-Symbol und Typ
                        guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.credit_title").getString(), leftPos + 15, y + 3, 0xFFAA00);
                        guiGraphics.drawString(this.font, "§f" + activeLoan.getType().getDisplayNameDE(), leftPos + 80, y + 3, 0xFFFFFF);

                        // Tägliche Rate
                        String dailyStr = String.format("§e%.2f€", activeLoan.getDailyPayment());
                        guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.credit_daily").getString() + dailyStr, leftPos + 15, y + 15, 0xFFFFFF);

                        // Restbetrag und Fortschritt
                        String remainingStr = String.format("§c%.2f€", activeLoan.getRemaining());
                        guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.credit_remaining").getString() + remainingStr, leftPos + 15, y + 27, 0xFFFFFF);

                        // Fortschrittsbalken
                        int progress = activeLoan.getProgressPercent();
                        int barWidth = 60;
                        int filledWidth = (barWidth * progress) / 100;
                        guiGraphics.fill(leftPos + 145, y + 28, leftPos + 145 + barWidth, y + 34, 0xFF333333);
                        guiGraphics.fill(leftPos + 145, y + 28, leftPos + 145 + filledWidth, y + 34, 0xFF00AA00);
                        guiGraphics.drawString(this.font, "§a" + progress + "%", leftPos + 148, y + 28, 0x55FF55, false);
                    }
                    y += 50;
                    contentHeight += 50;
                }
                // Zeige ALLE Daueraufträge mit vollständigen Informationen
                for (RecurringPayment payment : payments) {
                    if (y >= listStartY - 50 && y < endY) {
                        // Box für jeden Eintrag
                        guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 45, 0x44333333);

                        // Empfänger (voller Name wenn möglich)
                        String recipientStr = payment.getToPlayer().toString();
                        if (recipientStr.length() > 30) {
                            recipientStr = recipientStr.substring(0, 27) + "...";
                        }
                        guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.payment_to").getString() + recipientStr, leftPos + 15, y + 3, 0xFFFFFF);

                        // Betrag
                        String amountStr = String.format("§6%.2f€", payment.getAmount());
                        guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.payment_amount").getString() + amountStr, leftPos + 15, y + 15, 0xFFFFFF);

                        // Intervall
                        String intervalName = RecurringPaymentInterval.fromDays(payment.getIntervalDays()).getDisplayName();
                        guiGraphics.drawString(this.font, Component.translatable("gui.app.bank.payment_interval").getString() + intervalName, leftPos + 15, y + 27, 0xFFFFFF);

                        // Status
                        String statusStr = payment.isActive() ?
                            Component.translatable("gui.app.bank.payment_status_active").getString() :
                            Component.translatable("gui.app.bank.payment_status_paused").getString();
                        guiGraphics.drawString(this.font, statusStr, leftPos + 145, y + 27, 0xFFFFFF);
                    }
                    y += 50;
                    contentHeight += 50;
                }
            }
        } else {
            if (y >= listStartY - 15 && y < endY) {
                guiGraphics.drawCenteredString(this.font, Component.translatable("gui.app.bank.no_recurring").getString(),
                    leftPos + WIDTH / 2, y, 0xAAAAAA);
            }
            y += 15;
            contentHeight += 15;
        }

        // MaxScroll: Nur basierend auf dem sichtbaren Bereich der Liste
        int visibleListHeight = endY - listStartY;
        maxScroll = Math.max(0, contentHeight - visibleListHeight);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true; // Consume event, prevent closing
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
