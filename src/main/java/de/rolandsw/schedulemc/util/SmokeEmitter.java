package de.rolandsw.schedulemc.util;

import de.rolandsw.schedulemc.weapon.particle.WeaponParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Wiederverwendbare Rauch-Utility für die gesamte Mod.
 *
 * Verwendung:
 *   // Rauchgranaten-Wolke — GESTAFFELT über mehrere Ticks (kein Spike):
 *   // In onHit(): grenade.beginSmokeEmission()  (setzt Zähler)
 *   // In tick():  SmokeEmitter.emitGrenadeSmokeBatch(server, x, y, z, batchIndex)
 *
 *   // Maschinen-Rauch (im tick() aufrufen, alle N Ticks):
 *   SmokeEmitter.emitMachineSmoke(level, x + 0.5, y + 1.0, z + 0.5);
 *
 *   // Schornstein-Rauch (im tick() aufrufen):
 *   SmokeEmitter.emitChimneySmoke(level, x + 0.5, y + 1.0, z + 0.5);
 */
public final class SmokeEmitter {

    private SmokeEmitter() {}

    // ─────────────────────────────────────────────────────────────────
    // Rauchgranaten-Gitter — gemeinsame Konstanten
    // ─────────────────────────────────────────────────────────────────

    /**
     * Anzahl der Ticks, über die eine Rauchgranate ihre Partikel verteilt.
     * ThrownWeaponGrenade ruft emitGrenadeSmokeBatch(batch=0..SMOKE_BATCHES-1)
     * einmal pro Tick auf — so entstehen nie mehr als ~12 Partikel pro Tick.
     */
    public static final int SMOKE_BATCHES = 6;

    // 3×3-Gitter (XZ-Offsets), arm = 2.0 Blöcke
    private static final double ARM = 2.0;
    private static final double[][] GRID = {
        { 0,    0   },  // Mitte
        { ARM,  0   },  // O
        {-ARM,  0   },  // W
        { 0,    ARM },  // S
        { 0,   -ARM },  // N
        { ARM,  ARM },  // SO
        {-ARM,  ARM },  // SW
        { ARM, -ARM },  // NO
        {-ARM, -ARM },  // NW
    };
    // 2 Höhenstufen: Bein- (0.7) und Kopfzone (2.0) → deckt Spielerhöhe ab
    private static final double[] HEIGHTS = { 0.7, 2.0 };

    // ─────────────────────────────────────────────────────────────────
    // Rauchgranate — gestaffelter Burst (ein Batch pro Tick)
    // ─────────────────────────────────────────────────────────────────

    /**
     * Emittiert einen Batch der Rauchgranaten-Partikel.
     *
     * Aufzurufen einmal pro Tick aus ThrownWeaponGrenade.tick(), mit
     * batch = 0, 1, … SMOKE_BATCHES-1. So verteilen sich die 72 Partikel
     * auf 6 Ticks (je ~12/Tick) statt alle auf einmal zu entstehen.
     *
     * Gitter: 9 Punkte × 2 Höhen = 18 Positionen, 3 pro Batch.
     * Partikel pro Batch: 3 Positionen × 4 = 12 Partikel.
     * Gesamt: 18 × 4 = 72 Partikel über 6 Ticks.
     *
     * @param batch 0 .. SMOKE_BATCHES-1
     */
    public static void emitGrenadeSmokeBatch(ServerLevel server, double x, double y, double z, int batch) {
        if (batch < 0 || batch >= SMOKE_BATCHES) return;

        // 18 Positionen gleichmäßig auf 6 Batches verteilen → 3 pro Batch
        int positionsPerBatch = (GRID.length * HEIGHTS.length) / SMOKE_BATCHES; // = 3
        int start = batch * positionsPerBatch;
        int end   = start + positionsPerBatch;

        int idx = 0;
        outer:
        for (double[] offset : GRID) {
            for (double h : HEIGHTS) {
                if (idx >= start && idx < end) {
                    server.sendParticles(WeaponParticles.GRENADE_SMOKE.get(),
                            x + offset[0], y + h, z + offset[1],
                            4, 0.15, 0.10, 0.15, 0.0);
                }
                if (++idx >= end) break outer;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Maschinen-Rauch — leichter Dauerbetrieb-Rauch
    // ─────────────────────────────────────────────────────────────────

    /**
     * Leichter Maschinen-Rauch für laufende Produktionsmaschinen.
     * Im tick() aufrufen — z.B. alle 10 Ticks.
     *
     * Beispiel:
     *   if (level.getGameTime() % 10 == 0)
     *       SmokeEmitter.emitMachineSmoke(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5);
     */
    public static void emitMachineSmoke(Level level, double x, double y, double z) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel server)) return;
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                x, y, z,
                3, 0.15, 0.1, 0.15, 0.005);
    }

    /**
     * Wie emitMachineSmoke, aber als ServerLevel-Variante wenn bereits gecastet.
     */
    public static void emitMachineSmoke(ServerLevel server, double x, double y, double z) {
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                x, y, z,
                3, 0.15, 0.1, 0.15, 0.005);
    }

    // ─────────────────────────────────────────────────────────────────
    // Schornstein-Rauch — aufsteigend, mit leichter Drift
    // ─────────────────────────────────────────────────────────────────

    /**
     * Aufsteigender Schornstein-Rauch. Im tick() aufrufen — z.B. alle 8 Ticks.
     * Particle steigen nach oben (positiver Y-Speed).
     *
     * Beispiel:
     *   if (level.getGameTime() % 8 == 0)
     *       SmokeEmitter.emitChimneySmoke(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5);
     */
    public static void emitChimneySmoke(Level level, double x, double y, double z) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel server)) return;
        // speedY positiv → Partikel steigen auf
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                x, y, z,
                2, 0.1, 0.05, 0.1, 0.02);
    }

    public static void emitChimneySmoke(ServerLevel server, double x, double y, double z) {
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                x, y, z,
                2, 0.1, 0.05, 0.1, 0.02);
    }

    // ─────────────────────────────────────────────────────────────────
    // BlockPos-Varianten (convenience overloads)
    // ─────────────────────────────────────────────────────────────────

    public static void emitMachineSmoke(Level level, BlockPos pos) {
        emitMachineSmoke(level, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5);
    }

    public static void emitMachineSmoke(ServerLevel server, BlockPos pos) {
        emitMachineSmoke(server, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5);
    }

    public static void emitChimneySmoke(Level level, BlockPos pos) {
        emitChimneySmoke(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
    }

    public static void emitChimneySmoke(ServerLevel server, BlockPos pos) {
        emitChimneySmoke(server, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
    }
}
