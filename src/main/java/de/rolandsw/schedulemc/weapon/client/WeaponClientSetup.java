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
        event.registerSpriteSet(WeaponParticles.GRENADE_SMOKE.get(), GrenadeSmokeParticle.Provider::new);
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

    /**
     * Dichter, dunkelgrauer Rauchpartikel für die Rauchgranate.
     *
     * Render-Strategie: PARTICLE_SHEET_OPAQUE
     * ─────────────────────────────────────────
     * TRANSLUCENT erfordert jedes Frame einen O(n log n) Depth-Sort über ALLE
     * transparenten Partikel in der Szene — das kostet bei 10 Granaten ~330
     * Partikel × Sort = massiver FPS-Einbruch.
     *
     * OPAQUE benötigt keinen Sort, kein Alpha-Blending. Der Partikel-Shader
     * verwirft Pixel mit Alpha < 0.1 (discard), sodass die kreisförmige Textur
     * trotzdem runde Puffs ergibt. Vorne liegende Partikel verdecken hinten
     * liegende vollständig (Depth-Buffer-Writes aktiv) → man sieht NICHT durch
     * den Rauch. 10 Granaten = 500 opake Partikel = kaum messbar teurer als 50.
     *
     * Eigenschaften:
     *  - Kreisförmige Textur (grenade_smoke_puff.png) mit weichem Rand
     *  - Dunkelgrau (rCol/gCol/bCol ≈ 0.12–0.22)
     *  - 10–12 Sekunden Lebensdauer; ausblenden im letzten Viertel
     *  - Minimale Drift, kein Schwerkraft-Einfluss
     */
    static class GrenadeSmokeParticle extends TextureSheetParticle {

        GrenadeSmokeParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
            super(level, x, y, z);
            this.pickSprite(sprites);

            this.lifetime = 200 + random.nextInt(40);

            // 1.5–2.0 Blöcke Radius: kleiner als vorher (weniger Fill Rate),
            // aber bei ARM=2.0-Gitter noch ausreichend überlappend für Deckkraft.
            // Fill Rate ≈ 40 % weniger als bei quadSize 2.2–3.0.
            this.quadSize = 1.5f + random.nextFloat() * 0.5f;

            // Dunkelgrau — von rCol/gCol/bCol multipliziert mit Textur-Helligkeit
            float gray = 0.12f + random.nextFloat() * 0.10f;
            this.rCol = gray;
            this.gCol = gray;
            this.bCol = gray;

            // alpha wird mit Textur-Alpha multipliziert; Partikel-Shader
            // verwirft bei alpha*texAlpha < 0.1 → sauberes Ausblenden am Ende
            this.alpha = 1.0f;

            // Minimale Drift (wirkt wie echter Rauch)
            this.xd = (random.nextDouble() - 0.5) * 0.012;
            this.yd = 0.004 + random.nextDouble() * 0.008;
            this.zd = (random.nextDouble() - 0.5) * 0.012;

            this.gravity    = 0.0f;
            this.hasPhysics = false;
        }

        @Override
        public void tick() {
            // Letztes Viertel der Lebensdauer: ausblenden durch alpha-Reduktion.
            // Bei OPAQUE entspricht das einem harten Cutout (discard bei < 0.1),
            // aber da viele Partikel versetzt ablaufen, wirkt die Wolke trotzdem
            // weich am Ende.
            float lifeRatio = (float) this.age / (float) this.lifetime;
            if (lifeRatio > 0.75f) {
                float fade = (lifeRatio - 0.75f) / 0.25f;
                this.alpha = Math.max(0.0f, 1.0f - fade);
            }
            super.tick();
        }

        @Override
        public ParticleRenderType getRenderType() {
            // OPAQUE: kein Depth-Sort, kein Alpha-Blending → O(1) Sortieraufwand
            return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
        }

        public static class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;
            public Provider(SpriteSet sprites) { this.sprites = sprites; }

            @Override
            public net.minecraft.client.particle.Particle createParticle(
                    SimpleParticleType type, ClientLevel level,
                    double x, double y, double z,
                    double xd, double yd, double zd) {
                return new GrenadeSmokeParticle(level, x, y, z, sprites);
            }
        }
    }
}
