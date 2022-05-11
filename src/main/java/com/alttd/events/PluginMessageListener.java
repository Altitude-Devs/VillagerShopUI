package com.alttd.events;

import com.alttd.util.Logger;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PluginMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] bytes) {
        if (!channel.equals("VillagerUI:player-data")) {
            Logger.warning("Received plugin message on invalid channel");
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        switch (in.readUTF()) {

        }
    }
}
