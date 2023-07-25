package com.renatusnetwork.parkour.data.bank.types;

import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;

import java.util.HashSet;

public class Jackpot
{
    private Level level;
    private long bonus;
    private HashSet<String> completed;

    public Jackpot(Level level, long bonus)
    {
        this.level = level;
        this.bonus = bonus;

        completed = new HashSet<>();
    }

    public void addCompleted(String playerName)
    {
        completed.add(playerName);
    }

    public boolean hasCompleted(String playerName)
    {
        return completed.contains(playerName);
    }

    public void broadcastCompletion(String playerName)
    {
        Bukkit.broadcastMessage(Utils.translate("&2&m----------------------------------------"));
        Bukkit.broadcastMessage(Utils.translate(" &a&l" + playerName + " COMPLETED THE JACKPOT"));
        Bukkit.broadcastMessage(Utils.translate("   &7Type &2/jackpot play &7to join in!"));
        Bukkit.broadcastMessage(Utils.translate("&2&m----------------------------------------"));
    }
}
