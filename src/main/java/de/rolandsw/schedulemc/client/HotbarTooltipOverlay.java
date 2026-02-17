package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.util.EventHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector2i;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HUD Overlay für alle Spieler (inkl. Admins):
 * Zeigt den Item-Tooltip des aktiven Hotbar-Slots an,
 * wenn der Spieler den Slot wechselt oder das Item ändert.
 * Der Tooltip erscheint für 4 Sekunden über der Hotbar.
 *
 * Hintergrund: Normale Spieler können das Inventar nicht öffnen,
 * dadurch sind keine Maus-Hovers über Hotbar-Items möglich.
 * Dieses Overlay kompensiert das durch automatische Tooltip-Anzeige.
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HotbarTooltipOverlay {

    private static final int DISPLAY_DURATION = 80; // Ticks (4 Sekunden bei 20 TPS)

    private static int lastSelectedSlot = -1;
    private static int lastItemSignature = 0;
    private static int displayTicks = 0;

    /**
     * Client-Tick: Erkennt Slot-Wechsel und Item-Änderungen, steuert den Anzeigetimer.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        EventHelper.handleEvent(() -> {
            if (event.phase != TickEvent.Phase.END) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            int selectedSlot = mc.player.getInventory().selected;
            ItemStack item = mc.player.getInventory().getItem(selectedSlot);
            int sig = computeSignature(selectedSlot, item);

            // Slot oder Item hat sich geändert → Timer neu starten
            if (selectedSlot != lastSelectedSlot || sig != lastItemSignature) {
                lastSelectedSlot = selectedSlot;
                lastItemSignature = sig;
                displayTicks = item.isEmpty() ? 0 : DISPLAY_DURATION;
            }

            if (displayTicks > 0) {
                displayTicks--;
            }
        }, "HotbarTooltipOverlay_ClientTick");
    }

    /**
     * Render: Tooltip über dem aktiven Hotbar-Slot anzeigen.
     * Wird nur nach dem Hotbar-Layer gerendert (einmal pro Frame).
     */
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        EventHelper.handleEvent(() -> {
            // Nur nach dem Hotbar-Layer rendern (verhindert Mehrfach-Rendering pro Frame)
            if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            // Kein Overlay wenn ein anderer Screen (z.B. Chat, Werkzeugkiste) offen ist
            if (mc.screen != null) return;

            if (displayTicks <= 0) return;

            int selectedSlot = mc.player.getInventory().selected;
            ItemStack item = mc.player.getInventory().getItem(selectedSlot);
            if (item.isEmpty()) return;

            GuiGraphics guiGraphics = event.getGuiGraphics();

            // Tooltip-Komponenten aus dem Item ermitteln
            TooltipFlag flag = mc.options.advancedItemTooltips.get()
                    ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
            List<ClientTooltipComponent> components = item.getTooltipLines(mc.player, flag).stream()
                    .map(l -> ClientTooltipComponent.create(l.getVisualOrderText()))
                    .collect(Collectors.toList());
            if (components.isEmpty()) return;

            // Exakt oben links (0,0) – eigener Positioner umgeht den Default-Offset (+12/-12)
            guiGraphics.renderTooltip(mc.font, components, 0, 0,
                    (sw, sh, mx, my, tw, th) -> new Vector2i(0, 0));
        }, "HotbarTooltipOverlay_Render");
    }

    /**
     * Berechnet eine Signatur für Slot + Item zur Änderungserkennung.
     * Berücksichtigt Slot-Index, Item-Typ, Anzahl und NBT-Tag.
     */
    private static int computeSignature(int slot, ItemStack stack) {
        if (stack.isEmpty()) return -(slot + 1);
        int hash = slot;
        hash = hash * 31 + stack.getItem().hashCode();
        hash = hash * 31 + stack.getCount();
        if (stack.getTag() != null) {
            hash = hash * 31 + stack.getTag().hashCode();
        }
        return hash;
    }
}
