package de.quinscape.automaton.runtime.scalar;

import de.quinscape.domainql.annotation.GraphQLScalar;
import graphql.schema.CoercingParseValueException;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Wraps the actual node data for usage in GraphQL.
 */
@GraphQLScalar
public final class FieldExpressionScalar
    extends FilterDSLScalar
{
    final static Pattern ORDER_BY_RE = Pattern.compile("^!?[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$");

    public FieldExpressionScalar()
    {
        this(null);
    }

    public FieldExpressionScalar(Map<String, Object> root)
    {
        super(root);
    }

    public static FieldExpressionScalar forFieldExpression(String fieldExpr)
    {
        if (!ORDER_BY_RE.matcher(fieldExpr).matches())
        {
            throw new CoercingParseValueException("Invalid field expression: " + fieldExpr);
        }

        String nameExpr = fieldExpr;
        if (nameExpr.startsWith("!"))
        {
            return new FieldExpressionScalar(
                ConditionBuilder.operation(
                    "desc",
                    Collections.singletonList(
                        ConditionBuilder.field(nameExpr.substring(1))
                    )
                )
            );
        }
        else
        {
            return new FieldExpressionScalar( ConditionBuilder.field(nameExpr));
        }
    }
}
