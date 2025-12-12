package de.rolandsw.schedulemc.car.events;

import de.rolandsw.schedulemc.car.Main;
import de.rolandsw.schedulemc.car.entity.car.base.EntityGenericCar;
import de.rolandsw.schedulemc.car.net.MessageCenterCar;
import de.rolandsw.schedulemc.car.net.MessageStarting;
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

        if (!(riding instanceof EntityGenericCar)) {
            return;
        }

        EntityGenericCar car = (EntityGenericCar) riding;

        if (player.equals(car.getDriver())) {
            car.updateControls(Main.FORWARD_KEY.isDown(), Main.BACK_KEY.isDown(), Main.LEFT_KEY.isDown(), Main.RIGHT_KEY.isDown(), player);

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
                    car.getPhysicsComponent().onHornPressed(player);
                    wasHornPressed = true;
                }
            } else {
                wasHornPressed = false;
            }

            if (Main.CENTER_KEY.isDown()) {
                if (!wasCenterPressed) {
                    Main.SIMPLE_CHANNEL.sendToServer(new MessageCenterCar(player));
                    player.displayClientMessage(Component.translatable("message.center_car"), true);
                    wasCenterPressed = true;
                }
            } else {
                wasCenterPressed = false;
            }
        }

        // Open car GUI only when holding Shift + I (to not conflict with normal inventory)
        if (Main.CAR_GUI_KEY.isDown() && player.isShiftKeyDown()) {
            if (!wasGuiPressed) {
                car.openCarGUI(player);
                wasGuiPressed = true;
            }
        } else {
            wasGuiPressed = false;
        }

    }

}
