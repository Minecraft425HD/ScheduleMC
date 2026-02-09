package de.rolandsw.schedulemc.tobacco.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.npc.data.NPCPersonality;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.personality.NPCPersonalityTrait;
import de.rolandsw.schedulemc.production.core.DrugType;
import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import de.rolandsw.schedulemc.tobacco.TobaccoQuality;
import de.rolandsw.schedulemc.tobacco.TobaccoType;
import de.rolandsw.schedulemc.tobacco.business.*;
import de.rolandsw.schedulemc.tobacco.menu.TobaccoNegotiationMenu;
import de.rolandsw.schedulemc.tobacco.network.ModNetworking;
import de.rolandsw.schedulemc.tobacco.network.NegotiationPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für Tabak-Verhandlung mit NPC - Redesigned
 *
 * Layout (Vorschlag C - Vertikal Gestapelt):
 * ┌─────────────────────────────────────────────────────────────┐
 * │                     VERHANDLUNG · [NPC_NAME]                │
 * ├─────────────────────────────────────────────────────────────┤
 * │  ┌────────────────────┐  ┌───────────────────────────────┐ │
 * │  │  INVENTAR (3x3)    │  │  NPC · PERSONALITY · TRAIT    │ │
 * │  │  [0][1][2]         │  │  Sucht: TABAK █████░ 85%      │ │
 * │  │  [3][4][5]         │  │  Wallet: €180                 │ │
 * │  │  [6][7][8]         │  │  Beziehung: +45               │ │
 * │  │                    │  │  SCORE: [████████░░] 73/100   │ │
 * │  │  ▸ Slot X: Xg Type │  │  Risiko: ▓▓░░░░░░░░ 20%       │ │
 * │  │  ▸ Wert: X€        │  │  Stimmung: 😊━━━▲━━━😡 (2/5)  │ │
 * │  └────────────────────┘  └───────────────────────────────┘ │
 * ├─────────────────────────────────────────────────────────────┤
 * │  PREIS-SLIDER                                               │
 * │  10€    15€    20€    25€    30€    35€    40€              │
 * │  🔴━━━━🟡━━━━🟢━━━━🟢━━━━🟡━━━━🔴━━━━🔴                     │
 * │           ▲              ▲                                  │
 * │      NPC-Ziel       Dein Preis                              │
 * │  [ ◀◀ -5€ ]  [ ◀ -1€ ]   26.50€   [ +1€ ▶ ]  [ +5€ ▶▶ ]    │
 * ├─────────────────────────────────────────────────────────────┤
 * │  NPC: "Das ist mir noch etwas zu viel..."                   │
 * │                        [ ANBIETEN ]                         │
 * │  ░░░░░░░░░░░░░░░░ BEREITS GEKAUFT ░░░░░░░░░░░░░░░░░        │
 * └─────────────────────────────────────────────────────────────┘
 */
public class TobaccoNegotiationScreen extends AbstractContainerScreen<TobaccoNegotiationMenu> {

    // ═══════════════════════════════════════════════════════════
    // LAYOUT CONSTANTS
    // ═══════════════════════════════════════════════════════════

    private static final int GUI_WIDTH = 286;
    private static final int GUI_HEIGHT = 270;

    // Inventar-Bereich (links)
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 24;
    private static final int INVENTORY_WIDTH = 62;
    private static final int INVENTORY_HEIGHT = 100;

    // NPC-Info-Bereich (rechts)
    private static final int NPC_INFO_X = 80;
    private static final int NPC_INFO_Y = 24;
    private static final int NPC_INFO_WIDTH = 198;
    private static final int NPC_INFO_HEIGHT = 100;

    // Slider-Bereich
    private static final int SLIDER_X = 8;
    private static final int SLIDER_Y = 132;
    private static final int SLIDER_WIDTH = 270;
    private static final int SLIDER_HEIGHT = 60;

    // Response-Bereich
    private static final int RESPONSE_Y = 198;

