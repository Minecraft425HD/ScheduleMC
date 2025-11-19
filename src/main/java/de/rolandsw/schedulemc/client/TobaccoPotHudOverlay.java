package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.tobacco.blocks.TobaccoPlantBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Kompaktes HUD-Overlay für Tabak-Pflanzen - zentral, klein, halbtransparent
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TobaccoPotHudOverlay {

    private static final int BAR_WIDTH = 80; // Viel kleiner
    private static final int BAR_HEIGHT = 6;  // Viel kleiner
    private static final float SCALE = 0.6f;  // Skalierung für Text

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Prüfe ob Spieler auf eine Pflanze schaut
        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockState state = mc.level.getBlockState(blockHitResult.getBlockPos());

        if (!(state.getBlock() instanceof TobaccoPlantBlock plant)) return;

        // Nur für untere Hälfte anzeigen
        if (state.getValue(TobaccoPlantBlock.HALF) != DoubleBlockHalf.LOWER) return;

        int age = state.getValue(TobaccoPlantBlock.AGE);
        int growthPercent = (age * 100) / 7;
        boolean isFullyGrown = age >= 7;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Zentriert in der Mitte des Bildschirms
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2 + 40; // Leicht unter der Mitte

        // Hintergrund - halbtransparent
        int bgWidth = BAR_WIDTH + 20;
        int bgHeight = 30;
        int bgX = centerX - bgWidth / 2;
        int bgY = centerY - bgHeight / 2;

        // Halbtransparenter schwarzer Hintergrund (40% Transparenz)
        guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, 0x66000000);

        // Pflanzenname - zentriert, klein
        String plantName = plant.getTobaccoType().getColoredName();
        int textWidth = (int) (mc.font.width(plantName) * SCALE);
        int textX = centerX - textWidth / 2;
        int textY = bgY + 4;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
        guiGraphics.drawString(mc.font, plantName,
            (int)(textX / SCALE), (int)(textY / SCALE), 0xFFFFFF);
        guiGraphics.pose().popPose();

        // Wachstums-Balken
        int barX = centerX - BAR_WIDTH / 2;
        int barY = bgY + 16;

        drawGrowthBar(guiGraphics, barX, barY, growthPercent, isFullyGrown);

        // Prozent-Text - zentriert, klein
        String percentText = growthPercent + "%";
        if (isFullyGrown) {
            percentText += " §a✓";
        }
        int percentWidth = (int) (mc.font.width(percentText) * SCALE);
        int percentX = centerX - percentWidth / 2;
        int percentY = bgY + 24;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(SCALE, SCALE, 1.0f);
        int color = isFullyGrown ? 0x00FF00 : 0xFFFFFF;
        guiGraphics.drawString(mc.font, percentText,
            (int)(percentX / SCALE), (int)(percentY / SCALE), color);
        guiGraphics.pose().popPose();
    }

    private static void drawGrowthBar(GuiGraphics guiGraphics, int x, int y, int percent, boolean fullyGrown) {
        // Hintergrund-Balken - dunkelgrau
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF2A2A2A);

        // Gefüllter Teil
        int filledWidth = (BAR_WIDTH * percent) / 100;
        int color = fullyGrown ? 0xFF4CAF50 : 0xFFFDD835; // Grün wenn reif, Gelb sonst

        if (filledWidth > 0) {
            guiGraphics.fill(x, y, x + filledWidth, y + BAR_HEIGHT, color);
        }

        // Dünner weißer Rahmen
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + 1, 0x88FFFFFF); // oben
        guiGraphics.fill(x, y + BAR_HEIGHT - 1, x + BAR_WIDTH, y + BAR_HEIGHT, 0x88FFFFFF); // unten
        guiGraphics.fill(x, y, x + 1, y + BAR_HEIGHT, 0x88FFFFFF); // links
        guiGraphics.fill(x + BAR_WIDTH - 1, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0x88FFFFFF); // rechts
    }
}
