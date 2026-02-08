package de.rolandsw.schedulemc.mapview.integration.minecraft;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class MinecraftAccessor {
    @Nullable
    public static ClientLevel getWorld() {
        return Minecraft.getInstance().level;
    }

    public static int xCoord() {
        Player player = Minecraft.getInstance().player;
        return player != null ? Mth.floor(player.getX()) : 0;
    }

    public static int zCoord() {
        Player player = Minecraft.getInstance().player;
        return player != null ? Mth.floor(player.getZ()) : 0;
    }

    public static int yCoord() {
        Player player = Minecraft.getInstance().player;
        return player != null ? Mth.floor(player.getY()) : 0;
    }

    public static double xCoordDouble() {
        var player = Minecraft.getInstance().player;
        if (player == null) return 0;
        return MapViewConstants.getMinecraft().screen != null && MapViewConstants.getMinecraft().screen.isPauseScreen()
            ? player.getX()
            : player.xo + (player.getX() - player.xo) * MapViewConstants.getMinecraft().getFrameTime();
    }

    public static double zCoordDouble() {
        var player = Minecraft.getInstance().player;
        if (player == null) return 0;
        return MapViewConstants.getMinecraft().screen != null && MapViewConstants.getMinecraft().screen.isPauseScreen()
            ? player.getZ()
            : player.zo + (player.getZ() - player.zo) * MapViewConstants.getMinecraft().getFrameTime();
    }

    public static double yCoordDouble() {
        var player = Minecraft.getInstance().player;
        if (player == null) return 0;
        return MapViewConstants.getMinecraft().screen != null && MapViewConstants.getMinecraft().screen.isPauseScreen()
            ? player.getY()
            : player.yo + (player.getY() - player.yo) * MapViewConstants.getMinecraft().getFrameTime();
    }

    public static float rotationYaw() {
        Entity camera = MapViewConstants.getMinecraft().getCameraEntity();
        if (camera == null) return 0;
        return camera.yRotO + (camera.getYRot() - camera.yRotO) * MapViewConstants.getMinecraft().getFrameTime();
    }
}
