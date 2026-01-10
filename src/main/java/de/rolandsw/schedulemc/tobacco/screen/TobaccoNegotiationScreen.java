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

    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 220;

    // Inventar-Bereich (links)
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 24;
    private static final int INVENTORY_WIDTH = 62;
    private static final int INVENTORY_HEIGHT = 80;

    // NPC-Info-Bereich (rechts)
    private static final int NPC_INFO_X = 78;
    private static final int NPC_INFO_Y = 24;
    private static final int NPC_INFO_WIDTH = 170;
    private static final int NPC_INFO_HEIGHT = 80;

    // Slider-Bereich
    private static final int SLIDER_X = 8;
    private static final int SLIDER_Y = 112;
    private static final int SLIDER_WIDTH = 240;
    private static final int SLIDER_HEIGHT = 50;

    // Response-Bereich
    private static final int RESPONSE_Y = 168;

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
    private Button priceDownBigButton;
    private Button priceDownButton;
    private Button priceUpButton;
    private Button priceUpBigButton;

    // Response
    private String npcResponse = "";
    private boolean hasCooldown = false;

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

        // Preis-Buttons
        int buttonY = y + SLIDER_Y + 35;
        int buttonWidth = 35;
        int buttonHeight = 14;
        int centerX = x + (GUI_WIDTH / 2);

        priceDownBigButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.negotiation.button.price_down_big"),
            btn -> adjustPrice(-5.0)
        ).bounds(centerX - 85, buttonY, buttonWidth, buttonHeight).build());

        priceDownButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.negotiation.button.price_down"),
            btn -> adjustPrice(-1.0)
        ).bounds(centerX - 48, buttonY, buttonWidth, buttonHeight).build());

        priceUpButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.negotiation.button.price_up"),
            btn -> adjustPrice(1.0)
        ).bounds(centerX + 13, buttonY, buttonWidth, buttonHeight).build());

        priceUpBigButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.negotiation.button.price_up_big"),
            btn -> adjustPrice(5.0)
        ).bounds(centerX + 50, buttonY, buttonWidth, buttonHeight).build());

        // Anbieten-Button
        offerButton = addRenderableWidget(Button.builder(
            Component.translatable("gui.negotiation.button.offer"),
            btn -> makeOffer()
        ).bounds(centerX - 40, y + RESPONSE_Y + 16, 80, 20).build());
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

        // Parse Item-Daten
        String variantStr = PackagedDrugItem.getVariant(stack);
        TobaccoType type = variantStr != null ? TobaccoType.valueOf(variantStr.split("\\.")[1]) : TobaccoType.VIRGINIA;

        String qualityStr = PackagedDrugItem.getQuality(stack);
        TobaccoQuality quality = qualityStr != null ? TobaccoQuality.valueOf(qualityStr.split("\\.")[1]) : TobaccoQuality.GUT;

        int weight = PackagedDrugItem.getWeight(stack);

        // Berechne Preise
        NPCBusinessMetrics metrics = new NPCBusinessMetrics(npc);
        int reputation = metrics.getReputation(minecraft.player.getStringUUID());
        int satisfaction = metrics.getSatisfaction();
        DemandLevel demand = metrics.getDemand();

        fairPrice = PriceCalculator.calculateFairPrice(type, quality, weight, demand, reputation, satisfaction);
        npcTargetPrice = PriceCalculator.calculateIdealPrice(fairPrice);
        minPrice = Math.max(1.0, npcTargetPrice * 0.5);
        maxPrice = fairPrice * 1.5;

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
        priceDownBigButton.active = canOffer && currentPrice > minPrice + 5;
        priceDownButton.active = canOffer && currentPrice > minPrice;
        priceUpButton.active = canOffer && currentPrice < maxPrice;
        priceUpBigButton.active = canOffer && currentPrice < maxPrice - 5;
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

        // GUI schließen
        this.onClose();
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
        graphics.fill(x, y, x + GUI_WIDTH, y + 18, COLOR_HEADER);

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
        graphics.drawString(font, title, (GUI_WIDTH - titleWidth) / 2, 5, COLOR_TEXT, false);

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
        int y = INVENTORY_Y + INVENTORY_HEIGHT + 2;

        if (selectedSlot >= 0) {
            ItemStack stack = menu.getSelectedItem();
            if (!stack.isEmpty() && stack.getItem() instanceof PackagedDrugItem) {
                int weight = PackagedDrugItem.getWeight(stack);
                String variantStr = PackagedDrugItem.getVariant(stack);
                String typeDisplay = variantStr != null ? variantStr.split("\\.")[1] : "Unknown";
                String qualityStr = PackagedDrugItem.getQuality(stack);
                String qualityDisplay = qualityStr != null ? qualityStr.split("\\.")[1] : "";

                graphics.drawString(font, Component.translatable("gui.negotiation.selected_item",
                    weight, typeDisplay, qualityDisplay).getString(), x, y, COLOR_TEXT, false);
                graphics.drawString(font, Component.translatable("gui.negotiation.fair_price",
                    String.format("%.2f", fairPrice)).getString(), x, y + 10, COLOR_TEXT_SECONDARY, false);
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
        y += 12;
        String addictionStr = Component.translatable("gui.negotiation.addiction", addictionLevel).getString();
        graphics.drawString(font, addictionStr, x, y, COLOR_TEXT_SECONDARY, false);
        renderProgressBar(graphics, x + 80, y, 80, 8, addictionLevel / 100.0f, COLOR_RED);

        // Wallet
        y += 12;
        graphics.drawString(font, Component.translatable("gui.negotiation.wallet", walletBalance).getString(),
            x, y, COLOR_TEXT_SECONDARY, false);

        // Beziehung
        y += 12;
        String relationStr = (relationLevel >= 0 ? "+" : "") + relationLevel;
        graphics.drawString(font, Component.translatable("gui.negotiation.relation", relationStr).getString(),
            x, y, COLOR_TEXT_SECONDARY, false);

        // Score
        y += 14;
        int score = scoreData != null ? scoreData.getTotalScore() : 50;
        graphics.drawString(font, Component.translatable("gui.negotiation.score").getString(), x, y, COLOR_TEXT, false);
        renderProgressBar(graphics, x + 50, y, 100, 10, score / 100.0f, getScoreColor(score));
        graphics.drawString(font, score + "/100", x + 155, y, COLOR_TEXT, false);

        // Risiko
        y += 14;
        int risk = scoreData != null ? scoreData.getAbortRisk() : 20;
        graphics.drawString(font, Component.translatable("gui.negotiation.risk").getString(), x, y, COLOR_TEXT_SECONDARY, false);
        renderProgressBar(graphics, x + 50, y, 60, 8, risk / 100.0f, COLOR_RED);
        graphics.drawString(font, risk + "%", x + 115, y, COLOR_TEXT_SECONDARY, false);

        // Stimmung
        y += 12;
        renderMoodIndicator(graphics, x, y);
    }

    private void renderSliderSection(GuiGraphics graphics) {
        int x = SLIDER_X;
        int y = SLIDER_Y;

        // Slider-Leiste
        int sliderWidth = SLIDER_WIDTH - 16;
        int sliderX = x + 8;
        int sliderY = y + 18;

        // Slider-Hintergrund
        graphics.fill(sliderX, sliderY, sliderX + sliderWidth, sliderY + 12, COLOR_SLIDER_BG);

        if (selectedSlot >= 0 && maxPrice > minPrice) {
            // Preis-Zonen rendern
            renderPriceZones(graphics, sliderX, sliderY, sliderWidth);

            // NPC-Zielpreis Marker
            int npcTargetPos = (int) ((npcTargetPrice - minPrice) / (maxPrice - minPrice) * sliderWidth);
            graphics.fill(sliderX + npcTargetPos - 1, sliderY - 2, sliderX + npcTargetPos + 1, sliderY + 14, COLOR_YELLOW);

            // Spieler-Preis Marker
            int playerPos = (int) ((currentPrice - minPrice) / (maxPrice - minPrice) * sliderWidth);
            graphics.fill(sliderX + playerPos - 2, sliderY - 4, sliderX + playerPos + 2, sliderY + 16, COLOR_TEXT);

            // Preis-Labels
            graphics.drawString(font, String.format("%.0f", minPrice), sliderX, y + 4, COLOR_TEXT_SECONDARY, false);
            graphics.drawString(font, String.format("%.0f", maxPrice), sliderX + sliderWidth - 20, y + 4, COLOR_TEXT_SECONDARY, false);

            // Aktueller Preis (zentriert)
            String priceStr = String.format("%.2f", currentPrice);
            int priceWidth = font.width(priceStr);
            graphics.drawString(font, priceStr, (GUI_WIDTH - priceWidth) / 2, y + 35, COLOR_TEXT, false);
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
        int round = negotiationState != null ? negotiationState.getCurrentRound() : 0;
        int maxRounds = negotiationState != null ? negotiationState.getMaxRounds() : 5;
        float mood = negotiationState != null ? negotiationState.getMoodPercent() : 100;

        // Stimmungs-Leiste
        int barWidth = 80;
        graphics.drawString(font, "\uD83D\uDE0A", x, y, COLOR_GREEN, false); // Happy emoji
        graphics.fill(x + 12, y + 2, x + 12 + barWidth, y + 8, COLOR_SLIDER_BG);

        // Mood-Position
        int moodPos = (int) ((100 - mood) / 100.0f * barWidth);
        graphics.fill(x + 12 + moodPos - 1, y, x + 12 + moodPos + 1, y + 10, COLOR_TEXT);

        graphics.drawString(font, "\uD83D\uDE21", x + 14 + barWidth, y, COLOR_RED, false); // Angry emoji

        // Runden-Anzeige
        graphics.drawString(font, "(" + round + "/" + maxRounds + ")", x + 110, y, COLOR_TEXT_SECONDARY, false);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Scroll zum Anpassen des Preises
        if (selectedSlot >= 0) {
            adjustPrice(delta > 0 ? 0.5 : -0.5);
            updateButtonStates();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
