package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.CreditLoan;
import de.rolandsw.schedulemc.economy.CreditLoanManager;
import de.rolandsw.schedulemc.economy.RecurringPayment;
import de.rolandsw.schedulemc.economy.RecurringPaymentInterval;
import de.rolandsw.schedulemc.economy.RecurringPaymentManager;
import de.rolandsw.schedulemc.economy.SavingsAccount;
import de.rolandsw.schedulemc.economy.SavingsAccountManager;
import de.rolandsw.schedulemc.economy.Transaction;
import de.rolandsw.schedulemc.economy.TransactionHistory;
import de.rolandsw.schedulemc.economy.WalletManager;
import de.rolandsw.schedulemc.npc.bank.TransferLimitTracker;
import de.rolandsw.schedulemc.npc.menu.BankerMenu;
import de.rolandsw.schedulemc.npc.network.BankDepositPacket;
import de.rolandsw.schedulemc.npc.network.BankTransferPacket;
import de.rolandsw.schedulemc.npc.network.BankWithdrawPacket;
import de.rolandsw.schedulemc.npc.network.CreateRecurringPaymentPacket;
import de.rolandsw.schedulemc.npc.network.SavingsDepositPacket;
import de.rolandsw.schedulemc.npc.network.SavingsWithdrawPacket;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Vollständige Banker GUI mit 5-Tab-Navigation
 * Tab 1: Übersicht - Anzeige aller Konten
 * Tab 2: Girokonto - Einzahlen/Abheben für Girokonto
 * Tab 3: Sparkonto - Einzahlen/Abheben für Sparkonto
 * Tab 4: Überweisung - Transfer zwischen Spielern
 * Tab 5: Historie - Transaktionshistorie
 */
