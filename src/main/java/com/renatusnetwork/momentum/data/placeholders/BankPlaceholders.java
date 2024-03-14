package com.renatusnetwork.momentum.data.placeholders;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.bank.types.BankItem;
import com.renatusnetwork.momentum.data.bank.types.BankItemType;
import com.renatusnetwork.momentum.utils.Utils;

public class BankPlaceholders
{

    public static final String BANK_PREFIX = "bank";

    public static String processPlaceholder(String placeholder)
    {
        String[] split = placeholder.split("_");

        if (split.length == 2)
        {
            try
            {
                // get type
                BankItemType bankType = BankItemType.valueOf(split[0].toUpperCase());
                BankItem item = Momentum.getBankManager().getItem(bankType);

                switch (split[1].toLowerCase())
                {
                    // current holder
                    case "holder":
                        return item.getCurrentHolder();
                    // total of the bank item
                    case "total":
                        return Utils.formatNumber(item.getTotalBalance());
                    // cost of next bid
                    case "nextbid":
                        return Utils.formatNumber(item.getNextBid());
                }
            }
            catch (IllegalArgumentException exception)
            {
                return "Invalid bank type";
            }
        }
        return "";
    }
}
