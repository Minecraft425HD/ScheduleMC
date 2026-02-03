package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.gang.GangLevelRequirements;
import de.rolandsw.schedulemc.gang.GangPerk;
import de.rolandsw.schedulemc.gang.GangRank;
import de.rolandsw.schedulemc.gang.GangReputation;
import de.rolandsw.schedulemc.gang.client.ClientGangCache;
import de.rolandsw.schedulemc.gang.network.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

/**
 * Gang-App fuer das Smartphone - vollstaendige Gang-Verwaltung.
 *
 * Ansichten:
 * - NO_GANG: Gang gruenden (EditBox) oder Einladung annehmen
 * - OVERVIEW: Gang-Info + Mitglieder mit Kick/Promote + Einladen/Verlassen/Aufloesen
 * - PERKS: Perk-Tree mit Klick-zum-Freischalten
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

    // NO_GANG: Eingabefelder
    private EditBox createNameInput;
    private EditBox createTagInput;

    // OVERVIEW: Einladen-Feld
    private EditBox inviteInput;

    // PERKS/OVERVIEW: Klick-Positionen fuer scrollbare Listen
    private int listStartY;

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

    private void sendActionAndRefresh(GangActionPacket packet) {
        GangNetworkHandler.sendToServer(packet);
        // Daten nach kurzer Verzoegerung neu laden (Server braucht Zeit)
        GangNetworkHandler.sendToServer(new RequestGangDataPacket());
    }

    // ═══════════════════════════════════════════════════════════
    // WIDGET SETUP
    // ═══════════════════════════════════════════════════════════

    private void initButtons() {
        clearWidgets();
        createNameInput = null;
        createTagInput = null;
        inviteInput = null;

        if (currentView == ViewMode.NO_GANG) {
            initNoGangWidgets();
            return;
        }

        // Tab-Buttons
        int tabY = topPos + 28;
        int tabWidth = 70;
        int tabSpacing = 3;
        int tabStartX = leftPos + 10;

        addRenderableWidget(Button.builder(Component.literal("\u00A7f\u00BB \u00A7fInfo"), button -> {
            currentView = ViewMode.OVERVIEW; scrollOffset = 0; initButtons();
        }).bounds(tabStartX, tabY, tabWidth, 16).build());

        addRenderableWidget(Button.builder(Component.literal("\u00A7f\u2726 Perks"), button -> {
            currentView = ViewMode.PERKS; scrollOffset = 0; initButtons();
        }).bounds(tabStartX + tabWidth + tabSpacing, tabY, tabWidth, 16).build());

        addRenderableWidget(Button.builder(Component.literal("\u00A7f\u2691 Missions"), button -> {
            currentView = ViewMode.MISSIONS; scrollOffset = 0; initButtons();
        }).bounds(tabStartX + 2 * (tabWidth + tabSpacing), tabY, tabWidth, 16).build());

        if (currentView == ViewMode.OVERVIEW) {
            initOverviewWidgets();
        }

        // Zurueck + Refresh unten
        addRenderableWidget(Button.builder(Component.translatable("gui.app.gang.back"), button -> {
            if (minecraft != null) minecraft.setScreen(parentScreen);
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 60, 20).build());

        addRenderableWidget(Button.builder(Component.literal("\u21BB"), button -> {
            GangNetworkHandler.sendToServer(new RequestGangDataPacket());
        }).bounds(leftPos + WIDTH - 30, topPos + HEIGHT - 30, 20, 20).build());
    }

    private void initNoGangWidgets() {
        int formX = leftPos + 25;
        int contentY = topPos + 48;

        // Gang-Name Eingabe
        createNameInput = new EditBox(this.font, formX, contentY + 25, 150, 18, Component.literal("Name"));
        createNameInput.setMaxLength(32);
        createNameInput.setHint(Component.literal("Gang-Name..."));
        addRenderableWidget(createNameInput);

        // Gang-Tag Eingabe
        createTagInput = new EditBox(this.font, formX, contentY + 55, 60, 18, Component.literal("Tag"));
        createTagInput.setMaxLength(5);
        createTagInput.setHint(Component.literal("TAG"));
        addRenderableWidget(createTagInput);

        // Gruenden-Button
        addRenderableWidget(Button.builder(Component.literal("\u00A7aGruenden"), button -> {
            String name = createNameInput.getValue().trim();
            String tag = createTagInput.getValue().trim().toUpperCase();
            if (!name.isEmpty() && !tag.isEmpty() && tag.length() >= 2) {
                sendActionAndRefresh(GangActionPacket.create(name, tag, "RED"));
            }
        }).bounds(formX + 70, contentY + 55, 80, 18).build());

        // Einladung annehmen
        addRenderableWidget(Button.builder(Component.literal("\u00A7eEinladung annehmen"), button -> {
            sendActionAndRefresh(GangActionPacket.acceptInvite(UUID.randomUUID()));
        }).bounds(formX, contentY + 115, 150, 20).build());

        // Zurueck
        addRenderableWidget(Button.builder(Component.translatable("gui.app.gang.back"), button -> {
            if (minecraft != null) minecraft.setScreen(parentScreen);
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 60, 20).build());
    }

    private void initOverviewWidgets() {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.hasGang()) return;

        int footerY = topPos + HEIGHT - 55;

        // Einladen (nur wenn Berechtigung)
        if (data.canInvite()) {
            inviteInput = new EditBox(this.font, leftPos + 12, footerY, 105, 16, Component.literal("Invite"));
            inviteInput.setMaxLength(32);
            inviteInput.setHint(Component.literal("Spielername..."));
            addRenderableWidget(inviteInput);

            addRenderableWidget(Button.builder(Component.literal("\u00A7a+Einladen"), button -> {
                if (inviteInput != null && !inviteInput.getValue().trim().isEmpty()) {
                    sendActionAndRefresh(GangActionPacket.inviteByName(inviteInput.getValue().trim()));
                    inviteInput.setValue("");
                }
            }).bounds(leftPos + 120, footerY, 60, 16).build());
        }

        // Verlassen-Button
        addRenderableWidget(Button.builder(Component.literal("\u00A7cVerlassen"), button -> {
            sendActionAndRefresh(GangActionPacket.leave());
        }).bounds(leftPos + 185, footerY, 50, 16).build());

        // Aufloesen-Button (nur Boss)
        if (data.canDisband()) {
            addRenderableWidget(Button.builder(Component.literal("\u00A74Aufloesen"), button -> {
                sendActionAndRefresh(GangActionPacket.disband());
            }).bounds(leftPos + 185, footerY - 20, 50, 16).build());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // RENDER
    // ═══════════════════════════════════════════════════════════

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
        int formX = leftPos + 25;

        guiGraphics.drawString(this.font, "\u00A76\u00A7lGang gruenden:", formX, startY + 10, 0xFFAA00);
        guiGraphics.drawString(this.font, "\u00A77Name:", formX, startY + 16, 0xAAAAAA);
        guiGraphics.drawString(this.font, "\u00A77Tag:", formX, startY + 46, 0xAAAAAA);

        // Trennlinie
        guiGraphics.fill(leftPos + 15, startY + 85, leftPos + WIDTH - 15, startY + 86, 0x44FFFFFF);

        guiGraphics.drawString(this.font, "\u00A76Einladung erhalten?", formX, startY + 98, 0xFFAA00);
        guiGraphics.drawString(this.font, "\u00A78Klicke um beizutreten:", formX, startY + 108, 0x888888);
    }

    // ═══════════════════════════════════════════════════════════
    // OVERVIEW VIEW
    // ═══════════════════════════════════════════════════════════

    private static final int OVERVIEW_HEADER_HEIGHT = 75;
    private static final int MEMBER_ROW_HEIGHT = 22;

    private void renderOverviewView(GuiGraphics guiGraphics, int startY, int endY) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.hasGang()) return;

        // Footer-Bereich abziehen
        int footerHeight = 48;
        int memberEndY = endY - footerHeight;

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
        guiGraphics.drawCenteredString(this.font,
                "\u00A76Lv." + data.getGangLevel() + " \u00A77| " + rep.getFormattedName(),
                leftPos + WIDTH / 2, y, 0xFFFFFF);
        y += 12;

        // XP-Fortschrittsbalken
        int barX = leftPos + 20;
        int barWidth = WIDTH - 40;
        int filledWidth = (int) (barWidth * data.getGangProgress());
        guiGraphics.fill(barX, y, barX + barWidth, y + 8, 0xFF333333);
        if (filledWidth > 0) {
            int barColor = data.getGangLevel() >= 25 ? 0xFFFFAA00 : (data.getGangLevel() >= 15 ? 0xFF00AAFF : 0xFF00AA00);
            guiGraphics.fill(barX, y, barX + filledWidth, y + 8, barColor);
        }
        guiGraphics.drawCenteredString(this.font,
                String.format("%.0f%%", data.getGangProgress() * 100),
                leftPos + WIDTH / 2, y, 0xFFFFFF);
        y += 12;

        // Stats-Zeile
        String stats = "\u00A77XP:\u00A7f" + data.getGangXP() + " \u00A77Kasse:\u00A7a" + data.getGangBalance() +
                "\u20AC \u00A77M:\u00A7f" + data.getMemberCount() + "/" + data.getMaxMembers() +
                " \u00A77G:\u00A7f" + data.getTerritoryCount() + "/" + data.getMaxTerritory();
        guiGraphics.drawCenteredString(this.font, stats, leftPos + WIDTH / 2, y, 0xFFFFFF);
        y += 14;

        // ── Scrollbare Mitgliederliste ──
        guiGraphics.drawString(this.font, "\u00A76Mitglieder:", leftPos + 15, y - 2, 0xFFAA00);

        listStartY = startY + OVERVIEW_HEADER_HEIGHT;
        guiGraphics.enableScissor(leftPos + 5, listStartY, leftPos + WIDTH - 5, memberEndY);

        List<SyncGangDataPacket.GangMemberInfo> members = data.getMembers();
        int contentHeight = members.size() * MEMBER_ROW_HEIGHT;
        int rowY = listStartY - scrollOffset;
        int myRank = data.getMyRankPriority();

        for (SyncGangDataPacket.GangMemberInfo member : members) {
            if (rowY >= listStartY - MEMBER_ROW_HEIGHT && rowY < memberEndY + MEMBER_ROW_HEIGHT) {
                // Hintergrund
                int bgColor = member.online() ? 0x2200AA00 : 0x22333333;
                guiGraphics.fill(leftPos + 10, rowY, leftPos + WIDTH - 10, rowY + MEMBER_ROW_HEIGHT - 3, bgColor);

                // Rang + Name
                guiGraphics.drawString(this.font,
                        member.rankColor() + member.rank() + " \u00A7f" + member.name(),
                        leftPos + 15, rowY + 3, 0xFFFFFF);

                // Online-Status + XP
                String statusText = (member.online() ? "\u00A7a\u25CF" : "\u00A78\u25CB") +
                        " \u00A78+" + member.contributedXP() + "XP";

                // Aktions-Buttons: [↑] [X] nur wenn Berechtigung und niedrigerer Rang
                int rightX = leftPos + WIDTH - 15;
                boolean canActOnMember = myRank > member.rankPriority() && member.rankPriority() < 4;

                if (canActOnMember && myRank >= 3) {
                    // [X] Kick-Button
                    guiGraphics.fill(rightX - 13, rowY + 2, rightX, rowY + MEMBER_ROW_HEIGHT - 5, 0x66CC3333);
                    guiGraphics.drawCenteredString(this.font, "\u00A7cX",
                            rightX - 6, rowY + 3, 0xFF5555);
                    rightX -= 16;

                    // [↑] Promote-Button (nur Boss kann befoerdern)
                    if (myRank >= 4) {
                        guiGraphics.fill(rightX - 13, rowY + 2, rightX, rowY + MEMBER_ROW_HEIGHT - 5, 0x6633AA33);
                        guiGraphics.drawCenteredString(this.font, "\u00A7a\u2191",
                                rightX - 6, rowY + 3, 0x55FF55);
                        rightX -= 16;
                    }
                }

                int statusWidth = this.font.width(statusText);
                guiGraphics.drawString(this.font, statusText,
                        rightX - statusWidth - 2, rowY + 3, 0xFFFFFF);
            }
            rowY += MEMBER_ROW_HEIGHT;
        }

        guiGraphics.disableScissor();

        // Scrollbar
        int visibleHeight = memberEndY - listStartY;
        maxScroll = Math.max(0, contentHeight - visibleHeight);
        renderScrollbar(guiGraphics, listStartY, memberEndY, visibleHeight);
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

        if (data.canManagePerks() && available > 0) {
            guiGraphics.drawCenteredString(this.font, "\u00A7e\u00A7oKlicke auf einen Perk zum Freischalten",
                    leftPos + WIDTH / 2, startY + 16, 0xFFFF55);
        }

        listStartY = startY + (data.canManagePerks() && available > 0 ? 28 : 22);
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
                    guiGraphics.drawString(this.font,
                            perk.getBranch().getColorCode() + "\u00A7l" +
                            perk.getBranch().getDisplayName() + " \u00A77- " + perk.getBranch().getDescription(),
                            leftPos + 15, rowY + 3, 0xFFFFFF);
                }
                rowY += 16;
                contentHeight += 16;
            }

            if (rowY >= listStartY - PERK_ROW_HEIGHT && rowY < endY + PERK_ROW_HEIGHT) {
                boolean unlocked = data.hasPerk(perk);
                boolean canUnlock = !unlocked && perk.canUnlock(data.getGangLevel()) && available > 0 && data.canManagePerks();

                // Hintergrund
                int bgColor = unlocked ? 0x4400AA00 : (canUnlock ? 0x44FFAA00 : 0x44333333);
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

                // Klick-Hinweis fuer freischaltbare Perks
                if (canUnlock) {
                    guiGraphics.drawString(this.font, "\u00A7e[Klick]",
                            leftPos + WIDTH - 18 - this.font.width("\u00A7e[Klick]"), rowY + 15, 0xFFFF55);
                }
            }

            rowY += PERK_ROW_HEIGHT;
            contentHeight += PERK_ROW_HEIGHT;
        }

        guiGraphics.disableScissor();

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
                int bgColor = mission.completed() ? 0x4400AA00 : 0x44333333;
                guiGraphics.fill(leftPos + 10, rowY, leftPos + WIDTH - 10, rowY + MISSION_ROW_HEIGHT - 3, bgColor);

                String statusIcon = mission.completed() ? "\u00A7a\u2713 " : "\u00A7e\u25B6 ";
                guiGraphics.drawString(this.font, statusIcon + "\u00A7f" + mission.description(),
                        leftPos + 15, rowY + 3, 0xFFFFFF);

                int barX = leftPos + 15;
                int barY = rowY + 16;
                int barWidth = WIDTH - 60;
                int barHeight = 8;
                double progress = mission.getProgressPercent();
                int filledWidth = (int) (barWidth * Math.min(1.0, progress));

                guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
                if (filledWidth > 0) {
                    guiGraphics.fill(barX, barY, barX + filledWidth, barY + barHeight,
                            mission.completed() ? 0xFF00AA00 : 0xFFFFAA00);
                }

                guiGraphics.drawString(this.font,
                        "\u00A77" + mission.currentProgress() + "/" + mission.targetAmount(),
                        barX + barWidth + 5, barY, 0xAAAAAA);

                guiGraphics.drawString(this.font,
                        "\u00A7e+" + mission.xpReward() + " XP \u00A7a+" + mission.moneyReward() + "\u20AC",
                        leftPos + 15, rowY + 28, 0xFFFFFF);
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

    private void renderScrollbar(GuiGraphics guiGraphics, int listTop, int listBottom, int visibleHeight) {
        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(20, visibleHeight * visibleHeight / (visibleHeight + maxScroll));
            int scrollBarY = listTop + (scrollOffset * (visibleHeight - scrollBarHeight) / maxScroll);
            guiGraphics.fill(leftPos + WIDTH - 8, listTop, leftPos + WIDTH - 5, listBottom, 0x44FFFFFF);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Perk-Klick zum Freischalten
            if (currentView == ViewMode.PERKS) {
                if (handlePerkClick(mouseX, mouseY)) return true;
            }
            // Member-Aktionen (Kick/Promote)
            if (currentView == ViewMode.OVERVIEW) {
                if (handleMemberClick(mouseX, mouseY)) return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handlePerkClick(double mouseX, double mouseY) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.canManagePerks() || data.getAvailablePerkPoints() <= 0) return false;

        if (mouseX < leftPos + 10 || mouseX > leftPos + WIDTH - 10) return false;
        if (mouseY < listStartY || mouseY > topPos + HEIGHT - 40) return false;

        // Position im scrollbaren Bereich berechnen
        double relativeY = mouseY - listStartY + scrollOffset;

        GangPerk[] perks = GangPerk.values();
        int yOffset = 0;
        GangPerk.PerkBranch lastBranch = null;

        for (GangPerk perk : perks) {
            if (perk.getBranch() != lastBranch) {
                lastBranch = perk.getBranch();
                yOffset += 16; // Branch-Header
            }

            if (relativeY >= yOffset && relativeY < yOffset + PERK_ROW_HEIGHT) {
                boolean unlocked = data.hasPerk(perk);
                boolean canUnlock = !unlocked && perk.canUnlock(data.getGangLevel());
                if (canUnlock) {
                    sendActionAndRefresh(GangActionPacket.unlockPerk(perk.name()));
                    return true;
                }
            }
            yOffset += PERK_ROW_HEIGHT;
        }
        return false;
    }

    private boolean handleMemberClick(double mouseX, double mouseY) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null) return false;

        int myRank = data.getMyRankPriority();
        if (myRank < 3) return false; // Nur Boss/Underboss koennen Kick/Promote

        if (mouseY < listStartY || mouseY > topPos + HEIGHT - 88) return false;

        double relativeY = mouseY - listStartY + scrollOffset;

        List<SyncGangDataPacket.GangMemberInfo> members = data.getMembers();
        int memberIndex = (int) (relativeY / MEMBER_ROW_HEIGHT);
        if (memberIndex < 0 || memberIndex >= members.size()) return false;

        SyncGangDataPacket.GangMemberInfo member = members.get(memberIndex);
        if (myRank <= member.rankPriority() || member.rankPriority() >= 4) return false;

        int rightX = leftPos + WIDTH - 15;

        // [X] Kick-Button Bereich (rechts aussen)
        if (mouseX >= rightX - 13 && mouseX <= rightX) {
            sendActionAndRefresh(GangActionPacket.kick(member.uuid()));
            return true;
        }

        // [↑] Promote-Button Bereich (links neben Kick, nur Boss)
        if (myRank >= 4 && mouseX >= rightX - 29 && mouseX <= rightX - 16) {
            // Naechsten Rang bestimmen
            GangRank nextRank = getNextRank(member.rankPriority());
            if (nextRank != null) {
                sendActionAndRefresh(GangActionPacket.promote(member.uuid(), nextRank));
                return true;
            }
        }

        return false;
    }

    private GangRank getNextRank(int currentPriority) {
        return switch (currentPriority) {
            case 1 -> GangRank.MEMBER;    // RECRUIT -> MEMBER
            case 2 -> GangRank.UNDERBOSS; // MEMBER -> UNDERBOSS
            default -> null;              // UNDERBOSS kann nicht weiter befoerdert werden via Button
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Erlaube Tippen in EditBox-Feldern (E-Taste nicht blocken)
        if (getFocused() instanceof EditBox) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == 69) { // GLFW_KEY_E - Inventar nicht oeffnen
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // Sicherstellen dass Zeichen an fokussierte EditBox weitergeleitet werden
        if (getFocused() instanceof EditBox) {
            return super.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
