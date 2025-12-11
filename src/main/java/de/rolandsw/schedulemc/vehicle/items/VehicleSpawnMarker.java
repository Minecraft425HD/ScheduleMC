package de.rolandsw.schedulemc.vehicle.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Marker item to set vehicle spawn point for car dealer.
 * Players can place this marker to designate where their purchased vehicles will spawn.
 * Only one spawn marker per player is active at a time.
 */
public class VehicleSpawnMarker extends Item {

    // Server-side storage of spawn markers per player
    private static final Map<UUID, SpawnMarkerData> SPAWN_MARKERS = new HashMap<>();

    public VehicleSpawnMarker(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            BlockPos clickedPos = context.getClickedPos();
            BlockPos spawnPos = clickedPos.above();

            // Store spawn marker for this player
            SPAWN_MARKERS.put(player.getUUID(), new SpawnMarkerData(
                spawnPos,
                ((ServerLevel) level).dimension().location().toString()
            ));

            // Send success message
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));
            player.sendSystemMessage(Component.literal("✓ ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal("FAHRZEUG-SPAWNPUNKT GESETZT").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
            player.sendSystemMessage(Component.literal("Position: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("X: %d, Y: %d, Z: %d", spawnPos.getX(), spawnPos.getY(), spawnPos.getZ())).withStyle(ChatFormatting.AQUA)));
            player.sendSystemMessage(Component.literal("Gekaufte Fahrzeuge werden hier gespawnt.").withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.GREEN));

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Rechtsklick auf Block: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal("Spawnpunkt setzen").withStyle(ChatFormatting.AQUA)));
        tooltip.add(Component.literal("Gekaufte Fahrzeuge spawnen hier").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Nur ein aktiver Spawnpunkt pro Spieler").withStyle(ChatFormatting.DARK_GRAY));
    }

    /**
     * Get spawn marker for player
     */
    public static SpawnMarkerData getSpawnMarker(UUID playerUUID) {
        return SPAWN_MARKERS.get(playerUUID);
    }

    /**
     * Check if player has a spawn marker set
     */
    public static boolean hasSpawnMarker(UUID playerUUID) {
        return SPAWN_MARKERS.containsKey(playerUUID);
    }

    /**
     * Remove spawn marker for player
     */
    public static void removeSpawnMarker(UUID playerUUID) {
        SPAWN_MARKERS.remove(playerUUID);
    }

    /**
     * Data class to store spawn marker information
     */
    public static class SpawnMarkerData {
        private final BlockPos position;
        private final String dimension;

        public SpawnMarkerData(BlockPos position, String dimension) {
            this.position = position;
            this.dimension = dimension;
        }

        public BlockPos getPosition() {
            return position;
        }

        public String getDimension() {
            return dimension;
        }
    }
}
