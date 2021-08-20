package com.parkourcraft.parkour.data.perks;

import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class Perk {

    private String name;
    private String title;
    private HashMap<String, ItemStack> items;
    private List<String> shortenedRequirementsLore;
    private List<String> requirements;
    private List<String> requiredPermissions;

    private int price;
    private int ID = -1;

    public Perk(String perkName) {
        name = perkName;

        load();
    }

    public void load() {
        if (PerksYAML.exists(name)) {
            title = PerksYAML.getTitle(name);
            items = PerksYAML.getItems(name);
            requirements = PerksYAML.getRequirements(name);
            requiredPermissions = PerksYAML.getRequiredPermissions(name);
            price = PerksYAML.getPrice(name);
            shortenedRequirementsLore = PerksYAML.getShortenedRequirementsLore(name);
        }
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getFormattedTitle() {
        return Utils.translate(title);
    }

    public HashMap<String, ItemStack> getItems() { return items; }

    public List<String> getRequirements() {
        return requirements;
    }

    public List<String> getRequiredPermissions() {
        return requiredPermissions;
    }

    public List<String> getShortenedRequirementsLore() { return shortenedRequirementsLore; }

    public boolean hasShortenedRequirementsLore() { return shortenedRequirementsLore != null; }
    public int getPrice() {
        return price;
    }

    public int getID() {
        return ID;
    }

    public boolean hasRequirements(PlayerStats playerStats, Player player) {
        for (String requiredPermission : requiredPermissions)
            if (!player.hasPermission(requiredPermission))
                return false;

        for (String levelRequirement : requirements)
            if (playerStats.getLevelCompletionsCount(levelRequirement) < 1)
                return false;

        if (price > 0)
            if (!playerStats.hasPerk(name))
                return false;

        return true;
    }

}
