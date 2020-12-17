package de.quinscape.automaton.runtime.filter;

import de.quinscape.automaton.runtime.AutomatonException;
import de.quinscape.automaton.runtime.filter.impl.AddFilter;
import de.quinscape.automaton.runtime.filter.impl.AndFilter;
import de.quinscape.automaton.runtime.filter.impl.BetweenFilter;
import de.quinscape.automaton.runtime.filter.impl.BetweenSymmetricFilter;
import de.quinscape.automaton.runtime.filter.impl.BitAndFilter;
import de.quinscape.automaton.runtime.filter.impl.BitNandFilter;
import de.quinscape.automaton.runtime.filter.impl.BitNorFilter;
import de.quinscape.automaton.runtime.filter.impl.BitNotFilter;
import de.quinscape.automaton.runtime.filter.impl.BitOrFilter;
import de.quinscape.automaton.runtime.filter.impl.BitXNorFilter;
import de.quinscape.automaton.runtime.filter.impl.BitXorFilter;
import de.quinscape.automaton.runtime.filter.impl.ContainsFilter;
import de.quinscape.automaton.runtime.filter.impl.ContainsIgnoreCaseFilter;
import de.quinscape.automaton.runtime.filter.impl.ContextValue;
import de.quinscape.automaton.runtime.filter.impl.DivideFilter;
import de.quinscape.automaton.runtime.filter.impl.EndsWithFilter;
import de.quinscape.automaton.runtime.filter.impl.EqualFilter;
import de.quinscape.automaton.runtime.filter.impl.EqualIgnoreCaseFilter;
import de.quinscape.automaton.runtime.filter.impl.FalseFilter;
import de.quinscape.automaton.runtime.filter.impl.FieldValue;
import de.quinscape.automaton.runtime.filter.impl.GreaterOrEqualFilter;
import de.quinscape.automaton.runtime.filter.impl.GreaterThanFilter;
import de.quinscape.automaton.runtime.filter.impl.InFilter;
import de.quinscape.automaton.runtime.filter.impl.IsDistinctFromFilter;
import de.quinscape.automaton.runtime.filter.impl.IsFalseFilter;
import de.quinscape.automaton.runtime.filter.impl.IsNotDistinctFromFilter;
import de.quinscape.automaton.runtime.filter.impl.IsNotNullFilter;
import de.quinscape.automaton.runtime.filter.impl.IsNullFilter;
import de.quinscape.automaton.runtime.filter.impl.IsTrueFilter;
import de.quinscape.automaton.runtime.filter.impl.LessOrEqualFilter;
import de.quinscape.automaton.runtime.filter.impl.LessThanFilter;
import de.quinscape.automaton.runtime.filter.impl.LikeRegexFilter;
import de.quinscape.automaton.runtime.filter.impl.LiteralValue;
import de.quinscape.automaton.runtime.filter.impl.LowerFilter;
import de.quinscape.automaton.runtime.filter.impl.UpperFilter;
import de.quinscape.automaton.runtime.filter.impl.ModuloFilter;
import de.quinscape.automaton.runtime.filter.impl.MultiplyFilter;
import de.quinscape.automaton.runtime.filter.impl.NotBetweenFilter;
import de.quinscape.automaton.runtime.filter.impl.NotBetweenSymmetricFilter;
import de.quinscape.automaton.runtime.filter.impl.NotContainsFilter;
import de.quinscape.automaton.runtime.filter.impl.NotContainsIgnoreCaseFilter;
import de.quinscape.automaton.runtime.filter.impl.NotEqualFilter;
import de.quinscape.automaton.runtime.filter.impl.NotEqualIgnoreCaseFilter;
import de.quinscape.automaton.runtime.filter.impl.NotFilter;
import de.quinscape.automaton.runtime.filter.impl.NotLikeRegexFilter;
import de.quinscape.automaton.runtime.filter.impl.OrFilter;
import de.quinscape.automaton.runtime.filter.impl.PowerFilter;
import de.quinscape.automaton.runtime.filter.impl.ShlFilter;
import de.quinscape.automaton.runtime.filter.impl.ShrFilter;
import de.quinscape.automaton.runtime.filter.impl.StartsWithFilter;
import de.quinscape.automaton.runtime.filter.impl.SubtractFilter;
import de.quinscape.automaton.runtime.filter.impl.TrueFilter;
import de.quinscape.automaton.runtime.filter.impl.UnaryMinusFilter;
import de.quinscape.automaton.runtime.filter.impl.UnaryPlusFilter;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;
import de.quinscape.automaton.runtime.scalar.NodeType;
import de.quinscape.domainql.DomainQL;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transforms the FilterDSL condition nodes / maps into Filter instance graph to perform in-memory filtering of java objects.
 *
 */
