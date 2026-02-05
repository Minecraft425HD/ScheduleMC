package de.rolandsw.schedulemc.client.screen.apps;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.level.LevelRequirements;
import de.rolandsw.schedulemc.level.UnlockCategory;
import de.rolandsw.schedulemc.level.client.ClientProducerLevelCache;
import de.rolandsw.schedulemc.level.network.ProducerLevelNetworkHandler;
import de.rolandsw.schedulemc.level.network.RequestProducerLevelDataPacket;
import de.rolandsw.schedulemc.level.network.UnlockableData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import java.util.List;

/**
 * ProducerLevel-App - Zeigt Level-Fortschritt und Freischaltungen.
 *
 * 3 Ansichten:
 * - OVERVIEW: Aktuelles Level, XP-Fortschritt, Kategorien-Grid
 * - CATEGORY: Liste der Unlockables einer Kategorie (sortiert nach Level)
 * - DETAIL: Detail-Ansicht eines einzelnen Unlockable
 */
@OnlyIn(Dist.CLIENT)
public class ProducerLevelAppScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Screen parentScreen;
    private static final int WIDTH = 240;
    private static final int HEIGHT = 260;
    private static final int MARGIN_TOP = 5;
    private static final int SCROLL_SPEED = 15;

    // Views
    private enum ViewMode { OVERVIEW, CATEGORY, DETAIL }
    private ViewMode currentView = ViewMode.OVERVIEW;
    private UnlockCategory selectedCategory = null;
    private UnlockableData selectedUnlockable = null;

    // Scrolling
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private int leftPos;
    private int topPos;

    // Cached category data for CATEGORY view
    private List<UnlockableData> currentCategoryUnlockables = null;

    // PERFORMANCE: Cache static translatable strings
    private String cachedTitleStr;
    private String cachedLevelStr;
    private String cachedProgressStr;
    private String cachedCategoriesStr;
    private String cachedStatsStr;
    private String cachedUnlockedStr;
    private String cachedLockedStr;
    private String cachedRequiredLevelStr;
    private String cachedCategoryStr;
    private String cachedNoUnlockablesStr;
    private String cachedItemsSoldStr;
    private String cachedRevenueStr;

    public ProducerLevelAppScreen(Screen parent) {
        super(Component.translatable("gui.app.level.title"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = MARGIN_TOP;

        // Register listener for cache updates
        ClientProducerLevelCache.setUpdateListener(this::onCacheUpdated);

        // Request data from server
        refreshData();

        // Cache translatable strings
        cachedTitleStr = Component.translatable("gui.app.level.title").getString();
        cachedLevelStr = Component.translatable("gui.app.level.level").getString();
        cachedProgressStr = Component.translatable("gui.app.level.progress").getString();
        cachedCategoriesStr = Component.translatable("gui.app.level.categories").getString();
        cachedStatsStr = Component.translatable("gui.app.level.stats").getString();
        cachedUnlockedStr = Component.translatable("gui.app.level.unlocked").getString();
        cachedLockedStr = Component.translatable("gui.app.level.locked").getString();
        cachedRequiredLevelStr = Component.translatable("gui.app.level.required_level").getString();
        cachedCategoryStr = Component.translatable("gui.app.level.category").getString();
        cachedNoUnlockablesStr = Component.translatable("gui.app.level.no_unlockables").getString();
        cachedItemsSoldStr = Component.translatable("gui.app.level.items_sold").getString();
        cachedRevenueStr = Component.translatable("gui.app.level.revenue").getString();

        initButtons();
    }

    @Override
    public void removed() {
        super.removed();
        ClientProducerLevelCache.removeUpdateListener();
    }

    private void onCacheUpdated() {
        if (selectedCategory != null && currentView == ViewMode.CATEGORY) {
            loadCategoryUnlockables();
        }
    }

    private void initButtons() {
        clearWidgets();

        // Zurueck-Button
        Component backLabel = currentView == ViewMode.OVERVIEW
                ? Component.translatable("gui.app.level.back")
                : Component.translatable("gui.app.level.back_to_overview");
        addRenderableWidget(Button.builder(backLabel, button -> {
            if (currentView == ViewMode.OVERVIEW) {
                if (minecraft != null) {
                    minecraft.setScreen(parentScreen);
                }
            } else if (currentView == ViewMode.DETAIL) {
                currentView = ViewMode.CATEGORY;
                selectedUnlockable = null;
                scrollOffset = 0;
                initButtons();
            } else {
                currentView = ViewMode.OVERVIEW;
                selectedCategory = null;
                currentCategoryUnlockables = null;
                scrollOffset = 0;
                initButtons();
            }
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());

        // Refresh-Button
        addRenderableWidget(Button.builder(Component.literal("\u21BB"), button -> {
            refreshData();
        }).bounds(leftPos + WIDTH - 30, topPos + HEIGHT - 30, 20, 20).build());
    }

    private void refreshData() {
        ProducerLevelNetworkHandler.sendToServer(new RequestProducerLevelDataPacket());

        if (selectedCategory != null) {
            loadCategoryUnlockables();
        }
    }

    private void loadCategoryUnlockables() {
        if (!ClientProducerLevelCache.isInitialized() || selectedCategory == null) {
            currentCategoryUnlockables = null;
            return;
        }
        currentCategoryUnlockables = ClientProducerLevelCache.getUnlockablesByCategory(selectedCategory);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Smartphone-Hintergrund
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF2A2A2A);

        // Header
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 28, 0xFF1A1A1A);
        guiGraphics.drawCenteredString(this.font, cachedTitleStr, leftPos + WIDTH / 2, topPos + 10, 0xFFFFFF);

        int contentY = topPos + 32;
        int contentEndY = topPos + HEIGHT - 40;

        switch (currentView) {
            case OVERVIEW -> renderOverviewView(guiGraphics, contentY, contentEndY);
            case CATEGORY -> renderCategoryView(guiGraphics, contentY, contentEndY, mouseX, mouseY);
            case DETAIL -> renderDetailView(guiGraphics, contentY, contentEndY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // ═══════════════════════════════════════════════════════════
    // OVERVIEW VIEW
    // ═══════════════════════════════════════════════════════════

    /** Feste Hoehe des Level-Info-Headers in der Overview */
    private static final int OVERVIEW_HEADER_HEIGHT = 90;
    private static final int CATEGORY_ROW_HEIGHT = 30;

    private void renderOverviewView(GuiGraphics guiGraphics, int startY, int endY) {
        int level = ClientProducerLevelCache.getCurrentLevel();
        double prog = ClientProducerLevelCache.getProgress();
        int xpToNext = ClientProducerLevelCache.getXpToNextLevel();
        int totalXP = ClientProducerLevelCache.getTotalXP();
        int unlocked = ClientProducerLevelCache.getTotalUnlocked();
        int total = ClientProducerLevelCache.getTotalUnlockables();

        // ── Fester Header: Level-Info ──
        guiGraphics.fill(leftPos + 10, startY, leftPos + WIDTH - 10, startY + 75, 0x44228B22);

        String levelText = "\u00A76\u00A7lLevel " + level + " \u00A77/ " + LevelRequirements.MAX_LEVEL;
        guiGraphics.drawCenteredString(this.font, levelText, leftPos + WIDTH / 2, startY + 5, 0xFFFFFF);

        // XP-Fortschrittsbalken
        int barX = leftPos + 20;
        int barY = startY + 20;
        int barWidth = WIDTH - 40;
        int barHeight = 12;
        int filledWidth = (int) (barWidth * prog);

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        if (filledWidth > 0) {
            int barColor = level >= 25 ? 0xFFFFAA00 : (level >= 15 ? 0xFF00AAFF : 0xFF00AA00);
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + barHeight, barColor);
        }

        String progressPercent = String.format("%.0f%%", prog * 100);
        guiGraphics.drawCenteredString(this.font, progressPercent, leftPos + WIDTH / 2, barY + 2, 0xFFFFFF);

        if (level < LevelRequirements.MAX_LEVEL) {
            String xpText = "\u00A7eXP: \u00A7f" + totalXP + " \u00A77| \u00A7e" + xpToNext + " \u00A77bis Lv." + (level + 1);
            guiGraphics.drawCenteredString(this.font, xpText, leftPos + WIDTH / 2, startY + 37, 0xFFFFFF);
        } else {
            guiGraphics.drawCenteredString(this.font, "\u00A76\u00A7lMAX LEVEL!",
                    leftPos + WIDTH / 2, startY + 37, 0xFFAA00);
        }

        double unlockPercent = total > 0 ? (double) unlocked / total * 100.0 : 0;
        String unlockText = "\u00A7a" + unlocked + "\u00A77/\u00A7a" + total +
                " \u00A77Freigeschaltet (" + String.format("%.0f%%", unlockPercent) + ")";
        guiGraphics.drawCenteredString(this.font, unlockText, leftPos + WIDTH / 2, startY + 52, 0xFFFFFF);

        int soldTotal = ClientProducerLevelCache.getTotalItemsSold();
        double revenue = ClientProducerLevelCache.getTotalRevenue();
        String statsText = "\u00A77" + cachedItemsSoldStr + ": \u00A7f" + soldTotal +
                " \u00A77| " + cachedRevenueStr + ": \u00A7a" + String.format("%.0f\u20AC", revenue);
        guiGraphics.drawCenteredString(this.font, statsText, leftPos + WIDTH / 2, startY + 62, 0xFFFFFF);

        // ── Scrollbare Kategorien-Liste (1 pro Zeile) ──
        int listStartY = startY + OVERVIEW_HEADER_HEIGHT;
        guiGraphics.drawString(this.font, cachedCategoriesStr, leftPos + 15, listStartY - 12, 0xFFAA00);

        guiGraphics.enableScissor(leftPos + 5, listStartY, leftPos + WIDTH - 5, endY);

        UnlockCategory[] categories = UnlockCategory.values();
        int contentHeight = categories.length * CATEGORY_ROW_HEIGHT;
        int y = listStartY - scrollOffset;

        for (UnlockCategory category : categories) {
            if (y >= listStartY - CATEGORY_ROW_HEIGHT && y < endY + CATEGORY_ROW_HEIGHT) {
                int catUnlocked = ClientProducerLevelCache.getUnlockedCountForCategory(category);
                int catTotal = ClientProducerLevelCache.getTotalCountForCategory(category);
                boolean allDone = catTotal > 0 && catUnlocked >= catTotal;

                // Hintergrund
                int boxColor = allDone ? 0x4400AA00 : 0x44333333;
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + CATEGORY_ROW_HEIGHT - 3, boxColor);

                // Kategorie-Name mit Farbe
                String catName = category.getColorCode() + category.getDisplayName();
                guiGraphics.drawString(this.font, catName, leftPos + 15, y + 4, 0xFFFFFF);

                // Zaehler rechts
                String countText = "\u00A7a" + catUnlocked + "\u00A77/\u00A7f" + catTotal;
                int countWidth = this.font.width(countText);
                guiGraphics.drawString(this.font, countText, leftPos + WIDTH - 18 - countWidth, y + 4, 0xFFFFFF);

                // Beschreibung
                guiGraphics.drawString(this.font, "\u00A78" + category.getDescription(),
                        leftPos + 15, y + 16, 0x888888);
            }
            y += CATEGORY_ROW_HEIGHT;
        }

        guiGraphics.disableScissor();

        // Scrollbar
        int visibleHeight = endY - listStartY;
        maxScroll = Math.max(0, contentHeight - visibleHeight);
        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(20, visibleHeight * visibleHeight / (visibleHeight + maxScroll));
            int scrollBarY = listStartY + (scrollOffset * (visibleHeight - scrollBarHeight) / maxScroll);
            guiGraphics.fill(leftPos + WIDTH - 8, listStartY, leftPos + WIDTH - 5, endY, 0x44FFFFFF);
            guiGraphics.fill(leftPos + WIDTH - 8, scrollBarY, leftPos + WIDTH - 5,
                    scrollBarY + scrollBarHeight, 0xAAFFFFFF);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // CATEGORY VIEW
    // ═══════════════════════════════════════════════════════════

    private void renderCategoryView(GuiGraphics guiGraphics, int startY, int endY,
                                     int mouseX, int mouseY) {
        if (selectedCategory == null) return;

        // Kategorie-Header
        guiGraphics.fill(leftPos + 10, startY, leftPos + WIDTH - 10, startY + 25, 0x44FFAA00);
        String catHeader = selectedCategory.getColorCode() + selectedCategory.getDisplayName() +
                " \u00A77- " + selectedCategory.getDescription();
        guiGraphics.drawCenteredString(this.font, catHeader, leftPos + WIDTH / 2, startY + 8, 0xFFFFFF);

        int listStartY = startY + 30;

        if (currentCategoryUnlockables == null || currentCategoryUnlockables.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, cachedNoUnlockablesStr,
                    leftPos + WIDTH / 2, listStartY + 20, 0xAAAAAA);
            return;
        }

        // Scissor fuer scrollbaren Bereich
        guiGraphics.enableScissor(leftPos + 5, listStartY, leftPos + WIDTH - 5, endY);

        int y = listStartY - scrollOffset;
        int contentHeight = 0;
        int itemHeight = 35;

        for (UnlockableData unlock : currentCategoryUnlockables) {
            boolean isUnlocked = unlock.isUnlocked();
            int currentLevel = ClientProducerLevelCache.getCurrentLevel();
            int reqLevel = unlock.getRequiredLevel();

            if (y >= listStartY - itemHeight && y < endY + itemHeight) {
                // Hintergrund
                int boxColor;
                if (isUnlocked) {
                    boxColor = 0x4400AA00; // Gruen
                } else if (reqLevel <= currentLevel + 2) {
                    boxColor = 0x44FFAA00; // Fast freigeschaltet - orange
                } else {
                    boxColor = 0x44333333; // Gesperrt - grau
                }
                guiGraphics.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + itemHeight - 3, boxColor);

                // Status-Icon + Beschreibung
                String status = isUnlocked ? "\u00A7a\u2713" : "\u00A77\u25CB";
                String nameColor = isUnlocked ? "\u00A7f" : "\u00A78";
                guiGraphics.drawString(this.font, status + " " + nameColor + unlock.getDescription(),
                        leftPos + 15, y + 4, 0xFFFFFF);

                // Level-Anforderung
                String levelColor = isUnlocked ? "\u00A7a" : (reqLevel <= currentLevel + 2 ? "\u00A7e" : "\u00A7c");
                String levelText = levelColor + "Level " + reqLevel;
                guiGraphics.drawString(this.font, levelText, leftPos + 15, y + 18, 0xAAAAAA);

                // Status-Text rechts
                if (isUnlocked) {
                    guiGraphics.drawString(this.font, "\u00A7a\u2713",
                            leftPos + WIDTH - 25, y + 10, 0x55FF55);
                } else if (reqLevel <= currentLevel + 2) {
                    guiGraphics.drawString(this.font, "\u00A76\u26A1",
                            leftPos + WIDTH - 25, y + 10, 0xFFAA00);
                }
            }

            y += itemHeight;
            contentHeight += itemHeight;
        }

        guiGraphics.disableScissor();

        // Scrollbar
        int visibleHeight = endY - listStartY;
        maxScroll = Math.max(0, contentHeight - visibleHeight);
        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(20, visibleHeight * visibleHeight / (visibleHeight + maxScroll));
            int scrollBarY = listStartY + (scrollOffset * (visibleHeight - scrollBarHeight) / maxScroll);
            guiGraphics.fill(leftPos + WIDTH - 8, listStartY, leftPos + WIDTH - 5, endY, 0x44FFFFFF);
            guiGraphics.fill(leftPos + WIDTH - 8, scrollBarY, leftPos + WIDTH - 5,
                    scrollBarY + scrollBarHeight, 0xAAFFFFFF);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DETAIL VIEW
    // ═══════════════════════════════════════════════════════════

    private void renderDetailView(GuiGraphics guiGraphics, int startY, int endY) {
        if (selectedUnlockable == null) return;

        UnlockableData unlock = selectedUnlockable;
        boolean isUnlocked = unlock.isUnlocked();
        int reqLevel = unlock.getRequiredLevel();
        int currentLevel = ClientProducerLevelCache.getCurrentLevel();

        // Titel
        String nameColor = isUnlocked ? "\u00A7a" : "\u00A7c";
        guiGraphics.drawCenteredString(this.font, nameColor + unlock.getDescription(),
                leftPos + WIDTH / 2, startY + 10, 0xFFFFFF);

        // Kategorie
        UnlockCategory cat = unlock.getCategory();
        guiGraphics.drawCenteredString(this.font,
                "\u00A77" + cachedCategoryStr + ": " + cat.getColorCode() + cat.getDisplayName(),
                leftPos + WIDTH / 2, startY + 28, 0xAAAAAA);

        // Trennlinie
        guiGraphics.fill(leftPos + 30, startY + 42, leftPos + WIDTH - 30, startY + 43, 0x44FFFFFF);

        // Level-Anforderung Box
        int boxY = startY + 50;
        int boxColor = isUnlocked ? 0x4400AA00 : 0x44AA0000;
        guiGraphics.fill(leftPos + 20, boxY, leftPos + WIDTH - 20, boxY + 40, boxColor);

        guiGraphics.drawCenteredString(this.font,
                cachedRequiredLevelStr + ": \u00A7f" + reqLevel,
                leftPos + WIDTH / 2, boxY + 5, 0xFFFFFF);

        // Fortschrittsbalken zum benoetigten Level
        if (!isUnlocked) {
            double levelProgress = currentLevel >= reqLevel ? 1.0 :
                    (double) currentLevel / reqLevel;
            int barX = leftPos + 30;
            int barY = boxY + 20;
            int barWidth = WIDTH - 60;
            int barHeight = 10;
            int filledWidth = (int) (barWidth * levelProgress);

            guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
            int barColor = levelProgress >= 0.8 ? 0xFFFFAA00 : 0xFF4A90E2;
            if (filledWidth > 0) {
                guiGraphics.fill(barX, barY, barX + filledWidth, barY + barHeight, barColor);
            }

            String barText = "Level " + currentLevel + " / " + reqLevel;
            guiGraphics.drawCenteredString(this.font, "\u00A7e" + barText,
                    leftPos + WIDTH / 2, barY + 1, 0xFFFFFF);
        } else {
            guiGraphics.drawCenteredString(this.font, cachedUnlockedStr,
                    leftPos + WIDTH / 2, boxY + 22, 0x55FF55);
        }

        // Status
        int statusY = boxY + 50;
        if (isUnlocked) {
            guiGraphics.drawCenteredString(this.font, "\u00A7a\u00A7l\u2713 " + cachedUnlockedStr,
                    leftPos + WIDTH / 2, statusY, 0x55FF55);
        } else {
            int levelsNeeded = reqLevel - currentLevel;
            String lockedText = "\u00A7c\u00A7l\u2718 " + cachedLockedStr;
            guiGraphics.drawCenteredString(this.font, lockedText,
                    leftPos + WIDTH / 2, statusY, 0xFF5555);
            guiGraphics.drawCenteredString(this.font,
                    "\u00A77Noch \u00A7e" + levelsNeeded + " Level\u00A77 benoetigt",
                    leftPos + WIDTH / 2, statusY + 15, 0xAAAAAA);
        }

        // Kategorie-Beschreibung
        guiGraphics.drawCenteredString(this.font,
                "\u00A77" + cat.getDescription(),
                leftPos + WIDTH / 2, statusY + 35, 0xAAAAAA);
    }

    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int contentY = topPos + 32;
        int contentEndY = topPos + HEIGHT - 40;

        // Klick auf Kategorie-Zeile in OVERVIEW
        if (currentView == ViewMode.OVERVIEW) {
            int listStartY = contentY + OVERVIEW_HEADER_HEIGHT;
            if (mouseX >= leftPos + 10 && mouseX <= leftPos + WIDTH - 10 &&
                    mouseY >= listStartY && mouseY < contentEndY) {
                int y = listStartY - scrollOffset;
                UnlockCategory[] categories = UnlockCategory.values();
                for (UnlockCategory category : categories) {
                    int boxTop = y;
                    int boxBottom = y + CATEGORY_ROW_HEIGHT - 3;
                    if (mouseY >= boxTop && mouseY <= boxBottom &&
                            boxTop >= listStartY && boxBottom <= contentEndY) {
                        selectedCategory = category;
                        currentView = ViewMode.CATEGORY;
                        scrollOffset = 0;
                        loadCategoryUnlockables();
                        initButtons();
                        return true;
                    }
                    y += CATEGORY_ROW_HEIGHT;
                }
            }
        }

        // Klick auf Unlockable-Zeile in CATEGORY
        if (currentView == ViewMode.CATEGORY && currentCategoryUnlockables != null) {
            int listStartY = contentY + 30;
            int y = listStartY - scrollOffset;
            int itemHeight = 35;

            for (UnlockableData unlock : currentCategoryUnlockables) {
                int boxTop = y;
                int boxBottom = y + itemHeight - 3;

                if (mouseX >= leftPos + 10 && mouseX <= leftPos + WIDTH - 10 &&
                        mouseY >= boxTop && mouseY <= boxBottom &&
                        boxTop >= listStartY && boxBottom <= contentEndY) {
                    selectedUnlockable = unlock;
                    currentView = ViewMode.DETAIL;
                    scrollOffset = 0;
                    initButtons();
                    return true;
                }
                y += itemHeight;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if ((currentView == ViewMode.OVERVIEW || currentView == ViewMode.CATEGORY) && maxScroll > 0) {
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
