package net.narutotoexpansion.content.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.narutotoexpansion.config.ExpansionConfig;
import net.narutotoexpansion.data.BeastType;

import java.util.EnumSet;

/**
 * Wild tailed beast. Not hostile by default (see config). It can be weakened by damage; a weakened beast stops
 * attacking and can be sealed into a player with the Sealing Tag, which makes that player its jinchuriki.
 * Visuals are a placeholder model coded in Java (no external assets).
 */
public class TailedBeastEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_WEAKENED =
            SynchedEntityData.defineId(TailedBeastEntity.class, EntityDataSerializers.BOOLEAN);

    private final BeastType beast;
    private int bombCooldown = 100;

    public TailedBeastEntity(EntityType<? extends TailedBeastEntity> type, Level level, BeastType beast) {
        super(type, level);
        this.beast = beast;
        this.xpReward = 0;
        if (!level.isClientSide) {
            scaleWithConfig();
        }
    }

    public static AttributeSupplier.Builder createAttributes(BeastType type) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0 * type.power)
                .add(Attributes.ATTACK_DAMAGE, 14.0 * type.power)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.ARMOR, 8.0);
    }

    /** Config is read here (not in createAttributes) because common config is not loaded at attribute registration. */
    private void scaleWithConfig() {
        AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(health.getBaseValue() * ExpansionConfig.BEAST_HEALTH_MULT.get());
            setHealth(getMaxHealth());
        }
        AttributeInstance damage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(damage.getBaseValue() * ExpansionConfig.BEAST_DAMAGE_MULT.get());
        }
    }

    public BeastType getBeast() {
        return beast;
    }

    public boolean isWeakened() {
        return entityData.get(DATA_WEAKENED);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_WEAKENED, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new BeastBombGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 24.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                target -> ExpansionConfig.BEASTS_AGGRESSIVE.get()));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !isWeakened() && super.canAttack(target);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (isWeakened() && tickCount % 6 == 0) {
                level().addParticle(ParticleTypes.SMOKE, getRandomX(0.5), getY() + getBbHeight() * 0.8, getRandomZ(0.5), 0, 0.02, 0);
            }
            return;
        }
        boolean weak = getHealth() <= getMaxHealth() * ExpansionConfig.WEAKENED_THRESHOLD.get();
        if (weak != isWeakened()) {
            entityData.set(DATA_WEAKENED, weak);
        }
        if (weak && getTarget() != null) {
            setTarget(null);
        }
        if (bombCooldown > 0) {
            bombCooldown--;
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 2.5f;
    }

    /** Ranged attack: windup, then throw a Bijuudama at the target. */
    private static final class BeastBombGoal extends Goal {
        private static final int WINDUP_TICKS = 40;
        private final TailedBeastEntity mob;
        private int windup;

        BeastBombGoal(TailedBeastEntity mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = mob.getTarget();
            if (target == null || !target.isAlive() || mob.isWeakened() || mob.bombCooldown > 0) return false;
            double d = mob.distanceToSqr(target);
            return d > 100.0 && d < 1600.0 && mob.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive() && !mob.isWeakened() && windup < WINDUP_TICKS;
        }

        @Override
        public void start() {
            windup = 0;
            mob.getNavigation().stop();
        }

        @Override
        public void stop() {
            windup = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = mob.getTarget();
            if (target == null) return;
            mob.getLookControl().setLookAt(target, 30.0f, 30.0f);
            windup++;
            if (mob.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.ENCHANT, mob.getX(), mob.getY() + mob.getBbHeight() * 0.9, mob.getZ(),
                        6, 0.8, 0.4, 0.8, 0.1);
            }
            if (windup >= WINDUP_TICKS) {
                throwBomb(target);
                mob.bombCooldown = 160 + mob.random.nextInt(120);
            }
        }

        private void throwBomb(LivingEntity target) {
            Vec3 from = new Vec3(mob.getX(), mob.getY() + mob.getBbHeight() * 0.8, mob.getZ());
            Vec3 dir = target.getEyePosition().subtract(from).normalize();
            from = from.add(dir.scale(mob.getBbWidth() * 0.6));
            double radius = Math.min(ExpansionConfig.BOMB_MAX_RADIUS.get(),
                    5.0 * mob.beast.power * ExpansionConfig.BEAST_DAMAGE_MULT.get());
            BijuudamaEntity bomb = new BijuudamaEntity(ModEntities.BIJUUDAMA.get(), mob.level());
            bomb.setOwner(mob);
            bomb.setBeastId(mob.beast.id);
            bomb.setRadius((float) radius);
            bomb.setPos(from.x, from.y, from.z);
            bomb.setDeltaMovement(dir.scale(1.3));
            mob.level().addFreshEntity(bomb);
        }
    }
}
