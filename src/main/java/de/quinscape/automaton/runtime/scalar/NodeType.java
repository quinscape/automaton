package de.quinscape.automaton.runtime.scalar;

public enum NodeType
{
    CONDITION("Condition"),
    FIELD("Field"),
    VALUE("Value"),
    VALUES("Values"),
    OPERATION("Operation"),
    COMPONENT("Component");

    private final String name;


    NodeType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static NodeType forName(String name)
    {
        return NodeType.valueOf(name.toUpperCase());
    }
}
