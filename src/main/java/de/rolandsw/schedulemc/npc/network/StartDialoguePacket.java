package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.messaging.network.DialogueStatePacket;
import de.rolandsw.schedulemc.messaging.network.MessageNetworkHandler;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.dialogue.DialogueContext;
import de.rolandsw.schedulemc.npc.life.dialogue.DialogueManager;
import de.rolandsw.schedulemc.npc.life.dialogue.DialogueOption;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Client → Server: Startet (oder setzt fort) einen Dialog mit einem NPC.
 * Wird gesendet wenn ChatScreen für einen NPC geöffnet wird.
 */
public class StartDialoguePacket {

    private final int entityId;

    public StartDialoguePacket(int entityId) {
        this.entityId = entityId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    public static StartDialoguePacket decode(FriendlyByteBuf buf) {
        return new StartDialoguePacket(buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            Entity entity = player.level().getEntity(entityId);
            if (!(entity instanceof CustomNPCEntity npc) || npc.getNpcData() == null) return;

            DialogueManager mgr = DialogueManager.getInstance();
            if (mgr == null) return;

            DialogueContext context;
            if (mgr.isInDialogue(player)) {
                // Bereits im Dialog → einfach aktuelle Optionen erneut senden
                context = mgr.getActiveDialogue(player);
            } else {
                // Neuen Dialog starten
                context = mgr.startDialogue(player, npc);
                if (context == null) return;

                // NPC-Begrüßung als Nachricht speichern
                String npcText = context.getCurrentNode().getDisplayText(context, npc);
                String npcName = npc.getNpcData().getNpcName();
                sendNPCMessage(player, npc, npcName, npcText);
            }

            // Verfügbare Optionen an Client senden
            sendDialogueState(player, npc.getUUID(), context, npc);
        });
    }

    // ───────────────────────────────────────────────────────────
    // HELPERS (shared with SelectDialogueOptionPacket)
    // ───────────────────────────────────────────────────────────

    static void sendNPCMessage(ServerPlayer player, CustomNPCEntity npc, String npcName, String text) {
        MessageManager.sendMessage(
            npc.getUUID(), npcName, false,
            player.getUUID(), player.getName().getString(), true,
            text
        );
        // Notification-Overlay
        MessageNetworkHandler.sendToClient(
            new de.rolandsw.schedulemc.messaging.network.ReceiveMessagePacket(
                npc.getUUID(), npcName, false, text
            ),
            player
        );
    }

    static void sendDialogueState(ServerPlayer player, java.util.UUID npcUUID,
                                  DialogueContext context, CustomNPCEntity npc) {
        List<DialogueStatePacket.OptionEntry> entries = buildOptionEntries(context, npc);
        NPCNetworkHandler.sendToClient(new DialogueStatePacket(npcUUID, entries), player);
    }

    static List<DialogueStatePacket.OptionEntry> buildOptionEntries(DialogueContext context,
                                                                     CustomNPCEntity npc) {
        List<DialogueStatePacket.OptionEntry> entries = new ArrayList<>();
        if (context == null || context.getCurrentNode() == null) return entries;
        for (DialogueOption opt : context.getCurrentNode().getVisibleOptions(context, npc)) {
            entries.add(new DialogueStatePacket.OptionEntry(opt.getId(), opt.getText()));
        }
        return entries;
    }
}
