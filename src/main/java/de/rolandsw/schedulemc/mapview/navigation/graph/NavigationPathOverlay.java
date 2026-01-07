package de.rolandsw.schedulemc.mapview.navigation.graph;

import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NavigationPathOverlay - Speichert Pfaddaten für die Integration in die Kartendarstellung
 *
 * Der Pfad wird direkt in die Kartenfarben gemischt, nicht als separates Overlay gerendert.
 * Dadurch bewegt sich der Pfad perfekt mit der Karte (Zoom, Scroll, Rotation).
 */
public class NavigationPathOverlay {
    // Color Constants
    private static final int COLOR_PATH_MAGENTA = 0xFFFF00FF;  // Magenta color for navigation path
    private static final float PATH_BLEND_ALPHA = 0.75f;       // Path blend alpha (75% path, 25% original)

    // Bitmask Constants
    private static final long BITMASK_32BIT_UNSIGNED = 0xFFFFFFFFL;  // 32-bit unsigned integer bitmask

    // SICHERHEIT: volatile für Double-Checked Locking Pattern
    private static volatile NavigationPathOverlay instance;

    // Pfad-Positionen für schnellen Lookup (x,z -> ist auf Pfad)
    private final Set<Long> pathPositions = ConcurrentHashMap.newKeySet();

    // Aktuelle Pfad-Indizes (für das Verschwinden des Pfades)
    private volatile int currentPathIndex = 0;
    private volatile List<BlockPos> currentPath = null;

    // ═══════════════════════════════════════════════════════════
    // SINGLETON
    // ═══════════════════════════════════════════════════════════

    /**
     * SICHERHEIT: Double-Checked Locking für Thread-Safety
     */
    public static NavigationPathOverlay getInstance() {
        NavigationPathOverlay localRef = instance;
        if (localRef == null) {
            synchronized (NavigationPathOverlay.class) {
                localRef = instance;
                if (localRef == null) {
                    instance = localRef = new NavigationPathOverlay();
                }
            }
        }
        return localRef;
    }

    private NavigationPathOverlay() {}

    // ═══════════════════════════════════════════════════════════
    // PFAD MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Setzt den aktuellen Navigationspfad
     */
    public void setPath(List<BlockPos> path, int startIndex) {
        pathPositions.clear();
        currentPath = path;
        currentPathIndex = startIndex;

        if (path != null) {
            // Füge alle Positionen ab dem startIndex hinzu
            for (int i = startIndex; i < path.size(); i++) {
                BlockPos pos = path.get(i);
                pathPositions.add(posToKey(pos.getX(), pos.getZ()));
            }

            // Invalidiere alle Regions entlang des Pfades, damit sie neu gerendert werden
            invalidateRegionsAlongPath(path);
        }
    }

    /**
     * Aktualisiert den Fortschritt auf dem Pfad
     * Entfernt bereits abgelaufene Positionen
     */
    public void updateProgress(int newIndex) {
        if (currentPath == null || newIndex <= currentPathIndex) {
            return;
        }

        // Entferne Positionen die der Spieler bereits passiert hat
        for (int i = currentPathIndex; i < newIndex && i < currentPath.size(); i++) {
            BlockPos pos = currentPath.get(i);
            pathPositions.remove(posToKey(pos.getX(), pos.getZ()));
        }

        currentPathIndex = newIndex;
    }

    /**
     * Löscht den Pfad komplett
     */
    public void clearPath() {
        // Invalidiere Regions entlang des alten Pfades BEVOR wir ihn löschen
        if (currentPath != null && !currentPath.isEmpty()) {
            invalidateRegionsAlongPath(currentPath);
        }

        pathPositions.clear();
        currentPath = null;
        currentPathIndex = 0;
    }

    // ═══════════════════════════════════════════════════════════
    // FARB-ABFRAGE FÜR RENDERING
    // ═══════════════════════════════════════════════════════════

    /**
     * Prüft ob eine Position auf dem Pfad liegt
     */
    public boolean isOnPath(int worldX, int worldZ) {
        return pathPositions.contains(posToKey(worldX, worldZ));
    }

    /**
     * Gibt die Pfadfarbe zurück, oder 0 wenn nicht auf Pfad
     */
    public int getPathColor(int worldX, int worldZ) {
        if (isOnPath(worldX, worldZ)) {
            return COLOR_PATH_MAGENTA;
        }
        return 0;
    }

    /**
     * Mischt die Pfadfarbe mit einer Blockfarbe
     *
     * @param blockColor Die originale Blockfarbe (ARGB)
     * @param worldX Welt X-Koordinate
     * @param worldZ Welt Z-Koordinate
     * @return Gemischte Farbe oder originale Blockfarbe wenn nicht auf Pfad
     */
    public int blendWithPath(int blockColor, int worldX, int worldZ) {
        if (!isOnPath(worldX, worldZ)) {
            return blockColor;
        }

        return blendColors(blockColor, COLOR_PATH_MAGENTA, PATH_BLEND_ALPHA);
    }

    /**
     * Mischt zwei Farben
     */
    private int blendColors(int color1, int color2, float alpha2) {
        float alpha1 = 1.0f - alpha2;

        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 * alpha1 + a2 * alpha2);
        int r = (int) (r1 * alpha1 + r2 * alpha2);
        int g = (int) (g1 * alpha1 + g2 * alpha2);
        int b = (int) (b1 * alpha1 + b2 * alpha2);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════════════════════

    /**
     * Konvertiert X,Z zu einem eindeutigen Long-Schlüssel
     */
    private long posToKey(int x, int z) {
        return ((long) x << 32) | (z & BITMASK_32BIT_UNSIGNED);
    }

    /**
     * Gibt zurück ob ein Pfad aktiv ist
     */
    public boolean hasActivePath() {
        return !pathPositions.isEmpty();
    }

    /**
     * Invalidiert alle Regions entlang eines Pfades
     * Holt WorldMapData und ruft die Invalidierungs-Methode auf
     */
    private void invalidateRegionsAlongPath(List<BlockPos> path) {
        if (path == null || path.isEmpty()) {
            return;
        }

        try {
            de.rolandsw.schedulemc.mapview.MapViewConstants.getLightMapInstance()
                .getWorldMapData()
                .invalidateRegionsAlongPath(path);
        } catch (Exception e) {
            // Fehler beim Invalidieren abfangen - nicht kritisch
            de.rolandsw.schedulemc.mapview.MapViewConstants.getLogger()
                .warn("[NavigationPathOverlay] Failed to invalidate regions: " + e.getMessage());
        }
    }
}
