package com.alttd;

import com.alttd.GUI.GUIListener;
import com.alttd.commands.CommandManager;
import com.alttd.commands.database.Database;
import com.alttd.config.Config;
import com.alttd.config.VillagerConfig;
import com.alttd.config.WorthConfig;
import com.alttd.events.VillagerInteract;
import com.alttd.util.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VillagerUI extends JavaPlugin {

    public static VillagerUI instance;
    private static Economy econ = null;

    public static VillagerUI getInstance() {
        return instance;
    }

    public static Economy getEcon() {
        return econ;
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
        if (!setupEconomy()) {
            Logger.severe("% - Unable to find vault", getDescription().getName());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Database.init();
        Logger.info("--------------------------------------------------");
        Logger.info("Villager UI started");
        Logger.info("--------------------------------------------------");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new VillagerInteract(), this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();

        return econ != null;
    }

}
