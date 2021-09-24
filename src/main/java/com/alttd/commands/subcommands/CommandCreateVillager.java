package com.alttd.commands.subcommands;

import com.alttd.VillagerUI;
import com.alttd.commands.SubCommand;
import com.alttd.config.Config;
import com.alttd.config.VillagerConfig;
import com.alttd.objects.LoadedVillagers;
import com.alttd.objects.VillagerType;
import com.alttd.util.Utilities;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandCreateVillager extends SubCommand {

    @Override
    public boolean onCommand(CommandSender commandSender, String[] args) {
        if (args.length < 8) {
            commandSender.sendMessage(getMiniMessage().parse(getHelpMessage()));
            return true;
        }

        Optional<VillagerType> first = VillagerType.getVillagerTypes().stream().filter(villagerType -> villagerType.getName().equalsIgnoreCase(args[1])).findFirst();
        if (first.isEmpty()) {
            commandSender.sendMessage(getMiniMessage().parse(getHelpMessage()));
            return true;
        }
        VillagerType villagerType = first.get();

        World world = Bukkit.getServer().getWorld(args[7]);
        if (world == null) {
            commandSender.sendMessage(getMiniMessage().parse(getHelpMessage()));
            return true;
        }
        Location location = new Location(world, Double.parseDouble(args[2]),Double.parseDouble(args[3]),Double.parseDouble(args[4]), Float.parseFloat(args[5]), Float.parseFloat(args[6]));
        Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER, CreatureSpawnEvent.SpawnReason.CUSTOM);
        villager.setPersistent(true);
        villager.setInvulnerable(true);
//        villager.setVillagerType(Villager.Type.); TODO choose villager type?
        villager.setRemoveWhenFarAway(false);
        villager.customName(getMiniMessage().parse(Config.VILLAGER_NAME, Template.of("name", villagerType.getDisplayName())));
        villager.setCustomNameVisible(true);
        villager.setAI(false);

        UUID uuid = villager.getUniqueId();

        LoadedVillagers.addLoadedVillager(uuid, villagerType);
        VillagerConfig.addVillager(uuid, villagerType);
        return true;
    }

    @Override
    public String getName() {
        return "createvillager";
    }

    @Override
    public List<String> getTabComplete(CommandSender commandSender, String[] args) {
        List<String> res = new ArrayList<>();
        switch (args.length) {
            case 2 -> res.addAll(VillagerType.getVillagerTypes().stream()
                    .map(VillagerType::getName)
                    .collect(Collectors.toList()));
            case 3 -> {
                if (commandSender instanceof Player player) {
                    res.add(String.valueOf(Utilities.round(player.getLocation().getX(), 2)));
                }
            }
            case 4 -> {
                if (commandSender instanceof Player player) {
                    res.add(String.valueOf(Utilities.round(player.getLocation().getY(), 1)));
                }
            }
            case 5 -> {
                if (commandSender instanceof Player player) {
                    res.add(String.valueOf(Utilities.round(player.getLocation().getZ(), 2)));
                }
            }
            case 6 -> {
                if (commandSender instanceof Player player) {
                    res.add(String.valueOf(Utilities.round(player.getLocation().getYaw(), 2)));
                }
            }
            case 7 -> {
                if (commandSender instanceof Player player) {
                    res.add(String.valueOf(Utilities.round(player.getLocation().getPitch(), 2)));
                }
            }
            case 8 -> {
                if (commandSender instanceof Player player) {
                    res.add(player.getLocation().getWorld().getName());
                }
            }
        }
        return res;
    }

    @Override
    public String getHelpMessage() {
        return Config.CREATE_VILLAGER_MESSAGE;
    }
}
