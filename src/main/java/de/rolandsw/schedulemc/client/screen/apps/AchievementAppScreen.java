package de.rolandsw.schedulemc.client.screen.apps;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.achievement.*;
import de.rolandsw.schedulemc.achievement.client.ClientAchievementCache;
import de.rolandsw.schedulemc.achievement.network.AchievementData;
import de.rolandsw.schedulemc.achievement.network.AchievementNetworkHandler;
import de.rolandsw.schedulemc.achievement.network.RequestAchievementDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import java.util.*;

/**
 * Achievement-App - Vollständige Achievement-Verwaltung auf dem Smartphone
 *
 * Premium Features:
 * - Kategorie-Filter mit Icons
 * - Fortschrittsbalken pro Achievement
 * - Detail-Ansicht bei Klick
 * - Fast fertige Achievements hervorgehoben
 * - Statistiken und Gesamtfortschritt
 */
@OnlyIn(Dist.CLIENT)
public class AchievementAppScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Screen parentScreen;
    private static final int WIDTH = 240;
    private static final int HEIGHT = 260;
    private static final int BORDER_SIZE = 5;
    private static final int MARGIN_TOP = 15;
    private static final int MARGIN_BOTTOM = 60;

    // Views
    private enum ViewMode { OVERVIEW, CATEGORY, DETAIL }
    private ViewMode currentView = ViewMode.OVERVIEW;
    private AchievementCategory selectedCategory = null;

    // Scrolling
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int SCROLL_SPEED = 15;

    private int leftPos;
    private int topPos;

    // Cached Data
    private int totalAchievements = 0;
    private int unlockedAchievements = 0;
    private double totalEarned = 0.0;
    private List<AchievementData> currentAchievements = new ArrayList<>();
    private AchievementData selectedAchievementData = null;

    public AchievementAppScreen(Screen parent) {
        super(Component.literal("Achievements"));
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

        // Register listener for cache updates
        ClientAchievementCache.setUpdateListener(this::onCacheUpdated);

        // Cache data
        refreshData();

        // Buttons basierend auf aktuellem View
        initButtons();
    }

    @Override
    public void removed() {
        super.removed();
        // Clean up listener when screen is closed
        ClientAchievementCache.removeUpdateListener();
    }

    /**
     * Called when achievement cache is updated from server
     */
    private void onCacheUpdated() {
        LOGGER.info("AchievementAppScreen: Cache updated! Refreshing display (total: {}, unlocked: {})",
            ClientAchievementCache.getTotalAchievements(), ClientAchievementCache.getUnlockedCount());

        // Refresh displayed data
        totalAchievements = ClientAchievementCache.getTotalAchievements();
        unlockedAchievements = ClientAchievementCache.getUnlockedCount();
        totalEarned = ClientAchievementCache.getTotalEarned();

        if (selectedCategory != null) {
            loadCategoryAchievements();
            LOGGER.info("AchievementAppScreen: Loaded {} achievements for category {}",
                currentAchievements.size(), selectedCategory.name());
        }
    }

    private void initButtons() {
        clearWidgets();

        // Zurück-Button (immer sichtbar)
        String backLabel = currentView == ViewMode.OVERVIEW ? "← Zurück" : "← Übersicht";
        addRenderableWidget(Button.builder(Component.literal(backLabel), button -> {
            if (currentView == ViewMode.OVERVIEW) {
                if (minecraft != null) {
                    minecraft.setScreen(parentScreen);
                }
            } else if (currentView == ViewMode.DETAIL) {
                currentView = ViewMode.CATEGORY;
                selectedAchievementData = null;
                scrollOffset = 0;
                initButtons();
            } else {
                currentView = ViewMode.OVERVIEW;
                selectedCategory = null;
                scrollOffset = 0;
                initButtons();
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());

        // Refresh-Button
        addRenderableWidget(Button.builder(Component.literal("↻"), button -> {
            refreshData();
        }).bounds(leftPos + WIDTH - 30, topPos + HEIGHT - 30, 20, 20).build());

        // Kategorie-Buttons nur in Overview
        if (currentView == ViewMode.OVERVIEW) {
            int btnY = topPos + 120;
            int btnWidth = 105;
            int btnHeight = 28;
            int col = 0;
            int row = 0;

            for (AchievementCategory category : AchievementCategory.values()) {
                final AchievementCategory cat = category;
                int x = leftPos + 15 + (col * (btnWidth + 5));
                int y = btnY + (row * (btnHeight + 5));

                addRenderableWidget(Button.builder(
                    Component.literal(category.getEmoji() + " " + category.getDisplayName()),
                    button -> {
                        selectedCategory = cat;
                        currentView = ViewMode.CATEGORY;
                        scrollOffset = 0;
                        loadCategoryAchievements();
                        initButtons();
                    }
                ).bounds(x, y, btnWidth, btnHeight).build());

                col++;
                if (col >= 2) {
                    col = 0;
                    row++;
                }
            }
        }
    }

    private void refreshData() {
        LOGGER.info("AchievementAppScreen: Requesting achievement data from server");
        // Request fresh data from server
        AchievementNetworkHandler.sendToServer(new RequestAchievementDataPacket());

        // Load from client cache (will be updated when server responds)
        if (ClientAchievementCache.isInitialized()) {
            LOGGER.info("AchievementAppScreen: Cache already initialized, loading data");
            totalAchievements = ClientAchievementCache.getTotalAchievements();
            unlockedAchievements = ClientAchievementCache.getUnlockedCount();
            totalEarned = ClientAchievementCache.getTotalEarned();

            if (selectedCategory != null) {
                loadCategoryAchievements();
            }
        } else {
            LOGGER.info("AchievementAppScreen: Cache not yet initialized, waiting for server response");
        }
    }

    private void loadCategoryAchievements() {
        if (!ClientAchievementCache.isInitialized()) {
            currentAchievements.clear();
            return;
        }

        currentAchievements = ClientAchievementCache.getAchievementsByCategory(selectedCategory);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Smartphone-Hintergrund
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF2A2A2A);

        // Header
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 28, 0xFF1A1A1A);
        guiGraphics.drawCenteredString(this.font, "§e§l🏆 Achievements", leftPos + WIDTH / 2, topPos + 10, 0xFFFFFF);

        // Content basierend auf View
        int contentY = topPos + 32;
        int contentEndY = topPos + HEIGHT - 40;

        switch (currentView) {
            case OVERVIEW -> renderOverviewView(guiGraphics, contentY, contentEndY);
            case CATEGORY -> renderCategoryView(guiGraphics, contentY, contentEndY);
            case DETAIL -> renderDetailView(guiGraphics, contentY, contentEndY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // ═══════════════════════════════════════════════════════════
    // OVERVIEW VIEW
    // ═══════════════════════════════════════════════════════════

    private void renderOverviewView(GuiGraphics guiGraphics, int startY, int endY) {
        // Gesamt-Fortschritt Box
        guiGraphics.fill(leftPos + 10, startY, leftPos + WIDTH - 10, startY + 55, 0x44228B22);

        // Fortschritts-Prozent
        double percentage = totalAchievements > 0 ? (double) unlockedAchievements / totalAchievements * 100.0 : 0;
        guiGraphics.drawCenteredString(this.font, "§f§lGesamt-Fortschritt", leftPos + WIDTH / 2, startY + 5, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
            "§e" + unlockedAchievements + "§7/§e" + totalAchievements + " §7(" + String.format("%.1f%%", percentage) + ")",
            leftPos + WIDTH / 2, startY + 18, 0xFFFFFF);

        // Fortschrittsbalken
        int barX = leftPos + 20;
        int barY = startY + 32;
        int barWidth = WIDTH - 40;
        int barHeight = 8;
        int filledWidth = (int) (barWidth * percentage / 100.0);

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        if (filledWidth > 0) {
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + barHeight, 0xFF00AA00);
        }

        // Verdientes Geld
        guiGraphics.drawCenteredString(this.font,
            "§7Verdient: §a" + String.format("%.2f€", totalEarned),
            leftPos + WIDTH / 2, startY + 44, 0xFFFFFF);

        // Kategorien-Header
        guiGraphics.drawString(this.font, "§6Kategorien:", leftPos + 15, startY + 80, 0xFFAA00);

        // Kategorie-Buttons werden in initButtons() erstellt
    }

    // ═══════════════════════════════════════════════════════════
    // CATEGORY VIEW
    // ═══════════════════════════════════════════════════════════

    private void renderCategoryView(GuiGraphics guiGraphics, int startY, int endY) {
        if (selectedCategory == null) return;

        // Kategorie-Header
        guiGraphics.fill(leftPos + 10, startY, leftPos + WIDTH - 10, startY + 25, 0x44FFAA00);
        guiGraphics.drawCenteredString(this.font,
            selectedCategory.getFormattedName(),
            leftPos + WIDTH / 2, startY + 8, 0xFFFFFF);

        int listStartY = startY + 30;
        int y = listStartY - scrollOffset;
        int contentHeight = 0;

        // Achievement-Liste (sortiert nach Tier)
        for (AchievementData ach : currentAchievements) {
            boolean unlocked = ach.isUnlocked();
            double progress = ach.getProgress();
            double progressPercent = Math.min(100, progress / ach.getRequirement() * 100.0);

            if (y >= listStartY - 50 && y < endY) {
                // Achievement Box
                int boxColor = unlocked ? 0x4400AA00 : (progressPercent >= 70 ? 0x44FFAA00 : 0x44333333);
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 45, boxColor);

                // Status Icon & Name
                String status = unlocked ? "§a✓" : "§7○";
                String displayName = ach.isHidden() && !unlocked ? "§8???" : ach.getName();
                guiGraphics.drawString(this.font, status + " " + ach.getTier().getColorCode() + displayName,
                    leftPos + 15, y + 3, 0xFFFFFF);

                // Tier Badge
                guiGraphics.drawString(this.font, ach.getTier().getEmoji(),
                    leftPos + WIDTH - 25, y + 3, 0xFFFFFF);

                // Progress Bar
                int barX = leftPos + 15;
                int barY = y + 18;
                int barWidth = WIDTH - 40;
                int barHeight = 6;
                int filledWidth = (int) (barWidth * progressPercent / 100.0);

                guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
                int barColor = unlocked ? 0xFF00AA00 : (progressPercent >= 70 ? 0xFFFFAA00 : 0xFF4A90E2);
                if (filledWidth > 0) {
                    guiGraphics.fill(barX, barY, barX + filledWidth, barY + barHeight, barColor);
                }

                // Progress Text
                String progressText = ach.isHidden() && !unlocked ? "§8???/???" :
                    String.format("§e%.0f§7/§e%.0f", progress, ach.getRequirement());
                guiGraphics.drawString(this.font, progressText, leftPos + 15, y + 28, 0xAAAAAA);

                // Reward (wenn nicht freigeschaltet)
                if (!unlocked) {
                    String rewardString = String.format("§a+%.2f€", ach.getTier().getRewardMoney());
                    guiGraphics.drawString(this.font, rewardString,
                        leftPos + WIDTH - 60, y + 28, 0x55FF55);
                }

                // Fast fertig Marker
                if (!unlocked && progressPercent >= 70) {
                    guiGraphics.drawString(this.font, "§6⚡", leftPos + WIDTH - 25, y + 28, 0xFFAA00);
                }
            }
            y += 50;
            contentHeight += 50;
        }

        // Empty State
        if (currentAchievements.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, "§7Keine Achievements in dieser Kategorie",
                leftPos + WIDTH / 2, listStartY + 20, 0xAAAAAA);
        }

        // Scroll
        int visibleHeight = endY - listStartY;
        maxScroll = Math.max(0, contentHeight - visibleHeight);

        // Scroll-Indikator
        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(20, visibleHeight * visibleHeight / (visibleHeight + maxScroll));
            int scrollBarY = listStartY + (scrollOffset * (visibleHeight - scrollBarHeight) / maxScroll);
            guiGraphics.fill(leftPos + WIDTH - 8, listStartY, leftPos + WIDTH - 5, endY, 0x44FFFFFF);
            guiGraphics.fill(leftPos + WIDTH - 8, scrollBarY, leftPos + WIDTH - 5, scrollBarY + scrollBarHeight, 0xAAFFFFFF);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DETAIL VIEW
    // ═══════════════════════════════════════════════════════════

    private void renderDetailView(GuiGraphics guiGraphics, int startY, int endY) {
        if (selectedAchievementData == null) return;

        AchievementData ach = selectedAchievementData;
        boolean unlocked = ach.isUnlocked();
        double progress = ach.getProgress();
        double progressPercent = Math.min(100, progress / ach.getRequirement() * 100.0);

        // Achievement Name
        String formattedName = ach.getTier().getColorCode() + ach.getTier().getEmoji() + " §f" + ach.getName();
        guiGraphics.drawCenteredString(this.font, formattedName,
            leftPos + WIDTH / 2, startY + 10, 0xFFFFFF);

        // Category
        guiGraphics.drawCenteredString(this.font, "§7" + ach.getCategory().getFormattedName(),
            leftPos + WIDTH / 2, startY + 25, 0xAAAAAA);

        // Description
        guiGraphics.drawCenteredString(this.font, "§7" + ach.getDescription(),
            leftPos + WIDTH / 2, startY + 45, 0xAAAAAA);

        // Big Progress Circle (simplified as bar)
        int barX = leftPos + 30;
        int barY = startY + 70;
        int barWidth = WIDTH - 60;
        int barHeight = 15;
        int filledWidth = (int) (barWidth * progressPercent / 100.0);

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        int barColor = unlocked ? 0xFF00AA00 : 0xFF4A90E2;
        if (filledWidth > 0) {
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + barHeight, barColor);
        }

        // Progress Percentage
        guiGraphics.drawCenteredString(this.font,
            String.format("§e%.1f%%", progressPercent),
            leftPos + WIDTH / 2, barY + 3, 0xFFFFFF);

        // Progress Numbers
        guiGraphics.drawCenteredString(this.font,
            String.format("§e%.0f §7/ §e%.0f", progress, ach.getRequirement()),
            leftPos + WIDTH / 2, startY + 95, 0xFFFFFF);

        // Status
        String statusText = unlocked ? "§a§l✓ FREIGESCHALTET" : "§7○ In Arbeit";
        guiGraphics.drawCenteredString(this.font, statusText,
            leftPos + WIDTH / 2, startY + 115, 0xFFFFFF);

        // Reward
        guiGraphics.drawCenteredString(this.font, "§7Belohnung:",
            leftPos + WIDTH / 2, startY + 140, 0xAAAAAA);
        String rewardString = String.format("§a+%.2f€", ach.getTier().getRewardMoney());
        guiGraphics.drawCenteredString(this.font, rewardString,
            leftPos + WIDTH / 2, startY + 155, 0x55FF55);

        // Tier Info
        guiGraphics.drawCenteredString(this.font,
            "§7Schwierigkeit: " + ach.getTier().getFormattedName(),
            leftPos + WIDTH / 2, startY + 180, 0xAAAAAA);
    }

    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && currentView == ViewMode.CATEGORY) {
            // Check if clicked on an achievement
            int listStartY = topPos + 32 + 30;
            int y = listStartY - scrollOffset;

            for (AchievementData ach : currentAchievements) {
                int boxTop = y;
                int boxBottom = y + 45;

                if (mouseX >= leftPos + 10 && mouseX <= leftPos + WIDTH - 10 &&
                    mouseY >= boxTop && mouseY <= boxBottom &&
                    boxTop >= listStartY - 50 && boxBottom <= topPos + HEIGHT - 40) {

                    selectedAchievementData = ach;
                    currentView = ViewMode.DETAIL;
                    scrollOffset = 0;
                    initButtons();
                    return true;
                }
                y += 50;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (currentView == ViewMode.CATEGORY && maxScroll > 0) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - delta * SCROLL_SPEED));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 69) { // GLFW_KEY_E
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
