package de.rolandsw.schedulemc.weapon.handler;

import de.rolandsw.schedulemc.weapon.gun.GunItem;
import de.rolandsw.schedulemc.weapon.util.WeaponNBT;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class WeaponServerEventHandler {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
            Player player = event.player;
            if (player.getPersistentData().getBoolean(WeaponNBT.AUTO_FIRE_ACTIVE)) {
                int slot = player.getPersistentData().getInt(WeaponNBT.AUTO_FIRE_GUN_SLOT);
                if (slot != -1 && (slot < 0 || slot >= player.getInventory().getContainerSize())) {
                    player.getPersistentData().remove(WeaponNBT.AUTO_FIRE_ACTIVE);
                    return;
                }
                ItemStack gunStack = slot == -1 ? player.getOffhandItem() : player.getInventory().getItem(slot);
                if (gunStack.getItem() instanceof GunItem gun && gun.getFireMode(gunStack) == 2) {
                    if (gun.canShoot(gunStack, player)) {
                        gun.performShots(player.level(), player, gunStack, 1);
                    } else {
                        player.getPersistentData().remove(WeaponNBT.AUTO_FIRE_ACTIVE);
                    }
                } else {
                    player.getPersistentData().remove(WeaponNBT.AUTO_FIRE_ACTIVE);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity().getMainHandItem().getItem() instanceof GunItem) {
            event.setCanceled(true);
        }
    }
}
