package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.managers.NPCNameRegistry;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.data.MerchantShopDefaults;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.entity.NPCEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet zum Spawnen eines NPCs vom Client zum Server
 */
public class SpawnNPCPacket {
    private final BlockPos position;
    private final String npcName;
    private final String skinFile;
    private final NPCType npcType;
    private final MerchantCategory merchantCategory;

    public SpawnNPCPacket(BlockPos position, String npcName, String skinFile, NPCType npcType, MerchantCategory merchantCategory) {
        this.position = position;
        this.npcName = npcName;
        this.skinFile = skinFile;
        this.npcType = npcType;
        this.merchantCategory = merchantCategory;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(position);
        buf.writeUtf(npcName);
        buf.writeUtf(skinFile);
        buf.writeEnum(npcType);
        buf.writeEnum(merchantCategory);
    }

    public static SpawnNPCPacket decode(FriendlyByteBuf buf) {
        return new SpawnNPCPacket(
            buf.readBlockPos(),
            buf.readUtf(),
            buf.readUtf(),
            buf.readEnum(NPCType.class),
            buf.readEnum(MerchantCategory.class)
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerLevel level = player.serverLevel();

                // Prüfe ob Name bereits vergeben ist
                if (NPCNameRegistry.isNameTaken(npcName)) {
                    player.sendSystemMessage(
                        Component.literal("⚠ Ein NPC mit dem Namen '")
                            .withStyle(ChatFormatting.RED)
                            .append(Component.literal(npcName)
                                .withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal("' existiert bereits!")
                                .withStyle(ChatFormatting.RED))
                    );
                    return;
                }

                // Spawne NPC
                CustomNPCEntity npc = NPCEntities.CUSTOM_NPC.get().create(level);
                if (npc != null) {
                    // Setze Position (über dem angeklickten Block)
                    npc.setPos(position.getX() + 0.5, position.getY() + 1.0, position.getZ() + 0.5);

                    // Konfiguriere NPC Data
                    NPCData data = new NPCData(npcName, skinFile, npcType, merchantCategory);

                    // Füge typ-spezifische Standard-Dialoge hinzu
                    setupDialogForType(data, npcName, npcType, merchantCategory);

                    npc.setNpcData(data);
                    npc.setNpcName(npcName);
                    npc.setSkinFileName(skinFile);

                    // Füge Entity zur Welt hinzu
                    level.addFreshEntity(npc);

                    // Registriere Namen (nach dem Entity spawnen, damit ID verfügbar ist)
                    if (NPCNameRegistry.registerName(npcName, npc.getId())) {
                        player.sendSystemMessage(
                            Component.literal("✓ NPC '")
                                .withStyle(ChatFormatting.GREEN)
                                .append(Component.literal(npcName)
                                    .withStyle(ChatFormatting.YELLOW))
                                .append(Component.literal("' erfolgreich erstellt!")
                                    .withStyle(ChatFormatting.GREEN))
                        );
                        NPCNameRegistry.saveIfNeeded();

                        // Sende aktualisierte Namen-Liste an alle Clients
                        de.rolandsw.schedulemc.npc.events.NPCNameSyncHandler.broadcastNameUpdate(
                            level.getServer()
                        );
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Richtet typ-spezifische Dialoge ein
     */
    private static void setupDialogForType(NPCData data, String npcName, NPCType npcType, MerchantCategory merchantCategory) {
        switch (npcType) {
            case BEWOHNER:
                data.addDialogEntry(new NPCData.DialogEntry("Hallo! Ich bin " + npcName + ".", ""));
                data.addDialogEntry(new NPCData.DialogEntry("Wie kann ich dir helfen?", ""));
                data.addDialogEntry(new NPCData.DialogEntry("Schönen Tag noch!", ""));
                break;

            case VERKAEUFER:
                data.addDialogEntry(new NPCData.DialogEntry("Willkommen bei " + merchantCategory.getDisplayName() + "!", ""));
                data.addDialogEntry(new NPCData.DialogEntry("Ich bin " + npcName + ", dein Händler.", ""));
                data.addDialogEntry(new NPCData.DialogEntry("Was möchtest du kaufen oder verkaufen?", ""));
                data.addDialogEntry(new NPCData.DialogEntry("Danke für deinen Einkauf!", ""));

                // Initialisiere Shop-Items basierend auf Kategorie
                MerchantShopDefaults.setupShopItems(data, merchantCategory);
                break;

            case POLIZEI:
                data.addDialogEntry(new NPCData.DialogEntry("Guten Tag. Polizei " + npcName + ".", ""));
                data.addDialogEntry(new NPCData.DialogEntry("Kann ich Ihnen helfen?", ""));
                data.addDialogEntry(new NPCData.DialogEntry("Bleiben Sie sicher!", ""));
                break;
        }
    }
}
