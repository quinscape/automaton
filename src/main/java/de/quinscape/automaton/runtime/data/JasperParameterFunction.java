package de.quinscape.automaton.runtime.data;

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
    public Object evaluate(String name, List<GenericScalar> args)
    {
        if (args.size() != 1 || !(args.get(0).getValue() instanceof String))
        {
            throw new ComputedValueException("JasperParameterFunction accepts exactly 1 String parameter: " + JSONUtil.DEFAULT_GENERATOR.forValue(args));
        }
        final String parameterName = (String) args.get(0).getValue();
        return DSL.field("$P{" + parameterName + "}");
    }
}
