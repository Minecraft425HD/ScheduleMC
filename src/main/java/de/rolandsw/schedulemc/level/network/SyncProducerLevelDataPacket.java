package de.rolandsw.schedulemc.level.network;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.level.client.ClientProducerLevelCache;
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
 * Packet zum Synchronisieren von ProducerLevel-Daten.
 * Server -> Client
 */
public class SyncProducerLevelDataPacket {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final int currentLevel;
    private final int totalXP;
    private final int xpToNextLevel;
    private final double progress;
    private final int totalUnlocked;
    private final int totalUnlockables;
    private final List<UnlockableData> unlockables;

    // Statistiken
    private final int totalItemsSold;
    private final int totalIllegalSold;
    private final int totalLegalSold;
    private final double totalRevenue;

    public SyncProducerLevelDataPacket(int currentLevel, int totalXP, int xpToNextLevel,
                                       double progress, int totalUnlocked, int totalUnlockables,
                                       List<UnlockableData> unlockables,
                                       int totalItemsSold, int totalIllegalSold,
                                       int totalLegalSold, double totalRevenue) {
        this.currentLevel = currentLevel;
        this.totalXP = totalXP;
        this.xpToNextLevel = xpToNextLevel;
        this.progress = progress;
        this.totalUnlocked = totalUnlocked;
        this.totalUnlockables = totalUnlockables;
        this.unlockables = unlockables;
        this.totalItemsSold = totalItemsSold;
        this.totalIllegalSold = totalIllegalSold;
        this.totalLegalSold = totalLegalSold;
        this.totalRevenue = totalRevenue;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(currentLevel);
        buf.writeInt(totalXP);
        buf.writeInt(xpToNextLevel);
        buf.writeDouble(progress);
        buf.writeInt(totalUnlocked);
        buf.writeInt(totalUnlockables);

        buf.writeInt(unlockables.size());
        for (UnlockableData data : unlockables) {
            data.encode(buf);
        }

        buf.writeInt(totalItemsSold);
        buf.writeInt(totalIllegalSold);
        buf.writeInt(totalLegalSold);
        buf.writeDouble(totalRevenue);
    }

    public static SyncProducerLevelDataPacket decode(FriendlyByteBuf buf) {
        int currentLevel = buf.readInt();
        int totalXP = buf.readInt();
        int xpToNextLevel = buf.readInt();
        double progress = buf.readDouble();
        int totalUnlocked = buf.readInt();
        int totalUnlockables = buf.readInt();

        int size = Math.min(buf.readInt(), 200);
        List<UnlockableData> unlockables = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            unlockables.add(UnlockableData.decode(buf));
        }

        int totalItemsSold = buf.readInt();
        int totalIllegalSold = buf.readInt();
        int totalLegalSold = buf.readInt();
        double totalRevenue = buf.readDouble();

        return new SyncProducerLevelDataPacket(
                currentLevel, totalXP, xpToNextLevel, progress,
                totalUnlocked, totalUnlockables, unlockables,
                totalItemsSold, totalIllegalSold, totalLegalSold, totalRevenue
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LOGGER.debug("SyncProducerLevelDataPacket received: level={}, unlockables={}",
                    currentLevel, unlockables.size());
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient());
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        ClientProducerLevelCache.updateCache(
                currentLevel, totalXP, xpToNextLevel, progress,
                totalUnlocked, totalUnlockables, unlockables,
                totalItemsSold, totalIllegalSold, totalLegalSold, totalRevenue
        );
    }
}
