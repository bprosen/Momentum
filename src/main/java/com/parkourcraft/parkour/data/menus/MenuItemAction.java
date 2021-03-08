package com.parkourcraft.parkour.data.menus;

import com.connorlinfoot.titleapi.TitleAPI;
import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.Checkpoint_DB;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.data.perks.Perk;
import com.parkourcraft.parkour.data.rank.Rank;
import com.parkourcraft.parkour.data.rank.Ranks_DB;
import com.parkourcraft.parkour.data.rank.Ranks_YAML;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.data.stats.Stats_DB;
import com.parkourcraft.parkour.gameplay.LevelHandler;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public class MenuItemAction {

    private static void runCommands(Player player, List<String> commands, List<String> consoleCommands) {
        for (String command : commands)
            player.performCommand(command.replace("%player%", player.getName()));

        for (String command : consoleCommands)
            Bukkit.dispatchCommand(
                    Parkour.getPlugin().getServer().getConsoleSender(),
                    command.replace("%player%", player.getName())
            );
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
            performOpenItem(player, menuItem);
        else if (itemType.equals("type")) {

            // certain conditions of types for rankup
            String typeValue = menuItem.getTypeValue();
            if (typeValue.equals("coin-rankup"))
                performRankupItem(player);
            else if (typeValue.equals("rankup-level-1")
                    || typeValue.equals("rankup-level-2")
                    || typeValue.equals("rankup-level"))
                performLevelRankUpItem(player, menuItem);
            else if (typeValue.equals("exit"))
                player.closeInventory();
        }

        if (menuItem.hasCommands())
            runCommands(player, menuItem.getCommands(), menuItem.getConsoleCommands());
    }

    private static void performPerkItem(Player player, MenuItem menuItem) {
        Perk perk = Parkour.getPerkManager().get(menuItem.getTypeValue());

        if (perk != null) {
            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            if (menuItem.hasCommands() && perk.hasRequirements(playerStats, player)) {
                player.closeInventory();
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
        String levelName = Ranks_YAML.getRankUpLevel(rankName, levelType);

        if (levelName != null) {
            LevelObject level = Parkour.getLevelManager().get(levelName);
            if (level != null)
                performLevelTeleport(playerStats, player, level);
        }
    }

    private static void performLevelItem(Player player, MenuItem menuItem) {
        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        LevelObject level = Parkour.getLevelManager().get(menuItem.getTypeValue());

        performLevelTeleport(playerStats, player, level);
    }

    private static void performLevelTeleport(PlayerStats playerStats, Player player, LevelObject level) {
        if (!playerStats.inRace()) {
            if (!level.getName().equalsIgnoreCase(playerStats.getLevel())) {
                if (level.hasRequiredLevels(playerStats)) {
                    player.closeInventory();

                    for (PotionEffect potionEffect : player.getActivePotionEffects())
                        player.removePotionEffect(potionEffect.getType());

                    // save if has checkpoint
                    if (playerStats.getCheckpoint() != null) {
                        Checkpoint_DB.savePlayerAsync(player);
                        playerStats.resetCheckpoint();
                    }

                    // if in practice mode
                    if (playerStats.getPracticeLocation() != null)
                        playerStats.resetPracticeMode();

                    playerStats.setLevel(level.getName());

                    if (Checkpoint_DB.hasCheckpoint(player.getUniqueId(), level.getName())) {
                        Checkpoint_DB.loadPlayer(player.getUniqueId(), level.getName());
                        Parkour.getCheckpointManager().teleportPlayer(player);
                        player.sendMessage(Utils.translate("&eYou have been teleported to your last saved checkpoint"));
                    } else {
                        player.teleport(level.getStartLocation());
                        player.sendMessage(Utils.translate("&7You were teleported to the beginning of "
                                + level.getFormattedTitle()));
                    }
                    playerStats.disableLevelStartTime();

                    if (!level.getPotionEffects().isEmpty()) {
                        for (PotionEffect potionEffect : level.getPotionEffects())
                            player.addPotionEffect(potionEffect);
                    }

                    TitleAPI.sendTitle(
                            player, 10, 40, 10,
                            "",
                            level.getFormattedTitle()
                    );
                }
            } else {
                player.closeInventory();
                player.sendMessage(Utils.translate("&cUse the door to reset the level you are already in"));
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
            Ranks_DB.updateStage(player.getUniqueId(), 2);
            playerStats.setRankUpStage(2);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 8F, 2F);

            String menuName;
            // open level menu
            if (Ranks_YAML.isSingleLevelRankup(playerStats.getRank().getRankName()))
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