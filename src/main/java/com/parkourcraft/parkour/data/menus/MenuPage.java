package com.parkourcraft.parkour.data.menus;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuPage {

    private int pageNumber;
    private int rowCount;

    private Map<Integer, MenuItem> pageItemsMap = new HashMap<>();

    MenuPage(Menu menu, int pageNumber) {
        this.pageNumber = pageNumber;

        load(menu);
    }

    private void load(Menu menu) {
        rowCount = Menus_YAML.getRowCount(menu.getName(), pageNumber);
        int slotCount = rowCount * 9;

        for (int slot = 0; slot <= slotCount - 1; slot++) {
            if (Menus_YAML.hasItem(menu.getName(), pageNumber, slot))
                pageItemsMap.put(slot, new MenuItem(menu, this, slot));
        }
    }

    void formatInventory(Player player, InventoryView inventory) {
        if (!inventory.getTopInventory().getName().equalsIgnoreCase("submitted-plots")) {
            for (MenuItem menuItem : pageItemsMap.values())
                inventory.setItem(menuItem.getSlot(), MenuItemFormatter.format(
                        player,
                        Parkour.getStatsManager().get(player),
                        menuItem));

        // create plot submit gui for staff!
        } else {

            List<Plot> submittedPlots = Parkour.getPlotsManager().getSubmittedPlots();
            for (int i = 0; i < inventory.getTopInventory().getSize(); i++) {

                Plot plot = submittedPlots.get(i);
                ItemStack item = new ItemStack(Material.SKULL);
                SkullMeta skullMeta = (SkullMeta) item.getItemMeta();

                final String plotOwnerName = plot.getOwnerName();

                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(plot.getOwnerUUID()));
                skullMeta.setDisplayName(Utils.translate("&4" + plotOwnerName + "&c's Plot Submission"));

                List<String> itemLore = new ArrayList<String>() {{
                    add("");
                    add(Utils.translate("&7Click to teleport to"));
                    add(Utils.translate("&4" + plotOwnerName + "&c's Plot"));
                    add("");
                    add("&7Awaiting &aaccept &7or &cdeny");
                    add("");
                }};

                skullMeta.setLore(itemLore);
                item.setItemMeta(skullMeta);
            }
        }
    }

    int getPageNumber() {
        return pageNumber;
    }

    int getRowCount() {
        return rowCount;
    }

    public MenuItem getMenuItem(int slot) {
        return pageItemsMap.get(slot);
    }

    public MenuItem getMenuItemFromTitle(String itemTitle) {
        for (MenuItem menuItem : pageItemsMap.values()) {
            if (menuItem.getItem().getItemMeta().getDisplayName().equals(itemTitle))
                return menuItem;

            if (menuItem.getType().equals("level")) {
                LevelObject level = Parkour.getLevelManager().get(menuItem.getTypeValue());

                if (level != null
                        && level.getFormattedTitle().equals(itemTitle))
                    return menuItem;
            }
        }

        return null;
    }


}
