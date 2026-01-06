package de.rolandsw.schedulemc.util;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Umfassende Sicherheitstests für InputValidation
 *
 * KRITISCHE SICHERHEITSPRÜFUNGEN:
 * - Path Traversal Prevention
 * - Command Injection Prevention
 * - XSS Prevention
 * - Reserved Filename Prevention
 * - DoS durch Length Limits
 * - Koordinaten- und Betragsvalidierung
 */
@DisplayName("InputValidation Security Tests")
class InputValidationTest {

    // ═══════════════════════════════════════════════════════════
    // PATH TRAVERSAL PREVENTION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Path Traversal Prevention Tests")
    class PathTraversalTests {

        @Test
        @DisplayName("SECURITY: Should block '../' sequences in paths")
        void shouldBlockDotDotSlashInPaths() {
            InputValidation.Result result = InputValidation.validatePath("config/../../../etc/passwd");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("..");
        }

        @Test
        @DisplayName("SECURITY: Should block absolute Unix paths")
        void shouldBlockAbsoluteUnixPaths() {
            InputValidation.Result result = InputValidation.validatePath("/etc/passwd");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("Absolute Pfade");
        }

        @Test
        @DisplayName("SECURITY: Should block absolute Windows paths (C:)")
        void shouldBlockAbsoluteWindowsPaths() {
            InputValidation.Result result = InputValidation.validatePath("C:\\Windows\\System32");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("Absolute Pfade");
        }

        @Test
        @DisplayName("SECURITY: Should block Windows UNC paths")
        void shouldBlockWindowsUNCPaths() {
            InputValidation.Result result = InputValidation.validatePath("\\\\server\\share\\file.txt");

            // Nach Normalisierung wird \\ zu //, sollte nicht mit erlaubten Prefixes starten
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("SECURITY: Should only allow whitelisted directories")
        void shouldOnlyAllowWhitelistedDirectories() {
            InputValidation.Result result = InputValidation.validatePath("forbidden/path/file.txt");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("erlaubten Verzeichnis");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "config/settings.json",
            "skins/player.png",
            "data/plots.json",
            "backups/world_backup.zip"
        })
        @DisplayName("SECURITY: Should allow safe relative paths in whitelisted directories")
        void shouldAllowSafeRelativePaths(String path) {
            InputValidation.Result result = InputValidation.validatePath(path);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEqualTo(path);
        }

