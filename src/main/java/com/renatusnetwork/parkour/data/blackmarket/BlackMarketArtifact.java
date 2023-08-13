package com.renatusnetwork.parkour.data.blackmarket;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BlackMarketArtifact
{
    private String name;
    private String title;
    private String description;
    private int startingBid;
    private float nextBidMultiplier;
    private ItemStack item;
    private List<String> rewardCommands;

    public BlackMarketArtifact(String name)
    {
        this.name = name;
        this.startingBid = BlackMarketYAML.getStartingBid(name);
        this.nextBidMultiplier = BlackMarketYAML.getNextBidMultiplier(name);
        this.item = BlackMarketYAML.getItem(name);
        this.title = BlackMarketYAML.getTitle(name);
        this.description = BlackMarketYAML.getDescription(name);
        this.rewardCommands = BlackMarketYAML.getRewardCommands(name);
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription() { return description; }

    public ItemStack getItemStack() { return item; }

    public int getStartingBid()
    {
        return startingBid;
    }

    public float getNextBidMultiplier()
    {
        return nextBidMultiplier;
    }

    public List<String> getRewardCommands() { return rewardCommands; }
}
