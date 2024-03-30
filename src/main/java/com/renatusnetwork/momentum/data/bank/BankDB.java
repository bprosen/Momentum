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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class BankDB
{
    public static CompletableFuture<List<Map<String, String>>> getItem(String itemName)
    {
        return DatabaseQueries.getResultsAsync(DatabaseManager.BANK_ITEMS, "*", "WHERE name=?", itemName);
    }

    public static void insertWeek(int week, String brilliantName, String radiantName, String legendaryName)
    {
        long startTime = System.currentTimeMillis();

        DatabaseQueries.runAsyncQuery(
           "INSERT INTO " + DatabaseManager.BANK_WEEKS +
                " (week, brilliant_item_name, radiant_item_name, legendary_item_name, start_date) " +
                "VALUES(?,?,?,?,?)",
                week, brilliantName, radiantName, legendaryName, startTime
        );

        if (week > 1)
            DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.BANK_WEEKS + " SET end_date=? WHERE week=?", startTime, (week - 1));
    }

    public static void insertItem(String name)
    {
        DatabaseQueries.runAsyncQuery("INSERT INTO " + DatabaseManager.BANK_ITEMS + " (name) VALUES (?)", name);
    }

    public static void updateTitle(String name, String title)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.BANK_ITEMS + " SET title=? WHERE name=?", title, name);
    }

    public static void updateDescription(String name, String description)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.BANK_ITEMS + " SET description=? WHERE name=?", description, name);
    }

    public static void updateType(String name, BankItemType type)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.BANK_ITEMS + " SET bank_item_type=? WHERE name=?", type.name(), name);
    }

    public static void updateModifier(String name, String modifierName)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.BANK_ITEMS + " SET modifier_name=? WHERE name=?", modifierName, name);
    }

    public static HashMap<BankItemType, BankItem> getItems(int week)
    {
        Map<String, String> result = DatabaseQueries.getResult(DatabaseManager.BANK_WEEKS, "*", "WHERE week=?", week);
        HashMap<BankItemType, BankItem> items = new HashMap<>();

        if (!result.isEmpty())
        {
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

                    item.setName(itemResults.get("name"));
                    item.setTitle(itemResults.get("title"));
                    item.setDescription(itemResults.get("description"));
                    item.setModifier(Momentum.getModifiersManager().getModifier(itemResults.get("modifier_name")));
                    item.setCurrentHolder(getCurrentHolder(week, itemType));
                    item.setTotalBalance(getTotal(week, itemType));
                }

                items.put(itemType, item);
            }
        }

        return items;
    }

    public static String getCurrentHolder(int week, BankItemType type)
    {
        Map<String, String> bidResult = DatabaseQueries.getResult(
                DatabaseManager.BANK_BIDS + " b", "name",
                "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=b.uuid WHERE b.week=? AND b.bank_item_type=? ORDER BY total_bid DESC LIMIT 1", week, type.name()
        );

        return bidResult.get("name");
    }

    public static int getTotal(int week, BankItemType type)
    {
        Map<String, String> totalBid = DatabaseQueries.getResult(
                DatabaseManager.BANK_BIDS, "SUM(total_bid) AS total_amount", "WHERE week=? AND bank_item_type=?", week, type.name()
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
        List<Map<String, String>> results = DatabaseQueries.getResults(DatabaseManager.BANK_ITEMS, "*", "WHERE bank_item_type=?", type.name());

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

            item.setName(randomResult.get("name"));
            item.setTitle(randomResult.get("title"));
            item.setDescription(randomResult.get("description"));
            item.setModifier(Momentum.getModifiersManager().getModifier(randomResult.get("modifier_name")));
            item.calcNextBid();
        }

        return item;
    }
}
