package com.alttd.GUI.windows;

import com.alttd.GUI.GUIMerchant;
import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.config.WorthConfig;
import com.alttd.events.SpawnShopEvent;
import com.alttd.objects.EconUser;
import com.alttd.objects.Price;
import com.alttd.objects.VillagerType;
import com.alttd.util.Utilities;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.minimessage.template.TemplateResolver;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BuyGUI extends GUIMerchant {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BuyGUI(VillagerType villagerType, EconUser econUser, boolean bulk) {
        super(miniMessage.deserialize(Config.BUY_WINDOW, TemplateResolver.resolving(
                Template.template("trader", villagerType.getDisplayName()),
                Template.template("points", String.valueOf(Objects.requireNonNullElse(
                        econUser.getPointsMap().get(villagerType.getName()),
                        0)))
        )), villagerType);
        for (ItemStack itemStack : villagerType.getBuying()) {
            if (bulk)
                itemStack.setAmount(itemStack.getMaxStackSize());
            Price price = Utilities.getPrice(itemStack, WorthConfig.buy);
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
        int itemPts = (int) (Math.floor(price.getPrice(1) / WorthConfig.POINT_MOD) + 1);
        int transPts = itemPts * amount;
        EconUser econUser = EconUser.getUser(player.getUniqueId());
        int oldPoints = Objects.requireNonNullElse(econUser.getPointsMap().get(villagerType.getName()), 0);
        double cost = price.calculatePriceThing(oldPoints, transPts, true, itemPts);

        if (balance < cost) {
            player.sendMiniMessage(Config.NOT_ENOUGH_MONEY, List.of(
                    Template.template("money", String.valueOf(Utilities.round(balance, 2))),
                    Template.template("price", String.valueOf(cost))
            ));
            return;
        }

        var ref = new Object() {
            int space = 0;
        };
        Arrays.stream(player.getInventory().getContents())
                .filter(itemStack -> itemStack == null || itemStack.getType().equals(material))
                .forEach(itemStack -> {
                    if (itemStack == null)
                        ref.space += material.getMaxStackSize();
                    else
                        ref.space += itemStack.getMaxStackSize() - itemStack.getAmount();
                });
        if (ref.space < amount) {
            player.sendMiniMessage(Config.NOT_ENOUGH_SPACE, List.of(
                    Template.template("space", String.valueOf(ref.space)),
                    Template.template("amount", String.valueOf(amount))
            ));
            return;
        }

        econ.withdrawPlayer(player, cost);
        econUser.addPoints(villagerType.getName(), transPts);
        player.getInventory().addItem(new ItemStack(material, amount));

        int newPoints = econUser.getPointsMap().get(villagerType.getName());
        player.sendMiniMessage(Config.PURCHASED_ITEM, List.of(
                Template.template("amount", String.valueOf(amount)),
                Template.template("item", StringUtils.capitalize(material.name()
                        .toLowerCase().replaceAll("_", " "))),
                Template.template("price", String.valueOf(cost)),
                Template.template("points", String.valueOf(transPts)),
                Template.template("total_points", String.valueOf(newPoints))
        ));

        Bukkit.getServer().getPluginManager()
                .callEvent(new SpawnShopEvent(player, amount, cost, material,
                        oldPoints, newPoints, true));
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
        itemMeta.displayName(miniMessage.deserialize("<red>" + price + "</red>")); //TODO configurable
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
