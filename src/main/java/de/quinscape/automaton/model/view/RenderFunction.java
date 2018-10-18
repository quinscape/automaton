package de.quinscape.automaton.model.view;

import org.svenson.JSONTypeHint;

import java.util.List;

public class RenderFunction
{
    private String context;

    private List<ViewDeclaration> declarations;

    private Component root;


    public String getContext()
    {
        return context;
    }


    public void setContext(String context)
    {
        this.context = context;
    }


    @JSONTypeHint(ViewDeclaration.class)
    public List<ViewDeclaration> getDeclarations()
    {
        return declarations;
    }


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

    void describe(StringBuilder buff,int level)
    {
        buff.append("{\n");

        Component.indent(buff, level);
        buff.append(context)
            .append(" => {\n");


        for (ViewDeclaration declaration : declarations)
        {
            Component.indent(buff, level + 1);
            buff.append("const ").append(declaration.getName()).append(" = ").append(declaration.getCode()).append(";\n");
        }
        Component.indent(buff, level + 1);
        buff.append("return (\n");

        Component.describe(buff, root, level + 2);

        Component.indent(buff, level + 1);
        buff.append(")\n");

        Component.indent(buff, level);
        buff.append("}\n");

        Component.indent(buff, level - 1);
        buff.append("}\n");
    }
}
