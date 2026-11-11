package net.narutotoexpansion.integration;

import net.mcreator.narutoto.init.NarutotoModMobEffects;
import net.mcreator.narutoto.network.NarutotoModVariables;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.narutotoexpansion.NarutotoExpansion;

import java.util.Optional;

/**
 * The ONLY class of this add-on that touches Narutoto classes.
 * <p>
 * What it uses from Narutoto 0.4.1 (all public API of the original JAR, nothing is modified):
 * <ul>
 *   <li>{@code NarutotoModVariables.PLAYER_VARIABLES_CAPABILITY} - read/write of {@code chakra}, {@code chakra_maximum},
 *       {@code ninja} and read of {@code jinchuriki} (Narutoto stores 9.0 for the Kurama host).</li>
 *   <li>{@code PlayerVariables#syncPlayerVariables(Entity)} - Narutoto's own sync, called after every write.</li>
 *   <li>{@code NarutotoModMobEffects.NO_CHARKRA} - the effect Narutoto uses to block chakra use.</li>
 * </ul>
 * Every access is guarded: if Narutoto changes in a future version, the add-on logs once and refuses
 * chakra-dependent actions instead of crashing the server tick.
 */
public final class NarutotoBridge {
    private static boolean failureLogged;

    private static Optional<NarutotoModVariables.PlayerVariables> vars(Player player) {
        try {
            return player.getCapability(NarutotoModVariables.PLAYER_VARIABLES_CAPABILITY).resolve();
        } catch (LinkageError | RuntimeException e) {
            logOnce(e);
            return Optional.empty();
        }
    }

    private static void logOnce(Throwable e) {
        if (!failureLogged) {
            failureLogged = true;
            NarutotoExpansion.LOGGER.error("Could not access Narutoto player variables; chakra integration disabled. "
                    + "Is the installed Narutoto version 0.4.x?", e);
        }
    }

    /** True when Narutoto's capability could be read for this player. */
    public static boolean isAvailable(Player player) {
        return vars(player).isPresent();
    }

    /** Narutoto sets {@code ninja >= 1} once the player has chosen a ninja path. */
    public static boolean isNinja(Player player) {
        return vars(player).map(v -> v.ninja >= 1.0).orElse(false);
    }

    /** True if the player is the Nine-Tails (Kurama) jinchuriki handled by Narutoto itself. */
    public static boolean isNineTailsHost(Player player) {
        return vars(player).map(v -> v.jinchuriki == 9.0).orElse(false);
    }

    /** True if Narutoto's "no chakra" effect is active on the player. */
    public static boolean isChakraBlocked(Player player) {
        try {
            return player.hasEffect(NarutotoModMobEffects.NO_CHARKRA.get());
        } catch (LinkageError | RuntimeException e) {
            logOnce(e);
            return false;
        }
    }

    public static double getChakra(Player player) {
        return vars(player).map(v -> v.chakra).orElse(0.0);
    }

    public static double getChakraMax(Player player) {
        return vars(player).map(v -> v.chakra_maximum).orElse(0.0);
    }

    /** True if the player has at least {@code fraction} of their maximum chakra available. */
    public static boolean hasChakraFraction(Player player, double fraction) {
        return vars(player).map(v -> v.chakra >= v.chakra_maximum * fraction).orElse(false);
    }

    /** Spends {@code fraction} of the maximum chakra. Returns false (and spends nothing) if there is not enough. */
    public static boolean consumeChakraFraction(ServerPlayer player, double fraction) {
        Optional<NarutotoModVariables.PlayerVariables> opt = vars(player);
        if (opt.isEmpty()) return false;
        NarutotoModVariables.PlayerVariables v = opt.get();
        double need = v.chakra_maximum * fraction;
        if (v.chakra < need) return false;
        v.chakra -= need;
        v.syncPlayerVariables(player);
        return true;
    }

    /** Adds {@code fraction} of the maximum chakra (clamped to the maximum). Returns true if anything was added. */
    public static boolean addChakraFraction(ServerPlayer player, double fraction) {
        Optional<NarutotoModVariables.PlayerVariables> opt = vars(player);
        if (opt.isEmpty()) return false;
        NarutotoModVariables.PlayerVariables v = opt.get();
        if (v.chakra >= v.chakra_maximum) return false;
        v.chakra = Math.min(v.chakra_maximum, v.chakra + v.chakra_maximum * fraction);
        v.syncPlayerVariables(player);
        return true;
    }

    private NarutotoBridge() {}
}
