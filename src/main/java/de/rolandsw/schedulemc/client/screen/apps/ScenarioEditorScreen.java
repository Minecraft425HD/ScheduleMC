package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.gang.network.OpenScenarioEditorPacket;
import de.rolandsw.schedulemc.gang.scenario.*;
import de.rolandsw.schedulemc.gang.scenario.ObjectiveType.ParamWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

/**
 * Visueller Szenario-Editor im Blockly-Stil mit Dropdown-Auswahl fuer
 * NPCs, Grundstuecke, Berufe und vollstaendiger Parameter-Bearbeitung.
 *
 * Layout:
 * ┌──────────────────────────────────────────────────────────────┐
 * │ [Oeffnen][Speichern][Vorlagen][Aktive]  [Typ:▼][★★★][Neu][X]│
 * ├──────────┬───────────────────────────────┬───────────────────┤
 * │ PALETTE  │      EDITOR CANVAS            │  PROPERTIES       │
 * │ Baust.   │  Bloecke platzieren           │  Parameter des    │
 * │ nach     │  & verbinden                  │  selekt. Blocks   │
 * │ Kategorie│                               │  mit Dropdowns    │
 * ├──────────┴───────────────────────────────┴───────────────────┤
 * │ Name: [.......] │ 5 Phasen │ ★★★☆☆ │ DAILY │ Min-Lvl: 3    │
 * └──────────────────────────────────────────────────────────────┘
 */
public class ScenarioEditorScreen extends Screen {

    // ═══════════════════════════════════════════════════════════
    // LAYOUT
    // ═══════════════════════════════════════════════════════════
    private static final int TOOLBAR_H = 22;
    private static final int PALETTE_W = 100;
    private static final int PROPS_W = 120;
    private static final int STATUS_H = 16;
    private static final int BLOCK_W = 90;
    private static final int BLOCK_H = 32;
    private static final int CONNECTOR_R = 4;
    private static final int GRID = 5;

    // ═══════════════════════════════════════════════════════════
    // FARBEN
    // ═══════════════════════════════════════════════════════════
    private static final int C_BG = 0xFF1A1A2E;
    private static final int C_TOOLBAR = 0xFF16213E;
    private static final int C_PALETTE = 0xFF0F3460;
    private static final int C_CANVAS = 0xFF0A0A1A;
    private static final int C_GRID = 0xFF1A1A30;
    private static final int C_STATUS = 0xFF16213E;
    private static final int C_SEL = 0xFFE94560;
    private static final int C_CONN = 0xFF53E0BC;
    private static final int C_ARROW = 0xBBE94560;
    private static final int C_PROPS = 0xFF0F3460;
    private static final int C_DD_BG = 0xEE16213E;
    private static final int C_DD_HOVER = 0x33FFFFFF;

    // Statische Dropdown-Werte
    private static final String[] NPC_TYPES = {"BEWOHNER", "VERKAEUFER", "POLIZEI", "BANK", "ABSCHLEPPER", "DRUG_DEALER"};
    private static final String[] MERCHANT_CATS = {"BAUMARKT", "WAFFENHAENDLER", "TANKSTELLE", "LEBENSMITTEL",
            "PERSONALMANAGEMENT", "ILLEGALER_HAENDLER", "AUTOHAENDLER"};
    private static final String[] BANK_CATS = {"BANKER", "BOERSE", "KREDITBERATER"};
    private static final String[] SERVICE_CATS = {"ABSCHLEPPDIENST", "PANNENHILFE", "TAXI", "NOTDIENST"};
    private static final String[] DIFFICULTIES = {"1 - Leicht", "2 - Normal", "3 - Mittel", "4 - Schwer", "5 - Extrem"};
    private static final String[] ENTITY_TYPES = {"zombie", "skeleton", "spider", "creeper", "enderman",
            "witch", "pillager", "vindicator", "ravager", "phantom"};
    private static final String[] VEHICLE_TYPES = {"AUTO", "MOTORRAD", "LKW", "BUS", "BOOT", "HELIKOPTER"};
    private static final String[] OUTFIT_TYPES = {"POLIZIST", "ARZT", "BAUARBEITER", "GESCHAEFTSMANN",
            "LIEFERANT", "MECHANIKER"};
    private static final String[] METHOD_TYPES = {"ABLENKUNG", "BESTECHUNG", "FALSCHALARM", "GERAEUSCH", "KOEDER"};
    private static final String[] EVENT_TYPES = {"POLIZEI_KONTROLLE", "UEBERFALL", "UNFALL", "BRAND",
            "STROMAUSFALL", "UNWETTER"};
    private static final String[] COLOR_TYPES = {"WEISS", "ROT", "GRUEN", "BLAU", "GELB", "LILA", "ORANGE", "GRAU"};
    private static final String[] MISSION_TYPES = {"HOURLY", "DAILY", "WEEKLY"};
    private static final String[] MISSION_TYPE_LABELS = {"Stuendlich", "Taeglich", "Woechentlich"};

    // ═══════════════════════════════════════════════════════════
    // ZUSTAND
    // ═══════════════════════════════════════════════════════════
    private MissionScenario currentScenario;
    private List<MissionScenario> allScenarios;

    // Server-Daten fuer Dropdowns
    private final List<String> serverNpcNames;
    private final List<OpenScenarioEditorPacket.PlotInfo> serverPlots;
    private final List<OpenScenarioEditorPacket.LockInfo> serverLocks;

    // Canvas
    private int scrollX = 0, scrollY = 0;

    // Selektion & Drag
    private String selectedBlockId = null;
    private boolean dragging = false;
    private int dragOX, dragOY;

    // Verbindungsmodus
    private boolean connecting = false;
    private String connectSrcId = null;

    // Palette
    private int paletteScroll = 0;
    private final Map<ObjectiveType.Category, Boolean> catExpanded = new LinkedHashMap<>();

    // Toolbar-Dropdowns
    private enum ToolbarDD { NONE, OPEN, TEMPLATES, ACTIVE, MISSION_TYPE }
    private ToolbarDD toolbarDD = ToolbarDD.NONE;

    // Property-Dropdown (fuer ein bestimmtes Param-Feld)
    private String activeParamDD = null; // ParamDef.key des offenen Dropdowns
    private int paramDDScroll = 0;

    // Property EditBoxen
    private final List<EditBox> propFields = new ArrayList<>();
    private final List<String> propKeys = new ArrayList<>();
    private EditBox nameField;

    // Hovered Block (fuer Tooltip)
    private String hoveredBlockId = null;

    // ═══════════════════════════════════════════════════════════
    // KONSTRUKTOR
    // ═══════════════════════════════════════════════════════════

    public ScenarioEditorScreen(List<MissionScenario> scenarios,
                                List<String> npcNames,
                                List<OpenScenarioEditorPacket.PlotInfo> plots) {
        this(scenarios, npcNames, plots, new ArrayList<>());
    }

