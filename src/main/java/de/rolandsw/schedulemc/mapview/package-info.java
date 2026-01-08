/**
 * Interactive world map system with real-time rendering, zoom, and navigation.
 *
 * <h2>Overview</h2>
 * <p>This package provides a comprehensive map view system that renders the Minecraft world
 * as a 2D map with multiple zoom levels, plot boundaries, NPC markers, and navigation paths.</p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li><strong>MapViewRenderer:</strong> Main rendering engine (1,922 LOC)</li>
 *   <li><strong>ColorCalculationService:</strong> Block color calculation (1,298 LOC)</li>
 *   <li><strong>WorldMapScreen:</strong> Interactive UI (1,209 LOC)</li>
 *   <li><strong>NavigationOverlay:</strong> Path rendering and waypoints</li>
 * </ul>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>5 zoom levels (1:1 to 1:32 scale)</li>
 *   <li>Real-time chunk updates</li>
 *   <li>Plot boundary visualization</li>
 *   <li>NPC position markers</li>
 *   <li>Navigation path overlay</li>
 *   <li>Biome-aware coloring</li>
 *   <li>Lighting and height shading</li>
 * </ul>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.mapview.presentation.renderer.MapViewRenderer
 */
package de.rolandsw.schedulemc.mapview;
