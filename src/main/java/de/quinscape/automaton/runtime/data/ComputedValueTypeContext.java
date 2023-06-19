package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.model.domain.DecimalPrecision;

/**
 * Encapsulates the type information for the other operand
 *
 * @param typeName              type name. Will be null of the other operand is the result of an operator
 * @param fieldName             field name. Will be null of the other operand is the result of an operator
 * @param scalarType            Scalar type name
 * @param detail                Detail object for scalar type (current only used by BigDecimal in which case it will be
 *                              an instance of {@link DecimalPrecision}
 */
public record ComputedValueTypeContext(String typeName, String fieldName, String scalarType, Object detail)
{
}
