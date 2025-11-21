package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.menu.NPCInteractionMenu;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.NPCActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * GUI für NPC Interaktion
 * - Dialog anzeigen
 * - Optionen: Dialog, Verkaufen, Kaufen
 */
@OnlyIn(Dist.CLIENT)
public class NPCInteractionScreen extends AbstractContainerScreen<NPCInteractionMenu> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/npc_interaction.png");

    private Button dialogButton;
    private Button shopBuyButton;
    private Button shopSellButton;

    private String currentDialogText = "";

    public NPCInteractionScreen(NPCInteractionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Dialog Button
        dialogButton = addRenderableWidget(Button.builder(Component.literal("Dialog"), button -> {
            openDialog();
        }).bounds(x + 8, y + 30, 160, 20).build());

        // Shop Verkaufen Button
        shopSellButton = addRenderableWidget(Button.builder(Component.literal("Verkaufen"), button -> {
            openShopSell();
        }).bounds(x + 8, y + 54, 78, 20).build());

        // Shop Kaufen Button
        shopBuyButton = addRenderableWidget(Button.builder(Component.literal("Kaufen"), button -> {
            openShopBuy();
        }).bounds(x + 90, y + 54, 78, 20).build());

        // Lade initialen Dialog
        loadCurrentDialog();
    }

    /**
     * Lädt den aktuellen Dialog vom NPC
     */
    private void loadCurrentDialog() {
        CustomNPCEntity npc = menu.getNpc();
        if (npc != null) {
            NPCData.DialogEntry dialog = npc.getNpcData().getCurrentDialog();
            currentDialogText = dialog.getText();
        }
    }

    /**
     * Öffnet Dialog und zeigt nächsten Dialog-Text an
     */
    private void openDialog() {
        CustomNPCEntity npc = menu.getNpc();
        if (npc != null) {
            // Sende Packet an Server: Nächster Dialog
            NPCNetworkHandler.sendToServer(new NPCActionPacket(
                menu.getEntityId(),
                NPCActionPacket.Action.NEXT_DIALOG
            ));

            // Nächsten Dialog laden
            npc.getNpcData().nextDialog();
            loadCurrentDialog();
        }
    }

    /**
     * Öffnet Shop (Verkaufen)
     */
    private void openShopSell() {
        // TODO: Implementiere Shop-GUI für Verkaufen
        // Zeigt Items an die der NPC kauft
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.sendSystemMessage(Component.literal("Shop (Verkaufen) - Wird noch implementiert"));
        }
    }

    /**
     * Öffnet Shop (Kaufen)
     */
    private void openShopBuy() {
        // TODO: Implementiere Shop-GUI für Kaufen
        // Zeigt Items an die der NPC verkauft
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.sendSystemMessage(Component.literal("Shop (Kaufen) - Wird noch implementiert"));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
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
        CustomNPCEntity npc = menu.getNpc();
        if (npc != null) {
            guiGraphics.drawString(this.font, npc.getNpcName(), 8, 6, 0x404040, false);
        }

        // Dialog Text anzeigen
        guiGraphics.drawString(this.font, "Dialog:", 8, 18, 0x404040, false);

        // Mehrzeiliger Dialog-Text
        if (!currentDialogText.isEmpty()) {
            int maxWidth = imageWidth - 20;
            int lineHeight = 10;
            int startY = 78;

            // Split text into lines
            String[] words = currentDialogText.split(" ");
            StringBuilder currentLine = new StringBuilder();
            int currentY = startY;

            for (String word : words) {
                String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
                if (font.width(testLine) > maxWidth) {
                    guiGraphics.drawString(this.font, currentLine.toString(), 8, currentY, 0x404040, false);
                    currentLine = new StringBuilder(word);
                    currentY += lineHeight;
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }

            if (currentLine.length() > 0) {
                guiGraphics.drawString(this.font, currentLine.toString(), 8, currentY, 0x404040, false);
            }
        } else {
            guiGraphics.drawString(this.font, "Kein Dialog verfügbar", 8, 78, 0x808080, false);
        }
    }
}
