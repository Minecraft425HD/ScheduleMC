package de.rolandsw.schedulemc.lightmap.packets;

import com.google.gson.Gson;
import de.rolandsw.schedulemc.lightmap.LightMapConstants;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record LightMapSettingsS2C(String settingsJson) {
    public static final ResourceLocation PACKET_ID = new ResourceLocation("schedulemc", "lightmap_settings");

    public LightMapSettingsS2C(FriendlyByteBuf buf) {
        this(parse(buf));
    }

    private static String parse(FriendlyByteBuf buf) {
        buf.readByte(); // ignore
        return buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeByte(0);
        buf.writeUtf(settingsJson);
    }


    public static void parsePacket(LightMapSettingsS2C packet) {
        @SuppressWarnings("unchecked")
        Map<String, Object> settings = new Gson().fromJson(packet.settingsJson(), Map.class);
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            String setting = entry.getKey();
            Object value = entry.getValue();
            switch (setting) {
                case "worldName" -> {
                    if (value instanceof String worldName) {
                        Minecraft.getInstance().execute(() -> {
                            LightMapConstants.getLogger().info("Received world name from settings: " + worldName);
                            LightMapConstants.getLightMapInstance().newSubWorldName(worldName, true);
                        });
                    } else {
                        LightMapConstants.getLogger().warn("Invalid world name: " + value);
                    }
                }
                case "minimapAllowed" -> LightMapConstants.getLightMapInstance().getMapOptions().minimapAllowed = (Boolean) value;
                case "worldmapAllowed" -> LightMapConstants.getLightMapInstance().getMapOptions().worldmapAllowed = (Boolean) value;
                case "teleportCommand" -> LightMapConstants.getLightMapInstance().getMapOptions().serverTeleportCommand = (String) value;
                default -> LightMapConstants.getLogger().warn("Unknown configuration option " + setting);
            }
        }
    }
}
