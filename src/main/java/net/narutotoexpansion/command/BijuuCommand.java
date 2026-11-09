package net.narutotoexpansion.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.narutotoexpansion.NarutotoExpansion;
import net.narutotoexpansion.core.BijuuService;
import net.narutotoexpansion.core.FormService;
import net.narutotoexpansion.data.BeastRegistryData;
import net.narutotoexpansion.data.BeastType;
import net.narutotoexpansion.data.ExpansionCapability;
import net.narutotoexpansion.data.ExpansionData;
import net.narutotoexpansion.data.FormRules;
import net.narutotoexpansion.network.ExpansionNetwork;

import java.util.Arrays;

/**
 * /bijuu info [player]
 * /bijuu host <player> <beast>         (op)
 * /bijuu release [player]              (op)
 * /bijuu mastery <player> <beast> <n>  (op)
 * /bijuu form <level>                  (self, selects preferred/active level)
 * /bijuu free <beast>                  (op, clears the world registry entry)
 * /bijuu spawn <beast> [healthPercent] (op, spawns a wild beast at the executor)
 */
@Mod.EventBusSubscriber(modid = NarutotoExpansion.MODID)
public final class BijuuCommand {
    private static final SuggestionProvider<CommandSourceStack> BEAST_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(BeastType.implemented()).map(t -> t.key), builder);
    private static final SimpleCommandExceptionType UNKNOWN_BEAST =
            new SimpleCommandExceptionType(Component.translatable("command.narutoto_expansion.unknown_beast"));

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("bijuu")
                .then(Commands.literal("info")
                        .executes(c -> info(c.getSource(), c.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> info(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                .then(Commands.literal("host").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("beast", StringArgumentType.word()).suggests(BEAST_SUGGESTIONS)
                                        .executes(c -> host(c.getSource(), EntityArgument.getPlayer(c, "player"),
                                                beast(StringArgumentType.getString(c, "beast")))))))
                .then(Commands.literal("release").requires(s -> s.hasPermission(2))
                        .executes(c -> release(c.getSource(), c.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> release(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                .then(Commands.literal("mastery").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("beast", StringArgumentType.word()).suggests(BEAST_SUGGESTIONS)
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(c -> mastery(c.getSource(), EntityArgument.getPlayer(c, "player"),
                                                        beast(StringArgumentType.getString(c, "beast")),
                                                        IntegerArgumentType.getInteger(c, "value")))))))
                .then(Commands.literal("form")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, FormRules.MAX_FORM))
                                .executes(c -> form(c.getSource(), c.getSource().getPlayerOrException(),
                                        IntegerArgumentType.getInteger(c, "level")))))
                .then(Commands.literal("free").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("beast", StringArgumentType.word()).suggests(BEAST_SUGGESTIONS)
                                .executes(c -> free(c.getSource(), beast(StringArgumentType.getString(c, "beast"))))))
                .then(Commands.literal("spawn").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("beast", StringArgumentType.word()).suggests(BEAST_SUGGESTIONS)
                                .executes(c -> spawn(c.getSource(), beast(StringArgumentType.getString(c, "beast")), 100))
                                .then(Commands.argument("healthPercent", IntegerArgumentType.integer(1, 100))
                                        .executes(c -> spawn(c.getSource(), beast(StringArgumentType.getString(c, "beast")),
                                                IntegerArgumentType.getInteger(c, "healthPercent")))))));
    }

    private static BeastType beast(String key) throws CommandSyntaxException {
        BeastType type = BeastType.byKey(key);
        if (type == null || type.isExternal()) throw UNKNOWN_BEAST.create();
        return type;
    }

    private static int info(CommandSourceStack src, ServerPlayer player) {
        ExpansionData data = ExpansionCapability.get(player);
        BeastType type = BeastType.byId(data.hostBeast);
        if (type == null) {
            src.sendSuccess(() -> Component.translatable("command.narutoto_expansion.info.none", player.getDisplayName()), false);
        } else {
            int mastery = data.mastery[type.id];
            src.sendSuccess(() -> Component.translatable("command.narutoto_expansion.info.host", player.getDisplayName(),
                    Component.translatable(type.translationKey()), mastery,
                    FormRules.unlocked(type, mastery), data.form,
                    Component.translatable(data.mode.translationKey()),
                    String.format("%.0f/%.0f", data.pool, FormRules.poolMax(mastery))), false);
        }
        return 1;
    }

    private static int host(CommandSourceStack src, ServerPlayer player, BeastType type) {
        BijuuService.Result result = BijuuService.tryHost(player, type, true);
        if (result == BijuuService.Result.OK) {
            src.sendSuccess(() -> Component.translatable("command.narutoto_expansion.host.ok", player.getDisplayName(),
                    Component.translatable(type.translationKey())), true);
            return 1;
        }
        src.sendFailure(result.message());
        return 0;
    }

    private static int release(CommandSourceStack src, ServerPlayer player) {
        if (!ExpansionCapability.get(player).isHost()) {
            src.sendFailure(Component.translatable("msg.narutoto_expansion.not_host"));
            return 0;
        }
        BijuuService.release(player, true);
        src.sendSuccess(() -> Component.translatable("command.narutoto_expansion.release.ok", player.getDisplayName()), true);
        return 1;
    }

    private static int mastery(CommandSourceStack src, ServerPlayer player, BeastType type, int value) {
        ExpansionData data = ExpansionCapability.get(player);
        data.mastery[type.id] = FormRules.clampMastery(value);
        ExpansionNetwork.sync(player);
        int shown = data.mastery[type.id];
        src.sendSuccess(() -> Component.translatable("command.narutoto_expansion.mastery.ok", player.getDisplayName(),
                Component.translatable(type.translationKey()), shown), true);
        return 1;
    }

    private static int form(CommandSourceStack src, ServerPlayer player, int level) {
        ExpansionData data = ExpansionCapability.get(player);
        BeastType type = BeastType.byId(data.hostBeast);
        if (type == null) {
            src.sendFailure(Component.translatable("msg.narutoto_expansion.not_host"));
            return 0;
        }
        FormService.setSelectedForm(player, data, type, level);
        return 1;
    }

    private static int free(CommandSourceStack src, BeastType type) {
        BeastRegistryData.get(src.getServer()).clear(type);
        src.sendSuccess(() -> Component.translatable("command.narutoto_expansion.free.ok",
                Component.translatable(type.translationKey())), true);
        return 1;
    }

    private static int spawn(CommandSourceStack src, BeastType type, int healthPercent) throws CommandSyntaxException {
        ServerLevel level = src.getLevel();
        BijuuService.spawnBeast(level, type, src.getPosition(), healthPercent / 100.0f);
        src.sendSuccess(() -> Component.translatable("command.narutoto_expansion.spawn.ok",
                Component.translatable(type.translationKey()), healthPercent), true);
        return 1;
    }

    private BijuuCommand() {}
}
