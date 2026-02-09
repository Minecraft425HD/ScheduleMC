package de.rolandsw.schedulemc.mapview.config;

import de.rolandsw.schedulemc.mapview.config.MapOption;
import de.rolandsw.schedulemc.mapview.core.event.SubSettingsManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import net.minecraft.client.resources.language.I18n;

public class WorldMapConfiguration implements SubSettingsManager {
    protected int mapX;
    protected int mapZ;
    protected float zoom = 4.0F;
    private float minZoomPower = -1.0F;
    private float maxZoomPower = 4.0F;
    protected float minZoom = 0.5F;
    protected float maxZoom = 16.0F;
    protected int cacheSize = 500;
    protected boolean outputImages;

    @Override
    public void loadSettings(File settingsFile) {
        try (BufferedReader in = new BufferedReader(new FileReader(settingsFile))) {
            String sCurrentLine;
            while ((sCurrentLine = in.readLine()) != null) {
                String[] curLine = sCurrentLine.split(":");
                switch (curLine[0]) {
                    case "Worldmap Zoom" -> this.zoom = Float.parseFloat(curLine[1]);
                    case "Worldmap Minimum Zoom" -> this.minZoom = Float.parseFloat(curLine[1]);
                    case "Worldmap Maximum Zoom" -> this.maxZoom = Float.parseFloat(curLine[1]);
                    case "Worldmap Cache Size" -> this.cacheSize = Integer.parseInt(curLine[1]);
                    case "Output Images" -> this.outputImages = Boolean.parseBoolean(curLine[1]);
                }
            }
        } catch (IOException ignored) {}

        for (int power = -3; power <= 5; ++power) {
            if (Math.pow(2.0, power) == this.minZoom) {
                this.minZoomPower = power;
            }

            if (Math.pow(2.0, power) == this.maxZoom) {
                this.maxZoomPower = power;
            }
        }

        this.bindCacheSize();
        this.bindZoom();
    }

    @Override
    public void saveAll(PrintWriter out) {
        out.println("Worldmap Zoom:" + this.zoom);
        out.println("Worldmap Minimum Zoom:" + this.minZoom);
        out.println("Worldmap Maximum Zoom:" + this.maxZoom);
        out.println("Worldmap Cache Size:" + this.cacheSize);
    }

    @Override
    public String getKeyText(MapOption options) {
        return I18n.get(options.getName()) + ": ";
    }

    @Override
    public float getOptionFloatValue(MapOption options) {
        return 0.0F;
    }

    @Override
    public void setOptionFloatValue(MapOption options, float value) {
        // No configurable float options - zoom settings are fixed
    }

    public void setOptionValue(MapOption par1EnumOptions) {
        // No boolean options remain after waypoint removal
    }

    private void bindCacheSize() {
        int minCacheSize = (int) ((1600.0F / this.minZoom / 256.0F + 4.0F) * (1100.0F / this.minZoom / 256.0F + 3.0F) * 1.35F);
        this.cacheSize = Math.max(this.cacheSize, minCacheSize);
    }

    private void bindZoom() {
        this.zoom = Math.max(this.zoom, this.minZoom);
        this.zoom = Math.min(this.zoom, this.maxZoom);
    }

    // Public getters and setters for protected fields
    public int getMapX() {
        return mapX;
    }

    public void setMapX(int mapX) {
        this.mapX = mapX;
    }

    public int getMapZ() {
        return mapZ;
    }

    public void setMapZ(int mapZ) {
        this.mapZ = mapZ;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        this.bindZoom();
    }

    public float getMinZoom() {
        return minZoom;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
        this.bindCacheSize();
    }

    public boolean isOutputImages() {
        return outputImages;
    }

    public void setOutputImages(boolean outputImages) {
        this.outputImages = outputImages;
    }
}
