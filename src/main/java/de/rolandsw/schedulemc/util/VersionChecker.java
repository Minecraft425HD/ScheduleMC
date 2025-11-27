package de.rolandsw.schedulemc.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * Prüft auf neue Versionen im GitHub Repository
 */
public class VersionChecker {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/Minecraft425HD/ScheduleMC/releases/latest";
    private static String CURRENT_VERSION = null;

    private static String latestVersion = null;
    private static String downloadUrl = null;
    private static boolean updateAvailable = false;
    private static boolean checkInProgress = false;

    /**
     * Prüft asynchron auf Updates
     */
    public static void checkForUpdates() {
        if (checkInProgress) {
            return;
        }

        checkInProgress = true;
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(GITHUB_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    parseResponse(response.toString());
                } else {
                    ScheduleMC.LOGGER.warn("Version check failed with response code: " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                ScheduleMC.LOGGER.error("Error checking for updates", e);
            } finally {
                checkInProgress = false;
            }
        });
    }

    private static void parseResponse(String jsonResponse) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            String tagName = jsonObject.get("tag_name").getAsString();

            // Entferne 'v' Prefix falls vorhanden
            latestVersion = tagName.startsWith("v") ? tagName.substring(1) : tagName;

            // Hole Download-URL
            if (jsonObject.has("html_url")) {
                downloadUrl = jsonObject.get("html_url").getAsString();
            }

            // Vergleiche Versionen
            updateAvailable = isNewerVersion(latestVersion, getCurrentVersion());

            if (updateAvailable) {
                ScheduleMC.LOGGER.info("Update available! Current: " + getCurrentVersion() + ", Latest: " + latestVersion);
            } else {
                ScheduleMC.LOGGER.info("Running latest version: " + getCurrentVersion());
            }
        } catch (Exception e) {
            ScheduleMC.LOGGER.error("Error parsing version response", e);
        }
    }

    /**
     * Vergleicht zwei Versionen im Format x.y.z
     */
    private static boolean isNewerVersion(String latest, String current) {
        try {
            String[] latestParts = latest.split("\\.");
            String[] currentParts = current.split("\\.");

            int length = Math.max(latestParts.length, currentParts.length);

            for (int i = 0; i < length; i++) {
                int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;

                if (latestPart > currentPart) {
                    return true;
                } else if (latestPart < currentPart) {
                    return false;
                }
            }

            return false;
        } catch (Exception e) {
            ScheduleMC.LOGGER.error("Error comparing versions", e);
            return false;
        }
    }

    public static boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public static String getLatestVersion() {
        return latestVersion != null ? latestVersion : CURRENT_VERSION;
    }

    public static String getDownloadUrl() {
        return downloadUrl != null ? downloadUrl : "https://github.com/Minecraft425HD/ScheduleMC/releases";
    }

    public static String getCurrentVersion() {
        if (CURRENT_VERSION == null) {
            try {
                ModContainer container = ModList.get().getModContainerById(ScheduleMC.MOD_ID).orElse(null);
                if (container != null) {
                    CURRENT_VERSION = container.getModInfo().getVersion().toString();
                } else {
                    CURRENT_VERSION = "1.0.0-alpha"; // Fallback
                }
            } catch (Exception e) {
                ScheduleMC.LOGGER.error("Error getting mod version", e);
                CURRENT_VERSION = "1.0.0-alpha"; // Fallback
            }
        }
        return CURRENT_VERSION;
    }
}
