package de.rolandsw.schedulemc.gang.network;

import de.rolandsw.schedulemc.gang.GangPerk;
import de.rolandsw.schedulemc.gang.client.ClientGangCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

/**
 * Vollstaendige Gang-Daten fuer ein Mitglied (fuer die Gang-App).
 * Server -> Client
 */
public class SyncGangDataPacket {

    private final boolean hasGang;
    private final String gangName;
    private final String gangTag;
    private final int gangLevel;
    private final int gangXP;
    private final int gangBalance;
    private final int gangColorOrdinal;
    private final int memberCount;
    private final int maxMembers;
    private final int territoryCount;
    private final int maxTerritory;
    private final int availablePerkPoints;
    private final double gangProgress;

    // Mitglieder-Liste: Name, Rang, beigetragene XP
    private final List<GangMemberInfo> members;
    // Freigeschaltete Perks
    private final Set<String> unlockedPerks;
    // Aktive Missionen
    private final List<MissionInfo> missions;

    public SyncGangDataPacket(boolean hasGang, String gangName, String gangTag,
                              int gangLevel, int gangXP, int gangBalance,
                              int gangColorOrdinal, int memberCount, int maxMembers,
                              int territoryCount, int maxTerritory, int availablePerkPoints,
                              double gangProgress, List<GangMemberInfo> members,
                              Set<String> unlockedPerks, List<MissionInfo> missions) {
        this.hasGang = hasGang;
        this.gangName = gangName;
        this.gangTag = gangTag;
        this.gangLevel = gangLevel;
        this.gangXP = gangXP;
        this.gangBalance = gangBalance;
        this.gangColorOrdinal = gangColorOrdinal;
        this.memberCount = memberCount;
        this.maxMembers = maxMembers;
        this.territoryCount = territoryCount;
        this.maxTerritory = maxTerritory;
        this.availablePerkPoints = availablePerkPoints;
        this.gangProgress = gangProgress;
        this.members = members;
        this.unlockedPerks = unlockedPerks;
        this.missions = missions;
    }

    /** Kein Gang */
    public static SyncGangDataPacket noGang() {
        return new SyncGangDataPacket(false, "", "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                List.of(), Set.of(), List.of());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(hasGang);
        if (!hasGang) return;

        buf.writeUtf(gangName, 64);
        buf.writeUtf(gangTag, 10);
        buf.writeInt(gangLevel);
        buf.writeInt(gangXP);
        buf.writeInt(gangBalance);
        buf.writeInt(gangColorOrdinal);
        buf.writeInt(memberCount);
        buf.writeInt(maxMembers);
        buf.writeInt(territoryCount);
        buf.writeInt(maxTerritory);
        buf.writeInt(availablePerkPoints);
        buf.writeDouble(gangProgress);

        // Members
        buf.writeInt(members.size());
        for (GangMemberInfo m : members) {
            buf.writeUtf(m.name, 32);
            buf.writeUtf(m.rank, 16);
            buf.writeUtf(m.rankColor, 8);
            buf.writeInt(m.contributedXP);
            buf.writeBoolean(m.online);
        }

        // Perks
        buf.writeInt(unlockedPerks.size());
        for (String perk : unlockedPerks) {
            buf.writeUtf(perk, 64);
        }

        // Missions
        buf.writeInt(missions.size());
        for (MissionInfo mi : missions) {
            buf.writeUtf(mi.description, 128);
            buf.writeInt(mi.currentProgress);
            buf.writeInt(mi.targetAmount);
            buf.writeInt(mi.xpReward);
            buf.writeInt(mi.moneyReward);
            buf.writeBoolean(mi.completed);
        }
    }

    public static SyncGangDataPacket decode(FriendlyByteBuf buf) {
        boolean hasGang = buf.readBoolean();
        if (!hasGang) return noGang();

        String name = buf.readUtf(64);
        String tag = buf.readUtf(10);
        int level = buf.readInt();
        int xp = buf.readInt();
        int balance = buf.readInt();
        int colorOrd = buf.readInt();
        int members = buf.readInt();
        int maxMembers = buf.readInt();
        int territory = buf.readInt();
        int maxTerritory = buf.readInt();
        int perkPoints = buf.readInt();
        double progress = buf.readDouble();

        // Members
        int memberCount = Math.min(buf.readInt(), 50);
        List<GangMemberInfo> memberList = new ArrayList<>(memberCount);
        for (int i = 0; i < memberCount; i++) {
            memberList.add(new GangMemberInfo(
                    buf.readUtf(32), buf.readUtf(16), buf.readUtf(8),
                    buf.readInt(), buf.readBoolean()));
        }

        // Perks
        int perkCount = Math.min(buf.readInt(), 30);
        Set<String> perks = new HashSet<>();
        for (int i = 0; i < perkCount; i++) {
            perks.add(buf.readUtf(64));
        }

        // Missions
        int missionCount = Math.min(buf.readInt(), 10);
        List<MissionInfo> missionList = new ArrayList<>(missionCount);
        for (int i = 0; i < missionCount; i++) {
            missionList.add(new MissionInfo(
                    buf.readUtf(128), buf.readInt(), buf.readInt(),
                    buf.readInt(), buf.readInt(), buf.readBoolean()));
        }

        return new SyncGangDataPacket(true, name, tag, level, xp, balance, colorOrd,
                members, maxMembers, territory, maxTerritory, perkPoints, progress,
                memberList, perks, missionList);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient());
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        ClientGangCache.updateGangData(this);
    }

    // Getters
    public boolean hasGang() { return hasGang; }
    public String getGangName() { return gangName; }
    public String getGangTag() { return gangTag; }
    public int getGangLevel() { return gangLevel; }
    public int getGangXP() { return gangXP; }
    public int getGangBalance() { return gangBalance; }
    public int getGangColorOrdinal() { return gangColorOrdinal; }
    public int getMemberCount() { return memberCount; }
    public int getMaxMembers() { return maxMembers; }
    public int getTerritoryCount() { return territoryCount; }
    public int getMaxTerritory() { return maxTerritory; }
    public int getAvailablePerkPoints() { return availablePerkPoints; }
    public double getGangProgress() { return gangProgress; }
    public List<GangMemberInfo> getMembers() { return members; }
    public Set<String> getUnlockedPerks() { return unlockedPerks; }
    public List<MissionInfo> getMissions() { return missions; }

    public boolean hasPerk(GangPerk perk) {
        return unlockedPerks.contains(perk.name());
    }

    /**
     * Mitglieder-Info fuer die Client-Anzeige
     */
    public record GangMemberInfo(String name, String rank, String rankColor,
                                  int contributedXP, boolean online) {}

    /**
     * Missions-Info fuer die Client-Anzeige
     */
    public record MissionInfo(String description, int currentProgress, int targetAmount,
                               int xpReward, int moneyReward, boolean completed) {
        public double getProgressPercent() {
            return targetAmount > 0 ? (double) currentProgress / targetAmount : 0;
        }
    }
}
