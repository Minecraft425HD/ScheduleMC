package de.rolandsw.schedulemc.weapon.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class WeaponConfig {

    public static final ForgeConfigSpec SPEC;

    // Reichweite (Blöcke)
    public static final ForgeConfigSpec.IntValue PISTOL_RANGE;
    public static final ForgeConfigSpec.IntValue REVOLVER_RANGE;
    public static final ForgeConfigSpec.IntValue AK47_RANGE;
    public static final ForgeConfigSpec.IntValue MP5_RANGE;
    public static final ForgeConfigSpec.IntValue SNIPER_RANGE;
    public static final ForgeConfigSpec.IntValue SHOTGUN_RANGE;

    // Schaden (Herzen-Halbe pro Treffer)
    public static final ForgeConfigSpec.IntValue PISTOL_DAMAGE;
    public static final ForgeConfigSpec.IntValue REVOLVER_DAMAGE;
    public static final ForgeConfigSpec.IntValue AK47_DAMAGE;
    public static final ForgeConfigSpec.IntValue MP5_DAMAGE;
    public static final ForgeConfigSpec.IntValue SNIPER_DAMAGE;
    public static final ForgeConfigSpec.IntValue SHOTGUN_DAMAGE;

    // Trefferrate / Genauigkeit (0.0 - 1.0, höher = präziser)
    public static final ForgeConfigSpec.DoubleValue PISTOL_ACCURACY;
    public static final ForgeConfigSpec.DoubleValue REVOLVER_ACCURACY;
    public static final ForgeConfigSpec.DoubleValue AK47_ACCURACY;
    public static final ForgeConfigSpec.DoubleValue MP5_ACCURACY;
    public static final ForgeConfigSpec.DoubleValue SNIPER_ACCURACY;
    public static final ForgeConfigSpec.DoubleValue SHOTGUN_ACCURACY;

    // Feuerrate / Schuss-Cooldown (Ticks zwischen Schüssen, 20 = 1 s)
    public static final ForgeConfigSpec.IntValue PISTOL_COOLDOWN;
    public static final ForgeConfigSpec.IntValue REVOLVER_COOLDOWN;
    public static final ForgeConfigSpec.IntValue AK47_COOLDOWN;
    public static final ForgeConfigSpec.IntValue MP5_COOLDOWN;
    public static final ForgeConfigSpec.IntValue SNIPER_COOLDOWN;
    public static final ForgeConfigSpec.IntValue SHOTGUN_COOLDOWN;

    // Nachladezeit (Ticks, 20 = 1 s)
    public static final ForgeConfigSpec.IntValue PISTOL_RELOAD_TICKS;
    public static final ForgeConfigSpec.IntValue REVOLVER_RELOAD_TICKS;
    public static final ForgeConfigSpec.IntValue AK47_RELOAD_TICKS;
    public static final ForgeConfigSpec.IntValue MP5_RELOAD_TICKS;
    public static final ForgeConfigSpec.IntValue SNIPER_RELOAD_TICKS;
    public static final ForgeConfigSpec.IntValue SHOTGUN_RELOAD_TICKS;

    // Munitionstyp-Schadensmodifikatoren
    public static final ForgeConfigSpec.DoubleValue AMMO_AP_FIRE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue AMMO_AP_HIT_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue AMMO_RUBBER_DAMAGE_MULTIPLIER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("ScheduleMC WeaponSystem - maximum range per weapon (blocks)").push("weapon_ranges");
        PISTOL_RANGE = builder.comment("Pistol: maximum range in blocks").defineInRange("pistol_range", 60, 1, 1000);
        REVOLVER_RANGE = builder.comment("Revolver: maximum range in blocks").defineInRange("revolver_range", 80, 1, 1000);
        AK47_RANGE = builder.comment("AK47: maximum range in blocks").defineInRange("ak47_range", 120, 1, 1000);
        MP5_RANGE = builder.comment("MP5: maximum range in blocks").defineInRange("mp5_range", 80, 1, 1000);
        SNIPER_RANGE = builder.comment("Sniper rifle: maximum range in blocks").defineInRange("sniper_range", 400, 1, 2000);
        SHOTGUN_RANGE = builder.comment("Shotgun: maximum range in blocks").defineInRange("shotgun_range", 30, 1, 1000);
        builder.pop();

        builder.comment("Damage per hit (half-hearts), hit rate (accuracy 0..1), fire rate (cooldown ticks) and reload time (ticks) per weapon").push("weapon_stats");
        PISTOL_DAMAGE = builder.comment("Pistol: base damage per hit").defineInRange("pistol_damage", 6, 1, 100);
        PISTOL_ACCURACY = builder.comment("Pistol: hit rate / accuracy (0..1)").defineInRange("pistol_accuracy", 0.95, 0.0, 1.0);
        PISTOL_COOLDOWN = builder.comment("Pistol: ticks between shots (20 = 1s)").defineInRange("pistol_cooldown", 10, 1, 200);
        PISTOL_RELOAD_TICKS = builder.comment("Pistol: reload time in ticks (20 = 1s)").defineInRange("pistol_reload_ticks", 30, 0, 200);

        REVOLVER_DAMAGE = builder.comment("Revolver: base damage per hit").defineInRange("revolver_damage", 10, 1, 100);
        REVOLVER_ACCURACY = builder.comment("Revolver: hit rate / accuracy (0..1)").defineInRange("revolver_accuracy", 0.90, 0.0, 1.0);
        REVOLVER_COOLDOWN = builder.comment("Revolver: ticks between shots (20 = 1s)").defineInRange("revolver_cooldown", 15, 1, 200);
        REVOLVER_RELOAD_TICKS = builder.comment("Revolver: reload time in ticks (20 = 1s)").defineInRange("revolver_reload_ticks", 40, 0, 200);

        AK47_DAMAGE = builder.comment("AK47: base damage per hit").defineInRange("ak47_damage", 8, 1, 100);
        AK47_ACCURACY = builder.comment("AK47: hit rate / accuracy (0..1)").defineInRange("ak47_accuracy", 0.85, 0.0, 1.0);
        AK47_COOLDOWN = builder.comment("AK47: ticks between shots (20 = 1s)").defineInRange("ak47_cooldown", 3, 1, 200);
        AK47_RELOAD_TICKS = builder.comment("AK47: reload time in ticks (20 = 1s)").defineInRange("ak47_reload_ticks", 50, 0, 200);

        MP5_DAMAGE = builder.comment("MP5: base damage per hit").defineInRange("mp5_damage", 5, 1, 100);
        MP5_ACCURACY = builder.comment("MP5: hit rate / accuracy (0..1)").defineInRange("mp5_accuracy", 0.80, 0.0, 1.0);
        MP5_COOLDOWN = builder.comment("MP5: ticks between shots (20 = 1s)").defineInRange("mp5_cooldown", 2, 1, 200);
        MP5_RELOAD_TICKS = builder.comment("MP5: reload time in ticks (20 = 1s)").defineInRange("mp5_reload_ticks", 45, 0, 200);

        SNIPER_DAMAGE = builder.comment("Sniper rifle: base damage per hit").defineInRange("sniper_damage", 20, 1, 100);
        SNIPER_ACCURACY = builder.comment("Sniper rifle: hit rate / accuracy (0..1)").defineInRange("sniper_accuracy", 0.98, 0.0, 1.0);
        SNIPER_COOLDOWN = builder.comment("Sniper rifle: ticks between shots (20 = 1s)").defineInRange("sniper_cooldown", 40, 1, 200);
        SNIPER_RELOAD_TICKS = builder.comment("Sniper rifle: reload time in ticks (20 = 1s)").defineInRange("sniper_reload_ticks", 60, 0, 200);

        SHOTGUN_DAMAGE = builder.comment("Shotgun: base damage per pellet (fires multiple pellets)").defineInRange("shotgun_damage", 4, 1, 100);
        SHOTGUN_ACCURACY = builder.comment("Shotgun: hit rate / accuracy (0..1)").defineInRange("shotgun_accuracy", 0.60, 0.0, 1.0);
        SHOTGUN_COOLDOWN = builder.comment("Shotgun: ticks between shots (20 = 1s)").defineInRange("shotgun_cooldown", 25, 1, 200);
        SHOTGUN_RELOAD_TICKS = builder.comment("Shotgun: reload time in ticks (20 = 1s)").defineInRange("shotgun_reload_ticks", 60, 0, 200);
        builder.pop();

        builder.comment("Ammo type damage multipliers").push("ammo");
        AMMO_AP_FIRE_MULTIPLIER = builder
                .comment("Armor-piercing ammo: damage multiplier applied when firing")
                .defineInRange("ap_fire_multiplier", 0.8, 0.0, 10.0);
        AMMO_AP_HIT_MULTIPLIER = builder
                .comment("Armor-piercing ammo: extra damage multiplier applied on hit")
                .defineInRange("ap_hit_multiplier", 1.5, 0.0, 10.0);
        AMMO_RUBBER_DAMAGE_MULTIPLIER = builder
                .comment("Rubber ammo (non-lethal): damage multiplier")
                .defineInRange("rubber_damage_multiplier", 0.3, 0.0, 10.0);
        builder.pop();

        SPEC = builder.build();
    }
}
