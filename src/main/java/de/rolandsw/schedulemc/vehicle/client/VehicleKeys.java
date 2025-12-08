package de.rolandsw.schedulemc.vehicle.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Key bindings for vehicle controls.
 */
public class VehicleKeys {

    public static final String CATEGORY = "key.categories.vehicle";

    public static final KeyMapping FORWARD = new KeyMapping(
            "key.vehicle.forward",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_W,
            CATEGORY
    );

    public static final KeyMapping BACKWARD = new KeyMapping(
            "key.vehicle.backward",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_S,
            CATEGORY
    );

    public static final KeyMapping LEFT = new KeyMapping(
            "key.vehicle.left",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_A,
            CATEGORY
    );

    public static final KeyMapping RIGHT = new KeyMapping(
            "key.vehicle.right",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_D,
            CATEGORY
    );

    public static final KeyMapping START_ENGINE = new KeyMapping(
            "key.vehicle.start_engine",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );

    public static final KeyMapping HORN = new KeyMapping(
            "key.vehicle.horn",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
    );

    public static final KeyMapping BRAKE = new KeyMapping(
            "key.vehicle.brake",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SPACE,
            CATEGORY
    );

    public static final KeyMapping INVENTORY = new KeyMapping(
            "key.vehicle.inventory",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            CATEGORY
    );
}
