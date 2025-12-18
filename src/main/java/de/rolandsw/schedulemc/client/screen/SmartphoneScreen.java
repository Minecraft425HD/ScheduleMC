package de.rolandsw.schedulemc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.client.network.SmartphoneNetworkHandler;
import de.rolandsw.schedulemc.client.network.SmartphoneStatePacket;
import de.rolandsw.schedulemc.client.screen.apps.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Haupt-Smartphone-GUI mit 8 Apps
 * Scrollbar mit nur 6 sichtbaren Apps (3 Reihen x 2 Spalten)
 */
@OnlyIn(Dist.CLIENT)
public class SmartphoneScreen extends Screen {

    // Layout-Konstanten (kompakter für ALLE Bildschirmgrößen)
    private static final int PHONE_WIDTH = 220; // Etwas breiter für mehr Label-Platz
    private static final int PHONE_HEIGHT = 260; // Kompakt genug für alle Bildschirme
    private static final int APP_ICON_SIZE = 42; // Größer für bessere Lesbarkeit
    private static final int APP_SPACING = 15; // Guter Abstand zwischen Apps
    private static final int CLOSE_BUTTON_SIZE = 20;
    private static final int BORDER_SIZE = 5; // Rahmen um das Smartphone
    private static final int MARGIN_TOP = 15; // Mindestabstand vom oberen Bildschirmrand
    private static final int MARGIN_BOTTOM = 35; // Genug Platz für Hotbar
    private static final int SCROLLBAR_WIDTH = 8; // Etwas breiter für bessere Klickbarkeit
    private static final int SCROLLBAR_MARGIN = 10; // Abstand zwischen Apps und Scrollbar
    private static final int VISIBLE_ROWS = 3; // Nur 3 Reihen sichtbar (6 Apps)
    private static final int TOTAL_ROWS = 4; // Insgesamt 4 Reihen (8 Apps)

    // App-Icons (konfigurierbar über Ressourcen)
    private static final ResourceLocation APP_MAP = new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/apps/app_map.png");
    private static final ResourceLocation APP_DEALER = new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/apps/app_dealer.png");
    private static final ResourceLocation APP_PRODUCTS = new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/apps/app_products.png");
    private static final ResourceLocation APP_ORDER = new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/apps/app_order.png");
    private static final ResourceLocation APP_CONTACTS = new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/apps/app_contacts.png");
    private static final ResourceLocation APP_MESSAGES = new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/apps/app_messages.png");
    private static final ResourceLocation APP_PLOT = new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/apps/app_plot.png");
    private static final ResourceLocation APP_SETTINGS = new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/apps/app_settings.png");
    private static final ResourceLocation CLOSE_ICON = new ResourceLocation(ScheduleMC.MOD_ID, "textures/gui/apps/close.png");

    private int leftPos;
    private int topPos;
    private int scrollOffset = 0; // Scroll-Offset in Pixeln
    private int maxScrollOffset; // Maximaler Scroll-Offset
    private int hoveredAppIndex = -1; // Welche App wird gerade gehovered (-1 = keine)
    private boolean isDraggingScrollbar = false; // Wird die Scrollbar gerade gezogen?
    private int dragStartY = 0; // Y-Position beim Start des Draggens
    private int dragStartScrollOffset = 0; // Scroll-Offset beim Start des Draggens

    public SmartphoneScreen() {
        super(Component.literal("Smartphone"));
    }

