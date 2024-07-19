package com.alttd.commands.subcommands;

import com.alttd.commands.SubCommand;
import com.alttd.config.Config;
import com.alttd.config.VillagerConfig;
import com.alttd.objects.LoadedVillagers;
import com.alttd.objects.VillagerType;
import com.alttd.objects.VillagerTypeManager;
import com.alttd.util.Utilities;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.OldEnum;

import java.util.*;
import java.util.stream.Collectors;

public class CommandCreateVillager extends SubCommand {

    @Override
    public boolean onCommand(CommandSender commandSender, String[] args) {
        if (args.length != 9) {
            commandSender.sendMiniMessage(getHelpMessage(), null);
            return true;
        }

        Optional<VillagerType> first = VillagerTypeManager.getVillagerTypes().stream().filter(villagerType -> villagerType.getName().equalsIgnoreCase(args[1])).findFirst();
        if (first.isEmpty()) {
            commandSender.sendMiniMessage(getHelpMessage(), null);
            return true;
        }
        VillagerType villagerType = first.get();

        Villager.Type type = Villager.Type.valueOf(args[2].toUpperCase());
        if (type == null) { //TODO test if this might need a try catch?
            commandSender.sendMiniMessage(getHelpMessage(), null);
            return true;
        }

        World world = Bukkit.getServer().getWorld(args[8]);
        if (world == null) {
            commandSender.sendMiniMessage(getHelpMessage(), null);
            return true;
        }
        Location location = new Location(world, Double.parseDouble(args[3]),Double.parseDouble(args[4]),Double.parseDouble(args[5]), Float.parseFloat(args[6]), Float.parseFloat(args[7]));
        Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER, CreatureSpawnEvent.SpawnReason.CUSTOM);
        villager.setPersistent(true);
        villager.setInvulnerable(true);
        villager.setVillagerType(type);
        villager.setProfession(villagerType.getProfession());
        villager.setRemoveWhenFarAway(false);
        villager.setCollidable(false);
        villager.customName(getMiniMessage().deserialize(Config.VILLAGER_NAME, TagResolver.resolver(
                Placeholder.unparsed("name", villagerType.getDisplayName())))
        );
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
            case 2 -> res.addAll(VillagerTypeManager.getVillagerTypes().stream()
                    .map(VillagerType::getName)
                    .collect(Collectors.toList()));
            case 3 -> res.addAll(Arrays.stream(Villager.Type.values()).map(OldEnum::name).collect(Collectors.toList()));
            case 4 -> {
                if (commandSender instanceof Player player) {
                    res.add(String.valueOf(Utilities.round(player.getLocation().getX(), 2)));
                }
            }
            case 5 -> {
                if (commandSender instanceof Player player) {
                    res.add(String.valueOf(Utilities.round(player.getLocation().getY(), 1)));
                }
            }
            case 6 -> {
                if (commandSender instanceof Player player) {
                    res.add(String.valueOf(Utilities.round(player.getLocation().getZ(), 2)));
                }
            }
            case 7 -> {
                if (commandSender instanceof Player player) {
                    res.add(String.valueOf(Utilities.round(player.getLocation().getYaw(), 2)));
                }
            }
            case 8 -> {
                if (commandSender instanceof Player player) {
                    res.add(String.valueOf(Utilities.round(player.getLocation().getPitch(), 2)));
                }
            }
            case 9 -> {
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
