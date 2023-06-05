package de.quinscape.automaton.runtime.filter.transformer;

import de.quinscape.automaton.runtime.data.FilterTransformationException;
import de.quinscape.automaton.runtime.data.JOOQTransformer;
import de.quinscape.automaton.runtime.data.NodeTransformer;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;
import org.jooq.Field;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ToStringTransformer
    implements JOOQTransformer
{
    @Override
    public Object filter(Map<String, Object> node, NodeTransformer transformer)
    {
        final List<Map<String, Object>> operands = ConditionBuilder.getOperands(node);
        final Object value = transformer.transform(operands.get(0));

        if (value == null)
        {
            return null;
        }

        if (!(value instanceof Field))
        {
            throw new FilterTransformationException("Argument to toString() is not a field:" + value);
        }

        return ((Field<?>) value).cast(String.class);
    }
}
