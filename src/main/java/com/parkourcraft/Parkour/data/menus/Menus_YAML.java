package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.storage.local.FileManager;
import com.parkourcraft.Parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Menus_YAML {

    private static FileConfiguration menusConfig = FileManager.getFileConfig("menus");

    public static List<String> getNames() {
        return new ArrayList<>(menusConfig.getKeys(false));
    }

    public static boolean exists(String menuName) {
        return menusConfig.isSet(menuName);
    }

    public static boolean isSet(String menuName, String valuePath) {
        return menusConfig.isSet(menuName + "." + valuePath);
    }

    public static String getTitle(String menuName) {
        if (isSet(menuName, "settings.title"))
            return menusConfig.getString(menuName + ".settings.title");

        return menuName;
    }

    public static int getPageCount(String menuName) {
        if (isSet(menuName, "settings.page_count"))
            return menusConfig.getInt(menuName + ".settings.page_count");

        return 1;
    }

    public static boolean getUpdating(String menuName) {
        if (isSet(menuName, "settings.updating"))
            return menusConfig.getBoolean(menuName + ".settings.updating");

        return false;
    }

    public static ItemStack getSelectItem(String menuName) {
        String selectItemPath = "settings.select_item";

        if (isSet(menuName, selectItemPath + ".material")) {
            String material = menusConfig.getString(menuName + "." + selectItemPath + ".material");
            int type = 0;

            if (isSet(menuName, selectItemPath + ".type"))
                type = menusConfig.getInt(selectItemPath + ".type");

            if (Material.getMaterial(material) != null)
                return new ItemStack(Material.getMaterial(material), 1, (byte) type);
        }

        return null;
    }

    public static int getRowCount(String menuName, int pageNumber) {
        if (isSet(menuName, pageNumber + ".row_count"))
            return menusConfig.getInt(menuName + "." + pageNumber + ".row_count");

        return 1;
    }

    public static String getItemType(String menuName, int pageNumber, int itemSlot) {
        String itemPath = pageNumber + "." + itemSlot;

        if (isSet(menuName, itemPath + ".level"))
            return "level";
        if (isSet(menuName, itemPath + ".teleport"))
            return "teleport";
        if (isSet(menuName, itemPath + ".open"))
            return "open";

        return "display";
    }

    public static String getItemTypeValue(String menuName, int pageNumber, int itemSlot) {
        String itemPath = pageNumber + "." + itemSlot;

        if (isSet(menuName, itemPath + ".level"))
            return menusConfig.getString(menuName + "." + itemPath + ".level");
        if (isSet(menuName, itemPath + ".teleport"))
            return menusConfig.getString(menuName + "." + itemPath + ".teleport");
        if (isSet(menuName, itemPath + ".open"))
            return menusConfig.getString(menuName + "." + itemPath + ".open");

        return "";
    }

    public static boolean hasItem(String menuName, int pageNumber, int itemSlot) {
        return isSet(menuName, pageNumber + "." + itemSlot);
    }

    public static ItemStack getItem(String menuName, int pageNumber, int itemSlot) {
        String title = "";
        Material material = Material.STAINED_GLASS_PANE;
        int type = 7;
        int size = 1;
        List<String> lore = new ArrayList<>();

        if (hasItem(menuName, pageNumber, itemSlot)) {
            String itemPath = pageNumber + "." + itemSlot + ".item";

            if (isSet(menuName, itemPath + ".title"))
                title = menusConfig.getString(menuName + "." + itemPath + ".title");

            if (isSet(menuName, itemPath + ".material"))
                material = Material.getMaterial(menusConfig.getString(menuName + "." + itemPath + ".material"));
            if (material == null)
                material = Material.STAINED_GLASS_PANE;

            if (isSet(menuName, itemPath + ".type"))
                type = menusConfig.getInt(menuName + "." + itemPath + ".type");
            else
                type = 0;

            if (isSet(menuName, itemPath + ".size"))
                size = menusConfig.getInt(menuName + "." + itemPath + ".size");

            if (isSet(menuName, itemPath + ".lore")) {
                lore = menusConfig.getStringList(menuName + "." + itemPath + ".lore");
                lore = Utils.formatLore(lore);
            }
        }

        ItemStack item = new ItemStack(material, size, (byte) type);

        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));

        if (lore.size() > 0)
            itemMeta.setLore(lore);

        item.setItemMeta(itemMeta);

        return item;
    }

}
