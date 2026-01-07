package de.rolandsw.schedulemc.npc.client.screen;
import de.rolandsw.schedulemc.util.UIColors;
nimport de.rolandsw.schedulemc.util.StringUtils;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.economy.CreditLoan;
import de.rolandsw.schedulemc.economy.CreditScore;
import de.rolandsw.schedulemc.economy.EconomyManager;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.menu.CreditAdvisorMenu;
import de.rolandsw.schedulemc.npc.network.ApplyCreditLoanPacket;
import de.rolandsw.schedulemc.npc.network.NPCNetworkHandler;
import de.rolandsw.schedulemc.npc.network.RepayCreditLoanPacket;
import de.rolandsw.schedulemc.npc.network.RequestCreditDataPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Vollständige Kreditberater GUI
 *
 * Features:
 * - Bonitätsanzeige mit Sternen
 * - 4 Kredittypen zur Auswahl
 * - Aktiver Kredit-Status mit Fortschrittsbalken
 * - Vorzeitige Rückzahlung
 */
@OnlyIn(Dist.CLIENT)
public class CreditAdvisorScreen extends AbstractContainerScreen<CreditAdvisorMenu> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(ScheduleMC.MOD_ID, "textures/gui/npc_interaction.png");

    // Colors
    private static final int COLOR_PROGRESS_BAR_BACKGROUND = UIColors.BACKGROUND_LIGHT;
    private static final int COLOR_PROGRESS_BAR_FILL = UIColors.ACCENT_GREEN_BRIGHT;
    private static final int COLOR_PROGRESS_BAR_OUTLINE = UIColors.GRAY_MEDIUM;

    // Kredit-Buttons
    private Button starterLoanButton;
    private Button standardLoanButton;
    private Button premiumLoanButton;
    private Button vipLoanButton;

    // Aktionen-Buttons
    private Button repayButton;
    private Button closeButton;
    private Button refreshButton;

    // Client-Side gecachte Daten (werden vom Server aktualisiert)
    private int creditScore = 500;
    private CreditScore.CreditRating creditRating = CreditScore.CreditRating.BB;
    private boolean hasActiveLoan = false;
    private String activeLoanType = "";
    private double activeLoanRemaining = 0;
    private double activeLoanDaily = 0;
    private int activeLoanProgress = 0;
    private int activeLoanRemainingDays = 0;

    // Ausgewählter Kredittyp für Details
    private CreditLoan.CreditLoanType selectedLoanType = null;

    public CreditAdvisorScreen(CreditAdvisorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 280;
        this.imageHeight = 230;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Kredit-Buttons (4 Typen)
        int buttonWidth = 125;
        int buttonHeight = 22;
        int startY = y + 85;
        int spacing = 25;

        starterLoanButton = addRenderableWidget(Button.builder(
            Component.literal("STARTER - 5.000€"), button -> {
                selectLoanType(CreditLoan.CreditLoanType.STARTER);
            }
        ).bounds(x + 10, startY, buttonWidth, buttonHeight).build());

        standardLoanButton = addRenderableWidget(Button.builder(
            Component.literal("STANDARD - 25.000€"), button -> {
                selectLoanType(CreditLoan.CreditLoanType.STANDARD);
            }
        ).bounds(x + 145, startY, buttonWidth, buttonHeight).build());

        premiumLoanButton = addRenderableWidget(Button.builder(
            Component.literal("PREMIUM - 100.000€"), button -> {
                selectLoanType(CreditLoan.CreditLoanType.PREMIUM);
            }
        ).bounds(x + 10, startY + spacing, buttonWidth, buttonHeight).build());

        vipLoanButton = addRenderableWidget(Button.builder(
            Component.literal("VIP - 500.000€"), button -> {
                selectLoanType(CreditLoan.CreditLoanType.VIP);
            }
        ).bounds(x + 145, startY + spacing, buttonWidth, buttonHeight).build());

        // Rückzahlen Button
        repayButton = addRenderableWidget(Button.builder(
            Component.literal("Vorzeitig zurückzahlen"), button -> {
                handleRepayLoan();
            }
        ).bounds(x + 60, y + 175, 160, 20).build());

        // Refresh Button
        refreshButton = addRenderableWidget(Button.builder(
            Component.literal("\u21BB"), button -> { // ↻ Symbol
                requestCreditData();
            }
        ).bounds(x + 250, y + 20, 20, 18).build());

        // Close Button
        closeButton = addRenderableWidget(Button.builder(
            Component.literal("Schließen"), button -> {
                this.onClose();
            }
        ).bounds(x + 90, y + 205, 100, 20).build());

        // Initiale Daten vom Server anfordern
        requestCreditData();
    }

    /**
     * Wählt einen Kredittyp aus und zeigt Details/Bestätigung
     */
    private void selectLoanType(CreditLoan.CreditLoanType type) {
        if (hasActiveLoan) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.literal(
                    "§c§lFehler: §7Du hast bereits einen aktiven Kredit!"
                ));
            }
            return;
        }

        // Prüfe ob Rating ausreichend ist (Client-Side check, Server validiert nochmal)
        if (creditRating.ordinal() > type.getRequiredRating().ordinal()) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.literal(
                    "§c§lFehler: §7Deine Bonität reicht nicht aus!\n" +
                    "§7Benötigt: §e" + type.getRequiredRating().getDisplayName() + "\n" +
                    "§7Aktuell: §c" + creditRating.getDisplayName()
                ));
            }
            return;
        }

        this.selectedLoanType = type;

        // Sende Antrag an Server
        NPCNetworkHandler.sendToServer(new ApplyCreditLoanPacket(type.ordinal()));

        // Schließe GUI nach Antrag
        this.onClose();
    }

    /**
     * Zahlt den aktiven Kredit vorzeitig zurück
     */
    private void handleRepayLoan() {
        if (!hasActiveLoan) {
            return;
        }

        NPCNetworkHandler.sendToServer(new RepayCreditLoanPacket());
        this.onClose();
    }

    /**
     * Fordert aktuelle Kredit-Daten vom Server an
     */
    private void requestCreditData() {
        NPCNetworkHandler.sendToServer(new RequestCreditDataPacket());
    }

    /**
     * Wird vom Server über Packet aufgerufen um Daten zu aktualisieren
     */
    public void updateCreditData(int score, int ratingOrdinal, boolean hasLoan,
                                  String loanType, double remaining, double daily,
                                  int progress, int remainingDays) {
        this.creditScore = score;
        this.creditRating = CreditScore.CreditRating.values()[
            Math.min(ratingOrdinal, CreditScore.CreditRating.values().length - 1)
        ];
        this.hasActiveLoan = hasLoan;
        this.activeLoanType = loanType;
        this.activeLoanRemaining = remaining;
        this.activeLoanDaily = daily;
        this.activeLoanProgress = progress;
        this.activeLoanRemainingDays = remainingDays;

        // Update Button-Sichtbarkeit
        updateButtonVisibility();
    }

    /**
     * Aktualisiert Button-Sichtbarkeit basierend auf Kredit-Status
     */
    private void updateButtonVisibility() {
        boolean canApply = !hasActiveLoan;

        starterLoanButton.active = canApply && canTakeLoan(CreditLoan.CreditLoanType.STARTER);
        standardLoanButton.active = canApply && canTakeLoan(CreditLoan.CreditLoanType.STANDARD);
        premiumLoanButton.active = canApply && canTakeLoan(CreditLoan.CreditLoanType.PREMIUM);
        vipLoanButton.active = canApply && canTakeLoan(CreditLoan.CreditLoanType.VIP);

        repayButton.visible = hasActiveLoan;
        repayButton.active = hasActiveLoan;
    }

    /**
     * Prüft ob ein Kredittyp basierend auf Rating verfügbar ist
     */
    private boolean canTakeLoan(CreditLoan.CreditLoanType type) {
        return creditRating.ordinal() <= type.getRequiredRating().ordinal();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Header
        guiGraphics.drawString(this.font, "KREDITBERATER", x + 95, y + 6, 0x404040, false);

        // Bonitätsanzeige
        renderCreditScore(guiGraphics, x, y);

        // Verfügbare Kredite Header
        guiGraphics.drawString(this.font, "Verfügbare Kredite:", x + 10, y + 72, 0x606060, false);

        // Kredit-Details (Zinsen, Laufzeit)
        renderLoanDetails(guiGraphics, x, y);

        // Aktiver Kredit (falls vorhanden)
        if (hasActiveLoan) {
            renderActiveLoan(guiGraphics, x, y);
        }
    }

    /**
     * Rendert die Bonitätsanzeige
     */
    private void renderCreditScore(GuiGraphics guiGraphics, int x, int y) {
        int boxY = y + 22;

        // Hintergrund-Box
        guiGraphics.fill(x + 10, boxY, x + 270, boxY + 45, 0x44000000);

        // "Ihre Bonität:" Label
        guiGraphics.drawString(this.font, "Ihre Bonität:", x + 15, boxY + 5, 0x808080, false);

        // Sterne
        String stars = creditRating.getStarsString();
        guiGraphics.drawString(this.font, stars, x + 90, boxY + 5, creditRating.getColor(), false);

        // Rating-Name
        guiGraphics.drawString(this.font, "(" + creditRating.getDisplayName() + ")",
            x + 145, boxY + 5, creditRating.getColor(), false);

        // Score-Zahl
        guiGraphics.drawString(this.font, "Score: " + creditScore + "/1000",
            x + 15, boxY + 18, 0xAAAAAA, false);

        // Max Kreditbetrag
        String maxAmount = String.format("Max. Kredit: %.0f€", creditRating.getMaxLoanAmount());
        guiGraphics.drawString(this.font, maxAmount, x + 15, boxY + 31, 0x00AA00, false);

        // Kontostand
        if (minecraft != null && minecraft.player != null) {
            double balance = EconomyManager.getBalance(minecraft.player.getUUID());
            guiGraphics.drawString(this.font, String.format("Kontostand: %.2f€", balance),
                x + 145, boxY + 31, 0xFFD700, false);
        }
    }

    /**
     * Rendert Kredit-Details (Zinsen und Laufzeit)
     */
    private void renderLoanDetails(GuiGraphics guiGraphics, int x, int y) {
        int detailY = y + 135;

        // Kleine Info unter den Buttons
        guiGraphics.drawString(this.font, "Zinsen:", x + 10, detailY, 0x808080, false);
        guiGraphics.drawString(this.font, "8%", x + 55, detailY, 0xFFAAAA, false);
        guiGraphics.drawString(this.font, "12%", x + 80, detailY, 0xFFAAAA, false);
        guiGraphics.drawString(this.font, "15%", x + 115, detailY, 0xFFAAAA, false);
        guiGraphics.drawString(this.font, "10%", x + 150, detailY, 0xFFAAAA, false);

        guiGraphics.drawString(this.font, "Laufzeit:", x + 10, detailY + 12, 0x808080, false);
        guiGraphics.drawString(this.font, "14T", x + 55, detailY + 12, 0xAAAAFF, false);
        guiGraphics.drawString(this.font, "28T", x + 80, detailY + 12, 0xAAAAFF, false);
        guiGraphics.drawString(this.font, "56T", x + 115, detailY + 12, 0xAAAAFF, false);
        guiGraphics.drawString(this.font, "90T", x + 150, detailY + 12, 0xAAAAFF, false);

        // Hinweis bei niedrigem Rating
        if (creditRating.ordinal() >= CreditScore.CreditRating.CCC.ordinal()) {
            guiGraphics.drawString(this.font, "\u26A0 Niedrige Bonität - weniger Optionen",
                x + 10, detailY + 28, 0xFF5555, false);
        }
    }

    /**
     * Rendert den aktiven Kredit-Status
     */
    private void renderActiveLoan(GuiGraphics guiGraphics, int x, int y) {
        int loanY = y + 140;

        // Hintergrund-Box
        guiGraphics.fill(x + 10, loanY, x + 270, loanY + 32, 0x66004400);

        // Aktiver Kredit Header
        guiGraphics.drawString(this.font, "\u2714 AKTIVER KREDIT: " + activeLoanType,
            x + 15, loanY + 3, 0x00FF00, false);

        // Fortschrittsbalken
        int barX = x + 15;
        int barY = loanY + 14;
        int barWidth = 200;
        int barHeight = 8;

        // Hintergrund
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, COLOR_PROGRESS_BAR_BACKGROUND);
        // Fortschritt
        int progressWidth = (int) ((activeLoanProgress / 100.0) * barWidth);
        guiGraphics.fill(barX, barY, barX + progressWidth, barY + barHeight, COLOR_PROGRESS_BAR_FILL);
        // Rahmen
        guiGraphics.renderOutline(barX - 1, barY - 1, barWidth + 2, barHeight + 2, COLOR_PROGRESS_BAR_OUTLINE);

        // Prozent-Anzeige
        guiGraphics.drawString(this.font, activeLoanProgress + "%",
            barX + barWidth + 5, barY, 0xFFFFFF, false);

        // Details
        guiGraphics.drawString(this.font,
            String.format("Rest: %.2f€ | Täglich: %.2f€ | %d Tage",
                activeLoanRemaining, activeLoanDaily, activeLoanRemainingDays),
            x + 15, loanY + 24, 0xAAAAAA, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true;
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
        // Labels werden in render() gezeichnet
    }
}
