package de.quinscape.automaton.runtime.filter;

import java.sql.Timestamp;

public class FilterCoercionTarget
{
    private final String text;

    private final Timestamp created;


    public FilterCoercionTarget(String text, Timestamp created)
    {
        this.text = text;
        this.created = created;
    }


    public String getText()
    {
        return text;
    }


    public Timestamp getCreated()
    {
        return created;
    }
}
