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
        return Mth.floor(Minecraft.getInstance().player.getX());
    }

    public static int zCoord() {
        return Mth.floor(Minecraft.getInstance().player.getZ());
    }

    public static int yCoord() {
        return Mth.floor(Minecraft.getInstance().player.getY());
    }

    public static double xCoordDouble() {
        var player = Minecraft.getInstance().player;
        return MapViewConstants.getMinecraft().screen != null && MapViewConstants.getMinecraft().screen.isPauseScreen()
            ? player.getX()
            : player.xo + (player.getX() - player.xo) * MapViewConstants.getMinecraft().getFrameTime();
    }

    public static double zCoordDouble() {
        var player = Minecraft.getInstance().player;
        return MapViewConstants.getMinecraft().screen != null && MapViewConstants.getMinecraft().screen.isPauseScreen()
            ? player.getZ()
            : player.zo + (player.getZ() - player.zo) * MapViewConstants.getMinecraft().getFrameTime();
    }

    public static double yCoordDouble() {
        var player = Minecraft.getInstance().player;
        return MapViewConstants.getMinecraft().screen != null && MapViewConstants.getMinecraft().screen.isPauseScreen()
            ? player.getY()
            : player.yo + (player.getY() - player.yo) * MapViewConstants.getMinecraft().getFrameTime();
    }

    public static float rotationYaw() {
        return MapViewConstants.getMinecraft().getCameraEntity().yRotO + (MapViewConstants.getMinecraft().getCameraEntity().getYRot() - MapViewConstants.getMinecraft().getCameraEntity().yRotO) * MapViewConstants.getMinecraft().getFrameTime();
    }
}
