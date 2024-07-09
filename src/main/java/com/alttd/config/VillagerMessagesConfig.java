package com.alttd.config;

import com.alttd.objects.VillagerType;
import com.alttd.objects.VillagerTypeManager;
import com.alttd.util.Logger;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;
import java.util.Set;

public class VillagerMessagesConfig extends AbstractConfig {
    static VillagerMessagesConfig config;
    static int version;

    protected VillagerMessagesConfig() {
        super(new File(System.getProperty("user.home") + File.separator + "share" + File.separator + "configs" + File.separator + "VillagerShopUI"), "villager-messages.yml");
    }

    protected static void reload() {
        config = new VillagerMessagesConfig();

        version = config.getInt("config-version", 1);
        config.set("config-version", 1);

        config.readConfig(VillagerMessagesConfig.class, null);
    }

    private static void loadVillagerMessages() {
        Logger.info("Reloading VillagerMessages config...");
        ConfigurationSection configurationSection = config.getConfigurationSection("messages");
        if (configurationSection == null) {
            Logger.warning("No villager messages found in config.");
            config.getStringList("messages.example-tag", List.of("This is an example"));
            return;
        }

        Set<String> keys = configurationSection.getKeys(false);
        if (keys.isEmpty()) {
            Logger.warning("No villager types found in config.");
        }

        keys.forEach(key -> {
            VillagerType villagerType = VillagerTypeManager.getVillagerType(key);
            if (villagerType == null) {
                Logger.info("Unknown villager type: " + key);
                return;
            }
            villagerType.setMessages(configurationSection.getStringList(key));
        });
    }
}
