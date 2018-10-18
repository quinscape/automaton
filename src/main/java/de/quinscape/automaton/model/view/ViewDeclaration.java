package de.quinscape.automaton.model.view;

import javax.validation.constraints.NotNull;

/**
 * A named constant within a view.
 */
public class ViewDeclaration
{
    private String name;
    private String code;
    private String comment;


    public ViewDeclaration()
    {
        this("noName", "'no-value'");
    }
    
    public ViewDeclaration(String name, String code)
    {
        this.name = name;
        this.code = code;
    }


    @NotNull
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    @NotNull
    public String getCode()
    {
        return code;
    }


    public void setCode(String code)
    {
        this.code = code;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ViewDeclaration that = (ViewDeclaration) o;

        if (!name.equals(that.name))
        {
            return false;
        }
        return code.equals(that.code);
    }


    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + code.hashCode();
        return result;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            + ", code = '" + code + '\''
            ;
    }


    public String getComment()
    {
        return comment;
    }


    public void setComment(String comment)
    {
        this.comment = comment;
    }
}
