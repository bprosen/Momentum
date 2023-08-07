package com.renatusnetwork.parkour.data.blackmarket;


public class BlackMarketArtifact
{
    private String name;
    private String title;
    private String description;

    public BlackMarketArtifact(String name)
    {
        this.name = name;
        this.title = BlackMarketYAML.getTitle(name);
        this.description = BlackMarketYAML.getDescription(name);
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription() { return description; }
}
