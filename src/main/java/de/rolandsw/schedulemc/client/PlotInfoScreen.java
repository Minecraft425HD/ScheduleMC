package de.rolandsw.schedulemc.client;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.region.PlotArea;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.network.PlotNetworkHandler;
import de.rolandsw.schedulemc.region.network.PlotPurchasePacket;
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

    private final PlotRegion plot;
    private int backgroundWidth = 280;
    private int backgroundHeight = 200;
    private int leftPos;
    private int topPos;

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
            this.backgroundHeight = 200 + (apartmentCount * 25);
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
        guiGraphics.fill(leftPos, topPos, leftPos + backgroundWidth, topPos + 2, 0xFF4A90E2); // Oben
        guiGraphics.fill(leftPos, topPos + backgroundHeight - 2, leftPos + backgroundWidth, topPos + backgroundHeight, 0xFF4A90E2); // Unten
        guiGraphics.fill(leftPos, topPos, leftPos + 2, topPos + backgroundHeight, 0xFF4A90E2); // Links
        guiGraphics.fill(leftPos + backgroundWidth - 2, topPos, leftPos + backgroundWidth, topPos + backgroundHeight, 0xFF4A90E2); // Rechts

        int currentY = topPos + 15;

        // === PLOT-NAME (Titel) ===
        String plotName = plot.getPlotName();
        guiGraphics.drawCenteredString(this.font, "§6§l" + plotName, leftPos + backgroundWidth / 2, currentY, 0xFFD700);
        currentY += 20;

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

        // === RATING ===
        if (plot.getRatingCount() > 0) {
            guiGraphics.drawString(this.font, "§7Rating: §e" + plot.getRatingStars() +
                " §8(" + plot.getRatingCount() + " Bewertungen)",
                leftPos + 15, currentY, 0xFFFFFF);
            currentY += 12;
        }

        // === ID ===
        guiGraphics.drawString(this.font, "§8ID: " + plot.getPlotId(), leftPos + 15, currentY, 0x888888);
        currentY += 15;

        // === VERKAUF/MIETE STATUS ===
        if (!plot.hasOwner()) {
            // Plot ohne Besitzer = zum Verkauf
            guiGraphics.fill(leftPos + 10, currentY, leftPos + backgroundWidth - 10, currentY + 35, 0x44228B22);
            guiGraphics.drawString(this.font, "§a§l⚡ ZUM VERKAUF", leftPos + 15, currentY + 5, 0x00FF00);
            guiGraphics.drawString(this.font, "§7Preis: §e" + String.format("%.2f", plot.getPrice()) + "€",
                leftPos + 15, currentY + 18, 0xFFFFFF);
            currentY += 40;
        } else {
            if (plot.isForSale()) {
                guiGraphics.fill(leftPos + 10, currentY, leftPos + backgroundWidth - 10, currentY + 35, 0x44228B22);
                guiGraphics.drawString(this.font, "§a§l⚡ ZUM VERKAUF", leftPos + 15, currentY + 5, 0x00FF00);
                guiGraphics.drawString(this.font, "§7Preis: §e" + String.format("%.2f", plot.getSalePrice()) + "€",
                    leftPos + 15, currentY + 18, 0xFFFFFF);
                currentY += 40;
            } else if (plot.isForRent()) {
                if (plot.isRented()) {
                    guiGraphics.fill(leftPos + 10, currentY, leftPos + backgroundWidth - 10, currentY + 35, 0x44228B22);
                    guiGraphics.drawString(this.font, "§a§l✓ VERMIETET", leftPos + 15, currentY + 5, 0x00FF00);
                    guiGraphics.drawString(this.font, "§7Noch §e" + plot.getRentDaysLeft() + " Tage",
                        leftPos + 15, currentY + 18, 0xFFFFFF);
                    currentY += 40;
                } else {
                    guiGraphics.fill(leftPos + 10, currentY, leftPos + backgroundWidth - 10, currentY + 35, 0x44C71585);
                    guiGraphics.drawString(this.font, "§d§l⚡ ZU VERMIETEN", leftPos + 15, currentY + 5, 0xFF00FF);
                    guiGraphics.drawString(this.font, "§7Miete: §e" + String.format("%.2f", plot.getRentPricePerDay()) + "€/Tag",
                        leftPos + 15, currentY + 18, 0xFFFFFF);
                    currentY += 40;
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
    public boolean isPauseScreen() {
        return false; // Pausiert das Spiel nicht (wichtig für Multiplayer)
    }
}
