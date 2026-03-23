package de.rolandsw.schedulemc.weapon.attachment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeaponAttachments {
    public static final Attachment SCOPE = new Attachment(Attachment.Type.SCOPE, 1.2f, 0f, 1.1f, 1.0f);
    public static final Attachment SILENCER = new Attachment(Attachment.Type.SILENCER, 1.0f, 0.2f, 0.8f, 0.3f);
    public static final Attachment LASER = new Attachment(Attachment.Type.LASER, 1.0f, 0f, 1.1f, 1.0f);

    private static final Map<String, Attachment> BY_TYPE = new ConcurrentHashMap<>();
    static {
        BY_TYPE.put(SCOPE.getType().name(), SCOPE);
        BY_TYPE.put(SILENCER.getType().name(), SILENCER);
        BY_TYPE.put(LASER.getType().name(), LASER);
    }

    public static Attachment getByType(String typeName) {
        return BY_TYPE.get(typeName);
    }
}
