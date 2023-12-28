package com.renatusnetwork.parkour.data.modifiers;

public abstract class Modifier
{
    private String name;
    private String title;
    private ModifierType type;

    public Modifier(ModifierType type, String name, String title)
    {
        this.type = type;
        this.name = name;
        this.title = title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setType(ModifierType type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public String getTitle()
    {
        return title;
    }

    public ModifierType getType() { return type; }

    public boolean equals(Modifier modifier)
    {
        return modifier.getName().equals(name);
    }
}
