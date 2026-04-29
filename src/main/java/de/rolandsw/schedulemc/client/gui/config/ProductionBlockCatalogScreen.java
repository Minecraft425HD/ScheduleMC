package de.rolandsw.schedulemc.client.gui.config;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.tobacco.network.ModNetworking;
import de.rolandsw.schedulemc.tobacco.network.ReloadBlockCatalogPacket;
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
 * In-Game Config-Screen für alle Produktionsblock-Preise und Level-Voraussetzungen.
 * Drei Spalten pro Produkt: Name | Preis-EditBox | Level-EditBox
 */
@OnlyIn(Dist.CLIENT)
public class ProductionBlockCatalogScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int START_Y        = 58;
    private static final int ROW_HEIGHT     = 20;
    private static final int HEADER_HEIGHT  = 18;
    private static final int BOTTOM_RESERVE = 52;
    private static final int LABEL_X        = 30;
    private static final int PRICE_WIDTH    = 75;
    private static final int LEVEL_WIDTH    = 45;
    private static final int COL_GAP        = 4;

    // ── Block-Daten ───────────────────────────────────────────────────────────
    // {null, "§Farbcode", "header.translation.key"}  → Kategorie-Header
    // {"schedulemc:block_name", "block.schedulemc.block_name"}  → Produkt
    private static final String[][] BLOCK_DATA = {
        // CANNABIS
        {null, "§c", "gui.block_catalog.cat.cannabis"},
        {"schedulemc:cannabis_trimm_station",   "block.schedulemc.cannabis_trimm_station"},
        {"schedulemc:cannabis_curing_glas",     "block.schedulemc.cannabis_curing_glas"},
        {"schedulemc:cannabis_hash_presse",     "block.schedulemc.cannabis_hash_presse"},
        {"schedulemc:cannabis_oel_extraktor",   "block.schedulemc.cannabis_oel_extraktor"},
        // TABAK
        {null, "§6", "gui.block_catalog.cat.tobacco"},
        {"schedulemc:terracotta_pot",           "block.schedulemc.terracotta_pot"},
        {"schedulemc:ceramic_pot",              "block.schedulemc.ceramic_pot"},
        {"schedulemc:iron_pot",                 "block.schedulemc.iron_pot"},
        {"schedulemc:golden_pot",               "block.schedulemc.golden_pot"},
        {"schedulemc:small_drying_rack",        "block.schedulemc.small_drying_rack"},
        {"schedulemc:medium_drying_rack",       "block.schedulemc.medium_drying_rack"},
        {"schedulemc:big_drying_rack",          "block.schedulemc.big_drying_rack"},
        {"schedulemc:small_fermentation_barrel","block.schedulemc.small_fermentation_barrel"},
        {"schedulemc:medium_fermentation_barrel","block.schedulemc.medium_fermentation_barrel"},
        {"schedulemc:big_fermentation_barrel",  "block.schedulemc.big_fermentation_barrel"},
        {"schedulemc:small_packaging_table",    "block.schedulemc.small_packaging_table"},
        {"schedulemc:medium_packaging_table",   "block.schedulemc.medium_packaging_table"},
        {"schedulemc:large_packaging_table",    "block.schedulemc.large_packaging_table"},
        {"schedulemc:basic_grow_light_slab",    "block.schedulemc.basic_grow_light_slab"},
        {"schedulemc:advanced_grow_light_slab", "block.schedulemc.advanced_grow_light_slab"},
        {"schedulemc:premium_grow_light_slab",  "block.schedulemc.premium_grow_light_slab"},
        {"schedulemc:sink",                     "block.schedulemc.sink"},
        // WEIN
        {null, "§9", "gui.block_catalog.cat.wine"},
        {"schedulemc:crushing_station",         "block.schedulemc.crushing_station"},
        {"schedulemc:small_wine_press",         "block.schedulemc.small_wine_press"},
        {"schedulemc:medium_wine_press",        "block.schedulemc.medium_wine_press"},
        {"schedulemc:large_wine_press",         "block.schedulemc.large_wine_press"},
        {"schedulemc:small_fermentation_tank",  "block.schedulemc.small_fermentation_tank"},
        {"schedulemc:medium_fermentation_tank", "block.schedulemc.medium_fermentation_tank"},
        {"schedulemc:large_fermentation_tank",  "block.schedulemc.large_fermentation_tank"},
        {"schedulemc:small_aging_barrel",       "block.schedulemc.small_aging_barrel"},
        {"schedulemc:medium_aging_barrel",      "block.schedulemc.medium_aging_barrel"},
        {"schedulemc:large_aging_barrel",       "block.schedulemc.large_aging_barrel"},
        {"schedulemc:wine_bottling_station",    "block.schedulemc.wine_bottling_station"},
        // BIER
        {null, "§8", "gui.block_catalog.cat.beer"},
        {"schedulemc:malting_station",          "block.schedulemc.malting_station"},
        {"schedulemc:mash_tun",                 "block.schedulemc.mash_tun"},
        {"schedulemc:small_brew_kettle",        "block.schedulemc.small_brew_kettle"},
        {"schedulemc:medium_brew_kettle",       "block.schedulemc.medium_brew_kettle"},
        {"schedulemc:large_brew_kettle",        "block.schedulemc.large_brew_kettle"},
        {"schedulemc:small_beer_fermentation_tank","block.schedulemc.small_beer_fermentation_tank"},
        {"schedulemc:medium_beer_fermentation_tank","block.schedulemc.medium_beer_fermentation_tank"},
        {"schedulemc:large_beer_fermentation_tank","block.schedulemc.large_beer_fermentation_tank"},
        {"schedulemc:small_conditioning_tank",  "block.schedulemc.small_conditioning_tank"},
        {"schedulemc:medium_conditioning_tank", "block.schedulemc.medium_conditioning_tank"},
        {"schedulemc:large_conditioning_tank",  "block.schedulemc.large_conditioning_tank"},
        {"schedulemc:beer_bottling_station",    "block.schedulemc.beer_bottling_station"},
        // KAFFEE
        {null, "§6", "gui.block_catalog.cat.coffee"},
        {"schedulemc:wet_processing_station",   "block.schedulemc.wet_processing_station"},
        {"schedulemc:small_coffee_roaster",     "block.schedulemc.small_coffee_roaster"},
        {"schedulemc:medium_coffee_roaster",    "block.schedulemc.medium_coffee_roaster"},
        {"schedulemc:large_coffee_roaster",     "block.schedulemc.large_coffee_roaster"},
        {"schedulemc:coffee_grinder",           "block.schedulemc.coffee_grinder"},
        {"schedulemc:coffee_packaging_table",   "block.schedulemc.coffee_packaging_table"},
        // KÄSE
        {null, "§e", "gui.block_catalog.cat.cheese"},
        {"schedulemc:pasteurization_station",   "block.schedulemc.pasteurization_station"},
        {"schedulemc:curdling_vat",             "block.schedulemc.curdling_vat"},
        {"schedulemc:small_cheese_press",       "block.schedulemc.small_cheese_press"},
        {"schedulemc:medium_cheese_press",      "block.schedulemc.medium_cheese_press"},
        {"schedulemc:large_cheese_press",       "block.schedulemc.large_cheese_press"},
        {"schedulemc:small_aging_cave",         "block.schedulemc.small_aging_cave"},
        {"schedulemc:medium_aging_cave",        "block.schedulemc.medium_aging_cave"},
        {"schedulemc:large_aging_cave",         "block.schedulemc.large_aging_cave"},
        {"schedulemc:packaging_station",        "block.schedulemc.packaging_station"},
        // SCHOKOLADE
        {null, "§6", "gui.block_catalog.cat.chocolate"},
        {"schedulemc:roasting_station",         "block.schedulemc.roasting_station"},
        {"schedulemc:winnowing_machine",        "block.schedulemc.winnowing_machine"},
        {"schedulemc:grinding_mill",            "block.schedulemc.grinding_mill"},
        {"schedulemc:pressing_station",         "block.schedulemc.pressing_station"},
        {"schedulemc:small_conching_machine",   "block.schedulemc.small_conching_machine"},
        {"schedulemc:medium_conching_machine",  "block.schedulemc.medium_conching_machine"},
        {"schedulemc:large_conching_machine",   "block.schedulemc.large_conching_machine"},
        {"schedulemc:tempering_station",        "block.schedulemc.tempering_station"},
        {"schedulemc:small_molding_station",    "block.schedulemc.small_molding_station"},
        {"schedulemc:medium_molding_station",   "block.schedulemc.medium_molding_station"},
        {"schedulemc:large_molding_station",    "block.schedulemc.large_molding_station"},
        {"schedulemc:enrobing_machine",         "block.schedulemc.enrobing_machine"},
        {"schedulemc:cooling_tunnel",           "block.schedulemc.cooling_tunnel"},
        {"schedulemc:wrapping_station",         "block.schedulemc.wrapping_station"},
        // HONIG
        {null, "§e", "gui.block_catalog.cat.honey"},
        {"schedulemc:beehive",                  "block.schedulemc.beehive"},
        {"schedulemc:advanced_beehive",         "block.schedulemc.advanced_beehive"},
        {"schedulemc:apiary",                   "block.schedulemc.apiary"},
        {"schedulemc:honey_extractor",          "block.schedulemc.honey_extractor"},
        {"schedulemc:centrifugal_extractor",    "block.schedulemc.centrifugal_extractor"},
        {"schedulemc:filtering_station",        "block.schedulemc.filtering_station"},
        {"schedulemc:small_aging_chamber",      "block.schedulemc.small_aging_chamber"},
        {"schedulemc:medium_aging_chamber",     "block.schedulemc.medium_aging_chamber"},
        {"schedulemc:large_aging_chamber",      "block.schedulemc.large_aging_chamber"},
        {"schedulemc:processing_station",       "block.schedulemc.processing_station"},
        {"schedulemc:creaming_station",         "block.schedulemc.creaming_station"},
        {"schedulemc:bottling_station",         "block.schedulemc.bottling_station"},
        // PILZE
        {null, "§2", "gui.block_catalog.cat.mushroom"},
        {"schedulemc:klimalampe_small",         "block.schedulemc.klimalampe_small"},
        {"schedulemc:klimalampe_medium",        "block.schedulemc.klimalampe_medium"},
        {"schedulemc:klimalampe_large",         "block.schedulemc.klimalampe_large"},
        {"schedulemc:wassertank",               "block.schedulemc.wassertank"},
        // KOKAIN
        {null, "§7", "gui.block_catalog.cat.cocaine"},
        {"schedulemc:small_extraction_vat",     "block.schedulemc.small_extraction_vat"},
        {"schedulemc:medium_extraction_vat",    "block.schedulemc.medium_extraction_vat"},
        {"schedulemc:big_extraction_vat",       "block.schedulemc.big_extraction_vat"},
        {"schedulemc:small_refinery",           "block.schedulemc.small_refinery"},
        {"schedulemc:medium_refinery",          "block.schedulemc.medium_refinery"},
        {"schedulemc:big_refinery",             "block.schedulemc.big_refinery"},
        {"schedulemc:crack_kocher",             "block.schedulemc.crack_kocher"},
        // HEROIN
        {null, "§5", "gui.block_catalog.cat.heroin"},
        {"schedulemc:ritzmaschine",             "block.schedulemc.ritzmaschine"},
        {"schedulemc:opium_presse",             "block.schedulemc.opium_presse"},
        {"schedulemc:kochstation",              "block.schedulemc.kochstation"},
        {"schedulemc:heroin_raffinerie",        "block.schedulemc.heroin_raffinerie"},
        // METH
        {null, "§b", "gui.block_catalog.cat.meth"},
        {"schedulemc:chemie_mixer",             "block.schedulemc.chemie_mixer"},
        {"schedulemc:reduktionskessel",         "block.schedulemc.reduktionskessel"},
        {"schedulemc:kristallisator",           "block.schedulemc.kristallisator"},
        {"schedulemc:vakuum_trockner",          "block.schedulemc.vakuum_trockner"},
        // MDMA
        {null, "§d", "gui.block_catalog.cat.mdma"},
        {"schedulemc:reaktions_kessel",         "block.schedulemc.reaktions_kessel"},
        {"schedulemc:trocknungs_ofen",          "block.schedulemc.trocknungs_ofen"},
        {"schedulemc:pillen_presse",            "block.schedulemc.pillen_presse"},
        // LSD
        {null, "§e", "gui.block_catalog.cat.lsd"},
        {"schedulemc:fermentations_tank",       "block.schedulemc.fermentations_tank"},
        {"schedulemc:destillations_apparat",    "block.schedulemc.destillations_apparat"},
        {"schedulemc:mikro_dosierer",           "block.schedulemc.mikro_dosierer"},
        {"schedulemc:perforations_presse",      "block.schedulemc.perforations_presse"},
        // FANS
        {null, "§7", "gui.block_catalog.cat.fans"},
        {"schedulemc:fan_tier1",                "block.schedulemc.fan_tier1"},
        {"schedulemc:fan_tier2",                "block.schedulemc.fan_tier2"},
        {"schedulemc:fan_tier3",                "block.schedulemc.fan_tier3"},
    };

    // ── State ─────────────────────────────────────────────────────────────────
    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();
    // blockId → {priceBox, levelBox}
    private final Map<String, EditBox[]> editBoxes = new HashMap<>();

    private int scrollOffset = 0;
    private int listHeight;
    private int priceX;
    private int levelX;

    // ── Konstruktor & Init ────────────────────────────────────────────────────

    public ProductionBlockCatalogScreen(Screen parent) {
        super(Component.translatable("gui.block_catalog.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        listHeight = this.height - START_Y - BOTTOM_RESERVE;
        // Zwei Felder rechts bündig nebeneinander
        levelX = this.width - LEVEL_WIDTH - 18;
        priceX = levelX - PRICE_WIDTH - COL_GAP;

        Map<String, double[]> current = loadConfig();
        rows.clear();
        editBoxes.clear();

        for (String[] entry : BLOCK_DATA) {
            if (entry[0] == null) {
                Component label = Component.literal(entry[1] + "■ ")
                    .append(Component.translatable(entry[2]));
                rows.add(new Row(label));
            } else {
                String blockId = entry[0];
                String nameKey = entry[1];
                double[] vals = current.getOrDefault(blockId, new double[]{0, 0});

                EditBox priceBox = new EditBox(this.font, priceX, 0, PRICE_WIDTH, 18, Component.empty());
                priceBox.setMaxLength(10);
                priceBox.setValue(vals[0] > 0 ? formatPrice(vals[0]) : "");
                priceBox.setFilter(s -> s.matches("[0-9]*\\.?[0-9]*"));
                this.addRenderableWidget(priceBox);

                EditBox levelBox = new EditBox(this.font, levelX, 0, LEVEL_WIDTH, 18, Component.empty());
                levelBox.setMaxLength(2);
                levelBox.setValue(vals[1] >= 0 ? String.valueOf((int) vals[1]) : "0");
                levelBox.setFilter(s -> s.matches("\\d*") && (s.isEmpty() || Integer.parseInt(s) <= 30));
                this.addRenderableWidget(levelBox);

                rows.add(new Row(blockId, nameKey, priceBox, levelBox));
                editBoxes.put(blockId, new EditBox[]{priceBox, levelBox});
            }
        }

        refreshLayout();

        int btnY = this.height - BOTTOM_RESERVE + 16;
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.block_catalog.btn_save"),
            btn -> saveAll()
        ).bounds(this.width / 2 - 152, btnY, 100, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.block_catalog.btn_reset"),
            btn -> resetToDefaults()
        ).bounds(this.width / 2 - 50, btnY, 100, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.block_catalog.btn_back"),
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
            if (!row.isHeader) {
                int boxY  = contentY + (rowH - 18) / 2;
                boolean vis = boxY + 18 > START_Y && boxY < START_Y + listHeight;
                for (EditBox box : new EditBox[]{row.priceBox, row.levelBox}) {
                    box.setY(boxY);
                    box.visible = vis;
                    box.active  = vis;
                }
                row.priceBox.setX(priceX);
                row.levelBox.setX(levelX);
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
        scrollOffset -= (int) (delta * 18);
        refreshLayout();
        return true;
    }

    // ── Render ─────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        int listBottom = START_Y + listHeight;
        g.fill(10, START_Y - 4, this.width - 10, listBottom + 4, 0x22000000);

        g.enableScissor(10, START_Y, this.width - 10, listBottom);

        for (Row row : rows) {
            int y = row.renderY;
            if (y + ROW_HEIGHT < START_Y || y > listBottom) continue;

            if (row.isHeader) {
                g.drawString(this.font, row.headerLabel, LABEL_X, y + 3, 0xFFFFAA, false);
            } else {
                // Produkt-Name
                g.drawString(this.font, Component.translatable(row.nameKey),
                    LABEL_X + 10, y + 5, 0xDDDDDD, false);
                // Spalten-Beschriftungen (Preis / Level)
                g.drawString(this.font,
                    Component.translatable("gui.block_catalog.col_price"),
                    priceX - font.width(Component.translatable("gui.block_catalog.col_price").getString()) - 3,
                    y + 5, 0x888888, false);
                g.drawString(this.font,
                    Component.translatable("gui.block_catalog.col_level"),
                    levelX - font.width(Component.translatable("gui.block_catalog.col_level").getString()) - 3,
                    y + 5, 0x888888, false);
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

        super.render(g, mouseX, mouseY, partialTick);

        g.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        g.drawCenteredString(this.font,
            Component.translatable("gui.block_catalog.subtitle"),
            this.width / 2, 22, 0xAAAA55);
        g.drawCenteredString(this.font,
            Component.translatable("gui.block_catalog.hint"),
            this.width / 2, 35, 0x666666);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    // ── Speichern / Reset ──────────────────────────────────────────────────────

    private void saveAll() {
        List<String> prices = new ArrayList<>();
        for (Map.Entry<String, EditBox[]> e : editBoxes.entrySet()) {
            String rawPrice = e.getValue()[0].getValue().trim();
            String rawLevel = e.getValue()[1].getValue().trim();
            if (rawPrice.isEmpty()) continue;
            try {
                double price = Double.parseDouble(rawPrice);
                int level = rawLevel.isEmpty() ? 0 : Integer.parseInt(rawLevel);
                if (price > 0) prices.add(e.getKey() + "=" + price + ":" + level);
            } catch (NumberFormatException ignored) {}
        }
        ModConfigHandler.COMMON.BLOCK_PRICES.set(prices);
        ModConfigHandler.SPEC.save();
        ModNetworking.sendToServer(new ReloadBlockCatalogPacket());
        minecraft.setScreen(parent);
    }

    private void resetToDefaults() {
        Map<String, double[]> defaults = loadConfig();
        for (Map.Entry<String, EditBox[]> e : editBoxes.entrySet()) {
            double[] vals = defaults.getOrDefault(e.getKey(), new double[]{0, 0});
            e.getValue()[0].setValue(vals[0] > 0 ? formatPrice(vals[0]) : "");
            e.getValue()[1].setValue(String.valueOf((int) vals[1]));
        }
    }

    // ── Hilfsmethoden ──────────────────────────────────────────────────────────

    private static Map<String, double[]> loadConfig() {
        Map<String, double[]> map = new HashMap<>();
        try {
            for (String entry : ModConfigHandler.COMMON.BLOCK_PRICES.get()) {
                // Format: schedulemc:blockname=Preis:Level
                String[] eq = entry.split("=", 2);
                if (eq.length != 2) continue;
                String[] vals = eq[1].split(":", 2);
                if (vals.length != 2) continue;
                try {
                    double price = Double.parseDouble(vals[0].trim());
                    int level = Integer.parseInt(vals[1].trim());
                    map.put(eq[0].trim(), new double[]{price, level});
                } catch (NumberFormatException ignored) {}
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
        final Component headerLabel;
        final String    blockId;
        final String    nameKey;
        final EditBox   priceBox;
        final EditBox   levelBox;
        int             renderY;

        Row(Component headerLabel) {
            this.isHeader    = true;
            this.headerLabel = headerLabel;
            this.blockId     = null;
            this.nameKey     = null;
            this.priceBox    = null;
            this.levelBox    = null;
        }

        Row(String blockId, String nameKey, EditBox priceBox, EditBox levelBox) {
            this.isHeader    = false;
            this.headerLabel = null;
            this.blockId     = blockId;
            this.nameKey     = nameKey;
            this.priceBox    = priceBox;
            this.levelBox    = levelBox;
        }
    }
}
