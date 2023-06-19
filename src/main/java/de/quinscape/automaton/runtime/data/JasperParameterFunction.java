package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.model.domain.DecimalPrecision;
import de.quinscape.domainql.generic.GenericScalar;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.jooq.impl.DSL;

import java.util.List;

/**
 * Evaluates a "param" computed value to a Jasper compatible parameter expression
 */
public class JasperParameterFunction
    implements ComputedValue
{
    @Override
    public Object evaluate(ComputedValueContext ctx)
    {
        final List<GenericScalar> args = ctx.getArgs();

        if (args.size() != 1 || !(args.get(0).getValue() instanceof String)) {
            throw new ComputedValueException("JasperParameterFunction accepts exactly 1 String parameter: " + JSONUtil.DEFAULT_GENERATOR.forValue(args));
        }
        final String parameterName = (String) args.get(0).getValue();

        final ComputedValueTypeContext typeContext = ctx.getTypeContext();
        if (typeContext != null) {
            String scalarType = typeContext.scalarType();
            switch (scalarType)
            {
                case "Timestamp":
                    return DSL.field("jasper_param_safe_to_timestamp('$P{" + parameterName + "}')");
                case "Date":
                    return DSL.field("jasper_param_safe_to_date('$P{" + parameterName + "}')");
                case "Int":
                    return DSL.field("jasper_param_safe_to_integer('$P{" + parameterName + "}')");
                case "Boolean":
                    return DSL.field("jasper_param_safe_to_boolean('$P{" + parameterName + "}')");
                case "BigDecimal":
                    String formatString = buildNumericFormatString((DecimalPrecision) typeContext.detail());
                    return DSL.field("jasper_param_safe_to_number('$P{" + parameterName + "}', '" + formatString + "')");
            }
        }

        return DSL.val("$P{" + parameterName + "}");
    }

    private String buildNumericFormatString(DecimalPrecision dp) {

        return "9".repeat(Math.max(0, dp.getPrecision())) +
            "D" +
            "9".repeat(Math.max(0, dp.getScale()));
    }
}
