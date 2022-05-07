package com.alttd.commands.subcommands;

import com.alttd.commands.CommandManager;
import com.alttd.commands.SubCommand;
import com.alttd.config.Config;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.minimessage.template.TemplateResolver;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHelp extends SubCommand {

    private final CommandManager commandManager;

    public CommandHelp(CommandManager commandManager) {
        super();
        this.commandManager = commandManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, String[] args) {
        commandSender.sendMiniMessage(Config.HELP_MESSAGE_WRAPPER.replaceAll("<commands>", commandManager
                        .getSubCommands().stream()
                        .filter(subCommand -> commandSender.hasPermission(subCommand.getPermission()))
                        .map(SubCommand::getHelpMessage)
                        .collect(Collectors.joining("\n"))), null);
        return true;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return "villagerui.use";
    }

    @Override
    public List<String> getTabComplete(CommandSender commandSender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Config.HELP_MESSAGE;
    }
}
