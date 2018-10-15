package de.quinscape.automaton.model.process;

import de.quinscape.automaton.model.NamedModel;

public class Transition
    implements NamedModel
{
    private String name;

    private String to;

    private String code;


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public String getTo()
    {
        return to;
    }


    public void setTo(String to)
    {
        this.to = to;
    }


    public String getCode()
    {
        return code;
    }


    public void setCode(String code)
    {
        this.code = code;
    }
}
