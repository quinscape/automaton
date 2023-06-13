package de.quinscape.automaton.runtime.data;

import de.quinscape.domainql.generic.GenericScalar;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.List;

/**
 * Evaluates a "param" computed value to a Jasper compatible parameter expression
 */
public class JasperParameterFunction
        implements ComputedValue {
    @Override
    public Object evaluate(String name, List<GenericScalar> args, Field<?> conditionLeftSideField) {
        if (args.size() != 1 || !(args.get(0).getValue() instanceof String)) {
            throw new ComputedValueException("JasperParameterFunction accepts exactly 1 String parameter: " + JSONUtil.DEFAULT_GENERATOR.forValue(args));
        }
        final String parameterName = (String) args.get(0).getValue();

        if (conditionLeftSideField != null) {
            DataType<?> dataType = conditionLeftSideField.getDataType();
            if (dataType.isTimestamp()) {
                return DSL.field("jasper_param_safe_to_timestamp('$P{" + parameterName + "}')");
            } else if (dataType.isDate()) {
                return DSL.field("jasper_param_safe_to_date('$P{" + parameterName + "}')");
            } else if (dataType.isInteger()) {
                return DSL.field("jasper_param_safe_to_integer('$P{" + parameterName + "}')");
            } else if (dataType.getType() == Boolean.class) {
                return DSL.field("jasper_param_safe_to_boolean('$P{" + parameterName + "}')");
            } else if (dataType.isNumeric()) {
                String formatString = buildNumericFormatString(dataType);
                return DSL.field("jasper_param_safe_to_number('$P{" + parameterName + "}', '" + formatString + "')");
            }
        }

        return DSL.val("$P{" + parameterName + "}");
    }

    private String buildNumericFormatString(DataType<?> dataType) {
        StringBuilder formatStringBuilder = new StringBuilder();

        for (int i = 0; i < dataType.precision(); i++) {
            formatStringBuilder.append("9");
        }
        if (dataType.hasScale()) {
            formatStringBuilder.append("D");
            for (int i = 0; i < dataType.scale(); i++) {
                formatStringBuilder.append("9");
            }
        }
        return formatStringBuilder.toString();
    }
}
