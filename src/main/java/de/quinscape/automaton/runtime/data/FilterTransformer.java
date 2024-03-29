package de.quinscape.automaton.runtime.data;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.collect.ImmutableMap;
import de.quinscape.automaton.model.domain.DecimalPrecision;
import de.quinscape.automaton.runtime.AutomatonException;
import de.quinscape.automaton.runtime.filter.CachedFilterContextResolver;
import de.quinscape.automaton.runtime.filter.transformer.ConcatTransformer;
import de.quinscape.automaton.runtime.filter.transformer.ToStringTransformer;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;
import de.quinscape.automaton.runtime.scalar.ConditionScalar;
import de.quinscape.automaton.runtime.scalar.FieldExpressionScalar;
import de.quinscape.automaton.runtime.scalar.ComputedValueScalar;
import de.quinscape.automaton.runtime.scalar.NodeType;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.TableLookup;
import de.quinscape.domainql.meta.DomainQLMeta;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Transforms the FilterDSL condition nodes / maps into JOOQ conditions.
 */
public class FilterTransformer
{
    private final static Logger log = LoggerFactory.getLogger(FilterTransformer.class);

    private ConcurrentMap<String, AccessHolder> fieldAccessHolders = new ConcurrentHashMap<>();

    /**
     * Positive list for secure DSL condition and operation names. Should be kept in synch with
     * FilterDSL.js in @quinscape/automaton.js.
     *
     * Should be disjoint to the interface based implementations in {@link #transformers}
     */
    private final static Set<String> POSITIVE_LIST = Collections.unmodifiableSet(
        new HashSet<>(
            Arrays.asList(
                "greaterOrEqual", "lessOrEqual", "lt", "notBetweenSymmetric", "notEqualIgnoreCase", "betweenSymmetric",
                "lessThan", "equalIgnoreCase", "isDistinctFrom", "between", "ge", "greaterThan", "isNotNull", "notLikeRegex",
                "notBetween", "notEqual", "isFalse", "containsIgnoreCase", "eq", "gt", "equal", "likeRegex", "isTrue",
                "contains", "notContainsIgnoreCase", "notContains", "ne", "isNull", "endsWith", "le", "isNotDistinctFrom",
                "startsWith", "in", "not", "or", "orNot", "and", "andNot", "bitNand", "mod", "div", "neg", "rem", "add",
                "subtract", "plus", "bitAnd", "bitXor", "shl", "unaryMinus", "bitNor", "shr", "modulo", "bitXNor", "bitNot",
                "sub", "minus", "mul", "bitOr", "times", "pow", "divide", "power", "multiply", "unaryPlus", "lower",
                "upper", "asc", "desc"
            )
        )
    );

    private static MethodAccess fieldAccess = MethodAccess.get(Field.class);

    private final static JSON JSON_GEN = JSONUtil.DEFAULT_GENERATOR;

    private final DomainQL domainQL;

    private final FilterContextRegistry registry;

    private Map<String, JOOQTransformer> transformers = ImmutableMap.of(
        "toString", new ToStringTransformer(),
        "concat", new ConcatTransformer()
    );


    private Map<String, ComputedValue> computedValues = ImmutableMap.of(
        "now", new CurrentTimeStampFunction(),
        "today", new CurrentDateFunction(),
        "param", new JasperParameterFunction()
    );


    public FilterTransformer(DomainQL domainQL)
    {
        this(domainQL, null);
    }


    public FilterTransformer(DomainQL domainQL, FilterContextRegistry registry)
    {
        this.domainQL = domainQL;
        this.registry = registry;
    }


    public Condition transform(
        FieldResolver fieldResolver, ConditionScalar conditionScalar
    )
    {

        final CachedFilterContextResolver resolver = new CachedFilterContextResolver(registry);

        final Object transformed = transformRecursive(resolver, fieldResolver, conditionScalar.getRoot(), null);

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

        final Object transformed = transformRecursive(resolver, fieldResolver, fieldExpressionScalar.getRoot(), null);

        if (transformed == null)
        {
            return null;
        }

        if (!(transformed instanceof OrderField))
        {
            throw new FilterTransformationException("Transformed condition scalar returns no field: " + transformed);
        }
        return (OrderField<?>) transformed;
    }


