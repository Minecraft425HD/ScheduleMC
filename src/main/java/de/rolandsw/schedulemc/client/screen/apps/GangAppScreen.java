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
 * Gang-App im Messenger-Style (WhatsApp Gruppen-Info).
 *
 * Einzige scrollbare Seite mit Sektionen:
 * - Profil-Header (Name, Tag, Level, XP-Bar, Stats)
 * - Mitglieder (Kontakt-Zeilen mit Kick/Promote)
 * - Perks (Klick-zum-Freischalten)
 * - Andere Gangs (Server-Gang-Liste = /gang list + /gang info)
 * - Missionen (Fortschrittsbalken)
 *
 * NO_GANG: Gang gruenden oder Einladung annehmen.
 */
@OnlyIn(Dist.CLIENT)
public class GangAppScreen extends Screen {

    private final Screen parentScreen;
    private static final int WIDTH = 240;
    private static final int HEIGHT = 260;
    private static final int SCROLL_SPEED = 15;

    // Layout-Bereiche
    private static final int HEADER_HEIGHT = 28;
    private static final int FOOTER_HEIGHT = 50;

    private boolean hasGang = false;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private int leftPos;
    private int topPos;

    // Fester Inhaltbereich (zwischen Header und Footer)
    private int contentTop;
    private int contentBottom;

    // NO_GANG Widgets
    private EditBox createNameInput;
    private EditBox createTagInput;

    // HAS_GANG Widgets (im Footer)
    private EditBox inviteInput;
    private EditBox feeInput;

    // Berechnete Sektions-Positionen (fuer Klick-Erkennung)
    private int membersSectionY;
    private int perksSectionY;

