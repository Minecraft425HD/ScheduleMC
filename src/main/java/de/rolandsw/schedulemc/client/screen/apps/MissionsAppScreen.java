package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.mission.MissionCategory;
import de.rolandsw.schedulemc.mission.MissionStatus;
import de.rolandsw.schedulemc.mission.client.ClientMissionCache;
import de.rolandsw.schedulemc.mission.client.PlayerMissionDto;
import de.rolandsw.schedulemc.mission.network.MissionActionPacket;
import de.rolandsw.schedulemc.mission.network.MissionNetworkHandler;
import de.rolandsw.schedulemc.mission.network.RequestMissionsPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Smartphone-App: Hauptmissionen & Nebenmissionen.
 *
 * Layout:
 *   - Dunkler App-Hintergrund
 *   - Zwei Tab-Buttons (Haupt / Neben)
 *   - Scrollbare Mission-Liste für den aktiven Tab
 *   - Pro Eintrag: Titel, Kurzbeschreibung, Fortschrittsbalken, Status-Badge, Aktions-Button
 */
@OnlyIn(Dist.CLIENT)
public class MissionsAppScreen extends Screen {

    // Layout
    private static final int APP_WIDTH = 220;
    private static final int APP_HEIGHT = 260;
    private static final int MARGIN_TOP = 5;
    private static final int HEADER_HEIGHT = 30;
    private static final int TAB_HEIGHT = 22;
    private static final int ENTRY_HEIGHT = 56;
    private static final int ENTRY_PADDING = 4;
    private static final int SCROLL_SPEED = 10;
    private static final int VISIBLE_AREA_HEIGHT = 160;

    private final Screen previousScreen;
    private MissionCategory activeTab = MissionCategory.HAUPT;

    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;

    // Cached for performance
    private String cachedTitle;
    private String cachedTabHaupt;
    private String cachedTabNeben;
    private String cachedEmpty;

    public MissionsAppScreen(Screen previousScreen) {
        super(Component.translatable("gui.smartphone.app_missions"));
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - APP_WIDTH) / 2;
        this.topPos = MARGIN_TOP;

        cachedTitle = Component.translatable("gui.smartphone.app_missions").getString();
        cachedTabHaupt = Component.translatable("gui.missions.tab_haupt").getString();
        cachedTabNeben = Component.translatable("gui.missions.tab_neben").getString();
        cachedEmpty = Component.translatable("gui.missions.empty").getString();

        // Schliessen-Button
        addRenderableWidget(Button.builder(Component.literal("X"), btn -> this.onClose())
            .bounds(leftPos + APP_WIDTH - 28, topPos + 6, 20, 20)
            .build());

        // Tab-Buttons
        int tabY = topPos + HEADER_HEIGHT + 2;
        int tabWidth = (APP_WIDTH - 12) / 2;

