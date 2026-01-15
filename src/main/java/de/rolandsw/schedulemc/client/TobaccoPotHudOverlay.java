package de.rolandsw.schedulemc.client;
import de.rolandsw.schedulemc.util.EventHelper;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.cannabis.blocks.CannabisPlantBlock;
import de.rolandsw.schedulemc.coca.blocks.CocaPlantBlock;
import de.rolandsw.schedulemc.poppy.blocks.PoppyPlantBlock;
import de.rolandsw.schedulemc.production.blockentity.PlantPotBlockEntity;
import de.rolandsw.schedulemc.production.blocks.PlantPotBlock;
import de.rolandsw.schedulemc.production.core.PotType;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoPlantBlock;
import de.rolandsw.schedulemc.production.data.PlantPotData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
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

    private static final int BAR_WIDTH = 60;
    private static final int BAR_HEIGHT = 8;
    private static final int SEGMENT_WIDTH = BAR_WIDTH / 5;
    private static final float SCALE = 0.7f;
    private static final int HUD_X = 10;
    private static final int HUD_Y = 10;

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
            Block block = state.getBlock();
            BlockPos potPos = null;

            if (block instanceof TobaccoPlantBlock) {
                if (state.getValue(TobaccoPlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                    targetPos = targetPos.below();
                }
                potPos = targetPos.below();
            } else if (block instanceof CannabisPlantBlock) {
                if (state.getValue(CannabisPlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                    targetPos = targetPos.below();
                }
                potPos = targetPos.below();
            } else if (block instanceof CocaPlantBlock) {
                if (state.getValue(CocaPlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                    targetPos = targetPos.below();
                }
                potPos = targetPos.below();
            } else if (block instanceof PoppyPlantBlock) {
                if (state.getValue(PoppyPlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                    targetPos = targetPos.below();
                }
                potPos = targetPos.below();
            }

            if (potPos != null) {
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

        // Prüfe ob Pflanze erntebereit ist
        boolean isHarvestReady = isPlantHarvestReady(potData);

        if (isHarvestReady) {
            totalLines += 1; // ERNTEBEREIT!
        }

        if (potData.hasSoil() || potData.hasMist()) {
            totalLines += 5; // Wasser-Label + Balken + Erde-Label + Balken + Licht
        } else {
            totalLines += 1; // "Keine Erde"
        }

        if (potData.hasPlant() && !isHarvestReady) {
            totalLines += 2; // Pflanze-Info + Wachstum
        }

        int bgHeight = totalLines * lineHeight + 10;
        int bgWidth = BAR_WIDTH + 20;

        // Halbtransparenter Hintergrund
        guiGraphics.fill(HUD_X - 5, HUD_Y - 5, HUD_X + bgWidth + 5, HUD_Y + bgHeight, 0x88000000);

        // ═══════════════════════════════════════════════════════════
        // ERNTEBEREIT! - Auffällige Anzeige
        // ═══════════════════════════════════════════════════════════
        if (isHarvestReady) {
            // Blinkender Effekt (alle 500ms)
            boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
            String harvestText = blink ? "§a§l✓ ERNTEBEREIT!" : "§2§l✓ ERNTEBEREIT!";

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(SCALE * 1.2f, SCALE * 1.2f, 1.0f);
            guiGraphics.drawString(mc.font, harvestText, (int)(HUD_X / (SCALE * 1.2f)), (int)(currentY / (SCALE * 1.2f)), 0x00FF00);
            guiGraphics.pose().popPose();
            currentY += lineHeight + 4;
        }

        // ═══════════════════════════════════════════════════════════
        // Topf-Typ mit Kapazitäts-Info
        // ═══════════════════════════════════════════════════════════
        PotType type = potBlock.getPotType();
        String potName = type.getColoredName();
        String capacityInfo = " §7(" + type.getMaxPlants() + " Pflanzen)";
        if (type.hasQualityBoost()) {
            capacityInfo += " §d+Qualität";
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
        guiGraphics.drawString(mc.font, potName + capacityInfo, (int)(HUD_X / SCALE), (int)(currentY / SCALE), 0xFFFFFF);
        guiGraphics.pose().popPose();
        currentY += lineHeight;

        if (!potData.hasSoil() && !potData.hasMist()) {
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
        drawResourceBar(guiGraphics, HUD_X, currentY, waterRatio, 0xFF2196F3);
        currentY += BAR_HEIGHT + 4;

        // Erd-Balken mit Pflanzen-Kapazität
        int currentSoil = potData.getSoilLevel();
        int maxSoil = potData.getMaxSoil();
        int plantsCapacity = currentSoil / PotType.SOIL_PER_PLANT;

        String soilLabel = potData.hasMist() ?
            "§dSubstrat: " + currentSoil + "/" + maxSoil + " §7(" + plantsCapacity + " Pflanzen)" :
            "§6Erde: " + currentSoil + "/" + maxSoil + " §7(" + plantsCapacity + " Pflanzen)";

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
        guiGraphics.drawString(mc.font, soilLabel, (int)(HUD_X / SCALE), (int)(currentY / SCALE), 0xFFFFFF);
        guiGraphics.pose().popPose();
        currentY += 10;

        float soilRatio = (float) currentSoil / maxSoil;
        int soilColor = potData.hasMist() ? 0xFFAA00FF : 0xFF8D6E63;
        drawResourceBar(guiGraphics, HUD_X, currentY, soilRatio, soilColor);
        currentY += BAR_HEIGHT + 4;

        // Lichtlevel anzeigen (nur wenn nicht Pilze)
        if (!potData.hasMushroomPlant()) {
            BlockPos potPos = potBE.getBlockPos();

            // Prüfe ob ein Growlight vorhanden ist (2-3 Blöcke über dem Topf)
            int lightLevel;
            String lightSource = "";
            boolean isGrowLight = false;
            de.rolandsw.schedulemc.tobacco.blocks.GrowLightSlabBlock foundGrowLight = null;

            for (int yOffset = 2; yOffset <= 3; yOffset++) {
                BlockPos growLightPos = potPos.above(yOffset);
                BlockState growLightState = mc.level.getBlockState(growLightPos);
                Block growLightBlock = growLightState.getBlock();

                if (growLightBlock instanceof de.rolandsw.schedulemc.tobacco.blocks.GrowLightSlabBlock growLight) {
                    foundGrowLight = growLight;
                    isGrowLight = true;
                    break;
                }
            }

            if (isGrowLight && foundGrowLight != null) {
                // Growlight gefunden! Zeige dessen konfigurierten Lichtlevel
                lightLevel = foundGrowLight.getTier().getLightLevel();
                lightSource = " §7(Growlight " + foundGrowLight.getTier().name() + ")";
            } else {
                // Kein Growlight → Zeige tatsächliches Lichtlevel mit Tag/Nacht-Variation
                BlockPos checkPos = potPos.above(2);

                // Hole SKY und BLOCK Licht separat
                int skyLight = mc.level.getBrightness(net.minecraft.world.level.LightLayer.SKY, checkPos);
                int blockLight = mc.level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, checkPos);

                // Berechne tatsächliches Himmelslicht unter Berücksichtigung der Tageszeit
                int skyDarken = mc.level.getSkyDarken();
                int adjustedSkyLight = Math.max(0, skyLight - skyDarken);

                // Kombiniertes Licht = Maximum aus Himmelslicht und Blocklicht
                lightLevel = Math.max(adjustedSkyLight, blockLight);

                // Prüfe ob es Sonnenlicht ist (SKY > BLOCK)
                if (skyLight > blockLight) {
                    lightSource = " §7(Sonnenlicht)";
                } else {
                    lightSource = " §7(Künstlich)";
                }
            }

            int minLight = de.rolandsw.schedulemc.config.ModConfigHandler.TOBACCO.MIN_LIGHT_LEVEL.get();
            boolean hasEnoughLight = lightLevel >= minLight;

            String lightLabel = "§eLicht: " + lightLevel + "/15" + lightSource;
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
        }

        // Pflanzen-Info (falls vorhanden und nicht erntebereit)
        if (potData.hasPlant() && !isHarvestReady) {
            // Trennlinie
            guiGraphics.fill(HUD_X, currentY, HUD_X + BAR_WIDTH, currentY + 1, 0x44FFFFFF);
            currentY += 4;

            // Pflanzen-Typ + Qualität
            String plantInfo = getPlantInfo(potData);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
            guiGraphics.drawString(mc.font, plantInfo, (int)(HUD_X / SCALE), (int)(currentY / SCALE), 0xFFFFFF);
            guiGraphics.pose().popPose();
            currentY += 10;

            // Wachstum
            int growthStage = getPlantGrowthStage(potData);
            int growthPercent = (growthStage * 100) / 7;

            String growthLabel = "§eWachstum: " + growthPercent + "%";
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
            guiGraphics.drawString(mc.font, growthLabel, (int)(HUD_X / SCALE), (int)(currentY / SCALE), 0xFFFFFF);
            guiGraphics.pose().popPose();
            currentY += 10;

            drawProgressBar(guiGraphics, HUD_X, currentY, growthPercent, false);
        }
    }

    /**
     * Prüft ob eine Pflanze erntebereit ist
     */
    private static boolean isPlantHarvestReady(PlantPotData potData) {
        if (potData.hasTobaccoPlant() && potData.getPlant().isFullyGrown()) return true;
        if (potData.hasCannabisPlant() && potData.getCannabisPlant().isFullyGrown()) return true;
        if (potData.hasCocaPlant() && potData.getCocaPlant().isFullyGrown()) return true;
        if (potData.hasPoppyPlant() && potData.getPoppyPlant().isFullyGrown()) return true;
        if (potData.hasMushroomPlant() && potData.getMushroomPlant().canHarvest()) return true;
        return false;
    }

    /**
     * Gibt Pflanzen-Info als String zurück
     */
    private static String getPlantInfo(PlantPotData potData) {
        if (potData.hasTobaccoPlant()) {
            var plant = potData.getPlant();
            return plant.getType().getColoredName() + " §7| " + plant.getQuality().getColoredName();
        }
        if (potData.hasCannabisPlant()) {
            var plant = potData.getCannabisPlant();
            return plant.getStrain().getColoredName() + " §7| " + plant.getQuality().getColoredName();
        }
        if (potData.hasCocaPlant()) {
            var plant = potData.getCocaPlant();
            return plant.getType().getColoredName() + " §7| " + plant.getQuality().getColoredName();
        }
        if (potData.hasPoppyPlant()) {
            var plant = potData.getPoppyPlant();
            return plant.getType().getColoredName() + " §7| " + plant.getQuality().getColoredName();
        }
        if (potData.hasMushroomPlant()) {
            var plant = potData.getMushroomPlant();
            String phase = plant.isIncubating() ? "§8Inkubation" : "§aFruchtung";
            return plant.getType().getColoredName() + " §7| " + phase;
        }
        return "§7Unbekannt";
    }

    /**
     * Gibt Wachstumsstufe zurück
     */
    private static int getPlantGrowthStage(PlantPotData potData) {
        if (potData.hasTobaccoPlant()) return potData.getPlant().getGrowthStage();
        if (potData.hasCannabisPlant()) return potData.getCannabisPlant().getGrowthStage();
        if (potData.hasCocaPlant()) return potData.getCocaPlant().getGrowthStage();
        if (potData.hasPoppyPlant()) return potData.getPoppyPlant().getGrowthStage();
        if (potData.hasMushroomPlant()) return potData.getMushroomPlant().getGrowthStage();
        return 0;
    }

    /**
     * Ressourcen-Balken mit 5 Segmenten
     */
    private static void drawResourceBar(GuiGraphics guiGraphics, int x, int y, float fillRatio, int color) {
        // Hintergrund
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF1A1A1A);

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
        guiGraphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y, 0xAAFFFFFF);
        guiGraphics.fill(x - 1, y + BAR_HEIGHT, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xAAFFFFFF);
        guiGraphics.fill(x - 1, y, x, y + BAR_HEIGHT, 0xAAFFFFFF);
        guiGraphics.fill(x + BAR_WIDTH, y, x + BAR_WIDTH + 1, y + BAR_HEIGHT, 0xAAFFFFFF);
    }

    /**
     * Fortschritts-Balken (Wachstum)
     */
    private static void drawProgressBar(GuiGraphics guiGraphics, int x, int y, int percent, boolean fullyGrown) {
        int color = fullyGrown ? 0xFF4CAF50 : 0xFFFDD835;

        // Hintergrund
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF1A1A1A);

        // Gefüllter Teil
        int filledWidth = (BAR_WIDTH * percent) / 100;
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
