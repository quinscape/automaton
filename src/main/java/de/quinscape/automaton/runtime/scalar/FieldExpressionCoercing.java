package de.quinscape.automaton.runtime.scalar;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class FieldExpressionCoercing
    implements Coercing<FieldExpressionScalar, Object>
{
    private final static Logger log = LoggerFactory.getLogger(FieldExpressionCoercing.class);



    public FieldExpressionCoercing()
    {
    }


    @Override
    public Object serialize(Object result) throws CoercingSerializeException
    {
        if (result instanceof String)
        {
            return result;
        }

        if (!(result instanceof FieldExpressionScalar))
        {
            throw new CoercingParseValueException(result + " is not a FieldExpressionScalar");
        }

        final Map<String, Object> root = ((FieldExpressionScalar) result).getRoot();
        final String type = ConditionBuilder.getType(root);
        if (type.equals(NodeType.FIELD.getName()))
        {
            return ConditionBuilder.getName(root);
        }
        else if (type.equals(NodeType.OPERATION.getName()))
        {
            final String name = ConditionBuilder.getName(root);

            final Map<String, Object> operand = ConditionBuilder.getOperands(root).get(0);

            if (ConditionBuilder.getType(operand).equals(NodeType.FIELD.getName()))
            {
                if (name.equals("asc"))
                {
                    return ConditionBuilder.getName(
                        operand
                    );
                }
                else if (name.equals("desc"))
                {
                    return "!" + ConditionBuilder.getName(
                        operand
                    );
                }
            }

        }

        try
        {
            return root;
        }
        catch (RuntimeException e)
        {
            throw new CoercingParseValueException(e);
        }
    }


    @Override
    public FieldExpressionScalar parseValue(Object input) throws CoercingParseValueException
    {
        if (input instanceof String)
        {
            return FieldExpressionScalar.forFieldExpression((String) input);
        }

        if (!(input instanceof Map))
        {
            throw new CoercingParseValueException(
                "Cannot coerce " + input + " to FieldExpressionScalar, must be nested map structure (See " +
                    ConditionBuilder.class.getName() +
                    ") or string expression"
            );
        }

        try
        {
            return new FieldExpressionScalar((Map<String, Object>) input);
        }
        catch (RuntimeException e)
        {
            throw new CoercingParseValueException(e);
        }
    }


    @Override
    public FieldExpressionScalar parseLiteral(Object input) throws CoercingParseLiteralException
    {
        // XXX: is this possible?
        throw new CoercingParseLiteralException("Cannot coerce GenericScalarType from literal");
    }
}

