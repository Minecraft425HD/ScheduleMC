package de.rolandsw.schedulemc.client.gui.config;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PlotBlockRestrictionConfigScreen extends Screen {

    private static final int INPUT_WIDTH = 220;
    private static final int LEFT_X = 40;
    private static final int RIGHT_X = 300;
    private static final int START_Y = 60;
    private static final int ROW_SPACING = 26;

    private final Screen parent;

    private EditBox residential;
    private EditBox commercial;
    private EditBox industrial;
    private EditBox shop;
    private EditBox publicPlot;
    private EditBox government;
    private EditBox prison;
    private EditBox towingYard;

    public PlotBlockRestrictionConfigScreen(Screen parent) {
        super(Component.literal("Plot Block Restrictions"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        residential = addListEditor(LEFT_X, START_Y, ModConfigHandler.COMMON.RESIDENTIAL_PLOT_BLOCKS);
        commercial = addListEditor(RIGHT_X, START_Y, ModConfigHandler.COMMON.COMMERCIAL_PLOT_BLOCKS);

        industrial = addListEditor(LEFT_X, START_Y + ROW_SPACING, ModConfigHandler.COMMON.INDUSTRIAL_PLOT_BLOCKS);
        shop = addListEditor(RIGHT_X, START_Y + ROW_SPACING, ModConfigHandler.COMMON.SHOP_PLOT_BLOCKS);

        publicPlot = addListEditor(LEFT_X, START_Y + ROW_SPACING * 2, ModConfigHandler.COMMON.PUBLIC_PLOT_BLOCKS);
        government = addListEditor(RIGHT_X, START_Y + ROW_SPACING * 2, ModConfigHandler.COMMON.GOVERNMENT_PLOT_BLOCKS);

        prison = addListEditor(LEFT_X, START_Y + ROW_SPACING * 3, ModConfigHandler.COMMON.PRISON_PLOT_BLOCKS);
        towingYard = addListEditor(RIGHT_X, START_Y + ROW_SPACING * 3, ModConfigHandler.COMMON.TOWING_YARD_PLOT_BLOCKS);

        this.addRenderableWidget(Button.builder(
            Component.literal("Save"),
            button -> saveAndClose()
        ).bounds(this.width / 2 - 100, this.height - 50, 98, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            button -> this.minecraft.setScreen(parent)
        ).bounds(this.width / 2 + 2, this.height - 50, 98, 20).build());
    }

    private EditBox addListEditor(int x, int y, ForgeConfigSpec.ConfigValue<List<? extends String>> configValue) {
        EditBox box = new EditBox(this.font, x, y, INPUT_WIDTH, 18, Component.empty());
        box.setMaxLength(2048);
        box.setValue(String.join(",", configValue.get()));
        this.addRenderableWidget(box);
        return box;
    }

    private void saveAndClose() {
        ModConfigHandler.COMMON.RESIDENTIAL_PLOT_BLOCKS.set(parseList(residential.getValue()));
        ModConfigHandler.COMMON.COMMERCIAL_PLOT_BLOCKS.set(parseList(commercial.getValue()));
        ModConfigHandler.COMMON.INDUSTRIAL_PLOT_BLOCKS.set(parseList(industrial.getValue()));
        ModConfigHandler.COMMON.SHOP_PLOT_BLOCKS.set(parseList(shop.getValue()));
        ModConfigHandler.COMMON.PUBLIC_PLOT_BLOCKS.set(parseList(publicPlot.getValue()));
        ModConfigHandler.COMMON.GOVERNMENT_PLOT_BLOCKS.set(parseList(government.getValue()));
        ModConfigHandler.COMMON.PRISON_PLOT_BLOCKS.set(parseList(prison.getValue()));
        ModConfigHandler.COMMON.TOWING_YARD_PLOT_BLOCKS.set(parseList(towingYard.getValue()));

        ModConfigHandler.SPEC.save();
        this.minecraft.setScreen(parent);
    }

    private List<String> parseList(String raw) {
        List<String> values = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            values.add("ALL");
            return values;
        }

        String[] parts = raw.split(",");
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                values.add(trimmed);
            }
        }

        if (values.isEmpty()) {
            values.add("ALL");
        }

        return values;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Comma-separated IDs, e.g. ALL or schedulemc:block_id"),
            this.width / 2, 34, 0xAAAAAA);

        drawLabel(graphics, "RESIDENTIAL", LEFT_X, START_Y - 10);
        drawLabel(graphics, "COMMERCIAL", RIGHT_X, START_Y - 10);
        drawLabel(graphics, "INDUSTRIAL", LEFT_X, START_Y + ROW_SPACING - 10);
        drawLabel(graphics, "SHOP", RIGHT_X, START_Y + ROW_SPACING - 10);
        drawLabel(graphics, "PUBLIC", LEFT_X, START_Y + ROW_SPACING * 2 - 10);
        drawLabel(graphics, "GOVERNMENT", RIGHT_X, START_Y + ROW_SPACING * 2 - 10);
        drawLabel(graphics, "PRISON", LEFT_X, START_Y + ROW_SPACING * 3 - 10);
        drawLabel(graphics, "TOWING_YARD", RIGHT_X, START_Y + ROW_SPACING * 3 - 10);
    }

    private void drawLabel(GuiGraphics graphics, String text, int x, int y) {
        graphics.drawString(this.font, Component.literal(text), x, y, 0xFFD27A, false);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(Character.toLowerCase(codePoint), modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
