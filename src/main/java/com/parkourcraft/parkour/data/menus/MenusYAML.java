package com.parkourcraft.parkour.data.menus;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;

public class MenusYAML {

    private static FileConfiguration menusConfig = Parkour.getConfigManager().get("menus");

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

    public static void setItemType(String menuName, int pageNumber, int itemSlot, String oldValue, String newValue) {
        String itemPath = menuName + "." + pageNumber + "." + itemSlot;

        for (String string : menusConfig.getConfigurationSection(itemPath).getKeys(true))
            // found it
            if (menusConfig.getString(itemPath + "." + string).equalsIgnoreCase(oldValue)) {
                menusConfig.set(itemPath + "." + string, newValue);
                break;
            }

        Parkour.getConfigManager().save("menus");
    }

    public static String getItemType(String menuName, int pageNumber, int itemSlot) {
        String itemPath = pageNumber + "." + itemSlot;

        if (isSet(menuName, itemPath + ".level"))
            return "level";
        if (isSet(menuName, itemPath + ".perk"))
            return "perk";
        if (isSet(menuName, itemPath + ".teleport"))
            return "teleport";
        if (isSet(menuName, itemPath + ".open"))
            return "open";
        if (isSet(menuName, itemPath + ".type"))
            return "type";

        return "display";
    }

    public static String getItemTypeValue(String menuName, int pageNumber, int itemSlot) {
        String itemPath = pageNumber + "." + itemSlot;

        if (isSet(menuName, itemPath + ".level"))
            return menusConfig.getString(menuName + "." + itemPath + ".level");
        if (isSet(menuName, itemPath + ".perk"))
            return menusConfig.getString(menuName + "." + itemPath + ".perk");
        if (isSet(menuName, itemPath + ".teleport"))
            return menusConfig.getString(menuName + "." + itemPath + ".teleport");
        if (isSet(menuName, itemPath + ".open"))
            return menusConfig.getString(menuName + "." + itemPath + ".open");
        if (isSet(menuName, itemPath + ".type"))
            return menusConfig.getString(menuName + "." + itemPath + ".type");
        return "";
    }


    public static boolean hasItem(String menuName, int pageNumber, int itemSlot) {
        return isSet(menuName, pageNumber + "." + itemSlot);
    }

    public static List<String> getCommands(String menuName, int pageNumber, int itemSlot) {
        String itemPath = pageNumber + "." + itemSlot;

        if (isSet(menuName, itemPath + ".commands"))
            return menusConfig.getStringList(menuName + "." + itemPath + ".commands");

        return new ArrayList<>();
    }

    public static List<String> getConsoleCommands(String menuName, int pageNumber, int itemSlot) {
        String itemPath = pageNumber + "." + itemSlot;

        if (isSet(menuName, itemPath + ".console_commands"))
            return menusConfig.getStringList(menuName + "." + itemPath + ".console_commands");

        return new ArrayList<>();
    }

    public static List<String> getItemLore(String menuName, int pageNumber, int itemSlot) {
        String itemPath = pageNumber + "." + itemSlot + ".item";

        if (isSet(menuName, itemPath + ".lore"))
            return menusConfig.getStringList(menuName + "." + itemPath + ".lore");

        return new ArrayList<>();
    }

    public static String getItemTitle(String menuName, int pageNumber, int itemSlot) {
        String itemPath = pageNumber + "." + itemSlot + ".item";

        if (isSet(menuName, itemPath + ".title"))
            return menusConfig.getString(menuName + "." + itemPath + ".title");

        return "";

    }

    public static ItemStack getItem(String menuName, int pageNumber, int itemSlot) {
        String title = "";
        Material material = Material.STAINED_GLASS_PANE;
        int type = 7;
        int size = 1;
        List<String> lore = new ArrayList<>();
        int armorColor = -1;

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

            if (isSet(menuName, itemPath + ".armor_color"))
                armorColor = menusConfig.getInt(menuName + "." + itemPath + ".armor_color");

            lore = Utils.formatLore(getItemLore(menuName, pageNumber, itemSlot));
        }

        ItemStack item = new ItemStack(material, size, (byte) type);

        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));

        if (lore.size() > 0)
            itemMeta.setLore(lore);

        item.setItemMeta(itemMeta);

        if (armorColor > 0) {
            LeatherArmorMeta leatherItemMeta = (LeatherArmorMeta) item.getItemMeta();
            leatherItemMeta.setColor(Color.fromRGB(armorColor));
            item.setItemMeta(leatherItemMeta);
        }

        return item;
    }

}
