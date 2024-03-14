package com.renatusnetwork.momentum.data.menus;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CancelTasks
{
    private HashMap<Integer, ItemStack> beforeCancel;
    private List<BukkitTask> cancelledSlots;
    private Inventory lastEditedInventory;

    public CancelTasks(Inventory inventory)
    {
        this.lastEditedInventory = inventory;

        beforeCancel = new HashMap<>();
        cancelledSlots = new ArrayList<>();
    }

    public void setLastEditedInventory(Inventory inventory)
    {
        this.lastEditedInventory = inventory;
    }

    public Inventory getLastEditedInventory()
    {
        return lastEditedInventory;
    }

    public boolean hasItemInSlot(int slot)
    {
        return beforeCancel.containsKey(slot);
    }

    public void addSlot(int slot, ItemStack itemStack, BukkitTask task)
    {
        beforeCancel.put(slot, itemStack);
        cancelledSlots.add(task);
    }

    public HashMap<Integer, ItemStack> getBeforeCancelItems()
    {
        return beforeCancel;
    }

    public List<BukkitTask> getCancelledSlots()
    {
        return cancelledSlots;
    }
}
