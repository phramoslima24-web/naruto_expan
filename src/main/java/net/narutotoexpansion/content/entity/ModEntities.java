package net.narutotoexpansion.content.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.narutotoexpansion.NarutotoExpansion;
import net.narutotoexpansion.data.BeastType;

import java.util.EnumMap;
import java.util.Map;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NarutotoExpansion.MODID);

    /** One entity type per implemented beast (Kurama is provided by Narutoto). */
    public static final Map<BeastType, RegistryObject<EntityType<TailedBeastEntity>>> BEASTS = new EnumMap<>(BeastType.class);

    public static final RegistryObject<EntityType<BijuudamaEntity>> BIJUUDAMA = ENTITIES.register("bijuudama",
            () -> EntityType.Builder.<BijuudamaEntity>of(BijuudamaEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .fireImmune()
                    .build(NarutotoExpansion.MODID + ":bijuudama"));

    static {
        for (BeastType type : BeastType.implemented()) {
            BEASTS.put(type, ENTITIES.register(type.key, () -> {
                EntityType.Builder<TailedBeastEntity> builder = EntityType.Builder
                        .<TailedBeastEntity>of((entityType, level) -> new TailedBeastEntity(entityType, level, type), MobCategory.CREATURE)
                        .sized(type.width, type.height)
                        .clientTrackingRange(16);
                if (type.fireImmune) {
                    builder.fireImmune();
                }
                return builder.build(NarutotoExpansion.MODID + ":" + type.key);
            }));
        }
    }

    @Mod.EventBusSubscriber(modid = NarutotoExpansion.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBusEvents {
        @SubscribeEvent
        public static void attributes(EntityAttributeCreationEvent event) {
            for (BeastType type : BeastType.implemented()) {
                event.put(BEASTS.get(type).get(), TailedBeastEntity.createAttributes(type).build());
            }
        }
    }

    private ModEntities() {}
}
