package de.rolandsw.schedulemc.cannabis.screen;

import de.rolandsw.schedulemc.cannabis.blockentity.OilExtractorBlockEntity;
import de.rolandsw.schedulemc.cannabis.menu.OilExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OilExtractorScreen extends AbstractContainerScreen<OilExtractorMenu> {

    private static final int COLOR_PROGRESS = 0xFF44AA44;
    private static final int COLOR_HIGHLIGHT = 0xFF66CC66;
    private static final int COLOR_SEP       = 0xFF3A5A3A;

    // OilExtractor: 2 input slots side-by-side, then arrow, then output
    private static final int SLOT_MAT_X = 14;   // material
    private static final int SLOT_SOL_X = 36;   // solvent
    private static final int SLOT_OUT_X = 128;  // output
    private static final int SLOT_Y     = 30;
    private static final int ARROW_X1   = 58;
    private static final int ARROW_X2   = 118;
    private static final int ARROW_Y1   = 26;
    private static final int ARROW_Y2   = 52;

    private static final int BUTTON_X = 48;
    private static final int BUTTON_Y = 94;
    private static final int BUTTON_W = 80;
    private static final int BUTTON_H = 20;

    public OilExtractorScreen(OilExtractorMenu menu, Inventory inv, Component title) {
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

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // Panel
        g.fill(x,     y,     x + 176, y + 166, 0xFF1A2B1A);
        g.fill(x + 2, y + 2, x + 174, y + 164, 0xFF2B3B2B);
        g.fill(x + 2, y + 2, x + 174, y + 18,  0xFF0E1A0E);

        // Machine slots (2 inputs left, 1 output right)
        drawSlot(g, x + SLOT_MAT_X, y + SLOT_Y);
        drawSlot(g, x + SLOT_SOL_X, y + SLOT_Y);
        drawSlot(g, x + SLOT_OUT_X, y + SLOT_Y);

        // Arrow / extraction progress area
        g.fill(x + ARROW_X1, y + ARROW_Y1, x + ARROW_X2, y + ARROW_Y2, 0xFF111A11);
        if (menu.isExtracting()) {
            int filled = (int)(menu.getExtractionProgressF() * (ARROW_X2 - ARROW_X1));
            if (filled > 0) {
                g.fill(x + ARROW_X1, y + ARROW_Y1, x + ARROW_X1 + filled, y + ARROW_Y2, COLOR_PROGRESS);
                g.fill(x + ARROW_X1, y + ARROW_Y1, x + ARROW_X1 + filled, y + ARROW_Y1 + 3, COLOR_HIGHLIGHT);
            }
        } else if (menu.hasOutput()) {
            g.fill(x + ARROW_X1, y + ARROW_Y1, x + ARROW_X2, y + ARROW_Y2, COLOR_PROGRESS);
            g.fill(x + ARROW_X1, y + ARROW_Y1, x + ARROW_X2, y + ARROW_Y1 + 3, COLOR_HIGHLIGHT);
        }

        // Separator after machine area
        g.fill(x + 8, y + 62, x + 168, y + 63, COLOR_SEP);

        // Action button
        renderButton(g, x + BUTTON_X, y + BUTTON_Y, mouseX, mouseY);

        // Separator before hotbar
        g.fill(x + 8, y + 134, x + 168, y + 135, COLOR_SEP);

        // Hotbar slots
        for (int i = 0; i < 9; i++) {
            drawSlot(g, x + 8 + i * 18, y + 142);
        }
    }

    private void renderButton(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + BUTTON_W && mouseY >= y && mouseY < y + BUTTON_H;
        String text;
        int color;

        if (menu.hasOutput()) {
            text  = Component.translatable("gui.oel_extractor.oil_ready").getString();
            color = 0xFF335533;
        } else if (menu.isExtracting()) {
            text  = Component.translatable("gui.oel_extractor.extracting").getString();
            color = 0xFF2A3A4A;
        } else if (menu.canStart()) {
            text  = Component.translatable("gui.oel_extractor.button_start").getString();
            color = hovered ? 0xFF55DD55 : 0xFF44AA44;
        } else {
            text  = Component.translatable("gui.oel_extractor.button_start").getString();
            color = 0xFF2A3A2A;
        }

        g.fill(x + 2, y + 2, x + BUTTON_W + 2, y + BUTTON_H + 2, 0x66000000);
        g.fill(x - 1, y - 1, x + BUTTON_W + 1, y + BUTTON_H + 1, 0xFF224422);
        g.fill(x, y, x + BUTTON_W, y + BUTTON_H, color);
        int tw = font.width(text);
        g.drawString(font, text, x + BUTTON_W / 2 - tw / 2, y + 6, 0xFFFFFF, true);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;

        // Title
        g.drawString(font, Component.translatable("gui.oel_extractor.title").getString(),
                x + 8, y + 6, 0xFF88FF88, true);

        // Slot labels
        g.drawString(font, "Mat", x + 12, y + 19, 0xAAAAAA, false);
        g.drawString(font, "Sol", x + 34, y + 19, 0xAAAAAA, false);
        g.drawString(font, Component.translatable("gui.fermentation.output_label").getString(),
                x + 122, y + 19, 0xAAAAAA, false);

        // Progress pct in arrow area
        if (menu.isExtracting()) {
            String pct = (int)(menu.getExtractionProgressF() * 100) + "%";
            g.drawCenteredString(font, pct, x + 88, y + 36, 0xFFFFFF);
        }

        // Info rows
        String typeLabel = menu.isFromBuds()
                ? Component.translatable("gui.oel_extractor.buds").getString()
                : Component.translatable("gui.oel_extractor.trim").getString();
        g.drawString(font,
                Component.translatable("gui.oel_extractor.info_material").getString()
                + menu.getMaterialWeight() + "g " + typeLabel + " §7| " + menu.getStrain().getColoredName(),
                x + 8, y + 66, 0xFFFFFF, false);
        g.drawString(font,
                Component.translatable("gui.oel_extractor.info_solvent").getString()
                + menu.getSolventCount(),
                x + 8, y + 76, 0xFFFFFF, false);
        g.drawString(font,
                Component.translatable("gui.oel_extractor.info_oil").getString()
                + menu.getExpectedOilAmount()
                + Component.translatable("gui.oel_extractor.info_quality_sep").getString()
                + menu.getBaseQuality().getColoredName(),
                x + 8, y + 86, 0xFFFFFF, false);

        // Status hint
        if (!menu.isExtracting() && !menu.hasOutput()) {
            String hint = null;
            if (menu.getSolventCount() < 1) {
                hint = Component.translatable("gui.oel_extractor.hint_solvent_needed").getString();
            } else if (menu.getMaterialWeight() < OilExtractorBlockEntity.MIN_MATERIAL_WEIGHT) {
                hint = String.format(
                        Component.translatable("gui.oel_extractor.hint_min_material").getString(),
                        OilExtractorBlockEntity.MIN_MATERIAL_WEIGHT);
            }
            if (hint != null) g.drawString(font, hint, x + 8, y + 117, 0xAAAAAA, false);
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
                if (menu.canStart()) {
                    java.util.Objects.requireNonNull(this.minecraft).gameMode
                            .handleInventoryButtonClick(this.menu.containerId, OilExtractorMenu.BUTTON_START);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
