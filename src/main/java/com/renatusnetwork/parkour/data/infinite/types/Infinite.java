package com.renatusnetwork.parkour.data.infinite.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.locations.LocationManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Infinite
{
    private PlayerStats playerStats;
    private Location originalLoc;
    private Location startingLocation;
    private int infiniteBlocksSize;
    private HashMap<Integer, Block> blocks; // 0 index = block the player is on, 1 = next block, etc
    private Block plateBlock;
    private int score;

    public Infinite(PlayerStats playerStats, int infiniteBlocksSize)
    {
        this.playerStats = playerStats;
        this.originalLoc = playerStats.getPlayer().getLocation();
        this.infiniteBlocksSize = infiniteBlocksSize;
        this.blocks = new HashMap<>();
        this.startingLocation = Parkour.getInfiniteManager().findStartingLocation();
    }

    // by default respawn will end it
    public void respawn()
    {
        Parkour.getInfiniteManager().endPK(getPlayer());
    }

    public void start()
    {
        addBlock(startingLocation.getBlock());

        // teleport player first!
        teleportToFirst();
        playerStats.setInfinite(true);
    }

    public void teleportToFirst()
    {
        Location tpLoc = getFirstBlock().getLocation().clone();
        tpLoc.setPitch(getPlayer().getLocation().getPitch());
        tpLoc.setYaw(getPlayer().getLocation().getYaw());

        getPlayer().teleport(tpLoc.add(0.5, 1, 0.5));
    }

    public void end()
    {
        playerStats.setInfinite(false);
        getPlayer().teleport(getOriginalLoc()); // tp back
        clearBlocks();
    }

    public void next()
    {
        Location newLocation = getNextLocation();
        // now we have the next loc!
        addBlock(newLocation.getBlock());

        if (plateBlock != null)
            plateBlock.setType(Material.AIR);

        // second block will ALWAYS be the plate!
        Location newPlateLocation = blocks.get(1).getLocation();

        plateBlock = newPlateLocation.add(0, 1, 0).getBlock();
        plateBlock.setType(Material.IRON_PLATE);

        Location last = getLastBlock().getLocation();

        last.getWorld().spawnParticle(Particle.CLOUD, last.getBlockX() + 0.5, last.getBlockY() + 0.5, last.getBlockZ() + 0.5, 15);
        getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.35f, 2f);

    }

    public Location getNextLocation()
    {
        Location newLocation;

        do
            newLocation = Parkour.getInfiniteManager().generateNextBlockLocation(getLastBlock().getLocation(), getPlayer());
        while (!Parkour.getInfiniteManager().isLocationEmpty(newLocation));

        return newLocation;
    }

    public void setOriginalLoc(Location location)
    {
        this.originalLoc = location;
    }

    public PlayerStats getPlayerStats() { return playerStats; }

    public Player getPlayer() {
        return playerStats.getPlayer();
    }

    public String getUUID() {
        return playerStats.getUUID();
    }

    public String getName() {
        return playerStats.getPlayerName();
    }

    public void addScore() {
        score++;
    }

    public int getScore() {
        return score;
    }

    public Location getOriginalLoc() { return originalLoc; }

    public Block getBlock(int index)
    {
        return blocks.get(index);
    }

    public Block getFirstBlock() { return blocks.get(0); }

    public Block getLastBlock()
    {
        return blocks.get(blocks.size() - 1);
    }

    public void clearBlock(int index)
    {
        Block block = blocks.get(index);

        if (block != null)
        {
            // clear plate and block
            block.getLocation().add(0, 1, 0).getBlock().setType(Material.AIR);
            block.setType(Material.AIR);
        }
    }

    public void addBlock(Block block)
    {
        int size = blocks.size();

        // only adjust if more or = to infinitesize blocks are there!
        if (size >= infiniteBlocksSize)
        {
            clearBlock(0); // clear the first block
            // move it all over 1
            for (int i = 1; i < size; i++)
                blocks.replace(i - 1, blocks.get(i));

            blocks.replace(size - 1, block); // replace the last
        }
        else
            blocks.put(size, block);

        // set the actual block
        block.setType(playerStats.getInfiniteBlock());
    }

    public void clearBlocks()
    {
        for (Integer index : blocks.keySet())
            clearBlock(index);
    }

    public Collection<Block> getBlocks()
    {
        return blocks.values();
    }

    public Block getPlateBlock() { return plateBlock; }

    public boolean isSpeedrun()
    {
        return this instanceof Speedrun;
    }

    public boolean isClassic()
    {
        return this instanceof Classic;
    }

    public boolean isTimed()
    {
        return this instanceof Timed;
    }

    public boolean isSprint()
    {
        return this instanceof Sprint;
    }
}
