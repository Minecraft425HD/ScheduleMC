package de.rolandsw.schedulemc.tobacco.screen;

import de.rolandsw.schedulemc.tobacco.menu.MediumPackagingTableMenu;
import de.rolandsw.schedulemc.tobacco.network.ModNetworking;
import de.rolandsw.schedulemc.tobacco.network.MediumPackageRequestPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für den Medium Packaging Table (10g Schachteln).
 *
 * Design orientiert sich an SmallPackagingTableScreen:
 *  - Gleiche Abmessungen (176 × 190)
 *  - Input-Slot oben Mitte (80, 20)
 *  - Schachteln-Grid links (2 × 5), analog zur Tüten-Seite bei Small
 *  - Info-Panel rechts mit Gewichtsanzeige
 *  - Buttons zentriert auf y = 145, Hotbar auf y = 168
 */
public class MediumPackagingTableScreen extends AbstractContainerScreen<MediumPackagingTableMenu> {

    public MediumPackagingTableScreen(MediumPackagingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 190;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    // ─────────────────────────────────────────────────────────────
    // Buttons
    // ─────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();

        int bw      = 50;
        int bh      = 18;
        int centerX = this.leftPos + this.imageWidth / 2;
        int bY      = this.topPos + 145;

        // 10g Button
        this.addRenderableWidget(
            Button.builder(
                Component.translatable("gui.packaging_table.weight_10g"),
                btn -> onPackageButton(10)
            ).bounds(centerX - 55, bY, bw, bh).build()
        );

        // Entpacken Button
        this.addRenderableWidget(
            Button.builder(
                Component.translatable("gui.common.unpack"),
                btn -> onPackageButton(-1)
            ).bounds(centerX + 5, bY, bw, bh).build()
        );
    }

    private void onPackageButton(int weight) {
        ModNetworking.sendToServer(new MediumPackageRequestPacket(menu.blockEntity.getBlockPos(), weight));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return keyCode == 69 || super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ─────────────────────────────────────────────────────────────
    // Hintergrund
    // ─────────────────────────────────────────────────────────────

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Rahmen / Panel / Header – identisch zu Small
        g.fill(x,     y,     x + this.imageWidth,     y + this.imageHeight,     0xFF2B2B2B);
        g.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, 0xFF4C4C4C);
        g.fill(x + 2, y + 2, x + this.imageWidth - 2, y + 18,                   0xFF1E1E1E);

        renderSlotBorders(g, x, y);

        // Info-Panel rechts (analog zur Gläser-Seite bei Small)
        g.fill(x + 52, y + 40, x + 168, y + 140, 0xFF383838);
        // Innere Aufhellung – wirkt wie ein "Panel"
        g.fill(x + 54, y + 42, x + 166, y + 138, 0xFF404040);
    }

    private void renderSlotBorders(GuiGraphics g, int x, int y) {
        // Input-Slot – Mitte oben, wie Small
        drawSlot(g, x + 80, y + 20);

        // Schachteln-Slots – 2 × 5 links, wie Tüten in Small
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++) {
                drawSlot(g, x + 8 + col * 18, y + 50 + row * 18);
            }
        }

        // Hotbar
        for (int i = 0; i < 9; i++) {
            drawSlot(g, x + 8 + i * 18, y + 168);
        }
    }

    private void drawSlot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
        g.fill(x,     y,     x + 16, y + 16,  0xFF373737);
    }

    // ─────────────────────────────────────────────────────────────
    // Texte
    // ─────────────────────────────────────────────────────────────

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;

        // Titel
        g.drawString(font,
            Component.translatable("block.schedulemc.medium_packaging_table").getString(),
            x + 8, y + 6, 0xFFFFFF, false);

        // Info-Panel rechts: Gewichtsanzeige
        g.drawCenteredString(font,
            Component.translatable("gui.packaging_table.weight_10g").getString(),
            x + 110, y + 56, 0xFFAA00);
        g.drawCenteredString(font,
            Component.translatable("gui.packaging_table.no_material").getString(),
            x + 110, y + 90, 0x888888);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Alle Labels werden in render() gezeichnet
    }
}
