package de.rolandsw.schedulemc.mapview.mixins;

import de.rolandsw.schedulemc.mapview.MapViewConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// @Mixin(ClientPacketListener.class)
public abstract class APIMixinNetHandlerPlayClient {
    // @Inject(method = "sendCommand(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true, require = 0)
    public void onSendCommand(String string, CallbackInfo cir) {
        if (lightmap$parseCommand(string)) {
            cir.cancel();
        }
    }

    // sendUnattendedCommand doesn't exist in 1.20.1 - commenting out
    // @Inject(method = "sendUnattendedCommand", at = @At("HEAD"), cancellable = true)
    // public void onUnsignedCommand(String string, Screen screen, CallbackInfo ci) {
    //     if (lightmap$parseCommand(string)) {
    //         ci.cancel();
    //     }
    // }


    @Unique
    private boolean lightmap$parseCommand(String command) {
        MapViewConstants.getLogger().info("Command: " + command);
        return !MapViewConstants.onSendChatMessage(command);
    }
}
