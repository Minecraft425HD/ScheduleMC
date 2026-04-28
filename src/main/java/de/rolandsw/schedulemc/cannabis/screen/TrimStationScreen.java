package de.rolandsw.schedulemc.cannabis.screen;

import de.rolandsw.schedulemc.cannabis.blockentity.TrimStationBlockEntity;
import de.rolandsw.schedulemc.cannabis.items.DriedBudItem;
import de.rolandsw.schedulemc.cannabis.menu.TrimStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für die Trimm-Station.
 * Layout: IN-Slot links → Klick-Fortschrittsbalken mitte → BUD + TRIM Slots rechts → Hotbar unten
 */
public class TrimStationScreen extends AbstractContainerScreen<TrimStationMenu> {

    private static final int COLOR_PROGRESS = 0xFF44AA44;
    private static final int COLOR_HIGHLIGHT = 0xFF66CC66;
    private static final int COLOR_SEP       = 0xFF3A5A3A;

    // TrimStation: 1 input left, 2 outputs side-by-side right
    private static final int SLOT_IN_X   = 22;
    private static final int SLOT_BUD_X  = 118;
    private static final int SLOT_TRIM_X = 140;
    private static final int SLOT_Y      = 30;

    private static final int BUTTON_X = 48;
    private static final int BUTTON_Y = 94;
    private static final int BUTTON_W = 80;
    private static final int BUTTON_H = 20;

    private boolean mouseButtonHeld = false;
    private int     holdTick        = 0;

    public TrimStationScreen(TrimStationMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return keyCode == 69 || super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawSlot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
        g.fill(x,     y,     x + 16, y + 16,  0xFF373737);
    }

    private ItemStack getAvailableDriedBud() {
        if (menu.blockEntity != null && menu.blockEntity.hasInput()) {
            return menu.blockEntity.getInputItem();
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // Panel
        g.fill(x,     y,     x + 176, y + 166, 0xFF1A2B1A);
        g.fill(x + 2, y + 2, x + 174, y + 164, 0xFF2B3B2B);
        g.fill(x + 2, y + 2, x + 174, y + 18,  0xFF0E1A0E);

        // Machine slots: 1 input left, 2 output slots side-by-side right
        drawSlot(g, x + SLOT_IN_X,   y + SLOT_Y);
        drawSlot(g, x + SLOT_BUD_X,  y + SLOT_Y);
        drawSlot(g, x + SLOT_TRIM_X, y + SLOT_Y);

        // Arrow / click progress area (between input right edge and bud slot left edge)
        g.fill(x + 44, y + 26, x + 112, y + 52, 0xFF111A11);
        int clicks   = menu.getClickCount();
        int filledPx = (int)(clicks / (float) TrimStationBlockEntity.CLICKS_NEEDED * 68);
        if (filledPx > 0) {
            g.fill(x + 44, y + 26, x + 44 + filledPx, y + 52, COLOR_PROGRESS);
            g.fill(x + 44, y + 26, x + 44 + filledPx, y + 29, COLOR_HIGHLIGHT);
        }

        // Separator after machine area
        g.fill(x + 8, y + 62, x + 168, y + 63, COLOR_SEP);

        // Trim button
        renderButton(g, x + BUTTON_X, y + BUTTON_Y, mouseX, mouseY, !getAvailableDriedBud().isEmpty());

        // Separator before hotbar
        g.fill(x + 8, y + 134, x + 168, y + 135, COLOR_SEP);

        // Hotbar slots
        for (int i = 0; i < 9; i++) {
            drawSlot(g, x + 8 + i * 18, y + 142);
        }
    }

    private void renderButton(GuiGraphics g, int x, int y, int mouseX, int mouseY, boolean enabled) {
        boolean hovered = mouseX >= x && mouseX < x + BUTTON_W && mouseY >= y && mouseY < y + BUTTON_H;
        int color = enabled ? (hovered ? 0xFF55DD55 : 0xFF44AA44) : 0xFF2A3A2A;
        String text = Component.translatable("gui.trimm_station.button_trim").getString();

        g.fill(x + 2, y + 2, x + BUTTON_W + 2, y + BUTTON_H + 2, 0x66000000);
        g.fill(x - 1, y - 1, x + BUTTON_W + 1, y + BUTTON_H + 1, 0xFF224422);
        g.fill(x, y, x + BUTTON_W, y + BUTTON_H, color);
        int tw = font.width(text);
        g.drawString(font, text, x + BUTTON_W / 2 - tw / 2, y + 6, 0xFFFFFF, true);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Auto-click when holding button
        if (mouseButtonHeld && !getAvailableDriedBud().isEmpty()) {
            int bx = leftPos + BUTTON_X;
            int by = topPos  + BUTTON_Y;
            if (mouseX >= bx && mouseX < bx + BUTTON_W && mouseY >= by && mouseY < by + BUTTON_H) {
                holdTick++;
                if (holdTick % 5 == 0) {
                    java.util.Objects.requireNonNull(this.minecraft).gameMode
                            .handleInventoryButtonClick(this.menu.containerId, TrimStationMenu.BUTTON_TRIM);
                }
            }
        }

        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;

        // Title
        g.drawString(font, Component.translatable("gui.trimm_station.title").getString(),
                x + 8, y + 6, 0xFF88FF88, true);

        // Slot labels
        g.drawString(font, Component.translatable("gui.fermentation.input_label").getString(),
                x + 16, y + 19, 0xAAAAAA, false);
        g.drawString(font, "Bud",  x + 114, y + 19, 0xAAAAAA, false);
        g.drawString(font, "Trim", x + 136, y + 19, 0xAAAAAA, false);

        // Click count in arrow area (center of x+44..x+112 = x+78)
        String label = menu.getClickCount() + " / " + TrimStationBlockEntity.CLICKS_NEEDED;
        g.drawCenteredString(font, label, x + 78, y + 36, 0xFFFFFF);

        // Info rows (strain + quality from input)
        TrimStationBlockEntity be = menu.blockEntity;
        if (be != null && be.hasInput()) {
            ItemStack inputBud = be.getInputItem();
            g.drawString(font,
                    Component.translatable("gui.trimm_station.info_strain").getString()
                    + DriedBudItem.getStrain(inputBud).getColoredName(),
                    x + 8, y + 66, 0xFFFFFF, false);
            g.drawString(font,
                    Component.translatable("gui.trimm_station.info_quality").getString()
                    + DriedBudItem.getQuality(inputBud).getColoredName(),
                    x + 8, y + 76, 0xFFFFFF, false);
        } else if (getAvailableDriedBud().isEmpty() && (be == null || !be.hasOutput())) {
            String msg = Component.translatable("gui.trimm_station.no_bud").getString();
            g.drawCenteredString(font, msg, x + 88, y + 70, 0xFF888888);
        }

        if (!getAvailableDriedBud().isEmpty() && (be == null || !be.hasInput())) {
            String out = Component.translatable("gui.trimm_station.output_preview").getString();
            g.drawString(font, out, x + 8, y + 86, 0xFFFFFF, false);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) { }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int bx = leftPos + BUTTON_X;
            int by = topPos  + BUTTON_Y;
            if (mouseX >= bx && mouseX < bx + BUTTON_W && mouseY >= by && mouseY < by + BUTTON_H) {
                if (!getAvailableDriedBud().isEmpty()) {
                    mouseButtonHeld = true;
                    holdTick = 0;
                    java.util.Objects.requireNonNull(this.minecraft).gameMode
                            .handleInventoryButtonClick(this.menu.containerId, TrimStationMenu.BUTTON_TRIM);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            mouseButtonHeld = false;
            holdTick = 0;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
