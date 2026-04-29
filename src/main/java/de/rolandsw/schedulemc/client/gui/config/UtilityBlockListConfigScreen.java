package de.rolandsw.schedulemc.client.gui.config;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import de.rolandsw.schedulemc.utility.UtilityRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * In-game config screen to edit UTILITY_CONSUMER_BLOCKS.
 * Lists all blocks that consume electricity/water and allows adding or removing entries.
 * Autocomplete is sourced from UtilityRegistry (only known utility blocks).
 */
@OnlyIn(Dist.CLIENT)
public class UtilityBlockListConfigScreen extends Screen {

    private static final int LABEL_X            = 24;
    private static final int FIELD_X            = 185;
    private static final int START_Y            = 56;
    private static final int ROW_HEIGHT         = 20;
    private static final int LIST_BOTTOM_PADDING = 50;

    private final Screen parent;

    /** Single large EditBox for the comma-separated list */
    private EditBox consumerList;

    /** All known utility block IDs (for autocomplete) */
    private List<String> utilitySuggestions = new ArrayList<>();
    private String currentSuggestionText = "";

    private int inputWidth;

    public UtilityBlockListConfigScreen(Screen parent) {
        super(Component.literal("Utility Consumer Blocks"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        inputWidth = Math.max(220, this.width - FIELD_X - 20);

        // Suggestions: all registered utility IDs, sorted
        utilitySuggestions = UtilityRegistry.getAllRegisteredIds().stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        // EditBox spanning full width
        consumerList = new EditBox(this.font, FIELD_X, START_Y, inputWidth, 18, Component.empty());
        consumerList.setMaxLength(Integer.MAX_VALUE);
        consumerList.setValue(String.join(", ", ModConfigHandler.COMMON.UTILITY_CONSUMER_BLOCKS.get()));
        consumerList.setResponder(_unused -> updateSuggestion());
        this.addRenderableWidget(consumerList);

        this.addRenderableWidget(Button.builder(
                Component.literal("Save"),
                button -> saveAndClose()
        ).bounds(this.width / 2 - 100, this.height - 28, 98, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Back"),
                button -> this.minecraft.setScreen(parent)
        ).bounds(this.width / 2 + 2, this.height - 28, 98, 20).build());

        updateSuggestion();
    }

    private void saveAndClose() {
        ModConfigHandler.COMMON.UTILITY_CONSUMER_BLOCKS.set(parseList(consumerList.getValue()));
        ModConfigHandler.SPEC.save();
        this.minecraft.setScreen(parent);
    }

    private List<String> parseList(String raw) {
        List<String> result = new ArrayList<>();
        if (raw == null || raw.isBlank()) return result;
        for (String part : raw.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) result.add(t);
        }
        return result;
    }

    // ── Keyboard / Autocomplete ───────────────────────────────────────────────

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_TAB && applyAutocomplete()) {
            updateSuggestion();
            return true;
        }
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        updateSuggestion();
        return handled;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean handled = super.charTyped(Character.toLowerCase(codePoint), modifiers);
        updateSuggestion();
        return handled;
    }

    private void updateSuggestion() {
        String token = currentToken(consumerList.getValue());
        if (token.isEmpty()) {
            consumerList.setSuggestion("");
            currentSuggestionText = "";
            return;
        }
        String match = findBestMatch(token);
        consumerList.setSuggestion(match == null ? "" : match);
        currentSuggestionText = match == null ? "" : match;
    }

    private boolean applyAutocomplete() {
        String token = currentToken(consumerList.getValue());
        if (token.isEmpty()) return false;
        String match = findBestMatch(token);
        if (match == null) return false;
        consumerList.setValue(mergeCompletion(consumerList.getValue(), token, match));
        return true;
    }

    private String findBestMatch(String tokenRaw) {
        String token  = tokenRaw.toLowerCase(Locale.ROOT);
        boolean hasNs = token.contains(":");
        for (String fullId : utilitySuggestions) {
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

    private String currentToken(String text) {
        int comma = text.lastIndexOf(',');
        return comma < 0 ? text.trim() : text.substring(comma + 1).trim();
    }

    private String mergeCompletion(String fullText, String currentToken, String completion) {
        String tokenLower      = currentToken.toLowerCase(Locale.ROOT);
        String completionLower = completion.toLowerCase(Locale.ROOT);
        int comma      = fullText.lastIndexOf(',');
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

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
                Component.literal("Comma-separated IDs. TAB = Autocomplete"),
                this.width / 2, 22, 0xAAAAAA);
        graphics.drawCenteredString(this.font,
                Component.literal("§7Einträge entfernen = Block wird nicht mehr abgerechnet"),
                this.width / 2, 34, 0x888888);

        if (!currentSuggestionText.isEmpty()) {
            graphics.drawCenteredString(this.font,
                    Component.literal("§aSuggestion: " + currentSuggestionText),
                    this.width / 2, 44, 0x77CC77);
        }

        graphics.drawString(this.font,
                Component.literal("§eUTILITY BLOCKS"), LABEL_X, START_Y + 4, 0xFFD27A, false);

        // Count info
        int count = parseList(consumerList.getValue()).size();
        graphics.drawString(this.font,
                Component.literal("§7" + count + " Blöcke aktiv"),
                LABEL_X, START_Y + 24, 0xAAAAAA, false);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
