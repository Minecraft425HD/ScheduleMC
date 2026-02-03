package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.gang.GangLevelRequirements;
import de.rolandsw.schedulemc.gang.GangPerk;
import de.rolandsw.schedulemc.gang.GangReputation;
import de.rolandsw.schedulemc.gang.client.ClientGangCache;
import de.rolandsw.schedulemc.gang.network.GangNetworkHandler;
import de.rolandsw.schedulemc.gang.network.RequestGangDataPacket;
import de.rolandsw.schedulemc.gang.network.SyncGangDataPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Gang-App fuer das Smartphone.
 *
 * 4 Ansichten:
 * - NO_GANG: Kein Mitglied (Hinweis auf /gang create)
 * - OVERVIEW: Gang-Info + scrollbare Mitgliederliste
 * - PERKS: Perk-Tree nach Zweigen, scrollbar
 * - MISSIONS: Aktive Missionen mit Fortschrittsbalken
 */
@OnlyIn(Dist.CLIENT)
public class GangAppScreen extends Screen {

    private final Screen parentScreen;
    private static final int WIDTH = 240;
    private static final int HEIGHT = 260;
    private static final int MARGIN_TOP = 5;
    private static final int SCROLL_SPEED = 15;

    private enum ViewMode { NO_GANG, OVERVIEW, PERKS, MISSIONS }
    private ViewMode currentView = ViewMode.NO_GANG;

    private int scrollOffset = 0;
    private int maxScroll = 0;

    private int leftPos;
    private int topPos;

    public GangAppScreen(Screen parent) {
        super(Component.translatable("gui.app.gang.title"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = MARGIN_TOP;

        ClientGangCache.setUpdateListener(this::onCacheUpdated);

        // Daten vom Server anfordern
        GangNetworkHandler.sendToServer(new RequestGangDataPacket());

        updateViewMode();
        initButtons();
    }

    @Override
    public void removed() {
        super.removed();
        ClientGangCache.removeUpdateListener();
    }

    private void onCacheUpdated() {
        updateViewMode();
        initButtons();
    }

    private void updateViewMode() {
        if (!ClientGangCache.hasGang()) {
            currentView = ViewMode.NO_GANG;
        } else if (currentView == ViewMode.NO_GANG) {
            currentView = ViewMode.OVERVIEW;
        }
    }

    private void initButtons() {
        clearWidgets();

        if (currentView == ViewMode.NO_GANG) {
            // Nur Zurueck-Button
            addRenderableWidget(Button.builder(Component.translatable("gui.app.gang.back"), button -> {
                if (minecraft != null) minecraft.setScreen(parentScreen);
            }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());
            return;
        }

        // Tab-Buttons oben
        int tabY = topPos + 28;
        int tabWidth = 70;
        int tabSpacing = 3;
        int tabStartX = leftPos + 10;

        addRenderableWidget(Button.builder(Component.translatable("gui.app.gang.tab_overview"), button -> {
            currentView = ViewMode.OVERVIEW;
            scrollOffset = 0;
            initButtons();
        }).bounds(tabStartX, tabY, tabWidth, 16).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.app.gang.tab_perks"), button -> {
            currentView = ViewMode.PERKS;
            scrollOffset = 0;
            initButtons();
        }).bounds(tabStartX + tabWidth + tabSpacing, tabY, tabWidth, 16).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.app.gang.tab_missions"), button -> {
            currentView = ViewMode.MISSIONS;
            scrollOffset = 0;
            initButtons();
        }).bounds(tabStartX + 2 * (tabWidth + tabSpacing), tabY, tabWidth, 16).build());

        // Zurueck-Button
        addRenderableWidget(Button.builder(Component.translatable("gui.app.gang.back"), button -> {
            if (minecraft != null) minecraft.setScreen(parentScreen);
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 80, 20).build());

        // Refresh-Button
        addRenderableWidget(Button.builder(Component.literal("\u21BB"), button -> {
            GangNetworkHandler.sendToServer(new RequestGangDataPacket());
        }).bounds(leftPos + WIDTH - 30, topPos + HEIGHT - 30, 20, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Smartphone-Hintergrund
        guiGraphics.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF2A2A2A);

        // Header
        guiGraphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + 28, 0xFF1A1A1A);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("gui.app.gang.title").getString(),
                leftPos + WIDTH / 2, topPos + 10, 0xFFFFFF);

