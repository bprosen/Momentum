package com.parkourcraft.parkour.data.menus;

import com.connorlinfoot.titleapi.TitleAPI;
import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.CheckpointDB;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.data.levels.RatingDB;
import com.parkourcraft.parkour.data.perks.Perk;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.data.plots.PlotsDB;
import com.parkourcraft.parkour.data.ranks.RanksDB;
import com.parkourcraft.parkour.data.ranks.RanksYAML;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import java.util.List;

public class MenuItemAction {

    private static void runCommands(Player player, List<String> commands, List<String> consoleCommands) {
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        boolean armorCommand = false;

        // loop through to see if there is a chest setarmor
        if (!commands.isEmpty())
            for (String cmd : commands)
                if (cmd.startsWith("setarmor chest")) {
                    armorCommand = true;
                    break;
                }
        // if so, check if they are in elytra level
        if (armorCommand && playerStats != null && playerStats.getLevel() != null && playerStats.getLevel().isElytraLevel()) {

            player.sendMessage(Utils.translate("&cYou cannot change your armor in an Elytra level"));
            player.closeInventory();
            return;
        } else {

            for (String command : commands)
                player.performCommand(command.replace("%player%", player.getName()));

            for (String command : consoleCommands)
                Bukkit.dispatchCommand(
                        Parkour.getPlugin().getServer().getConsoleSender(),
                        command.replace("%player%", player.getName())
                );
        }
    }

    public static void perform(Player player, MenuItem menuItem) {
        String itemType = menuItem.getType();

        if (itemType.equals("perk"))
            performPerkItem(player, menuItem);
        else if (itemType.equals("level"))
            performLevelItem(player, menuItem);
        else if (itemType.equals("teleport"))
            performTeleportItem(player, menuItem);
        else if (itemType.equals("open"))
            // if it is rankup gui, do special method for it
            if (menuItem.getTypeValue().equals("rankup"))
                performRankupOpen(player, menuItem);
            else
                performOpenItem(player, menuItem);
        else if (itemType.equals("rate"))
            performLevelRate(player, menuItem);
        else if (itemType.equals("type")) {

            // certain conditions of types for rankup
            String typeValue = menuItem.getTypeValue();
            if (typeValue.equals("coin-rankup"))
                performRankupItem(player);
            else if (typeValue.equals("rankup-level-1")
                    || typeValue.equals("rankup-level-2")
                    || typeValue.equals("rankup-level"))
                performLevelRankUpItem(player, menuItem);
            else if (typeValue.equals("submit-plot"))
                performPlotSubmission(player);
            // dont need to get from stats and can skip performLevelItem
            else if (typeValue.equals("featured-level"))
                performLevelTeleport(Parkour.getStatsManager().get(player.getUniqueId().toString()),
                        player,
                        Parkour.getLevelManager().getFeaturedLevel());
            else if (typeValue.equals("exit"))
                player.closeInventory();
        } else if (menuItem.hasCommands())
            runCommands(player, menuItem.getCommands(), menuItem.getConsoleCommands());
    }

    private static void performRankupOpen(Player player, MenuItem menuItem) {

        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        String menuName = null;
        if (playerStats.getRankUpStage() == 1)
            menuName = "coin-rankup";
            // stage 2, meaning level rankup part
        else if (playerStats.getRankUpStage() == 2) {
            // get if it is a single level style rankup (expert and up)
            if (RanksYAML.isSingleLevelRankup(playerStats.getRank().getRankName()))
                menuName = "single-level-rankup";
            else
                menuName = "double-level-rankup";
        }

        if (menuName != null) {
            Menu menu = Parkour.getMenuManager().getMenu(menuName);

            if (menu != null) {
                int pageNumber = Utils.getTrailingInt(menuItem.getTypeValue());

                Inventory inventory = Parkour.getMenuManager().getInventory(menu.getName(), pageNumber);

                if (inventory != null) {
                    player.closeInventory();
                    player.openInventory(inventory);
                    Parkour.getMenuManager().updateInventory(player, player.getOpenInventory(), menu.getName(), pageNumber);
                }
            }
        }
    }

