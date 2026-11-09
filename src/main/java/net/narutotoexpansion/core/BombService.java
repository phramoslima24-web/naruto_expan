package net.narutotoexpansion.core;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.narutotoexpansion.config.ExpansionConfig;
import net.narutotoexpansion.content.entity.BijuudamaEntity;
import net.narutotoexpansion.content.entity.ModEntities;
import net.narutotoexpansion.data.BeastType;
import net.narutotoexpansion.data.ExpansionCapability;
import net.narutotoexpansion.data.ExpansionData;
import net.narutotoexpansion.integration.NarutotoBridge;
import net.narutotoexpansion.network.ExpansionNetwork;
import org.joml.Vector3f;

/** Player Bijuudama: hold the key to charge, release to throw. All checks run on the server. */
public final class BombService {
    private static final int MIN_CHARGE_TICKS = 10;

    public static void startCharge(ServerPlayer player) {
        ExpansionData data = ExpansionCapability.get(player);
        BeastType type = BeastType.byId(data.hostBeast);
        if (type == null) {
            FormService.msg(player, "msg.narutoto_expansion.not_host");
            return;
        }
        int minForm = ExpansionConfig.BOMB_MIN_FORM.get();
        if (data.form < minForm) {
            player.displayClientMessage(Component.translatable("msg.narutoto_expansion.bomb_need_form", minForm), true);
            return;
        }
        if (data.bombCooldown > 0) {
            FormService.msg(player, "msg.narutoto_expansion.bomb_cooldown");
            return;
        }
        if (NarutotoBridge.isChakraBlocked(player)) {
            FormService.msg(player, "msg.narutoto_expansion.chakra_blocked");
            return;
        }
        if (data.pool < ExpansionConfig.BOMB_POOL_COST.get()) {
            FormService.msg(player, "msg.narutoto_expansion.pool_empty");
            return;
        }
        if (!NarutotoBridge.hasChakraFraction(player, ExpansionConfig.BOMB_CHAKRA_FRACTION.get())) {
            FormService.msg(player, "msg.narutoto_expansion.no_chakra");
            return;
        }
        data.charging = true;
        data.chargeTicks = 0;
        ExpansionNetwork.sync(player);
    }

    /** Called every tick from FormService#serverTick. */
    static void tickCharge(ServerPlayer player, ExpansionData data, BeastType type) {
        if (!data.charging) return;
        int minForm = ExpansionConfig.BOMB_MIN_FORM.get();
        if (!player.isAlive() || data.form < minForm) {
            cancel(player, data);
            return;
        }
        int max = ExpansionConfig.BOMB_CHARGE_TICKS.get();
        data.chargeTicks = Math.min(max, data.chargeTicks + 1);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6, 1, true, false, false));

        if (player.level() instanceof ServerLevel level) {
            float charge = data.chargeTicks / (float) max;
            int c = type.color;
            Vector3f rgb = new Vector3f(((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f);
            double size = 0.3 + 0.9 * charge;
            level.sendParticles(new DustParticleOptions(rgb, 1.2f + charge), player.getX(), player.getY() + player.getBbHeight() + 0.6,
                    player.getZ(), 4, size * 0.4, size * 0.4, size * 0.4, 0.0);
        }
        if (data.chargeTicks % 5 == 0) {
            ExpansionNetwork.sync(player);
        }
    }

    /** Key released: throws the Bijuudama if charged enough and affordable. */
    public static void release(ServerPlayer player) {
        ExpansionData data = ExpansionCapability.get(player);
        if (!data.charging) return;
        BeastType type = BeastType.byId(data.hostBeast);
        int ticks = data.chargeTicks;
        data.charging = false;
        data.chargeTicks = 0;
        if (type == null || ticks < MIN_CHARGE_TICKS) {
            ExpansionNetwork.sync(player);
            return;
        }
        if (data.pool < ExpansionConfig.BOMB_POOL_COST.get()
                || !NarutotoBridge.consumeChakraFraction(player, ExpansionConfig.BOMB_CHAKRA_FRACTION.get())) {
            FormService.msg(player, "msg.narutoto_expansion.no_chakra");
            ExpansionNetwork.sync(player);
            return;
        }
        data.pool = Math.max(0.0, data.pool - ExpansionConfig.BOMB_POOL_COST.get());

        float charge = Math.min(1.0f, ticks / (float) ExpansionConfig.BOMB_CHARGE_TICKS.get());
        float masteryFactor = 1.0f + 0.5f * data.mastery[type.id] / (float) ExpansionConfig.MAX_MASTERY.get();
        double radius = ExpansionConfig.BOMB_BASE_RADIUS.get() * (0.35 + 0.65 * charge) * type.power * masteryFactor;
        radius = Math.min(radius, ExpansionConfig.BOMB_MAX_RADIUS.get());

        Vec3 look = player.getLookAngle();
        Vec3 pos = player.getEyePosition().add(look.scale(1.5));
        BijuudamaEntity bomb = new BijuudamaEntity(ModEntities.BIJUUDAMA.get(), player.level());
        bomb.setOwner(player);
        bomb.setBeastId(type.id);
        bomb.setRadius((float) radius);
        bomb.setPos(pos.x, pos.y, pos.z);
        bomb.setDeltaMovement(look.scale(1.5));
        player.level().addFreshEntity(bomb);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS, 1.5f, 0.7f);

        data.bombCooldown = ExpansionConfig.BOMB_COOLDOWN_TICKS.get();
        FormService.addMastery(player, data, 3);
        ExpansionNetwork.sync(player);
    }

    private static void cancel(ServerPlayer player, ExpansionData data) {
        data.charging = false;
        data.chargeTicks = 0;
        ExpansionNetwork.sync(player);
    }

    private BombService() {}
}
