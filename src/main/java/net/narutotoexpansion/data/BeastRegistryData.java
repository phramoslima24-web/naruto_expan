package net.narutotoexpansion.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * World-level registry: which player currently hosts each beast (one host per beast, like Narutoto does for Kurama).
 * Stored in the overworld's data storage under "narutoto_expansion_hosts".
 */
public class BeastRegistryData extends SavedData {
    private static final String NAME = "narutoto_expansion_hosts";
    private final Map<BeastType, UUID> hosts = new EnumMap<>(BeastType.class);

    public static BeastRegistryData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(BeastRegistryData::load, BeastRegistryData::new, NAME);
    }

    @Nullable
    public UUID getHost(BeastType type) {
        return hosts.get(type);
    }

    public boolean isFree(BeastType type) {
        return !hosts.containsKey(type);
    }

    public void setHost(BeastType type, UUID player) {
        hosts.put(type, player);
        setDirty();
    }

    public void clear(BeastType type) {
        if (hosts.remove(type) != null) {
            setDirty();
        }
    }

    public static BeastRegistryData load(CompoundTag tag) {
        BeastRegistryData data = new BeastRegistryData();
        for (BeastType t : BeastType.values()) {
            if (tag.hasUUID(t.key)) {
                data.hosts.put(t, tag.getUUID(t.key));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        hosts.forEach((type, uuid) -> tag.putUUID(type.key, uuid));
        return tag;
    }
}
