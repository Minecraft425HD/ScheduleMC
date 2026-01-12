package de.rolandsw.schedulemc.towing.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.towing.menu.TowingInvoiceMenu;
import de.rolandsw.schedulemc.towing.network.PayTowingInvoicePacket;
import de.rolandsw.schedulemc.towing.network.TowingNetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI Screen for towing invoice payment
 */
public class TowingInvoiceScreen extends AbstractContainerScreen<TowingInvoiceMenu> {
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/towing_invoice.png");

    private Button payButton;

    public TowingInvoiceScreen(TowingInvoiceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();

        // Payment button
        this.payButton = Button.builder(
            Component.translatable("towing.invoice.pay"),
            button -> payInvoice()
        ).bounds(leftPos + 60, topPos + 60, 56, 20).build();

        addRenderableWidget(payButton);
    }

    private void payInvoice() {
        // Send payment packet to server
        TowingNetworkHandler.sendToServer(new PayTowingInvoicePacket(
            menu.getInvoice().getInvoiceId()
        ));
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Title
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);

        // Invoice amount
        String amountText = String.format("%.0f€", menu.getInvoice().getAmount());
        int amountWidth = this.font.width(amountText);
        graphics.drawString(this.font, amountText, (imageWidth - amountWidth) / 2, 20, 0x404040, false);

        // Towing yard info
        String yardText = Component.translatable("towing.invoice.yard",
            menu.getInvoice().getTowingYardPlotId()).getString();
        graphics.drawString(this.font, yardText, 8, 50, 0x404040, false);

        // Don't render "Inventory" label - keep it clean
    }
}
