package de.quinscape.automaton.runtime.scalar;

import de.quinscape.domainql.annotation.GraphQLScalar;
import de.quinscape.spring.jsview.util.JSONUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Wraps the actual node data for usage in GraphQL.
 */
@GraphQLScalar
public final class ConditionScalar
{
    private Map<String, Object> root;

    public ConditionScalar()
    {
        this(null);
    }


    public ConditionScalar(Map<String, Object> root)
    {
        this.root = root;
    }


    public Map<String, Object> getRoot()
    {
        return root;
    }


    public void setRoot(Map<String, Object> root)
    {
        this.root = root;
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
        ConditionScalar that = (ConditionScalar) o;
        return Objects.equals(root, that.root);
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(root);
    }


    @Override
    public String toString()
    {
        return super.toString() + ": " + JSONUtil.DEFAULT_GENERATOR.forValue(root);
    }
}
