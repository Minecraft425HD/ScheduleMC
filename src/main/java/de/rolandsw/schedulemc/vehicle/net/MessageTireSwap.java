package de.rolandsw.schedulemc.vehicle.net;

import de.maxhenkel.corelib.net.Message;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.base.EntityGenericVehicle;
import de.rolandsw.schedulemc.vehicle.entity.vehicle.parts.*;
import de.rolandsw.schedulemc.vehicle.items.IVehiclePart;
import de.rolandsw.schedulemc.vehicle.items.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

/**
 * Client → Server: Führt einen Saisonreifentausch mit dem Wagenheber durch.
 * Der Spieler gibt den Hotbar-Slot an, in dem sich der neue Reifen befindet.
 * - Erster Tausch eines Reifensatzes: alter Reifen wird in denselben Slot zurückgelegt.
 * - Zweiter+ Tausch: alter Reifen ist abgenutzt und wird gelöscht.
 */
public class MessageTireSwap implements Message<MessageTireSwap> {

    private UUID vehicleUuid;
    private int hotbarSlot; // 0-8

    public MessageTireSwap() {}

    public MessageTireSwap(UUID vehicleUuid, int hotbarSlot) {
        this.vehicleUuid = vehicleUuid;
        this.hotbarSlot = hotbarSlot;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;
        if (hotbarSlot < 0 || hotbarSlot > 8) return;

        EntityGenericVehicle vehicle = findVehicle(player);
        if (vehicle == null) {
            player.displayClientMessage(
                Component.translatable("message.werkstatt.vehicle_not_found").withStyle(ChatFormatting.RED), false);
            return;
        }

        // Sicherheitsprüfung
        if (vehicle.getSecurityComponent() != null
                && !vehicle.getSecurityComponent().canPlayerAccessInventoryExternal(player)) {
            return;
        }

        // Aktuellen Fahrzeugreifen ermitteln
        PartTireBase currentTire = vehicle.getPartByClass(PartTireBase.class);
        if (currentTire == null) return;

        // Kein Saisonwechsel für Fahrzeuge mit Allwetterreifen (LKW)
        if (currentTire.getSeasonType() == TireSeasonType.ALL_SEASON) {
            player.displayClientMessage(
                Component.translatable("message.tire_swap.truck_not_supported").withStyle(ChatFormatting.RED), false);
            return;
        }

        // Neuen Reifen aus dem Hotbar-Slot holen
        ItemStack newTireStack = player.getInventory().getItem(hotbarSlot);
        if (newTireStack.isEmpty() || !(newTireStack.getItem() instanceof IVehiclePart)) return;

        Part newPart = ((IVehiclePart) newTireStack.getItem()).getPart(newTireStack);
        if (!(newPart instanceof PartTireBase newTire)) return;

        // Gültige Richtung: Sommer ↔ Winter (nicht gleiche Saison)
        TireSeasonType currentSeason = currentTire.getSeasonType();
        TireSeasonType newSeason = newTire.getSeasonType();

        if (currentSeason == newSeason) {
            player.displayClientMessage(
                Component.translatable("message.tire_swap.same_season").withStyle(ChatFormatting.RED), false);
            return;
        }
        if (newSeason == TireSeasonType.ALL_SEASON) return; // Allwetterreifen per Wagenheber nicht erlaubt

        // Abnutzungs-Zähler erhöhen
        boolean currentIsSummer = (currentSeason == TireSeasonType.SUMMER);
        int outgoingCount;
        if (currentIsSummer) {
            vehicle.incrementSummerTireSwapCount();
            outgoingCount = vehicle.getSummerTireSwapCount();
        } else {
            vehicle.incrementWinterTireSwapCount();
            outgoingCount = vehicle.getWinterTireSwapCount();
        }

        // ItemStack des ausgebauten Reifens bestimmen
        ItemStack outgoingStack = getItemStackForTire(currentTire);

        // Neuen Reifen aus Hotbar entfernen
        player.getInventory().setItem(hotbarSlot, ItemStack.EMPTY);

        // Neuen Reifen ins Fahrzeug einbauen
        replacePartInVehicle(vehicle, newTire);

        // Abgebauten Reifen zurücklegen oder löschen
        if (outgoingCount <= 1 && !outgoingStack.isEmpty()) {
            // Erster Ausbau: Reifen in denselben Slot zurücklegen
            player.getInventory().setItem(hotbarSlot, outgoingStack);
            player.displayClientMessage(
                Component.translatable("message.tire_swap.success_returned").withStyle(ChatFormatting.GREEN), false);
        } else {
            // Zweiter+ Ausbau: Reifen abgenutzt, wird verworfen
            player.displayClientMessage(
                Component.translatable("message.tire_swap.success_worn").withStyle(ChatFormatting.YELLOW), false);
        }
    }

    private EntityGenericVehicle findVehicle(ServerPlayer player) {
        Vec3 pos = player.position();
        AABB box = new AABB(pos.subtract(10, 10, 10), pos.add(10, 10, 10));
        return player.level().getEntitiesOfClass(EntityGenericVehicle.class, box,
            v -> v.getUUID().equals(vehicleUuid)).stream().findFirst().orElse(null);
    }

    private ItemStack getItemStackForTire(PartTireBase tire) {
        if (tire == PartRegistry.STANDARD_TIRE) return new ItemStack(ModItems.STANDARD_TIRE.get());
        if (tire == PartRegistry.SPORT_TIRE)    return new ItemStack(ModItems.SPORT_TIRE.get());
        if (tire == PartRegistry.PREMIUM_TIRE)  return new ItemStack(ModItems.PREMIUM_TIRE.get());
        if (tire == PartRegistry.WINTER_TIRE)   return new ItemStack(ModItems.WINTER_TIRE.get());
        return ItemStack.EMPTY;
    }

    private void replacePartInVehicle(EntityGenericVehicle vehicle, PartTireBase newTire) {
        ItemStack newStack = getItemStackForTire(newTire);
        if (newStack.isEmpty()) return;

        Container partInv = vehicle.getInventoryComponent().getPartInventory();
        for (int i = 0; i < partInv.getContainerSize(); i++) {
            ItemStack stack = partInv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof IVehiclePart partItem) {
                Part existing = partItem.getPart(stack);
                if (existing instanceof PartTireBase) {
                    partInv.setItem(i, newStack.copy());
                }
            }
        }
        vehicle.invalidatePartCache();
        vehicle.initParts();
        vehicle.setPartSerializer();
        vehicle.checkInitializing();
    }

    @Override
    public MessageTireSwap fromBytes(FriendlyByteBuf buf) {
        vehicleUuid = buf.readUUID();
        hotbarSlot = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(vehicleUuid);
        buf.writeInt(hotbarSlot);
    }
}
