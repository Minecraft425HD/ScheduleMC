package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.network.AbstractPacket;
import de.rolandsw.schedulemc.npc.client.ClientNPCNameCache;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Set;

/**
 * Delta-Sync Paket für NPC-Namen
 *
 * OPTIMIERUNG: Statt alle Namen zu senden, werden nur Änderungen übertragen.
 * Reduziert Netzwerk-Traffic erheblich bei vielen NPCs.
 *
 * @see SyncNPCNamesPacket für Full-Sync beim Login
 */
public class DeltaSyncNPCNamesPacket extends AbstractPacket {

    public enum Operation {
        ADD,
        REMOVE
    }

    private final Operation operation;
    private final Set<String> names;

    /**
     * Erstellt ein Delta-Sync Paket
     *
     * @param operation ADD oder REMOVE
     * @param names Die Namen die hinzugefügt/entfernt werden
     */
    public DeltaSyncNPCNamesPacket(Operation operation, Set<String> names) {
        this.operation = operation;
        this.names = names;
    }

    /**
     * Convenience-Konstruktor für einzelnen Namen
     */
    public DeltaSyncNPCNamesPacket(Operation operation, String name) {
        this(operation, Set.of(name));
    }

    @Override
    protected void writeData(FriendlyByteBuf buf) {
        buf.writeEnum(operation);
        writeStringSet(buf, names);
    }

    public static DeltaSyncNPCNamesPacket decode(FriendlyByteBuf buf) {
        Operation op = buf.readEnum(Operation.class);
        Set<String> names = readStringSet(buf);
        return new DeltaSyncNPCNamesPacket(op, names);
    }

    @Override
    protected void handleOnClient() {
        switch (operation) {
            case ADD -> ClientNPCNameCache.addNames(names);
            case REMOVE -> ClientNPCNameCache.removeNames(names);
        }
    }

    public Operation getOperation() {
        return operation;
    }

    public Set<String> getNames() {
        return names;
    }
}
