package com.alttd.GUI.windows;

import com.alttd.GUI.GUIMerchant;
import com.alttd.VillagerUI;
import com.alttd.config.Config;
import com.alttd.config.WorthConfig;
import com.alttd.objects.EconUser;
import com.alttd.objects.Price;
import com.alttd.objects.VillagerType;
import com.alttd.util.Utilities;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Objects;

public class SellGUI extends GUIMerchant {

    private static final MiniMessage miniMessage = MiniMessage.get();

    public SellGUI(VillagerType villagerType) {
        super(MiniMessage.get().parse(Config.SELL_WINDOW,
                Template.of("trader", villagerType.getDisplayName()),
                Template.of("percentage", "100")), villagerType); //TODO get percentage from player somehow
        for (ItemStack itemStack : villagerType.getSelling()) {
            Price price = Utilities.getPrice(itemStack);
            if (price == null)
                continue;
            addItem(itemStack,
                    getPriceItem(price.getPrice(itemStack.getAmount())),
                    null,
                    player -> sell(villagerType, player, itemStack.getType(), itemStack.getAmount(), price)
            );
        }
    }

    private void sell(VillagerType villagerType, Player player, Material material, int amount, Price price) {
        PlayerInventory inventory = player.getInventory();

        if (!inventory.containsAtLeast(new ItemStack(material), amount))
        {
            player.sendMessage(miniMessage.parse(Config.NOT_ENOUGH_ITEMS,
                    Template.of("type", material.name()),
                    Template.of("amount",String.valueOf(amount))));
            return;
        }

        Economy econ = VillagerUI.getInstance().getEconomy();
        EconUser econUser = EconUser.getUser(player.getUniqueId());
        int oldPoints = Objects.requireNonNullElse(econUser.getPointsMap().get(villagerType.getName()), 0);
        int trans_pts = (int) (Math.floor(price.getPrice(amount)/ WorthConfig.POINT_MOD) * amount);
        double cost = price.calculatePriceThing(oldPoints, trans_pts);

        econ.depositPlayer(player, cost);
        econUser.addPoints(villagerType.getName(), -price.getPoints());
        var ref = new Object() {
            int tmpAmount = amount;
        };
        Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .filter(itemStack -> itemStack.getType().equals(material))
                .forEach(itemStack -> {
                    if (ref.tmpAmount == 0)
                        return;
                    if (itemStack.getAmount() > ref.tmpAmount)
                    {
                        itemStack.setAmount(itemStack.getAmount() - ref.tmpAmount);
                        ref.tmpAmount = 0;
                    } else {
                        ref.tmpAmount -= itemStack.getAmount();
                        itemStack.setAmount(0);
                    }
                });
        //TODO remove items from inv
        player.sendMessage(MiniMessage.get().parse(Config.SOLD_ITEM,
                Template.of("amount", String.valueOf(amount)),
                Template.of("item", StringUtils.capitalize(material.name()
                        .toLowerCase().replaceAll("_", " "))),
                Template.of("price", String.valueOf(cost))));

//        Bukkit.getServer().getPluginManager()
//                .callEvent(new SpawnShopEvent(player, amount, cost, material,
//                        oldPoints, econUser.getPointsMap().get(villagerType.getName()), false));
        //TODO FIX LOGGING
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
        itemMeta.displayName(miniMessage.parse("<red>" + price * -1 + "</red>")); //TODO configurable
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
