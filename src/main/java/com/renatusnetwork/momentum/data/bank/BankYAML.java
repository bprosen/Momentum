package com.renatusnetwork.momentum.data.bank;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.bank.types.BankItemType;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BankYAML
{
    private static FileConfiguration bankConfig = Momentum.getConfigManager().get("bank");

    private static void commit() {
        Momentum.getConfigManager().save("bank");
    }

    public static boolean isSection(String pathName)
    {
        return bankConfig.isConfigurationSection(pathName);
    }

    public static boolean isSet(String pathName, String valuePath)
    {
        return bankConfig.isSet(pathName + "." + valuePath);
    }

    public static String chooseBankItem(BankItemType type)
    {
        String lowerCase = type.toString().toLowerCase();
        List<String> keys = new ArrayList<>(bankConfig.getConfigurationSection("items." + lowerCase).getKeys(false));

        return keys.get(ThreadLocalRandom.current().nextInt(keys.size()));
    }

    public static String getTitle(BankItemType type, String name)
    {
        return bankConfig.getString("items." + type.toString().toLowerCase() + "." + name + ".title");
    }

    public static String getTitle(BankItemType type)
    {
        String item = bankConfig.getString("data." + type.toString().toLowerCase() + ".item");
        return getTitle(type, item);
    }

    public static String getModifier(BankItemType type, String name)
    {
        return bankConfig.getString("items." + type.toString().toLowerCase() + "." + name + ".modifier");
    }

    public static long getTotal(BankItemType type)
    {
        return bankConfig.getLong("data." + type.toString().toLowerCase() + ".total");
    }

    public static String getDescription(BankItemType type)
    {
        String item = bankConfig.getString("data." + type.toString().toLowerCase() + ".item");
        return bankConfig.getString("items." + type.toString().toLowerCase() + "." + item + ".description");
    }

    public static String getHolder(BankItemType type)
    {
        return bankConfig.getString("data." + type.toString().toLowerCase() + ".holder");
    }

    public static String getModifier(BankItemType type)
    {
        return getModifier(type, bankConfig.getString("data." + type.toString().toLowerCase() + ".item"));
    }

    public static void updateBid(BankItemType type, long newTotal, String newHolder)
    {
        bankConfig.set("data." + type.toString().toLowerCase() + ".total", newTotal);
        bankConfig.set("data." + type.toString().toLowerCase() + ".holder", newHolder);
        commit();
    }

    public static void resetBid(BankItemType type, String itemName)
    {
        String typeString = type.toString().toLowerCase();
        bankConfig.set("data." + typeString + ".holder", "");

        switch (type)
        {
            case RADIANT:
                bankConfig.set("data." + typeString + ".total", Momentum.getSettingsManager().radiant_minimum_bid);
                break;
            case BRILLIANT:
                bankConfig.set("data." + typeString + ".total", Momentum.getSettingsManager().brilliant_minimum_bid);
                break;
            case LEGENDARY:
                bankConfig.set("data." + typeString + ".total", Momentum.getSettingsManager().legendary_minimum_bid);
                break;
        }
        bankConfig.set("data." + typeString + ".item", itemName);
        commit();
    }
}
