package net.narutotoexpansion.content.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.narutotoexpansion.NarutotoExpansion;
import net.narutotoexpansion.data.BeastType;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NarutotoExpansion.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.narutoto_expansion"))
            .icon(() -> new ItemStack(ModItems.SEALING_TAG.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.SEALING_TAG.get());
                for (BeastType type : BeastType.implemented()) {
                    output.accept(ModItems.SPAWN_EGGS.get(type).get());
                }
            })
            .build());

    private ModCreativeTabs() {}
}
