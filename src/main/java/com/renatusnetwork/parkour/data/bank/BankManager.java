package com.renatusnetwork.parkour.data.bank;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.types.*;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BankManager
{
    private HashMap<BankItemType, BankItem> items;
    private Jackpot currentJackpot;

    public BankManager()
    {
        currentJackpot = null;

        loadCache();
        runScheduler();
    }

    public void resetItems()
    {
        // reset items
        String radiantItem = BankYAML.chooseBankItem(BankItemType.RADIANT);
        String brilliantItem = BankYAML.chooseBankItem(BankItemType.BRILLIANT);
        String legendaryItem = BankYAML.chooseBankItem(BankItemType.LEGENDARY);

        // reset info and set new modifier
        BankYAML.resetBid(BankItemType.RADIANT, BankYAML.getModifier(BankItemType.RADIANT, radiantItem));
        BankYAML.resetBid(BankItemType.BRILLIANT, BankYAML.getModifier(BankItemType.BRILLIANT, brilliantItem));
        BankYAML.resetBid(BankItemType.LEGENDARY, BankYAML.getModifier(BankItemType.LEGENDARY, legendaryItem));

        loadCache();
    }

    public void broadcastReset()
    {
        Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));
        Bukkit.broadcastMessage(Utils.translate("&d&lTHE BANK HAS BEEN RESET"));
        Bukkit.broadcastMessage(Utils.translate("&7Head to &c/spawn &7to start bidding on the bank!"));
        Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));
    }

    private void loadCache()
    {
        items = new HashMap<>();

        // add into map as polymorphic
        items.put(BankItemType.RADIANT, new RadiantItem());
        items.put(BankItemType.BRILLIANT, new BrilliantItem());
        items.put(BankItemType.LEGENDARY, new RadiantItem());
    }

    private void runScheduler()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (ThreadLocalRandom.current().nextInt(0, 10) == 0) // 10% chance every 6 hours
                    startJackpot();
            }
        }.runTaskTimerAsynchronously(Parkour.getPlugin(), 20 * 21600, 20 * 21600);
    }

    public void startJackpot()
    {
        if (currentJackpot == null)
        {
            ArrayList<Level> tempList = new ArrayList<>();

            for (Level level : Parkour.getLevelManager().getLevelsInAllMenus())
                // only allow levels with reward > 0 and <= 5000
                if (level.getRequiredLevels().isEmpty() && !level.isFeaturedLevel() && !level.hasPermissionNode() && !level.isRankUpLevel() && !level.isAscendanceLevel() && level.getReward() > 0 && level.getReward() <= 5000)
                    tempList.add(level);

            Level level = tempList.get(new Random().nextInt(tempList.size()));
            long totalBalance = totalBalanceInBank();

            // max of 6.25 mill = 50k bonus
            if (totalBalance > 6250000)
                totalBalance = 6250000;

            int bonus = (int) (20 * Math.sqrt(totalBalance));

            currentJackpot = new Jackpot(level, bonus);
            currentJackpot.start(); // begin jackpot
        }
        else
        {
            Parkour.getPluginLogger().info("Tried to start Jackpot with one already running");
        }
    }

    public void chooseJackpot(Level level, int bonus)
    {
        if (currentJackpot == null)
        {
            currentJackpot = new Jackpot(level, bonus);
            currentJackpot.start();
        }
        else
        {
            Parkour.getPluginLogger().info("Tried to choose Jackpot with one already running");
        }
    }

    public void endJackpot()
    {
        if (currentJackpot != null)
        {
            currentJackpot.end();
            currentJackpot = null;
        }
        else
        {
            Parkour.getPluginLogger().info("Tried to end Jackpot with none currently running");
        }
    }
    public boolean isJackpotRunning()
    {
        return currentJackpot != null;
    }

    public long totalBalanceInBank()
    {
        long balance = 0;

        for (BankItem bankItem : items.values())
            balance += bankItem.getTotalBalance();

        return balance;
    }

    public Jackpot getJackpot()
    {
        return currentJackpot;
    }

    public BankItem getItem(BankItemType type)
    {
        return items.get(type);
    }
    public boolean isType(String typeName)
    {
        boolean result = false;

        // try catch to determine
        try
        {
            BankItemType type = BankItemType.valueOf(typeName);
            result = true;
        }
        catch (IllegalArgumentException ignored)
        {}

        return result;
    }

    public void bid(PlayerStats playerStats, BankItemType type)
    {
        BankItem bankItem = items.get(type);

        // make sure they do not bid on themselves
        if (!bankItem.hasCurrentHolder() || !bankItem.getCurrentHolder().equalsIgnoreCase(playerStats.getPlayerName()))
        {
            int bidAmount = bankItem.getNextBid();

            if (playerStats.getCoins() >= bidAmount)
            {
                String oldHolder = bankItem.getCurrentHolder();
                Player player = Bukkit.getPlayer(oldHolder);
                Modifier modifier = bankItem.getModifier();

                // remove from cache
                if (player != null)
                    Parkour.getModifiersManager().removeModifier(Parkour.getStatsManager().get(player), modifier);
                else
                    // remove from db only
                    Parkour.getDatabaseManager().add("DELETE FROM modifiers WHERE player_name='" + oldHolder + "' AND modifier_name='" + modifier.getName() + "'");

                Parkour.getStatsManager().removeCoins(playerStats, bidAmount); // remove coins
                bankItem.setCurrentHolder(playerStats.getPlayerName()); // update current holder
                bankItem.addBid(playerStats, bidAmount); // update in cache
                bankItem.broadcastNewBid(playerStats, bidAmount); // broadcast bid
                BankYAML.updateBid(type, bankItem.getTotalBalance(), playerStats.getPlayerName()); // update in config

                // update player info
                Parkour.getModifiersManager().addModifier(playerStats, bankItem.getModifier());
            }
            else
            {
                playerStats.getPlayer().sendMessage(Utils.translate("&cYou do not have enough coins to raise the bid to &6" + Utils.formatNumber(bidAmount) + " &eCoins"));
            }
        }
        else
        {
            playerStats.getPlayer().sendMessage(Utils.translate("&cYou cannot bid on yourself"));
        }
    }
}
