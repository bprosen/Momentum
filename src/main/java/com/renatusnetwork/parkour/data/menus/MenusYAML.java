package com.renatusnetwork.parkour.data.menus;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MenusYAML {

    private static FileConfiguration menusConfig = Parkour.getConfigManager().get("menus");

    public static List<String> getNames() {
        return new ArrayList<>(menusConfig.getKeys(false));
    }

    public static boolean exists(String menuName) {
        return menusConfig.isSet(menuName);
    }

    public static boolean isSet(String menuName, String valuePath)
    {
        return menusConfig.isSet(menuName + "." + valuePath);
    }

    public static Set<String> getKeys(String path, boolean deep)
    {
        return menusConfig.getConfigurationSection(path).getKeys(deep);
    }

    public static String getTitle(String menuName)
    {
        return menusConfig.getString(menuName + ".settings.title", menuName);
    }

    public static int getPageCount(String menuName)
    {
        return menusConfig.getInt(menuName + ".settings.page_count", 1);
    }

    public static ItemStack getSelectItem(String menuName)
    {
        String selectItemPath = "settings.select_item";

        if (isSet(menuName, selectItemPath + ".material"))
        {
            String material = menusConfig.getString(menuName + "." + selectItemPath + ".material");
            int type = 0;

            if (isSet(menuName, selectItemPath + ".type"))
                type = menusConfig.getInt(selectItemPath + ".type");

            if (Material.getMaterial(material) != null)
                return new ItemStack(Material.getMaterial(material), 1, (byte) type);
        }

        return null;
    }

    public static int getRowCount(String menuName, int pageNumber)
    {
        return menusConfig.getInt(menuName + "." + pageNumber + ".row_count", 1);
    }

    public static void setItemType(String menuName, int pageNumber, int itemSlot, String oldValue, String newValue)
    {
        String itemPath = menuName + "." + pageNumber + "." + itemSlot;

        for (String string : menusConfig.getConfigurationSection(itemPath).getKeys(true))
            // found it
            if (menusConfig.getString(itemPath + "." + string).equalsIgnoreCase(oldValue))
            {
                menusConfig.set(itemPath + "." + string, newValue);
                break;
            }

        Parkour.getConfigManager().save("menus");
    }

    public static String getItemType(String menuName, int pageNumber, String itemSlot)
    {
        String itemPath = pageNumber + "." + itemSlot;

        if (isSet(menuName, itemPath + ".level"))
            return "level";
        if (isSet(menuName, itemPath + ".perk"))
            return "perk";
        if (isSet(menuName, itemPath + ".teleport"))
            return "teleport";
        if (isSet(menuName, itemPath + ".open"))
            return "open";
        if (isSet(menuName, itemPath + ".rate"))
            return "rate";
        if (isSet(menuName, itemPath + ".type"))
            return "type";
        if (isSet(menuName, itemPath + ".bank"))
            return "bank";
        if (isSet(menuName, itemPath + ".infinite-mode"))
            return "infinite-mode";

        return "display";
    }

    public static String getItemTypeValue(String menuName, int pageNumber, String itemSlot, String type)
    {
        return menusConfig.getString(menuName + "." + pageNumber + "." + itemSlot + "." + type, "");
    }

    public static boolean hasItem(String menuName, int pageNumber, String itemSlot) {
        return isSet(menuName, pageNumber + "." + itemSlot);
    }

    public static boolean getSortedLevelTypes(String menuName)
    {
        return menusConfig.getBoolean(menuName + ".settings.sort_levels", false);
    }
    public static List<String> getCommands(String menuName, int pageNumber, String itemSlot)
    {
        return menusConfig.getStringList(menuName + "." + pageNumber + "." + itemSlot + ".commands");
    }

    public static List<String> getConsoleCommands(String menuName, int pageNumber, String itemSlot)
    {
        return menusConfig.getStringList(menuName + "." + pageNumber + "." + itemSlot + ".console_commands");
    }


    public static List<String> getItemLore(String menuName, int pageNumber, String itemSlot)
    {
        return menusConfig.getStringList(menuName + "." + pageNumber + "." + itemSlot + ".item.lore");
    }

    public static String getItemTitle(String menuName, int pageNumber, String itemSlot)
    {
        return menusConfig.getString(menuName + "." + pageNumber + "." + itemSlot + ".item.title");
    }

    public static boolean getGlow(String menuName, int pageNumber, String itemSlot)
    {
        return menusConfig.getBoolean(menuName + "." + pageNumber + "." + itemSlot + ".item.glow", false);
    }

    public static ItemStack getItem(String menuName, int pageNumber, String itemSlot)
    {
        String title = "";
        Material material = Material.STAINED_GLASS_PANE;
        int type = 7;
        int size = 1;
        List<String> lore = new ArrayList<>();
        Color armorColor = null;

        if (hasItem(menuName, pageNumber, itemSlot)) {
            String itemPath = pageNumber + "." + itemSlot + ".item";

            title = menusConfig.getString(menuName + "." + itemPath + ".title", "");

            if (isSet(menuName, itemPath + ".material"))
                material = Material.getMaterial(menusConfig.getString(menuName + "." + itemPath + ".material"));

            if (material == null)
                material = Material.STAINED_GLASS_PANE;

            type = menusConfig.getInt(menuName + "." + itemPath + ".type", 0);
            size = menusConfig.getInt(menuName + "." + itemPath + ".size", 1);

            if (isSet(menuName, itemPath + ".armor_color"))
                armorColor = Utils.getColorFromString(menusConfig.getString(menuName + "." + itemPath + ".armor_color"));

            lore = Utils.formatLore(getItemLore(menuName, pageNumber, itemSlot));
        }

        ItemStack item = new ItemStack(material, size, (byte) type);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));

        if (!lore.isEmpty())
            itemMeta.setLore(lore);

        item.setItemMeta(itemMeta);

        if (armorColor != null)
        {
            LeatherArmorMeta leatherItemMeta = (LeatherArmorMeta) item.getItemMeta();
            leatherItemMeta.setColor(armorColor);
            item.setItemMeta(leatherItemMeta);
        }
        return item;
    }
}
