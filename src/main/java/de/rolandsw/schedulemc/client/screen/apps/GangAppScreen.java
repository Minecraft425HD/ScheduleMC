package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.gang.GangLevelRequirements;
import de.rolandsw.schedulemc.gang.GangPerk;
import de.rolandsw.schedulemc.gang.GangRank;
import de.rolandsw.schedulemc.gang.GangReputation;
import de.rolandsw.schedulemc.gang.client.ClientGangCache;
import de.rolandsw.schedulemc.gang.mission.MissionType;
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
 * Gang-App im GTA-Handy-Style.
 *
 * Hauptansicht: Nachrichten-Inbox (wie WhatsApp-Chats).
 * Jede Kategorie fuehrt zu einer scrollbaren Unterseite.
 *
 * SubPages: INBOX, ZENTRALE, AUFTRAEGE, RIVALEN, MITGLIEDER, PERKS, BERICHT
 */
@OnlyIn(Dist.CLIENT)
public class GangAppScreen extends Screen {

    private final Screen parentScreen;
    private static final int WIDTH = 240;
    private static final int HEIGHT = 260;
    private static final int SCROLL_SPEED = 15;

    private static final int HEADER_HEIGHT = 28;
    private static final int FOOTER_HEIGHT = 26;

    // ═══════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════

    private enum SubPage {
        INBOX("Gang"),
        ZENTRALE("Gang-Zentrale"),
        AUFTRAEGE("Auftraege"),
        RIVALEN("Rivalen"),
        MITGLIEDER("Mitglieder"),
        PERKS("Perks & Upgrades"),
        BERICHT("Wochenbericht");

        final String title;
        SubPage(String title) { this.title = title; }
    }

    private SubPage currentPage = SubPage.INBOX;
    private boolean hasGang = false;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private int leftPos, topPos;
    private int contentTop, contentBottom;

    // Packet-Erstellungszeit (fuer Timer-Berechnung)
    private long dataReceivedAt = 0;

    // Widgets (werden pro Page neu erstellt)
    private EditBox createNameInput;
    private EditBox createTagInput;
    private EditBox inviteInput;
    private EditBox feeInput;

    // Inbox-Zeilen-Positionen fuer Klick-Erkennung
    private static final int INBOX_ROW_HEIGHT = 30;
    private static final int MEMBER_ROW_HEIGHT = 22;
    private static final int PERK_ROW_HEIGHT = 24;
    private static final int MISSION_CARD_HEIGHT = 32;

    public GangAppScreen(Screen parent) {
        super(Component.translatable("gui.app.gang.title"));
        this.parentScreen = parent;
    }

    private void navigateTo(SubPage page) {
        currentPage = page;
        scrollOffset = 0;
        rebuildWidgets();
    }

    // ═══════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = 5;
        this.contentTop = topPos + HEADER_HEIGHT;
        this.contentBottom = topPos + HEIGHT - FOOTER_HEIGHT;

