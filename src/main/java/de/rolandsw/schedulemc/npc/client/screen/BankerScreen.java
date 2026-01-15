package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.RecurringPaymentInterval;
import de.rolandsw.schedulemc.economy.Transaction;
import de.rolandsw.schedulemc.economy.network.ClientBankDataCache;
import de.rolandsw.schedulemc.economy.network.RequestBankDataPacket;
import de.rolandsw.schedulemc.npc.menu.BankerMenu;
import de.rolandsw.schedulemc.npc.network.BankDepositPacket;
import de.rolandsw.schedulemc.npc.network.BankTransferPacket;
import de.rolandsw.schedulemc.npc.network.BankWithdrawPacket;
import de.rolandsw.schedulemc.npc.network.CreateRecurringPaymentPacket;
import de.rolandsw.schedulemc.npc.network.SavingsDepositPacket;
import de.rolandsw.schedulemc.npc.network.SavingsWithdrawPacket;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
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
 * Vollständige Banker GUI mit 6-Tab-Navigation
 * FIXED: Verwendet ClientBankDataCache statt Server-seitige Manager
 *
 * Tab 1: Übersicht - Anzeige aller Konten
 * Tab 2: Girokonto - Einzahlen/Abheben für Girokonto
 * Tab 3: Sparkonto - Einzahlen/Abheben für Sparkonto
 * Tab 4: Überweisung - Transfer zwischen Spielern
 * Tab 5: Historie - Transaktionshistorie
 * Tab 6: Daueraufträge - Ständige Zahlungsaufträge
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

        // Request bank data from server
        NPCNetworkHandler.sendToServer(new RequestBankDataPacket());

        // Tab Buttons (6 Tabs)
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

        // Close Button (rechts unten)
        closeButton = addRenderableWidget(Button.builder(Component.translatable("gui.common.close"), button -> {
            this.onClose();
        }).bounds(x + 210, y + 205, 60, 20).build());

        // Initial tab
        updateComponentVisibility();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        // Data is automatically updated via ClientBankDataCache from server packets
    }

    private void switchTab(Tab tab) {
        this.currentTab = tab;
        this.transactionScrollOffset = 0;
        updateComponentVisibility();
    }

    private void updateComponentVisibility() {
        boolean isGirokonto = currentTab == Tab.GIROKONTO;
        boolean isSparkonto = currentTab == Tab.SPARKONTO;
        boolean isUeberweisung = currentTab == Tab.UEBERWEISUNG;
        boolean isHistorie = currentTab == Tab.HISTORIE;
        boolean isDauerauftraege = currentTab == Tab.DAUERAUFTRAEGE;

        giroDepositAmountInput.visible = isGirokonto;
        giroDepositButton.visible = isGirokonto;
        giroWithdrawAmountInput.visible = isGirokonto;
        giroWithdrawButton.visible = isGirokonto;

        savingsDepositAmountInput.visible = isSparkonto;
        savingsDepositButton.visible = isSparkonto;
        savingsWithdrawAmountInput.visible = isSparkonto;
        savingsWithdrawButton.visible = isSparkonto;

        transferTargetInput.visible = isUeberweisung;
        transferAmountInput.visible = isUeberweisung;
        transferButton.visible = isUeberweisung;

        scrollUpButton.visible = isHistorie;
        scrollDownButton.visible = isHistorie;

        recurringRecipientInput.visible = isDauerauftraege;
        recurringAmountInput.visible = isDauerauftraege;
        recurringIntervalButton.visible = isDauerauftraege;
        recurringCreateButton.visible = isDauerauftraege;
    }

    private void handleGiroDeposit() {
        try {
            double amount = Double.parseDouble(giroDepositAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new BankDepositPacket(amount));
                // GUI bleibt offen
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    private void handleGiroWithdraw() {
        try {
            double amount = Double.parseDouble(giroWithdrawAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new BankWithdrawPacket(amount));
                // GUI bleibt offen
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    private void handleSavingsDeposit() {
        try {
            double amount = Double.parseDouble(savingsDepositAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new SavingsDepositPacket(amount));
                // GUI bleibt offen
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    private void handleSavingsWithdraw() {
        try {
            double amount = Double.parseDouble(savingsWithdrawAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new SavingsWithdrawPacket(amount));
                // GUI bleibt offen
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    private void handleTransfer() {
        String target = transferTargetInput.getValue();
        if (target == null || target.trim().isEmpty()) return;

        try {
            double amount = Double.parseDouble(transferAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new BankTransferPacket(target, amount));
                // GUI bleibt offen
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    private void handleRecurringCreate() {
        String recipient = recurringRecipientInput.getValue();
        if (recipient == null || recipient.trim().isEmpty()) return;

        try {
            double amount = Double.parseDouble(recurringAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new CreateRecurringPaymentPacket(recipient, amount, selectedInterval));
                // GUI bleibt offen
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        switch (currentTab) {
            case UEBERSICHT -> renderUebersichtTab(guiGraphics, x, y);
            case GIROKONTO -> renderGirokontoTab(guiGraphics, x, y);
            case SPARKONTO -> renderSparkontoTab(guiGraphics, x, y);
            case UEBERWEISUNG -> renderUeberweisungTab(guiGraphics, x, y);
            case HISTORIE -> renderHistorieTab(guiGraphics, x, y);
            case DAUERAUFTRAEGE -> renderDauerauftraegeTab(guiGraphics, x, y);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Tab Renderers - ALL use ClientBankDataCache instead of server managers
    // ═══════════════════════════════════════════════════════════════════════════

    private void renderUebersichtTab(GuiGraphics g, int x, int y) {
        g.drawString(font, Component.translatable("gui.bank.overview_title").getString(), x + 55, y + 45, 0x404040, false);

        double bargeld = ClientBankDataCache.getWalletBalance();
        g.drawString(font, Component.translatable("gui.bank.cash").getString(), x + 30, y + 65, 0x808080, false);
        g.drawString(font, String.format("%.2f€", bargeld), x + 125, y + 65, 0xFFAA00, false);

        double girokonto = ClientBankDataCache.getBalance();
        g.drawString(font, Component.translatable("gui.bank.checking_balance").getString(), x + 30, y + 85, 0x808080, false);
        g.drawString(font, String.format("%.2f€", girokonto), x + 125, y + 85, 0x00AA00, false);

        double sparkonto = ClientBankDataCache.getSavingsBalance();
        g.drawString(font, Component.translatable("gui.bank.savings").getString(), x + 30, y + 105, 0x808080, false);
        g.drawString(font, String.format("%.2f€", sparkonto), x + 125, y + 105, 0x6666FF, false);

        g.fill(x + 20, y + 120, x + 200, y + 121, 0x44FFFFFF);

        double gesamt = bargeld + girokonto + sparkonto;
        g.drawString(font, Component.translatable("gui.bank.total").getString(), x + 30, y + 130, 0x404040, false);
        g.drawString(font, String.format("%.2f€", gesamt), x + 125, y + 130, 0xFFD700, false);

        if (!ClientBankDataCache.hasData()) {
            g.drawString(font, "§7Lade Daten...", x + 30, y + 150, 0x808080, false);
        }
    }

    private void renderGirokontoTab(GuiGraphics g, int x, int y) {
        g.drawString(font, Component.translatable("gui.bank.checking_title").getString(), x + 75, y + 45, 0x404040, false);

        double girokonto = ClientBankDataCache.getBalance();
        g.drawString(font, Component.translatable("gui.bank.balance").getString(), x + 20, y + 60, 0x808080, false);
        g.drawString(font, String.format("%.2f€", girokonto), x + 125, y + 60, 0x00AA00, false);

        double bargeld = ClientBankDataCache.getWalletBalance();
        g.drawString(font, Component.translatable("gui.bank.cash_balance").getString(), x + 20, y + 72, 0x808080, false);
        g.drawString(font, String.format("%.2f€", bargeld), x + 125, y + 72, 0xFFAA00, false);

        g.drawString(font, Component.translatable("gui.bank.deposit_from_cash").getString(), x + 15, y + 85, 0x808080, false);
        g.drawString(font, Component.translatable("gui.bank.withdraw_to_cash").getString(), x + 15, y + 128, 0x808080, false);

        double depositLimit = ModConfigHandler.COMMON.BANK_DEPOSIT_LIMIT.get();
        g.drawString(font, Component.translatable("gui.bank.limit").getString() + String.format("%.0f€", depositLimit), x + 15, y + 170, 0x606060, false);

        // DISPO-ANZEIGE
        if (ClientBankDataCache.isOverdrawn()) {
            double overdraft = ClientBankDataCache.getOverdraftAmount();
            int daysPassed = ClientBankDataCache.getDebtDaysPassed();
            int daysUntilPrison = ClientBankDataCache.getDaysUntilPrison();
            int daysUntilAutoRepay = ClientBankDataCache.getDaysUntilAutoRepay();
            double prisonMinutes = ClientBankDataCache.getPotentialPrisonMinutes();

            // Warnung: KONTO ÜBERZOGEN!
            g.drawString(font, "§c§l⚠ KONTO ÜBERZOGEN!", x + 20, y + 185, 0xFF5555, false);
            g.drawString(font, String.format("§cSchulden: %.2f€", overdraft), x + 20, y + 197, 0xFF5555, false);

            // Phase-abhängige Anzeige
            if (daysPassed < 7) {
                // Tag 1-6: Countdown zu Auto-Repay
                g.drawString(font, String.format("§eAuto-Ausgleich in %d Tagen", daysUntilAutoRepay), x + 20, y + 209, 0xFFAA00, false);
            } else if (daysPassed >= 7 && daysUntilPrison > 0) {
                // Tag 7-27: Countdown zu Gefängnis
                g.drawString(font, String.format("§cGefängnis in %d Tagen!", daysUntilPrison), x + 20, y + 209, 0xFF0000, false);
                g.drawString(font, String.format("§cStrafe: %.1f Minuten", prisonMinutes), x + 20, y + 221, 0xFF5555, false);
            } else if (daysUntilPrison == 0) {
                // Tag 28+: KRITISCH!
                g.drawString(font, "§4§lAB INS GEFÄNGNIS!", x + 20, y + 209, 0xAA0000, false);
            }
        }
    }

    private void renderSparkontoTab(GuiGraphics g, int x, int y) {
        g.drawString(font, Component.translatable("gui.bank.savings_title").getString(), x + 75, y + 45, 0x404040, false);

        double sparkonto = ClientBankDataCache.getSavingsBalance();
        g.drawString(font, Component.translatable("gui.bank.balance").getString(), x + 20, y + 60, 0x808080, false);
        g.drawString(font, String.format("%.2f€", sparkonto), x + 125, y + 60, 0x6666FF, false);

        double girokonto = ClientBankDataCache.getBalance();
        g.drawString(font, Component.translatable("gui.bank.checking_balance").getString(), x + 20, y + 72, 0x808080, false);
        g.drawString(font, String.format("%.2f€", girokonto), x + 125, y + 72, 0x00AA00, false);

        g.drawString(font, Component.translatable("gui.bank.deposit_from_checking").getString(), x + 15, y + 85, 0x808080, false);
        g.drawString(font, Component.translatable("gui.bank.withdraw_to_checking").getString(), x + 15, y + 128, 0x808080, false);

        g.drawString(font, Component.translatable("gui.bank.interest").getString(), x + 15, y + 170, 0x606060, false);
        g.drawString(font, Component.translatable("gui.bank.minimum").getString(), x + 15, y + 182, 0x606060, false);
        g.drawString(font, Component.translatable("gui.bank.lock_period").getString(), x + 15, y + 194, 0x606060, false);
    }

    private void renderUeberweisungTab(GuiGraphics g, int x, int y) {
        g.drawString(font, Component.translatable("gui.bank.transfer_title").getString(), x + 70, y + 45, 0x404040, false);
        g.drawString(font, Component.translatable("gui.bank.recipient_name").getString(), x + 15, y + 58, 0x808080, false);
        g.drawString(font, Component.translatable("gui.bank.amount_label").getString(), x + 15, y + 96, 0x808080, false);

        double balance = ClientBankDataCache.getBalance();
        g.drawString(font, Component.translatable("gui.bank.available_balance").getString(), x + 15, y + 160, 0x606060, false);
        g.drawString(font, String.format("%.2f€", balance), x + 140, y + 160, 0xFFD700, false);

        double remaining = ClientBankDataCache.getRemainingTransferLimit();
        g.drawString(font, Component.translatable("gui.bank.daily_limit").getString(), x + 15, y + 172, 0x606060, false);
        g.drawString(font, String.format("%.2f€", remaining), x + 140, y + 172, remaining > 0 ? 0x00AA00 : 0xFF5555, false);
    }

    private void renderHistorieTab(GuiGraphics g, int x, int y) {
        g.drawString(font, Component.translatable("gui.bank.history_title").getString(), x + 65, y + 45, 0x404040, false);

        List<Transaction> transactions = ClientBankDataCache.getTransactions();

        if (transactions.isEmpty()) {
            String msg = !ClientBankDataCache.hasData() ? "§7Lade Transaktionen..." : Component.translatable("gui.bank.no_transactions").getString();
            g.drawString(font, msg, x + 50, y + 80, 0x808080, false);
            return;
        }

        int maxDisplay = 8;
        int startIndex = Math.min(transactionScrollOffset, Math.max(0, transactions.size() - maxDisplay));
        int endIndex = Math.min(startIndex + maxDisplay, transactions.size());

        int yOffset = y + 65;
        for (int i = startIndex; i < endIndex; i++) {
            Transaction tx = transactions.get(i);

            String typeStr = tx.getType().getDisplayName();
            if (typeStr.length() > 12) typeStr = typeStr.substring(0, 11) + ".";
            g.drawString(font, typeStr, x + 15, yOffset, 0x404040, false);

            String amountStr = String.format("%+.0f€", tx.getAmount());
            g.drawString(font, amountStr, x + 100, yOffset, tx.getAmount() >= 0 ? 0x00AA00 : 0xFF5555, false);

            g.drawString(font, String.format("%.0f€", tx.getBalanceAfter()), x + 155, yOffset, 0x808080, false);
            yOffset += 12;
        }

        if (transactions.size() > maxDisplay) {
            g.drawString(font, String.format("%d/%d", startIndex + 1, transactions.size()), x + 165, y + 160, 0x808080, false);
        }
    }

    private void renderDauerauftraegeTab(GuiGraphics g, int x, int y) {
        g.drawString(font, Component.translatable("gui.bank.standing_orders_title").getString(), x + 85, y + 45, 0x404040, false);
        g.drawString(font, Component.translatable("gui.bank.new_standing_order").getString(), x + 15, y + 60, 0x606060, false);
        g.drawString(font, Component.translatable("gui.bank.recipient").getString(), x + 15, y + 73, 0x808080, false);
        g.drawString(font, Component.translatable("gui.bank.amount_label").getString(), x + 15, y + 103, 0x808080, false);
        g.drawString(font, Component.translatable("gui.bank.interval").getString(), x + 15, y + 133, 0x808080, false);

        List<ClientBankDataCache.RecurringPaymentData> payments = ClientBankDataCache.getRecurringPayments();
        int maxPerPlayer = ModConfigHandler.COMMON.RECURRING_MAX_PER_PLAYER.get();

        String limitStr = payments.size() + "/" + maxPerPlayer;
        g.drawString(font, Component.translatable("gui.bank.limit").getString() + limitStr, x + 200, y + 60, payments.size() >= maxPerPlayer ? 0xFF5555 : 0x00AA00, false);

        if (payments.size() >= maxPerPlayer) {
            g.drawString(font, "MAX!", x + 200, y + 73, 0xFF5555, false);
        }

        g.fill(x + 15, y + 168, x + 265, y + 169, 0x44FFFFFF);
        g.drawString(font, Component.translatable("gui.bank.active_orders").getString(), x + 15, y + 173, 0x606060, false);

        int yOffset = y + 186;
        ClientBankDataCache.CreditLoanData loan = ClientBankDataCache.getActiveLoan();

        if (loan != null) {
            g.fill(x + 15, yOffset - 2, x + 260, yOffset + 10, 0x44004400);
            g.drawString(font, Component.translatable("gui.bank.credit_payment").getString(), x + 18, yOffset, 0xFFD700, false);
            g.drawString(font, String.format("-%.0f€", loan.dailyPayment), x + 90, yOffset, 0xFF5555, false);
            g.drawString(font, Component.translatable("gui.bank.interval_daily").getString(), x + 145, yOffset, 0x00AAAA, false);
            g.drawString(font, "✓", x + 195, yOffset, 0x00FF00, false);
            g.drawString(font, loan.loanType.length() > 3 ? loan.loanType.substring(0, 3) : loan.loanType, x + 210, yOffset, 0x808080, false);
            yOffset += 13;
        }

        if (payments.isEmpty() && loan == null) {
            g.drawString(font, Component.translatable("gui.bank.no_standing_orders").getString(), x + 65, yOffset, 0x808080, false);
        } else {
            int maxDisplay = Math.min(loan != null ? 2 : 3, payments.size());
            for (int i = 0; i < maxDisplay; i++) {
                ClientBankDataCache.RecurringPaymentData p = payments.get(i);
                g.drawString(font, p.recipientName, x + 18, yOffset, 0xFFFFFF, false);
                g.drawString(font, String.format("%.0f€", p.amount), x + 90, yOffset, 0xFFAA00, false);
                g.drawString(font, p.intervalDays + "d", x + 145, yOffset, 0x00AAAA, false);
                g.drawString(font, p.isActive ? "✓" : "⏸", x + 195, yOffset, p.isActive ? 0x00FF00 : 0xFFAA00, false);
                String id = p.paymentId.length() > 4 ? p.paymentId.substring(0, 4) : p.paymentId;
                g.drawString(font, "#" + id, x + 210, yOffset, 0x808080, false);
                yOffset += 11;
            }

            int total = payments.size() + (loan != null ? 1 : 0);
            int displayed = maxDisplay + (loan != null ? 1 : 0);
            if (total > displayed) {
                g.drawString(font, "+" + (total - displayed) + " weitere...", x + 85, yOffset, 0x808080, false);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 69) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        g.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, Component.translatable("gui.bank.banker").getString(), 8, 6, 0x404040, false);
    }
}
