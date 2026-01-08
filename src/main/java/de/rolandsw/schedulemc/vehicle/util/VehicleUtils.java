package de.rolandsw.schedulemc.vehicle.util;

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

/**
 * Utility class for vehicle-related operations including lookup and rendering.
 * <p>
 * Provides methods for:
 * </p>
 * <ul>
 *   <li>Finding vehicles by UUID within player range</li>
 *   <li>Rendering vehicles on GUI screens (e.g., for menus, previews)</li>
 *   <li>Animated vehicle rendering with rotation effects</li>
 * </ul>
 * <p>
 * This class also contains inner utility classes for managing rotating vehicle
 * previews in GUI contexts, with support for both client-tick-based and
 * simulated-tick-based animation systems.
 * </p>
 *
 * @see VehicleRenderer
 * @see SimulatedVehicleRenderer
 */
public class VehicleUtils {

    /**
     * Finds a vehicle by its UUID within a 10-block radius of the player.
     * <p>
     * Searches for vehicles of type {@link EntityGenericVehicle} in a 10-block cube
     * centered on the player and returns the first one matching the specified UUID.
     * This is typically used for player interactions with nearby vehicles.
     * </p>
     * <p>
     * <strong>Search Range:</strong> 10 blocks in all directions (20x20x20 cube)
     * </p>
     *
     * @param player the player at the center of the search area
     * @param uuid the UUID of the vehicle to find
     * @return the matching vehicle if found within range, or null if not found
     */
    @Nullable
    public static EntityGenericVehicle getVehicleByUUID(Player player, UUID uuid) {
        double distance = 10D;
        return player.level().getEntitiesOfClass(EntityGenericVehicle.class, new AABB(player.getX() - distance, player.getY() - distance, player.getZ() - distance, player.getX() + distance, player.getY() + distance, player.getZ() + distance), entity -> entity.getUUID().equals(uuid)).stream().findAny().orElse(null);
    }

    /**
     * Renders a 3D vehicle entity on a 2D GUI screen at a specified position.
     * <p>
     * This method draws a vehicle as a 3D model within GUI contexts such as menus,
     * inventory screens, or vehicle selection interfaces. The rendering uses the
     * standard entity renderer with custom transformations for proper display.
     * </p>
     * <p>
     * <strong>Rendering Details:</strong>
     * </p>
     * <ul>
     *   <li>Vehicle is rendered at a fixed viewing angle (135 degrees + rotation)</li>
     *   <li>Shadows are disabled for cleaner GUI appearance</li>
     *   <li>Lighting is set to full brightness (0xF000F0)</li>
     *   <li>Z-coordinate is set to 100 for proper depth sorting</li>
     * </ul>
     *
     * @param graphics the GUI graphics context to render into
     * @param vehicle the vehicle entity to render
     * @param posX the X screen coordinate for the center of the vehicle
     * @param posY the Y screen coordinate for the center of the vehicle
     * @param scale the scale factor for the vehicle model (1.0 = normal size)
     * @param rotation the Y-axis rotation angle in degrees (added to base 135 degrees)
     */
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

    /**
     * A client-tick-based vehicle renderer for animated GUI previews.
     * <p>
     * This renderer automatically rotates a vehicle preview in GUI contexts by
     * tracking rotation state across client ticks. It uses Minecraft's frame time
     * for smooth interpolation between ticks, ensuring smooth rotation even at
     * varying frame rates.
     * </p>
     * <p>
     * <strong>Usage Example:</strong>
     * </p>
     * <pre>
     * VehicleRenderer renderer = new VehicleRenderer();
     * // In your tick method:
     * renderer.tick();
     * // In your render method:
     * renderer.render(guiGraphics, vehicle, x, y, scale);
     * </pre>
     * <p>
     * <strong>Thread Safety:</strong> Not thread-safe. Should only be used on the
     * client rendering thread.
     * </p>
     *
     * @see SimulatedVehicleRenderer
     */
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
            VehicleUtils.drawVehicleOnScreen(guiGraphics, vehicle, posX, posY, scale, rotation + rotationPerTick * minecraft.getFrameTime());
        }
    }

    /**
     * A simulated-tick vehicle renderer for contexts without access to client ticks.
     * <p>
     * This renderer uses a {@link SimulatedTicker} to create its own tick timing
     * based on system time rather than relying on Minecraft's client tick events.
     * This is useful for GUI contexts where tick events may not be available or
     * where independent timing is desired.
     * </p>
     * <p>
     * The simulated ticker generates ticks at approximately 20 ticks per second
     * (50ms per tick) to match Minecraft's tick rate, with partial tick interpolation
     * for smooth rendering between ticks.
     * </p>
     * <p>
     * <strong>Usage Example:</strong>
     * </p>
     * <pre>
     * SimulatedVehicleRenderer renderer = new SimulatedVehicleRenderer();
     * // In your render method (no tick method needed):
     * renderer.render(guiGraphics, vehicle, x, y, scale);
     * </pre>
     * <p>
     * <strong>Thread Safety:</strong> Not thread-safe. Should only be used on the
     * client rendering thread.
     * </p>
     *
     * @see VehicleRenderer
     * @see SimulatedTicker
     */
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
                    VehicleUtils.drawVehicleOnScreen(guiGraphics, vehicle, posX, posY, scale, rotation + rotationPerTick * partialTicks);
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

    /**
     * A time-based tick simulator for smooth rendering independent of client ticks.
     * <p>
     * This class simulates Minecraft's tick system using system nanotime instead of
     * relying on actual client tick events. It maintains a 50ms (20 TPS) tick rate
     * and calculates partial ticks for interpolation, enabling smooth animation in
     * contexts where client ticks are not available.
     * </p>
     * <p>
     * <strong>Tick Timing:</strong> Approximately 20 ticks per second (50 milliseconds
     * per tick), matching Minecraft's standard tick rate.
     * </p>
     * <p>
     * <strong>Implementation Note:</strong> Uses {@link System#nanoTime()} for
     * high-precision timing that is independent of system clock changes.
     * </p>
     *
     * @see SimulatedVehicleRenderer
     * @see Renderer
     */
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

    /**
     * Callback interface for tick-based rendering with partial tick interpolation.
     * <p>
     * This interface is used by {@link SimulatedTicker} to coordinate discrete tick
     * updates with continuous rendering. Implementations should update their state
     * in {@link #tick()} and perform interpolated rendering in {@link #render(float)}.
     * </p>
     * <p>
     * <strong>Usage Pattern:</strong>
     * </p>
     * <ul>
     *   <li>{@link #tick()} is called at discrete intervals (approximately 20 TPS)</li>
     *   <li>{@link #render(float)} is called every frame with partial tick value</li>
     *   <li>Partial tick ranges from 0.0 to 1.0 between ticks</li>
     * </ul>
     *
     * @see SimulatedTicker
     */
    public interface Renderer {
        void render(float partialTicks);

        void tick();
    }

}
