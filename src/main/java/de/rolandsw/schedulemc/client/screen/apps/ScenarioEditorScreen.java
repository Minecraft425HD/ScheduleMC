package de.rolandsw.schedulemc.client.screen.apps;

import de.rolandsw.schedulemc.gang.scenario.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

/**
 * Visueller Szenario-Editor im Blockly-Stil.
 *
 * Layout:
 * ┌──────────────────────────────────────────────────────────┐
 * │  [Oeffnen] [Speichern] [Vorlagen] [Aktive]   [X]       │  Toolbar
 * ├────────────┬─────────────────────────────────────────────┤
 * │            │                                             │
 * │  PALETTE   │          EDITOR CANVAS                      │
 * │  (Baust.)  │   Bloecke platzieren & verbinden            │
 * │            │                                             │
 * │────────────│                                             │
 * │ PROPERTIES │                                             │
 * │ (Parameter)│                                             │
 * ├────────────┴─────────────────────────────────────────────┤
 * │  Status: Name | Bloecke: N | Schwierigkeit: ★★★☆☆       │  Statusbar
 * └──────────────────────────────────────────────────────────┘
 */
public class ScenarioEditorScreen extends Screen {

    // ═══════════════════════════════════════════════════════════
    // LAYOUT KONSTANTEN
    // ═══════════════════════════════════════════════════════════
    private static final int TOOLBAR_H = 22;
    private static final int PALETTE_W = 100;
    private static final int STATUS_H = 14;
    private static final int BLOCK_W = 90;
    private static final int BLOCK_H = 32;
    private static final int CONNECTOR_R = 4;
    private static final int GRID = 5;

    // ═══════════════════════════════════════════════════════════
    // FARBEN
    // ═══════════════════════════════════════════════════════════
    private static final int COL_BG = 0xFF1A1A2E;
    private static final int COL_TOOLBAR = 0xFF16213E;
    private static final int COL_PALETTE = 0xFF0F3460;
    private static final int COL_CANVAS = 0xFF0A0A1A;
    private static final int COL_GRID_DOT = 0xFF1A1A30;
    private static final int COL_STATUS = 0xFF16213E;
    private static final int COL_SELECTED = 0xFFE94560;
    private static final int COL_CONNECTING = 0xFF53E0BC;
    private static final int COL_ARROW = 0xBBE94560;
    private static final int COL_PROPS_BG = 0xFF0F3460;

    // ═══════════════════════════════════════════════════════════
    // ZUSTAND
    // ═══════════════════════════════════════════════════════════
    private MissionScenario currentScenario;
    private List<MissionScenario> allScenarios;

    // Canvas
    private int canvasScrollX = 0, canvasScrollY = 0;

    // Selektion
    private String selectedBlockId = null;
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    // Verbindungsmodus
    private boolean connecting = false;
    private String connectSourceId = null;

    // Palette
    private int paletteScrollY = 0;
    private final Map<ObjectiveType.Category, Boolean> categoryExpanded = new LinkedHashMap<>();

    // Dropdowns
    private boolean showOpenDropdown = false;
    private boolean showTemplatesDropdown = false;
    private boolean showActiveDropdown = false;

    // Properties
    private final List<EditBox> propFields = new ArrayList<>();
    private final List<String> propKeys = new ArrayList<>();
    private EditBox nameField;

    // ═══════════════════════════════════════════════════════════
    // KONSTRUKTOR
    // ═══════════════════════════════════════════════════════════

