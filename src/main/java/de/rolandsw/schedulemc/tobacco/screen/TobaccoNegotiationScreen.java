package de.rolandsw.schedulemc.tobacco.screen;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.tobacco.business.DemandLevel;
import de.rolandsw.schedulemc.tobacco.business.NPCBusinessMetrics;
import de.rolandsw.schedulemc.tobacco.business.PriceCalculator;
import de.rolandsw.schedulemc.tobacco.items.PackagedTobaccoItem;
import de.rolandsw.schedulemc.tobacco.menu.TobaccoNegotiationMenu;
import de.rolandsw.schedulemc.tobacco.network.ModNetworking;
import de.rolandsw.schedulemc.tobacco.network.NegotiationPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für Tabak-Verhandlung mit NPC
 */
public class TobaccoNegotiationScreen extends AbstractContainerScreen<TobaccoNegotiationMenu> {

    private EditBox priceInput;
    private Button offerButton;
    private int selectedSlot = -1;
    private String responseMessage = "";
    private double fairPrice = 0.0;
    private int reputation = 0;
    private int satisfaction = 0;
    private DemandLevel demand = DemandLevel.MEDIUM;

    // Purchase Decision System
    private int purchaseScore = 0;          // 0-100+ Punkte
    private boolean willingToBuy = false;   // Kaufbereitschaft
    private int desiredGrams = 0;           // Gewünschte Menge

    public TobaccoNegotiationScreen(TobaccoNegotiationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 180; // Erhöht für Purchase Willingness Indicator
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;

        // Price Input Field
        priceInput = new EditBox(this.font, x + 10, y + 60, 80, 20, Component.literal("Preis"));
        priceInput.setValue("0.00");
        priceInput.setMaxLength(10);
        addRenderableWidget(priceInput);

        // Offer Button
        offerButton = addRenderableWidget(Button.builder(Component.literal("Anbieten"), button -> {
            makeOffer();
        }).bounds(x + 95, y + 60, 70, 20).build());
        offerButton.active = false;

        // Select Item Buttons (für die ersten 9 Inventory-Slots)
        for (int i = 0; i < 9; i++) {
            final int slot = i;
            addRenderableWidget(Button.builder(Component.literal(String.valueOf(i + 1)), button -> {
                selectSlot(slot);
            }).bounds(x + 8 + i * 18, y + 30, 18, 18).build());
        }

        updateNPCMetrics();
    }

    private void selectSlot(int slot) {
        ItemStack stack = minecraft.player.getInventory().getItem(slot);
        if (stack.getItem() instanceof PackagedTobaccoItem) {
            selectedSlot = slot;
            calculateFairPrice();
            offerButton.active = true;
            responseMessage = "§7Wähle einen Preis und klicke 'Anbieten'";
        } else {
            selectedSlot = -1;
            offerButton.active = false;
            responseMessage = "§cKein verpackter Tabak in diesem Slot!";
        }
    }

    private void updateNPCMetrics() {
        CustomNPCEntity npc = menu.getNpc();
        if (npc != null && minecraft.player != null) {
            NPCBusinessMetrics metrics = new NPCBusinessMetrics(npc);
            reputation = metrics.getReputation(minecraft.player.getStringUUID());
            satisfaction = metrics.getSatisfaction();
            demand = metrics.getDemand();
        }
    }

    /**
     * Wird vom PurchaseDecisionSyncPacket aufgerufen, um die Kaufbereitschaft anzuzeigen
     */
    public void updatePurchaseDecision(int score, boolean willing, int amount) {
        this.purchaseScore = score;
        this.willingToBuy = willing;
        this.desiredGrams = amount;
    }

    private void calculateFairPrice() {
        if (selectedSlot < 0) return;

        CustomNPCEntity npc = menu.getNpc();
        if (npc == null || minecraft.player == null) return;

        ItemStack stack = minecraft.player.getInventory().getItem(selectedSlot);
        if (!(stack.getItem() instanceof PackagedTobaccoItem)) return;

        fairPrice = PriceCalculator.calculateFairPrice(
            PackagedTobaccoItem.getType(stack),
            PackagedTobaccoItem.getQuality(stack),
            PackagedTobaccoItem.getWeight(stack),
            demand,
            reputation,
            satisfaction
        );

        priceInput.setValue(String.format("%.2f", fairPrice));
    }

