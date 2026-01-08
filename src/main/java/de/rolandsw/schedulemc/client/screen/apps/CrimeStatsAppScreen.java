package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.npc.crime.CrimeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

/**
 * Crime-Stats-App - Fahndungs- und Verbrechensinformationen
 *
 * Features:
 * - Aktuelles Wanted-Level anzeigen
 * - Kautions-Kosten berechnen
 * - Escape-Timer Status
 * - Crime-Statistiken
 */
@OnlyIn(Dist.CLIENT)
public class CrimeStatsAppScreen extends Screen {

    private final Screen parentScreen;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 240;
    private static final int BORDER_SIZE = 5;
    private static final int MARGIN_TOP = 15;
    private static final int MARGIN_BOTTOM = 60;

    // Tab-System
    private int currentTab = 0;
    private static final String[] TAB_NAMES = {"Status", "Kaution", "Info"};
    private static final int TAB_HEIGHT = 22;
    private static final int TAB_WIDTH = 62;

    // Scrolling
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int SCROLL_SPEED = 15;
    private static final int CONTENT_HEIGHT = 140;

    private int leftPos;
    private int topPos;

    // Cached Data
    private int wantedLevel = 0;
    private double bailCost = 0.0;
    private boolean isBeingChased = false;

    // Kosten pro Wanted-Stern
    private static final double BAIL_COST_PER_STAR = 1000.0;

    public CrimeStatsAppScreen(Screen parent) {
        super(Component.literal("Crime Stats"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;

        int centeredTop = (this.height - HEIGHT) / 2;
        int minTop = MARGIN_TOP + BORDER_SIZE;
        int maxTop = this.height - HEIGHT - BORDER_SIZE - MARGIN_BOTTOM;
        this.topPos = Math.max(minTop, Math.min(centeredTop, maxTop));

        // Cache data
        refreshData();

        // Tab-Buttons
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int tabIndex = i;
            addRenderableWidget(Button.builder(
                Component.literal(TAB_NAMES[i]),
                button -> {
                    currentTab = tabIndex;
                    scrollOffset = 0;
                    refreshData();
                }
            ).bounds(leftPos + 5 + (i * TAB_WIDTH), topPos + 30, TAB_WIDTH - 2, TAB_HEIGHT).build());
        }

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.literal("← Zurück"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());

        // Refresh-Button
        addRenderableWidget(Button.builder(Component.literal("↻"), button -> {
            refreshData();
        }).bounds(leftPos + WIDTH - 30, topPos + HEIGHT - 30, 20, 20).build());
    }

    /**
     * Aktualisiert gecachte Daten
     */
    private void refreshData() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        UUID playerUUID = mc.player.getUUID();

        // Wanted Level
        wantedLevel = CrimeManager.getWantedLevel(playerUUID);

        // Bail Cost (1000€ pro Stern)
        bailCost = wantedLevel * BAIL_COST_PER_STAR;

        // TODO: Check if being chased (würde Server-Packet benötigen)
        isBeingChased = wantedLevel > 0;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Smartphone-Hintergrund
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF2A2A2A);

