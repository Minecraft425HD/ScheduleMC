package de.rolandsw.schedulemc.npc.network;

import com.mojang.logging.LogUtils;
import de.rolandsw.schedulemc.mission.MissionEventBridge;
import de.rolandsw.schedulemc.mission.PlayerMissionManager;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.companion.CompanionManager;
import de.rolandsw.schedulemc.npc.life.companion.CompanionType;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Packet für NPC Aktionen (Dialog, Shop, Missionen, etc.)
 *
 * Das optionale `param`-Feld uebertraegt kontextabhaengige Daten:
 * - GIVE_MISSION:      param = missionDefinitionId
 * - COMPLETE_MISSION:  param = missionId
 * - GIVE_ITEM_TO_NPC:  param = Anzahl (als String) der gelieferten Items
 * - PAY_NPC:           param = Betrag (als String)
 * - RECRUIT_NPC:       param = CompanionType-Name (z.B. "FIGHTER")
 * - alle anderen:      param leer
 */
public class NPCActionPacket {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final int entityId;
    private final Action action;
    private final String param;

    public enum Action {
        NEXT_DIALOG,
        GIVE_MISSION,
        COMPLETE_MISSION,
        GIVE_ITEM_TO_NPC,
        PAY_NPC,
        INITIATE_BRIBE,
        CAPTURE_NPC,
        RECRUIT_NPC,
        INTIMIDATE
    }

    public NPCActionPacket(int entityId, Action action) {
        this(entityId, action, "");
    }

    public NPCActionPacket(int entityId, Action action, String param) {
        this.entityId = entityId;
        this.action = action;
        this.param = param == null ? "" : param;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeEnum(action);
        buf.writeUtf(param, 256);
    }

    public static NPCActionPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        Action action = buf.readEnum(Action.class);
        String param = buf.readUtf(256);
        return new NPCActionPacket(entityId, action, param);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            Entity entity = player.level().getEntity(entityId);
            if (!(entity instanceof CustomNPCEntity npc) || npc.getNpcData() == null) return;

            switch (action) {
                case NEXT_DIALOG -> npc.getNpcData().nextDialog();
                case GIVE_MISSION -> handleGiveMission(player, npc);
                case COMPLETE_MISSION -> handleCompleteMission(player);
                case GIVE_ITEM_TO_NPC -> handleGiveItemToNpc(player);
                case PAY_NPC -> handlePayNpc(player);
                case INITIATE_BRIBE -> handleInitiateBribe(player, npc);
                case CAPTURE_NPC -> handleCaptureNpc(player, npc);
                case RECRUIT_NPC -> handleRecruitNpc(player, npc);
                case INTIMIDATE -> handleIntimidate(player, npc);
            }
        });
    }

    // ───────────────────────────────────────────────────────────
    // HANDLER
    // ───────────────────────────────────────────────────────────

    /** NPC vergibt Mission aus seiner missionIds-Liste an den Spieler. */
    private void handleGiveMission(ServerPlayer player, CustomNPCEntity npc) {
        PlayerMissionManager mgr = PlayerMissionManager.getInstance();
        if (mgr == null) return;

        List<String> missionIds = npc.getNpcData().getMissionIds();
        String missionId = param.isEmpty() && !missionIds.isEmpty() ? missionIds.get(0) : param;
        if (missionId == null || missionId.isEmpty()) return;
        if (!missionIds.contains(missionId)) return;

        boolean accepted = mgr.acceptMission(player, missionId);
        if (accepted) {
            player.sendSystemMessage(Component.literal(
                "§a[Mission] §fMission angenommen: §e" + missionId));
        } else {
            player.sendSystemMessage(Component.literal(
                "§c[Mission] §fMission nicht verfuegbar oder Voraussetzungen fehlen."));
        }
    }

    /** Spieler schließt aktive Mission ab (Abgabe beim NPC). */
    private void handleCompleteMission(ServerPlayer player) {
        PlayerMissionManager mgr = PlayerMissionManager.getInstance();
        if (mgr == null || param.isEmpty()) return;

        boolean claimed = mgr.claimMission(player, param);
        if (claimed) {
            player.sendSystemMessage(Component.literal(
                "§a[Mission] §fBelohnung erhalten!"));
        } else {
            player.sendSystemMessage(Component.literal(
                "§c[Mission] §fMission noch nicht abgeschlossen."));
        }
    }

    /** Spieler liefert Items an NPC ab (package_delivered tracking). */
    private void handleGiveItemToNpc(ServerPlayer player) {
        int count = 1;
        try { count = Integer.parseInt(param); } catch (NumberFormatException ex) { count = 1; }
        MissionEventBridge.firePackageDelivered(player);
        MissionEventBridge.fireTransactionCompleted(player);
        player.sendSystemMessage(Component.literal(
            "§a[Lieferung] §f" + count + " Item(s) erfolgreich uebergeben."));
    }

    /** Spieler bezahlt NPC (transaction_completed tracking). */
    private void handlePayNpc(ServerPlayer player) {
        int amount = 0;
        try { amount = Integer.parseInt(param); } catch (NumberFormatException ex) { amount = 0; }
        MissionEventBridge.fireTransactionCompleted(player);
        if (amount > 0) MissionEventBridge.fireMoneyEarned(player, -amount); // negative = ausgegeben, not tracked
        player.sendSystemMessage(Component.literal(
            "§a[Zahlung] §fBetrag bezahlt."));
    }

    /** Bestechungsversuch – delegiert an BriberySystem wenn Zeugenbericht vorhanden. */
    private void handleInitiateBribe(ServerPlayer player, CustomNPCEntity _npc) {
        // Bestechung ohne aktiven Zeugenbericht: einfach Transaction tracken
        MissionEventBridge.fireTransactionCompleted(player);
        player.sendSystemMessage(Component.literal(
            "§e[Bestechung] §fBestechungsversuch gestartet."));
    }

    /** Versetzt NPC in Captive-State (folgt Spieler). */
    private void handleCaptureNpc(ServerPlayer player, CustomNPCEntity npc) {
        npc.setCaptiveFollower(player.getUUID());
        player.sendSystemMessage(Component.literal(
            "§c[Entfuehrung] §f" + npc.getNpcData().getNpcName() + " folgt dir jetzt."));
    }

    /** Rekrutiert NPC als Begleiter ueber CompanionManager. */
    private void handleRecruitNpc(ServerPlayer player, CustomNPCEntity npc) {
        CompanionType type = CompanionType.FIGHTER;
        try {
            if (!param.isEmpty()) type = CompanionType.valueOf(param.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            type = CompanionType.FIGHTER;
        }

        CompanionManager mgr = CompanionManager.getInstance();
        if (mgr == null) {
            player.sendSystemMessage(Component.literal("§c[Rekrutierung] §fCompanion-System nicht verfuegbar."));
            return;
        }
        mgr.recruit(player, type, npc, npc.getNpcData().getNpcName());
        player.sendSystemMessage(Component.literal(
            "§a[Rekrutierung] §f" + npc.getNpcData().getNpcName() + " ist jetzt dein Begleiter."));
    }

    /** NPC wird bedroht – loest Behavior-Engine aus. */
    private void handleIntimidate(ServerPlayer player, CustomNPCEntity npc) {
        try {
            npc.getBehaviorEngine().onThreatened(player, 0.8f);
        } catch (Exception ex) {
            LOGGER.debug("NPCActionPacket: failed to trigger threatened behavior for NPC {}", entityId, ex);
        }
        player.sendSystemMessage(Component.literal(
            "§c[Einschuechterung] §fDu hast " + npc.getNpcData().getNpcName() + " bedroht."));
    }
}
