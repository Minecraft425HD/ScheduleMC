package de.rolandsw.schedulemc.npc.network;

import de.rolandsw.schedulemc.managers.NPCNameRegistry;
import de.rolandsw.schedulemc.npc.data.NPCData;
import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.data.MerchantCategory;
import de.rolandsw.schedulemc.npc.data.BankCategory;
import de.rolandsw.schedulemc.npc.data.ServiceCategory;
import de.rolandsw.schedulemc.npc.data.MerchantShopDefaults;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.npc.entity.NPCEntities;
import de.rolandsw.schedulemc.util.InputValidation;
import de.rolandsw.schedulemc.util.PacketHandler;
import de.rolandsw.schedulemc.util.RateLimiter;
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
    // RATE LIMITING: DoS Protection für NPC-Spawning
    private static final RateLimiter npcSpawnLimiter = new RateLimiter("npc_spawn", 10, 60000L); // 10 per minute

    private final BlockPos position;
    private final String npcName;
    private final String skinFile;
    private final NPCType npcType;
    private final MerchantCategory merchantCategory;
    private final BankCategory bankCategory;
    private final ServiceCategory serviceCategory;

    public SpawnNPCPacket(BlockPos position, String npcName, String skinFile, NPCType npcType, MerchantCategory merchantCategory, BankCategory bankCategory, ServiceCategory serviceCategory) {
        this.position = position;
        this.npcName = npcName;
        this.skinFile = skinFile;
        this.npcType = npcType;
        this.merchantCategory = merchantCategory;
        this.bankCategory = bankCategory;
        this.serviceCategory = serviceCategory;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(position);
        buf.writeUtf(npcName);
        buf.writeUtf(skinFile);
        buf.writeEnum(npcType);
        buf.writeEnum(merchantCategory);
        buf.writeEnum(bankCategory);
        buf.writeEnum(serviceCategory);
    }

    public static SpawnNPCPacket decode(FriendlyByteBuf buf) {
        return new SpawnNPCPacket(
            buf.readBlockPos(),
            buf.readUtf(InputValidation.MAX_NPC_NAME_LENGTH + 10), // +10 für Sicherheitspuffer
            buf.readUtf(InputValidation.MAX_SKIN_FILE_LENGTH + 10),
            buf.readEnum(NPCType.class),
            buf.readEnum(MerchantCategory.class),
            buf.readEnum(BankCategory.class),
            buf.readEnum(ServiceCategory.class)
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            ServerLevel level = player.serverLevel();

            // RATE LIMITING: DoS Protection für NPC-Spawning
            if (!npcSpawnLimiter.allowOperation(player.getUUID())) {
                player.sendSystemMessage(Component.translatable("error.rate_limit.npc_spawn")
                    .withStyle(ChatFormatting.RED));
                return;
            }

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
            if (npcType == NPCType.POLIZEI && !player.hasPermissions(2)) {
                player.sendSystemMessage(Component.translatable("message.npc.no_permission_type"));
                return;
            }

            // Prüfe ob Name bereits vergeben ist
            if (NPCNameRegistry.isNameTaken(validatedName)) {
                player.sendSystemMessage(
                    Component.translatable("message.npc.already_exists_prefix")
                        .withStyle(ChatFormatting.RED)
                        .append(Component.literal(validatedName)
                            .withStyle(ChatFormatting.YELLOW))
                        .append(Component.translatable("message.npc.already_exists_suffix")
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
                data.setServiceCategory(serviceCategory);

                // Füge typ-spezifische Standard-Dialoge hinzu
                setupDialogForType(data, validatedName, npcType, merchantCategory, bankCategory, serviceCategory);

                npc.setNpcData(data);
                npc.setNpcName(validatedName);
                npc.setSkinFileName(skinFile);

                // Füge Entity zur Welt hinzu
                level.addFreshEntity(npc);

                // Registriere Namen (nach dem Entity spawnen, damit ID verfügbar ist)
                if (NPCNameRegistry.registerName(validatedName, npc.getId())) {
                    player.sendSystemMessage(
                        Component.translatable("message.npc.success_prefix")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(validatedName)
                                .withStyle(ChatFormatting.YELLOW))
                            .append(Component.translatable("message.common.created_suffix")
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
    private static void setupDialogForType(NPCData data, String npcName, NPCType npcType, MerchantCategory merchantCategory, BankCategory bankCategory, ServiceCategory serviceCategory) {
        switch (npcType) {
            case BEWOHNER:
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.resident.intro", npcName).getString(), ""));
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.resident.help").getString(), ""));
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.resident.goodbye").getString(), ""));
                break;

            case VERKAEUFER:
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.merchant.welcome", merchantCategory.getDisplayName()).getString(), ""));
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.merchant.intro", npcName).getString(), ""));
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.merchant.offer").getString(), ""));
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.merchant.thanks").getString(), ""));

                // Initialisiere Shop-Items basierend auf Kategorie
                MerchantShopDefaults.setupShopItems(data, merchantCategory);
                break;

            case POLIZEI:
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.police.intro", npcName).getString(), ""));
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.police.help").getString(), ""));
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.police.goodbye").getString(), ""));
                break;

            case BANK:
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.bank.welcome").getString(), ""));
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.bank.intro", npcName, bankCategory.getDisplayName()).getString(), ""));
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.bank.help").getString(), ""));
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.bank.thanks").getString(), ""));
                break;

            case ABSCHLEPPER:
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.service.welcome", serviceCategory.getDisplayName()).getString(), ""));
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.service.intro", npcName).getString(), ""));
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.service.help").getString(), ""));
                data.addDialogEntry(new NPCData.DialogEntry(Component.translatable("npc.dialog.service.thanks").getString(), ""));

                // Initialisiere Shop-Items basierend auf Service-Kategorie
                MerchantShopDefaults.setupServiceShopItems(data, serviceCategory);
                break;
        }
    }
}