    // Farben
    private static final int COLOR_BACKGROUND = 0xFF1A1A1A;
    private static final int COLOR_PANEL = 0xFF2D2D2D;
    private static final int COLOR_BORDER = 0xFF404040;
    private static final int COLOR_HEADER = 0xFF3A3A3A;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_TEXT_SECONDARY = 0xFFAAAAAA;
    private static final int COLOR_GREEN = 0xFF55FF55;
    private static final int COLOR_YELLOW = 0xFFFFFF55;
    private static final int COLOR_RED = 0xFFFF5555;
    private static final int COLOR_SLIDER_BG = 0xFF333333;

    // ═══════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════

    private int selectedSlot = -1;
    private double currentPrice = 0.0;
    private double fairPrice = 0.0;
    private double npcTargetPrice = 0.0;
    private double minPrice = 0.0;
    private double maxPrice = 0.0;

    // Score-Daten (vom Server synchronisiert)
    private NegotiationScoreCalculator scoreData;
    private NegotiationState negotiationState;

    // NPC-Daten
    private NPCPersonality personality;
    private NPCPersonalityTrait trait;
    private int addictionLevel = 0;
    private int walletBalance = 0;
    private int relationLevel = 0;

    // Buttons
    private Button offerButton;
    // Slider state - ersetzt Buttons
    private boolean isDraggingSlider = false;
    private int sliderBarX;
    private int sliderBarY;
    private int sliderBarWidth;

    // Response
    private String npcResponse = "";
    private boolean hasCooldown = false;

    // Server-synchronisierte Werte für Stimmung und Runde
    private float serverMood = 100.0f;
    private int serverRound = 0;
    private int serverMaxRounds = 6;

    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════

    public TobaccoNegotiationScreen(TobaccoNegotiationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = this.imageHeight + 10; // Verstecke Standard-Label
        this.titleLabelY = -100; // Verstecke Standard-Titel
    }

    // ═══════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;
        int centerX = x + (GUI_WIDTH / 2);

        // Slider-Position berechnen (für Maus-Interaktion)
        sliderBarX = x + SLIDER_X + 8;
        sliderBarY = y + SLIDER_Y + 18;
        sliderBarWidth = SLIDER_WIDTH - 16;

