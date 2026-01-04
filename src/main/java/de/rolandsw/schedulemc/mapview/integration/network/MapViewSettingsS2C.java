package de.rolandsw.schedulemc.mapview.integration.network;

import com.google.gson.Gson;
import de.rolandsw.schedulemc.mapview.MapViewConstants;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record MapViewSettingsS2C(String settingsJson) {
    public static final ResourceLocation PACKET_ID = ResourceLocation.fromNamespaceAndPath("schedulemc", "lightmap_settings");

    public MapViewSettingsS2C(FriendlyByteBuf buf) {
        this(parse(buf));
    }

    /**
     * SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
     */
    private static String parse(FriendlyByteBuf buf) {
        buf.readByte(); // ignore
        return buf.readUtf(32767); // JSON settings max 32KB
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeByte(0);
        buf.writeUtf(settingsJson);
    }


    public static void parsePacket(MapViewSettingsS2C packet) {
        @SuppressWarnings("unchecked")
        Map<String, Object> settings = new Gson().fromJson(packet.settingsJson(), Map.class);
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            String setting = entry.getKey();
            Object value = entry.getValue();
            switch (setting) {
                case "worldName" -> {
                    if (value instanceof String worldName) {
                        Minecraft.getInstance().execute(() -> {
                            MapViewConstants.getLogger().info("Received world name from settings: " + worldName);
                            MapViewConstants.getLightMapInstance().newSubWorldName(worldName, true);
                        });
                    } else {
                        MapViewConstants.getLogger().warn("Invalid world name: " + value);
                    }
                }
                case "minimapAllowed" -> MapViewConstants.getLightMapInstance().getMapOptions().minimapAllowed = (Boolean) value;
                case "worldmapAllowed" -> MapViewConstants.getLightMapInstance().getMapOptions().worldmapAllowed = (Boolean) value;
                case "teleportCommand" -> MapViewConstants.getLightMapInstance().getMapOptions().serverTeleportCommand = (String) value;
                default -> MapViewConstants.getLogger().warn("Unknown configuration option " + setting);
            }
        }
    }
}
