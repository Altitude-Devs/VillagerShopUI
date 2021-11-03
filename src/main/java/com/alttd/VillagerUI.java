package com.alttd;

import com.alttd.GUI.GUIListener;
import com.alttd.commands.CommandManager;
import com.alttd.commands.database.Database;
import com.alttd.config.Config;
import com.alttd.config.VillagerConfig;
import com.alttd.config.WorthConfig;
import com.alttd.events.VillagerInteract;
import org.bukkit.plugin.java.JavaPlugin;

public class VillagerUI extends JavaPlugin {

    public static VillagerUI instance;

    public static VillagerUI getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        registerEvents();
        new CommandManager();
        Config.reload();
        VillagerConfig.reload();
        WorthConfig.reload();
        Database.init();
        getLogger().info("--------------------------------------------------");
        getLogger().info("Villager UI started");
        getLogger().info("--------------------------------------------------");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new VillagerInteract(), this);
    }

}
