package de.rolandsw.schedulemc.mapview.integration.minecraft;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// @Mixin(LevelRenderer.class)
public abstract class MixinWorldRenderer {

    // @Inject(method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V", at = @At("RETURN"), require = 0)
    private void renderLevel(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        // Waypoint rendering removed
    }

    // @Inject(method = "setSectionDirty(IIIZ)V", at = @At("RETURN"), require = 0)
    public void postScheduleChunkRender(int x, int y, int z, boolean important, CallbackInfo ci) {
        if (MapViewConstants.getLightMapInstance().getWorldUpdateListener() != null) {
            MapViewConstants.getLightMapInstance().getWorldUpdateListener().notifyObservers(x, z);
        }
    }
}
