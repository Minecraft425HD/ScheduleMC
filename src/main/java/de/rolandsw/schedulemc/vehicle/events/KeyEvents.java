package de.rolandsw.schedulemc.vehicle.events;

import de.rolandsw.schedulemc.vehicle.Main;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.net.MessageVehicleGui;
import de.rolandsw.schedulemc.vehicle.net.MessageCenterVehicle;
import de.rolandsw.schedulemc.vehicle.net.MessageControlVehicle;
import de.rolandsw.schedulemc.vehicle.net.MessageStarting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.Minecraft;

@OnlyIn(Dist.CLIENT)
public class KeyEvents {

    private boolean wasStartPressed;
    private boolean wasGuiPressed;
    private boolean wasHornPressed;
    private boolean wasCenterPressed;

    public KeyEvents() {

    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();

        Player player = minecraft.player;

        if (player == null) {
            return;
        }

        Entity riding = player.getVehicle();

        if (!(riding instanceof EntityGenericVehicle)) {
            return;
        }

        EntityGenericVehicle vehicle = (EntityGenericVehicle) riding;

        if (player.equals(vehicle.getDriver())) {
            // Send control inputs to server
            Main.SIMPLE_CHANNEL.sendToServer(new MessageControlVehicle(
                Main.FORWARD_KEY.isDown(),
                Main.BACK_KEY.isDown(),
                Main.LEFT_KEY.isDown(),
                Main.RIGHT_KEY.isDown(),
                player
            ));

            if (Main.START_KEY.isDown()) {
                if (!wasStartPressed) {
                    Main.SIMPLE_CHANNEL.sendToServer(new MessageStarting(true, false, player));
                    wasStartPressed = true;
                }
            } else {
                if (wasStartPressed) {
                    Main.SIMPLE_CHANNEL.sendToServer(new MessageStarting(false, true, player));
                }
                wasStartPressed = false;
            }

            if (Main.HORN_KEY.isDown()) {
                if (!wasHornPressed) {
                    vehicle.getPhysicsComponent().onHornPressed(player);
                    wasHornPressed = true;
                }
            } else {
                wasHornPressed = false;
            }

            if (Main.CENTER_KEY.isDown()) {
                if (!wasCenterPressed) {
                    Main.SIMPLE_CHANNEL.sendToServer(new MessageCenterVehicle(player));
                    player.displayClientMessage(Component.translatable("message.center_vehicle"), true);
                    wasCenterPressed = true;
                }
            } else {
                wasCenterPressed = false;
            }
        }

        // Open vehicle GUI with I key - send network message to server
        if (Main.VEHICLE_GUI_KEY.isDown()) {
            if (!wasGuiPressed) {
                Main.SIMPLE_CHANNEL.sendToServer(new MessageVehicleGui(player));
                wasGuiPressed = true;
            }
        } else {
            wasGuiPressed = false;
        }

    }

}
