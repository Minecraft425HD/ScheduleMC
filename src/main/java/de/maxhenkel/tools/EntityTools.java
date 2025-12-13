package de.maxhenkel.tools;

import com.mojang.math.Axis;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityTools {

    /**
     * Gets the first vehicle in the range of 10 blocks of the player
     *
     * @param player the player
     * @param uuid   the UUID of the vehicle
     * @return the vehicle or null
     */
    @Nullable
    public static EntityGenericVehicle getVehicleByUUID(Player player, UUID uuid) {
        double distance = 10D;
        return player.level().getEntitiesOfClass(EntityGenericVehicle.class, new AABB(player.getX() - distance, player.getY() - distance, player.getZ() - distance, player.getX() + distance, player.getY() + distance, player.getZ() + distance), entity -> entity.getUUID().equals(uuid)).stream().findAny().orElse(null);
    }

    public static void drawVehicleOnScreen(GuiGraphics graphics, EntityGenericVehicle vehicle, int posX, int posY, float scale, float rotation) {
        graphics.pose().pushPose();
        graphics.pose().translate(posX, posY, 100D);
        graphics.pose().scale(1F, 1F, -1F);
        graphics.pose().scale(scale, scale, scale);

        graphics.pose().mulPose(Axis.YP.rotationDegrees(135F + rotation));
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(180F));
        EntityRenderDispatcher entityrenderermanager = Minecraft.getInstance().getEntityRenderDispatcher();
        entityrenderermanager.setRenderShadow(false);

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        entityrenderermanager.render(vehicle, 0D, 0D, 0D, 0F, 1F, graphics.pose(), buffer, 0xF000F0);
        buffer.endBatch();
        entityrenderermanager.setRenderShadow(true);
        graphics.pose().popPose();
    }

    public static class VehicleRenderer {
        private float rotation;
        private float rotationPerTick;
        private Minecraft minecraft;

        public VehicleRenderer(float rotationPerTick) {
            this.rotationPerTick = rotationPerTick;
            this.minecraft = Minecraft.getInstance();
        }

        public VehicleRenderer() {
            this(3.6F);
        }

        public void tick() {
            rotation += rotationPerTick;
            if (rotation >= 360F) {
                rotation = 0F;
            }
        }

        public void render(GuiGraphics guiGraphics, EntityGenericVehicle vehicle, int posX, int posY, int scale) {
            EntityTools.drawVehicleOnScreen(guiGraphics, vehicle, posX, posY, scale, rotation + rotationPerTick * minecraft.getFrameTime());
        }
    }

    public static class SimulatedVehicleRenderer {
        private float rotation;
        private float rotationPerTick;
        private SimulatedTicker ticker;

        public SimulatedVehicleRenderer(float rotationPerTick) {
            this.rotationPerTick = rotationPerTick;
            ticker = new SimulatedTicker();
        }

        public SimulatedVehicleRenderer() {
            this(3.6F);
        }

        public void render(GuiGraphics guiGraphics, EntityGenericVehicle vehicle, int posX, int posY, int scale) {
            ticker.render(new Renderer() {
                @Override
                public void render(float partialTicks) {
                    EntityTools.drawVehicleOnScreen(guiGraphics, vehicle, posX, posY, scale, rotation + rotationPerTick * partialTicks);
                }

                @Override
                public void tick() {
                    rotation += rotationPerTick;
                    if (rotation >= 360F) {
                        rotation = 0F;
                    }
                }
            });
        }
    }

    public static class SimulatedTicker {
        private static final long ONE_TICK = 50_000_000L;
        private long lastTick;

        public void render(Renderer renderer) {
            long currentNanos = System.nanoTime();

            if (currentNanos - lastTick >= ONE_TICK) {
                renderer.tick();
                lastTick = currentNanos;
            }
            renderer.render((float) (currentNanos - lastTick) / (float) ONE_TICK);
        }
    }

    public interface Renderer {
        void render(float partialTicks);

        void tick();
    }

}
