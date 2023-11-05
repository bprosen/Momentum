package com.renatusnetwork.parkour.data.menus;

import com.connorlinfoot.titleapi.TitleAPI;
import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.api.LevelBuyEvent;
import com.renatusnetwork.parkour.api.ShopBuyEvent;
import com.renatusnetwork.parkour.data.bank.BankManager;
import com.renatusnetwork.parkour.data.bank.types.BankItemType;
import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.LevelManager;
import com.renatusnetwork.parkour.data.levels.RatingDB;
import com.renatusnetwork.parkour.data.modifiers.ModifierTypes;
import com.renatusnetwork.parkour.data.modifiers.discounts.Discount;
import com.renatusnetwork.parkour.data.perks.Perk;
import com.renatusnetwork.parkour.data.plots.Plot;
import com.renatusnetwork.parkour.data.plots.PlotsDB;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.gameplay.handlers.PracticeHandler;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
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
        } else {

            for (String command : commands)
                player.performCommand(command.replace("%player%", player.getName()));

            for (String command : consoleCommands)
                Bukkit.dispatchCommand(
                        Parkour.getPlugin().getServer().getConsoleSender(),
                        command.replace("%player%", player.getName())
                );
        }
        player.closeInventory();
    }

    public static void perform(Player player, MenuItem menuItem) {
        String itemType = menuItem.getType();
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        if (itemType.equals("perk"))
            performPerkItem(player, menuItem);
        else if (itemType.equals("level"))
        {
            if (menuItem.getTypeValue().equals("featured"))
                performLevelTeleport(playerStats, player, Parkour.getLevelManager().getFeaturedLevel());
            else if (menuItem.getTypeValue().equals("rankup"))
            {
                // only allow this if they are not at the last rank!
                if (!playerStats.isLastRank())
                    performLevelTeleport(playerStats, player, playerStats.getRank().getRankupLevel());
            }
            else if (menuItem.getTypeValue().equals("random"))
                performRandomLevel(playerStats);
            else
                performLevelItem(player, menuItem);
        }
        else if (itemType.equals("teleport"))
            performTeleportItem(player, menuItem);
        else if (itemType.equals("bank"))
            performBankItem(playerStats, menuItem);
        else if (itemType.equals("open"))
            performOpenItem(player, menuItem);
        else if (itemType.equals("rate"))
            performLevelRate(player, menuItem);
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
            else if (typeValue.equals("exit"))
                player.closeInventory();
        } else if (menuItem.hasCommands())
            runCommands(player, menuItem.getCommands(), menuItem.getConsoleCommands());
    }

    private static void performInfiniteModeChange(PlayerStats playerStats, MenuItem menuItem)
    {
        String formatted = StringUtils.capitalize(menuItem.getTypeValue().toLowerCase());
        Player player = playerStats.getPlayer();

        if (!playerStats.getInfiniteType().toString().equalsIgnoreCase(formatted))
        {
            Parkour.getInfiniteManager().changeType(playerStats, InfiniteType.valueOf(formatted.toUpperCase()));
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
            BankItemType bankItemType = BankItemType.valueOf(menuItem.getTypeValue().toUpperCase());
            Parkour.getBankManager().bid(playerStats, bankItemType);

            playerStats.getPlayer().closeInventory();
        }
    }

    private static void performRandomLevel(PlayerStats playerStats)
    {
        Set<Level> menuLevels = Parkour.getLevelManager().getLevelsInAllMenus();
        ArrayList<Level> chosenLevels = new ArrayList<>();

        for (Level level : menuLevels)
        {
            boolean addLevel = true;

            // cover all conditions that can stop a player from entering a level
            if (((level.getPrice() > 0 && !playerStats.hasBoughtLevel(level.getName())) ||
                (!level.getRequiredLevels().isEmpty() && !level.hasRequiredLevels(playerStats)) ||
                (level.hasPermissionNode() && !playerStats.getPlayer().hasPermission(level.getRequiredPermissionNode())) ||
                (level.needsRank() && !Parkour.getRanksManager().isPastOrAtRank(playerStats, level.getRequiredRank()))) &&
                !level.isFeaturedLevel() && !level.isRankUpLevel())
                addLevel = false;

            // make sure we do not add the level they are already in
            if (addLevel && !(playerStats.inLevel() && playerStats.getLevel().getName().equalsIgnoreCase(level.getName())))
                chosenLevels.add(level);
        }

        // tp them to randomly chosen level
        Level chosenLevel = chosenLevels.get(ThreadLocalRandom.current().nextInt(chosenLevels.size()));
        performLevelTeleport(playerStats, playerStats.getPlayer(), chosenLevel);
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

                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                playerStats.setInfiniteBlock(Material.QUARTZ_BLOCK);

                Parkour.getDatabaseManager().runAsyncQuery("UPDATE players SET infinite_block='' WHERE uuid='" + playerStats.getUUID() + "'");
                break;
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
            if (perk.hasRequiredPermissions(player) || perk.hasRequirements(playerStats, player))
            {
                player.closeInventory();
                Parkour.getPerkManager().setPerk(perk, playerStats);

                // if has commands, run them
                if (menuItem.hasCommands())
                    runCommands(player, menuItem.getCommands(), menuItem.getConsoleCommands());

            }
            else if (!playerStats.hasPerk(perk.getName()) && perk.getPrice() > 0)
            {
                ShopBuyEvent event = new ShopBuyEvent(playerStats, perk);
                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                {
                    int playerBalance = (int) playerStats.getCoins();
                    int price = event.getPrice();

                    if (playerStats.hasModifier(ModifierTypes.SHOP_DISCOUNT))
                    {
                        Discount discount = (Discount) playerStats.getModifier(ModifierTypes.SHOP_DISCOUNT);
                        price *= (1.00f - discount.getDiscount());
                    }

                    if (playerBalance >= price)
                    {
                        Parkour.getStatsManager().removeCoins(playerStats, price);
                        Parkour.getPerkManager().bought(playerStats, perk);
                        Parkour.getMenuManager().updateInventory(player, player.getOpenInventory());

                        // if has commands, run them
                        if (menuItem.hasCommands())
                            runCommands(player, menuItem.getCommands(), menuItem.getConsoleCommands());
                    }
                }
            }
        }
    }

    private static void performLevelItem(Player player, MenuItem menuItem) {
        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        Level level = Parkour.getLevelManager().get(menuItem.getTypeValue());
        BankManager bankManager = Parkour.getBankManager();

        if (level != null)
        {
            // go through price buying if not featured, non null item, has price and has not bought level, or not the jackpot level
            if (menuItem != null && level.getPrice() > 0 &&
                !level.isFeaturedLevel() &&
                !(bankManager.isJackpotRunning() && bankManager.getJackpot().getLevelName().equalsIgnoreCase(level.getName()) &&
                !playerStats.hasBoughtLevel(level.getName()) && playerStats.getLevelCompletionsCount(level.getName()) <= 0))
                performLevelBuying(playerStats, player, level, menuItem);
            else
                performLevelTeleport(playerStats, player, level);
        }
    }

    public static void performLevelBuying(PlayerStats playerStats, Player player, Level level, MenuItem menuItem)
    {
        MenuManager menuManager = Parkour.getMenuManager();
        String menuName = menuItem.getMenuName();
        LevelManager levelManager = Parkour.getLevelManager();

        if (!levelManager.isBuyingLevel(player.getName(), level))
        {
            double coins = playerStats.getCoins();
            int total = levelManager.getTotalBuyingLevelsCost(player.getName());
            int price = level.getPrice();

            int oldTotal = total;
            int oldPrice = price;

            if (playerStats.hasModifier(ModifierTypes.LEVEL_DISCOUNT))
            {
                Discount discount = (Discount) playerStats.getModifier(ModifierTypes.LEVEL_DISCOUNT);
                total *= (1.00f - discount.getDiscount());
                price *= (1.00f - discount.getDiscount());
            }

            Inventory openInventory = player.getOpenInventory().getTopInventory();

            if (coins >= total + price)
            {
                ItemStack itemStack = new ItemStack(Material.STAINED_CLAY, 1, (short) 5);
                ItemMeta itemMeta = itemStack.getItemMeta();

                itemMeta.setDisplayName(Utils.translate(
                        "&cClick to confirm &a" + level.getFormattedTitle() + " &cfor " + Utils.getCoinFormat(oldPrice, price) + " &eCoins"
                ));

                List<String> loreString = new ArrayList<>();
                loreString.add(Utils.translate(" &7This will also confirm all other selected purchases"));
                loreString.add(Utils.translate(" &7For a total of " + Utils.getCoinFormat(oldTotal + oldPrice, total + price) + " &eCoins"));

                List<Integer> slots = levelManager.getBuyingLevelsSlots(player.getName());

                Inventory newInventory = Bukkit.createInventory(null, openInventory.getSize(), openInventory.getTitle());
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
                    itemMeta.setDisplayName(Utils.translate("&cNot enough coins to buy " + level.getFormattedTitle()));

                    int remaining = (int) ((total + price) - coins);

                    itemMeta.setLore(new ArrayList<String>() {{
                        add(Utils.translate(" &7You need &6" + Utils.formatNumber(remaining) + " &7more &eCoins"));
                    }});

                    itemStack.setItemMeta(itemMeta);

                    ItemStack preCancelItem = openInventory.getItem(menuItem.getSlot());

                    Inventory newInventory = Bukkit.createInventory(null, openInventory.getSize(), openInventory.getTitle());
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

                                            Inventory revertInventory = Bukkit.createInventory(null, lastEditedInventory.getSize(), lastEditedInventory.getTitle());
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
                            }.runTaskLater(Parkour.getPlugin(), 20 * 5)
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
                if (playerStats.hasModifier(ModifierTypes.LEVEL_DISCOUNT))
                {
                    Discount discount = (Discount) playerStats.getModifier(ModifierTypes.LEVEL_DISCOUNT);
                    totalCoins *= (1.00f - discount.getDiscount());
                }

                // add to db/cache
                for (Level boughtLevels : levels)
                {
                    StatsDB.addBoughtLevel(playerStats, boughtLevels.getName());
                    playerStats.buyLevel(boughtLevels.getName());
                }
                Parkour.getStatsManager().removeCoins(playerStats, totalCoins); // remove all coins

                // do not teleport if bought more than one!
                if (levels.size() > 1)
                {
                    // update and open inventory
                    Inventory inventory = Parkour.getMenuManager().getInventory(menuName, menuItem.getPageNumber());
                    player.openInventory(inventory);
                    Parkour.getMenuManager().updateInventory(player, player.getOpenInventory(), menuName, menuItem.getPageNumber());
                }
                else
                {
                    // teleport if only one
                    performLevelTeleport(playerStats, player, level);
                }
            }
        }
    }

    public static void performLevelTeleport(PlayerStats playerStats, Player player, Level level) {
        if (!playerStats.inRace())
        {
            if (!playerStats.isSpectating())
            {
                if (!playerStats.isEventParticipant())
                {
                    if (!playerStats.isInInfinite())
                    {
                        if (level.hasRequiredLevels(playerStats))
                        {
                            if (!playerStats.isInBlackMarket())
                            {

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

                                if (level.needsRank()) {
                                    Rank rank = level.getRequiredRank();

                                    if (!Parkour.getRanksManager().isPastOrAtRank(playerStats, rank))
                                    {
                                        player.sendMessage(Utils.translate("&cYou need to be rank " + rank.getRankTitle() + " &cto play this level"));
                                        return;
                                    }
                                }

                                playerStats.clearPotionEffects();

                                // toggle off if saved
                                Parkour.getStatsManager().toggleOffElytra(playerStats);

                                playerStats.resetCurrentCheckpoint(); // reset

                                // if in practice mode
                                PracticeHandler.resetDataOnly(playerStats);

                                // if currently attempting, reset
                                if (playerStats.isAttemptingRankup())
                                    Parkour.getRanksManager().leftRankup(playerStats);

                                Level rankupLevel = playerStats.getRank().getRankupLevel();
                                // this is a case where if they click the rankup button, OR click the level from replayable that WOULD be their rankup level, make them enter rankup
                                if (level.isRankUpLevel() && rankupLevel != null && rankupLevel.getName().equalsIgnoreCase(level.getName()))
                                    Parkour.getRanksManager().enteredRankup(playerStats);

                                Location save = playerStats.getSave(level.getName());
                                Location spawn = playerStats.getCheckpoint(level.getName());
                                if (spawn != null) {
                                    playerStats.setCurrentCheckpoint(spawn);

                                    // only tp if dont have a save
                                    if (save == null) {
                                        Parkour.getCheckpointManager().teleportToCP(playerStats);
                                        player.sendMessage(Utils.translate("&eYou have been teleported to your last saved checkpoint"));
                                    }
                                    // tp to start if no save
                                } else if (save == null) {
                                    player.teleport(level.getStartLocation());
                                    player.sendMessage(Utils.translate("&7You were teleported to the beginning of "
                                            + level.getFormattedTitle()));
                                }

                                // if they have a save
                                if (save != null) {
                                    Parkour.getSavesManager().loadSave(playerStats, save, level);
                                    player.sendMessage(Utils.translate("&7You have been teleport to your save for &c" + level.getFormattedTitle()));
                                    player.sendMessage(Utils.translate("&7Your save has been deleted, use &a/save &7again to save your location"));
                                }

                                playerStats.setLevel(level);
                                playerStats.disableLevelStartTime();
                                playerStats.resetFails();

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
                                player.sendMessage(Utils.translate("&cYou cannot do this while in the Black Market"));
                            }
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

        // null check
        if (location != null) {
            // region check
            ProtectedRegion region = WorldGuard.getRegion(location);
            if (region != null) {

                Level level = Parkour.getLevelManager().get(region.getId());

                // make sure the area they are spawning in is a level
                if (level != null)
                    performLevelTeleport(Parkour.getStatsManager().get(player), player, level);
            }
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
}