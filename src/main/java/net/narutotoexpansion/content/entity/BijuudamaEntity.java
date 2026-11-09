package net.narutotoexpansion.content.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.narutotoexpansion.config.ExpansionConfig;
import net.narutotoexpansion.data.BeastType;
import org.joml.Vector3f;

/**
 * Bijuudama: a beast-chakra sphere. Flies straight, detonates on any hit or after 6 s.
 * The explosion never damages its owner, destroys terrain only if the config AND the mobGriefing rule allow it,
 * and adds a beast-specific status effect to nearby entities.
 */
public class BijuudamaEntity extends Projectile {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(BijuudamaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> BEAST = SynchedEntityData.defineId(BijuudamaEntity.class, EntityDataSerializers.INT);
    private static final int MAX_LIFE = 120;

    private int life;

    public BijuudamaEntity(EntityType<? extends BijuudamaEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(RADIUS, 4.0f);
        entityData.define(BEAST, 0);
    }

    public float getRadius() {
        return entityData.get(RADIUS);
    }

    public void setRadius(float radius) {
        entityData.set(RADIUS, radius);
    }

    public int getBeastId() {
        return entityData.get(BEAST);
    }

    public void setBeastId(int id) {
        entityData.set(BEAST, id);
    }

    /** RGB 0xRRGGBB of the owning beast, white if unknown. Used by the renderer and the particles. */
    public int getColor() {
        BeastType type = BeastType.byId(getBeastId());
        return type == null ? 0xFFFFFF : type.color;
    }

    /** Visual size of the sphere, grows with the explosion radius. */
    public float getVisualScale() {
        return 0.8f + getRadius() * 0.25f;
    }

    @Override
    public void tick() {
        super.tick();
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hit)) {
            onHit(hit);
        }
        if (isRemoved()) return;

        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);

        int color = getColor();
        Vector3f rgb = new Vector3f(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f);
        if (level().isClientSide) {
            level().addParticle(new DustParticleOptions(rgb, 1.5f), getX(), getY(), getZ(), 0, 0, 0);
        } else {
            life++;
            if (life >= MAX_LIFE) {
                detonate();
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            detonate();
        }
    }

    private void detonate() {
        if (isRemoved() || !(level() instanceof ServerLevel server)) return;
        final Entity owner = getOwner();
        float radius = Math.min(getRadius(), ExpansionConfig.BOMB_MAX_RADIUS.get().floatValue());
        BeastType type = BeastType.byId(getBeastId());

        Level.ExplosionInteraction interaction = ExpansionConfig.BOMB_TERRAIN_DAMAGE.get()
                ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE;
        boolean fire = type != null && type.fireImmune && interaction != Level.ExplosionInteraction.NONE;
        DamageSource source = damageSources().explosion(this, owner);
        // In Minecraft 1.20.1 the damage calculator only customizes block resistance
        // and block destruction. Use the owner as the explosion source so vanilla
        // excludes that entity from its own explosion damage calculation.
        server.explode(owner != null ? owner : this, source, null,
                getX(), getY(), getZ(), radius, fire, interaction);

        MobEffect effect = statusEffectFor(type);
        if (effect != null) {
            AABB area = new AABB(blockPosition()).inflate(radius * 1.2);
            for (LivingEntity living : server.getEntitiesOfClass(LivingEntity.class, area, e -> e != owner)) {
                living.addEffect(new MobEffectInstance(effect, 100, 0));
            }
        }
        discard();
    }

    private static MobEffect statusEffectFor(BeastType type) {
        if (type == null) return null;
        return switch (type) {
            case SHUKAKU -> MobEffects.BLINDNESS;
            case SAIKEN -> MobEffects.POISON;
            case ISOBU -> MobEffects.MOVEMENT_SLOWDOWN;
            default -> null;
        };
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Radius", getRadius());
        tag.putInt("Beast", getBeastId());
        tag.putInt("Life", life);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setRadius(tag.getFloat("Radius"));
        setBeastId(tag.getInt("Beast"));
        life = tag.getInt("Life");
    }
}