        int contentY = topPos + 48;
        int contentEndY = topPos + HEIGHT - 40;

        switch (currentView) {
            case NO_GANG -> renderNoGangView(guiGraphics, contentY, contentEndY);
            case OVERVIEW -> renderOverviewView(guiGraphics, contentY, contentEndY);
            case PERKS -> renderPerksView(guiGraphics, contentY, contentEndY);
            case MISSIONS -> renderMissionsView(guiGraphics, contentY, contentEndY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // ═══════════════════════════════════════════════════════════
    // NO GANG VIEW
    // ═══════════════════════════════════════════════════════════

    private void renderNoGangView(GuiGraphics guiGraphics, int startY, int endY) {
        int centerX = leftPos + WIDTH / 2;
        int y = startY + 30;

        guiGraphics.drawCenteredString(this.font, "\u00A77Keine Gang", centerX, y, 0xAAAAAA);
        y += 20;
        guiGraphics.drawCenteredString(this.font, "\u00A78Du bist in keiner Gang.", centerX, y, 0x888888);
        y += 20;
        guiGraphics.drawCenteredString(this.font, "\u00A7eNutze \u00A7f/gang create <name> <tag>", centerX, y, 0xFFFF55);
        y += 12;
        guiGraphics.drawCenteredString(this.font, "\u00A7eum eine Gang zu gruenden.", centerX, y, 0xFFFF55);
        y += 25;
        guiGraphics.drawCenteredString(this.font, "\u00A77Oder warte auf eine Einladung.", centerX, y, 0xAAAAAA);
        y += 12;
        guiGraphics.drawCenteredString(this.font, "\u00A7e/gang accept \u00A77zum Annehmen", centerX, y, 0xAAAAAA);
    }

    // ═══════════════════════════════════════════════════════════
    // OVERVIEW VIEW
    // ═══════════════════════════════════════════════════════════

    private static final int OVERVIEW_HEADER_HEIGHT = 95;
    private static final int MEMBER_ROW_HEIGHT = 22;

    private void renderOverviewView(GuiGraphics guiGraphics, int startY, int endY) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.hasGang()) return;

        // ── Fester Header: Gang-Info ──
        guiGraphics.fill(leftPos + 10, startY, leftPos + WIDTH - 10, startY + OVERVIEW_HEADER_HEIGHT - 5, 0x44333333);

        int y = startY + 3;

        // Gang-Name + Tag
        String gangTag = formatGangTag(data);
        guiGraphics.drawCenteredString(this.font, gangTag + " \u00A7f" + data.getGangName(),
                leftPos + WIDTH / 2, y, 0xFFFFFF);
        y += 12;

        // Level + Reputation
        GangReputation rep = GangReputation.getForLevel(data.getGangLevel());
        String levelText = "\u00A76Level " + data.getGangLevel() + " \u00A77| " + rep.getFormattedName();
        guiGraphics.drawCenteredString(this.font, levelText, leftPos + WIDTH / 2, y, 0xFFFFFF);
        y += 12;

        // XP-Fortschrittsbalken
        int barX = leftPos + 20;
        int barWidth = WIDTH - 40;
        int barHeight = 10;
        int filledWidth = (int) (barWidth * data.getGangProgress());

        guiGraphics.fill(barX, y, barX + barWidth, y + barHeight, 0xFF333333);
        if (filledWidth > 0) {
            int barColor = data.getGangLevel() >= 25 ? 0xFFFFAA00 : (data.getGangLevel() >= 15 ? 0xFF00AAFF : 0xFF00AA00);
            guiGraphics.fill(barX, y, barX + filledWidth, y + barHeight, barColor);
        }
        String progressStr = String.format("%.0f%%", data.getGangProgress() * 100);
        guiGraphics.drawCenteredString(this.font, progressStr, leftPos + WIDTH / 2, y + 1, 0xFFFFFF);
        y += barHeight + 4;

        // XP + Balance
        if (data.getGangLevel() < GangLevelRequirements.MAX_LEVEL) {
            String xpText = "\u00A77XP: \u00A7f" + data.getGangXP() + " \u00A77| Kasse: \u00A7a" + data.getGangBalance() + "\u20AC";
            guiGraphics.drawCenteredString(this.font, xpText, leftPos + WIDTH / 2, y, 0xFFFFFF);
        } else {
            guiGraphics.drawCenteredString(this.font, "\u00A76\u00A7lMAX LEVEL! \u00A77Kasse: \u00A7a" + data.getGangBalance() + "\u20AC",
                    leftPos + WIDTH / 2, y, 0xFFAA00);
        }
        y += 12;

        // Mitglieder + Territory + Perks
        String infoLine = "\u00A77Mitgl: \u00A7f" + data.getMemberCount() + "/" + data.getMaxMembers() +
                " \u00A77| Gebiet: \u00A7f" + data.getTerritoryCount() + "/" + data.getMaxTerritory() +
                " \u00A77| Perks: \u00A7f" + data.getAvailablePerkPoints();
        guiGraphics.drawCenteredString(this.font, infoLine, leftPos + WIDTH / 2, y, 0xFFFFFF);
        y += 14;

        // ── Scrollbare Mitgliederliste ──
        guiGraphics.drawString(this.font, "\u00A76Mitglieder:", leftPos + 15, y - 2, 0xFFAA00);

        int listStartY = startY + OVERVIEW_HEADER_HEIGHT;
        guiGraphics.enableScissor(leftPos + 5, listStartY, leftPos + WIDTH - 5, endY);

        List<SyncGangDataPacket.GangMemberInfo> members = data.getMembers();
        int contentHeight = members.size() * MEMBER_ROW_HEIGHT;
        int rowY = listStartY - scrollOffset;

        for (SyncGangDataPacket.GangMemberInfo member : members) {
            if (rowY >= listStartY - MEMBER_ROW_HEIGHT && rowY < endY + MEMBER_ROW_HEIGHT) {
                // Hintergrund (online = gruen-tint, offline = grau)
                int bgColor = member.online() ? 0x2200AA00 : 0x22333333;
                guiGraphics.fill(leftPos + 10, rowY, leftPos + WIDTH - 10, rowY + MEMBER_ROW_HEIGHT - 3, bgColor);

                // Rang + Name
                String memberText = member.rankColor() + member.rank() + " \u00A7f" + member.name();
                guiGraphics.drawString(this.font, memberText, leftPos + 15, rowY + 3, 0xFFFFFF);

                // Online-Status + XP rechts
                String statusText = (member.online() ? "\u00A7a\u25CF" : "\u00A78\u25CB") +
                        " \u00A78+" + member.contributedXP() + " XP";
                int statusWidth = this.font.width(statusText);
                guiGraphics.drawString(this.font, statusText,
                        leftPos + WIDTH - 18 - statusWidth, rowY + 3, 0xFFFFFF);
            }
            rowY += MEMBER_ROW_HEIGHT;
        }

        guiGraphics.disableScissor();

        // Scrollbar
        int visibleHeight = endY - listStartY;
        maxScroll = Math.max(0, contentHeight - visibleHeight);
        renderScrollbar(guiGraphics, listStartY, endY, visibleHeight);
    }

