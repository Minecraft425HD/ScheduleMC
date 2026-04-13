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
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    private List<String> blockIdSuggestions = new ArrayList<>();

    public PlotBlockRestrictionConfigScreen(Screen parent) {
        super(Component.literal("Plot Block Restrictions"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        blockIdSuggestions = ForgeRegistries.BLOCKS.getKeys().stream()
            .map(Object::toString)
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());

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

        updateFocusedSuggestion();
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            EditBox focused = getFocusedEditBox();
            if (focused != null && applyAutocomplete(focused)) {
                updateFocusedSuggestion();
                return true;
            }
        }

        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        updateFocusedSuggestion();
        return handled;
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
        boolean handled = super.charTyped(Character.toLowerCase(codePoint), modifiers);
        updateFocusedSuggestion();
        return handled;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private EditBox getFocusedEditBox() {
        if (this.getFocused() instanceof EditBox box) {
            return box;
        }
        return null;
    }

    private void updateFocusedSuggestion() {
        EditBox focused = getFocusedEditBox();
        if (focused == null) {
            clearAllSuggestions();
            return;
        }

        String token = currentToken(focused.getValue());
        if (token.isEmpty()) {
            focused.setSuggestion("ALL");
            return;
        }

        String normalized = token.toLowerCase(Locale.ROOT);
        String match = blockIdSuggestions.stream()
            .filter(id -> id.startsWith(normalized))
            .findFirst()
            .orElse("");

        focused.setSuggestion(match.isEmpty() ? "" : match);
    }

    private void clearAllSuggestions() {
        residential.setSuggestion("");
        commercial.setSuggestion("");
        industrial.setSuggestion("");
        shop.setSuggestion("");
        publicPlot.setSuggestion("");
        government.setSuggestion("");
        prison.setSuggestion("");
        towingYard.setSuggestion("");
    }

    private boolean applyAutocomplete(EditBox box) {
        String token = currentToken(box.getValue()).toLowerCase(Locale.ROOT);
        if (token.isEmpty()) {
            box.setValue(appendToken(box.getValue(), "ALL"));
            return true;
        }

        String match = blockIdSuggestions.stream()
            .filter(id -> id.startsWith(token))
            .findFirst()
            .orElse(null);

        if (match == null) {
            return false;
        }

        box.setValue(replaceCurrentToken(box.getValue(), match));
        return true;
    }

    private String currentToken(String fullText) {
        int comma = fullText.lastIndexOf(',');
        if (comma < 0) {
            return fullText.trim();
        }
        return fullText.substring(comma + 1).trim();
    }

    private String replaceCurrentToken(String fullText, String replacement) {
        int comma = fullText.lastIndexOf(',');
        if (comma < 0) {
            return replacement;
        }
        String prefix = fullText.substring(0, comma + 1).trim();
        if (!prefix.endsWith(",")) {
            prefix = prefix + ",";
        }
        return prefix + replacement;
    }

    private String appendToken(String fullText, String token) {
        if (fullText == null || fullText.isBlank()) {
            return token;
        }
        String trimmed = fullText.trim();
        if (trimmed.endsWith(",")) {
            return trimmed + token;
        }
        return trimmed + "," + token;
    }
}