    private void makeOffer() {
        if (selectedSlot < 0) {
            responseMessage = "§cBitte wähle zuerst einen Tabak aus!";
            return;
        }

        try {
            double offeredPrice = Double.parseDouble(priceInput.getValue().replace(",", "."));
            if (offeredPrice <= 0) {
                responseMessage = "§cPreis muss größer als 0 sein!";
                return;
            }

            // Sende Negotiation Packet an Server
            ModNetworking.sendToServer(new NegotiationPacket(
                menu.getNpcEntityId(),
                selectedSlot,
                offeredPrice
            ));

            // GUI schließen - Server sendet Feedback als Chat-Nachricht
            this.onClose();

        } catch (NumberFormatException e) {
            responseMessage = "§cUngültiger Preis!";
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Dunkler Hintergrund
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF2B2B2B);

        // Hellerer innerer Bereich
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, 0xFF4C4C4C);

        // Header-Bereich
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + 20, 0xFF1E1E1E);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;

        CustomNPCEntity npc = menu.getNpc();
        if (npc != null) {
            // Title
            graphics.drawString(this.font, "Tabak verkaufen", x + 8, y + 6, 0xFFFFFF, false);

            // NPC Metriken (neue Position)
            graphics.drawString(this.font, "§7Ruf: §f" + reputation, x + 8, y + 86, 0xFFFFFF, false);
            graphics.drawString(this.font, "§7Zufriedenheit: §f" + satisfaction, x + 80, y + 86, 0xFFFFFF, false);
            graphics.drawString(this.font, demand.getDisplayName(), x + 8, y + 96, 0xFFFFFF, false);

            // Purchase Willingness Indicator (NEU)
            renderPurchaseWillingnessIndicator(graphics, x + 8, y + 106);

            // Fairer Preis
            if (selectedSlot >= 0) {
                String fairPriceText = String.format("§7Fairer Preis: §a%.2f€", fairPrice);
                graphics.drawString(this.font, fairPriceText, x + 8, y + 126, 0xFFFFFF, false);
            }
        }

        // Response Message
        if (!responseMessage.isEmpty()) {
            graphics.drawString(this.font, responseMessage, x + 8, y + 136, 0xFFFFFF, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Leer lassen - wir rendern alles in render()
    }

    /**
     * Rendert den Kaufbereitschafts-Indikator
     */
    private void renderPurchaseWillingnessIndicator(GuiGraphics graphics, int x, int y) {
        // Titel
        graphics.drawString(this.font, "§7Kaufbereitschaft:", x, y, 0xFFFFFF, false);

        // Score-Balken (100 Pixel breit)
        int barWidth = Math.min((purchaseScore * 100) / 100, 100); // Normalisiert auf 0-100
        graphics.fill(x, y + 10, x + 100, y + 16, 0xFF333333); // Hintergrund (dunkelgrau)

        // Farbe basierend auf Kaufbereitschaft
        int barColor;
        if (willingToBuy) {
            if (purchaseScore >= 70) barColor = 0xFF55FF55; // Grün (hohes Interesse)
            else if (purchaseScore >= 50) barColor = 0xFFFFFF55; // Gelb (mittleres Interesse)
            else barColor = 0xFFFFAA00; // Orange (niedriges Interesse)
        } else {
            barColor = 0xFFFF5555; // Rot (kein Interesse)
        }

        graphics.fill(x, y + 10, x + barWidth, y + 16, barColor);

        // Score-Text
        graphics.drawString(this.font, purchaseScore + "/100", x + 105, y + 10, 0xFFFFFF, false);

        // Kaufmenge oder "Kein Interesse"
        String amountText;
        ChatFormatting color;
        if (willingToBuy && desiredGrams > 0) {
            amountText = "Möchte " + desiredGrams + "g kaufen";
            color = ChatFormatting.GREEN;
        } else {
            amountText = "Kein Interesse";
            color = ChatFormatting.RED;
        }
        graphics.drawString(this.font, color + amountText, x, y + 20, 0xFFFFFF, false);
    }
}