public class JavaFilterTransformer
{
    private final Map<String, Class<? extends ConfigurableFilter>> filters;

    public JavaFilterTransformer()
    {
        this(Collections.emptyMap());
    }

    public JavaFilterTransformer(Map<String, Class<? extends ConfigurableFilter>> extraFilters)
    {
        filters = createDefaultFilters();
        filters.putAll(extraFilters);
    }

    private static Map<String, Class<? extends ConfigurableFilter>> createDefaultFilters()
    {
        final Map<String, Class<? extends ConfigurableFilter>> filters = new HashMap<>();

        filters.put("greaterOrEqual", GreaterOrEqualFilter.class );
        filters.put("lessOrEqual", LessOrEqualFilter.class );
        filters.put("lt", LessThanFilter.class );
        filters.put("notBetweenSymmetric", NotBetweenSymmetricFilter.class );
        filters.put("notEqualIgnoreCase", NotEqualIgnoreCaseFilter.class );
        filters.put("betweenSymmetric", BetweenSymmetricFilter.class );
        filters.put("lessThan", LessThanFilter.class );
        filters.put("equalIgnoreCase", EqualIgnoreCaseFilter.class );
        filters.put("isDistinctFrom", IsDistinctFromFilter.class );
        filters.put("between", BetweenFilter.class );
        filters.put("ge", GreaterOrEqualFilter.class );
        filters.put("greaterThan", GreaterThanFilter.class );
        filters.put("isNotNull", IsNotNullFilter.class );
        filters.put("notLikeRegex", NotLikeRegexFilter.class );
        filters.put("notBetween", NotBetweenFilter.class );
        filters.put("notEqual", NotEqualFilter.class );
        filters.put("isFalse", IsFalseFilter.class );
        filters.put("containsIgnoreCase", ContainsIgnoreCaseFilter.class );
        filters.put("eq", EqualFilter.class );
        filters.put("gt", GreaterThanFilter.class );
        filters.put("equal", EqualFilter.class );
        filters.put("likeRegex", LikeRegexFilter.class );
        filters.put("isTrue", IsTrueFilter.class );
        filters.put("contains", ContainsFilter.class );
        filters.put("notContainsIgnoreCase", NotContainsIgnoreCaseFilter.class );
        filters.put("notContains", NotContainsFilter.class );
        filters.put("ne", NotEqualFilter.class );
        filters.put("isNull", IsNullFilter.class );
        filters.put("endsWith", EndsWithFilter.class );
        filters.put("le", LessOrEqualFilter.class );
        filters.put("isNotDistinctFrom", IsNotDistinctFromFilter.class );
        filters.put("startsWith", StartsWithFilter.class );
        filters.put("in", InFilter.class );
        filters.put("bitNand", BitNandFilter.class );
        filters.put("mod", ModuloFilter.class );
        filters.put("div", DivideFilter.class );
        filters.put("neg", UnaryMinusFilter.class );
        filters.put("rem", ModuloFilter.class );
        filters.put("add", AddFilter.class );
        filters.put("subtract", SubtractFilter.class );
        filters.put("plus", AddFilter.class );
        filters.put("bitAnd", BitAndFilter.class );
        filters.put("bitXor", BitXorFilter.class );
        filters.put("shl", ShlFilter.class );
        filters.put("unaryMinus", UnaryMinusFilter.class );
        filters.put("bitNor", BitNorFilter.class );
        filters.put("shr", ShrFilter.class );
        filters.put("modulo", ModuloFilter.class );
        filters.put("bitXNor", BitXNorFilter.class );
        filters.put("bitNot", BitNotFilter.class );
        filters.put("sub", SubtractFilter.class );
        filters.put("minus", SubtractFilter.class );
        filters.put("mul", MultiplyFilter.class );
        filters.put("bitOr", BitOrFilter.class );
        filters.put("times", MultiplyFilter.class );
        filters.put("pow", PowerFilter.class );
        filters.put("divide", DivideFilter.class );
        filters.put("power", PowerFilter.class );
        filters.put("multiply", MultiplyFilter.class );
        filters.put("unaryPlus", UnaryPlusFilter.class );
        filters.put("lower", LowerFilter.class );
        filters.put("upper", UpperFilter.class );
        return filters;
    }