    // ═══════════════════════════════════════════════════════════
    // PERKS VIEW
    // ═══════════════════════════════════════════════════════════

    private static final int PERK_ROW_HEIGHT = 30;

    private void renderPerksView(GuiGraphics guiGraphics, int startY, int endY) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.hasGang()) return;

        // Perk-Punkte Header
        int usedPerks = data.getUnlockedPerks().size();
        int totalPoints = GangLevelRequirements.getAvailablePerkPoints(data.getGangLevel());
        int available = data.getAvailablePerkPoints();

        guiGraphics.fill(leftPos + 10, startY, leftPos + WIDTH - 10, startY + 18, 0x44333333);
        String headerText = "\u00A76Perk-Punkte: \u00A7a" + available + " \u00A77verfuegbar (" + usedPerks + "/" + totalPoints + ")";
        guiGraphics.drawCenteredString(this.font, headerText, leftPos + WIDTH / 2, startY + 5, 0xFFFFFF);

        int listStartY = startY + 22;
        guiGraphics.enableScissor(leftPos + 5, listStartY, leftPos + WIDTH - 5, endY);

        GangPerk[] perks = GangPerk.values();
        int contentHeight = 0;
        int rowY = listStartY - scrollOffset;

        GangPerk.PerkBranch lastBranch = null;

        for (GangPerk perk : perks) {
            // Zweig-Header
            if (perk.getBranch() != lastBranch) {
                lastBranch = perk.getBranch();
                if (rowY >= listStartY - 16 && rowY < endY + 16) {
                    guiGraphics.fill(leftPos + 10, rowY, leftPos + WIDTH - 10, rowY + 14, 0x44222222);
                    String branchHeader = perk.getBranch().getColorCode() + "\u00A7l" +
                            perk.getBranch().getDisplayName() + " \u00A77- " + perk.getBranch().getDescription();
                    guiGraphics.drawString(this.font, branchHeader, leftPos + 15, rowY + 3, 0xFFFFFF);
                }
                rowY += 16;
                contentHeight += 16;
            }

            if (rowY >= listStartY - PERK_ROW_HEIGHT && rowY < endY + PERK_ROW_HEIGHT) {
                boolean unlocked = data.hasPerk(perk);
                boolean canUnlock = !unlocked && perk.canUnlock(data.getGangLevel()) && available > 0;

                // Hintergrund
                int bgColor;
                if (unlocked) {
                    bgColor = 0x4400AA00;
                } else if (canUnlock) {
                    bgColor = 0x44FFAA00;
                } else {
                    bgColor = 0x44333333;
                }
                guiGraphics.fill(leftPos + 10, rowY, leftPos + WIDTH - 10, rowY + PERK_ROW_HEIGHT - 3, bgColor);

                // Status-Icon + Name
                String statusIcon = unlocked ? "\u00A7a\u2713" : (canUnlock ? "\u00A7e\u25CB" : "\u00A78\u2718");
                String nameColor = unlocked ? "\u00A7a" : (canUnlock ? "\u00A7f" : "\u00A78");
                guiGraphics.drawString(this.font, statusIcon + " " + nameColor + perk.getDisplayName(),
                        leftPos + 15, rowY + 3, 0xFFFFFF);

                // Level-Anforderung rechts
                String lvlText = "\u00A77Lv." + perk.getRequiredGangLevel();
                int lvlWidth = this.font.width(lvlText);
                guiGraphics.drawString(this.font, lvlText, leftPos + WIDTH - 18 - lvlWidth, rowY + 3, 0xFFFFFF);

                // Beschreibung
                String descColor = unlocked ? "\u00A72" : "\u00A78";
                guiGraphics.drawString(this.font, descColor + perk.getDescription(),
                        leftPos + 15, rowY + 15, 0x888888);
            }

            rowY += PERK_ROW_HEIGHT;
            contentHeight += PERK_ROW_HEIGHT;
        }

        guiGraphics.disableScissor();

        // Scrollbar
        int visibleHeight = endY - listStartY;
        maxScroll = Math.max(0, contentHeight - visibleHeight);
        renderScrollbar(guiGraphics, listStartY, endY, visibleHeight);
    }

    // ═══════════════════════════════════════════════════════════
    // MISSIONS VIEW
    // ═══════════════════════════════════════════════════════════

    private static final int MISSION_ROW_HEIGHT = 42;

    private void renderMissionsView(GuiGraphics guiGraphics, int startY, int endY) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.hasGang()) return;

        List<SyncGangDataPacket.MissionInfo> missions = data.getMissions();

        if (missions.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, "\u00A77Keine aktiven Missionen.",
                    leftPos + WIDTH / 2, startY + 30, 0xAAAAAA);
            guiGraphics.drawCenteredString(this.font, "\u00A78Missionen werden taeglich generiert.",
                    leftPos + WIDTH / 2, startY + 45, 0x888888);
            return;
        }

        guiGraphics.enableScissor(leftPos + 5, startY, leftPos + WIDTH - 5, endY);

        int contentHeight = missions.size() * MISSION_ROW_HEIGHT;
        int rowY = startY - scrollOffset;

        for (SyncGangDataPacket.MissionInfo mission : missions) {
            if (rowY >= startY - MISSION_ROW_HEIGHT && rowY < endY + MISSION_ROW_HEIGHT) {
                // Hintergrund
                int bgColor = mission.completed() ? 0x4400AA00 : 0x44333333;
                guiGraphics.fill(leftPos + 10, rowY, leftPos + WIDTH - 10, rowY + MISSION_ROW_HEIGHT - 3, bgColor);

                // Beschreibung
                String statusIcon = mission.completed() ? "\u00A7a\u2713 " : "\u00A7e\u25B6 ";
                guiGraphics.drawString(this.font, statusIcon + "\u00A7f" + mission.description(),
                        leftPos + 15, rowY + 3, 0xFFFFFF);

                // Fortschrittsbalken
                int barX = leftPos + 15;
                int barY = rowY + 16;
                int barWidth = WIDTH - 60;
                int barHeight = 8;
                double progress = mission.getProgressPercent();
                int filledWidth = (int) (barWidth * Math.min(1.0, progress));

                guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
                if (filledWidth > 0) {
                    int barColor = mission.completed() ? 0xFF00AA00 : 0xFFFFAA00;
                    guiGraphics.fill(barX, barY, barX + filledWidth, barY + barHeight, barColor);
                }

                // Fortschritt-Text
                String progressText = mission.currentProgress() + "/" + mission.targetAmount();
                guiGraphics.drawString(this.font, "\u00A77" + progressText,
                        barX + barWidth + 5, barY, 0xAAAAAA);

                // Belohnungen
                String rewardText = "\u00A7e+" + mission.xpReward() + " XP \u00A7a+" + mission.moneyReward() + "\u20AC";
                guiGraphics.drawString(this.font, rewardText, leftPos + 15, rowY + 28, 0xFFFFFF);
            }

            rowY += MISSION_ROW_HEIGHT;
        }

        guiGraphics.disableScissor();

        int visibleHeight = endY - startY;
        maxScroll = Math.max(0, contentHeight - visibleHeight);
        renderScrollbar(guiGraphics, startY, endY, visibleHeight);
    }

    // ═══════════════════════════════════════════════════════════
    // HILFSFUNKTIONEN
    // ═══════════════════════════════════════════════════════════

    private String formatGangTag(SyncGangDataPacket data) {
        int colorOrd = data.getGangColorOrdinal();
        net.minecraft.ChatFormatting cf = net.minecraft.ChatFormatting.getById(colorOrd);
        String colorCode = cf != null ? "\u00A7" + cf.getChar() : "\u00A7f";
        String stars = GangReputation.getLevelStars(data.getGangLevel());

        if (stars.isEmpty()) {
            return colorCode + "[" + data.getGangTag() + "]";
        }
        return colorCode + "[" + data.getGangTag() + " " + stars + colorCode + "]";
    }

    private void renderScrollbar(GuiGraphics guiGraphics, int listStartY, int endY, int visibleHeight) {
        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(20, visibleHeight * visibleHeight / (visibleHeight + maxScroll));
            int scrollBarY = listStartY + (scrollOffset * (visibleHeight - scrollBarHeight) / maxScroll);
            guiGraphics.fill(leftPos + WIDTH - 8, listStartY, leftPos + WIDTH - 5, endY, 0x44FFFFFF);
            guiGraphics.fill(leftPos + WIDTH - 8, scrollBarY, leftPos + WIDTH - 5,
                    scrollBarY + scrollBarHeight, 0xAAFFFFFF);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (currentView != ViewMode.NO_GANG && maxScroll > 0) {
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
