package com.renatusnetwork.parkour.data.modifiers;

import com.renatusnetwork.parkour.utils.Utils;

public abstract class Modifier
{
    private String name;
    private String displayName;
    private ModifierTypes type;

    public Modifier(ModifierTypes type, String name)
    {
        this.type = type;
        this.name = name;

        load();
    }

    public String getName()
    {
        return name;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public ModifierTypes getType() { return type; }

    public boolean equals(Modifier modifier)
    {
        return modifier.getName().equals(name);
    }

    private void load()
    {
        this.displayName = Utils.translate(ModifiersYAML.getDisplayName(name));
    }
}
