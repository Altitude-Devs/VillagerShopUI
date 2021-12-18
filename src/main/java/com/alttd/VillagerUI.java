package com.alttd;

import com.alttd.GUI.GUIListener;
import com.alttd.commands.CommandManager;
import com.alttd.database.Database;
import com.alttd.config.Config;
import com.alttd.config.VillagerConfig;
import com.alttd.config.WorthConfig;
import com.alttd.events.VillagerInteract;
import com.alttd.util.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VillagerUI extends JavaPlugin {

    public static VillagerUI instance;
    private Economy economy = null;

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
        if (!setupEconomy())
            return;
        Database.getDatabase().init();
        Logger.info("--------------------------------------------------");
        Logger.info("Villager UI started");
        Logger.info("--------------------------------------------------");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new VillagerInteract(), this);
    }

    public Economy getEconomy() {
        if(economy == null)
            setupEconomy();
        return economy;
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            this.getLogger().severe("Vault was not found. Please download vault.");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        } else {
            RegisteredServiceProvider rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                this.getLogger().severe("Can't find economy! Disabling plugin.");
                Bukkit.getPluginManager().disablePlugin(this);
                return false;
            } else {
                this.economy = (Economy)rsp.getProvider();
                return this.economy != null;
            }
        }
    }

}
