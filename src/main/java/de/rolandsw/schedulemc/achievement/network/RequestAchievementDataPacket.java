package de.rolandsw.schedulemc.achievement.network;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.achievement.Achievement;
import de.rolandsw.schedulemc.achievement.AchievementManager;
import de.rolandsw.schedulemc.achievement.PlayerAchievements;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Supplier;

/**
 * Packet zum Anfordern von Achievement-Daten
 * Client → Server
 */
public class RequestAchievementDataPacket {
    private static final Logger LOGGER = LogUtils.getLogger();

    public RequestAchievementDataPacket() {
    }

    public void encode(FriendlyByteBuf buf) {
        // No data needed
    }

    public static RequestAchievementDataPacket decode(FriendlyByteBuf buf) {
        return new RequestAchievementDataPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                LOGGER.warn("RequestAchievementDataPacket: player is null");
                return;
            }

            AchievementManager manager = AchievementManager.getInstance(player.server);
            if (manager == null) {
                LOGGER.warn("RequestAchievementDataPacket: AchievementManager is null");
                return;
            }

            UUID playerUUID = player.getUUID();
            PlayerAchievements playerAch = manager.getPlayerAchievements(playerUUID);

            // Collect all achievement data
            List<AchievementData> achievementList = new ArrayList<>();
            for (Achievement ach : manager.getAllAchievements()) {
                double progress = playerAch.getProgress(ach.getId());
                boolean unlocked = playerAch.isUnlocked(ach.getId());

                achievementList.add(new AchievementData(
                    ach.getId(),
                    ach.getName(),
                    ach.getDescription(),
                    ach.getCategory().name(),
                    ach.getTier().name(),
                    ach.getRequirement(),
                    ach.isHidden(),
                    progress,
                    unlocked
                ));
            }

            int totalAchievements = manager.getAllAchievements().size();
            int unlockedCount = playerAch.getUnlockedCount();
            double totalEarned = playerAch.getTotalPointsEarned();

            LOGGER.info("Sending {} achievements to player {} (unlocked: {}, earned: {}€)",
                totalAchievements, player.getName().getString(), unlockedCount, totalEarned);

            // Send response
            SyncAchievementDataPacket response = new SyncAchievementDataPacket(
                achievementList,
                totalAchievements,
                unlockedCount,
                totalEarned
            );
            AchievementNetworkHandler.sendToPlayer(response, player);
        });
        ctx.get().setPacketHandled(true);
    }
}