    public ScenarioEditorScreen(List<MissionScenario> scenarios,
                                List<String> npcNames,
                                List<OpenScenarioEditorPacket.PlotInfo> plots,
                                List<OpenScenarioEditorPacket.LockInfo> locks) {
        super(Component.literal("Szenario-Editor"));
        this.allScenarios = scenarios != null ? new ArrayList<>(scenarios) : new ArrayList<>();
        this.serverNpcNames = npcNames != null ? npcNames : new ArrayList<>();
        this.serverPlots = plots != null ? plots : new ArrayList<>();
        this.serverLocks = locks != null ? locks : new ArrayList<>();
        this.currentScenario = new MissionScenario();

        for (ObjectiveType.Category cat : ObjectiveType.Category.values()) {
            if (cat != ObjectiveType.Category.SPEZIAL) catExpanded.put(cat, false);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // INIT
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void init() {
        super.init();
        nameField = new EditBox(this.font, 44, this.height - STATUS_H + 3, 90, 10, Component.literal("Name"));
        nameField.setMaxLength(30);
        nameField.setValue(currentScenario.getName());
        nameField.setResponder(s -> currentScenario.setName(s));
        nameField.setBordered(false);
        nameField.setTextColor(0xFFFFFF);
        addRenderableWidget(nameField);
        rebuildProps();
    }

    private void rebuildProps() {
        for (EditBox b : propFields) removeWidget(b);
        propFields.clear();
        propKeys.clear();

        if (selectedBlockId == null) return;
        ScenarioObjective obj = currentScenario.getObjective(selectedBlockId);
        if (obj == null) return;

        int px = propsLeft() + 4;
        int pw = PROPS_W - 10;
        int py = TOOLBAR_H + 24;

        for (ObjectiveType.ParamDef def : obj.getType().getParamDefs()) {
            boolean isDropdown = def.widget().name().startsWith("DROPDOWN");
            if (!isDropdown) {
                // EditBox fuer TEXT, NUMBER, COORD
                EditBox box = new EditBox(this.font, px, py + 10, pw, 10, Component.literal(def.label()));
                box.setMaxLength(40);
                box.setValue(obj.getParam(def.key()));
                box.setBordered(false);
                box.setTextColor(getParamColor(def));
                final String key = def.key();
                box.setResponder(val -> obj.setParam(key, val));
                addWidget(box);
                propFields.add(box);
                propKeys.add(def.key());
            }
            py += 22;
        }
    }

    private int getParamColor(ObjectiveType.ParamDef def) {
        return switch (def.widget()) {
            case COORD -> def.key().endsWith("x") || def.key().equals("x") ? 0xFFFF6666 :
                    def.key().endsWith("y") || def.key().equals("y") ? 0xFF66FF66 : 0xFF6666FF;
            case NUMBER -> 0xFFFFCC44;
            default -> 0xFFFFFFFF;
        };
    }

    private int propsLeft() {
        return this.width - PROPS_W;
    }

    // ═══════════════════════════════════════════════════════════
    // RENDER
    // ═══════════════════════════════════════════════════════════

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(0, 0, this.width, this.height, C_BG);

        // Hovered Block finden
        hoveredBlockId = null;
        if (mx >= PALETTE_W && mx < propsLeft() && my >= TOOLBAR_H && my < this.height - STATUS_H) {
            int cL = PALETTE_W, cT = TOOLBAR_H;
            List<ScenarioObjective> objs = currentScenario.getObjectives();
            for (int i = objs.size() - 1; i >= 0; i--) {
                ScenarioObjective o = objs.get(i);
                int bx = cL + o.getEditorX() - scrollX, by = cT + o.getEditorY() - scrollY;
                if (mx >= bx && mx < bx + BLOCK_W && my >= by && my < by + BLOCK_H) {
                    hoveredBlockId = o.getId();
                    break;
                }
            }
        }

        renderToolbar(g, mx, my);
        renderPalette(g, mx, my);
        renderCanvas(g, mx, my);
        renderPropsPanel(g, mx, my);
        renderStatusBar(g, mx, my);
        renderToolbarDropdowns(g, mx, my);
        renderParamDropdown(g, mx, my);

        // Connecting-Linie
        if (connecting && connectSrcId != null) {
            ScenarioObjective src = currentScenario.getObjective(connectSrcId);
            if (src != null) {
                int sx = PALETTE_W + src.getEditorX() - scrollX + BLOCK_W / 2;
                int sy = TOOLBAR_H + src.getEditorY() - scrollY + BLOCK_H;
                drawDashedLine(g, sx, sy, mx, my, C_CONN);
            }
        }

        // Tooltip
        if (hoveredBlockId != null && !dragging && activeParamDD == null && toolbarDD == ToolbarDD.NONE) {
            renderBlockTooltip(g, mx, my);
        }

        super.render(g, mx, my, pt);
    }

    // ─── TOOLBAR ───
    private void renderToolbar(GuiGraphics g, int mx, int my) {
        g.fill(0, 0, this.width, TOOLBAR_H, C_TOOLBAR);
        g.fill(0, TOOLBAR_H - 1, this.width, TOOLBAR_H, 0xFF2A2A4A);

        int x = 4;
        x = tbBtn(g, x, "Oeffnen", mx, my, 0xFF3498DB);
        x = tbBtn(g, x, "Speichern", mx, my, 0xFF2ECC71);
        x = tbBtn(g, x, "Vorlagen", mx, my, 0xFFF39C12);
        x = tbBtn(g, x, "Aktive", mx, my, 0xFF9B59B6);

        // Rechts
        int rx = this.width - 4;
        rx = tbBtnR(g, rx, "X", mx, my, 0xFFE74C3C);
        rx = tbBtnR(g, rx, "Neu", mx, my, 0xFF1ABC9C);
        tbBtnR(g, rx, "Loeschen", mx, my, 0xFF7F8C8D);

        g.drawString(this.font, "\u00A78Szenario-Editor", this.width / 2 - 30, 7, 0x666666);
    }

    private int tbBtn(GuiGraphics g, int x, String l, int mx, int my, int c) {
        int w = this.font.width(l) + 8;
        boolean h = mx >= x && mx < x + w && my >= 2 && my < TOOLBAR_H - 2;
        g.fill(x, 3, x + w, TOOLBAR_H - 3, h ? c : (c & 0x88FFFFFF));
        g.drawString(this.font, l, x + 4, 6, 0xFFFFFF);
        return x + w + 3;
    }

    private int tbBtnR(GuiGraphics g, int re, String l, int mx, int my, int c) {
        int w = this.font.width(l) + 8;
        int x = re - w;
        boolean h = mx >= x && mx < x + w && my >= 2 && my < TOOLBAR_H - 2;
        g.fill(x, 3, x + w, TOOLBAR_H - 3, h ? c : (c & 0x88FFFFFF));
        g.drawString(this.font, l, x + 4, 6, 0xFFFFFF);
        return x - 3;
    }

    // ─── PALETTE ───
    private void renderPalette(GuiGraphics g, int mx, int my) {
        int pB = this.height - STATUS_H;
        g.fill(0, TOOLBAR_H, PALETTE_W, pB, C_PALETTE);
        g.fill(PALETTE_W - 1, TOOLBAR_H, PALETTE_W, pB, 0xFF2A2A4A);
        g.drawString(this.font, "\u00A7lBausteine", 4, TOOLBAR_H + 3, 0xCCCCCC);

        g.enableScissor(0, TOOLBAR_H + 13, PALETTE_W - 1, pB);
        int y = TOOLBAR_H + 14 - paletteScroll;
        for (var entry : catExpanded.entrySet()) {
            var cat = entry.getKey();
            boolean exp = entry.getValue();
            String arrow = exp ? "\u25BE " : "\u25B8 ";
            boolean cH = mx >= 2 && mx < PALETTE_W - 2 && my >= y && my < y + 11;
            if (cH) g.fill(2, y, PALETTE_W - 2, y + 11, 0x22FFFFFF);
            g.drawString(this.font, arrow + cat.getDisplayName(), 4, y + 1, cat.getColor() | 0xFF000000);
            y += 12;
            if (exp) {
                for (ObjectiveType t : ObjectiveType.values()) {
                    if (t.getCategory() != cat) continue;
                    boolean bH = mx >= 4 && mx < PALETTE_W - 4 && my >= y && my < y + 14;
                    g.fill(4, y, PALETTE_W - 4, y + 14, bH ? (t.getColor() & 0x44FFFFFF) : (t.getColor() & 0x22FFFFFF));
                    g.fill(4, y, 6, y + 14, t.getColor());
                    g.drawString(this.font, t.getIcon(), 9, y + 3, t.getColor());
                    g.drawString(this.font, "\u00A7f" + t.getDisplayName(), 18, y + 3, 0xDDDDDD);
                    y += 15;
                }
            }
            y += 3;
        }
        g.disableScissor();
    }

    // ─── CANVAS ───
    private void renderCanvas(GuiGraphics g, int mx, int my) {
        int cL = PALETTE_W, cT = TOOLBAR_H, cR = propsLeft(), cB = this.height - STATUS_H;
        g.fill(cL, cT, cR, cB, C_CANVAS);

        g.enableScissor(cL, cT, cR, cB);

        // Raster
        int gs = 20;
        for (int gx = cL - (scrollX % gs); gx < cR; gx += gs)
            for (int gy = cT - (scrollY % gs); gy < cB; gy += gs)
                g.fill(gx, gy, gx + 1, gy + 1, C_GRID);

        // Pfeile
        for (ScenarioObjective obj : currentScenario.getObjectives()) {
            if (obj.getNextObjectiveId() != null) {
                ScenarioObjective next = currentScenario.getObjective(obj.getNextObjectiveId());
                if (next != null) {
                    int fx = cL + obj.getEditorX() - scrollX + BLOCK_W / 2;
                    int fy = cT + obj.getEditorY() - scrollY + BLOCK_H;
                    int tx = cL + next.getEditorX() - scrollX + BLOCK_W / 2;
                    int ty = cT + next.getEditorY() - scrollY;
                    drawArrow(g, fx, fy, tx, ty, C_ARROW);
                }
            }
        }

        // Bloecke
        for (ScenarioObjective obj : currentScenario.getObjectives()) {
            renderBlock(g, obj, cL, cT);
        }

        if (currentScenario.getObjectives().size() <= 2) {
            String hint = "\u00A78Bausteine aus der Palette hierher ziehen";
            g.drawString(this.font, hint, cL + (cR - cL) / 2 - this.font.width(hint) / 2, cT + (cB - cT) / 2, 0x444444);
        }
        g.disableScissor();
    }

    private void renderBlock(GuiGraphics g, ScenarioObjective obj, int cL, int cT) {
        int bx = cL + obj.getEditorX() - scrollX, by = cT + obj.getEditorY() - scrollY;
        boolean sel = obj.getId().equals(selectedBlockId);
        boolean hov = obj.getId().equals(hoveredBlockId);
        int col = obj.getType().getColor(), dk = obj.getType().getDarkerColor();

        g.fill(bx + 2, by + 2, bx + BLOCK_W + 2, by + BLOCK_H + 2, 0x44000000);
        g.fill(bx, by, bx + BLOCK_W, by + BLOCK_H, dk);
        g.fill(bx + 1, by + 1, bx + BLOCK_W - 1, by + BLOCK_H - 1, sel ? blend(col, C_SEL) : (hov ? blend(col, 0xFF444466) : col));

        if (sel) {
            g.fill(bx - 1, by - 1, bx + BLOCK_W + 1, by, C_SEL);
            g.fill(bx - 1, by + BLOCK_H, bx + BLOCK_W + 1, by + BLOCK_H + 1, C_SEL);
            g.fill(bx - 1, by, bx, by + BLOCK_H, C_SEL);
            g.fill(bx + BLOCK_W, by, bx + BLOCK_W + 1, by + BLOCK_H, C_SEL);
        }

        g.fill(bx + 2, by + 2, bx + 14, by + 14, dk);
        g.drawCenteredString(this.font, obj.getType().getIcon(), bx + 8, by + 4, 0xFFFFFF);
        g.drawString(this.font, "\u00A7l" + obj.getType().getDisplayName(), bx + 16, by + 4, 0xFFFFFF);

        String sum = obj.getParamSummary();
        if (!sum.isEmpty()) {
            String t = sum.length() > 16 ? sum.substring(0, 15) + ".." : sum;
            g.drawString(this.font, "\u00A77" + t, bx + 4, by + BLOCK_H - 12, 0xAAAAAA);
        }

        // Konnektoren
        if (obj.getType() != ObjectiveType.REWARD) {
            int cx = bx + BLOCK_W / 2, cy = by + BLOCK_H;
            boolean csrc = connecting && obj.getId().equals(connectSrcId);
            int cc = csrc ? C_CONN : (obj.getNextObjectiveId() != null ? 0xFFE94560 : 0xFF555555);
            g.fill(cx - CONNECTOR_R, cy - 1, cx + CONNECTOR_R, cy + CONNECTOR_R + 1, cc);
        }
        if (obj.getType() != ObjectiveType.START) {
            int cx = bx + BLOCK_W / 2, cy = by;
            g.fill(cx - CONNECTOR_R, cy - CONNECTOR_R, cx + CONNECTOR_R, cy + 1, 0xFF555555);
        }
    }

    // ─── PROPERTIES PANEL ───
    private void renderPropsPanel(GuiGraphics g, int mx, int my) {
        int pL = propsLeft(), pT = TOOLBAR_H, pB = this.height - STATUS_H;
        g.fill(pL, pT, this.width, pB, C_PROPS);
        g.fill(pL, pT, pL + 1, pB, 0xFF2A2A4A);

        if (selectedBlockId == null) {
            g.drawString(this.font, "\u00A78Kein Block", pL + 8, pT + 40, 0x555555);
            g.drawString(this.font, "\u00A78gewaehlt", pL + 8, pT + 52, 0x555555);
            return;
        }

        ScenarioObjective obj = currentScenario.getObjective(selectedBlockId);
        if (obj == null) return;

        // Header
        g.fill(pL + 1, pT, this.width, pT + 16, obj.getType().getDarkerColor());
        g.drawString(this.font, obj.getType().getIcon() + " " + obj.getType().getDisplayName(), pL + 4, pT + 4, 0xFFFFFF);

        int y = pT + 20;
        int fieldIdx = 0;
        for (ObjectiveType.ParamDef def : obj.getType().getParamDefs()) {
            boolean isDD = def.widget().name().startsWith("DROPDOWN");

            // Label mit Typ-Icon
            String icon = getWidgetIcon(def.widget());
            g.drawString(this.font, icon + "\u00A77" + def.label(), pL + 4, y, 0x888888);
            y += 10;

            if (isDD) {
                // Dropdown-Button
                String val = obj.getParam(def.key());
                String displayVal = getDropdownDisplayValue(def, val);
                boolean ddHover = mx >= pL + 3 && mx < this.width - 3 && my >= y && my < y + 12;
                g.fill(pL + 3, y, this.width - 3, y + 12, ddHover ? 0xFF2A2A4A : 0xFF1A1A2A);
                g.fill(pL + 3, y + 12, this.width - 3, y + 13, 0xFF3A3A5A);
                String dv = displayVal.length() > 14 ? displayVal.substring(0, 13) + ".." : displayVal;
                g.drawString(this.font, "\u00A7f" + dv, pL + 6, y + 2, 0xDDDDDD);
                g.drawString(this.font, "\u00A77\u25BC", this.width - 12, y + 2, 0x888888);
            } else {
                // NUMBER: +/- Buttons
                if (def.widget() == ParamWidget.NUMBER || def.widget() == ParamWidget.COORD) {
                    boolean minH = mx >= this.width - 22 && mx < this.width - 12 && my >= y && my < y + 10;
                    boolean plH = mx >= this.width - 11 && mx < this.width - 1 && my >= y && my < y + 10;
                    g.fill(this.width - 22, y, this.width - 12, y + 10, minH ? 0xFFE74C3C : 0xFF3A2020);
                    g.fill(this.width - 11, y, this.width - 1, y + 10, plH ? 0xFF2ECC71 : 0xFF203A20);
                    g.drawCenteredString(this.font, "-", this.width - 17, y + 1, 0xFFFFFF);
                    g.drawCenteredString(this.font, "+", this.width - 6, y + 1, 0xFFFFFF);
                }
            }
            y += 13;
        }
    }

    private String getWidgetIcon(ParamWidget w) {
        return switch (w) {
            case COORD -> "\u00A7b\u2316 ";
            case NUMBER -> "\u00A7e# ";
            case DROPDOWN_NPC_NAME, DROPDOWN_NPC_TYPE -> "\u00A7a\u263A ";
            case DROPDOWN_MERCHANT_CAT, DROPDOWN_BANK_CAT, DROPDOWN_SERVICE_CAT -> "\u00A76\u2692 ";
            case DROPDOWN_PLOT, DROPDOWN_PLOT_TYPE -> "\u00A7d\u2302 ";
            case DROPDOWN_DIFFICULTY -> "\u00A7c\u2605 ";
            case DROPDOWN_ENTITY -> "\u00A74\u2694 ";
            case DROPDOWN_VEHICLE -> "\u00A79\u2708 ";
            case DROPDOWN_OUTFIT -> "\u00A78\u2663 ";
            case DROPDOWN_METHOD -> "\u00A73\u2699 ";
            case DROPDOWN_EVENT -> "\u00A7c? ";
            case DROPDOWN_COLOR -> "\u00A7e\u25CF ";
            case DROPDOWN_LOCK -> "\u00A7b\uD83D\uDD12 ";
            default -> "\u00A77\u25AA ";
        };
    }

    private String getDropdownDisplayValue(ObjectiveType.ParamDef def, String val) {
        if (val == null || val.isEmpty()) return "(waehlen)";
        return switch (def.widget()) {
            case DROPDOWN_DIFFICULTY -> {
                try { int d = Integer.parseInt(val); yield DIFFICULTIES[Math.max(0, Math.min(4, d - 1))]; }
                catch (NumberFormatException e) { yield val; }
            }
            case DROPDOWN_PLOT -> {
                for (var p : serverPlots) { if (p.id().equals(val)) { yield p.name() + " (" + p.type() + ")"; } }
                yield val;
            }
            case DROPDOWN_LOCK -> {
                for (var l : serverLocks) { if (l.lockId().equals(val)) { yield l.lockId() + " (" + l.lockType() + ")"; } }
                yield val;
            }
            default -> val;
        };
    }

    // ─── STATUS BAR ───
    private void renderStatusBar(GuiGraphics g, int mx, int my) {
        int y = this.height - STATUS_H;
        g.fill(0, y, this.width, this.height, C_STATUS);
        g.fill(0, y, this.width, y + 1, 0xFF2A2A4A);

        g.drawString(this.font, "\u00A77Name:", 4, y + 4, 0x888888);
        // nameField renders itself at (44, y+3)

        int infoX = 140;
        g.drawString(this.font, "\u00A78|", infoX, y + 4, 0x444444);
        infoX += 6;
        g.drawString(this.font, "\u00A77Phasen:\u00A7f" + currentScenario.getStepCount(), infoX, y + 4, 0xAAAAAA);
        infoX += 56;

        // Schwierigkeit (klickbar)
        g.drawString(this.font, "\u00A78|", infoX, y + 4, 0x444444);
        infoX += 6;
        g.drawString(this.font, "\u00A76" + currentScenario.getDifficultyStars(), infoX, y + 4, 0xFFAA00);
        // Klickbare +/- fuer Schwierigkeit
        int diffEndX = infoX + this.font.width(currentScenario.getDifficultyStars());
        boolean diffMinH = mx >= diffEndX + 2 && mx < diffEndX + 10 && my >= y + 2 && my < y + 13;
        boolean diffPlH = mx >= diffEndX + 11 && mx < diffEndX + 19 && my >= y + 2 && my < y + 13;
        g.fill(diffEndX + 2, y + 2, diffEndX + 10, y + 13, diffMinH ? 0xFFE74C3C : 0xFF2A1A1A);
        g.fill(diffEndX + 11, y + 2, diffEndX + 19, y + 13, diffPlH ? 0xFF2ECC71 : 0xFF1A2A1A);
        g.drawCenteredString(this.font, "-", diffEndX + 6, y + 3, 0xFFFFFF);
        g.drawCenteredString(this.font, "+", diffEndX + 15, y + 3, 0xFFFFFF);
        infoX = diffEndX + 24;

        // MissionType (klickbar)
        g.drawString(this.font, "\u00A78|", infoX, y + 4, 0x444444);
        infoX += 6;
        int mtIdx = getMissionTypeIndex();
        String mtLabel = MISSION_TYPE_LABELS[mtIdx];
        int mtColor = mtIdx == 0 ? 0xFFFFEE00 : mtIdx == 1 ? 0xFF55FF55 : 0xFFFFAA00;
        boolean mtHover = mx >= infoX && mx < infoX + this.font.width(mtLabel) + 4 && my >= y + 1 && my < y + 14;
        if (mtHover) g.fill(infoX - 1, y + 1, infoX + this.font.width(mtLabel) + 3, y + 14, 0x22FFFFFF);
        g.drawString(this.font, "\u00A7f" + mtLabel, infoX, y + 4, mtColor);
        infoX += this.font.width(mtLabel) + 8;

        // Min-Gang-Level
        g.drawString(this.font, "\u00A78|", infoX, y + 4, 0x444444);
        infoX += 6;
        g.drawString(this.font, "\u00A77Lvl:\u00A7f" + currentScenario.getMinGangLevel(), infoX, y + 4, 0xAAAAAA);
        int lvlX = infoX + this.font.width("Lvl:" + currentScenario.getMinGangLevel());
        boolean lvlMinH = mx >= lvlX + 2 && mx < lvlX + 10 && my >= y + 2 && my < y + 13;
        boolean lvlPlH = mx >= lvlX + 11 && mx < lvlX + 19 && my >= y + 2 && my < y + 13;
        g.fill(lvlX + 2, y + 2, lvlX + 10, y + 13, lvlMinH ? 0xFFE74C3C : 0xFF2A1A1A);
        g.fill(lvlX + 11, y + 2, lvlX + 19, y + 13, lvlPlH ? 0xFF2ECC71 : 0xFF1A2A1A);
        g.drawCenteredString(this.font, "-", lvlX + 6, y + 3, 0xFFFFFF);
        g.drawCenteredString(this.font, "+", lvlX + 15, y + 3, 0xFFFFFF);
    }

    private int getMissionTypeIndex() {
        String t = currentScenario.getMissionType();
        for (int i = 0; i < MISSION_TYPES.length; i++) if (MISSION_TYPES[i].equals(t)) return i;
        return 1;
    }

    // ─── TOOLBAR DROPDOWNS (im Vordergrund) ───
    private void renderToolbarDropdowns(GuiGraphics g, int mx, int my) {
        if (toolbarDD == ToolbarDD.NONE) return;

        // Vordergrund: Schatten-Overlay + Z-Translation
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);

        // Abdunkelung hinter dem Dropdown
        g.fill(0, TOOLBAR_H, this.width, this.height, 0x88000000);

        int ddX = 4;
        if (toolbarDD == ToolbarDD.OPEN) {
            String[] items = allScenarios.stream().map(s -> s.getName() + (s.isActive() ? " \u00A7a\u2713" : "")).toArray(String[]::new);
            if (items.length == 0) items = new String[]{"\u00A78Keine Szenarien"};
            renderDD(g, ddX, TOOLBAR_H, items, mx, my, 0xFF3498DB, 140);
        } else if (toolbarDD == ToolbarDD.TEMPLATES) {
            ddX = 4 + this.font.width("Oeffnen") + 8 + 3 + this.font.width("Speichern") + 8 + 3;
            renderDD(g, ddX, TOOLBAR_H, ScenarioTemplates.getTemplateNames(), mx, my, 0xFFF39C12, 140);
        } else if (toolbarDD == ToolbarDD.ACTIVE) {
            ddX = 4 + this.font.width("Oeffnen") + 8 + 3 + this.font.width("Speichern") + 8 + 3
                    + this.font.width("Vorlagen") + 8 + 3;
            String[] items = allScenarios.stream()
                    .map(s -> (s.isActive() ? "\u00A7a\u25CF " : "\u00A7c\u25CB ") + "\u00A7f" + s.getName())
                    .toArray(String[]::new);
            if (items.length == 0) items = new String[]{"\u00A78Keine Szenarien"};
            renderDD(g, ddX, TOOLBAR_H, items, mx, my, 0xFF9B59B6, 140);
        }

        g.pose().popPose();
    }

