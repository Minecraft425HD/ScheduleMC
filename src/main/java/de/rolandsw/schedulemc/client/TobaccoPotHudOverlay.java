package de.rolandsw.schedulemc.client;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.tobacco.blockentity.TobaccoPotBlockEntity;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoPotBlock;
import de.rolandsw.schedulemc.tobacco.data.TobaccoPlantData;
import de.rolandsw.schedulemc.tobacco.data.TobaccoPotData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * HUD-Overlay für Tabak-Töpfe - zeigt Wasser, Erde und Pflanzen-Info am oberen Bildschirmrand
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TobaccoPotHudOverlay {

    private static final int HUD_X = 10; // Position von links
    private static final int HUD_Y = 10; // Position von oben
    private static final int BAR_WIDTH = 200; // Breite eines Fortschrittsbalkens
    private static final int BAR_HEIGHT = 12; // Höhe eines Fortschrittsbalkens
    private static final int BAR_SPACING = 18; // Abstand zwischen Balken
    private static final int SEGMENT_WIDTH = BAR_WIDTH / 5; // Breite pro Einheit (5 Einheiten)

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Prüfe ob Spieler auf einen Topf schaut
        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockEntity blockEntity = mc.level.getBlockEntity(blockHitResult.getBlockPos());

        if (!(blockEntity instanceof TobaccoPotBlockEntity potBlockEntity)) return;

        TobaccoPotData potData = potBlockEntity.getPotData();
        if (potData == null) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();

        // Hintergrund zeichnen
        int bgHeight = potData.hasPlant() ? 85 : 60;
        guiGraphics.fill(HUD_X - 5, HUD_Y - 5, HUD_X + BAR_WIDTH + 10, HUD_Y + bgHeight, 0xAA000000);

        // Topf-Typ anzeigen
        TobaccoPotBlock block = (TobaccoPotBlock) mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock();
        String potName = block.getPotType().getColoredName();
        guiGraphics.drawString(mc.font, potName, HUD_X, HUD_Y, 0xFFFFFF);

        int currentY = HUD_Y + 15;

        // Wasser-Balken zeichnen
        if (potData.hasSoil()) {
            drawResourceBar(guiGraphics, mc, HUD_X, currentY, "Wasser", potData.getWaterLevel(), potData.getMaxWater(), 0x3F51B5, 0x1976D2);
            currentY += BAR_SPACING;

            // Erd-Balken zeichnen
            drawResourceBar(guiGraphics, mc, HUD_X, currentY, "Erde", potData.getSoilLevel(), potData.getMaxSoil(), 0x8D6E63, 0x5D4037);
            currentY += BAR_SPACING;

            // Pflanzen-Info
            if (potData.hasPlant()) {
                TobaccoPlantData plant = potData.getPlant();
                currentY += 5;

                String plantInfo = plant.getType().getColoredName() + " - " + plant.getQuality().getColoredName();
                guiGraphics.drawString(mc.font, plantInfo, HUD_X, currentY, 0xFFFFFF);
                currentY += 12;

                // Wachstums-Balken
                int growthPercent = (plant.getGrowthStage() * 100) / 7;
                drawGrowthBar(guiGraphics, mc, HUD_X, currentY, growthPercent, plant.isFullyGrown());
            }
        } else {
            guiGraphics.drawString(mc.font, "§cKeine Erde!", HUD_X, currentY, 0xFF0000);
        }
    }

    /**
     * Zeichnet einen Ressourcen-Balken (Wasser/Erde) mit 5 Einheiten
     */
    private static void drawResourceBar(GuiGraphics guiGraphics, Minecraft mc, int x, int y, String label, int current, int max, int colorFilled, int colorDark) {
        // Label
        guiGraphics.drawString(mc.font, label + ": " + current + "/" + max, x, y - 10, 0xFFFFFF);

        // Hintergrund-Balken
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF333333);

        // Berechne Füllgrad (0-100 System, 5 Einheiten = 20 pro Einheit)
        float fillRatio = (float) current / max;
        int filledWidth = (int) (BAR_WIDTH * fillRatio);

        // Gefüllter Teil
        if (filledWidth > 0) {
            guiGraphics.fill(x, y, x + filledWidth, y + BAR_HEIGHT, 0xFF000000 | colorFilled);
        }

        // Zeichne Trennlinien für 5 Einheiten
        for (int i = 1; i < 5; i++) {
            int segmentX = x + (i * SEGMENT_WIDTH);
            guiGraphics.fill(segmentX, y, segmentX + 1, y + BAR_HEIGHT, 0xFFFFFFFF);
        }

        // Rahmen
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + 1, 0xFFFFFFFF); // oben
        guiGraphics.fill(x, y + BAR_HEIGHT - 1, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFFFFFFFF); // unten
        guiGraphics.fill(x, y, x + 1, y + BAR_HEIGHT, 0xFFFFFFFF); // links
        guiGraphics.fill(x + BAR_WIDTH - 1, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFFFFFFFF); // rechts
    }

    /**
     * Zeichnet den Wachstums-Balken
     */
    private static void drawGrowthBar(GuiGraphics guiGraphics, Minecraft mc, int x, int y, int percent, boolean fullyGrown) {
        // Label
        String label = "Wachstum: " + percent + "%";
        if (fullyGrown) {
            label += " §a✓ REIF";
        }
        guiGraphics.drawString(mc.font, label, x, y - 10, fullyGrown ? 0x00FF00 : 0xFFFFFF);

        // Hintergrund-Balken
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF333333);

        // Gefüllter Teil (Grün wenn reif, sonst gelb)
        int filledWidth = (BAR_WIDTH * percent) / 100;
        int color = fullyGrown ? 0x4CAF50 : 0xFFEB3B;

        if (filledWidth > 0) {
            guiGraphics.fill(x, y, x + filledWidth, y + BAR_HEIGHT, 0xFF000000 | color);
        }

        // Rahmen
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y + BAR_HEIGHT - 1, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + BAR_HEIGHT, 0xFFFFFFFF);
        guiGraphics.fill(x + BAR_WIDTH - 1, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFFFFFFFF);
    }
}
