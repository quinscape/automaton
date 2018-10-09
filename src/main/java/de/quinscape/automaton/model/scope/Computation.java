package de.quinscape.automaton.model.scope;

public class Computation
    implements ScopeDeclaration
{
    private String name;

    private String code;


    public void setName(String name)
    {
        this.name = name;
    }


    @Override
    public String getName()
    {
        return name;
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
