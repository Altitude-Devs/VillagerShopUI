package com.alttd.events;

import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.database.Queries;
import com.alttd.objects.EconUser;
import com.alttd.util.Logger;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.stream.Collectors;

public class PluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] bytes) {
        if (!channel.equals("villagerui:player-data")) {
            Logger.warning("Received plugin message on invalid channel");
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        switch (in.readUTF()) {
            case "try-lock-result" -> {
                if (!in.readBoolean()) {
                    Logger.warning("Unable to lock row");
                    return; //TODO handle
                }
                UUID uuid = UUID.fromString(in.readUTF());
                loadUser(uuid);
            }
            case "queue-lock-failed" -> Logger.warning("Encountered uuid that was locked and had a lock queued: %, lock is from %", in.readUTF(), in.readUTF());
            case "try-unlock-result" -> {
                if (in.readBoolean()) {
                    // ignore?
                    return;
                }
                Logger.severe("Unable to unlock %.", in.readUTF());
            }
            case "locked-queue-lock" -> {
                if (!in.readBoolean()) {
                    Logger.warning("Got false back from locked queue lock");
                    return; //TODO handle
                }
                UUID uuid = UUID.fromString(in.readUTF());
                loadUser(uuid);
            }
            case "check-lock-result" -> {

            }
        }
    }

    private void loadUser(UUID uuid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                EconUser user = Queries.getEconUser(uuid);
                if (Config.DEBUG)
                    Logger.info("Loaded EconUser % with the following points:\n%",
                            uuid.toString(), user.getPointsMap().object2ObjectEntrySet().stream()
                                    .map(entry -> entry.getKey() + " - " + entry.getValue().toString())
                                    .collect(Collectors.joining("\n")));
                int minutes = Queries.getMinutesSinceUpdated(uuid);
                user.removePoints(minutes * 2);

                EconUser.removeQueriedUser(uuid);
            }
        }.runTaskAsynchronously(VillagerUI.getInstance());
    }
}
