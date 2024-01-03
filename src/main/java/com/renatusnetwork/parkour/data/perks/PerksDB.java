package com.renatusnetwork.parkour.data.perks;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PerksDB
{

    public static void insert(String perkName)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.PERKS_TABLE + " (name) VALUES (?)", perkName
        );
    }

    public static void remove(String perkName)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.PERKS_TABLE + " WHERE name=?", perkName
        );
    }

    public static void updateTitle(String perkName, String title)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PERKS_TABLE + " SET title=? WHERE name=?", title, perkName
        );
    }

    public static void updatePrice(String perkName, int price)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PERKS_TABLE + " SET price=? WHERE name=?", price, perkName
        );
    }

    public static void updateInfiniteBlock(String perkName, String material)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PERKS_TABLE + " SET infinite_block=? WHERE name=?", material, perkName
        );
    }

    public static void updateRequiredPermission(String perkName, String permission)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PERKS_TABLE + " SET required_permission=? WHERE name=?", permission, perkName
        );
    }

    public static void insertRequiredLevel(String perkName, String levelName)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.PERKS_LEVEL_REQUIREMENTS_TABLE + " (perk_name, level_name) VALUES (?,?)", perkName, levelName
        );
    }

    public static void removeRequiredLevel(String perkName, String levelName)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.PERKS_LEVEL_REQUIREMENTS_TABLE + " WHERE perk_name=? AND level_name=?", perkName, levelName
        );
    }

    public static void insertArmor(String perkName, String armorPiece, String material)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.PERKS_ARMOR_TABLE + " (perk_name, armor_piece, material) VALUES (?,?,?)", perkName, armorPiece, material
        );
    }

    public static void updateArmorGlow(String perkName, String armorPiece)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PERKS_ARMOR_TABLE + " SET glow=NOT glow WHERE perk_name=? AND armor_piece=?", perkName, armorPiece
        );
    }

    public static void updateArmorTitle(String perkName, String armorPiece, String title)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PERKS_ARMOR_TABLE + " SET title=? WHERE perk_name=? AND armor_piece=?", title, perkName, armorPiece
        );
    }

    public static void updateArmorMaterial(String perkName, String armorPiece, String material)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PERKS_ARMOR_TABLE + " SET material=? WHERE perk_name=? AND armor_piece=?", material, perkName, armorPiece
        );
    }

    public static void updateArmorMaterialType(String perkName, String armorPiece, int typeNum)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PERKS_ARMOR_TABLE + " SET type=? WHERE perk_name=? AND armor_piece=?", typeNum, perkName, armorPiece
        );
    }

    public static void removeArmor(String perkName, String armorPiece)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.PERKS_ARMOR_TABLE + " WHERE perk_name=? AND armor_piece=?", perkName, armorPiece
        );
    }

    public static HashMap<String, Perk> getPerks()
    {
        List<Map<String, String>> perksResults = DatabaseQueries.getResults(
                DatabaseManager.PERKS_TABLE, "*", ""
        );

        HashMap<String, Perk> perks = new HashMap<>();

        // parse
        for (Map<String, String> perkResult : perksResults)
        {
            String perkName = perkResult.get("name");
            perks.put(perkName, parsePerkFromResult(perkName, perkResult));
        }

        return perks;
    }

    public static Perk getPerk(String perkName)
    {
        Map<String, String> perkResult = DatabaseQueries.getResult(
                DatabaseManager.PERKS_TABLE, "*", "WHERE name=?", perkName
        );

        if (!perkResult.isEmpty())
            return parsePerkFromResult(perkName, perkResult);
        else
            return null;
    }
    private static Perk parsePerkFromResult(String perkName, Map<String, String> result)
    {
        Perk perk = new Perk(perkName);
        perk.setTitle(result.get("title"));

        String price = result.get("price");

        // parse if not null
        if (price != null)
            perk.setPrice(Integer.parseInt(price));

        perk.setRequiredPermission(result.get("required_permission"));

        // parse if not null
        String infiniteString = result.get("infinite_block");
        if (infiniteString != null)
            perk.setInfiniteBlock(Material.matchMaterial(infiniteString));

        perk.setRequiresMasteryLevels(Integer.parseInt(result.get("requires_mastery_levels")) == 1);

        // set data using 2 extra queries, is fine since it's only done on startup, allows for clean code
        perk.setRequiredLevels(getRequiredLevels(perkName));
        perk.setArmorItems(getArmorItems(perkName));
        perk.setCommands(getCommands(perkName));

        return perk;
    }

    public static void updateRequiresMasteryLevels(String perkName)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PERKS_TABLE + " SET requires_mastery_levels=NOT requires_mastery_levels WHERE name=?", perkName);
    }

    public static List<Level> getRequiredLevels(String perkName)
    {
        List<Map<String, String>> perksResults = DatabaseQueries.getResults(
                DatabaseManager.PERKS_LEVEL_REQUIREMENTS_TABLE, "required_level_name", "WHERE perk_name=?", perkName
        );

        List<Level> levels = new ArrayList<>();

        for (Map<String, String> perkResult : perksResults)
            levels.add(Parkour.getLevelManager().get(perkResult.get("required_level_name")));

        return levels;
    }

    public static HashMap<PerksArmorType, ItemStack> getArmorItems(String perkName)
    {
        List<Map<String, String>> perksResults = DatabaseQueries.getResults(
                DatabaseManager.PERKS_ARMOR_TABLE, "*", "WHERE perk_name=?", perkName
        );

        HashMap<PerksArmorType, ItemStack> armor = new HashMap<>();

        for (Map<String, String> perkResult : perksResults)
        {
            // get all types
            PerksArmorType armorType = PerksArmorType.valueOf(perkResult.get("armor_piece"));
            Material material = Material.matchMaterial(perkResult.get("material"));
            int type = Integer.parseInt(perkResult.get("type"));
            String title = perkResult.get("title");
            boolean glow = Integer.parseInt(perkResult.get("glow")) == 1;

            ItemStack item = new ItemStack(material, 1, (short) type);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(Utils.translate(title));

            if (glow)
            {
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(itemMeta);

            armor.put(armorType, item);
        }

        return armor;
    }

    public static HashSet<String> getCommands(String perkName)
    {
        List<Map<String, String>> perksResults = DatabaseQueries.getResults(
                DatabaseManager.PERKS_COMMANDS, "*", "WHERE perk_name=?", perkName
        );

        HashSet<String> commands = new HashSet<>();
        for (Map<String, String> result : perksResults)
            commands.add(result.get("command"));

        return commands;
    }

    public static void addCommand(String perkName, String command)
    {
        DatabaseQueries.runAsyncQuery("INSERT INTO " + DatabaseManager.PERKS_COMMANDS + " (perk_name, command) VALUES (?,?)", perkName, command);
    }

    public static void removeCommand(String perkName, String command)
    {
        DatabaseQueries.runAsyncQuery("DELETE FROM " + DatabaseManager.PERKS_COMMANDS + " WHERE perk_name=? AND command=?", perkName, command);
    }
}