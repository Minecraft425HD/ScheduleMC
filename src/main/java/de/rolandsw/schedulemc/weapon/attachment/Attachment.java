package de.rolandsw.schedulemc.weapon.attachment;

public class Attachment {
    public enum Type { SCOPE, SILENCER }

    private final Type type;
    private final float damageMultiplier;
    private final float spreadMultiplier;
    private final float accuracyMultiplier;
    private final float soundMultiplier;

    public Attachment(Type type, float damageMultiplier, float spreadMultiplier,
                      float accuracyMultiplier, float soundMultiplier) {
        this.type = type;
        this.damageMultiplier = damageMultiplier;
        this.spreadMultiplier = spreadMultiplier;
        this.accuracyMultiplier = accuracyMultiplier;
        this.soundMultiplier = soundMultiplier;
    }

    public Type getType() { return type; }
    public float getDamageMultiplier() { return damageMultiplier; }
    public float getSpreadMultiplier() { return spreadMultiplier; }
    public float getAccuracyMultiplier() { return accuracyMultiplier; }
    public float getSoundMultiplier() { return soundMultiplier; }
}