    public GangAppScreen(Screen parent) {
        super(Component.translatable("gui.app.gang.title"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = 5;
        this.contentTop = topPos + HEADER_HEIGHT;
        this.contentBottom = topPos + HEIGHT - FOOTER_HEIGHT;

        ClientGangCache.setUpdateListener(this::onCacheUpdated);

        // Daten laden
        GangNetworkHandler.sendToServer(new RequestGangDataPacket());
        GangNetworkHandler.sendToServer(new RequestGangListPacket());

        updateState();
        rebuildWidgets();
    }

    @Override
    public void removed() {
        super.removed();
        ClientGangCache.removeUpdateListener();
    }

    private void onCacheUpdated() {
        boolean wasInGang = hasGang;
        updateState();
        if (wasInGang != hasGang) {
            scrollOffset = 0;
        }
        rebuildWidgets();
    }

    private void updateState() {
        hasGang = ClientGangCache.hasGang();
    }

    private void sendActionAndRefresh(GangActionPacket packet) {
        GangNetworkHandler.sendToServer(packet);
        GangNetworkHandler.sendToServer(new RequestGangDataPacket());
        GangNetworkHandler.sendToServer(new RequestGangListPacket());
    }

    // ═══════════════════════════════════════════════════════════
    // WIDGET SETUP
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
        createNameInput = null;
        createTagInput = null;
        inviteInput = null;
        feeInput = null;

        if (!hasGang) {
            buildNoGangWidgets();
        } else {
            buildGangWidgets();
        }
    }

    private void buildNoGangWidgets() {
        int cx = leftPos + 25;
        int cy = contentTop + 20;

        // Gang-Name Eingabe
        createNameInput = new EditBox(this.font, cx, cy + 25, 150, 18, Component.literal("Name"));
        createNameInput.setMaxLength(20);
        createNameInput.setHint(Component.literal("Gang-Name..."));
        addRenderableWidget(createNameInput);

        // Gang-Tag Eingabe
        createTagInput = new EditBox(this.font, cx, cy + 55, 60, 18, Component.literal("Tag"));
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
        }).bounds(cx + 70, cy + 55, 80, 18).build());

        // Einladung annehmen
        addRenderableWidget(Button.builder(Component.literal("\u00A7eEinladung annehmen"), button -> {
            sendActionAndRefresh(GangActionPacket.acceptInvite(UUID.randomUUID()));
        }).bounds(cx, cy + 110, 150, 20).build());

        // Zurueck
        addRenderableWidget(Button.builder(Component.translatable("gui.app.gang.back"), button -> {
            if (minecraft != null) minecraft.setScreen(parentScreen);
        }).bounds(leftPos + 10, topPos + HEIGHT - 30, 60, 20).build());
    }

    private void buildGangWidgets() {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null) return;

        int footerY = contentBottom + 4;

        // Zeile 1: Einladen (wenn Berechtigung)
        if (data.canInvite()) {
            inviteInput = new EditBox(this.font, leftPos + 10, footerY, 120, 16, Component.literal("Invite"));
            inviteInput.setMaxLength(32);
            inviteInput.setHint(Component.literal("Spieler einladen..."));
            addRenderableWidget(inviteInput);

            addRenderableWidget(Button.builder(Component.literal("\u00A7a+"), button -> {
                if (inviteInput != null && !inviteInput.getValue().trim().isEmpty()) {
                    sendActionAndRefresh(GangActionPacket.inviteByName(inviteInput.getValue().trim()));
                    inviteInput.setValue("");
                }
            }).bounds(leftPos + 133, footerY, 20, 16).build());
        }

        // Verlassen (rechts oben im Footer)
        addRenderableWidget(Button.builder(Component.literal("\u00A7cVerlassen"), button -> {
            sendActionAndRefresh(GangActionPacket.leave());
        }).bounds(leftPos + 160, footerY, 70, 16).build());

        // Zeile 2: Zurueck + Beitrag (Boss) + Aufloesen (Boss) + Refresh
        int row2Y = footerY + 20;

        addRenderableWidget(Button.builder(Component.translatable("gui.app.gang.back"), button -> {
            if (minecraft != null) minecraft.setScreen(parentScreen);
        }).bounds(leftPos + 10, row2Y, 50, 18).build());

        if (data.canDisband()) {
            // Beitrag-Eingabe (nur Boss)
            feeInput = new EditBox(this.font, leftPos + 63, row2Y, 45, 18, Component.literal("Fee"));
            feeInput.setMaxLength(5);
            feeInput.setHint(Component.literal(String.valueOf(data.getWeeklyFee())));
            feeInput.setFilter(s -> s.matches("\\d*"));
            addRenderableWidget(feeInput);

            addRenderableWidget(Button.builder(Component.literal("\u00A7a\u20AC"), button -> {
                if (feeInput != null && !feeInput.getValue().trim().isEmpty()) {
                    sendActionAndRefresh(GangActionPacket.setFee(
                            Integer.parseInt(feeInput.getValue().trim())));
                    feeInput.setValue("");
                }
            }).bounds(leftPos + 110, row2Y, 18, 18).build());

            addRenderableWidget(Button.builder(Component.literal("\u00A74X"), button -> {
                sendActionAndRefresh(GangActionPacket.disband());
            }).bounds(leftPos + 131, row2Y, 18, 18).build());
        }

        addRenderableWidget(Button.builder(Component.literal("\u21BB"), button -> {
            GangNetworkHandler.sendToServer(new RequestGangDataPacket());
            GangNetworkHandler.sendToServer(new RequestGangListPacket());
        }).bounds(leftPos + WIDTH - 30, row2Y, 20, 18).build());
    }

    // ═══════════════════════════════════════════════════════════
    // RENDER
    // ═══════════════════════════════════════════════════════════

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);

        // Smartphone Rahmen
        g.fill(leftPos - 5, topPos - 5, leftPos + WIDTH + 5, topPos + HEIGHT + 5, 0xFF1C1C1C);
        g.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF2A2A2A);

        // Header
        g.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEADER_HEIGHT, 0xFF1A1A1A);
        g.drawCenteredString(this.font, "\u00A7f\u00A7lGang", leftPos + WIDTH / 2, topPos + 10, 0xFFFFFF);

        if (!hasGang) {
            renderNoGangView(g);
        } else {
            renderGangView(g, mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    // ═══════════════════════════════════════════════════════════
    // NO GANG VIEW
    // ═══════════════════════════════════════════════════════════

    private void renderNoGangView(GuiGraphics g) {
        int cx = leftPos + 25;
        int cy = contentTop + 20;

        g.drawString(this.font, "\u00A76\u00A7lGang gruenden", cx, cy + 10, 0xFFAA00);
        g.drawString(this.font, "\u00A77Name:", cx, cy + 16, 0xAAAAAA);
        g.drawString(this.font, "\u00A77Tag (2-5):", cx, cy + 46, 0xAAAAAA);

        // Trennlinie
        g.fill(leftPos + 15, cy + 85, leftPos + WIDTH - 15, cy + 86, 0x44FFFFFF);

        g.drawString(this.font, "\u00A76Einladung erhalten?", cx, cy + 95, 0xFFAA00);
        g.drawString(this.font, "\u00A78Klicke um beizutreten:", cx, cy + 105, 0x888888);

        // Andere Gangs anzeigen (auch ohne eigene Gang)
        renderGangListStandalone(g, cy + 140);
    }

    private void renderGangListStandalone(GuiGraphics g, int startY) {
        List<SyncGangListPacket.GangListEntry> gangs = ClientGangCache.getGangList();
        if (gangs.isEmpty()) return;

        g.fill(leftPos + 10, startY, leftPos + WIDTH - 10, startY + 1, 0x44FFFFFF);
        g.drawString(this.font, "\u00A76\u00A7lGangs auf dem Server", leftPos + 15, startY + 5, 0xFFAA00);

        int y = startY + 18;
        int shown = Math.min(gangs.size(), 4);
        for (int i = 0; i < shown; i++) {
            SyncGangListPacket.GangListEntry gang = gangs.get(i);
            String tagColor = getColorForOrdinal(gang.colorOrdinal());
            g.drawString(this.font, tagColor + "[" + gang.tag() + "] \u00A7f" + gang.name() +
                    " \u00A77Lv." + gang.level() + " \u00A78(" + gang.memberCount() + " Mitgl.)",
                    leftPos + 15, y, 0xFFFFFF);
            y += 12;
        }
        if (gangs.size() > shown) {
            g.drawString(this.font, "\u00A78...und " + (gangs.size() - shown) + " weitere",
                    leftPos + 15, y, 0x888888);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GANG VIEW (Messenger-Style: Einzelne scrollbare Seite)
    // ═══════════════════════════════════════════════════════════

    private void renderGangView(GuiGraphics g, int mouseX, int mouseY) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.hasGang()) return;

        // Footer-Hintergrund
        g.fill(leftPos, contentBottom, leftPos + WIDTH, topPos + HEIGHT, 0xFF1A1A1A);
        g.fill(leftPos, contentBottom, leftPos + WIDTH, contentBottom + 1, 0x44FFFFFF);

        // Scrollbarer Bereich
        g.enableScissor(leftPos, contentTop, leftPos + WIDTH, contentBottom);

        int y = contentTop - scrollOffset;
        int totalHeight = 0;

        // ── PROFIL-HEADER ──
        totalHeight += renderProfileSection(g, data, y);
        y = contentTop - scrollOffset + totalHeight;

        // ── SEKTION: MITGLIEDER ──
        membersSectionY = totalHeight;
        totalHeight += renderMembersSection(g, data, y);
        y = contentTop - scrollOffset + totalHeight;

        // ── SEKTION: PERKS ──
        perksSectionY = totalHeight;
        totalHeight += renderPerksSection(g, data, y);
        y = contentTop - scrollOffset + totalHeight;

        // ── SEKTION: ANDERE GANGS ──
        totalHeight += renderGangListSection(g, y);
        y = contentTop - scrollOffset + totalHeight;

        // ── SEKTION: MISSIONEN ──
        totalHeight += renderMissionsSection(g, data, y);

        g.disableScissor();

        // Scroll berechnen
        int visibleHeight = contentBottom - contentTop;
        maxScroll = Math.max(0, totalHeight - visibleHeight);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));

        // Scrollbar
        if (maxScroll > 0) {
            int barHeight = Math.max(15, visibleHeight * visibleHeight / (visibleHeight + maxScroll));
            int barY = contentTop + (scrollOffset * (visibleHeight - barHeight) / maxScroll);
            g.fill(leftPos + WIDTH - 6, contentTop, leftPos + WIDTH - 3, contentBottom, 0x33FFFFFF);
            g.fill(leftPos + WIDTH - 6, barY, leftPos + WIDTH - 3, barY + barHeight, 0xAAFFFFFF);
        }
    }

    // ── PROFIL-SEKTION ──
    private int renderProfileSection(GuiGraphics g, SyncGangDataPacket data, int y) {
        int startY = y;
        int cx = leftPos + WIDTH / 2;

        y += 5;

        // Gang-Name + Tag
        String tagStr = formatGangTag(data);
        g.drawCenteredString(this.font, tagStr + " \u00A7f\u00A7l" + data.getGangName(), cx, y, 0xFFFFFF);
        y += 13;

        // Level + Reputation
        GangReputation rep = GangReputation.getForLevel(data.getGangLevel());
        g.drawCenteredString(this.font, "\u00A76Lv." + data.getGangLevel() + " \u00A77| " +
                rep.getFormattedName(), cx, y, 0xFFFFFF);
        y += 13;

        // XP-Fortschrittsbalken
        int barX = leftPos + 15;
        int barWidth = WIDTH - 30;
        int filledWidth = (int) (barWidth * data.getGangProgress());
        g.fill(barX, y, barX + barWidth, y + 7, 0xFF333333);
        if (filledWidth > 0) {
            int barColor = data.getGangLevel() >= 25 ? 0xFFFFAA00 :
                    (data.getGangLevel() >= 15 ? 0xFF00AAFF : 0xFF00AA00);
            g.fill(barX, y, barX + filledWidth, y + 7, barColor);
        }
        String pctText = String.format("%.0f%%", data.getGangProgress() * 100);
        g.drawCenteredString(this.font, pctText, cx, y - 1, 0xFFFFFF);
        y += 11;

        // Stats in 2x2 Grid
        int halfW = WIDTH / 2;
        g.drawString(this.font, "\u00A78Kasse: \u00A7a" + data.getGangBalance() + "\u20AC",
                leftPos + 15, y, 0xFFFFFF);
        g.drawString(this.font, "\u00A78Mitglieder: \u00A7f" + data.getMemberCount() + "/" + data.getMaxMembers(),
                leftPos + halfW, y, 0xFFFFFF);
        y += 11;
        g.drawString(this.font, "\u00A78Territory: \u00A7f" + data.getTerritoryCount() + "/" + data.getMaxTerritory(),
                leftPos + 15, y, 0xFFFFFF);
        g.drawString(this.font, "\u00A78Perks: \u00A7f" + data.getUnlockedPerks().size() + "/" +
                GangLevelRequirements.getAvailablePerkPoints(data.getGangLevel()),
                leftPos + halfW, y, 0xFFFFFF);
        y += 11;

        // Wochenbeitrag
        int fee = data.getWeeklyFee();
        if (fee > 0) {
            // Eigenen Beitrag berechnen (basierend auf Rang-Multiplikator)
            int myRank = data.getMyRankPriority();
            double mult = switch (myRank) {
                case 4 -> 0.0;   // Boss
                case 3 -> 0.10;  // Underboss
                case 2 -> 0.50;  // Member
                default -> 1.0;  // Recruit
            };
            int myFee = (int) Math.ceil(fee * mult);
            String feeText = myFee > 0
                    ? "\u00A78Beitrag: \u00A7e" + fee + "\u20AC/Wo \u00A77(dein: \u00A7a" + myFee + "\u20AC\u00A77)"
                    : "\u00A78Beitrag: \u00A7e" + fee + "\u20AC/Wo \u00A77(du: \u00A7abefreit\u00A77)";
            g.drawString(this.font, feeText, leftPos + 15, y, 0xFFFFFF);
        } else {
            g.drawString(this.font, "\u00A78Beitrag: \u00A7akein Beitrag", leftPos + 15, y, 0xFFFFFF);
        }
        y += 14;

        return y - startY;
    }

    // ── MITGLIEDER-SEKTION ──
    private static final int MEMBER_ROW_HEIGHT = 20;

    private int renderMembersSection(GuiGraphics g, SyncGangDataPacket data, int y) {
        int startY = y;

        // Sektions-Header
        g.fill(leftPos + 5, y, leftPos + WIDTH - 5, y + 1, 0x44FFFFFF);
        y += 4;
        g.drawString(this.font, "\u00A76\u00A7l\u25B6 Mitglieder", leftPos + 10, y, 0xFFAA00);
        y += 13;

        List<SyncGangDataPacket.GangMemberInfo> members = data.getMembers();
        int myRank = data.getMyRankPriority();

        for (SyncGangDataPacket.GangMemberInfo member : members) {
            if (y > contentTop - MEMBER_ROW_HEIGHT && y < contentBottom + MEMBER_ROW_HEIGHT) {
                // Zeile
                int bgColor = member.online() ? 0x2200AA00 : 0x11333333;
                g.fill(leftPos + 8, y, leftPos + WIDTH - 8, y + MEMBER_ROW_HEIGHT - 2, bgColor);

                // Online-Dot + Rang + Name
                String onlineDot = member.online() ? "\u00A7a\u25CF " : "\u00A78\u25CB ";
                g.drawString(this.font, onlineDot + member.rankColor() + member.rank() +
                        " \u00A7f" + member.name(), leftPos + 12, y + 3, 0xFFFFFF);

                // Rechte Seite: XP + Aktions-Buttons
                int rightX = leftPos + WIDTH - 12;
                boolean canAct = myRank > member.rankPriority() && member.rankPriority() < 4;

                if (canAct && myRank >= 3) {
                    // [X] Kick
                    g.fill(rightX - 11, y + 2, rightX, y + MEMBER_ROW_HEIGHT - 4, 0x55CC3333);
                    g.drawCenteredString(this.font, "\u00A7cX", rightX - 5, y + 3, 0xFF5555);
                    rightX -= 14;

                    // [↑] Promote (nur Boss)
                    if (myRank >= 4) {
                        g.fill(rightX - 11, y + 2, rightX, y + MEMBER_ROW_HEIGHT - 4, 0x5533AA33);
                        g.drawCenteredString(this.font, "\u00A7a\u2191", rightX - 5, y + 3, 0x55FF55);
                        rightX -= 14;
                    }
                }

                // XP-Beitrag
                String xpText = "\u00A78+" + member.contributedXP() + "XP";
                int xpWidth = this.font.width(xpText);
                g.drawString(this.font, xpText, rightX - xpWidth - 2, y + 3, 0x888888);
            }
            y += MEMBER_ROW_HEIGHT;
        }

        y += 3;
        return y - startY;
    }

    // ── PERKS-SEKTION ──
    private static final int PERK_ROW_HEIGHT = 24;

    private int renderPerksSection(GuiGraphics g, SyncGangDataPacket data, int y) {
        int startY = y;

        // Sektions-Header
        g.fill(leftPos + 5, y, leftPos + WIDTH - 5, y + 1, 0x44FFFFFF);
        y += 4;

        int available = data.getAvailablePerkPoints();
        int used = data.getUnlockedPerks().size();
        int total = GangLevelRequirements.getAvailablePerkPoints(data.getGangLevel());
        g.drawString(this.font, "\u00A76\u00A7l\u2726 Perks \u00A77(" + used + "/" + total +
                (available > 0 ? ", \u00A7a" + available + " frei" : "") + "\u00A77)",
                leftPos + 10, y, 0xFFAA00);
        y += 13;

        if (data.canManagePerks() && available > 0) {
            g.drawString(this.font, "\u00A7e\u00A7oKlicke zum Freischalten",
                    leftPos + 15, y, 0xFFFF55);
            y += 10;
        }

        GangPerk[] perks = GangPerk.values();
        GangPerk.PerkBranch lastBranch = null;

        for (GangPerk perk : perks) {
            // Zweig-Ueberschrift
            if (perk.getBranch() != lastBranch) {
                lastBranch = perk.getBranch();
                if (y > contentTop - 14 && y < contentBottom + 14) {
                    g.drawString(this.font, perk.getBranch().getColorCode() + "\u00A7l" +
                            perk.getBranch().getDisplayName(), leftPos + 12, y + 1, 0xFFFFFF);
                }
                y += 12;
            }

            if (y > contentTop - PERK_ROW_HEIGHT && y < contentBottom + PERK_ROW_HEIGHT) {
                boolean unlocked = data.hasPerk(perk);
                boolean canUnlock = !unlocked && perk.canUnlock(data.getGangLevel()) &&
                        available > 0 && data.canManagePerks();

                int bg = unlocked ? 0x3300AA00 : (canUnlock ? 0x33FFAA00 : 0x22222222);
                g.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + PERK_ROW_HEIGHT - 3, bg);

                // Icon + Name
                String icon = unlocked ? "\u00A7a\u2713" : (canUnlock ? "\u00A7e\u25CB" : "\u00A78\u2718");
                String nameColor = unlocked ? "\u00A7a" : (canUnlock ? "\u00A7f" : "\u00A78");
                g.drawString(this.font, icon + " " + nameColor + perk.getDisplayName(),
                        leftPos + 13, y + 2, 0xFFFFFF);

                // Level rechts
                String lvl = "\u00A77Lv." + perk.getRequiredGangLevel();
                g.drawString(this.font, lvl, leftPos + WIDTH - 15 - this.font.width(lvl), y + 2, 0xFFFFFF);

                // Beschreibung
                String desc = (unlocked ? "\u00A72" : "\u00A78") + perk.getDescription();
                g.drawString(this.font, desc, leftPos + 13, y + 12, 0x888888);
            }
            y += PERK_ROW_HEIGHT;
        }

        y += 3;
        return y - startY;
    }

    // ── ANDERE GANGS SEKTION ──
    private static final int GANG_ROW_HEIGHT = 28;

    private int renderGangListSection(GuiGraphics g, int y) {
        int startY = y;

        List<SyncGangListPacket.GangListEntry> gangs = ClientGangCache.getGangList();

        // Sektions-Header
        g.fill(leftPos + 5, y, leftPos + WIDTH - 5, y + 1, 0x44FFFFFF);
        y += 4;
        g.drawString(this.font, "\u00A76\u00A7l\u2691 Andere Gangs \u00A77(" + gangs.size() + ")",
                leftPos + 10, y, 0xFFAA00);
        y += 13;

        if (gangs.isEmpty()) {
            g.drawString(this.font, "\u00A78Keine anderen Gangs auf dem Server.",
                    leftPos + 15, y, 0x888888);
            y += 14;
        } else {
            for (SyncGangListPacket.GangListEntry gang : gangs) {
                if (y > contentTop - GANG_ROW_HEIGHT && y < contentBottom + GANG_ROW_HEIGHT) {
                    g.fill(leftPos + 8, y, leftPos + WIDTH - 8, y + GANG_ROW_HEIGHT - 3, 0x22333333);

                    // Zeile 1: Tag + Name + Level
                    String tagColor = getColorForOrdinal(gang.colorOrdinal());
                    String stars = GangReputation.getLevelStars(gang.level());
                    String tagDisplay = tagColor + "[" + gang.tag();
                    if (!stars.isEmpty()) tagDisplay += " " + stars + tagColor;
                    tagDisplay += "]";

                    g.drawString(this.font, tagDisplay + " \u00A7f" + gang.name(),
                            leftPos + 12, y + 2, 0xFFFFFF);

                    // Zeile 2: Details
                    g.drawString(this.font, "\u00A78Lv.\u00A7f" + gang.level() +
                                    " \u00A78| " + gang.reputationName() +
                                    " \u00A78| \u00A7f" + gang.memberCount() + "/" + gang.maxMembers() + " Mitgl." +
                                    " \u00A78| \u00A7f" + gang.territoryCount() + " Gebiet" + (gang.territoryCount() != 1 ? "e" : ""),
                            leftPos + 12, y + 14, 0x888888);
                }
                y += GANG_ROW_HEIGHT;
            }
        }

        y += 3;
        return y - startY;
    }

    // ── MISSIONEN-SEKTION ──
    private static final int MISSION_ROW_HEIGHT = 36;

    private int renderMissionsSection(GuiGraphics g, SyncGangDataPacket data, int y) {
        int startY = y;

        // Sektions-Header
        g.fill(leftPos + 5, y, leftPos + WIDTH - 5, y + 1, 0x44FFFFFF);
        y += 4;
        g.drawString(this.font, "\u00A76\u00A7l\u2605 Missionen", leftPos + 10, y, 0xFFAA00);
        y += 13;

        List<SyncGangDataPacket.MissionInfo> missions = data.getMissions();

        if (missions.isEmpty()) {
            g.drawString(this.font, "\u00A78Keine aktiven Missionen.", leftPos + 15, y, 0x888888);
            g.drawString(this.font, "\u00A78Missionen werden taeglich generiert.", leftPos + 15, y + 11, 0x888888);
            y += 24;
        } else {
            for (SyncGangDataPacket.MissionInfo mission : missions) {
                if (y > contentTop - MISSION_ROW_HEIGHT && y < contentBottom + MISSION_ROW_HEIGHT) {
                    int bg = mission.completed() ? 0x3300AA00 : 0x22333333;
                    g.fill(leftPos + 8, y, leftPos + WIDTH - 8, y + MISSION_ROW_HEIGHT - 3, bg);

                    // Beschreibung
                    String icon = mission.completed() ? "\u00A7a\u2713 " : "\u00A7e\u25B6 ";
                    g.drawString(this.font, icon + "\u00A7f" + mission.description(), leftPos + 12, y + 2, 0xFFFFFF);

                    // Fortschrittsbalken
                    int barX = leftPos + 12;
                    int barW = WIDTH - 55;
                    double progress = mission.getProgressPercent();
                    int filledW = (int) (barW * Math.min(1.0, progress));

                    g.fill(barX, y + 14, barX + barW, y + 20, 0xFF333333);
                    if (filledW > 0) {
                        g.fill(barX, y + 14, barX + filledW, y + 20,
                                mission.completed() ? 0xFF00AA00 : 0xFFFFAA00);
                    }
                    g.drawString(this.font, "\u00A77" + mission.currentProgress() + "/" + mission.targetAmount(),
                            barX + barW + 3, y + 14, 0xAAAAAA);

                    // Belohnung
                    g.drawString(this.font, "\u00A7e+" + mission.xpReward() + "XP \u00A7a+" +
                            mission.moneyReward() + "\u20AC", leftPos + 12, y + 24, 0xFFFFFF);
                }
                y += MISSION_ROW_HEIGHT;
            }
        }

        y += 5;
        return y - startY;
    }

    // ═══════════════════════════════════════════════════════════
    // HILFSFUNKTIONEN
    // ═══════════════════════════════════════════════════════════

    private String formatGangTag(SyncGangDataPacket data) {
        int colorOrd = data.getGangColorOrdinal();
        String colorCode = getColorForOrdinal(colorOrd);
        String stars = GangReputation.getLevelStars(data.getGangLevel());
        if (stars.isEmpty()) {
            return colorCode + "[" + data.getGangTag() + "]";
        }
        return colorCode + "[" + data.getGangTag() + " " + stars + colorCode + "]";
    }

    private static String getColorForOrdinal(int ordinal) {
        net.minecraft.ChatFormatting cf = net.minecraft.ChatFormatting.getById(ordinal);
        return cf != null ? "\u00A7" + cf.getChar() : "\u00A7f";
    }

    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (hasGang && maxScroll > 0) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - delta * SCROLL_SPEED));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hasGang) {
            // Klick im scrollbaren Bereich?
            if (mouseY >= contentTop && mouseY < contentBottom) {
                double relY = mouseY - contentTop + scrollOffset;

                // Perk-Klick?
                if (handlePerkClick(relY, mouseX)) return true;

                // Member-Klick?
                if (handleMemberClick(relY, mouseX)) return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handlePerkClick(double relativeY, double mouseX) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.canManagePerks() || data.getAvailablePerkPoints() <= 0) return false;
        if (mouseX < leftPos + 10 || mouseX > leftPos + WIDTH - 10) return false;

        // Position innerhalb der Perk-Sektion
        double yInSection = relativeY - perksSectionY;

        // Header + "Klicke zum Freischalten" Text ueberspringen
        int available = data.getAvailablePerkPoints();
        double perkContentStart = 17 + (data.canManagePerks() && available > 0 ? 10 : 0);

        double localY = yInSection - perkContentStart;
        if (localY < 0) return false;

        GangPerk[] perks = GangPerk.values();
        GangPerk.PerkBranch lastBranch = null;
        int yOffset = 0;

        for (GangPerk perk : perks) {
            if (perk.getBranch() != lastBranch) {
                lastBranch = perk.getBranch();
                yOffset += 12; // Branch header
            }

            if (localY >= yOffset && localY < yOffset + PERK_ROW_HEIGHT) {
                boolean unlocked = data.hasPerk(perk);
                if (!unlocked && perk.canUnlock(data.getGangLevel())) {
                    sendActionAndRefresh(GangActionPacket.unlockPerk(perk.name()));
                    return true;
                }
            }
            yOffset += PERK_ROW_HEIGHT;
        }
        return false;
    }

    private boolean handleMemberClick(double relativeY, double mouseX) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null) return false;

        int myRank = data.getMyRankPriority();
        if (myRank < 3) return false;

        // Position innerhalb der Mitglieder-Sektion
        double yInSection = relativeY - membersSectionY;
        double memberContentStart = 17; // Header
        double localY = yInSection - memberContentStart;
        if (localY < 0) return false;

        List<SyncGangDataPacket.GangMemberInfo> members = data.getMembers();
        int memberIndex = (int) (localY / MEMBER_ROW_HEIGHT);
        if (memberIndex < 0 || memberIndex >= members.size()) return false;

        SyncGangDataPacket.GangMemberInfo member = members.get(memberIndex);
        if (myRank <= member.rankPriority() || member.rankPriority() >= 4) return false;

        int rightX = leftPos + WIDTH - 12;

        // [X] Kick
        if (mouseX >= rightX - 11 && mouseX <= rightX) {
            sendActionAndRefresh(GangActionPacket.kick(member.uuid()));
            return true;
        }

        // [↑] Promote (nur Boss)
        if (myRank >= 4 && mouseX >= rightX - 25 && mouseX <= rightX - 14) {
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
            case 1 -> GangRank.MEMBER;
            case 2 -> GangRank.UNDERBOSS;
            default -> null;
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (getFocused() instanceof EditBox) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == 69) { // GLFW_KEY_E
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
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
