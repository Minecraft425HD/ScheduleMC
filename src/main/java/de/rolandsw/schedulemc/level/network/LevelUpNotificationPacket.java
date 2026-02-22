package de.rolandsw.schedulemc.level.network;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.messaging.MessageNotificationOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Packet fuer Level-Up-Benachrichtigungen.
 * Server -> Client
 *
 * Zeigt eine Smartphone-Notification ueber MessageNotificationOverlay an.
 */
public class LevelUpNotificationPacket {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final int newLevel;
    private final List<String> newUnlockNames;

    public LevelUpNotificationPacket(int newLevel, List<String> newUnlockNames) {
        this.newLevel = newLevel;
        this.newUnlockNames = newUnlockNames;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(newLevel);
        buf.writeInt(newUnlockNames.size());
        for (String name : newUnlockNames) {
            buf.writeUtf(name);
        }
    }

    public static LevelUpNotificationPacket decode(FriendlyByteBuf buf) {
        int newLevel = buf.readInt();
        int size = buf.readInt();
        // SICHERHEIT: Ablehnen statt truncaten um Buffer-Korruption zu vermeiden
        if (size < 0 || size > 50) {
            return new LevelUpNotificationPacket(newLevel, new ArrayList<>());
        }
        List<String> names = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            names.add(buf.readUtf(256));
        }
        return new LevelUpNotificationPacket(newLevel, names);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient());
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        // Zeige Level-Up Notification
        String title = "\u2605 Level " + newLevel + " erreicht!";
        String message;
        if (newUnlockNames.isEmpty()) {
            message = "Weiter so!";
        } else if (newUnlockNames.size() == 1) {
            message = "Neu: " + newUnlockNames.get(0);
        } else {
            message = newUnlockNames.size() + " neue Inhalte freigeschaltet!";
        }

        MessageNotificationOverlay.showNotification(title, message);
        LOGGER.info("Level-Up Notification: Level {} - {} new unlocks", newLevel, newUnlockNames.size());
    }
}
