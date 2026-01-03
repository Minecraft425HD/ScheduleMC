package de.rolandsw.schedulemc.achievement.network;

import de.rolandsw.schedulemc.achievement.Achievement;
import de.rolandsw.schedulemc.achievement.AchievementManager;
import de.rolandsw.schedulemc.achievement.PlayerAchievements;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

/**
 * Packet zum Anfordern von Achievement-Daten
 * Client → Server
 */
public class RequestAchievementDataPacket {

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
            if (player == null) return;

            AchievementManager manager = AchievementManager.getInstance(player.server);
            if (manager == null) return;

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
