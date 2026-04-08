package de.rolandsw.schedulemc.util;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Hilfsklasse für sprachabhängige Geldbetragsformatierung.
 *
 * Deutsch:  "100,00€"  (Euro-Zeichen nach dem Betrag)
 * Englisch: "$100.00"  (Dollar-Zeichen vor dem Betrag)
 *
 * Verwendet den Translation-Key "currency.format" aus den Lang-Dateien,
 * daher nur auf dem Client verwendbar (wo die Sprache bekannt ist).
 */
@OnlyIn(Dist.CLIENT)
public final class MoneyFormat {

    private MoneyFormat() {}

    /**
     * Formatiert einen Geldbetrag mit Dezimalstellen sprachabhängig.
     * Beispiel: 1234.5 → "$1234.50" (EN) / "1234,50€" (DE)
     */
    public static String format(double amount) {
        return Component.translatable("currency.format", String.format("%.2f", amount)).getString();
    }

    /**
     * Formatiert einen ganzzahligen Geldbetrag sprachabhängig.
     * Beispiel: 500 → "$500" (EN) / "500€" (DE)
     */
    public static String format(int amount) {
        return Component.translatable("currency.format", String.valueOf(amount)).getString();
    }

    /**
     * Formatiert einen Geldbetrag ohne Nachkommastellen sprachabhängig.
     * Beispiel: 1234.7 → "$1234" (EN) / "1234€" (DE)
     */
    public static String formatNoDecimal(double amount) {
        return Component.translatable("currency.format", String.format("%.0f", amount)).getString();
    }

    /**
     * Formatiert einen Geldbetrag mit einer Nachkommastelle sprachabhängig.
     */
    public static String formatOneDecimal(double amount) {
        return Component.translatable("currency.format", String.format("%.1f", amount)).getString();
    }
}
