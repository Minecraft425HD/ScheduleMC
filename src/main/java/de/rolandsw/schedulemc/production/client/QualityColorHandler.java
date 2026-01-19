package de.rolandsw.schedulemc.production.client;

import de.rolandsw.schedulemc.production.core.ProductionQuality;
import de.rolandsw.schedulemc.production.items.PackagedDrugItem;
import net.minecraft.world.item.ItemStack;

/**
 * Utility-Klasse für Qualitäts-Färbung
 * Konvertiert Minecraft-Farbcodes in RGB-Hex-Werte
 */
public class QualityColorHandler {

    /**
     * Gibt RGB-Farbe basierend auf Quality-Level zurück
     * Layer 1 (Vignette) wird entsprechend eingefärbt
     */
    public static int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) {
            return 0xFFFFFF; // Layer 0 bleibt weiß (ungefärbt)
        }

        // Quality aus NBT auslesen
        ProductionQuality quality = PackagedDrugItem.parseQuality(
            PackagedDrugItem.getQuality(stack)
        );

        // Farbe basierend auf Quality-Level
        return getColorForQualityLevel(quality.getLevel());
    }

    /**
     * Konvertiert Quality-Level in RGB-Farbe
     * Mapping: Level -> Minecraft-Farbcode -> RGB
     */
    private static int getColorForQualityLevel(int level) {
        return switch (level) {
            case 0 -> 0x555555;  // §8 Dunkelgrau (Schlecht)
            case 1 -> 0xAAAAAA;  // §7 Grau (Standard)
            case 2 -> 0x55FF55;  // §a Grün (Gut)
            case 3 -> 0xFFAA00;  // §6 Gold (Premium)
            case 4 -> 0xFF55FF;  // §d Pink (Legendär/Exotic)
            default -> 0xFFFFFF; // Weiß (Fallback)
        };
    }

    /**
     * Alternative: Konvertiert Minecraft-Farbcode direkt in RGB
     * Beispiel: "§6" -> 0xFFAA00, "§d§l" -> 0xFF55FF
     */
    public static int minecraftColorToRGB(String colorCode) {
        if (colorCode == null || colorCode.isEmpty()) {
            return 0xFFFFFF;
        }

        // Extrahiere ersten Farbcode (ignoriere Formatting wie §l)
        char code = colorCode.length() >= 2 ? colorCode.charAt(1) : 'f';

        return switch (code) {
            case '0' -> 0x000000; // Schwarz
            case '1' -> 0x0000AA; // Dunkelblau
            case '2' -> 0x00AA00; // Dunkelgrün
            case '3' -> 0x00AAAA; // Dunkel Aqua
            case '4' -> 0xAA0000; // Dunkelrot
            case '5' -> 0xAA00AA; // Dunkel Lila
            case '6' -> 0xFFAA00; // Gold
            case '7' -> 0xAAAAAA; // Grau
            case '8' -> 0x555555; // Dunkelgrau
            case '9' -> 0x5555FF; // Blau
            case 'a' -> 0x55FF55; // Grün
            case 'b' -> 0x55FFFF; // Aqua
            case 'c' -> 0xFF5555; // Rot
            case 'd' -> 0xFF55FF; // Pink
            case 'e' -> 0xFFFF55; // Gelb
            case 'f' -> 0xFFFFFF; // Weiß
            default -> 0xFFFFFF;
        };
    }
}
