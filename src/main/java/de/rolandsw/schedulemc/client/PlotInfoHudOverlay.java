package de.rolandsw.schedulemc.client;
import de.rolandsw.schedulemc.util.EventHelper;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.region.PlotArea;
import de.rolandsw.schedulemc.region.PlotManager;
import de.rolandsw.schedulemc.region.PlotRegion;
import de.rolandsw.schedulemc.region.blocks.PlotInfoBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;

/**
 * HUD-Overlay für Plot-Info-Block
 * Zeigt Plot-Informationen wenn der Spieler den Block anschaut
 *
 * PERFORMANCE: Plot-Daten und SubArea-Listen werden gecacht und nur bei
 * Positionsänderung des Fadenkreuzes neu berechnet (statt 60x/Sek).
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlotInfoHudOverlay {

    private static final float SCALE = 0.8f;
    private static final int HUD_X = 10;
    private static final int HUD_Y = 10;
    private static final int LINE_HEIGHT = 11;

    // PERFORMANCE: Cache für Plot-Daten - vermeidet wiederholte Lookups/Stream-Ops pro Frame
    private static BlockPos lastTargetPos = null;
    private static PlotRegion cachedPlot = null;
    private static List<PlotArea> cachedAvailableSubAreas = null;
    private static int cachedSubAreaCount = 0;
    private static int cachedAvailableCount = 0;
    private static int cachedRentedCount = 0;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        EventHelper.handleEvent(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            HitResult hitResult = mc.hitResult;
            if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return;

            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos targetPos = blockHitResult.getBlockPos();
            BlockState state = mc.level.getBlockState(targetPos);

            // Prüfe ob Spieler auf PlotInfoBlock schaut
            if (state.getBlock() instanceof PlotInfoBlock) {
                // PERFORMANCE: Nur bei neuer Position Plot-Daten neu laden
                if (!targetPos.equals(lastTargetPos)) {
                    lastTargetPos = targetPos;
                    cachedPlot = PlotManager.getPlotAt(targetPos);
                    if (cachedPlot != null) {
                        cachedSubAreaCount = cachedPlot.getSubAreaCount();
                        cachedAvailableCount = cachedPlot.getAvailableSubAreaCount();
                        cachedRentedCount = cachedPlot.getRentedSubAreaCount();
                        cachedAvailableSubAreas = cachedPlot.getAvailableSubAreas();
                    }
                }

                if (cachedPlot != null) {
                    renderPlotHud(event.getGuiGraphics(), mc, cachedPlot);
                }
            } else {
                // Nicht mehr auf PlotInfoBlock → Cache invalidieren
                lastTargetPos = null;
                cachedPlot = null;
            }
        }, "onRenderGuiOverlay");
    }

    /**
     * Rendert das kompakte HUD am oberen linken Bildschirmrand
     */
    private static void renderPlotHud(GuiGraphics gui, Minecraft mc, PlotRegion plot) {
        int currentY = HUD_Y;

        // Berechne benötigte Höhe
        int totalLines = 3; // Titel + Besitzer + Größe

        // +1 wenn Beschreibung vorhanden
        String description = plot.getDescription();
        if (description != null && !description.isEmpty()) {
            totalLines += 1;
        }

        // Verkauf/Miete Status
        if (!plot.hasOwner()) {
            totalLines += 2; // "ZUM VERKAUF" + Preis
        } else if (plot.isForSale()) {
            totalLines += 2;
        } else if (plot.isForRent()) {
            totalLines += 2;
        }

        // Apartments - PERFORMANCE: Gecachte Werte nutzen statt wiederholter Stream-Aufrufe
        if (cachedSubAreaCount > 0) {
            totalLines += 1; // Apartment-Überschrift
            if (cachedAvailableCount > 0) {
                totalLines += Math.min(cachedAvailableCount, 3); // Max 3 Apartments anzeigen
                if (cachedAvailableCount > 3) {
                    totalLines += 1; // "... und X weitere"
                }
            }
        }

        int bgHeight = totalLines * LINE_HEIGHT + 14;
        int bgWidth = 220;

        // Halbtransparenter Hintergrund
        gui.fill(HUD_X - 5, HUD_Y - 5, HUD_X + bgWidth, HUD_Y + bgHeight, 0xDD000000);

        // Rahmen (Top & Bottom)
        gui.fill(HUD_X - 5, HUD_Y - 5, HUD_X + bgWidth, HUD_Y - 4, 0xFF4A90E2);
        gui.fill(HUD_X - 5, HUD_Y + bgHeight - 1, HUD_X + bgWidth, HUD_Y + bgHeight, 0xFF4A90E2);

        // === PLOT-NAME (Titel) ===
        String plotName = plot.getPlotName();
        if (plotName == null || plotName.isEmpty()) {
            plotName = net.minecraft.network.chat.Component.translatable("plot.unnamed").getString();
        }
        gui.pose().pushPose();
        gui.pose().scale(SCALE * 1.1f, SCALE * 1.1f, 1.0f);
        gui.drawString(mc.font, "§6§l" + plotName,
            (int)(HUD_X / (SCALE * 1.1f)), (int)(currentY / (SCALE * 1.1f)), 0xFFD700);
        gui.pose().popPose();
        currentY += LINE_HEIGHT + 2;

        // === BESCHREIBUNG ===
        if (description != null && !description.isEmpty()) {
            drawLine(gui, mc, "§7§o" + description, currentY);
            currentY += LINE_HEIGHT;
        }

        // === BESITZER ===
        if (!plot.hasOwner()) {
            drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.owner_none").getString(), currentY);
        } else {
            String ownerName = plot.getOwnerName();
            drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.owner", ownerName != null ? ownerName : net.minecraft.network.chat.Component.translatable("plot.no_owner").getString()).getString(), currentY);
        }
        currentY += LINE_HEIGHT;

        // === GRÖSSE ===
        drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.size", String.format("%,d", plot.getVolume())).getString(), currentY);
        currentY += LINE_HEIGHT + 3;

        // === VERKAUF/MIETE STATUS ===
        if (!plot.hasOwner()) {
            // Plot ohne Besitzer = zum Verkauf
            drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.for_sale").getString(), currentY);
            currentY += LINE_HEIGHT;
            drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.price", String.format("%.2f€", plot.getPrice())).getString(), currentY);
            currentY += LINE_HEIGHT;
            drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.right_click_details").getString(), currentY);
            currentY += LINE_HEIGHT;
        } else {
            // Plot mit Besitzer
            if (plot.isForSale()) {
                drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.for_sale").getString(), currentY);
                currentY += LINE_HEIGHT;
                drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.price", String.format("%.2f€", plot.getSalePrice())).getString(), currentY);
                currentY += LINE_HEIGHT;
            } else if (plot.isForRent()) {
                if (plot.isRented()) {
                    drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.rented").getString(), currentY);
                    currentY += LINE_HEIGHT;
                    drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.days_left", plot.getRentDaysLeft()).getString(), currentY);
                    currentY += LINE_HEIGHT;
                } else {
                    drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.for_rent").getString(), currentY);
                    currentY += LINE_HEIGHT;
                    drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.rent_per_day", String.format("%.2f€/Tag", plot.getRentPricePerDay())).getString(), currentY);
                    currentY += LINE_HEIGHT;
                }
            }
        }

        // === APARTMENTS === PERFORMANCE: Gecachte Werte nutzen
        if (cachedSubAreaCount > 0) {
            // Trennlinie
            gui.fill(HUD_X, currentY, HUD_X + bgWidth - 10, currentY + 1, 0x44FFFFFF);
            currentY += 4;

            drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.apartments", cachedAvailableCount, cachedSubAreaCount).getString(), currentY);
            currentY += LINE_HEIGHT;

            // Zeige verfügbare Apartments (max 3)
            if (cachedAvailableCount > 0 && cachedAvailableSubAreas != null) {
                int shown = 0;
                for (PlotArea apt : cachedAvailableSubAreas) {
                    if (shown >= 3) break;

                    String aptLine = "  §7├─ §e" + apt.getName() +
                        " §8│ §a" + String.format("%.0f", apt.getMonthlyRent()) + "€/Monat";
                    drawLine(gui, mc, aptLine, currentY);
                    currentY += LINE_HEIGHT;
                    shown++;
                }

                // "... und X weitere"
                if (cachedAvailableCount > 3) {
                    drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.apartments_more", cachedAvailableCount - 3).getString(), currentY);
                    currentY += LINE_HEIGHT;
                }
            }
        }

        // === FOOTER ===
        currentY += 2;
        drawLine(gui, mc, net.minecraft.network.chat.Component.translatable("hud.plot.right_click_options").getString(), currentY);
    }

    /**
     * Helper: Zeichnet eine Text-Zeile
     */
    private static void drawLine(GuiGraphics gui, Minecraft mc, String text, int y) {
        gui.pose().pushPose();
        gui.pose().scale(SCALE, SCALE, 1.0f);
        gui.drawString(mc.font, text, (int)(HUD_X / SCALE), (int)(y / SCALE), 0xFFFFFF);
        gui.pose().popPose();
    }
}
