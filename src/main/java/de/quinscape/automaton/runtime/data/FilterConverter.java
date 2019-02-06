package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.model.data.FieldFilterDefinition;
import org.jooq.Condition;
import org.springframework.stereotype.Component;

/**
 * Implemented by classes providing named conditions for the DefaultFilterTransformer
 */
@Component
public interface FilterConverter
{
    /**
     * Creates a JOOQ condition from the single field filter definition.
     * @param fieldFilterDefinition   filter definition
     * @return JOOQ condition
     */
    <T extends Condition> T createCondition(FieldFilterDefinition fieldFilterDefinition);
}
