package de.quinscape.automaton.runtime.data;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.automaton.runtime.AutomatonException;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;
import de.quinscape.automaton.runtime.scalar.ConditionScalar;
import de.quinscape.automaton.runtime.scalar.FieldExpressionScalar;
import de.quinscape.automaton.runtime.scalar.NodeType;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Transforms the FilterDSL condition nodes / maps into JOOQ conditions.
 */
public class FilterTransformer
{
    private final static Logger log = LoggerFactory.getLogger(FilterTransformer.class);

    private ConcurrentMap<String, AccessHolder> fieldAccessHolders = new ConcurrentHashMap<>();

    private static MethodAccess fieldAccess = MethodAccess.get(Field.class);

    private static JSON JSON_GEN = JSONUtil.DEFAULT_GENERATOR;


    public Condition transform(
        QueryContext queryContext, ConditionScalar conditionScalar
    )
    {
        final Object transformed = transformRecursive(queryContext, conditionScalar.getRoot());

        if (!(transformed instanceof Condition))
        {
            throw new AutomatonException("Transformed condition scalar returns no condition: " + transformed);
        }

        return (Condition) transformed;
    }

    public OrderField<?> transform(
        QueryContext queryContext, FieldExpressionScalar fieldExpressionScalar
    )
    {
        final Object transformed = transformRecursive(queryContext, fieldExpressionScalar.getRoot());

        if (!(transformed instanceof OrderField))
        {
            throw new AutomatonException("Transformed condition scalar returns no field: " + transformed);
        }
        return (OrderField<?>) transformed;
    }


    private Object transformRecursive(
        QueryContext queryContext,
        Map<String, Object> condition
    )
    {
        if (condition == null)
        {
            return DSL.trueCondition();
        }

        final NodeType nodeType = NodeType.forName(
            ConditionBuilder.getType(condition)
        );

        switch (nodeType)
        {
            case COMPONENT:
            {
                final Map<String, Object> kid = ConditionBuilder.getCondition(condition);
                return transformRecursive(queryContext, kid);
            }
            case CONDITION:
            {

                final String name = ConditionBuilder.getName(condition);
                final List<Map<String, Object>> operands = ConditionBuilder.getOperands(condition);

                if (name.equals("and"))
                {
                    return DSL.and(
                        transformOperands(queryContext, operands)
                    );
                }
                else if (name.equals("or"))
                {
                    return DSL.or(
                        transformOperands(queryContext, operands)
                    );
                }
                else
                {
                    return invokeFieldMethod(queryContext, condition);
                }
            }
            case FIELD:
            {
                final String name = ConditionBuilder.getName(condition);
                return queryContext.resolveField(name);
            }
            case VALUE:
            {
                return DSL.val(ConditionBuilder.getValue(condition));
            }
            case OPERATION:
            {
                return invokeFieldMethod(queryContext, condition);
            }
            default:
                throw new AutomatonException("Unhandled node type: " + nodeType);
        }
    }


    private Object invokeFieldMethod(
        QueryContext queryContext,
        Map<String, Object> condition
    )
    {
        final String name = ConditionBuilder.getName(condition);
        final List<Map<String, Object>> operands = ConditionBuilder.getOperands(condition);

        if (operands.size() == 0)
        {
            throw new AutomatonException("Field condition has no operand");
        }

        final Object value = transformRecursive(
            queryContext,
            operands.get(0)
        );

        if (!(value instanceof Field))
        {
            throw new AutomatonException("Field Operation: Fisrst operand did not evaluate to field: " + JSON_GEN.forValue(
                condition));
        }

        Field field = (Field) value;

        AccessHolder holder = new AccessHolder(name, operands.size() - 1);
        final AccessHolder existing = fieldAccessHolders.putIfAbsent(name, holder);
        if (existing != null)
        {
            holder = existing;
        }
        return holder.invoke(field, transformRestOfOperands(queryContext, operands));
    }


    private Object[] transformRestOfOperands(
        QueryContext queryContext,
        List<Map<String, Object>> operands
    )
    {
        Object[] array = new Object[operands.size() - 1];
        for (int i = 1; i < operands.size(); i++)
        {
            array[i - 1] = transformRecursive(queryContext, operands.get(i));
        }
        return array;
    }


    private Collection<? extends Condition> transformOperands(
        QueryContext queryContext,
        List<Map<String, Object>> operands
    )
    {
        List<Condition> list = new ArrayList<>(operands.size());

        for (Map<String, Object> operand : operands)
        {
            final Object value = transformRecursive(
                queryContext,
                operand
            );

            if (!(value instanceof Condition))
            {
                throw new AutomatonException("Operand did not transform to condition: " + JSON_GEN.forValue(operand));
            }

            list.add(
                (Condition) value
            );
        }

        return list;
    }


    private static class AccessHolder
    {
        private final String name;

        private final int numArgs;

        private volatile Integer methodIndex;


        public AccessHolder(String name, int numArgs)
        {

            this.name = name;
            this.numArgs = numArgs;
        }


        public Object invoke(Field field, Object... operands)
        {
            if (methodIndex == null)
            {
                synchronized (this)
                {
                    if (methodIndex == null)
                    {
                        methodIndex = findMethodIndex();
                    }
                }
            }
            return fieldAccess.invoke(field, methodIndex, operands);
        }


        private int findMethodIndex()
        {
            for (Method method : Field.class.getMethods())
            {
                if (method.getName().equals(name))
                {
                    final Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == numArgs && Arrays.stream(parameterTypes)
                        .allMatch(t -> Field.class.isAssignableFrom(t)))
                    {
                        return fieldAccess.getIndex(name, method.getParameterTypes());
                    }
                }
            }
            throw new AutomatonException("Could not find method with name '" + name + "' and " + numArgs + " Field parameters");
        }
    }
}
