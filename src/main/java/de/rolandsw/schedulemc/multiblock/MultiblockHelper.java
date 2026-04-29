package de.rolandsw.schedulemc.multiblock;

import de.rolandsw.schedulemc.utility.PlotUtilityManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

/**
 * Universelle Hilfsmethoden für das ScheduleMC-Multiblock-System.
 *
 * Ein "Booster" ist ein Block, der {@link IMultiblockBooster} implementiert
 * und eine FACING-Eigenschaft (HorizontalDirectionalBlock) besitzt.
 * Er zeigt mit seiner Vorderseite (FACING) in Richtung des Ziels.
 *
 * Scan-Logik:
 *  - Scannt von {@code masterPos} in alle 4 horizontalen Richtungen
 *  - Maximale Reichweite: {@code range} Blöcke
 *  - Linie-des-Sichtbereichs: alle Blöcke zwischen Booster und Ziel müssen Luft sein
 *  - Jeder gefundene Booster zählt nur einmal (verhindert Doppelzählung)
 *  - Ergebnis wird auf {@code MAX_MULTIPLIER} begrenzt
 *
 * <h3>Neuen Booster-Typ hinzufügen</h3>
 * Implementiere einfach {@link IMultiblockBooster} auf dem Block — kein Änderungsbedarf hier.
 *
 * <h3>Neuen Empfänger-Typ hinzufügen</h3>
 * Rufe {@code MultiblockHelper.scanForBoost(level, worldPosition, 4)} im {@code tick()} auf
 * und multipliziere die verarbeiteten Ticks mit dem zurückgegebenen Wert.
 */
public final class MultiblockHelper {

    /** Maximaler Gesamtmultiplikator (kein einzelner Ventilator kann diesen Wert überschreiten) */
    public static final float MAX_MULTIPLIER = 8.0f;

    private MultiblockHelper() {}

    /**
     * Scannt um {@code masterPos} nach Booster-Blöcken die in Richtung des Masters zeigen.
     *
     * @param level     Level (wird ignoriert wenn clientSide)
     * @param masterPos Position des BlockEntity-Masters (Scan-Mittelpunkt)
     * @param range     Maximale Suchreichweite in Blöcken (empfohlen: 3-5)
     * @return Gesamtmultiplikator ≥ 1.0, begrenzt auf {@link #MAX_MULTIPLIER}
     */
    public static float scanForBoost(Level level, BlockPos masterPos, int range) {
        if (level == null || level.isClientSide) return 1.0f;

        float totalBonus = 0f;
        Set<BlockPos> counted = new HashSet<>();

        for (Direction dir : new Direction[]{
                Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {

            for (int dist = 1; dist <= range; dist++) {
                BlockPos checkPos = masterPos.relative(dir, dist);

                // Bereits gezählten Booster überspringen
                if (counted.contains(checkPos)) break;

                BlockState checkState = level.getBlockState(checkPos);

                // Booster-Block gefunden?
                if (checkState.getBlock() instanceof IMultiblockBooster booster) {
                    // Linie-des-Sichtbereichs prüfen (alle Blöcke dazwischen müssen Luft sein)
                    if (dist > 1 && !hasLineOfSight(level, masterPos, dir, dist)) break;

                    // FACING muss auf den Master zeigen (entgegengesetzte Richtung des Scans)
                    if (checkState.hasProperty(HorizontalDirectionalBlock.FACING)) {
                        Direction fanFacing = checkState.getValue(HorizontalDirectionalBlock.FACING);
                        if (fanFacing == dir.getOpposite()) {
                            // Ventilator nur zählen wenn Utilities für seine Plot-Position aktiv
                            if (PlotUtilityManager.areUtilitiesEnabled(checkPos)) {
                                totalBonus += (booster.getBoostMultiplier() - 1.0f);
                                counted.add(checkPos);
                            }
                        }
                    }
                    break; // Erster Block in dieser Richtung - egal ob gezählt, nicht weitersuchen
                }

                // Fester Nicht-Booster-Block blockiert die Sicht
                if (!checkState.isAir()) break;
            }
        }

        return Math.min(1.0f + totalBonus, MAX_MULTIPLIER);
    }

    /**
     * Prüft ob zwischen masterPos und dem Block auf Distanz {@code dist} in Richtung {@code dir}
     * ausschließlich Luft ist (Blöcke auf Distanz 1 bis dist-1).
     */
    private static boolean hasLineOfSight(Level level, BlockPos masterPos, Direction dir, int dist) {
        for (int i = 1; i < dist; i++) {
            if (!level.getBlockState(masterPos.relative(dir, i)).isAir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Variante mit erweitertem Footprint: scannt von mehreren Ausgangspositionen.
     * Nützlich für breite Multiblock-Strukturen (z.B. 3×2-Trocknungsgestell).
     *
     * @param footprint Alle Blockpositionen der Multiblock-Struktur
     * @param range     Suchreichweite
     * @return Gesamtmultiplikator (Booster werden nur einmal gezählt)
     */
    public static float scanForBoost(Level level, Iterable<BlockPos> footprint, int range) {
        if (level == null || level.isClientSide) return 1.0f;

        float totalBonus = 0f;
        Set<BlockPos> counted = new HashSet<>();

        for (BlockPos origin : footprint) {
            for (Direction dir : new Direction[]{
                    Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {

                for (int dist = 1; dist <= range; dist++) {
                    BlockPos checkPos = origin.relative(dir, dist);
                    if (counted.contains(checkPos)) break;

                    BlockState checkState = level.getBlockState(checkPos);

                    if (checkState.getBlock() instanceof IMultiblockBooster booster) {
                        if (dist > 1 && !hasLineOfSight(level, origin, dir, dist)) break;
                        if (checkState.hasProperty(HorizontalDirectionalBlock.FACING)) {
                            Direction fanFacing = checkState.getValue(HorizontalDirectionalBlock.FACING);
                            if (fanFacing == dir.getOpposite()) {
                                // Ventilator nur zählen wenn Utilities für seine Plot-Position aktiv
                                if (PlotUtilityManager.areUtilitiesEnabled(checkPos)) {
                                    totalBonus += (booster.getBoostMultiplier() - 1.0f);
                                    counted.add(checkPos);
                                }
                            }
                        }
                        break;
                    }
                    if (!checkState.isAir()) break;
                }
            }
        }

        return Math.min(1.0f + totalBonus, MAX_MULTIPLIER);
    }
}
