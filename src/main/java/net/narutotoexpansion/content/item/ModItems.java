package net.narutotoexpansion.content.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.narutotoexpansion.NarutotoExpansion;
import net.narutotoexpansion.content.entity.ModEntities;
import net.narutotoexpansion.data.BeastType;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NarutotoExpansion.MODID);

    public static final RegistryObject<Item> SEALING_TAG = ITEMS.register("sealing_tag",
            () -> new SealingTagItem(new Item.Properties().stacksTo(16)));

    /** Spawn eggs (testing and creative). Eight beasts; Kurama comes from Narutoto. */
    public static final Map<BeastType, RegistryObject<Item>> SPAWN_EGGS = new EnumMap<>(BeastType.class);

    static {
        for (BeastType type : BeastType.implemented()) {
            SPAWN_EGGS.put(type, ITEMS.register(type.key + "_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.BEASTS.get(type), type.color, type.accentColor, new Item.Properties())));
        }
    }

    private ModItems() {}
}
