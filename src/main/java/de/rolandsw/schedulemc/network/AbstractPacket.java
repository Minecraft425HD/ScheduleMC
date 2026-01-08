package de.rolandsw.schedulemc.network;

import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Abstrakte Basis-Klasse für Netzwerk-Pakete
 *
 * OPTIMIERUNG: Reduziert ~20 Zeilen Boilerplate-Code pro Paket
 *
 * Verwendung:
 * <pre>
 * public class MyPacket extends AbstractPacket {
 *     private final String data;
 *
 *     public MyPacket(String data) { this.data = data; }
 *
 *     @Override
 *     protected void writeData(FriendlyByteBuf buf) {
 *         buf.writeUtf(data);
 *     }
 *
 *     // SICHERHEIT: Max-Länge für Strings gegen DoS/Memory-Angriffe
 *     public static MyPacket read(FriendlyByteBuf buf) {
 *         return new MyPacket(buf.readUtf(256));
 *     }
 *
 *     @Override
 *     protected void handleOnClient() {
 *         // Client-Logik
 *     }
 * }
 * </pre>
 */
public abstract class AbstractPacket {

    /**
     * Schreibt die Paket-Daten in den Buffer.
     * Muss von Subklassen implementiert werden.
     */
    protected abstract void writeData(FriendlyByteBuf buf);

    /**
     * Wird auf der Client-Seite ausgeführt.
     * Standard: Nichts tun (für Server-Pakete überschreiben).
     */
    protected void handleOnClient() {
        // Override in subclass for client-bound packets
    }

    /**
     * Wird auf der Server-Seite ausgeführt.
     * Standard: Nichts tun (für Client-Pakete überschreiben).
     *
     * @param ctx Network-Kontext mit Sender-Informationen
     */
    protected void handleOnServer(NetworkEvent.Context ctx) {
        // Override in subclass for server-bound packets
    }

    /**
     * Encodiert das Paket in den Buffer.
     * Ruft intern writeData() auf.
     */
    public final void encode(FriendlyByteBuf buf) {
        writeData(buf);
    }

    /**
     * Behandelt das Paket basierend auf der Seite (Client/Server).
     */
    public final void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide().isClient()) {
                handleOnClient();
            } else {
                handleOnServer(ctx);
            }
        });

        ctx.setPacketHandled(true);
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER-METHODEN für häufige Datentypen
    // ═══════════════════════════════════════════════════════════

    /**
     * Schreibt eine UUID in den Buffer
     */
    protected static void writeUUID(FriendlyByteBuf buf, java.util.UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Liest eine UUID aus dem Buffer
     */
    protected static java.util.UUID readUUID(FriendlyByteBuf buf) {
        return new java.util.UUID(buf.readLong(), buf.readLong());
    }

    /**
     * Schreibt eine String-Liste in den Buffer
     */
    protected static void writeStringList(FriendlyByteBuf buf, java.util.List<String> list) {
        buf.writeInt(list.size());
        for (String s : list) {
            buf.writeUtf(s);
        }
    }

    /**
     * Liest eine String-Liste aus dem Buffer
     * SICHERHEIT: Max-Länge und max Anzahl gegen DoS/Memory-Angriffe
     */
    protected static java.util.List<String> readStringList(FriendlyByteBuf buf) {
        int size = Math.min(buf.readInt(), 1000); // Max 1000 Einträge
        java.util.List<String> list = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buf.readUtf(256)); // Max 256 chars per String
        }
        return list;
    }

    /**
     * Schreibt ein String-Set in den Buffer
     */
    protected static void writeStringSet(FriendlyByteBuf buf, java.util.Set<String> set) {
        buf.writeInt(set.size());
        for (String s : set) {
            buf.writeUtf(s);
        }
    }

    /**
     * Liest ein String-Set aus dem Buffer
     * SICHERHEIT: Max-Länge und max Anzahl gegen DoS/Memory-Angriffe
     */
    protected static java.util.Set<String> readStringSet(FriendlyByteBuf buf) {
        int size = Math.min(buf.readInt(), 1000); // Max 1000 Einträge
        java.util.Set<String> set = new java.util.HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(buf.readUtf(256)); // Max 256 chars per String
        }
        return set;
    }

    /**
     * Schreibt eine UUID-Liste in den Buffer
     */
    protected static void writeUUIDList(FriendlyByteBuf buf, java.util.List<java.util.UUID> list) {
        buf.writeInt(list.size());
        for (java.util.UUID uuid : list) {
            writeUUID(buf, uuid);
        }
    }

    /**
     * Liest eine UUID-Liste aus dem Buffer
     */
    protected static java.util.List<java.util.UUID> readUUIDList(FriendlyByteBuf buf) {
        int size = buf.readInt();
        java.util.List<java.util.UUID> list = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(readUUID(buf));
        }
        return list;
    }

    /**
     * Schreibt eine optionale BlockPos in den Buffer
     */
    protected static void writeOptionalBlockPos(FriendlyByteBuf buf, net.minecraft.core.BlockPos pos) {
        buf.writeBoolean(pos != null);
        if (pos != null) {
            buf.writeBlockPos(pos);
        }
    }

    /**
     * Liest eine optionale BlockPos aus dem Buffer
     */
    protected static net.minecraft.core.BlockPos readOptionalBlockPos(FriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readBlockPos() : null;
    }
}
