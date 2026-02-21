package de.rolandsw.schedulemc.vehicle.gui;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.PartTireBase;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.TireSeasonType;
import de.rolandsw.schedulemc.vehicle.items.IVehiclePart;
import de.rolandsw.schedulemc.vehicle.net.MessageTireSwap;
import de.rolandsw.schedulemc.vehicle.util.SereneSeasonsCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Wagenheber-GUI: Wechsel zwischen Sommer- und Winterreifen ohne Werkstatt.
 * Zeigt aktuellen Reifentyp, Saison und die Schnellzugriffsleiste des Spielers.
 * Ein kompatibler Reifen muss in der Schnellzugriffsleiste liegen.
 */
public class GuiTireChange extends Screen {

    private static final int GUI_W = 200;
    private static final int GUI_H = 148;

    // Farben (passend zum Rest des Mods)
    private static final int COL_BG       = 0xFFC6C6C6;
    private static final int COL_HEADER   = 0xFF404040;
    private static final int COL_SHADOW   = 0xFF555555;
    private static final int COL_WHITE    = 0xFFFFFFFF;
    private static final int COL_SLOT_BG  = 0xFF8B8B8B;
    private static final int COL_SEL      = 0x9900BB00; // grüner Highlight
    private static final int COL_COMPAT   = 0x3300BB00; // schwacher grüner Hint
    private static final int COL_WARN     = 0xFFAA0000;
    private static final int COL_TEXT     = 0x404040;
    private static final int COL_LIGHT    = 0x555555;

    private final UUID vehicleUuid;
    private final TireSeasonType currentTireSeasonType;
    private final int summerSwapCount;
    private final int winterSwapCount;
    private final boolean isTruck;

    private int selectedSlot = -1;
    private Button btnSwap;

    public GuiTireChange(UUID vehicleUuid, TireSeasonType currentTireSeasonType,
                         int summerSwapCount, int winterSwapCount, boolean isTruck) {
        super(Component.translatable("gui.tire_change.title"));
        this.vehicleUuid = vehicleUuid;
        this.currentTireSeasonType = currentTireSeasonType;
        this.summerSwapCount = summerSwapCount;
        this.winterSwapCount = winterSwapCount;
        this.isTruck = isTruck;
    }

    @Override
    protected void init() {
        super.init();
        autoSelectFirstSlot();

        int gx = (this.width  - GUI_W) / 2;
        int gy = (this.height - GUI_H) / 2;

        btnSwap = addRenderableWidget(Button.builder(
                Component.translatable("gui.tire_change.btn.swap"),
                b -> performSwap())
                .bounds(gx + (GUI_W - 90) / 2, gy + GUI_H - 26, 90, 20)
                .build());

        updateSwapButton();
    }

    // ─── Rendering ───────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);

        int gx = (this.width  - GUI_W) / 2;
        int gy = (this.height - GUI_H) / 2;

        // Hintergrund + Rahmen
        drawFrame(g, gx, gy, GUI_W, GUI_H);

        // Titelzeile
        g.fill(gx + 1, gy + 1, gx + GUI_W - 1, gy + 13, COL_HEADER);
        g.drawCenteredString(font, this.title, gx + GUI_W / 2, gy + 3, 0xFFFFFF);

        int tx = gx + 6;
        int ty = gy + 18;

        // Aktueller Reifentyp
        g.drawString(font, Component.translatable("gui.tire_change.current_label",
                getTireTypeName()).getString(), tx, ty, COL_TEXT, false);
        ty += 11;

        // Saison
        g.drawString(font, Component.translatable("gui.tire_change.season_label",
                getSeasonName()).getString(), tx, ty, COL_TEXT, false);
        ty += 11;

        // Warnung Abnutzung
        if (!isTruck) {
            int outCount = (currentTireSeasonType == TireSeasonType.SUMMER) ? summerSwapCount : winterSwapCount;
            if (outCount >= 1) {
                g.drawString(font,
                    Component.translatable("gui.tire_change.warn.worn").getString(),
                    tx, ty, COL_WARN, false);
            }
        }

        ty = gy + 62;

        // Hotbar-Bezeichnung
        g.drawString(font,
            Component.translatable("gui.tire_change.hotbar_label").getString(),
            tx, ty, COL_TEXT, false);
        ty += 11;

        // Hotbar-Slots zeichnen
        if (!isTruck) {
            drawHotbar(g, gx, ty, mouseX, mouseY);
        } else {
            g.drawString(font,
                Component.translatable("gui.tire_change.warn.truck").getString(),
                tx, ty, COL_WARN, false);
        }

        // Keine passenden Reifen → Hinweis
        if (!isTruck && selectedSlot < 0) {
            g.drawString(font,
                Component.translatable("gui.tire_change.warn.no_tire").getString(),
                tx, gy + GUI_H - 30, COL_WARN, false);
        }

        super.render(g, mouseX, mouseY, partialTick);

