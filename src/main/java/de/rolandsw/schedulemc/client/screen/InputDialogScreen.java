package de.rolandsw.schedulemc.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

/**
 * Einfacher Input-Dialog für Text/Zahlen-Eingaben
 */
@OnlyIn(Dist.CLIENT)
public class InputDialogScreen extends Screen {

    private final Screen parent;
    private final String title;
    private final String description;
    private final Consumer<String> onConfirm;
    private final Runnable onCancel;
    private final InputType inputType;

    private EditBox inputField;

    public enum InputType {
        TEXT,       // Beliebiger Text
        NUMBER,     // Nur Zahlen (mit Dezimal)
        INTEGER     // Nur Ganzzahlen
    }

    public InputDialogScreen(Screen parent, String title, String description,
                            InputType inputType, Consumer<String> onConfirm, Runnable onCancel) {
        super(Component.literal(title));
        this.parent = parent;
        this.title = title;
        this.description = description;
        this.inputType = inputType;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    public InputDialogScreen(Screen parent, String title, String description,
                            InputType inputType, Consumer<String> onConfirm) {
        this(parent, title, description, inputType, onConfirm, null);
    }

    @Override
    protected void init() {
        super.init();

        int dialogWidth = 240;
        int dialogHeight = 120;
        int leftPos = (this.width - dialogWidth) / 2;
        int topPos = (this.height - dialogHeight) / 2;

        // Input-Feld
        inputField = new EditBox(this.font, leftPos + 20, topPos + 50, dialogWidth - 40, 20,
            Component.literal("Input"));
        inputField.setMaxLength(inputType == InputType.TEXT ? 200 : 20);
        inputField.setResponder(this::onInputChanged);
        addRenderableWidget(inputField);
        setInitialFocus(inputField);

        // Bestätigen-Button
        addRenderableWidget(Button.builder(Component.literal("✓ Bestätigen"), button -> {
            String value = inputField.getValue().trim();
            if (!value.isEmpty() && validateInput(value)) {
                onConfirm.accept(value);
                if (minecraft != null) {
                    minecraft.setScreen(parent);
                }
            }
        }).bounds(leftPos + 20, topPos + 80, 90, 20).build());

        // Abbrechen-Button
        addRenderableWidget(Button.builder(Component.literal("✗ Abbrechen"), button -> {
            if (onCancel != null) {
                onCancel.run();
            }
            if (minecraft != null) {
                minecraft.setScreen(parent);
            }
        }).bounds(leftPos + 130, topPos + 80, 90, 20).build());
    }

    private void onInputChanged(String text) {
        // Filter ungültige Zeichen basierend auf Input-Type
        if (inputType == InputType.NUMBER || inputType == InputType.INTEGER) {
            String filtered = text.replaceAll("[^0-9.]", "");
            if (inputType == InputType.INTEGER) {
                filtered = filtered.replaceAll("\\.", "");
            }
            if (!filtered.equals(text)) {
                inputField.setValue(filtered);
            }
        }
    }

    private boolean validateInput(String value) {
        if (inputType == InputType.NUMBER) {
            try {
                double num = Double.parseDouble(value);
                return num >= 0;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (inputType == InputType.INTEGER) {
            try {
                int num = Integer.parseInt(value);
                return num >= 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        int dialogWidth = 240;
        int dialogHeight = 120;
        int leftPos = (this.width - dialogWidth) / 2;
        int topPos = (this.height - dialogHeight) / 2;

        // Dialog-Box
        guiGraphics.fill(leftPos - 2, topPos - 2, leftPos + dialogWidth + 2, topPos + dialogHeight + 2, 0xFF1C1C1C);
        guiGraphics.fill(leftPos, topPos, leftPos + dialogWidth, topPos + dialogHeight, 0xFF2A2A2A);

        // Titel
        guiGraphics.fill(leftPos, topPos, leftPos + dialogWidth, topPos + 25, 0xFF1A1A1A);
        guiGraphics.drawCenteredString(this.font, "§l" + title, leftPos + dialogWidth / 2, topPos + 8, 0xFFFFFF);

        // Beschreibung
        guiGraphics.drawCenteredString(this.font, "§7" + description, leftPos + dialogWidth / 2, topPos + 32, 0xAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
