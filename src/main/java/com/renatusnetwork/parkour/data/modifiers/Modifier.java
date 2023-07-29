package com.renatusnetwork.parkour.data.modifiers;

import com.renatusnetwork.parkour.utils.Utils;

public abstract class Modifier
{
    private String name;
    private String displayName;

    public Modifier(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    private void load()
    {
        this.displayName = Utils.translate(ModifiersYAML.getDisplayName(name));
    }
}
