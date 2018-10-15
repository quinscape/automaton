package de.quinscape.automaton.model.view;

import org.svenson.JSONProperty;

import java.util.List;
import java.util.Map;

public class Component
{
    /**
     * Special attribute that the render will draw out to control rendering of the complete component.
     */
    public final static String RENDERED_IF = "renderedIf";

    private String name;
    private Map<String,String> attrs;
    private List<Component> kids;

    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    @JSONProperty(ignoreIfNull = true)
    public Map<String, String> getAttrs()
    {
        return attrs;
    }


    public void setAttrs(Map<String, String> attrs)
    {
        this.attrs = attrs;
    }

    @JSONProperty(ignoreIfNull = true)
    public List<Component> getKids()
    {
        return kids;
    }

    public void setKids(List<Component> kids)
    {
        this.kids = kids;
    }

    private void describe(StringBuilder buff, Component component, int level)
    {
        indent(buff, level);

        buff.append('<')
            .append(component.name);

        final Map<String, String> attrs = component.getAttrs();
        if (attrs != null)
        {
            for (Map.Entry<String, String> e : attrs.entrySet())
            {
                buff.append(' ').append(e.getKey()).append("=").append( e.getValue());
            }
        }

        final List<Component> kids = component.getKids();
        if (kids == null)
        {
            buff.append("/>\n");
        }
        else
        {
            buff.append(">\n");

            for (Component kid : kids)
            {
                describe(buff, kid, level + 1);
            }
            buff.append("</")
                .append(component.getName())
                .append(">\n");
        }
    }


    private void indent(StringBuilder buff, int level)
    {
        buff.ensureCapacity(buff.length() + level << 2);
        for (int i=0; i < level; i++)
        {
            buff.append("    ");
        }
    }


    @Override
    public String toString()
    {
        StringBuilder buff = new StringBuilder();

        buff.append(super.toString())
            .append(":\n");

        describe(buff, this, 0);

        return  buff.toString();
    }
}
