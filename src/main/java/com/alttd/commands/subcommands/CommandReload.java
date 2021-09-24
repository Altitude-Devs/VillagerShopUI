package com.alttd.commands.subcommands;

import com.alttd.commands.SubCommand;
import com.alttd.config.Config;
import com.alttd.config.VillagerConfig;
import com.alttd.config.WorthConfig;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandReload extends SubCommand {

    @Override
    public boolean onCommand(CommandSender commandSender, String[] args) {
        Config.reload();
        VillagerConfig.reload();
        WorthConfig.reload();
        commandSender.sendMessage(getMiniMessage().parse("<green>Reloaded VillagerShopUI config.</green>"));
        return true;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public List<String> getTabComplete(CommandSender commandSender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Config.RELOAD_MESSAGE;
    }
}
