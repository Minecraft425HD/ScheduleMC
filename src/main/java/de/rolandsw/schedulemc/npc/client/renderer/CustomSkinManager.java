package de.rolandsw.schedulemc.npc.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import de.rolandsw.schedulemc.ScheduleMC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Verwaltet das dynamische Laden von benutzerdefinierten NPC-Skins
 */
@OnlyIn(Dist.CLIENT)
public class CustomSkinManager {

    private static final Map<String, ResourceLocation> loadedSkins = new HashMap<>();
    private static final ResourceLocation DEFAULT_SKIN =
        new ResourceLocation(ScheduleMC.MOD_ID, "textures/entity/npc/default.png");

    /**
     * Lädt einen benutzerdefinierten Skin aus dem assets/schedulemc/skins/ Ordner und registriert ihn als Textur
     *
     * @param skinFileName Der Dateiname des Skins (z.B. "my_skin.png")
     * @return Die ResourceLocation der geladenen Textur, oder DEFAULT_SKIN bei Fehler
     */
    public static ResourceLocation loadCustomSkin(String skinFileName) {
        // Prüfe, ob der Skin bereits geladen wurde
        if (loadedSkins.containsKey(skinFileName)) {
            return loadedSkins.get(skinFileName);
        }

        try {
            // Erstelle ResourceLocation für den Skin im Ressourcen-Ordner
            // Pfad: assets/schedulemc/skins/skinFileName
            ResourceLocation skinResourceLocation = new ResourceLocation(
                ScheduleMC.MOD_ID,
                "skins/" + skinFileName
            );

            var resourceManager = Minecraft.getInstance().getResourceManager();

            // Prüfe, ob die Ressource existiert
            var resourceOptional = resourceManager.getResource(skinResourceLocation);
            if (resourceOptional.isEmpty()) {
                ScheduleMC.LOGGER.warn("Custom skin resource not found: {}", skinResourceLocation);
                return DEFAULT_SKIN;
            }

            // Lade die PNG-Datei als NativeImage aus der Ressource
            try (InputStream inputStream = resourceOptional.get().open()) {
                NativeImage image = NativeImage.read(inputStream);

                // Erstelle eine DynamicTexture aus dem NativeImage
                DynamicTexture texture = new DynamicTexture(image);

                // Registriere die Textur im TextureManager
                ResourceLocation textureLocation = new ResourceLocation(
                    ScheduleMC.MOD_ID,
                    "dynamic_skins/" + skinFileName.replace(".png", "")
                );

                Minecraft.getInstance().getTextureManager().register(textureLocation, texture);

                // Speichere die ResourceLocation für zukünftige Verwendung
                loadedSkins.put(skinFileName, textureLocation);

                ScheduleMC.LOGGER.info("Successfully loaded custom skin: {} -> {}",
                    skinFileName, textureLocation);

                return textureLocation;
            }
        } catch (Exception e) {
            ScheduleMC.LOGGER.error("Failed to load custom skin: {}", skinFileName, e);
            return DEFAULT_SKIN;
        }
    }

    /**
     * Entlädt alle geladenen benutzerdefinierten Skins
     * (Sollte aufgerufen werden, wenn die Ressourcen neu geladen werden)
     */
    public static void unloadAllSkins() {
        for (Map.Entry<String, ResourceLocation> entry : loadedSkins.entrySet()) {
            try {
                Minecraft.getInstance().getTextureManager().release(entry.getValue());
                ScheduleMC.LOGGER.info("Unloaded custom skin: {}", entry.getKey());
            } catch (Exception e) {
                ScheduleMC.LOGGER.warn("Failed to unload skin: {}", entry.getKey(), e);
            }
        }
        loadedSkins.clear();
    }

    /**
     * Entlädt einen spezifischen Skin
     */
    public static void unloadSkin(String skinFileName) {
        ResourceLocation location = loadedSkins.remove(skinFileName);
        if (location != null) {
            try {
                Minecraft.getInstance().getTextureManager().release(location);
                ScheduleMC.LOGGER.info("Unloaded custom skin: {}", skinFileName);
            } catch (Exception e) {
                ScheduleMC.LOGGER.warn("Failed to unload skin: {}", skinFileName, e);
            }
        }
    }

    /**
     * Prüft, ob ein Skin bereits geladen wurde
     */
    public static boolean isSkinLoaded(String skinFileName) {
        return loadedSkins.containsKey(skinFileName);
    }
}
