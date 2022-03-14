package com.alttd.commands.subcommands;

import com.alttd.commands.SubCommand;
import com.alttd.config.Config;
import com.alttd.config.VillagerConfig;
import com.alttd.objects.LoadedVillagers;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandRemoveVillager extends SubCommand {

    @Override
    public boolean onCommand(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMiniMessage(Config.NO_CONSOLE, null);
            return true;
        }

        for(Entity entity : player.getNearbyEntities(2, 2, 2)){
            if (!entity.getType().equals(EntityType.VILLAGER))
                continue;
            UUID uuid = entity.getUniqueId();
            if (LoadedVillagers.getLoadedVillager(uuid) == null)
                continue;
            LoadedVillagers.removeLoadedVillager(uuid);
            VillagerConfig.removeVillager(uuid);
            entity.remove();
            player.sendMiniMessage(Config.REMOVED_VILLAGER, TagResolver.resolver(
                    Placeholder.unparsed("uuid", uuid.toString())));
        }
        return true;
    }

    @Override
    public String getName() {
        return "removevillager";
    }

    @Override
    public List<String> getTabComplete(CommandSender commandSender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Config.REMOVE_VILLAGER_MESSAGE;
    }
}
