package de.rolandsw.schedulemc.achievement.network;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.achievement.client.ClientAchievementCache;
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
 * Packet zum Synchronisieren von Achievement-Daten
 * Server → Client
 */
public class SyncAchievementDataPacket {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<AchievementData> achievements;
    private final int totalAchievements;
    private final int unlockedCount;
    private final double totalEarned;

    public SyncAchievementDataPacket(List<AchievementData> achievements, int totalAchievements,
                                     int unlockedCount, double totalEarned) {
        this.achievements = achievements;
        this.totalAchievements = totalAchievements;
        this.unlockedCount = unlockedCount;
        this.totalEarned = totalEarned;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(achievements.size());
        for (AchievementData data : achievements) {
            data.encode(buf);
        }
        buf.writeInt(totalAchievements);
        buf.writeInt(unlockedCount);
        buf.writeDouble(totalEarned);
    }

    public static SyncAchievementDataPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        // SICHERHEIT: Ablehnen statt truncaten um Buffer-Korruption zu vermeiden
        if (size < 0 || size > 1000) {
            return new SyncAchievementDataPacket(new ArrayList<>(), buf.readInt(), buf.readInt(), buf.readDouble());
        }
        List<AchievementData> achievements = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            achievements.add(AchievementData.decode(buf));
        }
        int totalAchievements = buf.readInt();
        int unlockedCount = buf.readInt();
        double totalEarned = buf.readDouble();

        return new SyncAchievementDataPacket(achievements, totalAchievements, unlockedCount, totalEarned);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LOGGER.debug("SyncAchievementDataPacket received: {} achievements", totalAchievements);
            // Use DistExecutor to ensure client-only code is only run on client
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient());
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        LOGGER.debug("SyncAchievementDataPacket: Updating client cache with {} achievements", achievements.size());
        ClientAchievementCache.updateCache(achievements, totalAchievements, unlockedCount, totalEarned);
    }
}
