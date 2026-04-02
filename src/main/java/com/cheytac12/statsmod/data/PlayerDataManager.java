package com.cheytac12.statsmod.data;

import com.cheytac12.statsmod.StatsMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-player data: first join timestamp and last seen timestamp.
 * Data is stored as JSON files under <world>/statsmod_data/<uuid>.json.
 */
public class PlayerDataManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, PlayerData> CACHE = new HashMap<>();

    public static class PlayerData {
        public long firstJoin = 0L;
        public long lastSeen = 0L;
    }

    private static Path dataFile(MinecraftServer server, UUID uuid) {
        return server.getSavePath(WorldSavePath.ROOT)
                .resolve("statsmod_data")
                .resolve(uuid + ".json");
    }

    /** Returns cached or loaded data for the given player. */
    public static PlayerData getPlayerData(ServerPlayerEntity player, MinecraftServer server) {
        UUID uuid = player.getUuid();
        return CACHE.computeIfAbsent(uuid, id -> load(id, server));
    }

    /** Called when a player connects. Records first join if this is the first visit. */
    public static void onPlayerJoin(ServerPlayerEntity player, MinecraftServer server) {
        UUID uuid = player.getUuid();
        PlayerData data = load(uuid, server);
        long now = System.currentTimeMillis();
        if (data.firstJoin == 0L) {
            data.firstJoin = now;
        }
        data.lastSeen = now;
        CACHE.put(uuid, data);
        save(uuid, data, server);
    }

    /** Called when a player disconnects. Updates last seen timestamp. */
    public static void onPlayerDisconnect(ServerPlayerEntity player, MinecraftServer server) {
        UUID uuid = player.getUuid();
        PlayerData data = CACHE.getOrDefault(uuid, new PlayerData());
        data.lastSeen = System.currentTimeMillis();
        save(uuid, data, server);
        CACHE.remove(uuid);
    }

    private static PlayerData load(UUID uuid, MinecraftServer server) {
        Path file = dataFile(server, uuid);
        if (Files.exists(file)) {
            try {
                String json = Files.readString(file);
                PlayerData data = GSON.fromJson(json, PlayerData.class);
                if (data != null) {
                    return data;
                }
            } catch (IOException e) {
                StatsMod.LOGGER.error("Failed to load player data for {}", uuid, e);
            }
        }
        return new PlayerData();
    }

    private static void save(UUID uuid, PlayerData data, MinecraftServer server) {
        Path file = dataFile(server, uuid);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(data));
        } catch (IOException e) {
            StatsMod.LOGGER.error("Failed to save player data for {}", uuid, e);
        }
    }
}
