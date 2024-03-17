package com.renatusnetwork.momentum.data.menus;

import com.connorlinfoot.titleapi.TitleAPI;
import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.api.LevelBuyEvent;
import com.renatusnetwork.momentum.api.ShopBuyEvent;
import com.renatusnetwork.momentum.data.bank.BankManager;
import com.renatusnetwork.momentum.data.bank.items.BankItem;
import com.renatusnetwork.momentum.data.bank.items.BankItemType;
import com.renatusnetwork.momentum.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.levels.LevelManager;
import com.renatusnetwork.momentum.data.levels.LevelPreview;
import com.renatusnetwork.momentum.data.modifiers.ModifierType;
import com.renatusnetwork.momentum.data.modifiers.discounts.Discount;
import com.renatusnetwork.momentum.data.perks.Perk;
import com.renatusnetwork.momentum.data.plots.Plot;
import com.renatusnetwork.momentum.data.plots.PlotsDB;
import com.renatusnetwork.momentum.data.races.gamemode.ChoosingLevel;
import com.renatusnetwork.momentum.data.ranks.Rank;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsDB;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import com.renatusnetwork.momentum.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MenuItemAction {

    private static void runCommands(Player player, List<String> commands, List<String> consoleCommands) {
        PlayerStats playerStats = Momentum.getStatsManager().get(player);

        boolean armorCommand = false;

        // loop through to see if there is a chest setarmor
        if (!commands.isEmpty())
            for (String cmd : commands)
                if (cmd.startsWith("setarmor chest"))
                {
                    armorCommand = true;
                    break;
                }

        // if so, check if they are in elytra level
        if (armorCommand && playerStats != null && playerStats.getLevel() != null && playerStats.getLevel().isElytra()) {
            player.sendMessage(Utils.translate("&cYou cannot change your armor in an Elytra level"));
        } else {

            for (String command : commands)
                player.performCommand(command.replace("%player%", player.getName()));

            for (String command : consoleCommands)
                Bukkit.dispatchCommand(
                        Momentum.getPlugin().getServer().getConsoleSender(),
                        command.replace("%player%", player.getName())
                );
        }
        player.closeInventory();
    }

    public static void perform(Player player, MenuItem menuItem)
    {
        String itemType = menuItem.getType();
        PlayerStats playerStats = Momentum.getStatsManager().get(player);
        boolean choosingRace = Momentum.getRaceManager().containsChoosingRaceLevel(playerStats.getName());

        if (itemType.equals("race"))
        {
            if (choosingRace)
            {
                if (menuItem.getTypeValue().equals("random"))
                    performRandomRaceLevel(playerStats);
                else
                    performRaceItem(playerStats, menuItem.getTypeValue());
            }
            else
            {
                player.sendMessage(Utils.translate("&cYou do not have a pending race request"));
                player.closeInventory();
            }
        }
        else if (itemType.equals("perk"))
            performPerkItem(player, menuItem);
        else if (itemType.equals("level"))
        {
            if (menuItem.getTypeValue().equals("featured"))
            {
                Level level = Momentum.getLevelManager().getFeaturedLevel();
                performLevelTeleport(playerStats, level);
            }
            else if (menuItem.getTypeValue().equals("rankup"))
            {
                // only allow this if they are not at the last rank!
                if (!playerStats.isLastRank())
                    performLevelTeleport(playerStats, playerStats.getRank().getRankupLevel());
            }
            else if (menuItem.getTypeValue().equals("random"))
                performRandomLevel(playerStats);
            else if (menuItem.getTypeValue().startsWith("favorite-level"))
            {
                int index = Integer.parseInt(menuItem.getTypeValue().split("favorite-level-")[1]) - 1;
                Level favoriteLevel = playerStats.getFavoriteLevel(index);

                if (favoriteLevel != null)
                    performLevelTeleport(playerStats, favoriteLevel);
            }
            else
                performLevelItem(player, menuItem);
        }
        else if (itemType.equals("teleport"))
            performTeleportItem(playerStats, menuItem);
        else if (itemType.equals("bank"))
            performBankItem(playerStats, menuItem);
        else if (menuItem.getOpenMenu() != null) // replacement for type open, since we define page numbers
            performOpenItem(playerStats, menuItem);
        else if (itemType.equals("rate"))
            performLevelRate(playerStats, menuItem);
        else if (itemType.equals("infinite-mode"))
            performInfiniteModeChange(playerStats, menuItem);
        else if (itemType.equals("type"))
        {
            // certain conditions
            String typeValue = menuItem.getTypeValue();
            if (typeValue.equals("submit-plot"))
                performPlotSubmission(player);
            else if (typeValue.equals("clearhat") || typeValue.equals("cleararmor") ||
                     typeValue.equals("cleartrail") || typeValue.equals("clearnick") || typeValue.equals("clearinfinite"))
                performCosmeticsClear(player, typeValue, menuItem);
            else if (typeValue.equals("level-sorting") && !Momentum.getLevelManager().isBuyingLevelMenu(player.getName())) // do not allow switching sorting if buying
                performLevelSort(playerStats, menuItem.getPage());
            else if (typeValue.equals("exit"))
                player.closeInventory();
        } else if (menuItem.hasCommands())
            runCommands(player, menuItem.getCommands(), menuItem.getConsoleCommands());
    }

    private static void performRaceItem(PlayerStats playerStats, String levelName)
    {
        Level level = Momentum.getLevelManager().get(levelName);

        if (level.isRaceLevel())
        {
            ChoosingLevel choosingLevel = Momentum.getRaceManager().getChoosingLevelData(playerStats.getName());
            Momentum.getRaceManager().sendRequest(choosingLevel.getSender(), choosingLevel.getRequested(), level, choosingLevel.getBet());
            playerStats.getPlayer().closeInventory();
        }
    }

    private static void performLevelSort(PlayerStats playerStats, MenuPage menuPage)
    {
        Momentum.getStatsManager().updateMenuSortLevelsType(playerStats, LevelSortingType.getNext(playerStats.getLevelSortingType()));
        Momentum.getMenuManager().updateInventory(playerStats, playerStats.getPlayer().getOpenInventory(), menuPage);
    }

    private static void performInfiniteModeChange(PlayerStats playerStats, MenuItem menuItem)
    {
        String formatted = StringUtils.capitalize(menuItem.getTypeValue().toLowerCase());
        Player player = playerStats.getPlayer();

        if (!playerStats.getInfiniteType().toString().equalsIgnoreCase(formatted))
        {
            Momentum.getInfiniteManager().changeType(playerStats, InfiniteType.valueOf(formatted.toUpperCase()));
            player.sendMessage(Utils.translate("&7You changed your &5Infinite &7mode to &d" + formatted));
        }
        else
            player.sendMessage(Utils.translate("&7You are already in the &5Infinite &7mode &d" + formatted));

        player.closeInventory();
    }

    private static void performBankItem(PlayerStats playerStats, MenuItem menuItem)
    {
        String typeValue = menuItem.getTypeValue();

        if (!typeValue.endsWith("_total"))
        {
            BankItem item = Momentum.getBankManager().getItem(BankItemType.valueOf(menuItem.getTypeValue().toUpperCase()));
            Momentum.getBankManager().bid(playerStats, item);

            playerStats.getPlayer().closeInventory();
        }
    }

    private static void performRandomRaceLevel(PlayerStats playerStats)
    {
        List<Level> raceLevels = Momentum.getLevelManager().getRaceLevels();
        ArrayList<Level> chosenLevels = new ArrayList<>();

        for (Level level : raceLevels)
        {
            // cover all conditions that can stop a player from entering a level
            if (!level.isFeaturedLevel() && !level.isRankUpLevel() && playerStats.hasAccessTo(level) && level.isRaceLevel() &&
                !(playerStats.inLevel() && playerStats.getLevel().getName().equalsIgnoreCase(level.getName())))
                chosenLevels.add(level);
        }
        // tp them to randomly chosen level
        Level chosenLevel = chosenLevels.get(ThreadLocalRandom.current().nextInt(chosenLevels.size()));
        ChoosingLevel choosingLevel = Momentum.getRaceManager().getChoosingLevelData(playerStats.getName());

        Momentum.getRaceManager().sendRequest(choosingLevel.getSender(), choosingLevel.getRequested(), chosenLevel, choosingLevel.getBet());

        playerStats.getPlayer().closeInventory();
    }

    private static void performRandomLevel(PlayerStats playerStats)
    {
        Set<Level> menuLevels = Momentum.getLevelManager().getLevelsInAllMenus();
        ArrayList<Level> chosenLevels = new ArrayList<>();

        for (Level level : menuLevels)
            // cover all conditions that can stop a player from entering a level
            if (playerStats.hasAccessTo(level))
                chosenLevels.add(level);

        // tp them to randomly chosen level
        Level chosenLevel = chosenLevels.get(ThreadLocalRandom.current().nextInt(chosenLevels.size()));
        performLevelTeleport(playerStats, chosenLevel); // enforce no shift clicking
    }

    private static void performCosmeticsClear(Player player, String clearType, MenuItem menuItem) {

        switch (clearType) {
            case "cleararmor":
                player.getInventory().setChestplate(new ItemStack(Material.AIR));
                player.getInventory().setLeggings(new ItemStack(Material.AIR));
                player.getInventory().setBoots(new ItemStack(Material.AIR));

                player.closeInventory();
                player.sendMessage(Utils.translate("&cYou have cleared your current armor"));
                break;
            case "clearhat":
                player.getInventory().setHelmet(new ItemStack(Material.AIR));

                player.closeInventory();
                player.sendMessage(Utils.translate("&cYou have cleared your current hat"));
                break;
            case "clearnick":
                player.closeInventory();
                player.sendMessage(Utils.translate("&cYou have cleared your current nick"));

                // run clear cmds
                if (menuItem.hasCommands())
                    runCommands(player, menuItem.getCommands(), menuItem.getConsoleCommands());
                break;
            case "cleartrail":
                player.closeInventory();
                player.sendMessage(Utils.translate("&cYou have cleared your current trail"));

                // run clear cmds
                if (menuItem.hasCommands())
                    runCommands(player, menuItem.getCommands(), menuItem.getConsoleCommands());
                break;
            case "clearinfinite":
                player.closeInventory();
                player.sendMessage(Utils.translate("&cYou have cleared your current infinite block"));

                Momentum.getStatsManager().resetInfiniteBlock(Momentum.getStatsManager().get(player));
                break;
        }
    }

    private static void performLevelRate(PlayerStats playerStats, MenuItem menuItem)
    {
        Player player = playerStats.getPlayer();
        Level level = Momentum.getMenuManager().getChoosingRating(playerStats);

        player.closeInventory();

        if (level != null)
        {
            int rating = Integer.parseInt(menuItem.getTypeValue());

            if (rating >= 0 && rating <= 5)
            {
                if (level.hasRated(player.getName()))
                {
                    Momentum.getLevelManager().updateRating(player, level, rating);
                    player.sendMessage(Utils.translate("&7You updated your rating for &c" + level.getTitle() + "&7 to a &6" + rating + "&7! Thank you for rating!"));
                }
                else
                {
                    Momentum.getLevelManager().addRating(player, level, rating);
                    player.sendMessage(Utils.translate("&7You rated &c" + level.getTitle() + "&7 a &6" + rating + "&7! Thank you for rating!"));
                }
            }
            else
                player.sendMessage(Utils.translate("&cYour rating has to be anywhere from 0 to 5!"));
        }
        else
            player.sendMessage(Utils.translate("&cSomething went wrong with level, does it exist?"));
    }

    private static void performPlotSubmission(Player player)
    {
        player.closeInventory();

        Plot plot = Momentum.getPlotsManager().get(player.getName());

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
        Perk perk = Momentum.getPerkManager().get(menuItem.getTypeValue());

        if (perk != null)
        {
            PlayerStats playerStats = Momentum.getStatsManager().get(player);

            // bypass if have access (opped too)
            if (perk.hasAccess(playerStats))
            {
                player.closeInventory();
                Momentum.getPerkManager().setPerk(perk, playerStats);
                runPerkCommands(perk, playerStats);

            }
            else if (perk.requiresBuying())
            {
                ShopBuyEvent event = new ShopBuyEvent(playerStats, perk);
                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                {
                    int playerBalance = playerStats.getCoins();
                    int price = event.getPrice();

                    if (playerStats.hasModifier(ModifierType.SHOP_DISCOUNT))
                    {
                        Discount discount = (Discount) playerStats.getModifier(ModifierType.SHOP_DISCOUNT);
                        price *= (1.00f - discount.getDiscount());
                    }

                    if (playerBalance >= price)
                    {
                        Momentum.getStatsManager().removeCoins(playerStats, price);
                        Momentum.getPerkManager().bought(playerStats, perk);
                        Momentum.getMenuManager().updateInventory(playerStats, player.getOpenInventory(), menuItem.getPage());
                        runPerkCommands(perk, playerStats);
                    }
                }
            }
        }
    }

    private static void runPerkCommands(Perk perk, PlayerStats playerStats)
    {
        HashSet<String> commands = perk.getCommands();

        for (String command : commands)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", playerStats.getName()));
    }

    private static void performLevelItem(Player player, MenuItem menuItem)
    {
        PlayerStats playerStats = Momentum.getStatsManager().get(player);
        Level level = Momentum.getLevelManager().get(menuItem.getTypeValue());
        BankManager bankManager = Momentum.getBankManager();

        if (level != null)
        {
            // go through price buying if not featured, non null item, has price and has not bought level, or not the jackpot level
            if (level.requiresBuying() &&
                !level.isFeaturedLevel() &&
                !(bankManager.isJackpotRunning() && bankManager.getJackpot().getLevelName().equalsIgnoreCase(level.getName())) &&
                !playerStats.hasBoughtLevel(level) && !playerStats.hasCompleted(level))
            {
                if (Momentum.getMenuManager().containsShiftClicked(playerStats))
                    performLevelPreview(playerStats, level);
                else
                    performLevelBuying(playerStats, level, menuItem);
            }
            else
                performLevelTeleport(playerStats, level);
        }
    }

    public static void performLevelPreview(PlayerStats playerStats, Level level)
    {
        Player player = playerStats.getPlayer();
        player.closeInventory();

        if (nonLevelTeleportConditions(playerStats))
        {
            if (playerStats.inPracticeMode())
                Momentum.getStatsManager().resetPracticeMode(playerStats, false);

            // add preview and teleport
            LevelPreview levelPreview = new LevelPreview(playerStats, level, player.getLocation());
            playerStats.setPreviewLevel(levelPreview);
            levelPreview.teleport();

            player.sendMessage(Utils.translate(
                    "&7You are now previewing &c" + level.getTitle() + "&7, you can only move in a &6" + ((int) Momentum.getSettingsManager().preview_max_distance) + "&7 block radius"
            ));
            player.sendMessage(Utils.translate("&7You can leave at any point by typing &6/spawn&7, &6/preview leave &7or use the &cLeave &7item"));
        }
    }

    public static void performLevelBuying(PlayerStats playerStats, Level level, MenuItem menuItem)
    {
        MenuManager menuManager = Momentum.getMenuManager();
        LevelManager levelManager = Momentum.getLevelManager();
        Player player = playerStats.getPlayer();

        if (!levelManager.isBuyingLevel(player.getName(), level))
        {
            int coins = playerStats.getCoins();
            int total = levelManager.getTotalBuyingLevelsCost(player.getName());
            int price = level.getPrice();

            int oldTotal = total;
            int oldPrice = price;

            if (playerStats.hasModifier(ModifierType.LEVEL_DISCOUNT))
            {
                Discount discount = (Discount) playerStats.getModifier(ModifierType.LEVEL_DISCOUNT);
                total *= (1.00f - discount.getDiscount());
                price *= (1.00f - discount.getDiscount());
            }

            Inventory openInventory = player.getOpenInventory().getTopInventory();

            if (coins >= total + price)
            {
                ItemStack itemStack = new ItemStack(Material.STAINED_CLAY, 1, (short) 5);
                ItemMeta itemMeta = itemStack.getItemMeta();

                itemMeta.setDisplayName(Utils.translate(
                        "&cClick to confirm &a" + level.getTitle() + "&c for " + Utils.getCoinFormat(oldPrice, price) + " &eCoins"
                ));

                List<String> loreString = new ArrayList<>();
                loreString.add(Utils.translate(" &7This will also confirm all other selected purchases"));
                loreString.add(Utils.translate(" &7For a total of " + Utils.getCoinFormat(oldTotal + oldPrice, total + price) + " &eCoins"));

                List<Integer> slots = levelManager.getBuyingLevelsSlots(player.getName());

                Inventory newInventory = menuManager.createInventory(menuItem.getPage(), openInventory.getSize(), openInventory.getTitle());
                newInventory.setContents(openInventory.getContents()); // set contents

                // update current bought lore
                for (int slot : slots)
                {
                    ItemStack boughtItem = openInventory.getItem(slot);
                    ItemMeta boughtMeta = boughtItem.getItemMeta();

                    boughtMeta.setLore(loreString);

                    boughtItem.setItemMeta(boughtMeta);

                    newInventory.setItem(slot, boughtItem);
                }

                itemMeta.setLore(loreString);

                itemStack.setItemMeta(itemMeta);
                newInventory.setItem(menuItem.getSlot(), itemStack);

                player.openInventory(newInventory); // open new inv

                levelManager.addBuyingLevel(player.getName(), level, menuItem.getSlot());

                // set last edited
                CancelTasks cancelTask = menuManager.getCancelTasks(player.getName());
                if (cancelTask != null)
                    cancelTask.setLastEditedInventory(newInventory);
            }
            else
            {
                boolean alreadyExists = menuManager.hasCancelledItem(player.getName(), menuItem.getSlot());

                if (!alreadyExists)
                {
                    // this is where it creates a item telling them they cannot buy this!
                    ItemStack itemStack = new ItemStack(Material.STAINED_CLAY, 1, (short) 14);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(Utils.translate("&cNot enough coins to buy " + level.getTitle()));

                    int remaining = (int) ((total + price) - coins);

                    itemMeta.setLore(new ArrayList<String>() {{
                        add(Utils.translate(" &7You need &6" + Utils.formatNumber(remaining) + " &7more &eCoins"));
                    }});

                    itemStack.setItemMeta(itemMeta);

                    ItemStack preCancelItem = openInventory.getItem(menuItem.getSlot());

                    Inventory newInventory = menuManager.createInventory(menuItem.getPage(), openInventory.getSize(), openInventory.getTitle());
                    newInventory.setContents(openInventory.getContents()); // set contents
                    newInventory.setItem(menuItem.getSlot(), itemStack);

                    player.openInventory(newInventory);

                    menuManager.addActiveCancel(player.getName(), newInventory, menuItem.getSlot(), preCancelItem,
                            // reset item after 5 seconds
                            new BukkitRunnable()
                            {
                                @Override
                                public void run()
                                {
                                    if (player != null && menuManager.hasCancelTasks(player.getName()) && player.getOpenInventory() != null &&
                                        player.getOpenInventory().getTopInventory().getName().equalsIgnoreCase(openInventory.getName()))
                                    {
                                        CancelTasks cancelled = menuManager.getCancelTasks(player.getName());

                                        // if any left, cancel and remove the rest
                                        if (cancelled != null)
                                        {
                                            Inventory lastEditedInventory = cancelled.getLastEditedInventory();

                                            Inventory revertInventory = menuManager.createInventory(menuItem.getPage(), lastEditedInventory.getSize(), lastEditedInventory.getTitle());
                                            revertInventory.setContents(lastEditedInventory.getContents()); // set contents

                                            // cancel tasks
                                            for (BukkitTask task : cancelled.getCancelledSlots())
                                                task.cancel();

                                            // restore inventory
                                            for (Map.Entry<Integer, ItemStack> entry : cancelled.getBeforeCancelItems().entrySet())
                                                revertInventory.setItem(entry.getKey(), entry.getValue());

                                            // open reverted inventory
                                            player.openInventory(revertInventory);
                                        }
                                    }
                                    menuManager.removeCancelTasks(player.getName());
                                }
                            }.runTaskLater(Momentum.getPlugin(), 20 * 5)
                    );
                }
            }
        }
        else
        {
            // where the levels are bought
            Collection<Level> levels = levelManager.getBuyingLevels(player.getName()).values();
            int totalCoins = levelManager.getTotalBuyingLevelsCost(player.getName());

            LevelBuyEvent event = new LevelBuyEvent(playerStats, levels, totalCoins);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled())
            {
                totalCoins = event.getTotalPrice();

                // modifier discount
                if (playerStats.hasModifier(ModifierType.LEVEL_DISCOUNT))
                {
                    Discount discount = (Discount) playerStats.getModifier(ModifierType.LEVEL_DISCOUNT);
                    totalCoins *= (1.00f - discount.getDiscount());
                }

                // add to db/cache
                for (Level boughtLevels : levels)
                {
                    StatsDB.addBoughtLevel(playerStats.getUUID(), boughtLevels.getName());
                    playerStats.buyLevel(boughtLevels);
                }
                Momentum.getStatsManager().removeCoins(playerStats, totalCoins); // remove all coins

                // do not teleport if bought more than one!
                if (levels.size() > 1)
                    // update and open inventory
                    menuManager.openInventory(playerStats, player, menuItem.getMenu().getName(), menuItem.getPage().getPageNumber(), false);
                else
                {
                    // teleport if only one
                    performLevelTeleport(playerStats, level);
                }
            }
        }
    }

    public static void performLevelTeleport(PlayerStats playerStats, Level level)
    {
        Player player = playerStats.getPlayer();
        StatsManager statsManager = Momentum.getStatsManager();
        player.closeInventory();

        if (nonLevelTeleportConditions(playerStats))
        {
            if (level.playerHasRequiredLevels(playerStats))
            {
                // if the level has perm node, and player does not have perm node
                if (level.hasPermissionNode() && !player.hasPermission(level.getRequiredPermission()))
                {
                    player.sendMessage(Utils.translate("&cYou do not have permission to enter this level"));
                    return;
                }

                // if player is in level and their level is the level they clicked on, cancel
                if (playerStats.inLevel() && level.equals(playerStats.getLevel()))
                {
                    player.sendMessage(Utils.translate("&cUse the door to reset the level you are already in"));
                    return;
                }

                if (level.needsRank()) {
                    Rank rank = Momentum.getRanksManager().get(level.getRequiredRank());

                    if (!Momentum.getRanksManager().isPastOrAtRank(playerStats, rank)) {
                        player.sendMessage(Utils.translate("&cYou need to be rank " + rank.getTitle() + " &cto play this level"));
                        return;
                    }
                }

                if (playerStats.inLevel() && playerStats.hasAutoSave() && !playerStats.getPlayer().isOnGround())
                {
                    player.sendMessage(Utils.translate("&cYou cannot leave the level while in midair with auto-save enabled"));
                    return;
                }

                // perform leave level steps
                statsManager.leaveLevelAndReset(playerStats, true);

                Rank rank = playerStats.getRank();
                if (rank != null) {
                    Level rankupLevel = rank.getRankupLevel();

                    // this is a case where if they click the rankup button, OR click the level from replayable that WOULD be their rankup level, make them enter rankup
                    if (level.isRankUpLevel() && rankupLevel != null && rankupLevel.getName().equalsIgnoreCase(level.getName()))
                        statsManager.enteredRankup(playerStats);
                }

                if (Momentum.getMenuManager().containsShiftClicked(playerStats) &&
                        level.hasMastery() && playerStats.hasCompleted(level) &&
                        !playerStats.hasMasteryCompletion(level))
                    statsManager.enteredMastery(playerStats);

                boolean tpToStart = false;

                if (!playerStats.isAttemptingMastery()) {
                    Location save = playerStats.getSave(level);
                    Location spawn = playerStats.getCheckpoint(level);

                    if (spawn != null) {
                        playerStats.setCurrentCheckpoint(spawn);

                        // only tp if dont have a save
                        if (save == null)
                        {
                            Momentum.getCheckpointManager().teleportToCheckpoint(playerStats);
                            player.sendMessage(Utils.translate("&eYou have been teleported to your last saved checkpoint"));
                        }
                        // tp to start if no save
                    } else if (save == null)
                        tpToStart = true;

                    // if they have a save and are not attempting mastery
                    if (save != null)
                    {
                        Momentum.getSavesManager().teleportAndRemoveSave(playerStats, level, save);
                        player.sendMessage(Utils.translate("&7You have been teleport to your save for &c" + level.getTitle()));
                    }
                } else
                    tpToStart = true;

                if (tpToStart) {
                    player.teleport(level.getStartLocation());

                    player.sendMessage(Utils.translate("&7You were teleported to the start of " + level.getTitle()));

                    if (playerStats.isAttemptingMastery()) {
                        player.sendMessage(Utils.translate("&7This is a &5&lMastery &7attempt, you will get &e" + level.getMasteryMultiplier() + "x &7more &eCoins &7for completing"));
                        player.sendMessage(Utils.translate("&c&nCheckpoints and /practice is disabled!"));
                    }
                }

                playerStats.setLevel(level);
                playerStats.disableLevelStartTime();
                playerStats.resetFails();

                for (PotionEffect potionEffect : level.getPotionEffects())
                    player.addPotionEffect(potionEffect);

                if (level.isElytra())
                    Momentum.getStatsManager().toggleOnElytra(playerStats);

                TitleAPI.sendTitle(
                        player, 10, 40, 10,
                        "",
                        level.getFormattedTitle()
                );
            }
            else
                player.sendMessage(Utils.translate("&cYou do not have the required levels for this level"));
        }
    }

    private static boolean nonLevelTeleportConditions(PlayerStats playerStats)
    {
        Player player = playerStats.getPlayer();

        if (!playerStats.inRace())
        {
            if (!playerStats.isSpectating())
            {
                if (!playerStats.isEventParticipant())
                {
                    if (!playerStats.isInInfinite())
                    {
                        if (!playerStats.isInBlackMarket())
                            return true;
                        else
                            player.sendMessage(Utils.translate("&cYou cannot do this while in the Black Market"));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou cannot enter a level while in infinite parkour"));
                }
                else
                    player.sendMessage(Utils.translate("&cYou cannot enter a level while in an event"));
            }
            else
                player.sendMessage(Utils.translate("&cYou cannot enter a level while spectating"));
        }
        else
            player.sendMessage(Utils.translate("&cYou cannot do this while in a race"));

        return false;
    }

    private static void performTeleportItem(PlayerStats playerStats, MenuItem menuItem)
    {
        Location location = Momentum.getLocationManager().get(menuItem.getTypeValue());

        // null check
        if (location != null) {
            // region check
            ProtectedRegion region = WorldGuard.getRegion(location);
            if (region != null) {

                Level level = Momentum.getLevelManager().get(region.getId());

                // make sure the area they are spawning in is a level
                if (level != null)
                    performLevelTeleport(playerStats, level);
            }
        }
    }

    private static void performOpenItem(PlayerStats playerStats, MenuItem menuItem)
    {
        MenuPage menuPage = menuItem.getOpenMenu();

        if (menuPage != null)
            Momentum.getMenuManager().openInventory(playerStats, playerStats.getPlayer(), menuPage.getMenu().getName(), menuPage.getPageNumber(), false);
    }
}