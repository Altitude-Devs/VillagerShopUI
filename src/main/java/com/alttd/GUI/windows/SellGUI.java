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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SellGUI extends GUIMerchant {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SellGUI(VillagerType villagerType, EconUser econUser, boolean bulk) {
        super(MiniMessage.miniMessage().deserialize(Config.SELL_WINDOW, TemplateResolver.resolving(
                Template.template("trader", villagerType.getDisplayName()),
                Template.template("points", String.valueOf(Objects
                        .requireNonNullElse(econUser.getPointsMap().get(villagerType.getName())
                                , 0))))), villagerType);
        for (ItemStack is : villagerType.getSelling()) {
            ItemStack itemStack = is.clone();
            if (bulk)
                itemStack.setAmount(1);
            Price price = Utilities.getPrice(itemStack, WorthConfig.sell);
            if (price == null)
                continue;
            addItem(itemStack,
                    getPriceItem(price.getPrice(itemStack.getAmount())),
                    null,
                    player -> sell(villagerType, player, itemStack.getType(), itemStack.getAmount(), price, bulk)
            );
        }
    }

    private void sell(VillagerType villagerType, Player player, Material material, int amount, Price price, boolean bulk) {
        PlayerInventory inventory = player.getInventory();

        if (!inventory.containsAtLeast(new ItemStack(material), bulk ? 1 : amount)) {
            player.sendMiniMessage(Config.NOT_ENOUGH_ITEMS, List.of(
                    Template.template("type", material.name()),
                    Template.template("amount", String.valueOf(bulk ? 1 : amount))));
            return;
        }

        if (bulk)
            amount = Arrays.stream(inventory.getContents())
                    .filter(Objects::nonNull)
                    .filter(itemStack -> itemStack.getType().equals(material))
                    .mapToInt(ItemStack::getAmount).sum();
        Economy econ = VillagerUI.getInstance().getEconomy();
        EconUser econUser = EconUser.getUser(player.getUniqueId());
        int oldPoints = Objects.requireNonNullElse(econUser.getPointsMap().get(villagerType.getName()), 0);
        int itemPts = (int) (Math.floor(price.getPrice(1) / WorthConfig.POINT_MOD) + 1);
        int transPts = (itemPts * amount) * -1;
        double cost = price.calculatePriceThing(oldPoints, transPts, false, itemPts);

        econ.depositPlayer(player, cost);
        econUser.addPoints(villagerType.getName(), transPts);

        removeItems(inventory, material, amount);

        int newPoints = econUser.getPointsMap().get(villagerType.getName());
        player.sendMiniMessage(Config.SOLD_ITEM, List.of(
                Template.template("amount", String.valueOf(amount)),
                Template.template("item", StringUtils.capitalize(material.name()
                        .toLowerCase().replaceAll("_", " "))),
                Template.template("price", String.valueOf(cost)),
                Template.template("points", String.valueOf(transPts)),
                Template.template("total_points", String.valueOf(newPoints)),
                Template.template("villager_name", villagerType.getDisplayName())
        ));

        Bukkit.getServer().getPluginManager()
                .callEvent(new SpawnShopEvent(player, amount, cost, material,
                        oldPoints, newPoints, false));
    }

    private void removeItems(Inventory inventory, Material material, int amount) {
        var ref = new Object() {
            int tmpAmount = amount;
        };

        Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .filter(itemStack -> itemStack.getType().equals(material))
                .forEach(itemStack -> {
                    if (ref.tmpAmount == 0)
                        return;
                    if (itemStack.getAmount() > ref.tmpAmount) {
                        itemStack.setAmount(itemStack.getAmount() - ref.tmpAmount);
                        ref.tmpAmount = 0;
                    } else {
                        ref.tmpAmount -= itemStack.getAmount();
                        itemStack.setAmount(0);
                    }
                });
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
        itemMeta.displayName(miniMessage.deserialize("<green>" + price + "</green>")); //TODO configurable
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