        // Item-Tooltip im Hover
        if (!isTruck) {
            int slotStartX = gx + (GUI_W - 9 * 18) / 2;
            int slotY = gy + 73;
            for (int i = 0; i < 9; i++) {
                int sx = slotStartX + i * 18;
                if (mouseX >= sx && mouseX < sx + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                    ItemStack stack = getHotbarStack(i);
                    if (!stack.isEmpty()) g.renderTooltip(font, stack, mouseX, mouseY);
                }
            }
        }
    }

    private void drawHotbar(GuiGraphics g, int gx, int ty, int mouseX, int mouseY) {
        int slotStartX = gx + (GUI_W - 9 * 18) / 2;
        for (int i = 0; i < 9; i++) {
            int sx = slotStartX + i * 18;
            // Slot-Rahmen
            g.fill(sx,      ty,      sx + 18, ty + 1,  COL_SHADOW);
            g.fill(sx,      ty,      sx + 1,  ty + 18, COL_SHADOW);
            g.fill(sx + 1,  ty + 17, sx + 18, ty + 18, COL_WHITE);
            g.fill(sx + 17, ty + 1,  sx + 18, ty + 18, COL_WHITE);
            g.fill(sx + 1,  ty + 1,  sx + 17, ty + 17, COL_SLOT_BG);

            // Highlight: ausgewählt
            if (i == selectedSlot) {
                g.fill(sx + 1, ty + 1, sx + 17, ty + 17, COL_SEL);
            } else if (isCompatibleSlot(i)) {
                // kompatibler (aber noch nicht ausgewählter) Slot
                g.fill(sx + 1, ty + 1, sx + 17, ty + 17, COL_COMPAT);
            }

            // Item rendern
            ItemStack stack = getHotbarStack(i);
            if (!stack.isEmpty()) {
                g.renderItem(stack, sx + 1, ty + 1);
                g.renderItemDecorations(font, stack, sx + 1, ty + 1);
            }
        }
    }

    // ─── Interaktion ─────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isTruck) {
            int gx = (this.width  - GUI_W) / 2;
            int gy = (this.height - GUI_H) / 2;
            int slotStartX = gx + (GUI_W - 9 * 18) / 2;
            int slotY = gy + 73;
            for (int i = 0; i < 9; i++) {
                int sx = slotStartX + i * 18;
                if (mouseX >= sx && mouseX < sx + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                    if (isCompatibleSlot(i)) {
                        selectedSlot = i;
                        updateSwapButton();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ─── Hilfs-Methoden ──────────────────────────────────────────────────────

    private void autoSelectFirstSlot() {
        for (int i = 0; i < 9; i++) {
            if (isCompatibleSlot(i)) { selectedSlot = i; return; }
        }
        selectedSlot = -1;
    }

    private boolean isCompatibleSlot(int slot) {
        ItemStack stack = getHotbarStack(slot);
        if (stack.isEmpty() || !(stack.getItem() instanceof IVehiclePart vp)) return false;
        var part = vp.getPart(stack);
        if (!(part instanceof PartTireBase tire)) return false;
        TireSeasonType s = tire.getSeasonType();
        if (s == TireSeasonType.ALL_SEASON) return false;
        if (currentTireSeasonType == TireSeasonType.SUMMER) return s == TireSeasonType.WINTER;
        if (currentTireSeasonType == TireSeasonType.WINTER) return s == TireSeasonType.SUMMER;
        return false;
    }

    private ItemStack getHotbarStack(int slot) {
        Player p = Minecraft.getInstance().player;
        if (p == null) return ItemStack.EMPTY;
        return p.getInventory().getItem(slot);
    }

    private void updateSwapButton() {
        if (btnSwap != null) btnSwap.active = !isTruck && selectedSlot >= 0;
    }

    private void performSwap() {
        if (selectedSlot < 0) return;
        Main.SIMPLE_CHANNEL.sendToServer(new MessageTireSwap(vehicleUuid, selectedSlot));
        this.onClose();
    }

    private String getTireTypeName() {
        return switch (currentTireSeasonType) {
            case SUMMER    -> Component.translatable("gui.tire_change.current_tire.summer").getString();
            case WINTER    -> Component.translatable("gui.tire_change.current_tire.winter").getString();
            case ALL_SEASON -> Component.translatable("gui.tire_change.current_tire.allseason").getString();
        };
    }

    private String getSeasonName() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return Component.translatable("gui.tire_change.season.unknown").getString();

        if (SereneSeasonsCompat.isSereneSeasonsLoaded()) {
            if (SereneSeasonsCompat.isWinterConditions(level))
                return Component.translatable("gui.tire_change.season.winter").getString();
            if (SereneSeasonsCompat.isSummerConditions(level))
                return Component.translatable("gui.tire_change.season.summer").getString();
            return Component.translatable("gui.tire_change.season.other").getString();
        }

        // Biom-Fallback
        Player p = Minecraft.getInstance().player;
        if (p != null) {
            float temp = level.getBiome(p.blockPosition()).value().getBaseTemperature();
            if (temp < 0.15f)
                return Component.translatable("gui.tire_change.season.cold").getString();
            return Component.translatable("gui.tire_change.season.warm").getString();
        }
        return Component.translatable("gui.tire_change.season.unknown").getString();
    }

    // ─── Zeichenhilfen ───────────────────────────────────────────────────────

    private void drawFrame(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, COL_BG);
        g.fill(x, y, x + w, y + 1, COL_SHADOW);
        g.fill(x, y, x + 1, y + h, COL_SHADOW);
        g.fill(x, y + h - 1, x + w, y + h, COL_WHITE);
        g.fill(x + w - 1, y, x + w, y + h, COL_WHITE);
    }
}
