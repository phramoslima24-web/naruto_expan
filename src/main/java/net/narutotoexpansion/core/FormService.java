package net.narutotoexpansion.core;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.narutotoexpansion.config.ExpansionConfig;
import net.narutotoexpansion.data.BeastType;
import net.narutotoexpansion.data.ChakraMode;
import net.narutotoexpansion.data.ExpansionCapability;
import net.narutotoexpansion.data.ExpansionData;
import net.narutotoexpansion.data.FormRules;
import net.narutotoexpansion.integration.NarutotoBridge;
import net.narutotoexpansion.network.ExpansionNetwork;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * Server-side logic for transformations (form levels), chakra modes, the beast chakra pool and mastery.
 * Every player-visible number is derived from {@link FormRules}, {@link BeastType#power} and the config.
 */
public final class FormService {
    private static final UUID ID_DAMAGE = UUID.fromString("6d1f0a52-3b0e-4c53-9a71-0b5d1c000001");
    private static final UUID ID_SPEED = UUID.fromString("6d1f0a52-3b0e-4c53-9a71-0b5d1c000002");
    private static final UUID ID_HEALTH = UUID.fromString("6d1f0a52-3b0e-4c53-9a71-0b5d1c000003");
    private static final UUID ID_ARMOR = UUID.fromString("6d1f0a52-3b0e-4c53-9a71-0b5d1c000004");
    private static final UUID ID_KNOCKBACK = UUID.fromString("6d1f0a52-3b0e-4c53-9a71-0b5d1c000005");
    private static final UUID ID_RAGE_DAMAGE = UUID.fromString("6d1f0a52-3b0e-4c53-9a71-0b5d1c000006");
    private static final UUID ID_RAGE_ARMOR = UUID.fromString("6d1f0a52-3b0e-4c53-9a71-0b5d1c000007");
    private static final UUID[] ALL_IDS = {ID_DAMAGE, ID_SPEED, ID_HEALTH, ID_ARMOR, ID_KNOCKBACK, ID_RAGE_DAMAGE, ID_RAGE_ARMOR};

    // ---------------------------------------------------------------- player actions

    /** Key "transform": switches the form on (highest selected/unlocked level) or off. */
    public static void toggle(ServerPlayer player) {
        ExpansionData data = ExpansionCapability.get(player);
        BeastType type = BeastType.byId(data.hostBeast);
        if (type == null) {
            msg(player, "msg.narutoto_expansion.not_host");
            return;
        }
        if (data.form > 0) {
            deactivate(player, false);
            return;
        }
        if (ExpansionConfig.REQUIRE_NINJA.get() && !NarutotoBridge.isNinja(player)) {
            msg(player, "msg.narutoto_expansion.not_ninja");
            return;
        }
        if (NarutotoBridge.isChakraBlocked(player)) {
            msg(player, "msg.narutoto_expansion.chakra_blocked");
            return;
        }
        int unlocked = FormRules.unlocked(type, data.mastery[type.id]);
        int level = data.selectedForm > 0 ? Math.min(data.selectedForm, unlocked) : unlocked;
        if (data.pool < 10.0) {
            msg(player, "msg.narutoto_expansion.pool_empty");
            return;
        }
        double cost = Math.min(0.8, ExpansionConfig.ACTIVATION_CHAKRA_FRACTION.get() * level);
        if (!NarutotoBridge.consumeChakraFraction(player, cost)) {
            msg(player, "msg.narutoto_expansion.no_chakra");
            return;
        }
        data.form = level;
        applyModifiers(player, data, type);
        burst(player, type, 24 + level * 8);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 0.6f);
        player.displayClientMessage(Component.translatable("msg.narutoto_expansion.form_on", level), true);
        ExpansionNetwork.sync(player);
    }

    /** Key "transform" + sneak: cycles the preferred form level among the unlocked ones. */
    public static void cycleSelectedForm(ServerPlayer player) {
        ExpansionData data = ExpansionCapability.get(player);
        BeastType type = BeastType.byId(data.hostBeast);
        if (type == null) {
            msg(player, "msg.narutoto_expansion.not_host");
            return;
        }
        int unlocked = FormRules.unlocked(type, data.mastery[type.id]);
        int current = data.selectedForm > 0 ? Math.min(data.selectedForm, unlocked) : unlocked;
        int next = current % unlocked + 1;
        setSelectedForm(player, data, type, next);
    }

    /** Used by the cycle key and by the /bijuu form command. */
    public static void setSelectedForm(ServerPlayer player, ExpansionData data, BeastType type, int level) {
        int unlocked = FormRules.unlocked(type, data.mastery[type.id]);
        level = Math.max(1, Math.min(level, unlocked));
        int old = data.form;
        if (old > 0 && level != old) {
            if (level > old) {
                double cost = Math.min(0.8, ExpansionConfig.ACTIVATION_CHAKRA_FRACTION.get() * (level - old));
                if (!NarutotoBridge.consumeChakraFraction(player, cost)) {
                    msg(player, "msg.narutoto_expansion.no_chakra");
                    return;
                }
            }
            data.form = level;
            applyModifiers(player, data, type);
            burst(player, type, 16);
        }
        data.selectedForm = level;
        player.displayClientMessage(Component.translatable("msg.narutoto_expansion.form_selected", level), true);
        ExpansionNetwork.sync(player);
    }

    /** Key "mode": cycles SUPPRESSED -> SYNC -> RAGE. RAGE is skipped when no form is active. */
    public static void cycleMode(ServerPlayer player) {
        ExpansionData data = ExpansionCapability.get(player);
        BeastType type = BeastType.byId(data.hostBeast);
        if (type == null) {
            msg(player, "msg.narutoto_expansion.not_host");
            return;
        }
        ChakraMode next = data.mode.next();
        if (next == ChakraMode.RAGE && data.form == 0) {
            next = next.next();
        }
        data.mode = next;
        if (data.form > 0) {
            applyModifiers(player, data, type);
        }
        player.displayClientMessage(Component.translatable("msg.narutoto_expansion.mode_changed",
                Component.translatable(next.translationKey())), true);
        ExpansionNetwork.sync(player);
    }

    public static void deactivate(ServerPlayer player, boolean exhausted) {
        ExpansionData data = ExpansionCapability.get(player);
        data.form = 0;
        data.charging = false;
        data.chargeTicks = 0;
        if (data.mode == ChakraMode.RAGE) {
            data.mode = ChakraMode.SUPPRESSED;
        }
        clearModifiers(player);
        if (exhausted) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, true));
            msg(player, "msg.narutoto_expansion.exhausted");
        } else {
            player.displayClientMessage(Component.translatable("msg.narutoto_expansion.form_off"), true);
        }
        ExpansionNetwork.sync(player);
    }

    // ---------------------------------------------------------------- mastery

    public static void addMastery(ServerPlayer player, ExpansionData data, int amount) {
        BeastType type = BeastType.byId(data.hostBeast);
        if (type == null || amount <= 0) return;
        int before = FormRules.unlocked(type, data.mastery[type.id]);
        data.mastery[type.id] = FormRules.clampMastery(data.mastery[type.id] + amount);
        int after = FormRules.unlocked(type, data.mastery[type.id]);
        if (after > before) {
            player.sendSystemMessage(Component.translatable("msg.narutoto_expansion.form_unlocked", after));
        }
    }

    // ---------------------------------------------------------------- tick

    /** Called every server tick for every player (see PlayerEvents). */
    public static void serverTick(ServerPlayer player) {
        ExpansionData data = ExpansionCapability.get(player);
        if (data.bombCooldown > 0) {
            data.bombCooldown--;
        }
        BeastType type = BeastType.byId(data.hostBeast);
        if (type == null) {
            return;
        }
        BombService.tickCharge(player, data, type);
        if (player.tickCount % 20 == 0) {
            everySecond(player, data, type);
        }
        if (data.form > 0 && player.tickCount % 4 == 0) {
            aura(player, type, data.form, data.mode == ChakraMode.RAGE);
        }
    }

    private static void everySecond(ServerPlayer player, ExpansionData data, BeastType type) {
        double max = FormRules.poolMax(data.mastery[type.id]);
        boolean rage = data.mode == ChakraMode.RAGE;

        if (data.form > 0) {
            double upkeep = data.form * ExpansionConfig.FORM_UPKEEP_PER_SECOND.get() * (rage ? 2.0 : 1.0);
            data.pool -= upkeep;
            addMastery(player, data, ExpansionConfig.MASTERY_PER_SECOND_IN_FORM.get() * (rage ? 2 : 1));
            applyTraitEffects(player, type, data.form);
        } else {
            data.pool += ExpansionConfig.POOL_REGEN_PER_SECOND.get();
            if (rage) {
                data.mode = ChakraMode.SUPPRESSED;
            }
        }

        if (data.mode == ChakraMode.SYNC) {
            if (data.pool >= 2.0 && NarutotoBridge.addChakraFraction(player, 0.02)) {
                data.pool -= 2.0;
            }
            if ((player.tickCount / 20) % 5 == 0) {
                addMastery(player, data, 1);
            }
        }

        data.pool = Math.max(0.0, Math.min(max, data.pool));
        if (data.form > 0 && data.pool <= 0.0) {
            deactivate(player, true);
            return;
        }
        ExpansionNetwork.sync(player);
    }

    // ---------------------------------------------------------------- modifiers and traits

    private static void applyModifiers(ServerPlayer player, ExpansionData data, BeastType type) {
        float p = type.power;
        int level = data.form;
        boolean rage = data.mode == ChakraMode.RAGE;

        set(player, Attributes.ATTACK_DAMAGE, ID_DAMAGE, "bijuu_damage", 1.5 * level * p, AttributeModifier.Operation.ADDITION);
        set(player, Attributes.MOVEMENT_SPEED, ID_SPEED, "bijuu_speed", 0.08 * level * p, AttributeModifier.Operation.MULTIPLY_BASE);
        set(player, Attributes.MAX_HEALTH, ID_HEALTH, "bijuu_health", 4.0 * level * p, AttributeModifier.Operation.ADDITION);
        set(player, Attributes.ARMOR, ID_ARMOR, "bijuu_armor", 1.5 * level * p, AttributeModifier.Operation.ADDITION);
        set(player, Attributes.KNOCKBACK_RESISTANCE, ID_KNOCKBACK, "bijuu_knockback", 0.15 * level, AttributeModifier.Operation.ADDITION);
        if (rage) {
            set(player, Attributes.ATTACK_DAMAGE, ID_RAGE_DAMAGE, "bijuu_rage_damage", 2.0 * level * p, AttributeModifier.Operation.ADDITION);
            set(player, Attributes.ARMOR, ID_RAGE_ARMOR, "bijuu_rage_armor", -1.0 * level * p, AttributeModifier.Operation.ADDITION);
        } else {
            remove(player, Attributes.ATTACK_DAMAGE, ID_RAGE_DAMAGE);
            remove(player, Attributes.ARMOR, ID_RAGE_ARMOR);
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    /** Removes every attribute modifier this add-on may have added. Safe to call at any time. */
    public static void clearModifiers(ServerPlayer player) {
        Attribute[] attrs = {Attributes.ATTACK_DAMAGE, Attributes.MOVEMENT_SPEED, Attributes.MAX_HEALTH,
                Attributes.ARMOR, Attributes.KNOCKBACK_RESISTANCE};
        for (Attribute a : attrs) {
            AttributeInstance inst = player.getAttribute(a);
            if (inst == null) continue;
            for (UUID id : ALL_IDS) {
                inst.removeModifier(id);
            }
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void set(ServerPlayer player, Attribute attribute, UUID id, String name, double value,
                            AttributeModifier.Operation op) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return;
        inst.removeModifier(id);
        inst.addTransientModifier(new AttributeModifier(id, name, value, op));
    }

    private static void remove(ServerPlayer player, Attribute attribute, UUID id) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst != null) {
            inst.removeModifier(id);
        }
    }

    /** Passive potion-style traits, refreshed once per second while transformed. */
    private static void applyTraitEffects(ServerPlayer player, BeastType type, int level) {
        if (level >= 2) {
            effect(player, MobEffects.REGENERATION, 0);
        }
        switch (type) {
            case MATATABI -> effect(player, MobEffects.FIRE_RESISTANCE, 0);
            case ISOBU -> {
                effect(player, MobEffects.WATER_BREATHING, 0);
                if (player.isInWater()) {
                    effect(player, MobEffects.DOLPHINS_GRACE, 0);
                }
            }
            case SON_GOKU -> {
                effect(player, MobEffects.FIRE_RESISTANCE, 0);
                effect(player, MobEffects.DAMAGE_BOOST, level >= 3 ? 1 : 0);
            }
            case KOKUO -> effect(player, MobEffects.MOVEMENT_SPEED, level >= 3 ? 1 : 0);
            case CHOMEI -> {
                effect(player, MobEffects.SLOW_FALLING, 0);
                effect(player, MobEffects.JUMP, 1);
            }
            case GYUKI -> effect(player, MobEffects.DAMAGE_RESISTANCE, level >= 3 ? 1 : 0);
            default -> { /* Shukaku and Saiken traits are reactive, see PlayerEvents#onHurt */ }
        }
    }

    private static void effect(ServerPlayer player, MobEffect effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, 60, amplifier, true, false, false));
    }

    // ---------------------------------------------------------------- visuals

    private static void aura(ServerPlayer player, BeastType type, int level, boolean rage) {
        if (!(player.level() instanceof ServerLevel level2)) return;
        int color = rage ? 0xD02020 : type.color;
        Vector3f rgb = new Vector3f(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f);
        level2.sendParticles(new DustParticleOptions(rgb, 0.8f + 0.2f * level), player.getX(), player.getY() + 1.0, player.getZ(),
                3 + level * 2, 0.35, 0.7, 0.35, 0.0);
    }

    static void burst(ServerPlayer player, BeastType type, int count) {
        if (!(player.level() instanceof ServerLevel level)) return;
        level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 1.0, player.getZ(), count, 0.6, 0.9, 0.6, 0.05);
    }

    static void msg(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

    private FormService() {}
}