        @Test
        @DisplayName("SECURITY: Should normalize backslashes to forward slashes")
        void shouldNormalizeBackslashes() {
            InputValidation.Result result = InputValidation.validatePath("config\\settings.json");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEqualTo("config/settings.json");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("SECURITY: Should reject null/empty paths")
        void shouldRejectNullOrEmptyPaths(String path) {
            InputValidation.Result result = InputValidation.validatePath(path);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("nicht leer");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SKIN FILENAME VALIDATION (PATH TRAVERSAL + RESERVED NAMES)
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Skin Filename Validation Tests")
    class SkinFilenameTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "../../../etc/passwd",
            "..\\..\\Windows\\System32",
            "skin/../admin.png",
            "folder/../../secret.png"
        })
        @DisplayName("SECURITY: Should block path traversal in skin filenames")
        void shouldBlockPathTraversalInSkinFilenames(String filename) {
            InputValidation.Result result = InputValidation.validateSkinFileName(filename);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).containsAnyOf("..", "Pfadzeichen", "ungültige Zeichen");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "skin/player.png",
            "folder\\texture.png",
            "admin/root.png"
        })
        @DisplayName("SECURITY: Should block directory separators in skin filenames")
        void shouldBlockDirectorySeparators(String filename) {
            InputValidation.Result result = InputValidation.validateSkinFileName(filename);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("Pfadzeichen");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "CON",
            "PRN",
            "AUX",
            "NUL",
            "con.png",
            "prn.txt",
            "COM1",
            "COM9",
            "LPT1",
            "LPT9"
        })
        @DisplayName("SECURITY: Should block Windows reserved filenames")
        void shouldBlockWindowsReservedFilenames(String filename) {
            InputValidation.Result result = InputValidation.validateSkinFileName(filename);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("Reservierter Dateiname");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "player_skin.png",
            "zombie-texture.png",
            "skin_1.png",
            "custom.skin.png"
        })
        @DisplayName("Should allow valid skin filenames")
        void shouldAllowValidSkinFilenames(String filename) {
            InputValidation.Result result = InputValidation.validateSkinFileName(filename);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEqualTo(filename);
        }

        @Test
        @DisplayName("Should allow empty skin filename")
        void shouldAllowEmptySkinFilename() {
            InputValidation.Result result = InputValidation.validateSkinFileName("");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEmpty();
        }

        @Test
        @DisplayName("SECURITY: Should reject too long skin filenames")
        void shouldRejectTooLongFilenames() {
            String tooLong = "a".repeat(InputValidation.MAX_SKIN_FILE_LENGTH + 1);
            InputValidation.Result result = InputValidation.validateSkinFileName(tooLong);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("zu lang");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "skin<script>.png",
            "test;file.png",
            "file&name.png",
            "test|file.png"
        })
        @DisplayName("SECURITY: Should block special characters in filenames")
        void shouldBlockSpecialCharacters(String filename) {
            InputValidation.Result result = InputValidation.validateSkinFileName(filename);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("ungültige Zeichen");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // COMMAND INJECTION PREVENTION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Command Injection Prevention Tests")
    class CommandInjectionTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "/op hacker",
            "/gamemode creative @p",
            "/execute run kill @a",
            "/give @p minecraft:diamond 64",
            "/setblock ~ ~ ~ minecraft:tnt",
            "/kill @e[type=!player]"
        })
        @DisplayName("SECURITY: Should block dangerous Minecraft commands")
        void shouldBlockDangerousMinecraftCommands(String command) {
            InputValidation.Result result = InputValidation.validateDialogText(command);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).containsAnyOf("nicht mit '/' beginnen", "Befehle");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Hello /op admin in the middle",
            "Text with /gamemode 1 embedded",
            "Try this /execute command",
            "Get /give items here",
            "Use /setblock carefully",
            "Don't /kill anyone"
        })
        @DisplayName("SECURITY: Should block embedded commands in text")
        void shouldBlockEmbeddedCommands(String text) {
            InputValidation.Result result = InputValidation.validateDialogText(text);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("Befehle");
        }

        @Test
        @DisplayName("SECURITY: Should block command starting with slash")
        void shouldBlockCommandsStartingWithSlash() {
            InputValidation.Result result = InputValidation.validateDialogText("/help");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("nicht mit '/' beginnen");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Welcome to our server!",
            "Hello player, how are you?",
            "This is a normal dialog text.",
            "Click here to continue your quest."
        })
        @DisplayName("Should allow normal dialog text")
        void shouldAllowNormalDialogText(String text) {
            InputValidation.Result result = InputValidation.validateDialogText(text);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEqualTo(text);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should reject empty dialog text")
        void shouldRejectEmptyDialogText(String text) {
            InputValidation.Result result = InputValidation.validateDialogText(text);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("nicht leer");
        }

        @Test
        @DisplayName("SECURITY: Should reject too long dialog text")
        void shouldRejectTooLongDialogText() {
            String tooLong = "a".repeat(InputValidation.MAX_DIALOG_TEXT_LENGTH + 1);
            InputValidation.Result result = InputValidation.validateDialogText(tooLong);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("maximal");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // XSS PREVENTION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("XSS Prevention Tests")
    class XSSPreventionTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "<script>alert('XSS')</script>",
            "<SCRIPT>alert('XSS')</SCRIPT>",
            "Test <script>hack()</script> text",
            "<script src='evil.js'></script>"
        })
        @DisplayName("SECURITY: Should block <script> tags in names and text")
        void shouldBlockScriptTags(String input) {
            InputValidation.Result npcResult = InputValidation.validateNPCName(input);
            InputValidation.Result territoryResult = InputValidation.validateTerritoryName(input);

            assertThat(npcResult.isValid()).isFalse();
            assertThat(npcResult.getError()).contains("nicht erlaubte Zeichenfolgen");

            assertThat(territoryResult.isValid()).isFalse();
            assertThat(territoryResult.getError()).contains("nicht erlaubte Zeichenfolgen");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "javascript:alert('XSS')",
            "JAVASCRIPT:void(0)",
            "Name with javascript: protocol"
        })
        @DisplayName("SECURITY: Should block javascript: URIs")
        void shouldBlockJavaScriptURIs(String input) {
            InputValidation.Result result = InputValidation.validateNPCName(input);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).containsAnyOf("ungültige Zeichen", "nicht erlaubte Zeichenfolgen");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Name'--",
            "'; DROP TABLE users--",
            "Name with 1=1",
            "Test'; drop",
            "SQL'--injection"
        })
        @DisplayName("SECURITY: Should block SQL injection patterns")
        void shouldBlockSQLInjectionPatterns(String input) {
            InputValidation.Result result = InputValidation.validateNPCName(input);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).containsAnyOf("ungültige Zeichen", "nicht erlaubte Zeichenfolgen");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // NPC NAME VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("NPC Name Validation Tests")
    class NPCNameValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "Hans",
            "Max Mustermann",
            "Händler-1",
            "NPC_Quest.Giver",
            "Müller",
            "José García"
        })
        @DisplayName("Should allow valid NPC names")
        void shouldAllowValidNPCNames(String name) {
            InputValidation.Result result = InputValidation.validateNPCName(name);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEqualTo(name);
        }

        @Test
        @DisplayName("Should trim whitespace from NPC names")
        void shouldTrimWhitespaceFromNPCNames() {
            InputValidation.Result result = InputValidation.validateNPCName("  TestNPC  ");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEqualTo("TestNPC");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should reject null/empty NPC names")
        void shouldRejectNullOrEmptyNPCNames(String name) {
            InputValidation.Result result = InputValidation.validateNPCName(name);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("nicht leer");
        }

        @Test
        @DisplayName("Should reject too short NPC names")
        void shouldRejectTooShortNPCNames() {
            InputValidation.Result result = InputValidation.validateNPCName("X");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("mindestens 2 Zeichen");
        }

        @Test
        @DisplayName("SECURITY: Should reject too long NPC names (DoS prevention)")
        void shouldRejectTooLongNPCNames() {
            String tooLong = "a".repeat(InputValidation.MAX_NPC_NAME_LENGTH + 1);
            InputValidation.Result result = InputValidation.validateNPCName(tooLong);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("maximal " + InputValidation.MAX_NPC_NAME_LENGTH);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "NPC@123",
            "Test#Name",
            "Name$Dollar",
            "NPC[brackets]",
            "Test{braces}",
            "Name!Exclaim"
        })
        @DisplayName("Should reject invalid characters in NPC names")
        void shouldRejectInvalidCharactersInNPCNames(String name) {
            InputValidation.Result result = InputValidation.validateNPCName(name);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("ungültige Zeichen");
        }

        @Test
        @DisplayName("SECURITY: Should reject NPC names with control characters")
        void shouldRejectControlCharacters() {
            String nameWithControlChar = "Test\u0001Name";
            InputValidation.Result result = InputValidation.validateNPCName(nameWithControlChar);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).containsAnyOf("ungültige Zeichen", "nicht erlaubte Zeichenfolgen");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PLOT NAME VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Plot Name Validation Tests")
    class PlotNameValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "Spawn Area",
            "Player-Home_1",
            "Market.Place",
            "Arena PvP",
            "Große Stadt",
            "Château français"
        })
        @DisplayName("Should allow valid plot names")
        void shouldAllowValidPlotNames(String name) {
            InputValidation.Result result = InputValidation.validatePlotName(name);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEqualTo(name);
        }

        @Test
        @DisplayName("SECURITY: Should reject too long plot names")
        void shouldRejectTooLongPlotNames() {
            String tooLong = "a".repeat(InputValidation.MAX_PLOT_NAME_LENGTH + 1);
            InputValidation.Result result = InputValidation.validatePlotName(tooLong);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("maximal " + InputValidation.MAX_PLOT_NAME_LENGTH);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject null/empty plot names")
        void shouldRejectNullOrEmptyPlotNames(String name) {
            InputValidation.Result result = InputValidation.validatePlotName(name);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("nicht leer");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Plot@Home",
            "Test#123",
            "Name$Value",
            "Plot[1]",
            "Test{value}"
        })
        @DisplayName("Should reject invalid characters in plot names")
        void shouldRejectInvalidCharactersInPlotNames(String name) {
            InputValidation.Result result = InputValidation.validatePlotName(name);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("ungültige Zeichen");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TERRITORY NAME VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Territory Name Validation Tests")
    class TerritoryNameValidationTests {

        @Test
        @DisplayName("Should allow empty territory names")
        void shouldAllowEmptyTerritoryNames() {
            InputValidation.Result result = InputValidation.validateTerritoryName("");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEmpty();
        }

        @Test
        @DisplayName("SECURITY: Should reject too long territory names")
        void shouldRejectTooLongTerritoryNames() {
            String tooLong = "a".repeat(InputValidation.MAX_TERRITORY_NAME_LENGTH + 1);
            InputValidation.Result result = InputValidation.validateTerritoryName(tooLong);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("maximal " + InputValidation.MAX_TERRITORY_NAME_LENGTH);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Kingdom",
            "Empire-North",
            "Territory_1",
            "Zone.Alpha"
        })
        @DisplayName("Should allow valid territory names")
        void shouldAllowValidTerritoryNames(String name) {
            InputValidation.Result result = InputValidation.validateTerritoryName(name);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEqualTo(name);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // AMOUNT VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Amount Validation Tests")
    class AmountValidationTests {

        @Test
        @DisplayName("SECURITY: Should reject negative amounts")
        void shouldRejectNegativeAmounts() {
            InputValidation.ValidationResult result = InputValidation.validateAmount(-100.0);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("nicht negativ");
        }

        @Test
        @DisplayName("SECURITY: Should reject NaN amounts")
        void shouldRejectNaNAmounts() {
            InputValidation.ValidationResult result = InputValidation.validateAmount(Double.NaN);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("Ungültiger Betrag");
        }

        @Test
        @DisplayName("SECURITY: Should reject infinite amounts")
        void shouldRejectInfiniteAmounts() {
            InputValidation.ValidationResult positiveInfinity = InputValidation.validateAmount(Double.POSITIVE_INFINITY);
            InputValidation.ValidationResult negativeInfinity = InputValidation.validateAmount(Double.NEGATIVE_INFINITY);

            assertThat(positiveInfinity.isValid()).isFalse();
            assertThat(positiveInfinity.getError()).contains("Ungültiger Betrag");

            assertThat(negativeInfinity.isValid()).isFalse();
            assertThat(negativeInfinity.getError()).contains("Ungültiger Betrag");
        }

        @Test
        @DisplayName("SECURITY: Should reject amounts exceeding MAX_AMOUNT")
        void shouldRejectAmountsExceedingMaxAmount() {
            double tooLarge = InputValidation.MAX_AMOUNT + 1.0;
            InputValidation.ValidationResult result = InputValidation.validateAmount(tooLarge);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("zu groß");
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, 1.0, 100.5, 1000.99, 999999.99})
        @DisplayName("Should allow valid amounts")
        void shouldAllowValidAmounts(double amount) {
            InputValidation.ValidationResult result = InputValidation.validateAmount(amount);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Should allow MAX_AMOUNT exactly")
        void shouldAllowMaxAmountExactly() {
            InputValidation.ValidationResult result = InputValidation.validateAmount(InputValidation.MAX_AMOUNT);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("validatePrice should work like validateAmount")
        void validatePriceShouldWorkLikeValidateAmount() {
            InputValidation.ValidationResult validPrice = InputValidation.validatePrice(100.5);
            InputValidation.ValidationResult invalidPrice = InputValidation.validatePrice(-10.0);

            assertThat(validPrice.isValid()).isTrue();
            assertThat(invalidPrice.isValid()).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PERCENTAGE VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Percentage Validation Tests")
    class PercentageValidationTests {

        @Test
        @DisplayName("Should reject negative percentages")
        void shouldRejectNegativePercentages() {
            InputValidation.ValidationResult result = InputValidation.validatePercentage(-1);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("nicht negativ");
        }

        @Test
        @DisplayName("Should reject percentages greater than 100")
        void shouldRejectPercentagesGreaterThan100() {
            InputValidation.ValidationResult result = InputValidation.validatePercentage(101);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("nicht größer als 100");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 25, 50, 75, 100})
        @DisplayName("Should allow valid percentages (0-100)")
        void shouldAllowValidPercentages(int percentage) {
            InputValidation.ValidationResult result = InputValidation.validatePercentage(percentage);

            assertThat(result.isValid()).isTrue();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BLOCK POSITION VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("BlockPos Validation Tests")
    class BlockPosValidationTests {

        @Test
        @DisplayName("Should reject null BlockPos")
        void shouldRejectNullBlockPos() {
            InputValidation.Result result = InputValidation.validateBlockPos(null);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("nicht null");
        }

        @Test
        @DisplayName("SECURITY: Should reject Y coordinate below MIN_Y")
        void shouldRejectYBelowMinY() {
            BlockPos pos = new BlockPos(0, InputValidation.MIN_Y - 1, 0);
            InputValidation.Result result = InputValidation.validateBlockPos(pos);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("Y-Koordinate");
        }

        @Test
        @DisplayName("SECURITY: Should reject Y coordinate above MAX_Y")
        void shouldRejectYAboveMaxY() {
            BlockPos pos = new BlockPos(0, InputValidation.MAX_Y + 1, 0);
            InputValidation.Result result = InputValidation.validateBlockPos(pos);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("Y-Koordinate");
        }

        @Test
        @DisplayName("SECURITY: Should reject X coordinate beyond MAX_COORDINATE")
        void shouldRejectXBeyondMaxCoordinate() {
            BlockPos pos = new BlockPos(InputValidation.MAX_COORDINATE + 1, 64, 0);
            InputValidation.Result result = InputValidation.validateBlockPos(pos);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("Weltgrenzen");
        }

        @Test
        @DisplayName("SECURITY: Should reject Z coordinate beyond MAX_COORDINATE")
        void shouldRejectZBeyondMaxCoordinate() {
            BlockPos pos = new BlockPos(0, 64, InputValidation.MAX_COORDINATE + 1);
            InputValidation.Result result = InputValidation.validateBlockPos(pos);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("Weltgrenzen");
        }

        @Test
        @DisplayName("Should allow valid BlockPos within boundaries")
        void shouldAllowValidBlockPos() {
            BlockPos pos = new BlockPos(100, 64, 200);
            InputValidation.Result result = InputValidation.validateBlockPos(pos);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Should allow BlockPos at exact boundaries")
        void shouldAllowBlockPosAtExactBoundaries() {
            BlockPos minY = new BlockPos(0, InputValidation.MIN_Y, 0);
            BlockPos maxY = new BlockPos(0, InputValidation.MAX_Y, 0);
            BlockPos maxX = new BlockPos(InputValidation.MAX_COORDINATE, 64, 0);
            BlockPos maxZ = new BlockPos(0, 64, InputValidation.MAX_COORDINATE);

            assertThat(InputValidation.validateBlockPos(minY).isValid()).isTrue();
            assertThat(InputValidation.validateBlockPos(maxY).isValid()).isTrue();
            assertThat(InputValidation.validateBlockPos(maxX).isValid()).isTrue();
            assertThat(InputValidation.validateBlockPos(maxZ).isValid()).isTrue();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PLOT REGION VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Plot Region Validation Tests")
    class PlotRegionValidationTests {

        @Test
        @DisplayName("Should reject null positions")
        void shouldRejectNullPositions() {
            BlockPos valid = new BlockPos(0, 64, 0);

            InputValidation.Result result1 = InputValidation.validatePlotRegion(null, valid);
            InputValidation.Result result2 = InputValidation.validatePlotRegion(valid, null);

            assertThat(result1.isValid()).isFalse();
            assertThat(result2.isValid()).isFalse();
        }

        @Test
        @DisplayName("SECURITY: Should reject regions exceeding X size limit (DoS prevention)")
        void shouldRejectRegionsExceedingXSizeLimit() {
            BlockPos pos1 = new BlockPos(0, 64, 0);
            BlockPos pos2 = new BlockPos(10001, 64, 0);

            InputValidation.Result result = InputValidation.validatePlotRegion(pos1, pos2);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("zu groß");
        }

        @Test
        @DisplayName("SECURITY: Should reject regions exceeding Z size limit (DoS prevention)")
        void shouldRejectRegionsExceedingZSizeLimit() {
            BlockPos pos1 = new BlockPos(0, 64, 0);
            BlockPos pos2 = new BlockPos(0, 64, 10001);

            InputValidation.Result result = InputValidation.validatePlotRegion(pos1, pos2);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("zu groß");
        }

        @Test
        @DisplayName("Should allow valid plot regions within size limits")
        void shouldAllowValidPlotRegions() {
            BlockPos pos1 = new BlockPos(0, 64, 0);
            BlockPos pos2 = new BlockPos(100, 128, 150);

            InputValidation.Result result = InputValidation.validatePlotRegion(pos1, pos2);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Should allow region at exact size limit (10000x10000)")
        void shouldAllowRegionAtExactSizeLimit() {
            BlockPos pos1 = new BlockPos(0, 64, 0);
            BlockPos pos2 = new BlockPos(10000, 64, 10000);

            InputValidation.Result result = InputValidation.validatePlotRegion(pos1, pos2);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Should calculate region size correctly regardless of position order")
        void shouldCalculateRegionSizeRegardlessOfOrder() {
            BlockPos pos1 = new BlockPos(100, 64, 100);
            BlockPos pos2 = new BlockPos(0, 64, 0);

            InputValidation.Result result = InputValidation.validatePlotRegion(pos1, pos2);

            assertThat(result.isValid()).isTrue();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PACKET STRING VALIDATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Packet String Validation Tests")
    class PacketStringValidationTests {

        @Test
        @DisplayName("Should allow null packet strings")
        void shouldAllowNullPacketStrings() {
            InputValidation.Result result = InputValidation.validatePacketString(null, "TestField");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEmpty();
        }

        @Test
        @DisplayName("SECURITY: Should reject too long packet strings (DoS prevention)")
        void shouldRejectTooLongPacketStrings() {
            String tooLong = "a".repeat(InputValidation.MAX_PACKET_STRING_LENGTH + 1);
            InputValidation.Result result = InputValidation.validatePacketString(tooLong, "TestField");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getError()).contains("TestField");
            assertThat(result.getError()).contains("zu lang");
        }

        @Test
        @DisplayName("Should allow valid packet strings")
        void shouldAllowValidPacketStrings() {
            String valid = "This is a valid packet string.";
            InputValidation.Result result = InputValidation.validatePacketString(valid, "TestField");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEqualTo(valid);
        }

        @Test
        @DisplayName("Should allow packet string at exact length limit")
        void shouldAllowPacketStringAtExactLengthLimit() {
            String atLimit = "a".repeat(InputValidation.MAX_PACKET_STRING_LENGTH);
            InputValidation.Result result = InputValidation.validatePacketString(atLimit, "TestField");

            assertThat(result.isValid()).isTrue();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SANITIZE TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Sanitize Tests")
    class SanitizeTests {

        @Test
        @DisplayName("Should return empty string for null input")
        void shouldReturnEmptyStringForNull() {
            String result = InputValidation.sanitize(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should trim whitespace")
        void shouldTrimWhitespace() {
            String result = InputValidation.sanitize("  test  ");

            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("SECURITY: Should remove formatting codes")
        void shouldRemoveFormattingCodes() {
            String input = "§kObfuscated §lBold §mStrike §nUnderline §oItalic §rReset Text";
            String result = InputValidation.sanitize(input);

            assertThat(result).doesNotContain("§k", "§l", "§m", "§n", "§o", "§r");
        }

        @Test
        @DisplayName("SECURITY: Should remove control characters")
        void shouldRemoveControlCharacters() {
            String input = "Test\u0001\u0002\u0003Text";
            String result = InputValidation.sanitize(input);

            assertThat(result).isEqualTo("TestText");
        }

        @Test
        @DisplayName("Should preserve normal characters")
        void shouldPreserveNormalCharacters() {
            String input = "Normal text with 123 and äöü!";
            String result = InputValidation.sanitize(input);

            assertThat(result).isEqualTo(input);
        }

        @Test
        @DisplayName("Should allow newlines, tabs, and carriage returns")
        void shouldAllowWhitespaceCharacters() {
            String input = "Line1\nLine2\tTabbed\rReturn";
            String result = InputValidation.sanitize(input);

            // Trimming will remove trailing whitespace, but internal should be preserved
            assertThat(result).contains("\n", "\t");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // RESULT AND VALIDATION RESULT TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Result and ValidationResult Tests")
    class ResultTests {

        @Test
        @DisplayName("Result.success() should create valid result")
        void resultSuccessShouldCreateValidResult() {
            InputValidation.Result result = InputValidation.Result.success();

            assertThat(result.isValid()).isTrue();
            assertThat(result.isFailure()).isFalse();
            assertThat(result.getError()).isNull();
            assertThat(result.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("Result.success(value) should store sanitized value")
        void resultSuccessWithValueShouldStoreSanitizedValue() {
            InputValidation.Result result = InputValidation.Result.success("sanitized");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEqualTo("sanitized");
        }

        @Test
        @DisplayName("Result.failure() should create invalid result with error")
        void resultFailureShouldCreateInvalidResult() {
            InputValidation.Result result = InputValidation.Result.failure("Error message");

            assertThat(result.isValid()).isFalse();
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo("Error message");
            assertThat(result.getErrorMessage()).isEqualTo("Error message");
        }

        @Test
        @DisplayName("ValidationResult should work like Result")
        void validationResultShouldWorkLikeResult() {
            InputValidation.ValidationResult success = InputValidation.ValidationResult.success();
            InputValidation.ValidationResult failure = InputValidation.ValidationResult.failure("Error");

            assertThat(success.isValid()).isTrue();
            assertThat(failure.isValid()).isFalse();
            assertThat(failure.getError()).isEqualTo("Error");
        }

        @Test
        @DisplayName("ValidationResult.from() should convert Result")
        void validationResultFromShouldConvertResult() {
            InputValidation.Result result = InputValidation.Result.failure("Test error");
            InputValidation.ValidationResult validationResult = InputValidation.ValidationResult.from(result);

            assertThat(validationResult.isValid()).isFalse();
            assertThat(validationResult.getError()).isEqualTo("Test error");
        }

        @Test
        @DisplayName("validateName() alias should work like validatePlotName()")
        void validateNameAliasShouldWorkLikeValidatePlotName() {
            String validName = "TestPlot";
            InputValidation.ValidationResult result = InputValidation.validateName(validName);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSanitizedValue()).isEqualTo(validName);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // EDGE CASE AND INTEGRATION TESTS
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("SECURITY: Should handle Unicode normalization attacks")
        void shouldHandleUnicodeNormalizationAttacks() {
            // Test with various Unicode normalization forms
            String name1 = "café";  // NFC form
            String name2 = "café";  // NFD form (e + combining acute)

            InputValidation.Result result1 = InputValidation.validateNPCName(name1);
            InputValidation.Result result2 = InputValidation.validateNPCName(name2);

            // Both should be valid as they contain allowed characters
            assertThat(result1.isValid()).isTrue();
            assertThat(result2.isValid()).isTrue();
        }

        @Test
        @DisplayName("SECURITY: Should reject multiple dangerous patterns combined")
        void shouldRejectMultipleDangerousPattersCombined() {
            String dangerous = "<script>/op admin'; DROP TABLE--</script>";
            InputValidation.Result result = InputValidation.validateNPCName(dangerous);

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("SECURITY: Should handle very long strings gracefully")
        void shouldHandleVeryLongStringsGracefully() {
            String veryLong = "a".repeat(100000);

            assertThatCode(() -> {
                InputValidation.validateNPCName(veryLong);
                InputValidation.validatePlotName(veryLong);
                InputValidation.validateDialogText(veryLong);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle international characters correctly")
        void shouldHandleInternationalCharactersCorrectly() {
            String international = "Café François Müller García";
            InputValidation.Result result = InputValidation.validateNPCName(international);

            assertThat(result.isValid()).isTrue();
        }

        @ParameterizedTest
        @CsvSource({
            "'', false",
            "' ', false",
            "'A', false",
            "'AB', true",
            "'ABC', true"
        })
        @DisplayName("NPC name length boundary testing")
        void npcNameLengthBoundaryTesting(String name, boolean shouldBeValid) {
            InputValidation.Result result = InputValidation.validateNPCName(name);

            assertThat(result.isValid()).isEqualTo(shouldBeValid);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITY CLASS INSTANTIATION TEST
    // ═══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable (utility class pattern)")
        void shouldNotBeInstantiable() {
            assertThatThrownBy(() -> {
                var constructor = InputValidation.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasCauseInstanceOf(UnsupportedOperationException.class)
              .hasMessageContaining("Utility class");
        }
    }
}
