package de.rolandsw.schedulemc.lightmap.util;

import de.rolandsw.schedulemc.lightmap.LightMapConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public class GameVariableAccessShim {
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
        return LightMapConstants.getMinecraft().screen != null && LightMapConstants.getMinecraft().screen.isPauseScreen() ? LightMapConstants.getMinecraft().getCameraEntity().getX() : LightMapConstants.getMinecraft().getCameraEntity().xo + (LightMapConstants.getMinecraft().getCameraEntity().getX() - LightMapConstants.getMinecraft().getCameraEntity().xo) * LightMapConstants.getMinecraft().getFrameTime();
    }

    public static double zCoordDouble() {
        return LightMapConstants.getMinecraft().screen != null && LightMapConstants.getMinecraft().screen.isPauseScreen() ? LightMapConstants.getMinecraft().getCameraEntity().getZ() : LightMapConstants.getMinecraft().getCameraEntity().zo + (LightMapConstants.getMinecraft().getCameraEntity().getZ() - LightMapConstants.getMinecraft().getCameraEntity().zo) * LightMapConstants.getMinecraft().getFrameTime();
    }

    public static double yCoordDouble() {
        return LightMapConstants.getMinecraft().screen != null && LightMapConstants.getMinecraft().screen.isPauseScreen() ? LightMapConstants.getMinecraft().getCameraEntity().getY() : LightMapConstants.getMinecraft().getCameraEntity().yo + (LightMapConstants.getMinecraft().getCameraEntity().getY() - LightMapConstants.getMinecraft().getCameraEntity().yo) * LightMapConstants.getMinecraft().getFrameTime();
    }

    public static float rotationYaw() {
        return LightMapConstants.getMinecraft().getCameraEntity().yRotO + (LightMapConstants.getMinecraft().getCameraEntity().getYRot() - LightMapConstants.getMinecraft().getCameraEntity().yRotO) * LightMapConstants.getMinecraft().getFrameTime();
    }
}