    // ─── PARAM DROPDOWN (Properties, im Vordergrund) ───
    private void renderParamDropdown(GuiGraphics g, int mx, int my) {
        if (activeParamDD == null || selectedBlockId == null) return;
        ScenarioObjective obj = currentScenario.getObjective(selectedBlockId);
        if (obj == null) return;

        ObjectiveType.ParamDef def = null;
        int paramY = TOOLBAR_H + 20;
        for (ObjectiveType.ParamDef d : obj.getType().getParamDefs()) {
            if (d.key().equals(activeParamDD)) { def = d; break; }
            paramY += 23;
        }
        if (def == null) return;

        String[] items = getDropdownItems(def);
        int ddX = propsLeft() + 3;
        int ddW = PROPS_W - 8;
        int itemH = 12;
        int maxVisible = Math.min(items.length, 10);
        int ddH = maxVisible * itemH + 4;

        // Dropdown kann ueber Properties-Panel hinausragen -> links erweitern wenn noetig
        int actualDDW = Math.max(ddW, 120);
        int actualDDX = ddX;
        if (actualDDX + actualDDW > this.width) {
            actualDDX = this.width - actualDDW - 2;
        }

        // Vordergrund: Z-Translation
        g.pose().pushPose();
        g.pose().translate(0, 0, 500);

        // Abdunkelung
        g.fill(0, TOOLBAR_H, this.width, this.height, 0x66000000);

        // Rahmen + Hintergrund
        g.fill(actualDDX - 2, paramY + 20, actualDDX + actualDDW + 2, paramY + 22 + ddH + 2, 0xFF000000);
        g.fill(actualDDX - 1, paramY + 21, actualDDX + actualDDW + 1, paramY + 22 + ddH + 1, 0xFF3A3A5A);
        g.fill(actualDDX, paramY + 22, actualDDX + actualDDW, paramY + 22 + ddH, C_DD_BG);
        // Akzentlinie oben
        g.fill(actualDDX, paramY + 22, actualDDX + actualDDW, paramY + 23, 0xFF3498DB);

        g.enableScissor(actualDDX, paramY + 23, actualDDX + actualDDW, paramY + 22 + ddH);
        for (int i = 0; i < items.length; i++) {
            int iy = paramY + 24 + (i - paramDDScroll) * itemH;
            if (iy < paramY + 20 || iy > paramY + 22 + ddH) continue;
            boolean hover = mx >= actualDDX && mx < actualDDX + actualDDW && my >= iy && my < iy + itemH;
            if (hover) g.fill(actualDDX + 1, iy, actualDDX + actualDDW - 1, iy + itemH, C_DD_HOVER);
            String t = items[i].length() > 18 ? items[i].substring(0, 17) + ".." : items[i];
            g.drawString(this.font, t, actualDDX + 3, iy + 2, 0xDDDDDD);
        }
        g.disableScissor();

        // Scrollbar Hinweis
        if (items.length > maxVisible) {
            int sbH = ddH - 4;
            int thumbH = Math.max(8, sbH * maxVisible / items.length);
            int thumbY = paramY + 24 + (sbH - thumbH) * paramDDScroll / Math.max(1, items.length - maxVisible);
            g.fill(actualDDX + actualDDW - 3, paramY + 24, actualDDX + actualDDW - 1, paramY + 22 + ddH - 2, 0x33FFFFFF);
            g.fill(actualDDX + actualDDW - 3, thumbY, actualDDX + actualDDW - 1, thumbY + thumbH, 0xAAFFFFFF);
        }

        g.pose().popPose();
    }

