package de.quinscape.automaton.runtime.scalar;

import de.quinscape.automaton.runtime.AutomatonException;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.schema.DomainQLAware;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class ConditionCoercing
    implements graphql.schema.Coercing<ConditionScalar, Map<String, Object>>, DomainQLAware
{


    private final static Logger log = LoggerFactory.getLogger(ConditionCoercing.class);

    private DomainQL domainQL;


    public ConditionCoercing()
    {

    }


    @Override
    public Map<String, Object> serialize(Object result) throws CoercingSerializeException
    {
        if (!(result instanceof ConditionScalar))
        {
            throw new IllegalArgumentException(result + " is not a ConditionScalar");
        }

        try
        {
            return convert(
                ((ConditionScalar) result).getRoot(),
                true
            );
        }
        catch(RuntimeException e)
        {
            throw new CoercingParseValueException(e);
        }
    }

    @Override
    public ConditionScalar parseValue(Object input) throws CoercingParseValueException
    {
        if (!(input instanceof Map))
        {
            throw new CoercingParseValueException(
                "Cannot coerce " + input + " to ConditionScalar, must be nested map structure (See " +
                ConditionBuilder.class.getName() +
                ")"
            );
        }

        try
        {
            final Map<String, Object> data = convert(
                (Map<String, Object>) input,
                false
            );
            return new ConditionScalar(data);
        }
        catch(RuntimeException e)
        {
            throw new CoercingParseValueException(e);
        }
    }


    private Map<String, Object> convert(Map<String, Object> input, boolean serialize)
    {
        if (input == null)
        {
            return null;
        }
        
        final String type = ConditionBuilder.getType(input);
        final JSON gen = JSONUtil.DEFAULT_GENERATOR;
        if (type == null)
        {
            throw new IllegalStateException("Condition node has no type: " + gen.forValue(input));
        }
        Map<String,Object> output;

        final NodeType nodeType = NodeType.valueOf(type.toUpperCase());

        switch (nodeType)
        {
            case FIELD:
                output = ConditionBuilder.field( ConditionBuilder.getName(input));
                break;
            case CONTEXT:
                output = ConditionBuilder.context( ConditionBuilder.getName(input));
                break;
            case VALUE:
            {

                final String scalarTypeName = ConditionBuilder.getScalarType(input);
                final Object value = ConditionBuilder.getValue(input);
                final GraphQLScalarType graphQLType = getScalarType(scalarTypeName);

                final Object converted;
                if (serialize)
                {
                    converted = graphQLType.getCoercing().serialize(value);
                }
                else
                {
                    converted = graphQLType.getCoercing().parseValue(value);
                }

                output = ConditionBuilder.value(scalarTypeName, converted);
                break;
            }
            case VALUES:
            {

                final String scalarTypeName = ConditionBuilder.getScalarType(input);
                final Collection<?> values = ConditionBuilder.getValues(input);
                final GraphQLScalarType graphQLType = getScalarType(scalarTypeName);

                List<Object> convertedValues = new ArrayList<>(values.size());

                for (Object value : values)
                {
                    final Object converted;
                    if (serialize)
                    {
                        converted = graphQLType.getCoercing().serialize(value);
                    }
                    else
                    {
                        converted = graphQLType.getCoercing().parseValue(value);
                    }

                    convertedValues.add(converted);
                }


                output = ConditionBuilder.value(scalarTypeName, convertedValues);
                break;
            }
            case CONDITION:
            {
                List<Map<String, Object>> operands = convertOperands(input, serialize);
                output = ConditionBuilder.condition(ConditionBuilder.getName(input), operands);
                break;
            }
            case COMPONENT:
            {
                final String id = ConditionBuilder.getId(input);
                final Map<String, Object> condition = ConditionBuilder.getCondition(input);

                output = ConditionBuilder.component(id, this.convert(condition, serialize));
                break;
            }
            case OPERATION:
            {
                List<Map<String, Object>> operands = convertOperands(input, serialize);
                output = ConditionBuilder.operation(ConditionBuilder.getName(input), operands);
                break;
            }
            default:
                throw new AutomatonException("Invalid node type: " + type);
        }

        if (log.isDebugEnabled())
        {
            log.info((serialize ? "Serialized" : "Parsed" ) + " {} to {}", gen.dumpObjectFormatted(input), gen.dumpObjectFormatted(output));
        }

        return output;
    }


    private List<Map<String, Object>> convertOperands(Map<String, Object> input, boolean serialize)
    {
        final List<Map<String, Object>> list = ConditionBuilder.getOperands(input);
        List<Map<String, Object>> operands;
        if (list != null)
        {
            operands = new ArrayList<>(list.size());
            for (Map<String, Object> operand : list)
            {
                operands.add(
                    convert(operand, serialize)
                );
            }
        }
        else
        {
            operands = null;
        }
        return operands;
    }


    @Override
    public ConditionScalar parseLiteral(Object input) throws CoercingParseLiteralException
    {
        // XXX: is this possible?
        throw new CoercingParseLiteralException("Cannot coerce ConditionScalar from literal");
    }


    protected GraphQLScalarType getScalarType(String scalarTypeName)
    {
        final GraphQLType type = domainQL.getGraphQLSchema().getType(scalarTypeName);

        if (!(type instanceof GraphQLScalarType))
        {
            throw new IllegalStateException("Type '" + scalarTypeName + "' is not a scalar type: " + type);
        }
        return (GraphQLScalarType) type;
    }


    @Override
    public void setDomainQL(DomainQL domainQL)
    {
        this.domainQL = domainQL;
    }
}

