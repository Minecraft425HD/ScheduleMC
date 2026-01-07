package de.rolandsw.schedulemc.client;
import de.rolandsw.schedulemc.util.UIColors;
nimport de.rolandsw.schedulemc.util.StringUtils;
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

/**
 * HUD-Overlay für Plot-Info-Block
 * Zeigt Plot-Informationen wenn der Spieler den Block anschaut
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlotInfoHudOverlay {

    private static final float SCALE = 0.8f;
    private static final int HUD_X = 10;
    private static final int HUD_Y = 10;
    private static final int LINE_HEIGHT = 11;

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
                PlotRegion plot = PlotManager.getPlotAt(targetPos);

                if (plot != null) {
                    renderPlotHud(event.getGuiGraphics(), mc, plot);
                }
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

        // Apartments
        if (plot.getSubAreaCount() > 0) {
            totalLines += 1; // Apartment-Überschrift
            int availableApts = plot.getAvailableSubAreaCount();
            if (availableApts > 0) {
                totalLines += Math.min(availableApts, 3); // Max 3 Apartments anzeigen
                if (availableApts > 3) {
                    totalLines += 1; // "... und X weitere"
                }
            }
        }

        int bgHeight = totalLines * LINE_HEIGHT + 14;
        int bgWidth = 220;

        // Halbtransparenter Hintergrund
        gui.fill(HUD_X - 5, HUD_Y - 5, HUD_X + bgWidth, HUD_Y + bgHeight, 0xDD000000);

        // Rahmen (Top & Bottom)
        gui.fill(HUD_X - 5, HUD_Y - 5, HUD_X + bgWidth, HUD_Y - 4, UIColors.ACCENT_BLUE);
        gui.fill(HUD_X - 5, HUD_Y + bgHeight - 1, HUD_X + bgWidth, HUD_Y + bgHeight, UIColors.ACCENT_BLUE);

        // === PLOT-NAME (Titel) ===
        String plotName = plot.getPlotName();
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
        String ownerName = plot.getOwnerName();
        if (ownerName == null || ownerName.equals("Niemand")) {
            drawLine(gui, mc, "§7Besitzer: §cKein Besitzer", currentY);
        } else {
            drawLine(gui, mc, "§7Besitzer: §f" + ownerName, currentY);
        }
        currentY += LINE_HEIGHT;

        // === GRÖSSE ===
        drawLine(gui, mc, "§7Größe: §e" + String.format("%,d", plot.getVolume()) + " Blöcke", currentY);
        currentY += LINE_HEIGHT + 3;

        // === VERKAUF/MIETE STATUS ===
        if (!plot.hasOwner()) {
            // Plot ohne Besitzer = zum Verkauf
            drawLine(gui, mc, "§a§l⚡ ZUM VERKAUF", currentY);
            currentY += LINE_HEIGHT;
            drawLine(gui, mc, "§7Preis: §e" + String.format("%.2f", plot.getPrice()) + "€", currentY);
            currentY += LINE_HEIGHT;
            drawLine(gui, mc, "§8Rechtsklick für Details", currentY);
            currentY += LINE_HEIGHT;
        } else {
            // Plot mit Besitzer
            if (plot.isForSale()) {
                drawLine(gui, mc, "§a§l⚡ ZUM VERKAUF", currentY);
                currentY += LINE_HEIGHT;
                drawLine(gui, mc, "§7Preis: §e" + String.format("%.2f", plot.getSalePrice()) + "€", currentY);
                currentY += LINE_HEIGHT;
            } else if (plot.isForRent()) {
                if (plot.isRented()) {
                    drawLine(gui, mc, "§a§l✓ VERMIETET", currentY);
                    currentY += LINE_HEIGHT;
                    drawLine(gui, mc, "§7Noch §e" + plot.getRentDaysLeft() + " Tage", currentY);
                    currentY += LINE_HEIGHT;
                } else {
                    drawLine(gui, mc, "§d§l⚡ ZU VERMIETEN", currentY);
                    currentY += LINE_HEIGHT;
                    drawLine(gui, mc, "§7Miete: §e" + String.format("%.2f", plot.getRentPricePerDay()) + "€/Tag", currentY);
                    currentY += LINE_HEIGHT;
                }
            }
        }

        // === APARTMENTS ===
        if (plot.getSubAreaCount() > 0) {
            // Trennlinie
            gui.fill(HUD_X, currentY, HUD_X + bgWidth - 10, currentY + 1, UIColors.WHITE_SEMI_TRANSPARENT);
            currentY += 4;

            int rentedCount = plot.getRentedSubAreaCount();
            int availableCount = plot.getAvailableSubAreaCount();

            drawLine(gui, mc, "§6🏠 Apartments: §e" + availableCount + " §7verfügbar §8(" +
                plot.getSubAreaCount() + " gesamt)", currentY);
            currentY += LINE_HEIGHT;

            // Zeige verfügbare Apartments (max 3)
            if (availableCount > 0) {
                int shown = 0;
                for (PlotArea apt : plot.getAvailableSubAreas()) {
                    if (shown >= 3) break;

                    String aptLine = "  §7├─ §e" + apt.getName() +
                        " §8│ §a" + String.format("%.0f", apt.getMonthlyRent()) + "€/Monat";
                    drawLine(gui, mc, aptLine, currentY);
                    currentY += LINE_HEIGHT;
                    shown++;
                }

                // "... und X weitere"
                if (availableCount > 3) {
                    drawLine(gui, mc, "  §7└─ §8... und " + (availableCount - 3) + " weitere", currentY);
                    currentY += LINE_HEIGHT;
                }
            }
        }

        // === FOOTER ===
        currentY += 2;
        drawLine(gui, mc, "§8§oRechtsklick für Details & Optionen", currentY);
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
