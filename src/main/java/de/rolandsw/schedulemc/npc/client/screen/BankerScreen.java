package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.SavingsAccount;
import de.rolandsw.schedulemc.economy.SavingsAccountManager;
import de.rolandsw.schedulemc.economy.Transaction;
import de.rolandsw.schedulemc.economy.TransactionHistory;
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
        transferTargetInput = new EditBox(this.font, x + 10, y + 50, 80, 15, Component.literal("Spielername"));
        transferTargetInput.setMaxLength(16);
        transferTargetInput.setValue("");
        addRenderableWidget(transferTargetInput);

        transferAmountInput = new EditBox(this.font, x + 10, y + 70, 60, 15, Component.literal("Betrag"));
        transferAmountInput.setMaxLength(10);
        transferAmountInput.setValue("100");
        addRenderableWidget(transferAmountInput);

        transferButton = addRenderableWidget(Button.builder(Component.literal("Überweisen"), button -> {
            handleTransfer();
        }).bounds(x + 72, y + 70, 65, 15).build());

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

        // Girokonto (Hauptkonto)
        double girokonto = EconomyManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, "GIROKONTO", x + 10, y + 40, 0x404040, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", girokonto), x + 80, y + 40, 0x00AA00, false);

        // Sparkonto (Gesamt aller Sparkonten)
        SavingsAccountManager savingsManager = SavingsAccountManager.getInstance(minecraft.level.getServer());
        List<SavingsAccount> savingsAccounts = savingsManager.getAccounts(minecraft.player.getUUID());
        double sparkonto = savingsAccounts.stream()
            .mapToDouble(SavingsAccount::getBalance)
            .sum();

        guiGraphics.drawString(this.font, "SPARKONTO", x + 10, y + 48, 0x404040, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", sparkonto), x + 80, y + 48, 0xFFAA00, false);

        // Einzahlen/Abheben Labels
        guiGraphics.drawString(this.font, "Einzahlung:", x + 10, y + 50, 0x808080, false);
        guiGraphics.drawString(this.font, "Abhebung:", x + 10, y + 70, 0x808080, false);

        // Deposit Limit Info
        double depositLimit = ModConfigHandler.COMMON.BANK_DEPOSIT_LIMIT.get();
        guiGraphics.drawString(this.font, "Max: " + String.format("%.0f€", depositLimit),
            x + 10, y + 100, 0x808080, false);

        // Info
        guiGraphics.drawString(this.font, "Hinweis: Wertgegenstände werden", x + 10, y + 110, 0x606060, false);
        guiGraphics.drawString(this.font, "automatisch gewechselt.", x + 10, y + 118, 0x606060, false);
    }

    /**
     * Rendert Überweisen Tab
     */
    private void renderUeberweisenTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null) return;

        guiGraphics.drawString(this.font, "Empfänger:", x + 10, y + 40, 0x404040, false);
        guiGraphics.drawString(this.font, "Betrag:", x + 10, y + 60, 0x404040, false);

        // Transfer Limit Info
        TransferLimitTracker tracker = TransferLimitTracker.getInstance();
        double remaining = tracker.getRemainingLimit(minecraft.player.getUUID());
        double dailyLimit = ModConfigHandler.COMMON.BANK_TRANSFER_DAILY_LIMIT.get();

        guiGraphics.drawString(this.font, "TAGESLIMIT", x + 10, y + 95, 0x404040, false);
        guiGraphics.drawString(this.font, String.format("Verfügbar: %.2f€", remaining),
            x + 10, y + 105, remaining > 0 ? 0x00AA00 : 0xFF5555, false);
        guiGraphics.drawString(this.font, String.format("Maximum: %.2f€", dailyLimit),
            x + 10, y + 113, 0x808080, false);

        // Girokonto Balance
        double balance = EconomyManager.getBalance(minecraft.player.getUUID());
        guiGraphics.drawString(this.font, "Kontostand:", x + 10, y + 123, 0x808080, false);
        guiGraphics.drawString(this.font, String.format("%.2f€", balance),
            x + 75, y + 123, 0x00AA00, false);
    }

    /**
     * Rendert Transaktionen Tab
     */
    private void renderTransaktionenTab(GuiGraphics guiGraphics, int x, int y) {
        if (minecraft == null || minecraft.player == null || minecraft.level.getServer() == null) return;

        guiGraphics.drawString(this.font, "LETZTE TRANSAKTIONEN", x + 10, y + 40, 0x404040, false);

        // Get transaction history
        TransactionHistory history = TransactionHistory.getInstance(minecraft.level.getServer());
        List<Transaction> transactions = history.getTransactions(minecraft.player.getUUID());

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
