package de.rolandsw.schedulemc.tobacco.network;

import de.rolandsw.schedulemc.npc.data.NPCType;
import de.rolandsw.schedulemc.npc.entity.CustomNPCEntity;
import de.rolandsw.schedulemc.production.core.DrugType;
import de.rolandsw.schedulemc.tobacco.business.NPCBusinessMetrics;
import de.rolandsw.schedulemc.tobacco.business.NPCPurchaseDecision;
import de.rolandsw.schedulemc.tobacco.business.NegotiationScoreCalculator;
import de.rolandsw.schedulemc.tobacco.menu.TobaccoNegotiationMenu;
import de.rolandsw.schedulemc.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Packet um Tobacco Negotiation GUI zu öffnen
 */
public class OpenTobaccoNegotiationPacket {
    private final int npcEntityId;

    public OpenTobaccoNegotiationPacket(int npcEntityId) {
        this.npcEntityId = npcEntityId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(npcEntityId);
    }

    public static OpenTobaccoNegotiationPacket decode(FriendlyByteBuf buf) {
        return new OpenTobaccoNegotiationPacket(buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleServerPacket(ctx, player -> {
            Entity entity = player.level().getEntity(npcEntityId);
            if (!(entity instanceof CustomNPCEntity npc)) return;

            // Polizisten können keinen Tabak kaufen!
            if (npc.getNpcType() == NPCType.POLIZEI) {
                player.sendSystemMessage(Component.translatable("message.tobacco.police_no_buy"));
                return;
            }

            // Prüfe Cooldown (1x pro Tag pro NPC)
            boolean hasCooldown = false;
            String cooldownKey = "LastTobaccoSale_" + player.getStringUUID();
            if (npc.getNpcData().getCustomData().contains(cooldownKey)) {
                long currentDay = player.level().getDayTime() / 24000;
                long lastSaleDay = npc.getNpcData().getCustomData().getLong(cooldownKey);
                hasCooldown = lastSaleDay >= currentDay;
            }

            // Berechne Purchase Decision
            NPCBusinessMetrics metrics = new NPCBusinessMetrics(npc);
            metrics.updatePurchaseDecision(player);
            NPCPurchaseDecision decision = metrics.getLastPurchaseDecision();

            // Berechne neuen Score
            NegotiationScoreCalculator scoreCalculator = new NegotiationScoreCalculator();
            scoreCalculator.calculate(npc, player, DrugType.TOBACCO, 25.0); // Geschätzter Preis

            final boolean finalHasCooldown = hasCooldown;

            // Öffne Negotiation GUI mit Cooldown-Status
            NetworkHooks.openScreen(player, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return Component.translatable("gui.tobacco.negotiation.title");
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                    return new TobaccoNegotiationMenu(containerId, playerInventory, npcEntityId);
                }
            }, buf -> {
                buf.writeInt(npcEntityId);
                buf.writeBoolean(finalHasCooldown);
            });

            // Sende Purchase Decision zum Client (inkl. Wallet)
            if (decision != null) {
                PurchaseDecisionSyncPacket syncPacket = new PurchaseDecisionSyncPacket(
                    (int)decision.getTotalScore(),
                    decision.isWillingToBuy(),
                    decision.getDesiredAmount(),
                    npc.getNpcData().getWallet()
                );
                ModNetworking.sendToClient(syncPacket, player);
            }
        });
    }
}