    private String[] getDropdownItems(ObjectiveType.ParamDef def) {
        return switch (def.widget()) {
            case DROPDOWN_NPC_NAME -> serverNpcNames.toArray(new String[0]);
            case DROPDOWN_NPC_TYPE -> NPC_TYPES;
            case DROPDOWN_MERCHANT_CAT -> MERCHANT_CATS;
            case DROPDOWN_BANK_CAT -> BANK_CATS;
            case DROPDOWN_SERVICE_CAT -> SERVICE_CATS;
            case DROPDOWN_PLOT -> serverPlots.stream().map(p -> p.id() + "|" + p.name()).toArray(String[]::new);
            case DROPDOWN_PLOT_TYPE -> new String[]{"RESIDENTIAL", "COMMERCIAL", "SHOP", "PUBLIC", "GOVERNMENT", "PRISON", "TOWING_YARD"};
            case DROPDOWN_DIFFICULTY -> DIFFICULTIES;
            case DROPDOWN_ENTITY -> ENTITY_TYPES;
            case DROPDOWN_VEHICLE -> VEHICLE_TYPES;
            case DROPDOWN_OUTFIT -> OUTFIT_TYPES;
            case DROPDOWN_METHOD -> METHOD_TYPES;
            case DROPDOWN_EVENT -> EVENT_TYPES;
            case DROPDOWN_COLOR -> COLOR_TYPES;
            case DROPDOWN_LOCK -> serverLocks.stream().map(l -> l.lockId() + "|" + l.lockType() + " @ " + l.x() + "," + l.y() + "," + l.z()).toArray(String[]::new);
            default -> new String[]{};
        };
    }

