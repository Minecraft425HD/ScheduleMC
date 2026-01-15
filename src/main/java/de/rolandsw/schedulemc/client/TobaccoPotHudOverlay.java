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

    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 6;
    private static final int SEGMENT_WIDTH = BAR_WIDTH / 5;
    private static final float SCALE = 0.7f;
    private static final int HUD_X = 10;
    private static final int HUD_Y = 10;
    private static final int BOX_WIDTH = 140;
    private static final int LINE_HEIGHT = 10;
    private static final int PADDING = 4;

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
     * Gaming HUD am oberen Bildschirmrand mit Box-Design
     */
    private static void renderUnifiedHud(GuiGraphics guiGraphics, Minecraft mc, PlantPotBlock potBlock,
                                         PlantPotBlockEntity potBE, boolean lookingAtPlant) {
        PlantPotData potData = potBE.getPotData();
        if (potData == null) return;

        int x = HUD_X;
        int y = HUD_Y;
        int currentY = y + PADDING;

        // Prüfe ob Pflanze erntebereit ist
        boolean isHarvestReady = isPlantHarvestReady(potData);

        // Prüfe ob Growlight vorhanden ist (für Höhenberechnung)
        boolean hasGrowLight = false;
        if (!potData.hasMushroomPlant()) {
            BlockPos potPos = potBE.getBlockPos();
            for (int yOffset = 2; yOffset <= 3; yOffset++) {
                BlockPos growLightPos = potPos.above(yOffset);
                BlockState growLightState = mc.level.getBlockState(growLightPos);
                Block growLightBlock = growLightState.getBlock();
                if (growLightBlock instanceof de.rolandsw.schedulemc.tobacco.blocks.GrowLightSlabBlock) {
                    hasGrowLight = true;
                    break;
                }
            }
        }

        // Berechne Box-Höhe dynamisch (exakt wie currentY-Inkremente)
        int boxHeight = PADDING; // Oberer Rand
        boxHeight += LINE_HEIGHT; // Topf-Typ
        boxHeight += 4; // Separator (entspricht currentY += 4 im Code)

        if (isHarvestReady) {
            boxHeight += LINE_HEIGHT + 2; // Erntebereit-Banner
            boxHeight += 2; // Extra separator
        }

        if (potData.hasSoil() || potData.hasMist()) {
            boxHeight += LINE_HEIGHT; // Wasser-Label
            boxHeight += BAR_HEIGHT + 2; // Wasser-Balken
            boxHeight += LINE_HEIGHT; // Erde-Label
            boxHeight += BAR_HEIGHT + 2; // Erde-Balken
            boxHeight += LINE_HEIGHT + 2; // Pflanzen-Kapazität
            if (!potData.hasMushroomPlant()) {
                boxHeight += LINE_HEIGHT; // Licht-Label
                if (hasGrowLight) {
                    boxHeight += LINE_HEIGHT; // Growlight-Zeile
                }
            }
        } else {
            boxHeight += LINE_HEIGHT; // Warnung
        }

        if (potData.hasPlant() && !isHarvestReady) {
            boxHeight += 4; // Separator
            boxHeight += LINE_HEIGHT; // Pflanze-Info
            boxHeight += LINE_HEIGHT; // Qualität
            boxHeight += LINE_HEIGHT; // Wachstum-Label
            boxHeight += BAR_HEIGHT + 2; // Wachstum-Balken
            boxHeight += LINE_HEIGHT; // Zeit
        }

        boxHeight += PADDING; // Unterer Rand

        // Hintergrund
        guiGraphics.fill(x, y, x + BOX_WIDTH, y + boxHeight, 0xDD000000);

        // Box-Rahmen
        PotType type = potBlock.getPotType();
        int borderColor = getPotBorderColor(type);
        drawBox(guiGraphics, x, y, BOX_WIDTH, boxHeight, borderColor);

        // ═══════════════════════════════════════════════════════════
        // ERNTEBEREIT-Banner
        // ═══════════════════════════════════════════════════════════
        if (isHarvestReady) {
            boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
            int bannerBg = blink ? 0xDD00FF00 : 0xDD00AA00;
            guiGraphics.fill(x + 1, currentY - 1, x + BOX_WIDTH - 1, currentY + LINE_HEIGHT + 1, bannerBg);

            drawScaledText(guiGraphics, mc, "§l✅ ERNTEBEREIT!", x + PADDING, currentY, 0x000000);
            currentY += LINE_HEIGHT + 2;
            drawHorizontalLine(guiGraphics, x + PADDING, currentY, BOX_WIDTH - PADDING * 2, 0x88FFFFFF);
            currentY += 2;
        }

        // ═══════════════════════════════════════════════════════════
        // Header: Topf-Typ
        // ═══════════════════════════════════════════════════════════
        String potName = "🏺 " + type.name().toUpperCase() + "-TOPF";
        String qualityBadge = type.hasQualityBoost() ? "     §d🌟" : "";
        drawScaledText(guiGraphics, mc, potName + qualityBadge, x + PADDING, currentY, 0xFFFFFF);
        currentY += LINE_HEIGHT;

        drawHorizontalLine(guiGraphics, x + PADDING, currentY, BOX_WIDTH - PADDING * 2, borderColor);
        currentY += 4;

        if (!potData.hasSoil() && !potData.hasMist()) {
            drawScaledText(guiGraphics, mc, "§c⚠ Keine Erde!", x + PADDING, currentY, 0xFF0000);
            return;
        }

        // ═══════════════════════════════════════════════════════════
        // Ressourcen-Sektion
        // ═══════════════════════════════════════════════════════════

        // Wasser
        int waterLevel = potData.getWaterLevel();
        int maxWater = potData.getMaxWater();
        drawScaledText(guiGraphics, mc, "💧 WASSER    §b" + waterLevel, x + PADDING, currentY, 0xFFFFFF);
        currentY += LINE_HEIGHT;

        float waterRatio = (float) waterLevel / maxWater;
        int waterColor = getResourceColor(waterRatio, 0xFF2196F3);
        drawCompactBar(guiGraphics, x + PADDING, currentY, waterRatio, waterColor);
        currentY += BAR_HEIGHT + 2;

        // Erde/Substrat
        int currentSoil = potData.getSoilLevel();
        int maxSoil = potData.getMaxSoil();
        int plantsCapacity = currentSoil / PotType.SOIL_PER_PLANT;

        String soilLabel = potData.hasMist() ? "🍄 SUBSTRAT §d" + currentSoil : "🌱 ERDE     §6" + currentSoil;
        drawScaledText(guiGraphics, mc, soilLabel, x + PADDING, currentY, 0xFFFFFF);
        currentY += LINE_HEIGHT;

        float soilRatio = (float) currentSoil / maxSoil;
        int soilColor = potData.hasMist() ? 0xFFAA00FF : 0xFF8D6E63;
        soilColor = getResourceColor(soilRatio, soilColor);
        drawCompactBar(guiGraphics, x + PADDING, currentY, soilRatio, soilColor);
        currentY += BAR_HEIGHT + 2;

        // Pflanzen-Kapazität
        String capacityText = "   └─ §7" + plantsCapacity + " Pflanzen";
        drawScaledText(guiGraphics, mc, capacityText, x + PADDING, currentY, 0xFFFFFF);
        currentY += LINE_HEIGHT + 2;

        // Licht (nur wenn nicht Pilze)
        if (!potData.hasMushroomPlant()) {
            BlockPos potPos = potBE.getBlockPos();
            int lightLevel = 0;
            String lightSource = "";
            boolean isGrowLight = false;

            // Prüfe Growlight (2-3 Blöcke über Topf)
            for (int yOffset = 2; yOffset <= 3; yOffset++) {
                BlockPos growLightPos = potPos.above(yOffset);
                BlockState growLightState = mc.level.getBlockState(growLightPos);
                Block growLightBlock = growLightState.getBlock();

                if (growLightBlock instanceof de.rolandsw.schedulemc.tobacco.blocks.GrowLightSlabBlock growLight) {
                    lightLevel = growLight.getTier().getLightLevel();
                    lightSource = growLight.getTier().name();
                    isGrowLight = true;
                    break;
                }
            }

            if (!isGrowLight) {
                // Natürliches Licht berechnen
                BlockPos checkPos = potPos.above(2);
                int skyLight = mc.level.getBrightness(net.minecraft.world.level.LightLayer.SKY, checkPos);
                int blockLight = mc.level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, checkPos);

                int skyDarken = 0;
                if (mc.level.dimensionType().hasSkyLight()) {
                    float celestialAngle = mc.level.getTimeOfDay(1.0F);
                    float brightness = (float) Math.cos(celestialAngle * 2.0F * Math.PI) * 2.0F + 0.5F;
                    brightness = net.minecraft.util.Mth.clamp(brightness, 0.0F, 1.0F);
                    skyDarken = Math.round((1.0F - brightness) * 11.0F);
                }

                int adjustedSkyLight = Math.max(0, skyLight - skyDarken);
                lightLevel = Math.max(adjustedSkyLight, blockLight);
            }

            int minLight = de.rolandsw.schedulemc.config.ModConfigHandler.TOBACCO.MIN_LIGHT_LEVEL.get();
            boolean hasEnoughLight = lightLevel >= minLight;
            boolean lightOptional = !de.rolandsw.schedulemc.config.ModConfigHandler.TOBACCO.REQUIRE_LIGHT_FOR_GROWTH.get();

            String lightIcon = isGrowLight ? "⚡" : "☀️";
            String lightText = lightIcon + " LICHT     §e" + lightLevel + "/15";
            String status;

            if (lightOptional) {
                status = "";
            } else if (lightLevel >= 14) {
                status = "  §a✓";
            } else if (hasEnoughLight) {
                status = "  §7✓";
            } else {
                status = "  §c✗";
            }

            drawScaledText(guiGraphics, mc, lightText + status, x + PADDING, currentY, 0xFFFFFF);
            currentY += LINE_HEIGHT;

            if (isGrowLight) {
                drawScaledText(guiGraphics, mc, "   └─ §7Growlight", x + PADDING, currentY, 0xFFFFFF);
                currentY += LINE_HEIGHT;
            }
        }

        // ═══════════════════════════════════════════════════════════
        // Pflanzen-Sektion
        // ═══════════════════════════════════════════════════════════
        if (potData.hasPlant() && !isHarvestReady) {
            drawHorizontalLine(guiGraphics, x + PADDING, currentY, BOX_WIDTH - PADDING * 2, 0x88FFFFFF);
            currentY += 4;

            String plantInfo = getPlantInfoCompact(potData);
            drawScaledText(guiGraphics, mc, "🌿 " + plantInfo, x + PADDING, currentY, 0xFFFFFF);
            currentY += LINE_HEIGHT;

            String qualityInfo = getQualityInfo(potData);
            drawScaledText(guiGraphics, mc, "⭐ Qualität: " + qualityInfo, x + PADDING, currentY, 0xFFFFFF);
            currentY += LINE_HEIGHT;

            int growthStage = getPlantGrowthStage(potData);
            int growthPercent = (growthStage * 100) / 7;

            drawScaledText(guiGraphics, mc, "📊 Wachstum  §e" + growthPercent + "%", x + PADDING, currentY, 0xFFFFFF);
            currentY += LINE_HEIGHT;

            drawCompactBar(guiGraphics, x + PADDING, currentY, growthPercent / 100.0f, 0xFFFDD835);
            currentY += BAR_HEIGHT + 2;

            // Zeit-Schätzung
            int remainingMinutes = estimateTimeToHarvest(growthStage);
            if (remainingMinutes > 0) {
                String timeText = "   └─ §7~" + remainingMinutes + "min ⏱️";
                drawScaledText(guiGraphics, mc, timeText, x + PADDING, currentY, 0xFFFFFF);
            }
        }
    }

    /**
     * Prüft ob eine Pflanze erntebereit ist
     * Prüft zusätzlich ob der Pflanzen-Block tatsächlich noch existiert (Client-Server-Sync)
     */
    private static boolean isPlantHarvestReady(PlantPotData potData) {
        // Wichtig: Keine Pflanze im PotData = nicht erntebereit (auch wenn Daten verzögert sind)
        if (!potData.hasPlant() && !potData.hasMushroomPlant()) return false;

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

    // ═══════════════════════════════════════════════════════════
    // Gaming HUD Helper-Methoden
    // ═══════════════════════════════════════════════════════════

    /**
     * Zeichnet Box-Rahmen
     */
    private static void drawBox(GuiGraphics gui, int x, int y, int width, int height, int color) {
        // Top & Bottom
        gui.fill(x, y, x + width, y + 1, color);
        gui.fill(x, y + height - 1, x + width, y + height, color);
        // Left & Right
        gui.fill(x, y, x + 1, y + height, color);
        gui.fill(x + width - 1, y, x + width, y + height, color);
    }

    /**
     * Zeichnet horizontale Linie
     */
    private static void drawHorizontalLine(GuiGraphics gui, int x, int y, int width, int color) {
        gui.fill(x, y, x + width, y + 1, color);
    }

    /**
     * Zeichnet skalierten Text
     */
    private static void drawScaledText(GuiGraphics gui, Minecraft mc, String text, int x, int y, int color) {
        gui.pose().pushPose();
        gui.pose().scale(SCALE, SCALE, 1.0f);
        gui.drawString(mc.font, text, (int)(x / SCALE), (int)(y / SCALE), color);
        gui.pose().popPose();
    }

    /**
     * Kompakter Balken ohne Segmente
     */
    private static void drawCompactBar(GuiGraphics gui, int x, int y, float fillRatio, int color) {
        // Hintergrund
        gui.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF1A1A1A);

        // Füllung
        int filledWidth = (int) (BAR_WIDTH * fillRatio);
        if (filledWidth > 0) {
            gui.fill(x, y, x + filledWidth, y + BAR_HEIGHT, color);
        }

        // Rahmen
        gui.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y, 0x88FFFFFF);
        gui.fill(x - 1, y + BAR_HEIGHT, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0x88FFFFFF);
        gui.fill(x - 1, y, x, y + BAR_HEIGHT, 0x88FFFFFF);
        gui.fill(x + BAR_WIDTH, y, x + BAR_WIDTH + 1, y + BAR_HEIGHT, 0x88FFFFFF);
    }

    /**
     * Gibt Border-Farbe basierend auf Topf-Typ zurück
     */
    private static int getPotBorderColor(PotType type) {
        return switch (type) {
            case TERRACOTTA -> 0xFFD84315; // Orange
            case CERAMIC -> 0xFFECEFF1;    // Hellgrau
            case IRON -> 0xFF78909C;       // Grau
            case GOLDEN -> 0xFFFFD700;     // Gold
        };
    }

    /**
     * Dynamische Ressourcen-Farbe basierend auf Füllstand
     */
    private static int getResourceColor(float fillRatio, int baseColor) {
        if (fillRatio >= 0.5f) {
            return baseColor; // Normal
        } else if (fillRatio >= 0.2f) {
            return 0xFFFFA726; // Orange - Warnung
        } else {
            // Kritisch - Blinken
            boolean blink = (System.currentTimeMillis() / 300) % 2 == 0;
            return blink ? 0xFFE53935 : 0xFF8B0000; // Rot-Blinken
        }
    }

    /**
     * Kompakte Pflanzen-Info (nur Name)
     */
    private static String getPlantInfoCompact(PlantPotData potData) {
        if (potData.hasTobaccoPlant()) {
            return potData.getPlant().getType().getColoredName();
        }
        if (potData.hasCannabisPlant()) {
            return potData.getCannabisPlant().getStrain().getColoredName();
        }
        if (potData.hasCocaPlant()) {
            return potData.getCocaPlant().getType().getColoredName();
        }
        if (potData.hasPoppyPlant()) {
            return potData.getPoppyPlant().getType().getColoredName();
        }
        if (potData.hasMushroomPlant()) {
            var plant = potData.getMushroomPlant();
            String phase = plant.isIncubating() ? "§8Inkubation" : "§aFruchtung";
            return potData.getMushroomPlant().getType().getColoredName() + " §7| " + phase;
        }
        return "§7Unbekannt";
    }

    /**
     * Qualitäts-Info
     */
    private static String getQualityInfo(PlantPotData potData) {
        if (potData.hasTobaccoPlant()) {
            return potData.getPlant().getQuality().getColoredName();
        }
        if (potData.hasCannabisPlant()) {
            return potData.getCannabisPlant().getQuality().getColoredName();
        }
        if (potData.hasCocaPlant()) {
            return potData.getCocaPlant().getQuality().getColoredName();
        }
        if (potData.hasPoppyPlant()) {
            return potData.getPoppyPlant().getQuality().getColoredName();
        }
        return "§7-";
    }

    /**
     * Schätzt verbleibende Zeit bis zur Ernte (in Minuten)
     */
    private static int estimateTimeToHarvest(int currentStage) {
        if (currentStage >= 7) return 0;

        int remainingStages = 7 - currentStage;
        // Durchschnittlich ~2 Minuten pro Stage (kann variieren je nach Licht)
        return remainingStages * 2;
    }
}
