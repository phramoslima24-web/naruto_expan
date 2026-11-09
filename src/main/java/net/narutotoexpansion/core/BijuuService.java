package net.narutotoexpansion.core;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.narutotoexpansion.config.ExpansionConfig;
import net.narutotoexpansion.content.entity.ModEntities;
import net.narutotoexpansion.content.entity.TailedBeastEntity;
import net.narutotoexpansion.data.BeastRegistryData;
import net.narutotoexpansion.data.BeastType;
import net.narutotoexpansion.data.ChakraMode;
import net.narutotoexpansion.data.ExpansionCapability;
import net.narutotoexpansion.data.ExpansionData;
import net.narutotoexpansion.data.FormRules;
import net.narutotoexpansion.integration.NarutotoBridge;
import net.narutotoexpansion.network.ExpansionNetwork;

import javax.annotation.Nullable;
import java.util.UUID;

/** Jinchuriki system: becoming a host, releasing a beast and spawning wild beasts. */
public final class BijuuService {

    public enum Result {
        OK, EXTERNAL, UNAVAILABLE, NOT_NINJA, ALREADY_HOST, NINE_TAILS_HOST, BEAST_TAKEN;

        public Component message() {
            return Component.translatable("msg.narutoto_expansion.result." + name().toLowerCase());
        }
    }

    /**
     * Makes {@code player} the host of {@code type}.
     *
     * @param ignoreNinja true for admin commands (skips the Narutoto "ninja" requirement)
     */
    public static Result tryHost(ServerPlayer player, BeastType type, boolean ignoreNinja) {
        if (type.isExternal()) return Result.EXTERNAL;
        if (!NarutotoBridge.isAvailable(player)) return Result.UNAVAILABLE;
        if (!ignoreNinja && ExpansionConfig.REQUIRE_NINJA.get() && !NarutotoBridge.isNinja(player)) return Result.NOT_NINJA;
        if (NarutotoBridge.isNineTailsHost(player)) return Result.NINE_TAILS_HOST;

        ExpansionData data = ExpansionCapability.get(player);
        if (data.isHost()) return Result.ALREADY_HOST;

        BeastRegistryData registry = BeastRegistryData.get(player.server);
        UUID current = registry.getHost(type);
        if (current != null && !current.equals(player.getUUID())) return Result.BEAST_TAKEN;

        registry.setHost(type, player.getUUID());
        data.hostBeast = type.id;
        data.form = 0;
        data.mode = ChakraMode.SUPPRESSED;
        data.pool = FormRules.poolMax(data.mastery[type.id]);
        ExpansionNetwork.sync(player);
        player.server.getPlayerList().broadcastSystemMessage(
                Component.translatable("msg.narutoto_expansion.sealed_broadcast", player.getDisplayName(),
                        Component.translatable(type.translationKey())), false);
        return Result.OK;
    }

    /**
     * Frees the beast of {@code player}. Mastery is kept.
     *
     * @param spawnBeast if true, the beast reappears nearby as a weakened wild beast
     */
    public static void release(ServerPlayer player, boolean spawnBeast) {
        ExpansionData data = ExpansionCapability.get(player);
        BeastType type = BeastType.byId(data.hostBeast);
        if (type == null) return;

        if (data.form > 0 || data.charging) {
            FormService.deactivate(player, false);
        }
        FormService.clearModifiers(player);
        data.hostBeast = 0;
        data.form = 0;
        data.charging = false;
        data.mode = ChakraMode.SUPPRESSED;
        data.pool = 0.0;
        BeastRegistryData.get(player.server).clear(type);

        if (spawnBeast && player.level() instanceof ServerLevel level) {
            spawnBeast(level, type, player.position(), 0.3f);
        }
        ExpansionNetwork.sync(player);
    }

    /** Spawns a wild beast. {@code healthFraction} in (0,1]. Returns null for the external Kurama. */
    @Nullable
    public static TailedBeastEntity spawnBeast(ServerLevel level, BeastType type, Vec3 pos, float healthFraction) {
        if (type.isExternal()) return null;
        TailedBeastEntity beast = ModEntities.BEASTS.get(type).get().create(level);
        if (beast == null) return null;
        beast.moveTo(pos.x, pos.y, pos.z, level.random.nextFloat() * 360.0f, 0.0f);
        beast.setHealth(Math.max(1.0f, beast.getMaxHealth() * healthFraction));
        level.addFreshEntity(beast);
        return beast;
    }

    private BijuuService() {}
}
