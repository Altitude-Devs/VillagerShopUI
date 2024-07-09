package com.alttd.objects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ShopVillagerType implements VillagerType{
    private final String name;
    private final String displayName;
    private final Set<ItemStack> buying;
    private final Set<ItemStack> selling;
    private final Villager.Profession profession;
    private final List<String> messages = new ArrayList<>();

    private final Random random = new Random();

    public ShopVillagerType(String name, String displayName, TreeSet<ItemStack> buying, TreeSet<ItemStack> selling, String profession) {
        this.name = name;
        this.displayName = displayName;
        this.buying = buying;
        this.selling = selling;
        this.profession = Villager.Profession.valueOf(profession.toUpperCase());
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<ItemStack> getBuying() {
        return buying;
    }

    public Set<ItemStack> getSelling() {
        return selling;
    }

    public Villager.Profession getProfession() {
        return profession;
    }

    public String getPermission() {
        return "villagerui.villager." + getName();
    }

    public Optional<Component> getRandomMessage() {
        if (messages.isEmpty()) {
            return Optional.empty();
        }
        int index = random.nextInt(messages.size());
        String message = messages.get(index);
        return Optional.of(MiniMessage.miniMessage().deserialize(message));
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public void setMessages(List<String> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
    }
}