    @Override
    protected void init() {
        super.init();

        // Sende Paket an Server: Smartphone ist jetzt offen
        SmartphoneNetworkHandler.sendToServer(new SmartphoneStatePacket(true));

        // Zentriere das Smartphone horizontal
        this.leftPos = (this.width - PHONE_WIDTH) / 2;

        // Zentriere das Smartphone vertikal mit Margin-Check
        // Der Rahmen geht von (topPos - BORDER_SIZE) bis (topPos + PHONE_HEIGHT + BORDER_SIZE)

        // Für Zentrierung: topPos = (height - PHONE_HEIGHT) / 2
        int centeredTop = (this.height - PHONE_HEIGHT) / 2;

        // Obere Grenze: Rahmen muss mindestens MARGIN_TOP vom oberen Rand sein
        // topPos - BORDER_SIZE >= MARGIN_TOP → topPos >= MARGIN_TOP + BORDER_SIZE
        int minTop = MARGIN_TOP + BORDER_SIZE;

        // Untere Grenze: Rahmen muss mindestens MARGIN_BOTTOM vom unteren Rand sein
        // topPos + PHONE_HEIGHT + BORDER_SIZE <= height - MARGIN_BOTTOM
        // → topPos <= height - PHONE_HEIGHT - BORDER_SIZE - MARGIN_BOTTOM
        int maxTop = this.height - PHONE_HEIGHT - BORDER_SIZE - MARGIN_BOTTOM;

        // Wende Grenzen an
        this.topPos = Math.max(minTop, Math.min(centeredTop, maxTop));

        // Berechne maximalen Scroll-Offset
        // Gesamthöhe aller Reihen - sichtbare Höhe
        int totalContentHeight = (APP_ICON_SIZE * TOTAL_ROWS) + (APP_SPACING * (TOTAL_ROWS - 1));
        int visibleContentHeight = (APP_ICON_SIZE * VISIBLE_ROWS) + (APP_SPACING * (VISIBLE_ROWS - 1));
        this.maxScrollOffset = Math.max(0, totalContentHeight - visibleContentHeight);

        // Berechne Start-Position für App-Grid (zentriert im Smartphone)
        int gridWidth = (APP_ICON_SIZE * 2) + APP_SPACING;
        int gridStartX = leftPos + (PHONE_WIDTH - gridWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2;
        int gridStartY = topPos + 45; // Abstand von oben

        // Keine App-Buttons mehr - Klick-Handling erfolgt manuell in mouseClicked()

        // === SCHLIESSEN-BUTTON (oben rechts) ===
        addRenderableWidget(Button.builder(Component.literal("X"), button -> {
            this.onClose();
        }).bounds(leftPos + PHONE_WIDTH - CLOSE_BUTTON_SIZE - 10, topPos + 10, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE).build());

        // === ZURÜCK-BUTTON (unten zentriert) ===
        addRenderableWidget(Button.builder(Component.literal("Zurück"), button -> {
            this.onClose();
        }).bounds(leftPos + (PHONE_WIDTH - 80) / 2, topPos + PHONE_HEIGHT - 25, 80, 20).build());
    }

    /**
     * Öffnet eine App
     */
    private void openApp(Screen appScreen) {
        if (minecraft != null) {
            minecraft.setScreen(appScreen);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Linksklick
            int gridWidth = (APP_ICON_SIZE * 2) + APP_SPACING;
            int gridStartX = leftPos + (PHONE_WIDTH - gridWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2;
            int gridStartY = topPos + 45;
            int visibleContentHeight = (APP_ICON_SIZE * VISIBLE_ROWS) + (APP_SPACING * (VISIBLE_ROWS - 1));

            // === PRÜFE OB AUF SCROLLBAR GEKLICKT WURDE ===
            int scrollbarX = gridStartX + gridWidth + SCROLLBAR_MARGIN;
            int scrollbarY = gridStartY;

            if (mouseX >= scrollbarX && mouseX <= scrollbarX + SCROLLBAR_WIDTH &&
                mouseY >= scrollbarY && mouseY <= scrollbarY + visibleContentHeight) {

                // Berechne Scrollbar-Handle Position
                if (maxScrollOffset > 0) {
                    float scrollPercentage = (float) scrollOffset / maxScrollOffset;
                    int handleHeight = Math.max(20, visibleContentHeight * visibleContentHeight /
                        ((APP_ICON_SIZE * TOTAL_ROWS) + (APP_SPACING * (TOTAL_ROWS - 1))));
                    int handleY = scrollbarY + (int) ((visibleContentHeight - handleHeight) * scrollPercentage);

                    // Prüfe ob auf Handle geklickt wurde
                    if (mouseY >= handleY && mouseY <= handleY + handleHeight) {
                        isDraggingScrollbar = true;
                        dragStartY = (int) mouseY;
                        dragStartScrollOffset = scrollOffset;
                        return true;
                    }

                    // Klick auf Track (außerhalb Handle) - springe zu der Position
                    float clickPercentage = (float) (mouseY - scrollbarY) / visibleContentHeight;
                    scrollOffset = Math.max(0, Math.min(maxScrollOffset, (int) (clickPercentage * maxScrollOffset)));
                    return true;
                }
            }

            // Berechne welche App-Position angeklickt wurde (mit scrollOffset)
            int relativeX = (int) mouseX - gridStartX;
            int relativeY = (int) mouseY - gridStartY + scrollOffset;

            // Prüfe ob Klick im App-Grid-Bereich war
            if (relativeX >= 0 && relativeX <= gridWidth) {
                // Berechne Spalte (0 oder 1)
                int col = -1;
                if (relativeX < APP_ICON_SIZE) {
                    col = 0;
                } else if (relativeX >= APP_ICON_SIZE + APP_SPACING && relativeX < APP_ICON_SIZE * 2 + APP_SPACING) {
                    col = 1;
                }

                if (col >= 0) {
                    // Berechne Reihe (0-3)
                    int row = relativeY / (APP_ICON_SIZE + APP_SPACING);
                    int rowOffset = relativeY % (APP_ICON_SIZE + APP_SPACING);

                    // Prüfe ob Klick auf einem Icon war (nicht im Spacing)
                    if (rowOffset < APP_ICON_SIZE && row >= 0 && row < TOTAL_ROWS) {
                        // Bestimme welche App
                        int appIndex = row * 2 + col;

                        switch (appIndex) {
                            case 0: openApp(new MapAppScreen(this)); return true;
                            case 1: openApp(new DealerAppScreen(this)); return true;
                            case 2: openApp(new ProductsAppScreen(this)); return true;
                            case 3: openApp(new OrderAppScreen(this)); return true;
                            case 4: openApp(new ContactsAppScreen(this)); return true;
                            case 5: openApp(new MessagesAppScreen(this)); return true;
                            case 6: openApp(new PlotAppScreen(this)); return true;
                            case 7: openApp(new SettingsAppScreen(this)); return true;
                        }
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Halbtransparenter Hintergrund
        renderBackground(guiGraphics);

        // Smartphone-Gehäuse (dunkel mit Rand)
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + PHONE_WIDTH + 5, topPos + PHONE_HEIGHT + 5, 0xFF1C1C1C); // Rahmen
        guiGraphics.fill(leftPos, topPos, leftPos + PHONE_WIDTH, topPos + PHONE_HEIGHT, 0xFF2A2A2A); // Innerer Hintergrund

        // Oberer Bereich (Status-Bar)
        guiGraphics.fill(leftPos, topPos, leftPos + PHONE_WIDTH, topPos + 30, 0xFF1A1A1A);

        // Smartphone-Titel
        guiGraphics.drawCenteredString(this.font, "§6§lSmartphone", leftPos + PHONE_WIDTH / 2, topPos + 12, 0xFFFFFF);

        // Berechne Grid-Position für App-Labels
        int gridWidth = (APP_ICON_SIZE * 2) + APP_SPACING;
        int gridStartX = leftPos + (PHONE_WIDTH - gridWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2;
        int gridStartY = topPos + 45;
        int visibleContentHeight = (APP_ICON_SIZE * VISIBLE_ROWS) + (APP_SPACING * (VISIBLE_ROWS - 1));

        // === HOVER-ERKENNUNG ===
        // Berechne welche App gehovered wird
        hoveredAppIndex = -1;
        int relativeX = mouseX - gridStartX;
        int relativeY = mouseY - gridStartY + scrollOffset;

        if (mouseX >= gridStartX && mouseX <= gridStartX + gridWidth &&
            mouseY >= gridStartY && mouseY <= gridStartY + visibleContentHeight) {

            // Berechne Spalte (0 oder 1)
            int col = -1;
            if (relativeX >= 0 && relativeX < APP_ICON_SIZE) {
                col = 0;
            } else if (relativeX >= APP_ICON_SIZE + APP_SPACING && relativeX < APP_ICON_SIZE * 2 + APP_SPACING) {
                col = 1;
            }

            if (col >= 0) {
                // Berechne Reihe (0-3)
                int row = relativeY / (APP_ICON_SIZE + APP_SPACING);
                int rowOffset = relativeY % (APP_ICON_SIZE + APP_SPACING);

                // Prüfe ob Hover auf einem Icon ist (nicht im Spacing)
                if (rowOffset >= 0 && rowOffset < APP_ICON_SIZE && row >= 0 && row < TOTAL_ROWS) {
                    hoveredAppIndex = row * 2 + col;
                }
            }
        }

        // Aktiviere Scissor (Clipping) für den scrollbaren Bereich
        guiGraphics.enableScissor(
            gridStartX,
            gridStartY,
            gridStartX + gridWidth,
            gridStartY + visibleContentHeight
        );

        // App-Icons rendern mit scrollOffset (4 Reihen x 2 Spalten)
        renderAppIcon(guiGraphics, gridStartX, gridStartY - scrollOffset, APP_MAP, "Map", 0);
        renderAppIcon(guiGraphics, gridStartX + APP_ICON_SIZE + APP_SPACING, gridStartY - scrollOffset, APP_DEALER, "Dealer", 1);

        renderAppIcon(guiGraphics, gridStartX, gridStartY + APP_ICON_SIZE + APP_SPACING - scrollOffset, APP_PRODUCTS, "Produkte", 2);
        renderAppIcon(guiGraphics, gridStartX + APP_ICON_SIZE + APP_SPACING, gridStartY + APP_ICON_SIZE + APP_SPACING - scrollOffset,
            APP_ORDER, "Bestellung", 3);

        renderAppIcon(guiGraphics, gridStartX, gridStartY + (APP_ICON_SIZE + APP_SPACING) * 2 - scrollOffset, APP_CONTACTS, "Kontakte", 4);
        renderAppIcon(guiGraphics, gridStartX + APP_ICON_SIZE + APP_SPACING, gridStartY + (APP_ICON_SIZE + APP_SPACING) * 2 - scrollOffset,
            APP_MESSAGES, "Nachrichten", 5);

        renderAppIcon(guiGraphics, gridStartX, gridStartY + (APP_ICON_SIZE + APP_SPACING) * 3 - scrollOffset, APP_PLOT, "Immobilien", 6);
        renderAppIcon(guiGraphics, gridStartX + APP_ICON_SIZE + APP_SPACING, gridStartY + (APP_ICON_SIZE + APP_SPACING) * 3 - scrollOffset,
            APP_SETTINGS, "Settings", 7);

        // Deaktiviere Scissor
        guiGraphics.disableScissor();

        // === SCROLL-INDIKATOREN (Fade-Effekte) ===
        // Oberer Fade-Gradient wenn nach unten gescrollt wurde
        if (scrollOffset > 0) {
            guiGraphics.fillGradient(
                gridStartX,
                gridStartY,
                gridStartX + gridWidth,
                gridStartY + 15,
                0xAA2A2A2A,
                0x002A2A2A
            );
        }

        // Unterer Fade-Gradient wenn noch mehr Content verfügbar ist
        if (scrollOffset < maxScrollOffset) {
            guiGraphics.fillGradient(
                gridStartX,
                gridStartY + visibleContentHeight - 15,
                gridStartX + gridWidth,
                gridStartY + visibleContentHeight,
                0x002A2A2A,
                0xAA2A2A2A
            );
        }

        // Zeichne Scrollbar
        int scrollbarX = gridStartX + gridWidth + SCROLLBAR_MARGIN;
        int scrollbarY = gridStartY;
        int scrollbarHeight = visibleContentHeight;

        // Scrollbar-Hintergrund
        guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + SCROLLBAR_WIDTH, scrollbarY + scrollbarHeight, 0xFF1A1A1A);

        // Scrollbar-Handle
        if (maxScrollOffset > 0) {
            float scrollPercentage = (float) scrollOffset / maxScrollOffset;
            int handleHeight = Math.max(20, scrollbarHeight * visibleContentHeight /
                ((APP_ICON_SIZE * TOTAL_ROWS) + (APP_SPACING * (TOTAL_ROWS - 1))));
            int handleY = scrollbarY + (int) ((scrollbarHeight - handleHeight) * scrollPercentage);

            // Prüfe ob Scrollbar gehovered oder gedraggt wird
            boolean isScrollbarHovered = (mouseX >= scrollbarX && mouseX <= scrollbarX + SCROLLBAR_WIDTH &&
                                          mouseY >= handleY && mouseY <= handleY + handleHeight);

            // Handle-Farbe (heller wenn gehovered oder gedraggt)
            int handleColor = (isDraggingScrollbar || isScrollbarHovered) ? 0xFFAAAAAA : 0xFF888888;
            guiGraphics.fill(scrollbarX, handleY, scrollbarX + SCROLLBAR_WIDTH, handleY + handleHeight, handleColor);
        }

        // Buttons rendern
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * Rendert ein App-Icon mit Label
     */
    private void renderAppIcon(GuiGraphics guiGraphics, int x, int y, ResourceLocation iconTexture, String label, int appIndex) {
        // Icon-Hintergrund (heller wenn gehovered)
        boolean isHovered = (hoveredAppIndex == appIndex);
        int backgroundColor = isHovered ? 0xFF4A4A4A : 0xFF3A3A3A;
        guiGraphics.fill(x, y, x + APP_ICON_SIZE, y + APP_ICON_SIZE, backgroundColor);

        // Hover-Rahmen (subtiler Glow-Effekt)
        if (isHovered) {
            // Äußerer Rahmen
            guiGraphics.fill(x - 1, y - 1, x + APP_ICON_SIZE + 1, y, 0xFF6A6A6A); // Oben
            guiGraphics.fill(x - 1, y + APP_ICON_SIZE, x + APP_ICON_SIZE + 1, y + APP_ICON_SIZE + 1, 0xFF6A6A6A); // Unten
            guiGraphics.fill(x - 1, y, x, y + APP_ICON_SIZE, 0xFF6A6A6A); // Links
            guiGraphics.fill(x + APP_ICON_SIZE, y, x + APP_ICON_SIZE + 1, y + APP_ICON_SIZE, 0xFF6A6A6A); // Rechts
        }

        boolean iconRendered = false;

        // Versuche Icon zu laden, falls nicht vorhanden zeige Platzhalter
        if (iconTexture != null) {
            try {
                RenderSystem.setShaderTexture(0, iconTexture);
                guiGraphics.blit(iconTexture, x, y, 0, 0, APP_ICON_SIZE, APP_ICON_SIZE, APP_ICON_SIZE, APP_ICON_SIZE);
                iconRendered = true;
            } catch (Exception e) {
                // Icon konnte nicht geladen werden
            }
        }

        if (!iconRendered) {
            // Platzhalter wenn Bild nicht gefunden
            guiGraphics.fill(x + 2, y + 2, x + APP_ICON_SIZE - 2, y + APP_ICON_SIZE - 2, 0xFF505050);

            // Erste 2 Buchstaben als Platzhalter
            String initials = label.length() >= 2 ? label.substring(0, 2).toUpperCase() : label.toUpperCase();
            int textWidth = this.font.width(initials);
            guiGraphics.drawString(this.font, "§f" + initials,
                x + (APP_ICON_SIZE - textWidth) / 2,
                y + (APP_ICON_SIZE - 8) / 2,
                0xFFFFFF);
        }

        // Label unter dem Icon mit Ellipsis wenn zu lang
        String displayLabel = label;
        int maxLabelWidth = APP_ICON_SIZE + 15; // Mehr Platz für Labels
        int labelWidth = this.font.width(label);

        // Kürze Label wenn zu lang
        if (labelWidth > maxLabelWidth) {
            // Finde die maximale Länge die passt
            StringBuilder truncated = new StringBuilder();
            for (int i = 0; i < label.length(); i++) {
                String test = label.substring(0, i) + "...";
                if (this.font.width(test) > maxLabelWidth) {
                    break;
                }
                truncated = new StringBuilder(label.substring(0, i));
            }
            displayLabel = truncated.toString() + "...";
            labelWidth = this.font.width(displayLabel);
        }

        // Label zwischen den Reihen zentrieren (in der Mitte des Spacings)
        int labelY = y + APP_ICON_SIZE + (APP_SPACING / 2) - 3;
        guiGraphics.drawString(this.font, "§7" + displayLabel,
            x + (APP_ICON_SIZE - labelWidth) / 2,
            labelY,
            0xFFFFFF);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingScrollbar && button == 0) {
            int gridWidth = (APP_ICON_SIZE * 2) + APP_SPACING;
            int gridStartX = leftPos + (PHONE_WIDTH - gridWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2;
            int gridStartY = topPos + 45;
            int visibleContentHeight = (APP_ICON_SIZE * VISIBLE_ROWS) + (APP_SPACING * (VISIBLE_ROWS - 1));

            // Berechne wie viel die Maus bewegt wurde
            int deltaY = (int) mouseY - dragStartY;

            // Berechne Handle-Höhe
            int handleHeight = Math.max(20, visibleContentHeight * visibleContentHeight /
                ((APP_ICON_SIZE * TOTAL_ROWS) + (APP_SPACING * (TOTAL_ROWS - 1))));

            // Konvertiere deltaY zu scrollOffset-Änderung
            // deltaY / (visibleContentHeight - handleHeight) = scrollOffsetDelta / maxScrollOffset
            float scrollPercentageDelta = (float) deltaY / (visibleContentHeight - handleHeight);
            int scrollOffsetDelta = (int) (scrollPercentageDelta * maxScrollOffset);

            scrollOffset = Math.max(0, Math.min(maxScrollOffset, dragStartScrollOffset + scrollOffsetDelta));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isDraggingScrollbar) {
            isDraggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Scroll nur, wenn Maus über dem App-Bereich ist
        int gridWidth = (APP_ICON_SIZE * 2) + APP_SPACING;
        int gridStartX = leftPos + (PHONE_WIDTH - gridWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2;
        int gridStartY = topPos + 45;
        int visibleContentHeight = (APP_ICON_SIZE * VISIBLE_ROWS) + (APP_SPACING * (VISIBLE_ROWS - 1));

        if (mouseX >= gridStartX && mouseX <= gridStartX + gridWidth + SCROLLBAR_WIDTH + SCROLLBAR_MARGIN &&
            mouseY >= gridStartY && mouseY <= gridStartY + visibleContentHeight) {

            int scrollAmount = (int) (delta * 10); // Scroll-Geschwindigkeit
            scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset - scrollAmount));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        // Sende Paket an Server: Smartphone ist jetzt geschlossen
        SmartphoneNetworkHandler.sendToServer(new SmartphoneStatePacket(false));
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Spiel läuft weiter
    }

    /**
     * Prüft ob das Smartphone-GUI geöffnet ist
     */
    public static boolean isOpen() {
        return net.minecraft.client.Minecraft.getInstance().screen instanceof SmartphoneScreen;
    }
}
