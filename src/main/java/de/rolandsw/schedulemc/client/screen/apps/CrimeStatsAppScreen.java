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
    private static final int MARGIN_TOP = 5;
    private static final int MARGIN_BOTTOM = 60;

    // Tab-System
    private int currentTab = 0;
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

    // PERFORMANCE: Tab-Namen einmal cachen statt Component.translatable().getString() pro Aufruf
    private String[] cachedTabNames;

    private String[] getTabNames() {
        return cachedTabNames;
    }

    // PERFORMANCE: Gecachte Render-Strings - eliminiert per-frame GC pressure
    // Header
    private String cachedHeaderStr;
    // Status tab
    private String cachedWantedLevelTitle;
    private String cachedLevelNumeric;
    private String cachedBeingChased;
    private String cachedPoliceSearching;
    private String cachedNoWanted;
    private String cachedTipsTitle;
    private String cachedTipHide;
    private String cachedTipPayBail;
    private String cachedTipWait;
    private String cachedTipStayClean;
    private String cachedTipAvoidCrime;
    // Bail tab
    private String cachedBailTitle;
    private String cachedNoBailNeeded;
    private String cachedInnocent;
    private String cachedWantedLevelLabel;
    private String cachedStarsStr;
    private String cachedCostPerStar;
    private String cachedTotal;
    private String cachedHowToPay;
    private String cachedGotoPolice;
    private String cachedPayBailAction;
    private String cachedWantedCleared;
    private String cachedAlternative;
    private String cachedWaitDecay;
    // Info tab
    private String cachedWantedSystemTitle;
    private String cachedStarMeaning;
    private String[] cachedStarInfo;
    private String cachedPoliceBehavior;
    private String[] cachedPoliceInfo;
    private String cachedEscapeTitle;
    private String cachedEscapeHide;
    private String cachedEscapeDistance;
    // Pre-computed wanted level display (updated in refreshData when wantedLevel changes)
    private String cachedStarDisplay;
    private String cachedLevelText;

    // Kosten pro Wanted-Stern
    private static final double BAIL_COST_PER_STAR = 1000.0;
    /** Gecachte formatierte Strings (statt String.format() pro Frame) */
    private static final String CACHED_BAIL_COST_PER_STAR = String.format("§e%.0f€", BAIL_COST_PER_STAR);
    private String cachedBailCostFormatted = "";

    public CrimeStatsAppScreen(Screen parent) {
        super(Component.translatable("gui.app.crime.title"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;

        // Positioniere oben mit Margin
        this.topPos = MARGIN_TOP;

        // PERFORMANCE: Cache all translatable strings once in init()
        this.cachedTabNames = new String[]{
            Component.translatable("app.crime_stats.tab_status").getString(),
            Component.translatable("app.crime_stats.tab_bail").getString(),
            Component.translatable("app.crime_stats.tab_info").getString()
        };
        this.cachedHeaderStr = Component.translatable("app.crime_stats.header").getString();
        // Status tab strings
        this.cachedWantedLevelTitle = Component.translatable("app.crime_stats.wanted_level_title").getString();
        this.cachedBeingChased = Component.translatable("app.crime_stats.being_chased").getString();
        this.cachedPoliceSearching = Component.translatable("app.crime_stats.police_searching").getString();
        this.cachedNoWanted = Component.translatable("app.crime_stats.no_wanted").getString();
        this.cachedTipsTitle = Component.translatable("app.crime_stats.tips_title").getString();
        this.cachedTipHide = Component.translatable("app.crime_stats.tip_hide").getString();
        this.cachedTipPayBail = Component.translatable("app.crime_stats.tip_pay_bail").getString();
        this.cachedTipWait = Component.translatable("app.crime_stats.tip_wait").getString();
        this.cachedTipStayClean = Component.translatable("app.crime_stats.tip_stay_clean").getString();
        this.cachedTipAvoidCrime = Component.translatable("app.crime_stats.tip_avoid_crime").getString();
        // Bail tab strings
        this.cachedBailTitle = Component.translatable("app.crime_stats.bail_title").getString();
        this.cachedNoBailNeeded = Component.translatable("app.crime_stats.no_bail_needed").getString();
        this.cachedInnocent = Component.translatable("app.crime_stats.innocent").getString();
        this.cachedWantedLevelLabel = Component.translatable("app.crime_stats.wanted_level_label").getString();
        this.cachedStarsStr = Component.translatable("app.crime_stats.stars").getString();
        this.cachedCostPerStar = Component.translatable("app.crime_stats.cost_per_star").getString();
        this.cachedTotal = Component.translatable("app.crime_stats.total").getString();
        this.cachedHowToPay = Component.translatable("app.crime_stats.how_to_pay").getString();
        this.cachedGotoPolice = Component.translatable("app.crime_stats.goto_police").getString();
        this.cachedPayBailAction = Component.translatable("app.crime_stats.pay_bail_action").getString();
        this.cachedWantedCleared = Component.translatable("app.crime_stats.wanted_cleared").getString();
        this.cachedAlternative = Component.translatable("app.crime_stats.alternative").getString();
        this.cachedWaitDecay = Component.translatable("app.crime_stats.wait_decay").getString();
        // Info tab strings
        this.cachedWantedSystemTitle = Component.translatable("app.crime_stats.wanted_system_title").getString();
        this.cachedStarMeaning = Component.translatable("app.crime_stats.star_meaning").getString();
        this.cachedStarInfo = new String[]{
            Component.translatable("app.crime_stats.star_1").getString(),
            Component.translatable("app.crime_stats.star_2").getString(),
            Component.translatable("app.crime_stats.star_3").getString(),
            Component.translatable("app.crime_stats.star_4").getString(),
            Component.translatable("app.crime_stats.star_5").getString()
        };
        this.cachedPoliceBehavior = Component.translatable("app.crime_stats.police_behavior").getString();
        this.cachedPoliceInfo = new String[]{
            Component.translatable("app.crime_stats.police_1_2").getString(),
            Component.translatable("app.crime_stats.police_3_4").getString(),
            Component.translatable("app.crime_stats.police_5").getString()
        };
        this.cachedEscapeTitle = Component.translatable("app.crime_stats.escape_title").getString();
        this.cachedEscapeHide = Component.translatable("app.crime_stats.escape_hide").getString();
        this.cachedEscapeDistance = Component.translatable("app.crime_stats.escape_distance").getString();

        // Cache data
        refreshData();

        // Tab-Buttons
        String[] tabNames = getTabNames();
        for (int i = 0; i < tabNames.length; i++) {
            final int tabIndex = i;
            addRenderableWidget(Button.builder(
                Component.literal(tabNames[i]),
                button -> {
                    currentTab = tabIndex;
                    scrollOffset = 0;
                    refreshData();
                }
            ).bounds(leftPos + 5 + (i * TAB_WIDTH), topPos + 30, TAB_WIDTH - 2, TAB_HEIGHT).build());
        }

        // Zurück-Button
        addRenderableWidget(Button.builder(Component.translatable("gui.achievement_app.back"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(parentScreen);
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());

        // Refresh-Button
        addRenderableWidget(Button.builder(Component.translatable("app.crime_stats.refresh"), button -> {
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
        cachedBailCostFormatted = String.format("§c§l%.0f€", bailCost);

        // TODO: Check if being chased (würde Server-Packet benötigen)
        isBeingChased = wantedLevel > 0;

        // PERFORMANCE: Pre-compute wanted level display strings (only changes when wantedLevel changes)
        cachedStarDisplay = getStarDisplay(wantedLevel);
        cachedLevelText = getLevelText(wantedLevel);
        cachedLevelNumeric = Component.translatable("app.crime_stats.level_text", wantedLevel).getString();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Smartphone-Hintergrund
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF2A2A2A);

        // Header
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 28, 0xFF1A1A1A);
        guiGraphics.drawCenteredString(this.font, cachedHeaderStr, leftPos + WIDTH / 2, topPos + 10, 0xFFFFFF);

        // Tab-Hintergrund (aktiver Tab hervorheben)
        for (int i = 0; i < 3; i++) {
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

            guiGraphics.drawCenteredString(this.font, cachedWantedLevelTitle, leftPos + WIDTH / 2, y + 5, 0xFFFFFF);

            // Sterne visualisieren (pre-computed in refreshData)
            guiGraphics.drawCenteredString(this.font, cachedStarDisplay, leftPos + WIDTH / 2, y + 22, 0xFFFFFF);

            // Level-Text (pre-computed in refreshData)
            String levelColor = wantedLevel > 0 ? "§c" : "§a";
            guiGraphics.drawCenteredString(this.font, levelColor + "§l" + cachedLevelText, leftPos + WIDTH / 2, y + 40, 0xFFFFFF);

            // Numerischer Level (pre-computed in refreshData)
            guiGraphics.drawCenteredString(this.font, cachedLevelNumeric, leftPos + WIDTH / 2, y + 55, 0xAAAAAA);
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
                guiGraphics.drawString(this.font, cachedBeingChased, leftPos + 15, y + 3, 0xFF5555);
                guiGraphics.drawString(this.font, cachedPoliceSearching, leftPos + 15, y + 14, 0xFFFFFF);
            }
            y += 35;
            contentHeight += 35;
        } else {
            if (y >= startY - 25 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 20, 0x44228B22);
                guiGraphics.drawString(this.font, cachedNoWanted, leftPos + 15, y + 5, 0x55FF55);
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
            guiGraphics.drawString(this.font, cachedTipsTitle, leftPos + 15, y, 0xFFAA00);
        }
        y += 12;
        contentHeight += 12;

        if (wantedLevel > 0) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, cachedTipHide, leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, cachedTipPayBail, leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, cachedTipWait, leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;
        } else {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, cachedTipStayClean, leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, cachedTipAvoidCrime, leftPos + 15, y, 0xAAAAAA);
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
            guiGraphics.drawString(this.font, cachedBailTitle, leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        if (wantedLevel == 0) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawCenteredString(this.font, cachedNoBailNeeded, leftPos + WIDTH / 2, y + 20, 0x55FF55);
                guiGraphics.drawCenteredString(this.font, cachedInnocent, leftPos + WIDTH / 2, y + 35, 0xAAAAAA);
            }
            y += 60;
            contentHeight += 60;
        } else {
            // Kautions-Box
            if (y >= startY - 60 && y < endY) {
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 55, 0x44AA0000);

                guiGraphics.drawString(this.font, cachedWantedLevelLabel, leftPos + 15, y + 5, 0xFFFFFF);
                guiGraphics.drawString(this.font, "§c" + wantedLevel + cachedStarsStr, leftPos + 120, y + 5, 0xFF5555);

                guiGraphics.drawString(this.font, cachedCostPerStar, leftPos + 15, y + 18, 0xFFFFFF);
                guiGraphics.drawString(this.font, CACHED_BAIL_COST_PER_STAR, leftPos + 120, y + 18, 0xFFAA00);

                guiGraphics.fill(leftPos + 15, y + 30, leftPos + WIDTH - 15, y + 31, 0x44FFFFFF);

                guiGraphics.drawString(this.font, cachedTotal, leftPos + 15, y + 35, 0xFFFFFF);
                guiGraphics.drawString(this.font, cachedBailCostFormatted, leftPos + 100, y + 35, 0xFF5555);
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
                guiGraphics.drawString(this.font, cachedHowToPay, leftPos + 15, y, 0x888888);
            }
            y += 12;
            contentHeight += 12;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, cachedGotoPolice, leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, cachedPayBailAction, leftPos + 15, y, 0xAAAAAA);
            }
            y += 11;
            contentHeight += 11;

            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, cachedWantedCleared, leftPos + 15, y, 0xAAAAAA);
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
            guiGraphics.drawString(this.font, cachedAlternative, leftPos + 15, y, 0x888888);
        }
        y += 12;
        contentHeight += 12;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, cachedWaitDecay, leftPos + 15, y, 0xAAAAAA);
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
            guiGraphics.drawString(this.font, cachedWantedSystemTitle, leftPos + 15, y, 0xFFAA00);
        }
        y += 15;
        contentHeight += 15;

        // Erklärung
        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, cachedStarMeaning, leftPos + 15, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        for (String starInfo : cachedStarInfo) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, starInfo, leftPos + 15, y, 0xFFFFFF);
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
            guiGraphics.drawString(this.font, cachedPoliceBehavior, leftPos + 15, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        for (String policeInfo : cachedPoliceInfo) {
            if (y >= startY - 10 && y < endY) {
                guiGraphics.drawString(this.font, policeInfo, leftPos + 15, y, 0xAAAAAA);
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
            guiGraphics.drawString(this.font, cachedEscapeTitle, leftPos + 15, y, 0xFFFFFF);
        }
        y += 12;
        contentHeight += 12;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, cachedEscapeHide, leftPos + 15, y, 0xAAAAAA);
        }
        y += 11;
        contentHeight += 11;

        if (y >= startY - 10 && y < endY) {
            guiGraphics.drawString(this.font, cachedEscapeDistance, leftPos + 15, y, 0xAAAAAA);
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
            case 0 -> Component.translatable("gui.app.crime.clean").getString();
            case 1 -> Component.translatable("gui.app.crime.suspicious").getString();
            case 2 -> Component.translatable("gui.app.crime.wanted").getString();
            case 3 -> Component.translatable("gui.app.crime.dangerous").getString();
            case 4 -> Component.translatable("gui.app.crime.very_dangerous").getString();
            case 5 -> Component.translatable("gui.app.crime.hardcore_criminal").getString();
            default -> Component.translatable("gui.app.crime.unknown").getString();
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
