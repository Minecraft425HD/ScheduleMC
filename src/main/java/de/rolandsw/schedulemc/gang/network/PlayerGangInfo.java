package de.rolandsw.schedulemc.gang.network;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * Leichtgewichtige Gang+Level Info fuer einen Spieler.
 * Wird an ALLE Clients gesendet fuer Nametag- und TAB-Anzeige.
 */
public class PlayerGangInfo {

    private final UUID playerUUID;
    private final boolean inGang;
    private final String gangTag;      // z.B. "MAFIA"
    private final int gangColorOrdinal; // ChatFormatting ordinal
    private final int gangLevel;        // Gang-Level (fuer Sterne)
    private final String rankName;      // z.B. "Boss", "Member"
    private final String rankColorCode; // z.B. "§c"
    private final int playerLevel;      // ProducerLevel
    private final double playerProgress; // 0.0-1.0 ProducerLevel Progress

    public PlayerGangInfo(UUID playerUUID, boolean inGang, String gangTag,
                          int gangColorOrdinal, int gangLevel, String rankName,
                          String rankColorCode, int playerLevel, double playerProgress) {
        this.playerUUID = playerUUID;
        this.inGang = inGang;
        this.gangTag = gangTag;
        this.gangColorOrdinal = gangColorOrdinal;
        this.gangLevel = gangLevel;
        this.rankName = rankName;
        this.rankColorCode = rankColorCode;
        this.playerLevel = playerLevel;
        this.playerProgress = playerProgress;
    }

    /** Spieler ohne Gang */
    public static PlayerGangInfo noGang(UUID playerUUID, int playerLevel, double playerProgress) {
        return new PlayerGangInfo(playerUUID, false, "", 0, 0, "", "", playerLevel, playerProgress);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        buf.writeBoolean(inGang);
        if (inGang) {
            buf.writeUtf(gangTag, 10);
            buf.writeInt(gangColorOrdinal);
            buf.writeInt(gangLevel);
            buf.writeUtf(rankName, 32);
            buf.writeUtf(rankColorCode, 8);
        }
        buf.writeInt(playerLevel);
        buf.writeDouble(playerProgress);
    }

    public static PlayerGangInfo decode(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        boolean inGang = buf.readBoolean();
        String tag = "";
        int colorOrd = 0;
        int gLevel = 0;
        String rank = "";
        String rankColor = "";
        if (inGang) {
            tag = buf.readUtf(10);
            colorOrd = buf.readInt();
            gLevel = buf.readInt();
            rank = buf.readUtf(32);
            rankColor = buf.readUtf(8);
        }
        int pLevel = buf.readInt();
        double pProg = buf.readDouble();
        return new PlayerGangInfo(uuid, inGang, tag, colorOrd, gLevel, rank, rankColor, pLevel, pProg);
    }

    // Getters
    public UUID getPlayerUUID() { return playerUUID; }
    public boolean isInGang() { return inGang; }
    public String getGangTag() { return gangTag; }
    public int getGangLevel() { return gangLevel; }
    public String getRankName() { return rankName; }
    public String getRankColorCode() { return rankColorCode; }
    public int getPlayerLevel() { return playerLevel; }
    public double getPlayerProgress() { return playerProgress; }

    public ChatFormatting getGangColor() {
        ChatFormatting[] values = ChatFormatting.values();
        if (gangColorOrdinal >= 0 && gangColorOrdinal < values.length) {
            return values[gangColorOrdinal];
        }
        return ChatFormatting.WHITE;
    }

    /**
     * Formatierter Gang-Tag mit Sternen.
     * z.B. "§c[MAFIA §4★★★§c]"
     */
    public String getFormattedGangTag() {
        if (!inGang) return "";
        String colorStr = "\u00A7" + getGangColor().getChar();
        String stars = de.rolandsw.schedulemc.gang.GangReputation.getLevelStars(gangLevel);
        if (stars.isEmpty()) {
            return colorStr + "[" + gangTag + "]";
        }
        return colorStr + "[" + gangTag + " " + stars + colorStr + "]";
    }

    /**
     * Formatierte Level-Anzeige mit Fortschrittsbalken.
     * z.B. "§6Lv.18 §a████░░ §772%"
     */
    public String getFormattedLevel() {
        int filled = (int) (playerProgress * 6);
        int empty = 6 - filled;

        StringBuilder bar = new StringBuilder("\u00A7a");
        for (int i = 0; i < filled; i++) bar.append("\u2588");
        bar.append("\u00A77");
        for (int i = 0; i < empty; i++) bar.append("\u2591");

        return "\u00A76Lv." + playerLevel + " " + bar + " \u00A77" +
                String.format("%.0f%%", playerProgress * 100);
    }
}
