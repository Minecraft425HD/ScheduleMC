package de.rolandsw.schedulemc.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

/**
 * Zentrale Input-Validierung für alle Benutzereingaben
 *
 * SICHERHEIT: Verhindert Injection-Angriffe, DoS durch überlange Strings,
 * und ungültige Daten die zu Server-Crashes führen könnten.
 */
public class InputValidation {

    // Konstanten
    public static final int MAX_NPC_NAME_LENGTH = 32;
    public static final int MAX_PLOT_NAME_LENGTH = 64;
    public static final int MAX_TERRITORY_NAME_LENGTH = 48;  // OPTIMIERT: Territory Name Limit
    public static final int MAX_DESCRIPTION_LENGTH = 256;
    public static final int MAX_SKIN_FILE_LENGTH = 128;
    public static final int MAX_PACKET_STRING_LENGTH = 1024;
    public static final double MAX_AMOUNT = 1_000_000_000_000.0;  // Max money amount
    public static final int MAX_DIALOG_TEXT_LENGTH = 512;  // Dialog text limit

    // Aliase für Rückwärtskompatibilität
    public static class ValidationResult extends Result {
        private ValidationResult(boolean valid, String error, String errorKey, Object[] errorArgs, String sanitizedValue) {
            super(valid, error, errorKey, errorArgs, sanitizedValue);
        }

        public static ValidationResult success() { return new ValidationResult(true, null, null, null, null); }
        public static ValidationResult success(String sanitizedValue) { return new ValidationResult(true, null, null, null, sanitizedValue); }
        public static ValidationResult failure(String error) { return new ValidationResult(false, error, null, null, null); }
        public static ValidationResult failure(String errorKey, Object... args) { return new ValidationResult(false, null, errorKey, args, null); }

        // Konvertiert Result zu ValidationResult
        public static ValidationResult from(Result result) {
            return new ValidationResult(result.isValid(), result.getError(), result.getErrorKey(), result.getErrorArgs(), result.getSanitizedValue());
        }
    }

    // Weltgrenzen
    public static final int MIN_Y = -64;
    public static final int MAX_Y = 320;
    public static final int MAX_COORDINATE = 30_000_000;

    private static final String ALLOWED_NAME_CHARS = "^[a-zA-Z0-9_ äöüÄÖÜßéèêëàâáãåçñ\\-\\.]+$";
    private static final String ALLOWED_FILENAME_CHARS = "^[a-zA-Z0-9_\\-\\.]+$";

    public static class Result {
        private final boolean valid;
        private final String error;
        private final String errorKey;
        private final Object[] errorArgs;
        private final String sanitizedValue;

        protected Result(boolean valid, @Nullable String error, @Nullable String errorKey, @Nullable Object[] errorArgs, @Nullable String sanitizedValue) {
            this.valid = valid;
            this.error = error;
            this.errorKey = errorKey;
            this.errorArgs = errorArgs;
            this.sanitizedValue = sanitizedValue;
        }

        public static Result success() { return new Result(true, null, null, null, null); }
        public static Result success(String sanitizedValue) { return new Result(true, null, null, null, sanitizedValue); }
        public static Result failure(String error) { return new Result(false, error, null, null, null); }
        public static Result failure(String errorKey, Object... args) { return new Result(false, null, errorKey, args, null); }

        public boolean isValid() { return valid; }
        public boolean isFailure() { return !valid; }
        @Nullable public String getError() { return error; }
        @Nullable public String getErrorMessage() { return error; }  // Alias für getError
        @Nullable public String getErrorKey() { return errorKey; }
        @Nullable public Object[] getErrorArgs() { return errorArgs; }
        @Nullable public String getSanitizedValue() { return sanitizedValue; }

        public Component toComponent() {
            if (errorKey != null) {
                return Component.translatable(errorKey, errorArgs != null ? errorArgs : new Object[0]);
            }
            return Component.literal(error != null ? error : "");
        }
    }

    public static Result validateNPCName(@Nullable String name) {
        if (name == null || name.isEmpty()) {
            return Result.failure("validation.npc.name_empty");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NPC_NAME_LENGTH) {
            return Result.failure("validation.npc.max_length", MAX_NPC_NAME_LENGTH);
        }
        if (trimmed.length() < 2) {
            return Result.failure("validation.npc.min_length");
        }
        if (!trimmed.matches(ALLOWED_NAME_CHARS)) {
            return Result.failure("validation.npc.invalid_characters");
        }
        if (containsDangerousPatterns(trimmed)) {
            return Result.failure("validation.npc.dangerous_pattern");
        }
        return Result.success(trimmed);
    }

