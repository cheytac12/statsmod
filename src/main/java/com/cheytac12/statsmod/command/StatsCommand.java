package com.cheytac12.statsmod.command;

import com.cheytac12.statsmod.data.ServerConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Registers the /stats command.
 *
 * <p>/stats — shows server creation date, world file size, and in-game day count.
 * Note: the world-size calculation walks the entire world directory and may take
 * a moment on very large worlds.
 * <p>/stats setcreation <date> — (op level 2) sets the server creation date.
 */
public class StatsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("stats")
                .executes(ctx -> showStats(ctx.getSource()))
                .then(CommandManager.literal("setcreation")
                    .requires(src -> src.hasPermissionLevel(2))
                    .then(CommandManager.argument("date", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String date = StringArgumentType.getString(ctx, "date");
                            MinecraftServer server = ctx.getSource().getServer();
                            ServerConfig.setServerCreationDate(server, date);
                            ctx.getSource().sendFeedback(
                                () -> Text.literal("Server creation date set to: ")
                                    .formatted(Formatting.GREEN)
                                    .append(Text.literal(date).formatted(Formatting.WHITE)),
                                true
                            );
                            return 1;
                        })
                    )
                )
        );
    }

    private static int showStats(ServerCommandSource source) {
        MinecraftServer server = source.getServer();

        String creationDate = ServerConfig.getServerCreationDate(server);

        double sizeGb = directorySizeBytes(server.getSavePath(WorldSavePath.ROOT))
                / (1024.0 * 1024.0 * 1024.0);
        String sizeStr = String.format("%.2f GB", sizeGb);

        long inGameDays = server.getOverworld().getTime() / 24000L;

        source.sendFeedback(() -> Text.literal("=== Server Stats ===").formatted(Formatting.GOLD), false);
        source.sendFeedback(() -> Text.literal("Server Creation Date: ").formatted(Formatting.YELLOW)
            .append(Text.literal(creationDate).formatted(Formatting.WHITE)), false);
        source.sendFeedback(() -> Text.literal("World Size: ").formatted(Formatting.YELLOW)
            .append(Text.literal(sizeStr).formatted(Formatting.WHITE)), false);
        source.sendFeedback(() -> Text.literal("In-Game Days: ").formatted(Formatting.YELLOW)
            .append(Text.literal(String.valueOf(inGameDays)).formatted(Formatting.WHITE)), false);

        return 1;
    }

    /** Walks a directory tree and sums all regular file sizes in bytes. */
    private static long directorySizeBytes(Path root) {
        AtomicLong total = new AtomicLong(0L);
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(Files::isRegularFile).forEach(p -> {
                try {
                    total.addAndGet(Files.size(p));
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
        return total.get();
    }
}