    public ScenarioEditorScreen(List<MissionScenario> scenarios) {
        super(Component.literal("Szenario-Editor"));
        this.allScenarios = scenarios != null ? new ArrayList<>(scenarios) : new ArrayList<>();
        this.currentScenario = new MissionScenario();

        for (ObjectiveType.Category cat : ObjectiveType.Category.values()) {
            if (cat != ObjectiveType.Category.SPEZIAL) {
                categoryExpanded.put(cat, false);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // INIT
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void init() {
        super.init();

        // Name-Feld in der Statusbar
        nameField = new EditBox(this.font, 4, this.height - STATUS_H + 1, PALETTE_W - 8, STATUS_H - 3,
                Component.literal("Name"));
        nameField.setMaxLength(30);
        nameField.setValue(currentScenario.getName());
        nameField.setResponder(s -> currentScenario.setName(s));
        nameField.setBordered(false);
        nameField.setTextColor(0xFFFFFF);
        addWidget(nameField);

        rebuildProperties();
    }

    private void rebuildProperties() {
        // Alte Felder entfernen
        for (EditBox box : propFields) {
            removeWidget(box);
        }
        propFields.clear();
        propKeys.clear();

        if (selectedBlockId == null) return;
        ScenarioObjective obj = currentScenario.getObjective(selectedBlockId);
        if (obj == null) return;

        int propTop = this.height - STATUS_H - getPropsHeight(obj);
        int fieldX = 6;
        int fieldW = PALETTE_W - 14;
        int y = propTop + 14; // nach Label

        for (ObjectiveType.ParamDef def : obj.getType().getParamDefs()) {
            EditBox box = new EditBox(this.font, fieldX, y + 1, fieldW, 10, Component.literal(def.label()));
            box.setMaxLength(30);
            box.setValue(obj.getParam(def.key()));
            box.setBordered(false);
            box.setTextColor(0xFFFFFF);
            final String key = def.key();
            box.setResponder(val -> obj.setParam(key, val));
            addWidget(box);
            propFields.add(box);
            propKeys.add(def.key());
            y += 13;
        }
    }

    private int getPropsHeight(ScenarioObjective obj) {
        if (obj == null) return 0;
        return 16 + obj.getType().getParamDefs().length * 13 + 4;
    }

    // ═══════════════════════════════════════════════════════════
    // RENDER
    // ═══════════════════════════════════════════════════════════

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);

        // Hintergrund
        g.fill(0, 0, this.width, this.height, COL_BG);

        renderToolbar(g, mouseX, mouseY);
        renderPalette(g, mouseX, mouseY);
        renderCanvas(g, mouseX, mouseY);
        renderProperties(g);
        renderStatusBar(g);
        renderDropdowns(g, mouseX, mouseY);

        // Verbindungslinie waehrend Connecting-Mode
        if (connecting && connectSourceId != null) {
            ScenarioObjective src = currentScenario.getObjective(connectSourceId);
            if (src != null) {
                int sx = PALETTE_W + src.getEditorX() - canvasScrollX + BLOCK_W / 2;
                int sy = TOOLBAR_H + src.getEditorY() - canvasScrollY + BLOCK_H;
                drawDashedLine(g, sx, sy, mouseX, mouseY, COL_CONNECTING);
            }
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderToolbar(GuiGraphics g, int mx, int my) {
        g.fill(0, 0, this.width, TOOLBAR_H, COL_TOOLBAR);
        g.fill(0, TOOLBAR_H - 1, this.width, TOOLBAR_H, 0xFF2A2A4A);

        int x = 4;
        x = drawToolbarBtn(g, x, "Oeffnen", mx, my, 0xFF3498DB);
        x = drawToolbarBtn(g, x, "Speichern", mx, my, 0xFF2ECC71);
        x = drawToolbarBtn(g, x, "Vorlagen", mx, my, 0xFFF39C12);
        x = drawToolbarBtn(g, x, "Aktive", mx, my, 0xFF9B59B6);

        // Rechts: Loeschen und Schliessen
        int rightX = this.width - 4;
        rightX = drawToolbarBtnRight(g, rightX, "X", mx, my, 0xFFE74C3C);
        rightX = drawToolbarBtnRight(g, rightX, "Loeschen", mx, my, 0xFF7F8C8D);
        drawToolbarBtnRight(g, rightX, "Neu", mx, my, 0xFF1ABC9C);

        // Titel
        String title = "\u00A77Szenario-Editor";
        g.drawString(this.font, title, this.width / 2 - this.font.width(title) / 2, 7, 0xAAAAAA);
    }

    private int drawToolbarBtn(GuiGraphics g, int x, String label, int mx, int my, int color) {
        int w = this.font.width(label) + 8;
        boolean hover = mx >= x && mx < x + w && my >= 2 && my < TOOLBAR_H - 2;
        g.fill(x, 3, x + w, TOOLBAR_H - 3, hover ? color : (color & 0x88FFFFFF));
        g.drawString(this.font, label, x + 4, 6, 0xFFFFFF);
        return x + w + 3;
    }

    private int drawToolbarBtnRight(GuiGraphics g, int rightEdge, String label, int mx, int my, int color) {
        int w = this.font.width(label) + 8;
        int x = rightEdge - w;
        boolean hover = mx >= x && mx < x + w && my >= 2 && my < TOOLBAR_H - 2;
        g.fill(x, 3, x + w, TOOLBAR_H - 3, hover ? color : (color & 0x88FFFFFF));
        g.drawString(this.font, label, x + 4, 6, 0xFFFFFF);
        return x - 3;
    }

    private void renderPalette(GuiGraphics g, int mx, int my) {
        int paletteTop = TOOLBAR_H;
        int propH = 0;
        if (selectedBlockId != null) {
            ScenarioObjective sel = currentScenario.getObjective(selectedBlockId);
            propH = getPropsHeight(sel);
        }
        int paletteBottom = this.height - STATUS_H - propH;

        g.fill(0, paletteTop, PALETTE_W, paletteBottom, COL_PALETTE);
        g.fill(PALETTE_W - 1, paletteTop, PALETTE_W, paletteBottom, 0xFF2A2A4A);

        // Palette Header
        g.drawString(this.font, "\u00A7lBausteine", 4, paletteTop + 3, 0xFFCCCCCC);

        g.enableScissor(0, paletteTop + 13, PALETTE_W - 1, paletteBottom);

        int y = paletteTop + 14 - paletteScrollY;
        for (Map.Entry<ObjectiveType.Category, Boolean> entry : categoryExpanded.entrySet()) {
            ObjectiveType.Category cat = entry.getKey();
            boolean expanded = entry.getValue();

            // Kategorie-Header
            String arrow = expanded ? "\u25BE " : "\u25B8 ";
            boolean catHover = mx >= 2 && mx < PALETTE_W - 2 && my >= y && my < y + 11;
            if (catHover) g.fill(2, y, PALETTE_W - 2, y + 11, 0x22FFFFFF);
            g.drawString(this.font, arrow + cat.getDisplayName(), 4, y + 1, cat.getColor() | 0xFF000000);
            y += 12;

            if (expanded) {
                for (ObjectiveType type : ObjectiveType.values()) {
                    if (type.getCategory() != cat) continue;

                    boolean blockHover = mx >= 4 && mx < PALETTE_W - 4 && my >= y && my < y + 14;
                    int bgColor = blockHover ? (type.getColor() & 0x44FFFFFF) : (type.getColor() & 0x22FFFFFF);
                    g.fill(4, y, PALETTE_W - 4, y + 14, bgColor);
                    g.fill(4, y, 6, y + 14, type.getColor());
                    g.drawString(this.font, type.getIcon(), 9, y + 3, type.getColor());
                    g.drawString(this.font, "\u00A7f" + type.getDisplayName(), 18, y + 3, 0xDDDDDD);
                    y += 15;
                }
            }
            y += 3;
        }

        g.disableScissor();
    }

    private void renderCanvas(GuiGraphics g, int mx, int my) {
        int canvasLeft = PALETTE_W;
        int canvasTop = TOOLBAR_H;
        int canvasRight = this.width;
        int canvasBottom = this.height - STATUS_H;

        // Canvas-Hintergrund
        g.fill(canvasLeft, canvasTop, canvasRight, canvasBottom, COL_CANVAS);

        g.enableScissor(canvasLeft, canvasTop, canvasRight, canvasBottom);

        // Raster-Punkte
        int gridSize = 20;
        int startX = canvasLeft - (canvasScrollX % gridSize);
        int startY = canvasTop - (canvasScrollY % gridSize);
        for (int gx = startX; gx < canvasRight; gx += gridSize) {
            for (int gy = startY; gy < canvasBottom; gy += gridSize) {
                g.fill(gx, gy, gx + 1, gy + 1, COL_GRID_DOT);
            }
        }

        // Verbindungslinien (Pfeile)
        for (ScenarioObjective obj : currentScenario.getObjectives()) {
            if (obj.getNextObjectiveId() != null) {
                ScenarioObjective next = currentScenario.getObjective(obj.getNextObjectiveId());
                if (next != null) {
                    int fromX = canvasLeft + obj.getEditorX() - canvasScrollX + BLOCK_W / 2;
                    int fromY = canvasTop + obj.getEditorY() - canvasScrollY + BLOCK_H;
                    int toX = canvasLeft + next.getEditorX() - canvasScrollX + BLOCK_W / 2;
                    int toY = canvasTop + next.getEditorY() - canvasScrollY;
                    drawArrow(g, fromX, fromY, toX, toY, COL_ARROW);
                }
            }
        }

        // Bloecke
        for (ScenarioObjective obj : currentScenario.getObjectives()) {
            renderBlock(g, obj, canvasLeft, canvasTop, mx, my);
        }

        // Hinweis bei leerem Canvas
        if (currentScenario.getObjectives().size() <= 2) {
            String hint = "\u00A78Bausteine aus der Palette hierher ziehen";
            g.drawString(this.font, hint,
                    canvasLeft + (canvasRight - canvasLeft) / 2 - this.font.width(hint) / 2,
                    canvasTop + (canvasBottom - canvasTop) / 2, 0x444444);
        }

        g.disableScissor();
    }

    private void renderBlock(GuiGraphics g, ScenarioObjective obj,
                             int canvasLeft, int canvasTop, int mx, int my) {
        int bx = canvasLeft + obj.getEditorX() - canvasScrollX;
        int by = canvasTop + obj.getEditorY() - canvasScrollY;

        boolean isSelected = obj.getId().equals(selectedBlockId);
        boolean isConnectSource = connecting && obj.getId().equals(connectSourceId);
        int typeColor = obj.getType().getColor();
        int darkerColor = obj.getType().getDarkerColor();

        // Block-Schatten
        g.fill(bx + 2, by + 2, bx + BLOCK_W + 2, by + BLOCK_H + 2, 0x44000000);

        // Block-Koerper
        g.fill(bx, by, bx + BLOCK_W, by + BLOCK_H, darkerColor);
        g.fill(bx + 1, by + 1, bx + BLOCK_W - 1, by + BLOCK_H - 1,
                isSelected ? blendColor(typeColor, COL_SELECTED) : typeColor);

        // Selektionsrahmen
        if (isSelected) {
            g.fill(bx - 1, by - 1, bx + BLOCK_W + 1, by, COL_SELECTED);
            g.fill(bx - 1, by + BLOCK_H, bx + BLOCK_W + 1, by + BLOCK_H + 1, COL_SELECTED);
            g.fill(bx - 1, by, bx, by + BLOCK_H, COL_SELECTED);
            g.fill(bx + BLOCK_W, by, bx + BLOCK_W + 1, by + BLOCK_H, COL_SELECTED);
        }

        // Icon-Box links
        g.fill(bx + 2, by + 2, bx + 14, by + 14, darkerColor);
        g.drawCenteredString(this.font, obj.getType().getIcon(), bx + 8, by + 4, 0xFFFFFF);

        // Typ-Name
        g.drawString(this.font, "\u00A7l" + obj.getType().getDisplayName(), bx + 16, by + 4, 0xFFFFFF);

        // Parameter-Zusammenfassung (klein)
        String summary = obj.getParamSummary();
        if (!summary.isEmpty()) {
            String trimmed = summary.length() > 16 ? summary.substring(0, 15) + ".." : summary;
            g.drawString(this.font, "\u00A77" + trimmed, bx + 4, by + BLOCK_H - 12, 0xAAAAAA);
        }

        // Output-Konnektor (unten, wenn nicht REWARD)
        if (obj.getType() != ObjectiveType.REWARD) {
            int cx = bx + BLOCK_W / 2;
            int cy = by + BLOCK_H;
            int connColor = isConnectSource ? COL_CONNECTING : (obj.getNextObjectiveId() != null ? 0xFFE94560 : 0xFF555555);
            g.fill(cx - CONNECTOR_R, cy - 1, cx + CONNECTOR_R, cy + CONNECTOR_R + 1, connColor);
        }

        // Input-Konnektor (oben, wenn nicht START)
        if (obj.getType() != ObjectiveType.START) {
            int cx = bx + BLOCK_W / 2;
            int cy = by;
            g.fill(cx - CONNECTOR_R, cy - CONNECTOR_R, cx + CONNECTOR_R, cy + 1, 0xFF555555);
        }
    }

    private void renderProperties(GuiGraphics g) {
        if (selectedBlockId == null) return;
        ScenarioObjective obj = currentScenario.getObjective(selectedBlockId);
        if (obj == null) return;

        int propH = getPropsHeight(obj);
        int propTop = this.height - STATUS_H - propH;

        g.fill(0, propTop, PALETTE_W, this.height - STATUS_H, COL_PROPS_BG);
        g.fill(0, propTop, PALETTE_W, propTop + 1, 0xFF2A2A4A);
        g.fill(PALETTE_W - 1, propTop, PALETTE_W, this.height - STATUS_H, 0xFF2A2A4A);

        // Header
        g.drawString(this.font, "\u00A7l" + obj.getType().getDisplayName(), 4, propTop + 3, obj.getType().getColor());

        // Parameter-Labels
        int y = propTop + 14;
        for (ObjectiveType.ParamDef def : obj.getType().getParamDefs()) {
            g.drawString(this.font, "\u00A77" + def.label() + ":", PALETTE_W - this.font.width(def.label() + ":") - 6, y + 1, 0x888888);
            y += 13;
        }
    }

    private void renderStatusBar(GuiGraphics g) {
        int y = this.height - STATUS_H;
        g.fill(0, y, this.width, this.height, COL_STATUS);
        g.fill(0, y, this.width, y + 1, 0xFF2A2A4A);

        // Name-Feld ist links (Widget)

        // Mitte: Block-Anzahl
        int blocks = currentScenario.getStepCount();
        String info = "\u00A78|\u00A77 Phasen: \u00A7f" + blocks;
        g.drawString(this.font, info, PALETTE_W + 4, y + 3, 0xAAAAAA);

        // Rechts: Schwierigkeit + Typ
        String diff = "\u00A77Schwierigkeit: \u00A76" + currentScenario.getDifficultyStars();
        String mType = " \u00A78|\u00A77 Typ: \u00A7f" + currentScenario.getMissionType();
        g.drawString(this.font, diff + mType, this.width - this.font.width(diff + mType) - 4, y + 3, 0xAAAAAA);
    }

    private void renderDropdowns(GuiGraphics g, int mx, int my) {
        if (showOpenDropdown) {
            renderDropdown(g, 4, TOOLBAR_H, allScenarios.stream()
                    .map(s -> s.getName() + (s.isActive() ? " \u00A7a\u2713" : ""))
                    .toArray(String[]::new), mx, my, 0xFF3498DB);
        }
        if (showTemplatesDropdown) {
            int offset = this.font.width("Oeffnen") + this.font.width("Speichern") + 22;
            renderDropdown(g, offset, TOOLBAR_H, ScenarioTemplates.getTemplateNames(), mx, my, 0xFFF39C12);
        }
        if (showActiveDropdown) {
            int offset = this.font.width("Oeffnen") + this.font.width("Speichern")
                    + this.font.width("Vorlagen") + 31;
            List<String> activeNames = new ArrayList<>();
            for (MissionScenario s : allScenarios) {
                activeNames.add((s.isActive() ? "\u00A7a\u25CF " : "\u00A7c\u25CB ") + "\u00A7f" + s.getName());
            }
            if (activeNames.isEmpty()) activeNames.add("\u00A78Keine Szenarien");
            renderDropdown(g, offset, TOOLBAR_H, activeNames.toArray(new String[0]), mx, my, 0xFF9B59B6);
        }
    }

    private void renderDropdown(GuiGraphics g, int x, int y, String[] items, int mx, int my, int accentColor) {
        int w = 120;
        int itemH = 13;
        int h = items.length * itemH + 4;

        g.fill(x, y, x + w, y + h, 0xEE16213E);
        g.fill(x, y, x + w, y + 1, accentColor);
        g.fill(x, y, x + 1, y + h, accentColor & 0x66FFFFFF);
        g.fill(x + w - 1, y, x + w, y + h, accentColor & 0x66FFFFFF);
        g.fill(x, y + h - 1, x + w, y + h, accentColor & 0x66FFFFFF);

        for (int i = 0; i < items.length; i++) {
            int iy = y + 2 + i * itemH;
            boolean hover = mx >= x && mx < x + w && my >= iy && my < iy + itemH;
            if (hover) g.fill(x + 1, iy, x + w - 1, iy + itemH, accentColor & 0x33FFFFFF);
            String text = items[i].length() > 18 ? items[i].substring(0, 17) + ".." : items[i];
            g.drawString(this.font, text, x + 4, iy + 2, 0xDDDDDD);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ZEICHENHILFEN
    // ═══════════════════════════════════════════════════════════

    private void drawArrow(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int midY = y1 + (y2 - y1) / 2;
        // Vertikale Linie von Quelle nach Mitte
        g.fill(x1, y1, x1 + 1, midY, color);
        // Horizontale Linie
        if (x1 != x2) {
            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            g.fill(minX, midY, maxX + 1, midY + 1, color);
        }
        // Vertikale Linie von Mitte zum Ziel
        g.fill(x2, midY, x2 + 1, y2, color);
        // Pfeilspitze
        g.fill(x2 - 3, y2 - 5, x2 + 4, y2, color);
        g.fill(x2 - 2, y2 - 4, x2 + 3, y2, color);
    }

    private void drawDashedLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1) return;
        dx /= len; dy /= len;
        int dashLen = 4;
        for (double d = 0; d < len; d += dashLen * 2) {
            int sx = x1 + (int)(dx * d);
            int sy = y1 + (int)(dy * d);
            int ex = x1 + (int)(dx * Math.min(d + dashLen, len));
            int ey = y1 + (int)(dy * Math.min(d + dashLen, len));
            g.fill(Math.min(sx, ex), Math.min(sy, ey),
                    Math.max(sx, ex) + 1, Math.max(sy, ey) + 1, color);
        }
    }

    private int blendColor(int c1, int c2) {
        int r = (((c1 >> 16) & 0xFF) + ((c2 >> 16) & 0xFF)) / 2;
        int g = (((c1 >> 8) & 0xFF) + ((c2 >> 8) & 0xFF)) / 2;
        int b = ((c1 & 0xFF) + (c2 & 0xFF)) / 2;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    // ═══════════════════════════════════════════════════════════
    // MAUS-EVENTS
    // ═══════════════════════════════════════════════════════════

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Dropdown-Klicks abfangen
        if (showOpenDropdown || showTemplatesDropdown || showActiveDropdown) {
            boolean handled = handleDropdownClick(mx, my);
            showOpenDropdown = false;
            showTemplatesDropdown = false;
            showActiveDropdown = false;
            if (handled) return true;
        }

        // Toolbar
        if (my < TOOLBAR_H) {
            handleToolbarClick(mx, my);
            return true;
        }

        // Palette
        if (mx < PALETTE_W && my >= TOOLBAR_H && my < this.height - STATUS_H) {
            handlePaletteClick(mx, my);
            return true;
        }

        // Canvas
        if (mx >= PALETTE_W && my >= TOOLBAR_H && my < this.height - STATUS_H) {
            handleCanvasClick(mx, my, button);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleToolbarClick(int mx, int my) {
        int x = 4;
        int w;

        // Oeffnen
        w = this.font.width("Oeffnen") + 8;
        if (mx >= x && mx < x + w) { showOpenDropdown = !showOpenDropdown; showTemplatesDropdown = false; showActiveDropdown = false; return; }
        x += w + 3;

        // Speichern
        w = this.font.width("Speichern") + 8;
        if (mx >= x && mx < x + w) { saveCurrentScenario(); return; }
        x += w + 3;

        // Vorlagen
        w = this.font.width("Vorlagen") + 8;
        if (mx >= x && mx < x + w) { showTemplatesDropdown = !showTemplatesDropdown; showOpenDropdown = false; showActiveDropdown = false; return; }
        x += w + 3;

        // Aktive
        w = this.font.width("Aktive") + 8;
        if (mx >= x && mx < x + w) { showActiveDropdown = !showActiveDropdown; showOpenDropdown = false; showTemplatesDropdown = false; return; }

        // Rechts: X (Schliessen)
        int rx = this.width - 4;
        w = this.font.width("X") + 8;
        if (mx >= rx - w && mx < rx) { this.onClose(); return; }
        rx -= w + 3;

        // Loeschen
        w = this.font.width("Loeschen") + 8;
        if (mx >= rx - w && mx < rx) { deleteSelectedBlock(); return; }
        rx -= w + 3;

        // Neu
        w = this.font.width("Neu") + 8;
        if (mx >= rx - w && mx < rx) { newScenario(); }
    }

    private void handlePaletteClick(int mx, int my) {
        int propH = 0;
        if (selectedBlockId != null) {
            ScenarioObjective sel = currentScenario.getObjective(selectedBlockId);
            propH = getPropsHeight(sel);
        }
        int paletteBottom = this.height - STATUS_H - propH;
        if (my >= paletteBottom) return; // Click in Properties-Bereich

        int y = TOOLBAR_H + 14 - paletteScrollY;
        for (Map.Entry<ObjectiveType.Category, Boolean> entry : categoryExpanded.entrySet()) {
            ObjectiveType.Category cat = entry.getKey();
            boolean expanded = entry.getValue();

            if (my >= y && my < y + 11) {
                entry.setValue(!expanded);
                return;
            }
            y += 12;

            if (expanded) {
                for (ObjectiveType type : ObjectiveType.values()) {
                    if (type.getCategory() != cat) continue;
                    if (my >= y && my < y + 14) {
                        addBlockFromPalette(type);
                        return;
                    }
                    y += 15;
                }
            }
            y += 3;
        }
    }

    private void handleCanvasClick(int mx, int my, int button) {
        int canvasLeft = PALETTE_W;
        int canvasTop = TOOLBAR_H;

        // Rechtsklick: Verbindung loeschen / Deselektieren
        if (button == 1) {
            if (connecting) {
                connecting = false;
                connectSourceId = null;
                return;
            }
            // Rechtsklick auf Block: Verbindung loeschen
            for (ScenarioObjective obj : currentScenario.getObjectives()) {
                int bx = canvasLeft + obj.getEditorX() - canvasScrollX;
                int by = canvasTop + obj.getEditorY() - canvasScrollY;
                if (mx >= bx && mx < bx + BLOCK_W && my >= by && my < by + BLOCK_H) {
                    obj.setNextObjectiveId(null);
                    return;
                }
            }
            selectedBlockId = null;
            rebuildProperties();
            return;
        }

        // Connecting-Mode: Klick auf Ziel-Block
        if (connecting && connectSourceId != null) {
            for (ScenarioObjective obj : currentScenario.getObjectives()) {
                if (obj.getId().equals(connectSourceId)) continue;
                int bx = canvasLeft + obj.getEditorX() - canvasScrollX;
                int by = canvasTop + obj.getEditorY() - canvasScrollY;
                if (mx >= bx && mx < bx + BLOCK_W && my >= by && my < by + BLOCK_H) {
                    ScenarioObjective src = currentScenario.getObjective(connectSourceId);
                    if (src != null) src.setNextObjectiveId(obj.getId());
                    connecting = false;
                    connectSourceId = null;
                    return;
                }
            }
            connecting = false;
            connectSourceId = null;
            return;
        }

        // Klick auf Konnektor (Output, unterhalb Block)
        for (ScenarioObjective obj : currentScenario.getObjectives()) {
            if (obj.getType() == ObjectiveType.REWARD) continue;
            int bx = canvasLeft + obj.getEditorX() - canvasScrollX;
            int by = canvasTop + obj.getEditorY() - canvasScrollY;
            int cx = bx + BLOCK_W / 2;
            int cy = by + BLOCK_H;
            if (mx >= cx - CONNECTOR_R - 2 && mx <= cx + CONNECTOR_R + 2
                    && my >= cy - 3 && my <= cy + CONNECTOR_R + 3) {
                connecting = true;
                connectSourceId = obj.getId();
                obj.setNextObjectiveId(null); // Alte Verbindung loeschen
                return;
            }
        }

        // Klick auf Block -> Selektieren + Drag starten
        // Umgekehrte Reihenfolge damit oberste Bloecke zuerst getroffen werden
        List<ScenarioObjective> objs = currentScenario.getObjectives();
        for (int i = objs.size() - 1; i >= 0; i--) {
            ScenarioObjective obj = objs.get(i);
            int bx = canvasLeft + obj.getEditorX() - canvasScrollX;
            int by = canvasTop + obj.getEditorY() - canvasScrollY;
            if (mx >= bx && mx < bx + BLOCK_W && my >= by && my < by + BLOCK_H) {
                selectedBlockId = obj.getId();
                dragging = true;
                dragOffsetX = mx - bx;
                dragOffsetY = my - by;
                rebuildProperties();
                return;
            }
        }

        // Klick auf leeren Canvas -> Deselektieren
        selectedBlockId = null;
        rebuildProperties();
    }

    private boolean handleDropdownClick(int mx, int my) {
        if (showOpenDropdown && !allScenarios.isEmpty()) {
            int x = 4, y = TOOLBAR_H;
            int itemH = 13;
            for (int i = 0; i < allScenarios.size(); i++) {
                int iy = y + 2 + i * itemH;
                if (mx >= x && mx < x + 120 && my >= iy && my < iy + itemH) {
                    loadScenario(allScenarios.get(i));
                    return true;
                }
            }
        }
        if (showTemplatesDropdown) {
            String[] names = ScenarioTemplates.getTemplateNames();
            List<MissionScenario> templates = ScenarioTemplates.getAll();
            int offset = this.font.width("Oeffnen") + this.font.width("Speichern") + 22;
            int itemH = 13;
            for (int i = 0; i < names.length; i++) {
                int iy = TOOLBAR_H + 2 + i * itemH;
                if (mx >= offset && mx < offset + 120 && my >= iy && my < iy + itemH) {
                    loadScenario(templates.get(i));
                    return true;
                }
            }
        }
        if (showActiveDropdown && !allScenarios.isEmpty()) {
            int offset = this.font.width("Oeffnen") + this.font.width("Speichern")
                    + this.font.width("Vorlagen") + 31;
            int itemH = 13;
            for (int i = 0; i < allScenarios.size(); i++) {
                int iy = TOOLBAR_H + 2 + i * itemH;
                if (mx >= offset && mx < offset + 120 && my >= iy && my < iy + itemH) {
                    MissionScenario s = allScenarios.get(i);
                    s.setActive(!s.isActive());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && selectedBlockId != null && button == 0) {
            int mx = (int) mouseX;
            int my = (int) mouseY;
            int canvasLeft = PALETTE_W;
            int canvasTop = TOOLBAR_H;

            ScenarioObjective obj = currentScenario.getObjective(selectedBlockId);
            if (obj != null) {
                int newX = mx - canvasLeft - dragOffsetX + canvasScrollX;
                int newY = my - canvasTop - dragOffsetY + canvasScrollY;
                // Grid-Snap
                newX = Math.max(0, (newX / GRID) * GRID);
                newY = Math.max(0, (newY / GRID) * GRID);
                obj.setEditorX(newX);
                obj.setEditorY(newY);
            }
            return true;
        }

        // Canvas-Panning mit mittlerer Maustaste
        if (button == 2) {
            canvasScrollX -= (int) dragX;
            canvasScrollY -= (int) dragY;
            canvasScrollX = Math.max(0, canvasScrollX);
            canvasScrollY = Math.max(0, canvasScrollY);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Palette scrollen
        if (mx < PALETTE_W && my >= TOOLBAR_H) {
            paletteScrollY -= (int) (delta * 10);
            paletteScrollY = Math.max(0, paletteScrollY);
            return true;
        }

        // Canvas scrollen (vertikal)
        if (mx >= PALETTE_W && my >= TOOLBAR_H && my < this.height - STATUS_H) {
            canvasScrollY -= (int) (delta * 20);
            canvasScrollY = Math.max(0, canvasScrollY);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Delete-Taste: Ausgewaehlten Block loeschen
        if (keyCode == 261 && selectedBlockId != null) { // 261 = DELETE
            deleteSelectedBlock();
            return true;
        }
        // Escape: Connecting abbrechen oder Screen schliessen
        if (keyCode == 256) {
            if (connecting) {
                connecting = false;
                connectSourceId = null;
                return true;
            }
        }
        // +/- fuer Schwierigkeit
        if (keyCode == 334 && (modifiers & 2) != 0) { // Ctrl+Plus
            currentScenario.setDifficulty(currentScenario.getDifficulty() + 1);
            return true;
        }
        if (keyCode == 333 && (modifiers & 2) != 0) { // Ctrl+Minus
            currentScenario.setDifficulty(currentScenario.getDifficulty() - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ═══════════════════════════════════════════════════════════
    // AKTIONEN
    // ═══════════════════════════════════════════════════════════

    private void addBlockFromPalette(ObjectiveType type) {
        String id = currentScenario.nextObjectiveId();
        // Position: Mitte des sichtbaren Canvas
        int centerX = canvasScrollX + (this.width - PALETTE_W) / 2 - BLOCK_W / 2;
        int centerY = canvasScrollY + (this.height - TOOLBAR_H - STATUS_H) / 2 - BLOCK_H / 2;
        centerX = Math.max(0, (centerX / GRID) * GRID);
        centerY = Math.max(0, (centerY / GRID) * GRID);

        ScenarioObjective obj = new ScenarioObjective(id, type, centerX, centerY);
        currentScenario.addObjective(obj);
        selectedBlockId = id;
        rebuildProperties();
    }

    private void deleteSelectedBlock() {
        if (selectedBlockId == null) return;
        ScenarioObjective obj = currentScenario.getObjective(selectedBlockId);
        if (obj == null) return;
        // START und REWARD nicht loeschen
        if (obj.getType() == ObjectiveType.START || obj.getType() == ObjectiveType.REWARD) return;

        currentScenario.removeObjective(selectedBlockId);
        selectedBlockId = null;
        rebuildProperties();
    }

    private void newScenario() {
        currentScenario = new MissionScenario();
        selectedBlockId = null;
        canvasScrollX = 0;
        canvasScrollY = 0;
        if (nameField != null) nameField.setValue(currentScenario.getName());
        rebuildProperties();
    }

    private void loadScenario(MissionScenario scenario) {
        // Tiefe Kopie laden
        String json = ScenarioManager.scenarioToJson(scenario);
        currentScenario = ScenarioManager.fromJson(json);
        selectedBlockId = null;
        canvasScrollX = 0;
        canvasScrollY = 0;
        if (nameField != null) nameField.setValue(currentScenario.getName());
        rebuildProperties();
    }

    private void saveCurrentScenario() {
        // In allScenarios aktualisieren oder hinzufuegen
        boolean found = false;
        for (int i = 0; i < allScenarios.size(); i++) {
            if (allScenarios.get(i).getId().equals(currentScenario.getId())) {
                allScenarios.set(i, currentScenario);
                found = true;
                break;
            }
        }
        if (!found) {
            allScenarios.add(currentScenario);
        }

        // Netzwerk-Paket zum Server senden
        String json = ScenarioManager.scenarioToJson(currentScenario);
        de.rolandsw.schedulemc.gang.network.GangNetworkHandler.sendToServer(
                new de.rolandsw.schedulemc.gang.network.SaveScenarioPacket(json)
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