    public static Result validatePlotName(@Nullable String name) {
        if (name == null || name.isEmpty()) {
            return Result.failure("validation.plot.name_empty");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_PLOT_NAME_LENGTH) {
            return Result.failure("validation.plot.max_length", MAX_PLOT_NAME_LENGTH);
        }
        if (!trimmed.matches(ALLOWED_NAME_CHARS)) {
            return Result.failure("validation.plot.invalid_characters");
        }
        return Result.success(trimmed);
    }

    /**
     * Validiert Territory-Namen
     * SICHERHEIT: Verhindert DoS durch überlange Strings
     */
    public static Result validateTerritoryName(@Nullable String name) {
        if (name == null || name.isEmpty()) {
            return Result.success("");  // Leerer Name ist erlaubt
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_TERRITORY_NAME_LENGTH) {
            return Result.failure("validation.territory.max_length", MAX_TERRITORY_NAME_LENGTH);
        }
        if (!trimmed.matches(ALLOWED_NAME_CHARS)) {
            return Result.failure("validation.territory.invalid_characters");
        }
        if (containsDangerousPatterns(trimmed)) {
            return Result.failure("validation.territory.dangerous_pattern");
        }
        return Result.success(trimmed);
    }

    /**
     * Validiert Skin-Dateinamen
     * SICHERHEIT: Verhindert Path Traversal Angriffe
     */
    public static Result validateSkinFileName(@Nullable String filename) {
        if (filename == null || filename.isEmpty()) {
            return Result.success("");
        }
        String trimmed = filename.trim();
        if (trimmed.length() > MAX_SKIN_FILE_LENGTH) {
            return Result.failure("validation.skin.filename_too_long");
        }
        if (!trimmed.matches(ALLOWED_FILENAME_CHARS)) {
            return Result.failure("validation.skin.invalid_characters");
        }
        // SICHERHEIT: Verhindere Path Traversal
        if (trimmed.contains("..") || trimmed.contains("/") || trimmed.contains("\\")) {
            return Result.failure("validation.skin.path_characters");
        }
        // SICHERHEIT: Blockiere System-Dateien
        String lower = trimmed.toLowerCase();
        if (lower.equals("con") || lower.equals("prn") || lower.equals("aux") ||
            lower.equals("nul") || lower.startsWith("com") || lower.startsWith("lpt")) {
            return Result.failure("validation.skin.reserved_filename");
        }
        return Result.success(trimmed);
    }

