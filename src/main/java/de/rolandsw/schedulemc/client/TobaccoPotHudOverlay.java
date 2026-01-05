package de.rolandsw.schedulemc.client;
import de.rolandsw.schedulemc.util.EventHelper;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.production.blockentity.PlantPotBlockEntity;
import de.rolandsw.schedulemc.production.blocks.PlantPotBlock;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoPlantBlock;
import de.rolandsw.schedulemc.tobacco.data.TobaccoPlantData;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
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
 * Vereinheitlichtes HUD-Overlay am oberen Bildschirmrand für Töpfe und Pflanzen
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TobaccoPotHudOverlay {

    // Layout constants
    private static final int BAR_WIDTH = 60; // 50% kürzer (120 → 60)
    private static final int BAR_HEIGHT = 8;
    private static final int SEGMENT_WIDTH = BAR_WIDTH / 5;
    private static final float SCALE = 0.7f;
    private static final int HUD_X = 10;
    private static final int HUD_Y = 10;
    private static final int BG_WIDTH_PADDING = 20;
    private static final int PERCENT_MULTIPLIER = 100;

    // Colors
    private static final int COLOR_WATER_BLUE = 0xFF2196F3;
    private static final int COLOR_SOIL_BROWN = 0xFF8D6E63;
    private static final int COLOR_BAR_BACKGROUND = 0xFF1A1A1A;
    private static final int COLOR_FULLY_GROWN_GREEN = 0xFF4CAF50;
    private static final int COLOR_GROWING_YELLOW = 0xFFFDD835;

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

            GuiGraphics guiGraphics = event.getGuiGraphics();

            // Fall 1: Spieler schaut auf Pflanze - zeige Topf darunter
            if (state.getBlock() instanceof TobaccoPlantBlock) {
                if (state.getValue(TobaccoPlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                    targetPos = targetPos.below(); // Gehe zum unteren Teil
                }
                // Topf ist unter der Pflanze
                BlockPos potPos = targetPos.below();
                BlockEntity be = mc.level.getBlockEntity(potPos);
                BlockState potState = mc.level.getBlockState(potPos);

                if (be instanceof PlantPotBlockEntity potBE && potState.getBlock() instanceof PlantPotBlock potBlock) {
                    renderUnifiedHud(guiGraphics, mc, potBlock, potBE, true);
                }
                return;
            }

            // Fall 2: Spieler schaut direkt auf Topf
            if (state.getBlock() instanceof PlantPotBlock potBlock) {
                BlockEntity be = mc.level.getBlockEntity(targetPos);
                if (be instanceof PlantPotBlockEntity potBE) {
                    renderUnifiedHud(guiGraphics, mc, potBlock, potBE, false);
                }
            }
        }, "onRenderGuiOverlay");
    }

    /**
     * Vereinheitlichtes HUD am oberen Bildschirmrand
     */
    private static void renderUnifiedHud(GuiGraphics guiGraphics, Minecraft mc, PlantPotBlock potBlock,
                                         PlantPotBlockEntity potBE, boolean lookingAtPlant) {
        PlantPotData potData = potBE.getPotData();
        if (potData == null) return;

        int currentY = HUD_Y;

        // Berechne dynamische Höhe
        int lineHeight = 12;
        int totalLines = 1; // Topf-Typ

        if (potData.hasSoil()) {
            totalLines += 5; // Wasser-Label + Balken + Erde-Label + Balken + Licht
        } else {
            totalLines += 1; // "Keine Erde"
        }

        if (potData.hasPlant()) {
            totalLines += 2; // Pflanze-Info + Wachstum
            TobaccoPlantData plant = potData.getPlant();
            if (plant.hasFertilizer() || plant.hasGrowthBooster() || plant.hasQualityBooster()) {
                totalLines += 1; // Booster
            }
        }

        int bgHeight = totalLines * lineHeight + 10;
        int bgWidth = BAR_WIDTH + BG_WIDTH_PADDING;

        // Halbtransparenter Hintergrund
        guiGraphics.fill(HUD_X - 5, HUD_Y - 5, HUD_X + bgWidth + 5, HUD_Y + bgHeight, 0x88000000);

        // Topf-Typ
        String potName = potBlock.getPotType().getColoredName();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
        guiGraphics.drawString(mc.font, potName, (int)(HUD_X / SCALE), (int)(currentY / SCALE), 0xFFFFFF);
        guiGraphics.pose().popPose();
        currentY += lineHeight;

        if (!potData.hasSoil()) {
            // Keine Erde - zeige Warnung
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
            guiGraphics.drawString(mc.font, "§c⚠ Keine Erde!", (int)(HUD_X / SCALE), (int)(currentY / SCALE), 0xFF0000);
            guiGraphics.pose().popPose();
            return;
        }

        // Wasser-Balken
        String waterLabel = "§bWasser: " + potData.getWaterLevel() + "/" + potData.getMaxWater();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
        guiGraphics.drawString(mc.font, waterLabel, (int)(HUD_X / SCALE), (int)(currentY / SCALE), 0xFFFFFF);
        guiGraphics.pose().popPose();
        currentY += 10;

        float waterRatio = (float) potData.getWaterLevel() / potData.getMaxWater();
        drawResourceBar(guiGraphics, HUD_X, currentY, waterRatio, COLOR_WATER_BLUE);
        currentY += BAR_HEIGHT + 4;

        // Erd-Balken
        String soilLabel = "§6Erde: " + potData.getSoilLevel() + "/" + potData.getMaxSoil();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
        guiGraphics.drawString(mc.font, soilLabel, (int)(HUD_X / SCALE), (int)(currentY / SCALE), 0xFFFFFF);
        guiGraphics.pose().popPose();
        currentY += 10;

        float soilRatio = (float) potData.getSoilLevel() / potData.getMaxSoil();
        drawResourceBar(guiGraphics, HUD_X, currentY, soilRatio, COLOR_SOIL_BROWN);
        currentY += BAR_HEIGHT + 4;

        // Lichtlevel anzeigen
        BlockPos potPos = potBE.getBlockPos();
        BlockPos plantPos = potPos.above(2);  // 2 Blöcke über Topf = Pflanze
        int lightLevel = mc.level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, plantPos);
        int minLight = de.rolandsw.schedulemc.config.ModConfigHandler.TOBACCO.MIN_LIGHT_LEVEL.get();
        boolean hasEnoughLight = lightLevel >= minLight;

        String lightLabel = "§eLicht: " + lightLevel + "/15";
        String lightStatus;
        int lightColor;

        if (!de.rolandsw.schedulemc.config.ModConfigHandler.TOBACCO.REQUIRE_LIGHT_FOR_GROWTH.get()) {
            lightStatus = " §8(optional)";
            lightColor = 0xFFFFFF;
        } else if (lightLevel >= 14) {
            lightStatus = " §a✓";
            lightColor = 0x00FF00;
        } else if (hasEnoughLight) {
            lightStatus = " §7(ok)";
            lightColor = 0xFFFF00;
        } else {
            lightStatus = " §c✗";
            lightColor = 0xFF0000;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
        guiGraphics.drawString(mc.font, lightLabel + lightStatus, (int)(HUD_X / SCALE), (int)(currentY / SCALE), lightColor);
        guiGraphics.pose().popPose();
        currentY += 10;

        // Pflanzen-Info (falls vorhanden)
        if (potData.hasPlant()) {
            TobaccoPlantData plant = potData.getPlant();

            // Trennlinie
            guiGraphics.fill(HUD_X, currentY, HUD_X + BAR_WIDTH, currentY + 1, 0x44FFFFFF);
            currentY += 4;

            // Pflanzen-Typ + Qualität
            String plantInfo = plant.getType().getColoredName() + " §7| " + plant.getQuality().getColoredName();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
            guiGraphics.drawString(mc.font, plantInfo, (int)(HUD_X / SCALE), (int)(currentY / SCALE), 0xFFFFFF);
            guiGraphics.pose().popPose();
            currentY += 10;

            // Wachstum
            boolean isFullyGrown = plant.isFullyGrown();
            // Bei erntereifer Pflanze immer 100% anzeigen, auch wenn Ressourcen aufgebraucht
            int growthPercent = isFullyGrown ? PERCENT_MULTIPLIER : (plant.getGrowthStage() * PERCENT_MULTIPLIER) / 7;

            String growthLabel = "§eWachstum: " + growthPercent + "%" + (isFullyGrown ? " §a✓" : "");
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
            guiGraphics.drawString(mc.font, growthLabel, (int)(HUD_X / SCALE), (int)(currentY / SCALE),
                                 isFullyGrown ? 0x00FF00 : 0xFFFFFF);
            guiGraphics.pose().popPose();
            currentY += 10;

            drawProgressBar(guiGraphics, HUD_X, currentY, growthPercent, isFullyGrown);
            currentY += BAR_HEIGHT + 4;

            // Booster
            String boosters = "";
            if (plant.hasFertilizer()) boosters += "§6[Dünger] ";
            if (plant.hasGrowthBooster()) boosters += "§e[Wachstum+] ";
            if (plant.hasQualityBooster()) boosters += "§d[Qualität+]";

            if (!boosters.isEmpty()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(SCALE * 0.9f, SCALE * 0.9f, 1.0f);
                guiGraphics.drawString(mc.font, boosters.trim(), (int)(HUD_X / (SCALE * 0.9f)),
                                     (int)(currentY / (SCALE * 0.9f)), 0xFFFFFF);
                guiGraphics.pose().popPose();
            }
        }
    }

    /**
     * Ressourcen-Balken mit 5 Segmenten
     */
    private static void drawResourceBar(GuiGraphics guiGraphics, int x, int y, float fillRatio, int color) {
        // Hintergrund
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_BAR_BACKGROUND);

        // Gefüllter Teil
        int filledWidth = (int) (BAR_WIDTH * fillRatio);
        if (filledWidth > 0) {
            guiGraphics.fill(x, y, x + filledWidth, y + BAR_HEIGHT, color);
        }

        // Segment-Trennlinien (5 Einheiten)
        for (int i = 1; i < 5; i++) {
            int segmentX = x + (i * SEGMENT_WIDTH);
            guiGraphics.fill(segmentX, y, segmentX + 1, y + BAR_HEIGHT, 0xAAFFFFFF);
        }

        // Rahmen
        guiGraphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y, 0xAAFFFFFF); // oben
        guiGraphics.fill(x - 1, y + BAR_HEIGHT, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xAAFFFFFF); // unten
        guiGraphics.fill(x - 1, y, x, y + BAR_HEIGHT, 0xAAFFFFFF); // links
        guiGraphics.fill(x + BAR_WIDTH, y, x + BAR_WIDTH + 1, y + BAR_HEIGHT, 0xAAFFFFFF); // rechts
    }

    /**
     * Fortschritts-Balken (Wachstum)
     */
    private static void drawProgressBar(GuiGraphics guiGraphics, int x, int y, int percent, boolean fullyGrown) {
        int color = fullyGrown ? COLOR_FULLY_GROWN_GREEN : COLOR_GROWING_YELLOW;

        // Hintergrund
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_BAR_BACKGROUND);

        // Gefüllter Teil
        int filledWidth = (BAR_WIDTH * percent) / PERCENT_MULTIPLIER;
        if (filledWidth > 0) {
            guiGraphics.fill(x, y, x + filledWidth, y + BAR_HEIGHT, color);
        }

        // Rahmen
        guiGraphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y, 0xAAFFFFFF);
        guiGraphics.fill(x - 1, y + BAR_HEIGHT, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xAAFFFFFF);
        guiGraphics.fill(x - 1, y, x, y + BAR_HEIGHT, 0xAAFFFFFF);
        guiGraphics.fill(x + BAR_WIDTH, y, x + BAR_WIDTH + 1, y + BAR_HEIGHT, 0xAAFFFFFF);
    }
}
