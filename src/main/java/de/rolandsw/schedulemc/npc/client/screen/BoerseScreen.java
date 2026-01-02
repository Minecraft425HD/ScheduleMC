package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.menu.BoerseMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Börse (Stock Market) GUI Screen (Option B - Compact List)
 *
 * TODO: Full implementation:
 * - Display Gold/Diamond/Emerald prices with trends
 * - Show player inventory counts
 * - Quick trade buttons (Buy 1x, 10x, 64x, Max)
 * - Sell buttons (Sell 1x, 10x, All)
 * - Integration with StockMarketData for dynamic prices
 *
 * This is a minimal prototype showing the structure.
 */
@OnlyIn(Dist.CLIENT)
public class BoerseScreen extends AbstractContainerScreen<BoerseMenu> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/npc_interaction.png");

    private Button closeButton;

    public BoerseScreen(BoerseMenu menu, Inventory playerInventory, Component title) {
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

        // TODO: Get current stock prices from StockMarketData
        // TODO: Display price trends (arrows)
        // TODO: Show player's gold/diamond/emerald inventory

        // Placeholder stock display
        guiGraphics.drawString(this.font, "MARKT", x + 10, y + 30, 0x404040, false);
        guiGraphics.drawString(this.font, "Goldbarren: 250€ ↗", x + 10, y + 50, 0x404040, false);
        guiGraphics.drawString(this.font, "Diamant: 450€ ↘", x + 10, y + 65, 0x404040, false);
        guiGraphics.drawString(this.font, "Smaragd: 180€ →", x + 10, y + 80, 0x404040, false);

        // Placeholder message
        guiGraphics.drawString(this.font, "Stock Trading (Prototype)", x + 20, y + 105, 0x808080, false);
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
        guiGraphics.drawString(this.font, "BÖRSENMAKLER", 8, 6, 0x404040, false);
    }
}
