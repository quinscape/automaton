package de.quinscape.automaton.model.view;

import de.quinscape.automaton.model.NamedModel;

import java.util.List;

public class View
    implements NamedModel
{
    private String name;

    private List<ViewDeclaration> declarations;

    private Component root;

    public void setName(String name)
    {
        this.name = name;
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            + ", declarations = " + declarations
            + ", root = " + root
            ;
    }
}
