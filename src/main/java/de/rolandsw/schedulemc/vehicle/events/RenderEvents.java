package de.rolandsw.schedulemc.vehicle.events;
import de.rolandsw.schedulemc.util.EventHelper;
import de.rolandsw.schedulemc.config.ModConfigHandler;

import com.mojang.blaze3d.systems.RenderSystem;
import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityVehicleBase;
import de.maxhenkel.corelib.math.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class RenderEvents {

    protected static final ResourceLocation GUI_ICONS_LOCATION = ResourceLocation.parse("textures/gui/icons.png");
    private static final Minecraft mc = Minecraft.getInstance();

    // PERFORMANCE: Speed-String nur bei Änderung neu berechnen statt pro Frame
    private static float lastCachedSpeed = Float.NaN;
    private static String cachedSpeedStr = "";

    @SubscribeEvent
    public void onRender(ViewportEvent.ComputeCameraAngles evt) {
        EventHelper.handleEvent(() -> {
            // DISABLED: Camera.move() and getMaxZoom() are private/protected in 1.20.1
            // This feature provided custom camera zoom when riding vehicles, but is not critical
            /* DISABLED DUE TO API CHANGE IN 1.20.1
            if (getVehicle() != null && !mc.options.getCameraType().isFirstPerson()) {
                evt.getCamera().move(-evt.getCamera().getMaxZoom(ModConfigHandler.VEHICLE_CLIENT.vehicleZoom.get() - 4D), 0D, 0D);
            }
            */
        }, "onRender");
    }

    @SubscribeEvent
    public void onRender(InputEvent.MouseScrollingEvent evt) {
        EventHelper.handleEvent(() -> {
            if (getVehicle() != null && !mc.options.getCameraType().isFirstPerson()) {
                ModConfigHandler.VEHICLE_CLIENT.vehicleZoom.set(Math.max(1D, Math.min(20D, ModConfigHandler.VEHICLE_CLIENT.vehicleZoom.get() - evt.getScrollDelta())));
                ModConfigHandler.VEHICLE_CLIENT.vehicleZoom.save();
                evt.setCanceled(true);
            }
        }, "onRender");
    }

    private static EntityGenericVehicle getVehicle() {
        if (mc.player == null) {
            return null;
        }
        Entity e = mc.player.getVehicle();
        if (e instanceof EntityGenericVehicle) {
            return (EntityGenericVehicle) e;
        }
        return null;
    }

    public static boolean onRenderExperienceBar(GuiGraphics guiGraphics, int i) {
        Player player = mc.player;
        EntityGenericVehicle vehicle = getVehicle();

        if (vehicle == null || player == null) {
            return false;
        }

        if (!player.equals(vehicle.getDriver())) {
            return false;
        }
        renderFuelBar(guiGraphics, ((float) vehicle.getFuelAmount()) / ((float) vehicle.getMaxFuel()));
        renderSpeed(guiGraphics, vehicle.getKilometerPerHour());
        return true;
    }

    public static void renderFuelBar(GuiGraphics guiGraphics, float percent) {
        percent = Mth.clamp(percent, 0F, 1F);
        int x = mc.getWindow().getGuiScaledWidth() / 2 - 91;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        int k = mc.getWindow().getGuiScaledHeight() - 32 + 3;
        guiGraphics.blit(GUI_ICONS_LOCATION, x, k, 0, 64, 182, 5);

        int j = (int) (percent * 182F);

        if (j > 0) {
            guiGraphics.blit(GUI_ICONS_LOCATION, x, k, 0, 69, j, 5);
        }
    }

    public static void renderSpeed(GuiGraphics guiGraphics, float speed) {
        Font font = mc.gui.getFont();
        // PERFORMANCE: String nur bei Speed-Änderung neu erstellen
        float absSpeed = Math.abs(speed);
        if (absSpeed != lastCachedSpeed) {
            lastCachedSpeed = absSpeed;
            cachedSpeedStr = String.valueOf(MathUtils.round(absSpeed, 2));
        }
        String s = cachedSpeedStr;
        int i1 = (mc.getWindow().getGuiScaledWidth() - font.width(s)) / 2;
        int j1 = mc.getWindow().getGuiScaledHeight() - 31 - 4;
        guiGraphics.drawString(font, s, i1 + 1, j1, 0, false);
        guiGraphics.drawString(font, s, i1 - 1, j1, 0, false);
        guiGraphics.drawString(font, s, i1, j1 + 1, 0, false);
        guiGraphics.drawString(font, s, i1, j1 - 1, 0, false);
        guiGraphics.drawString(font, s, i1, j1, 8453920, false);
    }

    @SubscribeEvent
    public void renderToolTip(RenderTooltipEvent.Pre event) {
        EventHelper.handleEvent(() -> {
            ItemStack stack = event.getItemStack();

            if (!stack.hasTag()) {
                return;
            }
            CompoundTag compound = stack.getTag();
            if (!compound.contains("trading_item") && !compound.getBoolean("trading_item")) {
                return;
            }
            event.setCanceled(true);
        }, "renderToolTip");
    }

    @SubscribeEvent
    public void renderPlayerPre(RenderPlayerEvent.Pre event) {
        EventHelper.handleEvent(() -> {
            EntityGenericVehicle vehicle = getVehicle();
            if (vehicle != null) {
                event.getPoseStack().pushPose();
                event.getPoseStack().scale(EntityVehicleBase.SCALE_FACTOR, EntityVehicleBase.SCALE_FACTOR, EntityVehicleBase.SCALE_FACTOR);
                event.getPoseStack().translate(0D, (event.getEntity().getBbHeight() - (event.getEntity().getBbHeight() * EntityVehicleBase.SCALE_FACTOR)) / 1.5D + vehicle.getPlayerYOffset(), 0D);
            }
        }, "renderPlayerPre");
    }

    @SubscribeEvent
    public void renderPlayerPost(RenderPlayerEvent.Post event) {
        EventHelper.handleEvent(() -> {
            if (getVehicle() != null) {
                event.getPoseStack().popPose();
            }
        }, "renderPlayerPost");
    }

}
