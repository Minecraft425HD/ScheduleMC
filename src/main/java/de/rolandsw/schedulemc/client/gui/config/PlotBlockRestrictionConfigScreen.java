package de.rolandsw.schedulemc.client.gui.config;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

    private static final int LABEL_X = 24;
    private static final int FIELD_X = 150;
    private static final int START_Y = 56;
    private static final int ROW_SPACING = 24;
    private static final int LIST_BOTTOM_PADDING = 84;

    private final Screen parent;

    private EditBox residential;
    private EditBox commercial;
    private EditBox industrial;
    private EditBox shop;
    private EditBox publicPlot;
    private EditBox government;
    private EditBox prison;
    private EditBox towingYard;

    private final List<String> labels = List.of(
        "RESIDENTIAL", "COMMERCIAL", "INDUSTRIAL", "SHOP",
        "PUBLIC", "GOVERNMENT", "PRISON", "TOWING_YARD"
    );

    private List<EditBox> fields = new ArrayList<>();
    private List<String> fullBlockIdSuggestions = new ArrayList<>();
    private String currentSuggestionText = "";

    private int inputWidth;
    private int listHeight;
    private int scrollOffset;

    public PlotBlockRestrictionConfigScreen(Screen parent) {
        super(Component.literal("Plot Block Restrictions"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        inputWidth = Math.max(220, this.width - FIELD_X - 20);
        listHeight = Math.max(60, this.height - START_Y - LIST_BOTTOM_PADDING);
        scrollOffset = 0;

        fullBlockIdSuggestions = ForgeRegistries.BLOCKS.getKeys().stream()
            .map(ResourceLocation::toString)
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());

        residential = addListEditor(ModConfigHandler.COMMON.RESIDENTIAL_PLOT_BLOCKS);
        commercial = addListEditor(ModConfigHandler.COMMON.COMMERCIAL_PLOT_BLOCKS);
        industrial = addListEditor(ModConfigHandler.COMMON.INDUSTRIAL_PLOT_BLOCKS);
        shop = addListEditor(ModConfigHandler.COMMON.SHOP_PLOT_BLOCKS);
        publicPlot = addListEditor(ModConfigHandler.COMMON.PUBLIC_PLOT_BLOCKS);
        government = addListEditor(ModConfigHandler.COMMON.GOVERNMENT_PLOT_BLOCKS);
        prison = addListEditor(ModConfigHandler.COMMON.PRISON_PLOT_BLOCKS);
        towingYard = addListEditor(ModConfigHandler.COMMON.TOWING_YARD_PLOT_BLOCKS);

        fields = List.of(residential, commercial, industrial, shop, publicPlot, government, prison, towingYard);
        refreshFieldLayout();

        this.addRenderableWidget(Button.builder(
            Component.literal("Save"),
            button -> saveAndClose()
        ).bounds(this.width / 2 - 100, this.height - 34, 98, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Back"),
            button -> this.minecraft.setScreen(parent)
        ).bounds(this.width / 2 + 2, this.height - 34, 98, 20).build());

        updateFocusedSuggestion();
    }

    private EditBox addListEditor(ForgeConfigSpec.ConfigValue<List<? extends String>> configValue) {
        EditBox box = new EditBox(this.font, FIELD_X, START_Y, inputWidth, 18, Component.empty());
        box.setMaxLength(32767);
        box.setValue(String.join(",", configValue.get()));
        box.setResponder(_unused -> updateFocusedSuggestion());
        this.addRenderableWidget(box);
        return box;
    }

    private void refreshFieldLayout() {
        int maxScroll = getMaxScroll();
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        for (int i = 0; i < fields.size(); i++) {
            EditBox field = fields.get(i);
            int y = START_Y + i * ROW_SPACING - scrollOffset;
            boolean visible = y >= START_Y - 4 && y <= START_Y + listHeight - 18;

            field.setX(FIELD_X);
            field.setY(y);
            field.setWidth(inputWidth);
            field.visible = visible;
            field.active = visible;
        }
    }

    private int getMaxScroll() {
        int contentHeight = fields.size() * ROW_SPACING;
        return Math.max(0, contentHeight - listHeight + 8);
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

        for (String p : raw.split(",")) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                values.add(trimmed);
            }
        }

        if (values.isEmpty()) values.add("ALL");
        return values;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta == 0) return false;
        scrollOffset -= (int) (delta * 16);
        refreshFieldLayout();
        return true;
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
    public boolean charTyped(char codePoint, int modifiers) {
        boolean handled = super.charTyped(Character.toLowerCase(codePoint), modifiers);
        updateFocusedSuggestion();
        return handled;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Comma-separated IDs. TAB = Autocomplete / Mausrad = Scroll"),
            this.width / 2, 34, 0xAAAAAA);

        if (!currentSuggestionText.isEmpty()) {
            graphics.drawCenteredString(this.font,
                Component.literal("Suggestion: " + currentSuggestionText),
                this.width / 2, 44, 0x77CC77);
        }

        int listBottom = START_Y + listHeight;
        graphics.fill(12, START_Y - 6, this.width - 12, listBottom + 6, 0x22000000);

        for (int i = 0; i < labels.size(); i++) {
            int y = START_Y + i * ROW_SPACING - scrollOffset + 5;
            if (y >= START_Y && y <= listBottom - 8) {
                drawLabel(graphics, labels.get(i), LABEL_X, y);
            }
        }

        if (getMaxScroll() > 0) {
            int trackTop = START_Y;
            int trackBottom = listBottom - 2;
            int trackX = this.width - 16;
            graphics.fill(trackX, trackTop, trackX + 4, trackBottom, 0x44FFFFFF);

            int trackHeight = trackBottom - trackTop;
            int thumbHeight = Math.max(12, trackHeight * listHeight / (listHeight + getMaxScroll()));
            int thumbY = trackTop + (trackHeight - thumbHeight) * scrollOffset / Math.max(1, getMaxScroll());
            graphics.fill(trackX, thumbY, trackX + 4, thumbY + thumbHeight, 0xAAFFFFFF);
        }
    }

    private void drawLabel(GuiGraphics graphics, String text, int x, int y) {
        graphics.drawString(this.font, Component.literal(text), x, y, 0xFFD27A, false);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private EditBox getFocusedEditBox() {
        if (this.getFocused() instanceof EditBox box) return box;
        return null;
    }

    private void updateFocusedSuggestion() {
        EditBox focused = getFocusedEditBox();
        if (focused == null) {
            clearAllSuggestions();
            currentSuggestionText = "";
            return;
        }

        String token = currentToken(focused.getValue());
        if (token.isEmpty()) {
            focused.setSuggestion("ALL");
            currentSuggestionText = "ALL";
            return;
        }

        String match = findBestMatch(token);
        focused.setSuggestion(match == null ? "" : match);
        currentSuggestionText = match == null ? "" : match;
    }

    private void clearAllSuggestions() {
        for (EditBox field : fields) {
            field.setSuggestion("");
        }
    }

    private boolean applyAutocomplete(EditBox box) {
        String token = currentToken(box.getValue());
        if (token.isEmpty()) {
            box.setValue(appendToken(box.getValue(), "ALL"));
            return true;
        }

        String match = findBestMatch(token);
        if (match == null) return false;

        box.setValue(mergeCompletion(box.getValue(), token, match));
        return true;
    }

    private String findBestMatch(String tokenRaw) {
        String token = tokenRaw.toLowerCase(Locale.ROOT);
        boolean hasNamespace = token.contains(":");

        for (String fullId : fullBlockIdSuggestions) {
            String lowerFull = fullId.toLowerCase(Locale.ROOT);
            String path = lowerFull.contains(":") ? lowerFull.substring(lowerFull.indexOf(':') + 1) : lowerFull;

            if (hasNamespace) {
                if (lowerFull.startsWith(token)) {
                    return fullId;
                }
            } else if (path.startsWith(token) || lowerFull.startsWith(token)) {
                if (lowerFull.startsWith("schedulemc:")) {
                    return fullId.substring(fullId.indexOf(':') + 1);
                }
                return fullId;
            }
        }
        return null;
    }

    private String currentToken(String fullText) {
        int comma = fullText.lastIndexOf(',');
        if (comma < 0) return fullText.trim();
        return fullText.substring(comma + 1).trim();
    }

    private String replaceCurrentToken(String fullText, String replacement) {
        int comma = fullText.lastIndexOf(',');
        if (comma < 0) return replacement;
        return fullText.substring(0, comma + 1) + replacement;
    }

    private String mergeCompletion(String fullText, String currentToken, String completion) {
        String tokenLower = currentToken.toLowerCase(Locale.ROOT);
        String completionLower = completion.toLowerCase(Locale.ROOT);

        if (!currentToken.isEmpty() && completionLower.startsWith(tokenLower)) {
            return fullText + completion.substring(currentToken.length());
        }
        return replaceCurrentToken(fullText, completion);
    }

    private String appendToken(String fullText, String token) {
        if (fullText == null || fullText.isBlank()) return token;

        String trimmed = fullText.trim();
        if (trimmed.endsWith(",")) return trimmed + token;
        return trimmed + "," + token;
    }
}
