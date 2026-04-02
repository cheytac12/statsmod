package com.cheytac12.statsmod;

import com.cheytac12.statsmod.command.JoinDateCommand;
import com.cheytac12.statsmod.command.StatsCommand;
import com.cheytac12.statsmod.data.PlayerDataManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsMod implements ModInitializer {

    public static final String MOD_ID = "statsmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StatsCommand.register(dispatcher);
            JoinDateCommand.register(dispatcher);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            PlayerDataManager.onPlayerJoin(handler.player, server)
        );

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            PlayerDataManager.onPlayerDisconnect(handler.player, server)
        );

        LOGGER.info("StatsMod initialized.");
    }
}
