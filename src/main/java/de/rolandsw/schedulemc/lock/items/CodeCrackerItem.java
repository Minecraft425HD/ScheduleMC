package de.rolandsw.schedulemc.lock.items;

import de.rolandsw.schedulemc.lock.LockType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Rarity;

import java.util.List;

/**
 * Code-Cracker: Knackt nur Zahlenschloesser (COMBINATION).
 *
 * Haltbarkeit: 10 Versuche.
 * Erfolgschance: 50%.
 * Kein Alarm bei Fehlschlag.
 */
public class CodeCrackerItem extends HackingToolItem {

    public CodeCrackerItem() {
        super(10, Rarity.COMMON);
    }

    @Override
    protected boolean canHackLockType(LockType type) {
        return type == LockType.COMBINATION;
    }

    @Override
    protected String getToolName() {
        return "Code-Cracker";
    }

    @Override
    protected String getToolColor() {
        return "\u00A7a";
    }

    @Override
    protected void appendSpecificTooltip(List<Component> tips) {
        tips.add(Component.literal("\u00A78Knackt:"));
        tips.add(Component.literal("\u00A76  Zahlenschloss: \u00A7a\u2714"));
        tips.add(Component.literal("\u00A75  Dual-Lock: \u00A7c\u2716"));
    }
}
