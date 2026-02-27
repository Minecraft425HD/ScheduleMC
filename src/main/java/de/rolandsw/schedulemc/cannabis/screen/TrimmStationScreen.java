package de.rolandsw.schedulemc.cannabis.screen;

import de.rolandsw.schedulemc.cannabis.items.DriedBudItem;
import de.rolandsw.schedulemc.cannabis.menu.TrimmStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * GUI für die Trimm-Station.
 * Kein Minigame – 5× Button klicken, dann DriedBud → TrimmedBud + 2× Trim.
 */
public class TrimmStationScreen extends AbstractContainerScreen<TrimmStationMenu> {

    private static final int GUI_WIDTH  = 220;
    private static final int GUI_HEIGHT = 160;

    private static final int BAR_X      = 20;
    private static final int BAR_Y      = 80;
    private static final int BAR_WIDTH  = 180;
    private static final int BAR_HEIGHT = 14;

    private static final int BUTTON_X      = 70;
    private static final int BUTTON_Y      = 110;
    private static final int BUTTON_WIDTH  = 80;
    private static final int BUTTON_HEIGHT = 20;

    private static final int ROW1_Y   = 35;
    private static final int ROW2_Y   = 50;
    private static final int STATUS_Y = 138;

    private boolean mouseButtonHeld = false;
    private int     holdTick        = 0;

    public TrimmStationScreen(TrimmStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth  = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width  - GUI_WIDTH)  / 2;
        this.topPos  = (this.height - GUI_HEIGHT) / 2;
    }

    /** Sucht ersten DriedBud im Spieler-Inventar (client-side). */
    private ItemStack getPlayerDriedBud() {
        if (this.minecraft == null || this.minecraft.player == null) return ItemStack.EMPTY;
        for (ItemStack stack : this.minecraft.player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof DriedBudItem) return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Background
        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF0A1A0A);
        graphics.fill(x + 3, y + 3, x + GUI_WIDTH - 3, y + GUI_HEIGHT - 3, 0xFF1D2D1D);
        graphics.fill(x + 3, y + 3, x + GUI_WIDTH - 3, y + 25, 0xFF1D3D1D);

        // Info rows
        ItemStack playerBud = getPlayerDriedBud();
        if (!playerBud.isEmpty()) {
            String strain  = DriedBudItem.getStrain(playerBud).getColoredName();
            String quality = DriedBudItem.getQuality(playerBud).getColoredName();
            graphics.drawString(this.font, "§7Sorte: " + strain,     x + 10, y + ROW1_Y, 0xFFFFFF, false);
            graphics.drawString(this.font, "§7Qualität: " + quality,  x + 10, y + ROW2_Y, 0xFFFFFF, false);
        }

        // Progress bar
        renderProgressBar(graphics, x + BAR_X, y + BAR_Y);

        // Button
        renderButton(graphics, x + BUTTON_X, y + BUTTON_Y, mouseX, mouseY, !playerBud.isEmpty());
    }

    private void renderProgressBar(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF555555);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF222222);

        int clicks    = menu.getClickCount();
        int filledPx  = (int)((clicks / (float) de.rolandsw.schedulemc.cannabis.blockentity.TrimmStationBlockEntity.CLICKS_NEEDED) * BAR_WIDTH);
        if (filledPx > 0) {
            graphics.fill(x, y, x + filledPx, y + BAR_HEIGHT, 0xFF44AA44);
        }

        String label = clicks + " / " + de.rolandsw.schedulemc.cannabis.blockentity.TrimmStationBlockEntity.CLICKS_NEEDED;
        int tw = this.font.width(label);
        graphics.drawString(this.font, label, x + BAR_WIDTH / 2 - tw / 2, y + 2, 0xFFFFFF, true);
    }

    private void renderButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, boolean enabled) {
        boolean hovered = mouseX >= x && mouseX < x + BUTTON_WIDTH &&
                          mouseY >= y && mouseY < y + BUTTON_HEIGHT;

        int color = enabled ? (hovered ? 0xFF55DD55 : 0xFF44AA44) : 0xFF2A3A2A;
        String text = Component.translatable("gui.trimm_station.button_trim").getString();

        graphics.fill(x + 2, y + 2, x + BUTTON_WIDTH + 2, y + BUTTON_HEIGHT + 2, 0x66000000);
        graphics.fill(x - 1, y - 1, x + BUTTON_WIDTH + 1, y + BUTTON_HEIGHT + 1, 0xFF224422);
        graphics.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, color);
        int tw = this.font.width(text);
        graphics.drawString(this.font, text, x + BUTTON_WIDTH / 2 - tw / 2, y + 6, 0xFFFFFF, true);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Auto-click when holding mouse button over trim button
        if (mouseButtonHeld && !getPlayerDriedBud().isEmpty()) {
            int bx = this.leftPos + BUTTON_X;
            int by = this.topPos  + BUTTON_Y;
            if (mouseX >= bx && mouseX < bx + BUTTON_WIDTH && mouseY >= by && mouseY < by + BUTTON_HEIGHT) {
                holdTick++;
                if (holdTick % 5 == 0) {
                    java.util.Objects.requireNonNull(this.minecraft).gameMode
                            .handleInventoryButtonClick(this.menu.containerId, TrimmStationMenu.BUTTON_TRIM);
                }
            }
        }

        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;

        graphics.drawString(this.font,
                Component.translatable("gui.trimm_station.title").getString(),
                x + 10, y + 8, 0xFF88FF88, true);

        ItemStack playerBud = getPlayerDriedBud();
        if (playerBud.isEmpty()) {
            String msg = Component.translatable("gui.trimm_station.no_bud").getString();
            int w = this.font.width(msg);
            graphics.drawString(this.font, msg, x + GUI_WIDTH / 2 - w / 2, y + STATUS_Y, 0xFF888888, false);
        } else {
            // Show output preview
            String out = "→ 1× §aTrimmedBud §7+ 2× §7Trim";
            graphics.drawString(this.font, out, x + 10, y + STATUS_Y, 0xFFFFFF, false);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        // Empty
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int bx = this.leftPos + BUTTON_X;
            int by = this.topPos  + BUTTON_Y;
            if (mouseX >= bx && mouseX < bx + BUTTON_WIDTH &&
                mouseY >= by && mouseY < by + BUTTON_HEIGHT) {
                if (!getPlayerDriedBud().isEmpty()) {
                    mouseButtonHeld = true;
                    holdTick = 0;
                    java.util.Objects.requireNonNull(this.minecraft).gameMode
                            .handleInventoryButtonClick(this.menu.containerId, TrimmStationMenu.BUTTON_TRIM);
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
    public boolean isPauseScreen() {
        return false;
    }
}
