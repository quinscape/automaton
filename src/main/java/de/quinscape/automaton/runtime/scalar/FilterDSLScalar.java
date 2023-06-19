package de.quinscape.automaton.runtime.scalar;

import de.quinscape.spring.jsview.util.JSONUtil;

import java.util.Map;
import java.util.Objects;

abstract class FilterDSLScalar
{
    protected Map<String, Object> root;


    public FilterDSLScalar(Map<String, Object> root)
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
        return super.toString() + ": " + root;
    }
}