        // Anbieten-Button (einziger Button)
        offerButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.negotiation.button.offer"),
            btn -> makeOffer()
        ).bounds(centerX - 40, y + RESPONSE_Y + 32, 80, 20).build());
        offerButton.active = false;

        // Lade initiale NPC-Daten
        loadNPCData();

        // Check Cooldown
        hasCooldown = menu.hasCooldown();
        updateButtonStates();
    }

    // ═══════════════════════════════════════════════════════════
    // DATA LOADING
    // ═══════════════════════════════════════════════════════════

    private void loadNPCData() {
        CustomNPCEntity npc = menu.getNpc();
        if (npc == null || minecraft == null || minecraft.player == null) return;

        // Lade Persönlichkeit
        String personalityStr = npc.getNpcData().getCustomData().getString("personality");
        if (!personalityStr.isEmpty()) {
            try {
                personality = NPCPersonality.valueOf(personalityStr);
            } catch (IllegalArgumentException e) {
                personality = NPCPersonality.AUSGEWOGEN;
            }
        } else {
            personality = NPCPersonality.AUSGEWOGEN;
        }

        // Lade Trait
        String traitStr = npc.getNpcData().getCustomData().getString("personalityTrait");
        if (!traitStr.isEmpty()) {
            try {
                trait = NPCPersonalityTrait.valueOf(traitStr);
            } catch (IllegalArgumentException e) {
                trait = NPCPersonalityTrait.NEUTRAL;
            }
        } else {
            trait = NPCPersonalityTrait.NEUTRAL;
        }

        // Wallet
        walletBalance = npc.getNpcData().getWallet();

        // Initialisiere Verhandlungs-State
        negotiationState = new NegotiationState(personality, trait);
    }

    /**
     * Wird vom Server aufgerufen um Score-Daten zu synchronisieren
     */
    public void updateScoreData(NegotiationScoreCalculator score) {
        this.scoreData = score;
        if (score != null) {
            this.personality = score.getPersonality();
            this.trait = score.getTrait();
            this.addictionLevel = score.getAddictionLevel();
            this.walletBalance = score.getWalletBalance();
            this.relationLevel = score.getNpcRelationLevel();
        }
    }

    /**
     * Wird vom Server aufgerufen um Verhandlungs-State zu synchronisieren
     */
    public void updateNegotiationState(NegotiationState state) {
        this.negotiationState = state;
        this.npcResponse = state.getLastNPCResponse();
    }

    /**
     * Wird vom PurchaseDecisionSyncPacket aufgerufen, um die Kaufbereitschaft anzuzeigen
     * (Kompatibilität mit altem Packet-System)
     */
    public void updatePurchaseDecision(int score, boolean willing, int amount, int wallet) {
        // Aktualisiere lokale Werte basierend auf dem alten Packet-Format
        this.walletBalance = wallet;
        // Score wird im neuen System über updateScoreData gesetzt
    }

    /**
     * Setzt den Cooldown-Status
     */
    public void setCooldown(boolean cooldown) {
        this.hasCooldown = cooldown;
        updateButtonStates();
    }

    // ═══════════════════════════════════════════════════════════
    // SLOT SELECTION
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void slotClicked(@NotNull Slot slot, int slotId, int mouseButton, @NotNull net.minecraft.world.inventory.ClickType type) {
        // Nur Linksklick für Auswahl
        if (mouseButton != 0) return;
        if (slotId < 0 || slotId >= TobaccoNegotiationMenu.HOTBAR_SLOT_COUNT) return;

        // Prüfe ob Slot gültiges Item enthält
        if (menu.isSlotValid(slotId)) {
            selectedSlot = slotId;
            menu.selectSlot(slotId);
            calculatePrices();
            updateButtonStates();
        }
    }

    private void calculatePrices() {
        if (selectedSlot < 0) return;

        ItemStack stack = menu.getSelectedItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof PackagedDrugItem)) return;

        CustomNPCEntity npc = menu.getNpc();
        if (npc == null) return;

        // Get DrugType to handle different types correctly
        DrugType drugType = PackagedDrugItem.getDrugType(stack);
        int weight = PackagedDrugItem.getWeight(stack);

        // Get NPC metrics
        NPCBusinessMetrics metrics = new NPCBusinessMetrics(npc);
        int reputation = metrics.getReputation(minecraft.player.getStringUUID());
        int satisfaction = metrics.getSatisfaction();
        DemandLevel demand = metrics.getDemand();

        // Calculate prices based on DrugType
        if (drugType == DrugType.TOBACCO) {
            // Tobacco-specific price calculation
            String variantStr = PackagedDrugItem.getVariant(stack);
            TobaccoType type = TobaccoType.VIRGINIA;
            if (variantStr != null) {
                try {
                    String[] vParts = variantStr.split("\\.");
                    if (vParts.length >= 2) type = TobaccoType.valueOf(vParts[1]);
                } catch (IllegalArgumentException ignored) {}
            }

            String qualityStr = PackagedDrugItem.getQuality(stack);
            TobaccoQuality quality = TobaccoQuality.GUT;
            if (qualityStr != null) {
                try {
                    String[] qParts = qualityStr.split("\\.");
                    if (qParts.length >= 2) quality = TobaccoQuality.valueOf(qParts[1]);
                } catch (IllegalArgumentException ignored) {}
            }

            fairPrice = PriceCalculator.calculateFairPrice(type, quality, weight, demand, reputation, satisfaction);
            npcTargetPrice = PriceCalculator.calculateIdealPrice(fairPrice);
        } else {
            // Generic price calculation for other drugs (Cannabis, etc.)
            double basePrice = PackagedDrugItem.calculatePrice(stack);

            // Apply NPC modifiers
            double demandMultiplier = demand.getPriceMultiplier();
            double reputationMultiplier = 0.85 + (reputation / 100.0 * 0.35);
            double satisfactionMultiplier = 0.80 + (satisfaction / 100.0 * 0.30);

            fairPrice = basePrice * demandMultiplier * reputationMultiplier * satisfactionMultiplier;
            npcTargetPrice = fairPrice * 0.85;
        }

        // Dynamische Preisspanne: 0 bis 200% des fairen Preises
        minPrice = 0.0;
        maxPrice = fairPrice * 2.0;

        // Setze initialen Preis auf Fair Price
        currentPrice = fairPrice;
    }

    // ═══════════════════════════════════════════════════════════
    // PRICE ADJUSTMENT
    // ═══════════════════════════════════════════════════════════

    private void adjustPrice(double delta) {
        currentPrice = Math.max(minPrice, Math.min(maxPrice, currentPrice + delta));
    }

    private void updateButtonStates() {
        boolean canOffer = selectedSlot >= 0 && !hasCooldown && currentPrice > 0;
        offerButton.active = canOffer;
    }

    // ═══════════════════════════════════════════════════════════
    // OFFER SUBMISSION
    // ═══════════════════════════════════════════════════════════

    private void makeOffer() {
        if (selectedSlot < 0 || hasCooldown) return;

        ItemStack stack = menu.getSelectedItem();
        if (stack.isEmpty()) return;

        int weight = PackagedDrugItem.getWeight(stack);

        // Sende Angebot an Server
        ModNetworking.sendToServer(new NegotiationPacket(
            menu.getNpcEntityId(),
            selectedSlot,
            currentPrice,
            weight
        ));

        // GUI bleibt offen - wird vom Server-Response geschlossen wenn Deal akzeptiert
        // Zeige "Warte auf Antwort..." Status
        npcResponse = "Warte auf Antwort...";
        offerButton.active = false;  // Verhindere mehrfaches Klicken
    }

    /**
     * Wird vom Server aufgerufen (via NegotiationResponsePacket) um die GUI zu aktualisieren
     */
    public void updateNegotiationResponse(String message, double counterOffer, float mood, int round, int maxRounds) {
        this.npcResponse = message;
        this.npcTargetPrice = counterOffer;  // NPC's Gegenangebot als neuer Zielpreis

        // Aktualisiere lokalen NegotiationState mit den Server-Daten
        if (this.negotiationState != null) {
            // Wir müssen die Stimmung und Runde irgendwie speichern
            // Da wir keinen direkten Zugriff auf die internen Felder haben,
            // speichern wir sie lokal
            this.serverMood = mood;
            this.serverRound = round;
            this.serverMaxRounds = maxRounds;
        }

        // Reaktiviere den Offer-Button
        updateButtonStates();
    }

    // ═══════════════════════════════════════════════════════════
    // RENDERING
    // ═══════════════════════════════════════════════════════════

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = this.leftPos;
        int y = this.topPos;

        // Haupt-Hintergrund
        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, COLOR_BACKGROUND);

        // Header
        graphics.fill(x, y, x + GUI_WIDTH, y + 20, COLOR_HEADER);

        // Inventar-Panel
        renderPanel(graphics, x + INVENTORY_X - 2, y + INVENTORY_Y - 2, INVENTORY_WIDTH + 4, INVENTORY_HEIGHT + 4);

        // NPC-Info-Panel
        renderPanel(graphics, x + NPC_INFO_X - 2, y + NPC_INFO_Y - 2, NPC_INFO_WIDTH + 4, NPC_INFO_HEIGHT + 4);

        // Slider-Panel
        renderPanel(graphics, x + SLIDER_X - 2, y + SLIDER_Y - 2, SLIDER_WIDTH + 4, SLIDER_HEIGHT + 4);

        // Response-Panel
        renderPanel(graphics, x + 6, y + RESPONSE_Y - 2, GUI_WIDTH - 12, 50);

        // Slot-Hintergründe rendern
        renderSlotBackgrounds(graphics, x, y);
    }

    private void renderPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, COLOR_BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, COLOR_PANEL);
    }

    private void renderSlotBackgrounds(GuiGraphics graphics, int baseX, int baseY) {
        for (int i = 0; i < TobaccoNegotiationMenu.HOTBAR_SLOT_COUNT; i++) {
            Slot slot = menu.slots.get(i);
            int slotX = baseX + slot.x - 1;
            int slotY = baseY + slot.y - 1;

            // Highlight für ausgewählten Slot
            if (i == selectedSlot) {
                graphics.fill(slotX, slotY, slotX + 18, slotY + 18, COLOR_GREEN);
            } else if (menu.isSlotValid(i)) {
                graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF555555);
            } else {
                graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF333333);
            }

            // Innerer Slot-Hintergrund
            graphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xFF1A1A1A);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        // Titel
        CustomNPCEntity npc = menu.getNpc();
        String npcName = npc != null ? npc.getNpcName() : "NPC";
        String title = Component.translatable("gui.negotiation.title", npcName).getString();
        int titleWidth = font.width(title);
        graphics.drawString(font, title, (GUI_WIDTH - titleWidth) / 2, 6, COLOR_TEXT, false);

        // Inventar-Bereich
        renderInventorySection(graphics);

        // NPC-Info-Bereich
        renderNPCInfoSection(graphics);

        // Slider-Bereich
        renderSliderSection(graphics);

        // Response-Bereich
        renderResponseSection(graphics);
    }

    private void renderInventorySection(GuiGraphics graphics) {
        int x = INVENTORY_X;
        int y = INVENTORY_Y + INVENTORY_HEIGHT - 22;

        if (selectedSlot >= 0) {
            ItemStack stack = menu.getSelectedItem();
            if (!stack.isEmpty() && stack.getItem() instanceof PackagedDrugItem) {
                int weight = PackagedDrugItem.getWeight(stack);
                String variantStr = PackagedDrugItem.getVariant(stack);
                String typeDisplay = "Unknown";
                if (variantStr != null) {
                    String[] vp = variantStr.split("\\.");
                    if (vp.length >= 2) typeDisplay = vp[1];
                }
                String qualityStr = PackagedDrugItem.getQuality(stack);
                String qualityDisplay = "";
                if (qualityStr != null) {
                    String[] qp = qualityStr.split("\\.");
                    if (qp.length >= 2) qualityDisplay = qp[1];
                }

                graphics.drawString(font, Component.translatable("gui.negotiation.selected_item",
                    weight, typeDisplay, qualityDisplay).getString(), x, y, COLOR_TEXT, false);
                graphics.drawString(font, Component.translatable("gui.negotiation.fair_price",
                    String.format("%.2f", fairPrice)).getString(), x, y + 11, COLOR_TEXT_SECONDARY, false);
            }
        } else {
            graphics.drawString(font, Component.translatable("gui.negotiation.select_item").getString(),
                x, y, COLOR_TEXT_SECONDARY, false);
        }
    }

    private void renderNPCInfoSection(GuiGraphics graphics) {
        int x = NPC_INFO_X;
        int y = NPC_INFO_Y;

        // Persönlichkeit & Trait
        String personalityStr = personality != null ? personality.name() : "?";
        String traitStr = trait != null ? trait.getDisplayName() : "?";
        graphics.drawString(font, personalityStr + " · " + traitStr, x, y, COLOR_TEXT, false);

        // Suchtlevel
        y += 14;
        String addictionStr = Component.translatable("gui.negotiation.addiction", addictionLevel).getString();
        graphics.drawString(font, addictionStr, x, y, COLOR_TEXT_SECONDARY, false);
        renderProgressBar(graphics, x + 90, y, 100, 8, addictionLevel / 100.0f, COLOR_RED);

        // Wallet
        y += 14;
        graphics.drawString(font, Component.translatable("gui.negotiation.wallet", walletBalance).getString(),
            x, y, COLOR_TEXT_SECONDARY, false);

        // Beziehung
        y += 14;
        String relationStr = (relationLevel >= 0 ? "+" : "") + relationLevel;
        graphics.drawString(font, Component.translatable("gui.negotiation.relation", relationStr).getString(),
            x, y, COLOR_TEXT_SECONDARY, false);

        // Score
        y += 16;
        int score = scoreData != null ? scoreData.getTotalScore() : 50;
        graphics.drawString(font, Component.translatable("gui.negotiation.score").getString(), x, y, COLOR_TEXT, false);
        renderProgressBar(graphics, x + 60, y, 105, 10, score / 100.0f, getScoreColor(score));
        graphics.drawString(font, score + "/100", x + 168, y, COLOR_TEXT, false);

        // Risiko
        y += 16;
        int risk = scoreData != null ? scoreData.getAbortRisk() : 20;
        graphics.drawString(font, Component.translatable("gui.negotiation.risk").getString(), x, y, COLOR_TEXT_SECONDARY, false);
        renderProgressBar(graphics, x + 60, y, 70, 8, risk / 100.0f, COLOR_RED);
        graphics.drawString(font, risk + "%", x + 135, y, COLOR_TEXT_SECONDARY, false);

        // Stimmung
        y += 14;
        renderMoodIndicator(graphics, x, y);
    }

    private void renderSliderSection(GuiGraphics graphics) {
        int x = SLIDER_X;
        int y = SLIDER_Y;

        // Slider-Leiste
        int sliderWidth = SLIDER_WIDTH - 16;
        int sliderX = x + 8;
        int sliderY = y + 20;

        // Slider-Hintergrund
        graphics.fill(sliderX, sliderY, sliderX + sliderWidth, sliderY + 12, COLOR_SLIDER_BG);

        if (selectedSlot >= 0 && maxPrice > minPrice) {
            // Preis-Zonen rendern
            renderPriceZones(graphics, sliderX, sliderY, sliderWidth);

            // NPC-Zielpreis Marker (Dreieck unten)
            int npcTargetPos = (int) ((npcTargetPrice - minPrice) / (maxPrice - minPrice) * sliderWidth);
            graphics.fill(sliderX + npcTargetPos - 1, sliderY + 12, sliderX + npcTargetPos + 1, sliderY + 16, COLOR_YELLOW);
            graphics.drawString(font, "NPC", sliderX + npcTargetPos - 8, sliderY + 17, COLOR_YELLOW, false);

            // Spieler-Preis Griff (großer Griff für Drag)
            int playerPos = (int) ((currentPrice - minPrice) / (maxPrice - minPrice) * sliderWidth);
            int handleWidth = 8;
            int handleHeight = 20;
            int handleX = sliderX + playerPos - handleWidth / 2;
            int handleY = sliderY - 4;

            // Griff-Umrandung
            graphics.fill(handleX - 1, handleY - 1, handleX + handleWidth + 1, handleY + handleHeight + 1, COLOR_BORDER);
            // Griff-Körper
            int handleColor = isDraggingSlider ? 0xFFFFFFFF : 0xFFDDDDDD;
            graphics.fill(handleX, handleY, handleX + handleWidth, handleY + handleHeight, handleColor);
            // Griff-Linien (für visuelles Feedback)
            graphics.fill(handleX + 2, handleY + 5, handleX + handleWidth - 2, handleY + 6, COLOR_BORDER);
            graphics.fill(handleX + 2, handleY + 9, handleX + handleWidth - 2, handleY + 10, COLOR_BORDER);
            graphics.fill(handleX + 2, handleY + 13, handleX + handleWidth - 2, handleY + 14, COLOR_BORDER);

            // Preis-Labels (0%, 100%, 200%)
            graphics.drawString(font, "0€", sliderX, y + 6, COLOR_TEXT_SECONDARY, false);
            String fairStr = String.format("%.0f€", fairPrice);
            int fairPos = (int) ((fairPrice - minPrice) / (maxPrice - minPrice) * sliderWidth);
            graphics.drawString(font, fairStr, sliderX + fairPos - font.width(fairStr) / 2, y + 6, COLOR_GREEN, false);
            String maxStr = String.format("%.0f€", maxPrice);
            graphics.drawString(font, maxStr, sliderX + sliderWidth - font.width(maxStr), y + 6, COLOR_TEXT_SECONDARY, false);

            // Aktueller Preis (zentriert, größer)
            String priceStr = String.format("%.2f€", currentPrice);
            int priceWidth = font.width(priceStr);
            graphics.drawString(font, priceStr, (GUI_WIDTH - priceWidth) / 2, y + 44, COLOR_TEXT, false);

            // Prozent-Anzeige
            int percent = (int) ((currentPrice / fairPrice) * 100);
            String percentStr = "(" + percent + "%)";
            graphics.drawString(font, percentStr, (GUI_WIDTH - font.width(percentStr)) / 2, y + 54, COLOR_TEXT_SECONDARY, false);
        } else {
            graphics.drawString(font, Component.translatable("gui.negotiation.select_first").getString(),
                sliderX, sliderY + 2, COLOR_TEXT_SECONDARY, false);
        }
    }

    private void renderPriceZones(GuiGraphics graphics, int sliderX, int sliderY, int sliderWidth) {
        // Grüne Zone: 80-120% des NPC-Zielpreises
        double greenLow = npcTargetPrice * 0.80;
        double greenHigh = npcTargetPrice * 1.20;

        // Gelbe Zone: 60-80% und 120-150%
        double yellowLow = npcTargetPrice * 0.60;
        double yellowHigh = npcTargetPrice * 1.50;

        // Berechne Positionen
        int greenLowPos = (int) (Math.max(0, (greenLow - minPrice) / (maxPrice - minPrice)) * sliderWidth);
        int greenHighPos = (int) (Math.min(1, (greenHigh - minPrice) / (maxPrice - minPrice)) * sliderWidth);
        int yellowLowPos = (int) (Math.max(0, (yellowLow - minPrice) / (maxPrice - minPrice)) * sliderWidth);
        int yellowHighPos = (int) (Math.min(1, (yellowHigh - minPrice) / (maxPrice - minPrice)) * sliderWidth);

        // Rote Zonen (außen)
        graphics.fill(sliderX, sliderY, sliderX + yellowLowPos, sliderY + 12, COLOR_RED);
        graphics.fill(sliderX + yellowHighPos, sliderY, sliderX + sliderWidth, sliderY + 12, COLOR_RED);

        // Gelbe Zonen
        graphics.fill(sliderX + yellowLowPos, sliderY, sliderX + greenLowPos, sliderY + 12, COLOR_YELLOW);
        graphics.fill(sliderX + greenHighPos, sliderY, sliderX + yellowHighPos, sliderY + 12, COLOR_YELLOW);

        // Grüne Zone
        graphics.fill(sliderX + greenLowPos, sliderY, sliderX + greenHighPos, sliderY + 12, COLOR_GREEN);
    }

    private void renderResponseSection(GuiGraphics graphics) {
        int x = 10;
        int y = RESPONSE_Y;

        if (hasCooldown) {
            // Cooldown-Warnung
            String cooldownText = Component.translatable("gui.negotiation.cooldown_active").getString();
            int textWidth = font.width(cooldownText);
            graphics.drawString(font, cooldownText, (GUI_WIDTH - textWidth) / 2, y + 4, COLOR_RED, false);
        } else if (!npcResponse.isEmpty()) {
            // NPC-Antwort
            String prefix = Component.translatable("gui.negotiation.npc_says").getString();
            graphics.drawString(font, prefix + " \"" + npcResponse + "\"", x, y + 4, COLOR_TEXT_SECONDARY, false);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER RENDERING
    // ═══════════════════════════════════════════════════════════

    private void renderProgressBar(GuiGraphics graphics, int x, int y, int width, int height, float progress, int color) {
        graphics.fill(x, y, x + width, y + height, COLOR_SLIDER_BG);
        int filledWidth = (int) (width * Math.max(0, Math.min(1, progress)));
        graphics.fill(x, y, x + filledWidth, y + height, color);
    }

    private void renderMoodIndicator(GuiGraphics graphics, int x, int y) {
        // Nutze Server-synchronisierte Werte
        int round = serverRound;
        int maxRounds = serverMaxRounds;
        float mood = serverMood;

        // Stimmungs-Leiste
        int barWidth = 100;
        graphics.drawString(font, "\uD83D\uDE0A", x, y, COLOR_GREEN, false); // Happy emoji
        graphics.fill(x + 12, y + 2, x + 12 + barWidth, y + 8, COLOR_SLIDER_BG);

        // Mood-Position (0 = rechts/sauer, 100 = links/glücklich)
        int moodPos = (int) ((100 - mood) / 100.0f * barWidth);

        // Farbe basierend auf Stimmung
        int moodColor;
        if (mood > 70) {
            moodColor = COLOR_GREEN;
        } else if (mood > 40) {
            moodColor = COLOR_YELLOW;
        } else {
            moodColor = COLOR_RED;
        }

        // Größerer Mood-Marker
        graphics.fill(x + 12 + moodPos - 2, y - 1, x + 12 + moodPos + 2, y + 11, moodColor);

        graphics.drawString(font, "\uD83D\uDE21", x + 14 + barWidth, y, COLOR_RED, false); // Angry emoji

        // Runden-Anzeige
        graphics.drawString(font, "(" + round + "/" + maxRounds + ")", x + 130, y, COLOR_TEXT_SECONDARY, false);

        // Mood-Prozent anzeigen
        String moodStr = String.format("%.0f%%", mood);
        graphics.drawString(font, moodStr, x + 160, y, moodColor, false);
    }

    private int getScoreColor(int score) {
        if (score >= 70) return COLOR_GREEN;
        if (score >= 40) return COLOR_YELLOW;
        return COLOR_RED;
    }

    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E key from closing
        if (keyCode == 69) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Linksklick auf Slider
        if (button == 0 && isMouseOverSlider(mouseX, mouseY)) {
            isDraggingSlider = true;
            updatePriceFromMouse(mouseX);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isDraggingSlider) {
            isDraggingSlider = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingSlider && selectedSlot >= 0) {
            updatePriceFromMouse(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    /**
     * Prüft ob die Maus über dem Slider ist
     */
    private boolean isMouseOverSlider(double mouseX, double mouseY) {
        if (selectedSlot < 0) return false;
        return mouseX >= sliderBarX && mouseX <= sliderBarX + sliderBarWidth &&
               mouseY >= sliderBarY - 4 && mouseY <= sliderBarY + 16;
    }

    /**
     * Aktualisiert den Preis basierend auf der Mausposition
     */
    private void updatePriceFromMouse(double mouseX) {
        if (selectedSlot < 0 || maxPrice <= minPrice) return;

        // Berechne Position als Prozentsatz (0-1)
        double relativeX = (mouseX - sliderBarX) / sliderBarWidth;
        relativeX = Math.max(0.0, Math.min(1.0, relativeX));

        // Konvertiere zu Preis
        currentPrice = minPrice + (relativeX * (maxPrice - minPrice));

        // Runde auf 0.50€ Schritte für bessere Bedienbarkeit
        currentPrice = Math.round(currentPrice * 2) / 2.0;

        updateButtonStates();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Scroll zum Anpassen des Preises (auf 1% des Maxpreises)
        if (selectedSlot >= 0) {
            double step = maxPrice * 0.01;  // 1% des Maxpreises pro Scroll
            adjustPrice(delta > 0 ? step : -step);
            updateButtonStates();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
