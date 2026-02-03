package de.rolandsw.schedulemc.level.network;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.level.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet zum Anfordern von ProducerLevel-Daten.
 * Client -> Server
 */
public class RequestProducerLevelDataPacket {
    private static final Logger LOGGER = LogUtils.getLogger();

    public RequestProducerLevelDataPacket() {
    }

    public void encode(FriendlyByteBuf buf) {
        // Keine Daten noetig
    }

    public static RequestProducerLevelDataPacket decode(FriendlyByteBuf buf) {
        return new RequestProducerLevelDataPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                LOGGER.warn("RequestProducerLevelDataPacket: player is null");
                return;
            }

            UUID playerUUID = player.getUUID();
            ProducerLevel levelManager = ProducerLevel.getInstance();
            ProducerLevelData data = levelManager.getPlayerData(playerUUID);

            int currentLevel = data != null ? data.getLevel() : 0;
            int totalXP = data != null ? data.getTotalXP() : 0;
            int xpToNextLevel = data != null ? data.getXPToNextLevel() : LevelRequirements.getRequiredXP(1);
            double progress = data != null ? data.getProgress() : 0.0;

            // Alle Unlockables sammeln
            List<UnlockableData> unlockableList = new ArrayList<>();
            int unlockedCount = 0;

            for (Unlockable unlock : Unlockable.values()) {
                boolean unlocked = levelManager.isUnlocked(playerUUID, unlock);
                if (unlocked) unlockedCount++;

                unlockableList.add(new UnlockableData(
                        unlock.name(),
                        unlock.getDescription(),
                        unlock.getRequiredLevel(),
                        unlock.getCategory().ordinal(),
                        unlocked
                ));
            }

            int totalUnlockables = Unlockable.values().length;

            // Statistiken
            int totalItemsSold = data != null ? data.getTotalItemsSold() : 0;
            int totalIllegalSold = data != null ? data.getTotalIllegalSold() : 0;
            int totalLegalSold = data != null ? data.getTotalLegalSold() : 0;
            double totalRevenue = data != null ? data.getTotalRevenue() : 0.0;

            LOGGER.debug("Sending {} unlockables to player {} (level: {}, unlocked: {})",
                    totalUnlockables, player.getName().getString(), currentLevel, unlockedCount);

            SyncProducerLevelDataPacket response = new SyncProducerLevelDataPacket(
                    currentLevel, totalXP, xpToNextLevel, progress,
                    unlockedCount, totalUnlockables, unlockableList,
                    totalItemsSold, totalIllegalSold, totalLegalSold, totalRevenue
            );
            ProducerLevelNetworkHandler.sendToPlayer(response, player);
        });
        ctx.get().setPacketHandled(true);
    }
}
