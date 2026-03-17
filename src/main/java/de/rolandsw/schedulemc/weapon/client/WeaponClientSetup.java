package de.rolandsw.schedulemc.weapon.client;

import de.rolandsw.schedulemc.ScheduleMC;
import de.rolandsw.schedulemc.weapon.entity.WeaponEntities;
import de.rolandsw.schedulemc.weapon.particle.WeaponParticles;
import de.rolandsw.schedulemc.weapon.render.WeaponBulletRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ScheduleMC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WeaponClientSetup {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(WeaponEntities.THROWN_WEAPON_GRENADE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(WeaponEntities.WEAPON_BULLET.get(), WeaponBulletRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(WeaponParticles.MUZZLE_FLASH.get(), MuzzleFlashParticle.Provider::new);
        event.registerSpriteSet(WeaponParticles.BLOOD.get(), BloodParticle.Provider::new);
    }

    static class MuzzleFlashParticle extends TextureSheetParticle {
        MuzzleFlashParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
            super(level, x, y, z);
            this.pickSprite(sprites);
            this.lifetime = 3;
            this.scale(0.4f);
            this.rCol = 1.0f;
            this.gCol = 0.8f;
            this.bCol = 0.2f;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        public static class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;
            public Provider(SpriteSet sprites) { this.sprites = sprites; }

            @Override
            public net.minecraft.client.particle.Particle createParticle(
                    SimpleParticleType type, ClientLevel level,
                    double x, double y, double z,
                    double xd, double yd, double zd) {
                return new MuzzleFlashParticle(level, x, y, z, sprites);
            }
        }
    }

    static class BloodParticle extends TextureSheetParticle {
        BloodParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
            super(level, x, y, z);
            this.pickSprite(sprites);
            this.lifetime = 8;
            this.scale(0.2f);
            this.rCol = 0.8f;
            this.gCol = 0.0f;
            this.bCol = 0.0f;
            this.xd = (random.nextDouble() - 0.5) * 0.1;
            this.yd = random.nextDouble() * 0.1;
            this.zd = (random.nextDouble() - 0.5) * 0.1;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        public static class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;
            public Provider(SpriteSet sprites) { this.sprites = sprites; }

            @Override
            public net.minecraft.client.particle.Particle createParticle(
                    SimpleParticleType type, ClientLevel level,
                    double x, double y, double z,
                    double xd, double yd, double zd) {
                return new BloodParticle(level, x, y, z, sprites);
            }
        }
    }
}
