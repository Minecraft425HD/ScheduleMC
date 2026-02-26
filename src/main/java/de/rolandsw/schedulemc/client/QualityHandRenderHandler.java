package de.rolandsw.schedulemc.client;

import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Steuert den isRenderingHeld-Flag in QualityItemColors über den FORGE-Bus.
 *
 * Ablauf pro Frame:
 * 1. RenderHandEvent feuert (vor First-Person-Hand-Rendering) → Flag = true
 * 2. Hand wird gerendert (Qualitätsrahmen wird durch QualityFrameHidingModel unterdrückt)
 * 3. RenderGuiOverlayEvent.Pre feuert (vor erstem HUD-Overlay) → Flag = false
 * 4. Hotbar wird gerendert → Qualitätsrahmen wieder sichtbar
 *
 * Im Third-Person-Modus feuert RenderHandEvent nicht → Flag bleibt false → Rahmen immer sichtbar.
 */
@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class QualityHandRenderHandler {

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        QualityItemColors.isRenderingHeld = true;
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        QualityItemColors.isRenderingHeld = false;
    }
}
