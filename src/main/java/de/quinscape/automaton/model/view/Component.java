package de.quinscape.automaton.model.view;

import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

public class Component
{
    /**
     * Special attribute that the render will draw out to control rendering of the complete component.
     */
    public final static String RENDERED_IF = "renderedIf";

    private String name;

    private Map<String, Object> attrs;

    private RenderFunction kidsFn;

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
    public Map<String, Object> getAttrs()
    {
        return attrs;
    }


    @JSONTypeHint(RenderFunction.class)
    public void setAttrs(Map<String, Object> attrs)
    {
        ensureStringsOrRenderProps(attrs);

        this.attrs = attrs;
    }


    public RenderFunction getKidsFn()
    {
        return kidsFn;
    }


    @JSONProperty(ignoreIfNull = true)
    public void setKidsFn(RenderFunction kidsFn)
    {
        this.kidsFn = kidsFn;
    }


    private void ensureStringsOrRenderProps(Map<String, Object> attrs)
    {
        if (attrs != null)
        {
            for (Map.Entry<String, Object> e : attrs.entrySet())
            {
                final Object value = e.getValue();

                if (!(value instanceof String) && !(value instanceof RenderFunction))
                {
                    throw new IllegalArgumentException("Invalid attribute set on Component '" + name +"': " + value);
                }
            }
        }
    }


    @JSONProperty(ignoreIfNull = true)
    public List<Component> getKids()
    {
        return kids;
    }


    @JSONTypeHint(Component.class)
    public void setKids(List<Component> kids)
    {
        this.kids = kids;
    }


    static void describe(StringBuilder buff, Component component, int level)
    {
        indent(buff, level);

        buff.append('<')
            .append(component.name);

        final Map<String, Object> attrs = component.getAttrs();
        if (attrs != null)
        {
            for (Map.Entry<String, Object> e : attrs.entrySet())
            {
                buff.append(' ').append(e.getKey()).append("=");

                final Object value = e.getValue();

                if (value instanceof RenderFunction)
                {
                    ((RenderFunction) value).describe(buff, level + 1);
                }
                else
                {
                    buff.append(value);
                }

            }
        }

        final List<Component> kids = component.getKids();
        final RenderFunction kidsFn = component.getKidsFn();
        if (kids == null && kidsFn == null)
        {
            buff.append("/>\n");
        }
        else
        {
            buff.append(">\n");

            if (kidsFn != null)
            {
                Component.indent(buff, level);
                kidsFn.describe(buff, level + 1);
            }
            else
            {
                for (Component kid : kids)
                {
                    describe(buff, kid, level + 1);
                }
            }

            buff.append("</")
                .append(component.getName())
                .append(">\n");
        }
    }


    static void indent(StringBuilder buff, int level)
    {
        buff.ensureCapacity(buff.length() + level << 2);
        for (int i = 0; i < level; i++)
        {
            buff.append("    ");
        }
    }

    public String getAttribute(String name)
    {
        if (attrs == null)
        {
            return null;
        }

        final Object value = attrs.get(name);
        if (!(value instanceof String))
        {
            throw new IllegalStateException("<" + this.name + " " + name + " /> is not a string attribute");
        }
        return (String) value;
    }

    public RenderFunction getRenderProp(String name)
    {
        if (attrs == null)
        {
            return null;
        }

        final Object value = attrs.get(name);
        if (!(value instanceof RenderFunction))
        {
            throw new IllegalStateException("<" + this.name + " " + name + " /> is not a render prop");
        }
        return (RenderFunction) value;
    }


    @Override
    public String toString()
    {
        StringBuilder buff = new StringBuilder();

        buff.append(super.toString())
            .append(":\n");

        describe(buff, this, 0);

        return buff.toString();
    }

    @PostConstruct
    public void validate()
    {
        if (kids != null && kidsFn != null)
        {
            throw new IllegalStateException("A component cannot have both 'kids' and a 'kidsFn'");
        }
    }
}
