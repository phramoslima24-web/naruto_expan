package net.narutotoexpansion.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Common (server-authoritative) configuration. File: config/narutoto_expansion-common.toml */
public final class ExpansionConfig {
    public static final ForgeConfigSpec SPEC;

    // Integration
    public static final ForgeConfigSpec.BooleanValue REQUIRE_NINJA;
    // Beasts
    public static final ForgeConfigSpec.DoubleValue BEAST_HEALTH_MULT;
    public static final ForgeConfigSpec.DoubleValue BEAST_DAMAGE_MULT;
    public static final ForgeConfigSpec.BooleanValue BEASTS_AGGRESSIVE;
    public static final ForgeConfigSpec.DoubleValue WEAKENED_THRESHOLD;
    public static final ForgeConfigSpec.BooleanValue RELEASE_ON_DEATH;
    // Forms and modes
    public static final ForgeConfigSpec.DoubleValue ACTIVATION_CHAKRA_FRACTION;
    public static final ForgeConfigSpec.DoubleValue FORM_UPKEEP_PER_SECOND;
    public static final ForgeConfigSpec.DoubleValue POOL_REGEN_PER_SECOND;
    public static final ForgeConfigSpec.IntValue MASTERY_PER_SECOND_IN_FORM;
    public static final ForgeConfigSpec.IntValue MAX_MASTERY;
    // Bijuudama
    public static final ForgeConfigSpec.IntValue BOMB_MIN_FORM;
    public static final ForgeConfigSpec.IntValue BOMB_CHARGE_TICKS;
    public static final ForgeConfigSpec.IntValue BOMB_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.DoubleValue BOMB_BASE_RADIUS;
    public static final ForgeConfigSpec.DoubleValue BOMB_MAX_RADIUS;
    public static final ForgeConfigSpec.DoubleValue BOMB_CHAKRA_FRACTION;
    public static final ForgeConfigSpec.DoubleValue BOMB_POOL_COST;
    public static final ForgeConfigSpec.BooleanValue BOMB_TERRAIN_DAMAGE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("integration");
        REQUIRE_NINJA = b.comment("Require the player to have chosen a ninja path in Narutoto (variable 'ninja' >= 1) to become a host.")
                .define("requireNinja", true);
        b.pop();

        b.push("beasts");
        BEAST_HEALTH_MULT = b.comment("Multiplier applied to the base health of wild tailed beasts.")
                .defineInRange("healthMultiplier", 1.0, 0.05, 20.0);
        BEAST_DAMAGE_MULT = b.comment("Multiplier applied to the melee damage and Bijuudama radius of wild tailed beasts.")
                .defineInRange("damageMultiplier", 1.0, 0.0, 10.0);
        BEASTS_AGGRESSIVE = b.comment("If true, wild beasts attack players on sight. If false they only retaliate.")
                .define("aggressive", false);
        WEAKENED_THRESHOLD = b.comment("A beast can be sealed when its health is at or below this fraction of its maximum.")
                .defineInRange("weakenedThreshold", 0.25, 0.01, 0.9);
        RELEASE_ON_DEATH = b.comment("When a host dies, the beast is released as a weakened wild beast.")
                .define("releaseOnHostDeath", true);
        b.pop();

        b.push("forms");
        ACTIVATION_CHAKRA_FRACTION = b.comment("Fraction of the Narutoto chakra maximum spent per form level on activation.")
                .defineInRange("activationChakraFraction", 0.15, 0.0, 1.0);
        FORM_UPKEEP_PER_SECOND = b.comment("Beast chakra pool drained per second per form level while transformed.")
                .defineInRange("upkeepPerSecond", 1.0, 0.0, 100.0);
        POOL_REGEN_PER_SECOND = b.comment("Beast chakra pool regenerated per second while not transformed.")
                .defineInRange("poolRegenPerSecond", 1.0, 0.0, 100.0);
        MASTERY_PER_SECOND_IN_FORM = b.comment("Mastery gained per second while in a form (doubled in RAGE mode).")
                .defineInRange("masteryPerSecondInForm", 1, 0, 100);
        MAX_MASTERY = b.comment("Mastery cap per beast.")
                .defineInRange("maxMastery", 1000, 100, 100000);
        b.pop();

        b.push("bijuudama");
        BOMB_MIN_FORM = b.comment("Minimum active form level required to charge a Bijuudama.")
                .defineInRange("minForm", 2, 1, 4);
        BOMB_CHARGE_TICKS = b.comment("Ticks needed for a full charge.")
                .defineInRange("chargeTicks", 60, 20, 400);
        BOMB_COOLDOWN_TICKS = b.comment("Cooldown after throwing, in ticks.")
                .defineInRange("cooldownTicks", 200, 0, 12000);
        BOMB_BASE_RADIUS = b.comment("Explosion radius at full charge, before beast power and mastery factors.")
                .defineInRange("baseRadius", 6.0, 1.0, 64.0);
        BOMB_MAX_RADIUS = b.comment("Hard cap on the explosion radius.")
                .defineInRange("maxRadius", 20.0, 1.0, 64.0);
        BOMB_CHAKRA_FRACTION = b.comment("Fraction of the Narutoto chakra maximum spent when throwing.")
                .defineInRange("chakraFraction", 0.30, 0.0, 1.0);
        BOMB_POOL_COST = b.comment("Beast chakra pool spent when throwing.")
                .defineInRange("poolCost", 40.0, 0.0, 10000.0);
        BOMB_TERRAIN_DAMAGE = b.comment("Allow Bijuudama explosions to destroy blocks (still respects the mobGriefing game rule).")
                .define("terrainDamage", true);
        b.pop();

        SPEC = b.build();
    }

    private ExpansionConfig() {}
}
