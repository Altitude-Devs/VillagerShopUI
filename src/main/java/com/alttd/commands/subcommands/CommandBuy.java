package com.alttd.commands.subcommands;

import com.alttd.commands.SubCommand;
import com.alttd.config.Config;
import com.alttd.config.WorthConfig;
import com.alttd.objects.EconUser;
import com.alttd.objects.Price;
import com.alttd.objects.VillagerType;
import com.alttd.util.Logger;
import com.alttd.util.Utilities;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandBuy extends SubCommand {
    @Override
    public boolean onCommand(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMiniMessage(Config.NO_CONSOLE, null);
            return true;
        }
        if (args.length == 1) {
            //TODO open gui
            player.sendMiniMessage(getHelpMessage(), null); //TODO remove later
            return true;
        }
        if (args.length != 2) {
            player.sendMiniMessage(getHelpMessage(), null);
            return true;
        }
        EconUser user = EconUser.getUser(player.getUniqueId());
        if (user == null) {
            player.sendMiniMessage(Config.LOADING_ECON_DATA, null);
            return true;
        }
        Material item = Material.valueOf(args[1].toUpperCase());
        Optional<VillagerType> optionalVillagerType = VillagerType.getVillagerTypes().stream()
                .filter(villagerType -> villagerType.getBuying().stream()
                        .map(ItemStack::getType)
                        .anyMatch(material -> material.equals(item)))
                .findFirst();
        if (optionalVillagerType.isEmpty()) {
            player.sendMiniMessage(Config.NO_BUY_AT_SPAWN, TagResolver.resolver(Placeholder.unparsed("material", item.name())));
            return true;
        }
        VillagerType villagerType = optionalVillagerType.get();
        Price price = Utilities.getPrice(new ItemStack(item, 1), WorthConfig.buy);
        if (price == null) {
            Logger.warning("Price was null despite being impossible to be null");
            return true;
        }
        Integer curPoints = user.getPointsMap().getOrDefault(villagerType.getName(), 0);
        double cost = price.calculatePriceThing(curPoints, price.getPoints(), true, price.getPoints());
        player.sendMiniMessage(Config.BUY_ITEM_MESSAGE, TagResolver.resolver(
                Placeholder.unparsed("material", item.name()),
                Placeholder.unparsed("price", String.valueOf(cost)),
                Placeholder.unparsed("points", String.valueOf(price.getPoints())),
                Placeholder.unparsed("current_points", String.valueOf(curPoints)),
                Placeholder.unparsed("villager_type", villagerType.getDisplayName())
        ));
        return true;
    }

    @Override
    public String getName() {
        return "buy";
    }

    @Override
    public List<String> getTabComplete(CommandSender commandSender, String[] args) {
        List<String> res = new ArrayList<>();
        if (args.length == 2)
            res.addAll(Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList()));
        return res;
    }

    @Override
    public String getHelpMessage() {
        return Config.BUY_MESSAGE;
    }
}
