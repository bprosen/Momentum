package com.renatusnetwork.parkour.data.perks;

import com.renatusnetwork.parkour.data.elo.ELOTier;
import com.renatusnetwork.parkour.data.elo.ELOTiersManager;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Perk
{
    private String name;
    private String title;
    private String requiredPermission;
    private int price;
    private Material infiniteBlock;
    private ELOTier requiredELOTier;

    private HashMap<PerksArmorType, ItemStack> armorItems;
    private List<Level> requiredLevels;
    private HashSet<String> commands;
    private boolean requiresMasteryLevels;

    public Perk(String name)
    {
        this.requiredLevels = new ArrayList<>();
        this.armorItems = new HashMap<>();
        this.commands = new HashSet<>();
        this.name = name;
        this.title = name;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public ELOTier getRequiredELOTier() { return requiredELOTier; }

    public void setRequiredELOTier(ELOTier requiredELOTier)
    {
        this.requiredELOTier = requiredELOTier;
    }

    public void setTitle(String title) { this.title = title; }

    public boolean isInfiniteBlock() { return infiniteBlock != null; }

    public void setInfiniteBlock(Material infiniteBlock) { this.infiniteBlock = infiniteBlock; }

    public Material getInfiniteBlock() { return infiniteBlock; }

    public String getFormattedTitle() {
        return Utils.translate(title);
    }

    public boolean alreadyRequiresLevel(Level level) { return requiredLevels.contains(level); }

    public void addRequiredLevel(Level level) { requiredLevels.add(level); }

    public void removeRequiredLevel(Level level) { requiredLevels.remove(level); }

    public void setRequiredLevels(List<Level> requiredLevels) { this.requiredLevels = requiredLevels; }

    public void addArmorItem(PerksArmorType type, ItemStack itemStack) { armorItems.put(type, itemStack); }

    public boolean hasArmorItem(PerksArmorType type) { return armorItems.containsKey(type); }

    public void setArmorItems(HashMap<PerksArmorType, ItemStack> armorItems) { this.armorItems = armorItems; }

    public ItemStack getArmorPiece(PerksArmorType type) { return armorItems.get(type); }

    public void removeArmorItem(PerksArmorType type) { armorItems.remove(type); }

    public void setArmorItem(PerksArmorType type, ItemStack item) { armorItems.replace(type, item); }

    public HashMap<PerksArmorType, ItemStack> getArmorItems() { return armorItems; }

    public List<Level> getRequiredLevels() {
        return requiredLevels;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) { this.requiredPermission = requiredPermission; }


    public void setRequiresMasteryLevels(boolean result) { this.requiresMasteryLevels = result; }

    public void toggleRequiresMasteryLevels() { requiresMasteryLevels = !requiresMasteryLevels; }

    public boolean requiresMasteryLevels() { return requiresMasteryLevels; }

    public void setPrice(int price) { this.price = price; }

    public int getPrice() {
        return price;
    }

    public boolean requiresBuying() { return price > 0; }

    public HashSet<String> getCommands() { return commands; }

    public void setCommands(HashSet<String> commands) { this.commands = commands; }

    public void addCommand(String command)
    {
        commands.add(command);
    }

    public void removeCommand(String command)
    {
        commands.remove(command);
    }

    public boolean hasCommand(String command)
    {
        return commands.contains(command);
    }

    public boolean hasAccess(PlayerStats playerStats)
    {
        Player player = playerStats.getPlayer();

        // if they are opped or have the perk, no need to check other requirements
        if (player.isOp() || playerStats.hasPerk(this))
            return true;

        // if it needs a level completion, check all the levels to see if they are missing one, otherwise keep checking
        for (Level level : requiredLevels)
            // if it is a mastery cosmetic and they do not have the mastery needed, then it is false, but if it is not a mastery and they do not have it completed, return false too
            if ((requiresMasteryLevels && !playerStats.hasMasteryCompletion(level)) ||
                (!requiresMasteryLevels && !playerStats.hasCompleted(level)))
                return false;

        // if it can be purchased, immediately return false since we know they don't have access to this perk
        if (price > 0)
            return false;

        // if has elo tier and the required elo is greater than their ELO, do not allow access
        if (requiredELOTier != null && requiredELOTier.getRequiredELO() > playerStats.getELO())
            return false;

        // simple check of if they have permission, return result
        return requiredPermission == null || player.hasPermission(requiredPermission);
    }
}
