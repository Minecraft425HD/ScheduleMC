package de.rolandsw.schedulemc.weapon.attachment;

import net.minecraft.world.item.Item;

public abstract class BaseAttachmentItem extends Item {
    private final Attachment attachment;

    public BaseAttachmentItem(Attachment attachment) {
        super(new Item.Properties().stacksTo(1));
        this.attachment = attachment;
    }

    public Attachment getModAttachment() {
        return attachment;
    }
}
