package net.narutotoexpansion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.narutotoexpansion.data.ExpansionCapability;

/** Applies a server sync packet to the local player's data. Client-only; referenced via DistExecutor. */
public final class ClientSync {
    public static void apply(CompoundTag tag) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ExpansionCapability.get(player).loadFromSync(tag);
        }
    }

    private ClientSync() {}
}
