package net.narutotoexpansion.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.narutotoexpansion.NarutotoExpansion;
import net.narutotoexpansion.config.ExpansionConfig;
import net.narutotoexpansion.core.BijuuService;
import net.narutotoexpansion.core.FormService;
import net.narutotoexpansion.data.BeastRegistryData;
import net.narutotoexpansion.data.BeastType;
import net.narutotoexpansion.data.ExpansionCapability;
import net.narutotoexpansion.data.ExpansionData;
import net.narutotoexpansion.network.ExpansionNetwork;

import java.util.UUID;

/** Forge-bus event handlers of the add-on (server side). */
@Mod.EventBusSubscriber(modid = NarutotoExpansion.MODID)
public final class PlayerEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            FormService.serverTick(player);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // Attribute modifiers are transient and the form is never persisted: start clean.
        FormService.clearModifiers(player);
        ExpansionData data = ExpansionCapability.get(player);
        BeastType type = BeastType.byId(data.hostBeast);
        if (type != null) {
            // Reconcile with the world registry (it may have been edited by an admin).
            BeastRegistryData registry = BeastRegistryData.get(player.server);
            UUID registered = registry.getHost(type);
            if (registered == null) {
                registry.setHost(type, player.getUUID());
            } else if (!registered.equals(player.getUUID())) {
                data.hostBeast = 0;
                data.pool = 0.0;
            }
        }
        ExpansionNetwork.sync(player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FormService.clearModifiers(player);
            ExpansionNetwork.sync(player);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ExpansionNetwork.sync(player);
        }
    }

    /** Reactive traits of the hosted beast while a form is active. */
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ExpansionData data = ExpansionCapability.get(player);
        BeastType type = BeastType.byId(data.hostBeast);
        if (type == null || data.form < 1) return;

        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity a ? a : null;
        switch (type) {
            case SHUKAKU -> {
                if (event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
                    event.setAmount(event.getAmount() * 0.5f);
                }
            }
            case MATATABI -> {
                if (attacker != null && attacker != player) {
                    attacker.setSecondsOnFire(3 + data.form);
                }
            }
            case SAIKEN -> {
                if (attacker != null && attacker != player) {
                    attacker.addEffect(new MobEffectInstance(MobEffects.POISON, 60 + 20 * data.form, 0));
                }
            }
            default -> { }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        // A host dies: the beast is released.
        if (event.getEntity() instanceof ServerPlayer dead) {
            ExpansionData data = ExpansionCapability.get(dead);
            if (data.isHost()) {
                BijuuService.release(dead, ExpansionConfig.RELEASE_ON_DEATH.get());
            }
        }
        // A transformed host kills something: mastery.
        if (event.getSource().getEntity() instanceof ServerPlayer killer && event.getEntity() != killer) {
            ExpansionData data = ExpansionCapability.get(killer);
            if (data.isHost() && data.form > 0) {
                FormService.addMastery(killer, data, 2);
            }
        }
    }

    private PlayerEvents() {}
}
