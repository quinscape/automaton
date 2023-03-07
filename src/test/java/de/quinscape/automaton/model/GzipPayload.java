package de.quinscape.automaton.model;

public class GzipPayload
{
    private String value;


    public GzipPayload()
    {
        this(null);
    }
    public GzipPayload(String value)
    {
        this.value = value;
    }


    public String getValue()
    {
        return value;
    }


    public void setValue(String value)
    {
        this.value = value;
    }
}