        ClientGangCache.setUpdateListener(this::onCacheUpdated);
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
            currentPage = SubPage.INBOX;
            scrollOffset = 0;
        }
        dataReceivedAt = System.currentTimeMillis();
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
    // WIDGET SETUP (per Page)
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
            return;
        }

        int footerY = contentBottom + 3;

        // Zurueck-Button (alle Seiten)
        if (currentPage == SubPage.INBOX) {
            addRenderableWidget(Button.builder(Component.literal("\u00A77Zurueck"), b -> {
                if (minecraft != null) minecraft.setScreen(parentScreen);
            }).bounds(leftPos + 5, footerY, 50, 18).build());
        } else {
            addRenderableWidget(Button.builder(Component.literal("\u00A7f\u25C0 Inbox"), b -> {
                navigateTo(SubPage.INBOX);
            }).bounds(leftPos + 5, footerY, 55, 18).build());
        }

        // Refresh-Button (alle Seiten)
        addRenderableWidget(Button.builder(Component.literal("\u21BB"), b -> {
            GangNetworkHandler.sendToServer(new RequestGangDataPacket());
            GangNetworkHandler.sendToServer(new RequestGangListPacket());
        }).bounds(leftPos + WIDTH - 25, footerY, 20, 18).build());

        // Page-spezifische Widgets
        switch (currentPage) {
            case MITGLIEDER -> buildMitgliederWidgets(footerY);
            case ZENTRALE -> buildZentraleWidgets(footerY);
            default -> {}
        }
    }

    private void buildNoGangWidgets() {
        int cx = leftPos + 15;
        int cy = contentTop + 3;

        // Card 1: Gang gruenden - Inputs
        createNameInput = new EditBox(this.font, cx + 3, cy + 40, 140, 14, Component.literal("Name"));
        createNameInput.setMaxLength(20);
        createNameInput.setHint(Component.literal("Gang-Name..."));
        addRenderableWidget(createNameInput);

        createTagInput = new EditBox(this.font, cx + 3, cy + 70, 55, 14, Component.literal("Tag"));
        createTagInput.setMaxLength(5);
        createTagInput.setHint(Component.literal("TAG"));
        addRenderableWidget(createTagInput);

        addRenderableWidget(Button.builder(Component.literal("\u00A7a\u2713 Gruenden"), b -> {
            String name = createNameInput.getValue().trim();
            String tag = createTagInput.getValue().trim().toUpperCase();
            if (!name.isEmpty() && !tag.isEmpty() && tag.length() >= 2) {
                sendActionAndRefresh(GangActionPacket.create(name, tag, "RED"));
            }
        }).bounds(cx + 62, cy + 70, 80, 14).build());

        // Card 2: Gang beitreten (cy + 98)
        addRenderableWidget(Button.builder(Component.literal("\u00A7e\u2713 Einladung annehmen"), b -> {
            sendActionAndRefresh(GangActionPacket.acceptInvite(UUID.randomUUID()));
        }).bounds(cx + 3, cy + 98 + 40, 160, 14).build());

        // Zurueck-Button (Footer)
        addRenderableWidget(Button.builder(Component.literal("\u00A77\u25C0 Zurueck"), b -> {
            if (minecraft != null) minecraft.setScreen(parentScreen);
        }).bounds(leftPos + 5, topPos + HEIGHT - 24, 55, 18).build());
    }

    private void buildMitgliederWidgets(int footerY) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null) return;

        if (data.canInvite()) {
            inviteInput = new EditBox(this.font, leftPos + 63, footerY, 100, 18, Component.literal("Invite"));
            inviteInput.setMaxLength(32);
            inviteInput.setHint(Component.literal("Einladen..."));
            addRenderableWidget(inviteInput);

            addRenderableWidget(Button.builder(Component.literal("\u00A7a+"), b -> {
                if (inviteInput != null && !inviteInput.getValue().trim().isEmpty()) {
                    sendActionAndRefresh(GangActionPacket.inviteByName(inviteInput.getValue().trim()));
                    inviteInput.setValue("");
                }
            }).bounds(leftPos + 166, footerY, 18, 18).build());
        }

        addRenderableWidget(Button.builder(Component.literal("\u00A7cVerlassen"), b -> {
            sendActionAndRefresh(GangActionPacket.leave());
        }).bounds(leftPos + 187, footerY, 48, 18).build());
    }

    private void buildZentraleWidgets(int footerY) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.canDisband()) return;

        feeInput = new EditBox(this.font, leftPos + 63, footerY, 45, 18, Component.literal("Fee"));
        feeInput.setMaxLength(5);
        feeInput.setHint(Component.literal(String.valueOf(data.getWeeklyFee())));
        feeInput.setFilter(s -> s.matches("\\d*"));
        addRenderableWidget(feeInput);

        addRenderableWidget(Button.builder(Component.literal("\u00A7a\u20AC"), b -> {
            if (feeInput != null && !feeInput.getValue().trim().isEmpty()) {
                sendActionAndRefresh(GangActionPacket.setFee(Integer.parseInt(feeInput.getValue().trim())));
                feeInput.setValue("");
            }
        }).bounds(leftPos + 110, footerY, 18, 18).build());

        addRenderableWidget(Button.builder(Component.literal("\u00A74Aufloesen"), b -> {
            sendActionAndRefresh(GangActionPacket.disband());
        }).bounds(leftPos + 132, footerY, 52, 18).build());
    }

    // ═══════════════════════════════════════════════════════════
    // RENDER
    // ═══════════════════════════════════════════════════════════

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);

        // ═══ GTA PHONE FRAME ═══
        // Aeusserer Rahmen (silber-grau, "Handy-Gehaeuse")
        g.fill(leftPos - 8, topPos - 10, leftPos + WIDTH + 8, topPos + HEIGHT + 10, 0xFF3A3A3A);
        g.fill(leftPos - 7, topPos - 9, leftPos + WIDTH + 7, topPos + HEIGHT + 9, 0xFF2C2C2C);
        // Innerer Bildschirm
        g.fill(leftPos - 1, topPos - 1, leftPos + WIDTH + 1, topPos + HEIGHT + 1, 0xFF111111);
        g.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xFF1E1E1E);

        // ═══ STATUS BAR (oben, wie Smartphone) ═══
        g.fill(leftPos, topPos, leftPos + WIDTH, topPos + 10, 0xFF0D0D0D);
        g.drawString(this.font, "\u00A78\u2022\u2022\u2022\u2022", leftPos + 4, topPos + 1, 0x555555); // Signal
        g.drawString(this.font, "\u00A78\u2588\u2588\u2589", leftPos + WIDTH - 28, topPos + 1, 0x555555); // Akku

        // ═══ HEADER ═══
        if (hasGang) {
            // Farbiger Gang-Header
            SyncGangDataPacket headerData = ClientGangCache.getMyGangData();
            int headerColor = 0xFF8B0000; // Dunkelrot Standard
            if (headerData != null) {
                headerColor = getGangHeaderColor(headerData.getGangColorOrdinal());
            }
            g.fill(leftPos, topPos + 10, leftPos + WIDTH, topPos + HEADER_HEIGHT, headerColor);
            g.fill(leftPos, topPos + HEADER_HEIGHT - 1, leftPos + WIDTH, topPos + HEADER_HEIGHT, 0x44000000);

            // Page-Titel
            String title = currentPage == SubPage.INBOX
                    ? (headerData != null ? formatGangTag(headerData) + " \u00A7f\u00A7l" + headerData.getGangName() : "\u00A7f\u00A7lGang")
                    : "\u00A7f\u00A7l\u25C0 " + currentPage.title;
            g.drawCenteredString(this.font, title, leftPos + WIDTH / 2, topPos + 15, 0xFFFFFF);
        } else {
            g.fill(leftPos, topPos + 10, leftPos + WIDTH, topPos + HEADER_HEIGHT, 0xFF1A1A2E);
            g.drawCenteredString(this.font, "\u00A7f\u00A7lGang-App", leftPos + WIDTH / 2, topPos + 15, 0xFFFFFF);
        }

        if (!hasGang) {
            renderNoGangView(g);
        } else {
            renderCurrentPage(g, mouseX, mouseY);
        }

        // ═══ HOME BUTTON (unten am Phone) ═══
        int homeY = topPos + HEIGHT + 2;
        g.fill(leftPos + WIDTH / 2 - 12, homeY, leftPos + WIDTH / 2 + 12, homeY + 4, 0xFF444444);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private int getGangHeaderColor(int colorOrdinal) {
        return switch (colorOrdinal) {
            case 1 -> 0xFF993300; // GOLD/Orange
            case 2 -> 0xFF336633; // Dark Green
            case 3 -> 0xFF336666; // Teal
            case 4 -> 0xFF663333; // Dark Red
            case 5 -> 0xFF553366; // Purple
            case 6 -> 0xFF664400; // Brown/Gold
            case 9 -> 0xFF003366; // Blue
            case 10 -> 0xFF006633; // Green
            case 11 -> 0xFF006666; // Cyan
            case 12 -> 0xFF660033; // Red
            case 13 -> 0xFF660066; // Magenta
            case 14 -> 0xFF663300; // Dark Orange
            default -> 0xFF333333; // Gray
        };
    }

    private void renderCurrentPage(GuiGraphics g, int mouseX, int mouseY) {
        // Footer (dunkler Bereich mit Trennlinie)
        g.fill(leftPos, contentBottom, leftPos + WIDTH, topPos + HEIGHT, 0xFF0D0D0D);
        g.fill(leftPos, contentBottom, leftPos + WIDTH, contentBottom + 1, 0x33FFFFFF);

        // Page-Indikator (welche Seite, kleine Punkte)
        if (currentPage != SubPage.INBOX) {
            SubPage[] pages = SubPage.values();
            int dotX = leftPos + WIDTH / 2 - (pages.length * 5);
            for (SubPage page : pages) {
                int dotColor = page == currentPage ? 0xFFFFFFFF : 0x44FFFFFF;
                g.fill(dotX, contentBottom + 18, dotX + 3, contentBottom + 21, dotColor);
                dotX += 8;
            }
        }

        // Scrollbarer Content
        g.enableScissor(leftPos, contentTop, leftPos + WIDTH, contentBottom);

        int totalHeight = switch (currentPage) {
            case INBOX -> renderInbox(g);
            case ZENTRALE -> renderZentrale(g);
            case AUFTRAEGE -> renderAuftraege(g);
            case RIVALEN -> renderRivalen(g);
            case MITGLIEDER -> renderMitglieder(g);
            case PERKS -> renderPerks(g);
            case BERICHT -> renderBericht(g);
        };

        g.disableScissor();

        // Scroll
        int visibleH = contentBottom - contentTop;
        maxScroll = Math.max(0, totalHeight - visibleH);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));

        if (maxScroll > 0) {
            int barH = Math.max(15, visibleH * visibleH / (visibleH + maxScroll));
            int barY = contentTop + (scrollOffset * (visibleH - barH) / maxScroll);
            g.fill(leftPos + WIDTH - 5, contentTop, leftPos + WIDTH - 2, contentBottom, 0x33FFFFFF);
            g.fill(leftPos + WIDTH - 5, barY, leftPos + WIDTH - 2, barY + barH, 0xAAFFFFFF);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // NO GANG VIEW
    // ═══════════════════════════════════════════════════════════

    private void renderNoGangView(GuiGraphics g) {
        int cx = leftPos + 12;
        int cy = contentTop + 3;

        // ═══ CARD 1: Gang gruenden ═══
        g.fill(leftPos + 5, cy, leftPos + WIDTH - 5, cy + 92, 0xFF141A00);
        g.fill(leftPos + 5, cy, leftPos + WIDTH - 5, cy + 1, 0x33FFAA00);
        g.drawString(this.font, "\u00A76\u2302 \u00A7l\u00A76Gang gruenden", cx, cy + 4, 0xFFAA00);
        g.drawString(this.font, "\u00A78\u2514\u2500 Voraussetzung: \u00A7eLv.15 \u00A78+ \u00A7a25.000\u20AC", cx + 3, cy + 16, 0x888888);
        g.drawString(this.font, "\u00A77Name:", cx + 3, cy + 32, 0xAAAAAA);
        g.drawString(this.font, "\u00A77Tag (2-5):", cx + 3, cy + 62, 0xAAAAAA);

        cy += 98;

        // ═══ CARD 2: Gang beitreten ═══
        g.fill(leftPos + 5, cy, leftPos + WIDTH - 5, cy + 55, 0xFF001020);
        g.fill(leftPos + 5, cy, leftPos + WIDTH - 5, cy + 1, 0x3355AAFF);
        g.drawString(this.font, "\u00A7b\u263A \u00A7l\u00A7bGang beitreten", cx, cy + 4, 0x55AAFF);
        g.drawString(this.font, "\u00A78\u2514\u2500 Voraussetzung: \u00A7eLv.5 \u00A78+ \u00A7a2.500\u20AC", cx + 3, cy + 16, 0x888888);
        g.drawString(this.font, "\u00A78\u2514\u2500 Einladung eines Gang-Mitglieds noetig!", cx + 3, cy + 28, 0x888888);

        cy += 60;

        // ═══ CARD 3: Beitragssystem ═══
        g.fill(leftPos + 5, cy, leftPos + WIDTH - 5, cy + 55, 0xFF140014);
        g.fill(leftPos + 5, cy, leftPos + WIDTH - 5, cy + 1, 0x33FF55FF);
        g.drawString(this.font, "\u00A7d\u2726 \u00A7l\u00A7dWochenbeitrag-Staffelung", cx, cy + 4, 0xFF55FF);
        g.drawString(this.font, "\u00A78Boss legt Beitrag fest (0-10.000\u20AC/Wo)", cx + 3, cy + 16, 0x888888);
        g.drawString(this.font, "\u00A7cRecruit: \u00A7f100% \u00A78| \u00A7eMember: \u00A7f50%", cx + 3, cy + 28, 0xFFFFFF);
        g.drawString(this.font, "\u00A7bUnderboss: \u00A7f10% \u00A78| \u00A76Boss: \u00A7abefreit", cx + 3, cy + 40, 0xFFFFFF);

        cy += 60;

        // ═══ CARD 4: Server-Gangs ═══
        renderGangListStandalone(g, cy);
    }

    private void renderGangListStandalone(GuiGraphics g, int startY) {
        List<SyncGangListPacket.GangListEntry> gangs = ClientGangCache.getGangList();
        if (gangs.isEmpty()) return;

        int cardH = 15 + Math.min(gangs.size(), 5) * 13 + (gangs.size() > 5 ? 12 : 0);
        g.fill(leftPos + 5, startY, leftPos + WIDTH - 5, startY + cardH, 0xFF0D1117);
        g.fill(leftPos + 5, startY, leftPos + WIDTH - 5, startY + 1, 0x33FF5555);
        g.drawString(this.font, "\u00A7c\u2694 \u00A7l\u00A7cGangs auf dem Server", leftPos + 12, startY + 4, 0xFF5555);

        int y = startY + 16;
        int shown = Math.min(gangs.size(), 5);
        for (int i = 0; i < shown; i++) {
            SyncGangListPacket.GangListEntry gang = gangs.get(i);
            String tc = getColorForOrdinal(gang.colorOrdinal());
            g.drawString(this.font, "\u00A78#" + gang.rank() + " " + tc + "[" + gang.tag() + "] \u00A7f" + gang.name() +
                    " \u00A77Lv." + gang.level() + " \u00A78(" + gang.memberCount() + ")",
                    leftPos + 12, y, 0xFFFFFF);
            y += 13;
        }
        if (gangs.size() > shown) {
            g.drawString(this.font, "\u00A78...und " + (gangs.size() - shown) + " weitere", leftPos + 12, y, 0x888888);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PAGE: INBOX
    // ═══════════════════════════════════════════════════════════

    private int renderInbox(GuiGraphics g) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.hasGang()) return 0;

        int y = contentTop - scrollOffset + 2;
        int startY = y;
        int rowLeft = leftPos + 3;
        int rowRight = leftPos + WIDTH - 3;

        // ═══ QUICK-STATS BAR ═══
        if (y > contentTop - 20 && y < contentBottom + 20) {
            g.fill(rowLeft, y, rowRight, y + 18, 0xFF0D1117);
            g.fill(rowLeft, y + 18, rowRight, y + 19, 0x33FFFFFF);
            g.drawString(this.font, "\u00A76Lv." + data.getGangLevel(), rowLeft + 4, y + 5, 0xFFAA00);
            g.drawString(this.font, "\u00A7a" + data.getGangBalance() + "\u20AC", rowLeft + 45, y + 5, 0x55FF55);
            int online = (int) data.getMembers().stream().filter(SyncGangDataPacket.GangMemberInfo::online).count();
            g.drawString(this.font, "\u00A7f" + online + "/" + data.getMemberCount() + " \u00A7aonline", rowLeft + 110, y + 5, 0xFFFFFF);
            g.drawString(this.font, "\u00A76\u2605 " + GangReputation.getForLevel(data.getGangLevel()).getFormattedName(),
                    rowLeft + 175, y + 5, 0xFFFFFF);
        }
        y += 22;

        // ═══ INBOX ROWS ═══
        // Zeile 1: Gang-Zentrale
        int done = data.getCompletedMissionCount();
        int total = data.getMissions().size();
        List<SyncGangListPacket.GangListEntry> gangs = ClientGangCache.getGangList();
        int ownRank = 0;
        for (SyncGangListPacket.GangListEntry ge : gangs) {
            if (ge.name().equals(data.getGangName())) { ownRank = ge.rank(); break; }
        }
        int onlineCount = (int) data.getMembers().stream().filter(SyncGangDataPacket.GangMemberInfo::online).count();

        y = renderInboxRow(g, y, rowLeft, rowRight, "\u2302", 0xFFFF8800,
                "Gang-Zentrale", "\u00A7a" + data.getGangBalance() + "\u20AC \u00A78Kasse | Lv." + data.getGangLevel(),
                0xFF1A1400);

        // Zeile 2: Auftraege
        String missionPreview = done >= total && total > 0 ? "\u00A7aBelohnungen abholen!" :
                "\u00A7a" + done + "/" + total + " \u00A78erledigt | " + getShortTimerString(data);
        y = renderInboxRow(g, y, rowLeft, rowRight, "\u2606", 0xFFFFFF55,
                "Auftraege", missionPreview,
                0xFF141A00);

        // Zeile 3: Rivalen
        String rivalInfo = ownRank > 0 ? "\u00A7eRang #" + ownRank + " \u00A78| " + gangs.size() + " Gangs"
                : "\u00A78" + gangs.size() + " Gangs auf dem Server";
        y = renderInboxRow(g, y, rowLeft, rowRight, "\u2694", 0xFFFF5555,
                "Rivalen", rivalInfo,
                0xFF1A0000);

        // Zeile 4: Mitglieder
        y = renderInboxRow(g, y, rowLeft, rowRight, "\u263A", 0xFF55AAFF,
                "Mitglieder", "\u00A7a" + onlineCount + " online \u00A78| " + data.getMemberCount() + "/" + data.getMaxMembers(),
                0xFF001020);

        // Zeile 5: Perks
        int freePerks = data.getAvailablePerkPoints();
        String perkInfo = freePerks > 0 ? "\u00A7a" + freePerks + " Punkte verfuegbar!" :
                "\u00A78" + data.getUnlockedPerks().size() + "/" + GangLevelRequirements.getAvailablePerkPoints(data.getGangLevel()) + " freigeschaltet";
        y = renderInboxRow(g, y, rowLeft, rowRight, "\u2726", 0xFFFF55FF,
                "Perks & Upgrades", perkInfo,
                freePerks > 0 ? 0xFF1A0A1A : 0xFF140014);

        // Zeile 6: Wochenbericht
        y = renderInboxRow(g, y, rowLeft, rowRight, "\u2261", 0xFF55FF55,
                "Wochenbericht", "\u00A7a+" + data.getWeekXPGained() + " XP \u00A78| \u00A7a+" + data.getWeekMoneyEarned() + "\u20AC",
                0xFF001A00);

        return y - startY + scrollOffset;
    }

    private int renderInboxRow(GuiGraphics g, int y, int rowLeft, int rowRight,
                                String icon, int iconColor, String title, String subtitle, int bgColor) {
        if (y > contentTop - INBOX_ROW_HEIGHT && y < contentBottom + INBOX_ROW_HEIGHT) {
            // Hintergrund
            g.fill(rowLeft, y + 1, rowRight, y + INBOX_ROW_HEIGHT - 1, bgColor);
            // Trennlinie unten
            g.fill(rowLeft, y + INBOX_ROW_HEIGHT - 1, rowRight, y + INBOX_ROW_HEIGHT, 0x22FFFFFF);

            // Icon-Box (links, farbiges Quadrat)
            int iconBoxSize = INBOX_ROW_HEIGHT - 8;
            g.fill(rowLeft + 4, y + 4, rowLeft + 4 + iconBoxSize, y + 4 + iconBoxSize, iconColor & 0x44FFFFFF);
            g.fill(rowLeft + 5, y + 5, rowLeft + 3 + iconBoxSize, y + 3 + iconBoxSize, iconColor & 0x22FFFFFF);
            g.drawCenteredString(this.font, icon, rowLeft + 4 + iconBoxSize / 2, y + 7, iconColor);

            // Titel + Subtitle
            int textX = rowLeft + iconBoxSize + 10;
            g.drawString(this.font, "\u00A7f\u00A7l" + title, textX, y + 5, 0xFFFFFF);
            g.drawString(this.font, subtitle, textX, y + 17, 0xAAAAAA);

            // Pfeil rechts (>)
            g.drawString(this.font, "\u00A77\u25B6", rowRight - 12, y + 10, 0xAAAAAA);
        }
        return y + INBOX_ROW_HEIGHT;
    }

    // ═══════════════════════════════════════════════════════════
    // PAGE: ZENTRALE
    // ═══════════════════════════════════════════════════════════

    private int renderZentrale(GuiGraphics g) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.hasGang()) return 0;

        int y = contentTop - scrollOffset + 5;
        int startY = y;
        int cx = leftPos + WIDTH / 2;

        // Gang-Name + Tag
        g.drawCenteredString(this.font, formatGangTag(data) + " \u00A7f\u00A7l" + data.getGangName(), cx, y, 0xFFFFFF);
        y += 13;

        // Level + Reputation
        GangReputation rep = GangReputation.getForLevel(data.getGangLevel());
        g.drawCenteredString(this.font, "\u00A76Lv." + data.getGangLevel() + " \u00A77| " + rep.getFormattedName(), cx, y, 0xFFFFFF);
        y += 13;

        // XP-Bar
        int barX = leftPos + 15;
        int barW = WIDTH - 30;
        int filled = (int) (barW * data.getGangProgress());
        g.fill(barX, y, barX + barW, y + 7, 0xFF333333);
        if (filled > 0) {
            int barCol = data.getGangLevel() >= 25 ? 0xFFFFAA00 : (data.getGangLevel() >= 15 ? 0xFF00AAFF : 0xFF00AA00);
            g.fill(barX, y, barX + filled, y + 7, barCol);
        }
        g.drawCenteredString(this.font, String.format("%.0f%%", data.getGangProgress() * 100), cx, y - 1, 0xFFFFFF);
        y += 14;

        // Stats
        int halfW = WIDTH / 2;
        g.drawString(this.font, "\u00A78Kasse: \u00A7a" + data.getGangBalance() + "\u20AC", leftPos + 15, y, 0xFFFFFF);
        g.drawString(this.font, "\u00A78Mitglieder: \u00A7f" + data.getMemberCount() + "/" + data.getMaxMembers(), leftPos + halfW, y, 0xFFFFFF);
        y += 11;
        g.drawString(this.font, "\u00A78Territory: \u00A7f" + data.getTerritoryCount() + "/" + data.getMaxTerritory(), leftPos + 15, y, 0xFFFFFF);
        g.drawString(this.font, "\u00A78Perks: \u00A7f" + data.getUnlockedPerks().size() + "/" +
                GangLevelRequirements.getAvailablePerkPoints(data.getGangLevel()), leftPos + halfW, y, 0xFFFFFF);
        y += 14;

        // Beitrag
        int fee = data.getWeeklyFee();
        if (fee > 0) {
            int myRank = data.getMyRankPriority();
            double mult = switch (myRank) { case 4 -> 0.0; case 3 -> 0.10; case 2 -> 0.50; default -> 1.0; };
            int myFee = (int) Math.ceil(fee * mult);
            String feeText = myFee > 0
                    ? "\u00A78Beitrag: \u00A7e" + fee + "\u20AC/Wo \u00A77(dein: \u00A7a" + myFee + "\u20AC\u00A77)"
                    : "\u00A78Beitrag: \u00A7e" + fee + "\u20AC/Wo \u00A77(du: \u00A7abefreit\u00A77)";
            g.drawString(this.font, feeText, leftPos + 15, y, 0xFFFFFF);
        } else {
            g.drawString(this.font, "\u00A78Beitrag: \u00A7akein Beitrag", leftPos + 15, y, 0xFFFFFF);
        }
        y += 14;

        // Trennlinie
        g.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + 1, 0x44FFFFFF);
        y += 8;

        // Stufenrabatt-Info
        g.drawString(this.font, "\u00A76\u00A7lStufenrabatt", leftPos + 15, y, 0xFFAA00);
        y += 12;
        g.drawString(this.font, "\u00A7cRecruit: \u00A7f100% \u00A78| \u00A7eMember: \u00A7f50%", leftPos + 15, y, 0xFFFFFF);
        y += 11;
        g.drawString(this.font, "\u00A7bUnderboss: \u00A7f10% \u00A78| \u00A76Boss: \u00A7abefreit", leftPos + 15, y, 0xFFFFFF);
        y += 18;

        return y - startY + scrollOffset;
    }

    // ═══════════════════════════════════════════════════════════
    // PAGE: AUFTRAEGE
    // ═══════════════════════════════════════════════════════════

    private int renderAuftraege(GuiGraphics g) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.hasGang()) return 0;

        int y = contentTop - scrollOffset + 3;
        int startY = y;

        // Timer-Leiste
        g.fill(leftPos + 5, y, leftPos + WIDTH - 5, y + 12, 0x33FFFFFF);
        long elapsed = System.currentTimeMillis() - dataReceivedAt;
        String h = "\u00A7e\u23F1" + MissionType.formatTimer(Math.max(0, data.getHourlyResetMs() - elapsed));
        String d = "\u00A7a\u2600" + MissionType.formatTimer(Math.max(0, data.getDailyResetMs() - elapsed));
        String w = "\u00A76\u2605" + MissionType.formatTimer(Math.max(0, data.getWeeklyResetMs() - elapsed));
        g.drawString(this.font, h + " \u00A78| " + d + " \u00A78| " + w, leftPos + 10, y + 2, 0xFFFFFF);
        y += 16;

        // Stuendliche Auftraege
        y = renderMissionCategory(g, data, y, 0, "\u00A7e\u00A7l\u23F1 Stuendlich", 0x33FFAA00);
        // Taegliche Auftraege
        y = renderMissionCategory(g, data, y, 1, "\u00A7a\u00A7l\u2600 Taeglich", 0x3300AA00);
        // Woechentliche Auftraege
        y = renderMissionCategory(g, data, y, 2, "\u00A76\u00A7l\u2605 Woechentlich", 0x33FF8800);

        return y - startY + scrollOffset;
    }

    private int renderMissionCategory(GuiGraphics g, SyncGangDataPacket data, int y, int typeOrd, String header, int headerBg) {
        List<SyncGangDataPacket.MissionInfo> missions = data.getMissionsByType(typeOrd);

        if (y > contentTop - 16 && y < contentBottom + 16) {
            g.fill(leftPos + 5, y, leftPos + WIDTH - 5, y + 14, headerBg);
            g.drawString(this.font, header, leftPos + 10, y + 3, 0xFFFFFF);
        }
        y += 16;

        if (missions.isEmpty()) {
            if (y > contentTop - 12 && y < contentBottom + 12) {
                g.drawString(this.font, "\u00A78Keine Auftraege.", leftPos + 15, y, 0x888888);
            }
            y += 14;
        } else {
            for (SyncGangDataPacket.MissionInfo mi : missions) {
                if (y > contentTop - MISSION_CARD_HEIGHT && y < contentBottom + MISSION_CARD_HEIGHT) {
                    int bg = mi.claimable() ? 0x4400AA00 : (mi.completed() ? 0x3300AA00 : 0x22333333);
                    g.fill(leftPos + 8, y, leftPos + WIDTH - 8, y + MISSION_CARD_HEIGHT - 2, bg);

                    String icon = mi.claimable() ? "\u00A7a\u2713 " : (mi.completed() ? "\u00A72\u2713 " : "\u00A7e\u25B6 ");
                    g.drawString(this.font, icon + "\u00A7f" + mi.description(), leftPos + 12, y + 2, 0xFFFFFF);

                    // Fortschrittsbalken
                    int barX = leftPos + 12;
                    int barW = WIDTH - 75;
                    double prog = mi.getProgressPercent();
                    int filledW = (int) (barW * Math.min(1.0, prog));
                    g.fill(barX, y + 13, barX + barW, y + 18, 0xFF333333);
                    if (filledW > 0) {
                        g.fill(barX, y + 13, barX + filledW, y + 18, mi.completed() ? 0xFF00AA00 : 0xFFFFAA00);
                    }
                    g.drawString(this.font, "\u00A77" + mi.currentProgress() + "/" + mi.targetAmount(),
                            barX + barW + 3, y + 12, 0xAAAAAA);

                    // Belohnung
                    String reward = "\u00A7e+" + mi.xpReward() + "XP";
                    if (mi.moneyReward() > 0) reward += " \u00A7a+" + mi.moneyReward() + "\u20AC";
                    g.drawString(this.font, reward, leftPos + 12, y + 21, 0xFFFFFF);

                    if (mi.claimable()) {
                        g.drawString(this.font, "\u00A7e[Abholen!]", leftPos + WIDTH - 60, y + 21, 0xFFFF55);
                    }
                }
                y += MISSION_CARD_HEIGHT;
            }

            // Bonus-Check
            long completedCount = missions.stream().filter(SyncGangDataPacket.MissionInfo::completed).count();
            MissionType mt = MissionType.values()[typeOrd];
            if (y > contentTop - 14 && y < contentBottom + 14) {
                if (completedCount >= mt.getMissionCount()) {
                    g.drawString(this.font, "\u00A7a\u00A7l\u2713 BONUS: +" + mt.getBonusXP() + "XP +" + mt.getBonusMoney() + "\u20AC",
                            leftPos + 15, y, 0x55FF55);
                } else {
                    g.drawString(this.font, "\u00A78Alle " + mt.getMissionCount() + " = Bonus +" + mt.getBonusXP() + "XP (" +
                            completedCount + "/" + mt.getMissionCount() + ")", leftPos + 15, y, 0x888888);
                }
            }
            y += 16;
        }
        return y;
    }

    // ═══════════════════════════════════════════════════════════
    // PAGE: RIVALEN
    // ═══════════════════════════════════════════════════════════

    private int renderRivalen(GuiGraphics g) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.hasGang()) return 0;

        List<SyncGangListPacket.GangListEntry> gangs = ClientGangCache.getGangList();

        int y = contentTop - scrollOffset + 3;
        int startY = y;

        // Eigener Rang finden
        int ownRank = 0;
        for (SyncGangListPacket.GangListEntry ge : gangs) {
            if (ge.name().equals(data.getGangName())) { ownRank = ge.rank(); break; }
        }
        if (y > contentTop - 14 && y < contentBottom + 14) {
            g.drawString(this.font, "\u00A77Euer Rang: \u00A7e\u00A7l#" + ownRank, leftPos + 10, y, 0xFFFFFF);
        }
        y += 16;

        // Nach Bedrohungslevel gruppieren
        int lastThreat = -1;
        for (SyncGangListPacket.GangListEntry ge : gangs) {
            boolean isOwn = ge.name().equals(data.getGangName());

            // Bedrohungs-Header
            if (!isOwn && ge.threatLevel() != lastThreat) {
                lastThreat = ge.threatLevel();
                if (y > contentTop - 14 && y < contentBottom + 14) {
                    String tHeader = switch (ge.threatLevel()) {
                        case 2 -> "\u00A7c\u00A7l\u26A0 HOHE BEDROHUNG";
                        case 1 -> "\u00A7e\u00A7l\u25CF MITTEL";
                        default -> "\u00A7a\u00A7l\u25CB NIEDRIG";
                    };
                    g.fill(leftPos + 5, y, leftPos + WIDTH - 5, y + 1, 0x44FFFFFF);
                    y += 4;
                    g.drawString(this.font, tHeader, leftPos + 10, y, 0xFFFFFF);
                    y += 13;
                } else {
                    y += 17;
                }
            }

            if (y > contentTop - 40 && y < contentBottom + 40) {
                if (isOwn) {
                    // Eigene Gang hervorgehoben
                    g.fill(leftPos + 8, y, leftPos + WIDTH - 8, y + 14, 0x4400AAFF);
                    g.drawString(this.font, "\u00A7e#" + ge.rank() + " \u00A7b\u25B6 " +
                            getColorForOrdinal(ge.colorOrdinal()) + "[" + ge.tag() + "] \u00A7f" + ge.name() +
                            " \u00A78(EURE GANG)", leftPos + 12, y + 3, 0xFFFFFF);
                    y += 18;
                } else {
                    // Rival-Eintrag
                    int rowBg = switch (ge.threatLevel()) { case 2 -> 0x22CC3333; case 1 -> 0x22CCAA33; default -> 0x22333333; };
                    g.fill(leftPos + 8, y, leftPos + WIDTH - 8, y + 28, rowBg);

                    String arrow = switch (ge.rankChange()) { case 1 -> "\u00A7a\u2191"; case -1 -> "\u00A7c\u2193"; default -> "\u00A7e\u2192"; };
                    String tc = getColorForOrdinal(ge.colorOrdinal());
                    g.drawString(this.font, "\u00A7e#" + ge.rank() + " " + arrow + " " +
                            tc + "[" + ge.tag() + "] \u00A7f" + ge.name(),
                            leftPos + 12, y + 2, 0xFFFFFF);

                    // Stats-Zeile
                    g.drawString(this.font, "\u00A78Lv.\u00A7f" + ge.level() +
                            " \u00A78| " + ge.reputationName() +
                            " \u00A78| \u00A7f" + ge.memberCount() + " Mitgl." +
                            " \u00A78| \u00A7f" + ge.territoryCount() + " Geb.",
                            leftPos + 12, y + 14, 0x888888);

                    y += 32;
                }
            } else {
                y += isOwn ? 18 : 32;
            }
        }

        if (gangs.isEmpty()) {
            g.drawString(this.font, "\u00A78Keine anderen Gangs auf dem Server.", leftPos + 15, y, 0x888888);
            y += 14;
        }

        return y - startY + scrollOffset;
    }

    // ═══════════════════════════════════════════════════════════
    // PAGE: MITGLIEDER
    // ═══════════════════════════════════════════════════════════

    private int renderMitglieder(GuiGraphics g) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.hasGang()) return 0;

        int y = contentTop - scrollOffset + 3;
        int startY = y;

        int online = (int) data.getMembers().stream().filter(SyncGangDataPacket.GangMemberInfo::online).count();
        g.drawString(this.font, "\u00A78" + data.getMemberCount() + "/" + data.getMaxMembers() +
                " Mitglieder \u00A77| \u00A7a" + online + " online", leftPos + 10, y, 0xFFFFFF);
        y += 14;

        int myRank = data.getMyRankPriority();
        int fee = data.getWeeklyFee();

        for (SyncGangDataPacket.GangMemberInfo m : data.getMembers()) {
            if (y > contentTop - MEMBER_ROW_HEIGHT && y < contentBottom + MEMBER_ROW_HEIGHT) {
                int bg = m.online() ? 0x2200AA00 : 0x11333333;
                g.fill(leftPos + 8, y, leftPos + WIDTH - 8, y + MEMBER_ROW_HEIGHT - 2, bg);

                // Online-Dot + Rang + Name
                String dot = m.online() ? "\u00A7a\u25CF " : "\u00A78\u25CB ";
                g.drawString(this.font, dot + m.rankColor() + m.rank() + " \u00A7f" + m.name(),
                        leftPos + 12, y + 2, 0xFFFFFF);

                // Rechte Seite: Beitrag + XP + Buttons
                int rightX = leftPos + WIDTH - 12;
                boolean canAct = myRank > m.rankPriority() && m.rankPriority() < 4;

                if (canAct && myRank >= 3) {
                    g.fill(rightX - 11, y + 2, rightX, y + MEMBER_ROW_HEIGHT - 4, 0x55CC3333);
                    g.drawCenteredString(this.font, "\u00A7cX", rightX - 5, y + 3, 0xFF5555);
                    rightX -= 14;
                    if (myRank >= 4) {
                        g.fill(rightX - 11, y + 2, rightX, y + MEMBER_ROW_HEIGHT - 4, 0x5533AA33);
                        g.drawCenteredString(this.font, "\u00A7a\u2191", rightX - 5, y + 3, 0x55FF55);
                        rightX -= 14;
                    }
                }

                // XP + Beitrag
                double mult = switch (m.rankPriority()) { case 4 -> 0.0; case 3 -> 0.10; case 2 -> 0.50; default -> 1.0; };
                int memberFee = (int) Math.ceil(fee * mult);
                String info = "\u00A78+" + m.contributedXP() + "XP";
                if (fee > 0) info += " \u00A77" + memberFee + "\u20AC";
                int infoW = this.font.width(info);
                g.drawString(this.font, info, rightX - infoW - 2, y + 2, 0x888888);

                // Zeile 2 fuer Beitragsstatus
                if (fee > 0 && m.rankPriority() < 4) {
                    g.drawString(this.font, "\u00A78Beitrag/Wo: \u00A7f" + memberFee + "\u20AC",
                            leftPos + 24, y + 12, 0x888888);
                }
            }
            y += MEMBER_ROW_HEIGHT;
        }

        return y - startY + scrollOffset;
    }

    // ═══════════════════════════════════════════════════════════
    // PAGE: PERKS
    // ═══════════════════════════════════════════════════════════

    private int renderPerks(GuiGraphics g) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.hasGang()) return 0;

        int y = contentTop - scrollOffset + 3;
        int startY = y;

        int available = data.getAvailablePerkPoints();
        int used = data.getUnlockedPerks().size();
        int total = GangLevelRequirements.getAvailablePerkPoints(data.getGangLevel());
        g.drawString(this.font, "\u00A77Punkte: \u00A7a" + available + " frei \u00A77(" + used + "/" + total + ")",
                leftPos + 10, y, 0xFFFFFF);
        y += 12;

        if (data.canManagePerks() && available > 0) {
            g.drawString(this.font, "\u00A7e\u00A7oKlicke zum Freischalten", leftPos + 15, y, 0xFFFF55);
            y += 10;
        }

        GangPerk[] perks = GangPerk.values();
        GangPerk.PerkBranch lastBranch = null;

        for (GangPerk perk : perks) {
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
                boolean canUnlock = !unlocked && perk.canUnlock(data.getGangLevel()) && available > 0 && data.canManagePerks();

                int bg = unlocked ? 0x3300AA00 : (canUnlock ? 0x33FFAA00 : 0x22222222);
                g.fill(leftPos + 10, y, leftPos + WIDTH - 10, y + PERK_ROW_HEIGHT - 3, bg);

                String icon = unlocked ? "\u00A7a\u2713" : (canUnlock ? "\u00A7e\u25CB" : "\u00A78\u2718");
                String nameCol = unlocked ? "\u00A7a" : (canUnlock ? "\u00A7f" : "\u00A78");
                g.drawString(this.font, icon + " " + nameCol + perk.getDisplayName(), leftPos + 13, y + 2, 0xFFFFFF);

                String lvl = "\u00A77Lv." + perk.getRequiredGangLevel();
                g.drawString(this.font, lvl, leftPos + WIDTH - 15 - this.font.width(lvl), y + 2, 0xFFFFFF);

                String desc = (unlocked ? "\u00A72" : "\u00A78") + perk.getDescription();
                g.drawString(this.font, desc, leftPos + 13, y + 12, 0x888888);
            }
            y += PERK_ROW_HEIGHT;
        }

        return y - startY + scrollOffset;
    }

    // ═══════════════════════════════════════════════════════════
    // PAGE: WOCHENBERICHT
    // ═══════════════════════════════════════════════════════════

    private int renderBericht(GuiGraphics g) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.hasGang()) return 0;

        int y = contentTop - scrollOffset + 3;
        int startY = y;

        // Timer
        long elapsed = System.currentTimeMillis() - dataReceivedAt;
        g.drawString(this.font, "\u00A78Reset in: \u00A7f" + MissionType.formatTimer(
                Math.max(0, data.getWeeklyResetMs() - elapsed)), leftPos + 10, y, 0xFFFFFF);
        y += 16;

        // Zusammenfassung
        g.fill(leftPos + 5, y, leftPos + WIDTH - 5, y + 1, 0x44FFFFFF);
        y += 4;
        g.drawString(this.font, "\u00A7e\u00A7lZusammenfassung", leftPos + 10, y, 0xFFFF55);
        y += 14;

        g.drawString(this.font, "\u00A78Gang-XP gewonnen:   \u00A7a+" + data.getWeekXPGained(), leftPos + 15, y, 0xFFFFFF);
        y += 11;
        g.drawString(this.font, "\u00A78Einnahmen:          \u00A7a+" + data.getWeekMoneyEarned() + "\u20AC", leftPos + 15, y, 0xFFFFFF);
        y += 11;
        g.drawString(this.font, "\u00A78Beitraege kassiert: \u00A7a+" + data.getWeekFeesCollected() + "\u20AC", leftPos + 15, y, 0xFFFFFF);
        y += 16;

        // Auftrags-Statistik
        g.fill(leftPos + 5, y, leftPos + WIDTH - 5, y + 1, 0x44FFFFFF);
        y += 4;
        g.drawString(this.font, "\u00A7e\u00A7lAuftrags-Statistik", leftPos + 10, y, 0xFFFF55);
        y += 14;

        g.drawString(this.font, "\u00A78Stuendlich:   \u00A7a" + data.getWeekHourlyDone(), leftPos + 15, y, 0xFFFFFF);
        y += 11;
        g.drawString(this.font, "\u00A78Taeglich:     \u00A7a" + data.getWeekDailyDone(), leftPos + 15, y, 0xFFFFFF);
        y += 11;
        g.drawString(this.font, "\u00A78Woechentlich: \u00A7a" + data.getWeekWeeklyDone(), leftPos + 15, y, 0xFFFFFF);
        y += 11;
        int totalDone = data.getWeekHourlyDone() + data.getWeekDailyDone() + data.getWeekWeeklyDone();
        g.drawString(this.font, "\u00A78Gesamt:       \u00A7f" + totalDone + " Auftraege", leftPos + 15, y, 0xFFFFFF);
        y += 16;

        // Top-Mitglieder
        g.fill(leftPos + 5, y, leftPos + WIDTH - 5, y + 1, 0x44FFFFFF);
        y += 4;
        g.drawString(this.font, "\u00A7e\u00A7lTop-Mitglieder (XP)", leftPos + 10, y, 0xFFFF55);
        y += 14;

        List<SyncGangDataPacket.GangMemberInfo> sorted = data.getMembers().stream()
                .sorted((a, b) -> Integer.compare(b.contributedXP(), a.contributedXP()))
                .limit(3).toList();

        String[] medals = {"\u00A7e\u2655 ", "\u00A77\u2655 ", "\u00A76\u2655 "};
        for (int i = 0; i < sorted.size(); i++) {
            SyncGangDataPacket.GangMemberInfo m = sorted.get(i);
            String medal = i < medals.length ? medals[i] : "\u00A78  ";
            g.drawString(this.font, medal + "\u00A7f" + m.name() + " \u00A78+" + m.contributedXP() + "XP",
                    leftPos + 15, y, 0xFFFFFF);
            y += 11;
        }

        y += 10;
        return y - startY + scrollOffset;
    }

    // ═══════════════════════════════════════════════════════════
    // HILFSFUNKTIONEN
    // ═══════════════════════════════════════════════════════════

    private String formatGangTag(SyncGangDataPacket data) {
        int colorOrd = data.getGangColorOrdinal();
        String cc = getColorForOrdinal(colorOrd);
        String stars = GangReputation.getLevelStars(data.getGangLevel());
        if (stars.isEmpty()) return cc + "[" + data.getGangTag() + "]";
        return cc + "[" + data.getGangTag() + " " + stars + cc + "]";
    }

    private static String getColorForOrdinal(int ordinal) {
        net.minecraft.ChatFormatting cf = net.minecraft.ChatFormatting.getById(ordinal);
        return cf != null ? "\u00A7" + cf.getChar() : "\u00A7f";
    }

    private String getShortTimerString(SyncGangDataPacket data) {
        long elapsed = System.currentTimeMillis() - dataReceivedAt;
        return "\u00A7e\u23F1" + MissionType.formatTimer(Math.max(0, data.getHourlyResetMs() - elapsed)) +
                " \u00A78| \u00A7a\u2600" + MissionType.formatTimer(Math.max(0, data.getDailyResetMs() - elapsed)) +
                " \u00A78| \u00A76\u2605" + MissionType.formatTimer(Math.max(0, data.getWeeklyResetMs() - elapsed));
    }

    // ═══════════════════════════════════════════════════════════
    // INPUT HANDLING
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll > 0) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - delta * SCROLL_SPEED));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hasGang && mouseY >= contentTop && mouseY < contentBottom) {
            double relY = mouseY - contentTop + scrollOffset;

            switch (currentPage) {
                case INBOX -> { if (handleInboxClick(relY)) return true; }
                case AUFTRAEGE -> { if (handleMissionClick(relY, mouseX)) return true; }
                case MITGLIEDER -> { if (handleMemberClick(relY, mouseX)) return true; }
                case PERKS -> { if (handlePerkClick(relY, mouseX)) return true; }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleInboxClick(double relY) {
        // Quick-Stats Bar oben (22px), dann 6 Inbox-Rows
        double afterStats = relY - 22;
        if (afterStats < 0) return false;
        int rowIndex = (int) (afterStats / INBOX_ROW_HEIGHT);
        SubPage[] targets = {SubPage.ZENTRALE, SubPage.AUFTRAEGE, SubPage.RIVALEN,
                SubPage.MITGLIEDER, SubPage.PERKS, SubPage.BERICHT};
        if (rowIndex >= 0 && rowIndex < targets.length) {
            navigateTo(targets[rowIndex]);
            return true;
        }
        return false;
    }

    private boolean handleMissionClick(double relY, double mouseX) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null) return false;

        // Einfache Klick-Erkennung: Klick auf eine claimable Mission
        double y = 19; // Timer-Leiste
        for (int typeOrd = 0; typeOrd < 3; typeOrd++) {
            y += 16; // Kategorie-Header
            List<SyncGangDataPacket.MissionInfo> missions = data.getMissionsByType(typeOrd);
            if (missions.isEmpty()) {
                y += 14;
            } else {
                for (SyncGangDataPacket.MissionInfo mi : missions) {
                    if (relY >= y && relY < y + MISSION_CARD_HEIGHT && mi.claimable()) {
                        sendActionAndRefresh(GangActionPacket.claimMission(mi.missionId()));
                        return true;
                    }
                    y += MISSION_CARD_HEIGHT;
                }
                y += 16; // Bonus-Zeile
            }
        }
        return false;
    }

    private boolean handleMemberClick(double relY, double mouseX) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null) return false;

        int myRank = data.getMyRankPriority();
        if (myRank < 3) return false;

        double memberStart = 14; // Header
        int memberIndex = (int) ((relY - memberStart) / MEMBER_ROW_HEIGHT);
        List<SyncGangDataPacket.GangMemberInfo> members = data.getMembers();
        if (memberIndex < 0 || memberIndex >= members.size()) return false;

        SyncGangDataPacket.GangMemberInfo member = members.get(memberIndex);
        if (myRank <= member.rankPriority() || member.rankPriority() >= 4) return false;

        int rightX = leftPos + WIDTH - 12;

        if (mouseX >= rightX - 11 && mouseX <= rightX) {
            sendActionAndRefresh(GangActionPacket.kick(member.uuid()));
            return true;
        }
        if (myRank >= 4 && mouseX >= rightX - 25 && mouseX <= rightX - 14) {
            GangRank nextRank = switch (member.rankPriority()) {
                case 1 -> GangRank.MEMBER;
                case 2 -> GangRank.UNDERBOSS;
                default -> null;
            };
            if (nextRank != null) {
                sendActionAndRefresh(GangActionPacket.promote(member.uuid(), nextRank));
                return true;
            }
        }
        return false;
    }

    private boolean handlePerkClick(double relY, double mouseX) {
        SyncGangDataPacket data = ClientGangCache.getMyGangData();
        if (data == null || !data.canManagePerks() || data.getAvailablePerkPoints() <= 0) return false;
        if (mouseX < leftPos + 10 || mouseX > leftPos + WIDTH - 10) return false;

        double perkStart = 12 + (data.canManagePerks() && data.getAvailablePerkPoints() > 0 ? 10 : 0);
        double localY = relY - perkStart;
        if (localY < 0) return false;

        GangPerk[] perks = GangPerk.values();
        GangPerk.PerkBranch lastBranch = null;
        int yOffset = 0;

        for (GangPerk perk : perks) {
            if (perk.getBranch() != lastBranch) {
                lastBranch = perk.getBranch();
                yOffset += 12;
            }
            if (localY >= yOffset && localY < yOffset + PERK_ROW_HEIGHT) {
                if (!data.hasPerk(perk) && perk.canUnlock(data.getGangLevel())) {
                    sendActionAndRefresh(GangActionPacket.unlockPerk(perk.name()));
                    return true;
                }
            }
            yOffset += PERK_ROW_HEIGHT;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (getFocused() instanceof EditBox) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == 69) return true; // Block E-key
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
