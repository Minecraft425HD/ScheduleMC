package de.rolandsw.schedulemc.npc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.client.screen.apps.ChatScreen;
import de.rolandsw.schedulemc.messaging.Conversation;
import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.npc.data.BankCategory;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.menu.NPCInteractionMenu;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.NPCActionPacket;
import de.rolandsw.schedulemc.npc.network.OpenMerchantShopPacket;
import de.rolandsw.schedulemc.npc.network.OpenBankerMenuPacket;
import de.rolandsw.schedulemc.npc.network.OpenBoerseMenuPacket;
import de.rolandsw.schedulemc.npc.network.OpenCreditAdvisorMenuPacket;
import de.rolandsw.schedulemc.tobacco.network.ModNetworking;
import de.rolandsw.schedulemc.tobacco.network.OpenTobaccoNegotiationPacket;
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
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/gui/npc_interaction.png");

    private Button dialogButton;
    private Button shopBuyButton;
    private Button shopSellButton;
    private Button bankerButton;
    private Button boerseButton;
    private Button creditAdvisorButton;

    public NPCInteractionScreen(NPCInteractionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 100; // Ohne Player-Inventar
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        CustomNPCEntity npc = menu.getNpc();
        boolean canMessage = npc != null &&
            (npc.getNpcType() == NPCType.BEWOHNER || npc.getNpcType() == NPCType.VERKAEUFER);
        boolean isBank = npc != null && npc.getNpcType() == NPCType.BANK;
        boolean isCreditAdvisor = isBank && npc != null && npc.getBankCategory() == BankCategory.KREDITBERATER;

        // Dialog/Chat Button - opens chat for BEWOHNER and VERKAEUFER, dialog for others
        Component buttonLabel = canMessage ? Component.translatable("gui.npc.interaction.chat") : Component.translatable("gui.npc.interaction.dialog");
        dialogButton = addRenderableWidget(Button.builder(buttonLabel, button -> {
            if (canMessage) {
                openMessage();
            } else {
                openDialog();
            }
        }).bounds(x + 8, y + 30, 160, 20).build());

        // Bank-Buttons (nur für BANK NPCs - je nach Kategorie unterschiedlich)
        bankerButton = addRenderableWidget(Button.builder(Component.translatable("gui.npc.banking"), button -> {
            openBankerMenu();
        }).bounds(x + 8, y + 54, 78, 20).build());
        bankerButton.visible = isBank && !isCreditAdvisor;

        boerseButton = addRenderableWidget(Button.builder(Component.translatable("gui.npc.stock_market"), button -> {
            openBoerseMenu();
        }).bounds(x + 90, y + 54, 78, 20).build());
        boerseButton.visible = isBank && !isCreditAdvisor;

        // Kreditberater Button (nur für KREDITBERATER NPCs)
        creditAdvisorButton = addRenderableWidget(Button.builder(Component.translatable("gui.npc.apply_credit"), button -> {
            openCreditAdvisorMenu();
        }).bounds(x + 8, y + 54, 160, 20).build());
        creditAdvisorButton.visible = isCreditAdvisor;

        // Shop Verkaufen Button (nicht für Bank-NPCs)
        shopSellButton = addRenderableWidget(Button.builder(Component.translatable("gui.npc.interaction.sell"), button -> {
            openShopSell();
        }).bounds(x + 8, y + 54, 78, 20).build());
        shopSellButton.visible = !isBank;

        // Shop Kaufen Button (nicht für Bank-NPCs)
        shopBuyButton = addRenderableWidget(Button.builder(Component.translatable("gui.common.buy"), button -> {
            openShopBuy();
        }).bounds(x + 90, y + 54, 78, 20).build());
        shopBuyButton.visible = !isBank;

        // Deaktiviere Kaufen-Button wenn NPC außerhalb der Arbeitszeiten ist
        if (npc != null && minecraft != null && minecraft.level != null) {
            boolean withinWorkingHours = npc.getNpcData().isWithinWorkingHours(minecraft.level);
            shopBuyButton.active = withinWorkingHours;
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
        }
    }

    /**
     * Öffnet Nachricht (WhatsApp chat)
     */
    private void openMessage() {
        CustomNPCEntity npc = menu.getNpc();
        if (npc != null && minecraft != null && minecraft.player != null) {
            // Get or create conversation with this NPC
            Conversation conversation = MessageManager.getOrCreateConversation(
                minecraft.player.getUUID(),
                npc.getUUID(),
                npc.getNpcName(),
                false // NPC, not player
            );

            // Open chat screen
            minecraft.setScreen(new ChatScreen(this, conversation));
        }
    }

    /**
     * Öffnet Shop (Verkaufen) - Tabak-Verhandlung
     */
    private void openShopSell() {
        CustomNPCEntity npc = menu.getNpc();
        if (npc != null && npc.getNpcData().hasInventoryAndWallet()) {
            // Sende Packet an Server um Tobacco Negotiation GUI zu öffnen
            ModNetworking.sendToServer(new OpenTobaccoNegotiationPacket(menu.getEntityId()));
            // Schließe aktuelles GUI - das Negotiation-GUI wird vom Server geöffnet
            this.onClose();
        } else {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.translatable("message.npc.does_not_buy_items"));
            }
        }
    }

    /**
     * Öffnet Shop (Kaufen)
     */
    private void openShopBuy() {
        CustomNPCEntity npc = menu.getNpc();
        if (npc != null && (npc.getNpcType() == NPCType.VERKAEUFER || npc.getNpcType() == NPCType.ABSCHLEPPER)) {
            // Sende Packet an Server um Shop zu öffnen
            NPCNetworkHandler.sendToServer(new OpenMerchantShopPacket(menu.getEntityId()));
            // Schließe aktuelles GUI - das Shop-GUI wird vom Server geöffnet
            this.onClose();
        } else {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.translatable("message.npc.not_seller"));
            }
        }
    }

    /**
     * Öffnet Banker-Menü
     */
    private void openBankerMenu() {
        CustomNPCEntity npc = menu.getNpc();
        if (npc != null && npc.getNpcType() == NPCType.BANK) {
            // Sende Packet an Server um Banker-Menü zu öffnen
            NPCNetworkHandler.sendToServer(new OpenBankerMenuPacket(menu.getEntityId()));
            // Schließe aktuelles GUI - das Banker-GUI wird vom Server geöffnet
            this.onClose();
        }
    }

    /**
     * Öffnet Börsen-Menü
     */
    private void openBoerseMenu() {
        CustomNPCEntity npc = menu.getNpc();
        if (npc != null && npc.getNpcType() == NPCType.BANK) {
            // Sende Packet an Server um Börsen-Menü zu öffnen
            NPCNetworkHandler.sendToServer(new OpenBoerseMenuPacket(menu.getEntityId()));
            // Schließe aktuelles GUI - das Börsen-GUI wird vom Server geöffnet
            this.onClose();
        }
    }

    /**
     * Öffnet Kreditberater-Menü
     */
    private void openCreditAdvisorMenu() {
        CustomNPCEntity npc = menu.getNpc();
        if (npc != null && npc.getNpcType() == NPCType.BANK &&
            npc.getBankCategory() == BankCategory.KREDITBERATER) {
            // Sende Packet an Server um Kreditberater-Menü zu öffnen
            NPCNetworkHandler.sendToServer(new OpenCreditAdvisorMenuPacket(menu.getEntityId()));
            // Schließe aktuelles GUI - das Kreditberater-GUI wird vom Server geöffnet
            this.onClose();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true; // Consume event, prevent closing
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
    }
}
