package de.rolandsw.schedulemc.util;

/**
 * Input-Validierungs-Utilities für Commands
 * Verhindert ungültige Eingaben und potenzielle Exploits
 */
public class InputValidation {

    // Konstanten für Limits
    public static final double MAX_PRICE = 1_000_000_000.0; // 1 Milliarde
    public static final int MAX_NAME_LENGTH = 64;
    public static final int MIN_NAME_LENGTH = 1;
    public static final int MAX_AMOUNT = 1_000_000_000; // 1 Milliarde Items

    /**
     * Validiert einen Preis-Wert
     * @param price Der zu validierende Preis
     * @return ValidationResult mit Erfolg/Fehler
     */
    public static ValidationResult validatePrice(double price) {
        if (price <= 0) {
            return ValidationResult.failure("Der Preis muss positiv sein!");
        }
        if (price > MAX_PRICE) {
            return ValidationResult.failure("Der Preis ist zu hoch! Maximum: " +
                String.format("%.2f€", MAX_PRICE));
        }
        if (Double.isNaN(price) || Double.isInfinite(price)) {
            return ValidationResult.failure("Ungültiger Preis-Wert!");
        }
        return ValidationResult.success();
    }

    /**
     * Validiert einen Namen (für Plots, NPCs, etc.)
     * @param name Der zu validierende Name
     * @return ValidationResult mit Erfolg/Fehler
     */
    public static ValidationResult validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.failure("Der Name darf nicht leer sein!");
        }

        String trimmed = name.trim();

        if (trimmed.length() < MIN_NAME_LENGTH) {
            return ValidationResult.failure("Der Name ist zu kurz! Minimum: " + MIN_NAME_LENGTH + " Zeichen");
        }
        if (trimmed.length() > MAX_NAME_LENGTH) {
            return ValidationResult.failure("Der Name ist zu lang! Maximum: " + MAX_NAME_LENGTH + " Zeichen");
        }

        // Verhindere gefährliche Zeichen
        if (trimmed.contains("§")) {
            return ValidationResult.failure("Der Name darf keine Formatierungscodes (§) enthalten!");
        }

        // Verhindere Control Characters
        for (char c : trimmed.toCharArray()) {
            if (Character.isISOControl(c)) {
                return ValidationResult.failure("Der Name enthält ungültige Steuerzeichen!");
            }
        }

        return ValidationResult.success();
    }

    /**
     * Validiert einen Betrag (Geld, Items, etc.)
     * @param amount Der zu validierende Betrag
     * @return ValidationResult mit Erfolg/Fehler
     */
    public static ValidationResult validateAmount(double amount) {
        if (amount <= 0) {
            return ValidationResult.failure("Der Betrag muss positiv sein!");
        }
        if (amount > MAX_AMOUNT) {
            return ValidationResult.failure("Der Betrag ist zu hoch! Maximum: " + MAX_AMOUNT);
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            return ValidationResult.failure("Ungültiger Betrags-Wert!");
        }
        return ValidationResult.success();
    }

    /**
     * Validiert einen Integer-Betrag
     * @param amount Der zu validierende Betrag
     * @return ValidationResult mit Erfolg/Fehler
     */
    public static ValidationResult validateAmount(int amount) {
        if (amount <= 0) {
            return ValidationResult.failure("Der Betrag muss positiv sein!");
        }
        if (amount > MAX_AMOUNT) {
            return ValidationResult.failure("Der Betrag ist zu hoch! Maximum: " + MAX_AMOUNT);
        }
        return ValidationResult.success();
    }

    /**
     * Validiert einen Prozentsatz (0-100)
     * @param percentage Der zu validierende Prozentsatz
     * @return ValidationResult mit Erfolg/Fehler
     */
    public static ValidationResult validatePercentage(double percentage) {
        if (percentage < 0 || percentage > 100) {
            return ValidationResult.failure("Prozentsatz muss zwischen 0 und 100 liegen!");
        }
        return ValidationResult.success();
    }

    /**
     * Validiert eine Zeitangabe in Ticks
     * @param ticks Die zu validierende Tick-Anzahl
     * @return ValidationResult mit Erfolg/Fehler
     */
    public static ValidationResult validateTicks(int ticks) {
        if (ticks < 0) {
            return ValidationResult.failure("Zeitangabe darf nicht negativ sein!");
        }
        if (ticks > 72000) { // Max 1 Stunde (72000 ticks = 1 hour)
            return ValidationResult.failure("Zeitangabe zu groß! Maximum: 72000 Ticks (1 Stunde)");
        }
        return ValidationResult.success();
    }

    /**
     * Validiert eine Koordinate
     * @param coord Die zu validierende Koordinate
     * @return ValidationResult mit Erfolg/Fehler
     */
    public static ValidationResult validateCoordinate(int coord) {
        if (Math.abs(coord) > 30_000_000) { // Minecraft Welt-Grenze
            return ValidationResult.failure("Koordinate außerhalb der Weltgrenzen! (±30,000,000)");
        }
        return ValidationResult.success();
    }

    /**
     * Ergebnis einer Validierung
     */
    public static class ValidationResult {
        private final boolean success;
        private final String errorMessage;

        private ValidationResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isFailure() {
            return !success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
