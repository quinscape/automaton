package de.quinscape.automaton.model.view;

import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A named constant within a view.
 */
public class ViewDeclaration
{
    private List<String> names;
    private String code;


    public ViewDeclaration()
    {
        this( Collections.emptyList(), null);
    }
    
    public ViewDeclaration(String name, String code)
    {
        this(Collections.singletonList(name), code);
    }
    public ViewDeclaration(List<String> names, String code)
    {
        if (names == null)
        {
            throw new IllegalArgumentException("names can't be null");
        }

        this.names = names;
        this.code = code;
    }


    @NotNull
    public List<String> getNames()
    {
        return names;
    }


    public void setNames(List<String> names)
    {
        if (names == null)
        {
            throw new IllegalArgumentException("names can't be null");
        }
        this.names = names;
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

        if (!names.equals(that.names))
        {
            return false;
        }
        return code != null ? code.equals(that.code) : that.code == null;
    }


    @Override
    public int hashCode()
    {
        int result = names.hashCode();
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "names = " + names
            + ", code = '" + code + '\''
            ;
    }
}