    /**
     * Validiert Dialog-Text
     * SICHERHEIT: Verhindert Command-Injection in NPC-Dialogen
     */
    public static Result validateDialogText(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return Result.failure("validation.dialog.text_empty");
        }
        String trimmed = text.trim();
        if (trimmed.length() > MAX_DIALOG_TEXT_LENGTH) {
            return Result.failure("validation.dialog.max_length", MAX_DIALOG_TEXT_LENGTH);
        }
        // SICHERHEIT: Verhindere Command-Injection
        if (trimmed.startsWith("/")) {
            return Result.failure("validation.dialog.starts_with_slash");
        }
        // SICHERHEIT: Prüfe auf Server-Commands in Text
        if (containsCommandInjection(trimmed)) {
            return Result.failure("validation.dialog.command_injection");
        }
        // SICHERHEIT: Prüfe auf gefährliche Muster
        if (containsDangerousPatterns(trimmed)) {
            return Result.failure("validation.dialog.dangerous_pattern");
        }
        return Result.success(trimmed);
    }

    /**
     * Prüft auf Command-Injection-Versuche
     */
    private static boolean containsCommandInjection(String input) {
        String lower = input.toLowerCase();
        // Blockiere eingebettete Commands
        if (lower.contains("/op ") || lower.contains("/gamemode") ||
            lower.contains("/execute") || lower.contains("/give") ||
            lower.contains("/setblock") || lower.contains("/kill")) {
            return true;
        }
        return false;
    }

    /**
     * Validiert Verzeichnispfade
     * SICHERHEIT: Verhindert Path Traversal zu sensitiven Verzeichnissen
     */
    public static Result validatePath(@Nullable String path) {
        if (path == null || path.isEmpty()) {
            return Result.failure("validation.path.empty");
        }
        String normalized = path.trim().replace("\\", "/");

        // SICHERHEIT: Verhindere Path Traversal
        if (normalized.contains("..")) {
            return Result.failure("validation.path.traversal");
        }
        // SICHERHEIT: Blockiere absolute Pfade
        if (normalized.startsWith("/") || normalized.matches("^[A-Za-z]:.*")) {
            return Result.failure("validation.path.absolute_not_allowed");
        }
        // SICHERHEIT: Whitelist für erlaubte Basis-Verzeichnisse
        String[] allowedPrefixes = {"config/", "skins/", "data/", "backups/"};
        boolean hasAllowedPrefix = false;
        for (String prefix : allowedPrefixes) {
            if (normalized.startsWith(prefix)) {
                hasAllowedPrefix = true;
                break;
            }
        }
        if (!hasAllowedPrefix) {
            return Result.failure("validation.path.invalid_prefix");
        }
        return Result.success(normalized);
    }

    public static Result validatePacketString(@Nullable String value, String fieldName) {
        if (value == null) return Result.success("");
        if (value.length() > MAX_PACKET_STRING_LENGTH) {
            return Result.failure("validation.packet.too_long", fieldName);
        }
        return Result.success(value);
    }

    public static Result validateBlockPos(@Nullable BlockPos pos) {
        if (pos == null) {
            return Result.failure("validation.position.null");
        }
        if (Math.abs(pos.getX()) > MAX_COORDINATE || Math.abs(pos.getZ()) > MAX_COORDINATE) {
            return Result.failure("validation.position.out_of_bounds");
        }
        if (pos.getY() < MIN_Y || pos.getY() > MAX_Y) {
            return Result.failure("validation.position.y_out_of_bounds");
        }
        return Result.success();
    }

    public static Result validatePlotRegion(@Nullable BlockPos pos1, @Nullable BlockPos pos2) {
        Result r1 = validateBlockPos(pos1);
        if (!r1.isValid()) return r1;
        Result r2 = validateBlockPos(pos2);
        if (!r2.isValid()) return r2;

        int dx = Math.abs(pos1.getX() - pos2.getX());
        int dz = Math.abs(pos1.getZ() - pos2.getZ());
        if (dx > 10000 || dz > 10000) {
            return Result.failure("validation.plot.region_too_large");
        }
        return Result.success();
    }

    private static boolean containsDangerousPatterns(String input) {
        String lower = input.toLowerCase();
        if (lower.contains("'--") || lower.contains("'; drop") || lower.contains("1=1")) return true;
        if (lower.contains("<script") || lower.contains("javascript:")) return true;
        if (lower.contains("/op ") || lower.contains("/gamemode") || lower.contains("/execute")) return true;
        for (char c : input.toCharArray()) {
            if (c < 32 && c != '\n' && c != '\r' && c != '\t') return true;
        }
        return false;
    }

    public static String sanitize(String input) {
        if (input == null) return "";
        return input.replaceAll("§[klmnor]", "")
                   .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
                   .trim();
    }

    // ═══════════════════════════════════════════════════════════
    // ALIAS-METHODEN für Rückwärtskompatibilität (geben ValidationResult zurück)
    // ═══════════════════════════════════════════════════════════

    /**
     * Alias für validatePlotName - gibt ValidationResult zurück
     */
    public static ValidationResult validateName(@Nullable String name) {
        return ValidationResult.from(validatePlotName(name));
    }

    /**
     * Alias für validateAmount (double) - gibt ValidationResult zurück
     */
    public static ValidationResult validatePrice(double price) {
        return ValidationResult.from(validateAmountInternal(price));
    }

    /**
     * validateAmount mit ValidationResult Rückgabe für Rückwärtskompatibilität
     */
    public static ValidationResult validateAmount(double amount) {
        return ValidationResult.from(validateAmountInternal(amount));
    }

    /**
     * Validiert Prozentsätze (0-100)
     */
    public static ValidationResult validatePercentage(int percentage) {
        if (percentage < 0) {
            return ValidationResult.failure("validation.percentage.negative");
        }
        if (percentage > 100) {
            return ValidationResult.failure("validation.percentage.too_large");
        }
        return ValidationResult.success();
    }

    /**
     * Interne Implementierung von validateAmount
     */
    private static Result validateAmountInternal(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            return Result.failure("validation.amount.invalid");
        }
        if (amount < 0) {
            return Result.failure("validation.amount.negative");
        }
        if (amount > MAX_AMOUNT) {
            return Result.failure("validation.amount.too_large");
        }
        return Result.success();
    }

    private InputValidation() {
        throw new UnsupportedOperationException("Utility class");
    }
}
