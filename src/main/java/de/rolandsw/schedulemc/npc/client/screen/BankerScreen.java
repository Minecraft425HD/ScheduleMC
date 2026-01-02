package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
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
        new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/npc_interaction.png");

    private enum Tab {
        UEBERSICHT,
        GIROKONTO,
        SPARKONTO,
        UEBERWEISUNG,
        HISTORIE
    }

    private Tab currentTab = Tab.UEBERSICHT;

    // Tab Buttons
    private Button uebersichtTabButton;
    private Button girokontoTabButton;
    private Button sparkontoTabButton;
    private Button ueberweisungTabButton;
    private Button historieTabButton;

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

    // Close Button
    private Button closeButton;

    public BankerScreen(BankerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Tab Buttons (5 Tabs, kleinere Breite)
        int tabWidth = 33;
        int tabStartX = x + 5;

        uebersichtTabButton = addRenderableWidget(Button.builder(Component.literal("Übersicht"), button -> {
            switchTab(Tab.UEBERSICHT);
        }).bounds(tabStartX, y + 20, tabWidth, 15).build());

        girokontoTabButton = addRenderableWidget(Button.builder(Component.literal("Giro"), button -> {
            switchTab(Tab.GIROKONTO);
        }).bounds(tabStartX + tabWidth + 1, y + 20, tabWidth, 15).build());

        sparkontoTabButton = addRenderableWidget(Button.builder(Component.literal("Spar"), button -> {
            switchTab(Tab.SPARKONTO);
        }).bounds(tabStartX + (tabWidth + 1) * 2, y + 20, tabWidth, 15).build());

        ueberweisungTabButton = addRenderableWidget(Button.builder(Component.literal("Überw."), button -> {
            switchTab(Tab.UEBERWEISUNG);
        }).bounds(tabStartX + (tabWidth + 1) * 3, y + 20, tabWidth, 15).build());

        historieTabButton = addRenderableWidget(Button.builder(Component.literal("Hist."), button -> {
            switchTab(Tab.HISTORIE);
        }).bounds(tabStartX + (tabWidth + 1) * 4, y + 20, tabWidth + 2, 15).build());

        // Girokonto Tab Components
        giroDepositAmountInput = new EditBox(this.font, x + 10, y + 68, 75, 15, Component.literal("Betrag"));
        giroDepositAmountInput.setMaxLength(10);
        giroDepositAmountInput.setValue("1000");
        addRenderableWidget(giroDepositAmountInput);

        giroDepositButton = addRenderableWidget(Button.builder(Component.literal("Einzahlen"), button -> {
            handleGiroDeposit();
        }).bounds(x + 90, y + 68, 75, 15).build());

        giroWithdrawAmountInput = new EditBox(this.font, x + 10, y + 90, 75, 15, Component.literal("Betrag"));
        giroWithdrawAmountInput.setMaxLength(10);
        giroWithdrawAmountInput.setValue("500");
        addRenderableWidget(giroWithdrawAmountInput);

        giroWithdrawButton = addRenderableWidget(Button.builder(Component.literal("Abheben"), button -> {
            handleGiroWithdraw();
        }).bounds(x + 90, y + 90, 75, 15).build());

        // Sparkonto Tab Components
        savingsDepositAmountInput = new EditBox(this.font, x + 10, y + 68, 75, 15, Component.literal("Betrag"));
        savingsDepositAmountInput.setMaxLength(10);
        savingsDepositAmountInput.setValue("1000");
        addRenderableWidget(savingsDepositAmountInput);

        savingsDepositButton = addRenderableWidget(Button.builder(Component.literal("Einzahlen"), button -> {
            handleSavingsDeposit();
        }).bounds(x + 90, y + 68, 75, 15).build());

        savingsWithdrawAmountInput = new EditBox(this.font, x + 10, y + 90, 75, 15, Component.literal("Betrag"));
        savingsWithdrawAmountInput.setMaxLength(10);
        savingsWithdrawAmountInput.setValue("500");
        addRenderableWidget(savingsWithdrawAmountInput);

        savingsWithdrawButton = addRenderableWidget(Button.builder(Component.literal("Abheben"), button -> {
            handleSavingsWithdraw();
        }).bounds(x + 90, y + 90, 75, 15).build());

        // Überweisung Tab Components
        transferTargetInput = new EditBox(this.font, x + 10, y + 58, 155, 15, Component.literal("Spielername"));
        transferTargetInput.setMaxLength(16);
        transferTargetInput.setValue("");
        addRenderableWidget(transferTargetInput);

        transferAmountInput = new EditBox(this.font, x + 10, y + 90, 85, 15, Component.literal("Betrag"));
        transferAmountInput.setMaxLength(10);
        transferAmountInput.setValue("100");
        addRenderableWidget(transferAmountInput);

        transferButton = addRenderableWidget(Button.builder(Component.literal("Überweisen"), button -> {
            handleTransfer();
        }).bounds(x + 10, y + 110, 155, 18).build());

        // Historie Tab Components
        scrollUpButton = addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
            if (transactionScrollOffset > 0) {
                transactionScrollOffset--;
            }
        }).bounds(x + 155, y + 40, 15, 15).build());

        scrollDownButton = addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
            transactionScrollOffset++;
        }).bounds(x + 155, y + 120, 15, 15).build());

        // Close Button
        closeButton = addRenderableWidget(Button.builder(Component.literal("Schließen"), button -> {
            this.onClose();
        }).bounds(x + 38, y + 140, 100, 20).build());

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
        }
    }

    /**
     * Rendert Übersicht Tab (nur Anzeige)
     */
    private void renderUebersichtTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null) return;

        guiGraphics.drawString(this.font, "KONTEN-ÜBERSICHT", x + 45, y + 40, 0x404040, false);

        // Bargeld (Wallet)
        double bargeld = WalletManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, "BARGELD", x + 20, y + 55, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", bargeld), x + 95, y + 55, 0xFFAA00, false);

        // Girokonto
        double girokonto = EconomyManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, "GIROKONTO", x + 20, y + 70, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", girokonto), x + 95, y + 70, 0x00AA00, false);

        // Sparkonto
        SavingsAccountManager savingsManager = SavingsAccountManager.getInstance(minecraft.level.getServer());
        List<SavingsAccount> savingsAccounts = savingsManager.getAccounts(minecraft.player.getUUID());
        double sparkonto = savingsAccounts.stream()
            .mapToDouble(SavingsAccount::getBalance)
            .sum();
        guiGraphics.drawString(this.font, "SPARKONTO", x + 20, y + 85, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", sparkonto), x + 95, y + 85, 0x6666FF, false);

        // Trennlinie
        guiGraphics.fill(x + 15, y + 97, x + 160, y + 98, 0x44FFFFFF);

        // Gesamt
        double gesamt = bargeld + girokonto + sparkonto;
        guiGraphics.drawString(this.font, "GESAMT:", x + 20, y + 105, 0x404040, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", gesamt), x + 95, y + 105, 0xFFD700, false);
    }

    /**
     * Rendert Girokonto Tab
     */
    private void renderGirokontoTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null) return;

        guiGraphics.drawString(this.font, "GIROKONTO", x + 60, y + 40, 0x404040, false);

        // Kontostand
        double girokonto = EconomyManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, "Kontostand:", x + 15, y + 50, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", girokonto), x + 95, y + 50, 0x00AA00, false);

        // Bargeld
        double bargeld = WalletManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, "Bargeld:", x + 15, y + 58, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", bargeld), x + 95, y + 58, 0xFFAA00, false);

        // Einzahlen Label
        guiGraphics.drawString(this.font, "Einzahlen (aus Bargeld):", x + 10, y + 56, 0x808080, false);

        // Abheben Label
        guiGraphics.drawString(this.font, "Abheben (zu Bargeld):", x + 10, y + 78, 0x808080, false);

        // Info
        double depositLimit = ModConfigHandler.COMMON.BANK_DEPOSIT_LIMIT.get();
        guiGraphics.drawString(this.font, "Limit: " + String.format("%.0f€", depositLimit),
            x + 10, y + 110, 0x606060, false);
    }

    /**
     * Rendert Sparkonto Tab
     */
    private void renderSparkontoTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null) return;

        guiGraphics.drawString(this.font, "SPARKONTO", x + 60, y + 40, 0x404040, false);

        // Sparkonto Stand
        SavingsAccountManager savingsManager = SavingsAccountManager.getInstance(minecraft.level.getServer());
        List<SavingsAccount> savingsAccounts = savingsManager.getAccounts(minecraft.player.getUUID());
        double sparkonto = savingsAccounts.stream()
            .mapToDouble(SavingsAccount::getBalance)
            .sum();
        guiGraphics.drawString(this.font, "Kontostand:", x + 15, y + 50, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", sparkonto), x + 95, y + 50, 0x6666FF, false);

        // Girokonto
        double girokonto = EconomyManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, "Girokonto:", x + 15, y + 58, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", girokonto), x + 95, y + 58, 0x00AA00, false);

        // Einzahlen Label
        guiGraphics.drawString(this.font, "Einzahlen (vom Girokonto):", x + 10, y + 56, 0x808080, false);

        // Abheben Label
        guiGraphics.drawString(this.font, "Abheben (zum Girokonto):", x + 10, y + 78, 0x808080, false);

        // Info
        guiGraphics.drawString(this.font, "Zinsen: 5% pro Woche", x + 10, y + 110, 0x606060, false);
        guiGraphics.drawString(this.font, "Minimum: 1000€", x + 10, y + 118, 0x606060, false);
        guiGraphics.drawString(this.font, "4-Wochen Sperre", x + 10, y + 126, 0x606060, false);
    }

    /**
     * Rendert Überweisung Tab
     */
    private void renderUeberweisungTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null) return;

        guiGraphics.drawString(this.font, "ÜBERWEISUNG", x + 55, y + 40, 0x404040, false);

        // Verfügbar
        double balance = EconomyManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, "Verfügbar:", x + 10, y + 48, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", balance), x + 80, y + 48, 0xFFD700, false);

        // Empfänger Label
        guiGraphics.drawString(this.font, "Empfängername:", x + 10, y + 47, 0x808080, false);

        // Betrag Label
        guiGraphics.drawString(this.font, "Betrag in €:", x + 10, y + 79, 0x808080, false);

        // Info
        if (minecraft.level.getServer() != null) {
            TransferLimitTracker tracker = TransferLimitTracker.getInstance(minecraft.level.getServer());
            double remaining = tracker.getRemainingLimit(minecraft.player.getUUID());
            guiGraphics.drawString(this.font, "Tageslimit verfügbar:",
                x + 10, y + 132, 0x606060, false);
            guiGraphics.drawString(this.font, String.format("%.2f€", remaining),
                x + 115, y + 132, remaining > 0 ? 0x00AA00 : 0xFF5555, false);
        }
    }

    /**
     * Rendert Historie Tab
     */
    private void renderHistorieTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null || minecraft.level.getServer() == null) return;

        guiGraphics.drawString(this.font, "TRANSAKTIONEN", x + 50, y + 40, 0x404040, false);

        // Get transaction history
        TransactionHistory history = TransactionHistory.getInstance(minecraft.level.getServer());
        List<Transaction> transactions = history.getAllTransactions(minecraft.player.getUUID());

        if (transactions.isEmpty()) {
            guiGraphics.drawString(this.font, "Keine Transaktionen", x + 40, y + 70, 0x808080, false);
            return;
        }

        // Display transactions (scrollable)
        int maxDisplay = 6;
        int startIndex = Math.min(transactionScrollOffset, Math.max(0, transactions.size() - maxDisplay));
        int endIndex = Math.min(startIndex + maxDisplay, transactions.size());

        int yOffset = y + 55;
        for (int i = startIndex; i < endIndex; i++) {
            Transaction tx = transactions.get(i);

            // Typ
            String typeStr = tx.getType().getDisplayName();
            if (typeStr.length() > 12) {
                typeStr = typeStr.substring(0, 11) + ".";
            }
            guiGraphics.drawString(this.font, typeStr, x + 10, yOffset, 0x404040, false);

            // Betrag (+ grün, - rot)
            String amountStr = String.format("%+.0f€", tx.getAmount());
            int color = tx.getAmount() >= 0 ? 0x00AA00 : 0xFF5555;
            guiGraphics.drawString(this.font, amountStr, x + 85, yOffset, color, false);

            // Balance after
            String balanceStr = String.format("%.0f€", tx.getBalanceAfter());
            guiGraphics.drawString(this.font, balanceStr, x + 125, yOffset, 0x808080, false);

            yOffset += 10;
        }

        // Scroll indicator
        if (transactions.size() > maxDisplay) {
            guiGraphics.drawString(this.font,
                String.format("%d/%d", startIndex + 1, transactions.size()),
                x + 135, y + 125, 0x808080, false);
        }
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
        guiGraphics.drawString(this.font, "BANKER", 8, 6, 0x404040, false);
    }
}
