package com.alttd.commands;

import com.alttd.VillagerUI;
import com.alttd.commands.subcommands.*;
import com.alttd.config.Config;
import com.alttd.util.Logger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabExecutor {
    private final List<SubCommand> subCommands;
    private final MiniMessage miniMessage;

    public CommandManager() {
        VillagerUI villagerUI = VillagerUI.getInstance();

        PluginCommand command = villagerUI.getCommand("villagerui");
        if (command == null) {
            subCommands = null;
            miniMessage = null;
            Logger.severe("Unable to find villager ui command.");
            return;
        }
        command.setExecutor(this);
        command.setTabCompleter(this);

        subCommands = Arrays.asList(
                new CommandHelp(this),
                new CommandPoints(),
                new CommandBuy(),
                new CommandSell(),
                new CommandCreateVillager(),
                new CommandReload(),
                new CommandRemoveVillager());
        miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String cmd, @NotNull String[] args) {
        if (args.length == 0) {
            commandSender.sendMiniMessage(Config.HELP_MESSAGE_WRAPPER.replaceAll("<commands>", subCommands.stream()
                    .filter(subCommand -> commandSender.hasPermission(subCommand.getPermission()))
                    .map(SubCommand::getHelpMessage)
                    .collect(Collectors.joining("\n"))), null);
            return true;
        }

        SubCommand subCommand = getSubCommand(args[0]);

        if (!commandSender.hasPermission(subCommand.getPermission())) {
            commandSender.sendMiniMessage(Config.NO_PERMISSION, null);
            return true;
        }

        return subCommand.onCommand(commandSender, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String cmd, @NotNull String[] args) {
        List<String> res = new ArrayList<>();

        if (args.length <= 1) {
            res.addAll(subCommands.stream()
                    .filter(subCommand -> commandSender.hasPermission(subCommand.getPermission()))
                    .map(SubCommand::getName)
                    .filter(name -> args.length == 0 || name.startsWith(args[0]))
                    .collect(Collectors.toList())
            );
        } else {
            SubCommand subCommand = getSubCommand(args[0]);
            if (subCommand != null && commandSender.hasPermission(subCommand.getPermission()))
                res.addAll(subCommand.getTabComplete(commandSender, args).stream()
                        .filter(str -> str.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                        .collect(Collectors.toList()));
        }
        return res;
    }

    public List<SubCommand> getSubCommands() {
        return subCommands;
    }

    private SubCommand getSubCommand(String cmdName) {
        return subCommands.stream()
                .filter(subCommand -> subCommand.getName().equals(cmdName))
                .findFirst()
                .orElse(null);
    }
}