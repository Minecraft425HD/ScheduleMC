package de.rolandsw.schedulemc.mapview.integration.minecraft;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public class MinecraftAccessor {
    public static ClientLevel getWorld() {
        return Minecraft.getInstance().level;
    }

    public static int xCoord() {
        return Mth.floor(Minecraft.getInstance().getCameraEntity().getX());
    }

    public static int zCoord() {
        return Mth.floor(Minecraft.getInstance().getCameraEntity().getZ());
    }

    public static int yCoord() {
        return Mth.floor(Minecraft.getInstance().getCameraEntity().getY());
    }

    public static double xCoordDouble() {
        return MapViewConstants.getMinecraft().screen != null && MapViewConstants.getMinecraft().screen.isPauseScreen() ? MapViewConstants.getMinecraft().getCameraEntity().getX() : MapViewConstants.getMinecraft().getCameraEntity().xo + (MapViewConstants.getMinecraft().getCameraEntity().getX() - MapViewConstants.getMinecraft().getCameraEntity().xo) * MapViewConstants.getMinecraft().getFrameTime();
    }

    public static double zCoordDouble() {
        return MapViewConstants.getMinecraft().screen != null && MapViewConstants.getMinecraft().screen.isPauseScreen() ? MapViewConstants.getMinecraft().getCameraEntity().getZ() : MapViewConstants.getMinecraft().getCameraEntity().zo + (MapViewConstants.getMinecraft().getCameraEntity().getZ() - MapViewConstants.getMinecraft().getCameraEntity().zo) * MapViewConstants.getMinecraft().getFrameTime();
    }

    public static double yCoordDouble() {
        return MapViewConstants.getMinecraft().screen != null && MapViewConstants.getMinecraft().screen.isPauseScreen() ? MapViewConstants.getMinecraft().getCameraEntity().getY() : MapViewConstants.getMinecraft().getCameraEntity().yo + (MapViewConstants.getMinecraft().getCameraEntity().getY() - MapViewConstants.getMinecraft().getCameraEntity().yo) * MapViewConstants.getMinecraft().getFrameTime();
    }

    public static float rotationYaw() {
        return MapViewConstants.getMinecraft().getCameraEntity().yRotO + (MapViewConstants.getMinecraft().getCameraEntity().getYRot() - MapViewConstants.getMinecraft().getCameraEntity().yRotO) * MapViewConstants.getMinecraft().getFrameTime();
    }
}
