package com.renatusnetwork.momentum.data.bank;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.bank.items.*;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.modifiers.Modifier;
import com.renatusnetwork.momentum.data.stats.BankBid;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsDB;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.TimeUtils;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BankManager
{
    private HashMap<BankItemType, BankItem> items;
    private Jackpot currentJackpot;
    private int currentWeek;

    public BankManager()
    {
        this.currentWeek = Math.max(BankDB.getCurrentWeek(), 0);
        this.items = currentWeek > 0 ? BankDB.getItems(this.currentWeek) : new HashMap<>();

        runScheduler();

        Momentum.getPluginLogger().info("Bank week: " + this.currentWeek);
    }

    public boolean resetBank()
    {
        if (loadNewItems())
        {
            clearPlayersBids();
            broadcastReset();
            return true;
        }
        return false;
    }

    public void clearPlayersBids()
    {
        HashMap<String, PlayerStats> players = Momentum.getStatsManager().getPlayerStats();

        synchronized (players)
        {
            players.values().forEach(PlayerStats::resetBankBids);
        }
    }
    public boolean loadNewItems()
    {
        for (BankItemType type : BankItemType.values())
        {
            BankItem item = items.get(type);

            if (item != null && item.hasCurrentHolder() && item.getModifier() != null)
                Momentum.getStatsManager().removeModifierName(item.getCurrentHolder(), item.getModifier());

            BankItem bankItem = BankDB.createRandomBankItem(type);

            if (bankItem != null)
                items.put(type, bankItem);
        }

        if (items.size() == 3)
        {
            String brilliantItemName = items.get(BankItemType.BRILLIANT).getName();
            String radiantItemName = items.get(BankItemType.RADIANT).getName();
            String legendaryItemName = items.get(BankItemType.LEGENDARY).getName();

            this.currentWeek++;

            BankDB.insertWeek(currentWeek, brilliantItemName, radiantItemName, legendaryItemName);
            return true;
        }
        else
            items.clear();

        return false;
    }

    public void broadcastReset()
    {
        Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));
        Bukkit.broadcastMessage(Utils.translate("&d&lTHE BANK HAS BEEN RESET"));
        Bukkit.broadcastMessage(Utils.translate("&7Head to &c/spawn &7to start bidding on the bank!"));
        Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));
        Utils.playSound(Sound.ENTITY_PLAYER_LEVELUP);
    }

    private void runScheduler()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (currentWeek > 0 && ThreadLocalRandom.current().nextInt(0, 10) == 0) // 10% chance every 6 hours
                    startJackpot();
            }
        }.runTaskTimerAsynchronously(Momentum.getPlugin(), 20 * 21600, 20 * 21600);
    }

    public void bid(PlayerStats playerStats, BankItem bankItem)
    {
        BankItemType type = bankItem.getType();

        // make sure they do not bid on themselves
        if (!bankItem.hasCurrentHolder() || !bankItem.getCurrentHolder().equalsIgnoreCase(playerStats.getName()))
        {
            if (!bankItem.isLocked())
            {
                int bidAmount = bankItem.getNextBid();

                if (playerStats.getCoins() >= bidAmount)
                {
                    if (!alreadyHoldingOtherItem(playerStats, bankItem.getType()))
                    {
                        String oldHolder = bankItem.getCurrentHolder();

                        Modifier modifier = bankItem.getModifier();
                        StatsManager statsManager = Momentum.getStatsManager();
                        PlayerStats holderStats = statsManager.getByName(oldHolder);

                        // remove from cache
                        if (holderStats != null)
                            statsManager.removeModifier(holderStats, modifier);
                        else if (oldHolder != null)
                            StatsDB.removeModifierName(oldHolder, modifier.getName());

                        Momentum.getStatsManager().removeCoins(playerStats, bidAmount); // remove coins
                        bankItem.setCurrentHolder(playerStats.getName()); // update current holder
                        bankItem.addTotal(bidAmount); // update in cache
                        bankItem.calcNextBid(bidAmount); // calc next bid

                        statsManager.updateBankBid(playerStats, type, bidAmount);
                        playerStats.getPlayer().playSound(playerStats.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                        Utils.spawnFirework(playerStats.getPlayer().getLocation(), Color.PURPLE, Color.WHITE, true);

                        // update player info
                        statsManager.addModifier(playerStats, bankItem.getModifier());

                        int lockTime = lock(playerStats, bankItem);
                        broadcastNewBid(playerStats, bankItem, bidAmount, lockTime); // broadcast bid
                    }
                    else
                        playerStats.sendMessage(Utils.translate("&cYou cannot hold two bank items at once!"));
                }
                else
                    playerStats.sendMessage(Utils.translate("&cYou do not have enough coins to raise the bid to &6" + Utils.formatNumber(bidAmount) + " &eCoins"));
            }
            else
                playerStats.sendMessage(Utils.translate("&cYou cannot bid when it is locked!"));
        }
        else
            playerStats.sendMessage(Utils.translate("&cYou cannot bid on yourself"));
    }

    private int lock(PlayerStats playerStats, BankItem bankItem)
    {
        int lockTime = calculateLockTime(playerStats, bankItem.getType());

        // lock timer
        bankItem.lock(lockTime);

        // unlock timer
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));
                Bukkit.broadcastMessage(Utils.translate("&d&lTHE " + bankItem.getFormattedType() + " &d&lBANK HAS UNLOCKED"));
                Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));

                bankItem.removeLock();
            }
        }.runTaskLater(Momentum.getPlugin(), 20L * 60 * lockTime);

        return lockTime;
    }

    private void broadcastNewBid(PlayerStats playerStats, BankItem item, int bidAmount, int lockTime)
    {
        HashMap<String, PlayerStats> players = Momentum.getStatsManager().getPlayerStats();

        // thread safety
        synchronized (players)
        {
            for (PlayerStats stats : players.values())
            {
                Player player = stats.getPlayer();
                int nextBid = item.getNextBid();

                // only send new bank bid to people in spawn
                player.sendMessage(Utils.translate("&d&m----------------------------------------"));
                player.sendMessage(Utils.translate("&d&lNEW " + item.getFormattedType() + " &d&lBANK BID"));
                player.sendMessage(Utils.translate("&4&lLOCKED &7for &6" + lockTime + " &eminutes"));
                player.sendMessage(Utils.translate("&d" + playerStats.getDisplayName() + " &7bid &6" + Utils.formatNumber(bidAmount) + " &eCoins &7for " + item.getTitle()));

                if (!item.isCurrentHolder(stats))
                {
                    player.sendMessage("");
                    player.sendMessage(Utils.translate("&7Bid &6" + Utils.formatNumber(nextBid) + " &eCoins &7at &c/spawn &7to overtake " + playerStats.getDisplayName()));
                }

                player.sendMessage(Utils.translate("&d&m----------------------------------------"));
            }
        }
    }

    public int getCurrentWeek()
    {
        return currentWeek;
    }

    public boolean alreadyHoldingOtherItem(PlayerStats playerStats, BankItemType type)
    {
        for (Map.Entry<BankItemType, BankItem> entry : Momentum.getBankManager().getItems().entrySet())
            if (entry.getKey() != type && entry.getValue().isCurrentHolder(playerStats))
                return true;

        return false;
    }

    public void startJackpot()
    {
        if (currentJackpot == null)
        {
            ArrayList<Level> tempList = new ArrayList<>();

            for (Level level : Momentum.getLevelManager().getLevelsInAllMenus())
                // only allow levels with reward > 0 and <= 5000
                if (level.getRequiredLevels().isEmpty() && !level.isFeaturedLevel() && !level.hasPermissionNode() && !level.isRankUpLevel() && !level.isAscendance() && level.hasReward() && level.getReward() <= 5000)
                    tempList.add(level);

            Level level = tempList.get(new Random().nextInt(tempList.size()));
            long totalBalance = totalBalanceInBank();

            // max of 6.25 mill = 50k bonus
            if (totalBalance > 6250000)
                totalBalance = 6250000;

            int bonus = (int) (20 * Math.sqrt(totalBalance));

            currentJackpot = new Jackpot(level, bonus);
            currentJackpot.start(); // begin jackpot
            Utils.playSound(Sound.BLOCK_NOTE_PLING);
        }
        else
        {
            Momentum.getPluginLogger().info("Tried to start Jackpot with one already running");
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
            Momentum.getPluginLogger().info("Tried to choose Jackpot with one already running");
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
            Momentum.getPluginLogger().info("Tried to end Jackpot with none currently running");
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

    public int calculateLockTime(PlayerStats playerStats, BankItemType type)
    {
        BankBid bid = playerStats.getBankBid(type);

        int bidAmount = bid != null ? bid.getBid() : 0;
        int lockTime = ((int) Math.sqrt(bidAmount / 82f)) + 45;

        return Math.max(Momentum.getSettingsManager().min_lock_time, Math.min(Momentum.getSettingsManager().max_lock_time, lockTime));
    }

    public HashMap<BankItemType, BankItem> getItems() { return items; }

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
}
