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

/**
 * In-game config screen for NPC block lists with autocomplete.
 * Edits NPC_WALKABLE_BLOCKS and NAVIGATION_ROAD_BLOCKS.
 */
@OnlyIn(Dist.CLIENT)
public class NPCBlockListConfigScreen extends Screen {

    private static final int LABEL_X       = 24;
    private static final int FIELD_X       = 160;
    private static final int START_Y       = 56;
    private static final int ROW_SPACING   = 24;
    private static final int LIST_BOTTOM_PADDING = 84;

    private final Screen parent;

    private EditBox walkableBlocks;
    private EditBox roadBlocks;

    private final List<String> labels = List.of("NPC WALKABLE", "ROAD BLOCKS");

    private List<EditBox> fields = new ArrayList<>();
    private List<String> fullBlockIdSuggestions = new ArrayList<>();
    private String currentSuggestionText = "";

    private int inputWidth;
    private int listHeight;
    private int scrollOffset;

    public NPCBlockListConfigScreen(Screen parent) {
        super(Component.literal("NPC Block Lists"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        inputWidth  = Math.max(220, this.width - FIELD_X - 20);
        listHeight  = Math.max(60, this.height - START_Y - LIST_BOTTOM_PADDING);
        scrollOffset = 0;

        fullBlockIdSuggestions = ForgeRegistries.BLOCKS.getKeys().stream()
                .map(ResourceLocation::toString)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        walkableBlocks = addListEditor(ModConfigHandler.COMMON.NPC_WALKABLE_BLOCKS);
        roadBlocks     = addListEditor(ModConfigHandler.COMMON.NAVIGATION_ROAD_BLOCKS);

        fields = List.of(walkableBlocks, roadBlocks);
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
            int y       = START_Y + i * ROW_SPACING - scrollOffset;
            boolean vis = y >= START_Y - 4 && y <= START_Y + listHeight - 18;
            field.setX(FIELD_X);
            field.setY(y);
            field.setWidth(inputWidth);
            field.visible = vis;
            field.active  = vis;
        }
    }

    private int getMaxScroll() {
        int contentHeight = fields.size() * ROW_SPACING;
        return Math.max(0, contentHeight - listHeight + 8);
    }

    private void saveAndClose() {
        ModConfigHandler.COMMON.NPC_WALKABLE_BLOCKS.set(parseList(walkableBlocks.getValue()));
        ModConfigHandler.COMMON.NAVIGATION_ROAD_BLOCKS.set(parseList(roadBlocks.getValue()));
        ModConfigHandler.SPEC.save();
        this.minecraft.setScreen(parent);
    }

    private List<String> parseList(String raw) {
        List<String> values = new ArrayList<>();
        if (raw == null || raw.isBlank()) return values;
        for (String p : raw.split(",")) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) values.add(trimmed);
        }
        return values;
    }

    // ── Scrolling ─────────────────────────────────────────────────────────────

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta == 0) return false;
        scrollOffset -= (int) (delta * 16);
        refreshFieldLayout();
        return true;
    }

    // ── Keyboard / Autocomplete ───────────────────────────────────────────────

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

    // ── Rendering ─────────────────────────────────────────────────────────────

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
                graphics.drawString(this.font,
                        Component.literal(labels.get(i)), LABEL_X, y, 0xFFD27A, false);
            }
        }

        if (getMaxScroll() > 0) {
            int trackTop    = START_Y;
            int trackBottom = listBottom - 2;
            int trackX      = this.width - 16;
            graphics.fill(trackX, trackTop, trackX + 4, trackBottom, 0x44FFFFFF);
            int trackHeight = trackBottom - trackTop;
            int thumbHeight = Math.max(12, trackHeight * listHeight / (listHeight + getMaxScroll()));
            int thumbY      = trackTop + (trackHeight - thumbHeight) * scrollOffset / Math.max(1, getMaxScroll());
            graphics.fill(trackX, thumbY, trackX + 4, thumbY + thumbHeight, 0xAAFFFFFF);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    // ── Autocomplete helpers (same pattern as PlotBlockRestrictionConfigScreen) ─

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
            focused.setSuggestion("");
            currentSuggestionText = "";
            return;
        }
        String match = findBestMatch(token);
        focused.setSuggestion(match == null ? "" : match);
        currentSuggestionText = match == null ? "" : match;
    }

    private void clearAllSuggestions() {
        for (EditBox field : fields) field.setSuggestion("");
    }

    private boolean applyAutocomplete(EditBox box) {
        String token = currentToken(box.getValue());
        if (token.isEmpty()) return false;
        String match = findBestMatch(token);
        if (match == null) return false;
        box.setValue(mergeCompletion(box.getValue(), token, match));
        return true;
    }

    private String findBestMatch(String tokenRaw) {
        String token       = tokenRaw.toLowerCase(Locale.ROOT);
        boolean hasNs      = token.contains(":");
        for (String fullId : fullBlockIdSuggestions) {
            String lower = fullId.toLowerCase(Locale.ROOT);
            String path  = lower.contains(":") ? lower.substring(lower.indexOf(':') + 1) : lower;
            if (hasNs) {
                if (lower.startsWith(token)) return fullId;
            } else if (path.startsWith(token) || lower.startsWith(token)) {
                return fullId;
            }
        }
        return null;
    }

    private String currentToken(String fullText) {
        int comma = fullText.lastIndexOf(',');
        return comma < 0 ? fullText.trim() : fullText.substring(comma + 1).trim();
    }

    private String mergeCompletion(String fullText, String currentToken, String completion) {
        String tokenLower      = currentToken.toLowerCase(Locale.ROOT);
        String completionLower = completion.toLowerCase(Locale.ROOT);
        int comma    = fullText.lastIndexOf(',');
        int tokenStart = comma < 0 ? 0 : comma + 1;
        while (tokenStart < fullText.length() && Character.isWhitespace(fullText.charAt(tokenStart))) {
            tokenStart++;
        }
        String prefix     = fullText.substring(0, tokenStart);
        String rawCurrent = fullText.substring(tokenStart);
        if (!currentToken.isEmpty() && completionLower.startsWith(tokenLower)) {
            String suffix = completion.substring(Math.min(currentToken.length(), completion.length()));
            return prefix + rawCurrent + suffix;
        }
        return prefix + completion;
    }
}
