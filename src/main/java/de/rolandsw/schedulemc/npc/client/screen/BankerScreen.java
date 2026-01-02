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
 * Vollständige Banker GUI mit Tab-Navigation
 * Tab 1: Konten - Anzeige von Girokonto & Sparkonto
 * Tab 2: Überweisen - Transfer zwischen Spielern
 * Tab 3: Transaktionen - History der letzten Transaktionen
 */
@OnlyIn(Dist.CLIENT)
public class BankerScreen extends AbstractContainerScreen<BankerMenu> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/npc_interaction.png");

    private enum Tab {
        KONTEN,
        UEBERWEISEN,
        TRANSAKTIONEN
    }

    private Tab currentTab = Tab.KONTEN;

    // Tab Buttons
    private Button kontenTabButton;
    private Button ueberweisenTabButton;
    private Button transaktionenTabButton;

    // Konten Tab Components
    private Button depositButton;
    private Button withdrawButton;
    private EditBox depositAmountInput;
    private EditBox withdrawAmountInput;

    // Überweisen Tab Components
    private EditBox transferTargetInput;
    private EditBox transferAmountInput;
    private Button transferButton;

    // Transaktionen Tab Components
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

        // Tab Buttons
        kontenTabButton = addRenderableWidget(Button.builder(Component.literal("Konten"), button -> {
            switchTab(Tab.KONTEN);
        }).bounds(x + 8, y + 20, 50, 15).build());

        ueberweisenTabButton = addRenderableWidget(Button.builder(Component.literal("Überweis."), button -> {
            switchTab(Tab.UEBERWEISEN);
        }).bounds(x + 60, y + 20, 55, 15).build());

        transaktionenTabButton = addRenderableWidget(Button.builder(Component.literal("Historie"), button -> {
            switchTab(Tab.TRANSAKTIONEN);
        }).bounds(x + 117, y + 20, 51, 15).build());

        // Konten Tab Components
        depositAmountInput = new EditBox(this.font, x + 10, y + 60, 60, 15, Component.literal("Betrag"));
        depositAmountInput.setMaxLength(10);
        depositAmountInput.setValue("1000");
        addRenderableWidget(depositAmountInput);

        depositButton = addRenderableWidget(Button.builder(Component.literal("Einzahlen"), button -> {
            handleDeposit();
        }).bounds(x + 72, y + 60, 55, 15).build());

        withdrawAmountInput = new EditBox(this.font, x + 10, y + 80, 60, 15, Component.literal("Betrag"));
        withdrawAmountInput.setMaxLength(10);
        withdrawAmountInput.setValue("500");
        addRenderableWidget(withdrawAmountInput);

        withdrawButton = addRenderableWidget(Button.builder(Component.literal("Abheben"), button -> {
            handleWithdraw();
        }).bounds(x + 72, y + 80, 55, 15).build());

        // Überweisen Tab Components
        transferTargetInput = new EditBox(this.font, x + 10, y + 55, 145, 15, Component.literal("Spielername"));
        transferTargetInput.setMaxLength(16);
        transferTargetInput.setValue("");
        addRenderableWidget(transferTargetInput);

        transferAmountInput = new EditBox(this.font, x + 10, y + 87, 85, 15, Component.literal("Betrag"));
        transferAmountInput.setMaxLength(10);
        transferAmountInput.setValue("100");
        addRenderableWidget(transferAmountInput);

        transferButton = addRenderableWidget(Button.builder(Component.literal("Überweisen"), button -> {
            handleTransfer();
        }).bounds(x + 10, y + 107, 145, 18).build());

        // Transaktionen Tab Components
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
        boolean isKonten = currentTab == Tab.KONTEN;
        boolean isUeberweisen = currentTab == Tab.UEBERWEISEN;
        boolean isTransaktionen = currentTab == Tab.TRANSAKTIONEN;

        // Konten Tab
        depositAmountInput.visible = isKonten;
        depositButton.visible = isKonten;
        withdrawAmountInput.visible = isKonten;
        withdrawButton.visible = isKonten;

        // Überweisen Tab
        transferTargetInput.visible = isUeberweisen;
        transferAmountInput.visible = isUeberweisen;
        transferButton.visible = isUeberweisen;

        // Transaktionen Tab
        scrollUpButton.visible = isTransaktionen;
        scrollDownButton.visible = isTransaktionen;
    }

    /**
     * Verarbeitet Einzahlung
     */
    private void handleDeposit() {
        try {
            double amount = Double.parseDouble(depositAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new BankDepositPacket(amount));
                this.onClose();
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }

    /**
     * Verarbeitet Abhebung
     */
    private void handleWithdraw() {
        try {
            double amount = Double.parseDouble(withdrawAmountInput.getValue());
            if (amount > 0) {
                NPCNetworkHandler.sendToServer(new BankWithdrawPacket(amount));
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
            case KONTEN:
                renderKontenTab(guiGraphics, x, y);
                break;
            case UEBERWEISEN:
                renderUeberweisenTab(guiGraphics, x, y);
                break;
            case TRANSAKTIONEN:
                renderTransaktionenTab(guiGraphics, x, y);
                break;
        }
    }

    /**
     * Rendert Konten Tab
     */
    private void renderKontenTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null) return;

        // Bargeld (Wallet)
        double bargeld = WalletManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, "BARGELD", x + 10, y + 40, 0x404040, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", bargeld), x + 80, y + 40, 0xFFAA00, false);

        // Girokonto (Hauptkonto)
        double girokonto = EconomyManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, "GIROKONTO", x + 10, y + 48, 0x404040, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", girokonto), x + 80, y + 48, 0x00AA00, false);

        // Sparkonto (Gesamt aller Sparkonten)
        SavingsAccountManager savingsManager = SavingsAccountManager.getInstance(minecraft.level.getServer());
        List<SavingsAccount> savingsAccounts = savingsManager.getAccounts(minecraft.player.getUUID());
        double sparkonto = savingsAccounts.stream()
            .mapToDouble(SavingsAccount::getBalance)
            .sum();

        guiGraphics.drawString(this.font, "SPARKONTO", x + 10, y + 56, 0x404040, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", sparkonto), x + 80, y + 56, 0x6666FF, false);

        // Einzahlen/Abheben Labels
        guiGraphics.drawString(this.font, "Einzahlung:", x + 10, y + 68, 0x808080, false);
        guiGraphics.drawString(this.font, "Abhebung:", x + 10, y + 88, 0x808080, false);

        // Deposit Limit Info
        double depositLimit = ModConfigHandler.COMMON.BANK_DEPOSIT_LIMIT.get();
        guiGraphics.drawString(this.font, "Limit: " + String.format("%.0f€", depositLimit),
            x + 10, y + 108, 0x808080, false);

        // Info
        guiGraphics.drawString(this.font, "Bargeld aus Wallet wird", x + 10, y + 118, 0x606060, false);
        guiGraphics.drawString(this.font, "auf Girokonto eingezahlt.", x + 10, y + 126, 0x606060, false);
    }

    /**
     * Rendert Überweisen Tab
     */
    private void renderUeberweisenTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null) return;

        // Verfügbar Label und Betrag
        double balance = EconomyManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, "Verfügbar:", x + 10, y + 40, 0x404040, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", balance), x + 80, y + 40, 0xFFD700, false);

        // Empfänger Label (über dem Eingabefeld)
        guiGraphics.drawString(this.font, "Empfängername:", x + 10, y + 48, 0x808080, false);

        // Betrag Label (über dem Eingabefeld mit mehr Abstand)
        guiGraphics.drawString(this.font, "Betrag in €:", x + 10, y + 75, 0x808080, false);

        // Transfer Limit Info
        if (minecraft.level.getServer() != null) {
            TransferLimitTracker tracker = TransferLimitTracker.getInstance(minecraft.level.getServer());
            double remaining = tracker.getRemainingLimit(minecraft.player.getUUID());

            guiGraphics.drawString(this.font, "Überweisung an andere Spieler",
                x + 10, y + 127, 0x606060, false);
        }
    }

    /**
     * Rendert Transaktionen Tab
     */
    private void renderTransaktionenTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null || minecraft.level.getServer() == null) return;

        guiGraphics.drawString(this.font, "LETZTE TRANSAKTIONEN", x + 10, y + 40, 0x404040, false);

        // Get transaction history
        TransactionHistory history = TransactionHistory.getInstance(minecraft.level.getServer());
        List<Transaction> transactions = history.getAllTransactions(minecraft.player.getUUID());

        if (transactions.isEmpty()) {
            guiGraphics.drawString(this.font, "Keine Transaktionen", x + 20, y + 60, 0x808080, false);
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
