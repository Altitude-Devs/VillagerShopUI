package com.alttd.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class SubCommand {

    private final MiniMessage miniMessage;

    public SubCommand() {
        miniMessage = MiniMessage.miniMessage();
    }

    public abstract boolean onCommand(CommandSender commandSender, String[] args);

    public abstract String getName();

    public String getPermission() {
        return "villagerui." + getName();
    }

    public abstract List<String> getTabComplete(CommandSender commandSender, String[] args);

    public abstract String getHelpMessage();

    protected MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
