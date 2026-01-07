package de.rolandsw.schedulemc.client;
nimport de.rolandsw.schedulemc.util.StringUtils;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.region.PlotArea;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.network.PlotNetworkHandler;
import de.rolandsw.schedulemc.region.network.PlotPurchasePacket;
import de.rolandsw.schedulemc.region.network.PlotRatingPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI-Screen für detaillierte Plot-Informationen
 * Wird beim Rechtsklick auf PlotInfoBlock geöffnet
 */
@OnlyIn(Dist.CLIENT)
public class PlotInfoScreen extends Screen {

    // Colors
    private static final int COLOR_BORDER_BLUE = 0xFF4A90E2;

    // Layout constants
    private static final int BACKGROUND_WIDTH = 280;
    private static final int SPACING_AFTER_HEADER = 60;
    private static final int SECTION_SPACING = 40;

    private final PlotRegion plot;
    private int backgroundWidth = BACKGROUND_WIDTH;
    private int backgroundHeight = 250; // Erhöht von 200 auf 250 für Rating-Sektion
    private int leftPos;
    private int topPos;

    // Rating Button Positionen (werden in render() gesetzt)
    private int ratingButtonY = -1;
    private int ratingButtonX = -1;

    public PlotInfoScreen(PlotRegion plot) {
        super(Component.literal("Plot Information"));
        this.plot = plot;
    }