@OnlyIn(Dist.CLIENT)
public class BankerScreen extends AbstractContainerScreen<BankerMenu> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/gui/npc_interaction.png");

    private enum Tab {
        UEBERSICHT,
        GIROKONTO,
        SPARKONTO,
        UEBERWEISUNG,
        HISTORIE,
        DAUERAUFTRAEGE
    }

    private Tab currentTab = Tab.UEBERSICHT;

    // Tab Buttons
    private Button uebersichtTabButton;
    private Button girokontoTabButton;
    private Button sparkontoTabButton;
    private Button ueberweisungTabButton;
    private Button historieTabButton;
    private Button dauerauftraegeTabButton;

    // Girokonto Tab Components
    private Button giroDepositButton;
    private Button giroWithdrawButton;
    private EditBox giroDepositAmountInput;
    private EditBox giroWithdrawAmountInput;

    // Sparkonto Tab Components
    private Button savingsDepositButton;
    private Button savingsWithdrawButton;
    private EditBox savingsDepositAmountInput;
    private EditBox savingsWithdrawAmountInput;

    // Überweisung Tab Components
    private EditBox transferTargetInput;
    private EditBox transferAmountInput;
    private Button transferButton;

    // Historie Tab Components
    private int transactionScrollOffset = 0;
    private Button scrollUpButton;
    private Button scrollDownButton;

    // Daueraufträge Tab Components
    private EditBox recurringRecipientInput;
    private EditBox recurringAmountInput;
    private Button recurringIntervalButton;
    private Button recurringCreateButton;
    private RecurringPaymentInterval selectedInterval = RecurringPaymentInterval.MONTHLY;

    // Close Button
    private Button closeButton;

    public BankerScreen(BankerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 280;
        this.imageHeight = 230;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Tab Buttons (6 Tabs, volle Namen ohne Abkürzungen)
        int tabStartX = x + 5;
        int currentX = tabStartX;

        uebersichtTabButton = addRenderableWidget(Button.builder(Component.translatable("gui.bank.tab_overview"), button -> {
            switchTab(Tab.UEBERSICHT);
        }).bounds(currentX, y + 20, 40, 18).build());
        currentX += 41;

        girokontoTabButton = addRenderableWidget(Button.builder(Component.translatable("gui.bank.checking"), button -> {
            switchTab(Tab.GIROKONTO);
        }).bounds(currentX, y + 20, 40, 18).build());
        currentX += 41;

        sparkontoTabButton = addRenderableWidget(Button.builder(Component.translatable("gui.bank.savings"), button -> {
            switchTab(Tab.SPARKONTO);
        }).bounds(currentX, y + 20, 40, 18).build());
        currentX += 41;

        ueberweisungTabButton = addRenderableWidget(Button.builder(Component.translatable("gui.bank.tab_transfer"), button -> {
            switchTab(Tab.UEBERWEISUNG);
        }).bounds(currentX, y + 20, 50, 18).build());
        currentX += 51;

        historieTabButton = addRenderableWidget(Button.builder(Component.translatable("gui.bank.tab_history"), button -> {
            switchTab(Tab.HISTORIE);
        }).bounds(currentX, y + 20, 38, 18).build());
        currentX += 39;

        dauerauftraegeTabButton = addRenderableWidget(Button.builder(Component.translatable("gui.bank.standing_orders"), button -> {
            switchTab(Tab.DAUERAUFTRAEGE);
        }).bounds(currentX, y + 20, 62, 18).build());

        // Girokonto Tab Components
        giroDepositAmountInput = new EditBox(this.font, x + 15, y + 100, 90, 18, Component.translatable("gui.common.amount"));
        giroDepositAmountInput.setMaxLength(10);
        giroDepositAmountInput.setValue("1000");
        addRenderableWidget(giroDepositAmountInput);

        giroDepositButton = addRenderableWidget(Button.builder(Component.translatable("gui.bank.deposit"), button -> {
            handleGiroDeposit();
        }).bounds(x + 110, y + 100, 95, 18).build());

        giroWithdrawAmountInput = new EditBox(this.font, x + 15, y + 140, 90, 18, Component.translatable("gui.common.amount"));
        giroWithdrawAmountInput.setMaxLength(10);
        giroWithdrawAmountInput.setValue("500");
        addRenderableWidget(giroWithdrawAmountInput);

        giroWithdrawButton = addRenderableWidget(Button.builder(Component.translatable("gui.bank.withdraw"), button -> {
            handleGiroWithdraw();
        }).bounds(x + 110, y + 140, 95, 18).build());

        // Sparkonto Tab Components
        savingsDepositAmountInput = new EditBox(this.font, x + 15, y + 100, 90, 18, Component.translatable("gui.common.amount"));
        savingsDepositAmountInput.setMaxLength(10);
        savingsDepositAmountInput.setValue("1000");
        addRenderableWidget(savingsDepositAmountInput);

        savingsDepositButton = addRenderableWidget(Button.builder(Component.translatable("gui.bank.deposit"), button -> {
            handleSavingsDeposit();
        }).bounds(x + 110, y + 100, 95, 18).build());

        savingsWithdrawAmountInput = new EditBox(this.font, x + 15, y + 140, 90, 18, Component.translatable("gui.common.amount"));
        savingsWithdrawAmountInput.setMaxLength(10);
        savingsWithdrawAmountInput.setValue("500");
        addRenderableWidget(savingsWithdrawAmountInput);

        savingsWithdrawButton = addRenderableWidget(Button.builder(Component.translatable("gui.bank.withdraw"), button -> {
            handleSavingsWithdraw();
        }).bounds(x + 110, y + 140, 95, 18).build());

        // Überweisung Tab Components
        transferTargetInput = new EditBox(this.font, x + 15, y + 70, 190, 18, Component.translatable("gui.common.player_name"));
        transferTargetInput.setMaxLength(16);
        transferTargetInput.setValue("");
        addRenderableWidget(transferTargetInput);

        transferAmountInput = new EditBox(this.font, x + 15, y + 108, 90, 18, Component.translatable("gui.common.amount"));
        transferAmountInput.setMaxLength(10);
        transferAmountInput.setValue("100");
        addRenderableWidget(transferAmountInput);

        transferButton = addRenderableWidget(Button.builder(Component.translatable("gui.bank.transfer_button"), button -> {
            handleTransfer();
        }).bounds(x + 15, y + 135, 190, 20).build());

        // Historie Tab Components
        scrollUpButton = addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
            if (transactionScrollOffset > 0) {
                transactionScrollOffset--;
            }
        }).bounds(x + 200, y + 45, 15, 18).build());

        scrollDownButton = addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
            transactionScrollOffset++;
        }).bounds(x + 200, y + 155, 15, 18).build());

        // Daueraufträge Tab Components
        recurringRecipientInput = new EditBox(this.font, x + 15, y + 85, 120, 18, Component.translatable("message.bank.recipient"));
        recurringRecipientInput.setMaxLength(16);
        recurringRecipientInput.setValue("");
        addRenderableWidget(recurringRecipientInput);

        recurringAmountInput = new EditBox(this.font, x + 15, y + 115, 80, 18, Component.translatable("gui.common.amount"));
        recurringAmountInput.setMaxLength(10);
        recurringAmountInput.setValue("");
        addRenderableWidget(recurringAmountInput);

        recurringIntervalButton = addRenderableWidget(Button.builder(
            Component.literal(selectedInterval.getDisplayName()), button -> {
                selectedInterval = selectedInterval.next();
                recurringIntervalButton.setMessage(Component.literal(selectedInterval.getDisplayName()));
            }
        ).bounds(x + 15, y + 145, 120, 18).build());

        recurringCreateButton = addRenderableWidget(Button.builder(Component.translatable("gui.bank.create"), button -> {
            handleRecurringCreate();
        }).bounds(x + 145, y + 145, 120, 18).build());

        // Close Button
        closeButton = addRenderableWidget(Button.builder(Component.translatable("gui.common.close"), button -> {
            this.onClose();
        }).bounds(x + 60, y + 205, 100, 20).build());

        // Initial tab
        updateComponentVisibility();
    }

    /**
     * Wechselt zwischen Tabs
     */
    private void switchTab(Tab tab) {
        this.currentTab = tab;
        this.transactionScrollOffset = 0;
        updateComponentVisibility();
    }

    /**
     * Aktualisiert Sichtbarkeit der Tab-spezifischen Components
     */
    private void updateComponentVisibility() {
        boolean isGirokonto = currentTab == Tab.GIROKONTO;
        boolean isSparkonto = currentTab == Tab.SPARKONTO;
        boolean isUeberweisung = currentTab == Tab.UEBERWEISUNG;
        boolean isHistorie = currentTab == Tab.HISTORIE;
        boolean isDauerauftraege = currentTab == Tab.DAUERAUFTRAEGE;

        // Girokonto Tab
        giroDepositAmountInput.visible = isGirokonto;
        giroDepositButton.visible = isGirokonto;
        giroWithdrawAmountInput.visible = isGirokonto;
        giroWithdrawButton.visible = isGirokonto;

        // Sparkonto Tab
        savingsDepositAmountInput.visible = isSparkonto;
        savingsDepositButton.visible = isSparkonto;
        savingsWithdrawAmountInput.visible = isSparkonto;
        savingsWithdrawButton.visible = isSparkonto;

        // Überweisung Tab
        transferTargetInput.visible = isUeberweisung;
        transferAmountInput.visible = isUeberweisung;
        transferButton.visible = isUeberweisung;

        // Historie Tab
        scrollUpButton.visible = isHistorie;
        scrollDownButton.visible = isHistorie;

        // Daueraufträge Tab
        recurringRecipientInput.visible = isDauerauftraege;
        recurringAmountInput.visible = isDauerauftraege;
        recurringIntervalButton.visible = isDauerauftraege;
        recurringCreateButton.visible = isDauerauftraege;
    }

    /**
     * Verarbeitet Einzahlung auf Girokonto
     */
    private void handleGiroDeposit() {
        try {
            double amount = Double.parseDouble(giroDepositAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new BankDepositPacket(amount));
                this.onClose();
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }

    /**
     * Verarbeitet Abhebung vom Girokonto
     */
    private void handleGiroWithdraw() {
        try {
            double amount = Double.parseDouble(giroWithdrawAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new BankWithdrawPacket(amount));
                this.onClose();
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }

    /**
     * Verarbeitet Einzahlung auf Sparkonto
     */
    private void handleSavingsDeposit() {
        try {
            double amount = Double.parseDouble(savingsDepositAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new SavingsDepositPacket(amount));
                this.onClose();
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }

    /**
     * Verarbeitet Abhebung vom Sparkonto
     */
    private void handleSavingsWithdraw() {
        try {
            double amount = Double.parseDouble(savingsWithdrawAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new SavingsWithdrawPacket(amount));
                this.onClose();
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }

    /**
     * Verarbeitet Überweisung
     */
    private void handleTransfer() {
        String target = transferTargetInput.getValue();
        if (target == null || target.trim().isEmpty()) {
            return;
        }

        try {
            double amount = Double.parseDouble(transferAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new BankTransferPacket(target, amount));
                this.onClose();
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }

    /**
     * Verarbeitet Dauerauftrag-Erstellung
     */
    private void handleRecurringCreate() {
        String recipient = recurringRecipientInput.getValue();
        if (recipient == null || recipient.trim().isEmpty()) {
            return;
        }

        try {
            double amount = Double.parseDouble(recurringAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new CreateRecurringPaymentPacket(recipient, amount, selectedInterval));
                this.onClose();
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Render current tab content
        switch (currentTab) {
            case UEBERSICHT:
                renderUebersichtTab(guiGraphics, x, y);
                break;
            case GIROKONTO:
                renderGirokontoTab(guiGraphics, x, y);
                break;
            case SPARKONTO:
                renderSparkontoTab(guiGraphics, x, y);
                break;
            case UEBERWEISUNG:
                renderUeberweisungTab(guiGraphics, x, y);
                break;
            case HISTORIE:
                renderHistorieTab(guiGraphics, x, y);
                break;
            case DAUERAUFTRAEGE:
                renderDauerauftraegeTab(guiGraphics, x, y);
                break;
        }
    }

    /**
     * Rendert Übersicht Tab (nur Anzeige)
     */
    private void renderUebersichtTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null) return;

        guiGraphics.drawString(this.font, Component.translatable("gui.bank.overview_title").getString(), x + 55, y + 45, 0x404040, false);

        // Bargeld (Wallet)
        double bargeld = WalletManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.cash").getString(), x + 30, y + 65, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", bargeld), x + 125, y + 65, 0xFFAA00, false);

        // Girokonto
        double girokonto = EconomyManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.checking_balance").getString(), x + 30, y + 85, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", girokonto), x + 125, y + 85, 0x00AA00, false);

        // Sparkonto
        SavingsAccountManager savingsManager = SavingsAccountManager.getInstance(minecraft.level.getServer());
        List<SavingsAccount> savingsAccounts = savingsManager.getAccounts(minecraft.player.getUUID());
        double sparkonto = savingsAccounts.stream()
            .mapToDouble(SavingsAccount::getBalance)
            .sum();
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.savings").getString(), x + 30, y + 105, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", sparkonto), x + 125, y + 105, 0x6666FF, false);

        // Trennlinie
        guiGraphics.fill(x + 20, y + 120, x + 200, y + 121, 0x44FFFFFF);

        // Gesamt
        double gesamt = bargeld + girokonto + sparkonto;
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.total").getString(), x + 30, y + 130, 0x404040, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", gesamt), x + 125, y + 130, 0xFFD700, false);
    }

    /**
     * Rendert Girokonto Tab
     */
    private void renderGirokontoTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null) return;

        guiGraphics.drawString(this.font, Component.translatable("gui.bank.checking_title").getString(), x + 75, y + 45, 0x404040, false);

        // Kontostand
        double girokonto = EconomyManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.balance").getString(), x + 20, y + 60, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", girokonto), x + 125, y + 60, 0x00AA00, false);

        // Bargeld
        double bargeld = WalletManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.cash_balance").getString(), x + 20, y + 72, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", bargeld), x + 125, y + 72, 0xFFAA00, false);

        // Einzahlen Label
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.deposit_from_cash").getString(), x + 15, y + 85, 0x808080, false);

        // Abheben Label
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.withdraw_to_cash").getString(), x + 15, y + 128, 0x808080, false);

        // Info
        double depositLimit = ModConfigHandler.COMMON.BANK_DEPOSIT_LIMIT.get();
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.limit").getString() + String.format("%.0f€", depositLimit),
            x + 15, y + 170, 0x606060, false);
    }

    /**
     * Rendert Sparkonto Tab
     */
    private void renderSparkontoTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null) return;

        guiGraphics.drawString(this.font, Component.translatable("gui.bank.savings_title").getString(), x + 75, y + 45, 0x404040, false);

        // Sparkonto Stand
        SavingsAccountManager savingsManager = SavingsAccountManager.getInstance(minecraft.level.getServer());
        List<SavingsAccount> savingsAccounts = savingsManager.getAccounts(minecraft.player.getUUID());
        double sparkonto = savingsAccounts.stream()
            .mapToDouble(SavingsAccount::getBalance)
            .sum();
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.balance").getString(), x + 20, y + 60, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", sparkonto), x + 125, y + 60, 0x6666FF, false);

        // Girokonto
        double girokonto = EconomyManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.checking_balance").getString(), x + 20, y + 72, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", girokonto), x + 125, y + 72, 0x00AA00, false);

        // Einzahlen Label
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.deposit_from_checking").getString(), x + 15, y + 85, 0x808080, false);

        // Abheben Label
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.withdraw_to_checking").getString(), x + 15, y + 128, 0x808080, false);

        // Info
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.interest").getString(), x + 15, y + 170, 0x606060, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.minimum").getString(), x + 15, y + 182, 0x606060, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.lock_period").getString(), x + 15, y + 194, 0x606060, false);
    }

    /**
     * Rendert Überweisung Tab
     */
    private void renderUeberweisungTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null) return;

        guiGraphics.drawString(this.font, Component.translatable("gui.bank.transfer_title").getString(), x + 70, y + 45, 0x404040, false);

        // Empfänger Label
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.recipient_name").getString(), x + 15, y + 58, 0x808080, false);

        // Betrag Label
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.amount_label").getString(), x + 15, y + 96, 0x808080, false);

        // Info unten
        if (minecraft.level.getServer() != null) {
            double balance = EconomyManager.getBalance(minecraft.player.getUUID());
            guiGraphics.drawString(this.font, Component.translatable("gui.bank.available_balance").getString(),
                x + 15, y + 145, 0x606060, false);
            guiGraphics.drawString(this.font, String.format("%.2f€", balance),
                x + 140, y + 145, 0xFFD700, false);

            TransferLimitTracker tracker = TransferLimitTracker.getInstance(minecraft.level.getServer());
            double remaining = tracker.getRemainingLimit(minecraft.player.getUUID());
            guiGraphics.drawString(this.font, Component.translatable("gui.bank.daily_limit").getString(),
                x + 15, y + 157, 0x606060, false);
            guiGraphics.drawString(this.font, String.format("%.2f€", remaining),
                x + 140, y + 157, remaining > 0 ? 0x00AA00 : 0xFF5555, false);
        }
    }

    /**
     * Rendert Historie Tab
     */
    private void renderHistorieTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null || minecraft.level.getServer() == null) return;

        guiGraphics.drawString(this.font, Component.translatable("gui.bank.history_title").getString(), x + 65, y + 45, 0x404040, false);

        // Get transaction history
        TransactionHistory history = TransactionHistory.getInstance(minecraft.level.getServer());
        List<Transaction> transactions = history.getAllTransactions(minecraft.player.getUUID());

        if (transactions.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable("gui.bank.no_transactions").getString(), x + 50, y + 80, 0x808080, false);
            return;
        }

        // Display transactions (scrollable)
        int maxDisplay = 8;
        int startIndex = Math.min(transactionScrollOffset, Math.max(0, transactions.size() - maxDisplay));
        int endIndex = Math.min(startIndex + maxDisplay, transactions.size());

        int yOffset = y + 65;
        for (int i = startIndex; i < endIndex; i++) {
            Transaction tx = transactions.get(i);

            // Typ
            String typeStr = tx.getType().getDisplayName();
            if (typeStr.length() > 12) {
                typeStr = typeStr.substring(0, 11) + ".";
            }
            guiGraphics.drawString(this.font, typeStr, x + 15, yOffset, 0x404040, false);

            // Betrag (+ grün, - rot)
            String amountStr = String.format("%+.0f€", tx.getAmount());
            int color = tx.getAmount() >= 0 ? 0x00AA00 : 0xFF5555;
            guiGraphics.drawString(this.font, amountStr, x + 100, yOffset, color, false);

            // Balance after
            String balanceStr = String.format("%.0f€", tx.getBalanceAfter());
            guiGraphics.drawString(this.font, balanceStr, x + 155, yOffset, 0x808080, false);

            yOffset += 12;
        }

        // Scroll indicator
        if (transactions.size() > maxDisplay) {
            guiGraphics.drawString(this.font,
                String.format("%d/%d", startIndex + 1, transactions.size()),
                x + 165, y + 160, 0x808080, false);
        }
    }

    /**
     * Rendert Daueraufträge Tab
     */
    private void renderDauerauftraegeTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null) return;

        guiGraphics.drawString(this.font, Component.translatable("gui.bank.standing_orders_title").getString(), x + 85, y + 45, 0x404040, false);

        // Neuer Dauerauftrag erstellen
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.new_standing_order").getString(), x + 15, y + 60, 0x606060, false);

        // Empfänger Label (12 Pixel über Input bei y+85)
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.recipient").getString(), x + 15, y + 73, 0x808080, false);

        // Betrag Label (12 Pixel über Input bei y+115)
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.amount_label").getString(), x + 15, y + 103, 0x808080, false);

        // Intervall Label (12 Pixel über Button bei y+145)
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.interval").getString(), x + 15, y + 133, 0x808080, false);

        // Limit-Anzeige (10er-Limit prüfen)
        if (minecraft.level.getServer() != null) {
            RecurringPaymentManager manager = RecurringPaymentManager.getInstance(minecraft.level.getServer());
            java.util.List<RecurringPayment> payments = manager.getPayments(minecraft.player.getUUID());
            int maxPerPlayer = ModConfigHandler.COMMON.RECURRING_MAX_PER_PLAYER.get();

            String limitStr = payments.size() + "/" + maxPerPlayer;
            int limitColor = payments.size() >= maxPerPlayer ? 0xFF5555 : 0x00AA00;
            guiGraphics.drawString(this.font, Component.translatable("gui.bank.limit").getString() + limitStr, x + 200, y + 60, limitColor, false);

            if (payments.size() >= maxPerPlayer) {
                guiGraphics.drawString(this.font, "MAX!", x + 200, y + 73, 0xFF5555, false);
            }
        }

        // Trennlinie
        guiGraphics.fill(x + 15, y + 168, x + 265, y + 169, 0x44FFFFFF);

        // Aktive Daueraufträge Header
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.active_orders").getString(), x + 15, y + 173, 0x606060, false);

        int yOffset = y + 186;

        // Kredit-Ratenzahlungen anzeigen (falls vorhanden)
        if (minecraft.level.getServer() != null) {
            CreditLoanManager loanManager = CreditLoanManager.getInstance(minecraft.level.getServer());
            CreditLoan activeLoan = loanManager.getLoan(minecraft.player.getUUID());

            if (activeLoan != null) {
                // Kredit-Dauerauftrag Box
                guiGraphics.fill(x + 15, yOffset - 2, x + 260, yOffset + 10, 0x44004400);

                guiGraphics.drawString(this.font, Component.translatable("gui.bank.credit_payment").getString(), x + 18, yOffset, 0xFFD700, false);
                guiGraphics.drawString(this.font,
                    String.format("-%.0f€", activeLoan.getDailyPayment()),
                    x + 90, yOffset, 0xFF5555, false);
                guiGraphics.drawString(this.font, Component.translatable("gui.bank.interval_daily").getString(), x + 145, yOffset, 0x00AAAA, false);
                guiGraphics.drawString(this.font, "✓", x + 195, yOffset, 0x00FF00, false);
                guiGraphics.drawString(this.font,
                    activeLoan.getType().name().substring(0, 3),
                    x + 210, yOffset, 0x808080, false);

                yOffset += 13;
            }
        }

        // Normale Daueraufträge anzeigen
        if (minecraft.level.getServer() != null) {
            RecurringPaymentManager manager = RecurringPaymentManager.getInstance(minecraft.level.getServer());
            java.util.List<RecurringPayment> payments = manager.getPayments(minecraft.player.getUUID());

            if (payments.isEmpty() && (minecraft.level.getServer() == null ||
                CreditLoanManager.getInstance(minecraft.level.getServer()).getLoan(minecraft.player.getUUID()) == null)) {
                guiGraphics.drawString(this.font, Component.translatable("gui.bank.no_standing_orders").getString(), x + 65, yOffset, 0x808080, false);
            } else {
                // Zeige Daueraufträge an
                int remainingSpace = (y + 200) - yOffset;
                int maxDisplay = Math.min(remainingSpace / 11, payments.size());
                maxDisplay = Math.min(maxDisplay, 2); // Max 2 normale wenn Kredit vorhanden

                for (int i = 0; i < maxDisplay; i++) {
                    RecurringPayment payment = payments.get(i);

                    // Empfänger Name (gekürzt falls zu lang)
                    String recipientStr = payment.getToPlayer().toString().substring(0, 8);
                    guiGraphics.drawString(this.font, recipientStr + "...", x + 18, yOffset, 0xFFFFFF, false);

                    // Betrag
                    String amountStr = String.format("%.0f€", payment.getAmount());
                    guiGraphics.drawString(this.font, amountStr, x + 90, yOffset, 0xFFAA00, false);

                    // Intervall
                    String intervalStr = payment.getIntervalDays() + "d";
                    guiGraphics.drawString(this.font, intervalStr, x + 145, yOffset, 0x00AAAA, false);

                    // Status
                    String statusStr = payment.isActive() ? "✓" : "⏸";
                    int statusColor = payment.isActive() ? 0x00FF00 : 0xFFAA00;
                    guiGraphics.drawString(this.font, statusStr, x + 195, yOffset, statusColor, false);

                    // ID
                    String idStr = payment.getPaymentId().substring(0, 4);
                    guiGraphics.drawString(this.font, "#" + idStr, x + 210, yOffset, 0x808080, false);

                    yOffset += 11;
                }

                // Hinweis bei mehr Einträgen
                int totalDisplayed = maxDisplay +
                    (CreditLoanManager.getInstance(minecraft.level.getServer()).getLoan(minecraft.player.getUUID()) != null ? 1 : 0);
                int totalEntries = payments.size() +
                    (CreditLoanManager.getInstance(minecraft.level.getServer()).getLoan(minecraft.player.getUUID()) != null ? 1 : 0);

                if (totalEntries > totalDisplayed) {
                    guiGraphics.drawString(this.font,
                        "+" + (totalEntries - totalDisplayed) + " weitere...",
                        x + 85, yOffset, 0x808080, false);
                }
            }
        }
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
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, Component.translatable("gui.bank.banker").getString(), 8, 6, 0x404040, false);
    }
}
