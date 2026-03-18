package de.rolandsw.schedulemc.weapon.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.rolandsw.schedulemc.weapon.entity.WeaponBulletEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WeaponBulletRenderer extends EntityRenderer<WeaponBulletEntity> {

    public WeaponBulletRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(WeaponBulletEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Vec3 delta = entity.getDeltaMovement();
        double speed = delta.length();
        if (speed < 0.001) return;

        float beamLen = (float) Math.min(speed * 4.0, 0.9);
        float halfWidth = 0.022f;

        Vec3 dir = delta.normalize();
        Vec3 arbitrary = Math.abs(dir.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 right = dir.cross(arbitrary).normalize().scale(halfWidth);
        Vec3 up = dir.cross(right).normalize().scale(halfWidth);
        Vec3 back = dir.scale(-beamLen);

        poseStack.pushPose();
        VertexConsumer buf = bufferSource.getBuffer(RenderType.lightning());

        int r = 255, g = 235, b = 160, a = 210;

        vertex(buf, poseStack, back.add(right),             r, g, b, a);
        vertex(buf, poseStack, Vec3.ZERO.add(right),        r, g, b, a);
        vertex(buf, poseStack, Vec3.ZERO.add(right.scale(-1)), r, g, b, a);
        vertex(buf, poseStack, back.add(right.scale(-1)),   r, g, b, a);

        vertex(buf, poseStack, back.add(up),             r, g, b, a);
        vertex(buf, poseStack, Vec3.ZERO.add(up),        r, g, b, a);
        vertex(buf, poseStack, Vec3.ZERO.add(up.scale(-1)), r, g, b, a);
        vertex(buf, poseStack, back.add(up.scale(-1)),   r, g, b, a);

        poseStack.popPose();
    }

    private void vertex(VertexConsumer buf, PoseStack ps, Vec3 pos, int r, int g, int b, int a) {
        buf.vertex(ps.last().pose(), (float) pos.x, (float) pos.y, (float) pos.z)
           .color(r, g, b, a)
           .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(WeaponBulletEntity entity) {
        return new ResourceLocation("textures/misc/unknown_server.png");
    }
}
