package de.quinscape.automaton.runtime.filter.transformer;

import de.quinscape.automaton.runtime.data.FilterTransformationException;
import de.quinscape.automaton.runtime.data.JOOQTransformer;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.Map;
import java.util.function.Function;

public class ConcatTransformer
    implements JOOQTransformer
{
    @Override
    public Object filter(Map<String, Object> node, Function<Map<String, Object>, Object> transformer)
    {
        final Field<?>[] fields = ConditionBuilder.getOperands(node)
            .stream()
            .map(o -> {
                final Object result = transformer.apply(o);
                if (result instanceof Field)
                {
                    return (Field<?>) result;
                }
                else
                {
                    throw new FilterTransformationException("Argument to concat is not a field or value: " + result);
                }
            })
            .toArray(Field[]::new);

        return DSL.concat(fields);
    }
}