        // Header
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 28, 0xFF1A1A1A);
        guiGraphics.drawCenteredString(this.font, "§c§lPolizei-Akte", leftPos + WIDTH / 2, topPos + 10, 0xFFFFFF);

        // Tab-Hintergrund (aktiver Tab hervorheben)
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tabX = leftPos + 5 + (i * TAB_WIDTH);
            int tabY = topPos + 30;
            if (i == currentTab) {
                guiGraphics.fill(tabX - 1, tabY - 1, tabX + TAB_WIDTH - 1, tabY + TAB_HEIGHT + 1, 0xFF4A90E2);
            }
        }

        // Content-Bereich
        int contentY = topPos + 55;
        int contentEndY = topPos + HEIGHT - 40;

        // Render Tab-Content
        switch (currentTab) {
            case 0 -> renderStatusTab(guiGraphics, contentY, contentEndY);
            case 1 -> renderBailTab(guiGraphics, contentY, contentEndY);
            case 2 -> renderInfoTab(guiGraphics, contentY, contentEndY);
        }

        // Scroll-Indikator
        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(20, CONTENT_HEIGHT * CONTENT_HEIGHT / (CONTENT_HEIGHT + maxScroll));
            int scrollBarY = contentY + (scrollOffset * (CONTENT_HEIGHT - scrollBarHeight) / maxScroll);
            guiGraphics.fill(leftPos + WIDTH - 8, contentY, leftPos + WIDTH - 5, contentEndY, 0x44FFFFFF);
            guiGraphics.fill(leftPos + WIDTH - 8, scrollBarY, leftPos + WIDTH - 5, scrollBarY + scrollBarHeight, 0xAAFFFFFF);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 1: STATUS
    // ═══════════════════════════════════════════════════════════

    private void renderStatusTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        // Wanted-Level Box
        if (y >= startY - 80 && y < endY) {
            int boxColor = wantedLevel > 0 ? 0x44AA0000 : 0x44228B22;
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 75, boxColor);

            guiGraphics.drawCenteredString(this.font, "§f§lFAHNDUNGSStufe", leftPos + WIDTH / 2, y + 5, 0xFFFFFF);

            // Sterne visualisieren
            String stars = getStarDisplay(wantedLevel);
            guiGraphics.drawCenteredString(this.font, stars, leftPos + WIDTH / 2, y + 22, 0xFFFFFF);

            // Level-Text
            String levelText = getLevelText(wantedLevel);
            String levelColor = wantedLevel > 0 ? "§c" : "§a";
            guiGraphics.drawCenteredString(this.font, levelColor + "§l" + levelText, leftPos + WIDTH / 2, y + 40, 0xFFFFFF);

            // Numerischer Level
            guiGraphics.drawCenteredString(this.font, "§7Level: §f" + wantedLevel + " / 5", leftPos + WIDTH / 2, y + 55, 0xAAAAAA);
        }
        y += 80;
        contentHeight += 80;

        // Verfolgungsstatus
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (wantedLevel > 0) {
            if (y >= startY - 35 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 30, 0x66AA0000);
                guiGraphics.drawString(this.font, "§c§l⚠ VERFOLGUNG AKTIV", leftPos + 15, y + 3, 0xFF5555);
                guiGraphics.drawString(this.font, "§7Polizei sucht dich!", leftPos + 15, y + 14, 0xFFFFFF);
            }
            y += 35;
            contentHeight += 35;
        } else {
            if (y >= startY - 25 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 20, 0x44228B22);
                guiGraphics.drawString(this.font, "§a✓ KEINE FAHNDUNG", leftPos + 15, y + 5, 0x55FF55);
            }
            y += 25;
            contentHeight += 25;
        }

        // Tipps
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§6§lTIPPS:", leftPos + 15, y, 0xFFAA00);
        }
        y += 12;
        contentHeight += 12;

        if (wantedLevel > 0) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§7• Verstecke dich", leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§7• Zahle Kaution", leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§7• Warte 1 Tag ab", leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;
        } else {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§7• Bleib sauber!", leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§7• Vermeide Straftaten", leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;
        }

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 2: KAUTION
    // ═══════════════════════════════════════════════════════════

    private void renderBailTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        // Kautions-Info
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§6§lKAUTION", leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        if (wantedLevel == 0) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawCenteredString(this.font, "§aKeine Kaution nötig", leftPos + WIDTH / 2, y + 20, 0x55FF55);
                guiGraphics.drawCenteredString(this.font, "§7Du bist unschuldig!", leftPos + WIDTH / 2, y + 35, 0xAAAAAA);
            }
            y += 60;
            contentHeight += 60;
        } else {
            // Kautions-Box
            if (y >= startY - 60 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 55, 0x44AA0000);

                guiGraphics.drawString(this.font, "§fFahndungsstufe:", leftPos + 15, y + 5, 0xFFFFFF);
                guiGraphics.drawString(this.font, "§c" + wantedLevel + " Sterne", leftPos + 120, y + 5, 0xFF5555);

                guiGraphics.drawString(this.font, "§fKosten pro Stern:", leftPos + 15, y + 18, 0xFFFFFF);
                guiGraphics.drawString(this.font, String.format("§e%.0f€", BAIL_COST_PER_STAR), leftPos + 120, y + 18, 0xFFAA00);

                guiGraphics.fill(leftPos + 15, y + 30, leftPos + WIDTH - 15, y + 31, 0x44FFFFFF);

                guiGraphics.drawString(this.font, "§f§lGESAMT:", leftPos + 15, y + 35, 0xFFFFFF);
                guiGraphics.drawString(this.font, String.format("§c§l%.0f€", bailCost), leftPos + 100, y + 35, 0xFF5555);
            }
            y += 60;
            contentHeight += 60;

            // Info-Text
            if (y >= startY - 10 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
            }
            y += 8;
            contentHeight += 8;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§8Kaution zahlen:", leftPos + 15, y, 0x888888);
            }
            y += 12;
            contentHeight += 12;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§7• Gehe zur Polizei-Station", leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§7• Zahle Kaution", leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, "§7• Wanted-Level wird gelöscht", leftPos + 15, y, 0xAAAAAA);
            }
            y += 15;
            contentHeight += 15;
        }

        // Alternative
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§8Alternative:", leftPos + 15, y, 0x888888);
        }
        y += 12;
        contentHeight += 12;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§7Warte 1 Tag = -1 Stern", leftPos + 15, y, 0xAAAAAA);
        }
        y += 11;
        contentHeight += 11;

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════
    // TAB 3: INFO
    // ═══════════════════════════════════════════════════════════

    private void renderInfoTab(GuiGraphics guiGraphics, int startY, int endY) {
        int y = startY - scrollOffset;
        int contentHeight = 0;

        // Überschrift
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§6§lWANTED-SYSTEM", leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        // Erklärung
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§fSterne-Bedeutung:", leftPos + 15, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        String[] starInfo = {
            "§c★§7 = Kleinkriminalität",
            "§c★★§7 = Diebstahl",
            "§c★★★§7 = Schwere Straftat",
            "§c★★★★§7 = Gewalttat",
            "§c★★★★★§7 = Schwerverbrecher"
        };

        for (String line : starInfo) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, line, leftPos + 15, y, 0xFFFFFF);
            }
            y += 11;
            contentHeight += 11;
        }

        y += 5;
        contentHeight += 5;

        // Polizei-Verhalten
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§fPolizei-Verhalten:", leftPos + 15, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        String[] policeInfo = {
            "§71-2 Sterne: Festnahme",
            "§73-4 Sterne: Verstärkung",
            "§75 Sterne: Großfahndung"
        };

        for (String line : policeInfo) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, line, leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;
        }

        y += 5;
        contentHeight += 5;

        // Escape-Mechanik
        if (y >= startY - 10 && y < endY) {
            guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        }
        y += 8;
        contentHeight += 8;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§fEntkommen:", leftPos + 15, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§730 Sekunden verstecken", leftPos + 15, y, 0xAAAAAA);
        }
        y += 11;
        contentHeight += 11;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, "§7> 40 Blöcke Entfernung", leftPos + 15, y, 0xAAAAAA);
        }
        y += 11;
        contentHeight += 11;

        maxScroll = Math.max(0, contentHeight - CONTENT_HEIGHT);
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    private String getStarDisplay(int level) {
        if (level == 0) return "§7☆☆☆☆☆";

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < level) {
                stars.append("§c★");
            } else {
                stars.append("§7☆");
            }
        }
        return stars.toString();
    }

    private String getLevelText(int level) {
        return switch (level) {
            case 0 -> "SAUBER";
            case 1 -> "VERDÄCHTIG";
            case 2 -> "GESUCHT";
            case 3 -> "GEFÄHRLICH";
            case 4 -> "SEHR GEFÄHRLICH";
            case 5 -> "SCHWERVERBRECHER";
            default -> "UNBEKANNT";
        };
    }

    // ═══════════════════════════════════════════════════════════
    // SCROLL HANDLING
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll > 0) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - delta * SCROLL_SPEED));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
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
        return false;
    }
}
