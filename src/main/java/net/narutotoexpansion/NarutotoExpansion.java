package net.narutotoexpansion;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.narutotoexpansion.config.ExpansionConfig;
import net.narutotoexpansion.content.entity.ModEntities;
import net.narutotoexpansion.content.item.ModCreativeTabs;
import net.narutotoexpansion.content.item.ModItems;
import net.narutotoexpansion.network.ExpansionNetwork;
import org.slf4j.Logger;

/**
 * Entry point of the Narutoto Expansion add-on.
 * Event handlers are registered through {@code @Mod.EventBusSubscriber} on their own classes.
 */
@Mod(NarutotoExpansion.MODID)
public class NarutotoExpansion {
    public static final String MODID = "narutoto_expansion";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NarutotoExpansion() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        modBus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ExpansionConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ExpansionNetwork::register);
    }
}
