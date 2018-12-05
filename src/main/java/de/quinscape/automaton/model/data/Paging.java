package de.quinscape.automaton.model.data;

import org.svenson.JSONParameter;

public final class Paging
{
    private final int page;


    public Paging(
        @JSONParameter("page")
        int page
    )
    {
        this.page = page;
    }


    public int getPage()
    {
        return page;
    }
}