    @Override
    protected void init() {
        super.init();

        // Berechne Position (zentriert)
        this.leftPos = (this.width - this.backgroundWidth) / 2;
        this.topPos = (this.height - this.backgroundHeight) / 2;

        // Dynamische Höhe basierend auf Apartments
        int apartmentCount = plot.getAvailableSubAreaCount();
        if (apartmentCount > 0) {
            // Erhöhe Höhe für Apartment-Liste
            this.backgroundHeight = 250 + (apartmentCount * 25);
            this.topPos = (this.height - this.backgroundHeight) / 2;
        }

        // === BUTTONS ===

        int buttonY = topPos + backgroundHeight - 30;

        // Schließen Button
        addRenderableWidget(Button.builder(Component.literal("Schließen"), button -> {
            this.onClose();
        }).bounds(leftPos + backgroundWidth - 80, buttonY, 70, 20).build());

        // Kaufen Button (nur wenn zum Verkauf)
        if (!plot.hasOwner() || plot.isForSale()) {
            addRenderableWidget(Button.builder(
                Component.literal("§a§lKaufen"),
                button -> {
                    // Sende Purchase-Packet direkt
                    PlotNetworkHandler.sendToServer(
                        new PlotPurchasePacket(plot.getPlotId(), PlotPurchasePacket.PurchaseType.BUY)
                    );
                    this.onClose();
                }
            ).bounds(leftPos + 10, buttonY, 70, 20).build());
        }

        // Mieten Button (nur wenn Plot zu vermieten)
        if (plot.isForRent() && !plot.isRented()) {
            addRenderableWidget(Button.builder(
                Component.literal("§d§lMieten"),
                button -> {
                    // Sende Purchase-Packet mit RENT type
                    PlotNetworkHandler.sendToServer(
                        new PlotPurchasePacket(plot.getPlotId(), PlotPurchasePacket.PurchaseType.RENT)
                    );
                    this.onClose();
                }
            ).bounds(leftPos + 90, buttonY, 70, 20).build());
        }

        // Apartment-Buttons
        if (apartmentCount > 0) {
            int aptButtonY = topPos + 130;
            int index = 0;

            for (PlotArea apt : plot.getAvailableSubAreas()) {
                final String aptId = apt.getId();
                addRenderableWidget(Button.builder(
                    Component.literal("Mieten"),
                    button -> {
                        this.onClose();
                        if (minecraft != null && minecraft.player != null) {
                            minecraft.player.connection.sendCommand("plot apartment rent " + aptId);
                        }
                    }
                ).bounds(leftPos + backgroundWidth - 75, aptButtonY + (index * 25), 60, 18).build());

                index++;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Dunkler Hintergrund (für bessere Lesbarkeit)
        renderBackground(guiGraphics);

        // Haupt-Hintergrund
        guiGraphics.fill(leftPos, topPos, leftPos + backgroundWidth, topPos + backgroundHeight, 0xEE1A1A1A);

        // Rahmen
        guiGraphics.fill(leftPos, topPos, leftPos + backgroundWidth, topPos + 2, COLOR_BORDER_BLUE); // Oben
        guiGraphics.fill(leftPos, topPos + backgroundHeight - 2, leftPos + backgroundWidth, topPos + backgroundHeight, COLOR_BORDER_BLUE); // Unten
        guiGraphics.fill(leftPos, topPos, leftPos + 2, topPos + backgroundHeight, COLOR_BORDER_BLUE); // Links
        guiGraphics.fill(leftPos + backgroundWidth - 2, topPos, leftPos + backgroundWidth, topPos + backgroundHeight, COLOR_BORDER_BLUE); // Rechts

        int currentY = topPos + 15;

        // === PLOT-NAME (Titel) ===
        String plotName = plot.getPlotName();
        guiGraphics.drawCenteredString(this.font, "§6§l" + plotName, leftPos + backgroundWidth / 2, currentY, 0xFFD700);
        currentY += 15;

        // === BESCHREIBUNG ===
        String description = plot.getDescription();
        if (description != null && !description.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, "§7§o" + description, leftPos + backgroundWidth / 2, currentY, 0xAAAAAA);
            currentY += 15;
        } else {
            currentY += 5;
        }

        // Trennlinie
        guiGraphics.fill(leftPos + 10, currentY, leftPos + backgroundWidth - 10, currentY + 1, 0x66FFFFFF);
        currentY += 8;

        // === BESITZER ===
        String ownerName = plot.getOwnerName();
        if (ownerName == null || ownerName.equals("Niemand")) {
            guiGraphics.drawString(this.font, "§7Besitzer: §cKein Besitzer", leftPos + 15, currentY, 0xFFFFFF);
        } else {
            guiGraphics.drawString(this.font, "§7Besitzer: §f" + ownerName, leftPos + 15, currentY, 0xFFFFFF);
        }
        currentY += 12;

        // === GRÖSSE ===
        guiGraphics.drawString(this.font, "§7Größe: §e" + String.format("%,d", plot.getVolume()) + " Blöcke",
            leftPos + 15, currentY, 0xFFFFFF);
        currentY += 12;

        // === ID ===
        guiGraphics.drawString(this.font, "§8ID: " + plot.getPlotId(), leftPos + 15, currentY, 0x888888);
        currentY += 15;

        // === RATING SECTION ===
        // Trennlinie
        guiGraphics.fill(leftPos + 10, currentY, leftPos + backgroundWidth - 10, currentY + 1, 0x66FFFFFF);
        currentY += 8;

        // Rating Box
        guiGraphics.fill(leftPos + 10, currentY, leftPos + backgroundWidth - 10, currentY + 55, 0x44FFD700);

        // Rating Titel
        guiGraphics.drawString(this.font, "§6§l⭐ BEWERTUNG", leftPos + 15, currentY + 5, 0xFFD700);

        // Durchschnittliches Rating
        if (plot.getRatingCount() > 0) {
            String avgRating = String.format("%.1f", plot.getAverageRating());
            guiGraphics.drawString(this.font, "§7Durchschnitt: §e" + avgRating + "/5.0 §8(" +
                plot.getRatingCount() + " Bewertungen)", leftPos + 15, currentY + 18, 0xFFFFFF);

            // Stern-Anzeige
            guiGraphics.drawString(this.font, "§e" + plot.getRatingStars(), leftPos + 15, currentY + 30, 0xFFFFFF);
        } else {
            guiGraphics.drawString(this.font, "§7Noch keine Bewertungen", leftPos + 15, currentY + 18, 0xAAAAAA);
        }

        // Spieler kann nicht eigene Plots bewerten
        if (minecraft != null && minecraft.player != null &&
            !plot.getOwnerUUID().equals(minecraft.player.getUUID().toString()) &&
            plot.hasOwner()) {

            // Zeige ob Spieler bereits bewertet hat
            if (plot.hasRated(minecraft.player.getUUID())) {
                int playerRating = plot.getPlayerRating(minecraft.player.getUUID());
                String stars = "★".repeat(playerRating) + "☆".repeat(5 - playerRating);
                guiGraphics.drawString(this.font, "§7Deine Bewertung: §e" + stars,
                    leftPos + 15, currentY + 42, 0xFFFFFF);
            } else {
                guiGraphics.drawString(this.font, "§7Klicke auf Sterne zum Bewerten:",
                    leftPos + 15, currentY + 42, 0xAAAAAA);
            }

            // Zeichne klickbare Sterne (1-5)
            this.ratingButtonY = currentY + 30;
            this.ratingButtonX = leftPos + backgroundWidth - 95;

            int currentPlayerRating = plot.getPlayerRating(minecraft.player.getUUID());
            for (int i = 1; i <= 5; i++) {
                int starX = this.ratingButtonX + ((i - 1) * 16);
                boolean isHovered = mouseX >= starX && mouseX < starX + 14 &&
                                  mouseY >= this.ratingButtonY && mouseY < this.ratingButtonY + 14;

                // Zeichne Stern (gefüllt wenn bewertet oder gehovered)
                if (i <= currentPlayerRating || isHovered) {
                    guiGraphics.drawString(this.font, "§e★", starX, this.ratingButtonY, 0xFFFFFF);
                } else {
                    guiGraphics.drawString(this.font, "§7☆", starX, this.ratingButtonY, 0xFFFFFF);
                }
            }
        } else {
            this.ratingButtonY = -1; // Keine Buttons wenn Spieler nicht bewerten darf
        }

        currentY += SPACING_AFTER_HEADER;

        // === VERKAUF/MIETE STATUS ===
        if (!plot.hasOwner()) {
            // Plot ohne Besitzer = zum Verkauf
            guiGraphics.fill(leftPos + 10, currentY, leftPos + backgroundWidth - 10, currentY + 35, 0x44228B22);
            guiGraphics.drawString(this.font, "§a§l⚡ ZUM VERKAUF", leftPos + 15, currentY + 5, 0x00FF00);
            guiGraphics.drawString(this.font, "§7Preis: §e" + String.format("%.2f", plot.getPrice()) + "€",
                leftPos + 15, currentY + 18, 0xFFFFFF);
            currentY += SECTION_SPACING;
        } else {
            if (plot.isForSale()) {
                guiGraphics.fill(leftPos + 10, currentY, leftPos + backgroundWidth - 10, currentY + 35, 0x44228B22);
                guiGraphics.drawString(this.font, "§a§l⚡ ZUM VERKAUF", leftPos + 15, currentY + 5, 0x00FF00);
                guiGraphics.drawString(this.font, "§7Preis: §e" + String.format("%.2f", plot.getSalePrice()) + "€",
                    leftPos + 15, currentY + 18, 0xFFFFFF);
                currentY += SECTION_SPACING;
            } else if (plot.isForRent()) {
                if (plot.isRented()) {
                    guiGraphics.fill(leftPos + 10, currentY, leftPos + backgroundWidth - 10, currentY + 35, 0x44228B22);
                    guiGraphics.drawString(this.font, "§a§l✓ VERMIETET", leftPos + 15, currentY + 5, 0x00FF00);
                    guiGraphics.drawString(this.font, "§7Noch §e" + plot.getRentDaysLeft() + " Tage",
                        leftPos + 15, currentY + 18, 0xFFFFFF);
                    currentY += SECTION_SPACING;
                } else {
                    guiGraphics.fill(leftPos + 10, currentY, leftPos + backgroundWidth - 10, currentY + 35, 0x44C71585);
                    guiGraphics.drawString(this.font, "§d§l⚡ ZU VERMIETEN", leftPos + 15, currentY + 5, 0xFF00FF);
                    guiGraphics.drawString(this.font, "§7Miete: §e" + String.format("%.2f", plot.getRentPricePerDay()) + "€/Tag",
                        leftPos + 15, currentY + 18, 0xFFFFFF);
                    currentY += SECTION_SPACING;
                }
            }
        }

        // === APARTMENTS ===
        if (plot.getSubAreaCount() > 0) {
            // Trennlinie
            guiGraphics.fill(leftPos + 10, currentY, leftPos + backgroundWidth - 10, currentY + 1, 0x66FFFFFF);
            currentY += 8;

            int availableCount = plot.getAvailableSubAreaCount();
            guiGraphics.drawString(this.font, "§6§l🏠 APARTMENTS",
                leftPos + 15, currentY, 0xFFAA00);
            currentY += 12;

            guiGraphics.drawString(this.font, "§7Gesamt: §e" + plot.getSubAreaCount() + " §8│ " +
                "§7Verfügbar: §a" + availableCount + " §8│ " +
                "§7Vermietet: §c" + plot.getRentedSubAreaCount(),
                leftPos + 15, currentY, 0xFFFFFF);
            currentY += 15;

            // Zeige verfügbare Apartments
            if (availableCount > 0) {
                guiGraphics.drawString(this.font, "§d§lVERFÜGBARE WOHNUNGEN:", leftPos + 15, currentY, 0xFF00FF);
                currentY += 12;

                for (PlotArea apt : plot.getAvailableSubAreas()) {
                    // Apartment-Zeile
                    guiGraphics.drawString(this.font, "§e" + apt.getName(),
                        leftPos + 20, currentY + 4, 0xFFFFFF);
                    guiGraphics.drawString(this.font, "§a" + String.format("%.0f", apt.getMonthlyRent()) + "€/Monat",
                        leftPos + 120, currentY + 4, 0xFFFFFF);
                    // Button wird automatisch gerendert
                    currentY += 25;
                }
            }
        }

        // Rendere Buttons
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Block E key (inventory key - 69) from closing the screen
        if (keyCode == 69) { // GLFW_KEY_E
            return true; // Consume event, prevent closing
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Prüfe ob auf Rating-Sterne geklickt wurde
        if (button == 0 && ratingButtonY != -1) { // Linksklick
            if (mouseY >= ratingButtonY && mouseY < ratingButtonY + 14) {
                // Prüfe welcher Stern geklickt wurde
                for (int i = 1; i <= 5; i++) {
                    int starX = ratingButtonX + ((i - 1) * 16);
                    if (mouseX >= starX && mouseX < starX + 14) {
                        // Sende Rating zum Server
                        PlotNetworkHandler.sendToServer(new PlotRatingPacket(plot.getPlotId(), i));
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Pausiert das Spiel nicht (wichtig für Multiplayer)
    }
}
