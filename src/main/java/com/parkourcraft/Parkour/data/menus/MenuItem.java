package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MenuItem extends MenuPage {

    public static MenuItem menuItem;

    private int slot;
    private String actionType;
    private ItemStack item;

    private MenuItemType menuItemType;

    public MenuItem() {
        menuItem = this;
    }

    public void loadMenuItem(int slot) {
        this.slot = slot;

        String itemPath = menu.getName() + "." + menuPage.getPageNumber() + "." + slot;

        if (menusConfig.isSet(itemPath)) {
            String itemItemPath = itemPath + ".item";

            String title = "";
            String material = "STAINED_GLASS_PANE";
            int type = 7;
            int size = 1;
            List<String> lore = new ArrayList<>();

            if (menusConfig.isSet(itemPath + ".item")) {
                if (menusConfig.isSet(itemItemPath + ".title"))
                    title = menusConfig.getString(itemItemPath + ".title");

                if (menusConfig.isSet(itemItemPath + ".material"))
                    material = menusConfig.getString(itemItemPath + ".material");

                if (menusConfig.isSet(itemItemPath + ".type"))
                    type = menusConfig.getInt(itemItemPath + ".type");
                else
                    type = 0;

                if (menusConfig.isSet(itemItemPath + ".size"))
                    size = menusConfig.getInt(itemItemPath + ".size");

                if (menusConfig.isSet(itemItemPath + ".lore")) {
                    lore = menusConfig.getStringList(itemItemPath + ".lore");
                    lore = Utils.formatLore(lore);
                }
            }

            item = new ItemStack(Material.getMaterial(material), size, (byte) type);

            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);

            if (menusConfig.isSet(itemPath + ".level"))
                menuItemType = new MenuItemType(
                        "level",
                        menusConfig.getString(itemPath + ".level")
                );
            else if (menusConfig.isSet(itemPath + ".teleport"))
                menuItemType = new MenuItemType(
                        "teleport",
                        menusConfig.getString(itemPath + ".teleport")
                );
            else if (menusConfig.isSet(itemPath + ".open"))
                menuItemType = new MenuItemType(
                        "open",
                        menusConfig.getString(itemPath + ".open")
                );
            else
                menuItemType = new MenuItemType(
                        "display",
                        ""
                );
        }
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getFormattedItem(PlayerStats playerStats) {
        return menuItemType.getFormattedItem(playerStats);
    }

    public ItemStack getItem() {
        return item;
    }

}
