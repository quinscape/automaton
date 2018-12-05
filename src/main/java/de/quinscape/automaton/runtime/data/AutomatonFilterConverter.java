package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.model.data.FieldFilterDefinition;
import org.jooq.Condition;
import org.jooq.Field;

public interface AutomatonFilterConverter
{
    /**
     *
     * @param scalarType    Name of the scalar type for this field. 
     * @param field         JOOQ field
     * @param fieldFilterDefinition   Filter
     * @return
     */
    Condition createCondition(String scalarType, Field<?> field, FieldFilterDefinition fieldFilterDefinition);
}
