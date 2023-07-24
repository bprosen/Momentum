package com.renatusnetwork.parkour.data.bank;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.types.BankItemType;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
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

    public static int chooseBankItem(BankItemType type)
    {
        List<Integer> values = new ArrayList<>();

        for (int i = 1;; i++)
        {
            if (isSection("items." + type.toString() + "." + i))
                values.add(i);
            else break;
        }

        return values.get(ThreadLocalRandom.current().nextInt(values.size() - 1));
    }

    public static String getTitle(BankItemType type, int bankItemNum)
    {
        return bankConfig.getString("items." + type.toString() + "." + bankItemNum + ".title");
    }
}