    private static void performLevelRate(Player player, MenuItem menuItem) {

        // strip title since title will be "Rate (levelName)"
        String levelTitle = ChatColor.stripColor(player.getOpenInventory().getTopInventory().getTitle()).split("Rate ")[1];
        player.closeInventory();

        if (Utils.isInteger(menuItem.getTypeValue())) {
            Level level = Parkour.getLevelManager().getFromTitle(levelTitle);

            if (level != null) {
                int rating = Integer.parseInt(menuItem.getTypeValue());

                if (rating >= 0 && rating <= 5) {

                    level.addRatingAndCalc(rating);
                    RatingDB.addRating(player, level, rating);
                    player.sendMessage(Utils.translate("&7You rated &c" + level.getFormattedTitle() + " &7a &6" + rating + "&7! Thank you for rating!"));
                } else {
                    player.sendMessage(Utils.translate("&cYour rating has to be anywhere from 0 to 5!"));
                }
            } else {
                player.sendMessage(Utils.translate("&cSomething went wrong with level &4" + levelTitle + "&c, does it exist?"));
            }
        } else {
            player.sendMessage(Utils.translate("&cSomething went wrong, try again?"));
        }
    }

    private static void performPlotSubmission(Player player) {
        player.closeInventory();

        Plot plot = Parkour.getPlotsManager().get(player.getName());

        if (plot != null) {
            if (!plot.isSubmitted()) {
                // submit map
                plot.submit();
                PlotsDB.toggleSubmitted(player.getUniqueId().toString());

                player.sendMessage("");
                player.sendMessage(Utils.translate("&7You have &6submitted &7your plot! Please wait until an" +
                                    " &6Administrator &7gets a chance to look at it. Thank you for submitting."));
                player.sendMessage("");
            } else {
                player.sendMessage(Utils.translate("&cYou cannot submit a plot you already submitted"));
            }
        } else {
            player.sendMessage(Utils.translate("&cYou do not have a plot"));
        }
    }

    private static void performPerkItem(Player player, MenuItem menuItem) {
        Perk perk = Parkour.getPerkManager().get(menuItem.getTypeValue());

        if (perk != null) {
            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            // bypass if opped
            if (player.isOp() || perk.hasRequirements(playerStats, player)) {
                player.closeInventory();
                Parkour.getPerkManager().setPerk(perk, player);

                // if has commands, run them
                if (menuItem.hasCommands())
                    runCommands(player, menuItem.getCommands(), menuItem.getConsoleCommands());
            } else if (!playerStats.hasPerk(perk.getName())
                    && perk.getPrice() > 0) {
                int playerBalance = (int) Parkour.getEconomy().getBalance(player);

                if (playerBalance > perk.getPrice()) {
                    Parkour.getEconomy().withdrawPlayer(player, perk.getPrice());
                    Parkour.getPerkManager().bought(playerStats, perk);
                    Parkour.getMenuManager().updateInventory(player, player.getOpenInventory());
                }
            }
        }
    }

    private static void performLevelRankUpItem(Player player, MenuItem menuItem) {

        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        String rankName = playerStats.getRank().getRankName();
        String levelType = menuItem.getTypeValue();
        String levelName = RanksYAML.getRankUpLevel(rankName, levelType);

        if (levelName != null) {
            Level level = Parkour.getLevelManager().get(levelName);
            if (level != null)
                performLevelTeleport(playerStats, player, level);
        }
    }

    private static void performLevelItem(Player player, MenuItem menuItem) {
        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        Level level = Parkour.getLevelManager().get(menuItem.getTypeValue());

        if (level != null)
            performLevelTeleport(playerStats, player, level);
    }

