package de.rolandsw.schedulemc.economy.screen;

import de.rolandsw.schedulemc.economy.menu.ATMMenu;
import de.rolandsw.schedulemc.economy.network.ATMTransactionPacket;
import de.rolandsw.schedulemc.economy.network.EconomyNetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * ATM GUI Screen
 */
public class ATMScreen extends AbstractContainerScreen<ATMMenu> {
    
    private EditBox amountInput;
    private double balance = 0.0;
    private double walletBalance = 0.0;
    
    public ATMScreen(ATMMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        updateBalances();
        
        // Amount Input Field
        amountInput = new EditBox(this.font, x + 70, y + 50, 85, 20, Component.literal("Amount"));
        amountInput.setValue("100");
        amountInput.setMaxLength(10);
        addRenderableWidget(amountInput);
        
        // Withdraw Button
        addRenderableWidget(Button.builder(Component.literal("Abheben"), button -> {
            withdraw(getInputAmount());
        }).bounds(x + 20, y + 75, 70, 20).build());
        
        // Deposit Button
        addRenderableWidget(Button.builder(Component.literal("Einzahlen"), button -> {
            deposit(getInputAmount());
        }).bounds(x + 95, y + 75, 70, 20).build());
        
        // Quick Withdraw Buttons
        addRenderableWidget(Button.builder(Component.literal("100€"), button -> {
            withdraw(100);
        }).bounds(x + 20, y + 100, 45, 20).build());
        
        addRenderableWidget(Button.builder(Component.literal("1000€"), button -> {
            withdraw(1000);
        }).bounds(x + 70, y + 100, 45, 20).build());
        
        // Deposit All Button
        addRenderableWidget(Button.builder(Component.literal("Alles"), button -> {
            deposit(walletBalance);
        }).bounds(x + 120, y + 100, 45, 20).build());
    }
    
    private double getInputAmount() {
        try {
            return Double.parseDouble(amountInput.getValue());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    private void withdraw(double amount) {
        if (amount <= 0) return;
        
        ATMTransactionPacket packet = new ATMTransactionPacket(
            menu.getBlockPos(), 
            amount, 
            false
        );
        EconomyNetworkHandler.INSTANCE.sendToServer(packet);
        
        minecraft.player.closeContainer();
    }
    
    private void deposit(double amount) {
        if (amount <= 0) return;
        
        ATMTransactionPacket packet = new ATMTransactionPacket(
            menu.getBlockPos(), 
            amount, 
            true
        );
        EconomyNetworkHandler.INSTANCE.sendToServer(packet);
        
        minecraft.player.closeContainer();
    }
    
    private void updateBalances() {
        if (minecraft != null && minecraft.player != null && menu.getATM() != null) {
            balance = menu.getATM().getBalance(minecraft.player);
            walletBalance = menu.getATM().getWalletBalance(minecraft.player);
        }
    }
    
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        // Dunkler Hintergrund
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF2B2B2B);
        
        // Hellerer innerer Bereich
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF3C3C3C);
        
        // Header-Bereich
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + 20, 0xFF1E1E1E);
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
        
        updateBalances();
        
        // Title (Weiß)
        graphics.drawString(this.font, "GELDAUTOMAT", x + 50, y + 8, 0xFFFFFF, false);
        
        // Balance Info (Gelb)
        graphics.drawString(this.font, "Konto:", x + 10, y + 28, 0xFFFFFF, false);
        graphics.drawString(this.font, String.format("%.2f€", balance), x + 100, y + 28, 0xFFD700, false);
        
        // Wallet Info (Grün)
        graphics.drawString(this.font, "Bargeld:", x + 10, y + 38, 0xFFFFFF, false);
        graphics.drawString(this.font, String.format("%.2f€", walletBalance), x + 100, y + 38, 0x00FF00, false);
        
        // Input Label
        graphics.drawString(this.font, "Betrag:", x + 10, y + 54, 0xFFFFFF, false);
    }
    
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Leer lassen - wir rendern alles in renderInfo()
    }    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true; // Consume event, prevent closing
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    
    @Override
    public boolean isPauseScreen() {
        return super.isPauseScreen();
    }
}
