package net.narutotoexpansion.data;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.narutotoexpansion.NarutotoExpansion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Capability registration, attachment, persistence and clone handling for {@link ExpansionData}. */
public final class ExpansionCapability {
    public static final Capability<ExpansionData> DATA = CapabilityManager.get(new CapabilityToken<>() {});
    private static final ResourceLocation KEY = new ResourceLocation(NarutotoExpansion.MODID, "data");

    /** Returns the data of a player, or a throwaway empty instance if the capability is missing. */
    public static ExpansionData get(Player player) {
        return player.getCapability(DATA).orElseGet(ExpansionData::new);
    }

    private static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final ExpansionData data = new ExpansionData();
        private final LazyOptional<ExpansionData> optional = LazyOptional.of(() -> data);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == DATA ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.save();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.load(nbt);
        }
    }

    @Mod.EventBusSubscriber(modid = NarutotoExpansion.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBusEvents {
        @SubscribeEvent
        public static void register(RegisterCapabilitiesEvent event) {
            event.register(ExpansionData.class);
        }
    }

    @Mod.EventBusSubscriber(modid = NarutotoExpansion.MODID)
    public static final class ForgeBusEvents {
        @SubscribeEvent
        public static void attach(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                event.addCapability(KEY, new Provider());
            }
        }

        @SubscribeEvent
        public static void clone(PlayerEvent.Clone event) {
            event.getOriginal().reviveCaps();
            ExpansionData from = get(event.getOriginal());
            ExpansionData to = get(event.getEntity());
            // After death the host link is already cleared by the death handler; mastery is always kept.
            to.copyFrom(from, true);
            event.getOriginal().invalidateCaps();
        }
    }

    private ExpansionCapability() {}
}