        addRenderableWidget(Button.builder(Component.translatable("gui.missions.tab_haupt"), btn -> {
            activeTab = MissionCategory.HAUPT;
            scrollOffset = 0;
        }).bounds(leftPos + 6, tabY, tabWidth, TAB_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.missions.tab_neben"), btn -> {
            activeTab = MissionCategory.NEBEN;
            scrollOffset = 0;
        }).bounds(leftPos + 6 + tabWidth + 2, tabY, tabWidth, TAB_HEIGHT).build());

        // Zurueck-Button
        int backY = topPos + HEADER_HEIGHT + TAB_HEIGHT + VISIBLE_AREA_HEIGHT + 16;
        addRenderableWidget(Button.builder(Component.translatable("gui.common.back"), btn -> this.onClose())
            .bounds(leftPos + (APP_WIDTH - 80) / 2, backY, 80, 20)
            .build());

        // Missionen vom Server anfragen
        MissionNetworkHandler.sendToServer(new RequestMissionsPacket());
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);

        // App-Hintergrund
        gui.fill(leftPos - 5, topPos - 5, leftPos + APP_WIDTH + 5, topPos + APP_HEIGHT + 5, 0xFF1C1C1C);
        gui.fill(leftPos, topPos, leftPos + APP_WIDTH, topPos + APP_HEIGHT, 0xFF2A2A2A);

        // Header
        gui.fill(leftPos, topPos, leftPos + APP_WIDTH, topPos + HEADER_HEIGHT, 0xFF1A1A1A);
        gui.drawCenteredString(this.font, "§f" + cachedTitle,
            leftPos + APP_WIDTH / 2, topPos + 11, 0xFFFFFF);

        // Tab-Hervorhebung
        int tabY = topPos + HEADER_HEIGHT + 2;
        int tabWidth = (APP_WIDTH - 12) / 2;
        if (activeTab == MissionCategory.HAUPT) {
            gui.fill(leftPos + 6, tabY + TAB_HEIGHT - 2,
                leftPos + 6 + tabWidth, tabY + TAB_HEIGHT, 0xFF5588FF);
        } else {
            gui.fill(leftPos + 6 + tabWidth + 2, tabY + TAB_HEIGHT - 2,
                leftPos + 6 + tabWidth + 2 + tabWidth, tabY + TAB_HEIGHT, 0xFF5588FF);
        }

        // Missions-Liste
        int listY = topPos + HEADER_HEIGHT + TAB_HEIGHT + 6;
        renderMissionList(gui, listY, mouseX, mouseY, partialTick);

        super.render(gui, mouseX, mouseY, partialTick);
    }

    private void renderMissionList(GuiGraphics gui, int listStartY, int mouseX, int mouseY, float partialTick) {
        List<PlayerMissionDto> missions = ClientMissionCache.getByCategory(activeTab);

        // Scrollbar-Berechnung
        int totalHeight = missions.size() * (ENTRY_HEIGHT + ENTRY_PADDING);
        maxScrollOffset = Math.max(0, totalHeight - VISIBLE_AREA_HEIGHT);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

        // Scissor aktivieren
        gui.enableScissor(leftPos + 4, listStartY, leftPos + APP_WIDTH - 4, listStartY + VISIBLE_AREA_HEIGHT);

        if (missions.isEmpty()) {
            gui.drawCenteredString(this.font, "§7" + cachedEmpty,
                leftPos + APP_WIDTH / 2, listStartY + VISIBLE_AREA_HEIGHT / 2 - 4, 0xAAAAAA);
        } else {
            int y = listStartY - scrollOffset;
            for (PlayerMissionDto mission : missions) {
                renderMissionEntry(gui, leftPos + 4, y, APP_WIDTH - 8, mission, mouseX, mouseY);
                y += ENTRY_HEIGHT + ENTRY_PADDING;
            }
        }

        gui.disableScissor();

        // Scrollbar
        if (maxScrollOffset > 0) {
            int sbX = leftPos + APP_WIDTH - 8;
            int sbHeight = VISIBLE_AREA_HEIGHT;
            gui.fill(sbX, listStartY, sbX + 4, listStartY + sbHeight, 0xFF1A1A1A);
            int handleHeight = Math.max(16, sbHeight * VISIBLE_AREA_HEIGHT / Math.max(1, totalHeight));
            int handleY = listStartY + (int) ((float) scrollOffset / maxScrollOffset * (sbHeight - handleHeight));
            gui.fill(sbX, handleY, sbX + 4, handleY + handleHeight, 0xFF888888);
        }
    }

    private void renderMissionEntry(GuiGraphics gui, int x, int y, int width, PlayerMissionDto mission,
                                    int mouseX, int mouseY) {
        // Hintergrund
        int bgColor = getStatusColor(mission.getStatus(), 0x33);
        gui.fill(x, y, x + width, y + ENTRY_HEIGHT, bgColor);

        // Titel
        String title = "§f" + truncate(mission.getTitle(), width - 60, this.font);
        gui.drawString(this.font, title, x + 4, y + 4, 0xFFFFFF);

        // Status-Badge
        String badge = getStatusBadge(mission.getStatus());
        int badgeColor = getStatusBadgeColor(mission.getStatus());
        int badgeWidth = this.font.width(badge);
        gui.drawString(this.font, badge, x + width - badgeWidth - 4, y + 4, badgeColor);

        // Beschreibung (kurz)
        String desc = "§7" + truncate(mission.getDescription(), width - 8, this.font);
        gui.drawString(this.font, desc, x + 4, y + 16, 0xAAAAAA);

        // NPC-Geber (wenn vorhanden)
        if (!mission.getNpcGiverName().isEmpty()) {
            String npc = "§8" + mission.getNpcGiverName();
            gui.drawString(this.font, npc, x + 4, y + 26, 0x888888);
        }

        // Fortschrittsbalken
        if (mission.getTargetAmount() > 1) {
            int barY = y + 38;
            int barWidth = width - 8;
            gui.fill(x + 4, barY, x + 4 + barWidth, barY + 4, 0xFF444444);
            int fillWidth = (int) (barWidth * mission.getProgressPercent());
            if (fillWidth > 0) {
                gui.fill(x + 4, barY, x + 4 + fillWidth, barY + 4, 0xFF55AA55);
            }
            String progressText = mission.getCurrentProgress() + "/" + mission.getTargetAmount();
            gui.drawString(this.font, "§7" + progressText, x + 4, barY + 6, 0xAAAAAA);
        }

        // Aktions-Button (nur wenn in sichtbarem Bereich)
        int listStartY = topPos + HEADER_HEIGHT + TAB_HEIGHT + 6;
        if (y >= listStartY - ENTRY_HEIGHT && y <= listStartY + VISIBLE_AREA_HEIGHT) {
            renderActionButton(gui, x + width - 54, y + ENTRY_HEIGHT - 18, 50, 14, mission, mouseX, mouseY);
        }
    }

    private void renderActionButton(GuiGraphics gui, int x, int y, int w, int h,
                                    PlayerMissionDto mission, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        switch (mission.getStatus()) {
            case AVAILABLE -> {
                int bg = hovered ? 0xFF336633 : 0xFF224422;
                gui.fill(x, y, x + w, y + h, bg);
                String label = Component.translatable("gui.missions.action_accept").getString();
                gui.drawCenteredString(this.font, "§a" + truncate(label, w - 4, this.font),
                    x + w / 2, y + 3, 0xFFFFFF);
            }
            case ACTIVE -> {
                int bg = hovered ? 0xFF663322 : 0xFF442211;
                gui.fill(x, y, x + w, y + h, bg);
                String label = Component.translatable("gui.missions.action_abandon").getString();
                gui.drawCenteredString(this.font, "§c" + truncate(label, w - 4, this.font),
                    x + w / 2, y + 3, 0xFFFFFF);
            }
            case COMPLETED -> {
                int bg = hovered ? 0xFF886600 : 0xFF664400;
                gui.fill(x, y, x + w, y + h, bg);
                String label = Component.translatable("gui.missions.action_claim").getString();
                gui.drawCenteredString(this.font, "§e" + truncate(label, w - 4, this.font),
                    x + w / 2, y + 3, 0xFFFFFF);
            }
            case CLAIMED -> {
                gui.fill(x, y, x + w, y + h, 0xFF333333);
                gui.drawCenteredString(this.font, "§8✓", x + w / 2, y + 3, 0x888888);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int listStartY = topPos + HEADER_HEIGHT + TAB_HEIGHT + 6;
            List<PlayerMissionDto> missions = ClientMissionCache.getByCategory(activeTab);
            int y = listStartY - scrollOffset;

            for (PlayerMissionDto mission : missions) {
                int entryX = leftPos + 4;
                int entryWidth = APP_WIDTH - 8;
                int btnX = entryX + entryWidth - 54;
                int btnY = y + ENTRY_HEIGHT - 18;

                if (mouseX >= btnX && mouseX <= btnX + 50
                        && mouseY >= btnY && mouseY <= btnY + 14
                        && mouseY >= listStartY && mouseY <= listStartY + VISIBLE_AREA_HEIGHT) {
                    handleActionClick(mission);
                    return true;
                }
                y += ENTRY_HEIGHT + ENTRY_PADDING;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleActionClick(PlayerMissionDto mission) {
        switch (mission.getStatus()) {
            case AVAILABLE -> MissionNetworkHandler.sendToServer(
                new MissionActionPacket(MissionActionPacket.Action.ACCEPT, mission.getDefinitionId()));
            case ACTIVE -> MissionNetworkHandler.sendToServer(
                new MissionActionPacket(MissionActionPacket.Action.ABANDON, mission.getMissionId()));
            case COMPLETED -> MissionNetworkHandler.sendToServer(
                new MissionActionPacket(MissionActionPacket.Action.CLAIM, mission.getMissionId()));
            case CLAIMED -> { /* nichts */ }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int listStartY = topPos + HEADER_HEIGHT + TAB_HEIGHT + 6;
        if (mouseX >= leftPos && mouseX <= leftPos + APP_WIDTH
                && mouseY >= listStartY && mouseY <= listStartY + VISIBLE_AREA_HEIGHT) {
            scrollOffset = Math.max(0, Math.min(maxScrollOffset,
                scrollOffset - (int) (delta * SCROLL_SPEED)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 69) return true; // Block E
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(previousScreen);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ── Hilfsmethoden ────────────────────────────────────────────────────

    private String getStatusBadge(MissionStatus status) {
        return switch (status) {
            case AVAILABLE -> "○ Verfügbar";
            case ACTIVE -> "● Aktiv";
            case COMPLETED -> "★ Abgeschlossen";
            case CLAIMED -> "✓ Erhalten";
        };
    }

    private int getStatusBadgeColor(MissionStatus status) {
        return switch (status) {
            case AVAILABLE -> 0xAAAAAA;
            case ACTIVE -> 0x55AAFF;
            case COMPLETED -> 0xFFAA00;
            case CLAIMED -> 0x55FF55;
        };
    }

    private int getStatusColor(MissionStatus status, int alpha) {
        int a = alpha << 24;
        return switch (status) {
            case AVAILABLE -> a | 0x333333;
            case ACTIVE -> a | 0x223355;
            case COMPLETED -> a | 0x554422;
            case CLAIMED -> a | 0x224422;
        };
    }

    private String truncate(String text, int maxWidth, net.minecraft.client.gui.Font font) {
        if (font.width(text) <= maxWidth) return text;
        for (int i = text.length() - 1; i > 0; i--) {
            String test = text.substring(0, i) + "...";
            if (font.width(test) <= maxWidth) return test;
        }
        return "...";
    }
}
