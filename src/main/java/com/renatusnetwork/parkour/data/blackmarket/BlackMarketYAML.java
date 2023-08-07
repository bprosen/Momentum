package com.renatusnetwork.parkour.data.blackmarket;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.types.BankItemType;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BlackMarketYAML
{
    private static FileConfiguration blackmarketConfig = Parkour.getConfigManager().get("blackmarket");

    private static void commit() {
        Parkour.getConfigManager().save("blackmarket");
    }

    public static boolean isSection(String pathName)
    {
        return blackmarketConfig.isConfigurationSection(pathName);
    }

    public static boolean isSet(String pathName, String valuePath)
    {
        return blackmarketConfig.isSet(pathName + "." + valuePath);
    }

    public static String chooseBankItem()
    {
        List<String> keys = new ArrayList<>(blackmarketConfig.getConfigurationSection("items").getKeys(false));

        return keys.get(ThreadLocalRandom.current().nextInt(keys.size()));
    }

    public static String getTitle(String name)
    {
        return blackmarketConfig.getString("items." + name + ".title");
    }

    public static String getDescription(String name)
    {
        return blackmarketConfig.getString("items." + name + ".description");
    }
}
