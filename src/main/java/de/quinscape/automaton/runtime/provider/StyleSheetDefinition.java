package de.quinscape.automaton.runtime.provider;

public final class StyleSheetDefinition
{
    private final String name;

    private final String uri;


    public StyleSheetDefinition(String name, String uri)
    {

        this.name = name;
        this.uri = uri;
    }


    public String getName()
    {
        return name;
    }


    public String getUri()
    {
        return uri;
    }
}
