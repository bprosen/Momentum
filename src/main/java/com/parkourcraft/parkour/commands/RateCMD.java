package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.data.levels.LevelManager;
import com.parkourcraft.parkour.data.levels.RatingDB;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class RateCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (a.length >= 2) {

            LevelManager levelManager = Parkour.getLevelManager();
            String levelName;
            Level level;

            // if just levelName like coal-1
            if (a.length == 2) {
                levelName = a[0].toLowerCase();
                level = levelManager.get(levelName);
            // or if they want to use the title like Coal 1
            } else {
                // have to do -1 from a.length due to rating being after name
                String[] split = Arrays.copyOfRange(a, 0, a.length - 1);
                levelName = String.join(" ", split);
                level = levelManager.getFromTitle(levelName);
            }

            if (level != null) {
                // rating will be -1 the length of a
                if (Utils.isInteger(a[a.length - 1])) {

                    int rating = Integer.parseInt(a[a.length - 1]);
                    PlayerStats playerStats = Parkour.getStatsManager().get(player);

                    if (playerStats.getLevelCompletionsCount(level.getName()) > 0) {
                        if (!RatingDB.hasRatedLevel(player.getUniqueId().toString(), level.getID())) {
                            if (rating >= 0 && rating <= 5) {

                                level.addRatingAndCalc(rating);
                                RatingDB.addRating(player, level, rating);
                                player.sendMessage(Utils.translate("&7You rated &c" + level.getFormattedTitle() + " &7a &6" + rating + "&7! Thank you for rating!"));
                            } else {
                                player.sendMessage(Utils.translate("&cYour rating has to be anywhere from 0 to 5!"));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&cYou have already rated this level"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou have not yet completed &c" + level.getFormattedTitle()
                                + " &cyet to be able to rate it!"));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&4" + a[1] + " &cis not a valid integer"));
                }
            } else {
                sender.sendMessage(Utils.translate("&cNo level named &c" + levelName + " &cexists"));
            }
        }
        return false;
    }
}
