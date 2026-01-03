package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.managers.NPCNameRegistry;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.data.BankCategory;
import de.rolandsw.schedulemc.npc.data.MerchantShopDefaults;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.entity.NPCEntities;
import de.rolandsw.schedulemc.util.InputValidation;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
    private final BankCategory bankCategory;

    public SpawnNPCPacket(BlockPos position, String npcName, String skinFile, NPCType npcType, MerchantCategory merchantCategory, BankCategory bankCategory) {
        this.position = position;
        this.npcName = npcName;
        this.skinFile = skinFile;
        this.npcType = npcType;
        this.merchantCategory = merchantCategory;
        this.bankCategory = bankCategory;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(position);
        buf.writeUtf(npcName);
        buf.writeUtf(skinFile);
        buf.writeEnum(npcType);
        buf.writeEnum(merchantCategory);
        buf.writeEnum(bankCategory);
    }

    public static SpawnNPCPacket decode(FriendlyByteBuf buf) {
        return new SpawnNPCPacket(
            buf.readBlockPos(),
            buf.readUtf(InputValidation.MAX_NPC_NAME_LENGTH + 10), // +10 für Sicherheitspuffer
            buf.readUtf(InputValidation.MAX_SKIN_FILE_LENGTH + 10),
            buf.readEnum(NPCType.class),
            buf.readEnum(MerchantCategory.class),
            buf.readEnum(BankCategory.class)
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            ServerLevel level = player.serverLevel();

            // SICHERHEIT: Validiere alle Eingaben
            InputValidation.Result nameResult = InputValidation.validateNPCName(npcName);
            if (!nameResult.isValid()) {
                player.sendSystemMessage(Component.literal(nameResult.getError()));
                return;
            }
            String validatedName = nameResult.getSanitizedValue();

            InputValidation.Result skinResult = InputValidation.validateSkinFileName(skinFile);
            if (!skinResult.isValid()) {
                player.sendSystemMessage(Component.literal(skinResult.getError()));
                return;
            }

            InputValidation.Result posResult = InputValidation.validateBlockPos(position);
            if (!posResult.isValid()) {
                player.sendSystemMessage(Component.literal(posResult.getError()));
                return;
            }

            // SICHERHEIT: Prüfe Permission für spezielle NPC-Typen
            if ((npcType == NPCType.POLIZEI || npcType == NPCType.ARZT) && !player.hasPermissions(2)) {
                player.sendSystemMessage(Component.literal("§cDu hast keine Berechtigung für diesen NPC-Typ!"));
                return;
            }

            // Prüfe ob Name bereits vergeben ist
            if (NPCNameRegistry.isNameTaken(validatedName)) {
                player.sendSystemMessage(
                    Component.literal("⚠ Ein NPC mit dem Namen '")
                        .withStyle(ChatFormatting.RED)
                        .append(Component.literal(validatedName)
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

                // Konfiguriere NPC Data (mit validiertem Namen)
                NPCData data = new NPCData(validatedName, skinFile, npcType, merchantCategory);
                data.setBankCategory(bankCategory);

                // Füge typ-spezifische Standard-Dialoge hinzu
                setupDialogForType(data, validatedName, npcType, merchantCategory, bankCategory);

                npc.setNpcData(data);
                npc.setNpcName(validatedName);
                npc.setSkinFileName(skinFile);

                // Füge Entity zur Welt hinzu
                level.addFreshEntity(npc);

                // Registriere Namen (nach dem Entity spawnen, damit ID verfügbar ist)
                if (NPCNameRegistry.registerName(validatedName, npc.getId())) {
                    player.sendSystemMessage(
                        Component.literal("✓ NPC '")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(validatedName)
                                .withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal("' erfolgreich erstellt!")
                                .withStyle(ChatFormatting.GREEN))
                    );
                    NPCNameRegistry.saveIfNeeded();

                    // Delta-Sync: Sende nur den neuen Namen statt aller Namen
                    de.rolandsw.schedulemc.npc.events.NPCNameSyncHandler.broadcastNameAdded(
                        level.getServer(), validatedName
                    );
                }
            }
        });
    }

    /**
     * Richtet typ-spezifische Dialoge ein
     */
    private static void setupDialogForType(NPCData data, String npcName, NPCType npcType, MerchantCategory merchantCategory, BankCategory bankCategory) {
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

            case BANK:
                data.addDialogEntry(new NPCData.DialogEntry("Willkommen bei der Bank!", ""));
                data.addDialogEntry(new NPCData.DialogEntry("Ich bin " + npcName + ", Ihr " + bankCategory.getDisplayName() + ".", ""));
                data.addDialogEntry(new NPCData.DialogEntry("Wie kann ich Ihnen heute helfen?", ""));
                data.addDialogEntry(new NPCData.DialogEntry("Vielen Dank für Ihr Vertrauen!", ""));
                break;
        }
    }
}
