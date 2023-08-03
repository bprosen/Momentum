package com.renatusnetwork.parkour.data.bank;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.types.BankItemType;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BankYAML
{
    private static FileConfiguration bankConfig = Parkour.getConfigManager().get("bank");

    private static void commit() {
        Parkour.getConfigManager().save("bank");
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

    public static String getModifier(BankItemType type, String name)
    {
        return bankConfig.getString("items." + type.toString().toLowerCase() + "." + name + ".modifier");
    }

    public static long getTotal(BankItemType type)
    {
        return bankConfig.getLong("data." + type.toString().toLowerCase() + ".total");
    }

    public static String getHolder(BankItemType type)
    {
        return bankConfig.getString("data." + type.toString().toLowerCase() + ".holder");
    }

    public static String getModifier(BankItemType type)
    {
        return bankConfig.getString("data." + type.toString().toLowerCase() + ".modifier");
    }

    public static void updateBid(BankItemType type, long newTotal, String newHolder)
    {
        bankConfig.set("data." + type + ".total", newTotal);
        bankConfig.set("data." + type + ".holder", newHolder);
        commit();
    }

    public static void resetBid(BankItemType type, String modifierName)
    {
        bankConfig.set("data." + type + ".holder", "");

        switch (type)
        {
            case RADIANT:
                bankConfig.set("data." + type + ".total", Parkour.getSettingsManager().radiant_minimum_bid);
                break;
            case BRILLIANT:
                bankConfig.set("data." + type + ".total", Parkour.getSettingsManager().brilliant_minimum_bid);
                break;
            case LEGENDARY:
                bankConfig.set("data." + type + ".total", Parkour.getSettingsManager().legendary_minimum_bid);
                break;
        }
        bankConfig.set("data." + type + ".modifier", modifierName);
        commit();
    }
}
