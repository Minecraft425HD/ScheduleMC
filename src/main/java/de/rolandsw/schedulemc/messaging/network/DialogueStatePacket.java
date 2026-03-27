package de.rolandsw.schedulemc.messaging.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Server → Client: Synchronisiert den aktuellen Dialogzustand (verfügbare Optionen)
 * für einen NPC-Chat in ChatScreen.
 */
public class DialogueStatePacket {

    private static final int MAX_OPTIONS = 8;
    private static final int MAX_TEXT_LENGTH = 256;

    private final UUID npcUUID;
    /** Verfügbare Optionen: id → text */
    private final List<OptionEntry> options;

    public DialogueStatePacket(UUID npcUUID, List<OptionEntry> options) {
        this.npcUUID = npcUUID;
        this.options = Collections.unmodifiableList(new ArrayList<>(options));
    }

    public UUID getNpcUUID() {
        return npcUUID;
    }

    public List<OptionEntry> getOptions() {
        return options;
    }

    // ═══════════════════════════════════════════════════════════
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(npcUUID);
        int count = Math.min(options.size(), MAX_OPTIONS);
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            OptionEntry e = options.get(i);
            buf.writeUtf(e.id(), MAX_TEXT_LENGTH);
            buf.writeUtf(e.text(), MAX_TEXT_LENGTH);
        }
    }

    public static DialogueStatePacket decode(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        int count = buf.readVarInt();
        count = Math.min(count, MAX_OPTIONS);
        List<OptionEntry> opts = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String id = buf.readUtf(MAX_TEXT_LENGTH);
            String text = buf.readUtf(MAX_TEXT_LENGTH);
            opts.add(new OptionEntry(id, text));
        }
        return new DialogueStatePacket(uuid, opts);
    }

    // ═══════════════════════════════════════════════════════════
    // HANDLER (CLIENT-SIDE)
    // ═══════════════════════════════════════════════════════════

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(this::handleClient);
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof de.rolandsw.schedulemc.client.screen.apps.ChatScreen chatScreen
            && chatScreen.getParticipantUUID().equals(npcUUID)) {
            chatScreen.handleDialogueState(options);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════

    public record OptionEntry(String id, String text) {}
}
