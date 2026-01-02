package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.economy.SavingsAccountManager;
import de.rolandsw.schedulemc.economy.TransactionHistory;
import de.rolandsw.schedulemc.economy.Transaction;
import de.rolandsw.schedulemc.npc.menu.BankerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Banker GUI Screen (Option A - Tab-based)
 *
 * TODO: Full implementation with tabs:
 * - Tab 1: Konten (Girokonto/Sparkonto anzeigen)
 * - Tab 2: Überweisen (mit Limit-Tracking)
 * - Tab 3: Transaktionen (scrollbare Historie)
 *
 * This is a minimal prototype showing the structure.
 */
@OnlyIn(Dist.CLIENT)
public class BankerScreen extends AbstractContainerScreen<BankerMenu> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/npc_interaction.png");

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

        // Close Button
        closeButton = addRenderableWidget(Button.builder(Component.literal("Schließen"), button -> {
            this.onClose();
        }).bounds(x + 38, y + 140, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Get player wallet balance
        double wallet = 0.0;
        if (minecraft != null && minecraft.player != null) {
            wallet = EconomyManager.getBalance(minecraft.player.getUUID());
        }

        // Display wallet balance
        String walletText = "Wallet: " + String.format("%.2f€", wallet);
        guiGraphics.drawString(this.font, walletText, x + 10, y + 30, 0x404040, false);

        // TODO: Display Girokonto and Sparkonto balances
        // TODO: Add tab navigation
        // TODO: Add transaction history display
        // TODO: Add transfer functionality

        // Placeholder message
        guiGraphics.drawString(this.font, "Banking GUI (Prototype)", x + 30, y + 50, 0x404040, false);
        guiGraphics.drawString(this.font, "Full implementation pending", x + 20, y + 70, 0x808080, false);
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
