package de.quinscape.automaton.model.view;

import de.quinscape.automaton.model.NamedModel;
import org.svenson.JSONTypeHint;

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


    public List<ViewDeclaration> getDeclarations()
    {
        return declarations;
    }


    @JSONTypeHint(ViewDeclaration.class)
    public void setDeclarations(List<ViewDeclaration> declarations)
    {
        this.declarations = declarations;
    }


    public Component getRoot()
    {
        return root;
    }


    public void setRoot(Component root)
    {
        this.root = root;
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
