package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Arrays;

public class FavoriteCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;
        PlayerStats playerStats = Momentum.getStatsManager().get(player);
        if (a.length == 1 && a[0].equalsIgnoreCase("list"))
        {
            if (playerStats.hasFavoriteLevels())
            {
                String favoriteString = "&7Favorite Levels";

                for (Level level : playerStats.getFavoriteLevels())
                    favoriteString += "\n " + level.getTitle();

                sender.sendMessage(Utils.translate(favoriteString));
            }
            else
                sender.sendMessage(Utils.translate("&cYou do not have any favorite levels! Use &4/favorite (level) &cto add a level as a favorite"));
        }
        else if (a.length == 1 && a[0].equalsIgnoreCase("help"))
            sendHelp(sender);
        else if (a.length >= 2 && a[0].equalsIgnoreCase("remove"))
        {
            Level level = parseLevelFromArguments(sender, a, 1);

            if (level != null)
            {
                if (playerStats.hasFavorite(level))
                {
                    Momentum.getStatsManager().removeFavoriteLevel(playerStats, level);
                    sender.sendMessage(Utils.translate("&7You have removed &2" + level.getTitle() + "&7 from your favorite levels"));
                }
                else
                    sender.sendMessage(Utils.translate("&cYou do not have &4" + level.getTitle() + " &cas a favorite level"));
            }
        }
        else if (a.length >= 1)
        {
            Level level = parseLevelFromArguments(sender, a, 0);

            if (level != null)
            {
                if (playerStats != null && playerStats.isLoaded())
                {
                    if (playerStats.numFavoriteLevels() < Momentum.getSettingsManager().max_favorite_levels)
                    {
                        if (playerStats.hasCompleted(level))
                        {
                            if (Momentum.getLevelManager().isLevelInMenus(level))
                            {
                                if (!playerStats.hasFavorite(level))
                                {
                                    Momentum.getStatsManager().addFavoriteLevel(playerStats, level);
                                    sender.sendMessage(Utils.translate("&7You have added &2" + level.getTitle() + "&7 to your favorite levels"));
                                }
                                else
                                    sender.sendMessage(Utils.translate("&cYou already have &4" + level.getTitle() + "&c as a favorite"));
                            }
                            else
                                sender.sendMessage(Utils.translate("&cYou cannot favorite &4" + level.getTitle() + "&c when it is not in the menus"));
                        }
                        else
                            sender.sendMessage(Utils.translate("&cYou cannot favorite &4" + level.getTitle() + "&c when you haven't completed it"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cYou cannot favorite more than &4" + Momentum.getSettingsManager().max_favorite_levels + " &clevels"));
                }
                else
                    sender.sendMessage(Utils.translate("&cYou cannot do this while loading your stats"));
            }
        }
        else
            sendHelp(sender);

        return false;
    }

    private void sendHelp(CommandSender sender)
    {
        sender.sendMessage(Utils.translate("&a&lFavorite Help"));
        sender.sendMessage(Utils.translate("&a/favorite list  &7Lists your favorite levels"));
        sender.sendMessage(Utils.translate("&a/favorite remove (level)  &7Removes that favorite level"));
        sender.sendMessage(Utils.translate("&a/favorite (level)  &7Favorites a level"));
        sender.sendMessage(Utils.translate("&a/favorite help  &7Displays help page"));
    }

    private Level parseLevelFromArguments(CommandSender sender, String[] args, int startingIndex)
    {
        // allow ability to get from title or name
        String levelName = args[startingIndex].toLowerCase();
        if (args.length > (startingIndex + 1))
        {
            String[] split = Arrays.copyOfRange(args, startingIndex, args.length);
            levelName = String.join(" ", split);
        }

        // if it does not get it from name, then attempt to get it from title
        Level level = Momentum.getLevelManager().getNameThenTitle(levelName);

        if (level == null)
            sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a level"));

        return level;
    }
}