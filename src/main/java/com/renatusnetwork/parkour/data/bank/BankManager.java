package com.renatusnetwork.parkour.data.bank;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.types.*;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifiersDB;
import com.renatusnetwork.parkour.data.modifiers.ModifiersManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
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
        BankItem radiant = items.get(BankItemType.RADIANT);
        BankItem brilliant = items.get(BankItemType.BRILLIANT);
        BankItem legendary = items.get(BankItemType.LEGENDARY);
        ModifiersManager modifiersManager = Parkour.getModifiersManager();

        // remove modifiers
        modifiersManager.removeModifierName(radiant.getCurrentHolder(), radiant.getModifier());
        modifiersManager.removeModifierName(brilliant.getCurrentHolder(), brilliant.getModifier());
        modifiersManager.removeModifierName(legendary.getCurrentHolder(), legendary.getModifier());

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
        items.put(BankItemType.LEGENDARY, new LegendaryItem());
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
            if (!bankItem.isLocked())
            {
                int bidAmount = bankItem.getNextBid();

                if (playerStats.getCoins() >= bidAmount)
                {

                    boolean alreadyHasTier = false;

                    // check through all items, for the items not currently being picked and if the current holder is the player, deny them (cant have two at once)
                    for (BankItem item : items.values())
                        if (item.getType() != type && item.getCurrentHolder().equalsIgnoreCase(playerStats.getPlayerName()))
                            alreadyHasTier = true;

                    if (!alreadyHasTier)
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
                        bankItem.addTotal(bidAmount); // update in cache
                        bankItem.calcNextBid(); // calc next bid
                        broadcastNewBid(playerStats, bankItem, bidAmount); // broadcast bid
                        BankYAML.updateBid(type, bankItem.getTotalBalance(), playerStats.getPlayerName()); // update in config

                        // update player info
                        Parkour.getModifiersManager().addModifier(playerStats, bankItem.getModifier());

                        // lock chance (has to be minimum and 10% chance)
                        if (bankItem.getMinimumLock() < bankItem.getTotalBalance() && ThreadLocalRandom.current().nextDouble(0, 100) <= Parkour.getSettingsManager().lock_chance)
                            lock(playerStats, bankItem);
                    }
                    else
                    {
                        playerStats.getPlayer().sendMessage(Utils.translate("&cYou cannot hold two bank items at once!"));
                    }
                }
                else
                {
                    playerStats.getPlayer().sendMessage(Utils.translate("&cYou do not have enough coins to raise the bid to &6" + Utils.formatNumber(bidAmount) + " &eCoins"));
                }
            }
            else
            {
                playerStats.getPlayer().sendMessage(Utils.translate("&cYou cannot bid when it is locked!"));
            }
        }
        else
        {
            playerStats.getPlayer().sendMessage(Utils.translate("&cYou cannot bid on yourself"));
        }
    }

    private void lock(PlayerStats playerStats, BankItem bankItem)
    {
        // lock timer
        bankItem.setLocked(true);

        Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));
        Bukkit.broadcastMessage(Utils.translate("&d&lTHE " + bankItem.getFormattedType() + " &d&lBANK HAS LOCKED"));
        Bukkit.broadcastMessage(Utils.translate("&c" + playerStats.getPlayer().getDisplayName() + " &7gets &d" + bankItem.getTitle() + " &7for &d" + Parkour.getSettingsManager().lock_minutes + " minutes"));
        Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));

        // unlock timer
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));
                Bukkit.broadcastMessage(Utils.translate("&d&lTHE " + bankItem.getFormattedType() + " &d&lBANK HAS UNLOCKED"));
                Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));

                bankItem.setLocked(false);
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * 60 * Parkour.getSettingsManager().lock_minutes);
    }

    private void broadcastNewBid(PlayerStats playerStats, BankItem item, int bidAmount)
    {
        // only for people in spawn
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                HashMap<String, PlayerStats> players = Parkour.getStatsManager().getPlayerStats();

                // thread safety
                synchronized (players)
                {
                    for (PlayerStats stats : players.values())
                    {
                        Player player = stats.getPlayer();

                        // only send new bank bid to people in spawn
                        if (!stats.inLevel() && player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().main_world.getName()))
                        {
                            player.sendMessage(Utils.translate("&d&m----------------------------------------"));
                            player.sendMessage(Utils.translate("&d&lNEW " + item.getFormattedType() + " &d&lBANK BID"));
                            player.sendMessage(Utils.translate("&d" + playerStats.getPlayer().getDisplayName() + " &7bid &6" + Utils.formatNumber(bidAmount) + " &eCoins &7for " + item.getTitle()));
                            player.sendMessage(Utils.translate("&7Pay &6" + Utils.formatNumber(item.getNextBid()) + " &eCoins &7at &c/spawn &7to overtake " + playerStats.getPlayer().getDisplayName()));
                            player.sendMessage(Utils.translate("&d&m----------------------------------------"));
                        }
                    }
                }
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }
}
