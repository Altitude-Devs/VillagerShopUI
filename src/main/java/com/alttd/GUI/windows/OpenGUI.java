package com.alttd.GUI.windows;

import com.alttd.GUI.GUIInventory;
import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.objects.EconUser;
import com.alttd.objects.VillagerType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class OpenGUI extends GUIInventory {

    private static final ItemStack BUY = new ItemStack(Material.GOLD_INGOT);
    private static final ItemStack SELL = new ItemStack(Material.BUCKET);

    static {
        MiniMessage miniMessage = MiniMessage.get();
        ItemMeta itemMeta;
        {
            itemMeta = BUY.getItemMeta();
            itemMeta.displayName(miniMessage.parse("<green>Buy</green>"));
            BUY.setItemMeta(itemMeta);
        }
        {
            itemMeta = SELL.getItemMeta();
            itemMeta.displayName(miniMessage.parse("<green>Sell</green>"));
            SELL.setItemMeta(itemMeta);
        }
    }

    public OpenGUI(VillagerType villagerType, EconUser econUser) {
        super(InventoryType.HOPPER, MiniMessage.get().parse(Config.INITIAL_VILLAGER_WINDOW,
                Template.of("trader", villagerType.getDisplayName()),
                Template.of("points", String.valueOf(Objects.requireNonNullElse(econUser.getPointsMap().get(villagerType.getName()), 0)))));
        setItem(1, BUY, player -> new BukkitRunnable() {
            @Override
            public void run() {
                BuyGUI buyGUI = new BuyGUI(villagerType, econUser);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        buyGUI.open(player);
                    }
                }.runTask(VillagerUI.getInstance());
            }
        }.runTaskAsynchronously(VillagerUI.getInstance()));
        setItem(3, SELL, player -> new BukkitRunnable() {
            @Override
            public void run() {
                SellGUI sellGUI = new SellGUI(villagerType, econUser);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sellGUI.open(player);
                    }
                }.runTask(VillagerUI.getInstance());
            }
        }.runTaskAsynchronously(VillagerUI.getInstance()));
    }
}
