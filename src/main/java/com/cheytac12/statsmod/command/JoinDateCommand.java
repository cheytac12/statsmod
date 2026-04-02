package com.cheytac12.statsmod.command;

import com.cheytac12.statsmod.data.PlayerDataManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Registers the /joindate command.
 *
 * <p>/joindate — shows your own first join date, last seen, and time played.
 * <p>/joindate <player> — shows another online player's stats.
 */
public class JoinDateCommand {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("joindate")
                .executes(ctx -> showStats(ctx.getSource(), ctx.getSource().getPlayerOrThrow()))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(ctx -> {
                        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                        return showStats(ctx.getSource(), target);
                    })
                )
        );
    }

    private static int showStats(ServerCommandSource source, ServerPlayerEntity player)
            throws CommandSyntaxException {

        PlayerDataManager.PlayerData data =
                PlayerDataManager.getPlayerData(player, source.getServer());

        String firstJoin = data.firstJoin > 0L
                ? DATE_FMT.format(Instant.ofEpochMilli(data.firstJoin))
                : "Unknown";

        // If lastSeen equals the join timestamp the player is in their first session.
        // Either way, since this target is an online player, report them as online now.
        String lastSeen = "Currently Online (joined session: "
                + DATE_FMT.format(Instant.ofEpochMilli(data.lastSeen)) + ")";

        // Vanilla tracks play time in ticks (stat key: play_one_minute).
        int ticks = player.getStatHandler().getStat(
                Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)
        String timePlayed = formatTicks(ticks);

        String name = player.getName().getString();

        source.sendFeedback(() -> Text.literal("=== Player Stats: " + name + " ===")
                .formatted(Formatting.GOLD), false);
        source.sendFeedback(() -> Text.literal("First Join: ").formatted(Formatting.YELLOW)
                .append(Text.literal(firstJoin).formatted(Formatting.WHITE)), false);
        source.sendFeedback(() -> Text.literal("Last Seen: ").formatted(Formatting.YELLOW)
                .append(Text.literal(lastSeen).formatted(Formatting.WHITE)), false);
        source.sendFeedback(() -> Text.literal("Time Played: ").formatted(Formatting.YELLOW)
                .append(Text.literal(timePlayed).formatted(Formatting.WHITE)), false);

        return 1;
    }

    /** Converts a tick count (20 ticks/sec) into a human-readable duration string. */
    private static String formatTicks(long ticks) {
        long totalSeconds = ticks / 20L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }
}
