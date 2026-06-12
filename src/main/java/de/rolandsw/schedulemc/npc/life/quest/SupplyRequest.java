package de.rolandsw.schedulemc.npc.life.quest;

import java.util.UUID;

/**
 * Eine proaktive Warenanfrage eines NPCs an einen Spieler:
 * "Bring mir X mal Item Y zum Treffpunkt Z, ich zahle dir W€."
 *
 * Gson-freundliches POJO; persistiert in npc_supply_requests.json.
 */
public class SupplyRequest {

    /** Stabile NPC-Data-UUID (nicht Entity-UUID). */
    public UUID npcId;
    public UUID playerId;

    /** Registry-ID des gewünschten Items (z.B. "minecraft:bread"). */
    public String itemId;
    public int amount;
    /** Angebotene Bezahlung in €. */
    public int payment;

    /** Treffpunkt (Arbeitsplatz > Zuhause > Standort bei Erstellung). */
    public int meetingX;
    public int meetingY;
    public int meetingZ;

    /** Spieltag, an dem das Angebot erstellt wurde. */
    public long offerDay;

    /** Wurde bereits eine Smartphone-Nachricht verschickt? */
    public boolean notifiedRemotely;
    /** Wurde der Spieler bereits in der Nähe angesprochen? */
    public boolean noticedNearby;

    /** Quest-ID nach Annahme; null solange das Angebot offen ist. */
    public String questId;

    public SupplyRequest() {
        // Gson
    }

    public SupplyRequest(UUID npcId, UUID playerId, String itemId, int amount, int payment,
                         int meetingX, int meetingY, int meetingZ, long offerDay) {
        this.npcId = npcId;
        this.playerId = playerId;
        this.itemId = itemId;
        this.amount = amount;
        this.payment = payment;
        this.meetingX = meetingX;
        this.meetingY = meetingY;
        this.meetingZ = meetingZ;
        this.offerDay = offerDay;
    }

    public boolean isAccepted() {
        return questId != null;
    }

    public net.minecraft.core.BlockPos getMeetingPoint() {
        return new net.minecraft.core.BlockPos(meetingX, meetingY, meetingZ);
    }
}
