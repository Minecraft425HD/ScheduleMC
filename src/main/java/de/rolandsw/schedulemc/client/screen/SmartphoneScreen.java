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
 * Symmetrisch angeordnet (4x2 Grid)
 */
@OnlyIn(Dist.CLIENT)
public class SmartphoneScreen extends Screen {

    // Layout-Konstanten (kompakter für ALLE Bildschirmgrößen)
    private static final int PHONE_WIDTH = 200;
    private static final int PHONE_HEIGHT = 280; // Erhöht für 4. Reihe
    private static final int APP_ICON_SIZE = 36; // Etwas kleiner für mehr Platz
    private static final int APP_SPACING = 10; // Reduziert für 4 Reihen
    private static final int CLOSE_BUTTON_SIZE = 20;
    private static final int BORDER_SIZE = 5; // Rahmen um das Smartphone
    private static final int MARGIN_TOP = 15; // Mindestabstand vom oberen Bildschirmrand
    private static final int MARGIN_BOTTOM = 60; // Erhöht von 35 - genug Platz für Hotbar!

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

        // Berechne Start-Position für App-Grid (zentriert im Smartphone)
        int gridWidth = (APP_ICON_SIZE * 2) + APP_SPACING;
        int gridHeight = (APP_ICON_SIZE * 4) + (APP_SPACING * 3);
        int gridStartX = leftPos + (PHONE_WIDTH - gridWidth) / 2;
        int gridStartY = topPos + 45; // Abstand von oben

        // === APP BUTTONS (4 Reihen x 2 Spalten) ===

        // Reihe 1: Map, Dealer
        createAppButton(gridStartX, gridStartY, "Map", () -> openApp(new MapAppScreen(this)));
        createAppButton(gridStartX + APP_ICON_SIZE + APP_SPACING, gridStartY, "Dealer",
            () -> openApp(new DealerAppScreen(this)));

        // Reihe 2: Produkte, Bestellung
        createAppButton(gridStartX, gridStartY + APP_ICON_SIZE + APP_SPACING, "Produkte",
            () -> openApp(new ProductsAppScreen(this)));
        createAppButton(gridStartX + APP_ICON_SIZE + APP_SPACING, gridStartY + APP_ICON_SIZE + APP_SPACING,
            "Bestellung", () -> openApp(new OrderAppScreen(this)));

        // Reihe 3: Kontakte, Nachrichten
        createAppButton(gridStartX, gridStartY + (APP_ICON_SIZE + APP_SPACING) * 2, "Kontakte",
            () -> openApp(new ContactsAppScreen(this)));
        createAppButton(gridStartX + APP_ICON_SIZE + APP_SPACING, gridStartY + (APP_ICON_SIZE + APP_SPACING) * 2,
            "Nachrichten", () -> openApp(new MessagesAppScreen(this)));

        // Reihe 4: Immobilien (Plot App), Einstellungen
        createAppButton(gridStartX, gridStartY + (APP_ICON_SIZE + APP_SPACING) * 3, "Immobilien",
            () -> openApp(new PlotAppScreen(this)));
        createAppButton(gridStartX + APP_ICON_SIZE + APP_SPACING, gridStartY + (APP_ICON_SIZE + APP_SPACING) * 3,
            "Settings", () -> openApp(new SettingsAppScreen(this)));

        // === SCHLIESSEN-BUTTON (oben rechts) ===
        addRenderableWidget(Button.builder(Component.literal("X"), button -> {
            this.onClose();
        }).bounds(leftPos + PHONE_WIDTH - CLOSE_BUTTON_SIZE - 10, topPos + 10, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE).build());

        // === ZURÜCK-BUTTON (unten zentriert) ===
        addRenderableWidget(Button.builder(Component.literal("Zurück"), button -> {
            this.onClose();
        }).bounds(leftPos + (PHONE_WIDTH - 80) / 2, topPos + PHONE_HEIGHT - 35, 80, 20).build());
    }

    /**
     * Erstellt einen App-Button
     */
    private void createAppButton(int x, int y, String appName, Runnable onClick) {
        addRenderableWidget(Button.builder(Component.literal(""), button -> {
            onClick.run();
        }).bounds(x, y, APP_ICON_SIZE, APP_ICON_SIZE).build());
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
        int gridStartX = leftPos + (PHONE_WIDTH - gridWidth) / 2;
        int gridStartY = topPos + 45;

        // App-Icons rendern (4 Reihen x 2 Spalten)
        renderAppIcon(guiGraphics, gridStartX, gridStartY, APP_MAP, "Map");
        renderAppIcon(guiGraphics, gridStartX + APP_ICON_SIZE + APP_SPACING, gridStartY, APP_DEALER, "Dealer");

        renderAppIcon(guiGraphics, gridStartX, gridStartY + APP_ICON_SIZE + APP_SPACING, APP_PRODUCTS, "Produkte");
        renderAppIcon(guiGraphics, gridStartX + APP_ICON_SIZE + APP_SPACING, gridStartY + APP_ICON_SIZE + APP_SPACING,
            APP_ORDER, "Bestellung");

        renderAppIcon(guiGraphics, gridStartX, gridStartY + (APP_ICON_SIZE + APP_SPACING) * 2, APP_CONTACTS, "Kontakte");
        renderAppIcon(guiGraphics, gridStartX + APP_ICON_SIZE + APP_SPACING, gridStartY + (APP_ICON_SIZE + APP_SPACING) * 2,
            APP_MESSAGES, "Nachrichten");

        renderAppIcon(guiGraphics, gridStartX, gridStartY + (APP_ICON_SIZE + APP_SPACING) * 3, APP_PLOT, "Immobilien");
        renderAppIcon(guiGraphics, gridStartX + APP_ICON_SIZE + APP_SPACING, gridStartY + (APP_ICON_SIZE + APP_SPACING) * 3,
            APP_SETTINGS, "Settings");

        // Buttons rendern
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * Rendert ein App-Icon mit Label
     */
    private void renderAppIcon(GuiGraphics guiGraphics, int x, int y, ResourceLocation iconTexture, String label) {
        // Icon-Hintergrund
        guiGraphics.fill(x, y, x + APP_ICON_SIZE, y + APP_ICON_SIZE, 0xFF3A3A3A);

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

        // Label unter dem Icon
        int labelWidth = this.font.width(label);
        guiGraphics.drawString(this.font, "§7" + label,
            x + (APP_ICON_SIZE - labelWidth) / 2,
            y + APP_ICON_SIZE + 4,
            0xFFFFFF);
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