    // ─── TOOLTIP (im Vordergrund) ───
    private void renderBlockTooltip(GuiGraphics g, int mx, int my) {
        ScenarioObjective obj = currentScenario.getObjective(hoveredBlockId);
        if (obj == null) return;

        List<String> lines = new ArrayList<>();
        lines.add("\u00A7f\u00A7l" + obj.getType().getDisplayName());
        lines.add("\u00A78Kategorie: " + obj.getType().getCategory().getDisplayName());
        for (ObjectiveType.ParamDef def : obj.getType().getParamDefs()) {
            String val = obj.getParam(def.key());
            if (def.widget() == ParamWidget.DROPDOWN_PLOT) {
                for (var p : serverPlots) if (p.id().equals(val)) { val = p.name(); break; }
            } else if (def.widget() == ParamWidget.DROPDOWN_LOCK) {
                for (var l : serverLocks) if (l.lockId().equals(val)) { val = l.lockId() + " (" + l.lockType() + ")"; break; }
            }
            lines.add("\u00A77" + def.label() + ": \u00A7f" + (val.isEmpty() ? "-" : val));
        }
        if (obj.getNextObjectiveId() != null) lines.add("\u00A78\u2192 verbunden");

        int tw = 0;
        for (String l : lines) tw = Math.max(tw, this.font.width(l));
        int tx = mx + 10, ty = my - lines.size() * 10 - 4;
        if (tx + tw + 6 > this.width) tx = mx - tw - 14;
        if (ty < TOOLBAR_H) ty = my + 10;

        g.pose().pushPose();
        g.pose().translate(0, 0, 300);
        g.fill(tx - 2, ty - 2, tx + tw + 4, ty + lines.size() * 10 + 2, 0xEE000000);
        g.fill(tx - 2, ty - 2, tx + tw + 4, ty - 1, 0xFF3498DB);
        for (int i = 0; i < lines.size(); i++)
            g.drawString(this.font, lines.get(i), tx, ty + i * 10, 0xFFFFFF);
        g.pose().popPose();
    }

