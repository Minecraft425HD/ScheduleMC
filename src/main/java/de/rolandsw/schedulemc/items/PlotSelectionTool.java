package de.rolandsw.schedulemc.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plot-Auswahl-Werkzeug
 *
 * Wie WorldEdit's Wooden Axe:
 * - Linksklick: Setzt Position 1 (über Event-Handler in ScheduleMC.java)
 * - Rechtsklick auf Block: Setzt Position 2
 * - Rechtsklick in Luft: Zeigt aktuelle Selection
 */
public class PlotSelectionTool extends Item {
   
    // Speichert Positionen für jeden Spieler
    private static final Map<UUID, BlockPos> position1 = new ConcurrentHashMap<>();
    private static final Map<UUID, BlockPos> position2 = new ConcurrentHashMap<>();

    public static void setPosition1(UUID uuid, BlockPos pos) {
        position1.put(uuid, pos);
    }

    public static void setPosition2(UUID uuid, BlockPos pos) {
        position2.put(uuid, pos);
    }
   
    public PlotSelectionTool() {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(0)
        );
    }
   
    /**
     * Rechtsklick auf Block = Position 2
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
       
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
       
        BlockPos pos = context.getClickedPos();
       
        // Position 2 setzen
        position2.put(player.getUUID(), pos);
       
        // Info anzeigen
        player.displayClientMessage(Component.translatable(
            "message.plot.tool.pos2_set",
            pos.getX(), pos.getY(), pos.getZ()
        ), true);
       
        // Sound abspielen
        player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
       
        return InteractionResult.SUCCESS;
    }
   
    /**
     * Rechtsklick in Luft = Zeigt aktuelle Selection
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
       
        // Zeige aktuelle Selection
        showSelection(player);
       
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
   
    /**
     * Zeigt aktuelle Selection an
     */
    private void showSelection(Player player) {
        BlockPos pos1 = position1.get(player.getUUID());
        BlockPos pos2 = position2.get(player.getUUID());
       
        if (pos1 == null && pos2 == null) {
            player.displayClientMessage(Component.translatable(
                "message.plot.tool.no_selection"
            ), false);
        } else if (pos1 != null && pos2 == null) {
            player.displayClientMessage(Component.translatable(
                "message.plot.tool.selection_partial", pos1.toShortString()
            ), false);
        } else if (pos1 != null && pos2 != null) {
            long volume = Math.abs((long)(pos2.getX() - pos1.getX() + 1) *
                                  (pos2.getY() - pos1.getY() + 1) *
                                  (pos2.getZ() - pos1.getZ() + 1));
           
            player.displayClientMessage(Component.translatable(
                "message.plot.tool.selection_complete",
                pos1.toShortString(),
                pos2.toShortString(),
                Component.translatable("item.plot_selection_tool.size", String.format("%,d", volume)).getString()
            ), false);
        }
    }
   
    /**
     * Gibt Position 1 eines Spielers zurück
     */
    public static BlockPos getPosition1(UUID playerUUID) {
        return position1.get(playerUUID);
    }
   
    /**
     * Gibt Position 2 eines Spielers zurück
     */
    public static BlockPos getPosition2(UUID playerUUID) {
        return position2.get(playerUUID);
    }
   
    /**
     * Prüft ob Spieler eine vollständige Selection hat
     */
    public static boolean hasCompleteSelection(UUID playerUUID) {
        return position1.containsKey(playerUUID) && position2.containsKey(playerUUID);
    }
   
    /**
     * Löscht Selection eines Spielers
     */
    public static void clearSelection(UUID playerUUID) {
        position1.remove(playerUUID);
        position2.remove(playerUUID);
    }

    /**
     * Cleanup-Methode für Player Disconnect
     * Thread-safe cleanup aller Positionen für diesen Spieler
     */
    public static void cleanup(UUID playerUUID) {
        if (playerUUID == null) {
            return;
        }

        position1.remove(playerUUID);
        position2.remove(playerUUID);
    }
}