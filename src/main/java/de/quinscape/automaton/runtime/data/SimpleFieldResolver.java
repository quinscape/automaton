package de.quinscape.automaton.runtime.data;


import org.jooq.Field;
import org.jooq.impl.DSL;

/**
 * Simple field resolver that resolves field references as-is. The names are split at '.' boundaries to be converted
 * into a qualified name. 
 */
public class SimpleFieldResolver
    implements FieldResolver
{
    @Override
    public Field<?> resolveField(String fieldName)
    {
        final String[] qualifiedName = fieldName.split("\\.");
        return DSL.field(
            DSL.name(
                qualifiedName
            )
        );
    }
}
