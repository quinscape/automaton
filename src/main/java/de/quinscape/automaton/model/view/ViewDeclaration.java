package de.quinscape.automaton.model.view;

/**
 * A named constant within a view.
 */
public class ViewDeclaration
{
    private String name;
    private String type;
    private String code;


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    public String getCode()
    {
        return code;
    }


    public void setCode(String code)
    {
        this.code = code;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            + ", type = '" + type + '\''
            + ", code = '" + code + '\''
            ;
    }
}
