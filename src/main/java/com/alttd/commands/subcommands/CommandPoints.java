package com.alttd.commands.subcommands;

import com.alttd.commands.SubCommand;
import com.alttd.config.Config;
import com.alttd.objects.EconUser;
import com.alttd.objects.Price;
import com.alttd.objects.VillagerType;
import com.alttd.util.Logger;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.minimessage.template.TemplateResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandPoints extends SubCommand {

    static MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMiniMessage(Config.NO_CONSOLE, null);
            return true;
        }
        EconUser user = EconUser.getUser(player.getUniqueId());
        var ref = new Object() {
            Component message = miniMessage.deserialize(Config.POINTS_HEADER, TemplateResolver.resolving(
                    Template.template("player", player.getName())));
        };
        if (args.length == 1) {
            Object2ObjectArrayMap<String, Integer> pointsMap = user.getPointsMap();
            pointsMap.keySet().forEach(key -> {
                VillagerType villagerType = VillagerType.getVillagerType(key);
                if (villagerType == null) {
                    Logger.warning("Player % has unused villager type % in their point list.", player.getName(), key);
                    return;
                }
                int currentPoints = pointsMap.getOrDefault(key, 0);
                ref.message = ref.message.append(miniMessage.deserialize("\n", TemplateResolver.resolving()));
                ref.message = ref.message.append(miniMessage.deserialize(Config.POINTS_CONTENT, TemplateResolver.resolving(
                        Template.template("villager_type", VillagerType.getVillagerType(key).getDisplayName()),
                        Template.template("points", String.valueOf(currentPoints)),
                        Template.template("buy_multiplier", String.valueOf(Price.getCurrentMultiplier(currentPoints, true))),
                        Template.template("sell_multiplier", String.valueOf(Price.getCurrentMultiplier(currentPoints, true)))
                    )));
            });
        } else if (args.length == 2){
            Object2ObjectArrayMap<String, Integer> pointsMap = user.getPointsMap();
            if (!pointsMap.containsKey(args[1])) {
                player.sendMessage(ref.message);
                return true;
            }
            ref.message = ref.message.append(miniMessage.deserialize(Config.POINTS_CONTENT, TemplateResolver.resolving(
                    Template.template("villager_type", VillagerType.getVillagerType(args[1]).getDisplayName()),
                    Template.template("points", String.valueOf(pointsMap.get(args[1]))))));
        } else
            player.sendMiniMessage(getHelpMessage(), null);

        player.sendMessage(ref.message);
        return true;
    }

    @Override
    public String getName() {
        return "points";
    }

    @Override
    public List<String> getTabComplete(CommandSender commandSender, String[] args) {
        List<String> res = new ArrayList<>();
        if (args.length == 2)
            res.addAll(VillagerType.getVillagerTypes().stream()
                    .map(VillagerType::getName)
                    .collect(Collectors.toList()));
        return res;
    }

    @Override
    public String getHelpMessage() {
        return Config.POINTS_MESSAGE;
    }
}
