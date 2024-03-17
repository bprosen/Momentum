package com.renatusnetwork.momentum.data.bank;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.bank.items.*;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.storage.mysql.DatabaseManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import com.renatusnetwork.momentum.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class BankDB
{
    public static HashMap<BankItemType, BankItem> getItems(int week)
    {
        Map<String, String> result = DatabaseQueries.getResult(DatabaseManager.BANK_WEEKS, "*", "WHERE week=?", week);
        HashMap<BankItemType, BankItem> items = new HashMap<>();

        for (BankItemType itemType : BankItemType.values())
        {
            BankItem item = null;
            String itemNameKey = null;
            switch (itemType)
            {
                case RADIANT:
                    item = new RadiantItem();
                    itemNameKey = "radiant_item_name";
                    break;
                case BRILLIANT:
                    item = new BrilliantItem();
                    itemNameKey = "brilliant_item_name";
                    break;
                case LEGENDARY:
                    item = new LegendaryItem();
                    itemNameKey = "legendary_item_name";
                    break;
            }

            if (item != null)
            {
                Map<String, String> itemResults = DatabaseQueries.getResult(
                        DatabaseManager.BANK_ITEMS, "*",
              "WHERE bank_item_type=? AND name=?", itemType.name(), result.get(itemNameKey));

                item.setTitle(itemResults.get("title"));
                item.setDescription(itemResults.get("description"));
                item.setModifier(Momentum.getModifiersManager().getModifier(itemResults.get("modifier_name")));
                item.setCurrentHolder(getCurrentHolder(week, itemType));
                item.setTotalBalance(getTotal(week, itemType));
            }

            items.put(itemType, item);
        }

        return items;
    }

    public static String getCurrentHolder(int week, BankItemType type)
    {
        Map<String, String> bidResult = DatabaseQueries.getResult(
                DatabaseManager.BANK_BIDS + " b", "highest_name, MAX(total_bid) AS bid_amount",
                "JOIN players p ON p.uuid=b.uuid GROUP BY p.uuid WHERE b.week=? AND b.bank_item_type=?", week, type.name()
        );

        return bidResult.get("highest_name");
    }

    public static int getTotal(int week, BankItemType type)
    {
        Map<String, String> totalBid = DatabaseQueries.getResult(
                DatabaseManager.BANK_BIDS, "SUM(total_bid) AS total_amount", "WHERE b.week=? AND b.bank_item_type=?", week, type.name()
        );

        String total = totalBid.get("total_amount");
        return Utils.isInteger(total) ? Integer.parseInt(total) : 0;
    }


    public static int getCurrentWeek()
    {
        Map<String, String> weekResult = DatabaseQueries.getResult(DatabaseManager.BANK_WEEKS, "MAX(week) AS current_week", "");
        String current = weekResult.get("current_week");

        return current != null ? Integer.parseInt(current) : -1;
    }

    public static BankItem createRandomBankItem(BankItemType type)
    {
        BankItem item = null;
        List<Map<String, String>> results = DatabaseQueries.getResults(DatabaseManager.BANK_ITEMS, "*", "WHERE bank_item_type=?", type);

        if (!results.isEmpty())
        {
            Map<String, String> randomResult = results.get(ThreadLocalRandom.current().nextInt(results.size()));

            switch (type)
            {
                case RADIANT:
                    item = new RadiantItem();
                    break;
                case BRILLIANT:
                    item = new BrilliantItem();
                    break;
                case LEGENDARY:
                    item = new LegendaryItem();
                    break;
            }

            item.setTitle(randomResult.get("title"));
            item.setDescription(randomResult.get("description"));
            item.setModifier(Momentum.getModifiersManager().getModifier(randomResult.get("modifier_name")));
        }

        return item;
    }
}
