package com.alttd.objects;

import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BlackMarketVillagerType implements VillagerType {

    private final String name;
    private final String displayName;
    private final Villager.Profession profession;
    private final Set<ItemStack> trading;
    private final int maxAvailableItems;
    private final int maxTradesPerReboot;
    private final HashMap<UUID, Set<ItemStack>> playerTrades = new HashMap<>();
    private final HashMap<UUID, Integer> playerTradeCount = new HashMap<>();

    public BlackMarketVillagerType(String name, String displayName, String profession, int maxAvailableItems, int maxTradesPerReboot, Set<ItemStack> trading) {
        this.name = name;
        this.displayName = displayName;
        this.profession = Villager.Profession.valueOf(profession.toUpperCase());
        this.maxAvailableItems = maxAvailableItems;
        this.maxTradesPerReboot = maxTradesPerReboot;
        this.trading = trading;
    }

    private Set<ItemStack> getResizedCustomTradingSet() {
        Set<ItemStack> randomSubset = new TreeSet<>(new ItemStackComparator());
        if (trading.size() < maxAvailableItems) {
            randomSubset.addAll(trading);
            return randomSubset;
        }
        List<ItemStack> list = trading.stream().toList();
        Random random = new Random();

        for (int i = 0; i < maxAvailableItems; i++) {
            int randomIndex = random.nextInt(list.size());
            randomSubset.add(list.get(randomIndex));
        }
        return randomSubset;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Set<ItemStack> getBuying() {
        return Set.of();
    }

    @Override
    public Set<ItemStack> getSelling() {
        return Set.of();
    }

    public Set<ItemStack> getTrading(UUID uuid) {
        if (!playerTrades.containsKey(uuid)) {
            playerTrades.put(uuid, getResizedCustomTradingSet());
        }
        return playerTrades.get(uuid);
    }

    public void makeTrade(UUID uuid) {
        playerTradeCount.put(uuid, playerTradeCount.getOrDefault(uuid, 0) + 1);
    }

    public int getRemainingTrades(UUID uuid) {
        return maxTradesPerReboot - playerTradeCount.getOrDefault(uuid, 0);
    }

    @Override
    public Villager.Profession getProfession() {
        return profession;
    }

    @Override
    public String getPermission() {
        return "villagerui.villager." + getName();
    }
}
