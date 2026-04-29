package de.rolandsw.schedulemc.client.gui.config;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.tobacco.network.ModNetworking;
import de.rolandsw.schedulemc.tobacco.network.ReloadEconomyPricesPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-Game Config-Screen für alle EconomyController Referenzpreise.
 * Jedes Produkt hat ein Preisfeld (€/Einheit). TÖPFE und NAHRUNG ausgenommen.
 * Alle Texte werden über Übersetzungsschlüssel geladen.
 */
@OnlyIn(Dist.CLIENT)
public class EconomyPricesConfigScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int START_Y       = 58;
    private static final int ROW_HEIGHT    = 20;
    private static final int HEADER_HEIGHT = 18;
    private static final int BOTTOM_RESERVE = 52;
    private static final int LABEL_X       = 30;
    private static final int FIELD_WIDTH   = 90;

    // ── Produkt-Definitionen ─────────────────────────────────────────────────
    // Null in [0] → Kategorie-Header: [null, §-Farbcode, Translation-Key]
    // Sonst Produkt-Zeile:            [productId, Translation-Key des Namens]
    private static final String[][] PRODUCT_DATA = {
        // CANNABIS
        {null, "§c", "gui.economy_prices.cat.cannabis"},
        {"CANNABIS_INDICA",    "enum.cannabis_strain.indica"},
        {"CANNABIS_SATIVA",    "enum.cannabis_strain.sativa"},
        {"CANNABIS_HYBRID",    "enum.cannabis_strain.hybrid"},
        {"CANNABIS_AUTOFLOWER","enum.cannabis_strain.autoflower"},
        // TABAK
        {null, "§6", "gui.economy_prices.cat.tobacco"},
        {"TOBACCO_VIRGINIA",   "tobacco.type.virginia"},
        {"TOBACCO_BURLEY",     "tobacco.type.burley"},
        {"TOBACCO_ORIENTAL",   "tobacco.type.oriental"},
        {"TOBACCO_HAVANA",     "tobacco.type.havana"},
        // KOKAIN
        {null, "§7", "gui.economy_prices.cat.cocaine"},
        {"COCA_BOLIVIANISCH",  "enum.coca_type.bolivianisch"},
        {"COCA_PERUANISCH",    "enum.coca_type.peruanisch"},
        {"COCA_KOLUMBIANISCH", "enum.coca_type.kolumbianisch"},
        {"CRACK_ROCK",         "gui.economy_prices.product.crack_rock"},
        // HEROIN
        {null, "§5", "gui.economy_prices.cat.heroin"},
        {"POPPY_INDISCH",      "enum.poppy_type.indisch"},
        {"POPPY_TUERKISCH",    "enum.poppy_type.tuerkisch"},
        {"POPPY_AFGHANISCH",   "enum.poppy_type.afghanisch"},
        // METH
        {null, "§b", "gui.economy_prices.cat.meth"},
        {"METH_STANDARD",      "enum.meth_quality.standard"},
        {"METH_GUT",           "enum.meth_quality.gut"},
        {"METH_BLUE_SKY",      "enum.meth_quality.blue_sky"},
        // MDMA
        {null, "§d", "gui.economy_prices.cat.mdma"},
        {"MDMA_SCHLECHT",      "enum.mdma_quality.schlecht"},
        {"MDMA_STANDARD",      "enum.mdma_quality.standard"},
        {"MDMA_GUT",           "enum.mdma_quality.gut"},
        {"MDMA_PREMIUM",       "enum.mdma_quality.premium"},
        // LSD
        {null, "§e", "gui.economy_prices.cat.lsd"},
        {"LSD_SCHWACH",        "lsd.quality.schwach"},
        {"LSD_STANDARD",       "lsd.quality.standard"},
        {"LSD_STARK",          "lsd.quality.stark"},
        {"LSD_BICYCLE_DAY",    "lsd.quality.bicycle_day"},
        // PILZE
        {null, "§2", "gui.economy_prices.cat.mushroom"},
        {"MUSHROOM_MEXICANA",  "mushroom.type.mexicana"},
        {"MUSHROOM_CUBENSIS",  "mushroom.type.cubensis"},
        {"MUSHROOM_AZURESCENS","mushroom.type.azurescens"},
        // WEIN
        {null, "§9", "gui.economy_prices.cat.wine"},
        {"WINE_RIESLING",      "wine.type.riesling"},
        {"WINE_CHARDONNAY",    "wine.type.chardonnay"},
        {"WINE_SPAETBURGUNDER","wine.type.spaetburgunder"},
        {"WINE_MERLOT",        "wine.type.merlot"},
        // BIER
        {null, "§8", "gui.economy_prices.cat.beer"},
        {"BEER_PILSNER",       "beer.type.pilsner"},
        {"BEER_WEIZEN",        "beer.type.weizen"},
        {"BEER_ALE",           "beer.type.ale"},
        {"BEER_STOUT",         "beer.type.stout"},
        // KAFFEE
        {null, "§6", "gui.economy_prices.cat.coffee"},
        {"COFFEE_ARABICA",     "coffee.type.arabica"},
        {"COFFEE_ROBUSTA",     "coffee.type.robusta"},
        {"COFFEE_LIBERICA",    "coffee.type.liberica"},
        {"COFFEE_EXCELSA",     "coffee.type.excelsa"},
        // KÄSE
        {null, "§e", "gui.economy_prices.cat.cheese"},
        {"CHEESE_GOUDA",       "cheese.type.gouda"},
        {"CHEESE_EMMENTAL",    "cheese.type.emmental"},
        {"CHEESE_CAMEMBERT",   "cheese.type.camembert"},
        {"CHEESE_PARMESAN",    "cheese.type.parmesan"},
        // SCHOKOLADE
        {null, "§6", "gui.economy_prices.cat.chocolate"},
        {"CHOCOLATE_WHITE",    "chocolate.type.white"},
        {"CHOCOLATE_MILK",     "chocolate.type.milk"},
        {"CHOCOLATE_DARK",     "chocolate.type.dark"},
        {"CHOCOLATE_RUBY",     "chocolate.type.ruby"},
        // HONIG
        {null, "§e", "gui.economy_prices.cat.honey"},
        {"HONEY_ACACIA",       "honey.type.acacia"},
        {"HONEY_WILDFLOWER",   "honey.type.wildflower"},
        {"HONEY_FOREST",       "honey.type.forest"},
        {"HONEY_MANUKA",       "honey.type.manuka"},
        // FAHRZEUGE
        {null, "§7", "gui.economy_prices.cat.vehicles"},
        {"VEHICLE_OAK",        "vehicle.type.oak"},
        {"VEHICLE_BIG_OAK",    "vehicle.type.big_oak"},
        {"VEHICLE_SUV",        "vehicle.type.suv"},
        {"VEHICLE_TRANSPORTER","vehicle.type.transporter"},
        {"VEHICLE_SPORT",      "vehicle.type.sport"},
    };

    // ── State ─────────────────────────────────────────────────────────────────
    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();
    private final Map<String, EditBox> editBoxes = new HashMap<>();

    private int scrollOffset = 0;
    private int listHeight;
    private int fieldX;

    // ── Konstruktor & Init ────────────────────────────────────────────────────

    public EconomyPricesConfigScreen(Screen parent) {
        super(Component.translatable("gui.economy_prices.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        listHeight = this.height - START_Y - BOTTOM_RESERVE;
        fieldX     = this.width - FIELD_WIDTH - 20;

        Map<String, Double> currentPrices = loadConfigPrices();
        rows.clear();
        editBoxes.clear();

        for (String[] entry : PRODUCT_DATA) {
            if (entry[0] == null) {
                // Kategorie-Header: Farbe + "■ " + übersetzter Name
                Component label = Component.literal(entry[1] + "■ ")
                    .append(Component.translatable(entry[2]));
                rows.add(new Row(label));
            } else {
                String productId = entry[0];
                String nameKey   = entry[1];
                double current   = currentPrices.getOrDefault(productId, 0.0);

                EditBox box = new EditBox(this.font, fieldX, 0, FIELD_WIDTH, 18, Component.empty());
                box.setMaxLength(12);
                box.setValue(current > 0 ? formatPrice(current) : "");
                box.setFilter(s -> s.matches("[0-9]*\\.?[0-9]*"));
                this.addRenderableWidget(box);

                rows.add(new Row(productId, nameKey, box));
                editBoxes.put(productId, box);
            }
        }

        refreshLayout();

        int btnY = this.height - BOTTOM_RESERVE + 16;
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.economy_prices.btn_save"),
            btn -> saveAll()
        ).bounds(this.width / 2 - 152, btnY, 100, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.economy_prices.btn_reset"),
            btn -> resetToDefaults()
        ).bounds(this.width / 2 - 50, btnY, 100, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.economy_prices.btn_back"),
            btn -> minecraft.setScreen(parent)
        ).bounds(this.width / 2 + 52, btnY, 100, 20).build());
    }

    // ── Layout / Scroll ────────────────────────────────────────────────────────

    private void refreshLayout() {
        int maxScroll = getMaxScroll();
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        int contentY = START_Y - scrollOffset;
        for (Row row : rows) {
            int rowH = row.isHeader ? HEADER_HEIGHT : ROW_HEIGHT;
            if (!row.isHeader && row.editBox != null) {
                int boxY    = contentY + (rowH - 18) / 2;
                boolean vis = boxY + 18 > START_Y && boxY < START_Y + listHeight;
                row.editBox.setY(boxY);
                row.editBox.setX(fieldX);
                row.editBox.visible = vis;
                row.editBox.active  = vis;
            }
            row.renderY = contentY;
            contentY += rowH;
        }
    }

    private int getTotalContentHeight() {
        int h = 0;
        for (Row row : rows) h += row.isHeader ? HEADER_HEIGHT : ROW_HEIGHT;
        return h;
    }

    private int getMaxScroll() {
        return Math.max(0, getTotalContentHeight() - listHeight + 4);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta == 0) return false;
        scrollOffset -= (int)(delta * 18);
        refreshLayout();
        return true;
    }

    // ── Render ─────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        int listBottom = START_Y + listHeight;
        g.fill(10, START_Y - 4, this.width - 10, listBottom + 4, 0x22000000);

        // Scissor: EditBoxes außerhalb der Liste ausblenden
        g.enableScissor(10, START_Y, this.width - 10, listBottom);

        for (Row row : rows) {
            int y = row.renderY;
            if (y + ROW_HEIGHT < START_Y || y > listBottom) continue;

            if (row.isHeader) {
                g.drawString(this.font, row.headerLabel, LABEL_X, y + 3, 0xFFFFAA, false);
            } else {
                // Produkt-Name links
                g.drawString(this.font, Component.translatable(row.nameKey),
                    LABEL_X + 10, y + 5, 0xDDDDDD, false);
                // Währungssymbol rechts neben dem Feld
                g.drawString(this.font, Component.translatable("gui.economy_prices.currency"),
                    fieldX + FIELD_WIDTH + 3, y + 5, 0xAAAAAA, false);
            }
        }

        g.disableScissor();

        // Scrollbar
        int maxScroll = getMaxScroll();
        if (maxScroll > 0) {
            int trackX   = this.width - 14;
            int trackTop = START_Y;
            int trackBot = listBottom;
            int trackH   = trackBot - trackTop;
            g.fill(trackX, trackTop, trackX + 4, trackBot, 0x44FFFFFF);
            int thumbH = Math.max(12, trackH * listHeight / (listHeight + maxScroll));
            int thumbY = trackTop + (trackH - thumbH) * scrollOffset / Math.max(1, maxScroll);
            g.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, 0xAAFFFFFF);
        }

        // Widgets (EditBoxes + Buttons)
        super.render(g, mouseX, mouseY, partialTick);

        // Titelzeilen
        g.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        g.drawCenteredString(this.font,
            Component.translatable("gui.economy_prices.subtitle"),
            this.width / 2, 22, 0xAAAA55);
        g.drawCenteredString(this.font,
            Component.translatable("gui.economy_prices.hint"),
            this.width / 2, 35, 0x666666);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    // ── Speichern / Reset ──────────────────────────────────────────────────────

    private void saveAll() {
        List<String> prices = new ArrayList<>();
        for (Map.Entry<String, EditBox> e : editBoxes.entrySet()) {
            String raw = e.getValue().getValue().trim();
            if (raw.isEmpty()) continue;
            try {
                double val = Double.parseDouble(raw);
                if (val > 0) prices.add(e.getKey() + "=" + val);
            } catch (NumberFormatException ignored) {}
        }
        ModConfigHandler.COMMON.PRODUCT_PRICES.set(prices);
        ModConfigHandler.SPEC.save();
        ModNetworking.sendToServer(new ReloadEconomyPricesPacket());
        minecraft.setScreen(parent);
    }

    private void resetToDefaults() {
        Map<String, Double> defaults = loadConfigPrices();
        for (Map.Entry<String, EditBox> e : editBoxes.entrySet()) {
            double val = defaults.getOrDefault(e.getKey(), 0.0);
            e.getValue().setValue(val > 0 ? formatPrice(val) : "");
        }
    }

    // ── Hilfsmethoden ──────────────────────────────────────────────────────────

    private static Map<String, Double> loadConfigPrices() {
        Map<String, Double> map = new HashMap<>();
        try {
            for (String entry : ModConfigHandler.COMMON.PRODUCT_PRICES.get()) {
                String[] parts = entry.split("=", 2);
                if (parts.length == 2) {
                    try {
                        map.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception ignored) {}
        return map;
    }

    private static String formatPrice(double price) {
        if (price == Math.floor(price) && price < 1_000_000) {
            return String.valueOf((long) price);
        }
        return String.format("%.4f", price).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    // ── Zeilen-Modell ──────────────────────────────────────────────────────────

    private static class Row {
        final boolean   isHeader;
        final Component headerLabel; // nur bei Headern
        final String    productId;   // nur bei Produkten
        final String    nameKey;     // Übersetzungsschlüssel des Produktnamens
        final EditBox   editBox;
        int             renderY;

        /** Kategorie-Header */
        Row(Component headerLabel) {
            this.isHeader    = true;
            this.headerLabel = headerLabel;
            this.productId   = null;
            this.nameKey     = null;
            this.editBox     = null;
        }

        /** Produkt-Zeile */
        Row(String productId, String nameKey, EditBox editBox) {
            this.isHeader    = false;
            this.headerLabel = null;
            this.productId   = productId;
            this.nameKey     = nameKey;
            this.editBox     = editBox;
        }
    }
}
