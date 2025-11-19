package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.tobacco.blockentity.TobaccoPotBlockEntity;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoPotBlock;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoPlantBlock;
import de.rolandsw.schedulemc.tobacco.data.TobaccoPlantData;
import de.rolandsw.schedulemc.tobacco.data.TobaccoPotData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * HUD-Overlay für Tabak-Töpfe und Pflanzen - klein, halbtransparent, am oberen Rand
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TobaccoPotHudOverlay {

    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 8;
    private static final int SEGMENT_WIDTH = BAR_WIDTH / 5; // 5 Einheiten
    private static final float SCALE = 0.65f;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockState state = mc.level.getBlockState(blockHitResult.getBlockPos());

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        // Fall 1: Spieler schaut auf Pflanze
        if (state.getBlock() instanceof TobaccoPlantBlock plant) {
            if (state.getValue(TobaccoPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                renderPlantHud(guiGraphics, mc, screenWidth, plant, state);
            }
            return;
        }

        // Fall 2: Spieler schaut auf Topf
        if (state.getBlock() instanceof TobaccoPotBlock potBlock) {
            BlockEntity be = mc.level.getBlockEntity(blockHitResult.getBlockPos());
            if (be instanceof TobaccoPotBlockEntity potBE) {
                renderPotHud(guiGraphics, mc, screenWidth, potBlock, potBE);
            }
        }
    }

    /**
     * Pflanzen-HUD (Mitte des Bildschirms)
     */
    private static void renderPlantHud(GuiGraphics guiGraphics, Minecraft mc, int screenWidth, TobaccoPlantBlock plant, BlockState state) {
        int age = state.getValue(TobaccoPlantBlock.AGE);
        int growthPercent = (age * 100) / 7;
        boolean isFullyGrown = age >= 7;

        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2 + 40;

        // Kompakte Box
        int bgWidth = 110;
        int bgHeight = 30;
        int bgX = centerX - bgWidth / 2;
        int bgY = centerY - bgHeight / 2;

        // Halbtransparenter Hintergrund
        guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, 0x66000000);

        // Pflanzenname
        String plantName = plant.getTobaccoType().getColoredName();
        int textWidth = (int) (mc.font.width(plantName) * SCALE);
        int textX = centerX - textWidth / 2;
        int textY = bgY + 4;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
        guiGraphics.drawString(mc.font, plantName, (int)(textX / SCALE), (int)(textY / SCALE), 0xFFFFFF);
        guiGraphics.pose().popPose();

        // Wachstums-Balken
        int barX = centerX - 40;
        int barY = bgY + 16;
        drawProgressBar(guiGraphics, barX, barY, 80, BAR_HEIGHT, growthPercent, isFullyGrown ? 0xFF4CAF50 : 0xFFFDD835, false);

        // Prozent-Text
        String percentText = growthPercent + "%";
        if (isFullyGrown) percentText += " §a✓";
        int percentWidth = (int) (mc.font.width(percentText) * SCALE);
        int percentX = centerX - percentWidth / 2;
        int percentY = bgY + 24;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
        int color = isFullyGrown ? 0x00FF00 : 0xFFFFFF;
        guiGraphics.drawString(mc.font, percentText, (int)(percentX / SCALE), (int)(percentY / SCALE), color);
        guiGraphics.pose().popPose();
    }

    /**
     * Topf-HUD (Oberer Bildschirmrand)
     */
    private static void renderPotHud(GuiGraphics guiGraphics, Minecraft mc, int screenWidth, TobaccoPotBlock potBlock, TobaccoPotBlockEntity potBE) {
        TobaccoPotData potData = potBE.getPotData();
        if (potData == null) return;

        int hudX = 10;
        int hudY = 10;

        // Berechne Höhe basierend auf Inhalt
        int bgHeight = 50;
        if (potData.hasSoil()) bgHeight = 70;
        if (potData.hasPlant()) bgHeight = 90;

        int bgWidth = BAR_WIDTH + 20;

        // Halbtransparenter Hintergrund
        guiGraphics.fill(hudX - 5, hudY - 5, hudX + bgWidth + 5, hudY + bgHeight, 0x66000000);

        int currentY = hudY;

        // Topf-Typ
        String potName = potBlock.getPotType().getColoredName();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
        guiGraphics.drawString(mc.font, potName, (int)(hudX / SCALE), (int)(currentY / SCALE), 0xFFFFFF);
        guiGraphics.pose().popPose();

        currentY += 12;

        if (!potData.hasSoil()) {
            // Keine Erde
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
            guiGraphics.drawString(mc.font, "§cKeine Erde!", (int)(hudX / SCALE), (int)(currentY / SCALE), 0xFF0000);
            guiGraphics.pose().popPose();
            return;
        }

        // Wasser-Balken
        String waterLabel = "Wasser: " + potData.getWaterLevel() + "/" + potData.getMaxWater();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
        guiGraphics.drawString(mc.font, waterLabel, (int)(hudX / SCALE), (int)((currentY - 2) / SCALE), 0x88CCFF);
        guiGraphics.pose().popPose();
        currentY += 10;

        float waterRatio = (float) potData.getWaterLevel() / potData.getMaxWater();
        drawResourceBar(guiGraphics, hudX, currentY, waterRatio, 0xFF2196F3);
        currentY += BAR_HEIGHT + 10;

        // Erd-Balken
        String soilLabel = "Erde: " + potData.getSoilLevel() + "/" + potData.getMaxSoil();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
        guiGraphics.drawString(mc.font, soilLabel, (int)(hudX / SCALE), (int)((currentY - 2) / SCALE), 0xFFCC88);
        guiGraphics.pose().popPose();
        currentY += 10;

        float soilRatio = (float) potData.getSoilLevel() / potData.getMaxSoil();
        drawResourceBar(guiGraphics, hudX, currentY, soilRatio, 0xFF8D6E63);
        currentY += BAR_HEIGHT + 10;

        // Pflanzen-Info (falls vorhanden)
        if (potData.hasPlant()) {
            TobaccoPlantData plant = potData.getPlant();

            // Booster-Icons
            String boosters = "";
            if (plant.hasFertilizer()) boosters += "§6[Dünger] ";
            if (plant.hasGrowthBooster()) boosters += "§e[Wachstum+] ";
            if (plant.hasQualityBooster()) boosters += "§d[Qualität+] ";

            if (!boosters.isEmpty()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
                guiGraphics.drawString(mc.font, boosters.trim(), (int)(hudX / SCALE), (int)(currentY / SCALE), 0xFFFFFF);
                guiGraphics.pose().popPose();
            }
        }
    }

    /**
     * Ressourcen-Balken mit 5 Segmenten
     */
    private static void drawResourceBar(GuiGraphics guiGraphics, int x, int y, float fillRatio, int color) {
        // Hintergrund
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF2A2A2A);

        // Gefüllter Teil
        int filledWidth = (int) (BAR_WIDTH * fillRatio);
        if (filledWidth > 0) {
            guiGraphics.fill(x, y, x + filledWidth, y + BAR_HEIGHT, color);
        }

        // Segment-Trennlinien (5 Einheiten)
        for (int i = 1; i < 5; i++) {
            int segmentX = x + (i * SEGMENT_WIDTH);
            guiGraphics.fill(segmentX, y, segmentX + 1, y + BAR_HEIGHT, 0x88FFFFFF);
        }

        // Rahmen
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + 1, 0x88FFFFFF);
        guiGraphics.fill(x, y + BAR_HEIGHT - 1, x + BAR_WIDTH, y + BAR_HEIGHT, 0x88FFFFFF);
        guiGraphics.fill(x, y, x + 1, y + BAR_HEIGHT, 0x88FFFFFF);
        guiGraphics.fill(x + BAR_WIDTH - 1, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0x88FFFFFF);
    }

    /**
     * Fortschritts-Balken ohne Segmente
     */
    private static void drawProgressBar(GuiGraphics guiGraphics, int x, int y, int width, int height, int percent, int color, boolean showSegments) {
        // Hintergrund
        guiGraphics.fill(x, y, x + width, y + height, 0xFF2A2A2A);

        // Gefüllter Teil
        int filledWidth = (width * percent) / 100;
        if (filledWidth > 0) {
            guiGraphics.fill(x, y, x + filledWidth, y + height, color);
        }

        // Rahmen
        guiGraphics.fill(x, y, x + width, y + 1, 0x88FFFFFF);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0x88FFFFFF);
        guiGraphics.fill(x, y, x + 1, y + height, 0x88FFFFFF);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0x88FFFFFF);
    }
}
