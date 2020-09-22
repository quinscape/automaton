package de.quinscape.automaton.runtime.data;

import com.esotericsoftware.reflectasm.MethodAccess;
import de.quinscape.automaton.runtime.AutomatonException;
import de.quinscape.automaton.runtime.filter.CachedFilterContextResolver;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;
import de.quinscape.automaton.runtime.scalar.ConditionScalar;
import de.quinscape.automaton.runtime.scalar.FieldExpressionScalar;
import de.quinscape.automaton.runtime.scalar.NodeType;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.jooq.Condition;
import org.jooq.False;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    private final FilterContextRegistry registry;

    public FilterTransformer()
    {
        this(null);
    }
    public FilterTransformer(FilterContextRegistry registry)
    {
        this.registry = registry;
    }


    public Condition transform(
        FieldResolver fieldResolver, ConditionScalar conditionScalar
    )
    {

        final CachedFilterContextResolver resolver = new CachedFilterContextResolver(registry);

        final Object transformed = transformRecursive(resolver, fieldResolver, conditionScalar.getRoot());

//        if (!(transformed instanceof Condition))
//        {
//            throw new AutomatonException("Transformed condition scalar returns no condition: " + transformed);
//        }

        return (Condition) transformed;
    }

    public OrderField<?> transform(
        FieldResolver fieldResolver, FieldExpressionScalar fieldExpressionScalar
    )
    {
        final CachedFilterContextResolver resolver = new CachedFilterContextResolver(registry);

        final Object transformed = transformRecursive(resolver, fieldResolver, fieldExpressionScalar.getRoot());

        if (transformed == null)
        {
            return null;
        }

        if (!(transformed instanceof OrderField))
        {
            throw new AutomatonException("Transformed condition scalar returns no field: " + transformed);
        }
        return (OrderField<?>) transformed;
    }


    private Object transformRecursive(
        CachedFilterContextResolver resolver, FieldResolver fieldResolver,
        Map<String, Object> node
    )
    {
        if (node == null)
        {
            return DSL.trueCondition();
        }

        final NodeType nodeType = NodeType.forName(
            ConditionBuilder.getType(node)
        );

        switch (nodeType)
        {
            case COMPONENT:
            {
                final Map<String, Object> kid = ConditionBuilder.getCondition(node);
                return transformRecursive(resolver, fieldResolver, kid);
            }
            case CONDITION:
            {

                final String name = ConditionBuilder.getName(node);
                final List<Map<String, Object>> operands = ConditionBuilder.getOperands(node);

                if (name.equals("and"))
                {
                    return DSL.and(
                        transformOperands(resolver, fieldResolver, operands)
                    );
                }
                else if (name.equals("or"))
                {
                    return DSL.or(
                        transformOperands(resolver, fieldResolver, operands)
                    );
                }
                else if (name.equals("not"))
                {
                    if (operands.size() > 1)
                    {
                        throw new IllegalStateException("Not has only one argument");
                    }

                    final List<? extends Condition> conditions = transformOperands(resolver, fieldResolver, operands);
                    return DSL.not(
                        conditions.get(0)
                    );
                }
                else if (name.equals("true"))
                {
                    return DSL.trueCondition();
                }
                else if (name.equals("false"))
                {
                    return DSL.falseCondition();
                }
                else
                {
                    return invokeFieldMethod(resolver,fieldResolver, node);
                }
            }
            case FIELD:
            {
                final String name = ConditionBuilder.getName(node);
                return fieldResolver.resolveField(name);
            }
            case VALUE:
            {
                return DSL.val(ConditionBuilder.getValue(node));
            }
            case VALUES:
            {
                final Collection<?> values = ConditionBuilder.getValues(node);
                final List<Object> vals = new ArrayList<>(values.size());
                for (Object value : values)
                {
                    vals.add(DSL.val(value));
                }
                return vals;
            }
            case OPERATION:
            {
                return invokeFieldMethod(resolver, fieldResolver, node);
            }
            case CONTEXT:
            {
                final String name = ConditionBuilder.getName(node);
                return DSL.val(resolver.invokeProvider(resolver.resolveContext(name)));
            }
            default:
                throw new AutomatonException("Unhandled node type: " + nodeType);
        }
    }


    private Object invokeFieldMethod(
        CachedFilterContextResolver resolver, FieldResolver fieldResolver,
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
            resolver,
            fieldResolver,
            operands.get(0)
        );

        // field reference is not part of this query execution, we ignore the whole condition
        if (value == null)
        {
            return null;
        }

        if (!(value instanceof Field))
        {
            throw new AutomatonException("Field Operation: Fisrst operand did not evaluate to field: " + JSON_GEN.forValue(
                condition));
        }

        Field<?> field = (Field<?>) value;

        AccessHolder holder = new AccessHolder(name, operands.size() - 1);
        final AccessHolder existing = fieldAccessHolders.putIfAbsent(name, holder);
        if (existing != null)
        {
            holder = existing;
        }
        return holder.invoke(field, transformRestOfOperands(resolver, fieldResolver, operands));
    }


    private Object[] transformRestOfOperands(
        CachedFilterContextResolver resolver,
        FieldResolver fieldResolver,
        List<Map<String, Object>> operands
    )
    {
        Object[] array = new Object[operands.size() - 1];
        for (int i = 1; i < operands.size(); i++)
        {
            array[i - 1] = transformRecursive(resolver, fieldResolver, operands.get(i));
        }
        return array;
    }


    private List<? extends Condition> transformOperands(
        CachedFilterContextResolver resolver, FieldResolver fieldResolver,
        List<Map<String, Object>> operands
    )
    {
        List<Condition> list = new ArrayList<>(operands.size());

        for (Map<String, Object> operand : operands)
        {
            final Object value = transformRecursive(
                resolver,
                fieldResolver,
                operand
            );

            if (value != null)
            {
                list.add(
                    (Condition) value
                );
            }
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
                        .allMatch(t -> Field.class.isAssignableFrom(t) || t.equals(Collection.class)))
                    {
                        return fieldAccess.getIndex(name, method.getParameterTypes());
                    }
                }
            }
            throw new AutomatonException("Could not find method with name '" + name + "' and " + numArgs + " Field parameters");
        }
    }
}
