package de.rolandsw.schedulemc.tobacco.screen;
import de.rolandsw.schedulemc.util.UIColors;
nimport de.rolandsw.schedulemc.util.StringUtils;

import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.production.core.DrugType;
import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.business.DemandLevel;
import de.rolandsw.schedulemc.tobacco.business.NPCBusinessMetrics;
import de.rolandsw.schedulemc.tobacco.business.PriceCalculator;
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
    private EditBox gramsInput;  // NEU: Gramm-Auswahl
    private Button offerButton;
    private int selectedSlot = -1;
    private String responseMessage = "";
    private double fairPrice = 0.0;
    private int reputation = 0;
    private int satisfaction = 0;
    private DemandLevel demand = DemandLevel.MEDIUM;
    private int maxGramsAvailable = 0;  // NEU: Max Gramm im ausgewählten Paket
    private int dealAcceptanceProbability = 0;  // NEU: Wahrscheinlichkeit (0-100%)

    // Purchase Decision System
    private int purchaseScore = 0;          // 0-100+ Punkte
    private boolean willingToBuy = false;   // Kaufbereitschaft
    private int desiredGrams = 0;           // Gewünschte Menge
    private int npcWalletBalance = 0;       // NPC Wallet Balance (vom Server synchronisiert)

    public TobaccoNegotiationScreen(TobaccoNegotiationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 200; // Erhöht für Gramm-Input und Deal-Wahrscheinlichkeit
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;

        // Price Input Field
        priceInput = new EditBox(this.font, x + 10, y + 60, 60, 20, Component.literal("Preis"));
        priceInput.setValue("0.00");
        priceInput.setMaxLength(10);
        priceInput.setResponder(s -> calculateDealProbability());
        addRenderableWidget(priceInput);

        // Grams Input Field (NEU)
        gramsInput = new EditBox(this.font, x + 75, y + 60, 30, 20, Component.literal("Gramm"));
        gramsInput.setValue("1");
        gramsInput.setMaxLength(2);
        gramsInput.setResponder(s -> calculateDealProbability());
        addRenderableWidget(gramsInput);

        // Offer Button
        offerButton = addRenderableWidget(Button.builder(Component.literal("Anbieten"), button -> {
            makeOffer();
        }).bounds(x + 110, y + 60, 60, 20).build());
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
        if (stack.getItem() instanceof PackagedDrugItem &&
            PackagedDrugItem.getDrugType(stack) == DrugType.TOBACCO) {
            selectedSlot = slot;
            maxGramsAvailable = PackagedDrugItem.getWeight(stack);
            gramsInput.setValue(String.valueOf(Math.min(desiredGrams > 0 ? desiredGrams : 1, maxGramsAvailable)));
            calculateFairPrice();
            calculateDealProbability();
            offerButton.active = true;
            responseMessage = "§7Wähle Gramm und Preis, dann klicke 'Anbieten'";
        } else {
            selectedSlot = -1;
            maxGramsAvailable = 0;
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
    public void updatePurchaseDecision(int score, boolean willing, int amount, int wallet) {
        this.purchaseScore = score;
        this.willingToBuy = willing;
        this.desiredGrams = amount;
        this.npcWalletBalance = wallet;
    }

    private void calculateFairPrice() {
        if (selectedSlot < 0) return;

        CustomNPCEntity npc = menu.getNpc();
        if (npc == null || minecraft.player == null) return;

        ItemStack stack = minecraft.player.getInventory().getItem(selectedSlot);
        if (!(stack.getItem() instanceof PackagedDrugItem) ||
            PackagedDrugItem.getDrugType(stack) != DrugType.TOBACCO) return;

        // Parse Type und Quality aus PackagedDrugItem
        String variantStr = PackagedDrugItem.getVariant(stack);
        TobaccoType type = variantStr != null ? TobaccoType.valueOf(variantStr.split("\\.")[1]) : TobaccoType.VIRGINIA;

        String qualityStr = PackagedDrugItem.getQuality(stack);
        TobaccoQuality quality = qualityStr != null ? TobaccoQuality.valueOf(qualityStr.split("\\.")[1]) : TobaccoQuality.GUT;

        fairPrice = PriceCalculator.calculateFairPrice(
            type,
            quality,
            PackagedDrugItem.getWeight(stack),
            demand,
            reputation,
            satisfaction
        );

        priceInput.setValue(String.format("%.2f", fairPrice));
    }

    /**
     * Berechnet die Deal-Akzeptanz-Wahrscheinlichkeit basierend auf Preis, Menge und Wallet
     */
    private void calculateDealProbability() {
        if (selectedSlot < 0) {
            dealAcceptanceProbability = 0;
            return;
        }

        CustomNPCEntity npc = menu.getNpc();
        if (npc == null) {
            dealAcceptanceProbability = 0;
            return;
        }

        try {
            double offeredPrice = Double.parseDouble(priceInput.getValue().replace(",", "."));
            int offeredGrams = Integer.parseInt(gramsInput.getValue());

            if (offeredPrice <= 0 || offeredGrams <= 0 || offeredGrams > maxGramsAvailable) {
                dealAcceptanceProbability = 0;
                return;
            }

            // Preis pro Gramm berechnen
            double pricePerGram = offeredPrice / offeredGrams;
            double fairPricePerGram = fairPrice / maxGramsAvailable;

            // 1. Preis-Faktor (0-50%)
            double priceRatio = pricePerGram / fairPricePerGram;
            int priceScore = 0;
            if (priceRatio >= 1.2) priceScore = 50; // 20% über Fair Price = 50%
            else if (priceRatio >= 1.0) priceScore = (int)(25 + (priceRatio - 1.0) * 125); // 1.0-1.2 = 25-50%
            else if (priceRatio >= 0.8) priceScore = (int)(priceRatio * 25); // 0.8-1.0 = 20-25%
            else priceScore = (int)(priceRatio * 25); // < 0.8 = < 20%

            // 2. Mengen-Faktor (0-30%)
            int gramsScore = 0;
            if (desiredGrams > 0) {
                if (offeredGrams <= desiredGrams) {
                    // Gewünschte Menge oder weniger = Bonus
                    gramsScore = 30;
                } else {
                    // Mehr als gewünscht = Malus
                    float excessRatio = (float)(offeredGrams - desiredGrams) / desiredGrams;
                    gramsScore = Math.max(0, (int)(30 - excessRatio * 30));
                }
            } else {
                // NPC will nicht kaufen = niedrige Wahrscheinlichkeit
                gramsScore = 5;
            }

            // 3. Wallet-Check (0-20%) - verwende synchronisierten Wert
            int walletScore = 0;
            if (offeredPrice <= npcWalletBalance) {
                // NPC kann sich das leisten
                float budgetUsage = (float)offeredPrice / npcWalletBalance;
                if (budgetUsage <= 0.3f) walletScore = 20; // Nur 30% Budget = gut
                else if (budgetUsage <= 0.5f) walletScore = 15; // 30-50% = ok
                else if (budgetUsage <= 0.7f) walletScore = 10; // 50-70% = kritisch
                else walletScore = 5; // 70-100% = sehr kritisch
            } else {
                // NPC kann sich das nicht leisten
                walletScore = 0;
            }

            // Gesamt: Preis (0-50%) + Menge (0-30%) + Wallet (0-20%) = 0-100%
            dealAcceptanceProbability = Math.min(100, priceScore + gramsScore + walletScore);

        } catch (NumberFormatException e) {
            dealAcceptanceProbability = 0;
        }
    }

    private void makeOffer() {
        if (selectedSlot < 0) {
            responseMessage = "§cBitte wähle zuerst einen Tabak aus!";
            return;
        }

        try {
            double offeredPrice = Double.parseDouble(priceInput.getValue().replace(",", "."));
            int offeredGrams = Integer.parseInt(gramsInput.getValue());

            if (offeredPrice <= 0) {
                responseMessage = "§cPreis muss größer als 0 sein!";
                return;
            }

            if (offeredGrams <= 0 || offeredGrams > maxGramsAvailable) {
                responseMessage = "§cUngültige Grammzahl! (1-" + maxGramsAvailable + "g)";
                return;
            }

            // Sende Negotiation Packet an Server mit Gramm-Anzahl
            ModNetworking.sendToServer(new NegotiationPacket(
                menu.getNpcEntityId(),
                selectedSlot,
                offeredPrice,
                offeredGrams  // NEU: Gramm-Anzahl senden
            ));

            // GUI schließen - Server sendet Feedback als Chat-Nachricht
            this.onClose();

        } catch (NumberFormatException e) {
            responseMessage = "§cUngültige Eingabe!";
        }
    }    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true; // Consume event, prevent closing
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }



    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Dunkler Hintergrund
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, UIColors.BACKGROUND_MEDIUM);

        // Hellerer innerer Bereich
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, UIColors.BACKGROUND_PANEL);

        // Header-Bereich
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + 20, UIColors.BACKGROUND_DARK);
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

            // Input Labels
            graphics.drawString(this.font, "§7Preis €", x + 10, y + 50, 0xFFFFFF, false);
            graphics.drawString(this.font, "§7Gramm", x + 75, y + 50, 0xFFFFFF, false);

            // NPC Wallet Balance (verwende synchronisierten Wert vom Server)
            graphics.drawString(this.font, "§7NPC Geld: §e" + npcWalletBalance + "€", x + 8, y + 20, 0xFFFFFF, false);

            // NPC Metriken
            graphics.drawString(this.font, "§7Ruf: §f" + reputation, x + 8, y + 86, 0xFFFFFF, false);
            graphics.drawString(this.font, "§7Zufriedenheit: §f" + satisfaction, x + 80, y + 86, 0xFFFFFF, false);
            graphics.drawString(this.font, demand.getDisplayName(), x + 8, y + 96, 0xFFFFFF, false);

            // Purchase Willingness Indicator
            renderPurchaseWillingnessIndicator(graphics, x + 8, y + 106);

            // Fairer Preis und Deal-Wahrscheinlichkeit
            if (selectedSlot >= 0) {
                String fairPriceText = String.format("§7Fairer Preis: §a%.2f€ §7(für %dg)", fairPrice, maxGramsAvailable);
                graphics.drawString(this.font, fairPriceText, x + 8, y + 126, 0xFFFFFF, false);

                // Deal-Wahrscheinlichkeit mit Farbe
                String probText;
                ChatFormatting probColor;
                if (dealAcceptanceProbability >= 70) {
                    probColor = ChatFormatting.GREEN;
                    probText = "Sehr wahrscheinlich";
                } else if (dealAcceptanceProbability >= 50) {
                    probColor = ChatFormatting.YELLOW;
                    probText = "Wahrscheinlich";
                } else if (dealAcceptanceProbability >= 30) {
                    probColor = ChatFormatting.GOLD;
                    probText = "Möglich";
                } else if (dealAcceptanceProbability > 0) {
                    probColor = ChatFormatting.RED;
                    probText = "Unwahrscheinlich";
                } else {
                    probColor = ChatFormatting.DARK_RED;
                    probText = "Unmöglich";
                }

                String dealProbText = String.format("§7Deal-Akzeptanz: %s%d%% §7(%s)",
                    probColor, dealAcceptanceProbability, probText);
                graphics.drawString(this.font, dealProbText, x + 8, y + 136, 0xFFFFFF, false);
            }
        }

        // Response Message
        if (!responseMessage.isEmpty()) {
            graphics.drawString(this.font, responseMessage, x + 8, y + 146, 0xFFFFFF, false);
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
        graphics.fill(x, y + 10, x + 100, y + 16, UIColors.BACKGROUND_LIGHT); // Hintergrund (dunkelgrau)

        // Farbe basierend auf Kaufbereitschaft
        int barColor;
        if (willingToBuy) {
            if (purchaseScore >= 70) barColor = UIColors.ACCENT_LIME; // Grün (hohes Interesse)
            else if (purchaseScore >= 50) barColor = 0xFFFFFF55; // Gelb (mittleres Interesse)
            else barColor = UIColors.ACCENT_ORANGE; // Orange (niedriges Interesse)
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
