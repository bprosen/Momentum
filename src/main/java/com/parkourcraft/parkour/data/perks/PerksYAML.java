package com.parkourcraft.parkour.data.perks;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PerksYAML {

    private static FileConfiguration perksConfig = Parkour.getConfigManager().get("perks");

    private static void commit(String perkName) {
        Parkour.getConfigManager().save("perks");
        Parkour.getPerkManager().load(perkName);
    }

    public static List<String> getNames() {
        return new ArrayList<>(perksConfig.getKeys(false));
    }

    public static boolean exists(String perkName) {
        return perksConfig.isSet(perkName);
    }

    public static boolean isSet(String perkName, String valuePath) {
        return perksConfig.isSet(perkName + "." + valuePath);
    }

    public static String getTitle(String perkName) {
        if (isSet(perkName, "title"))
            return perksConfig.getString(perkName + ".title");

        return perkName;
    }

    public static HashMap<String, ItemStack> getItems(String perkName) {

        HashMap<String, ItemStack> items = new HashMap<>();

        if (perksConfig.isConfigurationSection(perkName + ".items")) {
            if (perksConfig.isConfigurationSection(perkName + ".items.head"))
                items.put("head", createItem(perkName, "head"));

            if (perksConfig.isConfigurationSection(perkName + ".items.chest"))
                items.put("chest", createItem(perkName, "chest"));

            if (perksConfig.isConfigurationSection(perkName + ".items.leg"))
                items.put("leg", createItem(perkName, "leg"));

            if (perksConfig.isConfigurationSection(perkName + ".items.feet"))
                items.put("feet", createItem(perkName, "feet"));
        }
        return items;
    }

    private static ItemStack createItem(String perkName, String armorType) {

        Material itemType = Material.matchMaterial(perksConfig.getString(perkName + ".items." + armorType + ".material"));

        // in case wrong material name, default to white glass
        ItemStack item;
        if (itemType != null)
            item = new ItemStack(itemType,1,
                                (short) perksConfig.getInt(perkName + ".items." + armorType + ".type"));
        else
            item = new ItemStack(Material.STAINED_GLASS_PANE, 1);

        // if not null, continue
        if (item != null) {

            ItemMeta itemMeta = item.getItemMeta();

            if (isSet(perkName, "items." + armorType + ".color")) {

                String leatherArmorColor = perksConfig.getString(perkName + ".items." + armorType + ".color");
                Color armorColor = Utils.getColorFromString(leatherArmorColor);

                // if not null, this means colored leather armor
                if (armorColor != null) {
                    LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
                    meta.setColor(armorColor);
                    meta.setDisplayName(Utils.translate(perksConfig.getString(perkName + ".items." + armorType + ".title")));

                    List<String> lore = new ArrayList<>();
                    for (String loreString : perksConfig.getStringList(perkName + ".items." + armorType + ".lore"))
                        lore.add(Utils.translate(loreString));

                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
            // everything else is here
            } else {
                itemMeta.setDisplayName(Utils.translate(perksConfig.getString(perkName + ".items." + armorType + ".title")));

                List<String> lore = perksConfig.getStringList(perkName + ".items." + armorType + ".lore");
                if (!lore.isEmpty()) {
                    for (String loreString : lore)
                        lore.add(Utils.translate(loreString));

                    itemMeta.setLore(lore);
                }
                item.setItemMeta(itemMeta);
            }
        }
        return item;
    }

    public static List<String> getRequirements(String perkName) {
        if (isSet(perkName, "requirements"))
            return perksConfig.getStringList(perkName + ".requirements");

        return new ArrayList<>();
    }

    public static List<String> getRequiredPermissions(String perkName) {
        if (isSet(perkName, "required_permissions"))
            return perksConfig.getStringList(perkName + ".required_permissions");

        return new ArrayList<>();
    }

    public static int getPrice(String perkName) {
        if (isSet(perkName, "price"))
            return perksConfig.getInt(perkName + ".price");

        return 0;
    }

    public static void create(String perkName) {
        if (!exists(perkName)) {
            perksConfig.set(perkName + ".permissions", new ArrayList<>());
            perksConfig.set(perkName + ".requirements", new ArrayList<>());

            commit(perkName);
        }
    }

    public static void setTitle(String perkName, String title) {
        if (exists(perkName))
            perksConfig.set(perkName + ".title", title);

        commit(perkName);
    }

    public static void setPermissions(String perkName, List<String> permissions) {
        if (exists(perkName))
            perksConfig.set(perkName + ".permissions", permissions);

        commit(perkName);
    }

    public static void setRequirements(String perkName, List<String> requirements) {
        if (exists(perkName))
            perksConfig.set(perkName + ".requirements", requirements);

        commit(perkName);
    }

    public static void setRequiredPermissions(String perkName, List<String> permissions) {
        if (exists(perkName))
            perksConfig.set(perkName + ".required_permissions", permissions);

        commit(perkName);
    }

    public static void setPrice(String perkName, int price) {
        if (exists(perkName))
            perksConfig.set(perkName + ".price", price);

        commit(perkName);
    }

}
