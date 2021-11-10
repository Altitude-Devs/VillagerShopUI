package com.alttd.GUI.windows;

import com.alttd.GUI.GUIMerchant;
import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.events.SpawnShopEvent;
import com.alttd.objects.EconUser;
import com.alttd.objects.Price;
import com.alttd.objects.VillagerType;
import com.alttd.util.Utilities;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
public class BuyGUI extends GUIMerchant {

    private static final MiniMessage miniMessage = MiniMessage.get();

    public BuyGUI(VillagerType villagerType) {
        super(MiniMessage.get().parse(Config.BUY_WINDOW,
                Template.of("trader", villagerType.getDisplayName()),
                Template.of("percentage", "100")), villagerType); //TODO get percentage from player somehow
        for (ItemStack itemStack : villagerType.getBuying()) {
            Price price = Utilities.getPrice(itemStack);
            if (price == null)
                continue;
            addItem(itemStack,
                    getPriceItem(price.getPrice(itemStack.getAmount())),
                    null,
                    player -> buy(villagerType, player, itemStack.getType(), itemStack.getAmount(), price)
            );
        }
    }

    private void buy(VillagerType villagerType, Player player, Material material, int amount, Price price) {
        Economy econ = VillagerUI.getInstance().getEconomy();
        double balance = econ.getBalance(player);
        double cost = price.getPrice(amount);

        if (balance < cost) {
            player.sendMessage(MiniMessage.get().parse(Config.NOT_ENOUGH_MONEY,
                    Template.of("money", String.valueOf(Utilities.round(balance, 2))),
                    Template.of("price", String.valueOf(price))));
            return;
        }
        EconUser econUser = EconUser.users.get(player.getUniqueId());
        int oldPoints = econUser.getPointsMap().get(villagerType.getName());

        econ.withdrawPlayer(player, cost);
        econUser.addPoints(villagerType.getName(), price.getPoints());
        player.sendMessage(MiniMessage.get().parse(Config.PURCHASED_ITEM,
                Template.of("amount", String.valueOf(amount)),
                Template.of("item", material.toString()),
                Template.of("price", String.valueOf(price))));

        Bukkit.getServer().getPluginManager()
                .callEvent(new SpawnShopEvent(player, amount, cost, material,
                        oldPoints, econUser.getPointsMap().get(villagerType.getName()), true));
    }

    private ItemStack getPriceItem(double price) {
        if (price < 0) return nameItem(new ItemStack(Material.BARRIER), -1);
        else if (price <= 10) return nameItem(new ItemStack(Material.IRON_INGOT), price);
        else if (price <= 100) return nameItem(new ItemStack(Material.GOLD_INGOT), price);
        else if (price <= 500) return nameItem(new ItemStack(Material.DIAMOND), price);
        else return nameItem(new ItemStack(Material.NETHERITE_INGOT), price);
    }

    private ItemStack nameItem(ItemStack itemStack, double price) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.parse("<green>" + price + "</green>")); //TODO configurable
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