    private static void performLevelTeleport(PlayerStats playerStats, Player player, Level level) {
        if (!playerStats.inRace()) {
            if (playerStats.getPlayerToSpectate() == null) {
                if (!playerStats.isEventParticipant()) {
                    if (!playerStats.isInInfinitePK()) {
                        if (level.hasRequiredLevels(playerStats)) {

                            player.closeInventory();

                            // if the level has perm node, and player does not have perm node
                            if (level.hasPermissionNode() && !player.hasPermission(level.getRequiredPermissionNode())) {
                                player.sendMessage(Utils.translate("&cYou do not have permission to enter this level"));
                                return;
                            }

                            // if player is in level and their level is the level they clicked on, cancel
                            if (playerStats.inLevel() && level.getName().equalsIgnoreCase(playerStats.getLevel().getName())) {
                                player.sendMessage(Utils.translate("&cUse the door to reset the level you are already in"));
                                return;
                            }

                            for (PotionEffect potionEffect : player.getActivePotionEffects())
                                player.removePotionEffect(potionEffect.getType());

                            // toggle off if saved
                            Parkour.getStatsManager().toggleOffElytra(playerStats);

                            // save if has checkpoint
                            if (playerStats.getCheckpoint() != null) {
                                CheckpointDB.savePlayerAsync(playerStats);
                                playerStats.resetCheckpoint();
                            }

                            // if in practice mode
                            if (playerStats.getPracticeLocation() != null)
                                playerStats.resetPracticeMode();

                            if (CheckpointDB.hasCheckpoint(player.getUniqueId().toString(), level)) {
                                CheckpointDB.loadPlayer(player.getUniqueId().toString(), level);
                                Parkour.getCheckpointManager().teleportPlayer(playerStats);
                                player.sendMessage(Utils.translate("&eYou have been teleported to your last saved checkpoint"));
                            } else {
                                player.teleport(level.getStartLocation());
                                player.sendMessage(Utils.translate("&7You were teleported to the beginning of "
                                        + level.getFormattedTitle()));
                            }
                            playerStats.setLevel(level);
                            playerStats.disableLevelStartTime();

                            if (!level.getPotionEffects().isEmpty()) {
                                for (PotionEffect potionEffect : level.getPotionEffects())
                                    player.addPotionEffect(potionEffect);
                            }

                            if (level.isElytraLevel())
                                Parkour.getStatsManager().toggleOnElytra(playerStats);

                            TitleAPI.sendTitle(
                                    player, 10, 40, 10,
                                    "",
                                    level.getFormattedTitle()
                            );
                        } else {
                            player.closeInventory();
                            player.sendMessage(Utils.translate("&cYou do not have the required levels for this level"));
                        }
                    } else {
                        player.closeInventory();
                        player.sendMessage(Utils.translate("&cYou cannot enter a level while in infinite parkour"));
                    }
                } else {
                    player.closeInventory();
                    player.sendMessage(Utils.translate("&cYou cannot enter a level while in an event"));
                }
            } else {
                player.closeInventory();
                player.sendMessage(Utils.translate("&cYou cannot enter a level while spectating"));
            }
        } else {
            player.closeInventory();
            player.sendMessage(Utils.translate("&cYou cannot do this while in a race"));
        }
    }

    private static void performTeleportItem(Player player, MenuItem menuItem) {
        Location location = Parkour.getLocationManager().get(menuItem.getTypeValue());

        if (location != null) {
            player.closeInventory();
            player.teleport(location);
        }
    }

    private static void performOpenItem(Player player, MenuItem menuItem) {
        Menu menu = Parkour.getMenuManager().getMenuFromStartingChars(menuItem.getTypeValue());

        if (menu != null) {
            int pageNumber = Utils.getTrailingInt(menuItem.getTypeValue());

            Inventory inventory = Parkour.getMenuManager().getInventory(menu.getName(), pageNumber);

            if (inventory != null) {
                player.closeInventory();
                player.openInventory(inventory);
                Parkour.getMenuManager().updateInventory(player, player.getOpenInventory(), menu.getName(), pageNumber);
            }
        }
    }

    private static void performRankupItem(Player player) {

        double playerBalance = Parkour.getEconomy().getBalance(player);
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        if (playerBalance >= playerStats.getRank().getRankUpPrice()) {
            player.closeInventory();
            // remove amount
            Parkour.getEconomy().withdrawPlayer(player, playerStats.getRank().getRankUpPrice());
            // change to next stage
            RanksDB.updateStage(player.getUniqueId(), 2);
            playerStats.setRankUpStage(2);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 8F, 2F);

            String menuName;
            // open level menu
            if (RanksYAML.isSingleLevelRankup(playerStats.getRank().getRankName()))
                menuName = "single-level-rankup";
            else
                menuName = "double-level-rankup";

            MenuManager menuManager = Parkour.getMenuManager();

            if (menuManager.exists(menuName)) {

                Inventory inventory = menuManager.getInventory(menuName, 1);
                if (inventory != null) {
                    player.closeInventory();
                    player.openInventory(inventory);
                    menuManager.updateInventory(player, player.getOpenInventory(), menuName, 1);
                } else {
                    player.sendMessage(Utils.translate("&cError loading the inventory"));
                }
            }
        } else {
            player.sendMessage(Utils.translate("&cYou do not have enough money for this rankup"));
            player.sendMessage(Utils.translate("  &7You need &4$" +
                    Utils.formatNumber(playerStats.getRank().getRankUpPrice() - playerBalance) + " &7more!"));
            player.closeInventory();
        }
    }
}