    public Filter transform(Map<String,Object> condition)
    {
        return transformInternal(condition);
    }


    private Filter transformInternal(Map<String, Object> condition)
    {

        if (condition == null)
        {
            return null;
        }

        final NodeType nodeType = NodeType.forName(
            ConditionBuilder.getType(condition)
        );

        switch (nodeType)
        {
            case COMPONENT:
            {
                final Map<String, Object> kid = ConditionBuilder.getCondition(condition);
                return transformInternal(kid);
            }
            case CONDITION:
            {
                final String name = ConditionBuilder.getName(condition);
                final List<Map<String, Object>> operands = ConditionBuilder.getOperands(condition);

                if (name.equals("and"))
                {
                    return new AndFilter(
                        transformOperands(operands)
                    );
                }
                else if (name.equals("or"))
                {
                    return new OrFilter(
                        transformOperands(operands)
                    );
                }
                else if (name.equals("not"))
                {
                    return new NotFilter(
                        transformOperands(operands).get(0)
                    );
                }
                else if (name.equals("true"))
                {
                    return TrueFilter.INSTANCE;
                }
                else if (name.equals("false"))
                {
                    return FalseFilter.INSTANCE;
                }
                else
                {
                    return transformByName(condition);
                }
            }
            case FIELD:
            {
                final String name = ConditionBuilder.getName(condition);
                return new FieldValue(name);
            }
            case VALUE:
            {
                final String scalarType = ConditionBuilder.getScalarType(condition);
                final Object value = ConditionBuilder.getValue(condition);
                return new LiteralValue(scalarType, value);
            }
            case VALUES:
            {
                final String scalarType = ConditionBuilder.getScalarType(condition);
                final Collection<?> values = ConditionBuilder.getValues(condition);
                return new LiteralValue(scalarType, values);
            }
            case OPERATION:
            {
                return transformByName(condition);
            }

            case CONTEXT:
            {
                final String name = ConditionBuilder.getName(condition);
                return new ContextValue(name);
            }
            default:
                throw new AutomatonException("Unhandled node type: " + nodeType);
        }
    }


    private static GraphQLScalarType findScalarType(DomainQL domainQL, String scalarType)
    {
        for (GraphQLScalarType type : domainQL.getTypeRegistry().getScalarTypes())
        {
            if (type.getName().equals(scalarType))
            {
                return type;
            }
        }

        throw new IllegalStateException("Could not find scalar type '" + scalarType + "'");
    }


    private Filter transformByName(
        Map<String, Object> condition
    )
    {
        final String name = ConditionBuilder.getName(condition);

        try
        {
            final Filter filter = filters.get(name).newInstance();
            if (filter instanceof ConfigurableFilter)
            {
                ((ConfigurableFilter) filter).configure(node -> transformInternal(node), condition);
            }
            return filter;
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new AutomatonException("Error creating filter node '" + name,e);
        }

    }


    private List<? extends Filter> transformOperands(
        List<Map<String, Object>> operands
    )
    {
        List<Filter> list = new ArrayList<>(operands.size());
        for (Map<String, Object> operand : operands)
        {
            final Filter value = transformInternal(operand);
            list.add(value);
        }
        return list;
    }


