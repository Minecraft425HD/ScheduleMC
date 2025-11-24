package de.rolandsw.schedulemc.tobacco.blocks;

import de.rolandsw.schedulemc.config.ModConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Grow Light Slab - Lichtquelle für Indoor-Pflanzenanbau
 *
 * Features:
 * - 3 Tier-Stufen (Basic, Advanced, Premium)
 * - Verschiedene Lichtlevel
 * - Wachstumsgeschwindigkeits-Boni
 * - Premium: Qualitätsbonus
 * - Partikeleffekte
 */
public class GrowLightSlabBlock extends SlabBlock {

    private final GrowLightTier tier;

    public GrowLightSlabBlock(Properties properties, GrowLightTier tier) {
        super(properties);
        this.tier = tier;
    }

    public GrowLightTier getTier() {
        return tier;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return tier.getLightLevel();
    }

    /**
     * Partikel-Effekte (Client-Seite)
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        // Nur für Advanced und Premium Lampen
        if (tier == GrowLightTier.BASIC) return;

        // Partikel nur bei Bottom-Slab oder Double-Slab
        SlabType slabType = state.getValue(TYPE);
        if (slabType == SlabType.TOP) return;

        // Gelegentlich Partikel nach unten
        if (random.nextFloat() < tier.getParticleChance()) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + (slabType == SlabType.DOUBLE ? 0.5 : 0.0);
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;

            // Particle nach unten
            level.addParticle(
                tier.getParticleType(),
                x, y, z,
                0.0, -0.05, 0.0  // Velocity: langsam nach unten
            );
        }
    }

    /**
     * Tier-System für Grow Lights
     */
    public enum GrowLightTier {
        BASIC("LED Grow Slab",
              0xFFDD88,  // Warmweiß/Gelb
              net.minecraft.core.particles.ParticleTypes.END_ROD,
              0.0f),     // Keine Partikel

        ADVANCED("Dual-Spectrum Grow Slab",
                 0xAA44FF,  // Violett
                 net.minecraft.core.particles.ParticleTypes.PORTAL,
                 0.15f),    // 15% Chance

        PREMIUM("Quantum Grow Slab",
                0xFFFFFF,   // Weiß (mit dynamischen Farben)
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                0.25f);     // 25% Chance

        private final String name;
        private final int color;
        private final net.minecraft.core.particles.ParticleOptions particleType;
        private final float particleChance;

        GrowLightTier(String name, int color, net.minecraft.core.particles.ParticleOptions particleType, float particleChance) {
            this.name = name;
            this.color = color;
            this.particleType = particleType;
            this.particleChance = particleChance;
        }

        public String getName() {
            return name;
        }

        public int getColor() {
            return color;
        }

        public net.minecraft.core.particles.ParticleOptions getParticleType() {
            return particleType;
        }

        public float getParticleChance() {
            return particleChance;
        }

        /**
         * Holt Lichtlevel aus Config
         */
        public int getLightLevel() {
            return switch (this) {
                case BASIC -> ModConfigHandler.TOBACCO.BASIC_GROW_LIGHT_LEVEL.get();
                case ADVANCED -> ModConfigHandler.TOBACCO.ADVANCED_GROW_LIGHT_LEVEL.get();
                case PREMIUM -> ModConfigHandler.TOBACCO.PREMIUM_GROW_LIGHT_LEVEL.get();
            };
        }

        /**
         * Holt Wachstumsgeschwindigkeit aus Config
         */
        public double getGrowthSpeedMultiplier() {
            return switch (this) {
                case BASIC -> ModConfigHandler.TOBACCO.BASIC_GROW_LIGHT_SPEED.get();
                case ADVANCED -> ModConfigHandler.TOBACCO.ADVANCED_GROW_LIGHT_SPEED.get();
                case PREMIUM -> ModConfigHandler.TOBACCO.PREMIUM_GROW_LIGHT_SPEED.get();
            };
        }

        /**
         * Holt Qualitätsbonus aus Config (nur Premium)
         */
        public double getQualityBonus() {
            if (this == PREMIUM) {
                return ModConfigHandler.TOBACCO.PREMIUM_GROW_LIGHT_QUALITY_BONUS.get();
            }
            return 0.0;
        }
    }
}
