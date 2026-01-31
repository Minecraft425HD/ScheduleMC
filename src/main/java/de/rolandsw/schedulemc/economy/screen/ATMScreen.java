package de.rolandsw.schedulemc.economy.screen;

import de.rolandsw.schedulemc.economy.menu.ATMMenu;
import de.rolandsw.schedulemc.economy.network.ATMTransactionPacket;
import de.rolandsw.schedulemc.economy.network.ClientBankDataCache;
import de.rolandsw.schedulemc.economy.network.EconomyNetworkHandler;
import de.rolandsw.schedulemc.economy.network.RequestATMDataPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * ATM GUI Screen mit Modus-System (Abheben/Einzahlen)
 */
public class ATMScreen extends AbstractContainerScreen<ATMMenu> {

    // Modus: true = Einzahlen, false = Abheben
    private boolean isDepositMode = false;

    // UI Components
    private EditBox amountInput;
    private Button withdrawModeButton;
    private Button depositModeButton;
    private Button confirmButton;

    // Quick amount buttons
    private Button[] quickButtons;
    private static final double[] QUICK_AMOUNTS = {50, 100, 200, 500, 1000, 5000};

    // Cached values from server
    private double balance = 0.0;
    private double walletBalance = 0.0;

    public ATMScreen(ATMMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Request initial data from server
        EconomyNetworkHandler.INSTANCE.sendToServer(new RequestATMDataPacket());

        // ═══════════════════════════════════════════════════════════════════════════
        // Mode Buttons (Abheben / Einzahlen)
        // ═══════════════════════════════════════════════════════════════════════════
        withdrawModeButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.bank.withdraw"),
            button -> setMode(false)
        ).bounds(x + 15, y + 55, 80, 20).build());

        depositModeButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.bank.deposit"),
            button -> setMode(true)
        ).bounds(x + 105, y + 55, 80, 20).build());

        // ═══════════════════════════════════════════════════════════════════════════
        // Quick Amount Buttons (2 rows x 3 columns)
        // ═══════════════════════════════════════════════════════════════════════════
        quickButtons = new Button[QUICK_AMOUNTS.length + 2]; // +2 for ALLES and custom

        int btnWidth = 55;
        int btnHeight = 18;
        int startX = x + 15;
        int startY = y + 82;
        int gap = 5;

        for (int i = 0; i < QUICK_AMOUNTS.length; i++) {
            final double amount = QUICK_AMOUNTS[i];
            int row = i / 3;
            int col = i % 3;
            int bx = startX + col * (btnWidth + gap);
            int by = startY + row * (btnHeight + gap);

            String label = amount >= 1000 ? String.format("%.0fk€", amount / 1000) : String.format("%.0f€", amount);
            quickButtons[i] = addRenderableWidget(Button.builder(
                Component.literal(label),
                button -> executeTransaction(amount)
            ).bounds(bx, by, btnWidth, btnHeight).build());
        }

        // "ALLES" Button (deposit all wallet / withdraw all balance)
        quickButtons[QUICK_AMOUNTS.length] = addRenderableWidget(Button.builder(
            Component.translatable("gui.atm.all_button"),
            button -> executeAllTransaction()
        ).bounds(x + 15, y + 130, 80, 18).build());

        // ═══════════════════════════════════════════════════════════════════════════
        // Amount Input Field
        // ═══════════════════════════════════════════════════════════════════════════
        amountInput = new EditBox(this.font, x + 15, y + 155, 110, 18, Component.translatable("gui.atm.amount_field"));
        amountInput.setValue("");
        amountInput.setHint(Component.translatable("gui.atm.custom_hint"));
        amountInput.setMaxLength(12);
        addRenderableWidget(amountInput);

        // Confirm Button for custom amount
        confirmButton = addRenderableWidget(Button.builder(
            Component.literal("OK"),
            button -> executeCustomAmount()
        ).bounds(x + 130, y + 155, 55, 18).build());

        // Set initial mode (Abheben)
        setMode(false);
    }

    /**
     * Sets the transaction mode
     */
    private void setMode(boolean deposit) {
        this.isDepositMode = deposit;
        updateModeButtons();
    }

    /**
     * Updates button appearance based on current mode
     */
    private void updateModeButtons() {
        // Visual feedback: active mode button is highlighted
        withdrawModeButton.active = isDepositMode;
        depositModeButton.active = !isDepositMode;
    }

    /**
     * Execute transaction with given amount
     */
    private void executeTransaction(double amount) {
        if (amount <= 0) return;

        // Validate against available funds
        // ATM-Gebühr ist immer 5.0€ (sync with FeeManager.ATM_FEE)
        double atmFee = 5.0;

        if (isDepositMode) {
            // Bei Einzahlen wird das gesamte Bargeld eingezahlt
            // Die Gebühr wird dann vom eingezahlten Betrag auf dem Konto abgezogen
            // Beispiel: 100€ Bargeld → 100€ aufs Konto → 5€ Gebühr abgezogen → 95€ Kontostand
            if (amount > walletBalance) {
                amount = walletBalance;
            }
        } else {
            // Bei Abheben wird die Gebühr zusätzlich vom Konto abgezogen
            // Beispiel: 100€ Konto → 95€ abheben möglich, 5€ Gebühr
            double maxWithdraw = Math.max(0, balance - atmFee);
            if (amount > maxWithdraw) {
                amount = maxWithdraw;
            }
        }

        if (amount <= 0) return;

        ATMTransactionPacket packet = new ATMTransactionPacket(
            menu.getBlockPos(),
            amount,
            isDepositMode
        );
        EconomyNetworkHandler.INSTANCE.sendToServer(packet);

        // Don't close - wait for server response to update display
    }

    /**
     * Execute "ALLES" transaction
     */
    private void executeAllTransaction() {
        double amount = isDepositMode ? walletBalance : balance;
        if (amount > 0) {
            executeTransaction(amount);
        }
    }

    /**
     * Execute transaction with custom input amount
     */
    private void executeCustomAmount() {
        try {
            double amount = Double.parseDouble(amountInput.getValue().replace(",", "."));
            if (amount > 0) {
                executeTransaction(amount);
                amountInput.setValue("");
            }
        } catch (NumberFormatException e) {
            // Invalid input - ignore
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        // Update from ClientBankDataCache
        if (ClientBankDataCache.hasData()) {
            balance = ClientBankDataCache.getBalance();
            walletBalance = ClientBankDataCache.getWalletBalance();
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Dark background
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1A1A2E);

        // Header area
        graphics.fill(x, y, x + imageWidth, y + 22, 0xFF16213E);

        // Separator lines
        graphics.fill(x + 10, y + 78, x + imageWidth - 10, y + 79, 0x44FFFFFF);
        graphics.fill(x + 10, y + 150, x + imageWidth - 10, y + 151, 0x44FFFFFF);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderInfo(graphics);
    }

    private void renderInfo(GuiGraphics graphics) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Title
        graphics.drawCenteredString(this.font, "\u00a7l " + Component.translatable("gui.atm.title").getString(), x + imageWidth / 2, y + 7, 0xFFFFFF);

        // Balance Info
        graphics.drawString(this.font, Component.translatable("gui.atm.account_label").getString(), x + 15, y + 28, 0xAAAAAA, false);
        graphics.drawString(this.font, String.format("%.2f\u20ac", balance), x + 100, y + 28, 0xFFD700, false);

        graphics.drawString(this.font, Component.translatable("gui.atm.cash_label").getString(), x + 15, y + 40, 0xAAAAAA, false);
        graphics.drawString(this.font, String.format("%.2f\u20ac", walletBalance), x + 100, y + 40, 0x00FF00, false);

        // ATM Fee info
        graphics.drawString(this.font, Component.translatable("gui.atm.fee_info").getString(), x + 15, y + 182, 0x666666, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Don't render default labels
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E key from closing
        if (keyCode == 69) {
            return true;
        }

        // Enter key executes custom amount
        if (keyCode == 257 && amountInput.isFocused()) {
            executeCustomAmount();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