    // ─── GENERIC DROPDOWN ───
    private void renderDD(GuiGraphics g, int x, int y, String[] items, int mx, int my, int ac, int w) {
        int ih = 13, h = items.length * ih + 4;
        // Schatten + Rahmen
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        g.fill(x, y, x + w, y + h, C_DD_BG);
        g.fill(x, y, x + w, y + 2, ac);
        for (int i = 0; i < items.length; i++) {
            int iy = y + 3 + i * ih;
            boolean hov = mx >= x && mx < x + w && my >= iy && my < iy + ih;
            if (hov) g.fill(x + 1, iy, x + w - 1, iy + ih, ac & 0x33FFFFFF);
            String t = items[i].length() > 22 ? items[i].substring(0, 21) + ".." : items[i];
            g.drawString(this.font, t, x + 4, iy + 2, 0xDDDDDD);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ZEICHENHILFEN
    // ═══════════════════════════════════════════════════════════

    private void drawArrow(GuiGraphics g, int x1, int y1, int x2, int y2, int c) {
        int mY = y1 + (y2 - y1) / 2;
        g.fill(x1, y1, x1 + 1, mY, c);
        if (x1 != x2) g.fill(Math.min(x1, x2), mY, Math.max(x1, x2) + 1, mY + 1, c);
        g.fill(x2, mY, x2 + 1, y2, c);
        g.fill(x2 - 3, y2 - 5, x2 + 4, y2, c);
    }

    private void drawDashedLine(GuiGraphics g, int x1, int y1, int x2, int y2, int c) {
        double dx = x2 - x1, dy = y2 - y1, l = Math.sqrt(dx * dx + dy * dy);
        if (l < 1) return;
        dx /= l; dy /= l;
        for (double d = 0; d < l; d += 8) {
            int sx = x1 + (int)(dx * d), sy = y1 + (int)(dy * d);
            int ex = x1 + (int)(dx * Math.min(d + 4, l)), ey = y1 + (int)(dy * Math.min(d + 4, l));
            g.fill(Math.min(sx, ex), Math.min(sy, ey), Math.max(sx, ex) + 1, Math.max(sy, ey) + 1, c);
        }
    }

    private int blend(int c1, int c2) {
        return 0xFF000000 | ((((c1 >> 16) & 0xFF) + ((c2 >> 16) & 0xFF)) / 2 << 16)
                | ((((c1 >> 8) & 0xFF) + ((c2 >> 8) & 0xFF)) / 2 << 8)
                | (((c1 & 0xFF) + (c2 & 0xFF)) / 2);
    }

    // ═══════════════════════════════════════════════════════════
    // MAUS-EVENTS
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX, my = (int) mouseY;

        // Offene Param-Dropdown behandeln
        if (activeParamDD != null) {
            boolean handled = handleParamDDClick(mx, my);
            activeParamDD = null;
            paramDDScroll = 0;
            if (handled) return true;
        }

        // Toolbar-Dropdown behandeln
        if (toolbarDD != ToolbarDD.NONE) {
            boolean handled = handleToolbarDDClick(mx, my);
            toolbarDD = ToolbarDD.NONE;
            if (handled) return true;
        }

        // Toolbar
        if (my < TOOLBAR_H) { handleToolbar(mx); return true; }

        // Status Bar
        if (my >= this.height - STATUS_H) { handleStatusBar(mx, my); return true; }

        // Properties Panel
        if (mx >= propsLeft() && my >= TOOLBAR_H && my < this.height - STATUS_H) {
            handlePropsClick(mx, my, button);
            return true;
        }

        // Palette
        if (mx < PALETTE_W && my >= TOOLBAR_H) { handlePalette(mx, my); return true; }

        // Canvas
        if (mx >= PALETTE_W && mx < propsLeft() && my >= TOOLBAR_H && my < this.height - STATUS_H) {
            handleCanvas(mx, my, button);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleToolbar(int mx) {
        int x = 4, w;
        w = this.font.width("Oeffnen") + 8;
        if (mx >= x && mx < x + w) { toolbarDD = toolbarDD == ToolbarDD.OPEN ? ToolbarDD.NONE : ToolbarDD.OPEN; return; }
        x += w + 3;
        w = this.font.width("Speichern") + 8;
        if (mx >= x && mx < x + w) { saveCurrentScenario(); return; }
        x += w + 3;
        w = this.font.width("Vorlagen") + 8;
        if (mx >= x && mx < x + w) { toolbarDD = toolbarDD == ToolbarDD.TEMPLATES ? ToolbarDD.NONE : ToolbarDD.TEMPLATES; return; }
        x += w + 3;
        w = this.font.width("Aktive") + 8;
        if (mx >= x && mx < x + w) { toolbarDD = toolbarDD == ToolbarDD.ACTIVE ? ToolbarDD.NONE : ToolbarDD.ACTIVE; return; }

        int rx = this.width - 4;
        w = this.font.width("X") + 8;
        if (mx >= rx - w && mx < rx) { onClose(); return; }
        rx -= w + 3;
        w = this.font.width("Neu") + 8;
        if (mx >= rx - w && mx < rx) { newScenario(); return; }
        rx -= w + 3;
        w = this.font.width("Loeschen") + 8;
        if (mx >= rx - w && mx < rx) deleteSelectedBlock();
    }

    private void handleStatusBar(int mx, int my) {
        int y = this.height - STATUS_H;
        // Schwierigkeit +/- Buttons
        int infoX = 140 + 6 + 56 + 6;
        int diffEndX = infoX + this.font.width(currentScenario.getDifficultyStars());
        if (mx >= diffEndX + 2 && mx < diffEndX + 10 && my >= y + 2)
            currentScenario.setDifficulty(currentScenario.getDifficulty() - 1);
        if (mx >= diffEndX + 11 && mx < diffEndX + 19 && my >= y + 2)
            currentScenario.setDifficulty(currentScenario.getDifficulty() + 1);

        // MissionType klick
        infoX = diffEndX + 24 + 6;
        int mtIdx = getMissionTypeIndex();
        int mtW = this.font.width(MISSION_TYPE_LABELS[mtIdx]) + 4;
        if (mx >= infoX && mx < infoX + mtW && my >= y + 1)
            currentScenario.setMissionType(MISSION_TYPES[(mtIdx + 1) % MISSION_TYPES.length]);

        // Min-Level +/-
        infoX += mtW + 4 + 6;
        int lvlX = infoX + this.font.width("Lvl:" + currentScenario.getMinGangLevel());
        if (mx >= lvlX + 2 && mx < lvlX + 10 && my >= y + 2)
            currentScenario.setMinGangLevel(currentScenario.getMinGangLevel() - 1);
        if (mx >= lvlX + 11 && mx < lvlX + 19 && my >= y + 2)
            currentScenario.setMinGangLevel(currentScenario.getMinGangLevel() + 1);
    }

    private void handlePropsClick(int mx, int my, int button) {
        if (selectedBlockId == null) return;
        ScenarioObjective obj = currentScenario.getObjective(selectedBlockId);
        if (obj == null) return;

        int pL = propsLeft();
        int y = TOOLBAR_H + 20;
        for (ObjectiveType.ParamDef def : obj.getType().getParamDefs()) {
            boolean isDD = def.widget().name().startsWith("DROPDOWN");
            // Klick auf Dropdown-Button
            if (isDD && my >= y + 10 && my < y + 22 && mx >= pL + 3 && mx < this.width - 3) {
                activeParamDD = def.key();
                paramDDScroll = 0;
                return;
            }
            // Klick auf +/- fuer NUMBER/COORD
            if ((def.widget() == ParamWidget.NUMBER || def.widget() == ParamWidget.COORD)
                    && my >= y + 10 && my < y + 20) {
                if (mx >= this.width - 22 && mx < this.width - 12) {
                    adjustNumber(obj, def.key(), -1);
                    rebuildProps();
                    return;
                }
                if (mx >= this.width - 11 && mx < this.width - 1) {
                    adjustNumber(obj, def.key(), 1);
                    rebuildProps();
                    return;
                }
            }
            y += 23;
        }
    }

    private void adjustNumber(ScenarioObjective obj, String key, int delta) {
        try {
            int val = Integer.parseInt(obj.getParam(key));
            obj.setParam(key, String.valueOf(val + delta));
        } catch (NumberFormatException ignored) {}
    }

    private boolean handleParamDDClick(int mx, int my) {
        if (selectedBlockId == null) return false;
        ScenarioObjective obj = currentScenario.getObjective(selectedBlockId);
        if (obj == null) return false;

        ObjectiveType.ParamDef def = null;
        int paramY = TOOLBAR_H + 20;
        for (ObjectiveType.ParamDef d : obj.getType().getParamDefs()) {
            if (d.key().equals(activeParamDD)) { def = d; break; }
            paramY += 23;
        }
        if (def == null) return false;

        String[] items = getDropdownItems(def);
        int ddX = propsLeft() + 3, ddW = PROPS_W - 8, itemH = 12;

        for (int i = 0; i < items.length; i++) {
            int iy = paramY + 24 + (i - paramDDScroll) * itemH;
            if (mx >= ddX && mx < ddX + ddW && my >= iy && my < iy + itemH) {
                String selected = items[i];
                // Fuer Plots: ID extrahieren (format: "id|name")
                if (def.widget() == ParamWidget.DROPDOWN_PLOT && selected.contains("|")) {
                    selected = selected.substring(0, selected.indexOf("|"));
                }
                // Fuer Locks: ID extrahieren (format: "lockId|lockType @ x,y,z")
                if (def.widget() == ParamWidget.DROPDOWN_LOCK && selected.contains("|")) {
                    selected = selected.substring(0, selected.indexOf("|"));
                }
                // Fuer Difficulty: Zahl extrahieren
                if (def.widget() == ParamWidget.DROPDOWN_DIFFICULTY) {
                    selected = String.valueOf(i + 1);
                }
                obj.setParam(def.key(), selected);
                rebuildProps();
                return true;
            }
        }
        return false;
    }

    private boolean handleToolbarDDClick(int mx, int my) {
        if (toolbarDD == ToolbarDD.OPEN && !allScenarios.isEmpty()) {
            int ih = 13;
            for (int i = 0; i < allScenarios.size(); i++) {
                int iy = TOOLBAR_H + 2 + i * ih;
                if (mx >= 4 && mx < 134 && my >= iy && my < iy + ih) {
                    loadScenario(allScenarios.get(i)); return true;
                }
            }
        }
        if (toolbarDD == ToolbarDD.TEMPLATES) {
            List<MissionScenario> templates = ScenarioTemplates.getAll();
            int ddX = 4 + this.font.width("Oeffnen") + 8 + 3 + this.font.width("Speichern") + 8 + 3;
            int ih = 13;
            for (int i = 0; i < templates.size(); i++) {
                int iy = TOOLBAR_H + 2 + i * ih;
                if (mx >= ddX && mx < ddX + 130 && my >= iy && my < iy + ih) {
                    loadScenario(templates.get(i)); return true;
                }
            }
        }
        if (toolbarDD == ToolbarDD.ACTIVE && !allScenarios.isEmpty()) {
            int ddX = 4 + this.font.width("Oeffnen") + 8 + 3 + this.font.width("Speichern") + 8 + 3
                    + this.font.width("Vorlagen") + 8 + 3;
            int ih = 13;
            for (int i = 0; i < allScenarios.size(); i++) {
                int iy = TOOLBAR_H + 2 + i * ih;
                if (mx >= ddX && mx < ddX + 130 && my >= iy && my < iy + ih) {
                    allScenarios.get(i).setActive(!allScenarios.get(i).isActive()); return true;
                }
            }
        }
        return false;
    }

    private void handlePalette(int mx, int my) {
        int pB = this.height - STATUS_H;
        int y = TOOLBAR_H + 14 - paletteScroll;
        for (var entry : catExpanded.entrySet()) {
            var cat = entry.getKey();
            boolean exp = entry.getValue();
            if (my >= y && my < y + 11) { entry.setValue(!exp); return; }
            y += 12;
            if (exp) {
                for (ObjectiveType t : ObjectiveType.values()) {
                    if (t.getCategory() != cat) continue;
                    if (my >= y && my < y + 14) { addBlock(t); return; }
                    y += 15;
                }
            }
            y += 3;
        }
    }

    private void handleCanvas(int mx, int my, int button) {
        int cL = PALETTE_W, cT = TOOLBAR_H;

        if (button == 1) {
            if (connecting) { connecting = false; connectSrcId = null; return; }
            for (ScenarioObjective o : currentScenario.getObjectives()) {
                int bx = cL + o.getEditorX() - scrollX, by = cT + o.getEditorY() - scrollY;
                if (mx >= bx && mx < bx + BLOCK_W && my >= by && my < by + BLOCK_H) {
                    o.setNextObjectiveId(null); return;
                }
            }
            selectedBlockId = null; rebuildProps(); return;
        }

        if (connecting && connectSrcId != null) {
            for (ScenarioObjective o : currentScenario.getObjectives()) {
                if (o.getId().equals(connectSrcId)) continue;
                int bx = cL + o.getEditorX() - scrollX, by = cT + o.getEditorY() - scrollY;
                if (mx >= bx && mx < bx + BLOCK_W && my >= by && my < by + BLOCK_H) {
                    ScenarioObjective src = currentScenario.getObjective(connectSrcId);
                    if (src != null) src.setNextObjectiveId(o.getId());
                    connecting = false; connectSrcId = null; return;
                }
            }
            connecting = false; connectSrcId = null; return;
        }

        // Connector klick
        for (ScenarioObjective o : currentScenario.getObjectives()) {
            if (o.getType() == ObjectiveType.REWARD) continue;
            int bx = cL + o.getEditorX() - scrollX, by = cT + o.getEditorY() - scrollY;
            int cx = bx + BLOCK_W / 2, cy = by + BLOCK_H;
            if (mx >= cx - 6 && mx <= cx + 6 && my >= cy - 3 && my <= cy + 6) {
                connecting = true; connectSrcId = o.getId(); o.setNextObjectiveId(null); return;
            }
        }

        // Block klick
        List<ScenarioObjective> objs = currentScenario.getObjectives();
        for (int i = objs.size() - 1; i >= 0; i--) {
            ScenarioObjective o = objs.get(i);
            int bx = cL + o.getEditorX() - scrollX, by = cT + o.getEditorY() - scrollY;
            if (mx >= bx && mx < bx + BLOCK_W && my >= by && my < by + BLOCK_H) {
                selectedBlockId = o.getId();
                dragging = true; dragOX = mx - bx; dragOY = my - by;
                rebuildProps(); return;
            }
        }

        selectedBlockId = null; rebuildProps();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {
        if (dragging && selectedBlockId != null && button == 0) {
            ScenarioObjective o = currentScenario.getObjective(selectedBlockId);
            if (o != null) {
                int nx = (int) mouseX - PALETTE_W - dragOX + scrollX;
                int ny = (int) mouseY - TOOLBAR_H - dragOY + scrollY;
                o.setEditorX(Math.max(0, (nx / GRID) * GRID));
                o.setEditorY(Math.max(0, (ny / GRID) * GRID));
            }
            return true;
        }
        if (button == 2) {
            scrollX = Math.max(0, scrollX - (int) dX);
            scrollY = Math.max(0, scrollY - (int) dY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dX, dY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int mx = (int) mouseX;
        if (mx < PALETTE_W) { paletteScroll = Math.max(0, paletteScroll - (int)(delta * 10)); return true; }
        if (mx >= propsLeft() && activeParamDD != null) { paramDDScroll = Math.max(0, paramDDScroll - (int) delta); return true; }
        if (mx >= PALETTE_W && mx < propsLeft()) { scrollY = Math.max(0, scrollY - (int)(delta * 20)); return true; }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mod) {
        if (key == 261 && selectedBlockId != null) { deleteSelectedBlock(); return true; }
        if (key == 256 && connecting) { connecting = false; connectSrcId = null; return true; }
        return super.keyPressed(key, scan, mod);
    }

    // ═══════════════════════════════════════════════════════════
    // AKTIONEN
    // ═══════════════════════════════════════════════════════════

    private void addBlock(ObjectiveType type) {
        String id = currentScenario.nextObjectiveId();
        int cx = scrollX + (propsLeft() - PALETTE_W) / 2 - BLOCK_W / 2;
        int cy = scrollY + (this.height - TOOLBAR_H - STATUS_H) / 2 - BLOCK_H / 2;
        currentScenario.addObjective(new ScenarioObjective(id, type, Math.max(0, (cx / GRID) * GRID), Math.max(0, (cy / GRID) * GRID)));
        selectedBlockId = id;
        rebuildProps();
    }

    private void deleteSelectedBlock() {
        if (selectedBlockId == null) return;
        ScenarioObjective o = currentScenario.getObjective(selectedBlockId);
        if (o == null || o.getType() == ObjectiveType.START || o.getType() == ObjectiveType.REWARD) return;
        currentScenario.removeObjective(selectedBlockId);
        selectedBlockId = null;
        rebuildProps();
    }

    private void newScenario() {
        currentScenario = new MissionScenario();
        selectedBlockId = null; scrollX = 0; scrollY = 0;
        if (nameField != null) nameField.setValue(currentScenario.getName());
        rebuildProps();
    }

    private void loadScenario(MissionScenario s) {
        currentScenario = ScenarioManager.fromJson(ScenarioManager.scenarioToJson(s));
        selectedBlockId = null; scrollX = 0; scrollY = 0;
        if (nameField != null) nameField.setValue(currentScenario.getName());
        rebuildProps();
    }

    private void saveCurrentScenario() {
        boolean found = false;
        for (int i = 0; i < allScenarios.size(); i++) {
            if (allScenarios.get(i).getId().equals(currentScenario.getId())) { allScenarios.set(i, currentScenario); found = true; break; }
        }
        if (!found) allScenarios.add(currentScenario);
        de.rolandsw.schedulemc.gang.network.GangNetworkHandler.sendToServer(
                new de.rolandsw.schedulemc.gang.network.SaveScenarioPacket(ScenarioManager.scenarioToJson(currentScenario)));
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
