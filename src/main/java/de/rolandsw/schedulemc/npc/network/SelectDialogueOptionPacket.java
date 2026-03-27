package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.messaging.MessageManager;
import de.rolandsw.schedulemc.messaging.network.DialogueStatePacket;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.life.dialogue.DialogueContext;
import de.rolandsw.schedulemc.npc.life.dialogue.DialogueManager;
import de.rolandsw.schedulemc.npc.life.dialogue.DialogueNode;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Client → Server: Spieler wählt eine Dialog-Option.
 */
public class SelectDialogueOptionPacket {

    private static final int MAX_STR = 256;

    private final int entityId;
    private final String optionId;
    /** Anzeigetext der gewählten Option — wird als Spieler-Nachricht gespeichert */
    private final String optionText;

    public SelectDialogueOptionPacket(int entityId, String optionId, String optionText) {
        this.entityId = entityId;
        this.optionId = optionId;
        this.optionText = optionText;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(optionId, MAX_STR);
        buf.writeUtf(optionText, MAX_STR);
    }

    public static SelectDialogueOptionPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        String optionId = buf.readUtf(MAX_STR);
        String optionText = buf.readUtf(MAX_STR);
        return new SelectDialogueOptionPacket(entityId, optionId, optionText);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            Entity entity = player.level().getEntity(entityId);
            if (!(entity instanceof CustomNPCEntity npc) || npc.getNpcData() == null) return;

            DialogueManager mgr = DialogueManager.getInstance();
            if (mgr == null) return;

            String npcName = npc.getNpcData().getNpcName();

            // 1. Spieler-Nachricht speichern
            MessageManager.sendMessage(
                player.getUUID(), player.getName().getString(), true,
                npc.getUUID(), npcName, false,
                optionText
            );

            // 2. Option im Dialog verarbeiten → NPC-Antwort-Node
            DialogueNode nextNode = mgr.selectOption(player, optionId);

            // 3a. Dialog liefert nächsten Node → NPC antwortet
            if (nextNode != null) {
                DialogueContext context = mgr.getActiveDialogue(player);
                String npcResponse = nextNode.getDisplayText(context, npc);

                // NPC-Antwort speichern + Notification
                StartDialoguePacket.sendNPCMessage(player, npc, npcName, npcResponse);

                // Neue Optionen an Client senden
                List<DialogueStatePacket.OptionEntry> entries =
                    StartDialoguePacket.buildOptionEntries(context, npc);
                NPCNetworkHandler.sendToClient(
                    new DialogueStatePacket(npc.getUUID(), entries), player
                );
            } else {
                // 3b. Dialog beendet → leere Options-Liste
                NPCNetworkHandler.sendToClient(
                    new DialogueStatePacket(npc.getUUID(), List.of()), player
                );
            }
        });
    }
}
