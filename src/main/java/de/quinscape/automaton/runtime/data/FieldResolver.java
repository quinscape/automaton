package de.quinscape.automaton.runtime.data;

import org.jooq.Field;

/**
 * Resolves a symbolic / named field reference within a Filter DSL graph to an actual JOOQ field within a query.
 *
 * The semantics of field names are implementation dependent. 
 */
public interface FieldResolver
{
    /**
     * Resolves a symbolic/named field reference to an actual JOOQ field within a query.
     *
     * @param fieldName     name of the field
     * @return
     */
    Field<?> resolveField(String fieldName);
}