    /**
     * Deserializes the "Value" and "Values" node values within a map graph from the JSON format to the Java Format. The
     * complete map graph copied / changed immutably.
     *
     * @param domainQL      domainQL instance containing all necessary scalar types
     * @param filter        filter expression as map graph.
     *
     * @return  filter expression as map graph with Java scalar values in value and values nodes.
     */
    public static Map<String,Object> deserialize(DomainQL domainQL, Map<String, Object> filter)
    {
        return deserialize(domainQL, filter, true);
    }


    /**
     * Deserializes the scalar values contained in value and values nodes of a filter expression. This is necessary when
     * Java scalar value types are not the same as the JSON level scalar type (e.g. Date or Timestamp).
     *
     * @param domainQL      domainQL instance containing all necessary scalar types
     * @param filter        filter expression as map graph.
     * @param copy          if <code>true</code>, do immutable changes with copying, otherwise change the values in-place
     *
     * @return  filter expression as map graph with Java scalar values in value and values nodes.
     */
    public static Map<String, Object> deserialize(DomainQL domainQL, Map<String, Object> filter, boolean copy)
    {

        if (filter == null)
        {
            return null;
        }

        final NodeType nodeType = NodeType.forName(
            ConditionBuilder.getType(filter)
        );



        switch (nodeType)
        {
            case COMPONENT:
            {
                final Map<String, Object> out = copy ? new HashMap<>(filter) : filter;
                final Map<String, Object> kid = ConditionBuilder.getCondition(filter);
                ConditionBuilder.setCondition(out, deserialize(domainQL, kid, copy));
                return out;
            }
            case OPERATION:
            case CONDITION:
            {
                final Map<String, Object> out = copy ? new HashMap<>(filter) : filter;
                final String name = ConditionBuilder.getName(filter);
                final List<Map<String, Object>> operands = ConditionBuilder.getOperands(filter);

                if (copy)
                {
                    final List<Map<String, Object>> convertedOperands = new ArrayList<>(operands.size());

                    for (Map<String, Object> operand : operands)
                    {
                        convertedOperands.add(
                            deserialize(domainQL, operand, true)
                        );
                    }

                    ConditionBuilder.setOperands(
                        out,
                        convertedOperands
                    );
                }
                else
                {
                    for (Map<String, Object> operand : operands)
                    {
                        deserialize(domainQL, operand, false);
                    }
                }
                return out;
            }
            case FIELD:
            case CONTEXT:
            {
                // filter and context can't contain values, never change
                return filter;
            }
            case VALUE:
            {
                final Map<String, Object> out = copy ? new HashMap<>(filter) : filter;
                final String scalarType = ConditionBuilder.getScalarType(filter);
                final GraphQLScalarType type = findScalarType(domainQL, scalarType);
                final Object value = ConditionBuilder.getValue(filter);
                final Object converted = value != null ? type.getCoercing().parseValue(value) : null;

                ConditionBuilder.setValue(out, converted);
                return out;
            }
            case VALUES:
            {
                final Map<String, Object> out = copy ? new HashMap<>(filter) : filter;

                final String scalarType = ConditionBuilder.getScalarType(filter);

                final GraphQLType gqlType = domainQL.getGraphQLSchema().getType(scalarType);

                if (!(gqlType instanceof GraphQLScalarType))
                {
                    throw new IllegalStateException("'" + scalarType + "' is not a known scalar type");
                }
                final Coercing coercing = ((GraphQLScalarType) gqlType).getCoercing();
                final Collection<?> values = ConditionBuilder.getValues(filter);

                List<Object> converted = new ArrayList<>(values.size());
                for (Object value : values)
                {
                    converted.add(
                        value != null ? coercing.parseValue(value) : null
                    );
                }
                ConditionBuilder.setValues(out, converted);
                return out;
            }
            default:
                throw new AutomatonException("Unhandled node type: " + nodeType);
        }
    }


}
