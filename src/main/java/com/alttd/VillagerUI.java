package com.alttd;

import com.alttd.GUI.GUIListener;
import com.alttd.commands.CommandManager;
import com.alttd.database.Database;
import com.alttd.config.Config;
import com.alttd.config.VillagerConfig;
import com.alttd.config.WorthConfig;
import com.alttd.database.Queries;
import com.alttd.events.*;
import com.alttd.logging.LogInOut;
import com.alttd.objects.EconUser;
import com.alttd.util.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class VillagerUI extends JavaPlugin {

    public static VillagerUI instance;
    private Economy economy = null;
    private LogInOut logInOut;

    public static VillagerUI getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        logInOut = new LogInOut();
        registerEvents();
        new CommandManager();
        Config.reload();
        VillagerConfig.reload();
        WorthConfig.reload();
        if (!setupEconomy())
            return;
        Database.getDatabase().init();
        scheduleTasks();
        Logger.info("--------------------------------------------------");
        Logger.info("Villager UI started");
        Logger.info("--------------------------------------------------");
    }

    @Override
    public void onDisable() {
        EconUser.getEconUsers().forEach(econUser -> {
            if (Config.DEBUG)
                Logger.info("Syncing %", econUser.getUuid().toString());
            Queries.updateUserPoints(econUser.getUuid(), econUser.getPointsMap());
        });
        logInOut.run();
    }

    private void scheduleTasks() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Config.DEBUG)
                    Logger.info("Syncing users.");
                EconUser.getEconUsers().forEach(econUser -> {
                    if (econUser == null)
                        return;
                    if (Config.DEBUG)
                        Logger.info("Syncing %", econUser.getUuid().toString());
                    econUser.removePoints();
                    econUser.syncPoints();
                });
            }
        }.runTaskTimerAsynchronously(getInstance(), 0L, 10 * 60 * 20L);
        logInOut.runTaskTimerAsynchronously(this, 20 * 60 * 5, 20 * 60 * 10);
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new VillagerEvents(), this);
        getServer().getPluginManager().registerEvents(new LogoutEvent(), this);
        getServer().getPluginManager().registerEvents(new LoginEvent(), this);
        getServer().getPluginManager().registerEvents(new VehicleEvent(), this);
        getServer().getPluginManager().registerEvents(new SpawnShopListener(logInOut), this);
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
