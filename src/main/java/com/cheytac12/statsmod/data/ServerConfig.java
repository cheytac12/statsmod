package com.cheytac12.statsmod.data;

import com.cheytac12.statsmod.StatsMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages the server creation date setting.
 * Stored at <server_root>/config/statsmod.json.
 */
public class ServerConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String NOT_SET = "Not set — use /stats setcreation <YYYY-MM-DD> (requires op)";

    private static class Config {
        String serverCreationDate = NOT_SET;
    }

    private static Path configFile(MinecraftServer server) {
        return server.getRunDirectory().resolve("config").resolve("statsmod.json");
    }

    /** Returns the configured server creation date, or a helpful hint if unset. */
    public static String getServerCreationDate(MinecraftServer server) {
        Path path = configFile(server);
        if (Files.exists(path)) {
            try {
                Config cfg = GSON.fromJson(Files.readString(path), Config.class);
                if (cfg != null && cfg.serverCreationDate != null) {
                    return cfg.serverCreationDate;
                }
            } catch (IOException e) {
                StatsMod.LOGGER.error("Failed to read statsmod config", e);
            }
        }
        return NOT_SET;
    }

    /** Persists a new server creation date to the config file. */
    public static void setServerCreationDate(MinecraftServer server, String date) {
        Path path = configFile(server);
        Config cfg = new Config();
        cfg.serverCreationDate = date;
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(cfg));
        } catch (IOException e) {
            StatsMod.LOGGER.error("Failed to write statsmod config", e);
        }
    }
}