    private Object transformRecursive(
        CachedFilterContextResolver resolver, FieldResolver fieldResolver,
        Map<String, Object> node,
        Map<String, Object> parent
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
                return transformRecursive(resolver, fieldResolver, kid, node);
            }
            case CONDITION:
            {

                final String name = ConditionBuilder.getName(node);
                final List<Map<String, Object>> operands = ConditionBuilder.getOperands(node);

                if (name.equals("and"))
                {
                    final List<? extends Condition> conditions = transformOperands(resolver, fieldResolver, operands, node);
                    if (conditions.size() == 0)
                    {
                        return DSL.trueCondition();
                    }

                    return DSL.and(
                        conditions
                    );
                }
                else if (name.equals("or"))
                {
                    final List<? extends Condition> conditions = transformOperands(resolver, fieldResolver, operands, node);
                    if (conditions.size() == 0)
                    {
                        return DSL.trueCondition();
                    }

                    return DSL.or(
                        conditions
                    );
                }
                else if (name.equals("not"))
                {
                    if (operands.size() > 1)
                    {
                        throw new FilterTransformationException("Not has only one argument");
                    }

                    final List<? extends Condition> conditions = transformOperands(resolver, fieldResolver, operands, node);
                    if(conditions.size()==0) {
                        return DSL.trueCondition();
                    }

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
                    return invokeFieldMethod(resolver, fieldResolver, node);
                }
            }
            case FIELD:
            {
                final String name = ConditionBuilder.getName(node);
                return fieldResolver.resolveField(name);
            }
            case VALUE:
            {
                final Object value = ConditionBuilder.getValue(node);
                if (value instanceof ComputedValueScalar)
                {
                    final ComputedValueScalar computedValueScalar = (ComputedValueScalar) value;
                    return invokeComputedValue(parent, computedValueScalar, fieldResolver);
                }

                return DSL.val(value);
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
                final String name = ConditionBuilder.getName(node);

                final JOOQTransformer transformer = transformers.get(name);
                if (transformer != null)
                {
                    return transformer.filter(node, kid ->
                        transformRecursive(
                            resolver,
                            fieldResolver,
                            kid,
                            node
                        )
                    );
                }

                if (!POSITIVE_LIST.contains(name))
                {
                    throw new FilterTransformationException("'" + name + "' is not in the positive list of allowed names." );
                }

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


    private Object invokeComputedValue(Map<String, Object> parent, ComputedValueScalar computedValueScalar,
        FieldResolver fieldResolver
    )
    {
        final ComputedValueTypeContext typeContext = resolveTypeContext(parent, computedValueScalar, fieldResolver);
        final ComputedValue computedValue = computedValues.get(computedValueScalar.getName());
        final ComputedValueContext ctx = new ComputedValueContext(computedValueScalar, typeContext);

        return computedValue.evaluate(ctx);
    }


    private ComputedValueTypeContext resolveTypeContext(Map<String, Object> node, ComputedValueScalar computedValueScalar,
        FieldResolver fieldResolver
    )
    {
        final NodeType nodeType = NodeType.forName(
            ConditionBuilder.getType(node)
        );

        switch (nodeType)
        {
            case COMPONENT:
            {
                final Map<String, Object> condition = ConditionBuilder.getCondition(node);
                return resolveTypeContext(condition, computedValueScalar, fieldResolver);
            }
            case OPERATION:
            case CONDITION:
            {
                final List<Map<String, Object>> operands = ConditionBuilder.getOperands(node);

                for (Map<String, Object> operand : operands)
                {
                    final ComputedValueTypeContext ctx = resolveTypeContext(
                        operand,
                        computedValueScalar,
                        fieldResolver
                    );
                    if (ctx != null)
                    {
                        if (nodeType == NodeType.OPERATION)
                        {
                            // operation removes the direct field reference
                            return new ComputedValueTypeContext(null, null, ctx.scalarType(), ctx.detail());
                        }
                        return ctx;
                    }
                }
                return null;
            }
            case FIELD:
            {

                final String name = ConditionBuilder.getName(node);
                final Name qualifiedName = fieldResolver.resolveField(name).getQualifiedName();
                final String typeName = findType(qualifiedName.first());

                final GraphQLObjectType type = (GraphQLObjectType) domainQL.getGraphQLSchema().getType(typeName);
                if (type == null)
                {
                    throw new FilterTransformationException("Could not find type '" + typeName + "'");
                }

                final String fieldName = qualifiedName.last();
                final GraphQLFieldDefinition fieldDefinition = type.getFieldDefinition(fieldName);

                if (fieldDefinition == null)
                {
                    throw new FilterTransformationException("Could not find field '" + fieldName + " in " + type);
                }

                final DomainQLMeta metaData = domainQL.getMetaData();
                DecimalPrecision precision = metaData.getTypeMeta(typeName).getFieldMeta(
                    fieldName,
                    "decimalPrecision"
                );

                return new ComputedValueTypeContext(
                    typeName,
                    fieldName,
                    GraphQLTypeUtil.unwrapAll(
                        fieldDefinition.getType()
                    ).getName(),
                    precision
                );

            }
            default:
                return null;
        }
    }


    private String findType(String name)
    {
        for (TableLookup lookup : domainQL.getJooqTables().values())
        {
            if (lookup.getTable().getName().equals(name))
            {
                return lookup.getPojoType().getSimpleName();
            }
        }

        throw new FilterTransformationException("Could not find table '" + name + "'");
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
            operands.get(0),
            condition
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
        return holder.invoke(field, transformRestOfOperands(resolver, fieldResolver, operands, condition));
    }


    private Object[] transformRestOfOperands(
        CachedFilterContextResolver resolver,
        FieldResolver fieldResolver,
        List<Map<String, Object>> operands,
        Map<String, Object> parent
    )
    {
        Object[] array = new Object[operands.size() - 1];
        for (int i = 1; i < operands.size(); i++)
        {
            array[i - 1] = transformRecursive(resolver, fieldResolver, operands.get(i), parent);
        }
        return array;
    }


    private List<? extends Condition> transformOperands(
        CachedFilterContextResolver resolver, FieldResolver fieldResolver,
        List<Map<String, Object>> operands,
        Map<String, Object> parent
    )
    {
        List<Condition> list = new ArrayList<>(operands.size());

        for (Map<String, Object> operand : operands)
        {
            final Object value = transformRecursive(
                resolver,
                fieldResolver,
                operand,
                parent
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
            throw new AutomatonException("Could not find method with name '" + name + "' and " + numArgs + " Field " +
                "parameters");
        }
    }
}
